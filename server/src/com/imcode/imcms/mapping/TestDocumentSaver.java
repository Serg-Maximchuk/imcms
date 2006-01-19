package com.imcode.imcms.mapping;

import com.imcode.db.mock.MockDatabase;
import com.imcode.imcms.api.NoPermissionException;
import com.imcode.imcms.api.SaveException;
import imcode.server.document.*;
import imcode.server.document.textdocument.TextDocumentDomainObject;
import imcode.server.document.textdocument.MenuItemDomainObject;
import imcode.server.document.textdocument.NoPermissionToAddDocumentToMenuException;
import imcode.server.user.RoleId;
import imcode.server.user.UserDomainObject;
import imcode.util.SystemClock;
import junit.framework.TestCase;

import java.util.Collection;
import java.util.Set;

public class TestDocumentSaver extends TestCase {
    private DefaultDocumentMapper documentMapper;
    private DocumentSaver documentSaver;

    protected void setUp() throws Exception {
        super.setUp();
        MockDatabase database = new MockDatabase();
        documentMapper = new DefaultDocumentMapper(null, database) {
            public void invalidateDocument(DocumentDomainObject document) {
            }
        };
        documentMapper.setDocumentPermissionSetMapper(new DocumentPermissionSetMapper(database));
        documentMapper.setClock(new SystemClock()) ;
        documentSaver = new DocumentSaver(documentMapper);
    }

    public void testRemoveOwnPermissionsFromDocument() throws SaveException, NoPermissionException {
        final TextDocumentDomainObject oldDocument = createDocument(1001);
        documentMapper.setDocumentGetter(new AbstractDocumentGetter() {
            public DocumentDomainObject getDocument(Integer documentId) {
                return oldDocument;
            }
        });

        TextDocumentDomainObject document = createDocument(1001);

        try {
            documentSaver.saveDocument(document, oldDocument, new UserDomainObject());
            fail("Expected exception.") ;
        } catch ( NoPermissionToEditDocumentException e) {
        }
        oldDocument.setDocumentPermissionSetTypeForRoleId(RoleId.USERS, DocumentPermissionSetTypeDomainObject.FULL);
        documentSaver.saveDocument(document, oldDocument, new UserDomainObject());
    }

    private TextDocumentDomainObject createDocument(int documentId) {
        TextDocumentDomainObject document = new TextDocumentDomainObject();
        documentMapper.makeDocumentLookNew(document, new UserDomainObject());
        document.setId(documentId);
        document.setTemplateId(1);
        return document;
    }


    public void testDocumentAddedWithoutPermission() {
        TextDocumentDomainObject document1001 = createTextDocument(1001);
        TextDocumentDomainObject document1002 = createTextDocument(1002);
        documentMapper.setDocumentGetter(new MapDocumentGetter(new DocumentDomainObject[] {
                document1001,
                document1002,
        }));
        final TextDocumentDomainObject oldDocument = (TextDocumentDomainObject) documentMapper.getDocument(1001);
        final TextDocumentDomainObject addedDocument = (TextDocumentDomainObject) documentMapper.getDocument(1002);
        UserDomainObject testedUser = new UserDomainObject();
        TextDocumentDomainObject document = (TextDocumentDomainObject) documentMapper.getDocument(1001);
        document.getMenu(1).addMenuItem(new MenuItemDomainObject(new DirectDocumentReference(addedDocument)));

        document1002.setLinkableByOtherUsers(true);
        assertDocumentsAddedWithPermission(document, null, testedUser);

        document1002.setLinkableByOtherUsers(false);
        assertDocumentsAddedWithoutPermission(document, null, testedUser);

        assertDocumentsAddedWithoutPermission(document, oldDocument, testedUser);

        document1002.setLinkableByOtherUsers(true);
        assertDocumentsAddedWithPermission(document, oldDocument, testedUser);

        document1002.setLinkableByOtherUsers(false);
        assertDocumentsAddedWithoutPermission(document, oldDocument, testedUser);

        document1002.setDocumentPermissionSetTypeForRoleId(RoleId.USERS, DocumentPermissionSetTypeDomainObject.FULL);
        assertDocumentsAddedWithPermission(document, oldDocument, testedUser);

        document1002.setDocumentPermissionSetTypeForRoleId(RoleId.USERS, DocumentPermissionSetTypeDomainObject.NONE);
        assertDocumentsAddedWithoutPermission(document, oldDocument, testedUser);

        testedUser.addRoleId(RoleId.SUPERADMIN);
        assertDocumentsAddedWithPermission(document, oldDocument, testedUser);

        testedUser.removeRoleId(RoleId.SUPERADMIN);
        assertDocumentsAddedWithoutPermission(document, oldDocument, testedUser);

        try {
            documentMapper.getDocumentSaver().checkDocumentsAddedWithoutPermission(document, oldDocument, testedUser);
            fail("Expected exception.");
        } catch ( NoPermissionToAddDocumentToMenuException e ) {}
    }

    private void assertDocumentsAddedWithPermission(TextDocumentDomainObject document,
                                                    TextDocumentDomainObject oldDocument, UserDomainObject user) {
        Set documentsAddedWithoutPermission = documentMapper.getDocumentSaver().getDocumentsAddedWithoutPermission(document, oldDocument, user);
        assertEmpty(documentsAddedWithoutPermission);
    }

    private void assertDocumentsAddedWithoutPermission(TextDocumentDomainObject document,
                                                       TextDocumentDomainObject oldDocument, UserDomainObject user) {
        Set documentsAddedWithoutPermission = documentMapper.getDocumentSaver().getDocumentsAddedWithoutPermission(document, oldDocument, user);
        assertNotEmpty(documentsAddedWithoutPermission);
    }

    private void assertEmpty(Collection collection) {
        assertTrue(collection.isEmpty());
    }

    private void assertNotEmpty(Collection collection) {
        assertFalse(collection.isEmpty());
    }

    private TextDocumentDomainObject createTextDocument(int documentId) {
        TextDocumentDomainObject newTextDocument = new TextDocumentDomainObject();
        newTextDocument.setId( documentId );
        return newTextDocument ;
    }

}