package co.jp.dreamteam.bactrackbydreamteam2;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class DrivingDivActivity extends Activity
{
	BroadcastReceiver mReceiver;

	SharedPreferences pref;
	SharedPreferences.Editor editor;

	Button driving_div_btnDecision;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_driving_div);

		driving_div_btnDecision = this.findViewById(R.id.driving_div_btnDecision);
		driving_div_btnDecision.setOnClickListener(btnDecisionClicked);

		pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		driving_div_btnDecision.setEnabled(true);
	}

	OnClickListener btnDecisionClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// ラジオグループのオブジェクトを取得
			RadioGroup rg = (RadioGroup)findViewById(R.id.driving_div_radioGroupDrivingDiv);
			// チェックされているラジオボタンの ID を取得
			int selected_id = rg.getCheckedRadioButtonId();
			// チェックされているラジオボタンオブジェクトを取得
			RadioButton selected_radio = (RadioButton)findViewById(selected_id);
			String DrivingDiv = "";
			switch(selected_id){
				case R.id.driving_div_radioGroupDrivingDiv_0:
					DrivingDiv = "0";
					break;
				case R.id.driving_div_radioGroupDrivingDiv_1:
					DrivingDiv = "1";
					break;
			}
			// 値保存
			editor = pref.edit();
			editor.putString(getString(R.string.PREF_KEY_DRIVING_DIV), DrivingDiv);
			editor.commit();

			// 画面移動
			Intent intent = new Intent(getApplication(), GPSActivity.class);
			startActivity(intent);
		}
	};

}
