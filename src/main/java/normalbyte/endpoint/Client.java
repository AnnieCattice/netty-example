package normalbyte.endpoint;
/**
 * 普通字节传输（小于8192）例如发送16进制的数据
 * @author Annie
 *
 */

import java.io.File;
import java.io.FileInputStream;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import normalbyte.ClientHandler;

/**
 * 普通字节传输（小于8192）,例如发送16进制大于8192字节的字符串文件流
 * 
 * @author Annie
 *
 */
public class Client {
	private static final int port = 2333;
	private static final String host = "localhost";
	// 待发送的16位进制的对象数据
	private String hexDump;

	public Client beforeStart(Object msg) {
		byte[] binaryByte = msg.toString().getBytes();
		// 将二进制转换成16进制字符串
		this.hexDump = ByteBufUtil.hexDump(binaryByte);
		System.err.println("初始化16进制：" + this.hexDump+"\n16进制字符串的长度："+this.hexDump.length());
		return this;
	}

	public void run() {
		Bootstrap client = new Bootstrap();
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			client.group(group).channel(NioSocketChannel.class).remoteAddress(host, port)
					.option(ChannelOption.SO_KEEPALIVE, true).handler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new ObjectEncoder());
							ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
							ch.pipeline().addLast(new ClientHandler());
						}
					});
			ChannelFuture f = client.connect().sync();
			if (f.isSuccess()) {
				System.out.println("---成功连接到服务器---");
			}
			f.channel().writeAndFlush(this.hexDump);
			f.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully();
		}
	}

	public static void main(String[] args) {
		File file = new File("F:\\test.txt");
		String s = "";
		try(FileInputStream in = new FileInputStream(file)){
			byte[]b = new byte[1024];
			while(in.read(b)!=-1){
				s = new String(b)+s;
			}
		}catch(Exception e){
			
		}
		System.err.println("文件字符串长度："+s.length());
//		new Client().beforeStart(new String("turn on the light")).run();
		new Client().beforeStart(s).run();
	}
}
