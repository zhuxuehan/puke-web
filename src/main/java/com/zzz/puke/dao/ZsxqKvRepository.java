package com.zzz.puke.dao;

import com.zzz.puke.bean.ZsxqKv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ZsxqKvRepository extends JpaRepository<ZsxqKv, Long> {

    @Query("select z from ZsxqKv z where z.zsxqGroup = :zsxqGroup")
    ZsxqKv findByGroup(@Param("zsxqGroup") String zsxqGroup);
}
