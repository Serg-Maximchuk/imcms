package imcode.server.document;

public class GetterDocumentReference extends DocumentReference {

    private final DocumentGetter documentGetter;

    public GetterDocumentReference( int documentId, DocumentGetter documentGetter ) {
        super(documentId) ;
        this.documentGetter = documentGetter;
    }

    public DocumentDomainObject getDocument() {
        return documentGetter.getDocument( new Integer(getDocumentId()) ) ;
    }

}
