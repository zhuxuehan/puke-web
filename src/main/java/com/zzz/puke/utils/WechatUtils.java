package com.zzz.puke.utils;

import com.overzealous.remark.Remark;
import com.zzz.puke.bean.ContentPacket;
import com.zzz.puke.bean.MessagePacket;
import org.apache.http.entity.ContentType;

import java.util.HashMap;

public class WechatUtils {

    public static String ERR = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=1d8d10cf-e460-44de-8a72-28fd211a0185";

    public static void sendWechatMessage(MessagePacket mPacket) {
        HashMap<String, String> paramMap = new HashMap<>();
        String text = "";
        ContentPacket cPacket = mPacket.getContentPacket();
        String content = cPacket.getText();
        if (!content.isEmpty()) {
            text += "[" + cPacket.getId() + "](" + mPacket.getLocalUrl() + ")\n";
            text += "###### " + cPacket.getCurrTime() + "\n\n";
            Remark remark = new Remark();
            String convert = remark.convert(content) + "\n";
            try {
                int byteLength = convert.getBytes("utf-8").length;
                while (byteLength > 3000) {
                    int length = convert.length();
                    String substring = cPacket.getId() + "\n" + cPacket.getCurrTime() + "\n\n";
                    substring += convert.substring(0, length / 2);
                    convert = convert.substring(length / 2);
                    sendMessage(substring, mPacket.getLocalUrl());
                    byteLength -= byteLength / 2;
                }
            } catch (Exception e) {
                e.printStackTrace();
                WechatUtils.sendErrorMessage(e.toString());
            }
            text += convert;
        }

        if (cPacket.getImages().size() > 0) {
            for (int j = 0; j < cPacket.getImages().size(); j++) {
                text += "\n[图片" + j + "](" + cPacket.getImages().get(j) + ") \n";
            }
        }

        if (cPacket.getAudios().size() > 0) {
            for (int j = 0; j < cPacket.getAudios().size(); j++) {
                text += "\n[录音" + j + "](" + cPacket.getAudios().get(j) + ") \n";
            }
        }

        if (cPacket.getFiles().size() > 0) {
            for (int j = 0; j < cPacket.getFiles().size(); j++) {
                text += "\n[文件" + j + "](" + cPacket.getFiles().get(j) + ") \n";
            }
        }

        for (int k = 0; k < cPacket.getComments().size(); k++) {
            text += "\n#### 评论" + k + "：" + cPacket.getComments().get(k) + "\n";
        }

        paramMap.put("message", MarkdownUtils.getMarkdown(text));
        HttpUtils.doPost(mPacket.getWebhook(), paramMap, null, ContentType.APPLICATION_JSON);
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
