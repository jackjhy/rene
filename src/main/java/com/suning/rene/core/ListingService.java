package com.suning.rene.core;

/**
 * Created by tiger on 14-3-22.
 */
public class ListingService implements IMCCommand {
	@Override
	public void add(String key) {
	}

	@Override
	public void set(String KeySubKey) {
	}

	@Override
	public boolean get(String keySubKey) {
		return false;
	}

	@Override
	public boolean del(String string) {
		return false;
	}

	@Override
	public boolean replace(String key) {
		return false;
	}

	@Override
	public String stats() {
		return null;
	}

}
