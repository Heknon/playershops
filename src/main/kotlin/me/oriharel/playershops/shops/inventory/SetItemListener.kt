package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.InventoryManager
import me.oriharel.playershops.utilities.Utils.openWithProperties
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class SetItemListener(private val inventoryManager: InventoryManager) : Listener {

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        if (e.view.title != "Set Item - Player Shop") return
        if (e.view.bottomInventory != e.clickedInventory) return
        if (e.currentItem == null || e.currentItem!!.type == Material.AIR) return

        inventoryManager.getContents(e.whoClicked as Player).ifPresent {
            val shop = InventoryConstants.ConstantUtilities.getShop(it)!!
            shop.item = e.currentItem
            shop.update()
            ShopSettingsInventory.INVENTORY.openWithProperties(e.whoClicked as Player, mutableMapOf(Pair(InventoryConstants.PASSED_DOWN_SHOP_CONTENT_ID, shop)))
        }
    }

}