package com.imcode.imcms.addon.imagearchive.validator;

import com.imcode.imcms.addon.imagearchive.command.EditKeywordCommand;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class EditKeywordValidator implements Validator {

    public void validate(Object target, Errors errors) {
        EditKeywordCommand command = (EditKeywordCommand) target;

        String keywordName = StringUtils.trimToNull(command.getEditKeywordName());

        if (keywordName == null) {
            errors.rejectValue("editKeywordName", "fieldEmptyError");

        } else if (keywordName.length() > 50) {
            errors.rejectValue("editKeywordName", "fieldLengthError", new Object[]{50}, "???");

        }

        command.setEditKeywordName(keywordName);
    }

    @SuppressWarnings("unchecked")
    public boolean supports(Class klass) {
        return EditKeywordCommand.class.isAssignableFrom(klass);
    }
}