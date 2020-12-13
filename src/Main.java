import model.AppUser;

import java.io.*;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Main {
	private static final String appId = "abc";
//	private static final String appId = UUID.randomUUID().toString();
	public static void main(String[] args) throws IOException {
		ESP8266UDPServer server = new ESP8266UDPServer(2333);

		UDPHandleProcess test1 = (data, address, port) -> {
			server.send(data, address, port);
			return !data.equals("no next");
		};
		UDPHandleProcess test2 = (data, address, port) -> {
			server.send("test2", address, port);
			return true;
		};



		server.register(appId, Arrays.asList(test1, test2));


		Scanner sc = new Scanner(System.in);
		while (sc.hasNext()) {
			String s = sc.next();
			if (s.equals("o")) {
				List<AppUser> appUsers = server.getOnlineList(appId);
				appUsers.forEach(t -> System.out.println(t.getAddress()+":"+t.getPort()));
			}else if (s.equals("s")) {
				AppUser appUser = server.getOnlineList(appId).get(0);
				server.send("s", appUser.getAddress(), appUser.getPort());
			}else if (s.equals("sall")) {
				server.send("sall", server.getOnlineList(appId));
			}else if (s.equals("sc")) {
				server.sendAndCheckOnline(appId,"sc", server.getOnlineList(appId),3000);
			}
		}
	}
}
