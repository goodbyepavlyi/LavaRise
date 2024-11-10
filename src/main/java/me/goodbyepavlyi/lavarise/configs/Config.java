package me.goodbyepavlyi.lavarise.configs;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.utils.ChatUtils;
import me.goodbyepavlyi.lavarise.utils.EnchantmentParser;
import me.goodbyepavlyi.lavarise.utils.Logger;
import me.goodbyepavlyi.lavarise.utils.YamlConfig;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Config extends YamlConfig {
    private final int CONFIG_VERSION = 3;

    public Config(LavaRiseInstance instance) {
        super(instance, "config.yml", true);
        this.migrateConfigVersion();
    }

    public void migrateConfigVersion() {
        Logger.debug(String.format("Checking config file version: %s", this.getFile().getName()));
        if (this.getConfigVersion() == this.CONFIG_VERSION) return;

        Logger.warning(String.format("The config file %s is outdated. Migrating to version %d...", this.getFile().getName(), this.CONFIG_VERSION));

        FileConfiguration resourceConfig = this.getResourceConfig();
        if (resourceConfig == null) return;

        if (this.getConfigVersion() == 1) {
            // Migrate deprecated %winner% placeholder to %player%
            this.getConfig().set("game.commands.winner", this.GameCommandsWinner()
                .stream()
                .map(command -> command.replace("%winner%", "%player%"))
                .collect(Collectors.toList()));
        }

        if (this.getConfigVersion() == 1 || this.getConfigVersion() == 2) {
            List<String> materialNames = this.getConfig().getStringList("game.items");
            this.getConfig().set("game.items", null);

            List<Map<String, Object>> itemList = new ArrayList<>();
            for (String materialName : materialNames) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("material", materialName);
                itemMap.put("name", null);
                itemMap.put("amount", 1);
                itemMap.put("lore", new ArrayList<String>());
                itemMap.put("enchantments", new ArrayList<String>());
                itemList.add(itemMap);
            }

            this.getConfig().set("game.items", itemList);
        }

        if (this.getConfigVersion() <= 3) {
            this.getConfig().set("game.lavaRisingTime.default", this.getConfig().getInt("game.lavaRisingTime"));
        }

        for (String key : resourceConfig.getKeys(true)) {
            if (this.getConfig().contains(key)) continue;
            Logger.debug(String.format("Migrating config key: %s", key));
            this.getConfig().set(key, resourceConfig.get(key));
        }

        this.setConfigVersion(this.CONFIG_VERSION);
        Logger.info(String.format("Config file %s migrated to version %d", this.getFile().getName(), this.CONFIG_VERSION));
        this.save();
    }

    public class LavaLevelConfig {
        private final int level;
        private final int time;

        public LavaLevelConfig(int level, int time) {
            this.level = level;
            this.time = time;
        }

        public int getLevel() {
            return level;
        }

        public int getTime() {
            return time;
        }
    }

    public class VisualEffectConfig {
        private final VisualEffectSoundConfig sound;
        private final VisualEffectTitleConfig title;

        public VisualEffectConfig(VisualEffectSoundConfig sound, VisualEffectTitleConfig title) {
            this.sound = sound;
            this.title = title;
        }

        public VisualEffectSoundConfig getSound() {
            return sound;
        }

        public VisualEffectTitleConfig getTitle() {
            return title;
        }
    }

    public class VisualEffectSoundConfig {
        private final boolean enabled;
        private final Sound sound;
        private final float volume;
        private final float pitch;

        public VisualEffectSoundConfig(boolean enabled, String sound, float volume, float pitch) {
            this.enabled = enabled;
            this.sound = Sound.valueOf(sound);
            this.volume = volume;
            this.pitch = pitch;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public Sound getSound() {
            return sound;
        }

        public float getVolume() {
            return volume;
        }

        public float getPitch() {
            return pitch;
        }
    }

    public class VisualEffectTitleConfig {
        private final boolean enabled;
        private final String title;
        private final String subtitle;
        private final int fadeIn;
        private final int stay;
        private final int fadeOut;

        public VisualEffectTitleConfig(boolean enabled, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
            this.enabled = enabled;
            this.title = ChatUtils.color(title);
            this.subtitle = ChatUtils.color(subtitle);
            this.fadeIn = fadeIn;
            this.stay = stay;
            this.fadeOut = fadeOut;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public int getFadeIn() {
            return fadeIn;
        }

        public int getStay() {
            return stay;
        }

        public int getFadeOut() {
            return fadeOut;
        }
    }

    public LavaLevelConfig getGameLavaRisingTime(int level) {
        return this.GameLavaRisingTimeLevels()
            .stream()
            .filter(lavaLevelConfig -> lavaLevelConfig.getLevel() >= level)
            .findFirst()
            .orElse(new LavaLevelConfig(0, this.GameLavaRisingTimeDefault()));
    }

    public int GameGracePhaseTime() {
        return this.getConfig().getInt("game.gracePhaseTime");
    }

    public int GameLavaRisingTimeDefault() {
        return this.getConfig().getInt("game.lavaRisingTime.default");
    }

    public List<LavaLevelConfig> GameLavaRisingTimeLevels() {
        List<LavaLevelConfig> lavaLevelConfigs = new ArrayList<>();
        for (Map<?, ?> lavaLevel : this.getConfig().getMapList("game.lavaRisingTime.levels")) {
            int level = (int) lavaLevel.get("level");
            int time = (int) lavaLevel.get("time");
            lavaLevelConfigs.add(new LavaLevelConfig(level, time));
        }

        return lavaLevelConfigs;
    }

    public int GameSpectatorSpawnYLavaOffset() {
        return this.getConfig().getInt("game.spectatorSpawnYLavaOffset");
    }

    public int GameEndGameDelay() {
        return this.getConfig().getInt("game.endGameDelay");
    }

    public int GamePVPGracePeriod() {
        return this.getConfig().getInt("game.pvpGracePeriod");
    }

    public List<ItemStack> GameItems() {
        List<Map<?, ?>> items = this.getConfig().getMapList("game.items");
        List<ItemStack> itemStacks = new ArrayList<>();

        for (Map<?, ?> item : items) {
            String materialName = (String) item.get("material");
            String name = (String) item.get("name");
            int amount = (int) item.get("amount");
            List<String> lore = (List<String>) item.get("lore");
            List<String> enchantments = (List<String>) item.get("enchantments");

            ItemStack itemStack = new ItemStack(Material.getMaterial(materialName), amount);
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (name != null) itemMeta.setDisplayName(ChatUtils.color(name));
            if (lore != null) itemMeta.setLore(ChatUtils.color(lore));
            itemStack.setItemMeta(itemMeta);

            if (enchantments != null) {
                for (String enchantment : enchantments) {
                    String[] enchantmentParts = enchantment.split(":");
                    itemStack.addUnsafeEnchantment(
                            EnchantmentParser.getEnchantment(enchantmentParts[0]),
                            Integer.parseInt(enchantmentParts[1])
                    );
                }
            }

            itemStacks.add(itemStack);
        }

        return itemStacks;
    }

    public List<String> GameCommandsWinner() {
        return this.getConfig().getStringList("game.commands.winner");
    }

    public List<String> GameCommandsLosers() {
        return this.getConfig().getStringList("game.commands.losers");
    }

    public List<String> GameCommandsPlayers() {
        return this.getConfig().getStringList("game.commands.players");
    }

    public VisualEffectConfig GameVisualEffectLava() {
        VisualEffectSoundConfig sound = new VisualEffectSoundConfig(
            this.getConfig().getBoolean("game.visualEffects.lava.sound.enabled"),
            this.getConfig().getString("game.visualEffects.lava.sound.sound"),
            (float) this.getConfig().getDouble("game.visualEffects.lava.sound.volume"),
            (float) this.getConfig().getDouble("game.visualEffects.lava.sound.pitch")
        );

        VisualEffectTitleConfig title = new VisualEffectTitleConfig(
            this.getConfig().getBoolean("game.visualEffects.lava.title.enabled"),
            this.getConfig().getString("game.visualEffects.lava.title.titleMessage"),
            this.getConfig().getString("game.visualEffects.lava.title.subtitleMessage"),
            this.getConfig().getInt("game.visualEffects.lava.title.fadeIn"),
            this.getConfig().getInt("game.visualEffects.lava.title.stay"),
            this.getConfig().getInt("game.visualEffects.lava.title.fadeOut")
        );

        return new VisualEffectConfig(sound, title);
    }

    public VisualEffectConfig GameVisualEffectDeathmatch() {
        VisualEffectSoundConfig sound = new VisualEffectSoundConfig(
            this.getConfig().getBoolean("game.visualEffects.deathmatch.sound.enabled"),
            this.getConfig().getString("game.visualEffects.deathmatch.sound.sound"),
            (float) this.getConfig().getDouble("game.visualEffects.deathmatch.sound.volume"),
            (float) this.getConfig().getDouble("game.visualEffects.deathmatch.sound.pitch")
        );

        VisualEffectTitleConfig title = new VisualEffectTitleConfig(
            this.getConfig().getBoolean("game.visualEffects.deathmatch.title.enabled"),
            this.getConfig().getString("game.visualEffects.deathmatch.title.titleMessage"),
            this.getConfig().getString("game.visualEffects.deathmatch.title.subtitleMessage"),
            this.getConfig().getInt("game.visualEffects.deathmatch.title.fadeIn"),
            this.getConfig().getInt("game.visualEffects.deathmatch.title.stay"),
            this.getConfig().getInt("game.visualEffects.deathmatch.title.fadeOut")
        );

        return new VisualEffectConfig(sound, title);
    }

    public int QueueCountdown() {
        return this.getConfig().getInt("queue.countdown");
    }

    public boolean QueueHalfFullQueueCountdownEnabled() {
        return this.getConfig().getBoolean("queue.halfFullQueueCountdown.enabled");
    }

    public int QueueHalfFullQueueCountdownValue() {
        return this.getConfig().getInt("queue.halfFullQueueCountdown.value");
    }

    public Material QueueLeaveItemMaterial() {
        return Material.getMaterial(this.getConfig().getString("queue.leaveItem.material"));
    }

    public int QueueLeaveItemSlot() {
        return this.getConfig().getInt("queue.leaveItem.slot");
    }

    public boolean Metrics() {
        return this.getConfig().getBoolean("metrics");
    }
}
