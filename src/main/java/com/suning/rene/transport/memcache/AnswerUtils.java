package com.suning.rene.transport.memcache;

import java.nio.charset.Charset;

/**
 * Created by tiger on 14-4-3.
 */
public class AnswerUtils {

	private final static Charset US_ASCII = Charset.forName("us-ascii");

	public static final byte[] STORED = "STORED".getBytes(US_ASCII);
	public static final byte[] NOT_STORED = "NOT_STORED".getBytes(US_ASCII);
	public static final byte[] ERROR = "ERROR".getBytes(US_ASCII);
	public static final byte[] END = "END".getBytes(US_ASCII);
	public static final byte[] TRUE = new byte[]{1};
	public static final byte[] FALSE = new byte[]{0};
}
