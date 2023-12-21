package co.jp.dreamteam.bactrackbydreamteam2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class SendListAdapter extends RealmRecyclerViewAdapter<RealmLocalDataAlcoholResult, SendListAdapter.SendListViewHolder> {


    private Context context;
    private final OrderedRealmCollection<RealmLocalDataAlcoholResult> alcoholResultList;
    private SendListAdapter.OnItemClickListener listener;

    public SendListAdapter(Context context, OrderedRealmCollection<RealmLocalDataAlcoholResult> localalcoholResultList,
                           SendListAdapter.OnItemClickListener onItemClickListener,
                                           boolean autoUpdate) {
        super(localalcoholResultList, autoUpdate);
        this.context = context;
        this.alcoholResultList = localalcoholResultList;
        this.listener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return alcoholResultList != null ? alcoholResultList.size() : 0;
    }

    @Override
    public void onBindViewHolder(SendListAdapter.SendListViewHolder holder, int position) {
        RealmLocalDataAlcoholResult alcoholResult = alcoholResultList != null ? alcoholResultList.get(position) : null;

        if (alcoholResult != null) {

            holder.list_item_send_list_container.setOnClickListener(view -> {
                listener.onItemClick(holder.list_item_send_list_text, alcoholResult);
            });

            holder.list_item_send_list_delete.setOnClickListener(view -> {
                listener.onItemClick(holder.list_item_send_list_delete, alcoholResult);
            });

            holder.list_item_send_list_text.setText(alcoholResult.getInspection_time());
        }
    }

    @Override
    public SendListAdapter.SendListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_send_list, viewGroup, false);
        return new SendListAdapter.SendListViewHolder(v);
    }

    public class SendListViewHolder extends RecyclerView.ViewHolder {
        LinearLayout list_item_send_list_container;
        TextView list_item_send_list_text;
        ImageButton list_item_send_list_delete;

        public SendListViewHolder(View view) {
            super(view);
            list_item_send_list_container = view.findViewById(R.id.list_item_send_list_container);
            list_item_send_list_text = view.findViewById(R.id.list_item_send_list_text);
            list_item_send_list_delete = view.findViewById(R.id.list_item_send_list_delete);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, RealmLocalDataAlcoholResult item);
    }
}
