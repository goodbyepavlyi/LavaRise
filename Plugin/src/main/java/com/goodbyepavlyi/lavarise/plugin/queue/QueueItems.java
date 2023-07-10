package com.goodbyepavlyi.lavarise.plugin.queue;

import com.goodbyepavlyi.lavarise.plugin.LavaRise;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class QueueItems {
    private final LavaRise instance;

    public QueueItems(LavaRise instance) {
        this.instance = instance;
    }

    public ItemStack getLeaveItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(instance.getMessages().QUEUE_ITEMS_LEAVE_NAME());

        item.setItemMeta(itemMeta);

        return item;
    }
}
