package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import fr.minuskube.inv.content.InventoryProvider
import org.bukkit.ChatColor
import org.bukkit.entity.Player

class ShopSettingsInventory : InventoryProvider {

    override fun init(player: Player, contents: InventoryContents) {

    }

    override fun update(player: Player, contents: InventoryContents) {

    }

    companion object {
        val inventory: SmartInventory = SmartInventory.builder()
                .id("shopSettingsInventory")
                .provider(ShopSettingsInventory())
                .size(3, 9)
                .title("${ChatColor.BLUE}Settings")
                .build()
    }
}