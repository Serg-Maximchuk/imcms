package com.imcode.imcms.mapping;

import imcode.server.document.DocumentDomainObject;
import imcode.server.document.index.DocumentIndex;
import imcode.server.document.index.DocumentIndexService;
import imcode.server.document.index.DocumentQuery;
import imcode.server.document.index.IndexException;
import imcode.server.user.UserDomainObject;

import java.util.Arrays;
import java.util.List;

public class MockDocumentIndex implements DocumentIndexService {

    private boolean indexDocumentCalled;
    private boolean removeDocumentCalled;

    public void indexDocument(DocumentDomainObject document) throws IndexException {
        this.indexDocumentCalled = true;
    }

    public void removeDocuments(int docId) throws IndexException {
        this.removeDocumentCalled = true;
    }

    public void indexDocuments(int docId) throws IndexException {
        this.indexDocumentCalled = true;
    }

    public void removeDocument(DocumentDomainObject document) throws IndexException {
        this.removeDocumentCalled = true;
    }


    public List search(DocumentQuery query, UserDomainObject searchingUser) throws IndexException {
        return Arrays.asList(new DocumentDomainObject[0]);
    }

    public void rebuild() {
    }

    public boolean isRemoveDocumentCalled() {
        return removeDocumentCalled;
    }

    public boolean isIndexDocumentCalled() {
        return indexDocumentCalled;
    }

    @Override
    public void shutdown() {
    }
}
