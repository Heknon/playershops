package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import me.oriharel.playershops.PlayerShops
import me.oriharel.playershops.shops.shop.PlayerShop
import me.oriharel.playershops.shops.shop.ShopSetting
import me.oriharel.playershops.utilities.Utils.applyPlaceholders
import me.oriharel.playershops.utilities.Utils.availableSpace
import me.oriharel.playershops.utilities.Utils.format
import me.oriharel.playershops.utilities.Utils.toOfflinePlayer
import me.oriharel.playershops.utilities.message.Message
import me.swanis.mobcoins.MobCoinsAPI
import org.bukkit.Material
import org.bukkit.entity.Player
import kotlin.math.min

class BuyInventory : PurchaseInventory(PlayerShops.INSTANCE.economy) {

    override fun init(player: Player, contents: InventoryContents) {
        super.init(player, contents)
        val shop = InventoryConstants.ConstantUtilities.getShop(contents)

        contents.set(3, 2, ClickableItem.of(getBuyItem("1")) {
            buyItems(player, shop!!, 1)
        })

        contents.set(3, 4, ClickableItem.of(getBuyItem("64")) {
            buyItems(player, shop!!, 64)
        })

        contents.set(3, 6, ClickableItem.of(getBuyItem("All")) {
            buyItems(player, shop!!, shop.item?.amount?.toLong()?.minus(1) ?: 0)
        })
    }

    private fun buyItems(player: Player, shop: PlayerShop, amount: Long) {
        val pair = handleBuyItems(player, shop, amount)
        if (pair.first != PurchaseReason.SUCCESS) {
            player.sendMessage(getFailMessage(pair.first) ?: "null")
        } else {
            player.sendMessage(getBuyMessage(shop, amount))
        }
    }

    private fun handleBuyItems(player: Player, shop: PlayerShop, amount: Long): Pair<PurchaseReason, Long> {
        val useMobCoins = shop.settings.contains(ShopSetting.USE_MOB_COINS)
        val profile = MobCoinsAPI.getProfileManager().getProfile(player)
        val moneyOfUser = if (useMobCoins) profile.mobCoins.toLong() else economy.getBalance(player).toLong()
        val availableSpace = player.inventory.availableSpace(shop.item)

        if (availableSpace == 0) {
            return Pair(PurchaseReason.NO_INVENTORY_SPACE, 0)
        } else if (shop.isEmpty) {
            return Pair(PurchaseReason.SHOP_EMPTY, 0)
        } else if (moneyOfUser < shop.price!!) {
            return Pair(PurchaseReason.INSUFFICIENT_FUNDS, 0)
        }

        val amountToPurchase = min(min(amount, moneyOfUser / shop.price!!).toInt(), availableSpace)
        val clone = shop.item?.clone()
        clone?.amount = amountToPurchase
        shop.item?.amount = shop.item?.amount?.minus(amountToPurchase)!!
        if (shop.settings.contains(ShopSetting.USE_INTERNAL_BANK)) {
            shop.bank!!.giveToAndWithdraw(amountToPurchase * shop.price!!, player.uniqueId)
        } else {
            if (useMobCoins) {
                profile.mobCoins += (amountToPurchase * shop.price!!).toInt()
            } else {
                economy.withdrawPlayer(shop.owner?.toOfflinePlayer(), (amountToPurchase * shop.price!!).toDouble())
                economy.depositPlayer(player, (amountToPurchase * shop.price!!).toDouble())
            }
        }
        shop.update()
        return Pair(PurchaseReason.SUCCESS, amountToPurchase.toLong())

    }

    private fun getBuyMessage(shop: PlayerShop, amount: Long) = Message.getConfigMessage("messages.yml", "BuyMessage")
            ?.applyPlaceholders(
                    amount = amount.format(),
                    thing = shop.itemName,
                    price = (amount * (shop.price ?: 0)).toString(),
                    shopOwner = shop.owner?.toOfflinePlayer()?.name ?: "null"
            ) ?: ""

    private fun getBuyItem(amount: String) = getBuySellItem(Material.LIME_STAINED_GLASS_PANE, "&a&l", "Buy", "from", amount)

    companion object {
        val INVENTORY: SmartInventory = SmartInventory.builder()
                .id(InventoryConstants.PurchaseInventory.ID)
                .title(InventoryConstants.PurchaseInventory.TITLE)
                .size(InventoryConstants.PurchaseInventory.ROWS, InventoryConstants.PurchaseInventory.COLUMNS)
                .closeable(InventoryConstants.PurchaseInventory.CLOSEABLE)
                .provider(BuyInventory())
                .manager(PlayerShops.INSTANCE.inventoryManager)
                .build()
    }
}


