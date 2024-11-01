package com.zzz.puke.utils;

import com.overzealous.remark.Remark;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.entity.ContentType;

import java.util.HashMap;
import java.util.List;

public class WechatUtils {

    public static String ERR = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=1d8d10cf-e460-44de-8a72-28fd211a0185";

    public static void sendWechatMessage(String localurl, String webhook, String lastId, String time, String content, List<String> images, List<String> audios, List<String> files, List<String> list_comments) {
        HashMap<String, String> paramMap = new HashMap<>();
        String text = "";
        if (!content.isEmpty()) {
            text += "[" + lastId + "](" + localurl + lastId + ")\n";
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
                    sendMessage(substring, webhook);
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
                text += "\n[图片" + j + "](" + images.get(j) + ") \n";
            }
        }

        if (audios.size() > 0) {
            for (int j = 0; j < audios.size(); j++) {
                text += "\n[录音" + j + "](" + audios.get(j) + ") \n";
            }
        }

        if (files.size() > 0) {
            for (int j = 0; j < files.size(); j++) {
                text += "\n[文件" + j + "](" + files.get(j) + ") \n";
            }
        }

        for (int k = 0; k < list_comments.size(); k++) {
            text += "\n#### 评论" + k + "：" + list_comments.get(k) + "\n";
        }

        paramMap.put("message", MarkdownUtils.getMarkdown(text));
        HttpUtils.doPost(webhook, paramMap, null, ContentType.APPLICATION_JSON);
    }


    public static void sendErrorMessage(String str) {
        sendMessage(str, ERR);
    }

    public static void sendMessage(String str, String url) {
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("message", MarkdownUtils.getMarkdown(str));
        HttpUtils.doPost(url, paramMap, null, ContentType.APPLICATION_JSON);
        System.out.println();
    }


}
