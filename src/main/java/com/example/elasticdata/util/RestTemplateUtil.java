package com.example.elasticdata.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.elasticdata.entity.other.Megic;
import lombok.Data;
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
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * <h3>elasticdata</h3>
 * <p>http客户端模板生成工具</p>
 *
 * @author : liliguang
 * @date : 2020-06-23 10:30
 **/
@Data
@Slf4j
@Component
public class RestTemplateUtil {

    @Autowired
    Megic megic;
    @Autowired
    RestTemplate restTemplate;

    public static RestTemplate proxyRestTemplate = null;

    public static String proxyHost ;
    public static Integer proxyPort ;

    /**
     * 创建Template
     */
    public synchronized RestTemplate restTemplate(){
        if(proxyRestTemplate!=null){
            try {
                ResponseEntity<String> forEntity = proxyRestTemplate.getForEntity("http://www.baidu.com", String.class);
                boolean successful = forEntity.getStatusCode().is2xxSuccessful();
                if(successful){
                    return proxyRestTemplate;
                }
            }catch (Exception e){
                log.warn("IP代理已经失效！异常信息:{}",e.getMessage());
            }
        }
        //初始化http客户端
        ResponseEntity<String> forEntity = restTemplate.getForEntity(megic.getJgip(), String.class);
        String jgip = forEntity.getBody();
        if(jgip.startsWith("{\"code\":113,")){
            JSONObject jsonObject = JSON.parseObject(jgip);
            String msg = jsonObject.getString("msg");
            msg = msg.replaceAll("请添加白名单","");
            String white = "http://wapi.http.cnapi.cc/index/index/save_white?neek=134950&appkey=eadd1516a28ee46b816ab9cf1bcea700&white="+msg;
            ResponseEntity<String> entity = restTemplate.getForEntity(white, String.class);
            log.info("添加白名单结束: {}",entity.getBody());
            restTemplate();
        }
        log.info("当前代理地址返回内容：{} ",jgip);
        //jgip
        String[] split = jgip.trim().split(":");
         proxyHost = split[0];
         proxyPort = Integer.valueOf(split[1]);

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
        proxyRestTemplate = new RestTemplate(requestFactory);

        return proxyRestTemplate;
    }
}
