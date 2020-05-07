package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.InventoryListener
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import me.oriharel.playershops.PlayerShops
import me.oriharel.playershops.shops.shop.PlayerShop
import me.oriharel.playershops.utilities.KItemStack
import me.oriharel.playershops.utilities.Utils.format
import me.oriharel.playershops.utilities.Utils.getItemAmountInInventory
import me.oriharel.playershops.utilities.Utils.giveItem
import me.oriharel.playershops.utilities.Utils.modifyMeta
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.min

class StorageInventory : NotUpdatableInventoryProvider {

    override fun init(player: Player, contents: InventoryContents) {
        val shop = InventoryConstants.ConstantUtilities.getShop(contents)

        contents.fill(ClickableItem.empty(InventoryConstants.Item.EMPTY_LIGHT_GRAY_STAINED_GLASS_PANE))
        contents.set(1, 4, ClickableItem.empty(getShopifiedItem(contents)))

        contents.set(1, 0, ClickableItem.of(getWithdrawDepositItem("-", "ALL")) {
            withdrawRoutine(player, shop, shop?.item?.amount)
            deployWithdrawMessage(player, shop?.item?.amount?.toLong(), shop)
        })
        contents.set(1, 1, ClickableItem.of(getWithdrawDepositItem("-", "64")) {
            withdrawRoutine(player, shop, 64)
            deployWithdrawMessage(player, 64, shop)
        })
        contents.set(1, 2, ClickableItem.of(getWithdrawDepositItem("-", "1")) {
            withdrawRoutine(player, shop, 1)
            deployWithdrawMessage(player, 1, shop)
        })

        contents.set(1, 6, ClickableItem.of(getWithdrawDepositItem("+", "1")) {
            depositRoutine(player, shop, 1)
            deployDepositMessage(player, 1, shop)
        })
        contents.set(1, 7, ClickableItem.of(getWithdrawDepositItem("+", "64")) {
            depositRoutine(player, shop, 64)
            deployDepositMessage(player, 64, shop)

        })
        contents.set(1, 8, ClickableItem.of(getWithdrawDepositItem("+", "ALL")) {
            val amount = it.inventory.getItemAmountInInventory(shop?.item).toLong()
            depositRoutine(player, shop, amount)
            deployDepositMessage(player, amount, shop)
        })

    }

    private fun depositRoutine(player: Player, shop: PlayerShop?, amount: Long) {
        var amountRemainingToFetch = amount

        player.inventory.contents.forEach {
            if (it.isSimilar(shop?.item)) {
                val amountToFetch: Int = min(it.amount, amountRemainingToFetch.toInt()) // not to take above the amount the item has

                it.amount -= amountToFetch
                shop?.item?.amount = shop?.item?.amount?.plus(amountToFetch) ?: 0

                amountRemainingToFetch -= amountToFetch

                if (amountRemainingToFetch <= 0) {
                    shop?.update()
                    return
                }
            }
        }
        shop?.update()
    }

    /**
     * @return returns true if the withdraw was successful and the item was given. returns false if shop doesn't have enough to give
     */
    private fun withdrawRoutine(player: Player, shop: PlayerShop?, amount: Int?): Boolean {
        if (amount == null) return false
        if (shop?.item?.amount ?: amount < amount) {
            return false
        }

        val cloneToGive = shop?.item?.clone()
        shop?.item?.amount = shop?.item?.amount?.minus(amount) ?: 0
        cloneToGive?.amount = amount
        if (cloneToGive != null) player.giveItem(cloneToGive)
        shop?.update()
        return true
    }

    private fun getShopifiedItem(contents: InventoryContents): ItemStack? {
        val shopifiedItem = InventoryConstants.ConstantUtilities.getShopifiedItem(contents)
        val shop = InventoryConstants.ConstantUtilities.getShop(contents)

        return shopifiedItem?.modifyMeta {
            if (it.lore == null) it.lore = mutableListOf()
            it.lore!!.add("&e&l&o&m-----")
            it.lore!!.add("&6&l* &eQuantity: &fx${shop?.item?.amount?.minus(1)?.format() ?: 0} / x${shop?.storageSize?.format() ?: 0}")
        }
    }

    private fun deployDepositMessage(player: Player, amount: Long?, shop: PlayerShop?) {
        deployStorageMessage(player, "deposited", amount, shop)
    }

    private fun deployWithdrawMessage(player: Player, amount: Long?, shop: PlayerShop?) {
        deployStorageMessage(player, "withdrew", amount, shop)
    }

    private fun deployStorageMessage(player: Player, holder: String, amount: Long?, shop: PlayerShop?) {
        player.sendMessage("§6§l[!] &r&eYou $holder &fx${amount?.format()} §7${shop?.item?.itemMeta?.displayName ?: "n/a"}§f.")
    }

    private fun getWithdrawDepositItem(sign: String, amount: String): ItemStack {
        val color = if (sign == "-") "&c&l" else "&a&l"
        val amountDisplayName = if (sign == "-" && amount == "ALL") "Take All" else if (amount == "ALL") "Deposit All" else amount
        return KItemStack(
                material = if (sign == "-") Material.RED_STAINED_GLASS_PANE else Material.LIME_STAINED_GLASS_PANE,
                displayName = "$color$sign$amountDisplayName",
                lore = listOf("&7&o(( Clicking this will deposit &b&n$amount &r&7to the stack))")
        )
    }

    companion object {
        val INVENTORY: SmartInventory = SmartInventory.builder()
                .id(InventoryConstants.StorageInventory.ID)
                .provider(StorageInventory())
                .size(InventoryConstants.StorageInventory.ROWS, InventoryConstants.StorageInventory.COLUMNS)
                .title(InventoryConstants.StorageInventory.TITLE)
                .closeable(InventoryConstants.StorageInventory.CLOSEABLE)
                .manager(PlayerShops.INSTANCE.inventoryManager)
                .build()
    }
}
