package com.btr.proxy.selector.misc;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * **************************************************************************
 * Implements a cache that can be used to warp it around an existing ProxySelector.
 * You can specify a maximum cache size and a "time to live" for positive resolves.
 *
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 *         **************************************************************************
 */

public class BufferedProxySelector extends ProxySelector {
// ------------------------------ FIELDS ------------------------------

    private ProxySelector delegate;

    private ConcurrentHashMap<String, CacheEntry> cache;
    private int maxSize;
    private long ttl;

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * **********************************************************************
     * Constructor
     *
     * @param maxSize  the max size for the cache.
     * @param ttl      the "time to live" for cache entries as amount in milliseconds.
     * @param delegate the delegate to use.
     *                 **********************************************************************
     */

    public BufferedProxySelector(int maxSize, long ttl, ProxySelector delegate) {
        super();
        this.cache = new ConcurrentHashMap<String, CacheEntry>();
        this.maxSize = maxSize;
        this.delegate = delegate;
        this.ttl = ttl;
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * **********************************************************************
     * connectFailed
     *
     * @see java.net.ProxySelector#connectFailed(java.net.URI, java.net.SocketAddress, java.io.IOException)
     *      **********************************************************************
     */

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        this.delegate.connectFailed(uri, sa, ioe);
    }

    /**
     * **********************************************************************
     * select
     *
     * @see java.net.ProxySelector#select(java.net.URI)
     *      **********************************************************************
     */

    @Override
    public List<Proxy> select(URI uri) {
        //String cacheKey = uri.getHost(); // Caching per host may produce wrong results
        String cacheKey = uri.toString();

        CacheEntry entry = this.cache.get(cacheKey);
        if (entry == null || entry.isExpired()) {
            List<Proxy> result = this.delegate.select(uri);
            entry = new CacheEntry(result, System.nanoTime() + this.ttl * 1000 * 1000);

            synchronized (this.cache) {
                if (this.cache.size() >= this.maxSize) {
                    purgeCache();
                }
                this.cache.put(cacheKey, entry);
            }
        }

        return entry.result;
    }

    /**
     * **********************************************************************
     * Purge cache to get some free space for a new entry.
     * **********************************************************************
     */

    private void purgeCache() {
        // Remove all expired entries and find the oldest.
        boolean removedOne = false;
        Entry<String, CacheEntry> oldest = null;

        Set<Entry<String, CacheEntry>> entries = this.cache.entrySet();
        for (Iterator<Entry<String, CacheEntry>> it = entries.iterator(); it.hasNext(); ) {
            Entry<String, CacheEntry> entry = it.next();
            if (entry.getValue().isExpired()) {
                it.remove();
                removedOne = true;
            } else if (oldest == null || entry.getValue().expireAt < oldest.getValue().expireAt) {
                oldest = entry;
            }
        }

        // Remove oldest if no expired entries were found.
        if (!removedOne && oldest != null) {
            this.cache.remove(oldest.getKey());
        }
    }

// -------------------------- INNER CLASSES --------------------------

    private static class CacheEntry {
        List<Proxy> result;
        long expireAt;

        public CacheEntry(List<Proxy> r, long expireAt) {
            super();
            this.result = new ArrayList<Proxy>(r.size());
            this.result.addAll(r);
            this.result = Collections.unmodifiableList(this.result);
            this.expireAt = expireAt;
        }

        public boolean isExpired() {
            return System.nanoTime() >= this.expireAt;
        }
    }
}