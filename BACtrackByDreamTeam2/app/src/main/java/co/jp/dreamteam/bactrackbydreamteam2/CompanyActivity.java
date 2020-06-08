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

import java.util.HashMap;

public class CompanyActivity extends Activity
{
	BroadcastReceiver mReceiver;
	
	SharedPreferences pref;
	SharedPreferences.Editor editor;

	EditText editTextCompany;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_company);
		
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

		this.editTextCompany = (EditText) this.findViewById(R.id.company_editTextCompany);

		this.findViewById(R.id.company_btnDecision).setOnClickListener(btnDecisionClicked);

		pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

		editTextCompany.setText(pref.getString(getString(R.string.PREF_KEY_COMPANY), ""));
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
	 * 会社エラー
	 */
	private void errorCompanyNotFound()
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(CompanyActivity.this);

				// ダイアログの設定
				alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
				alertDialog.setMessage(getString(R.string.TEXT_ERR_COMPANY_NOT_FOUND));

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
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(CompanyActivity.this);

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
	private void exec_post()
	{
		// 非同期タスクを定義
		HttpPostTask task = new HttpPostTask(
				this,
				getString(R.string.HTTP_URL) + "/" + getString(R.string.HTTP_COMPANY_CHECK),

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
							editor.putString(getString(R.string.PREF_KEY_COMPANY), editTextCompany.getText().toString());
							editor.commit();

							// 画面移動
							Intent intent = new Intent(getApplication(), DriverActivity.class);
							startActivity(intent);
						}
						else
						{
							errorCompanyNotFound();
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
		task.addPostHeader(getString(R.string.HTTP_HEADER_PARAM_KEY_AUTH), getString(R.string.HTTP_HEADER_PARAM_TOKEN));
		task.addPostParam(getString(R.string.HTTP_PARAM_COMPANY_CODE), editTextCompany.getText().toString());

		// タスクを開始
		task.execute();
	}
}
