package imcode.util.cache;

import java.util.Map;

public class MapCache implements Cache {

    private Map map;

    public MapCache(Map map) {
        this.map = map ;
    }

    public Object get(Object key) {
        return map.get(key) ;
    }

    public void put(Object key, Object value) {
        map.put(key, value) ;
    }

    public void remove(Object key) {
        map.remove(key);
    }
}
