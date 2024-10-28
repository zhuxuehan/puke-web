package com.zzz.puke.service;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
        String url = "https://api.zsxq.com/v2/groups/" + lastKv.getZsxqGroup() + "/topics?scope=all&count=20";
        String list = HttpUtils.doGet(url, params, header);
        ObjectMapper listMapper = new ObjectMapper();
        try {
            JsonNode listNode = listMapper.readValue(list, JsonNode.class);

            //获取上一次时间
            String lastTime = lastKv.getZsxqLastTime();
            ArrayNode rows = (ArrayNode) listNode.get("resp_data").get("topics");

            for (int i = rows.size() - 1; i > 0; i--) {
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
                        text = "Q：" + q + "\n" + "A：" + a;
                    }
                    lastTime = curTime;
                    lastId = row.get("topic_id").asText();

                    //评论
//                    ArrayNode list_comments = (ArrayNode) row.get("list_comments");
                    ArrayList<String> audios = new ArrayList<>();
                    ArrayList<String> files = new ArrayList<>();
                    ArrayList<String> list_comments = new ArrayList<>();

                    WechatUtils.sendWechatMessage(zsxqWebhook, lastId, lastTime, text, images, audios, files, list_comments);
                    images.clear();
                }
            }
            lastKv.setZsxqLastTime(lastTime);
            //设置上一次id
            zsxqKvRepository.save(lastKv);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("header: {} , list: {}", header, list);
            WechatUtils.sendErrorMessage("ContentAndSend:" + e + header);
        }

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
        return header;
    }


}
