package com.suning.rene.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * Created by tiger on 14-3-21.
 */
public class SNUtilities {
	public static long abs(long index) {
		long negbit = index >> 63;
		return (index ^ negbit) - negbit;
	}

	public static void waitOnFutures(Iterable<Future<?>> futures) {
		for (Future f : futures)
			waitOnFuture(f);
	}

	public static <T> T waitOnFuture(Future<T> future) {
		try {
			return future.get();
		} catch (ExecutionException ee) {
			throw new RuntimeException(ee);
		} catch (InterruptedException ie) {
			throw new AssertionError(ie);
		}
	}

    public static InetAddress getHostAddress(){
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if(addr instanceof Inet4Address) return addr;
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
