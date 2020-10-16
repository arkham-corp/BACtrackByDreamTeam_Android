package co.jp.dreamteam.bactrackbydreamteam2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class CarNoActivity extends Activity
{
	BroadcastReceiver mReceiver;
	
	SharedPreferences pref;
	SharedPreferences.Editor editor;

	String company_code;
	EditText editTextCarNo;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_car_no);
		
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

		this.editTextCarNo = (EditText) this.findViewById(R.id.car_no_editTextCarNo);

		this.findViewById(R.id.car_no_btnDecision).setOnClickListener(btnDecisionClicked);

		pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

		editTextCarNo.setText(pref.getString(getString(R.string.PREF_KEY_CAR_NO), ""));

		company_code = pref.getString(getString(R.string.PREF_KEY_COMPANY), "");
	}

	OnClickListener btnDecisionClicked = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			exec_post();
		}
	};

	/**
	 * 車番エラー
	 */
	private void errorDriverNotFound()
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(CarNoActivity.this);

				// ダイアログの設定
				alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
				alertDialog.setMessage(getString(R.string.TEXT_ERR_CAR_NO_NOT_FOUND));

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
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(CarNoActivity.this);

				// ダイアログの設定
				alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
				if (response.startsWith("Hostname al-check.com not verified"))
				{
					alertDialog.setMessage("Https通信のHostnameが不正です");
				}
				else {
					alertDialog.setMessage(response);
				}

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
	private void exec_post()
	{
		// 非同期タスクを定義
		HttpPostTask task = new HttpPostTask(
				this,
				getString(R.string.HTTP_URL) + "/" + getString(R.string.HTTP_CAR_NO_CHECK),

				// タスク完了時に呼ばれるUIのハンドラ
				new HttpPostHandler()
				{

					@Override
					public void onPostCompleted(String response)
					{
						// 受信結果をUIに表示
						if (response.startsWith(getString(R.string.HTTP_RESPONSE_OK)))
						{
							// 値保存
							editor = pref.edit();
							editor.putString(getString(R.string.PREF_KEY_CAR_NO), editTextCarNo.getText().toString());
							editor.commit();

							// 画面移動
							Intent intent = new Intent(getApplication(), InspectionActivity.class);
							startActivity(intent);
						}
						else
						{
							errorDriverNotFound();
						}
					}

					@Override
					public void onPostFailed(String response)
					{
						errorHttp(response);
					}
				}
		);

		// パラメータセット
		task.setVerify_hostname(getString(R.string.VERIFY_HOSTNAME));
		task.addPostParam(getString(R.string.HTTP_PARAM_COMPANY_CODE), company_code);
		task.addPostParam(getString(R.string.HTTP_PARAM_CAR_NO), editTextCarNo.getText().toString());

		// タスクを開始
		task.execute();
	}
}
