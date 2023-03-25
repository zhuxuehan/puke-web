package com.zzz.puke.utils;


public class DingtalkUtil {

    public static String hookUrl = "https:oapi.dingtalk.com/robot/send?access_token=c1772b7c6fbbf6275afe592acf018bb9a019bf8d538ac712d9e310d19146ddfd";
    public static String secret = "SECc0865563af1228835bef46e92b22a137594ab439d2fbf0a906536c2c5e13cf91";


//    public static void sendDingtalkMessage(int lastId, String content, JsonNode images, JsonNode audios, JsonNode files, ArrayNode list_comments) {
//        DingTalkClient client = new DefaultDingTalkClient(hookUrl + qinaming());
//        OapiRobotSendRequest request = new OapiRobotSendRequest();
//        OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
//         isAtAll类型如果不为Boolean，请升级至最新SDK
//        at.setIsAtAll(true);
//        request.setAt(at);
//
//        request.setMsgtype("markdown");
//        OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
//        markdown.setTitle(lastId + "");
//        String text = "";
//        if (!content.isEmpty()) {
//
//            text += "### " + content + "\n";
//        }
//        if (images.size() > 0) {
//            for (int j = 0; j < images.size(); j++) {
//                text += "![](" + PukeURL.FILE_PRE + images.get(j).get("src").asText() + ")\n";
//            }
//        }
//        if (audios.size() > 0) {
//            for (int j = 0; j < audios.size(); j++) {
//                text += "[录音" + j + "](" + audios.get(j).get("src").asText() + ") \n";
//            }
//        }
//        if (files.size() > 0) {
//            for (int j = 0; j < files.size(); j++) {
//                text += "[文件" + j + "](" + audios.get(j).get("src").asText() + ") \n";
//            }
//        }
//        for (int k = 0; k < list_comments.size(); k++) {
//            text += "#### 评论" + k + "：" + list_comments.get(k).get("content").asText() + "\n";
//        }
//        markdown.setText(text);
//        request.setMarkdown(markdown);
//        try {
//            OapiRobotSendResponse response = client.execute(request);
//            System.out.println(response.getErrmsg());
//        } catch (ApiException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static String qinaming() {
//        Long timestamp = System.currentTimeMillis();
//        String stringToSign = timestamp + "\n" + secret;
//        Mac mac = null;
//        try {
//            mac = Mac.getInstance("HmacSHA256");
//
//            mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
//            byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
//            String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
//            System.out.println(sign);
//
//            return "&timestamp=" + timestamp + "&sign=" + sign;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

}
