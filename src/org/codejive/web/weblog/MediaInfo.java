package org.codejive.web.weblog;

import java.io.File;

public class MediaInfo {
	private File file;
	private String title;
	private String description;
	
	public MediaInfo(File _file, String _title, String _description) {
		file = _file;
		title = _title;
		description = _description;
	}

	public File getFile() {
		return file;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}
}
