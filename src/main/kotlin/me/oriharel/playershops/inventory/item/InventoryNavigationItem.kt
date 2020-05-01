package me.oriharel.playershops.inventory.item

import me.oriharel.playershops.inventory.Inventory
import me.oriharel.playershops.inventory.InventoryLocation
import org.bukkit.inventory.ItemStack

open class InventoryNavigationItem(val route: String, val parent: Inventory, location: InventoryLocation, item: ItemStack) : InventoryItem(location, item) {
    fun navigate() {
        parent.navigate(parent.routes[route]!!)
    }
}