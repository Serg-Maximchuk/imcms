package com.imcode.imcms.api;

import imcode.server.document.*;
import imcode.server.user.UserAndRoleMapper;
import imcode.server.user.UserDomainObject;

import java.io.IOException;

public class DocumentService {

    private SecurityChecker securityChecker;
    private DocumentMapper documentMapper;
    private DocumentPermissionSetMapper documentPermissionSetMapper;
    private UserAndRoleMapper userAndRoleMapper;

    public DocumentService( SecurityChecker securityChecker, DocumentMapper documentMapper,
                            DocumentPermissionSetMapper documentPermissionSetMapper,
                            UserAndRoleMapper userAndRoleMapper ) {
        this.securityChecker = securityChecker;
        this.documentMapper = documentMapper;
        this.documentPermissionSetMapper = documentPermissionSetMapper;
        this.userAndRoleMapper = userAndRoleMapper;
    }

    /**
     * @param documentId The id number of the document requested, also somtimes known as "meta_id"
     * @return The document
     * @throws com.imcode.imcms.api.NoPermissionException
     *          If the current user dosen't have the rights to read this document.
     */
    public TextDocument getTextDocument( int documentId ) throws NoPermissionException {
        imcode.server.document.DocumentDomainObject doc = documentMapper.getDocument( documentId );
        TextDocument result = new TextDocument( doc, securityChecker, this, documentMapper,
                                                documentPermissionSetMapper, userAndRoleMapper );
        securityChecker.hasAtLeastDocumentReadPermission( result );
        return result;
    }

    public TextDocument createNewTextDocument( int parentId, int parentMenuNumber ) throws NoPermissionException {
        securityChecker.hasEditPermission( parentId );
        UserDomainObject user = securityChecker.getCurrentLoggedInUser();
        DocumentDomainObject newDoc = documentMapper.createNewTextDocument( user, parentId, parentMenuNumber );
        TextDocument result = new TextDocument( newDoc, securityChecker, this, documentMapper,
                                                documentPermissionSetMapper, userAndRoleMapper );
        return result;
    }

    public void saveChanges( TextDocument document ) throws NoPermissionException, MaxCategoriesOfTypeExceededException {
        securityChecker.hasEditPermission( document );
        try {
            documentMapper.saveDocument( document.getInternal() );
        } catch ( MaxCategoryDomainObjectsOfTypeExceededException e ) {
            throw new MaxCategoriesOfTypeExceededException( e );
        }
    }

    public Category getCategory( CategoryType categoryType, String categoryName ) {
        // Allow everyone to get a Category. No security check.
        final CategoryDomainObject category = documentMapper.getCategory( categoryType.getInternal(), categoryName );
        if ( null != category ) {
            return new Category( category );
        } else {
            return null;
        }
    }

    public Category getCategory( int categoryId ) {
        // Allow everyone to get a Category. No security check.
        final CategoryDomainObject category = documentMapper.getCategory( categoryId );
        if ( null != category ) {
            return new Category( category );
        } else {
            return null;
        }
    }

    public CategoryType getCategoryType( String categoryTypeName ) {
        // Allow everyone to get a CategoryType. No security check.
        final CategoryTypeDomainObject categoryType = documentMapper.getCategoryType( categoryTypeName );
        if ( null != categoryType ) {
            return new CategoryType( categoryType );
        }
        return null;
    }

    public Category[] getAllCategoriesOfType( CategoryType categoryType ) {
        // Allow everyone to get a CategoryType. No security check.
        CategoryDomainObject[] categoryDomainObjects = documentMapper.getAllCategoriesOfType(
                categoryType.getInternal() );
        Category[] categories = new Category[categoryDomainObjects.length];
        for ( int i = 0; i < categoryDomainObjects.length; i++ ) {
            CategoryDomainObject categoryDomainObject = categoryDomainObjects[i];
            categories[i] = new Category( categoryDomainObject );
        }
        return categories;
    }

    public CategoryType[] getAllCategoryTypes() {
        // Allow everyone to get a CategoryType. No security check.
        CategoryTypeDomainObject[] categoryTypeDomainObjects = documentMapper.getAllCategoryTypes();
        CategoryType[] categoryTypes = new CategoryType[categoryTypeDomainObjects.length];
        for ( int i = 0; i < categoryTypeDomainObjects.length; i++ ) {
            CategoryTypeDomainObject categoryTypeDomainObject = categoryTypeDomainObjects[i];
            categoryTypes[i] = new CategoryType( categoryTypeDomainObject );
        }
        return categoryTypes;
    }

    public Section getSection( int sectionId ) {
        // Allow everyone to get a Section. No security check.
        SectionDomainObject[] sections = documentMapper.getAllSections();
        Section result = null;
        for ( int i = 0; i < sections.length; i++ ) {
            SectionDomainObject section = sections[i];
            if ( sectionId == section.getId() ) {
                result = new Section( section );
            }
        }
        return result;
    }

    public Document[] search( SearchQuery query ) throws SearchException {
        try {
            DocumentDomainObject[] documentDomainObjects = documentMapper.getDocumentIndex().search( query.getQuery() );
            Document[] documents = new Document[documentDomainObjects.length];
            for ( int i = 0; i < documentDomainObjects.length; i++ ) {
                DocumentDomainObject documentDomainObject = documentDomainObjects[i];
                documents[i] =
                new Document( documentDomainObject, securityChecker, this, documentMapper, documentPermissionSetMapper,
                              userAndRoleMapper );
            }
            return documents;
        } catch ( IOException e ) {
            throw new SearchException( e );
        }
    }
}