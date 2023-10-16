package co.jp.dreamteam.bactrackbydreamteam2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import BACtrackAPI.API.BACtrackAPI;
import BACtrackAPI.API.BACtrackAPICallbacks;
import BACtrackAPI.Constants.BACTrackDeviceType;
import BACtrackAPI.Constants.BACtrackUnit;
import BACtrackAPI.Exceptions.BluetoothLENotSupportedException;
import BACtrackAPI.Exceptions.BluetoothNotEnabledException;
import BACtrackAPI.Exceptions.LocationServicesNotEnabledException;
import BACtrackAPI.Mobile.Constants.Errors;

public class InspectionActivity extends Activity {
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    private static final String TAG = "InspectionActivity";

    private TextView statusMessageTextView;
    private TextView batteryMessageTextView1;
    private TextView batteryMessageTextView2;
    private ProgressBar progressBar;
    private int progress_max;

    private BACtrackAPI mAPI;

    boolean blnFind = false;

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
    final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 2;

    final String apiKey = "e10582efcaf64f7d90d947c2899b43";

    BluetoothAdapter mBtAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection);

        mTextureView = findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                // 先ほどのカメラを開く部分をメソッド化した
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

            }
        });

        // BluetoothAdapterのインスタンス取得
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private void checkBluetooth() {
        if (Build.VERSION.SDK_INT <= 30) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                // permissionが許可されていません
                if (shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT) &&
                        shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_SCAN)) {
                    // 権限チェックした結果、持っていない場合はダイアログを出す
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

                    alertDialog.setCancelable(false);
                    alertDialog.setTitle(getString(R.string.ALERT_TITLE_INFO));
                    alertDialog.setMessage("アルコールマネージャー業務用アプリ 写真撮影版がBluetoothの使用を求めています。\n" +
                            "アルコールチェック機器に接続するために使用されます。");

                    alertDialog.setPositiveButton(android.R.string.ok, (dialog, which) -> ActivityCompat.requestPermissions(InspectionActivity.this,
                            new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                            MY_PERMISSIONS_REQUEST_BLUETOOTH));
                    alertDialog.setOnDismissListener(dialog -> ActivityCompat.requestPermissions(InspectionActivity.this,
                            new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                            MY_PERMISSIONS_REQUEST_BLUETOOTH));
                    alertDialog.create();
                    alertDialog.show();
                } else {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

                    alertDialog.setCancelable(false);
                    alertDialog.setTitle(getString(R.string.ALERT_TITLE_INFO));
                    alertDialog.setMessage("アルコールマネージャー業務用アプリ 写真撮影版がBluetoothの使用を求めています。\n" +
                            "アルコールチェック機器に接続するために使用されます。");

                    alertDialog.setPositiveButton(android.R.string.ok, (dialog, which) -> ActivityCompat.requestPermissions(InspectionActivity.this,
                            new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                            MY_PERMISSIONS_REQUEST_BLUETOOTH));
                    alertDialog.setOnDismissListener(dialog -> ActivityCompat.requestPermissions(InspectionActivity.this,
                            new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                            MY_PERMISSIONS_REQUEST_BLUETOOTH));
                    alertDialog.create();
                    alertDialog.show();
                }
            } else {
                finishBluetoothCheck();
            }
        } else {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // permissionが許可されていません
                if (shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT) &&
                        shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_SCAN)) {
                    // 権限チェックした結果、持っていない場合はダイアログを出す
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

                    alertDialog.setCancelable(false);
                    alertDialog.setTitle(getString(R.string.ALERT_TITLE_INFO));
                    alertDialog.setMessage("アルコールマネージャー業務用アプリ 写真撮影版がBluetoothの使用を求めています。\n" +
                            "アルコールチェック機器に接続するために使用されます。");

                    alertDialog.setPositiveButton(android.R.string.ok, (dialog, which) -> ActivityCompat.requestPermissions(InspectionActivity.this,
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                            MY_PERMISSIONS_REQUEST_BLUETOOTH));
                    alertDialog.setOnDismissListener(dialog -> ActivityCompat.requestPermissions(InspectionActivity.this,
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                            MY_PERMISSIONS_REQUEST_BLUETOOTH));
                    alertDialog.create();
                    alertDialog.show();
                } else {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

                    alertDialog.setCancelable(false);
                    alertDialog.setTitle(getString(R.string.ALERT_TITLE_INFO));
                    alertDialog.setMessage("アルコールマネージャー業務用アプリ 写真撮影版がBluetoothの使用を求めています。\n" +
                            "アルコールチェック機器に接続するために使用されます。\n" +
                            "設定画面より許可してください。");

                    alertDialog.setPositiveButton(android.R.string.ok, (dialog, which) -> requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                            MY_PERMISSIONS_REQUEST_BLUETOOTH));
                    alertDialog.setOnDismissListener(dialog -> requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                            MY_PERMISSIONS_REQUEST_BLUETOOTH));
                    alertDialog.create();
                    alertDialog.show();
                }
            } else {
                finishBluetoothCheck();
            }
        }
    }

    private void finishBluetoothCheck() {
        // Bluetooth判定
        if (mBtAdapter == null) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            // ダイアログの設定
            alertDialog.setCancelable(false);
            alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
            alertDialog.setMessage(getString(R.string.TEXT_BLUETOOTH_DISENABLE));

            // OK(肯定的な)ボタンの設定
            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                // OKボタン押下時の処理
                finish();
            });

            alertDialog.show();
            return;
        } else {
            if (!mBtAdapter.isEnabled()) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

                // ダイアログの設定
                alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
                alertDialog.setMessage(getString(R.string.TEXT_BLUETOOTH_SETTING));

                // OK(肯定的な)ボタンの設定
                alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                    // OKボタン押下時の処理
                    finish();
                });

                alertDialog.show();
                return;
            }
        }

        // 処理開始
        startMain();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length != 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ユーザーが許可したとき
                    // 許可が必要な機能を改めて実行する
                    openCamera();
                } else {
                    // ユーザーが許可しなかったとき
                    // 許可されなかったため機能が実行できないことを表示する
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

                    // ダイアログの設定
                    alertDialog.setCancelable(false);
                    alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
                    alertDialog.setMessage("カメラの使用が許可されていないため続行できません");

                    // OK(肯定的な)ボタンの設定
                    alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                        // OKボタン押下時の処理
                        finish();
                    });

                    alertDialog.setOnDismissListener(dialogInterface -> {
                        // OKボタン押下時の処理
                        finish();
                    });

                    alertDialog.show();
                }
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_BLUETOOTH) {
            if (grantResults.length != 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ユーザーが許可したとき
                    // 許可が必要な機能を改めて実行する
                    finishBluetoothCheck();
                } else {
                    // ユーザーが許可しなかったとき
                    // 許可されなかったため機能が実行できないことを表示する
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

                    // ダイアログの設定
                    alertDialog.setCancelable(false);
                    alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
                    alertDialog.setMessage("Bluetoothの使用が許可されていないため続行できません");

                    // OK(肯定的な)ボタンの設定
                    alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                        // OKボタン押下時の処理
                        finish();
                    });

                    alertDialog.setOnDismissListener(dialogInterface -> {
                        // OKボタン押下時の処理
                        finish();
                    });

                    alertDialog.show();
                }
            }
        }
    }

    private void openCamera() {

        //CameraManagerの取得
        Context context = getApplicationContext();
        CameraManager mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        // インカメラのIDを取得
        String mCameraId = null;
        try {
            //利用可能なカメラIDのリストを取得
            String[] cameraIdList;

            cameraIdList = mCameraManager.getCameraIdList();

            //用途に合ったカメラIDを設定
            for (String cameraId : cameraIdList) {
                //カメラの向き(インカメラ/アウトカメラ)は以下のロジックで判別可能です。(インカメラを使用します)
                CameraCharacteristics characteristics;
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

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // permissionが許可されていません
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    // 権限チェックした結果、持っていない場合はダイアログを出す
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

                    alertDialog.setCancelable(false);
                    alertDialog.setTitle(getString(R.string.ALERT_TITLE_INFO));
                    alertDialog.setMessage("アルコールマネージャー業務用アプリ 写真撮影版がカメラの使用を求めています。\n" +
                            "アルコールチェック時に写真撮影を行い、撮影した画像は、サーバーに送信され、運行管理者が確認するために使用されます。");

                    alertDialog.setPositiveButton(android.R.string.ok, (dialog, which) -> ActivityCompat.requestPermissions(InspectionActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA));
                    alertDialog.setOnDismissListener(dialog -> ActivityCompat.requestPermissions(InspectionActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA));
                    alertDialog.create();
                    alertDialog.show();
                } else {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

                    alertDialog.setCancelable(false);
                    alertDialog.setTitle(getString(R.string.ALERT_TITLE_INFO));
                    alertDialog.setMessage("アルコールマネージャー業務用アプリ 写真撮影版がカメラの使用を求めています。\n" +
                            "アルコールチェック時に写真撮影を行い、撮影した画像は、サーバーに送信され、運行管理者が確認するために使用されます。");

                    alertDialog.setPositiveButton(android.R.string.ok, (dialog, which) -> ActivityCompat.requestPermissions(InspectionActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA));
                    alertDialog.setOnDismissListener(dialog -> ActivityCompat.requestPermissions(InspectionActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA));
                    alertDialog.create();
                    alertDialog.show();
                }

                return;
            }
            assert mCameraId != null;
            mCameraManager.openCamera(mCameraId, mStateCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
            checkBluetooth();
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

    void configureTransformKeepAspect(TextureView textureView, int previewWidth, int previewHeight) {
        int rotation = InspectionActivity.this.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, textureView.getWidth(), textureView.getHeight());
        RectF bufferRect = new RectF(0, 0, previewWidth, previewHeight);
        PointF center = new PointF(viewRect.centerX(), viewRect.centerY());

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(center.x - bufferRect.centerX(), center.y - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);

            float scale = Math.min(
                    (float) textureView.getWidth() / previewWidth,
                    (float) textureView.getHeight() / previewHeight);
            matrix.postScale(scale, scale, center.x, center.y);

            matrix.postRotate(90 * (rotation - 2), center.x, center.y);
        } else {
            bufferRect.offset(center.x - bufferRect.centerX(), center.y - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);

            float scale = Math.min(
                    (float) textureView.getWidth() / previewWidth,
                    (float) textureView.getHeight() / previewHeight);
            matrix.postScale(scale, scale, center.x, center.y);

            matrix.postRotate(90 * rotation, center.x, center.y);
        }

        textureView.setTransform(matrix);
    }

    private void createCameraPreviewSession() {
        try {
            // アスペクト比調整
            int height = Math.min(mTextureView.getWidth(), mTextureView.getHeight());
            int width = (int) (height * 0.75);
            configureTransformKeepAspect(mTextureView, width, height);

            SurfaceTexture texture = mTextureView.getSurfaceTexture();

            //バッファのサイズをプレビューサイズに設定(画面サイズ等適当な値を入れる)
            //ここではQVGA
            assert texture != null;
            texture.setDefaultBufferSize(320, 240);

            Surface surface = new Surface(texture);

            // CaptureRequestを生成
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // CameraCaptureSessionを生成
            mCameraDevice.createCaptureSession(Collections.singletonList(surface),
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
                            Log.e(TAG, "error");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startMain() {

        // 設定情報表示
        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

        TextView meas_textViewDriver = this.findViewById(R.id.meas_textViewDriver);
        TextView meas_textViewCarNo = this.findViewById(R.id.meas_textViewCarNo);
        TextView meas_textViewAddress = this.findViewById(R.id.meas_textViewAddress);

        meas_textViewDriver.setText(pref.getString(getString(R.string.PREF_KEY_DRIVER), ""));
        meas_textViewCarNo.setText(pref.getString(getString(R.string.PREF_KEY_CAR_NO), ""));
        meas_textViewAddress.setText(pref.getString(getString(R.string.PREF_KEY_ADDRESS), ""));

        // 測定
        this.statusMessageTextView = this.findViewById(R.id.meas_status_message_text_view_id);
        this.batteryMessageTextView1 = this.findViewById(R.id.meas_battery_message_text_view_id1);
        this.batteryMessageTextView2 = this.findViewById(R.id.meas_battery_message_text_view_id2);
        this.progressBar = this.findViewById(R.id.meas_progressBar);

        progress_max = -1;
        this.progressBar.setProgress(0);

        // バックトラックAPI
        String firstConnection = pref.getString(getString(R.string.PREF_KEY_FIRST_CONNECTION), "");

        try {
            mAPI = new BACtrackAPI(this, mCallbacks, apiKey);
            this.setStatus(R.string.TEXT_CONNECTING);
            if (firstConnection.equals("")) {
                Thread.sleep(3000);
                editor = pref.edit();
                editor.putString(getString(R.string.PREF_KEY_FIRST_CONNECTION), "1");
                editor.commit();
            }
            mAPI.connectToNearestBreathalyzer();
        } catch (BluetoothLENotSupportedException e) {
            this.setStatus(R.string.TEXT_ERR_BLE_NOT_SUPPORTED);
        } catch (BluetoothNotEnabledException e) {
            this.setStatus(R.string.TEXT_ERR_BT_NOT_ENABLED);
        } catch (LocationServicesNotEnabledException e) {
            this.setStatus(R.string.TEXT_ERR_LOCATION_NOT_ENABLED);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 戻るボタンの操作を無効化する
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {//return true;
                disConnect();
            }
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * bactrack切断
     */
    private void disConnect() {
        if (mAPI != null) {
            mAPI.disconnect();
        }
    }

    /**
     * bactrackAPIのステータス更新用
     *
     * @param resourceId ID
     */
    private void setStatus(int resourceId) {
        this.setStatus(this.getResources().getString(resourceId));
    }

    /**
     * bactrackAPIのステータス更新用(メインメッセージ)
     */
    private void setStatus(final String message) {
        runOnUiThread(() -> statusMessageTextView.setText(message));
    }

    /**
     * bactrackAPIのステータス更新用(メインメッセージ)
     */
    private void setBatteryMessage1(final String message) {
        runOnUiThread(() -> batteryMessageTextView1.setText(message));
    }

    private void setBatteryMessage2(final String message) {
        runOnUiThread(() -> batteryMessageTextView2.setText(message));
    }

    private void setBatteryMessageWhiteColor2() {
        runOnUiThread(() -> batteryMessageTextView2.setTextColor(Color.WHITE));
    }

    private void setBatteryMessageOrangeColor2() {
        runOnUiThread(() -> batteryMessageTextView2.setTextColor(Color.rgb(255, 165, 0)));
    }

    private void setBatteryMessageRedColor2() {
        runOnUiThread(() -> batteryMessageTextView2.setTextColor(Color.RED));
    }

    /**
     * エラー
     */
    private void showErrorAlert(final String message) {
        runOnUiThread(() -> new AlertDialog.Builder(InspectionActivity.this)
                .setTitle(getString(R.string.ALERT_TITLE_ERROR))
                .setMessage(message)
                .setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> InspectionActivity.this.finish())
                .setCancelable(false)
                .show());
    }

    /**
     * bactrackAPIのプログレス更新用
     */
    private void setProgressValue(final int progressValue) {
        runOnUiThread(() -> progressBar.setProgress(progressValue));
    }

    /**
     * bactrackAPIの次画面移動用
     */
    private void moveResult(final float resultValue) {
        runOnUiThread(() -> {

            // シリアル取得
            mAPI.getSerialNumber();

            // 使用回数取得
            mAPI.getUseCount();

            // 切断
            disConnect();

            Date dateObj = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN);
            String inspectionTime = format.format(dateObj);

            BigDecimal bi = new BigDecimal(String.valueOf(resultValue));
            double value = bi.setScale(3, RoundingMode.HALF_UP).doubleValue();

            // 値保存
            editor = pref.edit();
            editor.putString(getString(R.string.PREF_KEY_INSPECTION_TIME), inspectionTime);
            editor.putString(getString(R.string.PREF_KEY_MEASUREMENT), String.valueOf(value));
            editor.commit();

            // 移動
            Intent intent = new Intent(getApplication(), ResultActivity.class);
            startActivity(intent);
        });
    }

    /**
     * bactrackAPICallbacks
     */
    private final BACtrackAPICallbacks mCallbacks = new BACtrackAPICallbacks() {
        @Override
        public void BACtrackAPIKeyDeclined(String errorMessage) {
            Log.d(TAG, "bactrackAPIKeyDeclined");
        }

        @Override
        public void BACtrackAPIKeyAuthorized() {
            Log.d(TAG, "bactrackAPIKeyAuthorized");
        }

        @Override
        public void BACtrackConnected(BACTrackDeviceType bacTrackDeviceType) {
            setStatus(R.string.TEXT_CONNECTED);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> {
                mAPI.getBreathalyzerBatteryVoltage();
                mAPI.startCountdown();
            });

        }

        @Override
        public void BACtrackDidConnect(String s) {
            setStatus(R.string.TEXT_DISCOVERING_SERVICES);

            blnFind = true;
        }

        @Override
        public void BACtrackDisconnected() {
            //setStatus(R.string.TEXT_DISCONNECTED);
            Log.d(TAG, "bactrackDisconnected");
            setStatus(R.string.TEXT_MEAS_DISCONNECT);
        }

        @Override
        public void BACtrackConnectionTimeout() {
            setStatus(R.string.TEXT_ERR_CONNECTION_TIMEOUT);
        }

        @Override
        public void BACtrackFoundBreathalyzer(BACtrackAPI.BACtrackDevice bactrackDevice) {
            Log.d(TAG, "BACtrackFoundBreathalyzer");
        }

        @Override
        public void BACtrackCountdown(int currentCountdownCount) {
            //setStatus(getString(R.string.TEXT_COUNTDOWN) + " " + currentCountdownCount);

            if (progress_max == -1) {
                progress_max = currentCountdownCount;
                progressBar.setMax(progress_max - 1);
            }

            setStatus(getString(R.string.TEXT_MEAS_PREPARATION));

            setProgressValue(progress_max - currentCountdownCount);
        }

        @Override
        public void BACtrackStart() {
            setStatus(R.string.TEXT_BLOW_NOW);
        }

        @Override
        public void BACtrackBlow(float v) {
            SavePhoto();
            setStatus(R.string.TEXT_KEEP_BLOWING);
        }

        private void SavePhoto() {
            try {
                //カメラプレビューを中断させる
                mCaptureSession.stopRepeating();
                if (mTextureView.isAvailable()) {
                    //TextureViewに表示されている画像をBitmapで取得
                    Bitmap bmp = mTextureView.getBitmap();
                    bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mTextureView.getTransform(null), true);
                    bmp = resize(bmp, 320, 320);
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

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
            if (maxHeight > 0 && maxWidth > 0) {
                int width = image.getWidth();
                int height = image.getHeight();
                if (width >= maxWidth || height >= maxHeight) {
                    float widthRate = (float) maxWidth / (float) width;
                    float heightRate = (float) maxHeight / (float) height;

                    int finalWidth = width;
                    int finalHeight = height;
                    if (widthRate > heightRate) {
                        finalWidth = (int) ((float) width * heightRate);
                        finalHeight = (int) ((float) height * heightRate);
                    } else {
                        finalWidth = (int) ((float) width * widthRate);
                        finalHeight = (int) ((float) height * widthRate);
                    }
                    image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
                }
            }
            return image;
        }

        @Override
        public void BACtrackAnalyzing() {
            setStatus(R.string.TEXT_ANALYZING);
        }

        @Override
        public void BACtrackResults(float measuredBac) {
            //setStatus(getString(R.string.TEXT_FINISHED) + " " + measuredBac);

            moveResult(measuredBac);
        }

        @Override
        public void BACtrackFirmwareVersion(String version) {
            Log.d(TAG, "FirmwareVersion: " + version);
            //setStatus(getString(R.string.TEXT_FIRMWARE_VERSION) + " " + version);
        }

        @Override
        public void BACtrackSerial(String serialHex) {
            SharedPreferences pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(getString(R.string.PREF_KEY_BACTRACK_ID), serialHex);
            editor.apply();
        }

        @Override
        public void BACtrackUseCount(int useCount) {
            SharedPreferences pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(getString(R.string.PREF_KEY_BACTRACK_USE_COUNT), String.valueOf(useCount));
            editor.apply();
        }

        @Override
        public void BACtrackBatteryVoltage(float voltage) {
            Log.d(TAG, "BatteryVoltage: " + voltage);
        }

        @Override
        public void BACtrackBatteryLevel(int level) {
            Log.d(TAG, "BatteryLevel: " + level);

            if (level == 0) {
                setBatteryMessageRedColor2();
                setBatteryMessage1("電池残量：少");
                setBatteryMessage2("充電してください");
            } else if (level < 3) {
                setBatteryMessageOrangeColor2();
                setBatteryMessage1("電池残量：中");
                setBatteryMessage2("");
            } else {
                setBatteryMessageWhiteColor2();
                setBatteryMessage1("電池残量：多");
                setBatteryMessage2("");
            }
        }

        @Override
        public void BACtrackError(int errorCode) {
            // 切断
            disConnect();

            if (errorCode == Errors.ERROR_TIME_OUT) {
                showErrorAlert(getString(R.string.TEXT_ERR_BLOW_ERROR));
            } else if (errorCode == Errors.ERROR_BLOW_ERROR) {
                showErrorAlert(getString(R.string.TEXT_ERR_BLOW_ERROR));
            } else if (errorCode == Errors.ERROR_OUT_OF_TEMPERATURE) {
                showErrorAlert("ERROR_OUT_OF_TEMPERATURE");
            } else if (errorCode == Errors.ERROR_LOW_BATTERY) {
                showErrorAlert("ERROR_LOW_BATTERY");
            } else if (errorCode == Errors.ERROR_CALIBRATION_FAIL) {
                showErrorAlert("ERROR_CALIBRATION_FAIL");
            } else if (errorCode == Errors.ERROR_NOT_CALIBRATED) {
                showErrorAlert("ERROR_NOT_CALIBRATED");
            } else if (errorCode == Errors.ERROR_COM_ERROR) {
                showErrorAlert("ERROR_COM_ERROR");
            } else if (errorCode == Errors.ERROR_INFLOW_ERROR) {
                showErrorAlert("ERROR_INFLOW_ERROR");
            } else if (errorCode == Errors.ERROR_SOLENOID_ERROR) {
                showErrorAlert("ERROR_SOLENOID_ERROR");
            } else {
                showErrorAlert(getString(R.string.TEXT_ERR_EXCEPTION));
            }
        }

        @Override
        public void BACtrackUnits(BACtrackUnit baCtrackUnit) {

        }
    };
}
