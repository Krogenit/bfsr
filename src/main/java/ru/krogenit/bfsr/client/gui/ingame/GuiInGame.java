package ru.krogenit.bfsr.client.gui.ingame;

import lombok.Setter;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import ru.krogenit.bfsr.client.camera.Camera;
import ru.krogenit.bfsr.client.font.GUIText;
import ru.krogenit.bfsr.client.gui.Gui;
import ru.krogenit.bfsr.client.gui.GuiTextureObject;
import ru.krogenit.bfsr.client.gui.Scroll;
import ru.krogenit.bfsr.client.gui.button.Button;
import ru.krogenit.bfsr.client.gui.input.InputChat;
import ru.krogenit.bfsr.client.input.Mouse;
import ru.krogenit.bfsr.client.language.Lang;
import ru.krogenit.bfsr.client.loader.TextureLoader;
import ru.krogenit.bfsr.client.particle.EnumParticlePositionType;
import ru.krogenit.bfsr.client.render.OpenGLHelper;
import ru.krogenit.bfsr.client.render.Renderer;
import ru.krogenit.bfsr.client.shader.BaseShader;
import ru.krogenit.bfsr.client.sound.SoundRegistry;
import ru.krogenit.bfsr.client.texture.Texture;
import ru.krogenit.bfsr.client.texture.TextureRegister;
import ru.krogenit.bfsr.collision.AxisAlignedBoundingBox;
import ru.krogenit.bfsr.component.Armor;
import ru.krogenit.bfsr.component.ArmorPlate;
import ru.krogenit.bfsr.component.hull.Hull;
import ru.krogenit.bfsr.component.reactor.Reactor;
import ru.krogenit.bfsr.component.shield.Shield;
import ru.krogenit.bfsr.component.weapon.WeaponSlot;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.core.Main;
import ru.krogenit.bfsr.entity.TextureObject;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.faction.Faction;
import ru.krogenit.bfsr.math.RotationHelper;
import ru.krogenit.bfsr.math.Transformation;
import ru.krogenit.bfsr.network.packet.client.PacketShipControl;
import ru.krogenit.bfsr.profiler.Profiler;
import ru.krogenit.bfsr.server.MainServer;
import ru.krogenit.bfsr.world.World;
import ru.krogenit.bfsr.world.WorldClient;
import ru.krogenit.bfsr.world.WorldServer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GuiInGame extends Gui {

	private final Vector4f white = new Vector4f(1,1,1,1);
	
	private final Core core = Core.getCore();
	
	private final List<GUIText> texts = new ArrayList<>();
	private final DecimalFormat formatter = new DecimalFormat("0.00");
	
	private final TextureObject armorPlate;
	private TextureObject buttonControl;
	private final TextureObject energy;
	private final TextureObject hudShip;
	private final TextureObject hudShipSecondary;
	private final TextureObject hudShipAdd0;
	private final TextureObject map;
	private final TextureObject shield;
	private final TextureObject chat;
	
	private Vector2f debugFontSize = new Vector2f(1f, 0.4f);
	private float yOffset = 12f;
	@Setter private long ping;
	
	private final GUIText controlText;
	private final GUIText upperText;
	private final GUIText worldText;
	private final GUIText shipText;
	private final GUIText shipCargo;
	private final GUIText shipCrew;
	private GUIText shipHull;
	private GUIText shipShield;
	private final GUIText textHull;
	private final GUIText textShield;
	
	private final InputChat chatInput;
	private final Scroll chatScroll;
	
	private boolean controlWasPressed;
	private boolean controlButtonCreated;
	private boolean needExitFromChat;
	
	private Ship currentShip;
	private Ship otherShip;

	private static final HashMap<Texture, List<Ship>> shipsByMap = new HashMap<>();
	private static final AxisAlignedBoundingBox shipAABB  = new AxisAlignedBoundingBox(new Vector2f(), new Vector2f());
	private static final AxisAlignedBoundingBox mapBoundingBox  = new AxisAlignedBoundingBox(new Vector2f(), new Vector2f());
	private static final Vector2f shipPos = new Vector2f();
	private static final Vector2f shipScale = new Vector2f();
	private static final Vector2f mapPos = new Vector2f();
	private static final Vector2f mapScale = new Vector2f();
	private static final Vector4f color = new Vector4f();
	private static final Vector2f weaponPos = new Vector2f();
	private static final Vector2f rotationVector = new Vector2f();

	public GuiInGame() {
		armorPlate = new GuiTextureObject(TextureRegister.guiArmorPlate);
		buttonControl = new GuiTextureObject(TextureRegister.guiButtonControl);
		energy = new GuiTextureObject(TextureRegister.guiEnergy);
		hudShip = new GuiTextureObject(TextureRegister.guiHudShip);
		hudShipSecondary = new GuiTextureObject(TextureRegister.guiHudShip);
		hudShipAdd0 = new GuiTextureObject(TextureRegister.guiHudShipAdd);
		map = new GuiTextureObject(TextureRegister.guiHudShip);
		shield = new GuiTextureObject(TextureRegister.guiShield);
		chat = new GuiTextureObject(TextureRegister.guiChat);
		
		chatScroll = new Scroll(new Vector2f(center.x + 490, center.y + 130), Transformation.getScale(18, 130));
		chatScroll.setVisible(8);
		
		chatInput = new InputChat(new Vector2f(center.x + 460, center.y - 216), chatScroll);
		chatInput.setMaxLineSize(0.4f);
		
		controlText = new GUIText(Lang.getString("gui.control"), new Vector2f(0.9f * Transformation.guiScale.x,0.48f*Transformation.guiScale.y), 
				ru.krogenit.bfsr.client.font.FontRegistry.XOLONIUM, Transformation.getOffsetByScale(new Vector2f(center.x + 490, center.y + 130)),
				new Vector4f(1,1,1,1), 1f, false, EnumParticlePositionType.GuiInGame);
		
		upperText = new GUIText("", debugFontSize, ru.krogenit.bfsr.client.font.FontRegistry.CONSOLA, new Vector2f(0, 0), white, false, EnumParticlePositionType.Last);
		worldText = new GUIText("", debugFontSize, ru.krogenit.bfsr.client.font.FontRegistry.CONSOLA, new Vector2f(0, 0), white, false, EnumParticlePositionType.Last);
		shipText = new GUIText("", debugFontSize, ru.krogenit.bfsr.client.font.FontRegistry.CONSOLA, new Vector2f(0, 0), white, false, EnumParticlePositionType.Last);
		
		shipCargo = new GUIText("", new Vector2f(1,1), ru.krogenit.bfsr.client.font.FontRegistry.CONSOLA, new Vector2f(0, 0), white, false, EnumParticlePositionType.GuiInGame);
		shipCrew = new GUIText("", new Vector2f(1,1), ru.krogenit.bfsr.client.font.FontRegistry.CONSOLA, new Vector2f(0, 0), white, false, EnumParticlePositionType.GuiInGame);
		
		textHull = new GUIText("", new Vector2f(1,1), ru.krogenit.bfsr.client.font.FontRegistry.CONSOLA, new Vector2f(0, 0), white, true, EnumParticlePositionType.GuiInGame);
		textShield = new GUIText("", new Vector2f(1,1), ru.krogenit.bfsr.client.font.FontRegistry.CONSOLA, new Vector2f(0, 0), white, true, EnumParticlePositionType.GuiInGame);
	}
	
	@Override
	public void init() {
		super.init();
		
		float scaleX = 700 / 3.5f;
		float scaleY = 550 / 3.5f;
		hudShip.setScale(scaleX, scaleY);
		hudShip.setPosition(center.x + 550, center.y + 286);
		
		hudShipSecondary.setScale(scaleX, scaleY);
		hudShipSecondary.setPosition(center.x + 550, center.y - 286);
		
		map.setScale(scaleX, scaleY);
		map.setPosition(center.x - 550, center.y - 286);
		
		scaleX = 350 / 3.5f;
		scaleY = 180 / 3.5f;
		hudShipAdd0.setScale(scaleX, scaleY);
		hudShipAdd0.setPosition(center.x + scaleX * 4.2f, center.y + scaleY * 6.5f);
		
		scaleX = 640 / 2.5f;
		scaleY = 340 / 2.5f;
		chat.setScale(scaleX, scaleY);
		chat.setPosition(center.x - 515, center.y + 294);

		chatInput.setScale(scaleX/1.05f, scaleY/1.12f);
		chatInput.setEmptyOffset(new Vector2f(-66, 49));
		chatInput.setTextOffset(new Vector2f(-114, 43));
		chatInput.setPosition(center.x - 516, center.y + 294);
		chatInput.init();
		
		scaleX = 257 / 2f;
		scaleY = 40 / 2f;
		controlText.setPosition(Transformation.getOffsetByScale(new Vector2f(center.x + scaleX * 4.1f, center.y + scaleY * 9.68f)));
		controlText.setFontSize(0.55f * Transformation.guiScale.x,0.32f*Transformation.guiScale.y);		
		if(core.getWorld() != null && core.getWorld().getPlayerShip() == null)
			controlText.updateText(Lang.getString("gui.control"));
		else 
			controlText.updateText(Lang.getString("gui.cancelControl"));
		
		createButton();
	}
	
	private void createButton() {
		float scaleX = 257 / 2f;
		float scaleY = 40 / 2f;
		Button buttonControl = new Button(0, TextureRegister.guiButtonControl,
				Transformation.getOffsetByScale(new Vector2f(center.x + scaleX * 4.4f, center.y + scaleY * 10.f)), 
				new Vector2f(scaleX, scaleY));
		buttonControl.setCollideSound(SoundRegistry.buttonCollide);
		buttonControl.setClickSound(SoundRegistry.buttonClick);
		buttons.add(buttonControl);
		chatInput.addEmptyText();
		if(core.getWorld() != null && core.getWorld().getPlayerShip() == null)
			controlText.updateText(Lang.getString("gui.control"));
		else 
			controlText.updateText(Lang.getString("gui.cancelControl"));
		chatScroll.setScale(Transformation.getScale(12, 99));
		chatScroll.setPosition(Transformation.getOffsetByScale(new Vector2f(center.x - 400, center.y + 284)));
		controlButtonCreated = true;
	}
	
	@Override
	protected void onButtonLeftClick(Button b) {
		if(b.getId() == 0) {
			WorldClient w = core.getWorld();
			controlWasPressed = true;
			Ship playerControlledShip = w.getPlayerShip();
			if(playerControlledShip != null) {
				core.sendPacket(new PacketShipControl(playerControlledShip.getId(), false));
				core.getWorld().setPlayerShip(null);
				selectShip(playerControlledShip);
				cancelShipControl();
			} else if(canControlShip(currentShip)) {
				core.getWorld().setPlayerShip(currentShip);
				core.sendPacket(new PacketShipControl(currentShip.getId(), true));
			}
		}
	}
	
	public void addChatMessage(String message) {
		chatInput.addNewLineToChat(message);
	}
	
	@Override
	protected void onLeftClicked() {
		for (Button b : buttons) {
			if (b.isIntersects()) {
				b.leftClick();
				onButtonLeftClick(b);
			} else controlWasPressed = false;
		}
		
		chatScroll.setMoving(false);
		
		if (chatInput.isIntersects()) {
			if(!chatInput.isTyping()) chatInput.setTyping(true);
		} else {
			if(chatInput.isTyping()) needExitFromChat = true;
			chatInput.setTyping(false);
		}
	}
	
	public boolean isActive() {
		if(controlWasPressed || needExitFromChat) return true;
		return chatInput.isTyping() || chatScroll.isMoving();
	}
	
	@Override
	public void textInput(int key) {
		if(chatInput.isTyping()) {
			chatInput.textInput(key);
		}
	}
	
	@Override
	public void input() {
		if (Mouse.isLeftStartDown())
			onLeftClicked();
		if (Mouse.isRightStartDown())
			onRightClicked();

		Vector2f mousePos = Mouse.getPosition();

		if(Mouse.isLeftDown()) {
			if(chatScroll.isMoving()) {
				chatScroll.input(mousePos.x, mousePos.y);
			}
		}

		if(Mouse.isLeftStartDown()) {
			if(chatScroll.isIntersects()) {
				chatScroll.setMoving(true);
			}
		}

		if(chatInput.isTyping()) {
			chatInput.input();
			chatScroll.setMaxValue(chatInput.getLines().size());
		}

		if(chatInput.isTyping()) {
			Vector2f scroll = Mouse.getScroll();
			chatScroll.scroll(scroll);
			chatInput.setOffsetByScroll(chatScroll.getValue());
			chatScroll.setScale(Transformation.getScale(12, 99));
			chatScroll.setPosition(Transformation.getOffsetByScale(new Vector2f(center.x - 400, center.y + 284)));
		}

		if(Mouse.isLeftRelease()) {
			needExitFromChat= false;
			controlWasPressed = false;
		}
	}
	
	@Override
	public void update(double delta) {
//		init();

		float yPos = 6 + map.getScale().y * 0.93f;
		float xPos = 6;

		Profiler profiler = core.getProfiler();
		Profiler sProfiler = MainServer.getServer() != null ? MainServer.getServer().getProfiler() : null;
		float updateTime = profiler.getResult("update");
		float renderTime = profiler.getResult("render");
		int drawCalls = core.getRenderer().getDrawCalls();
		core.getRenderer().setDrawCalls(0);
		float physicsTime = profiler.getResult("physics");
		float netTime = profiler.getResult("network");
		float sUpdateTime = sProfiler != null ? sProfiler.getResult("update") : 0;
		float sPhysicsTime = sProfiler != null ? sProfiler.getResult("physics") : 0;
		float sNetworkTime = sProfiler != null ? sProfiler.getResult("network") : 0;
		Runtime var1 = Runtime.getRuntime();
        long maxMemory = var1.maxMemory();
        long totalMemory = var1.totalMemory();
        long freeMemory = var1.freeMemory();
        long maxMemoryMB = maxMemory / 1024L / 1024L;
        long totalMemoryMB = totalMemory / 1024L / 1024L;
        long freeMemoryMB = freeMemory / 1024L / 1024L;
        String openGlVersion = GL11.glGetString(GL11.GL_VERSION);
        String openGlRenderer = GL11.glGetString(GL11.GL_RENDERER);
        int tps = MainServer.getServer() != null ? MainServer.getServer().getTps() : 0;
//        String openGlVendor = GL11.glGetString(GL11.GL_VENDOR);
        
		upperText.updateText("BFSR Client Dev 0.0.4 \n" +
			"FPS " + Main.fps + "/" + tps + " \n" +
			//"System: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") v." + System.getProperty("os.version") + " \n" +
			//"Java: " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor") + " \n" +
			//System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor") + " \n" +
			"Memory: " + (totalMemoryMB - freeMemoryMB) + "MB / " + totalMemoryMB + "MB up to " + maxMemoryMB + "MB \n" +
			"OpenGL: " + openGlRenderer + " \nversion " + openGlVersion + " \n" +
			
			" \n" +
			"Update: " + formatter.format(updateTime) + "ms / " +  formatter.format(sUpdateTime) + "ms " +
			"\nPhysics: " + formatter.format(physicsTime) + "ms / " +  formatter.format(sPhysicsTime) + "ms " +
			"\nRender: " + formatter.format(renderTime) + "ms " + drawCalls + " draw calls " + 
			"\nNetwork: " + formatter.format(netTime) + "ms / " +  formatter.format(sNetworkTime) + "ms " +
				"\nPing: " + ping);
		upperText.setPosition(xPos, yPos);
		yPos += yOffset*11;
		World world = core.getWorld();
		if(world != null) {
			chatInput.update(delta);
			chatScroll.update(delta);
			Camera cam = core.getRenderer().getCamera();
			Vector2f camPos = cam.getPosition();
			int bulletsCount = world.getBullets().size();
			int shipsCount = world.getShips().size();
			int particlesCount = world.getParticleRenderer().getParticles().size();
			int physicParticles = world.getParticleRenderer().getParticlesWrecks().size();
			
			WorldServer sWorld = MainServer.getServer() != null ? MainServer.getServer().getWorld() : null;
			int sBulletsCount = sWorld != null ? sWorld.getBullets().size() : 0;
			int sShipsCount = sWorld != null ? sWorld.getShips().size() : 0;
			int sParticlesCount = sWorld != null ? sWorld.getParticles().size() : 0;
			worldText.updateText("---World--- " +
					"\nCamera pos: " + formatter.format(camPos.x) + ", " + formatter.format(camPos.y) + " " +
					"\nShips count: " + shipsCount + "/" + sShipsCount +
					" \nBullets count: " + bulletsCount + "/" + sBulletsCount +
					" \nParticles count: " + particlesCount + 
					" \nPhysic particles count: " + physicParticles + "/" + sParticlesCount);
			worldText.setPosition(xPos, yPos);

			yPos += yOffset * 7;
			Ship playerShip = world.getPlayerShip();
			if(playerShip != null) {
				Vector2f pos = playerShip.getPosition();
				Vector2f velocity = playerShip.getVelocity();
				Hull hull = playerShip.getHull();
				Shield shield = playerShip.getShield();
				Reactor reactor = playerShip.getReactor();
				shipText.updateText("---Player Ship--- "
						+ "\nShip = " + playerShip.getClass().getSimpleName() +" \n"+
						"Pos: " + formatter.format(pos.x) + ", " + formatter.format(pos.y) +" \n" +
						"Velocity: " + formatter.format(velocity.x) + ", " + formatter.format(velocity.y) +" \n" +
						"Mass: " + formatter.format(playerShip.getBody().getMass().getMass()) +" \n" +
						
						"Hull: " + formatter.format(hull.getHull()) +"/" + formatter.format(hull.getMaxHull()) + " \n" +
						"Shield: " + formatter.format(shield.getShield()) + "/" + formatter.format(shield.getMaxShield()) + " \n" +
						"Reactor: " + formatter.format(reactor.getEnergy()) + "/" + formatter.format(reactor.getMaxEnergy()));
				shipText.setPosition(xPos, yPos);
			} else {
				shipText.updateText("");
			}
			
			if(currentShip != null) {
				if(canControlShip(currentShip))  {
					if(!controlButtonCreated) 
						createButton();
				}
				
				if(currentShip.isDead()) currentShip = null;
			} else {
				if(controlButtonCreated) {
					buttons.remove(0);
					controlButtonCreated = false;
				}
				controlText.clear();
				shipCrew.clear();
				shipCargo.clear();
			}
			
			if(otherShip != null) {
				if(otherShip.isDead()) otherShip = null;
			}
		}
		
		if(world != null) {
			super.update(delta);
		}
	}
	
	public boolean canControlShip(Ship s) {
		return s.getName().equals(core.getPlayerName());
	}

	public void setShipControl() {
		controlText.clear();
		controlText.updateText(Lang.getString("gui.cancelControl"));
	}
	
	
	public void cancelShipControl() {
		controlText.clear();
		controlText.updateText(Lang.getString("gui.control"));
	}
	
	public void selectShipSecondary(Ship ship) {
		if(ship == null) {
			if(otherShip != null) {
				otherShip = null;
			}
		} else {
			otherShip = ship;
		}
	}
	
	
	public void selectShip(Ship ship) {
		currentShip = ship;
	}
	
	void renderMap(BaseShader shader, World world) {
		map.render(shader);
		List<Ship> ships = world.getShips();
		Vector2f camPos = core.getRenderer().getCamera().getPosition();
		float mapOffsetX = 6000;
		float mapOffsetY = 6000;
		mapBoundingBox.getMin().x = camPos.x - mapOffsetX;
		mapBoundingBox.getMin().y = camPos.y - mapOffsetY;
		mapBoundingBox.getMax().x = camPos.x + mapOffsetX;
		mapBoundingBox.getMax().y = camPos.y + mapOffsetY;
		mapPos.x = center.x - 550;
		mapPos.y = center.y - 286;
		float mapScaleX = 50f;
		float mapScaleY = 70f;
		float shipSize = 10f;
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		Vector2f mappos1 = this.map.getPosition();
		mapScale.x = this.map.getScale().x;
		mapScale.y = this.map.getScale().y;
		int sizeX = (int)(mapScale.x);
		int sizeY = (int)(mapScale.y);
		mapScale.x /= 2f;
		mapScale.y /= 2f;
		int x = (int)(mappos1.x - mapScale.x);
		int y = (int)(mappos1.y + mapScale.y);
		int offsetY = (int) (17 * Transformation.guiScale.y);
		int offsetX = (int) (19 * Transformation.guiScale.x);
		GL11.glScissor(x + offsetX, height - y + offsetY, sizeX - offsetX*2, sizeY - offsetY*2);
		color.x = color.y = color.z = color.w = 1;
		shipsByMap.clear();

		for (Ship s : ships) {
			Vector2f pos = s.getPosition();
			Vector2f scale = s.getScale();
			shipScale.x = scale.x;
			shipScale.y = scale.y;
			shipScale.x /= shipSize;
			shipScale.y /= shipSize;
			float sX = shipScale.x / 2f;
			float sY = shipScale.y / 2f;
			shipAABB.setMinX(pos.x - sX);
			shipAABB.setMaxX(pos.x + sX);
			shipAABB.setMinY(pos.y - sY);
			shipAABB.setMaxY(pos.y + sY);
			if (mapBoundingBox.isIntersects(shipAABB)) {
				Texture t = s.getTexture();
				List<Ship> ss = shipsByMap.get(t);
				if (ss == null) ss = new ArrayList<>();
				ss.add(s);
				shipsByMap.put(t, ss);
			}
		}

		for (Texture key : shipsByMap.keySet()) {
			List<Ship> ss = shipsByMap.get(key);
			for (Ship s : ss) {
				Vector2f pos = s.getPosition();
				Vector2f scale = s.getScale();
				shipScale.x = scale.x;
				shipScale.y = scale.y;
				shipScale.x /= shipSize;
				shipScale.y /= shipSize;
				Faction faction = s.getFaction();
				if (faction == Faction.Engi) {
					color.x = 0.5f;
					color.y = 1.0f;
					color.z = 0.5f;
				} else if (faction == Faction.Human) {
					color.x = 0.5f;
					color.y = 0.5f;
					color.z = 1.0f;
				} else {
					color.x = 1.0f;
					color.y = 1.0f;
					color.z = 0.5f;
				}

				shipPos.x = mapPos.x + (pos.x - camPos.x) / mapScaleX;
				shipPos.y = mapPos.y + (pos.y - camPos.y) / mapScaleY;
				renderQuad(shader, color, s.getTexture(), shipPos, s.getRotation(), shipScale);
			}
		}
		
//		for(Ship s : ships) {
//			Vector2f pos = s.getPosition();
//			Vector2f scale = new Vector2f(s.getScale());
//			scale.x /= shipSize;
//			scale.y /= shipSize;
//			float sX = scale.x/2f;
//			float sY = scale.y/2f;
//			shipAABB.setMinX(pos.x - sX);
//			shipAABB.setMaxX(pos.x + sX);
//			shipAABB.setMinY(pos.y - sY);
//			shipAABB.setMaxY(pos.y + sY);
//			if(mapAABB.isIntersects(shipAABB)) {
//				Faction faction = s.getFaction();
//				if(faction == Faction.Engi) {
//					color.x = 0.5f;
//					color.y = 1.0f;
//					color.z = 0.5f;
//				} else if(faction == Faction.Human) {
//					color.x = 0.5f;
//					color.y = 0.5f;
//					color.z = 1.0f;
//				} else {
//					color.x = 1.0f;
//					color.y = 1.0f;
//					color.z = 0.5f;
//				}
//				
//				Vector2f onMapPos = new Vector2f(mapPos.x + (pos.x - camPos.x) / mapScaleX, mapPos.y + (pos.y - camPos.y) / mapScaleY);
//				renderQuad(shader, color, s.getTexture(), onMapPos, s.getRotation(), scale);
//			}
//		}
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}
	
	void renderShipHud(BaseShader shader, World world, Ship ship, boolean isSecondary) {
		if(ship != null) {
			OpenGLHelper.alphaGreater(0.01f);
			if(!isSecondary) hudShip.render(shader);
			else hudShipSecondary.render(shader);
			color.x = color.y = color.z = 0;
			color.w = 1;
			OpenGLHelper.alphaGreater(0.75f);
			if(!isSecondary) {
				shipPos.x = center.x + 550;
				shipPos.y = center.y + 286;
			} else {
				shipPos.x = center.x + 550;
				shipPos.y = center.y - 286;
			}
			float shipSize = 0.5f;
			shipScale.x = ship.getScale().x;
			shipScale.y = ship.getScale().y;
			shipScale.mul(shipSize);
			
			color.y = ship.getHull().getHull() / ship.getHull().getMaxHull();
			color.x = 1f - color.y;
			color.z = 0f;
			renderQuad(shader, color, ship.getTexture(), shipPos, (float) (-Math.PI / 2f), shipScale);
			Texture tShield = TextureLoader.getTexture(TextureRegister.shieldSmall0);
			if(!isSecondary) {
				textHull.setFontSize(0.47f * Transformation.guiScale.x,0.28f*Transformation.guiScale.y);
				textHull.updateText(Math.round(ship.getHull().getHull()) + "");
				textHull.setPosition(Transformation.getOffsetByScale(shipPos.x, shipPos.y + 16));
				color.x = 0;
				color.y = 0;
				color.z = 0;
				shipScale.x = 12;
				shipScale.y = 6 * textHull.getTextString().length() + 6;
				OpenGLHelper.alphaGreater(0.01f);
				shipPos.y += 15;
				renderQuad(shader, color, tShield, shipPos, (float) (-Math.PI / 2f), shipScale);
				shipPos.y -= 15;
			}
			
			Shield shield = ship.getShield();
			if(shield != null && shield.shieldAlive()) {
				color.y = shield.getShield() / shield.getMaxShield();
				color.x = 1f - color.y;
				color.z = 0;
				shipScale.x = 140f * shield.getSize();
				shipScale.y = 140f * shield.getSize();
				renderQuad(shader, color, this.shield.getTexture(), shipPos, (float) (-Math.PI), shipScale);
				if(!isSecondary) {	
					textShield.setFontSize(0.47f * Transformation.guiScale.x,0.28f*Transformation.guiScale.y);
					textShield.updateText(Math.round(shield.getShield()) + "");
					textShield.setPosition(Transformation.getOffsetByScale(shipPos.x, shipPos.y + 48));
					color.x = 0;
					color.y = 0;
					color.z = 0;
					shipScale.x = 12;
					shipScale.y = 6 * textShield.getTextString().length() + 6;
					shipPos.y += 47;
					renderQuad(shader, color, tShield, shipPos, (float) (-Math.PI / 2f), shipScale);
					shipPos.y -= 47;
				}
			} else {
				textShield.clear();
			}
			
			Armor armor = ship.getArmor();
			ArmorPlate[] plates = armor.getArmorPlates();
			float rot = (float) Math.PI;
			shipScale.x = this.armorPlate.getScale().x/1.8f;
			shipScale.y = this.armorPlate.getScale().y/1.8f;
			color.x = 1f;
			color.z = 0f;
			for(int i=0;i<4;i++) {
				ArmorPlate plate = plates[i];
				rot -= Math.PI / 2.0;
				if(plate != null) {
					RotationHelper.rotate(rot, -35, 0, rotationVector);
					rotationVector.x += shipPos.x;
					rotationVector.y += shipPos.y;
					color.y = plate.getArmor() / plate.getArmorMax();
					color.x = 1.0f - color.y;
					renderQuad(shader, color, this.armorPlate.getTexture(), rotationVector, (float) (rot + Math.PI), shipScale);
				}
			}
			
			if(!isSecondary && canControlShip(ship)) {				
				Texture  energyText = energy.getTexture();
				shipScale.x = energy.getScale().x * 0.6f;
				shipScale.y = energy.getScale().y * 0.6f;
				Reactor reactor = ship.getReactor();
				float energy = reactor.getEnergy() / reactor.getMaxEnergy() * 20f;
				for(int i=0;i<20;i++) {
					rot = (float) (i * 0.08f - Math.PI / 4f);
					RotationHelper.rotate(rot, -70, 0, rotationVector);
					rotationVector.x += shipPos.x;
					rotationVector.y += shipPos.y;
					color.x = 0;
					color.y = 0;
					color.z = 0;
					renderQuad(shader, color, energyText, rotationVector, (float) (-Math.PI + rot), shipScale);
					if(energy >= i) {
						color.x = 0.25f;
						color.y = 0.5f;
						color.z = 1f;
						renderQuad(shader, color, energyText, rotationVector, (float) (-Math.PI + rot), shipScale);
					}
				}
				OpenGLHelper.alphaGreater(0.01f);
				hudShipAdd0.render(shader);
				
				shipCargo.setPosition(Transformation.getOffsetByScale(center.x + 380, center.y + 320));
				shipCargo.setFontSize(0.47f * Transformation.guiScale.x,0.28f*Transformation.guiScale.y);
				shipCargo.updateText(Lang.getString(Lang.getString("gui.shipCargo") + ": " + ship.getCargo().getCapacity() + "/" + ship.getCargo().getMaxCapacity()));
				
				shipCrew.setPosition(Transformation.getOffsetByScale(center.x + 380, center.y + 330));
				shipCrew.setFontSize(0.47f * Transformation.guiScale.x,0.28f*Transformation.guiScale.y);
				shipCrew.updateText(Lang.getString(Lang.getString("gui.shipCrew") + ": " + ship.getCrew().getCrewSize() + "/" + ship.getCrew().getMaxCrewSize()));
			}

			OpenGLHelper.alphaGreater(0.75f);
			for(WeaponSlot slot : ship.getWeaponSlots()) {
				if(slot != null) {
					float reload = slot.getShootTimer() / slot.getShootTimerMax();
					color.x = reload;
					color.y = reload;
					color.z = 1f;
					Vector2f pos = slot.getAddPosition();
					RotationHelper.rotate((float) (-Math.PI / 2f), pos.x, pos.y, rotationVector);
					weaponPos.x = shipPos.x + rotationVector.x * shipSize;
					weaponPos.y = shipPos.y + rotationVector.y * shipSize;
					shipScale.x = slot.getScale().x;
					shipScale.y = slot.getScale().y;
					shipScale.mul(shipSize);
					renderQuad(shader, color, slot.getTexture(), weaponPos, (float) (-Math.PI / 2f), shipScale);
				}
			}
			
		}
	}
	
	void renderChat(BaseShader shader, World world) {
		OpenGLHelper.alphaGreater(0.01f);
		chat.render(shader);
		chatInput.render(shader);
		chatScroll.render(shader);
	}
	
	@Override
	public void render(BaseShader shader) {
		if(core.getWorld() != null) {
			OpenGLHelper.alphaGreater(0.01f);
			super.render(shader);
		}
		
		World world = core.getWorld();
		
		if(world != null) {
			textShield.clear();
			textHull.clear();
			OpenGLHelper.alphaGreater(0.01f);
			renderMap(shader, world);
			renderShipHud(shader, world, currentShip, false);
			renderShipHud(shader, world, otherShip, true);
			renderChat(shader, world);
		}
	}
	
	private void renderQuad(BaseShader shader, Vector4f color, Texture texture, Vector2f pos, float rot, Vector2f scale) {
		Vector2f pos1 = Transformation.getOffsetByScale(pos);
		shader.setColor(color);
		shader.setModelViewMatrix(Transformation.getModelViewMatrixGui(
				pos1.x, pos1.y,
				rot, scale.x * Transformation.guiScale.x, scale.y * Transformation.guiScale.y));
		if(texture != null) texture.bind();
		Renderer.quad.render();
	}
	
	public void clearByExit() {
		super.clear();
		controlButtonCreated = false;
		controlText.clear();
		upperText.clear();
		worldText.clear();
		shipText.clear();
		chatInput.clear();
		shipCargo.clear();
		shipCrew.clear();
		textHull.clear();
		textShield.clear();
	}
	
	@Override
	public void clear() {
		super.clear();
		controlText.clear();
		upperText.clear();
		worldText.clear();
		shipText.clear();
		shipCargo.clear();
		shipCrew.clear();
	}
	
	public Ship getCurrentShip() {
		return currentShip;
	}
	
	public Ship getOtherShip() {
		return otherShip;
	}
}
