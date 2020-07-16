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
@Document(indexName = "hbbz")
public class HBbz {
    @Field
    private String id;
    @Field
    private String user_name;
    @Field
    private String edit_time;
    @Field
    private String mark_info;
    @Field
    private String create_time;
    @Field
    private String mark_name;
    @Field
    private String geom;
    @Field
    private String type;
    @Field
    private String mark_icon;
}
