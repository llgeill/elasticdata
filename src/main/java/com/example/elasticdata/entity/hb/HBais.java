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
@Document(indexName = "hbais")
public class HBais {
    @Field
    private String id;
    @Field
    private String typename;
    @Field
    private String geom;
    @Field
    private String markname;
    @Field
    private String pid;
    @Field
    private String marktablecode;
}
