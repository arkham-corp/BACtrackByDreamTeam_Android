package co.jp.dreamteam.bactrackbydreamteam2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class SendListAdapter extends RealmRecyclerViewAdapter<RealmLocalDataAlcoholResult, SendListAdapter.SendListViewHolder> {


    private final Context context;
    private final OrderedRealmCollection<RealmLocalDataAlcoholResult> alcoholResultList;
    private final SendListAdapter.OnItemClickListener listener;

    public SendListAdapter(Context context, OrderedRealmCollection<RealmLocalDataAlcoholResult> local_alcohol_Result_List,
                           SendListAdapter.OnItemClickListener onItemClickListener, boolean autoUpdate) {
        super(local_alcohol_Result_List, autoUpdate);
        this.context = context;
        this.alcoholResultList = local_alcohol_Result_List;
        this.listener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return alcoholResultList != null ? alcoholResultList.size() : 0;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SendListAdapter.SendListViewHolder holder, int position) {
        RealmLocalDataAlcoholResult alcoholResult = alcoholResultList != null ? alcoholResultList.get(position) : null;

        if (alcoholResult != null) {

            holder.list_item_send_list_container.setOnClickListener(view -> listener.onItemClick(alcoholResult));

            String strDate = "";
            String strTime = "";
            if (!alcoholResult.getInspection_ymd().equals("")) {
                strDate = alcoholResult.getInspection_ymd().substring(0, 4) +
                        "/" + alcoholResult.getInspection_ymd().substring(4, 6) +
                        "/" + alcoholResult.getInspection_ymd().substring(6, 8);
            }
            if (!alcoholResult.getInspection_hm().equals("")) {
                strTime = alcoholResult.getInspection_hm().substring(0, 2) +
                        ":" + alcoholResult.getInspection_hm().substring(2, 4);
            }
            holder.list_item_send_list_text.setText(strDate + "  " + strTime);

            if(alcoholResult.getSend_flg().equals("0")) {
                holder.list_item_send_list_text2.setText("未");
            } else if(alcoholResult.getSend_flg().equals("1")) {
                holder.list_item_send_list_text2.setText("NG");
            } else {
                holder.list_item_send_list_text2.setText("済");
            }
        }
    }

    @NonNull
    @Override
    public SendListAdapter.SendListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
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
