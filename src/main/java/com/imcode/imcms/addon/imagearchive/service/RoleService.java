package com.imcode.imcms.addon.imagearchive.service;

import com.imcode.imcms.addon.imagearchive.command.SaveRoleCategoriesCommand;
import com.imcode.imcms.addon.imagearchive.entity.Categories;
import com.imcode.imcms.addon.imagearchive.entity.CategoryRoles;
import com.imcode.imcms.addon.imagearchive.entity.Exif;
import com.imcode.imcms.addon.imagearchive.entity.Roles;
import com.imcode.imcms.addon.imagearchive.util.Utils;
import com.imcode.imcms.api.ContentManagementSystem;
import com.imcode.imcms.api.User;
import imcode.server.user.RolePermissionDomainObject;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional
public class RoleService {
    @Autowired
    private SessionFactory factory;

    @Autowired
    private Facade facade;
    
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public Roles findRoleById(int id) {

        return (Roles) factory.getCurrentSession().get(Roles.class, id);
    }

    @SuppressWarnings("unchecked")
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public List<Roles> findRoles() {

        return factory.getCurrentSession()
                .createQuery("FROM Roles r ORDER BY r.roleName")
                .list();
    }

    @SuppressWarnings("unchecked")
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public List<Categories> findRoleCategories(int roleId) {

        return factory.getCurrentSession()
                .createQuery("SELECT c FROM CategoryRoles cr JOIN cr.category c WHERE " +
                "cr.roleId = :roleId AND c.type.name = 'Images' ORDER BY c.name")
                .setInteger("roleId", roleId)
                .list();
    }

    @SuppressWarnings("unchecked")
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public List<Categories> findFreeCategories(int roleId) {

        return factory.getCurrentSession()
                .createQuery("SELECT c FROM Categories c WHERE " +
                "NOT EXISTS (FROM CategoryRoles cr WHERE cr.roleId = :roleId AND cr.categoryId = c.id) " +
                "AND c.type.name = 'Images' ORDER BY c.name")
                .setInteger("roleId", roleId)
                .list();
    }

    @SuppressWarnings("unchecked")
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public List<Categories> findAllCategories() {

        return factory.getCurrentSession()
                .createQuery("SELECT c FROM Categories c WHERE " +
                " c.type.name = :typeName ORDER BY c.name")
                .setString("typeName", "Images")
                .list();
    }
    
    public void assignCategoryRoles(Roles role, List<SaveRoleCategoriesCommand.CategoryRight> categoryRights) {

        Session session = factory.getCurrentSession();

        StringBuilder deleteBuilder = new StringBuilder(
                "DELETE FROM CategoryRoles cr WHERE cr.roleId = :roleId ");


        Query deleteQuery = session.createQuery(deleteBuilder.toString())
                .setInteger("roleId", role.getId());
        deleteQuery.executeUpdate();

        if(categoryRights != null) {
            for(SaveRoleCategoriesCommand.CategoryRight categoryRight: categoryRights) {
                CategoryRoles cr = new CategoryRoles(categoryRight.getCategoryId(), role.getId(), categoryRight.isCanUse(), categoryRight.isCanEditOrAdd());
                    session.persist(cr);
            }
        }
        session.flush();
    }

    @SuppressWarnings("unchecked")
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public List<Categories> findCategories(User user, ContentManagementSystem cms, RolePermissionDomainObject... permissions) {

        Session session = factory.getCurrentSession();
        Set<Integer> roleIds;

        if (user.isSuperAdmin() || Utils.isImageAdmin(user, cms)) {
            return session.createQuery(
                    "SELECT DISTINCT c.id AS id, c.name AS name FROM Categories c " +
                    "WHERE c.type.name = 'Images' ORDER BY c.name")
                    .setResultTransformer(Transformers.aliasToBean(Categories.class))
                    .list();
            
        } else if (user.isDefaultUser()) {
            roleIds = new HashSet<Integer>();
            roleIds.add(Roles.USERS_ID);

        } else {
            roleIds = facade.getUserService().getRoleIdsWithPermission(user, null, permissions);
            if (roleIds.isEmpty()) {
                return Collections.EMPTY_LIST;
            }

        }

        return session.createQuery(
                "SELECT DISTINCT c.id AS id, c.name AS name FROM CategoryRoles cr INNER JOIN cr.category c " +
                "WHERE cr.roleId IN (:roleIds) AND c.type.name = 'Images' ORDER BY c.name")
                .setParameterList("roleIds", roleIds)
                .setResultTransformer(Transformers.aliasToBean(Categories.class))
                .list();

    }

    @SuppressWarnings("unchecked")
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public List<Integer> findCategoryIds(User user, RolePermissionDomainObject... permissions) {

        Set<Integer> roleIds = facade.getUserService().getRoleIdsWithPermission(user, null, permissions);
        if (roleIds.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        return factory.getCurrentSession()
                .createQuery(
                "SELECT cr.categoryId FROM CategoryRoles cr WHERE cr.roleId IN (:roleIds) AND cr.category.type.name = 'Images' ")
                .setParameterList("roleIds", roleIds)
                .list();
    }

    /* Checks whether a user has the provided permission(s), be it use/edit/any for the given category */
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public boolean hasAccessToCategory(User user, int categoryId, ContentManagementSystem cms, RolePermissionDomainObject... permissions) {

        Set<Integer> roleIds;

        if (user.isSuperAdmin() || Utils.isImageAdmin(user, cms)) {
            return true;

        } else if (user.isDefaultUser()) {
            roleIds = new HashSet<Integer>(1);
            roleIds.add(Roles.USERS_ID);

        } else {
            List<Integer> categoryIds = new ArrayList<Integer>();
            categoryIds.add(categoryId);
            roleIds = facade.getUserService().getRoleIdsWithPermission(user, categoryIds, permissions);
            if (roleIds.isEmpty()) {
                return false;
            }
            
        }

        long count = (Long) factory.getCurrentSession()
                .createQuery(
                "SELECT count(cr.categoryId) FROM CategoryRoles cr " +
                "WHERE cr.categoryId = :categoryId AND cr.roleId IN (:roleIds) AND cr.category.type.name = 'Images'")
                .setInteger("categoryId", categoryId)
                .setParameterList("roleIds", roleIds)
                .uniqueResult();

        return count != 0L;
    }

    @SuppressWarnings("unchecked")
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public List<String> findArtists(User user, ContentManagementSystem cms) {
        Session session = factory.getCurrentSession();
        if(user != null && (user.isSuperAdmin() || Utils.isImageAdmin(user, cms))) {
            return session.createCriteria(Exif.class, "exif")
                    .add(Restrictions.eq("exif.type", Exif.TYPE_CHANGED))
                    .add(Restrictions.ne("exif.artist", ""))
                    .setProjection(Projections.distinct(Projections.property("exif.artist")))
                    .list();
        }

        Set<Integer> roleIds;
        if (user == null || user.isDefaultUser()) {
            roleIds = new HashSet<Integer>(1);
            roleIds.add(Roles.USERS_ID);
        } else {
            roleIds = facade.getUserService().getRoleIdsWithPermission(user, null, Roles.ALL_PERMISSIONS);
            if (roleIds.isEmpty()) {
                return Collections.EMPTY_LIST;
            }
        }

        return session
                .getNamedQuery("artistsByRoleIds")
                .setParameterList("roleIds", roleIds)
                .setShort("changedType", Exif.TYPE_CHANGED)
                .setInteger("userId", user != null ? user.getId() : -1)
                .list();
    }
}
