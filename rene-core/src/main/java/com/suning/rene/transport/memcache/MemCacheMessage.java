package com.suning.rene.transport.memcache;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by tiger on 14-4-2.
 */
public class MemCacheMessage {

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {

		this.data = data;
	}

	Header header;
	byte[] data;

	public class Header {
		Op op;
		String key;
		int flags;

		public Op getOp() {
			return op;
		}

		public void setOp(Op op) {
			this.op = op;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public int getFlags() {
			return flags;
		}

		public void setFlags(int flags) {
			this.flags = flags;
		}

		public long getSsl() {
			return ssl;
		}

		public void setSsl(long ssl) {
			this.ssl = ssl;
		}

		public int getDataSize() {
			return dataSize;
		}

		public void setDataSize(int dataSize) {
			this.dataSize = dataSize;
		}

		public boolean isNoReply() {
			return noReply;
		}

		public void setNoReply(boolean noReply) {
			this.noReply = noReply;
		}

		long ssl;
		int dataSize;
		boolean noReply;

		public Header(Op op, String key, int dataSize, int flags) {
			this.op = op;
			this.key = key;
			this.dataSize = dataSize;
			this.flags = flags;
		}

		public ByteBuf responseToByte(Charset charset) {
			byte[] cmd = op.name().getBytes(charset);
			byte[] keyb = key.getBytes(charset);
			ByteBuf result = Unpooled.buffer(100);
			result.writeBytes(cmd).writeChar(' ').writeBytes(keyb)
					.writeChar(' ').writeInt(flags).writeChar(' ')
					.writeInt(dataSize).writeChar('\r').writeChar('\n');
			return result
					.readBytes(result.writerIndex() - result.readerIndex());
		}
	}

	public static MemCacheMessage newMessage(Op op, String key, int dataLength,
			int flags) {
		MemCacheMessage m = new MemCacheMessage();
		m.header = m.new Header(op, key, dataLength, flags);
		return m;
	}

	public ByteBuf responseToByte(Charset charset) {
		ByteBuf result = Unpooled.buffer(100, 1000);
		if (header != null)
			result.writeBytes(header.responseToByte(charset));
		result.writeBytes(data).writeChar('\r').writeChar('\n');
		if (header != null && header.op == Op.VALUE)
			result.writeBytes(AnswerUtils.END).writeChar('\r').writeChar('\n');
		return result.readBytes(result.writerIndex() - result.readerIndex());
	}
}
