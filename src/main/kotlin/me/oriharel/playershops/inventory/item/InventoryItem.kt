package me.oriharel.playershops.inventory.item

import org.bukkit.inventory.ItemStack

open class InventoryItem(val item: ItemStack) : ItemStack(item) {
    var onClick: (() -> Unit)? = null

    fun setOnClick(onClick: () -> Unit): InventoryItem {
        this.onClick = onClick
        return this
    }
}