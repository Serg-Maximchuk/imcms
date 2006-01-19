package imcode.server.document;

import java.util.List;
import java.util.Collection;

public interface DocumentGetter {
    List getDocuments(Collection documentIds);

    DocumentDomainObject getDocument(Integer documentId);
}
