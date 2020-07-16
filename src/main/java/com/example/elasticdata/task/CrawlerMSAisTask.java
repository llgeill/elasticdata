package com.example.elasticdata.task;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.elasticdata.entity.CrawlerShipAis;
import com.example.elasticdata.entity.other.Megic;
import com.example.elasticdata.repository.CrawlerShipAisRepository;


import com.example.elasticdata.util.RestTemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

import java.io.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * <h3>elasticdata</h3>
 * <p>国家水上交通信息服务平台</p>
 *
 * @author : liliguang
 * @date : 2020-07-02 14:29
 **/
@Slf4j
//@Component
public class CrawlerMSAisTask {
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
    //用户cookie
    private static String cookie = null;
    //用户名
    private static final String USER_NAME = "13711919653";
    //密码
    private static final String USER_PASSWORD = ".llg123456";
    //标识
    private static final String SOURCE_FLAG = "ms";


    @Autowired
    private CrawlerShipAisRepository crawlerShipAisRepository;
    @Autowired
    private Megic megic;
    @Autowired
    private RestTemplateUtil restTemplateUtil;


    @PostConstruct
    public void init() {
        //maps = maps(103.49900, 17.15317, 124.25983, 41.43400, 80);
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
        //初始化http客户端
        proxyRestTemplate = restTemplateUtil.restTemplate();
        //获取Cookie
        cookie = login(USER_NAME, USER_PASSWORD);
        //初始化线程池
        executorService = Executors.newFixedThreadPool(megic.getMs_thread_numb());
    }

    @Scheduled(fixedDelay = 1000)
    public void task() {
        try {
            //获取数据总条数
            AtomicInteger total = new AtomicInteger();
            //下标
            AtomicInteger index = new AtomicInteger();
            //计数器
            CountDownLatch countDownLatch = new CountDownLatch(megic.getMs_thread_numb());
            //间隔大小
            int avg = tmaps.size() / megic.getMs_thread_numb();
            //开始遍历
             for (int q = 0; q < megic.getMs_thread_numb(); q++) {
                //切割地图数组
                List<double[]> maps;
                if(q==megic.getMs_thread_numb()-1)maps = tmaps.subList(q * avg,tmaps.size());
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
                                    log.info("myships - 当前位置:{} 数据去重前长度:{} 数据去重后长度:{} 总长度:{}",
                                            index, cache.get(Arrays.toString(maps.get(i))).size(), collect.size(), total.addAndGet(collect.size()));
                                } else {
                                    //log.info("myships - 当前位置:{} 区域坐标: {} --查无数据", index, Arrays.toString(maps.get(i)));
                                }
                            }catch (ResourceAccessException resourceAccessException) {
                                //更新IP代理
                                i--;
                                proxyRestTemplate = restTemplateUtil.restTemplate();
                                log.info("myships - 代理IP已经失效，正在重新更换代理！相关异常信息:{}", resourceAccessException.getMessage());
                            }catch (HttpClientErrorException httpClientErrorException){
                                i--;
                                //获取Cookie
                                cookie = login(USER_NAME, USER_PASSWORD);
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


    /**
     * 对数据去重并且更新缓存
     *
     * @param collect 爬取源数据
     * @param map       当前地图的位置
     */
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

    /**
     * 根据地图坐标爬取相关数据
     *
     * @param map 地图坐标
     * @return
     */
    public List<CrawlerShipAis> getData(double[] map) {
        //创建请求头
        HttpHeaders heads = createHeads();
        //设置cookie
        heads.add("Cookie", cookie);
        //json
        JSONObject jsonObject = new JSONObject();
        //创建参数
        StringBuilder rgn = new StringBuilder();
        for (int j = 0; j < 4; j++) {
            int v = (int) (map[j] * 600000);
            StringBuilder s = new StringBuilder(String.valueOf(v));
            if (s.length() < 8) {
                for (int q = 0; q <= 8 - s.length(); q++) s.append("0");
            } else {
                s = new StringBuilder(s.toString().substring(0, 8));
            }
            rgn.append(s.toString());
            if (j < 3) rgn.append(",");
        }
        jsonObject.put("rgn", rgn.toString());
        jsonObject.put("age", 240);
        //创建请求实体
        HttpEntity<String> httpEntity = new HttpEntity<String>(jsonObject.toString(), heads);
        //开始请求
        ResponseEntity<String> result = proxyRestTemplate.postForEntity(megic.getMs_ais(), httpEntity, String.class);
        String resultBody = result.getBody();
        //解析数据
        JSONObject parseObject = JSON.parseObject(resultBody);
        Integer count = parseObject.getInteger("count");
        Integer code = parseObject.getInteger("code");
        if (count > 0 && code == 0) {
            JSONArray jsonArray = parseObject.getJSONArray("data");
            //转换实体
            List<CrawlerShipAis> collect = jsonArray.stream().map(js -> {
                JSONObject obj = (JSONObject) js;
                CrawlerShipAis crawlerShipAis = new CrawlerShipAis();
                crawlerShipAis.setDate(LocalDateTime.ofInstant(Instant.ofEpochSecond(obj.getLong("t")), ZoneId.systemDefault()).format(dateTimeFormatter));
                crawlerShipAis.setShipName(obj.getString("e"));
                crawlerShipAis.setMmsi(obj.getString("m"));
                double lat = BigDecimal.valueOf(obj.getDouble("a") / 600000).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                double lon = BigDecimal.valueOf(obj.getDouble("n") / 600000).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                crawlerShipAis.setLocation(new GeoPoint(lat, lon));
                crawlerShipAis.setShipFlag(obj.getString("f"));
                crawlerShipAis.setShipLong(obj.getDouble("l"));
                crawlerShipAis.setShipWidth(obj.getDouble("b"));
                crawlerShipAis.setImo(obj.getString("g"));
                crawlerShipAis.setInsLoadedDraft(obj.getString("d"));
                if (obj.getDouble("c") != null)
                    crawlerShipAis.setTraceDirection(String.valueOf(obj.getDouble("c") / 10));
                crawlerShipAis.setHeading(obj.getString("h"));
                if (obj.getDouble("s") != null) crawlerShipAis.setShipSpeed(obj.getDouble("s") / 10);
                crawlerShipAis.setDataSource(SOURCE_FLAG);
                return crawlerShipAis;
            }).collect(Collectors.toList());
            return collect;
        } else {
            //没有找到数据返回null
            return null;
        }

    }

    /**
     * 用户登录
     *
     * @param username
     * @param password
     * @return
     */
    public String login(String username, String password) {
        //设置无头请求
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.addArguments("--headless");
        //设置代理
        FirefoxProfile profile = new FirefoxProfile();
        //激活手动代理配置（对应着在 profile（配置文件）中设置首选项）
        //profile.setPreference("network.proxy.type", 1);
        //ip及其端口号配置为 http 协议代理
        //profile.setPreference("network.proxy.http", RestTemplateUtil.proxyHost);
        //profile.setPreference("network.proxy.http_port", RestTemplateUtil.proxyPort);
        //所有协议共用一种 ip 及端口，如果单独配置，不必设置该项，因为其默认为 False
        profile.setPreference("network.proxy.share_proxy_settings", true);
        //默认本地地址（localhost）不使用代理，如果有些域名在访问时不想使用代理可以使用类似下面的参数设置
        profile.setPreference("network.proxy.no_proxies_on", "localhost");
        //启动驱动
        WebDriver driver = new FirefoxDriver(firefoxOptions);
        //请求首页
        driver.get("https://www.myships.com/index.html");
        //显式等待， 针对某个元素等待
        WebDriverWait wait = new WebDriverWait(driver, 15, 1);
        //等待登录输入框
        wait.until((ExpectedCondition<WebElement>) text -> text.findElement(By.id("login_name"))).sendKeys(username);
        //密码框
        driver.findElement(By.id("login_password")).sendKeys(password);
        //点击登录
        driver.findElement(By.className("login-btn")).click();
        //获取cookie
        Set<Cookie> coo = driver.manage().getCookies();
        driver.quit();
        //driver.close();
        return "access_token=" + coo.iterator().next().getValue();
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
        headers.add("Origin", "https://www.myships.com");
        headers.add("Referer", "https://www.myships.com/index.html");
        headers.add("X-Requested-With", "XMLHttpRequest");
        String[] user_agent = megic.getUser_agent();
        headers.add("User-Agent", user_agent[new Random().nextInt(user_agent.length)]);
        return headers;
    }

    /**
     * 将原本广东省的坐标区域按照level字段进行切割
     *
     * @param sjd   开始经度 (对角线)
     * @param swd   开始纬度 (对角线)
     * @param ejd   结束经度 (对角线)
     * @param ewd   结束纬度 (对角线)
     * @param level 切割级别 例如2 则切割成2*2份数据
     * @return 切割后的坐标集合
     */
    public static List<double[]> maps(double sjd, double swd, double ejd, double ewd, int level) {
        List<double[]> maps = new ArrayList<>();
        double avgjd = (ejd - sjd) / level;
        double avgwd = (ewd - swd) / level;
        double tempsjd, tempejd, tempswd, tempewd;
        for (int i = 0; i < level; i++) {
            tempsjd = sjd + avgjd * i;
            tempejd = sjd + avgjd * (i + 1);
            for (int j = 0; j < level; j++) {
                tempswd = swd + avgwd * j;
                tempewd = swd + avgwd * (j + 1);
                maps.add(new double[]{tempswd, tempsjd, tempewd, tempejd});
            }
        }
        return maps;
    }
}
