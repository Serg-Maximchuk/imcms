package imcode.server ;

import java.io.* ;
import java.util.* ;

import imcode.server.parser.ParserParameters ;
import imcode.server.user.UserDomainObject;
import imcode.server.document.TextDocumentTextDomainObject;

import imcode.readrunner.* ;


/**
 * Interface for the Imcode Net Server.
 */
public interface IMCServiceInterface {

    /** Verify a Internet/Intranet user. Data from any SQL Database. **/
    UserDomainObject verifyUser(String login, String password)
	;

    /** Get a user by user-id **/
    UserDomainObject getUserById(int userId)
	;


    /** Check if a user has a special admin role **/
    public boolean checkUserAdminrole ( int userId, int adminRole )
	;

    /**
       Save a text field
    **/
    void saveText(UserDomainObject user,int meta_id,int txt_no,TextDocumentTextDomainObject text, String text_type)
	;

    /**
       Retrieve a text-field
    **/
    TextDocumentTextDomainObject getText(int meta_id,int txt_no)
	;

    String parsePage(DocumentRequest docReq, int flags, ParserParameters paramsToParse) throws IOException ;

    // Save an image
    void saveImage(int meta_id,UserDomainObject user,int img_no,imcode.server.Image image)
	;

    /**
       Delete a internalDocument
    **/
    void deleteDocAll(int meta_id,UserDomainObject user)
	;

    void addExistingDoc(int meta_id,UserDomainObject user,int existing_meta_id,int doc_menu_no)
	;

    void saveManualSort(int meta_id,UserDomainObject user,java.util.Vector childs, java.util.Vector sort_no)
	;

    /**
       Remove children from a menu
    **/
    void deleteChilds(int meta_id,int menu,UserDomainObject user,String childsThisMenu[])
	;

    // archive childs
    void archiveChilds(int meta_id,UserDomainObject user,String childsThisMenu[])
	;

    /** Copy documents and insert them in a new textdocument and menu **/
    String[] copyDocs( int meta_id, int doc_menu_no,  UserDomainObject user, String[] childsThisMenu, String copyPrefix)  ;

    // Save a url_doc
    void saveUrlDoc(int meta_id,UserDomainObject user,imcode.server.Table doc)
	;

    // Save a new url_doc
    void saveNewUrlDoc(int meta_id,UserDomainObject user,imcode.server.Table doc)
	;

    // List all archived docs
    //    String listArchive(int meta_id,imcode.server.user.User user)
    //;

    // check if url doc
    Table isUrlDoc(int meta_id,UserDomainObject user)
	;

    // Save a new frameset
    void saveNewFrameset(int meta_id,UserDomainObject user,imcode.server.Table doc)
	;

    // Save a frameset
    void saveFrameset(int meta_id,UserDomainObject user,imcode.server.Table doc)
	;

    // check if url doc
    String isFramesetDoc(int meta_id,UserDomainObject user)
	;

    // check if external doc
    ExternalDocType isExternalDoc(int meta_id,UserDomainObject user)
	;

    // remove child from child table
    void removeChild(int meta_id,int parent_meta_id,UserDomainObject user)
	;

    // activate child to child table
    void activateChild(int meta_id,UserDomainObject user)
	;

    // Parse doc replace variables with data
    String  parseDoc(String htmlStr,java.util.Vector variables)
	;

    // Send a sqlquery to the database and return a string array
    String[] sqlQuery(String sqlQuery)
	;

    // Send a sql update query to the database
    void sqlUpdateQuery(String sqlStr)  ;

    // Send a sqlquery to the database and return a string
    String sqlQueryStr(String sqlQuery)
	;

    // Send a procedure to the database and return a string array
    public String[] sqlProcedure(String procedure)
	;

    // Send a procedure to the database and return a string array
    public String[] sqlProcedure(String procedure, String[] params)
	;

    // Send a procedure to the database and return a string array
    public String[] sqlProcedure(String procedure, String[] params, boolean trim)
	;

    // Send a procedure to the database and return a string
    public String sqlProcedureStr(String procedure)
	;

    // Send a procedure to the database and return a string
    public String sqlProcedureStr(String procedure, String[] params)
	;

    // Send a procedure to the database and return a string
    public String sqlProcedureStr(String procedure, String[] params, boolean trim)
	;

    // Send a update procedure to the database
    public int sqlUpdateProcedure(String procedure)
	;

    // Send a update procedure to the database
    public int sqlUpdateProcedure(String procedure, String[] params)
	;

    // Parse doc replace variables with data, uses two vectors
    String  parseDoc(String htmlStr,java.util.Vector variables,java.util.Vector data)
	;

    // get external template folder
    File getExternalTemplateFolder(int meta_id)
	;

    // increment session counter
    int incCounter()  ;

    // get session counter
    int getCounter()  ;

    // set session counter
    int setCounter(int value)  ;

    // set  session counter date
    boolean setCounterDate(String date)  ;

    // set  session counter date
    String getCounterDate()  ;

    // Send a sqlquery to the database and return a string array and metadata
    String[] sqlQueryExt(String sqlQuery)
	;

    // Send a procedure to the database and return a string array
    public String[] sqlProcedureExt(String procedure)
	;

    // Send a sqlquery to the database and return a Hashtable
    public Hashtable sqlQueryHash(String sqlQuery)
	;

    // Send a procedure to the database and return a Hashtable
    public Hashtable sqlProcedureHash(String procedure)
	;

    // parsedoc use template
    public String  parseDoc(java.util.List variables,String admin_template_name,
			    String lang_prefix)  ;

    // parseExternaldoc use template
    public String parseExternalDoc(java.util.Vector variables, String external_template_name, String lang_prefix, String doc_type)
	;

    // parseExternaldoc use template
    public String parseExternalDoc(java.util.Vector variables, String external_template_name, String lang_prefix, String doc_type, String templateSet)
	;

    // get templatehome
    public byte[] getTemplateData(int template_id)
	throws IOException ;

    // get templatehome
    public File getTemplateHome()
	;

    // get url-path to images
    public String getImageUrl()
	;

    // get file-path to images
    public File getImagePath()
	;

    // get starturl
    public String getStartUrl()
	;

    // get language
    public String getLanguage()
	;

    // get doctype
    public int getDocType(int meta_id)
	;

    // checkDocAdminRights
    public boolean checkDocAdminRights(int meta_id, UserDomainObject user)
	;

    //get greatest permission_set
    public int getUserHighestPermissionSet (int meta_id, int user_id)
	;

    // save template to disk
    public  int saveTemplate(String name,String file_name,byte[] data,boolean overwrite,String lang_prefix)
	;

    // get demo template data
    public Object[] getDemoTemplate(int template_id)
	throws IOException ;

    // check if user can view internalDocument
    public boolean checkDocRights(int meta_id, UserDomainObject user)
	;

    public boolean checkDocAdminRights(int meta_id, UserDomainObject user, int permissions)
	;

    public boolean checkDocAdminRightsAny(int meta_id, UserDomainObject user, int permissions)
	;

    // delete template from db/disk
    public void deleteTemplate(int template_id)
	;

    // save demo template
    public int saveDemoTemplate(int template_id,byte [] data, String suffix)
	;

    // save templategroup
    public void saveTemplateGroup(String group_name,UserDomainObject user)
	;

    // delete templategroup
    public void deleteTemplateGroup(int group_id)
	;

    // save templategroup
    public void changeTemplateGroupName(int group_id,String new_name)
	;

    // Send a procedure to the database and return a multistring array
    public String[][] sqlProcedureMulti(String procedure)
	;

    // Send a procedure to the database and return a multistring array
    public String[][] sqlProcedureMulti(String procedure, String[] params)
	;

    // Send a sqlQuery to the database and return a multistring array
    public String[][] sqlQueryMulti(String sqlQuery)
	;

    // get server date
    public Date getCurrentDate()
	;

    // get demotemplates
    public String[] getDemoTemplateList()
	;

    // delete demotemplate
    public int deleteDemoTemplate(int template_id)
	;

    public String getMenuButtons(int meta_id, UserDomainObject user)  ;

    public String getMenuButtons(String meta_id, UserDomainObject user)  ;

    public String getLanguage(String lang_id)  ;

    public SystemData getSystemData()  ;

    public void setSystemData(SystemData sd)  ;

    // Get the information for each selected metaid. Used by existing documents
    // Wow. Wonderful methodname. Indeed. Just beautiful.
    public Hashtable ExistingDocsGetMetaIdInfo( String[] meta_id)   ;

    public String[] getDocumentTypesInList(String langPrefixStr)  ;

    public Hashtable getDocumentTypesInHash(String langPrefixStr)   ;

    public boolean checkUserDocSharePermission(UserDomainObject user, int meta_id)  ;

    public String getInclude(String path) throws IOException ;

    public String getFortune(String path) throws IOException ;

    public String getSearchTemplate(String path) throws IOException ;

    public File getInternalTemplateFolder(int meta_id) ;

    public List getQuoteList(String quoteListName) throws IOException ;

    public void setQuoteList(String quoteListName, List quoteList) throws IOException ;

    public List getPollList(String pollListName) throws IOException ;

    public void setPollList(String pollListName, List pollList) throws IOException ;

    public imcode.server.document.DocumentDomainObject getDocument(int meta_id) ;

    public boolean checkAdminRights(UserDomainObject user) ;
    public void setReadrunnerUserData(UserDomainObject user, ReadrunnerUserData rrUserData) ;

    public ReadrunnerUserData getReadrunnerUserData(UserDomainObject user) ;

    /**
       Retrieve the texts for a internalDocument
       @param meta_id The id of the internalDocument.
       @return A Map (Integer -> TextDocumentTextDomainObject) with all the  texts in the internalDocument.
    **/
    public Map getTexts(int meta_id);


    public int getSessionCounter();

    public String getSessionCounterDate();

    /** Get all possible userflags **/
    public Map getUserFlags() ;
    /** Get all userflags for a single user **/
    public Map getUserFlags(UserDomainObject user) ;
    /** Get all userflags of a single type **/
    public Map getUserFlags(int type) ;
    /** Get all userflags for a single user of a single type **/
    public Map getUserFlags(UserDomainObject user, int type) ;

    public void setUserFlag(UserDomainObject user, String flagName);

    public void unsetUserFlag(UserDomainObject user, String flagName);

    /** Get an interface to the poll handling system **/
    public imcode.util.poll.PollHandlingSystem getPollHandlingSystem();

    /** Get an interface to the shopping order system **/
    public imcode.util.shop.ShoppingOrderSystem getShoppingOrderSystem() ;

    void updateModifiedDatesOnDocumentAndItsParent( int metaId, Date dateTime );

    String[] sqlQuery( String sqlQuery, String[] params );
}
