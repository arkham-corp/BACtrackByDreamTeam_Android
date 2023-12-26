package co.jp.dreamteam.bactrackbydreamteam2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;

public class DrivingDivActivity extends Activity {
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    Button driving_div_btnDecision;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            // ラジオグループのオブジェクトを取得
            RadioGroup rg = (RadioGroup) findViewById(R.id.driving_div_radioGroupDrivingDiv);
            // チェックされているラジオボタンの ID を取得
            int selected_id = rg.getCheckedRadioButtonId();
            // チェックされているラジオボタンオブジェクトを取得
            String DrivingDiv = "";
            switch (selected_id) {
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
            Intent intent = new Intent(getApplication(), DriverActivity.class);
            startActivity(intent);
        }
    };

}
