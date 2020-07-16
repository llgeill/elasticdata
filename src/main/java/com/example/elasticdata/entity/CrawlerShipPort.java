package com.example.elasticdata.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * <h3>elasticdata</h3>
 * <p>航保水文站</p>
 *
 * @author : liliguang
 * @date : 2020-06-29 15:57
 **/
@Data
@TypeAlias("CrawlerShipPort")
@Document(indexName = "crawlershipport")
public class CrawlerShipPort {
    @Id
    private String id;
    @Field(type = FieldType.Keyword,index = false)
    private String pid;
    //数据来源
    @Field(type = FieldType.Keyword,index = false)
    private String dataSource;
    @Field
    private String pyName;
    @Field
    private String chName;
    @Field(type = FieldType.Text,index = false)
    private String location;
    @Field(type = FieldType.Text,index = false)
    private String boundary;
}
