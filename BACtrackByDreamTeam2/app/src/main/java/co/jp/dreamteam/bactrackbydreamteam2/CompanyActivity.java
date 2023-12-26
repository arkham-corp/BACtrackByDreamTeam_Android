package co.jp.dreamteam.bactrackbydreamteam2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class CompanyActivity extends Activity {
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    EditText editTextCompany;
    Button company_btnDecision;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company);

        // 設定ファイル取得
        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

        // 設定初期化
        editor = pref.edit();
        editor.putString(getString(R.string.PREF_KEY_HTTP_URL), "");
        editor.putString(getString(R.string.PREF_KEY_VERIFY_HOSTNAME), "");
        //editor.putString(getString(R.string.PREF_KEY_STATUS), "0");
        editor.commit();

        this.editTextCompany = this.findViewById(R.id.company_editTextCompany);

        company_btnDecision = this.findViewById(R.id.company_btnDecision);
        company_btnDecision.setOnClickListener(btnDecisionClicked);

        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

        editTextCompany.setText(pref.getString(getString(R.string.PREF_KEY_COMPANY), ""));
    }

    OnClickListener btnDecisionClicked = v -> exec_post();

    @Override
    protected void onStart() {
        super.onStart();
        company_btnDecision.setEnabled(true);
    }

    /**
     * 会社エラー
     */
    private void errorCompanyNotFound() {
        runOnUiThread(() -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(CompanyActivity.this);

            // ダイアログの設定
            alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
            alertDialog.setMessage(getString(R.string.TEXT_ERR_COMPANY_NOT_FOUND));

            // OK(肯定的な)ボタンの設定
            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                // OKボタン押下時の処理
                company_btnDecision.setEnabled(true);
            });

            alertDialog.show();
        });
    }

    /**
     * HTTPコネクションエラー
     */
    private void errorHttp() {
        runOnUiThread(() -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(CompanyActivity.this);
            alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
            alertDialog.setMessage("インターネット接続時にエラーが発生しました。\nこのまま続けて測定は可能です");
            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                editor = pref.edit();
                editor.putString(getString(R.string.PREF_KEY_STATUS), "1");
                editor.commit();
                company_btnDecision.setEnabled(true);
            });
            alertDialog.show();
        });
    }

    // POST通信を実行（AsyncTaskによる非同期処理を使うバージョン）
    private void exec_post() {
        company_btnDecision.setEnabled(false);

        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);
        String status = pref.getString(getString(R.string.PREF_KEY_STATUS), "0");
        if (status.equals("0")) {

            String test_flg = getString(R.string.TEST_FLG);
            String host_name;
            String http_url;

            if (test_flg.equals("1")) {
                host_name = getString(R.string.HTTP_TEST_HOST_NAME1);
                http_url = "http://" + getString(R.string.HTTP_TEST_HOST_NAME1) + getString(R.string.HTTP_GET_API_URL);
            } else if (test_flg.equals("2")) {
                host_name = getString(R.string.HTTP_TEST_HOST_NAME2);
                http_url = "http://" + getString(R.string.HTTP_TEST_HOST_NAME2) + "/com" + getString(R.string.HTTP_GET_API_URL);
            } else {
                host_name = getString(R.string.HTTP_HOST_NAME);
                http_url = "https://" + getString(R.string.HTTP_HOST_NAME) + getString(R.string.HTTP_GET_API_URL);
            }

            // 非同期タスクを定義
            @SuppressLint("HandlerLeak") HttpPostTask task = new HttpPostTask(
                    this,
                    http_url,

                    // タスク完了時に呼ばれるUIのハンドラ
                    new HttpPostHandler() {

                        @Override
                        public void onPostCompleted(String response) {
                            try {
                                JSONObject json = new JSONObject(response);

                                // 受信結果をUIに表示
                                if (json.getString("status").equals("true")) {
                                    // 値保存
                                    String str_url = json.getString("data");
                                    URL url = new URL(str_url);
                                    String host_name = url.getHost();

                                    editor = pref.edit();
                                    editor.putString(getString(R.string.PREF_KEY_HTTP_URL), str_url);
                                    editor.putString(getString(R.string.PREF_KEY_VERIFY_HOSTNAME), host_name);
                                    editor.commit();

                                    exec_post2();
                                } else {
                                    errorCompanyNotFound();
                                }
                            } catch (JSONException | MalformedURLException e) {
                                errorHttp();
                            }
                        }

                        @Override
                        public void onPostFailed(String response) {
                            errorHttp();
                        }
                    }
            );

            // パラメータセット
            task.setVerify_hostname(host_name);
            task.addPostParam(getString(R.string.HTTP_PARAM_COMPANY_CODE), editTextCompany.getText().toString());

            // タスクを開始
            task.execute();

        } else {
            if(editTextCompany.getText().toString().equals(""))
            {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(CompanyActivity.this);
                alertDialog.setMessage(getString(R.string.TEXT_ERROR_COMPANY_CODE));
                alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> company_btnDecision.setEnabled(false));
                alertDialog.show();
            } else {
                editor = pref.edit();
                editor.putString(getString(R.string.PREF_KEY_HTTP_URL), "");
                editor.putString(getString(R.string.PREF_KEY_VERIFY_HOSTNAME), "");
                editor.putString(getString(R.string.PREF_KEY_ALCOHOL_VALUE_DIV), "1");//0:血中1:呼気2:両方
                editor.putString(getString(R.string.PREF_KEY_COMPANY), editTextCompany.getText().toString());
                editor.putString(getString(R.string.PREF_KEY_MENU_DRIVING_REPORT_ENABLED), "0");
                editor.putString(getString(R.string.PREF_KEY_MENU_SEND_LIST_ENABLED), "0");
                editor.putString(getString(R.string.PREF_KEY_MENU_REMINDER_ENABLED), "0");
                editor.commit();

                // GPS画面移動
                Intent intent = new Intent(getApplication(), GPSActivity.class);
                startActivity(intent);
            }
}
    }

    private void exec_post2() {

        // 接続先
        String strHttpUrl = pref.getString(getString(R.string.PREF_KEY_HTTP_URL), "");
        String strVerifyHostname = pref.getString(getString(R.string.PREF_KEY_VERIFY_HOSTNAME), "");
        // 非同期タスクを定義
        @SuppressLint("HandlerLeak") HttpPostTask task = new HttpPostTask(
                this,
                strHttpUrl + getString(R.string.HTTP_COMPANY_CHECK),

                // タスク完了時に呼ばれるUIのハンドラ
                new HttpPostHandler() {

                    @Override
                    public void onPostCompleted(String response) {
                        // 受信結果をUIに表示
                        if (!response.equals("")) {
                            // 値保存
                            editor = pref.edit();
                            editor.putString(getString(R.string.PREF_KEY_ALCOHOL_VALUE_DIV), response);
                            editor.putString(getString(R.string.PREF_KEY_COMPANY), editTextCompany.getText().toString());
                            editor.commit();
                            exec_post3();
                        } else {
                            errorCompanyNotFound();
                        }
                    }

                    @Override
                    public void onPostFailed(String response) {
                        errorHttp();
                    }
                }
        );

        // パラメータセット
        task.setVerify_hostname(strVerifyHostname);
        task.addPostParam(getString(R.string.HTTP_PARAM_COMPANY_CODE), editTextCompany.getText().toString());

        // タスクを開始
        task.execute();
    }

    private void exec_post3() {

        String test_flg = getString(R.string.TEST_FLG);
        String host_name;
        String http_url;

        if (test_flg.equals("1")) {
            host_name = getString(R.string.HTTP_TEST_HOST_NAME1);
            http_url = "http://" + getString(R.string.HTTP_TEST_HOST_NAME1) + getString(R.string.HTTP_GET_MENU_CONTROL);
        } else if (test_flg.equals("2")) {
            host_name = getString(R.string.HTTP_TEST_HOST_NAME2);
            http_url = "http://" + getString(R.string.HTTP_TEST_HOST_NAME2) + "/com" + getString(R.string.HTTP_GET_MENU_CONTROL);
        } else {
            host_name = getString(R.string.HTTP_HOST_NAME);
            http_url = "https://" + getString(R.string.HTTP_HOST_NAME) + getString(R.string.HTTP_GET_MENU_CONTROL);
        }

        // 非同期タスクを定義
        @SuppressLint("HandlerLeak") HttpPostTask task = new HttpPostTask(
                this,
                http_url,

                // タスク完了時に呼ばれるUIのハンドラ
                new HttpPostHandler() {

                    @Override
                    public void onPostCompleted(String response) {
                        try {
                            JSONObject json = new JSONObject(response);

                            // 受信結果をUIに表示
                            if (json.getString("status").equals("true")) {
                                // 値保存
                                String data_list = json.getString("data");
                                String[] values = data_list.split(",");

                                String app_driving_report_enabled = "0";
                                String app_send_list_enabled = "0";
                                String app_reminder_enabled = "0";

                                if (values.length == 3) {
                                    app_driving_report_enabled = values[0].trim(); // 1つ目の値
                                    app_send_list_enabled = values[1].trim(); // 2つ目の値
                                    app_reminder_enabled = values[2].trim(); // 3つ目の値
                                }

                                editor = pref.edit();
                                editor.putString(getString(R.string.PREF_KEY_MENU_DRIVING_REPORT_ENABLED), app_driving_report_enabled);
                                editor.putString(getString(R.string.PREF_KEY_MENU_SEND_LIST_ENABLED), app_send_list_enabled);
                                editor.putString(getString(R.string.PREF_KEY_MENU_REMINDER_ENABLED), app_reminder_enabled);
                                editor.commit();

                                Intent intent;
                                if (app_driving_report_enabled.equals("1") ||
                                        app_send_list_enabled.equals("1") ||
                                        app_reminder_enabled.equals("1")
                                )
                                {
                                    // メニュー画面移動
                                    intent = new Intent(getApplication(), MenuActivity.class);
                                } else {
                                    // GPS画面移動
                                    intent = new Intent(getApplication(), GPSActivity.class);
                                }
                                startActivity(intent);

                            } else {
                                errorCompanyNotFound();
                            }
                        } catch (JSONException e) {
                            errorHttp();
                        }
                    }

                    @Override
                    public void onPostFailed(String response) {
                        errorHttp();
                    }
                }
        );

        // パラメータセット
        task.setVerify_hostname(host_name);
        task.addPostParam(getString(R.string.HTTP_PARAM_COMPANY_CODE), editTextCompany.getText().toString());

        // タスクを開始
        task.execute();
    }

}
