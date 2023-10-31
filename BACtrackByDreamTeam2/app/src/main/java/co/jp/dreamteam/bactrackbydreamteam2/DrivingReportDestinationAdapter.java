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

public class DrivingReportDestinationAdapter extends RealmRecyclerViewAdapter<RealmLocalDataDrivingReportDestination, DrivingReportDestinationAdapter.DrivignReportDestinationViewHolder> {

    private Context context;
    private final OrderedRealmCollection<RealmLocalDataDrivingReportDestination> drivingReporDestinationtList;
    private DrivingReportDestinationAdapter.OnItemClickListener listener;

    public DrivingReportDestinationAdapter(Context context, OrderedRealmCollection<RealmLocalDataDrivingReportDestination> drivingReportDestinationList,
                                           DrivingReportDestinationAdapter.OnItemClickListener onItemClickListener,
                                           boolean autoUpdate) {
        super(drivingReportDestinationList, autoUpdate);
        this.context = context;
        this.drivingReporDestinationtList = drivingReportDestinationList;
        this.listener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return drivingReporDestinationtList != null ? drivingReporDestinationtList.size() : 0;
    }

    @Override
    public void onBindViewHolder(DrivingReportDestinationAdapter.DrivignReportDestinationViewHolder holder, int position) {
        RealmLocalDataDrivingReportDestination drivingReportDestination = drivingReporDestinationtList != null ? drivingReporDestinationtList.get(position) : null;

        if (drivingReportDestination != null) {

            holder.list_item_driving_report_destination_container.setOnClickListener(view -> {
                listener.onItemClick(holder.list_item_driving_report_destination_text, drivingReportDestination);
            });

            holder.list_item_driving_report_destination_delete.setOnClickListener(view -> {
                listener.onItemClick(holder.list_item_driving_report_destination_delete, drivingReportDestination);
            });

            holder.list_item_driving_report_destination_text.setText(drivingReportDestination.getDestination());
        }
    }

    @Override
    public DrivingReportDestinationAdapter.DrivignReportDestinationViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_driving_report_destination, viewGroup, false);
        return new DrivingReportDestinationAdapter.DrivignReportDestinationViewHolder(v);
    }

    public class DrivignReportDestinationViewHolder extends RecyclerView.ViewHolder {
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
