package com.zzz.puke.dao;

import com.zzz.puke.bean.Tokens;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokensRepository extends CrudRepository<Tokens, Long> {
}
