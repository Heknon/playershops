package me.oriharel.playershops.inventory

class ShopInventory : Inventory() {
    override val routes: MutableMap<String, InventoryPage> = HashMap()
}