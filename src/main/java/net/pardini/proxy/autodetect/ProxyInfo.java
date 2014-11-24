package net.pardini.proxy.autodetect;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: pardini
 * Date: 15/09/13
 * Time: 23:04
 * To change this template use File | Settings | File Templates.
 */
public class ProxyInfo {

    private Proxy proxy;
    private String host;
    private int port;
    private String user;
    private String password;

    private static Pattern pattern = Pattern.compile("(\\S+?):(\\S+?)@(\\S+?)");

    public ProxyInfo(final Proxy proxy) {
        this.proxy = proxy;
        if (proxy.type() == Proxy.Type.HTTP) {
            InetSocketAddress addr = (InetSocketAddress) proxy.address();
            if (addr != null) {
                String hostName = addr.getHostName();
                Matcher matcher = pattern.matcher(hostName);
                if(matcher.matches()) {
                    this.user = matcher.group(1);
                    this.password = matcher.group(2);
                    this.host = matcher.group(3);
                } else {
                    this.host = hostName;
                }
                this.port = addr.getPort();
            }
        }
    }

    public Proxy getProxy() {
        return proxy;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "ProxyInfo{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
