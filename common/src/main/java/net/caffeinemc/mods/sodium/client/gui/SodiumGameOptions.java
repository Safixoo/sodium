package net.caffeinemc.mods.sodium.client.gui;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import net.caffeinemc.mods.sodium.client.gui.options.TextProvider;
import net.caffeinemc.mods.sodium.client.services.PlatformRuntimeInformation;
import net.caffeinemc.mods.sodium.client.util.FileUtil;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.SortBehavior;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

// TODO: Rename in Sodium 0.6
public class SodiumGameOptions {
    private static final String DEFAULT_FILE_NAME = "radium-options.json";

    public final QualitySettings quality = new QualitySettings();
    public final AdvancedSettings advanced = new AdvancedSettings();
    public final PerformanceSettings performance = new PerformanceSettings();
    public final NotificationSettings notifications = new NotificationSettings();

    private boolean readOnly;

    private SodiumGameOptions() {
        // NO-OP
    }

    public static SodiumGameOptions defaults() {
        return new SodiumGameOptions();
    }

    public static class PerformanceSettings {
        public int chunkBuilderThreads = 0;
        @SerializedName("always_defer_chunk_updates_v2") // this will reset the option in older configs
        public boolean alwaysDeferChunkUpdates = true;

        public boolean animateOnlyVisibleTextures = true;
        public boolean useEntityCulling = true;
        public boolean useFogOcclusion = true;
        public boolean useBlockFaceCulling = true;

        public boolean smartCull = false;

        @SerializedName("sorting_enabled_v2") // reset the older option in configs before we started hiding it
        public boolean sortingEnabled = true;

        public SortBehavior getSortBehavior() {
            return this.sortingEnabled ? SortBehavior.DYNAMIC_DEFER_NEARBY_ZERO_FRAMES : SortBehavior.OFF;
        }
    }

    public static class AdvancedSettings {
        public boolean enableMemoryTracing = false;
        public boolean useAdvancedStagingBuffers = true;

        public boolean cpuRenderAhead = true;
        public int cpuRenderAheadLimit = 3;

        public boolean fpsOverlay = true;
    }

    public static class QualitySettings {
        public boolean enableClouds = false;
        public int cloudHeight = 160;
        public GraphicsQuality cloudQuality = GraphicsQuality.DEFAULT;
        public GraphicsQuality weatherQuality = GraphicsQuality.DEFAULT;
        public GraphicsQuality leavesQuality = GraphicsQuality.DEFAULT;
        public LightingQuality smoothLighting = LightingQuality.HIGH;

        public int biomeBlendRadius = 2;

        public boolean enableVignette = true;
    }

    public static class NotificationSettings {
        public boolean hasClearedDonationButton = false;
        public boolean hasSeenDonationPrompt = false;
    }

    public enum LightingQuality implements TextProvider {
        OFF(new TranslatableText("options.ao.off")),
        LOW(new TranslatableText("options.ao.min")),
        HIGH(new TranslatableText("options.ao.max"));

        private final Text name;

        LightingQuality(Text name) {
            this.name = name;
        }

        @Override
        public Text getLocalizedName() {
            return this.name;
        }
    }

    public enum GraphicsQuality implements TextProvider {
        DEFAULT("generator.default"),
        FANCY("options.graphics.fancy"),
        FAST("options.graphics.fast");

        private final Text name;

        GraphicsQuality(String name) {
            this.name = new TranslatableText(name);
        }

        @Override
        public Text getLocalizedName() {
            return this.name;
        }

        public boolean isFancy(boolean fancy) {
            return (this == FANCY) || (this == DEFAULT && !fancy) || fancy;
        }
    }

    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .excludeFieldsWithModifiers(Modifier.PRIVATE)
            .create();

    public static SodiumGameOptions loadFromDisk() {
        Path path = getConfigPath();
        SodiumGameOptions config;

        if (Files.exists(path)) {
            try (FileReader reader = new FileReader(path.toFile())) {
                config = GSON.fromJson(reader, SodiumGameOptions.class);
            } catch (IOException e) {
                throw new RuntimeException("Could not parse config", e);
            }
        } else {
            config = new SodiumGameOptions();
        }

        try {
            writeToDisk(config);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't update config file", e);
        }

        return config;
    }

    private static Path getConfigPath() {
        return PlatformRuntimeInformation.getInstance().getConfigDirectory()
                .resolve(DEFAULT_FILE_NAME);
    }

    public static void writeToDisk(SodiumGameOptions config) throws IOException {
        if (config.isReadOnly()) {
            throw new IllegalStateException("Config file is read-only");
        }

        Path path = getConfigPath();
        Path dir = path.getParent();

        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        } else if (!Files.isDirectory(dir)) {
            throw new IOException("Not a directory: " + dir);
        }

        FileUtil.writeTextRobustly(GSON.toJson(config), path);
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public void setReadOnly() {
        this.readOnly = true;
    }
}
