package com.example.elasticdata;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.elasticdata.entity.hb.HBport;
import com.example.elasticdata.entity.other.Megic;
import com.example.elasticdata.entity.other.NBPortData;
import com.example.elasticdata.entity.other.Port;
import com.example.elasticdata.entity.other.PortData;
import com.example.elasticdata.repository.NBPortDataRepository;
import com.example.elasticdata.repository.PortRepository;
import com.example.elasticdata.repository.hb.HBportRepository;
import com.example.elasticdata.util.ExecutorsUtil;
import com.example.elasticdata.util.JavaScriptUtil;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@SpringBootTest
@Slf4j
class ElasticdataApplicationTests222 {
    private static final ExecutorService executorService = ExecutorsUtil.instance(5);

    private static RestTemplate proxyRestTemplate = null;

    @Autowired
    ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    PortRepository portRepository;

    @Autowired
    Megic megic;




    @Test
    public void port(){
        //设置请求参数
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<String, Object>();
        paramMap.add("method","initPort");
        paramMap.add("left_x","100.37918090820311");
        paramMap.add("left_y","43.034793466089553");
        paramMap.add("right_x","128.90652465820312");
        paramMap.add("right_y","17.794633839268798");
        paramMap.add("map_type","wmap");
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
        ResponseEntity<String> result = restTemplate.postForEntity(megic.getAddress(), httpEntity, String.class);
        String resultBody = result.getBody();
        JSONArray jsonArray = JSON.parseArray(resultBody);
        //转换实体对象
        List<Port> ports = jsonArray.stream().map(s -> {
            JSONArray data = (JSONArray) s;
            Port port = new Port();
            port.setId(data.getString(0));
            port.setPyName(data.getString(1));
            port.setChName(data.getString(2));
            port.setLocation(data.getString(4) + "," + data.getString(3));
            port.setBoundary(data.getString(5));
            return port;
        }).collect(Collectors.toList());
        //保存ES实体
        elasticsearchRestTemplate.save(ports);
    }

    @Autowired
    HBportRepository hBportRepository;

    @Autowired
    NBPortDataRepository nbPortDataRepository;

    @Test
    public void nbport() throws IOException {
        Iterable<NBPortData> all = nbPortDataRepository.findAll();
        List<NBPortData> hBports = new LinkedList<>();
        all.forEach(hb->hBports.add(hb));
        String string = JSON.toJSONString(hBports);
        File file = new File("nbport.json");
        FileOutputStream out = new FileOutputStream(file);
        out.write(string.getBytes());
        out.close();
    }

    @Test
    public void rnbport() throws IOException {

        File file = new File("nbport.json");
        FileInputStream inputStream = new FileInputStream(file);
        byte[] bb = new byte[1024];
        int read = -1;
        StringBuffer str = new StringBuffer();
        while (-1 != (read = inputStream.read(bb))){
            str.append(new String(bb,0,read));
        }
        JSONArray array = JSON.parseArray(str.toString());
        System.out.println(array.size());
    }

    @Test
    public void t23(){
        //获取所有港口数据
        Iterable<Port> portIterable = portRepository.findAll();
        //转化成List
        List<Port> tempPorts = new ArrayList<>();
        //边界生成港口
        Port port = portIterable.iterator().next();

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

        List<List<PortData>> sd = new ArrayList<>();
        tempPorts.forEach(port1 -> {
            List<PortData> andSavePortData = getAndSavePortData(port1);
            sd.add(andSavePortData);
        });
        System.out.println(1);


    }

    @Test
    public void tesasd(){
        //获取所有港口数据
        Iterable<Port> portIterable = portRepository.findAll();
        portIterable.forEach(data->{
            log.info(data.toString());
            String boundary = data.getBoundary();
            JSONArray array = JSON.parseArray(boundary);
            System.out.println(data.getLocation().split(",")[1]+","+data.getLocation().split(",")[0]);
            for(int i=0;i<array.size();i++){
                JSONArray jsonArray = array.getJSONArray(i);
                System.out.println(jsonArray.getString(0)+","+jsonArray.getString(1));
            }
            System.out.println();
        });
    }






    @Test
    public void test(){
        //设置请求参数
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<String, Object>();
        paramMap.add("method","poszoom");
        paramMap.add("center_x","108.27103703982898");
        paramMap.add("center_y","21.05720368852767");
        paramMap.add("resolution","152.8740565703525");
        paramMap.add("param1",true);
        paramMap.add("pos",1);
        paramMap.add("type",0);
        paramMap.add("map_type","wmap");
        paramMap.add("zoom",11);
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
        ResponseEntity<String> result = restTemplate.postForEntity("http://192.168.62.1:9000/test",httpEntity, String.class);
        String resultBody = result.getBody();
        String substring = resultBody.substring(9, resultBody.length() - 1);
        JSONArray jsonArray = JSON.parseArray(substring);
        List<PortData> dataList = jsonArray.stream().map(s -> {
            JSONArray data = (JSONArray) s;
            PortData portData = new PortData();
            portData.setShipName(data.getString(0));
            portData.setLocation(data.getString(2) + "," + data.getString(1));
            portData.setHeading(data.getString(3));
            portData.setTraceDirection(data.getString(4));
            portData.setUnknown_1(data.getString(5));
            portData.setMmsi(data.getString(6));
            portData.setUnknown_2(data.getString(7));
            portData.setUnknown_3(data.getString(8));
            portData.setUnknown_4(data.getString(9));
            portData.setShipType(data.getString(10));
            portData.setShipLong(data.getString(11));
            portData.setShipWide(data.getString(12));
            portData.setUnknown_5(data.getString(13));
            portData.setDate(data.getString(14));
            portData.setUnknown_6(data.getString(15));
            portData.setPreArrivalPort(data.getString(16));
            portData.setShipSpeed(data.getString(17));
            portData.setUnknown_7(data.getString(18));
            return portData;
        }).collect(Collectors.toList());
        elasticsearchRestTemplate.save(dataList);
        System.out.println(jsonArray);
    }

    @Test
    public void teasd(){
        List<double[]> maps = maps(114, 19, 127, 42, 30);
        List<double[]> maps1 = maps(99, 25, 114, 33, 30);
        List<double[]> maps2 = maps(105, 15, 114, 26, 30);

        maps.addAll(maps1);
        maps.addAll(maps2);

        List<double[]> collect = maps.stream().distinct().collect(Collectors.toList());
        List<PortData> allData = new ArrayList<>();
        List<Port> ports = new ArrayList<>();
        Iterator<double[]> iterator = collect.iterator();
        int count = 0;
        while (iterator.hasNext()){
            try {
                double[] next = iterator.next();
                Port port = new Port("随机抓取坐标点-"+(++count),next[1]+","+next[0]);
                port.setId(String.valueOf(count));
                List<PortData> andSavePortData = getAndSavePortData(port);
                if(andSavePortData==null){
                    iterator.remove();
                }else{
                    allData.addAll(andSavePortData);
                    ports.add(port);
                    log.info("ports:{} allData:{}",ports.size(),allData.size());
                }
            }catch (Exception e){
                log.error("请求错误");
            }
        }
        //对总数据进行去重
        List<PortData> savePortData = allData.stream().distinct().collect(Collectors.toList());
        elasticsearchRestTemplate.save(ports);



    }



    /**
     * 根据港口位置获取相关的经纬度坐标
     * @param port
     */
    public List<PortData> getAndSavePortData(Port port){

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
        paramMap.add("resolution","152.8740565703525");
        paramMap.add("param1",true);
        paramMap.add("pos",1);
        paramMap.add("type",0);
        paramMap.add("map_type","wmap");
        paramMap.add("zoom",11);
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
        ResponseEntity<String> result = restTemplate.postForEntity(megic.getAddress(), httpEntity, String.class);
        String resultBody = result.getBody();
        if(resultBody!=null&&resultBody.equals("callback([])")){
            return null;
        }
        String substring = resultBody.substring(9, resultBody.length() - 1);
        JSONArray jsonArray = JSON.parseArray(substring);
        List<PortData> dataList = jsonArray.stream().map(s -> {
            JSONArray data = (JSONArray) s;
            PortData portData = new PortData();
            portData.setShipName(data.getString(0));
            portData.setLocation(data.getString(2) + "," + data.getString(1));
            portData.setHeading(data.getString(3));
            portData.setTraceDirection(data.getString(4));
            portData.setUnknown_1(data.getString(5));
            portData.setMmsi(data.getString(6));
            portData.setUnknown_2(data.getString(7));
            portData.setUnknown_3(data.getString(8));
            portData.setUnknown_4(data.getString(9));
            portData.setShipType(data.getString(10));
            portData.setShipLong(data.getString(11));
            portData.setShipWide(data.getString(12));
            portData.setUnknown_5(data.getString(13));
            portData.setDate(data.getString(14));
            portData.setUnknown_6(data.getString(15));
            portData.setPreArrivalPort(data.getString(16));
            portData.setShipSpeed(data.getString(17));
            portData.setUnknown_7(data.getString(18));
            return portData;
        }).collect(Collectors.toList());
        log.info("成功爬取区域名称:{} 经度:{} 纬度:{} 数据量:{}",port.getChName(),xy[1],xy[0],dataList.size());
        return dataList;
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



    @Test
    public void testNB(){
//        String nbData = getNBData(113.05575227050784, 22.512968049229585, 113.15874909667968, 22.959435326778674);
        long l = System.currentTimeMillis();
        //生成代理
        ResponseEntity<String> forEntity = restTemplate.getForEntity(megic.getJgip(), String.class);
        proxyRestTemplate = restTemplate(forEntity.getBody());
        JSONArray allData = new JSONArray();

        List<double[]> maps = maps(103.49900, 17.15317, 124.25983, 41.43400, 8);

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        CountDownLatch countDownLatch = new CountDownLatch(maps.size());

        try {
            for(int i=0;i<maps.size();i++){
                int finalI = i;
                executorService.execute(()->{
                    double[] doubles = maps.get(finalI);
                    String nbData = getNBData(doubles[0], doubles[1], doubles[2], doubles[3]);
                    if( nbData.length()>13){
                        nbData = nbData.substring(11, nbData.length() - 2);
                        String content = JavaScriptUtil.maphaoDecrypt(nbData);
                        JSONArray array = JSONArray.parseArray(content);
                        if(array!=null){
                            allData.addAll(array);
                            log.info("当前位置:{} 数据长度:{} 总长度:{}", finalI,array.size(),allData.size());
                        }else{
                            log.error("{} {} {} {}", doubles[0], doubles[1], doubles[2], doubles[3]);
                        }

                    }
                    countDownLatch.countDown();
                });

            }
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(-(l-System.currentTimeMillis())/1000 + "s");
        System.out.println(-(l-System.currentTimeMillis())/1000 + "s");
//        String nbData = getNBData(103.49900, 17.15317, 124.25983, 41.43400);
    }



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


//    public static void main(String[] args) throws FileNotFoundException {
//
//
//
//
//        String s = "rrAhsfg/vsbGagSXBZm0Ui4sBc3wcRlnoulZdtMM0VMJ2DhSAeoat+dkcmLDUc09lxrJ8OQxZWzv9/mZc5G4ZdICvgtAW5hgD6+iT/0UmRV7aHXwpoYVKEeQL6rAMzeMubgVIlztoScebJEmHaB6xg==";
//        String ad = JavaScriptUtil.maphaoDecrypt(s);
//        String s2 = "rrAhsfg/vsbGagSXBZm0Ui4sBc3wcRlnoulZdtMM0VMJ2DhSAeoat+dkcmLDUc09lxrJ8OQxZWzv9/mZc5G4ZdICvgtAW5hgD6+iT/0UmRV7aHXwpoYVKEeQL6rAMzeMubgVIlztoScebJEmHaB6xg==";
//        String asd = JavaScriptUtil.maphaoDecrypt(s2);
//        File file =new File("C:\\Users\\NC053\\Desktop\\response.json");
//        System.out.println(ad+asd);
//        Scanner scanner = new Scanner(new FileInputStream(file));
//        StringBuilder stringBuilder = new StringBuilder();
//        while (scanner.hasNextLine()){
//            String nextLine = scanner.nextLine();
//            stringBuilder.append(nextLine);
//            System.out.println(1);
//        }
//        String string = stringBuilder.toString();
//        string = string.substring(11, string.length() - 2);
//        String decrypt = JavaScriptUtil.maphaoDecrypt(string);
//        JSONArray array = JSON.parseArray(decrypt);
//        System.out.println(1);
//
//    }


//    @Test
//    public  void testHs(){
//        ResponseEntity<String> forEntity = restTemplate.getForEntity("http://219.137.32.78:7302/page/getAllGkLocal", String.class);
//        String body = forEntity.getBody();
//        JSONObject jsonObject = JSON.parseObject(body);
//        JSONArray result = jsonObject.getJSONArray("result");
//        List<NBPortData> list = new LinkedList<>();
//        for(int i=0;i<result.size();i++){
//            NBPortData object = result.getObject(i, NBPortData.class);
//            String point = object.getPoint();
//            if(!StringUtils.isEmpty(point)){
//                String[] split = point.substring(6, point.length() - 1).split(" ");
//                object.setLocation(new GeoPoint(Double.parseDouble(split[1]),Double.parseDouble(split[0])));
//                list.add(object);
//            }
//        }
//        elasticsearchRestTemplate.save(list);
//        System.out.println(1);
//    }

    public static void main(String[] args) throws IOException {
        List<Double[]> array = new LinkedList<>();
        for(int i=0;i<10;i++){
            File file = new File(i+"maps.json");
            FileInputStream fileInputStream = new FileInputStream(file);
            StringBuilder s = new StringBuilder();
            byte[] bb = new byte[1024];
            int b = -1;
            while ((b=fileInputStream.read(bb))>0){
                s.append(new String(bb,0,b));
            }
            List<Double[]> jsonArray = JSON.parseArray(s.toString(),Double[].class);
            array.addAll(jsonArray);
        }
        log.info("2");
    }


    @Test
    public void sad(){
        String asd = "H4sIAAAAAAAAAIWWz4obRxDGX2XQKYFBdHV1VXcf5bW8kr0ZmawWmxizkPPGe/IT+GICIbeAL7HzB3I0hASza3CeZuX1W6S6p0fq6ZmxD2oJNDP9m+/7qqqfPJkRoyJUSs3q2cPNutl+BYBzT2QRKw1zb1ij/lr+PV2tH8rX0fKb7fJxdfTtcrFdb5pZreZQa+3nqkYtC5BHdoiK5aH17O65ATSGDIUdbFhgJteHS7XFua61wvCbw70q/JK/ntZCph0gIGFB5khXco1soI3NwAKJqp89v7holwhiLZKLIHfOvVMQr5sJRbwEuX/Lfgn7G2u9J6/9YH9jOwB0GcCqun/nO3d0cnJUrUZhRBXyfZgoSB8nqEHpA9Bq0bI4U2phgRMLOpW7dHfTHFf3183GTpCwamXZnH9/eXF5+YPc48ObAvVgik9gYe/YemJdsDAriizkGHRhDAwIQKMvCTItPmeMBIrZ+TKyzHq/P2b7P14vmuN7ZzChhFHqM57ERMrH2C6fnFnC2tvSklg4gsHK6FyG09VZU7UFI4+M7mb1YrztU7hEESMRvYCA4FIybE5h0A0oUkgZvMqr5MGyqU6WywpcAjEFiLI4AtJPBQSCUPGmJTqQKDNwxXckqCm35VTkWK0lps3GTYUUxkQxPRaN3QtgC5QiYlmzLjNqrW0zysYqymA+/v3nzdV/N+9+vf3nL1FgAkhbhyNZCTXDQQ5ZsLYqFW7NiexA5AbdTECSPNRLy8ef39y+vPJqShq5u6wemxsF0lWpTa47mJVIpPDkTUoS8kkbRpMbdfvTH7tXbxkmUYwdEWUmrx5jJSun0eBSgFN4lfIgRYJlCZFB3bnkcpJN82jRXq9HOgqCU6Um8dktt4O5ayvY9zFiP3GS3VIQqzpBtM7D8um369317zKb2gdHgWNJJA5tYCIl7dgL8NEZ7pLSs0bBYORx11OYOAM5Pls0q4W0twrDmAowFNOH2SAGPd7e4sayUNTCdBUEqjdycDByjE8s1tk8sdvFg2q7WFeUgbTdpYvJYOLYYWuJYurWnNjxOoecuDBwiNil4kE1rB6pEJwaxHrMIinjYKMsHENCPlWx2nPIO7jBkYCUSUlRwpRx7F682f377tOHV36qoxjgUVX2c5g5r+OeQyAHIKsHuRWLUsN1vZPb7per25evgcbnsQhIY6kVTbBtszh2GkhjEKGEkNaVJEH64jFNDClrxvVUSHPHdEM4HZIORQN60EXS/lK7+f43769vfnxNUy1ejmhj3SzUSwiCdEbpIqYrXn84D3g5gokhRAMhwHZC5LW72qyre4ttaiGma43dccAMDkdxdMxKD4ZeGCrjKf25RZAfmMdzG2bwozCDIb7fSCpM6UvsqHtfhNsPUJ7+D3kwakZYDAAA";
        byte[] bytes = asd.getBytes();
        byte[] uncompress = uncompress(bytes);
        String res = new String(uncompress);
        System.out.println(1);
    }

    public static byte[] uncompress(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

}
