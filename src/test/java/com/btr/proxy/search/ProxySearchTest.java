package com.btr.proxy.search;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: pardini
 * Date: 15/09/13
 * Time: 20:09
 * To change this template use File | Settings | File Templates.
 */
public class ProxySearchTest {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Test
    public void testGetDefaultProxySearch() throws Exception {
        ProxySearch defaultProxySearch = ProxySearch.getDefaultProxySearch();
        ProxySelector proxySelector = defaultProxySearch.getProxySelector();
        if (proxySelector == null) {
            System.out.println("No proxy detected.");
            return;
        }
        List<Proxy> select = proxySelector.select(new URI("http://www.me.com.br"));
        for (Proxy proxy : select) {
            System.out.println(proxy.toString());

        }


    }
}
