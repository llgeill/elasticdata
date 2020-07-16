package com.example.elasticdata.entity.other;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.Objects;

/**
 * <h3>elasticdata</h3>
 * <p>中国港口数据</p>
 *
 * @author : liliguang
 * @date : 2020-06-18 09:50
 **/
@Data
@Document(indexName = "portdata")
public class PortData {

    // 船名 经度 纬度 船首向 船迹向 * MMSI * * * 船舶类型 长 宽 * 时间 * 预到岗 航速 *
    // * 代表意思是未知字段
    @Field
    private String shipName;
    @Field
    private String location;
    @Field
    private String heading;
    @Field
    private String traceDirection;
    @Field
    private String unknown_1;
    @Field
    private String mmsi;
    @Field
    private String unknown_2;
    @Field
    private String unknown_3;
    @Field
    private String unknown_4;
    @Field
    private String shipType;
    @Field
    private String shipLong;
    @Field
    private String shipWide;
    @Field
    private String unknown_5;
    @Field
    private String date;
    @Field
    private String unknown_6;
    @Field
    private String preArrivalPort;
    @Field
    private String shipSpeed;
    @Field
    private String unknown_7;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PortData portData = (PortData) o;
        return mmsi.equals(portData.mmsi) &&
                date.equals(portData.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mmsi, date);
    }
}
