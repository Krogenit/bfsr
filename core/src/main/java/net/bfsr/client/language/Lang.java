package net.bfsr.client.language;

import net.bfsr.core.Core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Lang {

    private static final HashMap<String, HashMap<String, String>> TRANSLATIONS = new HashMap<>();
    private static final List<String> LANGUAGES = new ArrayList<>();

    public static void load() {
        File folder = new File("content", "lang");
        if (folder.exists()) {
            try {
                for (final File file : folder.listFiles()) {
                    String langName = file.getName().substring(0, file.getName().indexOf("."));
                    BufferedReader reader = new BufferedReader(new FileReader(file));
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getString(String value) {
        String lang = Core.getCore().getSettings().getLanguage();
        if (TRANSLATIONS.containsKey(lang)) {
            HashMap<String, String> langData = TRANSLATIONS.get(lang);
            if (langData.containsKey(value)) {
                return TRANSLATIONS.get(lang).get(value);
            }
        }

        return value;
    }

    public static List<String> getLanguages() {
        return new ArrayList<String>(TRANSLATIONS.keySet());
    }

    public static void setNextLang() {
        String curLang = Core.getCore().getSettings().getLanguage();
        int newLangId = 0;
        for (int i = 0; i < LANGUAGES.size() - 1; i++) {
            if (LANGUAGES.get(i).equals(curLang)) {
                newLangId = i + 1;
            }
        }
        Core.getCore().getSettings().setLanguage(LANGUAGES.get(newLangId));
    }
}
