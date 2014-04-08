package com.suning.rene;

import com.suning.rene.core.BloomFilter;
import com.suning.rene.core.BloomFilterContainer;
import com.suning.rene.transport.netty.MemCacheCommandHandler;
import com.suning.rene.transport.netty.codec.memcache.ascii.CommandDecoder;
import com.suning.rene.transport.netty.codec.memcache.ascii.CommandEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tiger on 14-4-7.
 */
public class ServerMain {
	private static final Logger logger = LoggerFactory
			.getLogger(ServerMain.class);
	public static void main(String[] args) throws InterruptedException {
		// check home
		String home = ServerDescriptor.getRootPath();
		if (home == null || home.trim().length() == 0) {
			logger.error("rene home is needed");
			return;
		}

		// get network setting -- netty
		// ascii port
		int asciiPort = ServerDescriptor.getAsciiPort();
		int threadSize = ServerDescriptor.getWorkerThreadNumber();
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup(threadSize);

		// start network --text protocol
		try {
			ServerBootstrap b = new ServerBootstrap();
			final BloomFilterContainer bc = BloomFilterContainer.instance;
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch)
								throws Exception {
							ch.pipeline().addLast(new CommandDecoder());
                            ch.pipeline().addLast(new CommandEncoder());
                            ch.pipeline().addLast(
                                    new LoggingHandler(LogLevel.DEBUG));
							ch.pipeline().addLast(
									new MemCacheCommandHandler(bc));
						}
					}).option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, true);

			ChannelFuture f = b.bind(asciiPort).sync();
			f.channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}

	}

}
