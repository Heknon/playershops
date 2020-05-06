package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.content.InventoryContents
import fr.minuskube.inv.content.InventoryProvider
import org.bukkit.entity.Player

interface NotUpdatableInventoryProvider : InventoryProvider {

    override fun init(player: Player, contents: InventoryContents)

    override fun update(player: Player?, contents: InventoryContents?) {

    }
}