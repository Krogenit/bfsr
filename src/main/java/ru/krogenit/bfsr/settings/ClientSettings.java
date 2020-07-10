package ru.krogenit.bfsr.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import lombok.Setter;
import ru.krogenit.bfsr.client.language.Lang;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.core.Main;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;

@Getter
@Setter
public class ClientSettings implements IConfig {

	private final transient Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private float soundVolume = 1.0f;

	private boolean cameraMoveByScreenBorders = false;
	private boolean vSync = true;
	private int maxFps = 60;
	private boolean isDebug = false;
	private boolean isProfiling = false;
	private float cameraMoveByScreenBordersSpeed = 6f;
	private float cameraMoveByScreenBordersOffset = 15f;
	private float cameraMoveByKeySpeed = 10f;

	private float cameraZoomSpeed = 0.2f;

	private boolean cameraFollowPlayer = true;
	
	private String language = "eng";

	public static transient HashMap<String, List<EnumOption>> optionsByCategory = new HashMap<>();

	private int getInt(EnumOption option, float value) {
		return (int) (option.getMinValue() + value * (option.getMaxValue() - option.getMinValue()));
	}

	private float getFloat(EnumOption option, float value) {
		return option.getMinValue() + value * (option.getMaxValue() - option.getMinValue());
	}

	public void setOptionValue(EnumOption option, Object value) {
		Class<?> type = option.getType();
		if(type == int.class) {
			value = getInt(option, (float) value);
		} else if(type == float.class) {
			value = getFloat(option, (float) value);
		} else if(type == boolean.class) {
			if(option == EnumOption.vSync) {
				Main.setVSync((boolean) value);
			} else if(option == EnumOption.isProfiling) {
				Core.getCore().getProfiler().setEnable((boolean) value);
			}
		} else {
			if(option == EnumOption.language) {
				Lang.setNextLang();
				value = getLanguage();
			}
		}
		
		try {
			Field field = this.getClass().getDeclaredField(option.toString());
			field.set(this, value);
		} catch (Exception e) {
			e.printStackTrace();
		}

		saveSettings();
	}

	public Object getOptionValue(EnumOption option) {
		try {
			Field field = this.getClass().getDeclaredField(option.toString());
			return field.get(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public void saveSettings() {
		String json = gson.toJson(this);

		try {
			FileWriter fileWriter = new FileWriter(getFile());
			fileWriter.write(json);
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void readSettings() {
		File file = getFile();
		if (file.exists()) {
			try {
				JsonReader reader = new JsonReader(new FileReader(file));
				IConfig config = gson.fromJson(reader, this.getClass());
				for (Field field : this.getClass().getDeclaredFields()) {
					if (!Modifier.isTransient(field.getModifiers())) {
						Field field1 = config.getClass().getDeclaredField(field.getName());
						field.setAccessible(true);
						field1.setAccessible(true);
						field.set(this, field1.get(config));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			saveSettings();
		}
	}

	@Override
	public File getFile() {
		return new File(".", "client_settings.json");
	}
}
