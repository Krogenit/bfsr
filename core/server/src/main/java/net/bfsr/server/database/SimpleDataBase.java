package net.bfsr.server.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;
import net.bfsr.faction.Faction;
import net.bfsr.server.MainServer;
import net.bfsr.server.component.weapon.WeaponSlot;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.player.PlayerServer;
import net.bfsr.server.world.WorldServer;

import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Log4j2
public class SimpleDataBase {
    private final MainServer server;
    private int nextUserId;

    private final Gson gson;
    private final File dataBaseFolder = new File(".", "database");

    private final List<PlayerServer> loadedPlayers = new ArrayList<>();
    private final HashMap<String, PlayerServer> loadedPlayersMap = new HashMap<>();

    public SimpleDataBase(MainServer server) {
        this.server = server;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        readSettings();
    }

    public PlayerServer registerPlayer(String playerName, String password) {
        PlayerServer player = new PlayerServer(playerName, password);
        player.setId(nextUserId);
        loadedPlayersMap.put(playerName, player);
        loadedPlayers.add(player);
        log.info("Registered new player {} with id {}", playerName, nextUserId);
        nextUserId++;
        return player;
    }

    public String authorizeUser(String playerName, String password) {
        PlayerServer player;
        if (loadedPlayersMap.containsKey(playerName)) {
            player = getPlayer(playerName);
        } else {
            File playerFile = new File(dataBaseFolder, playerName + ".json");
            if (playerFile.exists()) {
                player = loadUserFromFile(playerFile);
                loadedPlayersMap.put(playerName, player);
                loadedPlayers.add(player);
            } else {
                registerPlayer(playerName, password);
                return null;
            }
        }

        if (player.getPassword().equals(password)) {
            log.info("User {} successful autorized", playerName);
            return null;
        } else {
            log.info("User {} entered wrong password {}", playerName, password);
            return "Wrong password";
        }
    }

    public void getPlayerShips(PlayerServer player) {
        File playerFile = new File(dataBaseFolder, player.getUserName() + ".json");
        loadUserShips(playerFile, player);
    }

    private void loadUserShips(File playerFile, PlayerServer player) {
        try {
            JsonObject userObject = gson.fromJson(new FileReader(playerFile, StandardCharsets.UTF_8), JsonObject.class);
            if (userObject.has("shipData")) {
                player.getShips().clear();

                JsonObject shipJson = userObject.getAsJsonObject("shipData");
                JsonArray array = shipJson.getAsJsonArray("ships");
                for (int i = 0; i < array.size(); i++) {
                    JsonObject shipObject = array.get(i).getAsJsonObject();
                    String className = shipObject.get("className").getAsString();
                    Class<?> clazz = Class.forName(className);
                    Constructor<?> constr = clazz.getConstructor(WorldServer.class, float.class, float.class, float.class, boolean.class);
                    Ship ship = (Ship) constr.newInstance(server.getWorld(), 0.0f, 0.0f, 0.0f, false);
                    ship.init();

                    JsonArray weapons = shipObject.get("weapons").getAsJsonArray();
                    for (int i1 = 0; i1 < weapons.size(); i1++) {
                        JsonObject weaponObject = weapons.get(i1).getAsJsonObject();
                        String weaponClassName = weaponObject.get("className").getAsString();
                        clazz = Class.forName(weaponClassName);
                        constr = clazz.getConstructor(Ship.class);
                        WeaponSlot weapon = (WeaponSlot) constr.newInstance(ship);
                        ship.addWeaponToSlot(i1, weapon);
                    }

                    player.addShip(ship);
                    ship.setOwner(player);
                    ship.setFaction(player.getFaction());
                    ship.setName(player.getUserName());
                    ship.sendSpawnPacket();
                    player.setPlayerShip(ship);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PlayerServer loadUserFromFile(File playerFile) {
        try {
            JsonObject userObject = gson.fromJson(new FileReader(playerFile, StandardCharsets.UTF_8), JsonObject.class);
            JsonObject authData = userObject.getAsJsonObject("authData");

            PlayerServer player = new PlayerServer(authData.get("userName").getAsString(), authData.get("password").getAsString());
            int id = userObject.get("id").getAsInt();
            player.setId(id);

            if (userObject.has("playerInfo")) {
                JsonObject playerInfo = userObject.getAsJsonObject("playerInfo");
                String factionName = playerInfo.get("faction").getAsString();
                if (!"null".equals(factionName)) {
                    player.setFaction(Faction.valueOf(factionName));
                }
            }

            log.info("User " + player.getUserName() + " loaded from file");
            return player;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void saveUser(PlayerServer player) {
        String name = player.getUserName();
        String password = player.getPassword();
        JsonObject userObject = new JsonObject();

        userObject.addProperty("id", player.getId());

        JsonObject authData = new JsonObject();
        authData.addProperty("userName", name);
        authData.addProperty("password", password);

        userObject.add("authData", authData);

        JsonObject playerInfo = new JsonObject();

        Faction faction = player.getFaction();
        if (faction != null) {
            playerInfo.addProperty("faction", faction.toString());
        } else {
            playerInfo.addProperty("faction", "null");
        }

        userObject.add("playerInfo", playerInfo);

        List<Ship> playerShips = player.getShips();
        if (playerShips.size() > 0) {
            JsonObject shipData = new JsonObject();
            JsonArray shipsArray = new JsonArray(playerShips.size());
            for (Ship ship : playerShips) {
                JsonObject shipJson = new JsonObject();
                shipJson.addProperty("className", ship.getClass().getName());
                JsonArray weapons = new JsonArray();
                for (WeaponSlot slot : ship.getWeaponSlots()) {
                    JsonObject weaponSlot = new JsonObject();
                    weaponSlot.addProperty("className", slot.getClass().getName());
                    weapons.add(weaponSlot);
                }

                shipJson.add("weapons", weapons);
                shipsArray.add(shipJson);
            }
            shipData.add("ships", shipsArray);

            userObject.add("shipData", shipData);
        }

        FileWriter writer;
        try {
            writer = new FileWriter(new File(dataBaseFolder, name + ".json"), StandardCharsets.UTF_8);
            String jsonString = gson.toJson(userObject);
            writer.write(jsonString);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void save() {
        saveSettings();
        saveUsers();
    }

    private void saveUsers() {
        for (PlayerServer player : loadedPlayers) {
            saveUser(player);
        }
    }

    public void saveSettings() {
        File locationsFile = new File(".", "DBSettings.txt");
        try {
            FileWriter fw = new FileWriter(locationsFile, false);
            fw.write("nextUserId=" + nextUserId + "\n");
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readSettings() {
        if (!dataBaseFolder.exists()) dataBaseFolder.mkdir();

        File file = new File(".", "DBSettings.txt");
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
                nextUserId = Integer.parseInt(reader.readLine().split("=")[1]);
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public PlayerServer getPlayer(String playerName) {
        return loadedPlayersMap.get(playerName);
    }

    public File getDataBaseFolder() {
        return dataBaseFolder;
    }
}
