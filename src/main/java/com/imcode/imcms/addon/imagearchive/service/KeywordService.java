package com.imcode.imcms.addon.imagearchive.service;

import com.imcode.imcms.addon.imagearchive.entity.*;
import com.imcode.imcms.addon.imagearchive.service.exception.KeywordExistsException;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;

@Transactional
public class KeywordService {
    @Autowired
    @Qualifier("hibernateTemplate")
    private HibernateTemplate template;

    public Keywords createKeyword(final String newKeywordName) throws KeywordExistsException {
        return (Keywords) template.execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                if (existsKeyword(newKeywordName)) {
                    throw new KeywordExistsException();
                }

                Keywords keyword = new Keywords();
                keyword.setKeywordNm(newKeywordName);
                session.persist(keyword);
                session.flush();
                return keyword;
            }

        });
    }

    @SuppressWarnings("unchecked")
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Keywords> getKeywords() {
        return (List<Keywords>) template.execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria crit = session.createCriteria(Keywords.class, "k");
                crit.addOrder(Order.asc("k.keywordNm"));
                return crit.list();
            }
        });
    }

    public void deleteKeyword(final long keywordId) {
        template.execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.createQuery("DELETE FROM ImageKeywords ik WHERE ik.keywordId = :keywordId")
                        .setLong("keywordId", keywordId)
                        .executeUpdate();

                session.createQuery("DELETE FROM Keywords k WHERE k.id = :keywordId")
                        .setLong("keywordId", keywordId)
                        .executeUpdate();

                return null;
            }
        });
    }

    public void updateKeyword(final long keywordId, final String keywordName) throws KeywordExistsException {
        template.execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                if (existsKeyword(keywordId, keywordName)) {
                    throw new KeywordExistsException();
                }

                session.createQuery(
                        "UPDATE Keywords k SET k.keywordNm = :keywordName WHERE k.id = :keywordId")
                        .setString("keywordName", keywordName)
                        .setLong("keywordId", keywordId)
                        .executeUpdate();

                return null;
            }
        });
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public boolean existsKeyword(final String newKeywordName) {
        return (Boolean) template.execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return ((Number) session.createCriteria(Keywords.class, "k")
                        .add(Restrictions.sqlRestriction("{alias}.keyword_nm COLLATE utf8_bin = ?", newKeywordName, Hibernate.STRING))
                        .setProjection(Projections.rowCount())
                        .list().get(0)).longValue() > 0L;
            }
        });
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public boolean existsKeyword(final long keywordId, final String newKeywordName) {
        return (Boolean) template.execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return ((Number) session.createCriteria(Keywords.class, "k")
                        .add(Restrictions.sqlRestriction("{alias}.keyword_nm COLLATE utf8_bin = ?", newKeywordName, Hibernate.STRING))
                        .add(Restrictions.ne("k.id", keywordId))
                        .setProjection(Projections.rowCount())
                        .list().get(0)).longValue() > 0L;
            }
        });
    }
}