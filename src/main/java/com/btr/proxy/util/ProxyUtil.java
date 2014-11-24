package com.btr.proxy.util;

import com.btr.proxy.selector.fixed.FixedProxySelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * **************************************************************************
 * Small helper class for some common utility methods.
 *
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 *         **************************************************************************
 */

public class ProxyUtil {
// ------------------------------ FIELDS ------------------------------

    public static final int DEFAULT_PROXY_PORT = 80;

    private static List<Proxy> noProxyList;

    private static Pattern pattern = Pattern.compile("\\w*?:?/*([^:/]+):?(\\d*)/?");
    private static Pattern authenticatedPattern = Pattern.compile("(\\S+?)://((\\S+?):(\\S+?)@(\\S+?)):?(\\d*)");
    private Logger log = LoggerFactory.getLogger(getClass());

// -------------------------- STATIC METHODS --------------------------

    /**
     * **********************************************************************
     * Parse host and port out of a proxy variable.
     *
     * @param proxyVar the proxy string. example: http://192.168.10.9:8080/
     * @return a FixedProxySelector using this settings, null on parse error.
     *         **********************************************************************
     */
    public static FixedProxySelector parseProxySettings(String proxyVar) {
        if (proxyVar == null || proxyVar.trim().length() == 0) {
            return null;
        }
        Matcher matcher = pattern.matcher(proxyVar);
        if (matcher.matches()) {
            return getFixedProxySelector(matcher, 1, 2);
        } else {
            matcher = authenticatedPattern.matcher(proxyVar);
            if(matcher.matches()) {
                return getFixedProxySelector(matcher, 2, 6);
            }
            return null;
        }
    }

    private static FixedProxySelector getFixedProxySelector(Matcher matcher, int hostGroup, int portGroup) {
        String host = matcher.group(hostGroup);
        int port;
        if (!"".equals(matcher.group(portGroup))) {
            port = Integer.parseInt(matcher.group(portGroup));
        } else {
            port = DEFAULT_PROXY_PORT;
        }
        return new FixedProxySelector(host.trim(), port);
    }

    /**
     * **********************************************************************
     * Gets an unmodifiable proxy list that will have as it's only entry an DIRECT proxy.
     *
     * @return a list with a DIRECT proxy in it.
     *         **********************************************************************
     */

    public static synchronized List<Proxy> noProxyList() {
        if (noProxyList == null) {
            ArrayList<Proxy> list = new ArrayList<Proxy>(1);
            list.add(Proxy.NO_PROXY);
            noProxyList = Collections.unmodifiableList(list);
        }
        return noProxyList;
    }
}
