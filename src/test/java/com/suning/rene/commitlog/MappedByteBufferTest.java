package com.suning.rene.commitlog;

import com.suning.rene.ServerDescriptor;
import com.suning.rene.core.BloomCalculations;
import com.suning.rene.core.BloomFilterContainer;
import com.suning.rene.transport.memcache.Op;
import com.suning.rene.transport.netty.codec.memcache.ascii.CommandDecoder;
import com.suning.rene.utils.FileUtils;
import com.suning.rene.utils.SNUtilities;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.testng.annotations.Test;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Created by tiger on 14-3-26.
 */
public class MappedByteBufferTest {
//	@Test
	public void testDataOutputStream() {

		// cls.discard(true);
	}

//	@Test
	public void testRandomAccessReader() throws IOException {

		byte[] b = new byte[1000];
		int i = 0;
	}

//	@Test
	public void testMain() throws IOException {
	}

//	@Test
	public void testBloomCal() {
		System.out.println(BloomCalculations.maxBucketsPerElement(200000000));
		System.out.println(BloomCalculations.computeBloomSpec(10, 0.0005));
		System.out.println(BloomCalculations.computeBloomSpec(1));
		System.out.println(BloomCalculations.computeBloomSpec(2));
		System.out.println(BloomCalculations.computeBloomSpec(3));
		System.out.println(BloomCalculations.computeBloomSpec(4));
	}

//	@Test
	public void testbyte() throws IOException {
	}

//	@Test
	public void testNetty() {

		String s = "sdv/dsds";
		int index = s.indexOf('/');
		System.out.println(s.substring(0, index) + "......."
				+ s.substring(index + 1));

		ByteBuf b = Unpooled.wrappedBuffer(new byte[]{'a', ' ', 1, ' ', ' ',
				' ', 'c', 'd',});
		System.out.println(b.readerIndex() + "......." + b.writerIndex()
				+ "..........." + b.capacity());
		b.writerIndex(0);
		System.out.println(b.readerIndex() + "......." + b.writerIndex()
				+ "..........." + b.capacity());
		b.writeBoolean(false);
		System.out.println(b.readerIndex() + "......." + b.writerIndex()
				+ "..........." + b.capacity());
		// byte[] s = new byte[1];
		// b.readBytes(s);
		// System.out.println(s[0]);
		b.writeBytes(new byte[]{'a', 'b'});
		System.out.println(b.readerIndex() + "......." + b.writerIndex()
				+ "..........." + b.capacity());

		System.out.println("");
		// Bootstrap bootstrap = new ServerBootstrap(new
		// NioServerSocketChannelFactory(Executors.newCachedThreadPool(),Executors.newCachedThreadPool()));
		// bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
		// @Override
		// public ChannelPipeline getPipeline() throws Exception {
		// ChannelPipeline pipeline = Channels.pipeline();
		// pipeline.addLast("ss",Mem);
		// return null;
		// }
		// });
	}

//	@Test
	public void testProperties() throws UnknownHostException {
        InetAddress address = SNUtilities.getHostAddress();
	}

//	@Test
	public void testNamePattern() {
		Pattern p = Pattern.compile("neg.bf");
		boolean b = p.matcher("neg.bf").matches();
		System.out.print(b);
	}

}
