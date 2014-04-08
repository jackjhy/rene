package com.suning.rene.core;

/**
 * Created by tiger on 14-3-21.
 */
public interface IMCCommand {

	public void add(String key) throws ReneException;

	public void set(String keySubKey) throws ReneException;

	public boolean get(String keySubKey) throws ReneException;

	public boolean del(String string) throws ReneException;

	public boolean replace(String key) throws ReneException;

	public String stats() throws ReneException;
}
