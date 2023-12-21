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

public class SendListActivity extends Activity {

    private SharedPreferences pref;

    private Realm realm;

    RecyclerView send_list_recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_list);

        send_list_recyclerView = this.findViewById(R.id.send_list_recyclerView);

        realm = Realm.getDefaultInstance();

        // 一覧取得
        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);
        RealmResults<RealmLocalDataAlcoholResult> sendListList = realm.where(RealmLocalDataAlcoholResult.class)
                .equalTo("company_code", pref.getString(getString(R.string.PREF_KEY_COMPANY), ""))
                .findAll()
                .sort("inspection_time", Sort.ASCENDING);

        // クリック時の画面移動
        SendListAdapter.OnItemClickListener onItemClickListener = new SendListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RealmLocalDataAlcoholResult item) {
                // クリック時の処理
                if (view instanceof TextView) {
                    Intent intent = new Intent(getApplicationContext(), TransmissionContentActivity.class);
                    intent.putExtra("id", item.getId());
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (view instanceof ImageButton) {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(SendListActivity.this);
                    alertDialog.setTitle(getString(R.string.ALERT_TITLE_CONFIRM));
                    alertDialog.setMessage(getString(R.string.TEXT_QUESTION_DELETE));
                    alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                        DeleteData(item);
                    });
                    alertDialog.setNegativeButton(getString(R.string.ALERT_BTN_CANCEL), null);
                    alertDialog.show();
                }
            }
        };

        SendListAdapter adapter = new SendListAdapter(this, sendListList
                , onItemClickListener, true);

        send_list_recyclerView.setHasFixedSize(true);
        send_list_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        send_list_recyclerView.setAdapter(adapter);
    }

    private boolean DeleteData(RealmLocalDataAlcoholResult item) {
        realm.beginTransaction();
        item.deleteFromRealm();
        realm.commitTransaction();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

}