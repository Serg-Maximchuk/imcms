package imcode.server.document;

import imcode.server.db.MockDatabase;
import imcode.server.document.textdocument.TextDocumentDomainObject;
import imcode.server.document.index.DocumentIndex;
import imcode.server.document.index.IndexException;
import imcode.server.user.RoleDomainObject;
import imcode.server.user.UserDomainObject;
import junit.framework.TestCase;
import org.apache.lucene.search.Query;

public class TestDocumentMapper extends TestCase {

    private DocumentMapper documentMapper;
    private MockDatabase database;
    private UserDomainObject user;
    private RoleDomainObject testRole;
    private RoleDomainObject userRole;
    private TextDocumentDomainObject textDocument;
    private TextDocumentDomainObject oldDocument;

    protected void setUp() throws Exception {
        super.setUp();
        user = new UserDomainObject();
        userRole = new RoleDomainObject( 1, "Userrole", 0 );
        user.addRole( userRole );
        testRole = new RoleDomainObject( 2, "Testrole", 0 );
        oldDocument = new TextDocumentDomainObject();
        oldDocument.setId( 1001 );
        textDocument = new TextDocumentDomainObject();
        textDocument.setId( 1002 );
        database = new MockDatabase();
        documentMapper = new DocumentMapper( null, database, null, new DocumentPermissionSetMapper( database ), new TestDocumentMapper.MockDocumentIndex(), null );
    }

    public void testUpdateDocumentRolePermissionsWithNoPermissions() throws Exception {
        textDocument.setPermissionSetIdForRole( testRole, DocumentPermissionSetDomainObject.TYPE_ID__READ );
        documentMapper.updateDocumentRolePermissions( textDocument, user, oldDocument );
        assertEquals( 0, database.getSqlCallCount() );
    }

    public void testUpdateDocumentRolePermissionsWithRestricted1Permission() throws Exception {
        oldDocument.setPermissionSetIdForRole( userRole, DocumentPermissionSetDomainObject.TYPE_ID__RESTRICTED_1 );
        textDocument.setRolesMappedToPermissionSetIds( oldDocument.getRolesMappedToPermissionSetIds() );
        textDocument.setPermissionSetIdForRole( testRole, DocumentPermissionSetDomainObject.TYPE_ID__READ );
        DocumentPermissionSetDomainObject permissionSetForRestrictedOne = new DocumentPermissionSetDomainObject( 1 );
        oldDocument.setPermissionSetForRestrictedOne( permissionSetForRestrictedOne );

        permissionSetForRestrictedOne.setEditPermissions( false );
        documentMapper.updateDocumentRolePermissions( textDocument, user, oldDocument );
        assertEquals( 0, database.getSqlCallCount() );

        permissionSetForRestrictedOne.setEditPermissions( true );
        documentMapper.updateDocumentRolePermissions( textDocument, user, oldDocument );
        assertEquals( 2, database.getSqlCallCount() );

        textDocument.setPermissionSetIdForRole( testRole, DocumentPermissionSetDomainObject.TYPE_ID__RESTRICTED_1 );
        documentMapper.updateDocumentRolePermissions( textDocument, user, oldDocument );
        database.assertCalled( new MockDatabase.ProcedureSqlCallPredicate( DocumentMapper.SPROC_SET_PERMISSION_SET_ID_FOR_ROLE_ON_DOCUMENT ) );
    }

    public void testUpdateDocumentRolePermissionsWithFullPermission() throws Exception {
        oldDocument.setPermissionSetIdForRole( userRole, DocumentPermissionSetDomainObject.TYPE_ID__FULL );
        textDocument.setPermissionSetIdForRole( testRole, DocumentPermissionSetDomainObject.TYPE_ID__READ );
        textDocument.setRolesMappedToPermissionSetIds( oldDocument.getRolesMappedToPermissionSetIds() );
        documentMapper.updateDocumentRolePermissions( textDocument, user, oldDocument );
        assertEquals( 1, database.getSqlCallCount() );
    }

    public void testUpdateDocumentRolePermissionsRemovesPermission() {
        oldDocument.setPermissionSetIdForRole( userRole, DocumentPermissionSetDomainObject.TYPE_ID__FULL );
        documentMapper.updateDocumentRolePermissions( textDocument, user, oldDocument );
        database.assertNotCalled( new MockDatabase.EqualsWithParametersSqlCallPredicate( DocumentMapper.SPROC_SET_PERMISSION_SET_ID_FOR_ROLE_ON_DOCUMENT,
                                                                                         new String[]{
                                                                                             "" + userRole.getId(),
                                                                                             "" + textDocument.getId(),
                                                                                             "" + DocumentPermissionSetDomainObject.TYPE_ID__FULL} ) );
        database.assertCalled( new MockDatabase.EqualsWithParametersSqlCallPredicate( DocumentMapper.SPROC_SET_PERMISSION_SET_ID_FOR_ROLE_ON_DOCUMENT,
                                                                                      new String[]{
                                                                                          "" + userRole.getId(),
                                                                                          "" + textDocument.getId(),
                                                                                          "" + DocumentPermissionSetDomainObject.TYPE_ID__NONE} ) );
        assertEquals( 1, database.getSqlCallCount() );
    }

    public void testUpdateDocumentRolePermissionsAllowsNullOldDocument() throws Exception {
        documentMapper.updateDocumentRolePermissions( textDocument, user, null );
    }

    public void testSaveNewBrowserDocument() throws Exception {
        BrowserDocumentDomainObject browserDocument = new BrowserDocumentDomainObject();
        browserDocument.setPermissionSetIdForRole( userRole, DocumentPermissionSetDomainObject.TYPE_ID__FULL );
        browserDocument.setBrowserDocumentId( BrowserDocumentDomainObject.Browser.DEFAULT, 1001 );
        database.addExpectedSqlCall( new MockDatabase.InsertIntoTableSqlCallPredicate( "meta" ), "1002" );
        documentMapper.saveNewDocument( browserDocument, user );
        database.verifyExpectedSqlCalls();
        database.assertCallCount( 1, new MockDatabase.InsertIntoTableSqlCallPredicate( "browser_docs" ));
        assertEquals( 1002, browserDocument.getId() ) ;
    }

    public class MockDocumentIndex implements DocumentIndex {

        public void indexDocument( DocumentDomainObject document ) throws IndexException {
        }

        public void removeDocument( DocumentDomainObject document ) throws IndexException {
        }

        public DocumentDomainObject[] search( Query query, UserDomainObject searchingUser ) throws IndexException {
            return new DocumentDomainObject[0];
        }
    }
}