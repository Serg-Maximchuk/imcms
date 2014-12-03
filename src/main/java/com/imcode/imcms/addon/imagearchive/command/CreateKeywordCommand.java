package com.imcode.imcms.addon.imagearchive.command;

import java.io.Serializable;

public class CreateKeywordCommand implements Serializable {
    private static final long serialVersionUID = -6961175507802518276L;

    private String createKeywordName;

    public CreateKeywordCommand() {
    }

    public String getCreateKeywordName() {
        return createKeywordName;
    }

    public void setCreateKeywordName(String createKeywordName) {
        this.createKeywordName = createKeywordName;
    }
}