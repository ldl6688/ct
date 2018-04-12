package utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @ Autheor:ldl
 * @Description:
 * @Date: 2018/3/24 11:26
 * @Modified By:
 */

public class LRUCache<K,V> extends LinkedHashMap<K,V> {


    private static final long serialVersionUID = -7173389351827735562L;

    protected int maxElements;

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return (size() > this.maxElements);
    }

    public LRUCache(int maxSize) {
        super(maxSize, 0.75F, true);
        this.maxElements = maxSize;
    }


}
