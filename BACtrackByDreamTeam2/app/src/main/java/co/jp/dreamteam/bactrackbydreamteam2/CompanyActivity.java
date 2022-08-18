package co.jp.dreamteam.bactrackbydreamteam2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class CompanyActivity extends Activity
{
	BroadcastReceiver mReceiver;
	
	SharedPreferences pref;
	SharedPreferences.Editor editor;

	EditText editTextCompany;
	Button company_btnDecision;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_company);
		
		// BroadcastReceiverを LocalBroadcastManagerを使って登録
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

		// 設定ファイル取得
		pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

		// 設定初期化
		editor = pref.edit();
		editor.putString(getString(R.string.PREF_KEY_HTTP_URL), "");
		editor.putString(getString(R.string.PREF_KEY_VERIFY_HOSTNAME), "");
		editor.commit();

		this.editTextCompany = this.findViewById(R.id.company_editTextCompany);

		company_btnDecision = this.findViewById(R.id.company_btnDecision);
		company_btnDecision.setOnClickListener(btnDecisionClicked);

		pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

		editTextCompany.setText(pref.getString(getString(R.string.PREF_KEY_COMPANY), ""));
	}

	OnClickListener btnDecisionClicked = v -> exec_post();

	@Override
	protected void onStart() {
		super.onStart();
		company_btnDecision.setEnabled(true);
	}

	/**
	 * 会社エラー
	 */
	private void errorCompanyNotFound()
	{
		runOnUiThread(() -> {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(CompanyActivity.this);

			// ダイアログの設定
			alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
			alertDialog.setMessage(getString(R.string.TEXT_ERR_COMPANY_NOT_FOUND));

			// OK(肯定的な)ボタンの設定
			alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
				// OKボタン押下時の処理
				company_btnDecision.setEnabled(true);
			});

			alertDialog.show();
		});
	}

	/**
	 * HTTPコネクションエラー
	 */
	private void errorHttp(final String response)
	{
		runOnUiThread(() -> {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(CompanyActivity.this);

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
			alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
				// OKボタン押下時の処理
				company_btnDecision.setEnabled(true);
			});

			alertDialog.show();
		});
	}

	// POST通信を実行（AsyncTaskによる非同期処理を使うバージョン）
	private void exec_post()
	{
		company_btnDecision.setEnabled(false);

		// 非同期タスクを定義
		HttpPostTask task = new HttpPostTask(
				this,
				"https://almanecloud.com/alcoholmanager/linkmanager/api/getApplicationApiUrl",

				// タスク完了時に呼ばれるUIのハンドラ
				new HttpPostHandler()
				{

					@Override
					public void onPostCompleted(String response)
					{
						try {
							JSONObject json = new JSONObject(response);

							// 受信結果をUIに表示
							if (json.getString("status").equals("true"))
							{
								// 値保存
								String str_url = json.getString("data");
								URL url = new URL(str_url);
								String host_name = url.getHost();

								editor = pref.edit();
								editor.putString(getString(R.string.PREF_KEY_HTTP_URL), str_url);
								editor.putString(getString(R.string.PREF_KEY_VERIFY_HOSTNAME), host_name);
								editor.commit();

								exec_post2();
							}
							else
							{
								errorCompanyNotFound();
							}
						} catch (JSONException | MalformedURLException e) {
							errorHttp(response);
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
		task.setVerify_hostname("almanecloud.com");
		task.addPostParam(getString(R.string.HTTP_PARAM_COMPANY_CODE), editTextCompany.getText().toString());

		// タスクを開始
		task.execute();
	}

	private void exec_post2()
	{
		// 接続先
		String strHttpUrl = pref.getString(getString(R.string.PREF_KEY_HTTP_URL), "");
		String strVerifyHostname = pref.getString(getString(R.string.PREF_KEY_VERIFY_HOSTNAME), "");
		// 非同期タスクを定義
		HttpPostTask task = new HttpPostTask(
				this,
				strHttpUrl + "/" + getString(R.string.HTTP_COMPANY_CHECK),

				// タスク完了時に呼ばれるUIのハンドラ
				new HttpPostHandler()
				{

					@Override
					public void onPostCompleted(String response)
					{
						// 受信結果をUIに表示
						if (!response.equals(""))
						{
							// 値保存
							editor = pref.edit();
							editor.putString(getString(R.string.PREF_KEY_ALCOHOL_VALUE_DIV), response);
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
		task.setVerify_hostname(strVerifyHostname);
		task.addPostParam(getString(R.string.HTTP_PARAM_COMPANY_CODE), editTextCompany.getText().toString());

		// タスクを開始
		task.execute();
	}
}
