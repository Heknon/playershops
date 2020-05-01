package me.oriharel.playershops.inventory

import me.oriharel.playershops.inventory.item.InventoryItem
import org.bukkit.inventory.InventoryHolder

abstract class InventoryPage(val cancelOnClick: Boolean, val fillment: InventoryItem) : InventoryHolder {
    abstract val parent: Inventory
    abstract val inventoryItems: MutableList<InventoryItem>

    fun construct() {
        val inventory = this.inventory
        val filledIndices: MutableSet<Int> = HashSet()

        for (i in inventoryItems) {
            val index = i.location.getIndex()
            inventory.setItem(index, i.item)
            filledIndices.add(index)
        }
        for (i in 0..inventory.size) {
            if (!filledIndices.contains(i)) {
                inventory.setItem(i, fillment)
            }
        }
    }
}