package com.imcode.imcms.addon.imagearchive.command;

import java.io.Serializable;

public class EditKeywordCommand implements Serializable {
    private static final long serialVersionUID = 6013450352240491688L;

    private long editKeywordId;
    private String editKeywordName;

    public EditKeywordCommand() {
    }

    public long getEditKeywordId() {
        return editKeywordId;
    }

    public void setEditKeywordId(long editKeywordId) {
        this.editKeywordId = editKeywordId;
    }

    public String getEditKeywordName() {
        return editKeywordName;
    }

    public void setEditKeywordName(String editKeywordName) {
        this.editKeywordName = editKeywordName;
    }
}