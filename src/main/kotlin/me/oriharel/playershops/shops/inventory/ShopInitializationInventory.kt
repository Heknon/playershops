package me.oriharel.playershops.shops.inventory

import com.google.common.base.Preconditions
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
import me.oriharel.playershops.utilities.Utils.toTitleCase
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class ShopInitializationInventory(private val playerShops: PlayerShops) : InventoryProvider {

    override fun init(player: Player, contents: InventoryContents) {
        val shop: PlayerShop? = InventoryConstants.ConstantUtilities.getShop(contents)

        Preconditions.checkState(shop is PlayerShop || shop == null, "An invalid shop has been passed down!")

        InventoryConstants.ConstantUtilities.setShopifiedItem(contents, shop?.item)
                .setSelectedShopType(contents, shop?.getType() ?: ShopType.SHOWCASE)
                .setPrice(contents, if (shop is MoneyShop) shop.price ?: -1 else -1)

        contents.fill(
                ClickableItem.empty(
                        KItemStack(
                                material = Material.GRAY_STAINED_GLASS_PANE,
                                displayName = ""
                        )))

        contents.set(0, 2,
                ClickableItem.of(
                        getShopTypeSwitcherItem()) {
                    switchShopType(contents)
                })

        contents.set(0, 4,
                ClickableItem.of(
                        KItemStack(
                                material = Material.CYAN_STAINED_GLASS_PANE,
                                displayName = "&6Place the item to shopify here"
                        )) {
                    switchItemShopified(it, contents)
                })

        contents.set(0, 6,
                ClickableItem.of(
                        KItemStack(
                                material = Material.PURPLE_STAINED_GLASS_PANE,
                                displayName = if (InventoryConstants.ConstantUtilities.getPrice(contents) == -1L) "&9Set the cost of the item!"
                                else "&6Cost: &e${InventoryConstants.ConstantUtilities.getPrice(contents).format()}" +
                                        if (InventoryConstants.ConstantUtilities.getUseMobCoins(contents)!!) " Zen Coins"
                                        else " Money"
                        )) {
                    val type = InventoryConstants.ConstantUtilities.getSelectedShopType(contents)
                    if (type == ShopType.SHOWCASE) {
                        player.sendMessage("§c§l[!] §eYou must set the shop type to other than showcase to set a price!")
                        return@of
                    }
                    changePrice(contents, player)
                })

        contents.set(2, 4,
                ClickableItem.of(
                        KItemStack(
                                material = Material.ORANGE_STAINED_GLASS_PANE,
                                displayName = "&6DONE",
                                lore = listOf("&6These settings are changeable")
                        )) {
                    applyChanges(player, contents)
                })
    }

    override fun update(p: Player, c: InventoryContents) {

    }


    private fun applyChanges(player: Player, contents: InventoryContents) {
        val shopManager = PlayerShops.INSTANCE.shopManager
        var shop = InventoryConstants.ConstantUtilities.getShop(contents)!!

        val shopifiedItem = InventoryConstants.ConstantUtilities.getShopifiedItem(contents)
        val shopType = InventoryConstants.ConstantUtilities.getSelectedShopType(contents)
        val price = InventoryConstants.ConstantUtilities.getPrice(contents)

        if (shopifiedItem == null) {
            player.sendMessage("§c§l[!] §eYou must enter a item to shopify!")
            return
        }
        if (shop.getType() != shopType) {
            shop = shopManager.shopFactory.convertShop(shop, shopType!!)
        }

        if (price == -1L && shop is MoneyShop) {
            player.sendMessage("§c§l[!] §eYou must enter a price!")
            return
        }
        if (shop is MoneyShop) {
            shop.price = price
        }

        shop.item = shopifiedItem

        shopManager.setPlayerShopBlockState(shop.block!!, shop)
        player.sendMessage("§b§l[INFO] §eInitialized shop!")
        INVENTORY.close(player)
    }

    private fun switchShopType(contents: InventoryContents) {
        val currType: ShopType? = InventoryConstants.ConstantUtilities.getSelectedShopType(contents)
        val nextType: ShopType? = currType?.next()
        InventoryConstants.ConstantUtilities.setSelectedShopType(contents, nextType)

        contents.set(0, 2, ClickableItem.of(getShopTypeSwitcherItem(nextType)) {
            switchShopType(contents)
        })
    }

    private fun switchItemShopified(e: InventoryClickEvent, contents: InventoryContents) {
        val cursor = e.cursor
        val clone = cursor?.clone()

        if (cursor != null && cursor.type != Material.AIR) {
            cursor.amount -= 1
            e.isCancelled = true
            contents.set(0, 4, ClickableItem.of(clone) {
                switchItemShopified(e, contents)
            })

            InventoryConstants.ConstantUtilities.setShopifiedItem(contents, clone)
            return
        }

        e.whoClicked.sendMessage("§c§l[!] §eYou must be holding an item when trying to switch the item shopified!")
    }

    private fun changePrice(contents: InventoryContents, player: Player) {
        val shopType: ShopType? = InventoryConstants.ConstantUtilities.getSelectedShopType(contents)
        playerShops.createSignInput(player, "&6Price: &9", "&6Enter " + shopType?.toTitleCase() + " price", "", "") { p, strings ->
            print(strings[0].replace("\\D".toRegex(), "") + "           dfsads")
            val price: Long? = strings[0].replace("\\D".toRegex(), "").toLongOrNull()

            if (price == null) {
                p.sendMessage("§c&l[!] &eInvalid number!")
                INVENTORY.open(player)
                return@createSignInput true
            } else if (price < 0) {
                INVENTORY.open(player)
                p.sendMessage("§c&l[!] &ePrice must be a non-negative number!")
                return@createSignInput true
            }

            InventoryConstants.ConstantUtilities.setPrice(contents, price)
            return@createSignInput true
        }
    }

    private fun getShopTypeSwitcherItem(shopType: ShopType? = null): ItemStack {
        return KItemStack(
                material = Material.LIME_STAINED_GLASS_PANE,
                displayName = "&6" + if (shopType == null) ShopType.SHOWCASE.toTitleCase() else shopType.toTitleCase(),
                lore = listOf("&bClick to alternate between shop types")
        )
    }

    companion object {
        val INVENTORY: SmartInventory = SmartInventory.builder()
                .id(InventoryConstants.InitializationInventory.ID)
                .provider(ShopInitializationInventory(PlayerShops.INSTANCE))
                .size(InventoryConstants.InitializationInventory.ROWS, InventoryConstants.InitializationInventory.COLUMNS)
                .title(InventoryConstants.InitializationInventory.TITLE)
                .closeable(InventoryConstants.InitializationInventory.CLOSEABLE)
                .manager(PlayerShops.INSTANCE.inventoryManager)
                .build()
    }
}