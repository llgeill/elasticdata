package com.example.elasticdata.repository;

import com.example.elasticdata.entity.other.Port;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PortRepository extends ElasticsearchRepository<Port,String> {
}
