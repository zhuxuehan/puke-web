package com.zzz.puke.controlloer;

import com.zzz.puke.anno.InterfaceCount;
import com.zzz.puke.bean.ContentMode;
import com.zzz.puke.bean.PageData;
import com.zzz.puke.bean.ContentPacket;
import com.zzz.puke.service.MethodService;
import com.zzz.puke.service.WXContentService;
import com.zzz.puke.service.ZsxqContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Controller()
@RequestMapping("/xq")
public class XqController {

    @Autowired
    WXContentService wxContentService;

    @Autowired
    MethodService methodService;

    @Autowired
    ZsxqContentService zsxqContentService;

    //星球网页
    @GetMapping("/list/{group}")
    @InterfaceCount
    public String listXQ(Model model,
                         @PathVariable(value = "group") String group,
                         @RequestParam(value = "end_time", required = false) String end_time) {
        LinkedList<ContentMode> contents = new LinkedList<>();
        HashMap<String, String> params = new HashMap<>();
        if (null != end_time) {
            params.put("end_time", end_time);
        }
//        else {
//            params.put("end_time", );
//        }

        String lastTime = "";
        List<ContentPacket> packetsList = zsxqContentService.getXqPacketsList(group, "", params);
        try {
            for (int i = packetsList.size() - 1; i >= 0; i--) {
                ContentPacket contentPacket = packetsList.get(i);
                ContentMode contentMode = new ContentMode();
                contentMode.setId(contentPacket.getId());
                contentMode.setTime(contentPacket.getCurrTime());
                contentMode.setContent(contentPacket.getText());
                contentMode.setGroup(contentPacket.getGroup());

                //图片
                contentMode.setImages(contentPacket.getImages());
                //录音
                //文件
                contentMode.setFiles(contentPacket.getFiles());
                //评论
//                contentMode.setComments(list_comments);
                contents.add(contentMode);
                lastTime = contentPacket.getCurrTime();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        PageData<LinkedList<ContentMode>> pageData = new PageData<>();
        pageData.setData(contents);
        pageData.setPageNum(1);
        pageData.setPageSize(1);
        try {
            pageData.setEndTime(URLEncoder.encode(lastTime, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        model.addAttribute("pageData", pageData);
        return "xqindex";
    }

    @RequestMapping("/list/{group}")
    public String getFile() {
        zsxqContentService.getAllContentAndSend();
        return "success";
    }


    @GetMapping("/getzsxq/all")
    @ResponseBody
    public String zzxqAll() {
        zsxqContentService.getAllContentAndSend();
        return "success";
    }

}
