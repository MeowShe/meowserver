package io.meowresearch.mcserver.s4.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataWriter {
    public static void appendPlayerModInfoToFile(String playerName, List<String> installedModList) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map<String, List<String>> playerModInfo = new HashMap<>();
        playerModInfo.put(playerName, installedModList);

        File file = new File("./meow/playerInstalledModList.json");
        if (!file.exists()) {
            Files.createDirectories(Paths.get("./meow"));
            file.createNewFile();
        }

        Map<String, List<String>> existingData = new HashMap<>();
        if (file.length() > 0) {
            Type type = new TypeToken<Map<String, List<String>>>() {
            }.getType();
            try (FileReader reader = new FileReader(file)) {
                existingData = gson.fromJson(reader, type);
            }
        }

        existingData.putAll(playerModInfo);

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(existingData, writer);
        }
    }
}
