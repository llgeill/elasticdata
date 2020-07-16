package com.example.elasticdata.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.elasticdata.entity.CrawlerShipAis;
import com.example.elasticdata.entity.other.Megic;
import com.example.elasticdata.entity.other.Port;
import com.example.elasticdata.repository.CrawlerShipAisRepository;
import com.example.elasticdata.repository.PortRepository;
import com.example.elasticdata.util.HttpHeadersFactory;
import com.example.elasticdata.util.RestTemplateUtil;
import lombok.Data;
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
import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * <h3>elasticdata</h3>
 * <p>爬取船顺网Ais数据</p>
 *
 * @author : liliguang
 * @date : 2020-07-03 13:21
 **/
//@Component
@Data
@Slf4j
public class CrawlerShips66Ais {
    //代理http客户端
    private static RestTemplate proxyRestTemplate = null;
    //日期格式器
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    //标识
    private static final String SHIPS66 = "s66";
    //港口二维数组
    private static List<List<Port>> portsArray = new LinkedList<>();
    //线程池
    private static ExecutorService executorService = null;
    //线程池
    private static ExecutorService saveThreadPool = null;
    //地图
    private static List<double[]> maps = new LinkedList<>();
    //缓存
    private static Map<String,List<CrawlerShipAis>> cache = new ConcurrentHashMap<>();

    @Autowired
    private Megic megic;
    @Autowired
    HttpHeadersFactory httpHeadersFactory;
    @Autowired
    private CrawlerShipAisRepository crawlerShipAisRepository;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private PortRepository portRepository;
    @Autowired
    private RestTemplateUtil restTemplateUtil;
    @Autowired
    private RestTemplate restTemplate;


    @PostConstruct
    public void init() {
        proxyRestTemplate = restTemplateUtil.restTemplate();
        //需要访问的数据
        maps = maps(98, 5, 128, 42, 12);
    }


    @Scheduled(fixedDelay = 1000)
    public void getAndSavePortData() {
        //总计数
        AtomicInteger count = new AtomicInteger();
        //位置计数
        AtomicInteger index = new AtomicInteger();
        //需要访问的数据
        try {
            maps.forEach(doubles -> {
                String p1 = doubles[0] + " " + doubles[1];
                String p2 = doubles[0] + " " + doubles[3];
                String p3 = doubles[2] + " " + doubles[1];
                String p4 = doubles[2] + " " + doubles[3];
                String bbox = "MULTIPOLYGON(((" + p1 + "," + p2 + "," + p3 + "," + p4 + "," + p1 + ")))";
                HttpHeaders heads = httpHeadersFactory.createFromHeads("ais.ships66.com", "http://www.ships66.com", "http://www.ships66.com/");
                MultiValueMap<String, Object> param = createRequestParam(bbox);
                //合并请求头和请求参数
                HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param, heads);
                //开始请求
                ResponseEntity<String> result = proxyRestTemplate.postForEntity(megic.getShips66() + "?_=" + System.currentTimeMillis(), httpEntity, String.class);
                //获取结果
                String resultBody = result.getBody();
                //解析
                JSONObject jsonObject = null;
                try {
                    jsonObject = JSON.parseObject(resultBody);
                } catch (Exception e) {
                    log.info(resultBody);
                }
                //获取压缩数据
                String data = jsonObject.getString("data");
                //base64解密并gzip解压
                byte[] n = Base64.getDecoder().decode(data);
                String unCompress = unCompress(n);
                //对数据解析
                JSONArray jsonArray = JSON.parseArray(unCompress);
                //填充数据
                List<CrawlerShipAis> collect = jsonArray.stream().map(js -> {
                    JSONArray jobj = (JSONArray) js;
                    CrawlerShipAis crawlerShipAis = new CrawlerShipAis();
                    crawlerShipAis.setMmsi(jobj.getString(0));
                    String substring = jobj.getString(1).substring(6, jobj.getString(1).length() - 1);
                    String[] split = substring.split(" ");
                    crawlerShipAis.setLocation(new GeoPoint(Double.parseDouble(split[1]), Double.parseDouble(split[0])));
                    crawlerShipAis.setShipName(jobj.getString(3));
                    crawlerShipAis.setShipSpeed(jobj.getDouble(4));
                    crawlerShipAis.setShipLong(jobj.getDouble(5));
                    crawlerShipAis.setShipLong(jobj.getDouble(6));
                    crawlerShipAis.setDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(jobj.getLong(7)), ZoneId.systemDefault()).format(dateTimeFormatter));
                    crawlerShipAis.setHeading(jobj.getString(11));
                    crawlerShipAis.setCog(jobj.getString(12));
                    crawlerShipAis.setDataSource(SHIPS66);
                    return crawlerShipAis;
                }).collect(Collectors.toList());
                if(collect!=null&&collect.size()>0){
                    //缓存
                    List<CrawlerShipAis> copyArray = new LinkedList<>(collect);
                    if(cache.containsKey(bbox)){
                        List<CrawlerShipAis> ships66Data = cache.get(bbox);
                        Iterator<CrawlerShipAis> iterator = collect.iterator();
                        //去重
                        while (iterator.hasNext()){
                            Object next = iterator.next();
                            ships66Data.forEach(shipAis->{
                                if(shipAis.equals(next)){
                                    iterator.remove();
                                }
                            });
                        }
                    }
                    count.addAndGet(collect.size());
                    cache.put(bbox,copyArray);
                    crawlerShipAisRepository.saveAll(collect);
                    log.info("船顺网 - 当前位置:{} 数据去重前长度:{} 数据去重后长度:{} 总长度:{}", index.getAndIncrement(),copyArray.size(),collect.size(),count.get());
                }else{
                    //log.error("无数据: {} ", bbox);
                }
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(count.get()<1){
                proxyRestTemplate = restTemplateUtil.getRestTemplate();
            }
        }
    }


    public MultiValueMap<String, Object> createRequestParam(String bbox) {
        //设置请求参数
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<String, Object>();
        paramMap.add("change", false);
        paramMap.add("type", "SHIP");
        //paramMap.add("time",System.currentTimeMillis());
        paramMap.add("showTime", true);
        paramMap.add("lang", "zh-CN");
        paramMap.add("bbox", bbox);
        return paramMap;
    }


    /**
     * 对gzip数据进行解压缩
     */
    public static String unCompress(byte[] b) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            if (null == b || b.length <= 0) {
                return null;
            }
            // 创建一个新的 byte 数组输出流
            out = new ByteArrayOutputStream();
            // 创建一个 ByteArrayInputStream，使用 buf 作为其缓冲区数组
            ByteArrayInputStream in;
            in = new ByteArrayInputStream(b);

            // 使用默认缓冲区大小创建新的输入流
            GZIPInputStream gzip = new GZIPInputStream(in);
            byte[] buffer = new byte[1024];
            int n = 0;

            while ((n = gzip.read(buffer)) >= 0) {// 将未压缩数据读入字节数组
                // 将指定 byte 数组中从偏移量 off 开始的 len 个字节写入此 byte数组输出流
                out.write(buffer, 0, n);
            }
            // 使用指定的 charsetName，通过解码字节将缓冲区内容转换为字符串
            return out.toString("UTF-8");
        } catch (EOFException e) {
            //结束流
            //e.printStackTrace();
            return out.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
                maps.add(new double[]{tempsjd, tempswd, tempejd, tempewd});
            }
        }
        return maps;
    }
}
