package com.zzz.puke.dao;

import com.zzz.puke.bean.CircleWebhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CircleWebhookRepository extends JpaRepository<CircleWebhook, Long> {

    @Query("SELECT cw FROM CircleWebhook cw WHERE cw.circleId = :circleId AND cw.contentChannel = :contentChannel")
    List<CircleWebhook> findByCircleAndChannel(@Param("circleId") String circleId, @Param("contentChannel") String contentChannel);

    @Query("SELECT cw FROM CircleWebhook cw WHERE cw.contentChannel = :contentChannel")
    List<CircleWebhook> findByChannel(@Param("contentChannel") String contentChannel);


}
