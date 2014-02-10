package com.imcode.imcms.mapping;

import com.imcode.imcms.api.Document;
import com.imcode.imcms.api.Meta;
import com.imcode.imcms.dao.DocVersionDao;
import com.imcode.imcms.dao.MetaDao;
import com.imcode.imcms.mapping.orm.DocAppearance;
import com.imcode.imcms.mapping.orm.DocLanguage;
import com.imcode.imcms.mapping.orm.DocMeta;
import com.imcode.imcms.mapping.orm.DocVersion;
import imcode.server.ImcmsConstants;
import imcode.server.document.*;
import imcode.server.user.RoleId;

import java.util.Map;
import java.util.Set;

/**
 * Loads documents from the database.
 * <p/>
 * Instantiated by spring-framework and initialized in DocumentMapper constructor.
 */
public class DocumentLoader {

    /**
     * Permission to create child documents.
     */
    public final static int PERM_CREATE_DOCUMENT = 8;

    /**
     * Injected by spring.
     */
    private MetaDao metaDao;

    /**
     * Injected by spring.
     */
    private DocVersionDao documentVersionDao;

    /**
     * Initializes document's fields.
     */
    private DocumentInitializingVisitor documentInitializingVisitor;

    /**
     * Loads document's meta.
     *
     * @param docId document id.
     * @return
     */
    public Meta loadMeta(Integer docId) {
        DocMeta ormMeta = metaDao.getMeta(docId);

        if (ormMeta == null) return null;

        Meta meta = OrmToApi.toApi(ormMeta);

        if (meta != null) {
            meta.setActualModifiedDatetime(meta.getModifiedDatetime());

            Document.PublicationStatus publicationStatus = publicationStatusFromInt(ormMeta.getPublicationStatusInt());
            meta.setPublicationStatus(publicationStatus);

            initRoleIdToPermissionSetIdMap(meta, ormMeta);
            initDocumentsPermissionSets(meta, ormMeta);
            initDocumentsPermissionSetsForNew(meta, ormMeta);
        }

        return meta;
    }

    /**
     * Loads and initializes document's fields.
     *
     * @param meta
     * @param version
     * @param language
     * @return
     */
    public <T extends DocumentDomainObject> T loadAndInitDocument(DocMeta meta, DocVersion version, DocLanguage language) {
        return initDocument(this.<T>createDocument(meta, version, language));
    }


    /**
     * Creates document instance.
     */
    private <T extends DocumentDomainObject> T createDocument(DocMeta meta, DocVersion version, DocLanguage language) {
        DocAppearance i18nMeta = metaDao.getI18nMeta(meta.getId(), language);

        if (i18nMeta == null) {
            i18nMeta = new DocAppearance();
            i18nMeta.setLanguage(language);
            i18nMeta.setHeadline("");
            i18nMeta.setMenuText("");
            i18nMeta.setMenuImageURL("");
        }

        T document = DocumentDomainObject.fromDocumentTypeId(meta.getDocumentType());

        document.setMeta(OrmToApi.toApi(meta));
        document.setLanguage(OrmToApi.toApi(language));
        document.setAppearance(OrmToApi.toApi(i18nMeta));
        document.setVersionNo(version.getNo());

        return document;
    }

    /**
     * Initializes document's fields.
     */
    private <T extends DocumentDomainObject> T initDocument(T document) {
        if (document == null) return null;

        document.accept(documentInitializingVisitor);

        return document;
    }

    private Document.PublicationStatus publicationStatusFromInt(int publicationStatusInt) {
        Document.PublicationStatus publicationStatus = Document.PublicationStatus.NEW;
        if (Document.STATUS_PUBLICATION_APPROVED == publicationStatusInt) {
            publicationStatus = Document.PublicationStatus.APPROVED;
        } else if (Document.STATUS_PUBLICATION_DISAPPROVED == publicationStatusInt) {
            publicationStatus = Document.PublicationStatus.DISAPPROVED;
        }
        return publicationStatus;
    }

    // Moved from  DocumentInitializer.initDocuments
    private void initRoleIdToPermissionSetIdMap(Meta meta, DocMeta ormMeta) {
        RoleIdToDocumentPermissionSetTypeMappings rolePermissionMappings =
                new RoleIdToDocumentPermissionSetTypeMappings();

        for (Map.Entry<Integer, Integer> roleIdToPermissionSetId : ormMeta.getRoleIdToPermissionSetIdMap().entrySet()) {
            rolePermissionMappings.setPermissionSetTypeForRole(
                    new RoleId(roleIdToPermissionSetId.getKey()),
                    DocumentPermissionSetTypeDomainObject.fromInt(roleIdToPermissionSetId.getValue()));
        }

        meta.setRoleIdsMappedToDocumentPermissionSetTypes(rolePermissionMappings);
    }

    private void initDocumentsPermissionSets(Meta meta, DocMeta ormMeta) {
        DocumentPermissionSets permissionSets = createDocumentsPermissionSets(
                ormMeta.getPermissionSetBitsMap(), ormMeta.getPermisionSetEx());

        meta.setPermissionSets(permissionSets);
    }


    private void initDocumentsPermissionSetsForNew(Meta meta, DocMeta ormMeta) {
        DocumentPermissionSets permissionSets = createDocumentsPermissionSets(
                ormMeta.getPermissionSetBitsForNewMap(), ormMeta.getPermisionSetExForNew());

        meta.setPermissionSetsForNew(permissionSets);
    }


    private DocumentPermissionSets createDocumentsPermissionSets(
            Map<Integer, Integer> permissionSetBitsMap,
            Set<DocMeta.PermisionSetEx> permissionSetEx) {

        DocumentPermissionSets permissionSets = new DocumentPermissionSets();

        for (Map.Entry<Integer, Integer> permissionSetBitsEntry : permissionSetBitsMap.entrySet()) {
            Integer setId = permissionSetBitsEntry.getKey();
            Integer permissionSetBits = permissionSetBitsEntry.getValue();
            DocumentPermissionSetDomainObject restricted = permissionSets.getRestricted(setId);

            if (permissionSetBits != 0 && restricted.isEmpty()) {
                restricted.setFromBits(permissionSetBits);
            }
        }

        for (DocMeta.PermisionSetEx ex : permissionSetEx) {
            Integer setId = ex.getSetId();
            DocumentPermissionSetDomainObject restricted = permissionSets.getRestricted(setId);

            setPermissionData(restricted, ex.getPermissionId(), ex.getPermissionData());
        }

        return permissionSets;
    }


    private void setPermissionData(DocumentPermissionSetDomainObject permissionSet, Integer permissionId, Integer permissionData) {
        if (null != permissionId) {
            TextDocumentPermissionSetDomainObject textDocumentPermissionSet = (TextDocumentPermissionSetDomainObject) permissionSet;
            switch (permissionId) {
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


    public MetaDao getMetaDao() {
        return metaDao;
    }

    public void setMetaDao(MetaDao metaDao) {
        this.metaDao = metaDao;
    }

    public DocumentInitializingVisitor getDocumentInitializingVisitor() {
        return documentInitializingVisitor;
    }

    public void setDocumentInitializingVisitor(DocumentInitializingVisitor documentInitializingVisitor) {
        this.documentInitializingVisitor = documentInitializingVisitor;
    }

    public DocVersionDao getDocumentVersionDao() {
        return documentVersionDao;
    }

    public void setDocumentVersionDao(DocVersionDao documentVersionDao) {
        this.documentVersionDao = documentVersionDao;
    }
}