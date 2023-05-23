package net.azib.ipscan.gui.fetchers;

import net.azib.ipscan.fetchers.Fetcher;
import net.azib.ipscan.fetchers.FetcherPrefs;
import net.azib.ipscan.gui.PreferencesDialog;

/**
 * WebExtensionFetcherPrefs - just opens the appropriate tab of the PreferencesDialog
 *
 * @author GoogleX
 */
public class WebExtensionFetcherPrefs implements FetcherPrefs {

	private PreferencesDialog preferencesDialog;

	public WebExtensionFetcherPrefs(PreferencesDialog preferencesDialog) {
		this.preferencesDialog = preferencesDialog;
	}

	public void openFor(Fetcher fetcher) {
		preferencesDialog.openTab(3);
	}
	
}
