package co.jp.dreamteam.bactrackbydreamteam2;

import android.app.Application;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this); // Initialize Realm

        String test_flg = getString(R.string.TEST_FLG);

        if (test_flg.equals("1")) {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.deleteAll();

            CreateDummyData(realm);

            realm.commitTransaction();
        }
    }

    private void CreateDummyData(Realm realm) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN);

        for (int i = 0; i < 5; i++) {

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, i * -1);
            Date targetDate = calendar.getTime();

            // 初期化
            int nextId = 1;
            // userIdの最大値を取得
            Number maxId = realm.where(RealmLocalDataDrivingReport.class).max("id");
            // 1度もデータが作成されていない場合はNULLが返ってくるため、NULLチェックをする
            if (maxId != null) {
                nextId = maxId.intValue() + 1;
            }
            int driving_report_id = nextId;
            RealmLocalDataDrivingReport drivingReport = realm.createObject(RealmLocalDataDrivingReport.class, driving_report_id);
            drivingReport.setDriver_code("100");
            drivingReport.setCar_number("1234");
            drivingReport.setDriving_start_ymd(sdf.format(targetDate));
            drivingReport.setDriving_start_hm("0830");
            drivingReport.setDriving_end_ymd(sdf.format(targetDate));
            drivingReport.setDriving_end_hm("1730");
            if (i == 0 || i == 1) {
                drivingReport.setSendFlg("0");
            } else {
                drivingReport.setSendFlg("1");
            }

            realm.insert(drivingReport);

            // 明細
            for (int j = 0; j < 3; j++) {
                nextId = 1;
                // userIdの最大値を取得
                maxId = realm.where(RealmLocalDataDrivingReportDetail.class).max("id");
                // 1度もデータが作成されていない場合はNULLが返ってくるため、NULLチェックをする
                if (maxId != null) {
                    nextId = maxId.intValue() + 1;
                }

                RealmLocalDataDrivingReportDetail drivingReportDetail = realm.createObject(RealmLocalDataDrivingReportDetail.class, nextId);
                drivingReportDetail.setDriving_report_id(driving_report_id);
                drivingReportDetail.setDestination("行先" + (j + 1));
            }

        }

        // 行先
        RealmLocalDataDrivingReportDestination drivingReportDestination1 = realm.createObject(RealmLocalDataDrivingReportDestination.class, 1);
        drivingReportDestination1.setDestination("A");
        realm.insert(drivingReportDestination1);

        RealmLocalDataDrivingReportDestination drivingReportDestination2 = realm.createObject(RealmLocalDataDrivingReportDestination.class, 2);
        drivingReportDestination2.setDestination("B");
        realm.insert(drivingReportDestination2);

        RealmLocalDataDrivingReportDestination drivingReportDestination3 = realm.createObject(RealmLocalDataDrivingReportDestination.class, 3);
        drivingReportDestination3.setDestination("C");
        realm.insert(drivingReportDestination3);
    }

}
