package co.jp.dreamteam.bactrackbydreamteam2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class DrivingReportDetailActivity extends Activity {

    int driving_report_id;

    private Realm realm;
    RecyclerView driving_report_detail_recyclerView;
    Button driving_report_detail_btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driving_report_detail);

        driving_report_id = getIntent().getIntExtra("id", 0);

        driving_report_detail_recyclerView = this.findViewById(R.id.driving_report_detail_recyclerView);
        driving_report_detail_btnAdd = this.findViewById(R.id.driving_report_detail_btnAdd);
        driving_report_detail_btnAdd.setOnClickListener(btnAddClicked);

        realm = Realm.getDefaultInstance();

        RealmLocalDataDrivingReport drivingReport = readRecordHeader();

        if (String.valueOf(drivingReport.getSendFlg()).equals("1")) {
            driving_report_detail_btnAdd.setVisibility(View.GONE);
        }

        RealmResults<RealmLocalDataDrivingReportDetail> drivingReportDetailList = readAll();

        DrivingReportDetailAdapter adapter = new DrivingReportDetailAdapter(this, drivingReportDetailList
                , item -> {
            // クリック時の処理
            // 画面移動
            Intent intent = new Intent(getApplication(), DrivingReportDetailEditActivity.class);
            intent.putExtra("driving_report_id", driving_report_id);
            intent.putExtra("id", item.getId().intValue());
            startActivity(intent);
        }, true);

        driving_report_detail_recyclerView.setHasFixedSize(true);
        driving_report_detail_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        driving_report_detail_recyclerView.setAdapter(adapter);
    }

    View.OnClickListener btnAddClicked = v -> {
        Intent intent = new Intent(getApplication(), DrivingReportDetailEditActivity.class);
        intent.putExtra("driving_report_id", driving_report_id);
        intent.putExtra("id", 0);
        startActivity(intent);
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public RealmLocalDataDrivingReport readRecordHeader() {

        long recordCount = realm.where(RealmLocalDataDrivingReport.class)
                .equalTo("id", driving_report_id)
                .count();

        if (recordCount == 0) {
            return null;
        } else {
            return realm.where(RealmLocalDataDrivingReport.class)
                    .equalTo("id", driving_report_id)
                    .findFirst();
        }
    }

    public RealmResults<RealmLocalDataDrivingReportDetail> readAll() {
        return realm.where(RealmLocalDataDrivingReportDetail.class)
                .equalTo("driving_report_id", driving_report_id)
                .findAll()
                .sort("driving_start_hm", Sort.ASCENDING)
                .sort("driving_end_hm", Sort.ASCENDING);
    }
}