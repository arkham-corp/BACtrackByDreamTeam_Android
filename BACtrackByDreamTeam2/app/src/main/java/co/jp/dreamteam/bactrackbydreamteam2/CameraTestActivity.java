package co.jp.dreamteam.bactrackbydreamteam2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class CameraTestActivity extends Activity
{
	private static String TAG = "CameraTestActivity";

	private LinearLayout layoutTextureViewParent;

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

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_test);

		layoutTextureViewParent = (LinearLayout) findViewById(R.id.test_layoutTextureViewParent);
		mTextureView = (TextureView) findViewById(R.id.test_textureView);
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

		this.findViewById(R.id.test_send_button).setOnClickListener(btnSendClicked);
	}

	View.OnClickListener btnSendClicked = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			SavePhoto();
			exec_post();
		}
	};

	private void SavePhoto()
	{
		try {
			//カメラプレビューを中断させる
			mCaptureSession.stopRepeating();
			if (mTextureView.isAvailable()) {
				//TextureViewに表示されている画像をBitmapで取得
				Bitmap bmp = mTextureView.getBitmap();
				bmp = Bitmap.createBitmap( bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mTextureView.getTransform( null ), true );

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

	// POST通信を実行（AsyncTaskによる非同期処理を使うバージョン）
	public void exec_post() {
		// 非同期タスクを定義
		HttpPostTask task = new HttpPostTask(this,
				getString(R.string.HTTP_URL) + "/" + getString(R.string.HTTP_WRITE_ALCOHOL_VALUE),

				// タスク完了時に呼ばれるUIのハンドラ
				new HttpPostHandler() {

					@Override
					public void onPostCompleted(String response) {
						// 受信結果をUIに表示
						if (response.startsWith(getString(R.string.HTTP_RESPONSE_OK))) {
							//btnFinish.setVisibility(View.VISIBLE);
						} else {
							//errorSending();
						}
					}

					@Override
					public void onPostFailed(String response) {
						//errorHttp(response);
					}
				});

		SharedPreferences pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

		String strCompany = "COM";
		String strAddress = "静岡県 浜松市中区 新津町 600";
		String strLat = "34.72072";
		String strLong = "137.7419";
		String strDriver = "1000";
		String strCarNo = "1234";
		String strAlcoholValue = "0";

		// 画像取得
		byte[] photoByte = null;
		String strBitmap = pref.getString(getString(R.string.PREF_KEY_PHOTO), "");
		if (!strBitmap.equals(""))
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			photoByte = Base64.decode(strBitmap, Base64.DEFAULT);
		}

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
		int rotation = CameraTestActivity.this.getWindowManager().getDefaultDisplay().getRotation();
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

	private void createCameraPreviewSession()
	{
		try {
			// アスペクト比調整 4:3(0.75)に調整
			int width = mTextureView.getWidth();
			int height = mTextureView.getHeight();
			if (width < height)
			{
				height = width;
			}
			else {
				width = height;
			}
			width = (int) (height * 0.75);
			configureTransformKeepAspect(mTextureView, width, height);

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
			}
		}
		return super.dispatchKeyEvent(event);
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
				new AlertDialog.Builder(CameraTestActivity.this)
				.setTitle(getString(R.string.ALERT_TITLE_ERROR))
				.setMessage(message)
				.setPositiveButton(getString(R.string.ALERT_BTN_OK), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						CameraTestActivity.this.finish();
					}
				})
				.setCancelable(false)
				.show();
			}
		});
	}

}
