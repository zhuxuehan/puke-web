package com.zzz.puke.task;

import com.zzz.puke.service.MethodService;
import com.zzz.puke.service.PukeContentService;
import com.zzz.puke.service.ZsxqContentService;
import com.zzz.puke.utils.WechatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SendMessageTask {

    @Autowired
    PukeContentService pukeContentService;

    @Autowired
    ZsxqContentService zsxqContentService;

    @Autowired
    MethodService methodService;

    public static int num;

    @Scheduled(cron = "0 * 7-15 * * ?")
    public void execute() {
        pukeContentService.getAllContentAndSend();
        zsxqContentService.getAllContentAndSend();
    }

    @Scheduled(cron = "0 */1 15-23 * * ?")
    public void execute2() {
        pukeContentService.getAllContentAndSend();
        zsxqContentService.getAllContentAndSend();
    }

    @Scheduled(cron = "0 */20 0-7 * * ?")
    public void execute3() {
        pukeContentService.getAllContentAndSend();
        zsxqContentService.getAllContentAndSend();
    }

    @Scheduled(cron = "0 0 7 * * ?")
    public void sendUrl() {
        zsxqContentService.sendHistory();
        pukeContentService.sendHistory();

    }

    @Scheduled(cron = "0 0 7 * * ?")
    public void sendCount() {
        methodService.sendCountAndClear();
    }

}
