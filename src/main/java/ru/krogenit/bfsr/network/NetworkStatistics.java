package ru.krogenit.bfsr.network;

import java.util.concurrent.atomic.AtomicReference;

public class NetworkStatistics {
	private final NetworkStatistics.Tracker receiveTracker = new NetworkStatistics.Tracker();
	private final NetworkStatistics.Tracker sendTracker = new NetworkStatistics.Tracker();

	public void addReceive(int id, long bytesCount) {
		this.receiveTracker.track(id, bytesCount);
	}

	public void addSend(int id, long bytesCount) {
		this.sendTracker.track(id, bytesCount);
	}

	static class PacketStatData {
		private long totalBytes;
		private int count;
		private double averageBytes;

		private PacketStatData(long totalBytes, int count, double averageBytes) {
			this.totalBytes = totalBytes;
			this.count = count;
			this.averageBytes = averageBytes;
		}

		PacketStatData() { }

		public NetworkStatistics.PacketStatData addBytes(long bytesCount) {
			return new NetworkStatistics.PacketStatData(bytesCount + this.totalBytes, this.count + 1, (double) ((bytesCount + this.totalBytes) / (long) (this.count + 1)));
		}

		public String toString() {
			return "{totalBytes=" + this.totalBytes + ", count=" + this.count + ", averageBytes=" + this.averageBytes + '}';
		}
	}

	static class Tracker {
		private final AtomicReference<NetworkStatistics.PacketStatData>[] packetStatistics = new AtomicReference[100];

		public Tracker() {
			for (int i = 0; i < 100; ++i) {
				this.packetStatistics[i] = new AtomicReference<>(new NetworkStatistics.PacketStatData());
			}
		}

		public void track(int id, long bytesCount) {
			try {
				if (id < 0 || id >= 100) { return; }

				NetworkStatistics.PacketStatData packetstatdata;
				NetworkStatistics.PacketStatData packetstatdata1;

				do {
					packetstatdata = this.packetStatistics[id].get();
					packetstatdata1 = packetstatdata.addBytes(bytesCount);
				} while (!this.packetStatistics[id].compareAndSet(packetstatdata, packetstatdata1));
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}
}