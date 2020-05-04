package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import fr.minuskube.inv.content.InventoryProvider
import me.oriharel.playershops.shops.shop.PlayerShop
import org.bukkit.ChatColor
import org.bukkit.entity.Player

class BankInventory : InventoryProvider {

    override fun init(player: Player, contents: InventoryContents) {

    }

    override fun update(player: Player, contents: InventoryContents) {

    }

    private fun getShop(contents: InventoryContents): PlayerShop {
        return contents.property(InventoryConstants.PASSED_DOWN_SHOP_CONTENT_ID)
    }

    private fun getUseMobCoins(contents: InventoryContents): Boolean {
        return contents.property(InventoryConstants.USE_MOB_COINS_CONTENT_ID)
    }

    private fun getUseBank(contents: InventoryContents): Boolean {
        return contents.property(InventoryConstants.USE_BANK_CONTENT_ID)
    }

    companion object {

        val INVENTORY: SmartInventory = SmartInventory.builder()
                .id(InventoryConstants.BankInventory.ID)
                .provider(BankInventory())
                .size(InventoryConstants.BankInventory.ROWS, InventoryConstants.BankInventory.COLUMNS)
                .closeable(InventoryConstants.BankInventory.CLOSEABLE)
                .title(InventoryConstants.BankInventory.TITLE)
                .build()
    }
}