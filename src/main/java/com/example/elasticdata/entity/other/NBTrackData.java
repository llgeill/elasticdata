package com.example.elasticdata.entity.other;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.Objects;

/**
 * <h3>elasticdata</h3>
 * <p>南保轨迹数据</p>
 *
 * @author : liliguang
 * @date : 2020-06-23 09:11
 **/
@Data
@Document(indexName = "nbtrackdata")
public class NBTrackData {

    private String lat;
    private String lon;
    private Long addtime;

    @Field
    private String date;
    @Field
    private String cog;
    @Field
    private String depth;
    @Field
    private String dest;
    @Field
    private String exceptiontype;
    @Field
    private String location;
    @Field
    private String imo;
    @Field
    private String length;
    @Field
    private String mmsi;
    @Field
    private String names;
    @Field
    private String online;
    @Field
    private String rot;
    @Field
    private String sog;
    @Field
    private String thg;
    @Field
    private String type;
    @Field
    private String unpay;
    @Field
    private String width;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NBTrackData that = (NBTrackData) o;
        return Objects.equals(date, that.date) &&
                Objects.equals(mmsi, that.mmsi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, mmsi);
    }
}
