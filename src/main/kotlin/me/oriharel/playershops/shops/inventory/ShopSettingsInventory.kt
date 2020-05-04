package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.InventoryListener
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import fr.minuskube.inv.content.InventoryProvider
import me.oriharel.playershops.PlayerShops
import me.oriharel.playershops.shops.shop.ShopSetting
import me.oriharel.playershops.utilities.KItemStack
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent

class ShopSettingsInventory : InventoryProvider {

    override fun init(player: Player, contents: InventoryContents) {

        val shop = InventoryConstants.ConstantUtilities.getShop(contents)!!

        InventoryConstants.ConstantUtilities.setUseBank(contents, shop.settings?.contains(ShopSetting.USE_INTERNAL_BANK)!!)
        InventoryConstants.ConstantUtilities.setUseMobCoins(contents, shop.settings.contains(ShopSetting.USE_MOB_COINS))

        contents.set(0, 4,
                ClickableItem.of(
                        KItemStack(
                                material = Material.EMERALD_BLOCK,
                                displayName = "&6Reinitialize your shop"
                        )
                ) {
                    // Reinitialize a shop
                    shop.opeInitializationGUI(player)
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
                                displayName = "&6" + if (InventoryConstants.ConstantUtilities.getUseMobCoins(contents)!!) "USING ZENCOINS" else "USING MONEY"
                        )
                ) {
                    // Logic on whether to use zen coins or not
                    val useMobCoinsCurr = !InventoryConstants.ConstantUtilities.getUseMobCoins(contents)!!
                    InventoryConstants.ConstantUtilities.setUseMobCoins(contents, useMobCoinsCurr)
                    buildMobCoinsItem(contents, player)
                    player.sendMessage("§b§l[INFO] §eYour shop is now using " + if (useMobCoinsCurr) "Zen Coins" else "Money" + " as it's primary currency")
                })
    }

    private fun buildBankItem(contents: InventoryContents, player: Player) {
        contents.set(1, 6,
                ClickableItem.of(
                        KItemStack(
                                material = Material.PAPER,
                                displayName = "&6Bank"
                        )
                ) {
                    onBankItemClick(contents, player)
                })
    }

    private fun onBankItemClick(contents: InventoryContents, player: Player) {
        // Logic on whether to use bank and if enabled open bank gui
        BankInventory.INVENTORY.open(player)
        BankInventory.INVENTORY.manager.getContents(player).ifPresent {
            it.setProperty(InventoryConstants.PASSED_DOWN_SHOP_CONTENT_ID, InventoryConstants.ConstantUtilities.getShop(contents))
            player.sendMessage("§b§l[INFO] §bOpened bank")
        }
    }

    companion object {
        val INVENTORY: SmartInventory = SmartInventory.builder()
                .id(InventoryConstants.SettingsInventory.ID)
                .provider(ShopSettingsInventory())
                .size(InventoryConstants.SettingsInventory.ROWS, InventoryConstants.SettingsInventory.COLUMNS)
                .title(InventoryConstants.SettingsInventory.TITLE)
                .closeable(InventoryConstants.SettingsInventory.CLOSEABLE)
                .listener(InventoryListener(InventoryCloseEvent::class.java) {
                    print("closed")
                    val contents = PlayerShops.INSTANCE.inventoryManager.getContents(it.player as Player)!!.get()
                    val shop = InventoryConstants.ConstantUtilities.getShop(contents)!!
                    val useMobCoins = InventoryConstants.ConstantUtilities.getUseMobCoins(contents)!!

                    if (useMobCoins)
                        shop.settings?.add(ShopSetting.USE_MOB_COINS)
                    else
                        shop.settings?.remove(ShopSetting.USE_MOB_COINS)

                })
                .manager(PlayerShops.INSTANCE.inventoryManager)
                .build()
    }
}