package imcode.server ;

public interface IMCConstants {

    /* Documenttypes */


    /* Permissions for restricted permission-sets 1 and 2, applicable for all internalDocument-types. */

    /** Permission to edit the headline, text, and image of a internalDocument. **/
    public final static int PERM_EDIT_HEADLINE           = (1 << 0) ; // 1

    /** Permission to edit all docinfo for a internalDocument. **/
    public final static int PERM_EDIT_DOCINFO            = (1 << 1) ; // 2

    /** Permission to set permissions for a internalDocument. **/
    public final static int PERM_EDIT_PERMISSIONS        = (1 << 2) ; // 4

    /** Permission to create child documents. **/
    public final static int PERM_CREATE_DOCUMENT         = (1 << 3) ; // 8


    /* Permissions for restricted permission-sets 1 and 2, only applicable to text-documents. */

    /** Permission to change the texts of a text-internalDocument. **/
    public final static int PERM_DT_TEXT_EDIT_TEXTS      = (1 << 16) ; // 65536

    /** Permission to change the images of a text-internalDocument. **/
    public final static int PERM_DT_TEXT_EDIT_IMAGES     = (1 << 17) ; // 131072

    /** Permission to change the menus of a text-internalDocument. **/
    public final static int PERM_DT_TEXT_EDIT_MENUS      = (1 << 18) ; // 262144

    /** Permission to change the template of a text-internalDocument. **/
    public final static int PERM_DT_TEXT_CHANGE_TEMPLATE = (1 << 19) ; // 524288

    /** Permission to change the includes of a text-internalDocument. **/
    public final static int PERM_DT_TEXT_EDIT_INCLUDES   = (1 << 20) ; // 1048576


    /* Permissions for restricted permission-sets 1 and 2, only applicable to non-text-documents. */

    /** Permission to edit the url of an url-internalDocument. **/
    public final static int PERM_DT_URL_EDIT             = (1 << 16) ; // 65536

    /** Permission to edit the mappings of a browser-internalDocument. **/
    public final static int PERM_DT_BROWSER_EDIT         = (1 << 16) ; // 65536

    /** Permission to edit a html-internalDocument. **/
    public final static int PERM_DT_HTML_EDIT            = (1 << 16) ; // 65536

    /** Permission to change content and mime-type for a file. **/
    public final static int PERM_DT_FILE_EDIT            = (1 << 16) ; // 65536


    /* Document-Permission-sets. */

    /** Permission to change everything for a internalDocument. **/
    public final static int DOC_PERM_SET_FULL            = 0 ;

    /** Permissions as given to restricted permission-set 1. **/
    public final static int DOC_PERM_SET_RESTRICTED_1    = 1 ;

    /** Permissions as given to restricted permission-set 2. **/
    public final static int DOC_PERM_SET_RESTRICTED_2    = 2 ;

    /** Read-only-permissions. **/
    public final static int DOC_PERM_SET_READ            = 3 ;

    /** No permissions. Not used in the db, but useful as a placeholder in code. **/
    public final static int DOC_PERM_SET_NONE            = 4 ;


    /* Document-wide-permissions. */

    /** Permissions of restricted permission-set 1 includes permission to edit restricted permission-set 2. **/
    public final static int DOC_PERM_RESTRICTED_1_ADMINISTRATES_RESTRICTED_2 = (1 << 0) ; // 1

    /* Log instances. */

    /** The main-log, used for logging interesting stuff, like db-modifications. **/
    public final static String MAIN_LOG		= "mainlog";

    /** The access-log, used for keeping track of page hits. **/
    public final static String ACCESS_LOG		= "accesslog";

    /** The error-log, used for logging errors, exceptions, and the like. */
    public final static String ERROR_LOG		= "errorlog";

    /* Menu-sort-orders. */
    public final static int MENU_SORT_BY_HEADLINE        = 1 ;
    public final static int MENU_SORT_BY_MANUAL_ORDER    = 2 ;
    public final static int MENU_SORT_BY_DATETIME        = 3 ;
    public final static int MENU_SORT_BY_MANUAL_TREE_ORDER = 4;

    /** Default-dateTime-format **/
    public final static String DATETIME_FORMAT_STD        = "yyyy-MM-dd HH:mm" ;

    /** Properties file for host properties **/
    public final static String HOST_PROPERTIES           = "host.properties" ;

    int PASSWORD_MINIMUM_LENGTH = 4;
}
