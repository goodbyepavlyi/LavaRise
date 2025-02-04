package me.goodbyepavlyi.lavarise.configs;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.utils.ChatUtils;
import me.goodbyepavlyi.lavarise.utils.EnchantmentParser;
import me.goodbyepavlyi.lavarise.utils.Logger;
import me.goodbyepavlyi.lavarise.utils.YamlConfig;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
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
    private final int CONFIG_VERSION = 8;

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

    public enum VisualEffectType {
        LAVA("lava"),
        PVP("pvp"),
        DEATHMATCH("deathmatch"),
        WINNER("winner"),
        SPECTATOR("spectator");

        private final String key;

        VisualEffectType(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public class VisualEffectConfig {
        private VisualEffectSoundConfig sound;
        private VisualEffectTitleConfig title;
        private VisualEffectParticleConfig particle;

        public VisualEffectConfig(VisualEffectSoundConfig sound, VisualEffectTitleConfig title) {
            this.sound = sound;
            this.title = title;
        }

        public VisualEffectConfig(VisualEffectSoundConfig sound, VisualEffectParticleConfig particle) {
            this.sound = sound;
            this.particle = particle;
        }

        public VisualEffectConfig(VisualEffectSoundConfig sound, VisualEffectTitleConfig title, VisualEffectParticleConfig particle) {
            this.sound = sound;
            this.title = title;
            this.particle = particle;
        }

        public VisualEffectSoundConfig getSound() {
            return sound;
        }

        public VisualEffectTitleConfig getTitle() {
            return title;
        }

        public VisualEffectParticleConfig getParticle() {
            return particle;
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

    public class VisualEffectParticleConfig {
        private final boolean enabled;
        private final Particle particle;
        private final int amount;
        private final double offsetX;
        private final double offsetY;
        private final double offsetZ;
        private final double speed;

        public VisualEffectParticleConfig(boolean enabled, String particle, int amount, double offsetX, double offsetY, double offsetZ, double speed) {
            this.enabled = enabled;
            this.particle = Particle.valueOf(particle);
            this.amount = amount;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.speed = speed;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public Particle getParticle() {
            return particle;
        }

        public int getAmount() {
            return amount;
        }

        public double getOffsetX() {
            return offsetX;
        }

        public double getOffsetY() {
            return offsetY;
        }

        public double getOffsetZ() {
            return offsetZ;
        }

        public double getSpeed() {
            return speed;
        }
    }

    public LavaLevelConfig getGameLavaRisingTime(int level) {
        return this.GameLavaRisingTimeLevels()
            .stream()
            .filter(lavaLevelConfig -> lavaLevelConfig.getLevel() >= level)
            .findFirst()
            .orElse(new LavaLevelConfig(0, this.GameLavaRisingTimeDefault()));
    }

    // TODO: refactor this code
    public VisualEffectConfig getGameVisualEffect(VisualEffectType type) {
        VisualEffectSoundConfig sound = new VisualEffectSoundConfig(
            this.getConfig().getBoolean(String.format("game.visualEffects.%s.sound.enabled", type.getKey())),
            this.getConfig().getString(String.format("game.visualEffects.%s.sound.sound", type.getKey())),
            (float) this.getConfig().getDouble(String.format("game.visualEffects.%s.sound.volume", type.getKey())),
            (float) this.getConfig().getDouble(String.format("game.visualEffects.%s.sound.pitch", type.getKey()))
        );

        return switch (type) {
            case LAVA, DEATHMATCH, PVP, SPECTATOR -> {
                VisualEffectTitleConfig title = new VisualEffectTitleConfig(
                    this.getConfig().getBoolean(String.format("game.visualEffects.%s.title.enabled", type.getKey())),
                    this.getConfig().getString(String.format("game.visualEffects.%s.title.titleMessage", type.getKey())),
                    this.getConfig().getString(String.format("game.visualEffects.%s.title.subtitleMessage", type.getKey())),
                    this.getConfig().getInt(String.format("game.visualEffects.%s.title.fadeIn", type.getKey())),
                    this.getConfig().getInt(String.format("game.visualEffects.%s.title.stay", type.getKey())),
                    this.getConfig().getInt(String.format("game.visualEffects.%s.title.fadeOut", type.getKey()))
                );

                yield new VisualEffectConfig(sound, title);
            }
            case WINNER -> {
                VisualEffectTitleConfig title = new VisualEffectTitleConfig(
                    this.getConfig().getBoolean(String.format("game.visualEffects.%s.title.enabled", type.getKey())),
                    this.getConfig().getString(String.format("game.visualEffects.%s.title.titleMessage", type.getKey())),
                    this.getConfig().getString(String.format("game.visualEffects.%s.title.subtitleMessage", type.getKey())),
                    this.getConfig().getInt(String.format("game.visualEffects.%s.title.fadeIn", type.getKey())),
                    this.getConfig().getInt(String.format("game.visualEffects.%s.title.stay", type.getKey())),
                    this.getConfig().getInt(String.format("game.visualEffects.%s.title.fadeOut", type.getKey()))
                );

                VisualEffectParticleConfig particle = new VisualEffectParticleConfig(
                    this.getConfig().getBoolean(String.format("game.visualEffects.%s.particle.enabled", type.getKey())),
                    this.getConfig().getString(String.format("game.visualEffects.%s.particle.particle", type.getKey())),
                    this.getConfig().getInt(String.format("game.visualEffects.%s.particle.count", type.getKey())),
                    this.getConfig().getDouble(String.format("game.visualEffects.%s.particle.offsetX", type.getKey())),
                    this.getConfig().getDouble(String.format("game.visualEffects.%s.particle.offsetY", type.getKey())),
                    this.getConfig().getDouble(String.format("game.visualEffects.%s.particle.offsetZ", type.getKey())),
                    this.getConfig().getDouble(String.format("game.visualEffects.%s.particle.speed", type.getKey()))
                );

                yield new VisualEffectConfig(sound, title, particle);
            }
        };
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
    
    public GameMode GameSpectatorGameMode() {
        return GameMode.valueOf(this.getConfig().getString("game.spectator.gameMode"));
    }

    public boolean GameDeathmatchDamageEnabled() {
        return this.getConfig().getBoolean("game.deathmatch.damage.enabled");
    }

    public int GameDeathmatchDamageDelay() {
        return this.getConfig().getInt("game.deathmatch.damage.delay");
    }

    public double GameDeathmatchDamageAmount() {
        return this.getConfig().getDouble("game.deathmatch.damage.amount");
    }

    public int GameDeathmatchDamageInterval() {
        return this.getConfig().getInt("game.deathmatch.damage.interval");
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

    public boolean GameAllowedCommandsEnabled() {
        return this.getConfig().getBoolean("game.allowedCommands.enabled");
    }

    public List<String> GameAllowedCommands() {
        return this.getConfig().getStringList("game.allowedCommands.commands");
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

    public boolean QueueTipsEnable() {
        return this.getConfig().getBoolean("queue.tips.enable");
    }

    public int QueueTipsInterval() {
        return this.getConfig().getInt("queue.tips.interval");
    }
    
    public boolean QueueJoinSoundEnabled() {
        return this.getConfig().getBoolean("queue.joinSound.enabled");
    }
    
    public Sound QueueJoinSound() {
        return Sound.valueOf(this.getConfig().getString("queue.joinSound.sound"));
    }
    
    public boolean Metrics() {
        return this.getConfig().getBoolean("metrics");
    }
}
