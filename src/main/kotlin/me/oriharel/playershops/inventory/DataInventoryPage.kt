package me.oriharel.playershops.inventory

import me.oriharel.playershops.inventory.item.InventoryItem

class DataInventoryPage<T>(
        cancelOnClick: Boolean,
        fillment: InventoryItem, override val parent: Inventory, override val inventoryItems: MutableList<InventoryItem>, override var dataHeld: T
) : InventoryPage(
        cancelOnClick,
        fillment
), DataHolder<T> {
    override fun construct(payload: T): InventoryPage {
        TODO("Not yet implemented")
    }

    override fun getInventory(): org.bukkit.inventory.Inventory {
        TODO("Not yet implemented")
    }
}