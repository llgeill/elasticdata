package com.example.elasticdata.entity;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.io.Serializable;
import java.util.Objects;

/**
 * <h3>elasticdata</h3>
 * <p>爬虫-船舶ais数据</p>
 *
 * @author : liliguang
 * @date : 2020-07-01 11:32
 **/

@Document(indexName = "crawlershipais")
@Data
@TypeAlias("ShipAis")
public class CrawlerShipAis implements Serializable {
    @Id
    private String id;

    //数据来源
    @Field(type = FieldType.Keyword,index = false)
    private String dataSource;

    //船舶mmsi
    @Field(type = FieldType.Keyword)
    private String mmsi;

    //船舶名称
    @Field(type = FieldType.Text)
    private String shipName;

    //船舶经纬度
    @GeoPointField
    private GeoPoint location;

    //船首向
    @Field(type = FieldType.Keyword,index = false)
    private String heading;

    //船迹向
    @Field(type = FieldType.Keyword,index = false)
    private String traceDirection;

    //船舶类型
    @Field(type = FieldType.Keyword,index = false)
    private String shipType;

    //船舶长度
    @Field(type = FieldType.Double,index = false)
    private Double shipLong;

    //船舶宽度
    @Field(type = FieldType.Double,index = false)
    private Double shipWidth;

    //船舶时间
    @Field(type = FieldType.Date,format = DateFormat.date_hour_minute_second)
    private String date;

    //预到岗
    @Field(type = FieldType.Keyword,index = false)
    private String preArrivalPort;

    //航速
    @Field(type = FieldType.Keyword,index = false)
    private Double shipSpeed;

    //船舶呼号
    @Field(type = FieldType.Keyword,index = false)
    private String imo;

    //船舶旗帜
    @Field(type = FieldType.Keyword,index = false)
    private String shipFlag;

    //船舶载重量
    @Field(type = FieldType.Keyword,index = false)
    private String dwt;

    //船舶MC
    @Field(type = FieldType.Keyword,index = false)
    private String mc;

    //船舶重心
    @Field(type = FieldType.Keyword,index = false)
    private String cog;

    //船舶深度
    @Field(type = FieldType.Double,index = false)
    private Double depth;

    //船舶角速度
    @Field(type = FieldType.Keyword,index = false)
    private String rot;

    @Field(type = FieldType.Keyword,index = false)
    private String sog;

    @Field(type = FieldType.Keyword,index = false)
    private String thg;

    @Field(type = FieldType.Keyword,index = false)
    private String unpay;

    @Field(type = FieldType.Keyword,index = false)
    private String online;

    @Field(type = FieldType.Keyword,index = false)
    private String exceptiontype;
    //赤水
    @Field(type = FieldType.Keyword,index = false)
    private String insLoadedDraft;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrawlerShipAis that = (CrawlerShipAis) o;
        return Objects.equals(mmsi, that.mmsi) &&
                Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mmsi, date);
    }


}
