package co.jp.dreamteam.bactrackbydreamteam2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class SendListActivity extends Activity {

    SharedPreferences pref;
    Realm realm;
    RecyclerView send_list_recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_list);

        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

        send_list_recyclerView = this.findViewById(R.id.send_list_recyclerView);

        realm = Realm.getDefaultInstance();

        // 一覧取得
        RealmResults<RealmLocalDataAlcoholResult> resultList = realm.where(RealmLocalDataAlcoholResult.class)
                .equalTo("company_code", pref.getString(getString(R.string.PREF_KEY_COMPANY), ""))
                .findAll()
                .sort("inspection_time", Sort.DESCENDING);

        // 過去データ削除
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,-7); // 7日減算
        String deleteDate = sdf.format(cal.getTime());

        realm.beginTransaction();
        for (RealmLocalDataAlcoholResult sendList:resultList) {
            String start_ymd = sendList.getInspection_time();
            if (start_ymd.compareTo(deleteDate) <= 0) {
                sendList.deleteFromRealm();
            }
        }
        realm.commitTransaction();

        SendListAdapter adapter = new SendListAdapter(this, resultList
                , item -> {
            // クリック時の処理
            // 画面移動
            Intent intent = new Intent(getApplication(), TransmissionContentActivity.class);
            intent.putExtra("id", item.getId().intValue());
            startActivity(intent);
        }, true);

        send_list_recyclerView.setHasFixedSize(true);
        send_list_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        send_list_recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }


}