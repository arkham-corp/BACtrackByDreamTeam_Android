package co.jp.dreamteam.bactrackbydreamteam2;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmLocalDataAlcoholResult extends RealmObject {
    @PrimaryKey
    private long id = 0;
    private String company_code = "";
    private String inspection_time = "";
    private String inspection_ymd = "";
    private String inspection_hm = "";
    private String driver_code = "";
    private String car_number = "";
    private String location_name = "";
    private String location_lat = "0.0";
    private String location_long = "0.0";
    private String driving_div = "0";
    private String alcohol_value = "0";
    private String blood_alcohol_value = "0";
    private String breath_alcohol_value = "0";
    private String photo_file = "";
    private String backtrack_id = "";
    private String use_number = "";
    private String send_flg = "";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompany_code() {
        return company_code;
    }

    public void setCompany_code(String company_code) {
        this.company_code = company_code;
    }

    public String getInspection_time() {
        return inspection_time;
    }

    public void setInspection_time(String inspection_time) {
        this.inspection_time = inspection_time;
    }

    public String getInspection_ymd() {
        return inspection_ymd;
    }

    public void setInspection_ymd(String inspection_ymd) {
        this.inspection_ymd = inspection_ymd;
    }

    public String getInspection_hm() {
        return inspection_hm;
    }

    public void setInspection_hm(String inspection_hm) {
        this.inspection_hm = inspection_hm;
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

    public String getLocation_name() {
        return location_name;
    }

    public void setLocation_name(String location_name) {
        this.location_name = location_name;
    }

    public String getLocation_lat() {
        return location_lat;
    }

    public void setLocation_lat(String location_lat) {
        this.location_lat = location_lat;
    }

    public String getLocation_long() {
        return location_long;
    }

    public void setLocation_long(String location_long) {
        this.location_long = location_long;
    }

    public String getDriving_div() {
        return driving_div;
    }

    public void setDriving_div(String driving_div) {
        this.driving_div = driving_div;
    }

    public String getAlcohol_value() {
        return alcohol_value;
    }

    public void setAlcohol_value(String alcohol_value) {
        this.alcohol_value = alcohol_value;
    }

    public String getBlood_alcohol_value() {
        return blood_alcohol_value;
    }

    public void setBlood_alcohol_value(String blood_alcohol_value) {
        this.blood_alcohol_value = blood_alcohol_value;
    }

    public String getBreath_alcohol_value() {
        return breath_alcohol_value;
    }

    public void setBreath_alcohol_value(String breath_alcohol_value) {
        this.breath_alcohol_value = breath_alcohol_value;
    }

    public String getPhoto_file() {
        return photo_file;
    }

    public void setPhoto_file(String photo_file) {
        this.photo_file = photo_file;
    }

    public String getBacktrack_id() {
        return backtrack_id;
    }

    public void setBacktrack_id(String backtrack_id) {
        this.backtrack_id = backtrack_id;
    }

    public String getUse_number() {
        return use_number;
    }

    public void setUse_number(String use_number) {
        this.use_number = use_number;
    }

    public String getSend_flg() {
        return send_flg;
    }

    public void setSend_flg(String send_flg) {
        this.send_flg = send_flg;
    }
}
