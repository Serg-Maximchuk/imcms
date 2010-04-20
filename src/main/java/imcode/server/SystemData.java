package imcode.server ;

/**
   Storage-container for systemdata
 */
public class SystemData implements java.io.Serializable {

    /** The startdocument **/
    private int startDocument ;

    /** The servermaster */
    private String serverMaster ;

    /** The servermaster's address */
    private String serverMasterAddress ;

    /** The webmaster */
    private String webMaster ;

    /** The webmaster's address */
    private String webMasterAddress ;

    /** The systemmessage */
    private String systemMessage;

    /**
     * Default language id.
     * @see com.imcode.imcms.api.I18nLanguage
     */
    private Integer languageId;


    /** Get the startdocument **/
    public int getStartDocument() {
	return startDocument ;
    }

    /** Get the servermaster */
    public String getServerMaster() {
	return serverMaster ;
    }

    /** Get the servermaster's address */
    public String getServerMasterAddress() {
	return serverMasterAddress ;
    }

    /** Get the webmaster */
    public String getWebMaster() {
	return webMaster ;
    }

    /** Get the webmaster's address */
    public String getWebMasterAddress() {
	return webMasterAddress ;
    }

    /** Get the systemmessage */
    public String getSystemMessage() {
	return systemMessage ;
    }


    public void setStartDocument(int startDocument) {
	this.startDocument = startDocument ;
    }

    public void setServerMaster(String serverMaster) {
	this.serverMaster = serverMaster ;
    }

    /** Get the servermaster's address */
    public void setServerMasterAddress(String serverMasterAddress) {
	this.serverMasterAddress = serverMasterAddress ;
    }

    /** Get the webmaster */
    public void setWebMaster(String webMaster) {
	this.webMaster = webMaster ;
    }

    /** Get the webmaster's address */
    public void setWebMasterAddress(String webMasterAddress) {
	this.webMasterAddress = webMasterAddress ;
    }

    /** Get the systemmessage */
    public void setSystemMessage(String systemMessage) {
	this.systemMessage = systemMessage ;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }
}
