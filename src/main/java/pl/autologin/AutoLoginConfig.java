package pl.autologin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Config zapisywany w .minecraft/config/autologin.json (haslo jako zwykly tekst!). */
public class AutoLoginConfig {
    public static class Entry {
        public String password = "";
        public String command = "login %s";
        public boolean enabled = true;
    }

    /** Opoznienie wyslania komendy po wejsciu na serwer (20 tickow = 1 sekunda). */
    public int delayTicks = 30;
    public Map<String, Entry> servers = new HashMap<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static AutoLoginConfig instance;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("autologin.json");
    }

    public static AutoLoginConfig get() {
        if (instance == null) instance = load();
        return instance;
    }

    public static String keyFor(String address) {
        if (address == null) return null;
        return address.trim().toLowerCase(Locale.ROOT);
    }

    private static AutoLoginConfig load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                AutoLoginConfig c = GSON.fromJson(Files.readString(p, StandardCharsets.UTF_8), AutoLoginConfig.class);
                if (c != null) {
                    if (c.servers == null) c.servers = new HashMap<>();
                    return c;
                }
            }
        } catch (Exception e) {
            System.err.println("[AutoLogin] Nie udalo sie wczytac configu: " + e);
        }
        return new AutoLoginConfig();
    }

    public void save() {
        try {
            Files.writeString(path(), GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[AutoLogin] Nie udalo sie zapisac configu: " + e);
        }
    }
}
