package com.koutakutenn.overpoweredagain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.LoggerFactory;

public final class OverpoweredConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public volatile boolean legacyRecipe = true;
    public volatile boolean legacyTexture = true;

    public static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("overpowered_again.json");
    }

    public static OverpoweredConfig load() {
        if (Files.exists(path())) {
            try (var reader = Files.newBufferedReader(path())) {
                OverpoweredConfig config = GSON.fromJson(reader, OverpoweredConfig.class);
                if (config == null) throw new IOException("Empty config");
                return config;
            } catch (Exception exception) {
                LoggerFactory.getLogger("overpowered_again").error(
                        "Cannot read {}; using defaults without replacing the file", path(), exception);
            }
            return new OverpoweredConfig();
        }
        OverpoweredConfig defaults = new OverpoweredConfig();
        try {
            Files.createDirectories(path().getParent());
            Files.writeString(path(), GSON.toJson(defaults) + "\n", StandardOpenOption.CREATE_NEW);
        } catch (IOException exception) {
            LoggerFactory.getLogger("overpowered_again").error("Cannot create default config {}", path(), exception);
        }
        return defaults;
    }

    public void save() throws IOException {
        Files.createDirectories(path().getParent());
        Files.writeString(path(), GSON.toJson(this) + "\n");
    }
}
