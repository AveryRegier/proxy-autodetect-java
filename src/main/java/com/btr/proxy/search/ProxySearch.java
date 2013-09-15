package com.btr.proxy.search;

import com.btr.proxy.search.env.EnvProxySearchStrategy;
import com.btr.proxy.search.java.JavaProxySearchStrategy;
import com.btr.proxy.search.wpad.WpadProxySearchStrategy;
import com.btr.proxy.selector.misc.BufferedProxySelector;
import com.btr.proxy.selector.misc.ProxyListFallbackSelector;
import com.btr.proxy.selector.pac.PacProxySelector;
import com.btr.proxy.util.ProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.List;

/**
 * **************************************************************************
 * Main class to setup and initialize the proxy detection system.<br/>
 * This class can be used to select a proxy discovery strategy.<br/>
 * Implements the "Builder" pattern.<br/>
 * Use <code>addStrategy</code> to add one or more search strategies.<br/>
 * If you are done call the <code>getProxySelector</code> method. <br/>
 * Then the strategies are asked one after the other for a ProxySelector until
 * an valid selector is found. <br/>
 * <p>
 * Invoke the static <code>getDefaultProxySearch</code> method to use a default search strategy.
 * </p>
 *
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 *         **************************************************************************
 */

public class ProxySearch implements ProxySearchStrategy {
// ------------------------------ FIELDS ------------------------------

    private static Logger log = LoggerFactory.getLogger(ProxySearch.class);

    private static final int DEFAULT_PAC_CACHE_SIZE = 20;

    private static final long DEFAULT_PAC_CACHE_TTL = 1000 * 60 * 10; // 10 Minutes

    private List<ProxySearchStrategy> strategies;
    private int pacCacheSize;
    private long pacCacheTTL;

// -------------------------- STATIC METHODS --------------------------

    /**
     * **********************************************************************
     * Sets up a ProxySearch that uses a default search strategy suitable for
     * every platform.
     *
     * @return a ProxySearch initialized with default settings.
     *         **********************************************************************
     */

    public static ProxySearch getDefaultProxySearch() {
        ProxySearch s = new ProxySearch();

        s.addStrategy(Strategy.JAVA);
        s.addStrategy(Strategy.ENV_VAR);
        s.addStrategy(Strategy.WPAD);
        log.debug("Using default search priority: {0}", s);

        return s;
    }

    /**
     * **********************************************************************
     * Adds an search strategy to the list of proxy searches strategies.
     *
     * @param strategy the search strategy to add.
     *                 **********************************************************************
     */

    public void addStrategy(Strategy strategy) {
        switch (strategy) {
            case WPAD:
                this.strategies.add(new WpadProxySearchStrategy());
                break;
            case ENV_VAR:
                this.strategies.add(new EnvProxySearchStrategy());
                break;
            case JAVA:
                this.strategies.add(new JavaProxySearchStrategy());
                break;
            default:
                throw new IllegalArgumentException("Unknown strategy code!");
        }
    }

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * **********************************************************************
     * Constructor
     * **********************************************************************
     */
    public ProxySearch() {
        super();
        this.strategies = new ArrayList<ProxySearchStrategy>();
        this.pacCacheSize = DEFAULT_PAC_CACHE_SIZE;
        this.pacCacheTTL = DEFAULT_PAC_CACHE_TTL;
    }

// ------------------------ CANONICAL METHODS ------------------------

    /**
     * **********************************************************************
     * toString
     *
     * @see java.lang.Object#toString()
     *      **********************************************************************
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Proxy search: ");
        for (ProxySearchStrategy strat : this.strategies) {
            sb.append(strat);
            sb.append(" ");
        }
        return sb.toString();
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface ProxySearchStrategy ---------------------

    /**
     * **********************************************************************
     * Gets the proxy selector that will use the configured search order.
     *
     * @return a ProxySelector, null if none was found for the current
     *         builder configuration.
     *         **********************************************************************
     */

    public ProxySelector getProxySelector() {
        log.debug("Executing search strategies to find proxy selector");
        for (ProxySearchStrategy strat : this.strategies) {
            try {
                ProxySelector selector = strat.getProxySelector();
                if (selector != null) {
                    selector = installBufferingAndFallbackBehaviour(selector);
                    return selector;
                }
            } catch (ProxyException e) {
                log.debug("Strategy {0} failed trying next one.", e);
                // Ignore and try next strategy.
            }
        }

        return null;
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * **********************************************************************
     * If it is PAC and we have caching enabled set it here.
     *
     * @param selector
     * @return **********************************************************************
     */
    private ProxySelector installBufferingAndFallbackBehaviour(ProxySelector selector) {
        if (selector instanceof PacProxySelector) {
            if (this.pacCacheSize > 0) {
                selector = new BufferedProxySelector(this.pacCacheSize, this.pacCacheTTL, selector);
            }
            selector = new ProxyListFallbackSelector(selector);
        }
        return selector;
    }

    /**
     * **********************************************************************
     * Sets the cache size of the PAC proxy selector cache.
     * This defines the number of URLs that are cached together with the PAC
     * script result. This improves performance because for URLs that are
     * in the cache the script is not executed again.
     * You have to set this before you add any strategies that may create a
     * PAC script proxy selector.
     *
     * @param size of the cache. Set it to 0 to disable caching.
     * @param ttl  is the time to live of the cache entries as amount of milliseconds.
     *             **********************************************************************
     */

    public void setPacCacheSettings(int size, long ttl) {
        this.pacCacheSize = size;
        this.pacCacheTTL = ttl;
    }

// -------------------------- ENUMERATIONS --------------------------

    /**
     * **************************************************************************
     * Types of proxy detection supported by the builder.
     * **************************************************************************
     */
    public enum Strategy {
        // Use WPAD
        WPAD,
        /// Use environment variables for proxy settings.
        ENV_VAR,
        /// Use Java Networking system properties
        JAVA
    }
}
