package imcode.server.document.index;

import imcode.server.document.DocumentDomainObject;
import imcode.server.user.UserDomainObject;
import org.apache.lucene.search.Query;

public interface DocumentIndex {

    String FIELD__DOC_TYPE_ID = "doc_type_id";
    String FIELD__IMAGE_LINK_URL = "image_link_url";
    String FIELD__NONSTRIPPED_TEXT = "nonstripped_text";
    String FIELD__TEXT = "text";
    String FIELD__KEYWORD = "keyword";
    String FIELD__SECTION = "section";
    String FIELD__ACTIVATED_DATETIME = "activated_datetime";
    String FIELD__ARCHIVED_DATETIME = "archived_datetime";
    String FIELD__CATEGORY_ID = "category_id";
    String FIELD__CREATED_DATETIME = "created_datetime";
    String FIELD__META_HEADLINE = "meta_headline";
    String FIELD__META_ID = "meta_id";
    String FIELD__META_TEXT = "meta_text";
    String FIELD__MODIFIED_DATETIME = "modified_datetime";
    String FIELD__PARENT_ID = "parent_id";
    String FIELD__PARENT_MENU_ID = "parent_menu_id";
    String FIELD__PUBLICATION_END_DATETIME = "publication_end_datetime";
    String FIELD__PUBLICATION_START_DATETIME = "publication_start_datetime";
    String FIELD__STATUS = "status";
    String FIELD__PHASE = "phase" ;
    String FIELD__MIME_TYPE = "mime_type";
    String FIELD__CREATOR_ID = "creator_id";
    String FIELD__PUBLISHER_ID = "publisher_id";

    void indexDocument( DocumentDomainObject document ) throws IndexException ;

    void removeDocument( DocumentDomainObject document ) throws IndexException;

    DocumentDomainObject[] search( Query query, UserDomainObject searchingUser ) throws IndexException;

    void rebuild() throws IndexException;
}