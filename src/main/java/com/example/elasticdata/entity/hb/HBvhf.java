package com.example.elasticdata.entity.hb;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

/**
 * <h3>elasticdata</h3>
 * <p>航保水文站</p>
 *
 * @author : liliguang
 * @date : 2020-06-29 15:57
 **/
@Data
@Document(indexName = "hbvhf")
public class HBvhf {
    @Field
    private String id;
    @Field
    private String geomare;
    @Field
    private String names;
    @Field
    private String geom;
    @Field
    private String pid;
    @Field
    private String imgurl;
}
