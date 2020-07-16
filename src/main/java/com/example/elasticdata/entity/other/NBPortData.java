package com.example.elasticdata.entity.other;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

/**
 * <h3>elasticdata</h3>
 * <p>南宝港口数据</p>
 *
 * @author : liliguang
 * @date : 2020-06-24 13:37
 **/
@Document(indexName="nbportdata",shards = 3)
@Data
public class NBPortData {
    @Field
    private String id;
    @Field
    private String names;
    @Field
    private String pid;
    @Field
    private String geom;
    @Field
    private String type;
//    @GeoPointField
//    private GeoPoint location;
    @Field
    private String point;


}
