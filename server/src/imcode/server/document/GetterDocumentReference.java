package imcode.server.document;

public class GetterDocumentReference extends DocumentReference {

    private final DocumentGetter documentGetter;
    private DocumentDomainObject document ;
    
    public GetterDocumentReference( int documentId, DocumentGetter documentGetter ) {
        super(documentId) ;
        this.documentGetter = documentGetter;
    }

    public DocumentDomainObject getDocument() {
        if (null == document) {
            document = documentGetter.getDocument( new Integer(getDocumentId()) ) ;
        }
        return document;
    }

}
