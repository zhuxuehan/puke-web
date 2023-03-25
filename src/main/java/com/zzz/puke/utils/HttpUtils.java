package com.zzz.puke.utils;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpUtils {
    private final static Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    private final static PoolingHttpClientConnectionManager POOL_CONN_MANAGER = new PoolingHttpClientConnectionManager();

    static {   //类加载的时候 设置最大连接数 和 每个路由的最大连接数
        POOL_CONN_MANAGER.setMaxTotal(20);
        POOL_CONN_MANAGER.setDefaultMaxPerRoute(10);
    }

    private static RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setSocketTimeout(10 * 1000)
                .setConnectTimeout(10 * 1000)
                .setConnectionRequestTimeout(10 * 1000)
                .build();
    }

    private static CloseableHttpClient getCloseableHttpClient() {
        ConnectionKeepAliveStrategy connectionKeepAliveStrategy = (httpResponse, httpContext) -> {
            return 10 * 1000; // tomcat默认keepAliveTimeout为20s
        };
        return HttpClients.custom()
                .setDefaultRequestConfig(getRequestConfig())
                .setConnectionManager(POOL_CONN_MANAGER)
                .setKeepAliveStrategy(connectionKeepAliveStrategy)
                .build();
    }


    public static String doGet(String url, Map<String, String> paramMap, Map<String, String> header) {

        CloseableHttpClient httpClient = getCloseableHttpClient();
        CloseableHttpResponse response = null;
        String result = null;

        try {
            URIBuilder uri = new URIBuilder(url);
            for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                uri.addParameter(entry.getKey(), entry.getValue());
            }
            HttpGet httpGet = new HttpGet(uri.toString());
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue());
            }

            //3.执行get请求并返回结果
            response = httpClient.execute(httpGet);
            //4.处理结果，这里将结果返回为字符串
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("url: {} paramMap: {}  header: {} ", url, paramMap.toString(), header.toString());
            WechatUtils.sendErrorMessage("doGet:" + e.toString() + "header:" + header.toString());
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                WechatUtils.sendErrorMessage(e.toString());
            }
        }
        return result;
    }

    public static String doPost(String url, Map<String, String> paramMap, Map<String, String> headerMap, ContentType contentType) {
        CloseableHttpClient httpClient = getCloseableHttpClient();
        CloseableHttpResponse response = null;
        String result = null;
        // 创建httpPost远程连接实例
        HttpPost httpPost = new HttpPost(url);

        // 设置请求头
        httpPost.addHeader("Content-Type", contentType.getMimeType());
        if (null != headerMap && headerMap.size() > 0) {
            Set<Map.Entry<String, String>> entrySet = paramMap.entrySet();
            for (Map.Entry<String, String> mapEntry : entrySet) {
                httpPost.addHeader(mapEntry.getKey(), mapEntry.getValue());
            }
        }

        // 封装post请求参数
        if (null != paramMap && paramMap.size() > 0) {
            HttpEntity reqEntity = null;
            if (contentType.equals(ContentType.APPLICATION_FORM_URLENCODED)) {
                List<NameValuePair> nvps = new ArrayList<>();
                // 通过map集成entrySet方法获取entity
                Set<Map.Entry<String, String>> entrySet = paramMap.entrySet();
                // 循环遍历，获取迭代器
                for (Map.Entry<String, String> mapEntry : entrySet) {
                    nvps.add(new BasicNameValuePair(mapEntry.getKey(), mapEntry.getValue()));
                }
                try {
                    reqEntity = new UrlEncodedFormEntity(nvps, "UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                    WechatUtils.sendErrorMessage(e.toString());
                }
            } else if (contentType.equals(ContentType.MULTIPART_FORM_DATA)) {
                MultipartEntityBuilder builder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);
                if (paramMap != null) {
                    for (String key : paramMap.keySet()) {
                        builder.addPart(key, new StringBody(paramMap.get(key), ContentType.create("text/plain", Consts.UTF_8)));
                    }
                    reqEntity = builder.build();
                }
            } else if (contentType.equals(ContentType.APPLICATION_JSON)) {
                reqEntity = new StringEntity(paramMap.get("message"), "utf-8");
            }
            httpPost.setEntity(reqEntity);
        }
        try {
            // httpClient对象执行post请求,并返回响应参数对象
            response = httpClient.execute(httpPost);
            // 从响应对象中获取响应内容
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity);
        } catch (Exception e) {
            e.printStackTrace();
            WechatUtils.sendErrorMessage(e.toString());
        } finally {
            // 关闭资源
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    WechatUtils.sendErrorMessage(e.toString());
                }
            }
        }
        return result;
    }

}
