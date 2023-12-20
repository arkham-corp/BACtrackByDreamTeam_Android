package co.jp.dreamteam.bactrackbydreamteam2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class SendListAdapter extends RealmRecyclerViewAdapter<RealmLocalDataAlcoholResult, SendListAdapter.SendListViewHolder> {

    Context context;
    OrderedRealmCollection<RealmLocalDataAlcoholResult> AlcoholResultList;
    OnItemClickListener listener;

    public SendListAdapter(Context context, OrderedRealmCollection<RealmLocalDataAlcoholResult> resultList, OnItemClickListener onItemClickListener, boolean autoUpdate) {
        super(resultList, autoUpdate);
        this.context = context;
        this.AlcoholResultList = resultList;
        this.listener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return AlcoholResultList != null ? AlcoholResultList.size() : 0;
    }

    @Override
    public void onBindViewHolder(@NonNull SendListViewHolder holder, int position) {
        RealmLocalDataAlcoholResult result = AlcoholResultList != null ? AlcoholResultList.get(position) : null;

        if (result != null) {

            holder.list_item_send_list_container.setOnClickListener(view -> listener.onItemClick(result));

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm", Locale.JAPAN);
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN);

            String strStartYmd;
            String strStartHm = "";
            try {
                Date dateStartYmd = sdf.parse(result.getInspection_ymd());
                strStartYmd = sdf2.format(Objects.requireNonNull(dateStartYmd));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            try {
                if (!result.getInspection_hm().equals("")) {
                    Date date_hm = sdf.parse(result.getInspection_hm());
                    strStartHm = sdf.format(Objects.requireNonNull(date_hm));
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            String strYmd = strStartYmd + "  " + strStartHm;
            holder.list_item_send_list_text.setText(strYmd);

            if (result.getSend_flg().equals("0")) {
                holder.list_item_send_list_text2.setText("未");
            } else if (result.getSend_flg().equals("1")) {
                holder.list_item_send_list_text2.setText("NG");
            } else {
                holder.list_item_send_list_text2.setText("済");
            }
        }
    }

    @NonNull
    @Override
    public SendListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_send_list, viewGroup, false);
        return new SendListViewHolder(v);
    }

    public static class SendListViewHolder extends RecyclerView.ViewHolder {
        LinearLayout list_item_send_list_container;
        TextView list_item_send_list_text;
        TextView list_item_send_list_text2;

        public SendListViewHolder(View view) {
            super(view);
            list_item_send_list_container = view.findViewById(R.id.list_item_send_list_container);
            list_item_send_list_text = view.findViewById(R.id.list_item_send_list_text);
            list_item_send_list_text2 = view.findViewById(R.id.list_item_send_list_text2);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(RealmLocalDataAlcoholResult item);
    }
}
