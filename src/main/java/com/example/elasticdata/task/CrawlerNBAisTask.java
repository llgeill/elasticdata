package com.example.elasticdata.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.elasticdata.entity.CrawlerShipAis;
import com.example.elasticdata.entity.other.Megic;
import com.example.elasticdata.entity.other.NBTrackData;
import com.example.elasticdata.repository.CrawlerShipAisRepository;
import com.example.elasticdata.util.JavaScriptUtil;
import com.example.elasticdata.util.RestTemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
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

/**
 * <h3>elasticdata</h3>
 * <p>南宝任务</p>
 *
 * @author : liliguang
 * @date : 2020-06-23 09:50
 **/
//@Component
@Slf4j
public class CrawlerNBAisTask {

    //线程池
    private static ExecutorService executorService = null;
    //代理http客户端
    private static RestTemplate proxyRestTemplate = null;
    //地图
    private static List<double[]> maps = null;
    //日期格式器
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    //缓存
    private static Map<String,List<CrawlerShipAis>> cache = new ConcurrentHashMap<>();
    //爬取来源标识
    private static final String sourceFlag = "enb";


    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private RestTemplate restTemplateTemp;
    @Autowired
    private CrawlerShipAisRepository crawlerShipAisRepository;
    @Autowired
    private Megic megic;
    @Autowired
    private RestTemplateUtil restTemplateUtil;

    @PostConstruct
    public void init(){
        //初始化http客户端
        proxyRestTemplate = restTemplateUtil.restTemplate();
        //初始化线程池
        executorService = Executors.newFixedThreadPool(megic.getNb_thread_numb());
        //需要访问的数据
        maps = maps(103.49900, 17.15317, 124.25983, 41.43400, 12);
    }

    /**
     * 定时爬取南宝数据任务
     */
    @Scheduled(fixedDelay=1000)
    public void task(){
        try {
            //开始时间
            long start = System.currentTimeMillis();
            //收集所有数据
            //List<NBTrackData> allData = new LinkedList<>();
            //需要删除的地图数据
            List<double[]> deleteMaps = new LinkedList<>();
            //计数器
            CountDownLatch countDownLatch = new CountDownLatch(maps.size());
            //判断是否更新代理
            AtomicInteger allCount = new AtomicInteger();
            for(int i=0;i<maps.size();i++){
                int finalI = i;
                executorService.execute(()->{
                    try {
                        double[] doubles = maps.get(finalI);
                        String nbData = getNBData(doubles[0], doubles[1], doubles[2], doubles[3]);
                        if( nbData.length()>13){
                            //截取有用数据
                            nbData = nbData.substring(11, nbData.length() - 2);
                            //数据解密
                            String content = JavaScriptUtil.maphaoDecrypt(nbData);
                            JSONArray jsonArray = JSONArray.parseArray(content);
                            //数据转化
                            List<CrawlerShipAis> array = new LinkedList<>();
                            for(int k=0;k<jsonArray.size();k++){
                                //爬取对象
                                JSONObject jsonObject = jsonArray.getJSONObject(k);
                                //存储对象
                                CrawlerShipAis crawlerShipAis = new CrawlerShipAis();
                                crawlerShipAis.setDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(jsonObject.getLong("addtime")), ZoneId.systemDefault()).format(dateTimeFormatter));
                                crawlerShipAis.setLocation(new GeoPoint(jsonObject.getDouble("lat"),jsonObject.getDouble("lon")));
                                crawlerShipAis.setSog(jsonObject.getString("sog"));
                                crawlerShipAis.setCog(jsonObject.getString("cog"));
                                crawlerShipAis.setThg(jsonObject.getString("thg"));
                                crawlerShipAis.setDepth(jsonObject.getDouble("depth"));
                                crawlerShipAis.setPreArrivalPort(jsonObject.getString("dest"));
                                crawlerShipAis.setExceptiontype(jsonObject.getString("exceptiontype"));
                                crawlerShipAis.setImo(jsonObject.getString("imo"));
                                crawlerShipAis.setShipLong(jsonObject.getDouble("length"));
                                crawlerShipAis.setMmsi(jsonObject.getString("mmsi"));
                                crawlerShipAis.setShipName(jsonObject.getString("names"));
                                crawlerShipAis.setOnline(jsonObject.getString("online"));
                                crawlerShipAis.setRot(jsonObject.getString("rot"));
                                crawlerShipAis.setShipType(jsonObject.getString("type"));
                                crawlerShipAis.setUnpay(jsonObject.getString("unpay"));
                                crawlerShipAis.setShipWidth(jsonObject.getDouble("width"));
                                crawlerShipAis.setDataSource(sourceFlag);
                                array.add(crawlerShipAis);
                            }
                            if(array!=null){
                                //缓存
                                List<CrawlerShipAis> copyArray = new LinkedList<>(array);
                                if(cache.containsKey(""+doubles[0]+doubles[1]+doubles[2]+doubles[3])){
                                    List<CrawlerShipAis> nbTrackData = cache.get(""+doubles[0]+doubles[1]+doubles[2]+doubles[3]);
                                    Iterator<CrawlerShipAis> iterator = array.iterator();
                                    //去重
                                    while (iterator.hasNext()){
                                        Object next = iterator.next();
                                        nbTrackData.forEach(data->{
                                            if(data.equals(next)){
                                                iterator.remove();
                                            }
                                        });
                                    }
                                }
                                cache.put(""+doubles[0]+doubles[1]+doubles[2]+doubles[3],copyArray);
                                crawlerShipAisRepository.saveAll(array);
                                allCount.addAndGet(array.size());
                                log.info("南保- 当前位置:{} 数据去重前长度:{} 数据去重后长度:{} 总长度:{}", finalI,copyArray.size(),array.size(),allCount.get());
                            }else{
                                //判断是否是空白地图
                                if(nbData.equals("als")){
                                    deleteMaps.add(maps.get(finalI));
                                }
                                //log.error("南保- 无数据: {} {} {} {}", doubles[0], doubles[1], doubles[2], doubles[3]);
                            }
                        }
                    }catch (Exception e){
                        log.error(e.getMessage());
                    }finally {
                        countDownLatch.countDown();
                    }
                });
            }
            //栅栏等待
            countDownLatch.await();
            //保存数据
            log.info("南保-  任务结束耗时:{} s",(System.currentTimeMillis()-start)/1000);
            //重新初始化代理
            if(allCount.get() < 1){
                //初始化http客户端
                proxyRestTemplate = restTemplateUtil.restTemplate();
            }else{
                //删除空白地图
                if(deleteMaps.size()>0)maps.removeAll(deleteMaps);
                //保存数据
                //elasticsearchRestTemplate.save(allData);
            }
            log.info("南保-  任务结束耗时:{} s",(System.currentTimeMillis()-start)/1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取南宝数据(矩形坐标点)
     * @param left
     * @param bottom
     * @param right
     * @param top
     * @return
     */
    public String getNBData(Double left,Double bottom,Double right,Double top){
        //获取加密后的参数
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("left",left);
        jsonObject.put("bottom",bottom);
        jsonObject.put("right",right);
        jsonObject.put("top",top);
        //获取加密参数
        String param = JavaScriptUtil.maphaoEncrypt(jsonObject.toJSONString());
        //设置请求参数
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<String, Object>();
        paramMap.add("param",param);
        // 2、使用postForEntity请求接口
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept","application/json, text/javascript, */*; q=0.01");
        headers.add("Accept-Encoding","gzip, deflate");
        headers.add("Accept-Language","zh-CN,zh-TW;q=0.9,zh;q=0.8,ja;q=0.7,zh-HK;q=0.6,en;q=0.5");
        headers.add("Connection","keep-alive");
        headers.add("Content-Length","170");
        headers.add("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
        headers.add("Host","219.137.32.78:7302");
        headers.add("Origin","http://219.137.32.78:7302");
        headers.add("Referer","http://219.137.32.78:7302/main.html");
        headers.add("X-Requested-With","XMLHttpRequest");
        String[] user_agent = megic.getUser_agent();
        headers.add("User-Agent",user_agent[new Random().nextInt(user_agent.length)]);
        //合并请求头和请求参数
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(paramMap, headers);
        //开始请求
        ResponseEntity<String> result = proxyRestTemplate.postForEntity(megic.getNbAddress(), httpEntity, String.class);
        String resultBody = result.getBody();
        return resultBody;
    }

    /**
     * 将原本广东省的坐标区域按照level字段进行切割
     * @param sjd 开始经度 (对角线)
     * @param swd 开始纬度 (对角线)
     * @param ejd 结束经度 (对角线)
     * @param ewd 结束纬度 (对角线)
     * @param level 切割级别 例如2 则切割成2*2份数据
     * @return 切割后的坐标集合
     */
    public static List<double[]> maps(double sjd, double swd, double ejd, double ewd, int level){
        List<double[]> maps = new ArrayList<>();
        double avgjd = (ejd-sjd)/level;
        double avgwd = (ewd-swd)/level;
        double tempsjd,tempejd,tempswd,tempewd;
        for(int i=0;i<level;i++){
            tempsjd = sjd + avgjd*i;
            tempejd = sjd + avgjd*(i+1);
            for (int j=0;j<level;j++){
                tempswd = swd + avgwd*j;
                tempewd = swd + avgwd*(j+1);
                maps.add(new double[]{tempsjd,tempswd,tempejd,tempewd});
            }
        }
        return  maps;
    }

}
