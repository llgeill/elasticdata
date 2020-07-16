package com.example.elasticdata.task;

import com.example.elasticdata.entity.CrawlerShipAis;
import com.example.elasticdata.repository.CrawlerShipAisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <h3>elasticdata</h3>
 * <p></p>
 *
 * @author : liliguang
 * @date : 2020-07-09 13:59
 **/
//@Component
public class TestTask {

    @Autowired
    CrawlerShipAisRepository crawlerShipAisRepository;

    @Scheduled(fixedDelay = 30000)
    public void tet(){
        List<CrawlerShipAis> allByMmsi = crawlerShipAisRepository.findAllByDateBetweenAndMmsiOrderByDate("2020-07-06 00:00:00","2020-07-10 00:00:00","413906426");
        allByMmsi.forEach(s->{
            System.out.println(s);
        });
    }
}
