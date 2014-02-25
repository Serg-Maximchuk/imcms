package com.imcode.imcms.mapping.dao;

import com.imcode.imcms.mapping.orm.DocLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DocLanguageDao extends JpaRepository<DocLanguage, Integer> {

    DocLanguage findByCode(String code);

    @Modifying
    @Query("DELETE FROM DocLanguage l WHERE l.code = ?1")
    int deleteByCode(String code);
}
