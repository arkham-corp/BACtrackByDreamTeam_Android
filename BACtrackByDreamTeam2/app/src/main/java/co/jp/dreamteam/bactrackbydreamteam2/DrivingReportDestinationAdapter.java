package co.jp.dreamteam.bactrackbydreamteam2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class DrivingReportDestinationAdapter extends RealmRecyclerViewAdapter<RealmLocalDataDrivingReportDestination, DrivingReportDestinationAdapter.DrivignReportDestinationViewHolder> {

    private final Context context;
    private final OrderedRealmCollection<RealmLocalDataDrivingReportDestination> drivingReportDestinationList;
    private final DrivingReportDestinationAdapter.OnItemClickListener listener;

    public DrivingReportDestinationAdapter(Context context, OrderedRealmCollection<RealmLocalDataDrivingReportDestination> drivingReportDestinationList,
                                           DrivingReportDestinationAdapter.OnItemClickListener onItemClickListener,
                                           boolean autoUpdate) {
        super(drivingReportDestinationList, autoUpdate);
        this.context = context;
        this.drivingReportDestinationList = drivingReportDestinationList;
        this.listener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return drivingReportDestinationList != null ? drivingReportDestinationList.size() : 0;
    }

    @Override
    public void onBindViewHolder(@NonNull DrivingReportDestinationAdapter.DrivignReportDestinationViewHolder holder, int position) {
        RealmLocalDataDrivingReportDestination drivingReportDestination = drivingReportDestinationList != null ? drivingReportDestinationList.get(position) : null;

        if (drivingReportDestination != null) {

            holder.list_item_driving_report_destination_container.setOnClickListener(view -> listener.onItemClick(holder.list_item_driving_report_destination_text, drivingReportDestination));

            holder.list_item_driving_report_destination_delete.setOnClickListener(view -> listener.onItemClick(holder.list_item_driving_report_destination_delete, drivingReportDestination));

            holder.list_item_driving_report_destination_text.setText(drivingReportDestination.getDestination());
        }
    }

    @NonNull
    @Override
    public DrivingReportDestinationAdapter.DrivignReportDestinationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_driving_report_destination, viewGroup, false);
        return new DrivignReportDestinationViewHolder(v);
    }

    public static class DrivignReportDestinationViewHolder extends RecyclerView.ViewHolder {
        LinearLayout list_item_driving_report_destination_container;
        TextView list_item_driving_report_destination_text;
        ImageButton list_item_driving_report_destination_delete;

        public DrivignReportDestinationViewHolder(View view) {
            super(view);
            list_item_driving_report_destination_container = view.findViewById(R.id.list_item_driving_report_destination_container);
            list_item_driving_report_destination_text = view.findViewById(R.id.list_item_driving_report_destination_text);
            list_item_driving_report_destination_delete = view.findViewById(R.id.list_item_driving_report_destination_delete);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, RealmLocalDataDrivingReportDestination item);
    }

}
