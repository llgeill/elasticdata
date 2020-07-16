package com.example.elasticdata.repository;

import com.example.elasticdata.entity.CrawlerShipPort;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CrawlerShipPortRepository extends ElasticsearchRepository<CrawlerShipPort,String> {
}
