import java.util.*;

// Interface for Eviction Policies
interface EvictionPolicy {
    void accessKey(String key); // Tracks key access to maintain eviction order
    void evictKey(); // Removes a key based on eviction policy
    String getEvictedKey(); // Returns the key that should be evicted
}

/// LRU Eviction Policy using LinkedHashMap (it maintains insertion/access order)
class LRUCache implements EvictionPolicy {
    private LinkedHashMap<String, String> cacheData;
    private int maxSize;  // Declare maxSize

    public LRUCache(int size) {
        this.cacheData = new LinkedHashMap<>(size, 0.75f, true);
        this.maxSize = size;  // Initialize maxSize with the constructor parameter
    }

    public boolean contains(String key) {
        return cacheData.containsKey(key);
    }

    public String get(String key) {
        return cacheData.get(key);
    }

    public void put(String key, String value) {
        if (cacheData.size() >= maxSize) {
            evictKey();  // Trigger eviction if the size exceeds the max size
        }
        cacheData.put(key, value);
    }

    public void accessKey(String key) {
        if (cacheData.containsKey(key)) {
            String value = cacheData.get(key);
            cacheData.put(key, value); // refreshes order in LinkedHashMap
        }
    }

    public void evictKey() {
        String keyToEvict = getEvictedKey();
        if (keyToEvict != null) {
            cacheData.remove(keyToEvict);
        }
    }

    public String getEvictedKey() {
        if (cacheData.isEmpty()) {
            return null; // Handle empty map scenario
        }
        return cacheData.keySet().iterator().next(); // Return the first key in the map
    }

    public void displayCache() {
        System.out.println("Cache content: " + cacheData);
    }
}

// LFU Eviction Policy using PriorityQueue
class LFUCache implements EvictionPolicy {
    private Map<String, Integer> freqMap;
    private PriorityQueue<String> freqQueue;

    public LFUCache() {
        freqMap = new HashMap<>();
        freqQueue = new PriorityQueue<>(Comparator.comparingInt(freqMap::get));
    }

    public void accessKey(String key) {
        freqMap.put(key, freqMap.getOrDefault(key, 0) + 1);
        freqQueue.remove(key);
        freqQueue.add(key);
    }

    public void evictKey() {
        String evictedKey = freqQueue.poll();
        freqMap.remove(evictedKey);
    }

    public String getEvictedKey() {
        return freqQueue.peek();
    }
}

class CacheLevel {
    private int size;
    private EvictionPolicy evictionPolicy;
    private Map<String, String> cacheData;

    public CacheLevel(int size, String evictionPolicyType) {
        this.size = size;
        this.cacheData = new HashMap<>();

        if (evictionPolicyType.equals("LRU")) {
            this.evictionPolicy = new LRUCache(size);
        } else if (evictionPolicyType.equals("LFU")) {
            this.evictionPolicy = new LFUCache();
        } else {
            throw new IllegalArgumentException("Unknown eviction policy");
        }
    }

    public boolean contains(String key) {
        return cacheData.containsKey(key);
    }

    public String get(String key) {
        if (!cacheData.containsKey(key)) return null;
        evictionPolicy.accessKey(key);
        return cacheData.get(key);
    }

    public void put(String key, String value) {
        if (cacheData.size() >= size) {
            String keyToEvict = evictionPolicy.getEvictedKey();
            if (keyToEvict != null) {
                evictionPolicy.evictKey(); // Evict a key based on policy
                cacheData.remove(keyToEvict);
            }
        }
        cacheData.put(key, value);
        evictionPolicy.accessKey(key);
    }

    public void displayCache() {
        System.out.println("Cache data: " + cacheData);
    }
}

class CacheSystem {
    private List<CacheLevel> cacheLevels;

    public CacheSystem() {
        this.cacheLevels = new ArrayList<>();
    }

    // Add a new cache level with a given size and eviction policy
    public void addCacheLevel(int size, String evictionPolicy) {
        cacheLevels.add(new CacheLevel(size, evictionPolicy));
    }

    // Retrieve the data from the cache levels
    public String get(String key) {
        for (CacheLevel level : cacheLevels) {
            String value = level.get(key);
            if (value != null) {
                // If data is found, move it up to the L1 cache
                moveToTopCache(key, value);
                return value;
            }
        }
        return null; // Cache miss
    }

    // Insert the data into the L1 cache
    public void put(String key, String value) {
        if (!cacheLevels.isEmpty()) {
            CacheLevel topCache = cacheLevels.get(0);
            topCache.put(key, value);
        }
    }

    // Move the key-value pair to the top (L1) cache if found in a lower cache
    private void moveToTopCache(String key, String value) {
        CacheLevel topCache = cacheLevels.get(0);
        topCache.put(key, value);
    }

    // Remove a specific cache level
    public void removeCacheLevel(int level) {
        if (level < 1 || level > cacheLevels.size()) {
            throw new IllegalArgumentException("Invalid cache level");
        }
        cacheLevels.remove(level - 1);
    }

    // Display the current state of each cache level
    public void displayCache() {
        int level = 1;
        for (CacheLevel cacheLevel : cacheLevels) {
            System.out.println("Cache Level " + level + ":");
            cacheLevel.displayCache();
            level++;
        }
    }
}

public class Main {
    public static void main(String[] args) {
        CacheSystem cacheSystem = new CacheSystem();
        
        // Add cache levels with different eviction policies
        cacheSystem.addCacheLevel(3, "LRU");
        cacheSystem.addCacheLevel(2, "LFU");

        cacheSystem.put("A", "1");
        cacheSystem.put("B", "2");
        cacheSystem.put("C", "3");

        // Display state after initial inserts
        System.out.println("After Initial Inserts:");
        cacheSystem.displayCache();

        // Get data from cache and display again
        System.out.println("Get A: " + cacheSystem.get("A"));
        cacheSystem.put("D", "4");

        System.out.println("After Inserting D (with eviction):");
        cacheSystem.displayCache();

        // Fetch a value from L2 and move to L1
        System.out.println("Get C: " + cacheSystem.get("C"));
        cacheSystem.displayCache();
    }
}