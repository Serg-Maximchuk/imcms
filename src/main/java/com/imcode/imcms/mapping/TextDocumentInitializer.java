package com.imcode.imcms.mapping;

import com.imcode.imcms.dao.*;
import imcode.server.document.GetterDocumentReference;
import imcode.server.document.textdocument.ImageDomainObject;
import imcode.server.document.textdocument.MenuDomainObject;
import imcode.server.document.textdocument.MenuItemDomainObject;
import imcode.server.document.textdocument.TextDocumentDomainObject;
import imcode.server.document.textdocument.TextDomainObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.imcode.imcms.api.ContentLoop;
import com.imcode.imcms.dao.TextDocDao;
import com.imcode.imcms.mapping.orm.Include;
import com.imcode.imcms.mapping.orm.TemplateNames;
 
public class TextDocumentInitializer {

    private final static Logger LOG = Logger.getLogger(TextDocumentInitializer.class);

    private DocumentGetter documentGetter;

    private MetaDao metaDao;
    
    private TextDocDao textDocDao;

    /**
	 * Initializes text document.
     */
    public void initialize(TextDocumentDomainObject document) {
        initContentLoops(document);
        initTexts(document);
        initImages(document);
        initMenus(document);
        initIncludes(document);
        initTemplateNames(document);
    }
    
    public void initTexts(TextDocumentDomainObject document) {
    	Collection<TextDomainObject> texts = textDocDao.getTexts(document.getRef(), document.getLanguage());

    	for (TextDomainObject text: texts) {
            Integer no = text.getNo();

            document.setText(no, text);
    	}
    }
    
    
    public void initIncludes(TextDocumentDomainObject document) {
    	Collection<Include> includes = metaDao.getIncludes(document.getMeta().getId());
    	
    	Map<Integer, Integer> includesMap = new HashMap<Integer, Integer>();
    	
    	for (Include include: includes) {
    		includesMap.put(include.getIndex(), include.getIncludedDocumentId());
    	}
    	
    	document.setIncludesMap(includesMap);
    }
    
    
    public void initTemplateNames(TextDocumentDomainObject document) {
    	TemplateNames templateNames = metaDao.getTemplateNames(document.getMeta().getId());
    	
    	if (templateNames == null) {
    		templateNames = new TemplateNames();
    	}
    	
    	document.setTemplateNames(templateNames);
    }    
    
    
    public void initImages(TextDocumentDomainObject document) {
    	Collection<ImageDomainObject> images = textDocDao.getImages(document.getRef(), document.getLanguage());
    	
    	for (ImageDomainObject image: images) {
    		document.setImage(image.getNo(), image);
    	}
    }


    public void initMenus(TextDocumentDomainObject document) {
    	Collection<MenuDomainObject> menus = textDocDao.getMenus(document.getRef());
    	Map<Integer, MenuDomainObject> menusMap = new HashMap<Integer, MenuDomainObject>();

    	for (MenuDomainObject menu: menus) {
    		initMenuItems(menu, documentGetter);
	    	
	    	menusMap.put(menu.getNo(), menu);
    	}
    	
    	document.setMenus(menusMap);
    }   
    
    private void initMenuItems(MenuDomainObject menu, DocumentGetter documentGetter) {
    	
    	for (Map.Entry<Integer, MenuItemDomainObject> entry: menu.getItemsMap().entrySet()) {
    		Integer referencedDocumentId = entry.getKey();
    		MenuItemDomainObject menuItem = entry.getValue();
    		GetterDocumentReference gtr = new GetterDocumentReference(referencedDocumentId, documentGetter);
    		
    		menuItem.setDocumentReference(gtr);
    	}
    }


    /**
     * @throws IllegalStateException if a content loop is empty i.e. does not have a contents. 
     */
	public void initContentLoops(TextDocumentDomainObject document) {
		List<ContentLoop> loops = textDocDao.getLoops(document.getRef());
		Map<Integer, ContentLoop> loopsMap = new HashMap<Integer, ContentLoop>();
		
		for (ContentLoop loop: loops) {
			loopsMap.put(loop.getNo(), loop);
		}
		
		document.setContentLoops(loopsMap);
	}

	public MetaDao getMetaDao() {
		return metaDao;
	}

	public void setMetaDao(MetaDao metaDao) {
		this.metaDao = metaDao;
	}

	public TextDocDao getTextDocDao() {
		return textDocDao;
	}

	public void setTextDocDao(TextDocDao textDocDao) {
		this.textDocDao = textDocDao;
	}

	public DocumentGetter getDocumentGetter() {
		return documentGetter;
	}

	public void setDocumentGetter(DocumentGetter documentGetter) {
		this.documentGetter = documentGetter;
	}	
}