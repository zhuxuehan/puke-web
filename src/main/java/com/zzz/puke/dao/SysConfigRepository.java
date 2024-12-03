package com.zzz.puke.dao;

import com.zzz.puke.bean.SysConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SysConfigRepository extends JpaRepository<SysConfig, Long> {

    @Query("select sc from SysConfig sc where sc.sysK = :sysk")
    SysConfig findByK(@Param("sysk") String sysk);
}
