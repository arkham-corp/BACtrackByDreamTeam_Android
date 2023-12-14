package co.jp.dreamteam.bactrackbydreamteam2;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmLocalDataDrivingReportDestination extends RealmObject {
    @PrimaryKey
    private Integer id;
    private String destination = "";
    private String company_code = "";

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getCompany_code() {
        return company_code;
    }

    public void setCompany_code(String company_code) { this.company_code = company_code;
    }

}
