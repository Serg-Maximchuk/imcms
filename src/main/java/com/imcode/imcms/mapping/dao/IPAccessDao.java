package com.imcode.imcms.mapping.dao;

import com.imcode.imcms.mapping.orm.IPAccess;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public interface IPAccessDao extends CrudRepository<IPAccess, Integer> {

    List<IPAccess> findAll();
}
