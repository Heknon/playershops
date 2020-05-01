package me.oriharel.playershops.inventory

import org.bukkit.entity.Player

abstract class Inventory {
    private var currentPage: InventoryPage? = null
    abstract val routes: MutableMap<String, InventoryPage>
    fun navigate(nextPage: InventoryPage, player: Player) {
        currentPage = nextPage
        player.openInventory(currentPage!!.inventory)
    }

    fun <T> dataNavigate(nextPage: DataHolder<T>, data: T, player: Player) {
        if (nextPage !is InventoryPage) return
        nextPage.dataHeld = data
        currentPage = nextPage
        player.openInventory(currentPage!!.inventory)
    }
}