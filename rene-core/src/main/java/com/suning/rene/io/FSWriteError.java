package com.suning.rene.io;

import java.io.File;

/**
 * Created by tiger on 14-3-25.
 */
public class FSWriteError extends FSError {
	public FSWriteError(Throwable cause, File path) {
		super(cause, path);
	}

	public FSWriteError(Throwable cause, String path) {
		this(cause, new File(path));
	}

	@Override
	public String toString() {
		return "FSWriteError in " + path;
	}
}