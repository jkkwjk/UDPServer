package model;

import java.net.InetAddress;
import java.util.Objects;

public class AppUser {
	private InetAddress address;
	private Integer port;

	public AppUser(InetAddress address, Integer port) {
		this.address = address;
		this.port = port;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AppUser appUser = (AppUser) o;
		return Objects.equals(address, appUser.address) &&
				Objects.equals(port, appUser.port);
	}

	@Override
	public int hashCode() {
		return Objects.hash(address, port);
	}
}
