package org.jivesoftware.smack.proxy;

import javax.net.SocketFactory;

/**
 * Class which stores proxy information such as proxy type, host, port,
 * authentication etc.
 * 
 * 该类存储了代理相关的信息，如代理类型，host, port,认证等
 * 
 * @author Atul Aggarwal
 */

public class ProxyInfo {
	public static enum ProxyType {
		NONE, HTTP, SOCKS4, SOCKS5
	}

	private String proxyAddress;
	private int proxyPort;
	private String proxyUsername;
	private String proxyPassword;
	private ProxyType proxyType;
	//private String resourceID;

	public ProxyInfo(ProxyType pType, String pHost, int pPort, String pUser,
			String pPass) {
		this.proxyType = pType;
		this.proxyAddress = pHost;
		this.proxyPort = pPort;
		this.proxyUsername = pUser;
		this.proxyPassword = pPass;
	}

	public static ProxyInfo forHttpProxy(String pHost, int pPort, String pUser,
			String pPass) {
		return new ProxyInfo(ProxyType.HTTP, pHost, pPort, pUser, pPass);
	}

	public static ProxyInfo forSocks4Proxy(String pHost, int pPort,
			String pUser, String pPass) {
		return new ProxyInfo(ProxyType.SOCKS4, pHost, pPort, pUser, pPass);
	}

	public static ProxyInfo forSocks5Proxy(String pHost, int pPort,
			String pUser, String pPass) {
		return new ProxyInfo(ProxyType.SOCKS5, pHost, pPort, pUser, pPass);
	}

	public static ProxyInfo forNoProxy() {
		return new ProxyInfo(ProxyType.NONE, null, 0, null, null);
	}

	public static ProxyInfo forDefaultProxy() {
		return new ProxyInfo(ProxyType.NONE, null, 0, null, null);
	}

	public ProxyType getProxyType() {
		return proxyType;
	}

	public String getProxyAddress() {
		return proxyAddress;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public String getProxyUsername() {
		return proxyUsername;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public SocketFactory getSocketFactory() {
		if (proxyType == ProxyType.NONE) {
			return new DirectSocketFactory();
		} else if (proxyType == ProxyType.HTTP) {
			return new HTTPProxySocketFactory(this);
		} else if (proxyType == ProxyType.SOCKS4) {
			return new Socks4ProxySocketFactory(this);
		} else if (proxyType == ProxyType.SOCKS5) {
			return new Socks5ProxySocketFactory(this);
		} else {
			return null;
		}
	}
}
