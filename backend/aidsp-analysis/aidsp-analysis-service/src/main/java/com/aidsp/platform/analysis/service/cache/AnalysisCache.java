package com.aidsp.platform.analysis.service.cache;

import com.aidsp.platform.analysis.api.AnalysisResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 分析结果本地缓存。
 * <p>基于 LRU + TTL 的简单内存缓存；最大 1000 条。
 */
@Slf4j
@Component
public class AnalysisCache {

    /** 缓存条目（值 + 过期时间）。 */
    private record Entry(AnalysisResultDTO value, long expireAt) {
    }

    private static final int MAX_SIZE = 1000;

    private final long ttlSeconds;

    private final Map<String, Entry> store = Collections.synchronizedMap(
            new LinkedHashMap<>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
                    return size() > MAX_SIZE;
                }
            }
    );

    public AnalysisCache(@Value("${aidsp.analysis.cache.ttl-seconds:300}") long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public void put(String queryId, AnalysisResultDTO dto) {
        if (queryId == null || dto == null) {
            return;
        }
        long expireAt = System.currentTimeMillis() + ttlSeconds * 1000L;
        store.put(queryId, new Entry(dto, expireAt));
        log.debug("[AnalysisCache] put queryId={}, ttl={}s", queryId, ttlSeconds);
    }

    public AnalysisResultDTO get(String queryId) {
        if (queryId == null) {
            return null;
        }
        Entry e = store.get(queryId);
        if (e == null) {
            return null;
        }
        if (System.currentTimeMillis() > e.expireAt) {
            store.remove(queryId, e);
            return null;
        }
        return e.value;
    }
}
