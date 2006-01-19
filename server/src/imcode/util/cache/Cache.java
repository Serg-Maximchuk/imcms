package imcode.util.cache;

public interface Cache {

    Object get(Object key);

    void put(Object key, Object value);

    void remove(Object key);
}