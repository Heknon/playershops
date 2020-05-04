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
import me.oriharel.playershops.utilities.Utils.toTitleCase
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class ShopInitializationInventory(private val playerShops: PlayerShops) : InventoryProvider {

    override fun init(player: Player, contents: InventoryContents) {
        Preconditions.checkState(getShop(contents) is PlayerShop || getShop(contents) == null, "An invalid shop has been passed down!")

        setShopifiedItem(contents, null).setShopType(contents, ShopType.SHOWCASE).setPrice(contents, -1)

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
                                displayName = "&6Place the item to shopify here"
                        )) {
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
        var shop = getShop(contents)!!

        val shopifiedItem = getShopifiedItem(contents)
        val shopType = getShopType(contents)
        val price = getPrice(contents)

        if (shopifiedItem == null) {
            player.sendMessage("§c§l[!] §eYou must enter a item to shopify!")
            return
        }
        if (shop.getType() != shopType) {
            shop = shopManager.shopFactory.convertShop(shop, shopType)
        }

        if (price == -1L && shop is MoneyShop) {
            player.sendMessage("§c§l[!] §eYou must enter a price!")
            return
        }
        if (shop is MoneyShop) {
            shop.price = price
        }

        shop.item = shopifiedItem

        shopManager.setPlayerShopBlockData(shop.block, shop)
        player.sendMessage("§b§l[INFO] §eInitialized shop!")
        INVENTORY.close(player)
    }

    private fun switchShopType(contents: InventoryContents) {
        val currType: ShopType = getShopType(contents)
        val nextType: ShopType = currType.next()
        setShopType(contents, nextType)

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

            setShopifiedItem(contents, cursor)
        }

        e.whoClicked.sendMessage("§c§l[!] §eYou must be holding an item when trying to switch the item shopified!")
    }

    private fun changePrice(contents: InventoryContents, player: Player) {
        val shopType: ShopType = getShopType(contents)
        playerShops.createSignInput(player, "&6Price: &9", "&6Enter " + shopType.toTitleCase() + " price") { p, strings ->
            val price: Long? = strings[0].replace("\\D", "").toLongOrNull()

            // TESTME: Check if exiting the GUI exits the inventory as well
            if (price == null) {
                p.sendMessage("§c&l[!] &eInvalid number!")
                return@createSignInput true
            } else if (price < 0) {
                p.sendMessage("§c&l[!] &ePrice must be a non-negative number!")
                return@createSignInput true
            }

            setPrice(contents, price)
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

    private fun getPrice(contents: InventoryContents): Long {
        return contents.property(InventoryConstants.PRICE_CONTENT_ID)
    }

    private fun setPrice(contents: InventoryContents, price: Long): ShopInitializationInventory {
        contents.property(InventoryConstants.PRICE_CONTENT_ID, price)
        return this
    }

    private fun getShopType(contents: InventoryContents): ShopType {
        return contents.property(InventoryConstants.SHOP_TYPE_CONTENT_ID)
    }

    private fun setShopType(contents: InventoryContents, shopType: ShopType): ShopInitializationInventory {
        contents.setProperty(InventoryConstants.SHOP_TYPE_CONTENT_ID, shopType)
        return this
    }

    private fun getShopifiedItem(contents: InventoryContents): ItemStack? {
        return contents.property(InventoryConstants.SHOPIFIED_ITEM_CONTENT_ID)
    }

    private fun setShopifiedItem(contents: InventoryContents, shopifiedItem: ItemStack?): ShopInitializationInventory {
        contents.setProperty(InventoryConstants.SHOPIFIED_ITEM_CONTENT_ID, shopifiedItem)
        return this
    }

    private fun getShop(contents: InventoryContents): PlayerShop? {
        return contents.property(InventoryConstants.PASSED_DOWN_SHOP_CONTENT_ID)
    }

    companion object {
        val INVENTORY: SmartInventory = SmartInventory.builder()
                .id(InventoryConstants.InitializationInventory.ID)
                .provider(ShopInitializationInventory(PlayerShops.INSTANCE))
                .size(InventoryConstants.InitializationInventory.ROWS, InventoryConstants.InitializationInventory.COLUMNS)
                .title(InventoryConstants.InitializationInventory.TITLE)
                .closeable(InventoryConstants.InitializationInventory.CLOSEABLE)
                .build()
    }
}