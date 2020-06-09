package co.jp.dreamteam.bactrackbydreamteam2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ResultActivity extends Activity {

	final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1001;

	public static final double ALCOHOL_REMOVAL_RATE = 0.015;

	BroadcastReceiver mReceiver;

	SharedPreferences pref;
	SharedPreferences.Editor editor;

	TextView textViewResultValue;
	TextView textViewResultRemainValue;

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

		this.textViewResultValue = (TextView) this.findViewById(R.id.result_textViewResultValue);

		this.findViewById(R.id.result_btnDecision).setOnClickListener(btnDecisionClicked);

		pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

		String measurement = pref.getString(getString(R.string.PREF_KEY_MEASUREMENT), "");
		textViewResultValue.setText(measurement);
		// textViewResultValue.setText(pref.getString(getString(R.string.PREF_KEY_MEASUREMENT),
		// ""));

		// 計測結果から残留目安時間を表示
		this.textViewResultRemainValue = (TextView) this.findViewById(R.id.result_textViewRemainValue);
		textViewResultRemainValue.setText(getRemainTime(measurement) + " です。");

	}

	OnClickListener btnDecisionClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			exec_post();
		}
	};

	/**
	 * 計測値から残留時間を計算、アルコール消化時刻を返却
	 *
	 * @param measurement
	 * @return
	 */
	private String getRemainTime(String measurement) {

		double alcoholValue = Double.valueOf(measurement);

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
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(ResultActivity.this);

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
	private void exec_post() {

		if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{

			// permissionが許可されていません
			if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
				// 許可ダイアログで今後表示しないにチェックされていない場合
			}

			// permissionを許可してほしい理由の表示など

			// 許可ダイアログの表示
			// MY_PERMISSIONS_REQUEST_READ_CONTACTSはアプリ内で独自定義したrequestCodeの値
			requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
					MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

			return;
		}

		exec_post_next();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			// 先ほどの独自定義したrequestCodeの結果確認
			case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// ユーザーが許可したとき
					// 許可が必要な機能を改めて実行する
					exec_post_next();
				} else {
					// ユーザーが許可しなかったとき
					// 許可されなかったため機能が実行できないことを表示する
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

					// ダイアログの設定
					alertDialog.setCancelable(false);
					alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
					alertDialog.setMessage("端末情報が使用できません");

					// OK(肯定的な)ボタンの設定
					alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// OKボタン押下時の処理
							finish();
						}
					});

					alertDialog.show();
				}
				return;
			}
		}
	}

	private void exec_post_next() {

		// 非同期タスクを定義
		HttpPostTask task = new HttpPostTask(this,
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

		pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

		String strCompany = pref.getString(getString(R.string.PREF_KEY_COMPANY), "");
		String strAddress = pref.getString(getString(R.string.PREF_KEY_ADDRESS), "");
		String strLat = pref.getString(getString(R.string.PREF_KEY_LAT), "");
		String strLong = pref.getString(getString(R.string.PREF_KEY_LON), "");
		String strDriver = pref.getString(getString(R.string.PREF_KEY_DRIVER), "");
		String strCarNo = pref.getString(getString(R.string.PREF_KEY_CAR_NO), "");
		String strAlcoholValue = pref.getString(getString(R.string.PREF_KEY_MEASUREMENT), "");

		// パラメータセット
		task.addPostParam(getString(R.string.HTTP_PARAM_COMPANY_CODE), strCompany);
		task.addPostParam(getString(R.string.HTTP_PARAM_DRIVER_CODE), strDriver);
		task.addPostParam(getString(R.string.HTTP_PARAM_CAR_NO), strCarNo);
		task.addPostParam(getString(R.string.HTTP_PARAM_LOCATION_NAME), strAddress);
		task.addPostParam(getString(R.string.HTTP_PARAM_LOCATION_LAT), strLat);
		task.addPostParam(getString(R.string.HTTP_PARAM_LOCATION_LONG), strLong);
		task.addPostParam(getString(R.string.HTTP_PARAM_ALCOHOL_VALUE), strAlcoholValue);
		task.addPostParam(getString(R.string.HTTP_PARAM_APP_PROG), "Android");
		task.addPostParam(getString(R.string.HTTP_PARAM_APP_ID), "Android");

		// タスクを開始
		task.execute();
	}
}
