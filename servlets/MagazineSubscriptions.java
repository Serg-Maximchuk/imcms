import javax.servlet.* ;
import javax.servlet.http.* ;

import java.io.* ;
import java.util.* ;
import java.net.URLEncoder ;

import imcode.server.* ;
import imcode.util.* ;

import org.apache.log4j.Logger ;

public class MagazineSubscriptions extends HttpServlet {

    private final static String SUBSCRIPTIONS_CONFIG = "subscriptions.config" ;

    private final static String MAIL_FORMAT_TEMPLATE = "magazinesubscriptions/mailformat.txt" ;

    private final static int MAGAZINE_SUBSCRIPTION_USER_FLAG_TYPE = 1 ;

    private static Logger log = Logger.getLogger( MagazineSubscriptions.class.getName() ) ;

    public void service (HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

	IMCServiceInterface imcref = IMCServiceRMI.getIMCServiceInterface(req) ;
	String startUrl	= imcref.getStartUrl() ;
	ServletOutputStream out = res.getOutputStream() ;

	String[] flagParameterValues    = emptyIfNull(req.getParameterValues("flag")) ;
	String[] setflagParameterValues = emptyIfNull(req.getParameterValues("setflag")) ;
	/* Where to go next */
	String forwardTo = req.getParameter("next_url") ;

	HttpSession session = req.getSession(true) ;
	User user = (User)session.getAttribute("logon.isDone") ;

	/* Check if user logged on as someone else than "user" */
	if ( null == user || "user".equals(user.getLoginName()) ) {
	    String targetURL = HttpUtils.getRequestURL(req).append('?').append(urlEncodeParameters(flagParameterValues, setflagParameterValues, forwardTo)).toString() ;
	    log.debug("TargetUrl: "+targetURL) ;
	    session.setAttribute("login.target", targetURL) ;
	    String loginUrl =  Prefs.get ( "admin_url",  IMCConstants.HOST_PROPERTIES );
	    res.sendRedirect(loginUrl) ;
	    return ;
	}

	String[] mailTags = {
	    "#user_full_name#", user.getFullName(),
	    "#user_email#",     user.getEmailAddress()
	} ;

	StringBuffer theMail = new StringBuffer(Prefs.get("mail-format", SUBSCRIPTIONS_CONFIG)) ;
	theMail = Parser.parseDoc(theMail, mailTags) ;

	Map allFlags           = imcref.getUserFlags(MAGAZINE_SUBSCRIPTION_USER_FLAG_TYPE) ;
	Map previouslySetFlags = imcref.getUserFlags(user,MAGAZINE_SUBSCRIPTION_USER_FLAG_TYPE) ;

	/* Turn the set flags into a set. */
	Set setFlags = new HashSet(Arrays.asList(setflagParameterValues)) ;

	List flags = Arrays.asList(flagParameterValues) ;

	boolean addedToMail = false ;
	for (Iterator it = flags.iterator(); it.hasNext();) {
	    String currentFlagName = (String)it.next() ;

	    boolean flagSet           = setFlags.contains(currentFlagName) ;
	    boolean flagPreviouslySet = previouslySetFlags.containsKey(currentFlagName) ;

	    /* If flag is set and wasn't previously set, or vice versa */
	    if (flagSet ^ flagPreviouslySet) {
		if (flagSet) {
		    imcref.setUserFlag(user, currentFlagName) ;
		} else {
		    imcref.unsetUserFlag(user, currentFlagName) ;
		}

		Object object = allFlags.get(currentFlagName) ;
		addToMail(theMail, (UserFlag)object, flagSet) ;
		addedToMail = true ;
	    }
	}

	if (addedToMail) {
	    sendMail(theMail) ;
	}

	if (null == forwardTo || "".equals(forwardTo)) {
	    /* or, if there was no "next", back to where we came from */
	    forwardTo = req.getHeader("referer") ;
	}

	/* Forward the request to the given location */
	res.sendRedirect(forwardTo) ;

    }

    /**
       Work around java braindeadness.

       @return an empty array if ary is null, ary otherwise.
    **/
    private String[] emptyIfNull (String[] ary) {
	if (null == ary) {
	    return new String[0] ;
	}
	return ary ;
    }

    /** Build mail **/
    private void addToMail (StringBuffer theMail, UserFlag flag, boolean set) {
	try {
	    String message = Prefs.get( (set ?
					 "mail-begin-subscription-message" :
					 "mail-end-subscription-message"),
					SUBSCRIPTIONS_CONFIG) ;
	    String[] messageTags = {
		"#magazine#", flag.getDescription()
	    } ;
	    message = Parser.parseDoc(message, messageTags) ;

	    theMail.append(message) ;
	} catch (IOException ioex) {
	    log.error("Failed to add to subscription-mail.", ioex) ;
	}
    }

    /** Send mail **/
    private void sendMail (StringBuffer theMail) throws IOException {
	try {
	    String mailFromAddress = Prefs.get("mail-from-address", SUBSCRIPTIONS_CONFIG) ;
	    String mailToAddress   = Prefs.get("mail-to-address",   SUBSCRIPTIONS_CONFIG) ;
	    String mailSubject     = Prefs.get("mail-subject",      SUBSCRIPTIONS_CONFIG) ;

	    String mailServer =  Prefs.get   ( "smtp_server",  IMCConstants.HOST_PROPERTIES );
	    int    mailPort =    Prefs.getInt( "smtp_port",    IMCConstants.HOST_PROPERTIES, 25 );
	    int    mailTimeout = Prefs.getInt( "smtp_timeout", IMCConstants.HOST_PROPERTIES, 10000 );

	    /* Send the mail */
	    SMTP smtp = new SMTP(mailServer,mailPort,mailTimeout) ;
	    smtp.sendMailWait(mailFromAddress, mailToAddress, mailSubject,theMail.toString()) ;
	} catch (IOException ioex) {
	    log.error("Failed to send subscription-mail.", ioex) ;
	}
    }

    private String urlEncodeParameters(String[] flags, String[] setFlags, String next_url) {
	StringBuffer result = new StringBuffer() ;
	for (int i = 0; i < flags.length; ++i) {
	    result.append("flag=").append(URLEncoder.encode(flags[i])).append('&') ;
	}
	for (int i = 0; i < setFlags.length; ++i) {
	    result.append("setflag=").append(URLEncoder.encode(setFlags[i])).append('&') ;
	}
	if (null != next_url) {
	    result.append("next_url=").append(URLEncoder.encode(next_url)) ;
	}
	return result.toString() ;
    }
}
