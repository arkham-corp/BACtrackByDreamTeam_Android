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

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class DrivingReportAdapter extends RealmRecyclerViewAdapter<RealmLocalDataDrivingReport, DrivingReportAdapter.DrivignReportViewHolder> {

    private final Context context;
    private final OrderedRealmCollection<RealmLocalDataDrivingReport> drivingReportList;
    private final OnItemClickListener listener;

    public DrivingReportAdapter(Context context, OrderedRealmCollection<RealmLocalDataDrivingReport> drivingReportList, OnItemClickListener onItemClickListener, boolean autoUpdate) {
        super(drivingReportList, autoUpdate);
        this.context = context;
        this.drivingReportList = drivingReportList;
        this.listener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return drivingReportList != null ? drivingReportList.size() : 0;
    }

    @Override
    public void onBindViewHolder(@NonNull DrivignReportViewHolder holder, int position) {
        RealmLocalDataDrivingReport drivingReport = drivingReportList != null ? drivingReportList.get(position) : null;

        if (drivingReport != null) {

            holder.list_item_driving_report_container.setOnClickListener(view -> listener.onItemClick(drivingReport));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN);
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN);

            String strStartYmd= "";
            String strEndYmd = "";
            try {
                Date dateStartYmd = sdf.parse(drivingReport.getDriving_start_ymd());
                if(dateStartYmd != null) strStartYmd = sdf2.format(dateStartYmd);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            try {
                if (!drivingReport.getDriving_end_ymd().equals("")) {
                    Date dateEndYmd = sdf.parse(drivingReport.getDriving_end_ymd());
                    if(dateEndYmd != null) strEndYmd = sdf2.format(dateEndYmd);
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            String strYmd;
            if (strStartYmd.equals(strEndYmd)) {
                strYmd = strStartYmd;
            } else {
                strYmd = strStartYmd + "～" + strEndYmd;
            }
            holder.list_item_driving_report_text.setText(strYmd);

            if (drivingReport.getSendFlg().equals("1")) {
                holder.list_item_driving_report_text2.setText("済");
            } else {
                holder.list_item_driving_report_text2.setText("未");
            }
        }
    }

    @NonNull
    @Override
    public DrivignReportViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_driving_report, viewGroup, false);
        return new DrivignReportViewHolder(v);
    }

    public static class DrivignReportViewHolder extends RecyclerView.ViewHolder {
        LinearLayout list_item_driving_report_container;
        TextView list_item_driving_report_text;
        TextView list_item_driving_report_text2;

        public DrivignReportViewHolder(View view) {
            super(view);
            list_item_driving_report_container = view.findViewById(R.id.list_item_driving_report_container);
            list_item_driving_report_text = view.findViewById(R.id.list_item_driving_report_text);
            list_item_driving_report_text2 = view.findViewById(R.id.list_item_driving_report_text2);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(RealmLocalDataDrivingReport item);
    }
}
