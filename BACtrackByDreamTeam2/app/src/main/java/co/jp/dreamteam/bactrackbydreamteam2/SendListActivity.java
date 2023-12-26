package co.jp.dreamteam.bactrackbydreamteam2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class SendListActivity extends Activity {

    private Realm realm;

    RecyclerView send_list_recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_list);

        send_list_recyclerView = this.findViewById(R.id.send_list_recyclerView);

        realm = Realm.getDefaultInstance();

        // 一覧取得
        SharedPreferences pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);
        RealmResults<RealmLocalDataAlcoholResult> alcoholResult = realm.where(RealmLocalDataAlcoholResult.class)
                .equalTo("company_code", pref.getString(getString(R.string.PREF_KEY_COMPANY), ""))
                .findAll()
                .sort("inspection_time", Sort.ASCENDING);

        // クリック時の画面移動
        SendListAdapter adapter = new SendListAdapter(this, alcoholResult
                , item -> {
            // クリック時の処理
            // 画面移動
            Intent intent = new Intent(getApplication(), TransmissionContentActivity.class);
            intent.putExtra("id", item.getId());
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