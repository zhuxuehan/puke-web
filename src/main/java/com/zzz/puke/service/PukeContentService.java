package com.zzz.puke.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.zzz.puke.bean.*;
import com.zzz.puke.dao.CircleWebhookRepository;
import com.zzz.puke.dao.PukeKvRepository;
import com.zzz.puke.dao.SysConfigRepository;
import com.zzz.puke.dao.TokensRepository;
import com.zzz.puke.enums.ContentChannel;
import com.zzz.puke.enums.PukeURL;
import com.zzz.puke.utils.HttpUtils;
import com.zzz.puke.utils.WechatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class PukeContentService {
    private final static Logger logger = LoggerFactory.getLogger(PukeContentService.class);

    @Value("${myapp.pulic-host}")
    public String LOCAL_HOST;

    private static Random random = new Random();

    @Autowired
    private PukeKvRepository pukeKvRepository;

    @Autowired
    private TokensRepository tokensRepository;

    @Autowired
    private SysConfigRepository sysConfigRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CircleWebhookRepository circleWebhookRepository;

    public void getAllContentAndSend() {
        List<PukeKv> allKv = pukeKvRepository.findAll();
        HashMap<String, String> params = new HashMap<>();
        for (PukeKv lastKv : allKv) {
            getContentAndSend(lastKv, params);
        }
    }

    public void getContentAndSend(PukeKv pukeKv, HashMap<String, String> params) {
        String circleid = pukeKv.getCircleid();
        if (redisTemplate.hasKey(ContentChannel.PUKE + circleid)) {
            return;
        }
        redisTemplate.opsForValue().set(ContentChannel.PUKE + circleid, circleid, pukeKv.getIntervalTime(), TimeUnit.SECONDS);

        String list = getContentList(1, 10, pukeKv);
        ObjectMapper listMapper = new ObjectMapper();
        try {
            JsonNode listNode = listMapper.readValue(list, JsonNode.class);

            JsonNode data = listNode.get("data");

            //获取上一次id
            int lastId = Integer.parseInt(pukeKv.getLastid());
            int currId = 0;
            ArrayNode rows = (ArrayNode) data.get("rows");
            for (int i = rows.size() - 1; i >= 0; i--) {
                JsonNode row = rows.get(i);
                currId = row.get("id").asInt();
                if (currId > lastId) {
                    String LOCAL_URL = "http://" + LOCAL_HOST + "/pk/get/" + circleid + "/" + currId;
                    //再去查一边详细信息
                    String content = getContent(row.get("circle_id").asText(), row.get("id").asInt());
                    ContentPacket cPacket = getPkPacket(content);
                    List<CircleWebhook> webhookList = circleWebhookRepository
                            .findByCircleAndChannel(circleid + "", ContentChannel.PUKE);
                    for (CircleWebhook cw : webhookList) {
                        MessagePacket mPacket = new MessagePacket();
                        mPacket.setWebhook(cw.getWebhook());
                        mPacket.setLocalUrl(LOCAL_URL);
                        mPacket.setContentPacket(cPacket);
                        WechatUtils.sendWechatMessage(mPacket);
                    }
                    lastId = currId;
                }
            }

            pukeKv.setLastid(lastId + "");
            pukeKv.setLastSendTime(new Date());
            //设置上一次id
            pukeKvRepository.save(pukeKv);
        } catch (Exception e) {
            e.printStackTrace();
            WechatUtils.sendErrorMessage("ContentAndSend:" + e);
        }
    }

    public ContentPacket getPkPacket(String content) {
        //再去调用拿详细信息
        ObjectMapper contentMapper = new ObjectMapper();
        JsonNode contentNode = null;
        try {
            contentNode = contentMapper.readValue(content, JsonNode.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonNode contentData = contentNode.get("data");

        //id
        int lastId = contentData.get("id").asInt();
        String time = contentData.get("edit_at").asText();
        String circleId = contentData.get("circle_id").asText();

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
                audiosList.add(audio.get("src").asText());
            }
        }

        //文件
        JsonNode files = contentData.get("files");
        ArrayList<String> filesList = new ArrayList<>();

        //评论
        ArrayList<String> commentsList = new ArrayList<>();
//        ArrayNode list_commentsNode = (ArrayNode) row.get("list_comments");

//        if (list_commentsNode.size() > 0) {
//            for (JsonNode c : list_commentsNode) {
//                audiosList.add(PukeURL.FILE_PRE + c.get("src").asText());
//                commentsList.add(c.get("content").asText());
//            }
//        }

        ContentPacket cPacket = new ContentPacket();
        cPacket.setId(lastId + "");
        cPacket.setCurrTime(time);
        cPacket.setText(contentData.get("content").asText());
        cPacket.setImages(imagesList);
        cPacket.setAudios(audiosList);
        cPacket.setFiles(filesList);
        cPacket.setComments(commentsList);
        cPacket.setGroup(circleId);
        return cPacket;
    }

    public String getContentList(int pageNum, int pageSize, PukeKv pukeKv) {
        HashMap<String, String> listParams = new HashMap<>();
        listParams.put("page", "" + pageNum);
        listParams.put("rows", "" + pageSize);
        listParams.put("circle_id", pukeKv.getCircleid());
        listParams.put("dynamic_filter", "all");
        listParams.put("get_good_users", "1");
        listParams.put("get_award_users", "1");
        listParams.put("get_comments", "1");
        listParams.put("flag_order", "t");
        String contentList = HttpUtils.doGet(PukeURL.GET_LIST, listParams, getHeader(pukeKv));
        return contentList;
    }

    public List<ContentPacket> getPkPacketsListFromRemote(String circle, String user, int pageNum, int pageSize) {
        ArrayList<ContentPacket> pakcetsList = new ArrayList<>(10);
        PukeKv pukeKv = pukeKvRepository.findByCircle(circle);
        ArrayNode rows = null;
        int t = 0;
        try {
            do {
                String list = getContentList(pageNum, pageSize, pukeKv);
                ObjectMapper listMapper = new ObjectMapper();
                JsonNode listNode = null;
                listNode = listMapper.readValue(list, JsonNode.class);
                JsonNode data = listNode.get("data");
                rows = (ArrayNode) data.get("rows");
            } while (t < 2 && null == rows);

            if (null == rows) {
                return new ArrayList<>();
            }
            for (int i = 0; i < rows.size(); i++) {
                JsonNode row = rows.get(i);
                String content = getContent(row.get("circle_id").asText(), row.get("id").asInt());
                ContentPacket contentPacket = getPkPacket(content);
                pakcetsList.add(contentPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pakcetsList;
    }

    @Cacheable(cacheNames = {"detail"}, key = "#circleid + #id")
    public String getContent(String circleid, int id) {
        PukeKv pukeKv = pukeKvRepository.findByCircle(circleid);
        HashMap<String, String> contentParams = new HashMap<>();
        contentParams.put("id", id + "");
        contentParams.put("get_good_users", "1");
        String content = HttpUtils.doGet(PukeURL.GET_CONTENT, contentParams, getHeader(pukeKv));
        return content;
    }

    public void updateCookie(String xAccessToken, String newLastId) {
        PukeKv circle = pukeKvRepository.findByCircle("51520573");
        if (!Objects.isNull(xAccessToken)) {
            circle.setxAccessToken(xAccessToken);
        }
        if (!Objects.isNull(newLastId)) {
            circle.setLastid(newLastId);
        }
        pukeKvRepository.save(circle);

        Set<String> keys = redisTemplate.keys("*");
        redisTemplate.delete(keys);
    }

    private HashMap<String, String> getHeader(PukeKv pukeKv) {
        HashMap<String, String> header = new HashMap<>();
        header.put("x-access-token", pukeKv.getxAccessToken());
        header.put("x-client-key", pukeKv.getxClientKey());
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
        List<CircleWebhook> webhookList = circleWebhookRepository.findByChannel(ContentChannel.PUKE);
        for (CircleWebhook w : webhookList) {
            String listURL = "[点击查看历史消息](http://" + LOCAL_HOST + "/pk/list/" + w.getCircleId() + ")";
            //发送微信
            switch (w.getWebhookChannel()) {
                case WECHAT:
                    WechatUtils.sendMessage(listURL, w.getWebhook());
                    break;
                case DINGTALK:
                    // TODO: 发送钉钉
                    break;
            }
        }
    }


}
