package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import me.oriharel.playershops.PlayerShops
import me.oriharel.playershops.shops.shop.PlayerShop
import me.oriharel.playershops.shops.shop.ShopSetting
import me.oriharel.playershops.utilities.KItemStack
import me.oriharel.playershops.utilities.Utils.format
import me.oriharel.playershops.utilities.Utils.modifyMeta
import me.oriharel.playershops.utilities.Utils.toOfflinePlayer
import net.milkbowl.vault.economy.Economy
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

open class PurchaseInventory(protected val economy: Economy) : NotUpdatableInventoryProvider {

    override fun init(player: Player, contents: InventoryContents) {
        contents.fill(ClickableItem.empty(InventoryConstants.Item.EMPTY_GRAY_STAINED_GLASS_PANE))
        contents.fillRect(0, 3, 2, 5, ClickableItem.empty(InventoryConstants.Item.EMPTY_YELLOW_STAINED_GLASS_PANE))
        contents.fillRow(3, ClickableItem.empty(InventoryConstants.Item.EMPTY_WHITE_STAINED_GLASS_PANE))

        contents.set(1, 4, ClickableItem.empty(getItemSoldBought(InventoryConstants.ConstantUtilities.getShop(contents))))
    }


    protected fun getItemSoldBought(shop: PlayerShop?): ItemStack {
        val clone = shop?.item?.clone()
        val useMobCoins = shop?.settings?.contains(ShopSetting.USE_MOB_COINS) ?: false
        val priceText = (if (!useMobCoins) "$" else "") + (shop?.price?.format()
                ?: "n/a") + (if (useMobCoins) " Zen Coins" else "")
        clone?.amount = 1

        return clone?.modifyMeta {
            if (it.lore == null) it.lore = mutableListOf()
            it.lore!!.add("&e&l&o&m-----")
            it.lore!!.add("&6&l* &e&lQuantity: &fx${shop.item?.amount?.minus(1)?.format() ?: 0} / x${shop.storageSize.format()}")
            it.lore!!.add("&6&l* &e&lPrice: &a$priceText")
        } ?: KItemStack(
                material = Material.BARRIER,
                displayName = "&cAbsolutely nothing!"
        )
    }

    protected fun getPurchaseMessage(shop: PlayerShop, prefixAmount: String, amount: Long, cost: Long, suffixCost: String): String {
        val useMobCoins = shop.settings.contains(ShopSetting.USE_MOB_COINS)
        val costStr = (if (!useMobCoins) "$" else "") + (cost.format()) + (if (useMobCoins) " Zen Coins" else "")
        return "§6§l[!] §ePlayer Shop $prefixAmount §f${amount.format()} §7${shop.item?.itemMeta?.displayName ?: "n/a"}§e for §a$costStr §e$suffixCost ${shop.owner?.toOfflinePlayer()?.name}"
    }

    protected fun getFailMessage(purchaseReason: PurchaseReason): String {
        return when (purchaseReason) {
            PurchaseReason.NO_INVENTORY_SPACE -> "§4§l[!] §cYou do not have any free inventory space."
            PurchaseReason.INSUFFICIENT_FUNDS -> "§4§l[!] §cInsufficient funds."
            PurchaseReason.SHOP_EMPTY -> "§4§l[!] §cPlayer Shop is empty."
            PurchaseReason.NO_SHOP_SPACE -> "§4§l[!] §cPlayer Shop is full."
            PurchaseReason.SHOP_INSUFFICIENT_FUNDS -> "§4§l[!] §cShop is missing funds."
            else -> throw NotImplementedError("Invalid PurchaseReason")
        }
    }

    protected fun getBuySellItem(material: Material, prefix: String, text: String, suffix: String, amount: String): ItemStack {
        return KItemStack(
                material = material,
                displayName = "$prefix${text} $amount",
                lore = listOf("&7&o(( Clicking this will buy &b&o&m$amount &7&o$suffix the shop ))")
        )
    }

    companion object {
        val INVENTORY: SmartInventory = SmartInventory.builder()
                .id(InventoryConstants.PurchaseInventory.ID)
                .title(InventoryConstants.PurchaseInventory.TITLE)
                .size(InventoryConstants.PurchaseInventory.ROWS, InventoryConstants.PurchaseInventory.COLUMNS)
                .closeable(InventoryConstants.PurchaseInventory.CLOSEABLE)
                .provider(PurchaseInventory(PlayerShops.INSTANCE.economy))
                .manager(PlayerShops.INSTANCE.inventoryManager)
                .build()
    }

}