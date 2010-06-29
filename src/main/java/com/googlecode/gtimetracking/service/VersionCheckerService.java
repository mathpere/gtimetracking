package com.googlecode.gtimetracking.service;

import java.util.Comparator;

import org.springframework.beans.factory.annotation.Required;

public class VersionCheckerService {

	private static class VersionStringComparator implements Comparator<String> {

		public int compare(String s1, String s2) {

			if (s1 == null && s2 == null) {
				return 0;
			} else if (s1 == null) {
				return -1;
			} else if (s2 == null) {
				return 1;
			}

			String[] arr1 = s1.split("[^a-zA-Z0-9]+");
			String[] arr2 = s2.split("[^a-zA-Z0-9]+");

			int i1;
			int i2;
			int i3;

			for (int ii = 0, max = Math.min(arr1.length, arr2.length); ii <= max; ii++) {
				if (ii == arr1.length) {
					return ii == arr2.length ? 0 : -1;
				} else if (ii == arr2.length) {
					return 1;
				}

				try {
					i1 = Integer.parseInt(arr1[ii]);
				} catch (Exception x) {
					i1 = Integer.MAX_VALUE;
				}

				try {
					i2 = Integer.parseInt(arr2[ii]);
				} catch (Exception x) {
					i2 = Integer.MAX_VALUE;
				}

				if (i1 != i2) {
					return i1 - i2;
				}

				i3 = arr1[ii].compareTo(arr2[ii]);

				if (i3 != 0)
					return i3;
			}
			return 0;
		}
	}

	private final static VersionStringComparator VERSION_STRING_COMPARATOR = new VersionStringComparator();

	private String currentVersion;
	private String latestVersion;
	private String downloadUrl;
	private UIService uiService;

	public void checksVersion() {

		if (latestVersion != null
				&& latestVersion.equals("${project.latestversion}")) {
			return;
		}

		int compare = VERSION_STRING_COMPARATOR.compare(currentVersion,
				latestVersion);

		if (compare < 0) {
			boolean promptInformation = uiService.promptInformation(
					"Update Available !", "An update is available. "
							+ "Click to download the latest version "
							+ latestVersion);

			if (promptInformation) {
				try {
					uiService.browse(downloadUrl);
				} catch (Exception e) {
				}
			}
		}
	}

	@Required
	public void setCurrentVersion(String currentVersion) {
		this.currentVersion = currentVersion;
	}

	@Required
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public void setLatestVersion(String latestVersion) {
		this.latestVersion = latestVersion;
	}

	@Required
	public void setUiService(UIService uiService) {
		this.uiService = uiService;
	}
}