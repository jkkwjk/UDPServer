import java.net.InetAddress;

public interface UDPHandleProcess {
	/**
	 * 当接收到UDP数据时调用该接口函数
	 * @param data 接收到的数据
	 * @param address 发送方地址
	 * @param port 发送方端口
	 * @return 链式调用. true: 允许其他handle进行处理, 反之拒绝
	 */
	Boolean handle(String data, InetAddress address, int port);
}
