package co.jp.dreamteam.bactrackbydreamteam2;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmLocalDataDrivingReportDetail extends RealmObject {
    @PrimaryKey
    private Integer id;
    private Integer driving_report_id = 0;
    private String destination = "";
    private String driving_start_hm = "";
    private Double driving_start_km = 0.D;
    private String driving_end_hm = "";
    private Double driving_end_km = 0.D;
    private String cargo_weight = "";
    private String cargo_status = "";
    private String note = "";

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDriving_report_id() {
        return driving_report_id;
    }

    public void setDriving_report_id(Integer driving_report_id) {
        this.driving_report_id = driving_report_id;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDriving_start_hm() {
        return driving_start_hm;
    }

    public void setDriving_start_hm(String driving_start_hm) {
        this.driving_start_hm = driving_start_hm;
    }

    public Double getDriving_start_km() {
        return driving_start_km;
    }

    public void setDriving_start_km(Double driving_start_km) {
        this.driving_start_km = driving_start_km;
    }

    public String getDriving_end_hm() {
        return driving_end_hm;
    }

    public void setDriving_end_hm(String driving_end_hm) {
        this.driving_end_hm = driving_end_hm;
    }

    public Double getDriving_end_km() {
        return driving_end_km;
    }

    public void setDriving_end_km(Double driving_end_km) {
        this.driving_end_km = driving_end_km;
    }

    public String getCargo_weight() {
        return cargo_weight;
    }

    public void setCargo_weight(String cargo_weight) {
        this.cargo_weight = cargo_weight;
    }

    public String getCargo_status() {
        return cargo_status;
    }

    public void setCargo_status(String cargo_status) {
        this.cargo_status = cargo_status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
