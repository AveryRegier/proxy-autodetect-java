package net.pardini.proxy.autodetect;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: pardini
 * Date: 15/09/13
 * Time: 20:57
 * To change this template use File | Settings | File Templates.
 */
public class SmartProxyAutodetectorTest {

    private Logger log = LoggerFactory.getLogger(getClass());
    private ProxyAutodetector proxyAutodetector;


    @Before
    public void setUp() throws Exception {
        proxyAutodetector = new SmartProxyAutodetector();
    }

    @Test
    public void testDetectProxyForURL() throws Exception {
        ProxyInfo proxyHttp = proxyAutodetector.detectProxyForURL("http://www.google.com");
        log.info("Proxy for http url is {}", proxyHttp);
        ProxyInfo proxyHttps = proxyAutodetector.detectProxyForURL("https://www.google.com");
        log.info("Proxy for https url is {}", proxyHttps);
    }

}
