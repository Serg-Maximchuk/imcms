package com.imcode.imcms.mapping.jpa.doc.content.textdoc;

import com.imcode.imcms.mapping.container.DocVersionRef;
import com.imcode.imcms.mapping.jpa.JpaConfiguration;
import com.imcode.imcms.mapping.jpa.User;
import com.imcode.imcms.mapping.jpa.UserRepository;
import com.imcode.imcms.mapping.jpa.doc.DocVersion;
import com.imcode.imcms.mapping.jpa.doc.DocVersionRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JpaConfiguration.class})
@Transactional
public class LoopRepositoryTest {

    static final DocVersionRef DOC_VERSION_REF = new DocVersionRef(1001, 0);

    @Inject
    UserRepository userRepository;

    @Inject
    DocVersionRepository docVersionRepository;

    @Inject
    LoopRepository loopRepository;

    @PersistenceContext
    EntityManager entityManager;

    public List<Loop> recreateLoops() {
        loopRepository.deleteAll();
        docVersionRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.saveAndFlush(new User("admin", "admin", "admin@imcode.com"));
        DocVersion docVersion = docVersionRepository.saveAndFlush(
                new DocVersion(
                        DOC_VERSION_REF.getDocId(),
                        DOC_VERSION_REF.getDocVersionNo(),
                        user,
                        new Date(),
                        user, new Date()
                )
        );

        return Arrays.asList(
                loopRepository.saveAndFlush(
                    new Loop(
                            docVersion,
                            1,
                            2,
                            Arrays.asList(
                                new Loop.Entry(1)
                            )
                    )
                ),

                loopRepository.saveAndFlush(
                    new Loop(
                            docVersion,
                            2,
                            3,
                            Arrays.asList(
                                    new Loop.Entry(1),
                                    new Loop.Entry(2)
                            )
                    )
                ),

                loopRepository.saveAndFlush(
                    new Loop(
                            docVersion,
                            3,
                            4,
                            Arrays.asList(
                                    new Loop.Entry(1),
                                    new Loop.Entry(2),
                                    new Loop.Entry(3)
                            )
                    )
                )
        );
    }

    @Test
    public void textFindByDocVersion() {
        recreateLoops();

        DocVersion docVersion = docVersionRepository.findByDocIdAndNo(DOC_VERSION_REF.getDocId(), DOC_VERSION_REF.getDocVersionNo());
        List<Loop> loops = loopRepository.findByDocVersion(docVersion);

        assertThat(loops.size(), is(3));
    }

    @Test
    public void textFindByDocVersionAndNo() {
        recreateLoops();

        DocVersion docVersion = docVersionRepository.findByDocIdAndNo(DOC_VERSION_REF.getDocId(), DOC_VERSION_REF.getDocVersionNo());
        Loop loop1 = loopRepository.findByDocVersionAndNo(docVersion, 1);
        Loop loop2 = loopRepository.findByDocVersionAndNo(docVersion, 2);
        Loop loop3 = loopRepository.findByDocVersionAndNo(docVersion, 3);

        assertNotNull(loop1);
        assertNotNull(loop2);
        assertNotNull(loop3);
    }
}