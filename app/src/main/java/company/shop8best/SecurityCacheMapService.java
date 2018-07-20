package company.shop8best;

import net.jodah.expiringmap.ExpiringMap;

import java.util.concurrent.TimeUnit;

/**
 * Created by dat9 on 18/01/18.
 */

public enum SecurityCacheMapService {

    INSTANCE;

    private ExpiringMap<String,String> oauth2CacheMap;

    SecurityCacheMapService(){
        oauth2CacheMap = ExpiringMap.builder().variableExpiration().build();
    }

    public void putToCache(String key, String value, long expireTime){
        oauth2CacheMap.put(key,value, ExpiringMap.ExpirationPolicy.CREATED,expireTime, TimeUnit.SECONDS);
    }

    public boolean exists(String key) {
        return this.oauth2CacheMap.containsKey(key);
    }

    public void removeFromCache(String key){
        oauth2CacheMap.remove(key);
    }

    public String get(String key){
        return oauth2CacheMap.get(key);
    }
}
