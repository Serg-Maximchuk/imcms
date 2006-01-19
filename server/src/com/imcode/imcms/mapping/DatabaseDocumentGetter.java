package com.imcode.imcms.mapping;

import com.imcode.db.Database;
import com.imcode.db.DatabaseCommand;
import com.imcode.db.commands.SqlQueryDatabaseCommand;
import com.imcode.db.handlers.CollectionResultSetHandler;
import com.imcode.db.handlers.RowTransformer;
import com.imcode.imcms.api.Document;
import com.imcode.util.CountingIterator;
import imcode.server.ImcmsServices;
import imcode.server.LanguageMapper;
import imcode.server.ImcmsConstants;
import imcode.server.document.*;
import imcode.server.document.textdocument.TextDocumentDomainObject;
import imcode.server.user.RoleId;
import imcode.util.LazilyLoadedObject;
import imcode.util.Utility;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DatabaseDocumentGetter extends AbstractDocumentGetter {

    private Database database;
    private ImcmsServices services;
    Logger log = Logger.getLogger(DatabaseDocumentGetter.class);
    public static final String SQL_GET_DOCUMENTS = "SELECT meta_id,\n"
                                                   + "doc_type,\n"
                                                   + "meta_headline,\n"
                                                   + "meta_text,\n"
                                                   + "meta_image,\n"
                                                   + "owner_id,\n"
                                                   + "permissions,\n"
                                                   + "shared,\n"
                                                   + "show_meta,\n"
                                                   + "lang_prefix,\n"
                                                   + "date_created,\n"
                                                   + "date_modified,\n"
                                                   + "disable_search,\n"
                                                   + "target,\n"
                                                   + "archived_datetime,\n"
                                                   + "publisher_id,\n"
                                                   + "status,\n"
                                                   + "publication_start_datetime,\n"
                                                   + "publication_end_datetime\n"
                                                   + "FROM meta\n"
                                                   + "WHERE meta_id ";
    public static final String SQL_GET_SECTION_IDS_FOR_DOCUMENT = "SELECT ms.meta_id, ms.section_id\n"
                                                                  + "FROM meta_section ms\n"
                                                                  + "WHERE ms.meta_id ";
    /** Stored procedure names used in this class */
    static final String SQL_GET_TEMPLATE_GROUPS_WITH_PERMISSIONS = "SELECT meta_id, set_id, permission_data\n"
                                                                   + "FROM   doc_permission_sets_ex\n"
                                                                   + "       WHERE permission_id = 524288\n"
                                                                   + "       AND meta_id ";
    static final String SQL_GET_TEMPLATE_GROUPS_WITH_NEW_PERMISSIONS = "SELECT meta_id, set_id, permission_data\n"
                                                                       + "FROM   new_doc_permission_sets_ex\n"
                                                                       + "       WHERE permission_id = 524288\n"
                                                                       + "       AND meta_id ";
    static final String SQL_SELECT_PERMISSON_DATA__PREFIX = "SELECT meta_id, set_id, permission_data FROM ";
    /** Permission to create child documents. * */
    public final static int PERM_CREATE_DOCUMENT = 8;
    private static final String SQL_GET_KEYWORDS = "SELECT mc.meta_id, c.code FROM classification c JOIN meta_classification mc ON mc.class_id = c.class_id WHERE mc.meta_id ";

    public DatabaseDocumentGetter(Database database, ImcmsServices services) {
        this.database = database;
        this.services = services;
    }

    public List getDocuments(final Collection documentIds) {
        DocumentList documentList = new DocumentList(documentIds.size());
        StringBuffer sql = new StringBuffer(SQL_GET_DOCUMENTS);
        Integer[] parameters = appendInClause(sql, documentIds);
        DatabaseCommand command = new SqlQueryDatabaseCommand(sql.toString(), parameters, new CollectionResultSetHandler(documentList, new DocumentFromRowFactory()));
        database.execute(command);

        final Map documentMap = documentList.getMap();

        final MultiHashMap documentsSectionIds = new MultiHashMap();
        final MultiHashMap documentsKeywords = new MultiHashMap();
        final MultiHashMap documentsCategoryIds = new MultiHashMap();
        final HashMap documentsRolePermissionMappings = new HashMap();
        final HashMap documentsPermissionSets = new HashMap();
        final HashMap newDocumentsPermissionSets = new HashMap();
        DocumentInitializingVisitor documentInitializingVisitor = new DocumentInitializingVisitor(database, services.getDefaultDocumentMapper(), documentMap.keySet(), services.getDefaultDocumentMapper());
        for ( Iterator iterator = documentList.iterator(); iterator.hasNext(); ) {
            final DocumentDomainObject document = (DocumentDomainObject) iterator.next();
            final Integer documentId = new Integer(document.getId()) ;
            document.setLazilyLoadedPermissionSets(new LazilyLoadedObject(new DocumentPermissionSetsLoader(documentMap.keySet(), documentsPermissionSets, newDocumentsPermissionSets, documentsPermissionSets, documentId)));
            document.setLazilyLoadedPermissionSetsForNew(new LazilyLoadedObject(new DocumentPermissionSetsLoader(documentMap.keySet(), documentsPermissionSets, newDocumentsPermissionSets, newDocumentsPermissionSets, documentId)));
            document.setLazilyLoadedRoleIdsMappedToDocumentPermissionSetTypes(new LazilyLoadedObject(new DocumentRolePermissionsLoader(documentMap.keySet(), documentsRolePermissionMappings, documentId)));
            document.setLazilyLoadedSectionIds(new LazilyLoadedObject(new DocumentSectionIdsLoader(documentMap.keySet(), documentId, documentsSectionIds)));
            document.setLazilyLoadedKeywords(new LazilyLoadedObject(new DocumentKeywordsLoader(documentMap.keySet(), documentId, documentsKeywords)));
            document.setLazilyLoadedCategoryIds(new LazilyLoadedObject(new DocumentCategoryIdsLoader(documentMap.keySet(), documentId, documentsCategoryIds)));

            document.accept(documentInitializingVisitor);
        }
        return documentList;
    }

    private void initDocumentsCategoryIds(Collection documentIds, final MultiHashMap documentsCategoryIds) {
        StringBuffer sql = new StringBuffer(CategoryMapper.SQL__GET_DOCUMENT_CATEGORIES);
        Integer[] parameters = appendInClause(sql, documentIds);
        database.execute(new SqlQueryDatabaseCommand(sql.toString(), parameters, new ResultSetHandler() {
            public Object handle(ResultSet rs) throws SQLException {
                while ( rs.next() ) {
                    int documentId = rs.getInt(1);
                    int categoryId = rs.getInt(2);
                    documentsCategoryIds.put(new Integer(documentId), new Integer(categoryId));
                }
                return null;
            }
        }));
    }

    private void initDocumentsSectionIds(final Collection documentIds, final MultiMap documentSectionIds) {

        StringBuffer sql = new StringBuffer(SQL_GET_SECTION_IDS_FOR_DOCUMENT);
        Integer[] parameters = appendInClause(sql, documentIds);
        database.execute(new SqlQueryDatabaseCommand(sql.toString(), parameters, new ResultSetHandler() {
            public Object handle(ResultSet rs) throws SQLException {
                while ( rs.next() ) {
                    int documentId = rs.getInt(1);
                    int sectionId = rs.getInt(2);
                    documentSectionIds.put(new Integer(documentId), new Integer(sectionId));
                }
                return null;
            }
        }));
    }

    static Integer[] appendInClause(StringBuffer sql, Collection documentIds) {
        sql.append("IN (");
        Integer[] documentIdsArray = new Integer[documentIds.size()];
        for ( CountingIterator iterator = new CountingIterator(documentIds.iterator()); iterator.hasNext(); ) {
            Integer documentId = (Integer) iterator.next();
            documentIdsArray[iterator.getCount() - 1] = new Integer(documentId.intValue());
            sql.append('?');
            if ( iterator.hasNext() ) {
                sql.append(',');
            }
        }
        sql.append(')');
        return documentIdsArray;
    }

    private Document.PublicationStatus publicationStatusFromInt(int publicationStatusInt) {
        Document.PublicationStatus publicationStatus = Document.PublicationStatus.NEW;
        if ( Document.STATUS_PUBLICATION_APPROVED == publicationStatusInt ) {
            publicationStatus = Document.PublicationStatus.APPROVED;
        } else if ( Document.STATUS_PUBLICATION_DISAPPROVED == publicationStatusInt ) {
            publicationStatus = Document.PublicationStatus.DISAPPROVED;
        }
        return publicationStatus;
    }

    private void initDocumentsKeywords(Collection documentIds, final MultiMap documentsKeywords) {
        StringBuffer sql = new StringBuffer(SQL_GET_KEYWORDS);
        Integer[] parameters = appendInClause(sql, documentIds);
        database.execute(new SqlQueryDatabaseCommand(sql.toString(), parameters, new ResultSetHandler() {
            public Object handle(ResultSet rs) throws SQLException {
                while ( rs.next() ) {
                    int documentId = rs.getInt(1);
                    String keyword = rs.getString(2);
                    documentsKeywords.put(new Integer(documentId), keyword);
                }
                return null;
            }
        }));
    }

    public void initDocumentsRolePermissionMappings(Collection documentIds,
                                                    final Map documentsRolePermissionMappings) {
        if ( !documentsRolePermissionMappings.isEmpty() ) {
            return;
        }

        StringBuffer sql = new StringBuffer("SELECT "
                                            + "meta_id, role_id, set_id\n"
                                            + "FROM  roles_rights\n"
                                            + "WHERE meta_id ");
        Integer[] parameters = appendInClause(sql, documentIds);

        database.execute(new SqlQueryDatabaseCommand(sql.toString(), parameters, new ResultSetHandler() {
            public Object handle(ResultSet rs) throws SQLException {
                while ( rs.next() ) {
                    Integer documentId = new Integer(rs.getInt(1));
                    int roleId = rs.getInt(2);
                    int setId = rs.getInt(3);
                    RoleIdToDocumentPermissionSetTypeMappings rolePermissionMappings = (RoleIdToDocumentPermissionSetTypeMappings) documentsRolePermissionMappings.get(documentId);
                    if ( null == rolePermissionMappings ) {
                        rolePermissionMappings = new RoleIdToDocumentPermissionSetTypeMappings();
                        documentsRolePermissionMappings.put(documentId, rolePermissionMappings);
                    }
                    rolePermissionMappings.setPermissionSetTypeForRole(new RoleId(roleId), DocumentPermissionSetTypeDomainObject.fromInt(setId));
                }
                return null;
            }
        }));

    }

    public void initDocumentsPermissionSets(final Collection documentIds, final Map documentsPermissionSets,
                                            final Map newDocumentsPermissionSets) {
        if (!documentsPermissionSets.isEmpty() && !documentsPermissionSets.isEmpty()) {
            return ;
        }
        
        StringBuffer sql = new StringBuffer(
                "SELECT d.meta_id, d.set_id, d.permission_id, n.permission_id, de.permission_id, de.permission_data, ne.permission_id, ne.permission_data\n"
                + "FROM doc_permission_sets d\n"
                + "JOIN new_doc_permission_sets n ON d.meta_id = n.meta_id AND d.set_id = n.set_id\n"
                + "LEFT JOIN doc_permission_sets_ex de ON d.meta_id = de.meta_id AND d.set_id = de.set_id\n"
                + "LEFT JOIN new_doc_permission_sets_ex ne ON d.meta_id = ne.meta_id AND d.set_id = ne.set_id\n"
                + "WHERE d.meta_id ");
        Integer[] parameters = appendInClause(sql, documentIds);
        database.execute(new SqlQueryDatabaseCommand(sql.toString(), parameters, new ResultSetHandler() {
            public Object handle(ResultSet resultSet) throws SQLException {
                while ( resultSet.next() ) {
                    Integer documentId = new Integer(resultSet.getInt(1));
                    int setId = resultSet.getInt(2);
                    int permissionSetBits = resultSet.getInt(3);
                    int permissionSetBitsForNew = resultSet.getInt(4);
                    Integer permissionId = Utility.getInteger(resultSet.getObject(5));
                    Integer permissionData = Utility.getInteger(resultSet.getObject(6));
                    Integer permissionIdForNew = Utility.getInteger(resultSet.getObject(7));
                    Integer permissionDataForNew = Utility.getInteger(resultSet.getObject(8));

                    DocumentPermissionSets permissionSets = (DocumentPermissionSets) documentsPermissionSets.get(documentId);
                    if (null == permissionSets) {
                        permissionSets = new DocumentPermissionSets();
                        documentsPermissionSets.put(documentId,permissionSets) ;
                    }
                    DocumentPermissionSets permissionSetsForNewDocuments = (DocumentPermissionSets) newDocumentsPermissionSets.get(documentId);
                    if (null == permissionSetsForNewDocuments) {
                        permissionSetsForNewDocuments = new DocumentPermissionSets();
                        newDocumentsPermissionSets.put(documentId,permissionSetsForNewDocuments) ;
                    }
                    
                    permissionSets.setRestricted(setId, createDocumentPermissionSet(DocumentPermissionSetTypeDomainObject.fromInt(setId), permissionSetBits));
                    permissionSetsForNewDocuments.setRestricted(setId, createDocumentPermissionSet(DocumentPermissionSetTypeDomainObject.fromInt(setId), permissionSetBitsForNew));

                    setPermissionData(permissionSets, setId, permissionId, permissionData);
                    setPermissionData(permissionSetsForNewDocuments, setId, permissionIdForNew, permissionDataForNew);
                }
                return null;
            }
        }));
    }

    private void setPermissionData(DocumentPermissionSets permissionSets, int setId, Integer permissionId,
                                   Integer permissionData) {
        if (null != permissionId) {
            TextDocumentPermissionSetDomainObject textDocumentPermissionSet = (TextDocumentPermissionSetDomainObject) permissionSets.getRestricted(setId);
            switch(permissionId.intValue()) {
                case PERM_CREATE_DOCUMENT:
                    textDocumentPermissionSet.addAllowedDocumentTypeId(permissionData.intValue());
                    break;
                case ImcmsConstants.PERM_EDIT_TEXT_DOCUMENT_TEMPLATE:
                    textDocumentPermissionSet.addAllowedTemplateGroupId(permissionData.intValue());
                    break;
                default:
            }
        }
    }

    private void initAllowedDocumentTypes(final Map documents) {
        StringBuffer sql = new StringBuffer(SQL_SELECT_PERMISSON_DATA__PREFIX + "doc_permission_sets_ex"
                                            + " WHERE permission_id = "
                                            + PERM_CREATE_DOCUMENT + " AND meta_id ");
        Integer[] parameters = appendInClause(sql, documents.keySet());
        database.execute(new SqlQueryDatabaseCommand(sql.toString(), parameters, new ResultSetHandler() {
            public Object handle(ResultSet resultSet) throws SQLException {
                while ( resultSet.next() ) {
                    int documentId = resultSet.getInt(1);
                    int setId = resultSet.getInt(2);
                    int documentTypeId = resultSet.getInt(3);
                    TextDocumentDomainObject document = (TextDocumentDomainObject) documents.get(new Integer(documentId));
                    DocumentPermissionSets permissionSets = document.getPermissionSets();
                    TextDocumentPermissionSetDomainObject restricted = (TextDocumentPermissionSetDomainObject) permissionSets.getRestricted(setId);
                    restricted.addAllowedDocumentTypeId(documentTypeId);
                }
                return null;
            }
        }));

        sql = new StringBuffer(SQL_SELECT_PERMISSON_DATA__PREFIX + "new_doc_permission_sets_ex"
                               + " WHERE permission_id = "
                               + PERM_CREATE_DOCUMENT + " AND meta_id ");
        parameters = appendInClause(sql, documents.keySet());
        database.execute(new SqlQueryDatabaseCommand(sql.toString(), parameters, new ResultSetHandler() {
            public Object handle(ResultSet resultSet) throws SQLException {
                while ( resultSet.next() ) {
                    int documentId = resultSet.getInt(1);
                    int setId = resultSet.getInt(2);
                    int documentTypeId = resultSet.getInt(3);
                    TextDocumentDomainObject document = (TextDocumentDomainObject) documents.get(new Integer(documentId));
                    DocumentPermissionSets permissionSets = document.getPermissionSetsForNewDocuments();
                    TextDocumentPermissionSetDomainObject restricted = (TextDocumentPermissionSetDomainObject) permissionSets.getRestricted(setId);
                    restricted.addAllowedDocumentTypeId(documentTypeId);
                }
                return null;
            }
        }));
    }

    private void initAllowedTemplateGroups(final Map documents) {
        StringBuffer sql = new StringBuffer(SQL_GET_TEMPLATE_GROUPS_WITH_PERMISSIONS);
        Integer[] parameters = appendInClause(sql, documents.keySet());

        database.execute(new SqlQueryDatabaseCommand(sql.toString(), parameters, new ResultSetHandler() {
            public Object handle(ResultSet resultSet) throws SQLException {
                while ( resultSet.next() ) {
                    int documentId = resultSet.getInt(1);
                    int setId = resultSet.getInt(2);
                    int templateGroupId = resultSet.getInt(3);
                    TextDocumentDomainObject document = (TextDocumentDomainObject) documents.get(new Integer(documentId));
                    DocumentPermissionSets permissionSets = document.getPermissionSets();
                    TextDocumentPermissionSetDomainObject restricted = (TextDocumentPermissionSetDomainObject) permissionSets.getRestricted(setId);
                    restricted.addAllowedTemplateGroupId(templateGroupId);
                }
                return null;
            }
        }));

        sql = new StringBuffer(SQL_GET_TEMPLATE_GROUPS_WITH_NEW_PERMISSIONS);
        parameters = appendInClause(sql, documents.keySet());

        database.execute(new SqlQueryDatabaseCommand(sql.toString(), parameters, new ResultSetHandler() {
            public Object handle(ResultSet resultSet) throws SQLException {
                while ( resultSet.next() ) {
                    int documentId = resultSet.getInt(1);
                    int setId = resultSet.getInt(2);
                    int templateGroupId = resultSet.getInt(3);
                    TextDocumentDomainObject document = (TextDocumentDomainObject) documents.get(new Integer(documentId));
                    DocumentPermissionSets permissionSetsForNewDocuments = document.getPermissionSetsForNewDocuments();
                    TextDocumentPermissionSetDomainObject restricted = (TextDocumentPermissionSetDomainObject) permissionSetsForNewDocuments.getRestricted(setId);
                    restricted.addAllowedTemplateGroupId(templateGroupId);
                }
                return null;
            }
        }));
    }

    private DocumentPermissionSetDomainObject createDocumentPermissionSet(
            DocumentPermissionSetTypeDomainObject documentPermissionSetType,
            int permissionBits) {
        DocumentPermissionSetDomainObject documentPermissionSet = new TextDocumentPermissionSetDomainObject(documentPermissionSetType);
        documentPermissionSet.setFromBits(permissionBits);
        return documentPermissionSet;
    }

    private class DocumentFromRowFactory implements RowTransformer {

        public Object createObjectFromResultSetRow(ResultSet resultSet) throws SQLException {
            final int documentTypeId = resultSet.getInt(2);
            DocumentDomainObject document = DocumentDomainObject.fromDocumentTypeId(documentTypeId);

            int documentId = resultSet.getInt(1);
            document.setId(documentId);
            document.setHeadline(resultSet.getString(3));
            document.setMenuText(resultSet.getString(4));
            document.setMenuImage(resultSet.getString(5));

            document.setCreatorId(resultSet.getInt(6));
            document.setRestrictedOneMorePrivilegedThanRestrictedTwo(resultSet.getBoolean(7));
            document.setLinkableByOtherUsers(resultSet.getBoolean(8));
            document.setVisibleInMenusForUnauthorizedUsers(resultSet.getBoolean(9));
            document.setLanguageIso639_2(LanguageMapper.getAsIso639_2OrDefaultLanguage(resultSet.getString(10), services.getLanguageMapper().getDefaultLanguage()));
            document.setCreatedDatetime(resultSet.getTimestamp(11));
            Date modifiedDatetime = resultSet.getTimestamp(12);
            document.setModifiedDatetime(modifiedDatetime);
            document.setActualModifiedDatetime(modifiedDatetime);
            document.setSearchDisabled(resultSet.getBoolean(13));
            document.setTarget(resultSet.getString(14));
            document.setArchivedDatetime(resultSet.getTimestamp(15));
            Number publisherId = (Number) resultSet.getObject(16);
            document.setPublisherId(publisherId == null ? null : new Integer(publisherId.intValue()));
            int publicationStatusInt = resultSet.getInt(17);
            Document.PublicationStatus publicationStatus = publicationStatusFromInt(publicationStatusInt);
            document.setPublicationStatus(publicationStatus);
            document.setPublicationStartDatetime(resultSet.getTimestamp(18));
            document.setPublicationEndDatetime(resultSet.getTimestamp(19));

            return document;
        }

        public Class getClassOfCreatedObjects() {
            return DocumentDomainObject.class;
        }
    }

    private static class DocumentList extends AbstractList implements Serializable {

        private ArrayList list;
        private Map map;

        DocumentList(int capacity) {
            list = new ArrayList(capacity);
            map = Collections.synchronizedMap(new LinkedHashMap(capacity));
        }

        public synchronized Object remove(int index) {
            Object o = list.remove(index);
            DocumentDomainObject document = (DocumentDomainObject) o;
            map.remove(new Integer(document.getId()));
            return o;
        }

        public synchronized Object set(int index, Object o) {
            DocumentDomainObject document = (DocumentDomainObject) o;
            DocumentDomainObject previousDocument = (DocumentDomainObject) list.set(index, o);
            if ( null != previousDocument ) {
                map.remove(new Integer(previousDocument.getId()));
            }
            map.put(new Integer(document.getId()), document);
            return previousDocument;
        }

        public synchronized Object get(int index) {
            return list.get(index);
        }

        public synchronized Iterator iterator() {
            return list.iterator();
        }

        public synchronized boolean add(Object o) {
            DocumentDomainObject document = (DocumentDomainObject) o;
            map.put(new Integer(document.getId()), document);
            return list.add(o);
        }

        public synchronized int size() {
            return list.size();
        }

        public synchronized Map getMap() {
            return map;
        }
    }

    private class DocumentSectionIdsLoader implements LazilyLoadedObject.Loader {

        private final Collection documentIds;
        private final Integer documentId;
        private final MultiHashMap documentsSectionIds;

        DocumentSectionIdsLoader(Collection documentIds, Integer documentId,
                                 MultiHashMap documentsSectionIds) {
            this.documentIds = documentIds;
            this.documentId = documentId;
            this.documentsSectionIds = documentsSectionIds;
        }

        public LazilyLoadedObject.Copyable load() {
            if ( documentsSectionIds.isEmpty() ) {
                initDocumentsSectionIds(documentIds, documentsSectionIds);
            }
            Collection sectionIds = (Collection) documentsSectionIds.get(documentId);
            if ( null == sectionIds ) {
                sectionIds = Collections.EMPTY_SET;
            }
            return new CopyableHashSet(sectionIds);
        }
    }

    private class DocumentKeywordsLoader implements LazilyLoadedObject.Loader {

        private final Collection documentIds;
        private final Integer documentId;
        private final MultiHashMap documentsKeywords;

        public DocumentKeywordsLoader(Collection documentIds, Integer documentId,
                                      MultiHashMap documentsKeywords) {
            this.documentIds = documentIds;
            this.documentId = documentId;
            this.documentsKeywords = documentsKeywords;
        }

        public LazilyLoadedObject.Copyable load() {
            if ( documentsKeywords.isEmpty() ) {
                initDocumentsKeywords(documentIds, documentsKeywords);
            }
            Collection documentKeywords = (Collection) documentsKeywords.get(documentId);
            if ( null == documentKeywords ) {
                documentKeywords = Collections.EMPTY_SET;
            }
            return new CopyableHashSet(documentKeywords);
        }
    }

    private class DocumentCategoryIdsLoader implements LazilyLoadedObject.Loader {

        private final MultiHashMap documentsCategoryIds;
        private final Collection documentIds;
        private final Integer documentId;

        public DocumentCategoryIdsLoader(Collection documentIds, Integer documentId,
                                         MultiHashMap documentsCategoryIds
        ) {
            this.documentsCategoryIds = documentsCategoryIds;
            this.documentIds = documentIds;
            this.documentId = documentId;
        }

        public LazilyLoadedObject.Copyable load() {
            if ( documentsCategoryIds.isEmpty() ) {
                initDocumentsCategoryIds(documentIds, documentsCategoryIds);
            }
            Collection documentCategoryIds = documentsCategoryIds.getCollection(documentId);
            if ( null == documentCategoryIds ) {
                documentCategoryIds = Collections.EMPTY_SET;
            }
            return new CopyableHashSet(documentCategoryIds);
        }
    }

    private class DocumentRolePermissionsLoader implements LazilyLoadedObject.Loader {

        private final Collection documentIds;
        private final HashMap documentsRolePermissionMappings;
        private final Integer documentId;

        public DocumentRolePermissionsLoader(Collection documentIds, HashMap documentsRolePermissionMappings,
                                             Integer documentId) {
            this.documentIds = documentIds;
            this.documentsRolePermissionMappings = documentsRolePermissionMappings;
            this.documentId = documentId;
        }

        public LazilyLoadedObject.Copyable load() {
            initDocumentsRolePermissionMappings(documentIds, documentsRolePermissionMappings);
            RoleIdToDocumentPermissionSetTypeMappings rolePermissionMappings = (RoleIdToDocumentPermissionSetTypeMappings) documentsRolePermissionMappings.get(documentId);
            if ( null == rolePermissionMappings ) {
                rolePermissionMappings = new RoleIdToDocumentPermissionSetTypeMappings();
            }
            return rolePermissionMappings;
        }
    }

    private class DocumentPermissionSetsLoader implements LazilyLoadedObject.Loader {

        private final HashMap documentsPermissionSets;
        private final HashMap newDocumentsPermissionSets;
        private final HashMap documentPermissionSetsForThisLoader;
        private final Integer documentId;
        private final Collection documentIds;

        public DocumentPermissionSetsLoader(Collection documentIds, HashMap documentsPermissionSetsCache,
                                            HashMap newDocumentsPermissionSetsCache,
                                            HashMap permissionSetsForThisLoader, Integer documentId) {
            this.documentIds = documentIds;
            this.documentsPermissionSets = documentsPermissionSetsCache;
            this.newDocumentsPermissionSets = newDocumentsPermissionSetsCache;
            this.documentPermissionSetsForThisLoader = permissionSetsForThisLoader;
            this.documentId = documentId;
        }

        public LazilyLoadedObject.Copyable load() {
            initDocumentsPermissionSets(documentIds, documentsPermissionSets, newDocumentsPermissionSets);
            DocumentPermissionSets documentPermissionSets = (DocumentPermissionSets) documentPermissionSetsForThisLoader.get(documentId) ;
            if (null == documentPermissionSets) {
                documentPermissionSets = new DocumentPermissionSets();
            }
            return documentPermissionSets ;
        }
    }
}
