package co.jp.dreamteam.bactrackbydreamteam2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;

public class FinishActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_finish);

		this.findViewById(R.id.finish_btnDecision).setOnClickListener(btnDecisionClicked);
	}

	OnClickListener btnDecisionClicked = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// LocalBroadcastManagerを使ってBroadcastを送信
            Intent appFinishIntent = new Intent();
            appFinishIntent.setAction(getString(R.string.BLOADCAST_FINISH));
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(appFinishIntent);
            finish();
		}
	};
}
