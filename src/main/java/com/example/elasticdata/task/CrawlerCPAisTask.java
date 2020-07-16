package com.example.elasticdata.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.example.elasticdata.entity.CrawlerShipAis;
import com.example.elasticdata.entity.other.Megic;
import com.example.elasticdata.entity.other.Port;
import com.example.elasticdata.entity.other.PortData;
import com.example.elasticdata.repository.PortRepository;
import com.example.elasticdata.util.RestTemplateUtil;
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
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <h3>elasticdata</h3>
 * <p>中国港口爬虫任务</p>
 *
 * @author : liliguang
 * @date : 2020-06-18 11:18
 **/

@Slf4j
//@Component
public class CrawlerCPAisTask {
    //标识
    private static final String  SOURCE_FLAG= "cp";
    //港口二维数组
    private static List<List<Port>> portsArray =  new LinkedList<>();
    //任务线程池
    private static  ExecutorService executorService = null;
    //保存线程池
    private static  ExecutorService saveThreadPool = null;
    //代理http客户端
    private static RestTemplate proxyRestTemplate = null;



    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private PortRepository portRepository;
    @Autowired
    private RestTemplate restTemplateTemp;
    @Autowired
    private RestTemplateUtil restTemplateUtil;
    @Autowired
    private Megic megic;


    /**
     * 对http客户端、数据集合、数据切割等进行初始化
     */
    @PostConstruct
    public void init(){
        //获取代理客户端
        proxyRestTemplate = restTemplateUtil.restTemplate();
        //获取所有港口数据
        Iterable<Port> portIterable = portRepository.findAll();
        //转化成List
        List<Port> tempPorts = new LinkedList<>();
        //边界生成港口
        portIterable.forEach(port -> {
            String boundary = port.getBoundary();
            if(!StringUtils.isEmpty(boundary)){
                JSONArray array = JSON.parseArray(boundary);
                for(int i=0;i<array.size();i++){
                    JSONArray jsonArray = array.getJSONArray(i);
                    Port clone = port.clone(i);
                    clone.setLocation(jsonArray.getString(1)+","+jsonArray.getString(0));
                    tempPorts.add(clone);
                }
            }
            tempPorts.add(port);
        });
        //去重
        List<Port> ports = tempPorts.stream().distinct().collect(Collectors.toList());
        //jc分割线
        int splitLineJC = (ports.size()/megic.getFlagNumb());
        //多进程
        ports = ports.subList(megic.getFlag()*splitLineJC,Math.min((megic.getFlag()+1)*splitLineJC,ports.size()));
        //线程数
        Integer thread_numb = megic.getThread_numb();
        //分割线
        int splitLine = (ports.size()/thread_numb);
        //缓存端口
        for(int i=0;i<thread_numb;i++){
            portsArray.add(ports.subList(i*splitLine,Math.min((i+1)*splitLine,ports.size())));
        }
        //线程池
        executorService = Executors.newFixedThreadPool(megic.getThread_numb());
        //任务线程池
        saveThreadPool = Executors.newSingleThreadExecutor();
    }

    /**
     * 开始对所有港口进行定时爬取
     */
    @Scheduled(fixedDelay=1000)
    public void task(){
        try {
            //任务开始时间
            long start = System.currentTimeMillis();
            //港口所有船舶数据集合
            List<CrawlerShipAis> allPortDataList = new LinkedList<>();
            //计数器
            CountDownLatch countDownLatch = new CountDownLatch(megic.getThread_numb());
            //循环所有港口数据
            portsArray.forEach(ports -> {
                //循环港口数据
                executorService.execute(()->{
                    try {
                        //去重后数据
                        List<CrawlerShipAis> saveData = new LinkedList<>();
                        //开启当前线程任务
                        AtomicInteger count = new AtomicInteger(1);
                        //遍历每一个港口
                        for(Port port : ports){
                            try {
                                //获取港口数据
                                List<CrawlerShipAis> portData = getAndSavePortData(port);
                                //缓存数据
                                if(portData.size() > 0){
                                    saveData.addAll(portData);
                                }
                                if(count.getAndIncrement()%5==0){
                                    //提前小去重，防止OOM
                                    saveData = saveData.stream().distinct().collect(Collectors.toList());
                                }
                            }catch (Exception e){
                                log.error("中国港口- 爬取{}失败,具体失败原因{}",port.getChName(),e.getMessage());
                            }
                        }
                        //总去重
                        List<CrawlerShipAis> collect = saveData.stream().distinct().collect(Collectors.toList());
                        //添加到主线程
                        allPortDataList.addAll(collect);
                    }catch (Exception w){
                        log.error(w.getMessage());
                        w.printStackTrace();
                    }finally {
                        //当前线程执行完毕
                        countDownLatch.countDown();
                    }
                });
            });
            //等待收集所有数据
            countDownLatch.await();
            //对总数据进行去重
            List<CrawlerShipAis> savePortData = allPortDataList.stream().distinct().collect(Collectors.toList());
            //存储
            saveThreadPool.execute(()-> saveData(savePortData));
            //判断代理问题
            if(savePortData.size()==0){
                proxyRestTemplate = restTemplateUtil.restTemplate();
            }
            log.info("中国港口- allPortDataLis:{} savePortData{}",allPortDataList.size(),savePortData.size());
            log.info("中国港口- 访问所有港口需要花费时间: {} s",(System.currentTimeMillis()-start)/1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveData(List<CrawlerShipAis> savePortData ){
        if(savePortData.size()>0){
            int size = (savePortData.size() / 2000) + 1;
            for(int i=0;i<size;i++){
                List<CrawlerShipAis> portDataList = savePortData.subList(i * 2000, Math.min((i + 1) * 2000, savePortData.size()));
                elasticsearchRestTemplate.save(portDataList);
                log.info("异步存储 起始位置:{} 大小:{}",i+1,portDataList.size());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 根据港口位置获取相关的经纬度坐标
     * @param port
     */
    public List<CrawlerShipAis> getAndSavePortData(Port port){
        //获取港口的经纬度坐标
        String location = port.getLocation();
        if(StringUtils.isEmpty(location)||location.equals("[]")) return null;
        //切割经纬度字符串
        String[] xy = location.split(",");
        //设置请求参数
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<String, Object>();
        paramMap.add("method","poszoom");
        paramMap.add("center_x",xy[1]);
        paramMap.add("center_y",xy[0]);
        paramMap.add("param1",true);
        paramMap.add("pos",1);
        paramMap.add("type",0);
        paramMap.add("map_type","wmap");
        paramMap.add("resolution","152.8740565703525");
        paramMap.add("zoom",11);
        //paramMap.add("resolution","106.43702828517625");
        //paramMap.add("zoom",10);
        // 2、使用postForEntity请求接口
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept","*/*");
        headers.add("Accept-Encoding","gzip, deflate");
        headers.add("Accept-Language","zh-CN,zh-TW;q=0.9,zh;q=0.8,ja;q=0.7,zh-HK;q=0.6,en;q=0.5");
        headers.add("Connection","keep-alive");
        headers.add("Content-Length","130");
        headers.add("Content-Type","application/x-www-form-urlencoded");
        headers.add("Host","ship.chinaports.com");
        headers.add("Origin","http://ship.chinaports.com");
        headers.add("Referer","http://ship.chinaports.com/");
        headers.add("X-Requested-With","XMLHttpRequest");
        String[] user_agent = megic.getUser_agent();
        headers.add("User-Agent",user_agent[new Random().nextInt(user_agent.length)]);
        //合并请求头和请求参数
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(paramMap, headers);
        //开始请求
        ResponseEntity<String> result = proxyRestTemplate.postForEntity(megic.getAddress(), httpEntity, String.class);
        String resultBody = result.getBody();
        String substring = resultBody.substring(9, resultBody.length() - 1);
        JSONArray jsonArray = JSON.parseArray(substring);
        List<CrawlerShipAis> dataList = jsonArray.stream().map(s -> {
            JSONArray data = (JSONArray) s;
            CrawlerShipAis crawlerShipAis = new CrawlerShipAis();
            crawlerShipAis.setShipName(data.getString(0));
            crawlerShipAis.setLocation(new GeoPoint(data.getDouble(2),data.getDouble(1)));
            crawlerShipAis.setHeading(data.getString(3));
            crawlerShipAis.setTraceDirection(data.getString(4));
            crawlerShipAis.setMmsi(data.getString(6));
            crawlerShipAis.setShipType(data.getString(10));
            crawlerShipAis.setShipLong(data.getDouble(11));
            crawlerShipAis.setShipWidth(data.getDouble(12));
            crawlerShipAis.setDate(data.getString(14));
            crawlerShipAis.setPreArrivalPort(data.getString(16));
            crawlerShipAis.setShipSpeed(data.getDouble(17));
            crawlerShipAis.setDataSource(SOURCE_FLAG);
            return crawlerShipAis;
        }).collect(Collectors.toList());
        log.info("中国港口- 成功爬取区域名称:{} 经度:{} 纬度:{} 数据量:{}",port.getChName(),xy[1],xy[0],dataList.size());
        return dataList;
    }

    /**
     * 创建Template
     */
    public static RestTemplate restTemplate(String jgip){
        //代理IP
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
