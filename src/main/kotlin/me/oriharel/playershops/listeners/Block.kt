package me.oriharel.playershops.listeners

import me.oriharel.playershops.PlayerShopManager
import me.oriharel.playershops.ShopItem
import me.oriharel.playershops.utilities.Utils.getNBTClone
import net.minecraft.server.v1_15_R1.NBTTagCompound
import org.bukkit.block.BlockState
import org.bukkit.block.TileState
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

class Block(private val playerShopManager: PlayerShopManager) : Listener {

    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        val data: BlockState = e.block.state
        if (data !is TileState) return
        val compound: NBTTagCompound = e.itemInHand.getNBTClone()
        if (!compound.hasKey("playerShop")) return
        val shopItem: ShopItem = playerShopManager.getShopItem(e.itemInHand)!!
        shopItem.shop.onPlace(e, playerShopManager.playerShops)
    }

    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        val data: BlockState = e.block.state
        if (data !is TileState) return
        playerShopManager.removePlayerShop(e.block)
    }
}