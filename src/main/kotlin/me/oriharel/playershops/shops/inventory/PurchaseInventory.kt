package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import fr.minuskube.inv.content.InventoryProvider
import me.oriharel.playershops.PlayerShops
import me.oriharel.playershops.shops.shop.MoneyShop
import me.oriharel.playershops.shops.shop.PlayerShop
import me.oriharel.playershops.shops.shop.ShopType
import me.oriharel.playershops.utilities.KItemStack
import me.oriharel.playershops.utilities.Utils.format
import me.oriharel.playershops.utilities.Utils.ifOnline
import me.oriharel.playershops.utilities.Utils.toOfflinePlayer
import me.oriharel.playershops.utilities.Utils.toTitleCase
import org.bukkit.Material
import org.bukkit.entity.Player

class PurchaseInventory(private val playerShops: PlayerShops) : InventoryProvider {

    override fun init(player: Player, contents: InventoryContents) {
        val shop = getShop(contents) as MoneyShop

        contents.fill(
                ClickableItem.empty(
                        KItemStack(
                                material = Material.GRAY_STAINED_GLASS_PANE,
                                displayName = ""
                        )))

        contents.set(0, 4,
                ClickableItem.of(
                        KItemStack(
                                material = Material.LIME_STAINED_GLASS_PANE,
                                displayName = "&a&l" + shop.getType().name,
                                lore = listOf("&2${shop.getType().name}: &6${shop.price?.format()}")
                        )) {
                    playerShops.createSignInput(player, "&6Amount: &9", "&6&l${shop.getType()}") { _, strings ->
                        val amount: Int? = strings[0].replace("\\D", "").toIntOrNull()
                        // TESTME: Check if exiting the GUI exits the inventory as well
                        handleAmountToPurchaseInput(contents, player, amount)
                    }
                })

        contents.set(0, 8,
                ClickableItem.of(
                        KItemStack(
                                material = Material.RED_STAINED_GLASS_PANE,
                                displayName = "&c&lCANCEL TRANSACTION"
                        )) {
                    INVENTORY.close(it.whoClicked as Player)
                    it.whoClicked.sendMessage("§c§l[!] §eCancelled transaction!")
                })
    }

    override fun update(player: Player, contents: InventoryContents) {

    }

    private fun handleAmountToPurchaseInput(contents: InventoryContents, player: Player, amount: Int?): Boolean {
        val shop = getShop(contents)

        // TESTME: Check if exiting the GUI exits the inventory as well
        when {
            amount == null -> {
                player.sendMessage("§c&l[!] &eInvalid number!")
                return true
            }
            amount < 0 -> {
                player.sendMessage("§c&l[!] &ePrice must be a non-negative number!")
                return true
            }
            amount > shop.item?.amount!! -> {
                player.sendMessage("§c&l[!] &eThe owner does not have that amount in stock! Amount if stock - ${shop.item!!.amount}")
                return true
            }
            else -> {
                openConfirmationInventory(contents, player, amount)
                return true
            }
        }
    }

    private fun openConfirmationInventory(contents: InventoryContents, player: Player, amount: Int) {
        val shop = getShop(contents)
        val shopType = shop.getType()
        val owner = shop.owner?.toOfflinePlayer()
        val inventory = ConfirmationInventory.INVENTORY

        inventory.open(player)
        inventory.manager.getContents(player).get().setProperty(InventoryConstants.ON_CLICK_CONTENT_ID) { it: ConfirmationType ->
            if (it == ConfirmationType.CONFIRM) {
                INVENTORY.close(player)
                if (shopType == ShopType.BUY) {
                    owner?.ifOnline {
                        it.sendMessage("§b§l[INFO] §e${player.name} has just bought $amount of ${shop.item!!.type.toTitleCase()} from your shop!")
                    }
                    player.sendMessage("§b§l[INFO] §eYou have successfully sold $amount of ${shop.item!!.type.toTitleCase()}!")
                } else if (shopType == ShopType.SELL) {
                    owner?.ifOnline {
                        it.sendMessage("§b§l[INFO] §e${player.name} has just sold $amount of ${shop.item!!.type.toTitleCase()} to you!")
                    }
                    player.sendMessage("§b§l[INFO] §eYou've successfully bought $amount!")
                }
                shop.run(amount, player)
            } else {
                INVENTORY.close(player)
                player.sendMessage("§c§l[!] §eCancelled transaction!")
            }
        }

    }

    private fun getShop(contents: InventoryContents): PlayerShop {
        return contents.property(InventoryConstants.PASSED_DOWN_SHOP_CONTENT_ID)
    }

    companion object {
        val INVENTORY: SmartInventory = SmartInventory.builder()
                .id(InventoryConstants.PurchaseInventory.ID)
                .title(InventoryConstants.PurchaseInventory.TITLE)
                .size(InventoryConstants.PurchaseInventory.ROWS, InventoryConstants.PurchaseInventory.COLUMNS)
                .closeable(InventoryConstants.PurchaseInventory.CLOSEABLE)
                .provider(PurchaseInventory(PlayerShops.INSTANCE))
                .build()
    }

}