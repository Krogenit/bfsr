package ru.krogenit.bfsr.client.particle;

import org.joml.Vector2f;

import ru.krogenit.bfsr.client.camera.Camera;
import ru.krogenit.bfsr.client.shader.BaseShader;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.TextureObject;

public class AmbientCloud extends TextureObject {

	public enum CloudType {
		Near, Far;
	}

	private final Camera camera;
	private CloudType type;
	private final Vector2f addPosition;

	public AmbientCloud() {
		camera = Core.getCore().getRenderer().getCamera();
		addPosition = new Vector2f();
	}

	@Override
	public void update(double delta) {
		if (type == CloudType.Far) updateFarSmoke();
		else updateNearSmoke();
	}

	public void updateFarSmoke() {
		Vector2f camPos = camera.getPositionAndOrigin();
		position.x = camPos.x / 1.5F + addPosition.x;
		position.y = camPos.y / 1.5F + addPosition.y;

		if (camPos.x - position.y >= 2048 * 1.5F) {
			addPosition.x = addPosition.x + 2048 * 3;
		} else if (camPos.x - position.y <= -2048 * 1.5F) {
			addPosition.x = addPosition.x - 2048 * 3;
		}
		if (camPos.y - position.y >= 2048 * 1.5F) {
			addPosition.y = addPosition.y + 2048 * 3;
		} else if (camPos.y - position.y <= -2048 * 1.5F) {
			addPosition.y = addPosition.y - 2048 * 3;
		}
	}

	public void updateNearSmoke() {

	}

	@Override
	public void render(BaseShader shader) {

	}

}
