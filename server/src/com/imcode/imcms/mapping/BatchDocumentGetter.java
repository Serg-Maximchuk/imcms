package com.imcode.imcms.mapping;

import imcode.server.document.AbstractDocumentGetter;
import imcode.server.document.DocumentGetter;
import imcode.server.document.DocumentDomainObject;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;

public class BatchDocumentGetter extends AbstractDocumentGetter {

    private final Collection documentIds;
    private final Map documentsMap;
    private DocumentGetter documentGetter;

    BatchDocumentGetter(Collection documentIds, Map documents, DocumentGetter documentGetter) {
        this.documentIds = documentIds;
        this.documentsMap = documents;
        this.documentGetter = documentGetter;
    }

    public DocumentDomainObject getDocument(Integer documentId) {
        if (documentsMap.isEmpty()) {
            List documents = documentGetter.getDocuments(documentIds);
            for ( Iterator iterator = documents.iterator(); iterator.hasNext(); ) {
                DocumentDomainObject document = (DocumentDomainObject) iterator.next();
                this.documentsMap.put(new Integer(document.getId()), document) ;
            }
        }
        return (DocumentDomainObject) documentsMap.get(documentId) ;
    }
}
