package com.zzz.puke.task;

import com.zzz.puke.service.MethodService;
import com.zzz.puke.service.PukeContentService;
import com.zzz.puke.utils.WechatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SendMessageTask {

    @Autowired
    PukeContentService pukeContentService;

    @Autowired
    MethodService methodService;

    public static int num;

    @Scheduled(cron = "0/15 * 7-15 * * ?")
    public void execute() {
        pukeContentService.getContentAndSend();
    }

    @Scheduled(cron = "0 */20 15-23 * * ?")
    public void execute2() {
        pukeContentService.getContentAndSend();
    }

    @Scheduled(cron = "0 */20 0-7 * * ?")
    public void execute3() {
        pukeContentService.getContentAndSend();
    }

    @Scheduled(cron = "0 0 7 * * ?")
    public void sendUrl() {
        WechatUtils.sendSimpleMessage("[点击查看历史消息](http://82.157.136.21/list)");
    }

    @Scheduled(cron = "0 0 7 * * ?")
    public void sendCount() {
        methodService.sendCountAndClear();
    }

}
