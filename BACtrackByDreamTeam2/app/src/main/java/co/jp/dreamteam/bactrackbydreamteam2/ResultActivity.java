package co.jp.dreamteam.bactrackbydreamteam2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    public static final double ALCOHOL_REMOVAL_RATE = 0.015;

    SharedPreferences pref;

    Button btnFinish;
    TextView textViewSendingMessage;
    TextView textViewMessage;
    TextView textViewTitle;
    TextView textViewDrivingDiv;
    TextView textViewResultValue;
    TextView textViewResultRemainValue;

    int errorCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

        textViewSendingMessage = this.findViewById(R.id.result_textViewSendMessage);
        textViewMessage = this.findViewById(R.id.result_textViewResultMessage);
        textViewTitle = this.findViewById(R.id.result_textViewResultTitle);
        textViewDrivingDiv = this.findViewById(R.id.result_textViewDrivingDiv);
        textViewResultValue = this.findViewById(R.id.result_textViewResultValue);
        textViewResultRemainValue = this.findViewById(R.id.result_textViewRemainValue);

        // 測定値取得
        String strMeasurement = pref.getString(getString(R.string.PREF_KEY_MEASUREMENT), "");
        double alcoholValue = Double.parseDouble(strMeasurement);
        double alcoholValueBreath = Double.parseDouble(strMeasurement) * 5;
        String strAlcoholValue = String.format(Locale.JAPAN, "%.2f", alcoholValue);
        String strAlcoholValueBreath = String.format(Locale.JAPAN, "%.2f", alcoholValueBreath);

        // 異常判定
        if (Double.parseDouble(strAlcoholValue) != 0) {
            textViewMessage.setText(getString(R.string.TEXT_RESULT_WARNING));
            textViewMessage.setTextColor(Color.RED);
        }

        // 乗務区分取得
        String strDrivingDiv = pref.getString(getString(R.string.PREF_KEY_DRIVING_DIV), "");

        if (strDrivingDiv.equals("1")) {
            textViewDrivingDiv.setText(getString(R.string.TEXT_DRIVING_DIV_1));
        } else {
            textViewDrivingDiv.setText(getString(R.string.TEXT_DRIVING_DIV_0));
        }

        // 表示区分取得
        String strAlcoholValueDiv = pref.getString(getString(R.string.PREF_KEY_ALCOHOL_VALUE_DIV), "");

        if (strAlcoholValueDiv.equals("1")) {
            // 呼気を画面に表示
            textViewTitle.setText(getString(R.string.TEXT_RESULT_TITLE_BREATH));
            textViewResultValue.setText(String.format("%smg/L", strAlcoholValueBreath));
        } else if (strAlcoholValueDiv.equals("2")) {
            // 呼気を画面に表示
            textViewTitle.setText(getString(R.string.TEXT_RESULT_TITLE_BREATH_2));
            textViewResultValue.setText(String.format("%smg/L", strAlcoholValueBreath));
        } else {
            // 血中を画面に表示
            textViewTitle.setText(getString(R.string.TEXT_RESULT_TITLE_BLOOD));
            textViewResultValue.setText(String.format("%s%%", strAlcoholValue));
        }

        // 計測結果から残留目安時間を表示
        textViewResultRemainValue.setText(String.format("%s です。", getRemainTime(alcoholValue)));

        // 終了ボタン
        this.btnFinish = this.findViewById(R.id.result_btnFinish);
        btnFinish.setOnClickListener(btnFinishClicked);

        // 自動送信
        exec_post();
    }

    View.OnClickListener btnFinishClicked = v -> {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        moveTaskToBack(true);
    };

    /**
     * 計測値から残留時間を計算、アルコール消化時刻を返却
     *
     * @param alcoholValue 計測値
     * @return アルコール消化時刻
     */
    private String getRemainTime(double alcoholValue) {

        // 残留時間=計測結果/0.015(分計算は計算結果の小数に60を掛け、小数以下を四捨五入)
        double remain = alcoholValue / ALCOHOL_REMOVAL_RATE;
        double remain_h = Math.floor(alcoholValue / ALCOHOL_REMOVAL_RATE);
        double remain_m = Math.round((remain - remain_h) * 60);

        // 現在時刻に残留時間を足す
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.HOUR_OF_DAY, (int) remain_h);
        cal.add(java.util.Calendar.MINUTE, (int) remain_m);

        int h_for = cal.get(java.util.Calendar.HOUR_OF_DAY);
        int m_for = cal.get(java.util.Calendar.MINUTE);

        String h_str = String.format(Locale.JAPAN, "%02d", h_for);
        String m_str = String.format(Locale.JAPAN, "%02d", m_for);

        return h_str.substring(h_str.length() - 2) + ":" + m_str.substring(m_str.length() - 2);
    }

    /**
     * 送信エラー
     */
    private void errorSending() {
        runOnUiThread(() -> {

            errorCount += 1;

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ResultActivity.this);
            alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));

            if (errorCount < 3) {
                // ダイアログの設定
                alertDialog.setMessage(getString(R.string.TEXT_SEND_ERROR));

                // OK(肯定的な)ボタンの設定
                alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                    // OKボタン押下時の処理
                    exec_post();
                });
            } else {
                // ダイアログの設定
                alertDialog.setMessage(getString(R.string.TEXT_SEND_ERROR_LAST));

                // OK(肯定的な)ボタンの設定
                alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                    textViewSendingMessage.setText(getString(R.string.TEXT_SEND_FINISH_ERROR));
                    textViewSendingMessage.setTextColor(Color.RED);
                    // OKボタン押下時の処理
                    btnFinish.setVisibility(View.VISIBLE);
                });
            }

            alertDialog.show();
        });
    }

    /**
     * HTTPコネクションエラー
     */
    private void errorHttp(final String response) {
        runOnUiThread(() -> {
            errorCount += 1;

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ResultActivity.this);
            alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));

            if (errorCount < 3) {
                // ダイアログの設定
                alertDialog.setMessage(getString(R.string.TEXT_SEND_ERROR));

                // OK(肯定的な)ボタンの設定
                alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                    // OKボタン押下時の処理
                    exec_post();
                });
            } else {
                // ダイアログの設定
                alertDialog.setMessage(getString(R.string.TEXT_SEND_ERROR_LAST));

                // OK(肯定的な)ボタンの設定
                alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                    textViewSendingMessage.setText(getString(R.string.TEXT_SEND_FINISH_ERROR));
                    textViewSendingMessage.setTextColor(Color.RED);
                    // OKボタン押下時の処理
                    btnFinish.setVisibility(View.VISIBLE);
                });
            }

            alertDialog.show();
        });
    }

    // POST通信を実行（AsyncTaskによる非同期処理を使うバージョン）
    public void exec_post() {
        // 接続先
        String strHttpUrl = pref.getString(getString(R.string.PREF_KEY_HTTP_URL), "");
        String strVerifyHostname = pref.getString(getString(R.string.PREF_KEY_VERIFY_HOSTNAME), "");
        // 非同期タスクを定義
        HttpPostTask task = new HttpPostTask(this,
                strHttpUrl + getString(R.string.HTTP_WRITE_ALCOHOL_VALUE),

                // タスク完了時に呼ばれるUIのハンドラ
                new HttpPostHandler() {

                    @Override
                    public void onPostCompleted(String response) {
                        // 受信結果をUIに表示
                        if (response.startsWith(getString(R.string.HTTP_RESPONSE_OK))) {
                            textViewSendingMessage.setText(getString(R.string.TEXT_FINISH1));
                            textViewSendingMessage.setTextColor(Color.GREEN);
                            btnFinish.setVisibility(View.VISIBLE);
                        } else if (response.startsWith(getString(R.string.HTTP_RESPONSE_KEY_NG))) {
                            textViewSendingMessage.setText(getString(R.string.TEXT_FINISH_DUPLICATE));
                            textViewSendingMessage.setTextColor(Color.RED);
                            btnFinish.setVisibility(View.VISIBLE);
                        } else {
                            errorSending();
                        }
                    }

                    @Override
                    public void onPostFailed(String response) {
                        errorHttp(response);
                    }
                });

        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

        String strCompany = pref.getString(getString(R.string.PREF_KEY_COMPANY), "");
        String strInspectionTime = pref.getString(getString(R.string.PREF_KEY_INSPECTION_TIME), "");
        String strAddress = pref.getString(getString(R.string.PREF_KEY_ADDRESS), "");
        String strLat = pref.getString(getString(R.string.PREF_KEY_LAT), "");
        String strLong = pref.getString(getString(R.string.PREF_KEY_LON), "");
        String strDriver = pref.getString(getString(R.string.PREF_KEY_DRIVER), "");
        String strCarNo = pref.getString(getString(R.string.PREF_KEY_CAR_NO), "");
        String strAlcoholValue = pref.getString(getString(R.string.PREF_KEY_MEASUREMENT), "");
        String strBacTrackId = pref.getString(getString(R.string.PREF_KEY_BACTRACK_ID), "");
        String strUseCount = pref.getString(getString(R.string.PREF_KEY_BACTRACK_USE_COUNT), "");
        String strDrivingDiv = pref.getString(getString(R.string.PREF_KEY_DRIVING_DIV), "");

        // 画像取得
        byte[] photoByte = null;
        String strBitmap = pref.getString(getString(R.string.PREF_KEY_PHOTO), "");
        if (!strBitmap.equals("")) {
            photoByte = Base64.decode(strBitmap, Base64.DEFAULT);
        }

        // パラメータセット
        task.setVerify_hostname(strVerifyHostname);
        task.setHttp_multipart(true);
        task.addPostParam(getString(R.string.HTTP_PARAM_COMPANY_CODE), strCompany);
        task.addPostParam(getString(R.string.HTTP_PARAM_INSPECTION_TIME), strInspectionTime);
        task.addPostParam(getString(R.string.HTTP_PARAM_DRIVER_CODE), strDriver);
        task.addPostParam(getString(R.string.HTTP_PARAM_CAR_NO), strCarNo);
        task.addPostParam(getString(R.string.HTTP_PARAM_LOCATION_NAME), strAddress);
        task.addPostParam(getString(R.string.HTTP_PARAM_LOCATION_LAT), strLat);
        task.addPostParam(getString(R.string.HTTP_PARAM_LOCATION_LONG), strLong);
        task.addPostParam(getString(R.string.HTTP_PARAM_ALCOHOL_VALUE), strAlcoholValue);
        task.addPostParamJpeg(getString(R.string.HTTP_PARAM_PHOTO), photoByte);
        task.addPostParam(getString(R.string.HTTP_PARAM_BACTRACK_ID), strBacTrackId);
        task.addPostParam(getString(R.string.HTTP_PARAM_BACTRACK_USE_COUNT), strUseCount);
        task.addPostParam(getString(R.string.HTTP_PARAM_DRIVING_DIV), strDrivingDiv);
        task.addPostParam(getString(R.string.HTTP_PARAM_APP_PROG), "Android");
        task.addPostParam(getString(R.string.HTTP_PARAM_APP_ID), "Android");

        // タスクを開始
        task.execute();
    }

}
