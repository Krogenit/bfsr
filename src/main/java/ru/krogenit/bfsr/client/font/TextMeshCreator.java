package ru.krogenit.bfsr.client.font;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;

public class TextMeshCreator {

	protected static final double LINE_HEIGHT = 0.03f;
	protected static final int SPACE_ASCII = 32;
	protected static final double OFFSET_DEPENDS_ON_FONT_SIZE = 0.004f;

	private final MetaFile metaData;

	protected TextMeshCreator(File metaFile) {
		metaData = new MetaFile(metaFile);
	}

	protected TextMeshData createTextMesh(GUIText text) {
		List<Line> lines = createStructure(text.getTextString(), text.getFontSize(), text.getMaxLineSize());
		return createQuadVertices(lines, text.getFontSize(), text.isCentered(), text.getLineHeight());
	}
	
	public TextMeshData createTextMesh(String text, Vector2f fontSize, float maxLineSize, boolean isCentered, float lineHeight) {
		List<Line> lines = createStructure(text, fontSize, maxLineSize);
		return createQuadVertices(lines, fontSize, isCentered, lineHeight);
	}
	
	public boolean hasCharacter(char ascii) {
		return ascii == SPACE_ASCII || metaData.getCharacter(ascii) != null;
	}

	private List<Line> createStructure(GUIText text) {
		char[] chars = text.getTextString().toCharArray();
		List<Line> lines = new ArrayList<>();
		Line currentLine = new Line(metaData.getSpaceWidth(), text.getFontSize().x, text.getMaxLineSize());
		Word currentWord = new Word(text.getFontSize().x);
		for (char c : chars) {
			if ((int) c == SPACE_ASCII) {
				boolean added = currentLine.attemptToAddWord(currentWord);
				if (!added) {
					lines.add(currentLine);
					currentLine = new Line(metaData.getSpaceWidth(), text.getFontSize().x, text.getMaxLineSize());
					currentLine.attemptToAddWord(currentWord);
				}
				currentWord = new Word(text.getFontSize().x);
				continue;
			} 
			
			if(c == '\n') {
				lines.add(currentLine);
				currentLine = new Line(metaData.getSpaceWidth(), text.getFontSize().x, text.getMaxLineSize());
				continue;
			}
			
			Character character = metaData.getCharacter(c);
			if(character != null) {
				currentWord.addCharacter(character);
			}
		}
		completeStructure(lines, currentLine, currentWord, text.getFontSize().x, text.getMaxLineSize());
		return lines;
	}
	
	private List<Line> createStructure(String text, Vector2f fontSize, float maxLineSize) {
		char[] chars = text.toCharArray();
		List<Line> lines = new ArrayList<>();
		Line currentLine = new Line(metaData.getSpaceWidth(), fontSize.x, maxLineSize);
		Word currentWord = new Word(fontSize.x);
		for (char c : chars) {
			if ((int) c == SPACE_ASCII) {
				boolean added = currentLine.attemptToAddWord(currentWord);
				if (!added) {
					lines.add(currentLine);
					currentLine = new Line(metaData.getSpaceWidth(), fontSize.x, maxLineSize);
					currentLine.attemptToAddWord(currentWord);
				}
				currentWord = new Word(fontSize.x);
				continue;
			} 
			
			if(c == '\n') {
				lines.add(currentLine);
				currentLine = new Line(metaData.getSpaceWidth(), fontSize.x, maxLineSize);
				continue;
			}
			
			Character character = metaData.getCharacter(c);
			if(character != null) {
				currentWord.addCharacter(character);
			}
		}
		completeStructure(lines, currentLine, currentWord, fontSize.x, maxLineSize);
		return lines;
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
		double spaceWidth = font.getLoader().getMetaData().getSpaceWidth();
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
			
			if(c == '\n') {
				lines.add(currentLine);
				currentLine = new Line(spaceWidth, fontSize, maxLineWidth);
				continue;
			}
			
			Character character = font.getLoader().getMetaData().getCharacter(c);
			if(character != null)
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
			curserX  = initX;
			int cursorPos = 0;
			for (Word word : line.getWords()) {
				for (Character letter : word.getCharacters()) {
					double charSize = letter.getxAdvance() * fontSize * 1400;
					curserX += charSize;
					if(curserX - charSize/4.0 >= mousePos.x) return cursorPos;
					cursorPos++;
				}
				//Skip null chars
				if(word.getCharacters().size() > 0) curserX += spaceWidth * fontSize;
			}
			return cursorPos;
		}
		
		return 0;
	}
	
	public static float getLineWidth(String text, FontType font, float fontSize) {
		double spaceWidth = font.getLoader().getMetaData().getSpaceWidth();
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
			
			if(c == '\n') {
				lines.add(currentLine);
				currentLine = new Line(spaceWidth, fontSize, maxLineWidth);
				continue;
			}
			
			Character character = font.getLoader().getMetaData().getCharacter(c);
			if(character != null)
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
			curserX  = initX;
			for (Word word : line.getWords()) {
				for (Character letter : word.getCharacters()) {
					curserX += letter.getxAdvance() * fontSize;
				}
				//Skip null chars
				curserX += spaceWidth * fontSize;
			}
			return (float) (curserX - initX);
		}
		
		return 0;
	}
	
	public static int getLineSubPosIfTooBig(String text, FontType font, float fontSize, float maxLineSize) {
		double spaceWidth = font.getLoader().getMetaData().getSpaceWidth();
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
			
			if(c == '\n') {
				lines.add(currentLine);
				currentLine = new Line(spaceWidth, fontSize, maxLineWidth);
				continue;
			}
			
			Character character = font.getLoader().getMetaData().getCharacter(c);
			if(character != null)
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
		int index = 0;
		for (Line line : lines) {
			initX = 0.5f;
			curserX  = initX;
			for (Word word : line.getWords()) {
				int size = word.getCharacters().size();
				for (Character letter : word.getCharacters()) {
					curserX += letter.getxAdvance() * fontSize;
				}
				if(curserX > maxLineSize) return index;
				//Skip null chars
				index += size + 1;
				curserX += spaceWidth * fontSize;
				if(curserX > maxLineSize) return index;
			}
			if(curserX > maxLineSize) return index;
		}
		
		return 0;
	}

	private TextMeshData createQuadVertices(GUIText text, List<Line> lines) {
		text.setNumberOfLines(lines.size());
		double curserX;
		double initX;
		double curserY = text.isCentered() ? (-LINE_HEIGHT / 2.0 + 0.003f) : 0.001f;
		curserY *=  text.getFontSize().y;
		List<Float> vertices = new ArrayList<>();
		List<Float> textureCoords = new ArrayList<>();
		for (Line line : lines) {
			if (text.isCentered()) {
				initX = (line.getMaxLength() - line.getLineLength()) / 2.0;
			} else {
				initX = 0.5f;
			}
			curserX  = initX;
			for (Word word : line.getWords()) {
				for (Character letter : word.getCharacters()) {
					addVerticesForCharacter(curserX, curserY, letter, text.getFontSize(), vertices);
					addTexCoords(textureCoords, letter.getxTextureCoord(), letter.getyTextureCoord(),
							letter.getXMaxTextureCoord(), letter.getYMaxTextureCoord());
					curserX += letter.getxAdvance() * text.getFontSize().x;
				}
				//Skip null chars
				curserX += metaData.getSpaceWidth() * text.getFontSize().x;
			}
			text.setLineWidth((float) (curserX - initX));
			curserY += LINE_HEIGHT / 1.5f * text.getLineHeight() * text.getFontSize().y;
		}		
		return new TextMeshData(listToArray(vertices), listToArray(textureCoords));
	}
	
	private TextMeshData createQuadVertices(List<Line> lines, Vector2f fontSize, boolean isCentered, float lineHeight) {
		double curserX;
		double initX;
		double curserY = isCentered ? (-LINE_HEIGHT / 2.0 + 0.003f) : 0.001f;
		curserY *=  fontSize.y;
		List<Float> vertices = new ArrayList<>();
		List<Float> textureCoords = new ArrayList<>();
		for (Line line : lines) {
			if (isCentered) {
				initX = (line.getMaxLength() - line.getLineLength()) / 2.0;
			} else {
				initX = 0.5f;
			}
			curserX  = initX;
			for (Word word : line.getWords()) {
				for (Character letter : word.getCharacters()) {
					addVerticesForCharacter(curserX, curserY, letter, fontSize, vertices);
					addTexCoords(textureCoords, letter.getxTextureCoord(), letter.getyTextureCoord(),
							letter.getXMaxTextureCoord(), letter.getYMaxTextureCoord());
					curserX += letter.getxAdvance() * fontSize.x;
				}
				//Skip null chars
				curserX += metaData.getSpaceWidth() * fontSize.x;
			}

			curserY += LINE_HEIGHT / 1.5f * lineHeight * fontSize.y;
		}		
		return new TextMeshData(listToArray(vertices), listToArray(textureCoords));
	}

	private void addVerticesForCharacter(double curserX, double curserY, Character character, Vector2f fontSize,
			List<Float> vertices) {
		double x = curserX + (character.getxOffset() * fontSize.x);
		double y = curserY + (character.getyOffset() * fontSize.y) - OFFSET_DEPENDS_ON_FONT_SIZE * fontSize.y;
		double maxX = x + (character.getSizeX() * fontSize.x);
		double maxY = y + (character.getSizeY() * fontSize.y);
		double properX = (2 * x) - 1;
		double properY = (-2 * y) + 1;
		double properMaxX = (2 * maxX) - 1;
		double properMaxY = (-2 * maxY) + 1;
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

	private static void addTexCoords(List<Float> texCoords, double x, double y, double maxX, double maxY) {
		texCoords.add((float) x);
		texCoords.add((float) y);
		texCoords.add((float) x);
		texCoords.add((float) maxY);
		texCoords.add((float) maxX);
		texCoords.add((float) maxY);
		texCoords.add((float) maxX);
		texCoords.add((float) maxY);
		texCoords.add((float) maxX);
		texCoords.add((float) y);
		texCoords.add((float) x);
		texCoords.add((float) y);
	}

	private static float[] listToArray(List<Float> listOfFloats) {
		float[] array = new float[listOfFloats.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = listOfFloats.get(i);
		}
		return array;
	}

	public MetaFile getMetaData() {
		return metaData;
	}
}
