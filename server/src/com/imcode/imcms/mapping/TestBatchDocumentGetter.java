package com.imcode.imcms.mapping;

import junit.framework.*;
import com.imcode.imcms.mapping.BatchDocumentGetter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import imcode.server.document.DocumentDomainObject;
import imcode.server.document.textdocument.TextDocumentDomainObject;

public class TestBatchDocumentGetter extends TestCase {

    public void testGetDocument() throws Exception {
        List documentIds = Arrays.asList(new Integer[] {
            new Integer(1001),
            new Integer(1002)
        });
        Map documents = new HashMap() ;
        MapDocumentGetter documentGetter = new MapDocumentGetter(new DocumentDomainObject[] {
                new TextDocumentDomainObject(1001),
                new TextDocumentDomainObject(1002),
        });
        BatchDocumentGetter batchDocumentGetter = new BatchDocumentGetter(documentIds, documents, documentGetter);
        batchDocumentGetter.getDocument(new Integer(1001)) ;
        assertTrue(documents.keySet().containsAll(documentIds)) ;
    }
}