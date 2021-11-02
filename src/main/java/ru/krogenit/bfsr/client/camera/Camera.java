package ru.krogenit.bfsr.client.camera;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import ru.krogenit.bfsr.client.input.Keyboard;
import ru.krogenit.bfsr.client.input.Mouse;
import ru.krogenit.bfsr.collision.AxisAlignedBoundingBox;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.math.Transformation;
import ru.krogenit.bfsr.network.packet.client.PacketCameraPosition;
import ru.krogenit.bfsr.settings.ClientSettings;
import ru.krogenit.bfsr.world.World;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {

	private final Core core;
	private final ClientSettings settings;
	private final float Z_NEAR = 0.0f;
	private final float Z_FAR = 100.0f;
	@Getter private final Matrix4f orthographicMatrix;
	@Getter private final AxisAlignedBoundingBox boundingBox;

	@Getter private final Vector2f position;
	private final Vector2f positionAndOrigin;
	@Getter private float rotation;
	@Getter private final Vector2f origin;
	@Getter private float zoom, zoomBackground;

	private int width, height;

	private final Vector2f vectorInCamSpace = new Vector2f();
	
	private long lastSendTime;
	
	private Ship followShip;

	public Camera(int width, int height) {
		this.orthographicMatrix = new Matrix4f().ortho(0.0f, width, height, 0.0f, Z_NEAR, Z_FAR);

		this.position = new Vector2f(0, 0);
		this.rotation = 0f;
		this.origin = new Vector2f(-width / 2.0f, -height / 2.0f);
		this.width = width;
		this.height = height;
		this.zoom = 1.0f;
		this.positionAndOrigin = new Vector2f();
		this.core = Core.getCore();
		this.settings = core.getSettings();
		this.boundingBox = new AxisAlignedBoundingBox(new Vector2f(position.x + origin.x, position.y  + origin.y), new Vector2f(position.x - origin.x, position.y - origin.y));
	}

	private void followPlayer(double delta) {
		Ship playerShip = core.getWorld().getPlayerShip();
		if (playerShip != null) {
			Vector2f shipPosition = playerShip.getPosition();
			float minDistance = 2f;
			double dis = shipPosition.distance(position);
			if (dis > minDistance) {
				double mDx = shipPosition.x - position.x;
				double mDy = shipPosition.y - position.y;
				position.x += mDx * 3f * delta;
				position.y += mDy * 3f * delta;
			}
		} else {
//			boolean hasShip = false;
			World world = core.getWorld();
//			List<Ship> ships = world.getShips();
//			for(Ship s : ships) {
//				if(followShip == s) hasShip = true;
//			}
//			if(settings.isDebug()) {
				if(followShip == null || followShip.isDead() 
//						|| !hasShip
						) {
					Ship newShip = null;
					if(world.getShips().size() > 0) {
						float minDist = Float.MAX_VALUE;
						for(Ship s : world.getShips()) {
							float dist = s.getPosition().distance(position.x, position.y);
							if(dist < minDist) {
								newShip = s;
								minDist = dist;
							}
						}

						followShip = newShip;
					}
				} else {
					Vector2f shipPosition = followShip.getPosition();
					double dis = shipPosition.distance(position);
					float minDistance = 2f;
					if (dis > minDistance) {
						double mDx = shipPosition.x - position.x;
						double mDy = shipPosition.y - position.y;
						float max = 400f;
						if(mDx < -max) mDx = -max;
						else if(mDx > max) mDx = max;
						if(mDy < -max) mDy = -max;
						else if(mDy > max) mDy = max;
						
						position.x += mDx * 3f * delta;
						position.y += mDy * 3f * delta;
					}
				}
//			}
		}
	}

	private void moveByScreenBorders(double delta) {
		float screenMoveSpeed = settings.getCameraMoveByScreenBordersSpeed() / zoom;
		float offset = settings.getCameraMoveByScreenBordersOffset();
		float moveSpeed = 60f;
		Vector2f cursorPosition = Mouse.getPosition();
		if (cursorPosition.x <= offset) {
			position.x -= screenMoveSpeed * moveSpeed * delta;
		} else if (cursorPosition.x >= width - offset) {
			position.x += screenMoveSpeed * moveSpeed * delta;
		}

		if (cursorPosition.y <= offset) {
			position.y -= screenMoveSpeed * moveSpeed * delta;
		} else if (cursorPosition.y >= height - offset) {
			position.y += screenMoveSpeed * moveSpeed * delta;
		}
	}

	private void scroll() {
		Vector2f scroll = Mouse.getScroll();

		float zoomMax = 2.5f;
		float zoomMin = 0.3f;
		float step = settings.getCameraZoomSpeed() * zoom;
		float maxSteps = (zoomMax - zoomMin) / step;
		zoom += scroll.y * step;

		if (zoom > zoomMax) {
			zoom = zoomMax;
		} else if (zoom < zoomMin) {
			zoom = zoomMin;
		}

		zoomMax = 1.0075f;
		zoomMin = 0.9925f;
		step = (zoomMax - zoomMin) / maxSteps;

		zoomBackground += scroll.y * step;

		if (zoomBackground > zoomMax) {
			zoomBackground = zoomMax;
		} else if (zoomBackground < zoomMin) {
			zoomBackground = zoomMin;
		}
	}

	public void update(double delta) {
		if (core.getWorld() != null) {
			if (core.canControlShip()) {
				scroll();
				if (settings.isCameraMoveByScreenBorders()) moveByScreenBorders(delta);
			}

			boolean hasCurShip = core.getWorld().getPlayerShip() != null;
			float keyMoveSpeed = settings.getCameraMoveByKeySpeed();
			if (Keyboard.isKeyDown(GLFW_KEY_LEFT) || (!hasCurShip && Keyboard.isKeyDown(GLFW.GLFW_KEY_A))) {
				position.x -= keyMoveSpeed * 60f * delta;
			} else if (Keyboard.isKeyDown(GLFW_KEY_RIGHT) || (!hasCurShip && Keyboard.isKeyDown(GLFW.GLFW_KEY_D))) {
				position.x += keyMoveSpeed * 60f * delta;
			}

			if (Keyboard.isKeyDown(GLFW_KEY_UP) || (!hasCurShip && Keyboard.isKeyDown(GLFW.GLFW_KEY_W))) {
				position.y -= keyMoveSpeed * 60f * delta;
			} else if (Keyboard.isKeyDown(GLFW_KEY_DOWN) || (!hasCurShip && Keyboard.isKeyDown(GLFW.GLFW_KEY_S))) {
				position.y += keyMoveSpeed * 60f * delta;
			}

			if (Mouse.isRightDown()) {
				Vector2f delta1 = Mouse.getDelta();
				position.x -= delta1.x / zoom * 60f * delta;
				position.y -= delta1.y / zoom * 60f * delta;
			}

			if (settings.isCameraFollowPlayer()) followPlayer(delta);
		} else {
			zoom = zoomBackground = 1.0f;
			position.x = 0;
			position.y = 0;
		}

		this.boundingBox.setMinX(position.x + origin.x / zoom);
		this.boundingBox.setMinY(position.y + origin.y / zoom);
		this.boundingBox.setMaxX(position.x - origin.x / zoom);
		this.boundingBox.setMaxY(position.y - origin.y / zoom);

		long time = System.currentTimeMillis();
		if(time - lastSendTime > 500) {
			core.sendPacket(new PacketCameraPosition(position.x, position.y));
			lastSendTime = time;
		}
	}

	/**
	 * Uses for debug rendering
	 */
	public void setupOldOpenGLMatrixForDebugRendering() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, width, height, 0, Z_NEAR, Z_FAR);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();

		GL11.glTranslatef(-position.x - origin.x, -position.y - origin.y, 0);
		GL11.glTranslatef(position.x, position.y, 0);
		GL11.glScalef(zoom, zoom, 1f);
		GL11.glTranslatef(-position.x, -position.y, 0);
	}

	public void resize(int width, int height) {
		this.orthographicMatrix.identity();
		this.orthographicMatrix.ortho(0.0f, width, height, 0.0f, Z_NEAR, Z_FAR);
		Transformation.resize(width, height);
		this.width = width;
		this.height = height;
		this.origin.x = -width / 2f;
		this.origin.y = -height / 2f;
	}

	public void setPosition(float x, float y) {
		position.x = x;
		position.y = y;
	}

	public void setRotation(float x) {
		rotation = x;
	}

	public void rotate(float offsetX) {
		rotation += offsetX;
	}

	public Vector2f getWorldVector(Vector2f pos) {
		vectorInCamSpace.x = (pos.x + origin.x) / zoom + position.x;
		vectorInCamSpace.y = (pos.y + origin.y) / zoom + position.y;
		return vectorInCamSpace;
	}

	public Vector2f getPositionAndOrigin() {
		positionAndOrigin.x = position.x + origin.x;
		positionAndOrigin.y = position.y + origin.y;
		return positionAndOrigin;
	}

	public void clear() {
		followShip = null;
	}
	
	public boolean isIntersects(Vector2f pos) {
		return boundingBox.isIntersects(pos);
	}
	
	public boolean isIntersects(AxisAlignedBoundingBox aabb1) {
		return boundingBox.isIntersects(aabb1);
	}
}
