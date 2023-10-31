package co.jp.dreamteam.bactrackbydreamteam2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class DrivingReportActivity extends Activity {

    private SharedPreferences pref;

    private Realm realm;
    RecyclerView driving_report_recyclerView;
    Button driving_report_btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driving_report);

        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

        driving_report_recyclerView = this.findViewById(R.id.driving_report_recyclerView);
        driving_report_btnAdd = this.findViewById(R.id.driving_report_btnAdd);
        driving_report_btnAdd.setOnClickListener(btnAddClicked);

        realm = Realm.getDefaultInstance();

        RealmResults<RealmLocalDataDrivingReport> drivingReportList = readAll();

        DrivingReportAdapter adapter = new DrivingReportAdapter(this, drivingReportList
                , item -> {
            // クリック時の処理
            // 画面移動
            Intent intent = new Intent(getApplication(), DrivingReportEditActivity.class);
            intent.putExtra("id", item.getId().intValue());
            startActivity(intent);
        }, true);

        driving_report_recyclerView.setHasFixedSize(true);
        driving_report_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        driving_report_recyclerView.setAdapter(adapter);
    }

    View.OnClickListener btnAddClicked = v -> {
        Intent intent = new Intent(getApplication(), DrivingReportEditActivity.class);
        intent.putExtra("id", 0);
        startActivity(intent);
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public RealmResults<RealmLocalDataDrivingReport> readAll() {
        return realm.where(RealmLocalDataDrivingReport.class).findAll()
                .sort("driving_start_ymd", Sort.DESCENDING)
                .sort("driving_end_ymd", Sort.DESCENDING);
    }
}