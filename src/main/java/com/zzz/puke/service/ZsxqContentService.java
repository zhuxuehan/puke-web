package com.zzz.puke.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.zzz.puke.bean.CircleWebhook;
import com.zzz.puke.bean.ContentPacket;
import com.zzz.puke.bean.MessagePacket;
import com.zzz.puke.bean.ZsxqKv;
import com.zzz.puke.dao.CircleWebhookRepository;
import com.zzz.puke.dao.SysConfigRepository;
import com.zzz.puke.dao.ZsxqKvRepository;
import com.zzz.puke.enums.ContentChannel;
import com.zzz.puke.utils.DingtalkUtil;
import com.zzz.puke.utils.HttpUtils;
import com.zzz.puke.utils.WechatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ZsxqContentService {

    @Autowired
    ZsxqKvRepository zsxqKvRepository;

    @Autowired
    SysConfigRepository sysConfigRepository;

    @Autowired
    CircleWebhookRepository circleWebhookRepository;

    @Autowired
    RedisTemplate redisTemplate;

    public static String ZSXQ_GROUP_URL = "https://api.zsxq.com/v2/groups/%s/topics?scope=all&count=20";


    public static void sendHistory() {

    }

    public void getAllContentAndSend() {
        List<ZsxqKv> allKv = zsxqKvRepository.findAll();
        HashMap<String, String> params = new HashMap<>();
        for (ZsxqKv lastKv : allKv) {
            getContentAndSend(lastKv.getZsxqGroup(), params);
        }
    }

    public void getContentAndSend(String group, HashMap<String, String> params) {
        ZsxqKv groupKv = zsxqKvRepository.findByGroup(group);
        List<CircleWebhook> circleWebhookList = circleWebhookRepository.findByCircleAndChannel(group, ContentChannel.PUKE.toString());
        HashMap<String, String> header = getHeader(groupKv);

        if (redisTemplate.hasKey(ContentChannel.ZSXQ + group)) {
            return;
        }
        redisTemplate.opsForValue().set(ContentChannel.ZSXQ + group, group, groupKv.getIntervalTime(), TimeUnit.SECONDS);
        //星球访问频繁报错
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String list = HttpUtils.doGet(String.format(ZSXQ_GROUP_URL, group), params, header);
        ObjectMapper listMapper = new ObjectMapper();
        try {
            JsonNode listNode = listMapper.readValue(list, JsonNode.class);
            //获取上一次时间
            String lastTime = groupKv.getZsxqLastTime();
            ArrayNode rows = (ArrayNode) listNode.get("resp_data").get("topics");
            if (null == rows) {
                return;
            }
            for (int i = rows.size() - 1; i >= 0; i--) {
                JsonNode row = rows.get(i);
                String curTime = row.get("create_time").asText();
                if (curTime.compareTo(lastTime) > 0) {
                    ContentPacket contentPacket = getXqPacket(row, header);
                    String localurl = "localhost/xq/group/id?scop=1&endtime";

                    MessagePacket messagePacket = new MessagePacket();
                    messagePacket.setContentPacket(contentPacket);
                    messagePacket.setLocalUrl(localurl);
                    for (CircleWebhook circleWebhook : circleWebhookList) {
                        switch (circleWebhook.getWebhookChannel()) {
                            case WECHAT:
                                messagePacket.setWebhook(circleWebhook.getWebhook());
                                WechatUtils.sendWechatMessage(messagePacket);
                            case DINGTALK:
                                messagePacket.setWebhook(circleWebhook.getWebhook());
                                messagePacket.setSecret(circleWebhook.getSecret());
                                DingtalkUtil.sendDingtalkMessage(messagePacket);
                        }
                    }

                    lastTime = curTime;
                }
            }
            groupKv.setZsxqLastTime(lastTime);
            //设置上一次id
            zsxqKvRepository.save(groupKv);
        } catch (Exception e) {
            e.printStackTrace();
            WechatUtils.sendErrorMessage("ContentAndSend:" + e + groupKv.getZsxqGroup());
        }

    }

    public List<ContentPacket> getXqPacketsList(String group, String user, HashMap<String, String> params) {
        ArrayList<ContentPacket> pakcetsList = new ArrayList<>(20);
        ZsxqKv kv = zsxqKvRepository.findByGroup(group);
        HashMap<String, String> header = getHeader(kv);
        ArrayNode rows = null;
        int t = 0;
        do {
            String list = HttpUtils.doGet(String.format(ZSXQ_GROUP_URL, group), params, header);
            ObjectMapper listMapper = new ObjectMapper();
            JsonNode listNode;
            try {
                listNode = listMapper.readValue(list, JsonNode.class);
                rows = (ArrayNode) listNode.get("resp_data").get("topics");
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (t < 3 && null == rows);

        if (null == rows) {
            return new ArrayList<>();
        }
        for (int i = rows.size() - 1; i >= 0; i--) {
            JsonNode row = rows.get(i);
            ContentPacket contentPacket = getXqPacket(row, header);
            pakcetsList.add(contentPacket);
        }
        getXqPacketsListFromRemote(group, user, params);
        return pakcetsList;
    }

    public List<ContentPacket> getXqPacketsListFromRemote(String group, String user, HashMap<String, String> params) {
        ArrayList<ContentPacket> pakcetsList = new ArrayList<>(20);
        ZsxqKv kv = zsxqKvRepository.findByGroup(group);
        HashMap<String, String> header = getHeader(kv);
        ArrayNode rows = null;
        int t = 0;
        do {
            String list = HttpUtils.doGet(String.format(ZSXQ_GROUP_URL, group), params, header);
            ObjectMapper listMapper = new ObjectMapper();
            JsonNode listNode = null;
            try {
                listNode = listMapper.readValue(list, JsonNode.class);
                rows = (ArrayNode) listNode.get("resp_data").get("topics");
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (t < 3 && null == rows);

        if (null == rows) {
            return new ArrayList<>();
        }
        for (int i = rows.size() - 1; i >= 0; i--) {
            JsonNode row = rows.get(i);
            ContentPacket contentPacket = getXqPacket(row, header);
            pakcetsList.add(contentPacket);
        }

        return pakcetsList;
    }

    public ContentPacket getXqPacket(JsonNode row, HashMap<String, String> header) {
        ContentPacket contentPacket = new ContentPacket();
        String type = row.get("type").asText();
        contentPacket.setType(type);
        //根据不同类型的内容解析
        String text = "";
        String lastId = "";
        JsonNode talkNode = null;
        //文字
        if (type.equals("talk")) {
            talkNode = getTalkNode(row);
            text = getTalk(talkNode);
        } else if (type.equals("q&a")) {
            text = getQandA(row);
        }
        contentPacket.setText(text);
        lastId = row.get("topic_id").asText();
        contentPacket.setId(lastId);
        contentPacket.setCurrTime(row.get("create_time").asText());
        if (null != talkNode) {
            //图片
            contentPacket.setImages(getImagesArray(talkNode));
            //文件
            contentPacket.setFiles(getFilesArray(talkNode, header));
        }
        return contentPacket;
    }

    private String getFileUrl(String fileId, HashMap<String, String> header) {
        String u = "https://api.zsxq.com/v2/files/" + fileId + "/download_url";
        String list = HttpUtils.doGet(u, new HashMap<>(), header);
        ObjectMapper listMapper = new ObjectMapper();
        try {
            JsonNode listNode = listMapper.readValue(list, JsonNode.class);
            if (null != listNode.get("resp_data").get("download_url")) {
                return listNode.get("resp_data").get("download_url").asText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }


    private HashMap<String, String> getHeader(ZsxqKv gAndT) {
        HashMap<String, String> header = new HashMap<>();
        header.put("cookie", gAndT.getZsxqCookie());
        header.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36");
        header.put(":authority", "api.zsxq.com");
        return header;
    }

    private JsonNode getTalkNode(JsonNode row) {
        if (null != row) {
            return row.get("talk");
        }
        return null;
    }

    private String getTalk(JsonNode talk) {
        if (null != talk.get("text")) {
            return delStr(talk.get("text").asText());
        }
        return "";
    }

    private String getQandA(JsonNode row) {
        String q = row.get("question").get("text").asText();
        String a = delStr(row.get("answer").get("text").asText());
        return "<br>Q：" + q + "<br><br>" + "A：" + a + "\n";
    }


    private LinkedList<String> getImagesArray(JsonNode talkNode) {
        LinkedList<String> images = new LinkedList<>();
        ArrayNode imagesArray = (ArrayNode) talkNode.get("images");
        if (imagesArray != null) {
            for (JsonNode image : imagesArray) {
                images.add(image.get("large").get("url").asText());
            }
        }
        return images;
    }

    private LinkedList<String> getFilesArray(JsonNode talkNode, HashMap<String, String> header) {
        LinkedList<String> files = new LinkedList<>();
        JsonNode fileJson = talkNode.get("files");
        if (null != fileJson) {
            for (JsonNode fj : fileJson) {
                String fileId = fj.get("file_id").asText();
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                files.add(getFileUrl(fileId, header));
            }
        }
        return files;
    }

    private String delStr(String str) {
        try {
            if (str.contains("<e")) {
                String decodeStr = URLDecoder.decode(str, "utf-8");
                return decodeStr.replace("<e", "<a").replace("/>", ">链接</a>");
            }
            return str;
        } catch (Exception e) {
            return str;
        }
    }

}
