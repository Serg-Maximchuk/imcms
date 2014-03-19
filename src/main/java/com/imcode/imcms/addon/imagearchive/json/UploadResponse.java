package com.imcode.imcms.addon.imagearchive.json;

import java.util.List;
import java.util.Map;


public class UploadResponse {
    private List<String> imageErrors;
    private Map<String, String> dataErrors;
    private String redirect;
    private String redirectOnAllComplete;

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public List<String> getImageErrors() {
        return imageErrors;
    }

    public void setImageErrors(List<String> imageErrors) {
        this.imageErrors = imageErrors;
    }

    public Map<String, String> getDataErrors() {
        return dataErrors;
    }

    public void setDataErrors(Map<String, String> dataErrors) {
        this.dataErrors = dataErrors;
    }

    public String getRedirectOnAllComplete() {
        return redirectOnAllComplete;
    }

    public void setRedirectOnAllComplete(String redirectOnAllComplete) {
        this.redirectOnAllComplete = redirectOnAllComplete;
    }
}
