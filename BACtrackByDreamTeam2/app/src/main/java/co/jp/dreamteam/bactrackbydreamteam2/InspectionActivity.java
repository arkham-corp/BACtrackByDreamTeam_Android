package co.jp.dreamteam.bactrackbydreamteam2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Timer;

import BACtrackAPI.API.BACtrackAPI;
import BACtrackAPI.API.BACtrackAPICallbacks;
import BACtrackAPI.Constants.BACTrackDeviceType;
import BACtrackAPI.Exceptions.BluetoothLENotSupportedException;
import BACtrackAPI.Exceptions.BluetoothNotEnabledException;
import BACtrackAPI.Exceptions.LocationServicesNotEnabledException;
import BACtrackAPI.Mobile.Constants.Errors;

public class InspectionActivity extends Activity
{
	BroadcastReceiver mReceiver;

	SharedPreferences pref;
	SharedPreferences.Editor editor;

	private static String TAG = "InspectionActivity";

	private TextView statusMessageTextView;
	private TextView statusMessageCaption;
	private ProgressBar progressBar;
	private int progress_max;

	private BACtrackAPI mAPI;

	Timer timerConnect = new Timer();
	Timer timerStart = new Timer();

	Handler mHandler = new Handler();

	boolean blnFind = false;
	boolean blnConnected = false;

	private Context mContext;

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

	final String apiKey = "e10582efcaf64f7d90d947c2899b43";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inspection);

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
			startMain();
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

	private void startMain()
	{
		// BroadcastRecieverを LocalBroadcastManagerを使って登録
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(getString(R.string.BLOADCAST_FINISH));
		mReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent)
			{
				LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
				finish();
			}
		};
		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiver, intentFilter);

		// 設定情報表示
		pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

		TextView meas_textViewDriver = (TextView) this.findViewById(R.id.meas_textViewDriver);
		TextView meas_textViewCarNo = (TextView) this.findViewById(R.id.meas_textViewCarNo);
		TextView meas_textViewAddress = (TextView) this.findViewById(R.id.meas_textViewAddress);

		meas_textViewDriver.setText(pref.getString(getString(R.string.PREF_KEY_DRIVER), ""));
		meas_textViewCarNo.setText(pref.getString(getString(R.string.PREF_KEY_CAR_NO), ""));
		meas_textViewAddress.setText(pref.getString(getString(R.string.PREF_KEY_ADDRESS), ""));

		// 測定
		this.statusMessageTextView = (TextView) this.findViewById(R.id.meas_status_message_text_view_id);
		this.progressBar = (ProgressBar) this.findViewById(R.id.meas_progressBar);

		progress_max = -1;
		this.progressBar.setProgress(0);

		// バックトラックAPI
		try {
			mAPI = new BACtrackAPI(this, mCallbacks, apiKey);
			mAPI.connectToNearestBreathalyzer();
			this.setStatus(R.string.TEXT_CONNECTING);
		} catch (BluetoothLENotSupportedException e) {
			this.setStatus(R.string.TEXT_ERR_BLE_NOT_SUPPORTED);
			return;
		} catch (BluetoothNotEnabledException e) {
			this.setStatus(R.string.TEXT_ERR_BT_NOT_ENABLED);
			return;
		} catch (LocationServicesNotEnabledException e) {
			this.setStatus("LocationServicesNotEnabledException");
			return;
		}
	}

	/**
	 * 戻るボタンの操作を無効化する
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (event.getAction() == KeyEvent.ACTION_DOWN)
		{
			switch (event.getKeyCode())
			{
			case KeyEvent.KEYCODE_BACK:
				//return true;
				disConnect();
			}
		}
		return super.dispatchKeyEvent(event);
	}

	/**
	 * BACtrack切断
	 */
	private void disConnect()
	{
		if (mAPI != null)
		{
			mAPI.disconnect();
		}
	}

	/**
	 * BACtrackAPIのステータス更新用
	 * @param resourceId
	 */
	private void setStatus(int resourceId)
	{
		this.setStatus(this.getResources().getString(resourceId));
	}

	/**
	 * BACtrackAPIのステータス更新用(メインメッセージ)
	 */
	private void setStatus(final String message)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				statusMessageTextView.setText(message);
			}
		});
	}

	/**
	 * エラー
	 */
	private void showErrorAlert(final String message)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				new AlertDialog.Builder(InspectionActivity.this)
				.setTitle(getString(R.string.ALERT_TITLE_ERROR))
				.setMessage(message)
				.setPositiveButton(getString(R.string.ALERT_BTN_OK), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						InspectionActivity.this.finish();
					}
				})
				.setCancelable(false)
				.show();
			}
		});
	}

	/**
	 * BACtrackAPIのプログレス更新用
	 */
	private void setProgressValue(final int progressValue)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				progressBar.setProgress(progressValue);
			}
		});
	}

	/**
	 * BACtrackAPIの次画面移動用
	 */
	private void moveResult(final float resultValue)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				// 切断
				disConnect();

				BigDecimal bi = new BigDecimal(String.valueOf(resultValue));
				double value= bi.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();

				// 値保存
				editor = pref.edit();
				editor.putString(getString(R.string.PREF_KEY_MEASUREMENT), String.valueOf(value));
				editor.commit();

				// 移動
				Intent intent = new Intent(getApplication(), ResultActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * BACtrackAPICallbacks
	 */
	private final BACtrackAPICallbacks mCallbacks = new BACtrackAPICallbacks()
	{
		@Override
		public void BACtrackAPIKeyDeclined(String errorMessage) {
			Log.d(TAG, "BACtrackAPIKeyDeclined");
		}

		@Override
		public void BACtrackAPIKeyAuthorized() {
			Log.d(TAG, "BACtrackAPIKeyAuthorized");
		}

		@Override
		public void BACtrackConnected(BACTrackDeviceType bacTrackDeviceType)
		{
			setStatus(R.string.TEXT_CONNECTED);

			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}

			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					mAPI.startCountdown();
				}
			});

		}

		@Override
		public void BACtrackDidConnect(String s)
		{
			setStatus(R.string.TEXT_DISCOVERING_SERVICES);

			blnFind = true;
		}

		@Override
		public void BACtrackDisconnected()
		{
			//setStatus(R.string.TEXT_DISCONNECTED);
			Log.d(TAG, "BACtrackDisconnected");
			setStatus(R.string.TEXT_MEAS_DISCONNECT);
		}

		@Override
		public void BACtrackConnectionTimeout()
		{
			setStatus(R.string.TEXT_ERR_CONNECTION_TIMEOUT);
		}

		@Override
		public void BACtrackFoundBreathalyzer(BluetoothDevice bluetoothDevice) {
			Log.d(TAG, "Found breathalyzer : " + bluetoothDevice.getName());
		}

		@Override
		public void BACtrackCountdown(int currentCountdownCount)
		{
			//setStatus(getString(R.string.TEXT_COUNTDOWN) + " " + currentCountdownCount);

			if (progress_max == -1)
			{
				progress_max = currentCountdownCount;
				progressBar.setMax(progress_max - 1);
			}

			setStatus(getString(R.string.TEXT_MEAS_PREPARATION));

			setProgressValue(progress_max - currentCountdownCount);
		}

		@Override
		public void BACtrackStart()
		{
			setStatus(R.string.TEXT_BLOW_NOW);
		}

		@Override
		public void BACtrackBlow()
		{
			SavePhoto();
			setStatus(R.string.TEXT_KEEP_BLOWING);
		}

		private void SavePhoto()
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

		@Override
		public void BACtrackAnalyzing()
		{
			setStatus(R.string.TEXT_ANALYZING);
		}

		@Override
		public void BACtrackResults(float measuredBac)
		{
			//setStatus(getString(R.string.TEXT_FINISHED) + " " + measuredBac);

			moveResult(measuredBac);
		}

		@Override
		public void BACtrackFirmwareVersion(String version) {
			setStatus(getString(R.string.TEXT_FIRMWARE_VERSION) + " " + version);
		}

		@Override
		public void BACtrackSerial(String serialHex) {

		}

		@Override
		public void BACtrackUseCount(int useCount) {
			Log.d(TAG, "UseCount: " + useCount);
			setStatus(getString(R.string.TEXT_USE_COUNT) + " " + useCount);
		}

		@Override
		public void BACtrackBatteryVoltage(float voltage) {

		}

		@Override
		public void BACtrackBatteryLevel(int level) {

		}

		@Override
		public void BACtrackError(int errorCode)
		{
			if (errorCode == Errors.ERROR_BLOW_ERROR)
			{
				//setStatus(R.string.TEXT_ERR_BLOW_ERROR);

				// 切断
				disConnect();

				showErrorAlert(getString(R.string.TEXT_ERR_BLOW_ERROR));
			}
			else
			{
				// 切断
				disConnect();

				showErrorAlert(getString(R.string.TEXT_ERR_EXCEPTION));
			}
		}
	};
}
