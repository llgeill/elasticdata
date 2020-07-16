package com.example.elasticdata.util;

import com.example.elasticdata.entity.other.Megic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * <h3>elasticdata</h3>
 * <p>创建请求头</p>
 *
 * @author : liliguang
 * @date : 2020-07-03 13:24
 **/
@Component
public class HttpHeadersFactory {

    @Autowired
    private Megic megic;
    /**
     * 创建对象头
     * @return
     */
    public HttpHeaders createFromHeads(String host, String origin, String referer){
        HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Accept","application/json, text/javascript, */*; q=0.01");
        headers.add("Accept-Encoding","gzip, deflate, br");
        headers.add("Accept-Language","zh-CN,zh-TW;q=0.9,zh;q=0.8,ja;q=0.7,zh-HK;q=0.6,en;q=0.5");
        headers.add("Connection","keep-alive");
        headers.add("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
        headers.add("Host",host);
        headers.add("Origin",origin);
        headers.add("Referer",referer);
        headers.add("X-Requested-With","XMLHttpRequest");
        String[] user_agent = megic.getUser_agent();
        headers.add("User-Agent",user_agent[new Random().nextInt(user_agent.length)]);
        return headers;
    }


    /**
     * 创建对象头
     * @return
     */
    public HttpHeaders createJSONHeads(String host, String origin, String referer){
        HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Accept","application/json, text/javascript, */*; q=0.01");
        headers.add("Accept-Encoding","gzip, deflate, br");
        headers.add("Accept-Language","zh-CN,zh-TW;q=0.9,zh;q=0.8,ja;q=0.7,zh-HK;q=0.6,en;q=0.5");
        headers.add("Connection","keep-alive");
        headers.add("Content-Type","application/json; charset=UTF-8");
        headers.add("Host",host);
        headers.add("Origin",origin);
        headers.add("Referer",referer);
        headers.add("X-Requested-With","XMLHttpRequest");
        String[] user_agent = megic.getUser_agent();
        headers.add("User-Agent",user_agent[new Random().nextInt(user_agent.length)]);
        return headers;
    }
}
