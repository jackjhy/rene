package com.suning.rene.transport.netty;

import com.suning.rene.core.BloomFilterContainer;
import com.suning.rene.core.ReneException;
import com.suning.rene.transport.memcache.AnswerUtils;
import com.suning.rene.transport.memcache.MemCacheMessage;
import com.suning.rene.transport.memcache.Op;
import com.suning.rene.transport.netty.codec.memcache.ascii.CommandDecoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

import static com.suning.rene.transport.memcache.Op.ADD;
import static com.suning.rene.transport.memcache.Op.VALUE;

/**
 * Created by tiger on 14-4-2.
 */
public class MemCacheCommandHandler extends SimpleChannelInboundHandler<MemCacheMessage> {
	Logger logger = LoggerFactory.getLogger(MemCacheCommandHandler.class);

    private static final Charset US_ASCII = Charset.forName("US-ASCII");
	BloomFilterContainer container;

	public MemCacheCommandHandler(BloomFilterContainer container) {
		this.container = container;
	}

//	@Override
//	public void channelRead(ChannelHandlerContext ctx, Object msg)
//			throws Exception {
//		if (msg instanceof MemCacheMessage) {
//			MemCacheMessage mcm = (MemCacheMessage) msg;
//			ctx.write(deal(mcm));
//		} else {
//			throw new Exception("");
//		}
//	}

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MemCacheMessage msg) throws Exception {
        if (msg instanceof MemCacheMessage) {
            MemCacheMessage mcm = (MemCacheMessage) msg;
            ctx.writeAndFlush(deal(mcm));
        } else {
            throw new Exception("");
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        MemCacheMessage msg = new MemCacheMessage();
        msg.setData("Welcome to Rene!".getBytes(US_ASCII));
        ctx.writeAndFlush(msg);
    }

    @Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	public MemCacheMessage deal(MemCacheMessage message) {
		String key;
		MemCacheMessage result = null;
		try {
			switch (message.getHeader().getOp()) {
				case ADD :
					result = new MemCacheMessage();
					container.add(message.getHeader().getKey());
					result.setData(AnswerUtils.STORED);
					break;
				case SET :
					result = new MemCacheMessage();
					container.set(message.getHeader().getKey());
					result.setData(AnswerUtils.STORED);
					break;
				case GET :
					result = MemCacheMessage.newMessage(VALUE, message
							.getHeader().getKey(), 1, message.getHeader()
							.getFlags());
					if (container.get(message.getHeader().getKey()))
						result.setData(AnswerUtils.TRUE);
					else
						result.setData(AnswerUtils.FALSE);
					break;
				case DELETE :
					result = new MemCacheMessage();
					container.del(message.getHeader().getKey());
					result.setData(AnswerUtils.STORED);
					break;
				case REPLACE :
					result = new MemCacheMessage();
					container.replace(message.getHeader().getKey());
					result.setData(AnswerUtils.STORED);
					break;
				default :
					throw new ReneException(
							"do not support this kind of operation:"
									+ message.getHeader().getOp(), null);
			}
		} catch (ReneException e) {
			result = new MemCacheMessage();
			logger.warn(e.getMessage());
			result.setData(AnswerUtils.ERROR);
		}
		return result;
	}

}
