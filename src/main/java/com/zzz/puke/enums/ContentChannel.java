package com.zzz.puke.enums;

public enum ContentChannel {
    ZSXQ("知识星球"),
    PUKE("扑克");

    private final String chineseName;

    // 私有构造方法，用于初始化枚举常量
    ContentChannel(String chineseName) {
        this.chineseName = chineseName;
    }

    // 获取中文名字的方法
    public String getChineseName() {
        return chineseName;
    }


}
