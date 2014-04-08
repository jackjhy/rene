package com.suning.rene.transport.netty.codec.memcache.ascii;

import com.suning.rene.transport.memcache.MemCacheMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;

/**
 * Created by tiger on 14-4-2.
 */
public class CommandEncoder extends MessageToByteEncoder {
	private static final Charset US_ASCII = Charset.forName("US-ASCII");
	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out)
			throws Exception {
		MemCacheMessage mcm = (MemCacheMessage) msg;
		out.writeBytes(((MemCacheMessage) msg).responseToByte(US_ASCII));
	}
}
