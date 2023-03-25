package com.zzz.puke.service;

import com.zzz.puke.enums.WXURL;
import com.zzz.puke.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WXContentService {
    private final static Logger logger = LoggerFactory.getLogger(WXContentService.class);

    public static int lastId;
    public static HashMap<String, String> header;

    private static Random random = new Random();

    @Cacheable(cacheNames = {"voiceList"}, key = "#code")
    public ArrayList<String> getVoiceList(String code) {
        HashMap<String, String> contentParams = new HashMap<>();
        HashMap<String, String> header = new HashMap<>();
        String content = HttpUtils.doGet(WXURL.WX_PRE + code, contentParams, header);
        String pattern = "\"voice_id\":\"(.*?)\"";
        HashSet<String> voiceSet = new HashSet<>();
        Matcher matcher = Pattern.compile(pattern).matcher(content);
        while (matcher.find()) {
            String ret = matcher.group(1);
            voiceSet.add(WXURL.WX_VOICE_PRE + ret);
        }
        ArrayList<String> result = new ArrayList<>(voiceSet);
        return result;
    }

}
