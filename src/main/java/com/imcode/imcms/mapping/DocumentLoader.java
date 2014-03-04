package com.imcode.imcms.mapping;

import com.imcode.imcms.api.*;
import com.imcode.imcms.mapping.jpa.doc.*;
import com.imcode.imcms.mapping.jpa.doc.Language;
import com.imcode.imcms.mapping.jpa.doc.content.CommonContentRepository;
import imcode.server.ImcmsConstants;
import imcode.server.document.*;
import imcode.server.user.RoleId;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Loads documents from the database.
 */
@Component
public class DocumentLoader {

    /**
     * Permission to create child documents.
     */
    public final static int PERM_CREATE_DOCUMENT = 8;

    @Inject
    private DocRepository docRepository;

    @Inject
    private DocVersionRepository docVersionRepository;

    @Inject
    private MetaRepository metaRepository;

    @Inject
    private CommonContentRepository commonContentRepository;

    @Inject
    private DocumentLanguageMapper languageMapper;

    @Inject
    private DocumentContentMapper contentMapper;

    /**
     * Initializes document's fields.
     */
    @Inject
    private DocumentInitializingVisitor documentInitializingVisitor;

    /**
     * Loads document's meta.
     *
     * @param docId document id.
     * @return loaded meta of null if meta with given id does not exists.
     */
    public DocumentMeta loadMeta(int docId) {
        return toDomainObject(metaRepository.findOne(docId));
    }

    /**
     * Loads and initializes document's content.
     */
    public <T extends DocumentDomainObject> T loadAndInitContent(T document) {
        DocumentCommonContent dcc = contentMapper.getCommonContent(document.getRef());

        document.setCommonContent(dcc != null
                ? dcc
                : DocumentCommonContent.builder().headline("").menuImageURL("").menuText("").build()
        );

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
    private void initRoleIdToPermissionSetIdMap(DocumentMeta metaDO, Meta jpaMeta) {
        RoleIdToDocumentPermissionSetTypeMappings rolePermissionMappings =
                new RoleIdToDocumentPermissionSetTypeMappings();

        for (Map.Entry<Integer, Integer> roleIdToPermissionSetId : jpaMeta.getRoleIdToPermissionSetIdMap().entrySet()) {
            rolePermissionMappings.setPermissionSetTypeForRole(
                    new RoleId(roleIdToPermissionSetId.getKey()),
                    DocumentPermissionSetTypeDomainObject.fromInt(roleIdToPermissionSetId.getValue()));
        }

        metaDO.setRoleIdToDocumentPermissionSetTypeMappings(rolePermissionMappings);
    }

    private void initDocumentsPermissionSets(DocumentMeta metaDO, Meta ormMeta) {
        DocumentPermissionSets permissionSets = createDocumentsPermissionSets(
                ormMeta.getPermissionSetBitsMap(), ormMeta.getPermissionSetEx());

        metaDO.setPermissionSets(permissionSets);
    }


    private void initDocumentsPermissionSetsForNew(DocumentMeta metaDO, Meta jpaMeta) {
        DocumentPermissionSets permissionSets = createDocumentsPermissionSets(
                jpaMeta.getPermissionSetBitsForNewMap(), jpaMeta.getPermissionSetExForNew());

        metaDO.setPermissionSetsForNewDocument(permissionSets);
    }


    private DocumentPermissionSets createDocumentsPermissionSets(
            Map<Integer, Integer> permissionSetBitsMap,
            Set<Meta.PermissionSetEx> permissionSetEx) {

        DocumentPermissionSets permissionSets = new DocumentPermissionSets();

        for (Map.Entry<Integer, Integer> permissionSetBitsEntry : permissionSetBitsMap.entrySet()) {
            Integer setId = permissionSetBitsEntry.getKey();
            Integer permissionSetBits = permissionSetBitsEntry.getValue();
            DocumentPermissionSetDomainObject restricted = permissionSets.getRestricted(setId);

            if (permissionSetBits != 0 && restricted.isEmpty()) {
                restricted.setFromBits(permissionSetBits);
            }
        }

        for (Meta.PermissionSetEx ex : permissionSetEx) {
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

    private DocumentMeta toDomainObject(Meta jpaMeta) {
        if (jpaMeta == null) return null;

        DocumentMeta metaDO = new DocumentMeta();

        metaDO.setArchivedDatetime(jpaMeta.getArchivedDatetime());
        metaDO.setCategoryIds(jpaMeta.getCategoryIds());
        metaDO.setCreatedDatetime(jpaMeta.getCreatedDatetime());
        metaDO.setCreatorId(jpaMeta.getCreatorId());
        metaDO.setDefaultVersionNo(jpaMeta.getDefaultVersionNo());
        metaDO.setDisabledLanguageShowMode(DocumentMeta.DisabledLanguageShowMode.valueOf(jpaMeta.getDisabledLanguageShowMode().name()));
        metaDO.setDocumentType(jpaMeta.getDocumentType());

        Set<DocumentLanguage> apiLanguages = new HashSet<>();

        for (Language jpaLanguage : jpaMeta.getEnabledLanguages()) {
            apiLanguages.add(languageMapper.toApiObject(jpaLanguage));
        }

        metaDO.setEnabledLanguages(apiLanguages);
        metaDO.setId(jpaMeta.getId());
        metaDO.setKeywords(jpaMeta.getKeywords());
        metaDO.setLinkableByOtherUsers(jpaMeta.getLinkableByOtherUsers());
        metaDO.setLinkedForUnauthorizedUsers(jpaMeta.getLinkedForUnauthorizedUsers());
        metaDO.setModifiedDatetime(jpaMeta.getModifiedDatetime());
        metaDO.setActualModifiedDatetime(jpaMeta.getModifiedDatetime());
        //m.setPermissionSets(entity.getPermissionSets)
        //m.setPermissionSetsForNew(entity.getPermissionSetExForNew)
        //m.setPermissionSetsForNewDocuments(entity.getPermissionSetsForNewDocuments)
        metaDO.setProperties(jpaMeta.getProperties());
        metaDO.setPublicationEndDatetime(jpaMeta.getPublicationEndDatetime());
        metaDO.setPublicationStartDatetime(jpaMeta.getPublicationStartDatetime());
        metaDO.setPublicationStatus(publicationStatusFromInt(jpaMeta.getPublicationStatusInt()));
        metaDO.setPublisherId(jpaMeta.getPublisherId());
        metaDO.setRestrictedOneMorePrivilegedThanRestrictedTwo(jpaMeta.getRestrictedOneMorePrivilegedThanRestrictedTwo());
        //m.setRoleIdToDocumentPermissionSetTypeMappings()
        metaDO.setSearchDisabled(jpaMeta.getSearchDisabled());
        metaDO.setTarget(jpaMeta.getTarget());

        initRoleIdToPermissionSetIdMap(metaDO, jpaMeta);
        initDocumentsPermissionSets(metaDO, jpaMeta);
        initDocumentsPermissionSetsForNew(metaDO, jpaMeta);

        return metaDO;
    }


    public DocRepository getDocRepository() {
        return docRepository;
    }

    public void setDocRepository(DocRepository docRepository) {
        this.docRepository = docRepository;
    }

    public DocVersionRepository getDocVersionRepository() {
        return docVersionRepository;
    }

    public void setDocVersionRepository(DocVersionRepository docVersionRepository) {
        this.docVersionRepository = docVersionRepository;
    }

    public MetaRepository getMetaRepository() {
        return metaRepository;
    }

    public void setMetaRepository(MetaRepository metaRepository) {
        this.metaRepository = metaRepository;
    }

    public CommonContentRepository getCommonContentRepository() {
        return commonContentRepository;
    }

    public void setCommonContentRepository(CommonContentRepository commonContentRepository) {
        this.commonContentRepository = commonContentRepository;
    }

    public DocumentInitializingVisitor getDocumentInitializingVisitor() {
        return documentInitializingVisitor;
    }

    public void setDocumentInitializingVisitor(DocumentInitializingVisitor documentInitializingVisitor) {
        this.documentInitializingVisitor = documentInitializingVisitor;
    }
}