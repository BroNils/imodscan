package net.azib.ipscan.fetchers;

import net.azib.ipscan.config.LoggerFactory;
import net.azib.ipscan.config.ScannerConfig;
import net.azib.ipscan.core.ScanningResult;
import net.azib.ipscan.core.ScanningSubject;
import net.azib.ipscan.core.values.WebExtensionMatcher;
import net.azib.ipscan.gui.fetchers.WebExtensionFetcherPrefs;
import net.azib.ipscan.util.HTTPRequester;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.currentThread;
import static java.util.Collections.singleton;
import static net.azib.ipscan.fetchers.PortsFetcher.PARAMETER_OPEN_PORTS;

/**
 * WebExtensionFetcher - detects specified ext for open ports.
 *
 * @author GoogleX
 */
public class WebExtensionFetcher extends AbstractFetcher {
	private static final Logger LOG = LoggerFactory.getLogger();
	private ScannerConfig scannerConfig;
	private boolean scanOpenPorts;
	private Map<String, ArrayList<Integer>> results;

	public WebExtensionFetcher(ScannerConfig scannerConfig) {
		this.scannerConfig = scannerConfig;
		this.scanOpenPorts = true;
	}

	public WebExtensionMatcher getWebExtensionMatcher(){
		return this.scannerConfig.webExtensionMatcher;
	}

	@Override
	public Class<? extends FetcherPrefs> getPreferencesClass() {
		return WebExtensionFetcherPrefs.class;
	}

	public Object scan(ScanningSubject subject) {
		this.results = new HashMap<>();
		Iterator<Integer> portIterator = getPortIterator(subject);

		while (portIterator.hasNext() && !currentThread().isInterrupted()) {
			int currentPort = portIterator.next();

			String response;
			try {
				HTTPRequester requester = new HTTPRequester(subject.getAddress().getHostAddress(), currentPort);
				requester.TIMEOUT = subject.getAdaptedPortTimeout() == 0 ? 10000 : subject.getAdaptedPortTimeout();
				response = requester.send();
			} catch (Exception e) {
				LOG.warning("Failed to fetch data from " + subject.getAddress().getHostAddress() + ":" + currentPort + ": " + e.getMessage());
				continue;
			}

			Iterator<Map<String, String>> webExtensionIterator = getWebExtensionMatcher().iterator();

			while (webExtensionIterator.hasNext() && !currentThread().isInterrupted()) {
				Map<String, String> currentWebExtension = webExtensionIterator.next();

				if (!results.containsKey(currentWebExtension.get("name"))) {
					results.put(currentWebExtension.get("name"), new ArrayList<>());
				}
				ArrayList<Integer> existInPorts = results.get(currentWebExtension.get("name"));

				Pattern matchingRegexp = Pattern.compile(currentWebExtension.get("matcher"), Pattern.MULTILINE);
				Matcher matcher = matchingRegexp.matcher(response);
				if (matcher.find()) {
					existInPorts.add(currentPort);
				}
				// return response + " " + currentWebExtension.get("name") + " " + currentWebExtension.get("matcher") + " " + currentPort;
			}
		}

		// mark that additional info is available
		subject.setResultType(ScanningResult.ResultType.WITH_PORTS);

		// return the required contents
		return getResult();
	}
	
	public String getId() {
		return "fetcher.webExtension";
	}

	private String getResult() {
		StringBuilder sb = new StringBuilder();
		int currentIndex = 0;
		for (Map.Entry<String, ArrayList<Integer>> entry : results.entrySet()) {
			if (entry.getValue().size() == 0) {
				if (currentIndex >= results.size() - 1 && sb.length() > 3) {
					sb.delete(sb.length() - 3 - (System.lineSeparator().length()), sb.length());
				}

				currentIndex++;
				continue;
			}

			sb.append(entry.getKey()).append(": ");
			for (Integer port : entry.getValue()) {
				sb.append(port).append(", ");
			}
			sb.delete(sb.length() - 2, sb.length());

			if (currentIndex < results.size() - 1) {
				sb.append(" | ");
				sb.append(System.lineSeparator());
			}

			currentIndex++;
		}

		return sb.length() == 0 ? "" : sb.toString();
	}

	private Iterator<Integer> getPortIterator(ScanningSubject subject) {
		if (scanOpenPorts) {
			@SuppressWarnings("unchecked")
			SortedSet<Integer> openPorts = (SortedSet<Integer>) subject.getParameter(PARAMETER_OPEN_PORTS);
			if (openPorts != null) {
				SortedSet<Integer> ports = new TreeSet<>(openPorts);
				ports.add(80);
				return ports.iterator();
			}
		}
		return subject.isAnyPortRequested() ? subject.requestedPortsIterator() : singleton(80).iterator();
	}
}
