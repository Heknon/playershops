package me.oriharel.playershops.inventory

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class InventoryListeners : Listener {
    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        val clickedInventory: Inventory? = e.clickedInventory
        if (clickedInventory == null || clickedInventory.holder == null || clickedInventory.holder !is InventoryPage) return

        val holder: InventoryPage = clickedInventory.holder!! as InventoryPage
        e.isCancelled = holder.cancelOnClick
        for (i in 0..clickedInventory.size) {
            val item = clickedInventory.getItem(i)
            val invItem = holder.inventoryItems[InventoryLocation.fromIndex(i)]
            if (item == invItem!!.item) {
                invItem.onClick?.invoke()
            }
        }
    }

    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        if (e.inventory.holder !is InventoryPage) return
        (e.inventory.holder as InventoryPage).onClose?.invoke()
    }
}