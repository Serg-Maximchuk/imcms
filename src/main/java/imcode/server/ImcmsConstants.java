package imcode.server;

public interface ImcmsConstants {

    /* Permissions for restricted permission-sets 1 and 2, applicable for all document-types. */

    /**
     * Permission to edit the headline, text, and image of a document. *
     */
    public final static int PERM_EDIT_HEADLINE = (1 << 0); // 1

    /**
     * Permission to edit all docinfo for a document. *
     */
    public final static int PERM_EDIT_DOCINFO = PERM_EDIT_HEADLINE;

    /**
     * Permission to set permissions for a document. *
     */
    public final static int PERM_EDIT_PERMISSIONS = (1 << 2); // 4


    /* Permissions for restricted permission-sets 1 and 2, only applicable to non-text-documents. */

    public final static int PERM_EDIT_DOCUMENT = (1 << 16); // 65536

    /**
     * Permission to edit the url of an url-document. *
     */
    public final static int PERM_EDIT_URL_DOCUMENT = PERM_EDIT_DOCUMENT;

    /**
     * Permission to edit a html-document. *
     */
    public final static int PERM_EDIT_HTML_DOCUMENT = PERM_EDIT_DOCUMENT;

    /**
     * Permission to change content and mime-type for a file. *
     */
    public final static int PERM_EDIT_FILE_DOCUMENT = PERM_EDIT_DOCUMENT;

    /* Permissions for restricted permission-sets 1 and 2, only applicable to text-documents. */

    /**
     * Permission to change the texts of a text-document. *
     */
    public final static int PERM_EDIT_TEXT_DOCUMENT_TEXTS = PERM_EDIT_DOCUMENT;

    /**
     * Permission to change the images of a text-document. *
     */
    public final static int PERM_EDIT_TEXT_DOCUMENT_IMAGES = (1 << 17); // 131072

    /**
     * Permission to change the menus of a text-document. *
     */
    public final static int PERM_EDIT_TEXT_DOCUMENT_MENUS = (1 << 18); // 262144

    /**
     * Permission to change the template of a text-document. *
     */
    public final static int PERM_EDIT_TEXT_DOCUMENT_TEMPLATE = (1 << 19); // 524288

    /**
     * Permission to change the includes of a text-document. *
     */
    public final static int PERM_EDIT_TEXT_DOCUMENT_INCLUDES = (1 << 20); // 1048576

    /**
     * Permission to change content loops of a text-document. *
     */
    public final static int PERM_EDIT_TEXT_DOCUMENT_CONTENT_LOOPS = (1 << 21); //  2097152

    /**
     * Permission to make document version. *
     */
    public final static int PERM_PUBLISH = (1 << 22); //  4194304

    /**
     * Permission set default document version. *
     */
    public final static int SET_DEFAULT_VERSION = (1 << 23); //  8388608

    /* Log instances. */

    /**
     * The access-log, used for keeping track of page hits. *
     */
    public final static String ACCESS_LOG = "com.imcode.imcms.log.access";

    public final static String MAIN_LOG = "com.imcode.imcms.log.main";

    final static int PASSWORD_MINIMUM_LENGTH = 4;

    int DISPATCH_FLAG__DOCINFO_PAGE = PERM_EDIT_HEADLINE;
    int DISPATCH_FLAG__EDIT_HTML_DOCUMENT = PERM_EDIT_HTML_DOCUMENT;
    int DISPATCH_FLAG__EDIT_URL_DOCUMENT = PERM_EDIT_URL_DOCUMENT;
    int DISPATCH_FLAG__EDIT_FILE_DOCUMENT = PERM_EDIT_FILE_DOCUMENT;
    int DISPATCH_FLAG__EDIT_MENU = PERM_EDIT_TEXT_DOCUMENT_MENUS;
    int DISPATCH_FLAG__EDIT_TEXT_DOCUMENT_IMAGES = PERM_EDIT_TEXT_DOCUMENT_IMAGES;
    int DISPATCH_FLAG__EDIT_TEXT_DOCUMENT_TEXTS = PERM_EDIT_HTML_DOCUMENT;
    int DISPATCH_FLAG__EDIT_TEXT_DOCUMENT_LOOPS = PERM_EDIT_TEXT_DOCUMENT_CONTENT_LOOPS;
    int DISPATCH_FLAG__DOCUMENT_PERMISSIONS_PAGE = PERM_EDIT_PERMISSIONS;
    int DISPATCH_FLAG__PUBLISH = PERM_PUBLISH;
    int DISPATCH_FLAG__SET_DEFAULT_VERSION = SET_DEFAULT_VERSION;

    /**
     * Doc's id; 'meta_id' is the legacy identifier used across the project.
     */
    public static final String REQUEST_PARAM__DOC_ID = "meta_id";

    /**
     * Doc's language code.
     */
    public static final String REQUEST_PARAM__DOC_LANGUAGE = "lang";

    /**
     * Doc's version no or an alias.
     * {@link #REQUEST_PARAM_VALUE__DOC_VERSION__ALIAS_DEFAULT} or {@link #REQUEST_PARAM_VALUE__DOC_VERSION__ALIAS_WORKING}.
     */
    public static final String REQUEST_PARAM__DOC_VERSION = "v";

    public static final String REQUEST_PARAM_VALUE__DOC_VERSION__ALIAS_WORKING = "w";

    public static final String REQUEST_PARAM_VALUE__DOC_VERSION__ALIAS_DEFAULT = "d";

    /**
     * Overrides default return URL which is used when a user leaves the editor or an add-on page.
     */
    public static final String REQUEST_PARAM__RETURN_URL = "imcms.return.url";
}
