package com.imcode.imcms.mapping.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional(Transactional.TxType.SUPPORTS)
public interface UserRepository extends JpaRepository<User, Integer>, UserRepositoryCustom {

    User findByLogin(String login);

    User findByLoginIgnoreCase(String login);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER (?1)")
    List<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(?1)")
    User findByEmailUnique(String email);

    User findById(int id);

    User findByPasswordResetId(String resetId);

    @Transactional
    @Query("UPDATE User u SET u.sessionId = ?1 WHERE u.id = ?2")
    void updateSessionId(int userId, String sessionId);

    @Transactional
    @Query("SELECT u.sessionId FROM User u WHERE u.id = ?1")
    String findSessionId(int userId);
}


