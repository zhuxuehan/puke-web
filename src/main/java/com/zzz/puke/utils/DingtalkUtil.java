package com.zzz.puke.utils;


import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.overzealous.remark.Remark;
import com.zzz.puke.bean.ContentPacket;
import com.zzz.puke.bean.MessagePacket;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;

public class DingtalkUtil {

    public static void sendDingtalkMessage(MessagePacket mPacket) {
        DingTalkClient client = new DefaultDingTalkClient(mPacket.getWebhook() + qinaming(mPacket.getSecret()));
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
        at.setIsAtAll(true);
        request.setAt(at);

        request.setMsgtype("markdown");
        OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
//        markdown.setTitle(localurl);
        String text = "";
        ContentPacket cPacket = mPacket.getContentPacket();
        String content = cPacket.getText();
        if (!content.isEmpty()) {
            text += "[" + cPacket.getId() + "](" + mPacket.getLocalUrl() + cPacket.getId() + ")\n";
            text += "###### " + cPacket.getCurrTime() + "\n\n";
            Remark remark = new Remark();
            String convert = remark.convert(content) + "\n";
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
        markdown.setText(text);
        request.setMarkdown(markdown);
        try {
            client.execute(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String qinaming(String secret) {
        Long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + secret;

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
            String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
            return "?timestamp=" + timestamp + "&sign=" + sign;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
