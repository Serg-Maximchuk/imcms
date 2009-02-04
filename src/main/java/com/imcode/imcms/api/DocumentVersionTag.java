package com.imcode.imcms.api;

/**
 * Document version tags.
 * 
 * For every document there might be 
 *   - at most one WORKING version
 *   - at most one PUBLISHED version
 *   - unlimited numbers of CANCELLED versions
 *   - unlimited numbers of ARCHUVED versions   
 */
public enum DocumentVersionTag {
    WORKING,
    CANCELLED,
    PUBLISHED,		
    ARCHIVED,
}