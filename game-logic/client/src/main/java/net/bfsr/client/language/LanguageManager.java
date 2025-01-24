package net.bfsr.client.language;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.engine.util.PathHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Log4j2
public class LanguageManager {
    private final HashMap<String, HashMap<String, String>> translations = new HashMap<>();
    private final List<String> languages = new ArrayList<>();

    public LanguageManager load() {
        Path folder = PathHelper.CLIENT_CONTENT.resolve("lang");
        try {
            Files.walkFileTree(folder, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String langName = PathHelper.getFileNameWithoutExtension(file.getFileName().toString());
                    List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                    HashMap<String, String> langData = new HashMap<>();
                    for (int i = 0; i < lines.size(); i++) {
                        String s = lines.get(i);
                        String[] data = s.split("=");
                        if (data.length > 1) {
                            langData.put(data[0], data[1]);
                        }
                    }
                    translations.put(langName, langData);
                    languages.add(langName);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Can't load localization", e);
        }

        return this;
    }

    public String getString(String value) {
        String lang = ClientSettings.LANGUAGE.getString();
        if (translations.containsKey(lang)) {
            HashMap<String, String> langData = translations.get(lang);
            if (langData.containsKey(value)) {
                return translations.get(lang).get(value);
            }
        }

        return value;
    }

    public String getNextLang(String currentLang) {
        int newLangId = 0;
        for (int i = 0; i < languages.size() - 1; i++) {
            if (languages.get(i).equals(currentLang)) {
                newLangId = i + 1;
            }
        }
        return languages.get(newLangId);
    }
}