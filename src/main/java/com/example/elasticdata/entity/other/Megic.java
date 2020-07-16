package com.example.elasticdata.entity.other;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <h3>elasticdata</h3>
 * <p></p>
 *
 * @author : liliguang
 * @date : 2020-06-17 15:08
 **/
@Component
@ConfigurationProperties(prefix = "winfo.megic")
@Data
public class Megic {
    private Integer thread_numb;
    private Integer flag;
    private Integer flagNumb;
    private String address;
    private String[] user_agent;
    private String jgip;

    private String nbAddress;
    private Integer nb_thread_numb;
    private Integer ms_thread_numb;

    private String shipxy_address;
    private Integer shipxy_thread_numb;



    private String hb_swz;
    private String hb_vhf;
    private String hb_bz;
    private String hb_hb;
    private String hb_ais;
    private String hb_port;
    private String ehb_port;

    private String ms_ais;


    private String ships66;
}
