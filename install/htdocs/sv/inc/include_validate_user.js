/*
	remember to include the global variable:
	var isAddPage = 0/1; // add or change page
	...
	and the FORM-event:
	onSubmit="checkValues('FORM_NAME'); return false"
*/

/* Validating functions */

function isMail(sAddress,isMandatory) {
	if (isMandatory) {
		return /\b(^(\S+@).+(\..{2,5})$)\b/gi.test(sAddress);
	} else {
		if (sAddress.length) {
			return /\b(^(\S+@).+(\..{2,5})$)\b/gi.test(sAddress);
		} else {
			return true;
		}
	}
}

/* Simple string-checker */

function isOk(theVal,theCharPattern,isMandatory,iLenMin,iLenMax) {
	iLenMin = (!isNaN(iLenMin) && iLenMin) ? iLenMin : 1;
	iLenMax = (!isNaN(iLenMax) && iLenMax) ? iLenMax : 10000;
	var re = new RegExp('^' + theCharPattern + '{' + iLenMin + ',' + iLenMax + '}$', 'g');
	if (isMandatory) {
		return re.test(theVal);
	} else {
		if (theVal.length) {
			return re.test(theVal);
		} else {
			return true;
		}
	}
}

/* Check before submit */

function checkValues(theForm) {
	var f = eval('document.forms.' + theForm);
	var errStr = '';
	var strFields = '';
	var strFields2 = '';
	// check names
	if (!isOk(f.first_name.value,'.',1,1,null)) strFields += '\n - F�rnamn';
	if (!isOk(f.last_name.value,'.',1,1,null)) strFields += '\n - Efternamn';
	if (!isOk(f.company.value,'.',1,1,null)) strFields += '\n - F�retag';
	// check mail - empty
	if (!isOk(f.email.value,'.',1,1,null)) strFields += '\n - E-postadress';
	// check passwords - empty on "add-page"
	if (isAddPage && !isOk(f.password1.value,'[a-z0-9]',1,1,null)) strFields += '\n - Nytt l�senord';
	if (isAddPage && !isOk(f.password2.value,'[a-z0-9]',1,1,null)) strFields += '\n - Repetera l�senord';
	// let's put the fields together
	if (strFields != '') errStr = 'Du har gl�mt att fylla i f�lten:' + strFields;
	// check mail - correct
	if (!isMail(f.email.value,1)) errStr += '\n\nDin e-postadress verkar inte vara korrekt ifylld!';
	// check passwords - to short on "add-page"
	if (isAddPage && !isOk(f.password1.value,'[a-z0-9]',1,6,null)) errStr += '\n\nDet f�rsta l�senordet (nytt) �r f�r kort eller felaktigt ifyllt!\nM�ste vara minst 6st siffror och/eller bokst�ver!';
	if (isAddPage && !isOk(f.password2.value,'[a-z0-9]',1,6,null)) errStr += '\n\nDet andra l�senordet (repetera) �r f�r kort eller felaktigt ifyllt!\nM�ste vara minst 6st siffror och/eller bokst�ver!';
	// compare new passwords
	if (isAddPage && f.password1.value != f.password2.value) errStr += '\n\nL�senordet m�ste vara samma i b�da l�senordsrutorna!';
	// check passwords - not empty on "change-page". compare/validate
	if (!isAddPage) {
		var hasPW0 = (f.password_current.value != '') ? 1 : 0;
		var hasPW1 = (f.password1.value != '') ? 1 : 0;
		var hasPW2 = (f.password2.value != '') ? 1 : 0;
		if (hasPW0 || hasPW1 || hasPW2) {
			if (hasPW0 && (!hasPW1 || !hasPW2)) errStr += '\n\nDu m�ste ange ditt nya l�senord om du vill byta!';
			if ((hasPW1 || hasPW2) && !hasPW0) errStr += '\n\nDu m�ste ange ditt gamla l�senord om du vill byta!';
			if (f.password1.value != f.password2.value) errStr += '\n\nL�senordet m�ste vara samma i b�da l�senordsrutorna!';
			if (!isOk(f.password_current.value,'.',1,1,null)) strFields2 += '\n - Gammalt l�senord';
			if (!isOk(f.password1.value,'[a-z0-9]',1,6,null)) strFields2 += '\n - Nytt l�senord';
			if (!isOk(f.password2.value,'[a-z0-9]',1,6,null)) strFields2 += '\n - Repetera l�senord';
			if (strFields2 != '') errStr += '\n\nDessa l�senord �r f�r korta eller felaktigt ifyllda!' + strFields2 + '\nNya m�ste vara minst 6st siffror och/eller bokst�ver!\n';
		}
	}
	// doit!
	if (errStr != '') {
		alert(errStr);
	} else {
		f.submit();
	}
}