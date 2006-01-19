package com.imcode.imcms.mapping;

import com.imcode.db.commands.InsertIntoTableDatabaseCommand;
import com.imcode.db.commands.SqlUpdateDatabaseCommand;
import com.imcode.imcms.api.Document;
import com.imcode.imcms.db.DatabaseUtils;
import imcode.server.document.*;
import imcode.server.document.textdocument.NoPermissionToAddDocumentToMenuException;
import imcode.server.document.textdocument.TextDocumentDomainObject;
import imcode.server.user.RoleId;
import imcode.server.user.UserDomainObject;
import imcode.util.Utility;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;

import java.util.*;

class DocumentSaver {

    private final DefaultDocumentMapper documentMapper ;

    private static final int META_HEADLINE_MAX_LENGTH = 255;
    private static final int META_TEXT_MAX_LENGTH = 1000;
    public static final String SQL_DELETE_ROLE_DOCUMENT_PERMISSION_SET_ID = "DELETE FROM roles_rights WHERE role_id = ? AND meta_id = ?";
    public static final String SQL_SET_ROLE_DOCUMENT_PERMISSION_SET_ID = "INSERT INTO roles_rights (role_id, meta_id, set_id) VALUES(?,?,?)";

    DocumentSaver(DefaultDocumentMapper documentMapper) {
        this.documentMapper = documentMapper;
    }

    void saveDocument(DocumentDomainObject document, DocumentDomainObject oldDocument,
                      final UserDomainObject user) throws NoPermissionToEditDocumentException, NoPermissionToAddDocumentToMenuException {
        if (!user.canEdit(oldDocument)) {
            throw new NoPermissionToEditDocumentException("No permission to edit document "+oldDocument.getId()) ;
        }

        checkDocumentForSave(document, oldDocument, user);

        try {
            Date lastModifiedDatetime = Utility.truncateDateToMinutePrecision(document.getActualModifiedDatetime());
            Date modifiedDatetime = Utility.truncateDateToMinutePrecision(document.getModifiedDatetime());
            boolean modifiedDatetimeUnchanged = lastModifiedDatetime.equals(modifiedDatetime);
            if (modifiedDatetimeUnchanged) {
                document.setModifiedDatetime(documentMapper.getClock().getCurrentDate());
            }

            sqlUpdateMeta(document);

            updateDocumentSectionsCategoriesKeywords(document);

            if (user.canEditPermissionsFor(oldDocument)) {
                updateDocumentRolePermissions(document, user, oldDocument);

                documentMapper.getDocumentPermissionSetMapper().saveRestrictedDocumentPermissionSets(document, user, oldDocument);
            }

            document.accept(new DocumentSavingVisitor(oldDocument, documentMapper.getDatabase(), documentMapper.getImcmsServices()));
        } finally {
            documentMapper.invalidateDocument(document);
        }
    }

    void checkDocumentsAddedWithoutPermission(TextDocumentDomainObject textDocument,
                                              TextDocumentDomainObject oldTextDocument,
                                              final UserDomainObject user) throws NoPermissionToAddDocumentToMenuException
    {
        Collection documentsAddedWithoutPermission = getDocumentsAddedWithoutPermission(textDocument, oldTextDocument, user);
        boolean documentsWereAddedWithoutPermission = !documentsAddedWithoutPermission.isEmpty();
        if (documentsWereAddedWithoutPermission ) {
            Collection documentIds = CollectionUtils.collect(documentsAddedWithoutPermission, new Transformer() {
                public Object transform(Object object) {
                    DocumentDomainObject document = (DocumentDomainObject) object;
                    return ""+document.getId() ;
                }
            });
            throw new NoPermissionToAddDocumentToMenuException("User is not allowed to add documents "+documentIds +" to document "+textDocument.getId()) ;
        }
    }

    private void sqlUpdateMeta(DocumentDomainObject document) {
        String headline = document.getHeadline();
        String text = document.getMenuText();

        StringBuffer sqlStr = new StringBuffer("update meta set ");

        ArrayList sqlUpdateColumns = new ArrayList();
        ArrayList sqlUpdateValues = new ArrayList();

        makeDateSqlUpdateClause("publication_start_datetime", document.getPublicationStartDatetime(), sqlUpdateColumns, sqlUpdateValues);
        makeDateSqlUpdateClause("publication_end_datetime", document.getPublicationEndDatetime(), sqlUpdateColumns, sqlUpdateValues);
        makeDateSqlUpdateClause("archived_datetime", document.getArchivedDatetime(), sqlUpdateColumns, sqlUpdateValues);
        makeDateSqlUpdateClause("date_created", document.getCreatedDatetime(), sqlUpdateColumns, sqlUpdateValues);
        String headlineThatFitsInDB = headline.substring(0, Math.min(headline.length(), META_HEADLINE_MAX_LENGTH - 1));
        makeStringSqlUpdateClause("meta_headline", headlineThatFitsInDB, sqlUpdateColumns, sqlUpdateValues);
        makeStringSqlUpdateClause("meta_image", document.getMenuImage(), sqlUpdateColumns, sqlUpdateValues);
        makeDateSqlUpdateClause("date_modified", document.getModifiedDatetime(), sqlUpdateColumns, sqlUpdateValues);
        makeStringSqlUpdateClause("target", document.getTarget(), sqlUpdateColumns, sqlUpdateValues);
        String textThatFitsInDB = text.substring(0, Math.min(text.length(), META_TEXT_MAX_LENGTH - 1));
        makeStringSqlUpdateClause("meta_text", textThatFitsInDB, sqlUpdateColumns, sqlUpdateValues);
        makeStringSqlUpdateClause("lang_prefix", document.getLanguageIso639_2(), sqlUpdateColumns, sqlUpdateValues);
        makeBooleanSqlUpdateClause("disable_search", document.isSearchDisabled(), sqlUpdateColumns, sqlUpdateValues);
        makeBooleanSqlUpdateClause("shared", document.isLinkableByOtherUsers(), sqlUpdateColumns, sqlUpdateValues);
        makeBooleanSqlUpdateClause("show_meta", document.isVisibleInMenusForUnauthorizedUsers(), sqlUpdateColumns, sqlUpdateValues);
        makeBooleanSqlUpdateClause("permissions", document.isRestrictedOneMorePrivilegedThanRestrictedTwo(), sqlUpdateColumns, sqlUpdateValues);
        makeIntSqlUpdateClause("publisher_id", document.getPublisherId(), sqlUpdateColumns,
                                                                                 sqlUpdateValues);
        makeIntSqlUpdateClause("owner_id", new Integer(document.getCreatorId()), sqlUpdateColumns,
                                   sqlUpdateValues);
        Document.PublicationStatus publicationStatus = document.getPublicationStatus();
        int publicationStatusInt = convertPublicationStatusToInt(publicationStatus);
        makeIntSqlUpdateClause("status", new Integer(publicationStatusInt), sqlUpdateColumns, sqlUpdateValues);

        sqlStr.append(StringUtils.join(sqlUpdateColumns.iterator(), ","));
        sqlStr.append(" where meta_id = ?");
        sqlUpdateValues.add("" + document.getId());
        String[] params = (String[]) sqlUpdateValues.toArray(new String[sqlUpdateValues.size()]);
        DatabaseUtils.executeUpdate(documentMapper.getDatabase(), sqlStr.toString(), params);
    }

    static int convertPublicationStatusToInt(Document.PublicationStatus publicationStatus) {
        int publicationStatusInt = Document.STATUS_NEW;
        if ( Document.PublicationStatus.APPROVED.equals(publicationStatus) ) {
            publicationStatusInt = Document.STATUS_PUBLICATION_APPROVED ;
        } else if ( Document.PublicationStatus.DISAPPROVED.equals(publicationStatus) ) {
            publicationStatusInt = Document.STATUS_PUBLICATION_DISAPPROVED ;
        }
        return publicationStatusInt;
    }

    private void updateDocumentSectionsCategoriesKeywords(DocumentDomainObject document) {
        updateDocumentSections(document.getId(), document.getSectionIds());

        documentMapper.getCategoryMapper().updateDocumentCategories(document);

        updateDocumentKeywords(document);
    }

    void saveNewDocument(UserDomainObject user,
                         DocumentDomainObject document) throws NoPermissionToAddDocumentToMenuException {
        if (!user.canEdit(document)) {
            return; // TODO: More specific check needed. Throw exception ?
        }

        checkDocumentForSave(document, null, user);

        documentMapper.setCreatedAndModifiedDatetimes(document, new Date());

        int newMetaId = sqlInsertIntoMeta(document);

        if (!user.isSuperAdminOrHasFullPermissionOn(document)) {
            document.getPermissionSets().setRestricted1(document.getPermissionSetsForNewDocuments().getRestricted1());
            document.getPermissionSets().setRestricted2(document.getPermissionSetsForNewDocuments().getRestricted2());
        }

        document.setId(newMetaId);

        updateDocumentSectionsCategoriesKeywords(document);

        updateDocumentRolePermissions(document, user, null);

        documentMapper.getDocumentPermissionSetMapper().saveRestrictedDocumentPermissionSets(document, user, null);

        document.accept(new DocumentCreatingVisitor(documentMapper.getDatabase(), documentMapper.getImcmsServices()));

        documentMapper.invalidateDocument(document);
    }

    private void checkDocumentForSave(DocumentDomainObject document,
                                      DocumentDomainObject oldDocument, UserDomainObject user
    ) throws NoPermissionToAddDocumentToMenuException {
        if (document instanceof TextDocumentDomainObject) {
            checkDocumentsAddedWithoutPermission((TextDocumentDomainObject)document, (TextDocumentDomainObject) oldDocument, user);
        }

        documentMapper.getCategoryMapper().checkMaxDocumentCategoriesOfType(document);
    }

    void updateDocumentRolePermissions(DocumentDomainObject document, UserDomainObject user,
                                       DocumentDomainObject oldDocument) {
        RoleIdToDocumentPermissionSetTypeMappings mappings = new RoleIdToDocumentPermissionSetTypeMappings();

        if (null != oldDocument) {
            RoleIdToDocumentPermissionSetTypeMappings.Mapping[] oldDocumentMappings = oldDocument.getRoleIdsMappedToDocumentPermissionSetTypes().getMappings();
            for ( int i = 0; i < oldDocumentMappings.length; i++ ) {
                RoleIdToDocumentPermissionSetTypeMappings.Mapping mapping = oldDocumentMappings[i];
                mappings.setPermissionSetTypeForRole(mapping.getRoleId(), DocumentPermissionSetTypeDomainObject.NONE);
            }
        }

        RoleIdToDocumentPermissionSetTypeMappings.Mapping[] documentMappings = document.getRoleIdsMappedToDocumentPermissionSetTypes().getMappings() ;
        for ( int i = 0; i < documentMappings.length; i++ ) {
            RoleIdToDocumentPermissionSetTypeMappings.Mapping mapping = documentMappings[i];
            mappings.setPermissionSetTypeForRole(mapping.getRoleId(), mapping.getDocumentPermissionSetType());
        }

        RoleIdToDocumentPermissionSetTypeMappings.Mapping[] mappingsArray = mappings.getMappings();
        for ( int i = 0; i < mappingsArray.length; i++ ) {
            RoleIdToDocumentPermissionSetTypeMappings.Mapping mapping = mappingsArray[i];
            RoleId roleId = mapping.getRoleId();
            DocumentPermissionSetTypeDomainObject documentPermissionSetType = mapping.getDocumentPermissionSetType();

            if (null == oldDocument
                || user.canSetDocumentPermissionSetTypeForRoleIdOnDocument(documentPermissionSetType, roleId, oldDocument)) {
                String[] params1 = new String[]{"" + roleId,
                                                "" + document.getId()};
                DatabaseUtils.executeUpdate(documentMapper.getDatabase(), SQL_DELETE_ROLE_DOCUMENT_PERMISSION_SET_ID, params1);
                if ( !DocumentPermissionSetTypeDomainObject.NONE.equals(documentPermissionSetType) ) {
                    String[] params = new String[]{
                        "" + roleId.intValue(), "" + document.getId(), "" + documentPermissionSetType };
                    DatabaseUtils.executeUpdate(documentMapper.getDatabase(), SQL_SET_ROLE_DOCUMENT_PERMISSION_SET_ID, params);
                }
            }
        }
    }

    static void makeBooleanSqlUpdateClause(String columnName, boolean bool, List sqlUpdateColumns,
                                           List sqlUpdateValues) {
        sqlUpdateColumns.add(columnName + " = ?");
        sqlUpdateValues.add(bool ? "1" : "0");
    }

    static void makeDateSqlUpdateClause(String columnName, Date date, List sqlUpdateColumns,
                                        List sqlUpdateValues) {
        makeStringSqlUpdateClause(columnName, Utility.makeSqlStringFromDate(date), sqlUpdateColumns, sqlUpdateValues);
    }

    static void makeIntSqlUpdateClause(String columnName, Integer integer, ArrayList sqlUpdateColumns,
                                       ArrayList sqlUpdateValues) {
        if (null != integer) {
            sqlUpdateColumns.add(columnName + " = ?");
            sqlUpdateValues.add("" + integer);
        } else {
            sqlUpdateColumns.add(columnName + " = NULL");
        }
    }

    static void makeStringSqlUpdateClause(String columnName, String value, List sqlUpdateColumns,
                                          List sqlUpdateValues) {
        if (null != value) {
            sqlUpdateColumns.add(columnName + " = ?");
            sqlUpdateValues.add(value);
        } else {
            sqlUpdateColumns.add(columnName + " = NULL");
        }
    }

    private int sqlInsertIntoMeta(DocumentDomainObject document) {

        final Number documentId = (Number) documentMapper.getDatabase().execute(new InsertIntoTableDatabaseCommand("meta", new String[][]{
            { "doc_type", document.getDocumentTypeId() + ""},
            { "meta_headline", document.getHeadline()},
            { "meta_text", document.getMenuText()},
            { "meta_image", document.getMenuImage()},
            { "owner_id", document.getCreatorId() + ""},
            { "permissions", makeSqlStringFromBoolean(document.isRestrictedOneMorePrivilegedThanRestrictedTwo())},
            { "shared", makeSqlStringFromBoolean(document.isLinkableByOtherUsers())},
            { "show_meta", makeSqlStringFromBoolean(document.isVisibleInMenusForUnauthorizedUsers())},
            { "lang_prefix", document.getLanguageIso639_2()},
            { "date_created", Utility.makeSqlStringFromDate(document.getCreatedDatetime()) },
            { "date_modified", Utility.makeSqlStringFromDate(document.getModifiedDatetime())},
            { "disable_search", makeSqlStringFromBoolean(document.isSearchDisabled())},
            { "target", document.getTarget()},
            { "activate", "1"},
            { "archived_datetime", Utility.makeSqlStringFromDate(document.getArchivedDatetime())},
            { "publisher_id", null != document.getPublisherId() ? document.getPublisherId() + "" : null},
            { "status", "" + document.getPublicationStatus()},
            { "publication_start_datetime", Utility.makeSqlStringFromDate(document.getPublicationStartDatetime())},
            { "publication_end_datetime", Utility.makeSqlStringFromDate(document.getPublicationEndDatetime())}
        }));
        return documentId.intValue();
    }

    private String makeSqlStringFromBoolean(final boolean bool) {
        return bool ? "1" : "0";
    }

    Set getDocumentsAddedWithoutPermission(TextDocumentDomainObject textDocument,
                                           TextDocumentDomainObject oldTextDocument,
                                           final UserDomainObject user) {
        Set documentIdsAdded = getDocumentIdsAdded(textDocument, oldTextDocument);
        List documents = documentMapper.getDocuments(documentIdsAdded);
        Collection documentsAddedWithoutPermission = CollectionUtils.select(documents, new Predicate() {
            public boolean evaluate(Object object) {
                return !user.canAddDocumentToAnyMenu((DocumentDomainObject) object) ;
            }
        }) ;
        return new HashSet(documentsAddedWithoutPermission);
    }

    private Set getDocumentIdsAdded(TextDocumentDomainObject textDocument, TextDocumentDomainObject oldTextDocument
    ) {
        Set documentIdsAdded;
        if (null != oldTextDocument) {
            documentIdsAdded = getChildDocumentIdsDifference(textDocument, oldTextDocument);
        } else {
            documentIdsAdded = textDocument.getChildDocumentIds() ;
        }
        return documentIdsAdded;
    }

    private Set getChildDocumentIdsDifference(TextDocumentDomainObject minuend, TextDocumentDomainObject subtrahend) {
        Set minuendChildDocumentIds = minuend.getChildDocumentIds() ;
        Set subtrahendChildDocumentIds = subtrahend.getChildDocumentIds() ;
        Set result = new HashSet(minuendChildDocumentIds) ;
        result.removeAll(subtrahendChildDocumentIds) ;
        return result ;
    }

    void updateDocumentKeywords(DocumentDomainObject document) {
        int meta_id = document.getId();
        Set keywords = document.getKeywords();
        Set allKeywords = new HashSet(Arrays.asList(documentMapper.getAllKeywords()));
        deleteKeywordsFromDocument(meta_id);
        for ( Iterator iterator = keywords.iterator(); iterator.hasNext(); ) {
            String keyword = (String) iterator.next();
            final boolean keywordExists = allKeywords.contains(keyword);
            if (!keywordExists) {
                addKeyword(keyword);
            }
            addExistingKeywordToDocument(meta_id, keyword);
        }
        deleteUnusedKeywords();
    }

    void updateDocumentSections(int metaId,
                                Set sectionIds) {
        removeAllSectionsFromDocument(metaId);
        for ( Iterator iterator = sectionIds.iterator(); iterator.hasNext(); ) {
            Integer sectionId = (Integer) iterator.next();
            addSectionIdToDocument(metaId, sectionId);
        }
    }

    private void addSectionIdToDocument(int metaId, Integer sectionId) {
        Integer[] params = new Integer[]{new Integer(metaId), sectionId };
        documentMapper.getDatabase().execute(new SqlUpdateDatabaseCommand("INSERT INTO meta_section VALUES(?,?)", params));
    }

    private void deleteKeywordsFromDocument(int meta_id) {
        String sqlDeleteKeywordsFromDocument = "DELETE FROM meta_classification WHERE meta_id = ?";
        String[] params = new String[]{"" + meta_id};
        DatabaseUtils.executeUpdate(documentMapper.getDatabase(), sqlDeleteKeywordsFromDocument, params);
    }

    private void deleteUnusedKeywords() {
        String[] params = new String[0];
        DatabaseUtils.executeUpdate(documentMapper.getDatabase(), "DELETE FROM classification WHERE class_id NOT IN (SELECT class_id FROM meta_classification)", params);
    }

    private void addKeyword(String keyword) {
        String[] params = new String[]{keyword};
        DatabaseUtils.executeUpdate(documentMapper.getDatabase(), "INSERT INTO classification (code) VALUES(?)", params);
    }

    private void removeAllSectionsFromDocument(int metaId) {
        String[] params = new String[]{"" + metaId};
        DatabaseUtils.executeUpdate(documentMapper.getDatabase(), "DELETE FROM meta_section WHERE meta_id = ?", params);
    }

    private void addExistingKeywordToDocument(int meta_id, String keyword) {
        String[] params1 = new String[]{
            keyword
        };
        int keywordId = Integer.parseInt(DatabaseUtils.executeStringQuery(documentMapper.getDatabase(), "SELECT class_id FROM classification WHERE code = ?", params1));
        String[] params = new String[]{"" + meta_id, "" + keywordId};
        DatabaseUtils.executeUpdate(documentMapper.getDatabase(), "INSERT INTO meta_classification (meta_id, class_id) VALUES(?,?)", params);
    }
}
