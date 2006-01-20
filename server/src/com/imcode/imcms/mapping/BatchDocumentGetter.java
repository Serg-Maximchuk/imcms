package com.imcode.imcms.mapping;

import imcode.server.document.AbstractDocumentGetter;
import imcode.server.document.DocumentGetter;
import imcode.server.document.DocumentDomainObject;

import java.util.*;

public class BatchDocumentGetter extends AbstractDocumentGetter {

    private final Collection documentIds;
    private Map documentsMap ;
    private DocumentGetter documentGetter;

    BatchDocumentGetter(Collection documentIds, DocumentGetter documentGetter) {
        this.documentIds = documentIds;
        this.documentGetter = documentGetter;
    }

    public DocumentDomainObject getDocument(Integer documentId) {
        if (null == documentsMap) {
            documentsMap = new HashMap();
            List documents = documentGetter.getDocuments(documentIds);
            for ( Iterator iterator = documents.iterator(); iterator.hasNext(); ) {
                DocumentDomainObject document = (DocumentDomainObject) iterator.next();
                documentsMap.put(new Integer(document.getId()), document) ;
            }
        }
        return (DocumentDomainObject) documentsMap.get(documentId) ;
    }
}
