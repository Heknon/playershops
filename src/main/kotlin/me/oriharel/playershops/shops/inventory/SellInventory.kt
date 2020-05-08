package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import me.oriharel.playershops.PlayerShops
import me.oriharel.playershops.shops.shop.PlayerShop
import me.oriharel.playershops.shops.shop.ShopSetting
import me.oriharel.playershops.utilities.Utils.amount
import me.oriharel.playershops.utilities.Utils.toOfflinePlayer
import me.swanis.mobcoins.MobCoinsAPI
import org.bukkit.Material
import org.bukkit.entity.Player
import kotlin.math.min

class SellInventory : PurchaseInventory(PlayerShops.INSTANCE.economy) {

    override fun init(player: Player, contents: InventoryContents) {
        super.init(player, contents)
        val shop = InventoryConstants.ConstantUtilities.getShop(contents)

        contents.set(3, 2, ClickableItem.of(getSellItem("1")) {
            sellItems(player, shop!!, 1)
        })

        contents.set(3, 4, ClickableItem.of(getSellItem("64")) {
            sellItems(player, shop!!, 64)
        })

        contents.set(3, 6, ClickableItem.of(getSellItem("All")) {
            sellItems(player, shop!!, player.inventory.amount(shop.item).toLong())
        })
    }

    private fun sellItems(player: Player, shop: PlayerShop, amount: Long) {
        val pair = handleSellItems(player, shop, amount)
        if (pair.first != PurchaseReason.SUCCESS) {
            player.sendMessage(getFailMessage(pair.first))
        } else {
            player.sendMessage(getSellMessage(shop, amount))
        }
    }

    private fun handleSellItems(player: Player, shop: PlayerShop, amount: Long): Pair<PurchaseReason, Long> {
        val balanceOfShop = shop.balance

        if (shop.isFull) {
            return Pair(PurchaseReason.NO_SHOP_SPACE, 0)
        } else if (balanceOfShop.toLong() < shop.price!!) {
            return Pair(PurchaseReason.SHOP_INSUFFICIENT_FUNDS, 0)
        }

        val profile = MobCoinsAPI.getProfileManager().getProfile(player)
        val amountToSell = min(player.inventory.amount(shop.item).toLong(), min(min(amount, shop.storageRemaining), balanceOfShop.toLong() / shop.price!!))

        if (shop.settings.contains(ShopSetting.USE_INTERNAL_BANK)) {
            shop.bank!!.takeFromAndDeposit(amountToSell * shop.price!!, player.uniqueId)
        } else {
            if (shop.settings.contains(ShopSetting.USE_MOB_COINS)) {
                profile.mobCoins += (amountToSell * shop.price!!).toInt()
            } else {
                economy.withdrawPlayer(shop.owner?.toOfflinePlayer(), (amountToSell * shop.price!!).toDouble())
                economy.depositPlayer(player, (amountToSell * shop.price!!).toDouble())
            }
        }
        return Pair(PurchaseReason.SUCCESS, amountToSell)
    }

    private fun getSellMessage(shop: PlayerShop, amount: Long) = getPurchaseMessage(shop, "sold", amount, amount * shop.price!!, "to")
    private fun getSellItem(amount: String) = getBuySellItem(Material.RED_STAINED_GLASS_PANE, "&c&l", "Sell", "to", amount)

    companion object {
        val INVENTORY: SmartInventory = SmartInventory.builder()
                .id(InventoryConstants.PurchaseInventory.ID)
                .title(InventoryConstants.PurchaseInventory.TITLE)
                .size(InventoryConstants.PurchaseInventory.ROWS, InventoryConstants.PurchaseInventory.COLUMNS)
                .closeable(InventoryConstants.PurchaseInventory.CLOSEABLE)
                .provider(SellInventory())
                .manager(PlayerShops.INSTANCE.inventoryManager)
                .build()
    }
}