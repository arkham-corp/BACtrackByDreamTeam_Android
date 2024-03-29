package co.jp.dreamteam.bactrackbydreamteam2;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmLocalDataDrivingReport extends RealmObject {
    @PrimaryKey
    private Integer id;
    private String company_code = "";
    private String driver_code = "";
    private String car_number = "";
    private String driving_start_ymd = "";
    private String driving_start_hm = "";
    private String driving_end_ymd = "";
    private String driving_end_hm = "";
    private Double driving_start_km = 0.D;
    private Double driving_end_km = 0.D;
    private String refueling_status = "";
    private String abnormal_report = "";
    private String instruction = "";
    private String free_fld1 = "";
    private String free_fld2 = "";
    private String free_fld3 = "";
    private String free_title1 = "";
    private String free_title2 = "";
    private String free_title3 = "";
    private String send_flg = "";

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCompany_code() {
        return company_code;
    }

    public void setCompany_code(String company_code) {
        this.company_code = company_code;
    }

    public String getDriver_code() {
        return driver_code;
    }

    public void setDriver_code(String driver_code) {
        this.driver_code = driver_code;
    }

    public String getCar_number() {
        return car_number;
    }

    public void setCar_number(String car_number) {
        this.car_number = car_number;
    }

    public String getDriving_start_ymd() {
        return driving_start_ymd;
    }

    public void setDriving_start_ymd(String driving_start_ymd) {
        this.driving_start_ymd = driving_start_ymd;
    }

    public String getDriving_start_hm() {
        return driving_start_hm;
    }

    public void setDriving_start_hm(String driving_start_hm) {
        this.driving_start_hm = driving_start_hm;
    }

    public String getDriving_end_ymd() {
        return driving_end_ymd;
    }

    public void setDriving_end_ymd(String driving_end_ymd) {
        this.driving_end_ymd = driving_end_ymd;
    }

    public String getDriving_end_hm() {
        return driving_end_hm;
    }

    public void setDriving_end_hm(String driving_end_hm) {
        this.driving_end_hm = driving_end_hm;
    }

    public Double getDriving_start_km() {
        return driving_start_km;
    }

    public void setDriving_start_km(Double driving_start_km) {
        this.driving_start_km = driving_start_km;
    }

    public Double getDriving_end_km() {
        return driving_end_km;
    }

    public void setDriving_end_km(Double driving_end_km) {
        this.driving_end_km = driving_end_km;
    }

    public String getRefueling_status() {
        return refueling_status;
    }

    public void setRefueling_status(String refueling_status) {
        this.refueling_status = refueling_status;
    }

    public String getAbnormal_report() {
        return abnormal_report;
    }

    public void setAbnormal_report(String abnormal_report) {
        this.abnormal_report = abnormal_report;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getFree_fld1() {
        return free_fld1;
    }

    public void setFree_fld1(String free_fld1) {
        this.free_fld1 = free_fld1;
    }

    public String getFree_fld2() {
        return free_fld2;
    }

    public void setFree_fld2(String free_fld2) {
        this.free_fld2 = free_fld2;
    }

    public String getFree_fld3() {
        return free_fld3;
    }

    public void setFree_fld3(String free_fld3) {
        this.free_fld3 = free_fld3;
    }

    public String getFree_title1() {
        return free_title1;
    }

    public void setFree_title1(String free_title1) {
        this.free_title1 = free_title1;
    }

    public String getFree_title2() {
        return free_title2;
    }

    public void setFree_title2(String free_title2) {
        this.free_title2 = free_title2;
    }

    public String getFree_title3() {
        return free_title3;
    }

    public void setFree_title3(String free_title3) {
        this.free_title3 = free_title3;
    }

    public String getSendFlg() {
        return send_flg;
    }

    public void setSendFlg(String send_flg) {
        this.send_flg = send_flg;
    }

}
