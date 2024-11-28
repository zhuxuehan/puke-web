package com.zzz.puke.service;

import com.zzz.puke.enums.ContentChannel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

public class SendService {

    @Autowired
    PukeContentService pukeContentService;

    @Autowired
    ZsxqContentService zsxqContentService;

    void sendContent(ContentChannel c, HashMap<String, String> params) {
        switch (c) {
            case PUKE:
//                pukeContentService.getContentAndSend();
            case ZSXQ:
//                zsxqContentService.getContentAndSend(params);
        }
    }
}
