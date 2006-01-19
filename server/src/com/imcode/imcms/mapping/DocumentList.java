package com.imcode.imcms.mapping;

import imcode.server.document.DocumentDomainObject;

import java.util.*;
import java.io.Serializable;

class DocumentList extends AbstractList implements Serializable {

    private ArrayList list;
    private Map map;

    DocumentList(int capacity) {
        list = new ArrayList(capacity);
        map = Collections.synchronizedMap(new LinkedHashMap(capacity));
    }

    public synchronized Object remove(int index) {
        Object o = list.remove(index);
        DocumentDomainObject document = (DocumentDomainObject) o;
        map.remove(new Integer(document.getId()));
        return o;
    }

    public synchronized Object set(int index, Object o) {
        DocumentDomainObject document = (DocumentDomainObject) o;
        DocumentDomainObject previousDocument = (DocumentDomainObject) list.set(index, o);
        if ( null != previousDocument ) {
            map.remove(new Integer(previousDocument.getId()));
        }
        map.put(new Integer(document.getId()), document);
        return previousDocument;
    }

    public synchronized Object get(int index) {
        return list.get(index);
    }

    public synchronized Iterator iterator() {
        return list.iterator();
    }

    public synchronized boolean add(Object o) {
        DocumentDomainObject document = (DocumentDomainObject) o;
        map.put(new Integer(document.getId()), document);
        return list.add(o);
    }

    public synchronized int size() {
        return list.size();
    }

    public synchronized Map getMap() {
        return map;
    }
}
