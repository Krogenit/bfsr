package ru.krogenit.bfsr.util;

import java.io.File;

public class PathHelper
{
	public static File content = new File(".", "content");
	public static File texture = new File(content, "texture");
	public static File sound = new File(content, "sound");
	public static File shader = new File(content, "shader");
	public static File font = new File(content, "font");
}
