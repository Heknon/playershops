package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.InventoryListener
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import me.oriharel.playershops.PlayerShops
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent

class StorageInventory : NotUpdatableInventoryProvider {

    override fun init(player: Player, contents: InventoryContents) {


    }

    private fun getShopifiedItem(contents: InventoryContents) {

    }

    companion object {
        val INVENTORY: SmartInventory = SmartInventory.builder()
                .id(InventoryConstants.StorageInventory.ID)
                .provider(StorageInventory())
                .size(InventoryConstants.StorageInventory.ROWS, InventoryConstants.StorageInventory.COLUMNS)
                .title(InventoryConstants.StorageInventory.TITLE)
                .closeable(InventoryConstants.StorageInventory.CLOSEABLE)
                .listener(InventoryListener(InventoryCloseEvent::class.java) {
                    val contents = PlayerShops.INSTANCE.inventoryManager.getContents(it.player as Player)!!.get()
                    val shop = InventoryConstants.ConstantUtilities.getShop(contents) ?: return@InventoryListener

                    shop.buildHologram(PlayerShops.INSTANCE)
                    PlayerShops.INSTANCE.shopManager.setPlayerShopBlockState(shop.block!!, shop)

                })
                .manager(PlayerShops.INSTANCE.inventoryManager)
                .build()
    }
}