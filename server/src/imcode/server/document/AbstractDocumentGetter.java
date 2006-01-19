package imcode.server.document;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Arrays;

public class AbstractDocumentGetter implements DocumentGetter {

    public List getDocuments(Collection documentIds) {
        List documents = new ArrayList(documentIds.size()) ;
        for ( Iterator iterator = documentIds.iterator(); iterator.hasNext(); ) {
            Integer documentId = (Integer) iterator.next();
            DocumentDomainObject document = getDocument(documentId);
            if (null != document) {
                documents.add(document) ;
            }
        }
        return documents;
    }

    public DocumentDomainObject getDocument(Integer documentId) {
        List documents = getDocuments(Arrays.asList(new Integer[] {documentId} ));
        if (documents.isEmpty()) {
            return null ;
        }
        return (DocumentDomainObject) documents.get(0);
    }
}
