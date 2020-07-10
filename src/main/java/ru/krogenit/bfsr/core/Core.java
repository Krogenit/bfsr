package ru.krogenit.bfsr.core;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ListenableFutureTask;
import org.joml.Vector3f;
import org.lwjgl.openal.AL11;
import ru.krogenit.bfsr.client.gui.Gui;
import ru.krogenit.bfsr.client.gui.ingame.GuiInGame;
import ru.krogenit.bfsr.client.gui.menu.GuiMainMenu;
import ru.krogenit.bfsr.client.input.Keyboard;
import ru.krogenit.bfsr.client.language.Lang;
import ru.krogenit.bfsr.client.render.Renderer;
import ru.krogenit.bfsr.client.sound.SoundListener;
import ru.krogenit.bfsr.client.sound.SoundManager;
import ru.krogenit.bfsr.network.NetworkManager;
import ru.krogenit.bfsr.network.Packet;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.profiler.Profiler;
import ru.krogenit.bfsr.server.ThreadLocalServer;
import ru.krogenit.bfsr.settings.ClientSettings;
import ru.krogenit.bfsr.world.WorldClient;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class Core {

	private static Core instance;

	private int screenWidth, screenHeight;

	private final SoundManager soundManager;
	private WorldClient world;
	private Gui currentGui;
	private final Renderer renderer;
	private final ClientSettings settings;
	private final Profiler profiler;

	private ThreadLocalServer localServer;
	private NetworkManagerClient networkManager;

	private boolean isActive = true;
	
	private String playerName;

	private final Queue<ListenableFutureTask<?>> futureTasks = Queues.newArrayDeque();

	public Core(int screenWidth, int screenHeight) {
		instance = this;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;

		Lang.load();
		this.settings = new ClientSettings();
		this.settings.readSettings();
		this.soundManager = new SoundManager();
		this.soundManager.init();
		this.renderer = new Renderer(this);
		this.currentGui = new GuiMainMenu();
		this.currentGui.init();

		this.profiler = new Profiler(settings.isProfiling());
	}

	public void init() {
		soundManager.setAttenuationModel(AL11.AL_EXPONENT_DISTANCE);
		soundManager.setListener(new SoundListener(new Vector3f(0, 0, 0)));
	}

	public void input() {
		profiler.startSection("input");
		renderer.input();
		if (isActive && currentGui != null) currentGui.input();
		if (world != null) world.input();
	}

	public void update(double delta) {
		profiler.endStartSection("tasks");
		synchronized (this.futureTasks) {
			while (!this.futureTasks.isEmpty()) {
				this.futureTasks.poll().run();
			}
		}

		profiler.endStartSection("network");
		if(networkManager != null) {
			if (networkManager.isChannelOpen()) {
				networkManager.processReceivedPackets();
			} else if (networkManager.getExitMessage() != null) {
				networkManager.onDisconnect(networkManager.getExitMessage());
				clearNetwork();
			} else {
				networkManager.onDisconnect("Disconnected from server");
				clearNetwork();
			}
		}

		profiler.endStartSection("update");
		if (world != null) {
			renderer.update(delta);
			soundManager.updateListenerPosition(renderer.getCamera());
			world.update(delta);
		}

		if (currentGui != null) currentGui.update(delta);
	}

	public void render() {
		profiler.endStartSection("render");
		renderer.render();
		profiler.endSection();
	}

	public void startSingleplayer() {
		startLocalServer();
		localServer.connectToLocalServer();
	}

	private void startLocalServer() {
		playerName = "Local Player";
		localServer = new ThreadLocalServer();
		localServer.setName("Local Server");
		localServer.start();
	}

	public void stopServer() {
		if (localServer != null) {
			localServer.stopServer();
			localServer = null;
		}
	}

	public void quitToMainMenu() {
		renderer.clear();
		clearNetwork();
		stopServer();
		if(world != null) {
			world.clear();
			world = null;
		}

		setCurrentGui(new GuiMainMenu());
	}
	
	public void clearNetwork() {
		if(networkManager != null) {
			networkManager.closeChannel("Quitting");
			networkManager.stop();
			networkManager = null;
		}
	}

	public void resize(int width, int height) {
		this.screenWidth = width;
		this.screenHeight = height;
		this.renderer.resize(width, height);

		if (currentGui != null) currentGui.resize(width, height);
	}

	public void setCurrentGui(Gui gui) {
		if (currentGui != null) currentGui.clear();
		this.currentGui = gui;
		if (currentGui != null) currentGui.init();
	}

	public void sendPacket(Packet packet) {
		if(networkManager !=  null) networkManager.scheduleOutboundPacket(packet);
	}

	public void addFutureTask(Runnable runnable) {
		addFutureTask(Executors.callable(runnable));
	}

	public void addFutureTask(Callable<?> callable) {
		ListenableFutureTask<?> futureTask = ListenableFutureTask.create(callable);

		synchronized (futureTasks) {
			futureTasks.add(futureTask);
		}
	}

	public Renderer getRenderer() {
		return renderer;
	}

	public WorldClient getWorld() {
		return world;
	}

	public Gui getCurrentGui() {
		return currentGui;
	}
	
	public GuiInGame getGuiInGame() {
		return renderer.getGuiInGame();
	}

	public int getWidth() {
		return screenWidth;
	}

	public int getHeight() {
		return screenHeight;
	}

	public static Core getCore() {
		return instance;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isActive() {
		return isActive;
	}

	public ClientSettings getSettings() {
		return settings;
	}

	public void clear() {
		clearNetwork();
		if (localServer != null) {
			localServer.stopServer();
		}

		soundManager.cleanup();
	}

	public SoundManager getSoundManager() {
		return soundManager;
	}

	public void setWorld(WorldClient world) {
		this.world = world;
	}

	public Profiler getProfiler() {
		return profiler;
	}

	public String getPlayerName() {
		return playerName;
	}

	public boolean canControlShip() {
		return !renderer.getGuiInGame().isActive() && currentGui == null;
	}
	
	public NetworkManager getNetworkManager() {
		return networkManager;
	}
	
	public void setNetworkManager(NetworkManagerClient networkManager) {
		this.networkManager = networkManager;
	}
}
