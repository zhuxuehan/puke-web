package com.zzz.puke.controlloer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.zzz.puke.anno.InterfaceCount;
import com.zzz.puke.bean.ContentMode;
import com.zzz.puke.bean.PageData;
import com.zzz.puke.enums.PukeURL;
import com.zzz.puke.service.MethodService;
import com.zzz.puke.service.PukeContentService;
import com.zzz.puke.service.WXContentService;
import com.zzz.puke.service.ZsxqContentService;
import com.zzz.puke.utils.WechatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.LinkedList;

@Controller
public class ContentController {

    @Autowired
    PukeContentService pukeContentService;

    @Autowired
    WXContentService wxContentService;

    @Autowired
    MethodService methodService;

    @Autowired
    ZsxqContentService zsxqContentService;


    @GetMapping("/list")
    @InterfaceCount
    public String list(Model model,
                       @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                       @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                       @RequestParam(value = "randomV", defaultValue = "10") String randomV
    ) {
        LinkedList<ContentMode> contents = new LinkedList<>();
        ObjectMapper listMapper = new ObjectMapper();
        String list = pukeContentService.getContentList(pageNum, 10);
        JsonNode listNode;
        try {
            listNode = listMapper.readValue(list, JsonNode.class);
            JsonNode data = listNode.get("data");
            ArrayNode rows = (ArrayNode) data.get("rows");
            int j = 0;
            int maxId = 0;
            if (pageNum == 1) {
                for (int i = 0; i < rows.size(); i++) {
                    JsonNode row = rows.get(i);
                    int id = row.get("id").asInt();
                    if (id > maxId) {
                        maxId = id;
                        j = i;
                    }
                }
            }

            for (int i = j; i < rows.size(); i++) {
                JsonNode row = rows.get(i);
                ContentMode contentMode = new ContentMode();
                contentMode.setId(row.get("id").asText());
                contentMode.setTime(row.get("edit_at").asText());
                contentMode.setContent(row.get("content").asText().replace("</p>......", "......</p>"));

                //图片
                ArrayNode imagesNode = (ArrayNode) row.get("images");
                ArrayList<String> images = new ArrayList<>(imagesNode.size());
                for (int k = 0; k < imagesNode.size(); k++) {
                    images.add(imagesNode.get(k).get("url").asText());
                }
                contentMode.setImages(images);


                //录音
                ArrayNode audiosNode = (ArrayNode) row.get("audios");
                ArrayList<String> audios = new ArrayList<>(audiosNode.size());
                for (int k = 0; k < audiosNode.size(); k++) {
                    audios.add(audiosNode.get(k).get("src").asText());
                }
                contentMode.setAudios(audios);

                //文件

                //评论
                ArrayNode list_commentsNode = (ArrayNode) row.get("list_comments");
                ArrayList<String> list_comments = new ArrayList<>(list_commentsNode.size());
                for (int k = 0; k < list_commentsNode.size(); k++) {
                    list_comments.add(list_commentsNode.get(k).get("content").asText());
                }
                contentMode.setComments(list_comments);

                contents.add(contentMode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        PageData<LinkedList<ContentMode>> pageData = new PageData<>();
        pageData.setData(contents);
        pageData.setPageNum(pageNum);
        pageData.setPageSize(pageSize);
        model.addAttribute("pageData", pageData);
        return "index";
    }


    @GetMapping("/get/{id}")
    @InterfaceCount
    public String getContentByid(Model model, @PathVariable(value = "id") Integer id) {
        ContentMode contentDetail = new ContentMode();
        ObjectMapper contentMapper = new ObjectMapper();
        String contentRespon = pukeContentService.getContent(id);
        JsonNode contentNode = null;
        try {
            contentNode = contentMapper.readValue(contentRespon, JsonNode.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonNode contentData = contentNode.get("data");
        if (contentData != null) {
            //id
            contentDetail.setId(contentData.get("id").asText());
            contentDetail.setTime(contentData.get("edit_at").asText());
            //contnet
            contentDetail.setContent(contentData.get("content").asText());
            //图片
            JsonNode imagesNode = contentData.get("images");
            ArrayList<String> images = new ArrayList<>();
            if (imagesNode.size() > 0) {
                for (int j = 0; j < imagesNode.size(); j++) {
                    images.add(PukeURL.FILE_PRE + imagesNode.get(j).get("src").asText());
                }
            }
            contentDetail.setImages(images);
            //录音
            JsonNode audiosNode = contentData.get("audios");
            ArrayList<String> audios = new ArrayList<>();
            if (audiosNode.size() > 0) {
                for (int j = 0; j < audiosNode.size(); j++) {
                    audios.add(audiosNode.get(j).get("src").asText());
                }
            }
            contentDetail.setAudios(audios);
        }
        model.addAttribute("contentDetail", contentDetail);
        return "detail";
    }

    @GetMapping("/setHeader")
    @ResponseBody
    public String setHeader() {
        pukeContentService.setHeader();
        return "success";
    }

    @GetMapping("/sendURl")
    @ResponseBody
    public String sendURl() {
        pukeContentService.sendHistory();
        return "success";
    }

    @GetMapping("/getVoice")
    public String getVoice() {
        return "wxvoice.html";
    }

    @GetMapping("/getVoiceList/{code}")
    @InterfaceCount
    public String getVoiceList(Model model, @PathVariable(value = "code") String code) {
        ArrayList<String> voiceList = wxContentService.getVoiceList(code);
        ContentMode contentDetail = new ContentMode();
        //id
        contentDetail.setId(code);
        contentDetail.setTime("0000-00-00 00:00:00");

        //录音
        ArrayList<String> audios = new ArrayList<>();
        if (voiceList.size() > 0) {
            for (String src : voiceList) {
                audios.add(src);
            }
        }
        contentDetail.setAudios(audios);

        model.addAttribute("contentDetail", contentDetail);
        return "detail";
    }

    @GetMapping("/updatePage")
    public String udpatePage() {
        return "update.html";
    }

    @GetMapping("/update")
    public void updateToken(String tok, String newLastId) {
        pukeContentService.updateCookie(tok, newLastId);
    }


    @GetMapping("/getCount")
    @ResponseBody
    public String getCount() {
        methodService.sendCountAndClear();
        return "success";
    }

    @GetMapping("/getzsxq/{group}")
    @ResponseBody
    public String zzxq(String group) {
        zsxqContentService.getAllContentAndSend();
        return "success";
    }


}
