package com.imcode.imcms.mapping;

import imcode.server.document.DocumentGetter;
import imcode.server.document.AbstractDocumentGetter;
import imcode.server.document.DocumentDomainObject;
import imcode.util.ShouldNotBeThrownException;

import java.util.*;

public class CachingDocumentGetter extends AbstractDocumentGetter {

    private Map cache;
    private DocumentGetter documentGetter;

    public CachingDocumentGetter(DocumentGetter documentGetter, Map cache) {
        this.cache = cache ;
        this.documentGetter = documentGetter ;
    }

    public DocumentDomainObject getDocument(Integer documentId) {
        DocumentDomainObject document = (DocumentDomainObject) cache.get(documentId) ;
        if (null == document) {
            document = documentGetter.getDocument(documentId) ;
            if (null == document) {
                return null ;
            }
            cache.put(documentId, document) ;
        }
        try {
            return (DocumentDomainObject) document.clone() ;
        } catch ( CloneNotSupportedException e ) {
            throw new ShouldNotBeThrownException(e);
        }
    }

    public List getDocuments(Collection documentIds) {
        return documentGetter.getDocuments(documentIds) ;
    }
    
}
