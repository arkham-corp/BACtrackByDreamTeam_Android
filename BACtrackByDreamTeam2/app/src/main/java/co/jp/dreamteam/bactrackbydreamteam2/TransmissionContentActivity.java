package co.jp.dreamteam.bactrackbydreamteam2;

import static android.text.InputType.TYPE_NULL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

import io.realm.Realm;

public class TransmissionContentActivity extends FragmentActivity {

    private SharedPreferences pref;
    private Realm realm;
    int transmission_id;
    EditText transmission_content_txtInspectionYmd;
    EditText transmission_content_txtInspectionHm;
    EditText transmission_content_txtDriver;
    EditText transmission_content_txtCarNumber;
    EditText transmission_content_txtLocation;
    EditText transmission_content_txtDrivingDiv;
    EditText transmission_content_txtAlcoholValue;
    EditText transmission_content_txtBackTrackId;
    EditText transmission_content_txtUseNumber;
    EditText transmission_content_txtSendFlg;

    TextView transmission_content_lblInspectionTime;
    TextView transmission_content_lblInspectionLat;
    TextView transmission_content_lblInspectionLong;
    ImageView transmission_content_photo;

    Button transmission_btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transmission_content);

        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

        realm = Realm.getDefaultInstance();

        transmission_id = getIntent().getIntExtra("id", 0);

        // 測定日
        transmission_content_txtInspectionYmd = this.findViewById(R.id.transmission_content_txtInspectionYmd);

        // 測定時間
        transmission_content_txtInspectionHm = this.findViewById(R.id.transmission_content_txtInspectionHm);

        // 運転手
        transmission_content_txtDriver = this.findViewById(R.id.transmission_content_txtDriver);

        // 車番
        transmission_content_txtCarNumber = this.findViewById(R.id.transmission_content_txtCarNumber);

        // 測定場所
        transmission_content_txtLocation = this.findViewById(R.id.transmission_content_txtLocation);

        // 乗務区分
        transmission_content_txtDrivingDiv = this.findViewById(R.id.transmission_content_txtDrivingDiv);

        // 測定値
        transmission_content_txtAlcoholValue = this.findViewById(R.id.transmission_content_txtAlcoholValue);

        // 測定機器ID
        transmission_content_txtBackTrackId = this.findViewById(R.id.transmission_content_txtBackTrackId);

        // 使用回数
        transmission_content_txtUseNumber = this.findViewById(R.id.transmission_content_txtUseNumber);

        // 送信フラグ
        transmission_content_txtSendFlg = this.findViewById(R.id.transmission_content_txtSendFlg);

        // 測定日時
        transmission_content_lblInspectionTime = this.findViewById(R.id.transmission_content_lblInspectionTime);
        // 経度
        transmission_content_lblInspectionLat = this.findViewById(R.id.transmission_content_lblInspectionLat);
        // 緯度
        transmission_content_lblInspectionLong = this.findViewById(R.id.transmission_content_lblInspectionLong);

        // 送信ボタン
        transmission_btnSend = this.findViewById(R.id.transmission_content_btnSend);
        transmission_btnSend.setOnClickListener(btnSendClicked);

        SetReadOnly(transmission_content_txtInspectionYmd);
        SetReadOnly(transmission_content_txtInspectionHm);
        SetReadOnly(transmission_content_txtDriver);
        SetReadOnly(transmission_content_txtCarNumber);
        SetReadOnly(transmission_content_txtLocation);
        SetReadOnly(transmission_content_txtDrivingDiv);
        SetReadOnly(transmission_content_txtAlcoholValue);
        SetReadOnly(transmission_content_txtBackTrackId);
        SetReadOnly(transmission_content_txtUseNumber);
        SetReadOnly(transmission_content_txtSendFlg);

        // 値取得
        RealmLocalDataAlcoholResult alcoholResult = readRecord();

        if (alcoholResult == null) {
            transmission_btnSend.setEnabled(false);
        } else {
            // 値セット
            transmission_content_lblInspectionTime.setText(alcoholResult.getInspection_time());
            if (!alcoholResult.getInspection_ymd().equals("")) {
                String strDate = alcoholResult.getInspection_ymd().substring(0, 4) +
                        "/" + alcoholResult.getInspection_ymd().substring(4, 6) +
                        "/" + alcoholResult.getInspection_ymd().substring(6, 8);
                transmission_content_txtInspectionYmd.setText(strDate);
            }
            if (!alcoholResult.getInspection_hm().equals("")) {
                String strTime = alcoholResult.getInspection_hm().substring(0, 2) +
                        ":" + alcoholResult.getInspection_hm().substring(2, 4);
                transmission_content_txtInspectionHm.setText(strTime);
            }
            transmission_content_txtDriver.setText(alcoholResult.getDriver_code());
            transmission_content_txtCarNumber.setText(alcoholResult.getCar_number());
            transmission_content_txtLocation.setText(alcoholResult.getLocation_name());
            transmission_content_lblInspectionLat.setText(alcoholResult.getLocation_lat());
            transmission_content_lblInspectionLong.setText(alcoholResult.getLocation_long());

            if (alcoholResult.getDriving_div().equals("")) {
                transmission_content_txtDrivingDiv.setText("");
            } else if (!alcoholResult.getDriving_div().equals("0")) {
                transmission_content_txtDrivingDiv.setText(getString(R.string.TEXT_DRIVING_DIV_0));
            } else  {
                transmission_content_txtDrivingDiv.setText(getString(R.string.TEXT_DRIVING_DIV_1));
            }
            transmission_content_txtAlcoholValue.setText(alcoholResult.getAlcohol_value());

            transmission_content_txtBackTrackId.setText(alcoholResult.getBacktrack_id());
            transmission_content_txtUseNumber.setText(alcoholResult.getUse_number());
            if (alcoholResult.getSend_flg().equals("0")) {
                transmission_content_txtSendFlg.setText(getString(R.string.TEXT_SEND_FLG_0));
                transmission_btnSend.setEnabled(true);
            } else if (!alcoholResult.getSend_flg().equals("1")) {
                transmission_content_txtSendFlg.setText(getString(R.string.TEXT_SEND_FLG_1));
                transmission_btnSend.setEnabled(true);
            } else  {
                transmission_content_txtSendFlg.setText(getString(R.string.TEXT_SEND_FLG_2));
            }

            transmission_btnSend.setEnabled(!String.valueOf(alcoholResult.getSend_flg()).equals("1"));

            if (!alcoholResult.getPhoto_file().equals("")) {

                String bitmapStr = alcoholResult.getPhoto_file();
                byte[] decodedByte = Base64.decode(bitmapStr, 0);
                Bitmap bmp =  BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
                transmission_content_photo.setImageBitmap(bmp);

            }

        }
    }

    private void SetReadOnly(EditText control) {
        control.setFocusable(false);
        control.setFocusableInTouchMode(false);
        control.setInputType(TYPE_NULL);
    }

    public RealmLocalDataAlcoholResult readRecord() {

        long recordCount = realm.where(RealmLocalDataAlcoholResult.class)
                .equalTo("id", transmission_id)
                .count();

        if (recordCount == 0) {
            return null;
        } else {
            return realm.where(RealmLocalDataAlcoholResult.class)
                .equalTo("id", transmission_id)
                .findFirst();
        }
    }
    View.OnClickListener btnSendClicked = v -> {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
        if (capabilities != null) {
            checkCompany();
        } else {
            //インターネットに接続していません
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(TransmissionContentActivity.this);
            alertDialog.setTitle(getString(R.string.ALERT_TITLE_SESSION_ERROR));
            alertDialog.setMessage(getString(R.string.ALERT_TITLE_INTERNET_ERROR));
            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
            });
            alertDialog.show();
        }
    };

    /**
     * 会社エラー
     */
    private void errorCompanyNotFound() {
        runOnUiThread(() -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(TransmissionContentActivity.this);
            alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
            alertDialog.setMessage(getString(R.string.TEXT_ERR_COMPANY_NOT_FOUND));
            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                transmission_btnSend.setEnabled(false);
            });
            alertDialog.show();
        });
    }

    /**
     * 運転手エラー
     */
    private void errorDriverNotFound() {
        runOnUiThread(() -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(TransmissionContentActivity.this);
            alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
            alertDialog.setMessage(getString(R.string.TEXT_ERR_DRIVER_NOT_FOUND));
            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                transmission_btnSend.setEnabled(false);
            });
            alertDialog.show();
        });
    }


    /**
     * 車番エラー
     */
    private void errorCarNoNotFound() {
        runOnUiThread(() -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(TransmissionContentActivity.this);
            alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
            alertDialog.setMessage(getString(R.string.TEXT_ERR_CAR_NO_NOT_FOUND));
            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                transmission_btnSend.setEnabled(false);
            });
            alertDialog.show();
        });
    }

    /**
     * 送信エラー
     */
    private void errorSending() {
        runOnUiThread(() -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(TransmissionContentActivity.this);
            alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
            alertDialog.setMessage(getString(R.string.TEXT_SEND_ERROR_LAST));
            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                transmission_btnSend.setEnabled(true);
            });
            alertDialog.show();
        });
    }

    /**
     * HTTPコネクションエラー
     */
    private void errorHttp(final String response) {
        runOnUiThread(() -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(TransmissionContentActivity.this);
            alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
            if (response.startsWith("Hostname al-check.com not verified")) {
                alertDialog.setMessage("Https通信のHostnameが不正です");
            } else {
                alertDialog.setMessage(response);
            }
            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                transmission_btnSend.setEnabled(true);
            });
            alertDialog.show();
        });
    }

    // POST通信を実行（AsyncTaskによる非同期処理を使うバージョン）
    private void checkCompany() {

        String strHttpUrl = pref.getString(getString(R.string.PREF_KEY_HTTP_URL), "");
        String strVerifyHostname = pref.getString(getString(R.string.PREF_KEY_VERIFY_HOSTNAME), "");

        // 非同期タスクを定義
        @SuppressLint("HandlerLeak") HttpPostTask task = new HttpPostTask(
                this,
                strHttpUrl + getString(R.string.HTTP_GET_API_URL),

                // タスク完了時に呼ばれるUIのハンドラ
                new HttpPostHandler() {

                    @Override
                    public void onPostCompleted(String response) {
                        // 受信結果をUIに表示
                        if (!response.equals("")) {
                            checkDriver();
                        } else {
                            errorCompanyNotFound();
                        }
                    }

                    @Override
                    public void onPostFailed(String response) {
                        errorHttp(response);
                    }
                }
        );

        // パラメータセット
        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);
        task.addPostParam(getString(R.string.HTTP_PARAM_COMPANY_CODE), pref.getString(getString(R.string.PREF_KEY_COMPANY), ""));

        // タスクを開始
        task.execute();

    }

    private void checkDriver() {

        // 接続先
        String strHttpUrl = pref.getString(getString(R.string.PREF_KEY_HTTP_URL), "");
        String strVerifyHostname = pref.getString(getString(R.string.PREF_KEY_VERIFY_HOSTNAME), "");
        // 非同期タスクを定義
        HttpPostTask task = new HttpPostTask(
                this,
                strHttpUrl + getString(R.string.HTTP_DRIVER_CHECK),

                // タスク完了時に呼ばれるUIのハンドラ
                new HttpPostHandler() {

                    @Override
                    public void onPostCompleted(String response) {
                        // 受信結果をUIに表示
                        if (response.startsWith(getString(R.string.HTTP_RESPONSE_OK))) {
                            checkCarNo();
                        } else {
                            errorDriverNotFound();
                        }
                    }

                    @Override
                    public void onPostFailed(String response) {
                        errorHttp(response);
                    }
                }
        );

        // パラメータセット
        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);
        task.setVerify_hostname(strVerifyHostname);
        task.addPostParam(getString(R.string.HTTP_PARAM_COMPANY_CODE), pref.getString(getString(R.string.PREF_KEY_COMPANY), ""));
        task.addPostParam(getString(R.string.HTTP_PARAM_DRIVER_CODE), pref.getString(getString(R.string.PREF_KEY_DRIVER), ""));

        // タスクを開始
        task.execute();
    }
    private void checkCarNo() {

        // 接続先
        String strHttpUrl = pref.getString(getString(R.string.PREF_KEY_HTTP_URL), "");
        String strVerifyHostname = pref.getString(getString(R.string.PREF_KEY_VERIFY_HOSTNAME), "");
        // 非同期タスクを定義
        HttpPostTask task = new HttpPostTask(
                this,
                strHttpUrl + getString(R.string.HTTP_CAR_NO_CHECK),

                // タスク完了時に呼ばれるUIのハンドラ
                new HttpPostHandler() {

                    @Override
                    public void onPostCompleted(String response) {
                        // 受信結果をUIに表示
                        if (response.startsWith(getString(R.string.HTTP_RESPONSE_OK))) {
                            exec_post();
                        } else {
                            errorCarNoNotFound();
                        }
                    }

                    @Override
                    public void onPostFailed(String response) {
                        errorHttp(response);
                    }
                }
        );

        // パラメータセット
        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);
        task.setVerify_hostname(strVerifyHostname);
        task.addPostParam(getString(R.string.HTTP_PARAM_COMPANY_CODE), pref.getString(getString(R.string.PREF_KEY_COMPANY), ""));
        task.addPostParam(getString(R.string.HTTP_PARAM_DRIVER_CODE), pref.getString(getString(R.string.PREF_KEY_DRIVER), ""));
        task.addPostParam(getString(R.string.HTTP_PARAM_CAR_NO), pref.getString(getString(R.string.PREF_KEY_CAR_NO), ""));

        // タスクを開始
        task.execute();

    }
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
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(TransmissionContentActivity.this);
                            alertDialog.setTitle(getString(R.string.TEXT_FINISH1));
                            alertDialog.setMessage(getString(R.string.TEXT_FINISH1));
                            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                            });
                            alertDialog.show();
                            SaveData();
                            transmission_btnSend.setEnabled(false);
                        } else if (response.startsWith(getString(R.string.HTTP_RESPONSE_KEY_NG))) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(TransmissionContentActivity.this);
                            alertDialog.setTitle(getString(R.string.TEXT_FINISH_DUPLICATE));
                            alertDialog.setMessage(getString(R.string.TEXT_FINISH_DUPLICATE));
                            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                            });
                            alertDialog.show();
                            transmission_btnSend.setEnabled(false);
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
        String strInspectionTime = String.valueOf(transmission_content_lblInspectionTime.getText());
        String strAddress = String.valueOf(transmission_content_txtLocation.getText());
        String strLat = String.valueOf(transmission_content_lblInspectionLat.getText());
        String strLong = String.valueOf(transmission_content_lblInspectionLong.getText());
        String strDriver = String.valueOf(transmission_content_txtDriver.getText());
        String strCarNo = String.valueOf(transmission_content_txtCarNumber.getText());
        String strAlcoholValue = String.valueOf(transmission_content_txtAlcoholValue.getText());
        String strBacTrackId = String.valueOf(transmission_content_txtBackTrackId.getText());
        String strUseCount = String.valueOf(transmission_content_txtUseNumber.getText());
        String strDrivingDiv = "0";
        if(String.valueOf(transmission_content_txtUseNumber.getText()).equals(getString(R.string.TEXT_DRIVING_DIV_1)))
        {
            strDrivingDiv = "1";
        }

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
    private void SaveData() {
        realm.beginTransaction();
        RealmLocalDataAlcoholResult alcoholResult = readRecord();
        alcoholResult.setSend_flg("2");
        realm.insertOrUpdate(alcoholResult);
        realm.commitTransaction();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

}
