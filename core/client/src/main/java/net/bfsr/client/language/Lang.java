package net.bfsr.client.language;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.settings.Option;
import net.bfsr.util.PathHelper;

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
public final class Lang {
    private static final HashMap<String, HashMap<String, String>> TRANSLATIONS = new HashMap<>();
    private static final List<String> LANGUAGES = new ArrayList<>();

    public static void load() {
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
                    TRANSLATIONS.put(langName, langData);
                    LANGUAGES.add(langName);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Can't load localization", e);
        }
    }

    public static String getString(String value) {
        String lang = Option.LANGUAGE.getString();
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