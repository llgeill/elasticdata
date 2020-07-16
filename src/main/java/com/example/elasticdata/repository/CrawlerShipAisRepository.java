package com.example.elasticdata.repository;

import com.example.elasticdata.entity.CrawlerShipAis;
import com.example.elasticdata.entity.other.Port;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface CrawlerShipAisRepository extends ElasticsearchRepository<CrawlerShipAis,String> {
    List<CrawlerShipAis> findAllByDateBetweenAndMmsiOrderByDate(String startTime, String endTime, String mmsi);
}
