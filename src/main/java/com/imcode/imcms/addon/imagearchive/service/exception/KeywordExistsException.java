package com.imcode.imcms.addon.imagearchive.service.exception;


public class KeywordExistsException extends RuntimeException {
    private static final long serialVersionUID = -3884469330589233434L;

    public KeywordExistsException() {
    }

    public KeywordExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeywordExistsException(String message) {
        super(message);
    }

    public KeywordExistsException(Throwable cause) {
        super(cause);
    }
}