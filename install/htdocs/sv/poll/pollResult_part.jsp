<%@ page language="java"
import="java.util.*, java.text.*, imcode.server.*, imcode.util.*, imcode.util.poll.*"
%><%

final String image_url = "@rooturl@/poll/images/blue_animation.gif";

// Get parameters from request
String image_height = request.getParameter("imageHeight") != null ? request.getParameter("imageHeight") : "15" ;
String diagram_width = request.getParameter("diagramWidth") != null ? request.getParameter("diagramWidth") : "350";
String meta_id = request.getHeader("X-Meta-Id");

// Get a reference to IMCServiceInterface //
IMCServiceInterface imcref = IMCServiceRMI.getIMCServiceInterface(request) ;

//Get a PollHandlingSystem
PollHandlingSystem poll = imcref.getPollHandlingSystem();

//Get PollParameters from db 
String[] poll_param;
int poll_id;

if ( meta_id != null ){
	poll_param = poll.getPollParameters( ""+ meta_id ); 
	
	if ( poll_param != null && poll_param.length !=0 ){
	
		poll_id = Integer.parseInt(poll_param[0]);
		
		// Get all questions for this poll
		String[][] allQuestions = poll.getAllPollQuestions(""+poll_id);
		
		if (allQuestions != null && allQuestions.length != 0 ){
			for ( int i = 0; i < allQuestions.length; i++){
			
				int question_no = Integer.parseInt( allQuestions[i][2]);
				int question_id = Integer.parseInt( allQuestions[i][0]);
				int text_no = Integer.parseInt( allQuestions[i][3]);
				String question_text = getText( imcref, Integer.parseInt(meta_id), text_no );
				
				//lets show the result if question textstring not is empty   
				if ( !("").equals(question_text) ){
					out.write( getQuestionResult(imcref, poll, Integer.parseInt(meta_id), question_id, question_no, question_text, Integer.parseInt(diagram_width), image_height, image_url ).toString() );
				}
			}
		}
	}
}

//System.out.println( "");

%>



<%!
private StringBuffer getQuestionResult(IMCServiceInterface imcref, PollHandlingSystem poll, int meta_id, int question_id, int question_no, String question_text, int diagram_width, String image_height, String image_url ){

	
	String[][] allAnswers = poll.getAllPollAnswers(""+ question_id);
	
	StringBuffer oStr = new StringBuffer(); // html result to return
	int total_votes =0; // total votes on one question
	int option_count = 0; // total votes on one option
	String option_text; 
	
	
	if ( allAnswers != null && allAnswers.length != 0 ){
	
		for ( int i= 0; i < allAnswers.length; i++){
			total_votes += Integer.parseInt(allAnswers[i][4]) ;
		}
	
		oStr.append("<!-- Fr�ga " +  question_no + "-->\n" +
		"<tr><td><b>" + question_text + "</b><br><br></td></tr>\n" +
	 "<tr><td>\n" +  
	   "<table border='0' cellspacing='0' cellpadding='0' width='100%'>\n" );
		
		for ( int i = 0; i < allAnswers.length; i++ ){
			
			 int text_no = Integer.parseInt( allAnswers[i][2] );	
			 option_text = getText( imcref, meta_id, text_no );
			 option_count = Integer.parseInt( allAnswers[i][4] );
			 int width = getWidth( total_votes, option_count, diagram_width); // calculate the animation width
			 String vote_percent = total_votes > 0 ? ( option_count * 100 / total_votes ) + "%" : "0%";
		
		 	oStr.append( "<tr><td valign='bottom' width='15%'>" + option_text + "</td>\n" +
			 "<td >&nbsp;&nbsp;<img src='" + image_url + "' height='" +  image_height + "' width='" + width + "' >&nbsp;" + vote_percent + " (" + option_count + ")</td></tr>\n");
		}	
		oStr.append("</table></td></tr>\n" );
	    oStr.append("<tr><td><br>Totalt antal r�ster: " + total_votes + "</td></tr>\n" );
	    oStr.append("<tr><td><hr></td></tr>\n\n");
	}
	return oStr;
}



//Get one text from db 
private String getText( IMCServiceInterface imcref, int meta_id, int text_no ){
	return imcref.getText(meta_id, text_no).getText();					
}

//Calculate the animation width
private int getWidth(int total_votes, int option_count, int diagram_width){
	return total_votes > 0 ? (diagram_width * option_count)/total_votes : 0;
}

%> 










 



