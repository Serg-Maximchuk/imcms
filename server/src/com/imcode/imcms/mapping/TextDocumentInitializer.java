package com.imcode.imcms.mapping;

import com.imcode.db.Database;
import com.imcode.db.commands.SqlQueryCommand;
import imcode.server.document.DirectDocumentReference;
import imcode.server.document.DocumentDomainObject;
import imcode.server.document.GetterDocumentReference;
import imcode.server.document.textdocument.*;
import imcode.util.LazilyLoadedObject;
import imcode.util.Utility;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TextDocumentInitializer {

    private final static Logger log = Logger.getLogger(TextDocumentInitializer.class);

    private final Collection documentIds;
    private final Database database;
    private final DocumentGetter documentGetter;
    private Map documentsMenuItems;
    private Map documentsImages;
    private Map documentsIncludes;
    private Map documentsTexts;
    private Map documentsTemplateIds;

    static final String SQL_GET_MENU_ITEMS = "SELECT meta_id, menus.menu_id, menu_index, sort_order, to_meta_id, manual_sort_order, tree_sort_index FROM menus,childs WHERE menus.menu_id = childs.menu_id AND meta_id ";

    public TextDocumentInitializer(Database database, DocumentGetter documentGetter, Collection documentIds) {
        this.database = database;
        this.documentGetter = documentGetter;
        this.documentIds = documentIds;
    }

    public void initialize(TextDocumentDomainObject document) {
        Integer documentId = new Integer(document.getId()) ;
        document.setLazilyLoadedMenus(new LazilyLoadedObject(new MenusLoader(documentId)));
        document.setLazilyLoadedTexts(new LazilyLoadedObject(new TextsLoader(documentId)));
        document.setLazilyLoadedImages(new LazilyLoadedObject(new ImagesLoader(documentId)));
        document.setLazilyLoadedIncludes(new LazilyLoadedObject(new IncludesLoader(documentId)));
        document.setLazilyLoadedTemplateIds(new LazilyLoadedObject(new TemplateIdsLoader(documentId)));
    }

    private class MenusLoader implements LazilyLoadedObject.Loader {

        private final Integer documentId;

        MenusLoader(Integer documentId) {
            this.documentId = documentId;
        }

        public LazilyLoadedObject.Copyable load() {
            initDocumentsMenuItems();
            DocumentMenusMap menusMap = (DocumentMenusMap) documentsMenuItems.get(documentId);
            if ( null == menusMap ) {
                menusMap = new DocumentMenusMap();
            }
            return menusMap;
        }

        void initDocumentsMenuItems() {
            if ( null == documentsMenuItems ) {
                documentsMenuItems = new HashMap();
                StringBuffer sql = new StringBuffer(SQL_GET_MENU_ITEMS);
                Integer[] parameters = DocumentInitializer.appendInClause(sql, documentIds);
                final Set destinationDocumentIds = new HashSet();
                final BatchDocumentGetter batchDocumentGetter = new BatchDocumentGetter(destinationDocumentIds, documentGetter);
                database.execute(new SqlQueryCommand(sql.toString(), parameters, new ResultSetHandler() {
                    public Object handle(ResultSet rs) throws SQLException {
                        while ( rs.next() ) {
                            int documentId = rs.getInt(1);
                            int menuId = rs.getInt(2);
                            int menuIndex = rs.getInt(3);
                            int menuSortOrder = rs.getInt(4);
                            Integer destinationDocumentId = new Integer(rs.getInt(5));
                            Integer sortKey = Utility.getInteger(rs.getObject(6));

                            destinationDocumentIds.add(destinationDocumentId);
                            Map documentMenus = (Map) documentsMenuItems.get(new Integer(documentId));
                            if ( null == documentMenus ) {
                                documentMenus = new DocumentMenusMap();
                                documentsMenuItems.put(new Integer(documentId), documentMenus);
                            }

                            MenuDomainObject menu = (MenuDomainObject) documentMenus.get(new Integer(menuIndex));
                            if ( null == menu ) {
                                menu = new MenuDomainObject(menuId, menuSortOrder);
                                documentMenus.put(new Integer(menuIndex), menu);
                            }
                            MenuItemDomainObject menuItem = new MenuItemDomainObject(new GetterDocumentReference(destinationDocumentId.intValue(), batchDocumentGetter), sortKey, new TreeSortKeyDomainObject(rs.getString(7)));
                            menu.addMenuItemUnchecked(menuItem);
                        }
                        return null;
                    }
                }));
            }
        }
    }

    private class IncludesLoader implements LazilyLoadedObject.Loader {

        private final Integer documentId;

        IncludesLoader(Integer documentId) {
            this.documentId = documentId;
        }

        public LazilyLoadedObject.Copyable load() {
            initDocumentsIncludes();
            CopyableHashMap documentIncludesMap = (CopyableHashMap) documentsIncludes.get(documentId);
            if ( null == documentIncludesMap ) {
                documentIncludesMap = new CopyableHashMap();
            }
            return documentIncludesMap;
        }

        private void initDocumentsIncludes() {
            if ( null == documentsIncludes ) {
                documentsIncludes = new HashMap();
                StringBuffer sql = new StringBuffer("SELECT meta_id, include_id, included_meta_id FROM includes WHERE meta_id ");
                Integer[] parameters = DocumentInitializer.appendInClause(sql, documentIds);
                database.execute(new SqlQueryCommand(sql.toString(), parameters, new ResultSetHandler() {
                    public Object handle(ResultSet rs) throws SQLException {
                        while ( rs.next() ) {
                            Integer documentId = new Integer(rs.getInt(1));
                            Integer includeIndex = new Integer(rs.getInt(2));
                            Integer includedDocumentId = new Integer(rs.getInt(3));

                            CopyableHashMap documentIncludesMap = (CopyableHashMap) documentsIncludes.get(documentId);
                            if ( null == documentIncludesMap ) {
                                documentIncludesMap = new CopyableHashMap();
                                documentsIncludes.put(documentId, documentIncludesMap);
                            }
                            documentIncludesMap.put(includeIndex, includedDocumentId);
                        }
                        return null;
                    }
                }));
            }
        }

    }

    private class ImagesLoader implements LazilyLoadedObject.Loader {

        private final Integer documentId;

        ImagesLoader(Integer documentId) {
            this.documentId = documentId;
        }

        public LazilyLoadedObject.Copyable load() {
            initDocumentsImages();
            CopyableHashMap documentImagesMap = (CopyableHashMap) documentsImages.get(documentId);
            if ( null == documentImagesMap ) {
                documentImagesMap = new CopyableHashMap();
            }
            return documentImagesMap;
        }

        private void initDocumentsImages() {
            if ( null == documentsImages ) {
                documentsImages = new HashMap();
                StringBuffer sql = new StringBuffer("SELECT meta_id,iname,image_name,imgurl,"
                                                    + "width,height,border,v_space,h_space,"
                                                    + "target,align,alt_text,low_scr,linkurl,itype "
                                                    + "FROM images WHERE meta_id ");
                Integer[] parameters = DocumentInitializer.appendInClause(sql, documentIds);
                database.execute(new SqlQueryCommand(sql.toString(), parameters, new ResultSetHandler() {
                    public Object handle(ResultSet rs) throws SQLException {
                        while ( rs.next() ) {
                            Integer documentId = new Integer(rs.getInt(1));
                            Map imageMap = (Map) documentsImages.get(documentId);
                            if ( null == imageMap ) {
                                imageMap = new CopyableHashMap();
                                documentsImages.put(documentId, imageMap);
                            }
                            Integer imageIndex = new Integer(rs.getInt(2));
                            ImageDomainObject image = new ImageDomainObject();

                            image.setName(rs.getString(3));
                            String imageSource = rs.getString(4);
                            image.setWidth(rs.getInt(5));
                            image.setHeight(rs.getInt(6));
                            image.setBorder(rs.getInt(7));
                            image.setVerticalSpace(rs.getInt(8));
                            image.setHorizontalSpace(rs.getInt(9));
                            image.setTarget(rs.getString(10));
                            image.setAlign(rs.getString(11));
                            image.setAlternateText(rs.getString(12));
                            image.setLowResolutionUrl(rs.getString(13));
                            image.setLinkUrl(rs.getString(14));
                            int imageType = rs.getInt(15);

                            if ( StringUtils.isNotBlank(imageSource) ) {
                                if ( ImageSource.IMAGE_TYPE_ID__FILE_DOCUMENT == imageType ) {
                                    try {
                                        int fileDocumentId = Integer.parseInt(imageSource);
                                        DocumentDomainObject document = documentGetter.getDocument(new Integer(fileDocumentId));
                                        if ( null != document ) {
                                            image.setSource(new FileDocumentImageSource(new DirectDocumentReference(document)));
                                        }
                                    } catch ( NumberFormatException nfe ) {
                                        log.warn("Non-numeric document-id \"" + imageSource + "\" for image in database.");
                                    } catch ( ClassCastException cce ) {
                                        log.warn("Non-file-document-id \"" + imageSource + "\" for image in database.");
                                    }
                                } else if ( ImageSource.IMAGE_TYPE_ID__IMAGES_PATH_RELATIVE_PATH == imageType ) {
                                    image.setSource(new ImagesPathRelativePathImageSource(imageSource));
                                }
                            }
                            imageMap.put(imageIndex, image);
                        }
                        return null;
                    }
                }));
            }

        }
    }

    private class TextsLoader implements LazilyLoadedObject.Loader {

        private final Integer documentId;

        TextsLoader(Integer documentId) {
            this.documentId = documentId;
        }

        public LazilyLoadedObject.Copyable load() {
            initDocumentsTexts();
            CopyableHashMap documentTexts = (CopyableHashMap) documentsTexts.get(documentId);
            if ( null == documentTexts ) {
                documentTexts = new CopyableHashMap();
            }
            return documentTexts;
        }

        private void initDocumentsTexts() {
            if ( null == documentsTexts ) {
                documentsTexts = new HashMap();
                StringBuffer sql = new StringBuffer("SELECT meta_id, iname, text, itype FROM texts WHERE meta_id ");
                Integer[] parameters = DocumentInitializer.appendInClause(sql, documentIds);
                database.execute(new SqlQueryCommand(sql.toString(), parameters, new ResultSetHandler() {
                    public Object handle(ResultSet rs) throws SQLException {
                        while ( rs.next() ) {
                            Integer documentId = new Integer(rs.getInt(1));
                            Integer textIndex = new Integer(rs.getInt(2));
                            String text = rs.getString(3);
                            int textType = rs.getInt(4);
                            CopyableHashMap documentTextsMap = (CopyableHashMap) documentsTexts.get(documentId);
                            if ( null == documentTextsMap ) {
                                documentTextsMap = new CopyableHashMap();
                                documentsTexts.put(documentId, documentTextsMap);
                            }
                            documentTextsMap.put(textIndex, new TextDomainObject(text, textType));
                        }
                        return null;
                    }
                }));
            }
        }

    }

    private class TemplateIdsLoader implements LazilyLoadedObject.Loader {

        private final Integer documentId;

        TemplateIdsLoader(Integer documentId) {
            this.documentId = documentId;
        }

        public LazilyLoadedObject.Copyable load() {
            initDocumentsTemplateIds();
            TextDocumentDomainObject.TemplateIds templateIds = (TextDocumentDomainObject.TemplateIds) documentsTemplateIds.get(documentId) ;
            if (null == templateIds) {
                templateIds = new TextDocumentDomainObject.TemplateIds();
            }
            return templateIds ;
        }

        private void initDocumentsTemplateIds() {
            if ( null == documentsTemplateIds ) {
                documentsTemplateIds = new HashMap();
                StringBuffer sql = new StringBuffer("SELECT meta_id, template_id, group_id, default_template, default_template_1, default_template_2 FROM text_docs WHERE meta_id ");
                Integer[] parameters = DocumentInitializer.appendInClause(sql, documentIds);
                database.execute(new SqlQueryCommand(sql.toString(), parameters, new ResultSetHandler() {
                    public Object handle(ResultSet rs) throws SQLException {
                        while ( rs.next() ) {
                            Integer documentId = new Integer(rs.getInt(1));
                            TextDocumentDomainObject.TemplateIds templateIds = new TextDocumentDomainObject.TemplateIds();
                            templateIds.setTemplateId(rs.getInt(2));
                            templateIds.setTemplateGroupId(rs.getInt(3));
                            templateIds.setDefaultTemplateId(Utility.getInteger(rs.getObject(4)));
                            Integer defaultTemplateIdForR1 = Utility.getInteger(rs.getObject(5));
                            Integer defaultTemplateIdForR2 = Utility.getInteger(rs.getObject(6));
                            if ( defaultTemplateIdForR1.intValue() == -1 ) {
                                defaultTemplateIdForR1 = null;
                            }
                            if ( defaultTemplateIdForR2.intValue() == -1 ) {
                                defaultTemplateIdForR2 = null;
                            }
                            templateIds.setDefaultTemplateIdForRestricted1(defaultTemplateIdForR1);
                            templateIds.setDefaultTemplateIdForRestricted2(defaultTemplateIdForR2);
                            documentsTemplateIds.put(documentId, templateIds);
                        }
                        return null;
                    }
                }));
            }
        }

    }

}
