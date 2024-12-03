package com.zzz.puke.controlloer;

import com.zzz.puke.anno.InterfaceCount;
import com.zzz.puke.bean.ContentMode;
import com.zzz.puke.bean.ContentPacket;
import com.zzz.puke.bean.PageData;
import com.zzz.puke.service.MethodService;
import com.zzz.puke.service.PukeContentService;
import com.zzz.puke.service.WXContentService;
import com.zzz.puke.service.ZsxqContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Controller
@RequestMapping("/pk")
public class ContentController {

    @Autowired
    PukeContentService pukeContentService;

    @Autowired
    WXContentService wxContentService;

    @Autowired
    MethodService methodService;

    @Autowired
    ZsxqContentService zsxqContentService;


    @GetMapping("/list/{circleid}")
    @InterfaceCount
    public String list(Model model,
                       @PathVariable(value = "circleid") String circleid,
                       @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                       @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        LinkedList<ContentMode> contents = new LinkedList<>();
        List<ContentPacket> pkList = pukeContentService.getPkPacketsListFromRemote(circleid, "user", pageNum, pageSize);
        int j = 0;
        int maxId = 0;
        if (pageNum == 1) {
            for (int i = 0; i < pkList.size(); i++) {
                ContentPacket contentPacket = pkList.get(i);
                int id = Integer.parseInt(contentPacket.getId());
                if (id > maxId) {
                    maxId = id;
                    j = i;
                }
            }
        }

        for (int i = j; i < pkList.size(); i++) {
            ContentPacket contentPacket = pkList.get(i);
            ContentMode contentMode = new ContentMode();
            contentMode.setId(contentPacket.getId());
            contentMode.setGroup(contentPacket.getGroup());
            contentMode.setTime(contentPacket.getCurrTime());
            contentMode.setContent(contentPacket.getText().replace("</p>......", "......</p>"));

            //图片
            contentMode.setImages(contentPacket.getImages());
            //录音
            contentMode.setAudios(contentPacket.getAudios());
            //文件
            //评论
            contentMode.setComments(contentMode.getComments());
            contents.add(contentMode);
        }
        PageData<LinkedList<ContentMode>> pageData = new PageData<>();
        pageData.setData(contents);
        pageData.setPageNum(pageNum);
        pageData.setPageSize(pageSize);
        model.addAttribute("pageData", pageData);
        return "index";
    }

    @GetMapping("/get/{circleid}/{id}")
    @InterfaceCount
    public String getContentByid(Model model,
                                 @PathVariable(value = "circleid") String circleid,
                                 @PathVariable(value = "id") Integer id) {
        ContentMode contentDetail = new ContentMode();
        String contentRespon = pukeContentService.getContent(circleid, id);
        ContentPacket cPacket = pukeContentService.getPkPacket(contentRespon);

        //重新打包
        contentDetail.setId(cPacket.getId());
        contentDetail.setTime(cPacket.getCurrTime());
        contentDetail.setContent(cPacket.getText());
        contentDetail.setImages(cPacket.getImages());
        contentDetail.setAudios(cPacket.getAudios());
        model.addAttribute("contentDetail", contentDetail);
        return "detail";
    }

    @GetMapping("/setHeader")
    @ResponseBody
    public String setHeader() {
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


}
