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
	
//	window.onerror = handleError;

//		function handleError(message, url, line){
			// ät upp felet bara....
//			return true;
//		}

	
	function showLine(layerName, toplayer){
		
		var topL = 'title' +toplayer

		if (document.layers) {
				var layN = document.layers[layerName];
				var topPos = document.layers[topL].top;
			}
			else if (document.all) {
				var layN = document.all(layerName).style;
				var topPos = document.all(topL).style.top;
			}
			else if (document.getElementById) {
				var layN = document.getElementById(layerName).style;
				var topPos = document.getElementById(topL).style.top;
		}
			layN.top = parseInt(topPos)-2;
			layN.visibility = visible;
	}
	
	function hideLine(layerName){
		if (document.layers) {
				var layN = document.layers[layerName];
				
			}
			else if (document.all) {
				var layN = document.all(layerName).style;
				
			}
			else if (document.getElementById) {
				var layN = document.getElementById(layerName).style;
		}
		layN.visibility = hidden;
				
	}
	
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
var choice0_move = 0;
var choice1_move = 283;//225
var choice2_move = 55;
var choice3_move = 70;
var choice4_move = 0;
var choice5_move = 0;
var choice6_move = 0;
var choice7_move = 0;
var choice8_move = 0;
var choice9_move = 0;
var choice10_move = 0;

var titlearray = new Array();
var choicearray = new Array();
var linearray = new Array()

function arrays() {
	for (var i = 1; i <= nob; i++) {
		titlearray[i] = ('title' + i);
		choicearray[i] = ('choice' +i);
		linearray[i] = ('line' + i);
	}
}

function shifte(n) {
		var menu = ('choice' + n);
		var checkOpen = 0;
		//alert(n);
		if (document.layers) {
			choice = document.layers[menu];
		}
		else if (document.all) {
			choice = document.all(menu).style;
		}
		else if (document.getElementById) {
			choice = document.getElementById(menu).style;
		}
		if ((uptimer == null) && (downtimer == null)) {
			for (var i = 0; i <= nob; i++) {
				m = ('choice' + i);
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
					var p = (parseInt(c.top)-(eval('choice' + i +'_move')));
					checkOpen=1;
					}
				}
			
			if (checkOpen == 1) {
				var pstn = 120;
				if ((isIE && isMac) || (willOpen)){
					moveup_mac(nb, p, n,pstn);
				}else{
					moveup(nb, p, n,pstn);
				}
			}else{
				if (choice.visibility.toLowerCase() == visible) {
					var pstn = (parseInt(choice.top) - eval('choice' + n +'_move'));
					if(n<4 && n>0){
					if ((isIE && isMac) || (willOpen)){
						moveup_mac(n,pstn);
					}else{
						moveup(n,pstn);
					}
					}else{
					hideLine('line'+n);
					changeImages('img_title'+n,'/images/meny/off_title'+n+'.gif' ,0);
					}
				}else {
					var pstn = (parseInt(choice.top) + eval('choice' + n +'_move'));
					//alert(eval('meny/on_title' + n +'.gif'))
					imgID = ('img_title' +n)
					//alert('img_title'+n + ' ' +'/images/meny/on_title'+n+'.gif')
					changeImages('img_title'+n,'/images/meny/on_title'+n+'.gif' ,0);
					showLine('line'+n,n);
					if(n<4&& n>0){ 
						if ((isIE && isMac) || (willOpen)){
							movedown_mac(n,pstn);
						}else{
							movedown(n,pstn);
						}
					}
					
				
				}
			}
			
		}
	willOpen = false;
}

function shifteMac(n){
	
	var menu = ('menu' + n);
	if (document.layers) {
			choice = document.layers[menu];
		}
		else if (document.all) {
			choice = document.all(menu).style;
		}
		else if (document.getElementById) {
			choice = document.getElementById(menu).style;
	}
	if (n == '0'){
			choice.visibility = visible;
	}else if (n=='1'){
	
	}else if (n=='2'){
	
	}

}

function movedown() {
	
	var a = arguments; 
	num = parseInt(a[0]);
	endpos = parseInt(a[1]);
	menu = ('choice' + a[0]);

	if (document.layers) {
		choice = document.layers[menu];
	}
	else if (document.all) {
		choice = document.all(menu).style;
	}
	else if (document.getElementById) {
		choice = document.getElementById(menu).style;
	}
	var ypos = parseInt(choice.top);
	if (ypos < endpos) {
		//alert(ypos + ' ' + choice.top)
		choice.visibility = visible;
		choice.top = (ypos + 15);
		for (var i = (num+1); i <= nob; i++) {
			if (document.layers) {
				var tit = document.layers[titlearray[i]];
				var subm = document.layers[choicearray[i]];
				var hideL= document.layers[linearray[i]];
			}
			else if (document.all) {
				var tit = document.all(titlearray[i]).style;
				var subm = document.all(choicearray[i]).style;
				var hideL= document.all(linearray[i]).style;
			}
			else if (document.getElementById) {
				var tit = document.getElementById(titlearray[i]).style;
				var subm = document.getElementById(choicearray[i]).style;
				var hideL= document.getElementById(linearray[i]).style;
			}
			//alert(tit.top+' '+ subm.top)
			tit.top = (parseInt(tit.top) + 15);
			subm.top = (parseInt(subm.top) + 15);
			hideL.top = (parseInt(hideL.top) + 15);
		}
	}
	//alert(ypos + ' ' +endpos)
	if (!(ypos >= endpos)) {
		
		downtimer = setTimeout("movedown(num,endpos)",1);
	}
	else {
		//choice.visibility = visible;
		clearTimeout(downtimer);
		downtimer = null;
	}
}

function movedown_mac() {
	
	var a = arguments; 
	num = parseInt(a[0]);
	endpos = parseInt(a[1]);
	menu = ('choice' + a[0]);

	if (document.layers) {
		choice = document.layers[menu];
	}
	else if (document.all) {
		choice = document.all(menu).style;
	}
	else if (document.getElementById) {
		choice = document.getElementById(menu).style;
	}
	var ypos = parseInt(choice.top);
	if (ypos < endpos) {
		choice.visibility = visible;
		choice.top = (ypos +  eval('choice' + num +'_move'));
		for (var i = (num+1); i <= nob; i++) {
			if (document.layers) {
				var tit = document.layers[titlearray[i]];
				var subm = document.layers[choicearray[i]];
				var hideL = document.layers[linearray[i]];
			}
			else if (document.all) {
				var tit = document.all(titlearray[i]).style;
				var subm = document.all(choicearray[i]).style;
				var hideL = document.all(linearray[i]).style;
			}
			else if (document.getElementById) {
				var tit = document.getElementById(titlearray[i]).style;
				var subm = document.getElementById(choicearray[i]).style;
				var hideL = document.getElementById(linearray[i]).style;
			}
			//alert(tit.top+' '+ subm.top)
			tit.top = (parseInt(tit.top) +  eval('choice' + num +'_move'));
			subm.top = (parseInt(subm.top) +  eval('choice' + num +'_move'));
			hideL.top = (parseInt(hideL.top) +  eval('choice' + num +'_move'));
		}
	}
	clearTimeout(downtimer);
	downtimer = null;
	
}

function moveup() {
	var a = arguments; 
	var num = parseInt(a[0]);
	var endpos = parseInt(a[1]);
	var newnum = parseInt(a[2]);
	var newendpos = parseInt(a[3]);
	menu = ('choice' + a[0]);

	if (document.layers) {
		choice = document.layers[menu];
	}
	else if (document.all) {
		choice = document.all(menu).style;
	}
	else if (document.getElementById) {
		choice = document.getElementById(menu).style;
	}
	var ypos = parseInt(choice.top);
	if (ypos > endpos) {
		choice.top = (ypos - 15);
		for (var i = (num+1); i <= nob; i++) {
			if (document.layers) {
				var tit = document.layers[titlearray[i]];
				var subm = document.layers[choicearray[i]];
				var hideL = document.layers[linearray[i]];
			}
			else if (document.all) {
				var tit = document.all(titlearray[i]).style;
				var subm = document.all(choicearray[i]).style;
				var hideL = document.all(linearray[i]).style;
			}
			else if (document.getElementById) {
				var tit = document.getElementById(titlearray[i]).style;
				var subm = document.getElementById(choicearray[i]).style;
				var hideL = document.getElementById(linearray[i]).style;
			}
			tit.top = (parseInt(tit.top) - 15);
			subm.top = (parseInt(subm.top) - 15);
			hideL.top = (parseInt(hideL.top) - 15);
		}
	}
	if (!(ypos <= endpos)) {
		eval("uptimer = setTimeout(\"moveup(" + num+ ',' + endpos+ ',' +newnum+ ','  +newendpos+")\",1)");
	}
	else {
		clearTimeout(uptimer);
		uptimer = null;
		choice.visibility = hidden;
		hideLine('line' + num);
		changeImages('img_title'+num,'/images/meny/off_title'+num+'.gif' ,0); 
		if (!isNaN(newnum)){
		eval("shifte(" + newnum+ ');');		
		} 
	}
}

function moveup_mac() {
	var a = arguments; 
	var num = parseInt(a[0]);
	var endpos = parseInt(a[1]);
	var newnum = parseInt(a[2]);
	var newendpos = parseInt(a[3]);
	menu = ('choice' + a[0]);

	if (document.layers) {
		choice = document.layers[menu];
	}
	else if (document.all) {
		choice = document.all(menu).style;
	}
	else if (document.getElementById) {
		choice = document.getElementById(menu).style;
	}
	var ypos = parseInt(choice.top);
	if (ypos > endpos) {
		choice.top = (ypos - eval('choice' + num +'_move'));
		for (var i = (num+1); i <= nob; i++) {
			if (document.layers) {
				var tit = document.layers[titlearray[i]];
				var subm = document.layers[choicearray[i]];
				var hideL = document.layers[linearray[i]];
			}
			else if (document.all) {
				var tit = document.all(titlearray[i]).style;
				var subm = document.all(choicearray[i]).style;
				var hideL= document.all(linearray[i]).style;
			}
			else if (document.getElementById) {
				var tit = document.getElementById(titlearray[i]).style;
				var subm = document.getElementById(choicearray[i]).style;
				var hideL= document.getElementById(linearray[i]).style;
			}
			tit.top = (parseInt(tit.top) - eval('choice' + num +'_move'));
			subm.top = (parseInt(subm.top) - eval('choice' + num +'_move'));
			hideL.top = (parseInt(hideL.top) - eval('choice' + num +'_move'));
		}
	}
	clearTimeout(uptimer);
	uptimer = null;
	choice.visibility = hidden;
	hideLine('line' + num);
	changeImages('img_title'+num,'/images/meny/off_title'+num+'.gif' ,0); 
	if (!isNaN(newnum))
	{
		eval("shifte(" + newnum+ ');');	
	}
}


// Initiate the javascript

function startup(pageID) {
        nob = 11;
        nof = 4;
        arrays();
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
		if (page!=''){
		willOpen= true;
		eval("shifte(" + page+ ');');		
		}
		if (sub_page!=''){
			changeImages('choice'+page +'_'+sub_page,'/images/meny/on_choice'+page +'_'+sub_page+'.gif' ,0);
		}
		
		
		// laddar huvudmeny bilder
		img_title0 = newImage("/images/meny/on_title0.gif");
		img_title1 = newImage("/images/meny/on_title1.gif");
		img_title2 = newImage("/images/meny/on_title2.gif");
		img_title3 = newImage("/images/meny/on_title3.gif");
		img_title4 = newImage("/images/meny/on_title4.gif");
		img_title5 = newImage("/images/meny/on_title5.gif");
		img_title6 = newImage("/images/meny/on_title6.gif");
		img_title7 = newImage("/images/meny/on_title7.gif");
		img_title8 = newImage("/images/meny/on_title8.gif");
		img_title9 = newImage("/images/meny/on_title9.gif");
		choice1_0 = newImage("/images/meny/on_choice1_0.gif");
		choice1_1 = newImage("/images/meny/on_choice1_1.gif");
		choice1_2 = newImage("/images/meny/on_choice1_2.gif");
		choice1_3 = newImage("/images/meny/on_choice1_3.gif");
		choice1_4 = newImage("/images/meny/on_choice1_4.gif");
		choice1_5 = newImage("/images/meny/on_choice1_5.gif");
		choice1_6 = newImage("/images/meny/on_choice1_6.gif");
		choice1_7 = newImage("/images/meny/on_choice1_7.gif");
		choice1_8 = newImage("/images/meny/on_choice1_8.gif");
		choice1_9 = newImage("/images/meny/on_choice1_9.gif");
		choice1_10 = newImage("/images/meny/on_choice1_10.gif");
		choice1_11 = newImage("/images/meny/on_choice1_11.gif");
		choice1_12 = newImage("/images/meny/on_choice1_12.gif");
		choice1_13 = newImage("/images/meny/on_choice1_13.gif");
		choice1_14 = newImage("/images/meny/on_choice1_14.gif");
		choice1_15 = newImage("/images/meny/on_choice1_15.gif");
		choice1_16 = newImage("/images/meny/on_choice1_16.gif");
		choice1_17 = newImage("/images/meny/on_choice1_17.gif");
		choice2_0 = newImage("/images/meny/on_choice2_0.gif");
		choice2_1 = newImage("/images/meny/on_choice2_1.gif");
		choice2_2 = newImage("/images/meny/on_choice2_2.gif");
		choice2_3 = newImage("/images/meny/on_choice2_3.gif");
		choice3_0 = newImage("/images/meny/on_choice3_0.gif");
		choice3_1 = newImage("/images/meny/on_choice3_1.gif");
		choice3_2 = newImage("/images/meny/on_choice3_2.gif");
		choice3_3 = newImage("/images/meny/on_choice3_3.gif");
		
		preloadFlag = true;
		
}


