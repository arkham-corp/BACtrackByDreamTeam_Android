package co.jp.dreamteam.bactrackbydreamteam2;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import BACtrackAPI.API.BACtrackAPI;

public class MainActivity extends Activity
{
	BroadcastReceiver mReceiver;

	SharedPreferences pref;
	SharedPreferences.Editor editor;

	private BACtrackAPI mAPI;

	/**
	 * 初期処理
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		boolean httpTestFlg = false;
		boolean cameraTestFlg = false;

		if (httpTestFlg)
		{
			Intent intent = new Intent(getApplication(), CompanyActivity.class);
			startActivity(intent);
			return;
		}

		if (cameraTestFlg)
		{
			Intent intent = new Intent(getApplication(), CameraTestActivity.class);
			startActivity(intent);
			return;
		}

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

		this.findViewById(R.id.main_btnDecision).setOnClickListener(btnDecisionClicked);

		// Bluetooth判定
		// BluetoothAdapterのインスタンス取得
		BluetoothAdapter mBtAdapter;
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mBtAdapter == null) {

			AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

			// ダイアログの設定
			alertDialog.setCancelable(false);
			alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
			alertDialog.setMessage("Bluetoothが使用できません");

			// OK(肯定的な)ボタンの設定
			alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// OKボタン押下時の処理
					finish();
				}
			});

			alertDialog.show();

		} else
		{
			if (!mBtAdapter.isEnabled()) {
				mBtAdapter.enable();
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

				// ダイアログの設定
				alertDialog.setTitle(getString(R.string.ALERT_TITLE_INFO));
				alertDialog.setMessage("BluetoothをONにしました");

				// OK(肯定的な)ボタンの設定
				alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// OKボタン押下時の処理
					}
				});

				alertDialog.show();
			}
		}

		// 利用規約確認
		pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

		String agreement = pref.getString(getString(R.string.PREF_KEY_AGREEMENT), "");

		if (agreement.equals("")) {

			// 利用規約ダイアログ
			LayoutInflater inflater = (LayoutInflater) this.getSystemService(
					LAYOUT_INFLATER_SERVICE);
			final View layout = inflater.inflate(R.layout.dialog_areement,
					(ViewGroup) findViewById(R.id.layout_root));

			// アラーとダイアログ を生成
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("利用規約");
			builder.setView(layout);
			builder.setPositiveButton("同意する", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					editor = pref.edit();
					editor.putString(getString(R.string.PREF_KEY_AGREEMENT), "1");
					editor.commit();
				}
			});
			builder.setNegativeButton("同意しない", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					MainActivity.this.finish();
				}
			});

			// 表示
			builder.create().show();
		}

	}

	OnClickListener btnDecisionClicked = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Intent intent = new Intent(getApplication(), GPSActivity.class);
			startActivity(intent);
		}
	};
}
