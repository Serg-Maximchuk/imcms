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

    public List getDocuments(Collection documentIds) {
        Map documentsMap = new HashMap() ;
        List uncachedDocumentIds = new ArrayList(documentIds.size());
        for ( Iterator iterator = documentIds.iterator(); iterator.hasNext(); ) {
            Integer documentId = (Integer) iterator.next();
            DocumentDomainObject document = (DocumentDomainObject) cache.get(documentId);
            if (null != document) {
                try {
                    documentsMap.put(new Integer(document.getId()), document.clone()) ;
                } catch ( CloneNotSupportedException e ) {
                    throw new ShouldNotBeThrownException(e);
                }
            } else {
                uncachedDocumentIds.add(documentId);
            }
        }
        if (!uncachedDocumentIds.isEmpty()) {
            List uncachedDocuments = documentGetter.getDocuments(uncachedDocumentIds);
            for ( Iterator iterator = uncachedDocuments.iterator(); iterator.hasNext(); ) {
                DocumentDomainObject document = (DocumentDomainObject) iterator.next();
                Integer documentId = new Integer(document.getId());
                documentsMap.put(documentId, document) ;
                cache.put(documentId,document) ;
            }
        }
        List result = new ArrayList(documentsMap.size()) ;
        for ( Iterator iterator = documentIds.iterator(); iterator.hasNext(); ) {
            Integer documentId = (Integer) iterator.next();
            result.add(documentsMap.get(documentId));            
        }
        return result ;
    }
    
}
