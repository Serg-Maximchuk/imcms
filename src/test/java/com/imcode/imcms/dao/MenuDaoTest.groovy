package com.imcode.imcms.dao

import org.dbunit.dataset.xml.FlatXmlDataSet
import static org.testng.Assert.*
//todo: Test named queries
public class MenuDaoTest extends DaoTest {
	
	MenuDao dao;
	
	@BeforeClass void init() {
		dao = Context.getBean("menuDao")
	}		
		
	@Override
	def getDataSetFileName() {
		"dbunit-menus-data.xml"
	}
	

    @Test void getNonExistingMenu() {
        def menu = dao.getMenu(10001, 1)
        
        assertNull(menu)
    }
    
    
    @Test void getMenus() {
        def menus = dao.getMenus(1001)
        
        assertTrue(menus.size() == 3)
    }    
    
    
    @Test void insertMenu() {
        def menus = dao.getMenus(1001)
        
        def menu = new MenuDomainObject()
        menu.setMetaId(1001)
        menu.setIndex(4)
        menu.setSortOrder(MenuDomainObject.MENU_SORT_ORDER__BY_HEADLINE)
              
        def mi = new MenuItemDomainObject()
        mi.sortKey = 2
        mi.treeSortKey = new TreeSortKeyDomainObject("3") 
                
        menu.itemsMap.put(1001, mi)        
        menus << menu
        
        def menusMap = new HashMap()
        
        menus.each {
        	menusMap.put(it.index, it)
        }
        
        dao.saveDocumentMenus(1001, menusMap)
                
        menus = dao.getMenus(1001)
                
        assertTrue(menus.size() == 4)        
    }   
}