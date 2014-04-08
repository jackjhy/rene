package com.suning.rene.commitlog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tiger on 14-3-25.
 */
public class CommitLogDescriptor {

	private static final String SEPARATOR = "-";
	private static final String FILENAME_PREFIX = "CommitLog" + SEPARATOR;
	private static final String FILENAME_EXTENSION = ".log";
	// match both legacy and new version of commitlogs Ex: CommitLog-12345.log
	// and CommitLog-4-12345.log.
	private static final Pattern COMMIT_LOG_FILE_PATTERN = Pattern
			.compile(FILENAME_PREFIX + "((\\d+)(" + SEPARATOR + "\\d+)?)"
					+ FILENAME_EXTENSION);

	public static final int VERSION_12 = 2;
	public static final int VERSION_20 = 3;
	/**
	 * Increment this number if there is a changes in the commit log disc layout
	 * or MessagingVersion changes. Note: make sure to handle
	 * {@link #getMessagingVersion()}
	 */
	public static final int current_version = VERSION_20;

	private final int version;
	public final long id;

	public CommitLogDescriptor(int version, long id) {
		this.version = version;
		this.id = id;
	}

	public CommitLogDescriptor(long id) {
		this(current_version, id);
	}

	public static CommitLogDescriptor fromFileName(String name) {
		Matcher matcher;
		if (!(matcher = COMMIT_LOG_FILE_PATTERN.matcher(name)).matches())
			throw new RuntimeException("Cannot parse the version of the file: "
					+ name);

		if (matcher.group(3) == null)
			throw new UnsupportedOperationException(
					"Commitlog segment is too old to open; upgrade to 1.2.5+ first");

		long id = Long.parseLong(matcher.group(3).split(SEPARATOR)[1]);
		return new CommitLogDescriptor(Integer.parseInt(matcher.group(2)), id);
	}

	public int getMessagingVersion() {
		switch (version) {
			case VERSION_12 :
				return VERSION_12;
			case VERSION_20 :
				return VERSION_20;
			default :
				throw new IllegalStateException("Unknown commitlog version "
						+ version);
		}
	}

	public String fileName() {
		return FILENAME_PREFIX + version + SEPARATOR + id + FILENAME_EXTENSION;
	}

	/**
	 * @param filename
	 *            the filename to check
	 * @return true if filename could be a commit log based on it's filename
	 */
	public static boolean isValid(String filename) {
		return COMMIT_LOG_FILE_PATTERN.matcher(filename).matches();
	}
}