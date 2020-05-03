package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import fr.minuskube.inv.content.InventoryProvider
import me.oriharel.playershops.PlayerShops
import me.oriharel.playershops.shops.shop.MoneyShop
import me.oriharel.playershops.shops.shop.ShopType
import me.oriharel.playershops.utilities.KItemStack
import me.oriharel.playershops.utilities.Utils.format
import me.oriharel.playershops.utilities.Utils.ifOnline
import me.oriharel.playershops.utilities.Utils.toOfflinePlayer
import me.oriharel.playershops.utilities.Utils.toTitleCase
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player

class PurchaseInventory(private val playerShops: PlayerShops) : InventoryProvider {

    lateinit var block: Block
    lateinit var shop: MoneyShop

    override fun init(player: Player, contents: InventoryContents) {
        shop = contents.property("shop")
        block = contents.property("shopBlock")

        contents.fill(ClickableItem.empty(KItemStack(material = Material.GRAY_STAINED_GLASS_PANE, displayName = "")))
        contents.set(0, 4, ClickableItem.of(KItemStack(
                material = Material.LIME_STAINED_GLASS_PANE,
                displayName = "&a&l" + shop.getType().name,
                lore = listOf("&2${shop.getType().name}: &6${shop.price.format()}")
        )) {
            playerShops.createSignInput(player, "&6Amount: &9", "&6&l${shop.getType()}") { p, strings ->
                val amount: Int? = strings[0].replace("\\D", "").toIntOrNull()
                // TESTME: Check if exiting the GUI exits the inventory as well
                when {
                    amount == null -> {
                        p.sendMessage("§c&l[!] &eInvalid number!")
                        return@createSignInput true
                    }
                    amount < 0 -> {
                        p.sendMessage("§c&l[!] &ePrice must be a non-negative number!")
                        return@createSignInput true
                    }
                    amount > shop.item.amount -> {
                        p.sendMessage("§c&l[!] &eThe owner does not have that amount in stock! Amount if stock - ${shop.item.amount}")
                        return@createSignInput true
                    }
                    else -> {
                        SmartInventory.builder()
                                .closeable(false)
                                .title("Confirm")
                                .size(1, 9)
                                .parent(inventory)
                                .provider(ConfirmationInventory(
                                        deny = KItemStack(Material.RED_STAINED_GLASS_PANE, displayName = "&c&lDENY"),
                                        confirm = KItemStack(Material.LIME_STAINED_GLASS, displayName = "&a&lCONFIRM")
                                ) { it ->
                                    if (it == ConfirmationType.CONFIRM) {
                                        inventory.close(player)
                                        if (shop.getType() == ShopType.BUY) {
                                            shop.owner.toOfflinePlayer().ifOnline {
                                                it.sendMessage("§b§l[INFO] §e${player.name} has just bought $amount of ${shop.item.type.toTitleCase()} from your shop!")
                                            }
                                            player.sendMessage("§b§l[INFO] §eYou have successfully sold $amount of ${shop.item.type.toTitleCase()}!")
                                        } else if (shop.getType() == ShopType.SELL) {
                                            shop.owner.toOfflinePlayer().ifOnline {
                                                it.sendMessage("§b§l[INFO] §e${player.name} has just sold $amount of ${shop.item.type.toTitleCase()} to you!")
                                            }
                                            player.sendMessage("§b§l[INFO] §eYou've successfully bought $amount!")
                                        }
                                        shop.run(amount, player)
                                    } else {
                                        inventory.close(player)
                                        player.sendMessage("§c§l[!] §eCancelled transaction!")
                                    }
                                }).build().open(player)

                        return@createSignInput true
                    }
                }
            }
        })
        contents.set(0, 8, ClickableItem.of(KItemStack(material = Material.RED_STAINED_GLASS_PANE, displayName = "&c&lCANCEL TRANSACTION")) {
            inventory.close(it.whoClicked as Player)
            it.whoClicked.sendMessage("§c§l[!] §eCancelled transaction!")
        })
    }

    override fun update(player: Player, contents: InventoryContents) {

    }

    companion object {
        val inventory: SmartInventory = SmartInventory.builder()
                .id("purchaseShopInventory")
                .title("${ChatColor.BLUE}SHOP")
                .size(1, 9)
                .provider(PurchaseInventory(PlayerShops.instance))
                .build()
    }

}