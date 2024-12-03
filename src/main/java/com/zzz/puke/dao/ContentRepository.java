package com.zzz.puke.dao;

import com.zzz.puke.bean.ContentPacket;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepository extends MongoRepository<ContentPacket, String> {
    // 自定义查询方法
}
