package com.zzz.puke.dao;

import com.zzz.puke.bean.CircleWebhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CircleWebhookRepository extends JpaRepository<CircleWebhook, Long> {

    @Query("SELECT cw FROM CircleWebhook cw WHERE cw.circleId = :circleId AND cw.contentChannel = :contentChannel")
    List<CircleWebhook> findByCircleAndChannel(@Param("circleId") String circleId, @Param("contentChannel")  Enum contentChannel);

    @Query("SELECT cw FROM CircleWebhook cw WHERE cw.contentChannel = :contentChannel")
    List<CircleWebhook> findByChannel(@Param("contentChannel") Enum contentChannel);


}
