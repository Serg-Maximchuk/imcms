package imcode.server;

import com.imcode.db.Database;
import com.imcode.db.mock.MockDatabase;
import com.imcode.imcms.db.ProcedureExecutor;
import com.imcode.imcms.mapping.CategoryMapper;
import com.imcode.imcms.mapping.DocumentMapper;
import imcode.server.document.TemplateMapper;
import imcode.server.parser.ParserParameters;
import imcode.server.user.ImcmsAuthenticatorAndUserAndRoleMapper;
import imcode.server.user.RoleGetter;
import imcode.server.user.UserDomainObject;
import imcode.util.CachingFileLoader;
import imcode.util.net.SMTP;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.security.KeyStore;
import java.text.Collator;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class MockImcmsServices implements ImcmsServices {

    private ImcmsAuthenticatorAndUserAndRoleMapper imcmsAuthenticatorAndUserAndRoleMapper;

    private Database database = new MockDatabase();
    private KeyStore keyStore;
    private TemplateMapper templateMapper;
    private DocumentMapper documentMapper;
    private CategoryMapper categoryMapper;
    private LanguageMapper languageMapper = new LanguageMapper(null, null);
    private RoleGetter roleGetter;
    private ProcedureExecutor procedureExecutor;
    private Config config = new Config();

    public UserDomainObject verifyUser( String login, String password ) {
        return null;
    }

    public void parsePage(ParserParameters paramsToParse, Writer out) throws IOException {
        
    }

    public void incrementSessionCounter() {

    }

    // set session counter
    public void setSessionCounter( int value ) {

    }

    // set  session counter date
    public void setSessionCounterDate( Date date ) {

    }

    // set  session counter date
    public Date getSessionCounterDate() {
        return null;
    }

    // parsedoc use template
    public String getAdminTemplate( String adminTemplateName, UserDomainObject user, List tagsWithReplacements ) {
        return null;
    }

    // parseExternaldoc use template
    public String getTemplateFromDirectory( String adminTemplateName, UserDomainObject user, List variables,
                                            String directory ) {
        return null;
    }

    // get doctype
    public int getDocType( int meta_id ) {
        return 0;
    }

    public SystemData getSystemData() {
        return null;
    }

    public void setSystemData( SystemData sd ) {

    }

    public String[][] getAllDocumentTypes( String langPrefixStr ) {
        return new String[0][];
    }

    public int getSessionCounter() {
        return 0;
    }

    public String getSessionCounterDateAsString() {
        return null;
    }

    public void updateMainLog( String logMessage ) {

    }

    public DocumentMapper getDocumentMapper() {
        return documentMapper;
    }

    public ImcmsAuthenticatorAndUserAndRoleMapper getImcmsAuthenticatorAndUserAndRoleMapper() {
        return imcmsAuthenticatorAndUserAndRoleMapper ;
    }

    public TemplateMapper getTemplateMapper() {
        return templateMapper;
    }

    public SMTP getSMTP() {
        return null;
    }

    public Properties getLanguageProperties( UserDomainObject user ) {
        return null;
    }

    public File getIncludePath() {
        return null;
    }

    public Collator getDefaultLanguageCollator() {
        return null;
    }

    public VelocityEngine getVelocityEngine( UserDomainObject user ) {
        return null;
    }

    public VelocityContext getVelocityContext( UserDomainObject user ) {
        return null;
    }

    public Config getConfig() {
        return this.config;
    }

    public Database getDatabase() {
        return database ;
    }

    public CategoryMapper getCategoryMapper() {
        return categoryMapper ;
    }

    public LanguageMapper getLanguageMapper() {
        return languageMapper;
    }

    public CachingFileLoader getFileCache() {
        return null ;
    }

    public RoleGetter getRoleGetter() {
        return roleGetter ;
    }

    public ProcedureExecutor getProcedureExecutor() {
        return procedureExecutor;
    }

    public UserDomainObject verifyUserByIpOrDefault(String remoteAddr) {
        return null;
    }

    public Properties getLanguageProperties(String languageIso639_2) {
        return null;
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public void setImcmsAuthenticatorAndUserAndRoleMapper(
            ImcmsAuthenticatorAndUserAndRoleMapper imcmsAuthenticatorAndUserAndRoleMapper ) {
        this.imcmsAuthenticatorAndUserAndRoleMapper = imcmsAuthenticatorAndUserAndRoleMapper;
    }

    public void setDatabase( Database database ) {
        this.database = database;
    }

    public void setKeyStore( KeyStore keyStore ) {
        this.keyStore = keyStore;
    }

    public void setTemplateMapper( TemplateMapper templateMapper ) {
        this.templateMapper = templateMapper;
    }

    public void setDocumentMapper( DocumentMapper documentMapper ) {
        this.documentMapper = documentMapper;
    }

    public void setCategoryMapper(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public void setLanguageMapper(LanguageMapper languageMapper) {
        this.languageMapper = languageMapper;
    }

    public void setRoleGetter(RoleGetter roleGetter) {
        this.roleGetter = roleGetter;
    }

    public void setProcedureExecutor(ProcedureExecutor procedureExecutor) {
        this.procedureExecutor = procedureExecutor;
    }
}
