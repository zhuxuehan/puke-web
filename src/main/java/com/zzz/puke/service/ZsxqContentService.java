package com.zzz.puke.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.zzz.puke.bean.XqPacket;
import com.zzz.puke.bean.ZsxqKv;
import com.zzz.puke.dao.SysConfigRepository;
import com.zzz.puke.dao.ZsxqKvRepository;
import com.zzz.puke.utils.HttpUtils;
import com.zzz.puke.utils.WechatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class ZsxqContentService {

    @Autowired
    ZsxqKvRepository zsxqKvRepository;

    @Autowired
    SysConfigRepository sysConfigRepository;

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
        String zsxqWebhook = groupKv.getZsxqWebhook();
        HashMap<String, String> header = getHeader(groupKv);

        if (redisTemplate.hasKey(group)) {
            return;
        }
        redisTemplate.opsForValue().set(group, group, groupKv.getIntervalTime(), TimeUnit.SECONDS);
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
                    XqPacket packet = getXqPacket(row, header);
                    String localurl = "localhost/xq/group/id?scop=1&endtime";
                    WechatUtils.sendWechatMessage(localurl, zsxqWebhook,
                            packet.getId(), curTime, packet.getText(),
                            packet.getImages(), packet.getAudios(),
                            packet.getFiles(), packet.getComments());
                    lastTime = curTime;
                }
            }
            groupKv.setZsxqLastTime(lastTime);
            groupKv.setLastSendTime(new Date());
            //设置上一次id
            zsxqKvRepository.save(groupKv);
        } catch (Exception e) {
            e.printStackTrace();
            WechatUtils.sendErrorMessage("ContentAndSend:" + e + groupKv.getZsxqGroup());
        }

    }

    public List<XqPacket> getXqPacketsList(String group, String user, HashMap<String, String> params) {
        ArrayList<XqPacket> pakcetsList = new ArrayList<>(20);
        ZsxqKv kv = zsxqKvRepository.findByGroup(group);
        HashMap<String, String> header = getHeader(kv);
        String list = HttpUtils.doGet(String.format(ZSXQ_GROUP_URL, group), params, header);
        ObjectMapper listMapper = new ObjectMapper();
        JsonNode listNode = null;
        try {
            listNode = listMapper.readValue(list, JsonNode.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayNode rows = (ArrayNode) listNode.get("resp_data").get("topics");
        if (null == rows) {
            return new ArrayList<>();
        }
        for (int i = rows.size() - 1; i >= 0; i--) {
            JsonNode row = rows.get(i);
            XqPacket packet = getXqPacket(row, header);
            pakcetsList.add(packet);
        }
        return pakcetsList;
    }

    public XqPacket getXqPacket(JsonNode row, HashMap<String, String> header) {
        XqPacket xqPacket = new XqPacket();
        String type = row.get("type").asText();
        xqPacket.setType(type);
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
        xqPacket.setText(text);
        lastId = row.get("topic_id").asText();
        xqPacket.setId(lastId);
        xqPacket.setCurrTime(row.get("create_time").asText());
        if (null != talkNode) {
            //图片
            xqPacket.setImages(getImagesArray(talkNode));
            //文件
            xqPacket.setFiles(getFilesArray(talkNode, header));
        }
        return xqPacket;
    }

    private String getFileUrl(String fileId, HashMap<String, String> header) {
        String u = "https://api.zsxq.com/v2/files/" + fileId + "/download_url";
        String list = HttpUtils.doGet(u, new HashMap<>(), header);
        ObjectMapper listMapper = new ObjectMapper();
        try {
            JsonNode listNode = listMapper.readValue(list, JsonNode.class);
            if (null != listNode.get("resp_data")) {
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
            return talk.get("text").asText();
        }
        return "";
    }

    private String getQandA(JsonNode row) {
        String q = row.get("question").get("text").asText();
        String a = row.get("answer").get("text").asText();
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


}
