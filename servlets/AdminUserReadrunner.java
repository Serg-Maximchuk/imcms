import javax.servlet.http.* ;
import javax.servlet.* ;

import java.io.* ;
import java.util.* ;
import java.text.* ;

import imcode.server.* ;
import imcode.util.* ;

import imcode.readrunner.* ;

public class AdminUserReadrunner extends HttpServlet {

    private final static String HTML_RESPONSE_USER = "readrunner/adminreadrunneruser_user.html" ;
    private final static String HTML_RESPONSE_ADMIN = "readrunner/adminreadrunneruser.html" ;

    public void doGet (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

	IMCServiceInterface imcref = IMCServiceRMI.getIMCServiceInterface(req) ;
	User user = null ;

	if( null == (user = Check.userLoggedOn(req,res,imcref.getStartUrl())) ) {
	    // User is not logged on
	    return ;
	}

	HttpSession session = req.getSession( false );
	String userToChangeId = (String)session.getAttribute("userToChange");
	User userToChange = null;
	if ( userToChangeId != null ) {
	    userToChange = imcref.getUserById(Integer.parseInt( userToChangeId ) ) ;
	}

	ReadrunnerUserData rrUserData = (ReadrunnerUserData)session.getAttribute("tempRRUserData") ;
	if (null == rrUserData) {
	    rrUserData = imcref.getReadrunnerUserData(userToChange) ;
	}

	displayPage(imcref,user,userToChange, rrUserData, res) ;

    }


    public void doPost(HttpServletRequest req,HttpServletResponse res)
	throws ServletException, IOException {

	IMCServiceInterface imcref = IMCServiceRMI.getIMCServiceInterface(req) ;
	User user = null ;

	HttpSession session = req.getSession( false );

	if( null == (user = Check.userLoggedOn(req,res,imcref.getStartUrl())) ) {
	    // User is not logged on
	    return ;
	}

	String userToChangeId = req.getParameter("user_id");

	User userToChange = null;

	if ( !("").equals(userToChangeId) ){
	    userToChange = imcref.getUserById(Integer.parseInt(userToChangeId) ) ;
	}

	if (null != req.getParameter("cancel")) {
	    //	String goback = session.getAttribute("go_back");

	    if ( null != userToChange ){
		res.sendRedirect("AdminUserProps?CHANGE_USER=true") ;
	    }else{
		res.sendRedirect("AdminUserProps?ADD_USER=true") ;
	    }

	    return ;
	}

	ReadrunnerUserData rrUserData = (ReadrunnerUserData)session.getAttribute("tempRRUserData") ;
	if (null == rrUserData) {
	    rrUserData = imcref.getReadrunnerUserData(userToChange) ;
	}

	/*
	  God-fucking-damn-it, does java suck...
	*/

	boolean badData = false ;
	try {
	    rrUserData.setUses ( intFromString(req.getParameter("uses")) ) ;
	} catch (NumberFormatException nfe) {
	    badData = true ;
	}

	try {
	    rrUserData.setMaxUses ( intFromString(req.getParameter("max_uses")) ) ;
	} catch (NumberFormatException nfe) {
	    badData = true ;
	}

	try {
	    rrUserData.setMaxUsesWarningThreshold ( intFromString(req.getParameter("max_uses_warning_threshold")) ) ;
	} catch (NumberFormatException nfe) {
	    badData = true ;
	}

	try {
	    String expiryDate = req.getParameter("expiry_date") ;
	    rrUserData.setExpiryDate ( null == expiryDate || "".equals(expiryDate.trim()) ? null : new SimpleDateFormat("yyyy-MM-dd").parse(expiryDate) ) ;
	} catch (ParseException pe) {
	    badData = true ;
	}

	try {
	    rrUserData.setExpiryDateWarningThreshold ( intFromString(req.getParameter("expiry_date_warning_threshold")) ) ;
	} catch (NumberFormatException nfe) {
	    badData = true ;
	}

	if (true == badData) {

	    displayPage(imcref, user, userToChange, rrUserData, res) ;
	    return ;

	} else {

	    rrUserData.setExpiryDateWarningSent	     ( false ) ; // Reset expiry-date-warning-sent-flag;

	    session.setAttribute("tempRRUserData", rrUserData);
	    if ( null != userToChange ){
		res.sendRedirect("AdminUserProps?CHANGE_USER=true") ;
	    }else{
		res.sendRedirect("AdminUserProps?ADD_USER=true") ;
	    }
	}
    }


    private void displayPage(IMCServiceInterface imcref, User user, User userToChange, ReadrunnerUserData rrUserData, HttpServletResponse res)
	throws IOException {

	// check if user is a Useradmin, adminRole = 2
	boolean isUseradmin = imcref.checkUserAdminrole ( user.getUserId(), IMCConstants.ROLE_USERADMIN );

	// check if user is a Superadmin, adminRole = 1
	boolean isSuperadmin = imcref.checkUserAdminrole ( user.getUserId(), IMCConstants.ROLE_SUPERADMIN );

	String userToChangeId = "";

	if ( userToChange != null ) {
	    userToChangeId = "" + userToChange.getUserId();
	}

	if (null == rrUserData ) {
	    rrUserData = new ReadrunnerUserData() ;
	}

	String expiryDateString =
	    null != rrUserData.getExpiryDate()
	    ? new SimpleDateFormat("yyyy-MM-dd").format(rrUserData.getExpiryDate())
	    : "" ;

	ArrayList parseList = new ArrayList() ;

	parseList.add("#user_id#") ;                       parseList.add(userToChangeId) ;
	parseList.add("#uses#") ;                          parseList.add(""+rrUserData.getUses()) ;
	parseList.add("#max_uses#") ;                      parseList.add(0 == rrUserData.getMaxUses() ? "" : ""+rrUserData.getMaxUses()) ;
	parseList.add("#max_uses_warning_threshold#") ;    parseList.add(0 == rrUserData.getMaxUsesWarningThreshold() ? "" : ""+rrUserData.getMaxUsesWarningThreshold()) ;
	parseList.add("#expiry_date#") ;                   parseList.add(expiryDateString) ;
	parseList.add("#expiry_date_warning_threshold#") ; parseList.add(0 == rrUserData.getExpiryDateWarningThreshold() ? "" : ""+rrUserData.getExpiryDateWarningThreshold()) ;

	res.setContentType("text/html") ;
	Writer out = res.getWriter() ;

	//Useradmin is not allowed to change his own readrunner values
	if (isUseradmin && (null == userToChange) ||  // useradmin is going to add a new user
	    (isUseradmin && user.getUserId() != userToChange.getUserId() ) || // or is going to change a user
	    isSuperadmin){   // or Superadmin
	    out.write(imcref.parseDoc(parseList, HTML_RESPONSE_ADMIN, user.getLangPrefix())) ;
	}else{
	    out.write(imcref.parseDoc(parseList, HTML_RESPONSE_USER, user.getLangPrefix())) ;
	}
    }

    private int intFromString(String str) throws NumberFormatException {
	return null == str || "".equals(str.trim()) ? 0 : Integer.parseInt(str) ;
    }

}
