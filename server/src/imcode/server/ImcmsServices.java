package imcode.server ;

import com.imcode.db.Database;
import com.imcode.imcms.db.ProcedureExecutor;
import com.imcode.imcms.mapping.CategoryMapper;
import com.imcode.imcms.mapping.DocumentMapper;
import imcode.server.document.TemplateMapper;
import imcode.server.parser.ParserParameters;
import imcode.server.user.ImcmsAuthenticatorAndUserAndRoleMapper;
import imcode.server.user.RoleGetter;
import imcode.server.user.UserDomainObject;
import imcode.util.CachingFileLoader;
import imcode.util.LocalizedMessage;
import imcode.util.net.SMTP;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.security.KeyStore;
import java.text.Collator;
import java.util.Date;
import java.util.Properties;

public interface ImcmsServices {

    /** Verify a Internet/Intranet user. Data from any SQL Database. **/
    UserDomainObject verifyUser(String login, String password)
	;

    void parsePage(ParserParameters paramsToParse, Writer out) throws IOException ;

    void incrementSessionCounter();

    // set session counter
    void setSessionCounter(int value)  ;

    // set  session counter date
    void setSessionCounterDate(Date date)  ;

    // set  session counter date
    Date getSessionCounterDate()  ;

    // parsedoc use template
    String getAdminTemplate( String adminTemplateName, UserDomainObject user, java.util.List tagsWithReplacements )  ;

    // parseExternaldoc use template
    String getTemplateFromDirectory( String adminTemplateName, UserDomainObject user, java.util.List variables,
                                                 String directory )
	;

    // get doctype
    int getDocType(int meta_id)
    ;

    SystemData getSystemData()  ;

    void setSystemData(SystemData sd)  ;

    String[][] getAllDocumentTypes(String langPrefixStr)  ;

    int getSessionCounter();

    String getSessionCounterDateAsString();

    void updateMainLog( String logMessage );

    DocumentMapper getDocumentMapper();

    ImcmsAuthenticatorAndUserAndRoleMapper getImcmsAuthenticatorAndUserAndRoleMapper();

    TemplateMapper getTemplateMapper();

    SMTP getSMTP();

    File getIncludePath();

    Collator getDefaultLanguageCollator();

    VelocityEngine getVelocityEngine(UserDomainObject user);

    VelocityContext getVelocityContext( UserDomainObject user );

    Config getConfig();

    KeyStore getKeyStore();

    Database getDatabase();

    CategoryMapper getCategoryMapper();

    LanguageMapper getLanguageMapper();

    CachingFileLoader getFileCache();

    RoleGetter getRoleGetter();

    ProcedureExecutor getProcedureExecutor();

    UserDomainObject verifyUserByIpOrDefault(String remoteAddr);

}
