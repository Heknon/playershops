package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import fr.minuskube.inv.content.InventoryProvider
import me.oriharel.playershops.utilities.KItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ConfirmationInventory(private val deny: ItemStack, private val confirm: ItemStack, private val onClick: (ConfirmationType) -> Unit) : InventoryProvider {

    override fun init(player: Player, contents: InventoryContents) {
        contents.set(0, 2, ClickableItem.of(deny) {
            onClick(ConfirmationType.DENY)
        })
        contents.set(0, 6, ClickableItem.of(confirm) {
            onClick(ConfirmationType.CONFIRM)
        })
    }

    override fun update(p0: Player?, p1: InventoryContents?) {

    }
}