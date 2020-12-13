import com.sun.istack.internal.NotNull;
import model.AppUser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class ESP8266UDPServer {
	private static Integer maxReceiveBytes = 4 * 1024;

	private DatagramSocket UDP;

	// 在线机器列表
	private Map<String, Set<AppUser>> appUserMap;
	// 层级handleProcess处理
	private Map<String, List<UDPHandleProcess>> appHandleMap;
	// 需要测试机器是否在线
	private Map<String, Set<AppUser>> checkOnlineMap;

	ESP8266UDPServer(int port) {
		appUserMap = new HashMap<>();
		appHandleMap = new HashMap<>();
		checkOnlineMap = new HashMap<>();

		new Thread(()->{
			try {
				UDP = new DatagramSocket(port);
				while (true) {
					String data = waitUtilReceive();

					if (this.handle(data, packet.getAddress(), packet.getPort())) {
						int pos = data.indexOf(',');

						if (pos != -1) {
							String appId = data.substring(0, pos);
							List<UDPHandleProcess> handleProcessList = appHandleMap.get(appId);

							if (handleProcessList != null) {
								this.online(appId, new AppUser(packet.getAddress(), packet.getPort()));

								int i = 0;
								while (i < handleProcessList.size()
										&& handleProcessList.get(i).handle(data.substring(pos + 1), packet.getAddress(), packet.getPort())) {
									++i;
								}
							}else {
								// appId 不存在
								this.send("Can't find appId", packet.getAddress(), packet.getPort());
							}
						}else {
							// 发送的数据包没有appId
							this.send("Please send appid,[data]", packet.getAddress(), packet.getPort());
						}
					}

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	public void send(String data, InetAddress address, int port){
		try {
			DatagramPacket sendPacket = new DatagramPacket(data.getBytes(), data.getBytes().length, address, port);

			UDP.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void send(String data, @NotNull List<AppUser> appUsers){
		appUsers.forEach(user -> this.send(data, user.getAddress(), user.getPort()));
	}

	public void sendAndCheckOnline(String appId, String data, List<AppUser> checkUsers, Integer timeout) {
		Set<AppUser> appUserWillCheck = this.checkOnlineMap.get(appId);

		for (AppUser checkUser : checkUsers) {
			if (appUserWillCheck.contains(checkUser)) {
				// 如果之前向该用户发送信息 且该用户未响应 则该用户下线
				appUserWillCheck.remove(checkUser);
				this.offline(appId, checkUser);
			}else {
				appUserWillCheck.add(checkUser);
				this.send(data, checkUser.getAddress(), checkUser.getPort());
			}
		}

		new Thread(() -> {
			try {
				Thread.sleep(timeout);
				for (AppUser checkUser : checkUsers) {
					if (appUserWillCheck.contains(checkUser)) {
						appUserWillCheck.remove(checkUser);

						this.appUserMap.get(appId).remove(checkUser);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}

	public void register(String appId, @NotNull List<UDPHandleProcess> handleProcessList) {
		this.appHandleMap.put(appId, handleProcessList);

		this.appUserMap.put(appId, new HashSet<>());
		this.checkOnlineMap.put(appId, new HashSet<>());
	}

	public void register(String appId) {
		this.appHandleMap.put(appId, new ArrayList<>());

		this.appUserMap.put(appId, new HashSet<>());
		this.checkOnlineMap.put(appId, new HashSet<>());
	}

	/**
	 *
	 * @param appUser
	 * @return 是否为新用户
	 */
	public boolean online(String appId, AppUser appUser) {
		this.checkOnlineMap.get(appId).remove(appUser);
		return this.appUserMap.get(appId).add(appUser);
	}

	public void offline(String appId, AppUser appUser) {
		this.appUserMap.get(appId).remove(appUser);
	}

	public List<AppUser> getOnlineList(String appId) {
		return new ArrayList<>(this.appUserMap.get(appId));
	}


	private byte[] buffer = new byte[maxReceiveBytes];
	private DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

	private String waitUtilReceive() {
		String data = null;
		try {
			UDP.receive(packet);
			data = new String(packet.getData(), packet.getOffset(), packet.getLength());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	private Boolean handle(String data, InetAddress address, int port) {
		String ret = null;
		if (data.startsWith("AT+CIPSEND")) {
			ret = "OK";
		}else if (data.startsWith("AT")) {
			// 程序在透传模式发送
			ret = "+++";
		}

		if (ret != null) {
			this.send(ret, address, port);
		}
		return ret == null;
	}
}
