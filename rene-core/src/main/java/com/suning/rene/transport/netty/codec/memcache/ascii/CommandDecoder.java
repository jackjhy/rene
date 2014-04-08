package com.suning.rene.transport.netty.codec.memcache.ascii;

import com.suning.rene.transport.memcache.MemCacheMessage;
import com.suning.rene.transport.memcache.Op;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.LineBasedFrameDecoder;
import static com.suning.rene.transport.netty.Utils.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiger on 14-4-2.
 */
public class CommandDecoder extends ByteToMessageDecoder {

	private final static int DEFAULT_COMMAND_MAX_LENGTH = 1000;
	private static final Charset US_ASCII = Charset.forName("US-ASCII");
	private static LineBasedFrameDecoder lineBasedFrameDecoder = new LineBasedFrameDecoder(
			DEFAULT_COMMAND_MAX_LENGTH);

	private boolean isReady = true;
	private MemCacheMessage message;

	private boolean discarding;
	private int discardedBytes;

	public CommandDecoder() {

	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		if (isReady) {
			ByteBuf command = commandDecode(ctx, in);
			if (command != null && command.readableBytes() > 0) {
				// ByteBuf[] b = splitByBlank(command);
				String[] s = command.toString(US_ASCII).split(" ");
				// message =
				// MemCacheMessage.newMessage(Op.FindOp(b[0].toString(US_ASCII)),b[1].toString(US_ASCII),atoi(b[4]),atoi(b[2]));
				message = MemCacheMessage.newMessage(Op.FindOp(s[0]), s[1],
						Integer.parseInt(s[4]), Integer.parseInt(s[2]));
				byte[] data = dataDecode(ctx, in, message.getHeader()
						.getDataSize());
				if (data == null)
					isReady = false;
				else
					out.add(message);
			}
		} else {
			byte[] data = dataDecode(ctx, in, message.getHeader().getDataSize());
			if (data != null) {
				isReady = true;
				out.add(message);
			}
		}

	}

	protected ByteBuf commandDecode(ChannelHandlerContext ctx, ByteBuf buffer) {
		final int eol = findEndOfLine(buffer);
		if (!discarding) {
			if (eol >= 0) {
				final ByteBuf frame;
				final int length = eol - buffer.readerIndex();
				final int delimLength = buffer.getByte(eol) == '\r' ? 2 : 1;

				if (length > DEFAULT_COMMAND_MAX_LENGTH) {
					buffer.readerIndex(eol + delimLength);
					fail(ctx, length);
					return null;
				}

				frame = buffer.readBytes(length);
				buffer.skipBytes(delimLength);

				return frame;
			} else {
				final int length = buffer.readableBytes();
				if (length > DEFAULT_COMMAND_MAX_LENGTH) {
					discardedBytes = length;
					buffer.readerIndex(buffer.writerIndex());
					discarding = true;
					fail(ctx, "over " + discardedBytes);
				}
				return null;
			}
		} else {
			if (eol >= 0) {
				final int length = discardedBytes + eol - buffer.readerIndex();
				final int delimLength = buffer.getByte(eol) == '\r' ? 2 : 1;
				buffer.readerIndex(eol + delimLength);
				discardedBytes = 0;
				discarding = false;
				fail(ctx, length);
			} else {
				discardedBytes = buffer.readableBytes();
				buffer.readerIndex(buffer.writerIndex());
			}
			return null;
		}
	}

	protected byte[] dataDecode(ChannelHandlerContext ctx, ByteBuf in,
			int length) {
		int rl = in.writerIndex() - in.readerIndex();
		if (rl >= length + 2) {
			ByteBuf data = in.readBytes(length);
			in.skipBytes(2);
			return data.array();
		} else
			return null;
	}

	/**
	 * Returns the index in the buffer of the end of line found. Returns -1 if
	 * no end of line was found in the buffer.
	 */
	private static int findEndOfLine(final ByteBuf buffer) {
		final int n = buffer.writerIndex();
		for (int i = buffer.readerIndex(); i < n; i++) {
			final byte b = buffer.getByte(i);
			if (b == '\n') {
				return i;
			} else if (b == '\r' && i < n - 1 && buffer.getByte(i + 1) == '\n') {
				return i; // \r\n
			}
		}
		return -1; // Not found.
	}

	public static ByteBuf[] splitByBlank(final ByteBuf buffer) {
		ArrayList<ByteBuf> l = new ArrayList<ByteBuf>();
		int n = buffer.writerIndex();
		for (int i = buffer.readerIndex(); i < n; i++) {
			if (buffer.getByte(i) == ' ') {
				l.add(buffer.readBytes(i - buffer.readerIndex()));
				buffer.readByte();
			}
		}
		l.add(buffer.readBytes(buffer.writerIndex() - buffer.readerIndex()));
		return l.toArray(new ByteBuf[]{});
	}

	private void fail(final ChannelHandlerContext ctx, int length) {
		fail(ctx, String.valueOf(length));
	}

	private void fail(final ChannelHandlerContext ctx, String length) {
		ctx.fireExceptionCaught(new DecoderException(
				"uncorrected command length:" + length));
	}
}
