package me.oriharel.playershops.inventory.item

import me.oriharel.playershops.inventory.DataHolder
import me.oriharel.playershops.inventory.Inventory
import me.oriharel.playershops.inventory.InventoryLocation
import me.oriharel.playershops.inventory.InventoryPage
import org.bukkit.inventory.ItemStack

class DataHolderNavigationItem<T>(route: String, parent: Inventory, location: InventoryLocation, item: ItemStack, override var dataHeld: T, val constructPage: (T) -> InventoryPage) : InventoryNavigationItem(route, parent, location, item), DataHolder<T> {
    override fun construct(payload: T): InventoryPage {
        return constructPage(payload)
    }

}