package co.jp.dreamteam.bactrackbydreamteam2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Space;

import org.json.JSONException;
import org.json.JSONObject;
import java.net.URL;

public class MenuActivity extends Activity {

    SharedPreferences pref;

    private Space menu_spaceDrivingReport;
    private Space menu_spaceSendList;
    private Space menu_spaceReminder;

    private Button menu_btnInspection;
    private Button menu_btnDrivingReport;
    private Button menu_btnSendList;
    private Button menu_btnReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

        // ボタン有効確認
        String app_roll_call_enabled = pref.getString(getString(R.string.PREF_KEY_MENU_DRIVING_REPORT_ENABLED), "0");
        String app_send_list_enabled = pref.getString(getString(R.string.PREF_KEY_MENU_SEND_LIST_ENABLED), "0");
        String app_reminder_enabled = pref.getString(getString(R.string.PREF_KEY_MENU_REMINDER_ENABLED), "0");

        // スペース参照
        menu_spaceDrivingReport = this.findViewById(R.id.menu_spaceDrivingReport);
        menu_spaceSendList = this.findViewById(R.id.menu_spaceSendList);
        menu_spaceReminder = this.findViewById(R.id.menu_spaceReminder);

        // ボタン参照
        menu_btnInspection = this.findViewById(R.id.menu_btnInspection);
        menu_btnDrivingReport = this.findViewById(R.id.menu_btnDrivingReport);
        menu_btnSendList = this.findViewById(R.id.menu_btnSendList);
        menu_btnReminder = this.findViewById(R.id.menu_btnReminder);

        // ボタン設定
        menu_btnInspection.setOnClickListener(btnInspectionClicked);

        if (app_roll_call_enabled.equals("1")) {
            menu_btnDrivingReport.setOnClickListener(btnDrivingReportClicked);
        } else {
            menu_spaceDrivingReport.setVisibility(View.INVISIBLE);
            menu_btnDrivingReport.setVisibility(View.INVISIBLE);
        }

        if (app_send_list_enabled.equals("1")) {
            menu_btnSendList.setOnClickListener(btnSendListClicked);
        } else {
            menu_spaceSendList.setVisibility(View.INVISIBLE);
            menu_btnSendList.setVisibility(View.INVISIBLE);
        }

        if (app_reminder_enabled.equals("1")) {
            menu_btnReminder.setOnClickListener(btnReminderClicked);
        } else {
            menu_spaceReminder.setVisibility(View.INVISIBLE);
            menu_btnReminder.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        menu_btnInspection.setEnabled(true);
        menu_btnDrivingReport.setEnabled(true);
        menu_btnSendList.setEnabled(true);
        menu_btnReminder.setEnabled(true);
    }

    View.OnClickListener btnInspectionClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            menu_btnInspection.setEnabled(false);
            Intent intent = new Intent(getApplication(), GPSActivity.class);
            startActivity(intent);
        }
    };

    View.OnClickListener btnDrivingReportClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//23231211
            exec_post25();
//20231211
        }
    };

    View.OnClickListener btnSendListClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            menu_btnSendList.setEnabled(false);
            Intent intent = new Intent(getApplication(), SendListActivity.class);
            startActivity(intent);
        }
    };

    View.OnClickListener btnReminderClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            menu_btnReminder.setEnabled(false);
            Intent intent = new Intent(getApplication(), ReminderActivity.class);
            startActivity(intent);
        }
    };
//20231211
    /**
     * HTTPコネクションエラー
     */
    private void errorHttp(final String response) {
        runOnUiThread(() -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MenuActivity.this);

            // ダイアログの設定
            alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
            if (response.startsWith("Hostname al-check.com not verified")) {
                alertDialog.setMessage("Https通信のHostnameが不正です");
            } else {
                if(response.equals("")) {
                    alertDialog.setMessage("[httpGetFreeTitleServlet]が応答しませんでした。");
                } else {
                    alertDialog.setMessage(response);
                }
            }

            // OK(肯定的な)ボタンの設定
            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                // OKボタン押下時の処理
                menu_btnDrivingReport.setEnabled(false);
            });

            alertDialog.show();
        });
    }
    /**
     * 会社エラー
     */
    private void errorCompanyNotFound() {
        runOnUiThread(() -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MenuActivity.this);

            // ダイアログの設定
            alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
            alertDialog.setMessage(getString(R.string.TEXT_ERR_COMPANY_NOT_FOUND));

            // OK(肯定的な)ボタンの設定
            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                // OKボタン押下時の処理
                menu_btnDrivingReport.setEnabled(false);
            });

            alertDialog.show();
        });
    }

    private void exec_post25() {

        // 接続先
        String strHttpUrl = pref.getString(getString(R.string.PREF_KEY_HTTP_URL), "");
        String strVerifyHostname = pref.getString(getString(R.string.PREF_KEY_VERIFY_HOSTNAME), "");

        // 非同期タスクを定義
        @SuppressLint("HandlerLeak") HttpPostTask task = new HttpPostTask(
                this,
                strHttpUrl + getString(R.string.HTTP_GET_FREE_TITLE),

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

                                String title1 = "";
                                String title2 = "";
                                String title3 = "";

                                if (values.length == 3) {
                                    title1 = values[0].trim(); // 1つ目の値
                                    title2 = values[1].trim(); // 2つ目の値
                                    title3 = values[2].trim(); // 3つ目の値
                                }

                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString(getString(R.string.PREF_KEY_FREE_TITLE1), title1);
                                editor.putString(getString(R.string.PREF_KEY_FREE_TITLE2), title2);
                                editor.putString(getString(R.string.PREF_KEY_FREE_TITLE3), title3);
                                editor.commit();

                                menu_btnDrivingReport.setEnabled(false);
                                Intent intent = new Intent(getApplication(), DrivingReportActivity.class);
                                startActivity(intent);

                            } else {
                                errorCompanyNotFound();
                            }
                        } catch (JSONException e) {
                            errorHttp(response);
                        }
                    }

                    @Override
                    public void onPostFailed(String response) {
                        errorHttp(response);
                    }
                }
        );

        // パラメータセット
        task.setVerify_hostname(strVerifyHostname);
        String company_code = pref.getString(getString(R.string.PREF_KEY_COMPANY), "");
        task.addPostParam(getString(R.string.HTTP_PARAM_COMPANY_CODE), company_code);

        // タスクを開始
        task.execute();
    }
//20231211
}
