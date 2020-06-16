package co.jp.dreamteam.bactrackbydreamteam2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ResultActivity extends Activity {

	final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1001;

	public static final double ALCOHOL_REMOVAL_RATE = 0.015;

	BroadcastReceiver mReceiver;

	SharedPreferences pref;
	SharedPreferences.Editor editor;

	Button btnFinish;
	TextView textViewMessage;
	TextView textViewTitle;
	TextView textViewResultValue;
	TextView textViewResultRemainValue;

	int errorCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);

		// BroadcastRecieverを LocalBroadcastManagerを使って登録
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(getString(R.string.BLOADCAST_FINISH));
		mReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
				finish();
			}
		};
		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiver, intentFilter);

		this.textViewMessage = (TextView) this.findViewById(R.id.result_textViewResultMessage);
		this.textViewTitle = (TextView) this.findViewById(R.id.result_textViewResultTitle);
		this.textViewResultValue = (TextView) this.findViewById(R.id.result_textViewResultValue);
		this.textViewResultRemainValue = (TextView) this.findViewById(R.id.result_textViewRemainValue);

		pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

		// 測定値取得
		String strMeasurement = pref.getString(getString(R.string.PREF_KEY_MEASUREMENT), "");
		double alcoholValue = Double.valueOf(strMeasurement);
		double alcoholValueBreath = Double.valueOf(strMeasurement) * 5;
		String strAlcoholValue = String.format("%.2f", alcoholValue);
		String strAlcoholValueBreath = String.format("%.2f", alcoholValueBreath);

		// 異常判定
		if (Double.valueOf(strAlcoholValue) != 0)
		{
			textViewMessage.setText(getString(R.string.TEXT_RESULT_WARNING));
			textViewMessage.setTextColor(Color.RED);
		}

		// 表示区分取得
		String strAlcoholValueDiv = pref.getString(getString(R.string.PREF_KEY_ALCOHOL_VALUE_DIV), "");

		if (strAlcoholValueDiv.equals("1"))
		{
			// 呼気を画面に表示
			textViewTitle.setText(getString(R.string.TEXT_RESULT_TITLE_BREATH));
			textViewResultValue.setText(strAlcoholValueBreath + "%");
		}
		else
		{
			// 血中を画面に表示
			textViewTitle.setText(getString(R.string.TEXT_RESULT_TITLE_BLOOD));
			textViewResultValue.setText(strAlcoholValue + "mg");
		}

		// 計測結果から残留目安時間を表示
		textViewResultRemainValue.setText(getRemainTime(alcoholValue) + " です。");

		// 終了ボタン
		this.btnFinish = this.findViewById(R.id.result_btnFinish);
		btnFinish.setOnClickListener(btnFinishClicked);

		// 自動送信
		exec_post();
	}

	View.OnClickListener btnFinishClicked = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// LocalBroadcastManagerを使ってBroadcastを送信
			Intent appFinishIntent = new Intent();
			appFinishIntent.setAction(getString(R.string.BLOADCAST_FINISH));
			LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(appFinishIntent);
			finish();
		}
	};

	/**
	 * 計測値から残留時間を計算、アルコール消化時刻を返却
	 *
	 * @param alcoholValue
	 * @return
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

		String h_str = "00" + String.valueOf(h_for);
		String m_str = "00" + String.valueOf(m_for);
		String ret = h_str.substring(h_str.length() - 2) + ":" + m_str.substring(m_str.length() - 2);

		return ret;
	}

	/**
	 * 送信エラー
	 */
	private void errorSending() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				errorCount += 1;

				AlertDialog.Builder alertDialog = new AlertDialog.Builder(ResultActivity.this);
				alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));

				if (errorCount < 3) {
					// ダイアログの設定
					alertDialog.setMessage(getString(R.string.TEXT_SEND_ERROR));

					// OK(肯定的な)ボタンの設定
					alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// OKボタン押下時の処理
							exec_post();
						}
					});
				}
				else
				{
					// ダイアログの設定
					alertDialog.setMessage(getString(R.string.TEXT_SEND_ERROR_LAST));

					// OK(肯定的な)ボタンの設定
					alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// OKボタン押下時の処理
							btnFinish.setVisibility(View.VISIBLE);
						}
					});
				}

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
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(ResultActivity.this);

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

}
