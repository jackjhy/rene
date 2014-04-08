package com.suning.rene.transport.memcache;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tiger on 14-4-2.
 */
public enum Op {
	GET, GETS, APPEND, PREPEND, DELETE, DECR, INCR, REPLACE, ADD, SET, CAS, STATS, VERSION, QUIT, FLUSH_ALL, VERBOSITY, VALUE;

	private static Map<String, Op> opsbf = new HashMap<String, Op>();

	static {
		for (int x = 0; x < Op.values().length; x++) {
			opsbf.put(Op.values()[x].toString(), Op.values()[x]);
		}
	}

	public static Op FindOp(String cmd) {
		return opsbf.get(cmd);
	}

}
