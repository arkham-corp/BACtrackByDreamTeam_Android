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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
    BroadcastReceiver mReceiver;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    private Button main_btnDecision;

    /**
     * 初期処理
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // BroadcastReceiverを LocalBroadcastManagerを使って登録
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

        main_btnDecision = this.findViewById(R.id.main_btnDecision);
        main_btnDecision.setOnClickListener(btnDecisionClicked);

        // 利用規約確認
        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

        String agreement = pref.getString(getString(R.string.PREF_KEY_AGREEMENT), "");

        if (agreement.equals("")) {

            // 利用規約ダイアログ
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(
                    LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.dialog_areement,
                    findViewById(R.id.layout_root));

            // アラーとダイアログ を生成
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("利用規約");
            builder.setView(layout);
            builder.setPositiveButton(getString(R.string.TEXT_AGREE), (dialog, which) -> {
                editor = pref.edit();
                editor.putString(getString(R.string.PREF_KEY_AGREEMENT), "1");
                editor.commit();
            });
            builder.setNegativeButton(getString(R.string.TEXT_NOT_AGREE), (dialog, which) -> MainActivity.this.finish());

            // 表示
            builder.create().show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        main_btnDecision.setEnabled(true);
    }

    OnClickListener btnDecisionClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            main_btnDecision.setEnabled(false);
            Intent intent = new Intent(getApplication(), GPSActivity.class);
            startActivity(intent);
        }
    };
}
