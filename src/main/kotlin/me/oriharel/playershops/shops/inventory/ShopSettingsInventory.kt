package me.oriharel.playershops.shops.inventory

import me.oriharel.playershops.inventory.Inventory
import me.oriharel.playershops.inventory.InventoryPage

class ShopSettingsInventory : Inventory() {
    override val routes: MutableMap<String, InventoryPage> = HashMap()
}