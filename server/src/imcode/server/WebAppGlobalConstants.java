package imcode.server;

import java.io.File ;

/*
* Uggly, uggly. But ther's so much static object creation all around the app
* so I realy have no choice.../Hasse
*/

public class WebAppGlobalConstants {
	private final static String CVS_REV="$Revision$" ;
	private final static String CVS_DATE = "$Date$" ;
	private static WebAppGlobalConstants singletonInstance;
	
	/** 
	* This must be called before any other method is called.
	* When the first Servlet is loaded.
	*/
	public static void init( File webAppRealPath ) 
	{
		singletonInstance = new WebAppGlobalConstants( webAppRealPath );
	}

	public static WebAppGlobalConstants getInstance() 
	{
		return singletonInstance;
	}
	
	public File getAbsoluteWebAppPath() 
	{
		return WEB_APP_ABSOLUTE_ROOT;
	}
	
	private File WEB_APP_ABSOLUTE_ROOT;

	private WebAppGlobalConstants( File webAppRealPath ) 
	{
		WEB_APP_ABSOLUTE_ROOT = webAppRealPath;
	}
}
