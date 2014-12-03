package com.imcode.imcms.addon.imagearchive.validator;

import com.imcode.imcms.addon.imagearchive.command.CreateKeywordCommand;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class CreateKeywordValidator implements Validator {

    public void validate(Object target, Errors errors) {
        CreateKeywordCommand command = (CreateKeywordCommand) target;

        String keywordName = StringUtils.trimToNull(command.getCreateKeywordName());

        if (keywordName == null) {
            errors.rejectValue("createKeywordName", "fieldEmptyError");

        } else if (keywordName.length() > 50) {
            errors.rejectValue("createKeywordName", "fieldLengthError", new Object[]{50}, "???");

        }

        command.setCreateKeywordName(keywordName);
    }

    @SuppressWarnings("unchecked")
    public boolean supports(Class klass) {
        return CreateKeywordCommand.class.isAssignableFrom(klass);
    }
}
