package de.may.ac.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Data;

@Data
public class ImageInfo {

	public static final Pattern FILENAME_PATTERN = Pattern.compile("(\\d+)(-(\\d+))?\\.png");
	
	private String id;

	private int timeout;

	private String filename;

	public ImageInfo(String filename, int defaultTimeout) {
		this.filename = filename;
		Matcher matcher = FILENAME_PATTERN.matcher(filename);
		if(!matcher.matches()) {
			throw new IllegalArgumentException("File schema does not match!");
		}
		id = matcher.group(1);
		if (matcher.group(3) != null) {
			timeout = Integer.parseInt(matcher.group(3)) * 1000;
		} else {
			timeout = defaultTimeout;
		}
	}

}
