package com.example.elasticdata.entity.other;

import lombok.Data;

import java.util.Date;

/**
 * <h3>elasticdata</h3>
 * <p>ais msa</p>
 *
 * @author : liliguang
 * @date : 2020-06-15 14:45
 **/
@Data
public class Aismsa {
    //MMSI  时间  经度  纬度  船迹向_船首向 船名 * IMO * * 船速 船旗 DWT * 长 宽 呼号 * * 目的港

    private String MMSI;
    private String SJ;
    private String JD;
    private String WD;
    private String SHIP_JX_SX;
    private String SHIP_NAME;
    private String UNKNOWN_1;
    private String IMO;
    private String UNKNOWN_2;
    private String UNKNOWN_3;
    private String SHIP_SPEED;
    private String SHIP_FLAG;
    private String DWT;
    private String UNKNOWN_4;
    private String SHIP_LONG;
    private String SHIP_WIDE;
    private String UNKNOWN_6;
    private String DESTINATION;

}
