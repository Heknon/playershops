package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import fr.minuskube.inv.content.InventoryProvider
import me.oriharel.playershops.shops.shop.PlayerShop
import me.oriharel.playershops.utilities.KItemStack
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player

class ShopSettingsInventory : InventoryProvider {

    override fun init(player: Player, contents: InventoryContents) {

        contents.set(0, 4,
                ClickableItem.of(
                        KItemStack(
                                material = Material.EMERALD_BLOCK,
                                displayName = "&6Reinitialize your shop"
                        )
                ) {
                    // Reinitialize a shop
                    getShop(contents).opeInitializationGUI(player)
                })

        buildMobCoinsItem(contents, player)
        buildBankItem(contents, player)

    }

    override fun update(player: Player, contents: InventoryContents) {

    }

    private fun buildMobCoinsItem(contents: InventoryContents, player: Player) {
        contents.set(1, 2,
                ClickableItem.of(
                        KItemStack(
                                material = Material.MAGMA_CREAM,
                                displayName = "&6" + if (getUseMobCoins(contents)) "USING ZENCOINS" else "USING MONEY"
                        )
                ) {
                    // Logic on whether to use zen coins or not
                    val useMobCoinsCurr = !getUseMobCoins(contents)
                    setUseMobCoins(contents, useMobCoinsCurr)
                    buildMobCoinsItem(contents, player)
                    player.sendMessage("§b§l[INFO] §eYour shop is now using " + if (useMobCoinsCurr) "Zen Coins" else "Money" + " as it's primary currency")
                })
    }

    private fun buildBankItem(contents: InventoryContents, player: Player) {
        contents.set(1, 6,
                ClickableItem.of(
                        KItemStack(
                                material = Material.PAPER,
                                displayName = "&6" + if (getUseBank(contents)) "Bank" else "Click to enable the bank"
                        )
                ) {
                    onBankItemClick(contents, player)
                })
    }

    private fun onBankItemClick(contents: InventoryContents, player: Player) {
        // Logic on whether to use bank and if enabled open bank gui
        if (getUseBank(contents)) {
            BankInventory.INVENTORY.open(player)
            BankInventory.INVENTORY.manager.getContents(player).ifPresent {
                it.setProperty(InventoryConstants.PASSED_DOWN_SHOP_CONTENT_ID, getShop(contents))
                player.sendMessage("§b§l[INFO] §bOpened bank")
            }
        } else {
            setUseBank(contents, true)
            buildBankItem(contents, player)
            player.sendMessage("§b§l[INFO] §bActivated bank")
        }
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

    private fun setUseBank(contents: InventoryContents, useBank: Boolean): ShopSettingsInventory {
        contents.setProperty(InventoryConstants.USE_BANK_CONTENT_ID, useBank)
        return this
    }

    private fun setUseMobCoins(contents: InventoryContents, mobCoins: Boolean): ShopSettingsInventory {
        contents.setProperty(InventoryConstants.USE_MOB_COINS_CONTENT_ID, mobCoins)
        return this
    }

    companion object {
        val INVENTORY: SmartInventory = SmartInventory.builder()
                .id(InventoryConstants.SettingsInventory.ID)
                .provider(ShopSettingsInventory())
                .size(InventoryConstants.SettingsInventory.ROWS, InventoryConstants.SettingsInventory.COLUMNS)
                .title(InventoryConstants.SettingsInventory.TITLE)
                .closeable(InventoryConstants.SettingsInventory.CLOSEABLE)
                .build()
    }
}