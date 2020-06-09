package co.jp.dreamteam.bactrackbydreamteam2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import static android.content.ContentValues.TAG;

public class TestActivity extends Activity {

    //表示するテクスチャー
    private TextureView mTextureView = null;
    //カメラデバイス
    private CameraDevice mCameraDevice = null;
    //CameraCaptureSession用変数
    CameraCaptureSession mCaptureSession = null;
    //CaptureRequest用変数
    CaptureRequest mPreviewRequest = null;
    CaptureRequest.Builder mPreviewRequestBuilder = null;

    final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    private void openCamera() {
        //CameraManagerの取得
        Context context = getApplicationContext();
        CameraManager mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        // インカメラのIDを取得
        String mCameraId = null;
        try {
            //利用可能なカメラIDのリストを取得
            String[] cameraIdList = new String[0];

            cameraIdList = mCameraManager.getCameraIdList();

            //用途に合ったカメラIDを設定
            for (String cameraId : cameraIdList) {
                //カメラの向き(インカメラ/アウトカメラ)は以下のロジックで判別可能です。(インカメラを使用します)
                CameraCharacteristics characteristics = null;
                characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                switch (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                    case CameraCharacteristics.LENS_FACING_FRONT:
                        //インカメラ
                        mCameraId = cameraId;
                        break;
                    case CameraCharacteristics.LENS_FACING_BACK:
                        //アウトカメラ
                        break;
                    default:
                }
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                // permissionが許可されていません
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    // 許可ダイアログで今後表示しないにチェックされていない場合
                }

                // permissionを許可してほしい理由の表示など

                // 許可ダイアログの表示
                // MY_PERMISSIONS_REQUEST_READ_CONTACTSはアプリ内で独自定義したrequestCodeの値
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);

                return;
            }
            mCameraManager.openCamera(mCameraId, mStateCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // パーミッションが必要な処理
                    openCamera();
                } else {
                    // パーミッションが得られなかった時
                    // 処理を中断する・エラーメッセージを出す・アプリケーションを終了する等
                    finish();
                }
            }
        }
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;
        }

    };

    private void createCameraPreviewSession()
    {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();

            //バッファのサイズをプレビューサイズに設定(画面サイズ等適当な値を入れる)
            //ここではQVGA
            texture.setDefaultBufferSize(320, 240);

            Surface surface = new Surface(texture);

            // CaptureRequestを生成
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // CameraCaptureSessionを生成
            mCameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            //Session設定完了(準備完了)時、プレビュー表示を開始
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // カメラプレビューを開始(TextureViewにカメラの画像が表示され続ける)
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest, null, null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            //Session設定失敗時
                            Log.e(TAG,"error");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        this.findViewById(R.id.buttonCamera).setOnClickListener(btnCameraClicked);
        this.findViewById(R.id.buttonSend).setOnClickListener(btnSendClicked);

        mTextureView = (TextureView) findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                // 先ほどのカメラを開く部分をメソッド化した
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

    }

    View.OnClickListener btnCameraClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            try {
                //カメラプレビューを中断させる
                mCaptureSession.stopRepeating();
                if (mTextureView.isAvailable()) {
                    //TextureViewに表示されている画像をBitmapで取得
                    Bitmap bmp = mTextureView.getBitmap();

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    String bitmapStr = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);

                    SharedPreferences pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(getString(R.string.PREF_KEY_PHOTO), bitmapStr);
                    editor.apply();
                }

                // カメラプレビューを再開
                //mCaptureSession.setRepeatingRequest(mPreviewRequest, null, null);

            } catch(Exception e){
                e.printStackTrace();
            }
        }
    };

    /**
     * 送信エラー
     */
    private void errorSending() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(TestActivity.this);

                // ダイアログの設定
                alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
                alertDialog.setMessage(getString(R.string.TEXT_SEND_ERROR));

                // OK(肯定的な)ボタンの設定
                alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // OKボタン押下時の処理
                    }
                });

                alertDialog.show();
            }
        });
    }

    /**
     * HTTPコネクションエラー
     */
    private void errorHttp(final String response)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(TestActivity.this);

                // ダイアログの設定
                alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
                alertDialog.setMessage(response);

                // OK(肯定的な)ボタンの設定
                alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // OKボタン押下時の処理
                    }
                });

                alertDialog.show();
            }
        });
    }

    View.OnClickListener btnSendClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            try {

                // 非同期タスクを定義
                HttpPostTask task = new HttpPostTask(TestActivity.this,
                        getString(R.string.HTTP_URL) + "/" + getString(R.string.HTTP_WRITE_ALCOHOL_VALUE),

                        // タスク完了時に呼ばれるUIのハンドラ
                        new HttpPostHandler() {

                            @Override
                            public void onPostCompleted(String response) {
                                // 受信結果をUIに表示
                                if (response.startsWith(getString(R.string.HTTP_RESPONSE_OK))) {
                                    // 画面移動
                                    Intent intent = new Intent(getApplication(), FinishActivity.class);
                                    startActivity(intent);
                                } else {
                                    errorSending();
                                }
                            }

                            @Override
                            public void onPostFailed(String response) {
                                errorHttp(response);
                            }
                        });

                SharedPreferences pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

                String strCompany = pref.getString(getString(R.string.PREF_KEY_COMPANY), "");
                String strAddress = pref.getString(getString(R.string.PREF_KEY_ADDRESS), "");
                String strLat = pref.getString(getString(R.string.PREF_KEY_LAT), "");
                String strLong = pref.getString(getString(R.string.PREF_KEY_LON), "");
                String strDriver = pref.getString(getString(R.string.PREF_KEY_DRIVER), "");
                String strCarNo = pref.getString(getString(R.string.PREF_KEY_CAR_NO), "");
                String strAlcoholValue = pref.getString(getString(R.string.PREF_KEY_MEASUREMENT), "");
                // 画像取得
                byte[] photoByte = null;
                String strBitmap = pref.getString(getString(R.string.PREF_KEY_PHOTO), "");
                if (!strBitmap.equals(""))
                {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    photoByte = Base64.decode(strBitmap, Base64.DEFAULT);
                }

                strCompany = "COM";
                strDriver = "100";
                strCarNo = "1234";
                strAddress = "静岡県 浜松市中区 新津町 656";
                strLat = "34.72102";
                strLong = "137.7417";
                strAlcoholValue = "0.5";

                // パラメータセット
                task.setHttp_multipart(true);
                task.addPostParam(getString(R.string.HTTP_PARAM_COMPANY_CODE), strCompany);
                task.addPostParam(getString(R.string.HTTP_PARAM_DRIVER_CODE), strDriver);
                task.addPostParam(getString(R.string.HTTP_PARAM_CAR_NO), strCarNo);
                task.addPostParam(getString(R.string.HTTP_PARAM_LOCATION_NAME), strAddress);
                task.addPostParam(getString(R.string.HTTP_PARAM_LOCATION_LAT), strLat);
                task.addPostParam(getString(R.string.HTTP_PARAM_LOCATION_LONG), strLong);
                task.addPostParam(getString(R.string.HTTP_PARAM_ALCOHOL_VALUE), strAlcoholValue);
                task.addPostParamJpeg(getString(R.string.HTTP_PARAM_PHOTO), photoByte);
                task.addPostParam(getString(R.string.HTTP_PARAM_APP_PROG), "Android");
                task.addPostParam(getString(R.string.HTTP_PARAM_APP_ID), "Android");

                // タスクを開始
                task.execute();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    };

}