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
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Base64;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import io.realm.Realm;

public class TransmissionContentActivity extends FragmentActivity {

    private SharedPreferences pref;
    private Realm realm;
    long transmission_id;
    private TextureView transmission_content_photo = null;
    EditText transmission_content_txtDriver;
    EditText transmission_content_txtCarNumber;

    TextView transmission_content_lblInspectionTime;
    TextView transmission_content_lblInspectionYmd;
    TextView transmission_content_lblInspectionHm;
    TextView transmission_content_lblInspectionLat;
    TextView transmission_content_lblInspectionLong;
    TextView transmission_content_lblLocation;
    TextView transmission_content_lblDrivingDiv;
    TextView transmission_content_lblAlcoholValue;
    TextView transmission_content_lblBackTrackId;
    TextView transmission_content_lblUseNumber;
    TextView transmission_content_lblSendFlg;
    int viewWidth = 0;
    int viewHeight = 0;

    Button transmission_btnSend;
    RealmLocalDataAlcoholResult alcoholResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transmission_content);

        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

        realm = Realm.getDefaultInstance();

        transmission_id = getIntent().getLongExtra("id", 0);

        // 測定日
        transmission_content_lblInspectionYmd = this.findViewById(R.id.transmission_content_lblInspectionYmd);

        // 測定時間
        transmission_content_lblInspectionHm = this.findViewById(R.id.transmission_content_lblInspectionHm);

        // 運転手
        transmission_content_txtDriver = this.findViewById(R.id.transmission_content_txtDriver);

        // 車番
        transmission_content_txtCarNumber = this.findViewById(R.id.transmission_content_txtCarNumber);

        // 測定場所
        transmission_content_lblLocation = this.findViewById(R.id.transmission_content_lblLocation);

        // 乗務区分
        transmission_content_lblDrivingDiv = this.findViewById(R.id.transmission_content_lblDrivingDiv);

        // 測定値
        transmission_content_lblAlcoholValue = this.findViewById(R.id.transmission_content_lblAlcoholValue);

        // 測定機器ID
        transmission_content_lblBackTrackId = this.findViewById(R.id.transmission_content_lblBackTrackId);

        // 使用回数
        transmission_content_lblUseNumber = this.findViewById(R.id.transmission_content_lblUseNumber);

        // 送信フラグ
        transmission_content_lblSendFlg = this.findViewById(R.id.transmission_content_lblSendFlg);

        // 測定日時
        transmission_content_lblInspectionTime = this.findViewById(R.id.transmission_content_lblInspectionTime);

        // 経度
        transmission_content_lblInspectionLat = this.findViewById(R.id.transmission_content_lblInspectionLat);

        // 緯度
        transmission_content_lblInspectionLong = this.findViewById(R.id.transmission_content_lblInspectionLong);

        // 写真
        transmission_content_photo = this.findViewById(R.id.transmission_content_photo);

        // 送信ボタン
        transmission_btnSend = this.findViewById(R.id.transmission_content_btnSend);
        transmission_btnSend.setOnClickListener(btnSendClicked);
        transmission_btnSend.setEnabled(false);

        SetReadOnly(transmission_content_txtDriver);
        SetReadOnly(transmission_content_txtCarNumber);

        // 値取得
        alcoholResult = readRecord();
        if (alcoholResult != null) {
            // 値セット
            transmission_content_lblInspectionTime.setText(alcoholResult.getInspection_time());
            if (!alcoholResult.getInspection_ymd().equals("")) {
                String strDate = alcoholResult.getInspection_ymd().substring(0, 4) +
                        "/" + alcoholResult.getInspection_ymd().substring(4, 6) +
                        "/" + alcoholResult.getInspection_ymd().substring(6, 8);
                transmission_content_lblInspectionYmd.setText(strDate);
            }
            if (!alcoholResult.getInspection_hm().equals("")) {
                String strTime = alcoholResult.getInspection_hm().substring(0, 2) +
                        ":" + alcoholResult.getInspection_hm().substring(2, 4);
                transmission_content_lblInspectionHm.setText(strTime);
            }
            transmission_content_txtDriver.setText(alcoholResult.getDriver_code());
            transmission_content_txtCarNumber.setText(alcoholResult.getCar_number());
            transmission_content_lblLocation.setText(alcoholResult.getLocation_name());
            transmission_content_lblInspectionLat.setText(alcoholResult.getLocation_lat());
            transmission_content_lblInspectionLong.setText(alcoholResult.getLocation_long());

            if (alcoholResult.getDriving_div().equals("")) {
                transmission_content_lblDrivingDiv.setText("");
            } else if (alcoholResult.getDriving_div().equals("0")) {
                transmission_content_lblDrivingDiv.setText(getString(R.string.TEXT_DRIVING_DIV_0));
            } else  {
                transmission_content_lblDrivingDiv.setText(getString(R.string.TEXT_DRIVING_DIV_1));
            }
            transmission_content_lblAlcoholValue.setText(alcoholResult.getAlcohol_value());

            transmission_content_lblBackTrackId.setText(alcoholResult.getBacktrack_id());
            transmission_content_lblUseNumber.setText(alcoholResult.getUse_number());
            if (alcoholResult.getSend_flg().equals("0")) {
                transmission_content_lblSendFlg.setText(getString(R.string.TEXT_SEND_FLG_0));
                transmission_btnSend.setEnabled(true);
            } else if (alcoholResult.getSend_flg().equals("1")) {
                transmission_content_lblSendFlg.setText(getString(R.string.TEXT_SEND_FLG_1));
                transmission_btnSend.setEnabled(true);
            } else  {
                transmission_content_lblSendFlg.setText(getString(R.string.TEXT_SEND_FLG_2));
            }

            if (transmission_content_photo != null) {
                transmission_content_photo.setSurfaceTextureListener(surfaceTextureListener);
            }

        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(TransmissionContentActivity.this);
            alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
            alertDialog.setMessage(getString(R.string.TEXT_TRANS_ERROR));
            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                transmission_btnSend.setEnabled(false);
            });
            alertDialog.show();
        }
    }
    private final TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height)  {
            // TextureViewが利用可能になると呼ばれます
            try {
                viewWidth  = width;
                viewHeight = height;
                drawBitmapOnSurfaceTexture(surface);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            // TextureViewが破棄されると呼ばれます
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            try {
                drawBitmapOnSurfaceTexture(surface);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    };
    private void drawBitmapOnSurfaceTexture(SurfaceTexture surfaceTexture) throws UnsupportedEncodingException {
        Canvas canvas = transmission_content_photo.lockCanvas();
        if (canvas != null) {

            String bitmapStr = alcoholResult.getPhoto_file();
            byte[] decodedByte = Base64.decode(bitmapStr, 0);
//            InputStream is = new ByteArrayInputStream(decodedByte);
//            Bitmap bitmap = BitmapFactory.decodeStream(is);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedByte,0,decodedByte.length);

            // イメージ描画
            float bitmapWidth = bitmap.getWidth();
            float bitmapHeight = bitmap.getHeight();
            float scaleX = viewWidth / bitmapWidth;
            float scaleY = viewHeight / bitmapHeight;

            Matrix matrix = new Matrix();
            matrix.postScale(scaleX, scaleY);

            // 描画位置を中央に調整
            float translateX = (viewWidth - bitmapWidth * scaleX) / 2f;
            float translateY = (viewHeight - bitmapHeight * scaleY) / 2f;
            matrix.postTranslate(translateX, translateY);

            Paint paint = new Paint();
            paint.setFilterBitmap(true);

            canvas.drawBitmap(bitmap, matrix, paint);
            transmission_content_photo.unlockCanvasAndPost(canvas);
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(TransmissionContentActivity.this);
        if (capabilities != null) {
            alertDialog.setMessage(getString(R.string.TEXT_QUESTION_SEND));
            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_YES), (dialog, which) -> {
//                checkCompany();
                exec_post();
            });
            alertDialog.setNegativeButton(getString(R.string.ALERT_BTN_NO), (dialog, which) -> {
            });
        } else {
            //インターネットに接続していません
            alertDialog.setTitle(getString(R.string.ALERT_TITLE_SESSION_ERROR));
            alertDialog.setMessage(getString(R.string.ALERT_TITLE_INTERNET_ERROR));
            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
            });
        }
        alertDialog.show();
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
                strHttpUrl + getString(R.string.HTTP_COMPANY_CHECK),

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
        task.setVerify_hostname(strVerifyHostname);
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
        task.addPostParam(getString(R.string.HTTP_PARAM_DRIVER_CODE), String.valueOf(transmission_content_txtDriver.getText()));

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
        task.addPostParam(getString(R.string.HTTP_PARAM_DRIVER_CODE), String.valueOf(transmission_content_txtDriver.getText()));
        task.addPostParam(getString(R.string.HTTP_PARAM_CAR_NO), String.valueOf(transmission_content_txtCarNumber.getText()));

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
                            alertDialog.setTitle(getString(R.string.TEXT_SENDING_ERROR));
                            alertDialog.setMessage(getString(R.string.TEXT_FINISH1));
                            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                            });
                            alertDialog.show();
                            SaveData("2");
                            transmission_btnSend.setEnabled(false);
                        } else if (response.startsWith(getString(R.string.HTTP_RESPONSE_KEY_NG))) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(TransmissionContentActivity.this);
                            alertDialog.setTitle(getString(R.string.TEXT_SENDING_ERROR));
                            alertDialog.setMessage(getString(R.string.TEXT_FINISH_DUPLICATE));
                            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                            });
                            alertDialog.show();
                            SaveData("1");
                            transmission_btnSend.setEnabled(false);
                        } else if (response.startsWith(getString(R.string.HTTP_RESPONSE_DRIVER_NG))) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(TransmissionContentActivity.this);
                            alertDialog.setTitle(getString(R.string.TEXT_SENDING_ERROR));
                            alertDialog.setMessage(getString(R.string.TEXT_ERR_DRIVER_NOT_FOUND));
                            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                            });
                            alertDialog.show();
                            SaveData("1");
                            transmission_btnSend.setEnabled(false);
                        } else if (response.startsWith(getString(R.string.HTTP_RESPONSE_CAR_NUMBER_NG))) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(TransmissionContentActivity.this);
                            alertDialog.setTitle(getString(R.string.TEXT_SENDING_ERROR));
                            alertDialog.setMessage(getString(R.string.TEXT_ERR_CAR_NO_NOT_FOUND));
                            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                            });
                            alertDialog.show();
                            SaveData("1");
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
        String strAddress = String.valueOf(transmission_content_lblLocation.getText());
        String strLat = String.valueOf(transmission_content_lblInspectionLat.getText());
        String strLong = String.valueOf(transmission_content_lblInspectionLong.getText());
        String strDriver = String.valueOf(transmission_content_txtDriver.getText());
        String strCarNo = String.valueOf(transmission_content_txtCarNumber.getText());
        String strAlcoholValue = String.valueOf(transmission_content_lblAlcoholValue.getText());
        String strBacTrackId = String.valueOf(transmission_content_lblBackTrackId.getText());
        String strUseCount = String.valueOf(transmission_content_lblUseNumber.getText());
        String strDrivingDiv = "0";
        if(String.valueOf(transmission_content_lblDrivingDiv.getText()).equals(getString(R.string.TEXT_DRIVING_DIV_1)))
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
    private void SaveData(String flg) {
        realm.beginTransaction();
        RealmLocalDataAlcoholResult alcoholResult = readRecord();
        alcoholResult.setSend_flg(flg);
        realm.insertOrUpdate(alcoholResult);
        realm.commitTransaction();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

}
