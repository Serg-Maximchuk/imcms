/* **********************************
 *   By: Tommy Ullberg, imCode       
 *   www.imcode.com                  
 ********************************* */

/* ***** HELP FUNCTIONS ***** */

function showHelp(what){
	if(what != ''){
		hideAllLayers();
		disableButtons('help');
		document.getElementById("disableTopBtnDiv").style.width = 674;
		if (parseInt(editorDiv.style.width) > 525) editorDiv.style.width = 525;
		moveLayer('helpDiv',null,null,0,null);
		switch(what){
			case 'all':
				hideHelp('allLay');
				moveLayer('helpDiv',null,null,0,null);
				moveLayer('helpTextAllDiv',null,null,1,null);
				break;
			case 'upperbuttons':
				hideHelp('allLay');
				moveLayer('helpDiv',null,null,0,null);
				moveLayer('helpTextAllUpperDiv',null,null,1,null);
				break;
			case 'rightbuttons':
				hideHelp('allLay');
				moveLayer('helpDiv',null,null,0,null);
				moveLayer('helpTextAllRightDiv',null,null,1,null);
				break;
		}
	}
}

function hideHelp(what){
	moveLayer('helpTextAllDiv',null,null,0,null);
	moveLayer('helpTextAllUpperDiv',null,null,0,null);
	moveLayer('helpTextAllRightDiv',null,null,0,null);
	moveLayer('helpDescRightDiv',null,null,0,null);
	showHelpLayer('DefaultText');
	moveLayer('helpSubjectDiv',null,null,0,null);
	document.forms[0].execState.value = '1';
	editorDiv.focus();
	//showDefaultPane();
	
	for(var i=1; i<=30; i++){
		if (document.getElementById("btn" + i)) document.getElementById("btn" + i).style.cursor = 'hand';
		if (document.getElementById("btn" + i)) document.getElementById("btn" + i).disabled = 0;
	}
	
	if(what == 'allLay'){
		moveLayer('helpDiv',null,null,0,null);
	} else if(what == 'all'){
		if (!showSimple && !showAdv) {
			editorDiv.style.width = 675;
		} else {
			editorDiv.style.width = 525;
		}
		moveLayer('helpDiv',null,null,0,null);
		enableButtons();
		document.getElementById("disableTopBtnDiv").style.width = 535;
		if (document.getElementById("topClassSelect")) document.getElementById("topClassSelect").disabled = 0;
		if (document.getElementById("topClassWordSelect")) document.getElementById("topClassWordSelect").disabled = 0;
		if (document.getElementById("fontcolor")) document.getElementById("fontcolor").disabled = 0;
		if (document.getElementById("backgroundcolor")) document.getElementById("backgroundcolor").disabled = 0;
	}
}

function showHelpSubjects(){
	hideHelp('all');
	hideAllLayers();
	if(document.getElementById("helpDiv").style.visibility == 'hidden'){
		if (parseInt(editorDiv.style.width) > 525) editorDiv.style.width = 525;
		moveLayer('helpDiv',null,null,1,null);
		moveLayer('helpSubjectDiv',null,null,1,null);
		moveLayer('helpDescRightDiv',null,null,1,null);
		showHelpLayer('DefaultText');
		document.forms[0].execState.value = '2';
		document.getElementById("hideHelpBtn").focus();
		for(var i=1; i<=30; i++){
			if(i != 5){
				if (document.getElementById("btn" + i)) document.getElementById("btn" + i).style.cursor = 'help';
			} else {
				if (document.getElementById("btn" + i)) document.getElementById("btn" + i).style.cursor = 'default';
				if (document.getElementById("btn" + i)) document.getElementById("btn" + i).disabled = 1;
			}
		}
		if (document.getElementById("topClassSelect")) document.getElementById("topClassSelect").disabled = 1;
		if (document.getElementById("topClassWordSelect")) document.getElementById("topClassWordSelect").disabled = 1;
		if (document.getElementById("fontcolor")) document.getElementById("fontcolor").disabled = 1;
		if (document.getElementById("backgroundcolor")) document.getElementById("backgroundcolor").disabled = 1;
		if (document.getElementById("topClassSelectClickDiv")) document.getElementById("topClassSelectClickDiv").style.cursor = 'help';
		if (document.getElementById("topClassWordSelectClickDiv")) document.getElementById("topClassWordSelectClickDiv").style.cursor = 'help';
		if (document.getElementById("fontcolorClickDiv")) document.getElementById("fontcolorClickDiv").style.cursor = 'help';
		if (document.getElementById("backgroundcolorClickDiv")) document.getElementById("backgroundcolorClickDiv").style.cursor = 'help';
	}
}


/* ***** HELP TEXTS ***** */

function showHelpLayer(what){
	var helpImageIcon = '';
	var helpImageIconNew = '';
	var helpHeading = '';
	var helpContent = '';
	var helpHeadingLayer = document.getElementById("helpHeadingDiv");
	var helpContentLayer = document.getElementById("helpContentDiv");
	moveLayer('helpSubjectDiv',null,null,1,null);
	switch(what){
		// * Help texts for the click-button-help
		case 'Cut':
			helpImageIcon = 'images/btn_cut.gif';
			helpHeading = 'Knappen &quot;Klipp ut&quot;';
			helpContent = 'Anv�nds f�r att klippa ut markerad text till <i>Urklipp/Klippbordet</i>. Inneh�llet kan sedan klistras in p� annan plats eller i annat program.';
			break;
		case 'Copy':
			helpImageIcon = 'images/btn_copy.gif';
			helpHeading = 'Knappen &quot;Kopiera&quot;';
			helpContent = 'Anv�nds f�r att kopiera markerad text till <i>Urklipp/Klippbordet</i>. Inneh�llet kan sedan klistras in p� annan plats eller i annat program.';
			break;
		case 'Paste':
			helpImageIcon = 'images/btn_paste.gif';
			helpHeading = 'Knappen &quot;Klistra in&quot;';
			helpContent = 'Anv�nds f�r att klistra in det senast kopierade eller urklippta inneh�llet i <i>Urklipp/Klippbordet</i>.<br><br>Inneh�llet klistras in d�r mark�ren st�r.<br>Om n�gon text �r markerad, byts den ut mot det inklistrade inneh�llet.';
			break;
		case 'Preview':
			helpImageIcon = 'images/btn_preview.gif';
			helpHeading = 'Knappen &quot;F�rhandsgranska&quot;';
			helpContent = '�ppnar och visar hur HTML-koden ser ut, i ett nytt f�nster.<br><br>I f�rhandsgranskningsf�nstret f�r man se hur koden verkligen genereras av en webl�sare, n�r man v�l har sparat sin kod.<br><br>Vidare kan man justera inneh�llsbredden, om man vet denna, f�r att �terspegla den bredd man anv�nder d�r sj�lva texten/koden skall ligga. D�rmed f�r man en exakt uppfattning om radbrytningar och ev. f�rskjutningar som kan uppst� p� fel st�lle.';
			break;
		case 'Undo':
			helpImageIcon = 'images/btn_undo.gif';
			helpHeading = 'Knappen &quot;�ngra&quot;';
			helpContent = '�ngrar/�terst�ller senast utf�rda �tg�rd.<br><br><b>OBS! G�ller inte alla funktioner.</b><br><br>Vissa funktioner kan man bara �terst�lla genom att g�ra n�got av f�ljande:<ul><li>Ladda om senast sparade version<li>Manuellt editera bort koden i HTML-l�ge.<li>V�lja att radera all formatering (tar bort all HTML)</ul>';
			break;
		case 'Redo':
			helpImageIcon = 'images/btn_redo.gif';
			helpHeading = 'Knappen &quot;G�r om&quot;';
			helpContent = '�terst�ller senast �ngrade �tg�rd.';
			break;
		case 'Refresh':
			helpImageIcon = 'images/btn_refresh.gif';
			helpHeading = 'Knappen &quot;Ladda om texten&quot;';
			helpContent = '�terst�ller orginalversionen, eller senast sparade version (om man sparat), fr�n imCMS textinmatningsformul�r.';
			break;
		case 'Erase':
			helpImageIcon = 'images/btn_eraser.gif';
			helpHeading = 'Knappen &quot;Radera all formatering&quot;';
			helpContent = 'Raderar alla HTML taggar i texten och �terst�ller formateringen till ren text.<br><br><b>OBS!</b> Raderar inte radbrytningar men ev. styckeformatering bibeh�lls inte.';
			break;
		case 'FontClass':
			helpImageIcon = '';
			helpHeading = 'Formatv�ljare';
			helpContent = '�ndrar stilen p� markerad text till det valda formatet.<br><br>';
			helpContent += '<b>Notera!&nbsp;&nbsp;</b>Det �r b�ttre att markera texten som skall �ndras genom att dra med muspekaren, �n att dubbel- eller trippelklicka som i ordbehandlingsprogrammen. I bland f�rsvinner radbrytningen efter markeringen om man markerar s� och anv�nder funktionen. (Microsofts fel)<br><br>';
			helpContent += 'Det �r ingen fara dock. Bara att st�lla sig efter markeringen och g�ra en ny radbrytning.';
			break;
		case 'FontColor':
			helpImageIcon = 'images/btn_color_text.gif';
			helpHeading = 'Teckensnittsf�rg';
			helpContent = '�ndrar f�rgen p� <font style="color:blue">markerad</font> text till den valda f�rgen.';
			break;
		case 'FontBgColor':
			helpImageIcon = 'images/btn_color_background.gif';
			helpHeading = '�verstrykningsf�rg';
			helpContent = '�ndrar �verstrykningsf�rgen p� <font style="background-color:#bbffbb">markerad</font> text till den valda f�rgen.<br>Simulerar en �verstrykningspenna.';
			break;
		case 'EditCode':
			helpImageIconNew = '<button disabled style="width:55; height:21; cursor:default"><img src="images/btn_preview_editor.gif"></button>&nbsp;<button disabled style="width:55; height:21; cursor:default"><img src="images/btn_preview_html.gif"></button>';
			helpHeading = 'Knappen &quot;V�xla editeringsl�ge&quot;';
			helpContent = 'V�xlar mellan l�gena WYSIWYG editor (What You See Is What You Get) och HTML editor.<br><br>I l�get HTML editor kan man allts� finjustera sin HTML-kod, eller r�tta till ev. felaktigheter, om man har den kunskapen.<br>I l�get WYSIWYG editor fungerar editeringen n�stan som en ordbehandlare.';
			break;
		case 'Bold':
			helpImageIcon = 'images/btn_format_f.gif';
			helpHeading = 'Knappen &quot;Fetstil&quot;';
			helpContent = 'Formaterar <b>markerad</b> text som fetstil.';
			break;
		case 'Italic':
			helpImageIcon = 'images/btn_format_k.gif';
			helpHeading = 'Knappen &quot;Kursiv stil&quot;';
			helpContent = 'Formaterar <i>markerad</i> text som kursiv (lutande).';
			break;
		case 'Underline':
			helpImageIcon = 'images/btn_format_u.gif';
			helpHeading = 'Knappen &quot;Understrykning&quot;';
			helpContent = 'Stryker under <u>markerad</u> text.<br><br>B�r anv�ndas med f�rsiktighet, eftersom understruken text l�tt kan f�rv�xlas med en l�nk p� Internet.';
			break;
		case 'StrikeThrough':
			helpImageIconNew = '<button class="button" onClick="return false" style="cursor:default"><strike style="position:relative; top:-1; font: bold 14px Times New Roman">S</strike></button>';
			helpHeading = 'Knappen &quot;�verstrykning&quot;';
			helpContent = 'Stryker �ver <strike>markerad</strike> text.';
			break;
		case 'JustifyLeft':
			helpImageIcon = 'images/btn_justify_left.gif';
			helpHeading = 'Knappen &quot;V�nster justera&quot;';
			helpContent = 'V�nster justerar markerat stycke. (standard)';
			break;
		case 'JustifyCenter':
			helpImageIcon = 'images/btn_justify_center.gif';
			helpHeading = 'Knappen &quot;Centrera&quot;';
			helpContent = 'Centrerar markerat stycke.';
			break;
		case 'JustifyRight':
			helpImageIcon = 'images/btn_justify_right.gif';
			helpHeading = 'Knappen &quot;H�ger justera&quot;';
			helpContent = 'H�ger justerar markerat stycke.';
			break;
		case 'SuperScript':
			helpImageIcon = 'images/btn_superscript.gif';
			helpHeading = 'Knappen &quot;Upph�jd text&quot;';
			helpContent = 'Formaterar markerad text som upph�jd text. Ex: m<sup>2</sup>.';
			break;
		case 'SubScript':
			helpImageIcon = 'images/btn_subscript.gif';
			helpHeading = 'Knappen &quot;Ners�nkt text&quot;';
			helpContent = 'Formaterar markerad text som ners�nkt text. Ex: H<sub>2</sub>O.';
			break;
		case 'InsertOrderedList':
			helpImageIcon = 'images/btn_list_ordered.gif';
			helpHeading = 'Knappen &quot;Skapa numrerad lista&quot;';
			helpContent = 'Skapar en numrerad lista (1,2,3...) av alla markerade stycken.<br><br>Ytterligare punkter kan l�ggas till genom att klicka p� <i>Enter</i>.<br>F�r att byta rad utan att skapa ny punkt - klicka <i>Shift + Enter</i>.<br><br>F�r att b�rja skriva utanf�r listan:<br>Klicka utanf�r listtexten p� det st�lle du vill b�rja skriva - eller...<br>klicka p� <i>Enter</i> och sedan p� &quot;Skapa numrerad lista&quot; igen.';
			break;
		case 'InsertUnorderedList':
			helpImageIcon = 'images/btn_list_unordered.gif';
			helpHeading = 'Knappen &quot;Skapa punktlista&quot;';
			helpContent = 'Skapar en punktlista av alla markerade stycken.<br><br>Ytterligare punkter kan l�ggas till genom att klicka p� <i>Enter</i>.<br>F�r att byta rad utan att skapa ny punkt - klicka <i>Shift + Enter</i>.<br><br>F�r att b�rja skriva utanf�r listan:<br>Klicka utanf�r listtexten p� det st�lle du vill b�rja skriva - eller...<br>klicka p� <i>Enter</i> och sedan p� &quot;Skapa punktlista&quot; igen.';
			break;
		case 'Outdent':
			helpImageIcon = 'images/btn_outdent.gif';
			helpHeading = 'Knappen &quot;�ka indrag&quot;';
			helpContent = '�kar indraget (v�nstermarginalen) p� markerade stycken med ett steg.<br><br>Hur stort indraget �r beror p� vilken webl�sare man har. Det brukar vara ungef�r 40 pixlar. Man kan justera detta v�rde genom att l�gga in <nobr class="imEditHelpCode">BLOCKQUOTE { margin-left: <i>nytt v�rde</i> }</nobr> i sitt stylesheet.';
			break;
		case 'Indent':
			helpImageIcon = 'images/btn_indent.gif';
			helpHeading = 'Knappen &quot;Minska indrag&quot;';
			helpContent = 'Minskar indraget (v�nstermarginalen) p� markerade stycken med ett steg.<br><br>Hur stort indraget �r beror p� vilken webl�sare man har. Det brukar vara ungef�r 40 pixlar. Man kan justera detta v�rde genom att l�gga in <nobr class="imEditHelpCode">BLOCKQUOTE { margin-left: <i>nytt v�rde</i> }</nobr> i sitt stylesheet.';
			break;
			
		case 'DefaultText':
			helpImageIcon = '';
			helpHeading = 'Funktions beskrivning';
			helpContent = 'Klicka p� knappen/funktionen du vill veta mer om. G�ller f�r knappar/funktioner d�r muspekaren blir till ett fr�getecken n�r man f�r musen �ver densamma.';
			break;
		default:
			helpHeading = 'Ingen hj�lp till detta!';
			helpContent = 'Klicka p� en knapp som har en hj�lp-muspekare...';
	}
	
	
	if(helpHeading != ''){
		if(helpImageIcon != ''){
			sHeading = '<table border="0" cellpadding="0" cellspacing="0"><tr><td class="imEditHelpHeading">' + helpHeading + '</td><td>&nbsp;&nbsp;</td><td height="23"><button disabled class=button style="cursor:default"><img src="' + helpImageIcon + '"></button></td></tr></table>';
		} else if(helpImageIconNew != ''){
			sHeading = '<table border="0" cellpadding="0" cellspacing="0"><tr><td class="imEditHelpHeading">' + helpHeading + '</td><td>&nbsp;&nbsp;</td><td height="23">' + helpImageIconNew + '</td></tr></table>';
		} else {
			sHeading = '<table border="0" cellspacing="0" cellpadding="0"><tr><td height="23" class="imEditHelpHeading">' + helpHeading + '</td></tr></table>';
		}
		helpHeadingLayer.innerHTML = sHeading;
		helpContentLayer.innerHTML = helpContent;
	}
}