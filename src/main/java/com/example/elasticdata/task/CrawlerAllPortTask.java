package com.example.elasticdata.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.elasticdata.entity.CrawlerShipPort;
import com.example.elasticdata.entity.other.Megic;
import com.example.elasticdata.entity.hb.*;
import com.example.elasticdata.repository.CrawlerShipPortRepository;
import com.example.elasticdata.repository.hb.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * <h3>elasticdata</h3>
 * <p>航保通航要素爬取任务</p>
 *
 *  一周更新一次
 *
 * @author : liliguang
 * @date : 2020-06-29 13:44
 **/
//@Component
@Slf4j
public class CrawlerAllPortTask {

    //代理http客户端
    private static RestTemplate restTemplate = null;

    private static final String NB = "nb";
    private static final String ENB = "enb";

    @Autowired
    private RestTemplate restTemplateTemp;
    @Autowired
    private HBswzRepository hBswzRepository;
    @Autowired
    private HBvhfRepository hBvhfRepository;
    @Autowired
    private HBbzRepository hBbzRepository;
    @Autowired
    private HBhbRepository hBhbRepository;
    @Autowired
    private HBaisRepository hBaisRepository;
    @Autowired
    private CrawlerShipPortRepository crawlerShipPortRepository;

    @Autowired
    private Megic megic;

    //@Scheduled(cron = "0 15 10 ? * MON")
    @Scheduled(fixedDelay = 1000)
    public void hbTask(){
        //初始化http客户端
        ResponseEntity<String> forEntity = restTemplateTemp.getForEntity(megic.getJgip(), String.class);
        restTemplate = restTemplate(forEntity.getBody());
        //获取所有港口数据
        getAllPort();
        //作用不大的数据
        getAllRadio();
        getAllSwz();
        getHBais();
        getHBbz();
        getHBhb();
    }

    public void getAllPort(){
        List<CrawlerShipPort> allGKLocalFromE = getAllGKLocalFromE();
        List<CrawlerShipPort> allGKLocal = getAllGKLocal();
        allGKLocal.addAll(allGKLocalFromE);
        crawlerShipPortRepository.deleteAll();
        crawlerShipPortRepository.saveAll(allGKLocal);
    }

    /**
     * 获取南保nb港口
     */
    public  List<CrawlerShipPort> getAllGKLocalFromE(){
        //合并请求头和请求参数
        HttpHeaders headers = createHeads("219.137.32.78:7302","http://219.137.32.78:7302","http://219.137.32.78:7302/");
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> entity = restTemplateTemp.postForEntity(megic.getEhb_port(), httpEntity, String.class);
        String body = entity.getBody();
        JSONObject parse = JSON.parseObject(body);
        String result = parse.getString("result");
        JSONArray hBports = JSON.parseArray(result);
        List<CrawlerShipPort> crawlerShipPorts = new LinkedList<>();
        hBports.forEach(hb->{
            JSONObject obj = (JSONObject) hb;
            CrawlerShipPort crawlerShipPort = new CrawlerShipPort();
            crawlerShipPort.setChName(obj.getString("names"));
            String geom = obj.getString("geom");
            //对边界数据进行统一格式处理  xx,xx|xx,xx  lon lat
            geom = geom.substring(9,geom.length()-2);
            geom = geom.replaceAll(",","|").replaceAll(" ",",");
            crawlerShipPort.setBoundary(geom);
            crawlerShipPort.setPid(obj.getString("pid"));
            //对中心坐标进行处理 lat lon
            String point = obj.getString("point");
            point = point.substring(6,point.length()-1);
            String[] split = point.split(" ");
            crawlerShipPort.setLocation(split[1]+","+split[0]);
            crawlerShipPort.setDataSource(ENB);
            crawlerShipPorts.add(crawlerShipPort);
        });
        return crawlerShipPorts;
    }


    /**
     * 获取南保nb港口
     */
    public List<CrawlerShipPort> getAllGKLocal(){
        //合并请求头和请求参数
        HttpHeaders headers = createHeads();
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> entity = restTemplateTemp.postForEntity(megic.getHb_port(), httpEntity, String.class);
        String body = entity.getBody();
        JSONObject parse = JSON.parseObject(body);
        String result = parse.getString("result");
        JSONArray hBports = JSON.parseArray(result);
        List<CrawlerShipPort> crawlerShipPorts = new LinkedList<>();
        hBports.forEach(hb->{
            JSONObject obj = (JSONObject) hb;
            CrawlerShipPort crawlerShipPort = new CrawlerShipPort();
            crawlerShipPort.setChName(obj.getString("names"));
            String geom = obj.getString("geom");
            //对边界数据进行统一格式处理  xx,xx|xx,xx  lon lat
            geom = geom.substring(9,geom.length()-2);
            geom = geom.replaceAll(",","|").replaceAll(" ",",");
            crawlerShipPort.setBoundary(geom);
            crawlerShipPort.setPid(obj.getString("pid"));
            crawlerShipPort.setDataSource(NB);
            crawlerShipPorts.add(crawlerShipPort);
        });
        return crawlerShipPorts;
    }


    /**
     * 获取ais基站
     */
    public void getHBais(){
        //设置请求参数
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<String, Object>();
        paramMap.add("order","asc");
        paramMap.add("offset",0);
        paramMap.add("limit",1);
        //合并请求头和请求参数
        HttpHeaders headers = createHeads();
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(paramMap,headers);
        ResponseEntity<String> entity = restTemplateTemp.postForEntity(megic.getHb_ais(), httpEntity, String.class);
        String body = entity.getBody();
        JSONObject parse = JSON.parseObject(body);
        String total = parse.getString("total");
        //再次请求
        paramMap.put("limit", Collections.singletonList(Integer.parseInt(total)));
        ResponseEntity<String> entity2 = restTemplateTemp.postForEntity(megic.getHb_ais(), httpEntity, String.class);
        String body2 = entity2.getBody();
        JSONObject parse2 = JSON.parseObject(body2);
        String rows = parse2.getString("rows");
        List<HBais> hBais = JSON.parseArray(rows, HBais.class);
        hBaisRepository.deleteAll();
        hBaisRepository.saveAll(hBais);
    }


    /**
     * 获取航标
     */
    public void getHBhb(){
        //设置请求参数
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<String, Object>();
        paramMap.add("order","asc");
        paramMap.add("offset",0);
        paramMap.add("limit", 1);
        //合并请求头和请求参数
        HttpHeaders headers = createHeads();
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(paramMap,headers);
        ResponseEntity<String> entity = restTemplateTemp.postForEntity(megic.getHb_hb(), httpEntity, String.class);
        String body = entity.getBody();
        JSONObject parse = JSON.parseObject(body);
        String total = parse.getString("total");
        //再次请求
        paramMap.put("limit", Collections.singletonList(Integer.parseInt(total)));
        ResponseEntity<String> entity2 = restTemplateTemp.postForEntity(megic.getHb_hb(), httpEntity, String.class);
        String body2 = entity2.getBody();
        JSONObject parse2 = JSON.parseObject(body2);
        String rows = parse2.getString("rows");
        List<HBhb> hBhbs = JSON.parseArray(rows, HBhb.class);
        hBhbRepository.deleteAll();
        hBhbRepository.saveAll(hBhbs);
    }

    /**
     * 获取标注
     */
    public void getHBbz(){
        //合并请求头和请求参数
        HttpHeaders headers = createHeads();
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> entity = restTemplateTemp.postForEntity(megic.getHb_bz(), httpEntity, String.class);
        String body = entity.getBody();
        List<HBbz> hBbzs = JSON.parseArray(body,HBbz.class);
        hBbzRepository.deleteAll();
        hBbzRepository.saveAll(hBbzs);
    }

    /**
     * 获取vhf
     */
    public void getAllRadio(){
        //合并请求头和请求参数
        HttpHeaders headers = createHeads();
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> entity = restTemplateTemp.postForEntity(megic.getHb_vhf(), httpEntity, String.class);
        String body = entity.getBody();
        JSONObject parse = JSON.parseObject(body);
        String result = parse.getString("result");
        List<HBvhf> hBvhfs = JSON.parseArray(result, HBvhf.class);
        hBvhfRepository.deleteAll();
        hBvhfRepository.saveAll(hBvhfs);
    }

    /**
     * 获取水文信息
     */
    public void getAllSwz(){
        //合并请求头和请求参数
        HttpHeaders headers = createHeads();
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> entity = restTemplateTemp.postForEntity(megic.getHb_swz(), httpEntity, String.class);
        String body = entity.getBody();
        JSONObject parse = JSON.parseObject(body);
        String result = parse.getString("result");
        List<HBswz> hBswzs = JSON.parseArray(result, HBswz.class);
        hBswzRepository.deleteAll();
        hBswzRepository.saveAll(hBswzs);
    }

    /**
     * 创建对象头
     * @return
     */
    public HttpHeaders createHeads(){
        HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Accept","application/json, text/javascript, */*; q=0.01");
        headers.add("Accept-Encoding","gzip, deflate, br");
        headers.add("Accept-Language","zh-CN,zh-TW;q=0.9,zh;q=0.8,ja;q=0.7,zh-HK;q=0.6,en;q=0.5");
        headers.add("Connection","keep-alive");
        headers.add("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
        headers.add("Host","www.nhhb.org.cn");
        headers.add("Origin","https://www.nhhb.org.cn");
        headers.add("Referer","https://www.nhhb.org.cn/nbwebgis/");
        headers.add("X-Requested-With","XMLHttpRequest");
        String[] user_agent = megic.getUser_agent();
        headers.add("User-Agent",user_agent[new Random().nextInt(user_agent.length)]);
        return headers;
    }

    /**
     * 创建对象头
     * @return
     */
    public HttpHeaders createHeads(String host,String origin,String referer){
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
     * 创建Template
     */
    public static RestTemplate restTemplate(String jgip){
        //jgip
        String[] split = jgip.trim().split(":");
        String proxyHost = split[0];
        Integer proxyPort = Integer.valueOf(split[1]);
        //连接池
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        // 设置整个连接池最大连接数 根据自己的场景决定
        // todo 后面调整从配置中心获取
        connectionManager.setMaxTotal(200);
        // 路由是对maxTotal的细分
        // todo 后面调整从配置中心获取
        connectionManager.setDefaultMaxPerRoute(100);
        //生成一个设置了连接超时时间、请求超时时间、异常最大重试次数的httpClient
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(5000)
                .setConnectTimeout(10000)
                .setSocketTimeout(20000)
                .setExpectContinueEnabled(false)
                .setProxy(new HttpHost(proxyHost, proxyPort))
                .setCircularRedirectsAllowed(true) // 允许多次重定向
                .build();
        HttpClientBuilder builder = HttpClientBuilder.create()
                .setDefaultRequestConfig(config)
                .setConnectionManager(connectionManager)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
        HttpClient httpClient = builder.build();
        //使用httpClient创建一个ClientHttpRequestFactory的实现
        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        //ClientHttpRequestFactory作为参数构造一个使用作为底层的RestTemplate
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        return restTemplate;
    }



}
