package com.zzz.puke.dao;

import com.zzz.puke.bean.PukeKv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PukeKvRepository extends JpaRepository<PukeKv, Long> {

    @Query("select p from PukeKv p where p.circleid = :circleid")
    PukeKv findByCircle(@Param("circleid") String circleid);
}
