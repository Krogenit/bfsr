package ru.krogenit.bfsr.math;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import ru.krogenit.bfsr.client.camera.Camera;
import ru.krogenit.bfsr.client.font.GUIText;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.TextureObject;

public class Transformation {

	private static final Matrix4f viewMatrix = new Matrix4f();
	private static final Matrix4f viewMatrixBackground = new Matrix4f();
	private static final Matrix4f viewMatrixGui = new Matrix4f();
	private static final Matrix4f modelViewMatrix = new Matrix4f();
	private static final Matrix4f finalModelViewMatrix = new Matrix4f();
	
	public static Vector2f guiScale = new Vector2f(Core.getCore().getWidth() / 1280f, Core.getCore().getHeight() / 720f);

	public static Matrix4f getModelViewMatrix(float x, float y, float rotation, float scaleX, float scaleY, EnumZoomFactor factor) {
		modelViewMatrix.identity().translate(x, y, 0);
		if(rotation != 0) modelViewMatrix.rotateZ(rotation);
		modelViewMatrix.scale(scaleX, -scaleY, 1);
		
		Matrix4f viewMatrixByType = getViewMatrixByType(factor);
		return viewMatrixByType.mul(modelViewMatrix, finalModelViewMatrix);
	}
	
	public static Matrix4f getModelViewMatrixGui(float x, float y, float rotation, float scaleX, float scaleY) {
		modelViewMatrix.identity().translate(x, y, 0);
		if(rotation != 0) modelViewMatrix.rotateZ(rotation);
		modelViewMatrix.scale(scaleX, -scaleY, 1);
		return viewMatrixGui.mul(modelViewMatrix, finalModelViewMatrix);
	}

	public static Matrix4f getModelViewMatrix(TextureObject gameObj) {
		Vector2f position = gameObj.getPosition();
		float rotation = gameObj.getRotation();
		Vector2f scale = gameObj.getScale();
		EnumZoomFactor zoomFactor = gameObj.getZoomFactor();
		
		modelViewMatrix.identity().translate(position.x, position.y, 0);
		if(rotation != 0) modelViewMatrix.rotateZ(rotation);
		modelViewMatrix.scale(scale.x, -scale.y, 1);
		
		Matrix4f viewMatrixByType = getViewMatrixByType(zoomFactor);
		return viewMatrixByType.mul(modelViewMatrix, finalModelViewMatrix);
	}

	public static Matrix4f getOrthographicViewMatrixForFontRendering(float x, float y, EnumZoomFactor factor) {
		modelViewMatrix.identity().translate(x, y, 0).scale(2f, 1f, 1);
		Matrix4f viewMatrixByType = getViewMatrixByType(factor);
		return viewMatrixByType.mul(modelViewMatrix, finalModelViewMatrix);
	}
	
	public static Matrix4f getModelViewMatrixForTextRendering(Vector2f pos, EnumZoomFactor factor) {
		modelViewMatrix.identity().translate(pos.x, pos.y, 0).scale(700, -700, 1).translate(0, -1f, 0);
		Matrix4f viewMatrixByType = getViewMatrixByType(factor);
		return viewMatrixByType.mul(modelViewMatrix, finalModelViewMatrix);
	}
	
	public static Matrix4f getModelViewMatrixForTextRendering(Vector2f pos, EnumZoomFactor factor, Vector2f offset) {
		modelViewMatrix.identity().translate(pos.x + offset.x, pos.y + offset.y, 0).scale(700, -700, 1).translate(0, -1f, 0);
		Matrix4f viewMatrixByType = getViewMatrixByType(factor);
		return viewMatrixByType.mul(modelViewMatrix, finalModelViewMatrix);
	}

	public static Matrix4f getModelViewMatrix(GUIText text) {
		EnumZoomFactor zoomFactor = text.getZoomFactor();

		modelViewMatrix.identity().translate(text.getPosition().x, text.getPosition().y + 700, 0).scale(700, -700, 1);
		Matrix4f viewMatrixByType = getViewMatrixByType(zoomFactor);
		return viewMatrixByType.mul(modelViewMatrix, finalModelViewMatrix);
	}
	
	public static Matrix4f getModelViewMatrix(GUIText text, Vector2f offset) {
		EnumZoomFactor zoomFactor = text.getZoomFactor();
		
		modelViewMatrix.identity().translate(text.getPosition().x + offset.x, text.getPosition().y + offset.y + 700, 0).scale(700, -700, 1);
		Matrix4f viewMatrixByType = getViewMatrixByType(zoomFactor);
		return viewMatrixByType.mul(modelViewMatrix, finalModelViewMatrix);
	}

	public static Matrix4f getViewMatrix(Camera camera) {
		Vector2f cameraPos = camera.getPositionAndOrigin();
		float rotation = camera.getRotation();

		viewMatrix.identity();
		if(rotation != 0) viewMatrix.rotateZ(rotation);
		viewMatrix.translate(-cameraPos.x, -cameraPos.y, 0);
		return viewMatrix;
	}

	public static void updateViewMatrix(Camera camera) {
		Vector2f camPos = camera.getPosition();
		Vector2f camOrigin = camera.getOrigin();
		float rotation = camera.getRotation();
		float zoom = camera.getZoom();

		viewMatrix.identity();
		viewMatrix.rotateZ(rotation);
		viewMatrix.translate(-camOrigin.x, -camOrigin.y, 0);
		viewMatrix.scale(zoom, zoom, 1);
		viewMatrix.translate(-camPos.x, -camPos.y, 0);
		
		zoom = camera.getZoomBackground();
		float moveFactor = 0.005f;
		
		viewMatrixBackground.identity();
		viewMatrixBackground.rotateZ(rotation);
		viewMatrixBackground.translate(-camOrigin.x, -camOrigin.y, 0);
		viewMatrixBackground.scale(zoom, zoom, 1);
		float x = -camPos.x;
		float y = -camPos.y;
		viewMatrixBackground.translate(x * moveFactor, y * moveFactor, 0);

		viewMatrixGui.identity();
		viewMatrixGui.rotateZ(rotation);
	}
	
	public static Matrix4f getViewMatrixByType(EnumZoomFactor zoomFactor) {
		if(zoomFactor == EnumZoomFactor.Background) {
			return viewMatrixBackground;
		} else if(zoomFactor == EnumZoomFactor.Gui) {
			return viewMatrixGui;
		} else {
			return viewMatrix;
		}
	}

	public static void updateGenericViewMatrix(Vector2f position, float rotation, Matrix4f matrix) {
		if(rotation != 0) matrix.rotationZ(rotation);
		matrix.translate(-position.x, -position.y, -0);
	}

	public static void resize(int width, int height) {
		guiScale.x = width / 1280f;
		guiScale.y = height / 720f;
	}
	
	public static Vector2f getScale(float x, float y) {
		return new Vector2f(x * guiScale.x, y * guiScale.y);
	}
	
	public static Vector2f getScale(Vector2f scale) {
		return new Vector2f(scale.x * guiScale.x, scale.y * guiScale.y);
	}
	
	public static Vector2f getOffsetByScale(float x, float y) {
		return new Vector2f(x  - (x - Core.getCore().getWidth() / 2f) * (1.0f - guiScale.x), 
				y - (y - Core.getCore().getHeight() / 2f) * (1.0f - guiScale.y));
	}

	public static Vector2f getOffsetByScale(Vector2f pos) {
		return new Vector2f(pos.x  - (pos.x - Core.getCore().getWidth() / 2f) * (1.0f - guiScale.x), 
				pos.y - (pos.y - Core.getCore().getHeight() / 2f) * (1.0f - guiScale.y));
	}
}
