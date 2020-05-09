package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import me.oriharel.playershops.PlayerShops
import me.oriharel.playershops.shops.shop.PlayerShop
import me.oriharel.playershops.utilities.KItemStack
import me.oriharel.playershops.utilities.Utils.availableSpace
import me.oriharel.playershops.utilities.Utils.format
import me.oriharel.playershops.utilities.Utils.getItemAmountInInventory
import me.oriharel.playershops.utilities.Utils.giveItem
import me.oriharel.playershops.utilities.Utils.modifyMeta
import me.oriharel.playershops.utilities.Utils.sendMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.min

class StorageInventory : NotUpdatableInventoryProvider {

    override fun init(player: Player, contents: InventoryContents) {
        val shop = InventoryConstants.ConstantUtilities.getShop(contents)!!

        contents.fill(ClickableItem.empty(InventoryConstants.Item.EMPTY_LIGHT_GRAY_STAINED_GLASS_PANE))
        contents.set(1, 4, ClickableItem.empty(getShopifiedItem(shop)))

        contents.set(1, 0, ClickableItem.of(getWithdrawDepositItem("-", "ALL")) {
            deployWithdrawMessage(player, withdrawRoutine(player, shop, shop.item?.amount).toLong(), shop)
            contents.set(1, 4, ClickableItem.empty(getShopifiedItem(shop)))
        })
        contents.set(1, 1, ClickableItem.of(getWithdrawDepositItem("-", "64")) {
            deployWithdrawMessage(player, withdrawRoutine(player, shop, 64).toLong(), shop)
            contents.set(1, 4, ClickableItem.empty(getShopifiedItem(shop)))
        })
        contents.set(1, 2, ClickableItem.of(getWithdrawDepositItem("-", "1")) {
            deployWithdrawMessage(player, withdrawRoutine(player, shop, 1).toLong(), shop)
            contents.set(1, 4, ClickableItem.empty(getShopifiedItem(shop)))
        })

        contents.set(1, 6, ClickableItem.of(getWithdrawDepositItem("+", "1")) {
            val amountDeposited = depositRoutine(player, shop, 1)
            sendDepositMessage(player, amountDeposited.toLong(), shop)
            contents.set(1, 4, ClickableItem.empty(getShopifiedItem(shop)))
        })
        contents.set(1, 7, ClickableItem.of(getWithdrawDepositItem("+", "64")) {
            sendDepositMessage(player, depositRoutine(player, shop, 64).toLong(), shop)
            contents.set(1, 4, ClickableItem.empty(getShopifiedItem(shop)))
        })
        contents.set(1, 8, ClickableItem.of(getWithdrawDepositItem("+", "ALL")) {
            val amount = it.view.bottomInventory.getItemAmountInInventory(shop.item).toLong()
            sendDepositMessage(player, depositRoutine(player, shop, amount).toLong(), shop)
            contents.set(1, 4, ClickableItem.empty(getShopifiedItem(shop)))
        })

    }

    private fun depositRoutine(player: Player, shop: PlayerShop, amount: Long): Int {
        var amountRemainingToFetch = amount
        var amountDeposited = 0

        player.inventory.contents.forEach {
            if (it?.isSimilar(shop.item) == true) {
                val amountToDeposit: Int = min(it.amount, amountRemainingToFetch.toInt()) // not to take above the amount the item has

                it.amount -= amountToDeposit
                shop.item!!.amount += amountToDeposit
                amountDeposited += amountToDeposit

                amountRemainingToFetch -= amountToDeposit

                if (amountRemainingToFetch <= 0) {
                    if (amountDeposited != 0) shop.update()
                    return amountDeposited
                }
            }
        }
        if (amountDeposited != 0) shop.update()
        return amountDeposited
    }

    /**
     * @return returns amount withdrawn
     */
    private fun withdrawRoutine(player: Player, shop: PlayerShop, amount: Int?): Int {
        if (amount == null) return -1

        val amountToWithdraw = min(min(amount, shop.amountInStock.toInt()), player.inventory.availableSpace(shop.item))
        val cloneToGive = shop.item?.clone()

        shop.item!!.amount -= amountToWithdraw
        cloneToGive?.amount = amountToWithdraw

        if (cloneToGive != null) player.giveItem(cloneToGive)
        if (amountToWithdraw != 0) shop.update()

        return amountToWithdraw
    }

    private fun getShopifiedItem(shop: PlayerShop): ItemStack? {
        val clone = shop.item!!.clone()
        clone.amount = 1
        return clone.modifyMeta {
            var lore = it.lore
            if (lore == null) {
                it.lore = mutableListOf()
                lore = it.lore!!
            }
            lore.add("&e&l&o&m-----")
            lore.add("&6&l* &eQuantity: &fx${shop.amountInStock} / x${shop.storageSize.format()}")
            it.lore = lore
        }
    }

    private fun sendDepositMessage(player: Player, amount: Long?, shop: PlayerShop?) = player.sendMessage("messages.yml", "DepositToStorage", amount = amount?.format()
            ?: "0", thing = shop?.itemName ?: "n/a")

    private fun deployWithdrawMessage(player: Player, amount: Long?, shop: PlayerShop?) = player.sendMessage("messages.yml", "WithdrawFromStorage", amount = amount?.format()
            ?: "0", thing = shop?.itemName ?: "n/a")


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
