package co.jp.dreamteam.bactrackbydreamteam2;

import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

import BACtrackAPI.API.BACtrackAPI;
import BACtrackAPI.API.BACtrackAPICallbacks;
import BACtrackAPI.Constants.BACTrackDeviceType;
import BACtrackAPI.Exceptions.LocationServicesNotEnabledException;
import BACtrackAPI.Mobile.Constants.Errors;
import BACtrackAPI.Exceptions.BluetoothLENotSupportedException;
import BACtrackAPI.Exceptions.BluetoothNotEnabledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ProgressBar;
import android.widget.TextView;

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

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inspection);

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
		this.statusMessageCaption = (TextView) this.findViewById(R.id.meas_status_message_caption);
		this.progressBar = (ProgressBar) this.findViewById(R.id.meas_progressBar);

		progress_max = -1;
		this.progressBar.setProgress(0);

		try
		{
			String apiKey = "e10582efcaf64f7d90d947c2899b43";

			mAPI = new BACtrackAPI(this, mCallbacks, apiKey);

			// タイマーで接続されるまで開始を待機
			timerConnect.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					// mHandlerを通じてUI Threadへ処理をキューイング
					mHandler.post(new Runnable()
					{
						public void run()
						{
							if (blnFind)
							{
								timerConnect.cancel();
							}
							else
							{
								// 接続
								connectNearest();
							}
						}
					});
				}
			}, 1000, 5000);

			// タイマーで接続されるまで開始を待機
			timerStart.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					// mHandlerを通じてUI Threadへ処理をキューイング
					mHandler.post(new Runnable()
					{
						public void run()
						{
							if (blnConnected)
							{
								startBlowProcess();

								timerStart.cancel();
							}
						}
					});
				}
			}, 1000, 1000);

			setStatus(R.string.TEXT_CONNECTING);
		}
		catch (BluetoothLENotSupportedException e)
		{
			e.printStackTrace();
			this.setStatus(R.string.TEXT_ERR_BLE_NOT_SUPPORTED);
		}
		catch (BluetoothNotEnabledException e)
		{
			e.printStackTrace();
			this.setStatus(R.string.TEXT_ERR_BT_NOT_ENABLED);
		} catch (LocationServicesNotEnabledException e) {
			e.printStackTrace();
			this.setStatus("LocationServicesNotEnabledException");
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
	 * BACtrack接続
	 */
	private void connectNearest()
	{
		if (mAPI != null)
		{
			mAPI.connectToNearestBreathalyzer();
		}
	}

	/**
	 * 測定開始
	 */
	private void startBlowProcess()
	{
		boolean result = false;

		if (mAPI != null)
		{
			result = mAPI.startCountdown();
		}
		if (!result)
		{
			setStatus(R.string.TEXT_ERR_START_COUNTDOWN);

			Log.e(TAG, "mAPI.startCountdown() failed");
		}
		else
		{
			Log.d(TAG, "Blow process start requested");
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
	 * BACtrackAPIのステータス更新用(詳細)
	 */
	private void setStatusCapt(final String message)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				statusMessageCaption.setText(message);
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

	private class APIKeyVerificationAlert extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			return urls[0];
		}

		@Override
		protected void onPostExecute(String result) {
			AlertDialog.Builder apiApprovalAlert = new AlertDialog.Builder(mContext);
			apiApprovalAlert.setTitle("API Approval Failed");
			apiApprovalAlert.setMessage(result);
			apiApprovalAlert.setPositiveButton(
					"Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							mAPI.disconnect();
							setStatus(R.string.TEXT_DISCONNECTED);
							dialog.cancel();
						}
					});

			apiApprovalAlert.create();
			apiApprovalAlert.show();
		}
	}

	/**
	 * BACtrackAPICallbacks
	 */
	private final BACtrackAPICallbacks mCallbacks = new BACtrackAPICallbacks()
	{
		@Override
		public void BACtrackAPIKeyDeclined(String errorMessage) {
			APIKeyVerificationAlert verify = new APIKeyVerificationAlert();
			verify.execute(errorMessage);
		}

		@Override
		public void BACtrackAPIKeyAuthorized() {

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

			blnConnected = true;
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
			setStatusCapt(getString(R.string.TEXT_MEAS_PREPARATION_CAPT));

			setProgressValue(progress_max - currentCountdownCount);
		}

		@Override
		public void BACtrackStart()
		{
			setStatus(R.string.TEXT_BLOW_NOW);
			setStatusCapt("  ");
		}

		@Override
		public void BACtrackBlow()
		{
			setStatus(R.string.TEXT_KEEP_BLOWING);
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
