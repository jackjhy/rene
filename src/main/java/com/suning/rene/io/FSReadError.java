package com.suning.rene.io;

import java.io.File;

/**
 * Created by tiger on 14-3-25.
 */
public class FSReadError extends FSError {
	public FSReadError(Throwable cause, File path) {
		super(cause, path);
	}

	public FSReadError(Throwable cause, String path) {
		this(cause, new File(path));
	}

	@Override
	public String toString() {
		return "FSReadError in " + path;
	}
}
