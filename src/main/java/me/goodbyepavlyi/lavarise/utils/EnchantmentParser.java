package me.goodbyepavlyi.lavarise.utils;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;

public class EnchantmentParser {
    private static final Map<String, Enchantment> enchantmentMap = new HashMap<>();
    private static final boolean isLegacy;

    static {
        String version = Bukkit.getBukkitVersion();
        isLegacy = version.startsWith("1.12") || version.startsWith("1.13");

        if (isLegacy) {
            enchantmentMap.put("EFFICIENCY", Enchantment.getByName("DIG_SPEED"));
            enchantmentMap.put("UNBREAKING", Enchantment.getByName("DURABILITY"));
            enchantmentMap.put("FORTUNE", Enchantment.getByName("LOOT_BONUS_BLOCKS"));
            enchantmentMap.put("SHARPNESS", Enchantment.getByName("DAMAGE_ALL"));
            enchantmentMap.put("SMITE", Enchantment.getByName("DAMAGE_UNDEAD"));
            enchantmentMap.put("BANE_OF_ARTHROPODS", Enchantment.getByName("DAMAGE_ARTHROPODS"));
            enchantmentMap.put("POWER", Enchantment.getByName("ARROW_DAMAGE"));
            enchantmentMap.put("PUNCH", Enchantment.getByName("ARROW_KNOCKBACK"));
            enchantmentMap.put("FLAME", Enchantment.getByName("ARROW_FIRE"));
            enchantmentMap.put("INFINITY", Enchantment.getByName("ARROW_INFINITE"));
            enchantmentMap.put("LOOTING", Enchantment.getByName("LOOT_BONUS_MOBS"));
            enchantmentMap.put("PROTECTION", Enchantment.getByName("PROTECTION_ENVIRONMENTAL"));
            enchantmentMap.put("FIRE_PROTECTION", Enchantment.getByName("PROTECTION_FIRE"));
            enchantmentMap.put("FEATHER_FALLING", Enchantment.getByName("PROTECTION_FALL"));
            enchantmentMap.put("BLAST_PROTECTION", Enchantment.getByName("PROTECTION_EXPLOSIONS"));
            enchantmentMap.put("PROJECTILE_PROTECTION", Enchantment.getByName("PROTECTION_PROJECTILE"));
            enchantmentMap.put("RESPIRATION", Enchantment.getByName("OXYGEN"));
            enchantmentMap.put("AQUA_AFFINITY", Enchantment.getByName("WATER_WORKER"));
        }
    }

    public static Enchantment getEnchantment(String name) {
        if (enchantmentMap.containsKey(name.toUpperCase())) {
            Logger.debug(String.format("Enchantment %s is a legacy mapping, using %s instead", name, enchantmentMap.get(name.toUpperCase()).getName()));
            return enchantmentMap.get(name.toUpperCase());
        }

        return Enchantment.getByName(name.toUpperCase());
    }
}
