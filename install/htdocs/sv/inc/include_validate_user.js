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
	if (!isOk(f.first_name.value,'.',1,1,null)) strFields += '\n - Förnamn';
	if (!isOk(f.last_name.value,'.',1,1,null)) strFields += '\n - Efternamn';
	if (!isOk(f.company.value,'.',1,1,null)) strFields += '\n - Företag';
	// check mail - empty
	if (!isOk(f.email.value,'.',1,1,null)) strFields += '\n - E-postadress';
	// check passwords - empty on "add-page"
	if (isAddPage && !isOk(f.password1.value,'[a-z0-9]',1,1,null)) strFields += '\n - Nytt lösenord';
	if (isAddPage && !isOk(f.password2.value,'[a-z0-9]',1,1,null)) strFields += '\n - Repetera lösenord';
	// let's put the fields together
	if (strFields != '') errStr = 'Du har glömt att fylla i fälten:' + strFields;
	// check mail - correct
	if (!isMail(f.email.value,1)) errStr += '\n\nDin e-postadress verkar inte vara korrekt ifylld!';
	// check passwords - to short on "add-page"
	if (isAddPage && !isOk(f.password1.value,'[a-z0-9]',1,6,null)) errStr += '\n\nDet första lösenordet (nytt) är för kort eller felaktigt ifyllt!\nMåste vara minst 6st siffror och/eller bokstäver!';
	if (isAddPage && !isOk(f.password2.value,'[a-z0-9]',1,6,null)) errStr += '\n\nDet andra lösenordet (repetera) är för kort eller felaktigt ifyllt!\nMåste vara minst 6st siffror och/eller bokstäver!';
	// compare new passwords
	if (isAddPage && f.password1.value != f.password2.value) errStr += '\n\nLösenordet måste vara samma i båda lösenordsrutorna!';
	// check passwords - not empty on "change-page". compare/validate
	if (!isAddPage) {
		var hasPW0 = (f.password_current.value != '') ? 1 : 0;
		var hasPW1 = (f.password1.value != '') ? 1 : 0;
		var hasPW2 = (f.password2.value != '') ? 1 : 0;
		if (hasPW0 || hasPW1 || hasPW2) {
			if (hasPW0 && (!hasPW1 || !hasPW2)) errStr += '\n\nDu måste ange ditt nya lösenord om du vill byta!';
			if ((hasPW1 || hasPW2) && !hasPW0) errStr += '\n\nDu måste ange ditt gamla lösenord om du vill byta!';
			if (f.password1.value != f.password2.value) errStr += '\n\nLösenordet måste vara samma i båda lösenordsrutorna!';
			if (!isOk(f.password_current.value,'.',1,1,null)) strFields2 += '\n - Gammalt lösenord';
			if (!isOk(f.password1.value,'[a-z0-9]',1,6,null)) strFields2 += '\n - Nytt lösenord';
			if (!isOk(f.password2.value,'[a-z0-9]',1,6,null)) strFields2 += '\n - Repetera lösenord';
			if (strFields2 != '') errStr += '\n\nDessa lösenord är för korta eller felaktigt ifyllda!' + strFields2 + '\nNya måste vara minst 6st siffror och/eller bokstäver!\n';
		}
	}
	// doit!
	if (errStr != '') {
		alert(errStr);
	} else {
		f.submit();
	}
}