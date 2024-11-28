package com.zzz.puke.enums;

public enum WebhookChannel {
    WECHAT("微信"),
    DINGTALK("钉钉");

    private final String chineseName;

    // 私有构造方法，用于初始化枚举常量
    WebhookChannel(String chineseName) {
        this.chineseName = chineseName;
    }

    // 获取中文名字的方法
    public String getChineseName() {
        return chineseName;
    }


}
