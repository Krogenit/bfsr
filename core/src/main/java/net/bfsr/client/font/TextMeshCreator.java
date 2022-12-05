package net.bfsr.client.font;

import lombok.Getter;
import org.joml.Vector2f;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class TextMeshCreator {

	protected static final float LINE_HEIGHT = 0.03f;
	protected static final int SPACE_ASCII = 32;
	protected static final float OFFSET_DEPENDS_ON_FONT_SIZE = 0.004f;

	private static final List<Line> LINES = new ArrayList<>();
	private static final List<Float> VERTICES_AND_TEXTURE_COORDS = new ArrayList<>();

	private final List<Float> vertices = new ArrayList<>();
	private final List<Float> textureCoords = new ArrayList<>();

	@Getter
	private final MetaFile metaData;

	protected TextMeshCreator(File metaFile) {
		metaData = new MetaFile(metaFile);
	}

	protected TextMeshData createTextMesh(GUIText text) {
		Vector2f fontSize = text.getFontSize();
		List<Line> lines = createStructure(text.getTextString(), fontSize.x, text.getMaxLineSize());
		return createQuadVertices(lines, fontSize, text.isCentered(), text.getLineHeight());
	}

	public float[] createTextMeshOptimized(String text, float fontSizeX, float fontSizeY, float maxLineSize, boolean isCentered, float lineHeight) {
		List<Line> lines = createStructure(text, fontSizeX, maxLineSize);
		return createQuadVerticesOptimized(lines, fontSizeX, fontSizeY, isCentered, lineHeight);
	}

	public boolean hasCharacter(char ascii) {
		return ascii == SPACE_ASCII || metaData.getCharacter(ascii) != null;
	}


	private List<Line> createStructure(String text, float fontSizeX, float maxLineSize) {
		char[] chars = text.toCharArray();
		LINES.clear();
		Line currentLine = new Line(metaData.getSpaceWidth(), fontSizeX, maxLineSize);
		Word currentWord = new Word(fontSizeX);
		for (char c : chars) {
			if ((int) c == SPACE_ASCII) {
				boolean added = currentLine.attemptToAddWord(currentWord);
				if (!added) {
					LINES.add(currentLine);
					currentLine = new Line(metaData.getSpaceWidth(), fontSizeX, maxLineSize);
					currentLine.attemptToAddWord(currentWord);
				}
				currentWord = new Word(fontSizeX);
				continue;
			}

			if (c == '\n') {
				boolean added = currentLine.attemptToAddWord(currentWord);
				if (!added) {
					LINES.add(currentLine);
					currentLine = new Line(metaData.getSpaceWidth(), fontSizeX, maxLineSize);
					currentLine.attemptToAddWord(currentWord);
				}
				currentWord = new Word(fontSizeX);
				LINES.add(currentLine);
				currentLine = new Line(metaData.getSpaceWidth(), fontSizeX, maxLineSize);
				continue;
			}

			Character character = metaData.getCharacter(c);
			if (character != null) {
				currentWord.addCharacter(character);
			}
		}
		completeStructure(LINES, currentLine, currentWord, fontSizeX, maxLineSize);
		return LINES;
	}

	private void completeStructure(List<Line> lines, Line currentLine, Word currentWord, float fontSizeX, float maxLineSize) {
		boolean added = currentLine.attemptToAddWord(currentWord);
		if (!added) {
			lines.add(currentLine);
			currentLine = new Line(metaData.getSpaceWidth(), fontSizeX, maxLineSize);
			currentLine.attemptToAddWord(currentWord);
		}
		lines.add(currentLine);
	}

	public static int getCursorPositionInLine(String text, FontType font, float fontSize, Vector2f mousePos) {
		float spaceWidth = font.getLoader().getMetaData().getSpaceWidth();
		float maxLineWidth = 1.0f;
		char[] chars = text.toCharArray();
		List<Line> lines = new ArrayList<>();
		Line currentLine = new Line(spaceWidth, fontSize, maxLineWidth);
		Word currentWord = new Word(fontSize);
		for (char c : chars) {
			if ((int) c == SPACE_ASCII) {
				boolean added = currentLine.attemptToAddWord(currentWord);
				if (!added) {
					lines.add(currentLine);
					currentLine = new Line(spaceWidth, fontSize, maxLineWidth);
					currentLine.attemptToAddWord(currentWord);
				}
				currentWord = new Word(fontSize);
				continue;
			}

			if (c == '\n') {
				lines.add(currentLine);
				currentLine = new Line(spaceWidth, fontSize, maxLineWidth);
				continue;
			}

			Character character = font.getLoader().getMetaData().getCharacter(c);
			if (character != null)
				currentWord.addCharacter(character);
		}
		boolean added = currentLine.attemptToAddWord(currentWord);
		if (!added) {
			lines.add(currentLine);
			currentLine = new Line(spaceWidth, fontSize, maxLineWidth);
			currentLine.attemptToAddWord(currentWord);
		}
		lines.add(currentLine);
		double curserX;
		double initX;

		for (Line line : lines) {
			initX = 0.5f;
			curserX = initX;
			int cursorPos = 0;
			for (Word word : line.getWords()) {
				for (Character letter : word.getCharacters()) {
					double charSize = letter.getXAdvance() * fontSize * 1400;
					curserX += charSize;
					if (curserX - charSize / 4.0 >= mousePos.x) return cursorPos;
					cursorPos++;
				}
				if (word.getCharacters().size() > 0) curserX += spaceWidth * fontSize;
			}
			return cursorPos;
		}

		return 0;
	}

	public static float getLineWidth(String text, FontType font, float fontSize) {
		float spaceWidth = font.getLoader().getMetaData().getSpaceWidth();
		float maxLineWidth = 1.0f;
		char[] chars = text.toCharArray();
		LINES.clear();
		Line currentLine = new Line(spaceWidth, fontSize, maxLineWidth);
		Word currentWord = new Word(fontSize);
		for (char c : chars) {
			if ((int) c == SPACE_ASCII) {
				boolean added = currentLine.attemptToAddWord(currentWord);
				if (!added) {
					LINES.add(currentLine);
					currentLine = new Line(spaceWidth, fontSize, maxLineWidth);
					currentLine.attemptToAddWord(currentWord);
				}
				currentWord = new Word(fontSize);
				continue;
			}

			if (c == '\n') {
				LINES.add(currentLine);
				currentLine = new Line(spaceWidth, fontSize, maxLineWidth);
				continue;
			}

			Character character = font.getLoader().getMetaData().getCharacter(c);
			if (character != null)
				currentWord.addCharacter(character);
		}
		boolean added = currentLine.attemptToAddWord(currentWord);
		if (!added) {
			LINES.add(currentLine);
			currentLine = new Line(spaceWidth, fontSize, maxLineWidth);
			currentLine.attemptToAddWord(currentWord);
		}
		LINES.add(currentLine);
		double curserX;
		double initX;

		for (Line line : LINES) {
			initX = 0.5f;
			curserX = initX;
			for (Word word : line.getWords()) {
				for (Character letter : word.getCharacters()) {
					curserX += letter.getXAdvance() * fontSize;
				}
				curserX += spaceWidth * fontSize;
			}
			return (float) (curserX - initX);
		}

		return 0;
	}

	public static int getLineSubPosIfTooBig(String text, FontType font, float fontSize, float maxLineSize) {
		float spaceWidth = font.getLoader().getMetaData().getSpaceWidth();
		float maxLineWidth = 1.0f;
		char[] chars = text.toCharArray();
		LINES.clear();
		Line currentLine = new Line(spaceWidth, fontSize, maxLineWidth);
		Word currentWord = new Word(fontSize);
		for (char c : chars) {
			if ((int) c == SPACE_ASCII) {
				boolean added = currentLine.attemptToAddWord(currentWord);
				if (!added) {
					LINES.add(currentLine);
					currentLine = new Line(spaceWidth, fontSize, maxLineWidth);
					currentLine.attemptToAddWord(currentWord);
				}
				currentWord = new Word(fontSize);
				continue;
			}

			if (c == '\n') {
				LINES.add(currentLine);
				currentLine = new Line(spaceWidth, fontSize, maxLineWidth);
				continue;
			}

			Character character = font.getLoader().getMetaData().getCharacter(c);
			if (character != null)
				currentWord.addCharacter(character);
		}
		boolean added = currentLine.attemptToAddWord(currentWord);
		if (!added) {
			LINES.add(currentLine);
			currentLine = new Line(spaceWidth, fontSize, maxLineWidth);
			currentLine.attemptToAddWord(currentWord);
		}
		LINES.add(currentLine);
		double curserX;
		double initX;
		int index = 0;
		for (Line line : LINES) {
			initX = 0.5f;
			curserX = initX;
			for (Word word : line.getWords()) {
				int size = word.getCharacters().size();
				for (Character letter : word.getCharacters()) {
					curserX += letter.getXAdvance() * fontSize;
				}
				if (curserX > maxLineSize) return index;

				index += size + 1;
				curserX += spaceWidth * fontSize;
				if (curserX > maxLineSize) return index;
			}
			if (curserX > maxLineSize) return index;
		}

		return 0;
	}

	private float[] createQuadVerticesOptimized(List<Line> lines, float fontSizeX, float fontSizeY, boolean isCentered, float lineHeight) {
		float curserX = 0f;
		float curserY = 0f;
		VERTICES_AND_TEXTURE_COORDS.clear();
		for (Line line : lines) {
			if (isCentered) {
				curserX = (line.getMaxLength() - line.getLineLength()) / 2f;
			}
			for (Word word : line.getWords()) {
				for (Character letter : word.getCharacters()) {
					addVerticesAndTextureCoordsForCharacter(curserX, curserY, letter, fontSizeX, fontSizeY, letter.getXTextureCoord(), letter.getYTextureCoord(), letter.getXMaxTextureCoord(), letter.getYMaxTextureCoord());
					curserX += letter.getXAdvance() * fontSizeX;
				}

				curserX += metaData.getSpaceWidth() * fontSizeX;
			}

			curserY += lineHeight * fontSizeY;
			curserX = 0;
		}
		return listToArray(VERTICES_AND_TEXTURE_COORDS);
	}

	private TextMeshData createQuadVertices(List<Line> lines, Vector2f fontSize, boolean isCentered, float lineHeight) {
		double curserX;
		double initX;
		double curserY = isCentered ? (-LINE_HEIGHT / 2.0 + 0.003f) : 0.001f;
		curserY *= fontSize.y;
		vertices.clear();
		textureCoords.clear();
		for (Line line : lines) {
			if (isCentered) {
				initX = (line.getMaxLength() - line.getLineLength()) / 2.0;
			} else {
				initX = 0.5f;
			}
			curserX = initX;
			for (Word word : line.getWords()) {
				for (Character letter : word.getCharacters()) {
					addVerticesForCharacter(curserX, curserY, letter, fontSize, vertices);
					addVertices(textureCoords, letter.getXTextureCoord(), letter.getYTextureCoord(),
							letter.getXMaxTextureCoord(), letter.getYMaxTextureCoord());
					curserX += letter.getXAdvance() * fontSize.x;
				}

				curserX += metaData.getSpaceWidth() * fontSize.x;
			}

			curserY += LINE_HEIGHT / 1.5f * lineHeight * fontSize.y;
		}
		return new TextMeshData(listToArray(vertices), listToArray(textureCoords));
	}

	private void addVerticesAndTextureCoordsForCharacter(float curserX, float curserY, Character character, float fontSizeX, float fontSizeY, float textureCoordX, float textureCoordY, float textureCoordMaxX, float textureCoordMaxY) {
		float x = curserX + (character.getXOffset() * fontSizeX);
		float y = curserY + (character.getYOffset() * fontSizeY);
		float maxX = x + (character.getSizeX() * fontSizeX);
		float maxY = y + (character.getSizeY() * fontSizeY);
		float properX = (2f * x) - 1f;
		float properY = (-2f * y) + 1f;
		float properMaxX = (2f * maxX) - 1f;
		float properMaxY = (-2f * maxY) + 1f;

		VERTICES_AND_TEXTURE_COORDS.add(properX);
		VERTICES_AND_TEXTURE_COORDS.add(properY);
		VERTICES_AND_TEXTURE_COORDS.add(textureCoordX);
		VERTICES_AND_TEXTURE_COORDS.add(textureCoordY);
		VERTICES_AND_TEXTURE_COORDS.add(properX);
		VERTICES_AND_TEXTURE_COORDS.add(properMaxY);
		VERTICES_AND_TEXTURE_COORDS.add(textureCoordX);
		VERTICES_AND_TEXTURE_COORDS.add(textureCoordMaxY);
		VERTICES_AND_TEXTURE_COORDS.add(properMaxX);
		VERTICES_AND_TEXTURE_COORDS.add(properMaxY);
		VERTICES_AND_TEXTURE_COORDS.add(textureCoordMaxX);
		VERTICES_AND_TEXTURE_COORDS.add(textureCoordMaxY);
		VERTICES_AND_TEXTURE_COORDS.add(properMaxX);
		VERTICES_AND_TEXTURE_COORDS.add(properMaxY);
		VERTICES_AND_TEXTURE_COORDS.add(textureCoordMaxX);
		VERTICES_AND_TEXTURE_COORDS.add(textureCoordMaxY);
		VERTICES_AND_TEXTURE_COORDS.add(properMaxX);
		VERTICES_AND_TEXTURE_COORDS.add(properY);
		VERTICES_AND_TEXTURE_COORDS.add(textureCoordMaxX);
		VERTICES_AND_TEXTURE_COORDS.add(textureCoordY);
		VERTICES_AND_TEXTURE_COORDS.add(properX);
		VERTICES_AND_TEXTURE_COORDS.add(properY);
		VERTICES_AND_TEXTURE_COORDS.add(textureCoordX);
		VERTICES_AND_TEXTURE_COORDS.add(textureCoordY);
	}

	private void addVerticesForCharacter(double curserX, double curserY, Character character, Vector2f fontSize, List<Float> vertices) {
		double x = curserX + (character.getXOffset() * fontSize.x);
		double y = curserY + (character.getYOffset() * fontSize.y) - OFFSET_DEPENDS_ON_FONT_SIZE * fontSize.y;
		double maxX = x + (character.getSizeX() * fontSize.x);
		double maxY = y + (character.getSizeY() * fontSize.y);
		double properX = (2.0 * x) - 1.0;
		double properY = (-2.0 * y) + 1.0;
		double properMaxX = (2.0 * maxX) - 1.0;
		double properMaxY = (-2.0 * maxY) + 1.0;
		addVertices(vertices, properX, properY, properMaxX, properMaxY);
	}

	private static void addVertices(List<Float> vertices, double x, double y, double maxX, double maxY) {
		vertices.add((float) x);
		vertices.add((float) y);
		vertices.add((float) x);
		vertices.add((float) maxY);
		vertices.add((float) maxX);
		vertices.add((float) maxY);
		vertices.add((float) maxX);
		vertices.add((float) maxY);
		vertices.add((float) maxX);
		vertices.add((float) y);
		vertices.add((float) x);
		vertices.add((float) y);
	}

	private static float[] listToArray(List<Float> listOfFloats) {
		float[] array = new float[listOfFloats.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = listOfFloats.get(i);
		}
		return array;
	}

	public float calculateFontHeight() {
		float height = 0;
		String string = "!#№$&;%:?*()[]{}\\|/AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZzАаБбВвГгДдЕеЁёЖжЗзИиЙйКкЛлМмНнОоПпРрСсТтУуФфХхЦцЧчШшЩщЪъЫыЬьЭэЮюЯя";
		for (char c : string.toCharArray()) {
			Character character = metaData.getCharacter(c);
			if (character != null) {
				float sizeY = character.getSizeY();
				if (sizeY > height) height = sizeY;
			}
		}

		return height;
	}
}
