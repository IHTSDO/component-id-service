package org.snomed.cis.config;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

    @Bean
    public net.sf.ehcache.CacheManager ehCachemanager()
    {
        //To Cache UserAccount returned from Token
        CacheConfiguration acccountconfig = new CacheConfiguration();
        acccountconfig.setName("account-cache");
        acccountconfig.setMemoryStoreEvictionPolicy("LFU");
        acccountconfig.setTimeToLiveSeconds(600);
        acccountconfig.setMaxEntriesLocalHeap(1000);
        acccountconfig.setMaxEntriesLocalDisk(10000);
        acccountconfig.setTimeToIdleSeconds(300);

        //To Cache Token
        CacheConfiguration tokenconfig = new CacheConfiguration();
        tokenconfig.setName("token-cache");
        tokenconfig.setMemoryStoreEvictionPolicy("LFU");
        tokenconfig.setTimeToLiveSeconds(600);
        tokenconfig.setMaxEntriesLocalHeap(1000);
        tokenconfig.setMaxEntriesLocalDisk(10000);
        tokenconfig.setTimeToIdleSeconds(300);

        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
        config.addCache(acccountconfig);
        config.addCache(tokenconfig);
        return net.sf.ehcache.CacheManager.newInstance(config);
    }

    @Bean
    public org.springframework.cache.CacheManager cacheManager() {
        return new EhCacheCacheManager(ehCachemanager());
    }
}
