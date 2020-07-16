/**
 * Copyright 2018-2020 stylefeng & fengshuonan (sn93@qq.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.elasticdata.config;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author liliguang
 * @Date 2019年10月31日11:50:26
 */
@Configuration
public class SingleDataSourceConfig {

    // 代理服务器
    final static String proxyHost = "58.218.92.198";
    final static Integer proxyPort = 3487;


    @Bean("restTemplateTemp")
    public RestTemplate restTemplate(){
        HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
        //连接池
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        // 设置整个连接池最大连接数 根据自己的场景决定
        // todo 后面调整从配置中心获取
        connectionManager.setMaxTotal(20);
        // 路由是对maxTotal的细分
        // todo 后面调整从配置中心获取
        connectionManager.setDefaultMaxPerRoute(10);
        //生成一个设置了连接超时时间、请求超时时间、异常最大重试次数的httpClient
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(5000)
                .setConnectTimeout(10000)
                .setSocketTimeout(40000)
                .setExpectContinueEnabled(false)
                //.setProxy(new HttpHost(proxyHost, proxyPort))
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

