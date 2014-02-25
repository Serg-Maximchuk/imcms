package com.imcode.imcms.mapping;

import com.google.common.base.Optional;
import com.imcode.imcms.api.*;
import com.imcode.imcms.mapping.dao.*;
import com.imcode.imcms.mapping.orm.DocVersion;
import com.imcode.imcms.mapping.orm.TextDocMenu;
import com.imcode.imcms.mapping.orm.TextDocMenuItem;
import imcode.server.document.GetterDocumentReference;
import imcode.server.document.textdocument.*;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
// fixme: implment
// fixme: images: TextDocumentUtils.initImagesSources
public class TextDocMapper {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private DocVersionDao versionDao;

    @Inject
    private TextDocTextDao textDao;

    @Inject
    private TextDocImageDao imageDao;

    @Inject
    private TextDocMenuDao menuDao;

    @Inject
    TextDocTemplateNamesDao templateNamesDao;

    @Inject
    private TextDocLoopDao loopDao;

    @Inject
    private DocLanguageDao languageDao;

    //fixme: init to doc mapper
    private DocumentGetter menuItemDocumentGetter;

    // -----------------------------------------------------------------------------------------------------------------
    public List<TextDocumentTextWrapper> getAllTexts(DocVersionRef docVersionRef) {


        throw new NotImplementedException();
    }

    // -----------------------------------------------------------------------------------------------------------------
    public Map<DocumentLanguage, Map<Integer, TextDomainObject>> getTexts(DocVersionRef docVersionRef) {
        throw new NotImplementedException();
    }

    public Map<DocumentLanguage, Optional<TextDomainObject>> getTexts(DocVersionRef docVersionRef, int textNo) {
        throw new NotImplementedException();
    }

    public Map<Integer, TextDomainObject> getTexts(DocRef docRef) {
        throw new NotImplementedException();
    }

    public Optional<TextDomainObject> getText(DocRef docRef, int textNo) {
        throw new NotImplementedException();
    }

    // -----------------------------------------------------------------------------------------------------------------
    public Map<DocumentLanguage, Map<LoopItemRef, TextDomainObject>> getLoopTexts(DocVersionRef docVersionRef) {
        throw new NotImplementedException();
    }

    public Map<DocumentLanguage, Optional<TextDomainObject>> getLoopTexts(DocVersionRef docVersionRef, LoopItemRef loopItemRef) {
        throw new NotImplementedException();
    }

    public Map<LoopItemRef, TextDomainObject> getLoopTexts(DocRef docRef) {
        throw new NotImplementedException();
    }

    public Optional<TextDomainObject> getLoopText(DocRef docRef, LoopItemRef loopItemRef) {
        throw new NotImplementedException();
    }

    // -----------------------------------------------------------------------------------------------------------------
    public List<TextDocumentImageWrapper> getAllImages(DocVersionRef docVersionRef) {
        throw new NotImplementedException();
    }

    // -----------------------------------------------------------------------------------------------------------------
    public Map<DocumentLanguage, Map<Integer, ImageDomainObject>> getImages(DocVersionRef docVersionRef) {
        throw new NotImplementedException();
    }

    public Map<DocumentLanguage, Optional<ImageDomainObject>> getImages(DocVersionRef docVersionRef, int textNo) {
        throw new NotImplementedException();
    }

    public Map<Integer, ImageDomainObject> getImages(DocRef docRef) {
        throw new NotImplementedException();
    }

    public Optional<ImageDomainObject> getImage(DocRef docRef, int textNo) {
        throw new NotImplementedException();
    }

    // -----------------------------------------------------------------------------------------------------------------
    public Map<DocumentLanguage, Map<LoopItemRef, ImageDomainObject>> getLoopImages(DocVersionRef docVersionRef) {
        throw new NotImplementedException();
    }

    public Map<DocumentLanguage, Optional<ImageDomainObject>> getLoopImages(DocVersionRef docVersionRef, LoopItemRef loopItemRef) {
        throw new NotImplementedException();
    }

    public Map<LoopItemRef, ImageDomainObject> getLoopImages(DocRef docRef) {
        throw new NotImplementedException();
    }

    public Optional<ImageDomainObject> getLoopImage(DocRef docRef, LoopItemRef loopItemRef) {
        throw new NotImplementedException();
    }

    // -----------------------------------------------------------------------------------------------------------------
    public Map<Integer, Loop> getLoops(DocVersionRef docVersionRef) {
        throw new NotImplementedException();
    }

    public Optional<Loop> getLoop(DocVersionRef docVersionRef, int no) {
        throw new NotImplementedException();
    }


    // -----------------------------------------------------------------------------------------------------------------
    public Map<Integer, MenuDomainObject> getMenus(DocVersionRef docVersionRef) {
        DocVersion docVersion = versionDao.findByDocIdAndNo(docVersionRef.getDocId(), docVersionRef.getDocVersionNo());
        List<TextDocMenu> textDocMenus = menuDao.getByDocVersion(docVersion);
        Map<Integer, MenuDomainObject> menus = new HashMap<>();

        for (TextDocMenu textDocMenu : textDocMenus) {
            menus.put(textDocMenu.getNo(), initMenuItems(OrmToApi.toApi(textDocMenu)));
        }

        return menus;
    }

    private MenuDomainObject initMenuItems(MenuDomainObject menu) {
        for (Map.Entry<Integer, MenuItemDomainObject> entry : menu.getItemsMap().entrySet()) {
            Integer referencedDocumentId = entry.getKey();
            MenuItemDomainObject menuItem = entry.getValue();
            GetterDocumentReference gtr = new GetterDocumentReference(referencedDocumentId, menuItemDocumentGetter);

            menuItem.setDocumentReference(gtr);
        }

        return menu;
    }

    public TextDocumentDomainObject.TemplateNames getTemplateNames(int docId) {
        return OrmToApi.toApi(templateNamesDao.findOne(docId));
    }

    // get menu
    // get include
}
