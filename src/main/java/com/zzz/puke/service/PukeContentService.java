package com.zzz.puke.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.zzz.puke.bean.PukeKv;
import com.zzz.puke.bean.SysConfig;
import com.zzz.puke.bean.Tokens;
import com.zzz.puke.dao.PukeKvRepository;
import com.zzz.puke.dao.SysConfigRepository;
import com.zzz.puke.dao.TokensRepository;
import com.zzz.puke.enums.PukeURL;
import com.zzz.puke.utils.HttpUtils;
import com.zzz.puke.utils.WechatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PukeContentService {
    private final static Logger logger = LoggerFactory.getLogger(PukeContentService.class);

    public static String PUKE = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=be5b3c4a-e9f5-43c9-8edb-a501d5196438";

    public static int lastId;
    public static HashMap<String, String> header;
    public static String LOCAL_HOST;

    private static Random random = new Random();

    @Autowired
    PukeKvRepository pukeKvRepository;

    @Autowired
    TokensRepository tokensRepository;

    @Autowired
    SysConfigRepository sysConfigRepository;

    @Autowired
    RedisTemplate redisTemplate;


    public void setHeader() {
        header = getHeader();
    }

    public void getContentAndSend() {
        String list = getContentList(1, 10);
        ObjectMapper listMapper = new ObjectMapper();
        try {
            JsonNode listNode = listMapper.readValue(list, JsonNode.class);

            JsonNode data = listNode.get("data");

            //获取上一次id
            Optional<PukeKv> lastOptional = pukeKvRepository.findById((long) 7);
            PukeKv lastKv = lastOptional.get();
            lastId = Integer.parseInt(lastKv.getPukeV());

            ArrayNode rows = (ArrayNode) data.get("rows");
            for (int i = rows.size() - 1; i >= 0; i--) {
                JsonNode row = rows.get(i);
                if (row.get("id").asInt() > lastId) {
                    String content = getContent(row.get("id").asInt());
                    ObjectMapper contentMapper = new ObjectMapper();
                    JsonNode contentNode = contentMapper.readValue(content, JsonNode.class);
                    JsonNode contentData = contentNode.get("data");

                    //id
                    lastId = contentData.get("id").asInt();
                    String time = contentData.get("edit_at").asText();

                    //图片
                    JsonNode images = contentData.get("images");
                    ArrayList<String> imagesList = new ArrayList<>();
                    if (images.size() > 0) {
                        for (JsonNode image : images) {
                            imagesList.add(PukeURL.FILE_PRE + image.get("src").asText());
                        }
                    }

                    //录音
                    JsonNode audios = contentData.get("audios");
                    ArrayList<String> audiosList = new ArrayList<>();
                    if (audios.size() > 0) {
                        for (JsonNode audio : audios) {
                            audiosList.add(PukeURL.FILE_PRE + audio.get("src").asText());
                        }
                    }

                    //文件
                    JsonNode files = contentData.get("files");
                    ArrayList<String> fList = new ArrayList<>();

                    //评论
                    ArrayNode list_comments = (ArrayNode) row.get("list_comments");
                    ArrayList<String> cs = new ArrayList<>();
                    if (list_comments.size() > 0) {
                        for (JsonNode c : list_comments) {
                            audiosList.add(PukeURL.FILE_PRE + c.get("src").asText());
                            cs.add(c.get("content").asText());
                        }
                    }
                    String LOCAL_URL = "http://" + LOCAL_HOST + "/get/";
                    WechatUtils.sendWechatMessage(LOCAL_URL, PUKE, lastId + "", time, contentData.get("content").asText(), imagesList, audiosList, fList, cs);
                }
            }
            lastKv.setPukeV(lastId + "");
            //设置上一次id
            pukeKvRepository.save(lastKv);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("header: {} , list: {}", header, list);
            WechatUtils.sendErrorMessage("ContentAndSend:" + e.toString() + header.toString());
        }

    }

    public String getContentList(int pageNum, int pageSize) {
        HashMap<String, String> listParams = new HashMap<>();
        listParams.put("page", "" + pageNum);
        listParams.put("rows", "" + pageSize);
        listParams.put("circle_id", "5157636");
        listParams.put("dynamic_filter", "all");
        listParams.put("get_good_users", "1");
        listParams.put("get_award_users", "1");
        listParams.put("get_comments", "1");
        listParams.put("flag_order", "t");
        setHeader();
        String contentList = HttpUtils.doGet(PukeURL.GET_LIST, listParams, header);
        return contentList;
    }

    @Cacheable(cacheNames = {"detail"}, key = "#id")
    public String getContent(int id) {
        HashMap<String, String> contentParams = new HashMap<>();
        contentParams.put("id", id + "");
        contentParams.put("get_good_users", "1");
        String content = HttpUtils.doGet(PukeURL.GET_CONTENT, contentParams, header);
        return content;
    }

    public void updateCookie(String xAccessToken, String newLastId) {
        Optional<PukeKv> lastOptional = pukeKvRepository.findById((long) 7);
        PukeKv lastKv = lastOptional.get();
        lastKv.setPukeV(newLastId);
        pukeKvRepository.save(lastKv);

        Optional<PukeKv> tokenOptional = pukeKvRepository.findById((long) 1);
        PukeKv tokenKv = tokenOptional.get();
        tokenKv.setPukeV(xAccessToken);
        pukeKvRepository.save(tokenKv);

        Set<String> keys = redisTemplate.keys("*");
        redisTemplate.delete(keys);
    }

    private HashMap<String, String> getHeader() {
        HashMap<String, String> configMap = new HashMap<>();
        Iterable<PukeKv> allConfig = pukeKvRepository.findAll();
        Iterator<PukeKv> iterator = allConfig.iterator();
        while (iterator.hasNext()) {
            PukeKv next = iterator.next();
            configMap.put(next.getPukeK(), next.getPukeV());
        }
        lastId = Integer.parseInt(configMap.get("lastId"));
        HashMap<String, String> header = new HashMap<>();
        header.put("x-access-token", configMap.get("xAccessToken"));
        header.put("x-client-key", configMap.get("xClientKey"));
        //获取token
        header.put("x-client-token", getxClientToken());
        return header;
    }

    private String getxClientToken() {
        long t = random.nextInt(10000);
        if (t == 2208) {
            t = random.nextInt(10000);
        }
        Optional<Tokens> tokensOptional = tokensRepository.findById(t);
        Tokens token = tokensOptional.get();
        String tokenPre = token.getXClientToken();
        long time = new Date().getTime() / 1000 - t;
        StringBuffer sb = new StringBuffer(tokenPre + Long.toHexString(time));
        String result = sb.reverse().toString();
        return result;
    }

    public void sendHistory() {
        SysConfig config = sysConfigRepository.findByK("host");
        String host = config.getSysV();
        LOCAL_HOST = host;
        WechatUtils.sendMessage("[点击查看历史消息](http://" + host + "/list)", PUKE);
    }
}
