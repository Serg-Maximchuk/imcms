import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import javax.servlet.*;
import javax.servlet.http.*;


import imcode.util.* ;
/**
  Save image data.
  */
public class SaveImage extends HttpServlet {

	/**
	init()
	*/
	public void init( ServletConfig config ) throws ServletException {
		super.init( config ) ;
	}


	/**
	doPost()
	*/
	public void doPost( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException {
		String host 				= req.getHeader("Host") ;
		String imcserver 			= Utility.getDomainPref("adminserver",host) ;
		String start_url        	= Utility.getDomainPref( "start_url",host ) ;
		String servlet_url        	= Utility.getDomainPref( "servlet_url",host ) ;
		String image_url			= Utility.getDomainPref( "image_url",host ) ;

		imcode.server.User user ;
		String htmlStr = "" ;
		String submit_name = "" ;
		String values[] ;
		int img_no = 0 ;
		imcode.server.Image image = new imcode.server.Image( ) ;


		res.setContentType( "text/html" );
		ServletOutputStream out = res.getOutputStream( );

		// get meta_id
		String m_id = req.getParameter( "meta_id" ) ;
		int meta_id = Integer.parseInt( m_id ) ;
//		int parent_meta_id = Integer.parseInt( req.getParameter( "parent_meta_id" ) ) ;

		// get img_no
		String i_no = req.getParameter( "img_no" ) ;
		img_no = Integer.parseInt( i_no ) ;

		// get image_height
		String image_height = req.getParameter( "image_height" ) ;

		// get image_width
		String image_width = req.getParameter( "image_width" ) ;

		// get image_border
		String image_border = req.getParameter( "image_border" ) ;

		// get vertical_space
		String v_space = req.getParameter( "v_space" ) ;

		// get horizonal_space
		String h_space = req.getParameter( "h_space" ) ;


		try {
			image.setImageHeight( Integer.parseInt( image_height ) ) ;
		} catch ( NumberFormatException ex ) {
			image_height = "0" ;
			image.setImageHeight( 0 ) ;
		}

		try {
			image.setImageBorder( Integer.parseInt( image_border ) ) ;
		} catch ( NumberFormatException ex ) {
			image_border = "0" ;
			image.setImageBorder( 0 ) ;
		}

		try {
			image.setImageWidth( Integer.parseInt( image_width ) ) ;
		} catch ( NumberFormatException ex ) {
			image_width = "0" ;
			image.setImageWidth( 0 ) ;
		}

		try {
			image.setVerticalSpace( Integer.parseInt( v_space ) ) ;
		} catch ( NumberFormatException ex ) {
			v_space = "0" ;
			image.setVerticalSpace( 0 ) ;
		}

		try {
			image.setHorizonalSpace( Integer.parseInt( h_space ) ) ;
		} catch ( NumberFormatException ex ) {
			h_space = "0" ;
			image.setHorizonalSpace( 0 ) ;
		}

		// get imageref
		String image_ref = req.getParameter( "imageref" ) ;
		if ( image_ref.length() > 0 && image_ref.indexOf("/")==-1 ) {
				image_ref = image_url+image_ref ;
		}
		image.setImageRef( image_ref ) ;

		// get image_name
		String image_name = req.getParameter( "image_name" ) ;
		image.setImageName( image_name ) ;

		// get image_align
		String image_align = req.getParameter( "image_align" ) ;
		image.setImageAlign( image_align ) ;

		// get alt_text
		String alt_text = req.getParameter( "alt_text" ) ;
		image.setAltText( alt_text ) ;

		// get low_scr
		String low_scr = req.getParameter( "low_scr" ) ;
		image.setLowScr( low_scr ) ;

		// get target
		String target = req.getParameter( "target" ) ;
		image.setTarget( target ) ;


		// get target_name
		String target_name = req.getParameter( "target_name" ) ;
		image.setTargetName( target_name ) ;

		// get image_ref_link
		String imageref_link = req.getParameter( "imageref_link" ) ;
		image.setImageRefLink( imageref_link ) ;


		// redirect data
		String scheme = req.getScheme( );
		String serverName = req.getServerName( );
		int p = req.getServerPort( );
		String port = (p == 80) ? "" : ":" + p;




		// Get the session
		HttpSession session = req.getSession( true );

		// Does the session indicate this user already logged in?
		Object done = session.getValue( "logon.isDone" );  // marker object
		user = (imcode.server.User)done ;

		if( done == null ) {
			// No logon.isDone means he hasn't logged in.
			// Save the request URL as the true target and redirect to the login page.

			res.sendRedirect( scheme + "://" + serverName + port + start_url ) ;
			return ;
		}
		// Check if user has write rights
		if ( !IMCServiceRMI.checkDocAdminRights(imcserver,meta_id,user,131072 ) ) {	// Checking to see if user may edit this
			byte[] tempbytes ;
			tempbytes = AdminDoc.adminDoc(meta_id,meta_id,host,user,req,res) ;
			if ( tempbytes != null ) {
				out.write(tempbytes) ;
			}
			return ;
		}
		user.put("flags",new Integer(131072)) ;

		if( req.getParameter( "cancel" )!=null ) {
//			htmlStr = IMCServiceRMI.interpretTemplate( imcserver,meta_id,user ) ;
			byte[] tempbytes = AdminDoc.adminDoc(meta_id,meta_id,host,user,req,res) ;
			if ( tempbytes != null ) {
				out.write(tempbytes) ;
			}
			return ;

		} else if( req.getParameter( "show_img" )!=null ) {
			Vector vec = new Vector () ;
			vec.add("#imgName#") ;
			vec.add(image_name) ;
			vec.add("#imgRef#") ;
			vec.add(image_ref) ;
			vec.add("#imgWidth#") ;
			vec.add(image_width) ;
			vec.add("#imgHeight#") ;
			vec.add(image_height) ;
			vec.add("#imgBorder#") ;
			vec.add(image_border) ;
			vec.add("#imgVerticalSpace#") ;
			vec.add(v_space) ;
			vec.add("#imgHorizontalSpace#") ;
			vec.add(h_space) ;
			if ( "_top".equals(target) ) {
				vec.add("#target_name#") ;
				vec.add("") ;
				vec.add("#top_checked#") ;
			} else if ( "_self".equals(target) ) {
				vec.add("#target_name#") ;
				vec.add("") ;
				vec.add("#self_checked#") ;
			} else if ( "_blank".equals(target) ) {
				vec.add("#target_name#") ;
				vec.add("") ;
				vec.add("#blank_checked#") ;
			} else if ( "_parent".equals(target) ) {
				vec.add("#target_name#") ;
				vec.add("") ;
				vec.add("#blank_checked#") ;
			} else {
				vec.add("#target_name#") ;
				vec.add(target_name) ;
				vec.add("#other_checked#") ;
			}
			vec.add("selected") ;

			if ( "baseline".equals(image_align) ) {
				vec.add("#baseline_selected#") ;
			} else if ( "top".equals(image_align) ) {
				vec.add("#top_selected#") ;
			} else if ( "middle".equals(image_align) ) {
				vec.add("#middle_selected#") ;
			} else if ( "bottom".equals(image_align) ) {
				vec.add("#bottom_selected#") ;
			} else if ( "texttop".equals(image_align) ) {
				vec.add("#texttop_selected#") ;
			} else if ( "absmiddle".equals(image_align) ) {
				vec.add("#absmiddle_selected#") ;
			} else if ( "absbottom".equals(image_align) ) {
				vec.add("#absbottom_selected#") ;
			} else if ( "left".equals(image_align) ) {
				vec.add("#left_selected#") ;
			} else if ( "right".equals(image_align) ) {
				vec.add("#right_selected#") ;
			} else {
				vec.add("#none_selected#") ;
			}
			vec.add("selected") ;

			vec.add("#imgAltText#") ;
			vec.add(alt_text) ;
			vec.add("#imgLowScr#") ;
			vec.add(low_scr) ;
			vec.add("#imgRefLink#") ;
			vec.add(imageref_link) ;
			vec.add("#getMetaId#") ;
			vec.add(m_id) ;
			vec.add("#img_no#") ;
			vec.add(i_no) ;
			//IMCServiceRMI.saveImage( imcserver,meta_id,user,img_no,image ) ;
			//res.sendRedirect( scheme + "://" + serverName + port + servlet_url + "ChangeImage?meta_id=" + meta_id + "&img=" + img_no );
			String lang_prefix = IMCServiceRMI.sqlQueryStr(imcserver, "select lang_prefix from lang_prefixes where lang_id = "+user.getInt("lang_id")) ;
			htmlStr = IMCServiceRMI.parseDoc(imcserver,vec,"change_img.html", lang_prefix) ;
			out.print(htmlStr) ;
			return ;
		} else if ( req.getParameter("delete") != null ) {
			Vector vec = new Vector () ;
			vec.add("#imgName#") ;
			vec.add("") ;
			vec.add("#imgRef#") ;
			vec.add("") ;
			vec.add("#imgWidth#") ;
			vec.add("0") ;
			vec.add("#imgHeight#") ;
			vec.add("0") ;
			vec.add("#imgBorder#") ;
			vec.add("0") ;
			vec.add("#imgVerticalSpace#") ;
			vec.add("0") ;
			vec.add("#imgHorizontalSpace#") ;
			vec.add("0") ;
			vec.add("#target_name#") ;
			vec.add("") ;
			vec.add("#self_checked#") ;
			vec.add("selected") ;
			vec.add("#top_selected#") ;
			vec.add("selected") ;
			vec.add("#imgAltText#") ;
			vec.add("") ;
			vec.add("#imgLowScr#") ;
			vec.add("") ;
			vec.add("#imgRefLink#") ;
			vec.add("") ;
			vec.add("#getMetaId#") ;
			vec.add(String.valueOf(meta_id)) ;
			vec.add("#img_no#") ;
			vec.add(String.valueOf(img_no)) ;
			String lang_prefix = IMCServiceRMI.sqlQueryStr(imcserver, "select lang_prefix from lang_prefixes where lang_id = "+user.getInt("lang_id")) ;
			htmlStr = IMCServiceRMI.parseDoc(imcserver,vec,"change_img.html", lang_prefix) ;
			out.print(htmlStr) ;
			return ;
		} else {
			IMCServiceRMI.saveImage( imcserver,meta_id,user,img_no,image ) ;

			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd") ;
			Date dt = IMCServiceRMI.getCurrentDate(imcserver) ;

			String sqlStr = "update meta set date_modified = '"+dateformat.format(dt)+"' where meta_id = "+meta_id ;
			IMCServiceRMI.sqlUpdateQuery(imcserver,sqlStr);

			//			htmlStr = IMCServiceRMI.interpretTemplate( imcserver,meta_id,user ) ;
			byte[] tempbytes = AdminDoc.adminDoc(meta_id,meta_id,host,user,req,res) ;
			if ( tempbytes != null ) {
				out.write(tempbytes) ;
			}
			return ;
		}
	}
}
