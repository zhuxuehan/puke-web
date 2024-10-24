package com.zzz.puke.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.overzealous.remark.Remark;
import com.zzz.puke.enums.PukeURL;
import org.apache.http.entity.ContentType;

import java.util.HashMap;

public class WechatUtils {
    public static String PUKE = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=be5b3c4a-e9f5-43c9-8edb-a501d5196438";
    public static String ERR = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=1d8d10cf-e460-44de-8a72-28fd211a0185";
    public static String LOCAL = "http://139.196.238.213/get/";

    public static void sendWechatMessage(int lastId, String time, String content, JsonNode images, JsonNode audios, JsonNode files, ArrayNode list_comments) {
        HashMap<String, String> paramMap = new HashMap<>();
        String text = "";
        if (!content.isEmpty()) {
            text += "[" + lastId + "](" + LOCAL + lastId + ")\n";
            text += "###### " + time + "\n\n";
            Remark remark = new Remark();
            String convert = remark.convert(content) + "\n";
            try {
                int byteLength = convert.getBytes("utf-8").length;
                while (byteLength > 3000) {
                    int length = convert.length();
                    String substring = lastId + "\n" + time + "\n\n";
                    substring += convert.substring(0, length / 2);
                    convert = convert.substring(length / 2);
                    sendSimpleMessage(substring);
                    byteLength -= byteLength / 2;
                }
            } catch (Exception e) {
                e.printStackTrace();
                WechatUtils.sendErrorMessage(e.toString());
            }
            text += convert;
        }
        if (images.size() > 0) {
            for (int j = 0; j < images.size(); j++) {
//                text += "![](" + PukeURL.FILE_PRE + images.get(j).get("src").asText() + ")\n";
                text += "\n[图片" + j + "](" + PukeURL.FILE_PRE + images.get(j).get("src").asText() + ") \n";
            }
        }
        if (audios.size() > 0) {
            for (int j = 0; j < audios.size(); j++) {
                text += "\n[录音" + j + "](" + audios.get(j).get("src").asText() + ") \n";
            }
        }
        if (files.size() > 0) {
            for (int j = 0; j < files.size(); j++) {
                text += "\n[文件" + j + "](" + files.get(j).get("src").asText() + ") \n";
            }
        }
        for (int k = 0; k < list_comments.size(); k++) {
            text += "\n#### 评论" + k + "：" + list_comments.get(k).get("content").asText() + "\n";
        }

        paramMap.put("message", MarkdownUtils.getMarkdown(text));
        HttpUtils.doPost(PUKE, paramMap, null, ContentType.APPLICATION_JSON);
    }

    public static void sendSimpleMessage(String str) {
        sendMessage(str, PUKE);
    }

    public static void sendErrorMessage(String str) {
        sendMessage(str, ERR);
    }

    public static void sendMessage(String str, String url) {
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("message", MarkdownUtils.getMarkdown(str));
        HttpUtils.doPost(url, paramMap, null, ContentType.APPLICATION_JSON);
    }


}
