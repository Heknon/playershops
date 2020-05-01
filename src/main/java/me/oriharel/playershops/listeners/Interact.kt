package me.oriharel.playershops.listeners

import me.oriharel.playershops.PlayerShopManager
import me.oriharel.playershops.shops.shop.PlayerShop
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class Interact(private val shopManager: PlayerShopManager) : Listener {
    @EventHandler
    fun onInteract(e: PlayerInteractEvent) {
        if (e.action != Action.RIGHT_CLICK_BLOCK) return

        val shop: PlayerShop = shopManager.getPlayerShopFromBlock(e.clickedBlock) ?: return
        shop.open(e.player.uniqueId)
    }
}