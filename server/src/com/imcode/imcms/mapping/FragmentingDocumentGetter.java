package com.imcode.imcms.mapping;

import imcode.server.document.AbstractDocumentGetter;
import imcode.server.document.DocumentGetter;
import imcode.util.CompositeList;

import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;

public class FragmentingDocumentGetter extends AbstractDocumentGetter {

    private static final int DOCUMENTS_PER_FRAGMENT = 50;
    private final DocumentGetter documentGetter ;

    public FragmentingDocumentGetter(DocumentGetter documentGetter) {
        this.documentGetter = documentGetter ;
    }

    public List getDocuments(final Collection documentIds) {
        if (documentIds.isEmpty()) {
            return Collections.EMPTY_LIST ;
        }
        List documentIdList = new ArrayList(documentIds) ;
        CompositeList compositeDocumentList = new CompositeList();
        for (int i = 0; i < documentIds.size(); i += DOCUMENTS_PER_FRAGMENT ) {
            int toIndex = Math.min(documentIds.size(), i+DOCUMENTS_PER_FRAGMENT) ;
            List documentIdSubList = documentIdList.subList(i, toIndex);
            List documentList = documentGetter.getDocuments(documentIdSubList);
            compositeDocumentList.addList(documentList);
        }
        return compositeDocumentList ;
    }
}
