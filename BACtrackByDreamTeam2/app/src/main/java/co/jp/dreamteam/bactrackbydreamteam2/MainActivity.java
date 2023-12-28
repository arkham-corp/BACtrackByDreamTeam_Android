package co.jp.dreamteam.bactrackbydreamteam2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

public class MainActivity extends Activity {

    final int REQUEST_CODE_START_UPDATE_FLOW = 1;
    private static final String TAG = "MainActivity";

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

        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

        // 更新確認
        UpdateCheck();

        main_btnDecision = this.findViewById(R.id.main_btnDecision);
        main_btnDecision.setOnClickListener(btnDecisionClicked);

        // 利用規約確認
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


        //インターネット接毒出来ない場合の測定継続判断
        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);
        editor = pref.edit();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
        if (capabilities != null) {
            //インターネットに接続あり

            editor.putString(getString(R.string.PREF_KEY_STATUS), "0");
            main_btnDecision.setEnabled(true);

        } else {
            //インターネットに接続なし

            editor.putString(getString(R.string.PREF_KEY_STATUS), "1");

            android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle(getString(R.string.ALERT_TITLE_SESSION_ERROR));
            alertDialog.setMessage(getString(R.string.TEXT_QUESTION_CONTINUE));
            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_YES), (dialog, which) -> {
                main_btnDecision.setEnabled(true);
            });
            alertDialog.setNegativeButton(getString(R.string.ALERT_BTN_NO), (dialog, which) -> {
                main_btnDecision.setEnabled(false);
            });
            alertDialog.show();
        }
        editor.commit();

    }

    public void UpdateCheck() {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);

        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // This example applies an immediate update. To apply a flexible update
                    // instead, pass in AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                // Request the update.
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
                            appUpdateInfo,
                            // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                            AppUpdateType.IMMEDIATE,
                            // The current activity making the update request.
                            this,
                            // Include a request code to later monitor this update request.
                            REQUEST_CODE_START_UPDATE_FLOW);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_START_UPDATE_FLOW) {
            if (resultCode != RESULT_OK) {
                Log.d(TAG, getString(R.string.UPDATE_FILED));
            }
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
            Intent intent = new Intent(getApplication(), CompanyActivity.class);
            startActivity(intent);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }


}
