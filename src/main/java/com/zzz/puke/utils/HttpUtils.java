package com.zzz.puke.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.*;

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

    public static void main(String[] args) {
        String url = "https://api.zsxq.com/v2/groups/88851125114152/topics?scope=all&count=20";
        HashMap<String, String> paramMap = new HashMap<>();
        HashMap<String, String> header = new HashMap<>();
        header.put("cookie", "sensorsdata2015jssdkcross=%7B%22distinct_id%22%3A%22191bc86a338206-0d964c8ddefd8a8-26001151-1327104-191bc86a339c9a%22%2C%22first_id%22%3A%22%22%2C%22props%22%3A%7B%22%24latest_traffic_source_type%22%3A%22%E8%87%AA%E7%84%B6%E6%90%9C%E7%B4%A2%E6%B5%81%E9%87%8F%22%2C%22%24latest_search_keyword%22%3A%22%E6%9C%AA%E5%8F%96%E5%88%B0%E5%80%BC%22%2C%22%24latest_referrer%22%3A%22https%3A%2F%2Fwww.google.com.hk%2F%22%7D%2C%22identities%22%3A%22eyIkaWRlbnRpdHlfY29va2llX2lkIjoiMTkxYmM4NmEzMzgyMDYtMGQ5NjRjOGRkZWZkOGE4LTI2MDAxMTUxLTEzMjcxMDQtMTkxYmM4NmEzMzljOWEifQ%3D%3D%22%2C%22history_login_id%22%3A%7B%22name%22%3A%22%22%2C%22value%22%3A%22%22%7D%2C%22%24device_id%22%3A%22191bc86a338206-0d964c8ddefd8a8-26001151-1327104-191bc86a339c9a%22%7D; zsxq_access_token=2DD8ECD3-941E-53DB-9847-98E44E8FBD8B_0B864C13C20A0924; tfstk=fyFjt1YWOnxbou6VdFQrdUHk-cl_Gl1ehFgT-Pd2WjhvXFn7uI-Z3Pn_fuoo_mP43RT0NkOwgiX0WCGZ6MSFT6zDofcOYAaC2Z-mJVAA_mpvyygQFfsFT6zvR1LK5MP2hSMKAVhtkmdAwagn7f39Mln-e23eDfEt6aG-82xvXVpxwY3E2cht6lU-NFISrtiYla4Sr6MA51aoVCdB-mkvVr9wKpVSlxNTk0gjl7gjhDMoJ0H3MuzTgSDleLGgzJZ_BkIy3meIRuMUhMObAo0T5VEftei8GPNSn88e9zMsc8FxFFdTzzexpRZfcKmY3uMnyYL1t4rE2rVYFNx8kkojM4HP9wa-B8PmKSSJGXe3ujyTvgJqDJgO41REAoo6CUMH14iFPa9MI5_JrB_0Lx3EH40zTa_WcdDxr4iFPa9MIx3ozY75Pn9G.; zsxqsessionid=f19ef86aa3f4e17ddf1eb3b3e5084a6b; abtest_env=product");
        try {
            String result = doGet(url, paramMap, header);
            ObjectMapper listMapper = new ObjectMapper();
            JsonNode listNode = listMapper.readValue(result, JsonNode.class);
            // 输出：你好
            System.out.println(listNode);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
