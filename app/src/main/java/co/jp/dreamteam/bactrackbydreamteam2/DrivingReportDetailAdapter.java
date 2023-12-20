package co.jp.dreamteam.bactrackbydreamteam2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class DrivingReportDetailAdapter extends RealmRecyclerViewAdapter<RealmLocalDataDrivingReportDetail, DrivingReportDetailAdapter.DrivignReportDetailViewHolder> {

    private Context context;
    private final OrderedRealmCollection<RealmLocalDataDrivingReportDetail> drivingReporDetailtList;
    private OnItemClickListener listener;

    public DrivingReportDetailAdapter(Context context, OrderedRealmCollection<RealmLocalDataDrivingReportDetail> drivingReportDetailList,
                                      OnItemClickListener onItemClickListener, boolean autoUpdate) {
        super(drivingReportDetailList, autoUpdate);
        this.context = context;
        this.drivingReporDetailtList = drivingReportDetailList;
        this.listener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return drivingReporDetailtList != null ? drivingReporDetailtList.size() : 0;
    }

    @Override
    public void onBindViewHolder(DrivignReportDetailViewHolder holder, int position) {
        RealmLocalDataDrivingReportDetail drivingReportDetail = drivingReporDetailtList != null ? drivingReporDetailtList.get(position) : null;

        if (drivingReportDetail != null) {

            holder.list_item_driving_report_detail_container.setOnClickListener(view -> {
                listener.onItemClick(drivingReportDetail);
            });

            holder.list_item_driving_report_detail_text.setText(drivingReportDetail.getDestination());
        }
    }

    @Override
    public DrivignReportDetailViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_driving_report_detail, viewGroup, false);
        return new DrivignReportDetailViewHolder(v);
    }

    public class DrivignReportDetailViewHolder extends RecyclerView.ViewHolder {
        LinearLayout list_item_driving_report_detail_container;
        TextView list_item_driving_report_detail_text;

        public DrivignReportDetailViewHolder(View view) {
            super(view);
            list_item_driving_report_detail_container = view.findViewById(R.id.list_item_driving_report_detail_container);
            list_item_driving_report_detail_text = view.findViewById(R.id.list_item_driving_report_detail_text);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(RealmLocalDataDrivingReportDetail item);
    }
}
