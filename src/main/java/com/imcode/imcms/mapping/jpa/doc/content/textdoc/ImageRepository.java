package com.imcode.imcms.mapping.jpa.doc.content.textdoc;

import com.imcode.imcms.mapping.jpa.doc.Version;
import com.imcode.imcms.mapping.jpa.doc.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {

    @Query("SELECT l FROM Image l WHERE l.docVersion = ?1 AND l.language = ?2 AND l.loopEntryRef IS NULL")
    List<Image> findByDocVersionAndLanguageAndLoopEntryRefIsNull(Version version, Language language);

    @Query("SELECT l FROM Image l WHERE l.docVersion = ?1 AND l.language = ?2 AND l.loopEntryRef = IS NOT NULL")
    List<Image> findByDocVersionAndLanguageAndLoopEntryRefIsNotNull(Version version, Language language);


    @Query("SELECT l FROM Image l WHERE l.docVersion = ?1 AND l.no = ?2 AND l.loopEntryRef IS NULL")
    List<Image> findByDocVersionAndNoAndLoopEntryRefIsNull(Version version, int no);

    //@Query("SELECT l FROM Image l WHERE l.docVersion = ?1 AND l.no = ?2 AND l.loopEntryRef = ?3")
    List<Image> findByDocVersionAndNoAndLoopEntryRef(Version version, int no, LoopEntryRef loopEntryRef);


    @Query("SELECT l FROM Image l WHERE l.docVersion = ?1 AND l.language = ?2 AND l.no = ?3 AND l.loopEntryRef IS NULL")
    Image findByDocVersionAndLanguageAndNoAndLoopEntryRefIsNull(Version version, Language language, int no);

    //@Query("SELECT l FROM Image l WHERE l.docVersion = ?1 AND l.language = ?2 AND l.no = ?3 AND l.loopEntryRef = ?4")
    Image findByDocVersionAndLanguageAndNoAndLoopEntryRef(Version version, Language language, int no, LoopEntryRef loopEntryRef);

    @Query("DELETE FROM Image i WHERE i.docVersion = ?1 AND i.language = ?2")
    int deleteByDocVersionAndLanguage(Version version, Language language);
}
