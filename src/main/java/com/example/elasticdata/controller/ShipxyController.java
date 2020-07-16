package com.example.elasticdata.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.elasticdata.entity.ShipxyQueryEntity;
import com.example.elasticdata.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * <h3>elasticdata</h3>
 * <p>船讯网接口调用</p>
 *
 * @author : liliguang
 * @date : 2020-07-06 14:06
 **/
@RestController
@RequestMapping("/shipxy")
@Slf4j
public class ShipxyController {

    private static final String KEY = "d2cbf41b7f684737b23e341be3232395";

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 以通过船名、呼号、MMSI、IMO 当中任意一个信息查询获取船舶的船名、呼号、
     * MMSI（ShipID）、IMO、来源和船舶类型等静态信息
     * @return
     */
    @RequestMapping("/queryShip")
    public Result queryShip(@RequestBody ShipxyQueryEntity shipxyQueryEntity){
        JSONArray result;
        try {
            ResponseEntity<String> forEntity = restTemplate.getForEntity(
                    "http://api.shipxy.com/apicall/QueryShip?k=" + KEY + "&enc=1&kw="+shipxyQueryEntity.getKeyword()+"&max=100",
                    String.class
            );
            JSONObject jsobj = JSON.parseObject(forEntity.getBody());
            result = jsobj.getJSONArray("data");
        }catch (Exception e){
            log.error("船舶搜索调用失败，请求参数 keyword:{}",shipxyQueryEntity.getKeyword());
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
        log.info("船舶搜索调用成功，请求参数 keyword:{}",shipxyQueryEntity.getKeyword());
        return Result.success(result);
    }


    /**
     * 单船查询
     *
     * @return
     */
    @RequestMapping("/getSingleShip")
    public Result getSingleShip(@RequestBody ShipxyQueryEntity shipxyQueryEntity){
        JSONArray result;
        try {
            ResponseEntity<String> forEntity = restTemplate.getForEntity(
                    "http://api.shipxy.com/apicall/GetSingleShip?k=" + KEY + "&enc=1&id="+shipxyQueryEntity.getKeyword(),
                    String.class
            );
            JSONObject jsobj = JSON.parseObject(forEntity.getBody());
            result = jsobj.getJSONArray("data");
        }catch (Exception e){
            log.error("单船查询调用失败，请求参数 keyword:{}",shipxyQueryEntity.getKeyword());
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
        log.info("单船查询调用成功，请求参数 keyword:{}",shipxyQueryEntity.getKeyword());
        return Result.success(result);
    }


    /**
     * 多船查询
     *
     * @return
     */
    @RequestMapping("/getManyShip")
    public Result getManyShip(@RequestBody ShipxyQueryEntity shipxyQueryEntity){
        JSONArray result;
        try {
            ResponseEntity<String> forEntity = restTemplate.getForEntity(
                    "http://api.shipxy.com/apicall/GetManyShip?k=" + KEY + "&enc=1&id="+shipxyQueryEntity.getKeyword(),
                    String.class
            );
            JSONObject jsobj = JSON.parseObject(forEntity.getBody());
            result = jsobj.getJSONArray("data");
        }catch (Exception e){
            log.error("多船查询调用失败，请求参数 keyword:{}",shipxyQueryEntity.getKeyword());
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
        log.info("多船查询调用成功，请求参数 keyword:{}",shipxyQueryEntity.getKeyword());
        return Result.success(result);
    }
}
