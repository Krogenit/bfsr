package ru.krogenit.bfsr.world;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.*;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import ru.krogenit.bfsr.client.gui.ingame.GuiInGameMenu;
import ru.krogenit.bfsr.client.input.Keyboard;
import ru.krogenit.bfsr.client.input.Mouse;
import ru.krogenit.bfsr.client.particle.EnumParticlePositionType;
import ru.krogenit.bfsr.client.particle.ParticleRenderer;
import ru.krogenit.bfsr.client.render.OpenGLHelper;
import ru.krogenit.bfsr.client.shader.BaseShader;
import ru.krogenit.bfsr.client.texture.Texture;
import ru.krogenit.bfsr.client.texture.TextureGenerator;
import ru.krogenit.bfsr.collision.AxisAlignedBoundingBox;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.TextureObject;
import ru.krogenit.bfsr.entity.bullet.Bullet;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.faction.Faction;
import ru.krogenit.bfsr.math.EnumZoomFactor;
import ru.krogenit.bfsr.network.packet.client.PacketCommand;
import ru.krogenit.bfsr.server.EnumCommand;

import java.text.DecimalFormat;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;

public class WorldClient extends World {

	private final Core core;
	private TextureObject background;
	private final ParticleRenderer particleRenderer;
	private Ship playerShip;
	private int spawnTimer;
	private final HashMap<Texture, List<Ship>> shipsByMapForRender = new HashMap<>();
	private final HashMap<Texture, List<Bullet>> bulletByMapForRender = new HashMap<>();
	
	public WorldClient() {
		super(true, Core.getCore().getProfiler());
		
		this.core = Core.getCore();
		this.particleRenderer = new ParticleRenderer(this);
	}
	
	public void setSeed(long seed) {
		if(background != null) background.clear();
		createBackground(seed);
	}
	
	private void createBackground(long seed) {
		int width = 2560 * 2;
		int height = 2560 * 2;
		this.background = new TextureObject(TextureGenerator.generateNebulaTexture(width, height, new Random(seed)), new Vector2f(0,0), new Vector2f(width, height));
		this.background.setZoomFactor(EnumZoomFactor.Background);
	}

	public void input() {
		if(Mouse.isLeftRelease()) {
			DecimalFormat f = new DecimalFormat("0.00");
			Vector2f mpos = Mouse.getWorldPosition(core.getRenderer().getCamera());
			System.out.println("vertecies[0] = new Vector2(" + f.format(mpos.x) + "f, " + f.format(mpos.y) +"f);");
		}
		
		if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_ESCAPE) && Core.getCore().canControlShip()) {
			Core.getCore().setCurrentGui(new GuiInGameMenu());
		}
		
		
		int bots = 0;
		boolean sameFaction = true;
		Faction lastFaction = null;
		for (Ship s : ships) {
			if (s.isBot()) {
				bots++;
			}

			if (lastFaction != null && lastFaction != s.getFaction()) {
				sameFaction = false;
			}

			lastFaction = s.getFaction();
		}
		
//		if(Core.getCore().canControlShip()) {
			if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_F) 
//					|| --spawnTimer <= 0
//					|| ((bots == 0 || sameFaction) && --spawnTimer <= 0)
					)
			{
				Vector2f pos = Mouse.getWorldPosition(Core.getCore().getRenderer().getCamera());
				
				if(core.getNetworkManager() != null)
//					for(int i=0;i<1;i++) {
//						Vector2f pos = new Vector2f(Core.getCore().getRenderer().getCamera().getPosition()).add(RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 5500 * rand.nextFloat()));
						core.sendPacket(new PacketCommand(EnumCommand.SpawnShip, "" + pos.x, "" + pos.y));
//					}
				spawnTimer = 60;
			} else if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_G)) {
				Vector2f pos = Mouse.getWorldPosition(Core.getCore().getRenderer().getCamera());
				Vector2f randomVector1 = new Vector2f(pos).add(-10 + rand.nextInt(21), -10 + rand.nextInt(21));
				core.sendPacket(new PacketCommand(EnumCommand.SpawnParticle, "" + randomVector1.x, "" + randomVector1.y));
				
				
				
//				particleSystem.spawnMediumGarbage(rand.nextInt(2) + 1, randomVector1, new Vector2f(),  50f + rand.nextFloat() * 40f);
//				particleSystem.spawnSmallGarbage(4, randomVector1, new Vector2f(), 50f);
//				particleSystem.spawnDamageDerbis(1, new Vector2f(), randomVector1);
//				particleSystem.spawnShipOst(randomVector1, new Vector2f());
//				particleSystem.spawnLight(randomVector1, 5f, new Vector4f(1.0f, 0.5f, 0.5f, 0.7f), 0.04f, false, EnumParticlePositionType.Default);
//				particleSystem.spawnSpark(randomVector1, 0.5f);
//				particleSystem.spawnExplosion(randomVector1, 0.125F);
			} 
		else if(Keyboard.isKeyPressed(GLFW.GLFW_KEY_J)) {
			if(playerShip != null) {
//				playerShip.getDamages().clear();
				//saimon
//				playerShip.addDamage(new Damage(playerShip, 0.2f, 0, new Vector2f(10, -4), 0.8f));
//				playerShip.addDamage(new Damage(playerShip, 0.4f, 0, new Vector2f(5, -18), 1f));
//				playerShip.addDamage(new Damage(playerShip, 0.6f, 1, new Vector2f(5, 15), 0.55f));
//				playerShip.addDamage(new Damage(playerShip, 0.8f, 3, new Vector2f(-19, 0), 0.6f));
				//engi
//				playerShip.addDamage(new Damage(playerShip, 0.2f, 0, new Vector2f(-10, 15), 0.8f));
//				playerShip.addDamage(new Damage(playerShip, 0.4f, 0, new Vector2f(5, -12), 0.8f));
//				playerShip.addDamage(new Damage(playerShip, 0.6f, 1, new Vector2f(-15, -0), 0.55f));
//				playerShip.addDamage(new Damage(playerShip, 0.8f, 2, new Vector2f(12, 5), 0.5f));
				//human
//				playerShip.addDamage(new Damage(playerShip, 0.2f, 0, new Vector2f(-5, 15), 0.8f));
//				playerShip.addDamage(new Damage(playerShip, 0.4f, 0, new Vector2f(-18, -8), 0.8f));
//				playerShip.addDamage(new Damage(playerShip, 0.6f, 1, new Vector2f(-5, -15), 0.55f));
//				playerShip.addDamage(new Damage(playerShip, 0.8f, 2, new Vector2f(8, -2), 0.5f));
			}

		}
		
		if(Mouse.isRightStartDown()) {
			core.getGuiInGame().selectShipSecondary(null);
			for (Ship ship : ships) {
				if (ship.getAABB().isIntersects(Mouse.getWorldPosition(core.getRenderer().getCamera()))) {
					core.getGuiInGame().selectShipSecondary(ship);
				}
			}
		}
		
		if(playerShip == null) {
			if(Mouse.isLeftStartDown() && core.canControlShip()) {
				core.getGuiInGame().selectShip(null);
				for (Ship ship : ships) {
					if (ship.getAABB().isIntersects(Mouse.getWorldPosition(core.getRenderer().getCamera()))) {
						core.getGuiInGame().selectShip(ship);
					}
				}
			}
		}
	}
	
	@Override
	public void update(double delta) {
		super.update(delta);
		particleRenderer.update(delta);

		if(playerShip != null) {
			if(core.canControlShip() && playerShip.isSpawned())
				playerShip.control(delta);
		}
	}
	
	@Override
	public void addShip(Ship ship) {
		super.addShip(ship);
	}
	
	@Override
	protected void removeShip(Ship ship) {
		super.removeShip(ship);
		
		if(ship == playerShip) 
			playerShip = null;
	}

	public void renderAmbient(BaseShader shader) {
		if(background != null) {
			background.render(shader);
		}
	}

	public void renderEntities(BaseShader shader) {
		shader.enable();
		AxisAlignedBoundingBox cameraAABB = core.getRenderer().getCamera().getBoundingBox();
		
		Iterator<Texture> it = shipsByMapForRender.keySet().iterator();
		while(it.hasNext()) {
			Texture key = it.next();
			List<Ship> ss = shipsByMapForRender.get(key);
			ss.clear();
		}

		for (Ship s : ships) {
			if (s.getAABB().isIntersects(cameraAABB)) {
				Texture t = s.getTexture();
				List<Ship> ss = shipsByMapForRender.computeIfAbsent(t, k -> new ArrayList<>());
				ss.add(s);
			}
		}
		
		it = shipsByMapForRender.keySet().iterator();
		while(it.hasNext()) {
			Texture key = it.next();
			List<Ship> ss = shipsByMapForRender.get(key);
			for(Ship s : ss) {
				s.render(shader);
			}
		}
		
//		for (int i = 0; i < ships.size(); i++) {
//			Ship s = ships.get(i);
//			if(s.getAABB().isIntersects(core.getRenderer().getCamera().getAabb()));
//				s.render(shader);
//		}
		
		OpenGLHelper.blendFunc(GL_SRC_ALPHA, GL_ONE);
		
		it = bulletByMapForRender.keySet().iterator();
		while(it.hasNext()) {
			Texture key = it.next();
			List<Bullet> ss = bulletByMapForRender.get(key);
			ss.clear();
		}
		
		for (Bullet b : bullets) {
			if(b.getAABB().isIntersects(cameraAABB)) {
				Texture t = b.getTexture();
				List<Bullet> ss = bulletByMapForRender.computeIfAbsent(t, k -> new ArrayList<>());
				ss.add(b);
			}
		}
		
		it = bulletByMapForRender.keySet().iterator();
		while(it.hasNext()) {
			Texture key = it.next();
			List<Bullet> ss = bulletByMapForRender.get(key);
			for(Bullet b : ss) {
				b.render(shader);
			}
		}
		
//		for(Bullet bullet : bullets) {
//			if(bullet.getAABB().isIntersects(core.getRenderer().getCamera().getAabb()));
//				bullet.render(shader);
//		}
		OpenGLHelper.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}
	
	public void renderBackParticles() {
		particleRenderer.render(EnumParticlePositionType.Background);
	}

	public void renderParticles() {
		particleRenderer.render(EnumParticlePositionType.Default);
	}

	public void renderDebug() {
		for(Body body : physicWorld.getBodies()) {
			if(body.getMass().isInfinite()) {
				AABB aabb = body.createAABB();
				Vector2 center = body.getTransform().getTranslation();
				double rot = Math.toDegrees(body.getTransform().getRotationAngle());

//				glColor3f( 1, 1, 1 );
				glBegin(GL_LINE_LOOP);
				glVertex2d(aabb.getMinX(), aabb.getMinY());
				glVertex2d(aabb.getMinX(), aabb.getMaxY());
				glVertex2d(aabb.getMaxX(), aabb.getMaxY());
				glVertex2d(aabb.getMaxX(), aabb.getMinY());
				glEnd();

				List<BodyFixture> fixtures = body.getFixtures();
				for (BodyFixture bodyFixture : fixtures) {
					Convex convex = bodyFixture.getShape();
					if (convex instanceof Rectangle) {
						Rectangle rect = (Rectangle) convex;
						glPushMatrix();
						glTranslated(center.x, center.y, 0);
						glRotated(rot, 0, 0, 1);
						glBegin(GL_LINE_LOOP);
						for (int i = 0; i < rect.getVertices().length; i++) {
							Vector2 vect1 = rect.getVertices()[i];
							glVertex2d(vect1.x, vect1.y);
						}
						glEnd();
						glPopMatrix();
					} else if (convex instanceof Polygon) {
						Polygon polygon = (Polygon) convex;
						glPushMatrix();
						glTranslated(center.x, center.y, 0);
						glRotated(rot, 0, 0, 1);
						glBegin(GL_LINE_LOOP);
						for (int i = 0; i < polygon.getVertices().length; i++) {
							Vector2 vect1 = polygon.getVertices()[i];
							glVertex2d(vect1.x, vect1.y);
						}
						glEnd();
						glPopMatrix();
					}
				}
			}
		}

		for (Ship s : ships) {
			s.renderDebug();
		}
		
		for(Bullet bullet : bullets) {
			bullet.renderDebug();
		}
	}
	
	@Override
	public ParticleRenderer getParticleRenderer() {
		return particleRenderer;
	}
	
	public void setPlayerShip(Ship playerShip) {
		this.playerShip = playerShip;
		core.getGuiInGame().selectShip(playerShip);
		core.getGuiInGame().setShipControl();
	}
	
	public Ship getPlayerShip() {
		return playerShip;
	}
	
	@Override
	public void clear() {
		super.clear();
	}
}
