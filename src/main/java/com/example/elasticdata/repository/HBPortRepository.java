package com.example.elasticdata.repository;

import com.example.elasticdata.entity.hb.HBport;
import com.example.elasticdata.entity.other.Port;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface HBPortRepository extends ElasticsearchRepository<HBport,String> {
}
