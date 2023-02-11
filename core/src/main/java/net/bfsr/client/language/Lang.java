package net.bfsr.client.language;

import lombok.extern.log4j.Log4j2;
import net.bfsr.settings.EnumOption;
import net.bfsr.util.PathHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Log4j2
public final class Lang {
    private static final HashMap<String, HashMap<String, String>> TRANSLATIONS = new HashMap<>();
    private static final List<String> LANGUAGES = new ArrayList<>();

    public static void load() {
        File folder = new File(PathHelper.CONTENT, "lang");
        if (folder.exists()) {
            try {
                for (final File file : folder.listFiles()) {
                    String langName = file.getName().substring(0, file.getName().indexOf('.'));
                    BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
                    String s;
                    HashMap<String, String> langData = new HashMap<>();
                    while ((s = reader.readLine()) != null) {
                        if (s.length() > 0) {
                            s = s.replace("%N", "\n");
                            String[] data = s.split("=");
                            langData.put(data[0], data[1]);
                        }
                    }
                    TRANSLATIONS.put(langName, langData);
                    LANGUAGES.add(langName);
                    reader.close();
                }
            } catch (IOException e) {
                log.error("Can't load localization", e);
            }
        }
    }

    public static String getString(String value) {
        String lang = EnumOption.LANGUAGE.getString();
        if (TRANSLATIONS.containsKey(lang)) {
            HashMap<String, String> langData = TRANSLATIONS.get(lang);
            if (langData.containsKey(value)) {
                return TRANSLATIONS.get(lang).get(value);
            }
        }

        return value;
    }

    public static String getNextLang(String currentLang) {
        int newLangId = 0;
        for (int i = 0; i < LANGUAGES.size() - 1; i++) {
            if (LANGUAGES.get(i).equals(currentLang)) {
                newLangId = i + 1;
            }
        }
        return LANGUAGES.get(newLangId);
    }
}
