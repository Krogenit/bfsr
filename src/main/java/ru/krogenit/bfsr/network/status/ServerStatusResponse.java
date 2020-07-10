package ru.krogenit.bfsr.network.status;

public class ServerStatusResponse {
	
	private int playerCount;
	private String version;

	public ServerStatusResponse(int playerCount, String version) {
		this.playerCount = playerCount;
		this.version = version;
	}

	public int getPlayerCountData() {
		return this.playerCount;
	}

	public void setPlayerCount(int playerCount) {
		this.playerCount = playerCount;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}