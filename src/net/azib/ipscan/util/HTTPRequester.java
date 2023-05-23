package net.azib.ipscan.util;

import net.azib.ipscan.config.LoggerFactory;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Utility class to send http request.
 */
public class HTTPRequester {
	protected static final Logger LOG = LoggerFactory.getLogger();
//	public static String USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:" + toHexString(parseInt("3" + "1" + "0" + "0", 16)) + ") Gecko/20070309 Firefox/" + toHexString(parseInt("2" + "0", 16));
//	public static String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

	public int PORT = 80;
	public String HOST_NAME = "www.angryip.org";
	public String SCHEME = "http";
	public String METHOD = "GET";
	public String PATH = "/";
	public int TIMEOUT = 10000;
	public int MAX_REDIRECTS = 5;

	private int redirectCounter = 0;

	public HTTPRequester() {}

	public HTTPRequester(String hostName, int port, String scheme) {
		this.HOST_NAME = hostName;
		this.PORT = port;
		this.SCHEME = scheme;
	}

	public HTTPRequester(String hostName, int port) {
		this(hostName, port, "http");
	}

	public HTTPRequester(String hostName) {
		this(hostName, 80);
	}

	public String urlBuilder() {
		StringBuilder url = new StringBuilder();
		url.append(SCHEME).append("://").append(HOST_NAME);
		if (PORT != 80 && !(SCHEME.equals("https") && PORT == 443)) {
			url.append(":").append(PORT);
		}
		url.append(PATH);
		return url.toString();
	}

	public String send(String path) {
		if (!SCHEME.equals("https")) {
			return sendHTTP(path);
		}

		return sendHTTPS(path);
	}

	public String send() {
		return send(PATH);
	}

	public String sendHTTP(String path) {
		this.PATH = path;
		this.SCHEME = "http";
		try {
			URL url = new URL(urlBuilder());

			HttpURLConnection.setFollowRedirects(true);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod(METHOD);
			connection.setConnectTimeout(TIMEOUT);
			connection.setReadTimeout(TIMEOUT);
			connection.setInstanceFollowRedirects(true);

			connection.connect();

			if (connection.getResponseCode() >= 300 && connection.getResponseCode() < 400) {
				String location = connection.getHeaderField("Location");

				if (location == null) {
					connection.disconnect();
					return null;
				}
				if (location.contains("https")) {
					connection.disconnect();
					return redirect(location);
				}
			}

			InputStream inputStream = connection.getResponseCode() < 400 ? connection.getInputStream() : connection.getErrorStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String response = reader.lines().collect(Collectors.joining(System.lineSeparator()));

			reader.close();
			inputStream.close();

			connection.disconnect();

			return response;
		} catch (Exception e) {
			LOG.log(Level.FINE, HOST_NAME, e);
			throw new RuntimeException(e);
		}
	}

	public String sendHTTP() {
		return sendHTTP(PATH);
	}

	public String sendHTTPS(String path) {
		this.PATH = path;
		this.SCHEME = "https";
		this.PORT = 443;
		try {
			TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType) {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			}};

			SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

			HostnameVerifier allHostsValid = (hostname, session) -> true;
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

			HttpsURLConnection.setFollowRedirects(true);
			URL urlObj = new URL(urlBuilder());
			HttpsURLConnection connection = (HttpsURLConnection) urlObj.openConnection();
			connection.setConnectTimeout(TIMEOUT);
			connection.setReadTimeout(TIMEOUT);
			connection.setRequestMethod(METHOD);
			connection.setInstanceFollowRedirects(true);

			connection.connect();

			if (connection.getResponseCode() >= 300 && connection.getResponseCode() < 400) {
				String location = connection.getHeaderField("Location");

				if (location == null) {
					connection.disconnect();
					return null;
				}
				if (location.contains("http")) {
					connection.disconnect();
					return redirect(location);
				}
			}

			InputStream inputStream = connection.getResponseCode() < 400 ? connection.getInputStream() : connection.getErrorStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String response = reader.lines().collect(Collectors.joining(System.lineSeparator()));

			reader.close();
			inputStream.close();

			connection.disconnect();

			return response;
		} catch (Exception e) {
			LOG.log(Level.FINE, HOST_NAME, e);
			// throw
			throw new RuntimeException(e);
		}
	}

	public String sendHTTPS() {
		return sendHTTPS(PATH);
	}

	public String redirect(String location) {
		if (this.redirectCounter > MAX_REDIRECTS) {
			throw new RuntimeException("Too many redirects");
		}

		URL url;
		try{
			url = new URL(location);
		} catch (Exception e) {
			LOG.log(Level.FINE, HOST_NAME, e);
			throw new RuntimeException(e);
		}

		this.SCHEME = url.getProtocol();
		this.HOST_NAME = url.getHost();
		this.PORT = url.getPort();
		this.PATH = url.getPath();
		this.redirectCounter++;

		if (SCHEME.equals("http")) {
			return sendHTTP();
		}

		return sendHTTPS();
	}
}
