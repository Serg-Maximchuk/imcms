package com.imcode.imcms.api;

import imcode.server.Config;
import imcode.server.MockImcmsServices;
import imcode.server.db.impl.MockDatabase;
import imcode.server.document.DocumentMapper;
import imcode.server.user.RoleDomainObject;
import imcode.server.user.UserDomainObject;
import junit.framework.TestCase;

public class TestDocumentService extends TestCase {

    private DocumentService documentService;
    private MockDatabase database;
    private User user;

    public void setUp() throws Exception {
        super.setUp();
        MockContentManagementSystem contentManagementSystem = new MockContentManagementSystem();
        user = new User(new UserDomainObject());
        contentManagementSystem.setCurrentUser( user );
        MockImcmsServices imcmsServices = new MockImcmsServices();
        database = new MockDatabase();
        imcmsServices.setDocumentMapper(new DocumentMapper(imcmsServices, database, null,null,null,null,new Config() )) ;
        contentManagementSystem.setInternal( imcmsServices );
        this.documentService = new DocumentService(contentManagementSystem) ;
    }

    public void testSaveCategory() throws CategoryAlreadyExistsException, NoPermissionException {
        String[][] allCategoryTypesResult = new String[][] { { "1", "test", "0" } };
        database.addExpectedSqlCall( new MockDatabase.MatchesRegexSqlCallPredicate( "SELECT category_type_id"), allCategoryTypesResult );
        CategoryType categoryType = documentService.getAllCategoryTypes()[0] ;
        String categoryName = "name";
        Category category = new Category( categoryName, categoryType );
        category.setDescription( "description" );
        category.setImage( "image" );
        try {
            documentService.saveCategory(category);
            fail() ;
        } catch( NoPermissionException e ) {}
        user.addRole( new Role( RoleDomainObject.SUPERADMIN ) );
        database.addExpectedSqlCall( new MockDatabase.MatchesRegexSqlCallPredicate( "insert.*categories.*IDENTITY"), "1" );
        documentService.saveCategory(category);
        database.verifyExpectedSqlCalls();

        String[] categoryResult = new String[] { "1", category.getName(), category.getDescription(), category.getImage() };
        database.addExpectedSqlCall( new MockDatabase.EqualsSqlCallPredicate(DocumentMapper.SQL_GET_CATEGORY), categoryResult );
        documentService.saveCategory( category );
        database.verifyExpectedSqlCalls();

        database.addExpectedSqlCall( new MockDatabase.EqualsSqlCallPredicate(DocumentMapper.SQL_GET_CATEGORY), categoryResult );
        Category otherCategory = new Category( categoryName, categoryType );
        try {
            documentService.saveCategory( otherCategory );
            fail() ;
        } catch ( CategoryAlreadyExistsException e ) {
        }
        database.verifyExpectedSqlCalls();

        String otherName = "other name";
        category.setName( otherName );
        category.setDescription( "other description");
        category.setImage( "other image");
        documentService.saveCategory( category );
        database.assertCalled( new MockDatabase.UpdateTableSqlCallPredicate( "categories", otherName ));
    }

}
