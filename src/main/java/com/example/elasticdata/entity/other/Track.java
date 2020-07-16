package com.example.elasticdata.entity.other;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.Date;

/**
 * <h3>elasticdata</h3>
 * <p>轨迹</p>
 *
 * @author : liliguang
 * @date : 2020-06-12 14:23
 **/
@Document(indexName = "track")
public class Track {
    @Field
    private String MC;
    @Field
    private float HX;
    @Field
    private float HS;
    @Field
    private String MMSI;
    @Field
    private String SJ;
    @Field
    private String location;

    private String JD;
    private String WD;

    public String getMC() {
        return MC;
    }

    public void setMC(String MC) {
        this.MC = MC;
    }

    public float getHX() {
        return HX;
    }

    public void setHX(float HX) {
        this.HX = HX;
    }

    public float getHS() {
        return HS;
    }

    public void setHS(float HS) {
        this.HS = HS;
    }

    public String getMMSI() {
        return MMSI;
    }

    public void setMMSI(String MMSI) {
        this.MMSI = MMSI;
    }

    public String getSJ() {
        return SJ;
    }

    public void setSJ(String SJ) {
        this.SJ = SJ;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getJD() {
        return JD;
    }

    public void setJD(String JD) {
        this.JD = JD;
    }

    public String getWD() {
        return WD;
    }

    public void setWD(String WD) {
        this.WD = WD;
    }
}
