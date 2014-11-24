package net.pardini.proxy.autodetect;

/**
 * Created with IntelliJ IDEA.
 * User: pardini
 * Date: 15/09/13
 * Time: 20:53
 * To change this template use File | Settings | File Templates.
 */
public interface ProxyAutodetector {

    public ProxyInfo detectProxyForURL(String url);

}
