package com.zzz.puke.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.zzz.puke.bean.ZsxqKv;
import com.zzz.puke.dao.SysConfigRepository;
import com.zzz.puke.dao.ZsxqKvRepository;
import com.zzz.puke.utils.HttpUtils;
import com.zzz.puke.utils.WechatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class ZsxqContentService {
    private final static Logger logger = LoggerFactory.getLogger(ZsxqContentService.class);

    @Autowired
    ZsxqKvRepository zsxqKvRepository;

    @Autowired
    SysConfigRepository sysConfigRepository;

    @Autowired
    RedisTemplate redisTemplate;

    public static void sendHistory() {

    }

    public void getAllContentAndSend() {
        List<ZsxqKv> allKv = zsxqKvRepository.findAll();
        HashMap<String, String> params = new HashMap<>();
        for (ZsxqKv lastKv : allKv) {
            getContentAndSend(lastKv, params);
        }
    }

    public void getContentAndSend(ZsxqKv lastKv, HashMap<String, String> params) {
        String zsxqWebhook = lastKv.getZsxqWebhook();
        HashMap<String, String> header = getHeader(lastKv);
        String zsxqGroup = lastKv.getZsxqGroup();
        String url = "https://api.zsxq.com/v2/groups/" + zsxqGroup + "/topics?scope=all&count=20";

        if (redisTemplate.hasKey(zsxqGroup)) {
            return;
        }
        redisTemplate.opsForValue().set(zsxqGroup, zsxqGroup, lastKv.getIntervalTime(), TimeUnit.SECONDS);
        //星球访问频繁报错
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String list = HttpUtils.doGet(url, params, header);
        ObjectMapper listMapper = new ObjectMapper();
        try {
            JsonNode listNode = listMapper.readValue(list, JsonNode.class);
            //获取上一次时间
            String lastTime = lastKv.getZsxqLastTime();
            ArrayNode rows = (ArrayNode) listNode.get("resp_data").get("topics");
            if (null == rows) {
                return;
            }

            for (int i = rows.size() - 1; i >= 0; i--) {
                JsonNode row = rows.get(i);
                String curTime = row.get("create_time").asText();
                LinkedList<String> images = new LinkedList<>();
                if (curTime.compareTo(lastTime) > 0) {

                    String type = row.get("type").asText();
                    //根据不同类型的内容解析
                    String text = "";
                    String lastId = "";
                    if (type.equals("talk")) {
                        text = row.get("talk").get("text").asText();
                        //图片
                        ArrayNode imagesArray = (ArrayNode) row.get("talk").get("images");
                        if (imagesArray != null) {
                            for (JsonNode image : imagesArray) {
                                images.add(image.get("large").get("url").asText());
                            }
                        }
                    } else if (type.equals("q&a")) {
                        String q = row.get("question").get("text").asText();
                        String a = row.get("answer").get("text").asText();
                        text = "<br>Q：" + q + "<br><br>" + "A：" + a + "\n";
                    }
                    lastTime = curTime;
                    lastId = row.get("topic_id").asText();

                    //评论
//                    ArrayNode list_comments = (ArrayNode) row.get("list_comments");
                    ArrayList<String> audios = new ArrayList<>();
                    //文件
                    ArrayList<String> files = new ArrayList<>();
                    JsonNode fileJson = row.get("talk").get("files");
                    if (null != fileJson) {
                        for (JsonNode fj : fileJson) {
                            String fileId = fj.get("file_id").asText();
                            Thread.sleep(1500);
                            files.add(getFileUrl(fileId, header));
                        }
                    }
                    ArrayList<String> list_comments = new ArrayList<>();
                    String localurl = "http://139.196.238.213/get/";
                    WechatUtils.sendWechatMessage(localurl, zsxqWebhook, lastId, lastTime, text, images, audios, files, list_comments);
                    images.clear();
                }
            }
            lastKv.setZsxqLastTime(lastTime);
            lastKv.setLastSendTime(new Date());
            //设置上一次id
            zsxqKvRepository.save(lastKv);
        } catch (Exception e) {
            e.printStackTrace();
            WechatUtils.sendErrorMessage("ContentAndSend:" + e + lastKv.getZsxqGroup());
        }

    }

    private String getFileUrl(String fileId, HashMap<String, String> header) {
        String u = "https://api.zsxq.com/v2/files/" + fileId + "/download_url";
        String list = HttpUtils.doGet(u, new HashMap<>(), header);
        ObjectMapper listMapper = new ObjectMapper();
        try {
            JsonNode listNode = listMapper.readValue(list, JsonNode.class);
            return listNode.get("resp_data").get("download_url").asText();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "error";
    }

//    public void updateCookie(String xAccessToken, String newLastId) {
//        Optional<PukeKv> lastOptional = pukeKvRepository.findById((long) 7);
//        PukeKv lastKv = lastOptional.get();
//        lastKv.setPukeV(newLastId);
//        pukeKvRepository.save(lastKv);
//
//        Optional<PukeKv> tokenOptional = pukeKvRepository.findById((long) 1);
//        PukeKv tokenKv = tokenOptional.get();
//        tokenKv.setPukeV(xAccessToken);
//        pukeKvRepository.save(tokenKv);
//
//        Set<String> keys = redisTemplate.keys("*");
//        redisTemplate.delete(keys);
//    }

    private HashMap<String, String> getHeader(ZsxqKv gAndT) {
        HashMap<String, String> header = new HashMap<>();
        header.put("cookie", gAndT.getZsxqCookie());
        header.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36");
        return header;
    }


}
