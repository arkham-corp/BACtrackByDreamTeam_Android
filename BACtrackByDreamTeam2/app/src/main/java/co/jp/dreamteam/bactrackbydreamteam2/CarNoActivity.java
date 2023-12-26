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

public class CarNoActivity extends Activity {

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    String company_code;
    String driver_code;
    EditText editTextCarNo;
    Button car_no_btnDecision;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_no);

        this.editTextCarNo = this.findViewById(R.id.car_no_editTextCarNo);

        car_no_btnDecision = this.findViewById(R.id.car_no_btnDecision);
        car_no_btnDecision.setOnClickListener(btnDecisionClicked);

        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

        editTextCarNo.setText(pref.getString(getString(R.string.PREF_KEY_CAR_NO), ""));

        company_code = pref.getString(getString(R.string.PREF_KEY_COMPANY), "");
        driver_code = pref.getString(getString(R.string.PREF_KEY_DRIVER), "");
    }

    OnClickListener btnDecisionClicked = v -> exec_post();

    @Override
    protected void onStart() {
        super.onStart();
        car_no_btnDecision.setEnabled(true);
    }

    /**
     * 車番エラー
     */
    private void errorDriverNotFound() {
        runOnUiThread(() -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(CarNoActivity.this);

            // ダイアログの設定
            alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
            alertDialog.setMessage(getString(R.string.TEXT_ERR_CAR_NO_NOT_FOUND));

            // OK(肯定的な)ボタンの設定
            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                // OKボタン押下時の処理
                car_no_btnDecision.setEnabled(true);
            });

            alertDialog.show();
        });
    }

    /**
     * HTTPコネクションエラー
     */
    private void errorHttp() {
        runOnUiThread(() -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(CarNoActivity.this);
            alertDialog.setMessage("インターネット接続時にエラーが発生しました。\nこのまま続けて測定は可能です");
            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                editor = pref.edit();
                editor.putString(getString(R.string.PREF_KEY_STATUS), "1");
                editor.commit();
                car_no_btnDecision.setEnabled(true);
            });
            alertDialog.show();
        });
    }

    // POST通信を実行（AsyncTaskによる非同期処理を使うバージョン）
    private void exec_post() {
        car_no_btnDecision.setEnabled(false);

        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);
        String status = pref.getString(getString(R.string.PREF_KEY_STATUS), "0");
        if (status.equals("0")) {

            // 接続先
            String strHttpUrl = pref.getString(getString(R.string.PREF_KEY_HTTP_URL), "");
            String strVerifyHostname = pref.getString(getString(R.string.PREF_KEY_VERIFY_HOSTNAME), "");
            // 非同期タスクを定義
            @SuppressLint("HandlerLeak") HttpPostTask task = new HttpPostTask(
                    this,
                    strHttpUrl + getString(R.string.HTTP_CAR_NO_CHECK),

                    // タスク完了時に呼ばれるUIのハンドラ
                    new HttpPostHandler() {

                        @Override
                        public void onPostCompleted(String response) {
                            // 受信結果をUIに表示
                            if (response.startsWith(getString(R.string.HTTP_RESPONSE_OK))) {
                                // 値保存
                                editor = pref.edit();
                                editor.putString(getString(R.string.PREF_KEY_CAR_NO), editTextCarNo.getText().toString());
                                editor.commit();

                                // 画面移動
                                Intent intent = new Intent(getApplication(), InspectionActivity.class);
                                startActivity(intent);
                            } else {
                                errorDriverNotFound();
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
            task.addPostParam(getString(R.string.HTTP_PARAM_COMPANY_CODE), company_code);
            task.addPostParam(getString(R.string.HTTP_PARAM_DRIVER_CODE), driver_code);
            task.addPostParam(getString(R.string.HTTP_PARAM_CAR_NO), editTextCarNo.getText().toString());

            // タスクを開始
            task.execute();

        } else {
            if(editTextCarNo.getText().toString().equals(""))
            {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(CarNoActivity.this);
                alertDialog.setMessage(getString(R.string.TEXT_ERROR_CAR_NUMBER));
                alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> car_no_btnDecision.setEnabled(false));
                alertDialog.show();
            } else {
                editor = pref.edit();
                editor.putString(getString(R.string.PREF_KEY_CAR_NO), editTextCarNo.getText().toString());
                editor.commit();

                Intent intent = new Intent(getApplication(), InspectionActivity.class);
                startActivity(intent);
            }
        }
    }
}
