	// writeLayer - given a layer write text to it.
	var global = 1;
	var section = null;
	var sub = null;
	var preloadFlag = false;
	var willOpen = false;
	var isIE = (navigator.appVersion.indexOf("MSIE") != -1) ? true : false;  
	var isWin = (navigator.appVersion.indexOf("Windows") != -1) ? true : false; 
	var isNs = ((navigator.appVersion.indexOf("Netscape") != -1) || (navigator.appVersion.indexOf("Nav") != -1))? true : false; 
	var isMac = (navigator.appVersion.indexOf("Mac") != -1) ? true : false; 
	var page 
//	window.onerror = handleError;

//		function handleError(message, url, line){
			// ät upp felet bara....
//			return true;
//		}

	
	
	
	// loadContenInLayerOrIframe - opens a document in either an iframe or an layer depending on browser.

	function loadContenInLayerOrIframe(sTargetFrame, sURL ,iTargetWidth) {
	  if(document.layers){
	    document.layers[sTargetFrame].load(sURL, iTargetWidth);}
	  else if(document.all || document.getElementById){
	    window[sTargetFrame].location.href = sURL;}
	}
	
	
	//change images functions
	function changeImages() {
        if (document.images) {
                for (var i=0; i<changeImages.arguments.length; i+=3) {
                       	doc =  document;
						//alert(changeImages.arguments[i])
						obj = doc.images[changeImages.arguments[i] ];
                        if( obj ){
                                obj.src = changeImages.arguments[i+1];
								}
                		}
        		}
	}
		function newImage(arg) {
        if (document.images) {
                rslt = new Image();
                rslt.src = arg;
                return rslt;
       	 	}
		}
		
		
		

		
//menu function starts
var nob;
var nof;
var uptimer = null;
var downtimer = null;


function shifte(n) {
	var linePos = 60;
	var linePos2 = 60;
	if (n < 4 && n > 0) {
	var menu = ('menu' + (parseInt(n)));
	if (document.layers) {
			choice = document.layers[menu];
			hideL = document.layers['line0'];
			hideL2 = document.layers['line1'];
		}
		else if (document.all) {
			choice = document.all(menu).style;
			hideL = document.all('line0').style;
			hideL2 = document.all('line1').style;
		}
		else if (document.getElementById) {
			choice = document.getElementById(menu).style;
			hideL = document.getElementById('line0').style;
			hideL2 = document.getElementById('line1').style;			
	}
	
	for (var i = 1; i <= 3; i++) {
	m = ('menu' + i);
	if (document.layers) {
			c = document.layers[m];
		}
		else if (document.all) {
			c = document.all(m).style;
		}
		else if (document.getElementById) {
			c = document.getElementById(m).style;
		}
		if((c.visibility.toLowerCase() == visible)&& (c!=choice)){
			var nb = i
			changeImages('img_title'+(parseInt(nb))+'_'+nb,'/images/meny/off_title'+(parseInt(nb))+'.gif' ,0);
			c.visibility = hidden;
			hideL.visibility = hidden;
		}
	}
	
	if (choice.visibility.toLowerCase() == visible) {
		if(n==1){
			hideL.top = linePos
			linePos2 = linePos2 
		}else if (n==2){
			hideL.top = linePos +22
			linePos2 = linePos2 
		}else if (n==3) {
			hideL.top = linePos +44
			linePos2 = linePos2 
		}
		if(page==4){
			hideL2.top = linePos2 +80
		}else if (page==5){
			hideL2.top = linePos2 +102
		}else if (page==6) {
			hideL2.top = linePos2 +124
		}else if (page==7) {
			hideL2.top = linePos2 +146
		}else if (page==8) {
			hideL2.top = linePos2 +182
		}else if (page==9) {
			hideL2.top = linePos2 +204
		}else if (page==10) {
			hideL2.top = linePos2 +226
		}	
		//alert('img_title'+(parseInt(n))+'_'+n + '  '+ '/images/meny/off_title'+(parseInt(n))+'.gif')
		changeImages('img_title'+(parseInt(n))+'_'+n,'/images/meny/off_title'+(parseInt(n))+'.gif' ,0);
		hideL2.visibility = visible;
		hideL.visibility = hidden;
		choice.visibility = hidden;
		
	}else{
		if(n==1){
			hideL.top = linePos
			linePos2 = linePos2 + 234
		}else if (n==2){
			hideL.top = linePos +22
			linePos2 = linePos2 + 62
		}else if (n==3) {
			hideL.top = linePos +44
			linePos2 = linePos2 + 78
		}
		if(page==4){
			hideL2.top = linePos2 +80
		}else if (page==5){
			hideL2.top = linePos2 +102
		}else if (page==6) {
			hideL2.top = linePos2 +124
		}else if (page==7) {
			hideL2.top = linePos2 +146
		}else if (page==8) {
			hideL2.top = linePos2 +182
		}else if (page==9) {
			hideL2.top = linePos2 +204
		}else if (page==10) {
			hideL2.top = linePos2 +226
		}	
		//alert('img_title'+(parseInt(n))+'_'+n + '  '+ '/images/meny/on_title'+(parseInt(n))+'.gif')
		hideL.visibility = visible;
		changeImages('img_title'+(parseInt(n))+'_'+n,'/images/meny/on_title'+(parseInt(n))+'.gif' ,0);
		choice.visibility = visible;
	}
	}else{
		
		if (document.layers) {
			
			hideL2 = document.layers['line1'];
		}
		else if (document.all) {
			
			hideL2 = document.all('line1').style;
		}
		else if (document.getElementById) {
			
			hideL2 = document.getElementById('line1').style;
		}
		
		if(n==4){
			hideL2.top = linePos +80
		}else if (n==5){
			hideL2.top = linePos +102
		}else if (n==6) {
			hideL2.top = linePos +124
		}else if (n==7) {
			hideL2.top = linePos +146
		}else if (n==8) {
			hideL2.top = linePos +182
		}else if (n==9) {
			hideL2.top = linePos +204
		}else if (n==10) {
			hideL2.top = linePos +226
		}
		hideL2.visibility = visible;
		changeImages('img_title'+(parseInt(n))+'_0','/images/meny/on_title'+(parseInt(n))+'.gif' ,0);
		changeImages('img_title'+(parseInt(n))+'_1','/images/meny/on_title'+(parseInt(n))+'.gif' ,0);
		changeImages('img_title'+(parseInt(n))+'_2','/images/meny/on_title'+(parseInt(n))+'.gif' ,0);
		changeImages('img_title'+(parseInt(n))+'_3','/images/meny/on_title'+(parseInt(n))+'.gif' ,0);
	}
	
}









// Initiate the javascript

function startup(pageID) {
        if (document.layers) {
                visible = "show";
                hidden = "hide";
        }
        else if (document.all) {
                visible = "visible";
                hidden = "hidden";
        }
        else if (document.getElementById) {
                visible = "visible";
                hidden = "hidden";
        }
		
		page = pageID.substring(0,1)
		sub_page = pageID.substring(1,3)
		if (page!='' && page != '-'){
		eval("shifte(" + (parseInt(page))+ ');');	
		}
		if (sub_page!=''){
			changeImages('choice'+page +'_'+sub_page,'/images/meny/on_choice'+page +'_'+sub_page+'.gif' ,0);
		}
		
		
		// laddar huvudmeny bilder
		img_title0_0 = newImage("/images/meny/on_title0.gif");
		img_title1_0 = newImage("/images/meny/on_title1.gif");
		img_title2_0 = newImage("/images/meny/on_title2.gif");
		img_title3_0 = newImage("/images/meny/on_title3.gif");
		img_title4_0 = newImage("/images/meny/on_title4.gif");
		img_title5_0 = newImage("/images/meny/on_title5.gif");
		img_title6_0 = newImage("/images/meny/on_title6.gif");
		img_title7_0 = newImage("/images/meny/on_title7.gif");
		img_title8_0 = newImage("/images/meny/on_title8.gif");
		img_title9_0 = newImage("/images/meny/on_title9.gif");
		img_title0_1 = newImage("/images/meny/on_title0.gif");
		img_title1_1 = newImage("/images/meny/on_title1.gif");
		img_title2_1 = newImage("/images/meny/on_title2.gif");
		img_title3_1 = newImage("/images/meny/on_title3.gif");
		img_title4_1 = newImage("/images/meny/on_title4.gif");
		img_title5_1 = newImage("/images/meny/on_title5.gif");
		img_title6_1 = newImage("/images/meny/on_title6.gif");
		img_title7_1 = newImage("/images/meny/on_title7.gif");
		img_title8_1 = newImage("/images/meny/on_title8.gif");
		img_title9_1 = newImage("/images/meny/on_title9.gif");
		img_title0_2 = newImage("/images/meny/on_title0.gif");
		img_title1_2 = newImage("/images/meny/on_title1.gif");
		img_title2_2 = newImage("/images/meny/on_title2.gif");
		img_title3_2 = newImage("/images/meny/on_title3.gif");
		img_title4_2 = newImage("/images/meny/on_title4.gif");
		img_title5_2 = newImage("/images/meny/on_title5.gif");
		img_title6_2 = newImage("/images/meny/on_title6.gif");
		img_title7_2 = newImage("/images/meny/on_title7.gif");
		img_title8_3= newImage("/images/meny/on_title8.gif");
		img_title9_3 = newImage("/images/meny/on_title9.gif");
		img_title0_3 = newImage("/images/meny/on_title0.gif");
		img_title1_3 = newImage("/images/meny/on_title1.gif");
		img_title2_3 = newImage("/images/meny/on_title2.gif");
		img_title3_3 = newImage("/images/meny/on_title3.gif");
		img_title4_3 = newImage("/images/meny/on_title4.gif");
		img_title5_3 = newImage("/images/meny/on_title5.gif");
		img_title6_3 = newImage("/images/meny/on_title6.gif");
		img_title7_3 = newImage("/images/meny/on_title7.gif");
		img_title8_3 = newImage("/images/meny/on_title8.gif");
		img_title9_3 = newImage("/images/meny/on_title9.gif");
		choice0_0 = newImage("/images/meny/on_choice0_0.gif");
		choice0_1 = newImage("/images/meny/on_choice0_1.gif");
		choice0_2 = newImage("/images/meny/on_choice0_2.gif");
		choice0_3 = newImage("/images/meny/on_choice0_3.gif");
		choice0_4 = newImage("/images/meny/on_choice0_4.gif");
		choice0_5 = newImage("/images/meny/on_choice0_5.gif");
		choice0_6 = newImage("/images/meny/on_choice0_6.gif");
		choice0_7 = newImage("/images/meny/on_choice0_7.gif");
		choice0_8 = newImage("/images/meny/on_choice0_8.gif");
		choice0_9 = newImage("/images/meny/on_choice0_9.gif");
		choice0_10 = newImage("/images/meny/on_choice0_10.gif");
		choice0_11 = newImage("/images/meny/on_choice0_11.gif");
		choice0_12 = newImage("/images/meny/on_choice0_12.gif");
		choice0_13 = newImage("/images/meny/on_choice0_13.gif");
		choice0_14 = newImage("/images/meny/on_choice0_14.gif");
		choice0_15 = newImage("/images/meny/on_choice0_15.gif");
		choice0_16 = newImage("/images/meny/on_choice0_16.gif");
		choice0_17 = newImage("/images/meny/on_choice0_17.gif");
		choice1_0 = newImage("/images/meny/on_choice1_0.gif");
		choice1_1 = newImage("/images/meny/on_choice1_1.gif");
		choice1_2 = newImage("/images/meny/on_choice1_2.gif");
		choice1_3 = newImage("/images/meny/on_choice1_3.gif");
		choice2_0 = newImage("/images/meny/on_choice2_0.gif");
		choice2_1 = newImage("/images/meny/on_choice2_1.gif");
		choice2_2 = newImage("/images/meny/on_choice2_2.gif");
		choice2_3 = newImage("/images/meny/on_choice2_3.gif");
		
		preloadFlag = true;
		
}


