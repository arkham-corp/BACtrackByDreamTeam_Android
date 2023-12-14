package co.jp.dreamteam.bactrackbydreamteam2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
//20231211
        // 一覧取得
        RealmResults<RealmLocalDataDrivingReport> drivingReportList = realm.where(RealmLocalDataDrivingReport.class)
            .equalTo("company_code", pref.getString(getString(R.string.PREF_KEY_COMPANY), ""))
            .findAll()
            .sort("driving_start_ymd", Sort.DESCENDING)
            .sort("driving_end_ymd", Sort.DESCENDING);

        //RealmResults<RealmLocalDataDrivingReport> drivingReportList = readAll();
//20231211

        // 過去データ削除
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,-7); // 7日減算
        String deleteDate = sdf.format(cal.getTime());

        realm.beginTransaction();
        for (RealmLocalDataDrivingReport drivingReport:drivingReportList) {
//20231211   if (drivingReport.getSendFlg().equals("1")) {
            String start_ymd = drivingReport.getDriving_start_ymd();
            if (start_ymd.compareTo(deleteDate) <= 0) {
                RealmResults<RealmLocalDataDrivingReportDetail> list = realm.where(RealmLocalDataDrivingReportDetail.class)
                        .equalTo("driving_report_id", drivingReport.getId())
                        .findAll();
                list.deleteAllFromRealm();
                drivingReport.deleteFromRealm();
            }
//20231211   }
        }
        realm.commitTransaction();

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

    private RealmResults<RealmLocalDataDrivingReport> readAll() {
        return realm.where(RealmLocalDataDrivingReport.class).findAll()
                .sort("driving_start_ymd", Sort.DESCENDING)
                .sort("driving_end_ymd", Sort.DESCENDING);
    }

}