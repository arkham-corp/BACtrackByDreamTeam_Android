package co.jp.dreamteam.bactrackbydreamteam2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;


public class DrivingReportDestinationActivity extends Activity {

    private SharedPreferences pref;

    private Realm realm;

    RecyclerView driving_report_destination_recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driving_report_destination);

        driving_report_destination_recyclerView = this.findViewById(R.id.driving_report_destination_recyclerView);

        realm = Realm.getDefaultInstance();

//20231211
//        RealmResults<RealmLocalDataDrivingReportDestination> drivingReportDestinationList = readAll();
        // 一覧取得
        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);
        RealmResults<RealmLocalDataDrivingReportDestination> drivingReportDestinationList = realm.where(RealmLocalDataDrivingReportDestination.class)
                .equalTo("company_code", pref.getString(getString(R.string.PREF_KEY_COMPANY), ""))
                .findAll()
                .sort("destination", Sort.ASCENDING);
//20231211

        DrivingReportDestinationAdapter.OnItemClickListener onItemClickListener = new DrivingReportDestinationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RealmLocalDataDrivingReportDestination item) {
                // クリック時の処理
                if (view instanceof TextView) {
                    Intent intent = new Intent(getApplicationContext(), DrivingReportDetailEditActivity.class);
                    intent.putExtra(DrivingReportDetailEditActivity.EXTRA_MESSAGE, item.getDestination());
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (view instanceof ImageButton) {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(DrivingReportDestinationActivity.this);

                    // ダイアログの設定
                    alertDialog.setTitle(getString(R.string.ALERT_TITLE_CONFIRM));
                    alertDialog.setMessage(getString(R.string.TEXT_QUESTION_DELETE));

                    // OK(肯定的な)ボタンの設定
                    alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                        // OKボタン押下時の処理
                        DeleteData(item);
                    });

                    alertDialog.setNegativeButton(getString(R.string.ALERT_BTN_CANCEL), null);

                    alertDialog.show();
                }
            }
        };

        DrivingReportDestinationAdapter adapter = new DrivingReportDestinationAdapter(this, drivingReportDestinationList
                , onItemClickListener, true);

        driving_report_destination_recyclerView.setHasFixedSize(true);
        driving_report_destination_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        driving_report_destination_recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public RealmResults<RealmLocalDataDrivingReportDestination> readAll() {
        return realm.where(RealmLocalDataDrivingReportDestination.class)
                .findAll()
                .sort("destination", Sort.ASCENDING);
    }

    private boolean DeleteData(RealmLocalDataDrivingReportDestination item) {
        realm.beginTransaction();
        item.deleteFromRealm();
        realm.commitTransaction();
        return true;
    }
}