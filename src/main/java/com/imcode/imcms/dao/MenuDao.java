package com.imcode.imcms.dao;

import imcode.server.document.textdocument.MenuDomainObject;
import imcode.server.document.textdocument.MenuItemDomainObject;
import imcode.server.document.textdocument.TreeSortKeyDomainObject;

import java.util.List;
import java.util.Map;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.Transactional;

public class MenuDao extends HibernateTemplate {

	/*
	@Transactional
	public MenuDomainObject getMenu(long id) {
		return (MenuDomainObject)get(MenuDomainObject.class, id);
	}
	*/
	
	/*
	@Transactional
	public MenuDomainObject getMenu(int metaId, int index) {
		String hql = "SELECT m FROM Menu m  WHERE m.metaId = :metaId AND m.index = :index";
		
		return (MenuDomainObject)getSession().createQuery(hql)
			.setParameter("metaId", metaId)
			.setParameter("index", index)
			.uniqueResult();
	}
	*/

    /*
	@Transactional
	public MenuDomainObject getMenu(Integer documentId, Integer index) {
		String hql = "SELECT m FROM Menu m WHERE m.metaId = :metaId AND m.index = :index";
		
		MenuDomainObject menu = (MenuDomainObject)getSession().createQuery(hql)
			.setParameter("metaId", documentId)
			.setParameter("index", index)
			.uniqueResult();
		
		if (menu != null) {
			initMenu(menu);
		}
					
		return menu;
	}
	*/


	@Transactional
	public List<MenuDomainObject> getMenus(Integer docId, Integer docVersionNo) {
		String hql = "SELECT m FROM Menu m  WHERE m.metaId = :docId AND m.docVersionNo = :docVersionNo";

		return (List<MenuDomainObject>)findByNamedParam(hql,
                new String[] {"docId", "docVersionNo"},
                new Object[] {docId, docVersionNo});
	}


    @Transactional
	public void saveMenu(MenuDomainObject menu) {
        for (Map.Entry<Integer, MenuItemDomainObject> itemEntry: menu.getItemsMap().entrySet()) {
            MenuItemDomainObject item = itemEntry.getValue();
            item.setTreeSortIndex(item.getTreeSortKey().toString());
        }
        
	    saveOrUpdate(menu);			
	}
	
	
	@Transactional
	public Map<Integer, MenuDomainObject> saveDocumentMenus(Integer docId, Integer docVersionNo, Map<Integer, MenuDomainObject> menusMap) {
		for (Map.Entry<Integer, MenuDomainObject> entry: menusMap.entrySet()) {
			MenuDomainObject menu = entry.getValue();
			
			menu.setDocId(docId);
            menu.setDocVersionNo(docVersionNo);
			menu.setNo(entry.getKey());
			
			for (Map.Entry<Integer, MenuItemDomainObject> itemEntry: menu.getItemsMap().entrySet()) {
				MenuItemDomainObject item = itemEntry.getValue();
				item.setTreeSortIndex(item.getTreeSortKey().toString());
			}
			
			saveOrUpdate(menu);			
		}
		
		return menusMap;
	}

	@Transactional	
	public void deleteMenu(MenuDomainObject menu) {
		delete(menu);
	}

}