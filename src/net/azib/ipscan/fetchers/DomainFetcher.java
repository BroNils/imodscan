package net.azib.ipscan.fetchers;

import net.azib.ipscan.config.LoggerFactory;
import net.azib.ipscan.core.ScanningSubject;

import javax.net.ssl.*;
import java.net.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

/**
 * DomainFetcher retrieves domain for IP Address by some method.
 * 
 * @author GoogleX
 */
public class DomainFetcher extends AbstractFetcher {
	private static final Logger LOG = LoggerFactory.getLogger();

	public static final String ID = "fetcher.domain";
	public String getId() {
		return ID;
	}

	public DomainFetcher() {}

	private String extractCommonName(String subject) {
		String[] parts = subject.split(",");
		for (String part : parts) {
			if (part.trim().startsWith("CN=")) {
				return part.trim().substring(3);
			}
		}
		return "";
	}

	private String resolveWithSSLCommonName(ScanningSubject subject) {
		try {
//			@SuppressWarnings("unchecked")
//			SortedSet<Integer> openPorts = (SortedSet<Integer>) subject.getParameter(PARAMETER_OPEN_PORTS);
			// if (!openPorts.contains(443)) return null;

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

			URL urlObj = new URL("https://"+subject.getAddress().getHostAddress());
			HttpsURLConnection connection = (HttpsURLConnection) urlObj.openConnection();
			connection.setConnectTimeout(subject.getAdaptedPortTimeout() == 0 ? 10000 : subject.getAdaptedPortTimeout());
			connection.setReadTimeout(subject.getAdaptedPortTimeout() == 0 ? 10000 : subject.getAdaptedPortTimeout());

			connection.connect();
			Optional<SSLSession> sslSession = connection.getSSLSession();
			if (sslSession.isEmpty()) return null;

			Certificate[] certificates = sslSession.get().getPeerCertificates();
			X509Certificate serverCertificate = (X509Certificate) certificates[0];
			String commonName = serverCertificate.getSubjectX500Principal().getName();
			connection.disconnect();

			return extractCommonName(commonName);
		}
		catch (Exception e) {
			LOG.log(WARNING, "Failed to get hostname by SSL CN " + subject, e);
			return null;
		}
	}

	public Object scan(ScanningSubject subject) {
		return resolveWithSSLCommonName(subject);
	}
}
