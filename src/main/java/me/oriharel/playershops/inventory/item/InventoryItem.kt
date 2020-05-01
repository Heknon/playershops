package me.oriharel.playershops.inventory.item

import me.oriharel.playershops.inventory.InventoryLocation
import org.bukkit.inventory.ItemStack

abstract class InventoryItem(val location: InventoryLocation, val item: ItemStack) : ItemStack(item) {
    var onClick: (() -> Unit)? = null

    fun setOnClick(onClick: () -> Unit): InventoryItem {
        this.onClick = onClick
        return this
    }
}