package imcode.server.parser;

import java.io.*;
import java.util.*;
import java.net.*;
import javax.servlet.http.Cookie;

import org.apache.oro.text.regex.*;
import imcode.server.*;
import imcode.server.util.DateHelper;
import imcode.server.document.DocumentDomainObject;
import imcode.server.document.TextDocumentTextDomainObject;
import imcode.server.user.UserDomainObject;
import imcode.util.*;

import org.apache.log4j.Category;

public class ImcmsTagSubstitution implements Substitution, IMCConstants {

    private static Pattern HTML_PREBODY_PATTERN = null;
    private static Pattern HTML_POSTBODY_PATTERN = null;
    private static Pattern IMCMS_TAG_ATTRIBUTES_PATTERN = null;

    private static Category log = Category.getInstance( "server" );

    private FileCache fileCache = new FileCache();

    static {
        Perl5Compiler patComp = new Perl5Compiler();

        try {

            IMCMS_TAG_ATTRIBUTES_PATTERN = patComp.compile( "\\s*(\\w+)\\s*=\\s*([\"'])(.*?)\\2", Perl5Compiler.SINGLELINE_MASK | Perl5Compiler.READ_ONLY_MASK );
            HTML_PREBODY_PATTERN = patComp.compile( "^.*?<[Bb][Oo][Dd][Yy].*?>", Perl5Compiler.SINGLELINE_MASK | Perl5Compiler.READ_ONLY_MASK );
            HTML_POSTBODY_PATTERN = patComp.compile( "<\\/[Bb][Oo][Dd][Yy]>.*$", Perl5Compiler.SINGLELINE_MASK | Perl5Compiler.READ_ONLY_MASK );

        } catch ( MalformedPatternException ignored ) {
            // I ignore the exception because i know that these patterns work, and that the exception will never be thrown.
            log.fatal( "Danger, Will Robinson!", ignored );
        }
    }

    private final Substitution NULL_SUBSTITUTION = new StringSubstitution( "" );

    private TextDocumentParser textDocParser;

    private DocumentRequest documentRequest;

    private File templatePath;

    private boolean includeMode;
    private int includeLevel;
    private File includePath;
    private int implicitIncludeNumber = 1;

    private Map textMap;
    private boolean textMode;
    private int implicitTextNumber = 1;

    private Map imageMap;
    private boolean imageMode;
    private int implicitImageNumber = 1;

    private String labelTemplate;

    private DocumentDomainObject document;

    private HashMap included_docs = new HashMap();

    private ParserParameters parserParameters;

    private ReadrunnerFilter readrunnerFilter;

    private IMCServiceInterface serverObject;


    public ImcmsTagSubstitution( TextDocumentParser textdocparser, DocumentRequest documentRequest,
                                 File templatepath,
                                 List included_list, boolean includemode, int includelevel, File includepath,
                                 Map textmap, boolean textmode,
                                 Map imagemap, boolean imagemode,
                                 ParserParameters parserParameters,
                                 ReadrunnerFilter readrunnerFilter ) {
        this.textDocParser = textdocparser;
        this.documentRequest = documentRequest;
        this.document = documentRequest.getDocument();
        this.serverObject = textDocParser.getServerObject();

        this.templatePath = templatepath;

        this.includeMode = includemode;
        this.includeLevel = includelevel;
        this.includePath = includepath;
        for ( Iterator i = included_list.iterator(); i.hasNext(); ) {
            included_docs.put( i.next(), i.next() );
        }

        this.textMap = textmap;
        this.textMode = textmode;

        this.imageMap = imagemap;
        this.imageMode = imagemode;

        this.parserParameters = parserParameters;

        this.readrunnerFilter = readrunnerFilter;

        String langPrefix = documentRequest.getUser().getLangPrefix();
        File label_template_file = new File( templatePath, langPrefix + "/admin/textdoc/label.frag" );
        try {
            this.labelTemplate = fileCache.getCachedFileString( label_template_file );
        } catch ( IOException ex ) {
            log.error( "Failed to load template '" + label_template_file + "'" );
        }
    }

    /**
     Handle a <?imcms:metaid?> tag.

     **/
    public String tagMetaId() {
        return "" + document.getMetaId();
    }

    /**
     Handle a <?imcms:section?> tag.

     **/
    public String tagSection() {
        String section = document.getSection();
        return section == null ? "" : section;
    }

    /**
     Handle a <?imcms:include ...?> tag

     @param attributes The attributes of the include tag
     @param patMat     A pattern matcher.
     **/
    public String tagInclude( Properties attributes, PatternMatcher patMat ) {
        int no = 0;
        String attributevalue;

        //lets get the templates simplename or null if there isn't one
        ParserParameters paramsToParse = new ParserParameters();
        paramsToParse.setTemplate( attributes.getProperty( "template" ) );
        paramsToParse.setParameter( attributes.getProperty( "param" ) );

        if ( null != ( attributevalue = attributes.getProperty( "no" ) ) ) {	    // If we have the attribute no="number"...
            // Set the number of this include-tag
            try {
                no = Integer.parseInt( attributevalue.trim() ); // Then set the number wanted
                implicitIncludeNumber = no + 1;
            } catch ( NumberFormatException ex ) {
                return "<!-- imcms:include no failed: " + ex + " -->";
            }
        } else if ( null != ( attributevalue = attributes.getProperty( "file" ) ) ) { // If we have the attribute file="filename"...
            // Fetch a file from the disk
            try {
                return fileCache.getCachedFileString( new File( includePath, attributevalue ) ); // Get a file from the include directory
            } catch ( IOException ex ) {
                return "<!-- imcms:include file failed: " + ex + " -->";
            }
        } else if ( null != ( attributevalue = attributes.getProperty( "document" ) ) ) { // If we have the attribute document="meta-id"
            try {
                if ( includeLevel > 0 ) {
                    int included_meta_id = Integer.parseInt( attributevalue );
                    // Recursively parse the wanted page.
                    DocumentRequest includedDocumentRequest = new DocumentRequest( documentRequest.getServerObject(),
                                                                                   documentRequest.getRemoteAddr(),
                                                                                   documentRequest.getSessionId(),
                                                                                   documentRequest.getUser(), included_meta_id, document );
                    includedDocumentRequest.setUserAgent( documentRequest.getUserAgent() );
                    includedDocumentRequest.setContextPath( documentRequest.getContextPath() );
                    String documentStr = textDocParser.parsePage( includedDocumentRequest, -1, includeLevel - 1, paramsToParse );
                    documentStr = org.apache.oro.text.regex.Util.substitute( patMat, HTML_PREBODY_PATTERN, NULL_SUBSTITUTION, documentStr );
                    documentStr = org.apache.oro.text.regex.Util.substitute( patMat, HTML_POSTBODY_PATTERN, NULL_SUBSTITUTION, documentStr );
                    return documentStr;
                }
            } catch ( NumberFormatException ex ) {
                return "<!-- imcms:include document failed: " + ex + " -->";
            } catch ( IOException ex ) {
                return "<!-- imcms:include document failed: " + ex + " -->";
            } catch ( RuntimeException ex ) {
                return "<!-- imcms:include document failed: " + ex + " -->";
            }
            return "";
        } else if ( null != ( attributevalue = attributes.getProperty( "url" ) ) ) { // If we have an attribute of the form url="url:url"
            try {
                String urlStr = attributevalue;
                if ( urlStr.startsWith( "/" ) ) {  // lets add hostname if we got a relative path
                    urlStr = "http://" + documentRequest.getHostName() + urlStr;
                }
                URL url = new URL( urlStr );
                String urlProtocol = url.getProtocol();
                if ( "file".equalsIgnoreCase( urlProtocol ) ) { // Make sure we don't have to defend against file://urls...
                    return "<!-- imcms:include url failed: file-url not allowed -->";
                }
                String sessionId = documentRequest.getSessionId();
                URLConnection urlConnection = url.openConnection();
                urlConnection.setRequestProperty( "User-Agent", documentRequest.getUserAgent() );
                if ( null != attributes.getProperty( "sendsessionid" ) ) {
                    urlConnection.addRequestProperty( "Cookie", "JSESSIONID=" + sessionId );
                }
                if ( null != attributes.getProperty( "sendcookies" ) ) {
                    Cookie[] requestCookies = documentRequest.getCookies();
                    for ( int i = 0; requestCookies != null && i < requestCookies.length; ++i ) {
                        Cookie theCookie = requestCookies[i];
                        if ( !"JSESSIONID".equals( theCookie.getName() ) ) {
                            urlConnection.addRequestProperty( "Cookie", theCookie.getName() + "=" + theCookie.getValue() );
                        }
                    }
                }
                if ( null != attributes.getProperty( "sendmetaid" ) ) {
                    urlConnection.setRequestProperty( "X-Meta-Id", "" + document.getMetaId() );
                }

                InputStreamReader urlInput = new InputStreamReader( urlConnection.getInputStream() );
                int charsRead = -1;
                final int URL_BUFFER_LEN = 16384;
                char[] buffer = new char[URL_BUFFER_LEN];
                StringBuffer urlResult = new StringBuffer();
                while ( -1 != ( charsRead = urlInput.read( buffer, 0, URL_BUFFER_LEN ) ) ) {
                    urlResult.append( buffer, 0, charsRead );
                }
                return urlResult.toString();
            } catch ( MalformedURLException ex ) {
                return "<!-- imcms:include url failed: " + ex + " -->";
            } catch ( IOException ex ) {
                return "<!-- imcms:include url failed: " + ex + " -->";
            } catch ( RuntimeException ex ) {
                return "<!-- imcms:include url failed: " + ex + " -->";
            }
        } else { // If we have none of the attributes no, file, url, or document
            no = implicitIncludeNumber++; // Implicitly use the next number.
        }
        try {
            if ( includeMode ) {
                String included_meta_id_str = (String)included_docs.get( String.valueOf( no ) );
                String langPrefix = documentRequest.getUser().getLangPrefix();
                return imcode.util.Parser.parseDoc( fileCache.getCachedFileString( new File( templatePath, langPrefix + "/admin/change_include.html" ) ),
                                                    new String[]{
                                                        "#meta_id#", String.valueOf( document.getMetaId() ),
                                                        "#include_id#", String.valueOf( no ),
                                                        "#include_meta_id#", included_meta_id_str == null ? "" : included_meta_id_str
                                                    }
                );
            } else if ( includeLevel > 0 ) {
                int included_meta_id = Integer.parseInt( (String)included_docs.get( String.valueOf( no ) ) );
                DocumentRequest includedDocumentRequest = new DocumentRequest( documentRequest.getServerObject(),
                                                                               documentRequest.getRemoteAddr(),
                                                                               documentRequest.getSessionId(),
                                                                               documentRequest.getUser(), included_meta_id, document );
                includedDocumentRequest.setUserAgent( documentRequest.getUserAgent() );
                includedDocumentRequest.setContextPath( documentRequest.getContextPath() );
                String documentStr = textDocParser.parsePage( includedDocumentRequest, -1, includeLevel - 1, paramsToParse );
                ;
                documentStr = org.apache.oro.text.regex.Util.substitute( patMat, HTML_PREBODY_PATTERN, NULL_SUBSTITUTION, documentStr );
                documentStr = org.apache.oro.text.regex.Util.substitute( patMat, HTML_POSTBODY_PATTERN, NULL_SUBSTITUTION, documentStr );
                return documentStr;
            } else {
                return "<!-- imcms:include failed: max include-level reached. -->";
            }
        } catch ( IOException ex ) {
            return "<!-- imcms:include failed: " + ex + " -->";
        } catch ( NumberFormatException ex ) {
            // There was no such include in the db.
            return "";
        }
    }

    /**
     Handle a <?imcms:text ...?> tag

     @param attributes The attributes of the text tag
     @param patMat     A pattern matcher.

     attributes:
     - no	( int )   text number in document
     - label ( String ) lable to show in write mode
     - mode  ( read | write )
     - filter ( String )
     - type   (String)

     Supported text_types is:

     pollquestion-n	#where n represent the quest�on number in this document

     pollanswer-n-m		#where n represent the quest�on number in this document
     and m represent the answer number in question number n

     pollpointanswer-n-m     #where n represent the quest�on number in this document
     and m represent the answer number in question number n

     pollparameter-popup_frequency
     pollparameter-cookie
     pollparameter-hideresults
     pollparameter-confirmation_text
     pollparameter-email_recipients
     pollparameter-email_from
     pollparameter-email_subject
     pollparameter-result_template     #template to use when return the result
     pollparameter-name
     pollparameter-description

     **/
    public String tagText( Properties attributes, PatternMatcher patMat ) {
        String mode = attributes.getProperty( "mode" );
        if ( ( mode != null && !"".equals( mode ) )
                && ( ( textMode && "read".startsWith( mode ) ) // With mode="read", we don't want anything in textMode.
                || ( !textMode && "write".startsWith( mode ) )// With mode="write", we don't want anything unless we're in textMode.
                ) ) {
            return "";
        }
        // Get the 'no'-attribute of the <?imcms:text no="..."?>-tag
        String noStr = attributes.getProperty( "no" );
        TextDocumentTextDomainObject text = null;
        if ( null != noStr ) {
            noStr = noStr.trim();
            text = (TextDocumentTextDomainObject)textMap.get( noStr );
            implicitTextNumber = Integer.parseInt( noStr ) + 1;
        } else {
            text = (TextDocumentTextDomainObject)textMap.get( noStr = String.valueOf( implicitTextNumber++ ) );
        }
        String result;
        if ( text == null ) {
            result = "";
        } else {
            // Since this is supposed to be a html-view of the db, we'll do some html-escaping.
            result = text.toHtmlString();
        }

        String filter = attributes.getProperty( "filter" );
        if ( null != filter && "readrunner".equalsIgnoreCase( filter ) ) {

            result = readrunnerFilter.filter( result, patMat, parserParameters.getReadrunnerParameters() );
        }

        String type = attributes.getProperty( "type" ); // get text type, ex. pollparameter-xxxx
        if ( type == null ) {
            type = "";
        }


        String finalresult = result;
        if ( textMode ) {
            String label = attributes.getProperty( "label", "" );
            String label_urlparam = "";
            if ( !"".equals( label ) ) {
                org.apache.oro.text.perl.Perl5Util perl5util = new org.apache.oro.text.perl.Perl5Util();
                label_urlparam = perl5util.substitute( "s!<.+?>!!g", label );
                label_urlparam = java.net.URLEncoder.encode( label_urlparam );
                label = imcode.util.Parser.parseDoc( labelTemplate, new String[]{"#label#", label} );
            }
            String[] replace_tags = new String[]{
                "#meta_id#", String.valueOf( document.getMetaId() ),
                "#text_id#", noStr,
                "#text#", finalresult,
                "#label_url#", label_urlparam,
                "#label#", label,
                "#type#", type
            };
            String langPrefix = documentRequest.getUser().getLangPrefix();
            File admin_template_file = new File( templatePath, langPrefix + "/admin/textdoc/admin_text.frag" );
            try {
                finalresult = imcode.util.Parser.parseDoc( fileCache.getCachedFileString( admin_template_file ), replace_tags );
            } catch ( IOException ex ) {
                log.error( "Failed to load template '" + admin_template_file + "'" );
            }
        }

        return finalresult;
    }

    /**
     Handle a <?imcms:image ...?> tag

     @param attributes The attributes of the image tag
     @param patMat     A pattern matcher.
     **/
    public String tagImage( Properties attributes, PatternMatcher patMat ) {
        String mode = attributes.getProperty( "mode" );
        if ( ( mode != null && !"".equals( mode ) )
                && ( ( imageMode && "read".startsWith( mode ) ) // With mode="read", we don't want anything in imageMode.
                || ( !imageMode && "write".startsWith( mode ) )// With mode="write", we don't want anything it not in imageMode.
                ) ) {
            return "";
        }
        // Get the 'no'-attribute of the <?imcms:text no="..."?>-tag
        String noStr = attributes.getProperty( "no" );
        String result = null;
        if ( null != noStr ) {
            noStr = noStr.trim();
            result = (String)imageMap.get( noStr );
            implicitImageNumber = Integer.parseInt( noStr ) + 1;
        } else {
            result = (String)imageMap.get( noStr = String.valueOf( implicitImageNumber++ ) );
        }
        if ( result == null ) {
            result = "";
        }
        String label = attributes.getProperty( "label", "" );
        String label_urlparam = "";
        if ( !"".equals( label ) ) {
            org.apache.oro.text.perl.Perl5Util perl5util = new org.apache.oro.text.perl.Perl5Util();
            label_urlparam = perl5util.substitute( "s!<.+?>!!g", label );
            label_urlparam = java.net.URLEncoder.encode( label_urlparam );
            label = imcode.util.Parser.parseDoc( labelTemplate, new String[]{"#label#", label} );
        }

        String finalresult = result;
        if ( imageMode ) {
            String[] replace_tags = new String[]{
                "#meta_id#", String.valueOf( document.getMetaId() ),
                "#image_id#", noStr,
                "#image#", finalresult,
                "#label_url#", label_urlparam,
                "#label#", label
            };
            File admin_template_file = null;
            String langPrefix = documentRequest.getUser().getLangPrefix();
            if ( "".equals( result ) ) { // no data in the db-field.
                admin_template_file = new File( templatePath, langPrefix + "/admin/textdoc/admin_no_image.frag" );
            } else {               // data in the db-field.
                admin_template_file = new File( templatePath, langPrefix + "/admin/textdoc/admin_image.frag" );
            }

            try {
                finalresult = imcode.util.Parser.parseDoc( fileCache.getCachedFileString( admin_template_file ), replace_tags );
            } catch ( IOException ex ) {
                log.error( "Failed to load template '" + admin_template_file + "'" );
            }
        }

        return finalresult;
    }

    /**
     Handle a <?imcms:datetime ...?> tag

     @param attributes The attributes of the datetime tag.
     format attribute defines a user pattern to use when geting the date.
     type attribute defines what date to get they can bee
     now, created, modified, activated, archived

     **/
    public String tagDatetime( Properties attributes ) {
        String format = attributes.getProperty( "format" ) == null ? DateHelper.DATETIME_FORMAT_STRING : attributes.getProperty( "format" );
        String type = attributes.getProperty( "type" );
        String lang = attributes.getProperty( "lang" );

        Date date = null;

        if ( type != null ) {
            type = type.toLowerCase();
            if ( "now".startsWith( type ) ) {
                date = new Date();
            } else if ( "created".startsWith( type ) ) {
                date = document.getCreatedDateTime();
            } else if ( "modified".startsWith( type ) ) {
                date = document.getModifiedDateTime();
            } else if ( "archived".startsWith( type ) ) {
                date = document.getArchivedDateTime();
            } else if ( "activated".startsWith( type ) ) {
                date = document.getActivatedDateTime();
            } else {
                return "<!-- <?imcms:datetime ... type=\"" + type + "\" is empty, wrong or does not exist! -->";
            }
        } else {
            date = new Date();
        }

        java.text.SimpleDateFormat formatter;
        if ( lang == null ) {
            formatter = new java.text.SimpleDateFormat( format );
        } else {
            formatter = new java.text.SimpleDateFormat( format, new Locale( lang, "" ) );
        }

        try {
            if ( null == date ) {
                return ""; // There was no date of the requested type (activated/archived?)
            } else {
                return formatter.format( date );
            }
        } catch ( IllegalArgumentException ex ) {
            return "<!-- imcms:datetime failed: " + ex.getMessage() + " -->";
        }
    }


    /**
     Handle a <?imcms:user who='...' get='xxxxxxx'?> tag.

     **/
    public String tagUser( Properties attributes ) {

        UserDomainObject user = null ;
        String who = attributes.getProperty( "who" );

        if ( null != who && "creator".equalsIgnoreCase(who)) {
            user = documentRequest.getDocument().getCreator() ;
        } else {
            user = documentRequest.getUser();
        }

        String result = "";
        String get = attributes.getProperty( "get" );

        if ( get != null && !"".equals( get ) ) {
            if ( "name".equalsIgnoreCase( get ) ) {
                result = user.getFullName();
            } else if ( "firstname".equalsIgnoreCase( get ) ) {
                result = user.getFirstName();
            } else if ( "lastname".equalsIgnoreCase( get ) ) {
                result = user.getLastName();
            } else if ( "company".equalsIgnoreCase( get ) ) {
                result = user.getCompany();
            } else if ( "address".equalsIgnoreCase( get ) ) {
                result = user.getAddress();
            } else if ( "zip".equalsIgnoreCase( get ) ) {
                result = user.getZip();
            } else if ( "city".equalsIgnoreCase( get ) ) {
                result = user.getCity();
            } else if ( "workphone".equalsIgnoreCase( get ) ) {
                result = user.getWorkPhone();
            } else if ( "mobilephone".equalsIgnoreCase( get ) ) {
                result = user.getMobilePhone();
            } else if ( "homephone".equalsIgnoreCase( get ) ) {
                result = user.getHomePhone();
            } else if ( "email".equalsIgnoreCase( get ) ) {
                result = user.getEmailAddress();
            }
        }

        return result;
    }


    public void appendSubstitution( StringBuffer sb, MatchResult matres, int sc, PatternMatcherInput originalInput, PatternMatcher patMat, Pattern pat ) {
        String tagname = matres.group( 1 );
        String tagattributes = matres.group( 2 );
        Properties attributes = new Properties();
        PatternMatcherInput pminput = new PatternMatcherInput( tagattributes );
        while ( patMat.contains( pminput, IMCMS_TAG_ATTRIBUTES_PATTERN ) ) {
            MatchResult attribute_matres = patMat.getMatch();
            attributes.setProperty( attribute_matres.group( 1 ), attribute_matres.group( 3 ) );
        }
        String result;

        /* FIXME: This is quickly growing ugly.
           A better solution would be a class per tag (TagHandler's if you will),
           with a known interface, looked up through some HashMap.
           JSP already fixes this with tag-libs. */
        if ( "text".equals( tagname ) ) {
            result = tagText( attributes, patMat );
        } else if ( "image".equals( tagname ) ) {
            result = tagImage( attributes, patMat );
        } else if ( "include".equals( tagname ) ) {
            result = tagInclude( attributes, patMat );
        } else if ( "metaid".equals( tagname ) ) {
            result = tagMetaId();
        } else if ( "datetime".equals( tagname ) ) {
            result = tagDatetime( attributes );
        } else if ( "section".equals( tagname ) ) {
            result = tagSection();
        } else if ( "user".equals( tagname ) ) {
            result = tagUser( attributes );
        } else {
            result = matres.group( 0 );
        }

        /* If result equals something other than the empty string we have to
           handle pre and post attributes */
        if ( !"".equals( result ) ) {
            String tempAtt = null;
            if ( ( tempAtt = attributes.getProperty( "pre" ) ) != null ) {
                result = tempAtt + result;
            }
            if ( ( tempAtt = attributes.getProperty( "post" ) ) != null ) {
                result = result + tempAtt;
            }
        }
        sb.append( result );
    }

}
