package com.example.elasticdata.task;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


import com.example.elasticdata.entity.CrawlerShipAis;

import com.example.elasticdata.entity.other.Megic;
import com.example.elasticdata.repository.CrawlerShipAisRepository;
import com.example.elasticdata.util.MapsUtil;
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
import org.apache.lucene.search.FieldComparator;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * <h3>elasticdata</h3>
 * <p>shipxy</p>
 *
 * @author : liliguang
 * @date : 2020-07-07 10:28
 **/
//@Component
@Slf4j
public class CrawlerShipxyTask {
    //日期格式器
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    //代理http客户端
    private static RestTemplate proxyRestTemplate = null;
    //线程池
    private ExecutorService executorService = null;
    //地图
    private static List<double[]> tmaps = new LinkedList<>();
    //缓存
    private static Map<String, List<CrawlerShipAis>> cache = new ConcurrentHashMap<>();
    //资源标识
    private static final String SOURCE_FLAG = "sxy";
    //船讯网游览器驱动
    private static WebDriver driver;

    @Autowired
    private CrawlerShipAisRepository crawlerShipAisRepository;
    @Autowired
    private Megic megic;
    @Autowired
    private RestTemplateUtil restTemplateUtil;
    @Autowired
    private RestTemplate restTemplate;

    static{
        driver = lodaDrive();
    }

    @PostConstruct
    public void init(){
        //获取
        driver = lodaDrive();
        //获取地图（暂时使用ms地图）
        try {
            Resource resource = new DefaultResourceLoader().getResource("classpath:static/ms/maps.json");
            StringBuilder sb = new StringBuilder();
            InputStream stream = resource.getInputStream();
            byte[] bytes = new byte[1024];
            int r = -1;
            while ((r = stream.read(bytes)) > 0) {
                sb.append(new String(bytes, 0, r));
            }
            tmaps = JSON.parseArray(sb.toString(), double[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //代理
        //proxyRestTemplate = restTemplateUtil.restTemplate();
        proxyRestTemplate = restTemplate;
        //初始化线程池
        executorService = Executors.newFixedThreadPool(megic.getShipxy_thread_numb());
    }

    @Scheduled(fixedDelay = 1000)
    public void task() {
        try {
            //获取数据总条数
            AtomicInteger total = new AtomicInteger();
            //下标
            AtomicInteger index = new AtomicInteger();
            //计数器
            CountDownLatch countDownLatch = new CountDownLatch(megic.getShipxy_thread_numb());
            //间隔大小
            int avg = tmaps.size() / megic.getShipxy_thread_numb();
            //开始遍历
            for (int q = 0; q < megic.getShipxy_thread_numb(); q++) {
                //切割地图数组
                List<double[]> maps;
                if(q==megic.getShipxy_thread_numb()-1)maps = tmaps.subList(q * avg,tmaps.size());
                else maps = tmaps.subList(q * avg,(q + 1) * avg);
                executorService.execute(() -> {
                    try {
                        for (int i = 0; i < maps.size(); i++) {
                            //下标+1
                            index.getAndIncrement();
                            try {
                                //根据地图坐标获取数据
                                List<CrawlerShipAis> collect = getData(maps.get(i));
                                //数据不为空则根据上一次获取的数据进行去重
                                if (collect != null && collect.size() > 0) {
                                    //去重并且更新缓存
                                    duplicateRemove(collect, maps.get(i));
                                    //保存数据
                                    crawlerShipAisRepository.saveAll(collect);
                                    log.info("船讯网 - 当前位置:{} 数据去重前长度:{} 数据去重后长度:{} 总长度:{}",
                                            index, cache.get(Arrays.toString(maps.get(i))).size(), collect.size(), total.addAndGet(collect.size()));
                                } else {
                                    //log.info("船讯网 - 当前位置:{} 区域坐标: {} --查无数据", index, Arrays.toString(maps.get(i)));
                                }
                            }catch (ResourceAccessException resourceAccessException) {
                                //更新IP代理
                                i--;
                                proxyRestTemplate = restTemplateUtil.restTemplate();
                                log.info("船讯网 - 代理IP已经失效，正在重新更换代理！相关异常信息:{}", resourceAccessException.getMessage());
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }finally {
                        countDownLatch.countDown();
                    }
                });
            }
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void duplicateRemove(List<CrawlerShipAis> collect, double[] map) {
        //缓存
        List<CrawlerShipAis> copyArray = new LinkedList<>(collect);
        if (cache.containsKey(Arrays.toString(map))) {
            List<CrawlerShipAis> ships66Data = cache.get(Arrays.toString(map));
            Iterator<CrawlerShipAis> iterator = collect.iterator();
            //去重
            while (iterator.hasNext()) {
                Object next = iterator.next();
                ships66Data.forEach(shipAis -> {
                    if (shipAis.equals(next)) {
                        iterator.remove();
                    }
                });
            }
        }
        //更新缓存
        cache.put(Arrays.toString(map), copyArray);
    }



    private List<CrawlerShipAis> getData(double[] doubles) {
        int d1 = (int) (doubles[0]*1e6);
        int d2 = (int) (doubles[1]*1e6);
        int d3 = (int) (doubles[2]*1e6);
        int d4 = (int) (doubles[3]*1e6);
        String address = "http://www.shipxy.com/ship/getareashipssimple?level=11&area="+d2+","+d1+","+d4+","+d3+"&enc=0&src=0&_t="+System.currentTimeMillis();
        HttpHeaders heads = createHeads();
        HttpEntity httpEntity = new HttpEntity(heads);
        String forEntity = proxyRestTemplate.getForObject(address,String.class,httpEntity);
        JSONObject jsonObject = JSON.parseObject(forEntity);
        List<CrawlerShipAis> array = decodeData(jsonObject.getString("data"));
        return array;
    }

    /**
     * 解密数据并返回
     * @param data
     * @return
     */

    private static List<CrawlerShipAis> decodeData(String data){
        String msg = "return L.ParseUtils.parseCanvasShipSimple('"+data+"')";
        System.out.println(msg);
        //执行脚本
        Object executeScript = ((JavascriptExecutor) driver).executeScript(msg);
        //获取响应内容
        Map map = (Map) executeScript;
        //获取详细船舶数据
        Object shipData = map.get("data");
        //强转为List
        List<Map<String,Object>> array = (List<Map<String,Object>>) shipData;
        //收集有效数据
        List<CrawlerShipAis> crawlerShipAisList = new LinkedList<>();
        //循环处理
        if(array!=null&&array.size()>0){
            for (Map<String, Object> object : array) {
                if(object.get("mmsi")==null) break;
                CrawlerShipAis crawlerShipAis = new CrawlerShipAis();
                crawlerShipAis.setMmsi(String.valueOf(object.get("mmsi")));
                crawlerShipAis.setCog(String.valueOf(object.get("cog")));
                crawlerShipAis.setDate(LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(String.valueOf(object.get("lastdyn")))), ZoneId.systemDefault()).format(dateTimeFormatter));
                crawlerShipAis.setLocation(new GeoPoint(Double.parseDouble(String.valueOf(object.get("lat"))), Double.parseDouble(String.valueOf(object.get("lng")))));
                crawlerShipAis.setShipLong(Double.parseDouble(String.valueOf(object.get("length"))));
                crawlerShipAis.setSog(String.valueOf(object.get("sog")));
                crawlerShipAis.setHeading(String.valueOf(object.get("hdg")));
                crawlerShipAis.setDataSource(SOURCE_FLAG);
                crawlerShipAisList.add(crawlerShipAis);
            }
        }
        return crawlerShipAisList;
    }


    /**
     * 初始化加载驱动
     */
    private static WebDriver lodaDrive(){
        //设置无头请求
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.addArguments("--headless");
        //设置代理
        FirefoxProfile profile = new FirefoxProfile();
        //profile.setPreference("network.proxy.http", RestTemplateUtil.proxyHost);
        //profile.setPreference("network.proxy.http_port", RestTemplateUtil.proxyPort);
        profile.setPreference("network.proxy.share_proxy_settings", true);
        //默认本地地址（localhost）不使用代理，如果有些域名在访问时不想使用代理可以使用类似下面的参数设置
        profile.setPreference("network.proxy.no_proxies_on", "localhost");
        //启动驱动
        WebDriver driver = new FirefoxDriver(firefoxOptions);
        //显式等待， 针对某个元素等待
        //WebDriverWait wait = new WebDriverWait(driver, 15, 1);
        //请求首页
        driver.get("http://www.shipxy.com/");

        return driver;
    }

    /**
     * 创建对象头
     *
     * @return
     */
    public HttpHeaders createHeads() {
        HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.add("Accept-Encoding", "gzip, deflate, br");
        headers.add("Accept-Language", "zh-CN,zh-TW;q=0.9,zh;q=0.8,ja;q=0.7,zh-HK;q=0.6,en;q=0.5");
        headers.add("Connection", "keep-alive");
        headers.add("Content-Type", "application/json;charset=UTF-8");
        headers.add("Host", "www.myships.com");
        headers.add("Referer", "http://www.shipxy.com/");
        headers.add("X-Requested-With", "XMLHttpRequest");
        String[] user_agent = megic.getUser_agent();
        headers.add("User-Agent", user_agent[new Random().nextInt(user_agent.length)]);
        return headers;
    }


}
