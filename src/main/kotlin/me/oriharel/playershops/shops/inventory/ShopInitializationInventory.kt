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
import me.oriharel.playershops.utilities.Utils.toTitleCase
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class ShopInitializationInventory(private val playerShops: PlayerShops) : InventoryProvider {

    private var cachedPrice: Long? = null
    private var cachedType: ShopType? = null
    private var cachedItemStack: ItemStack? = null

    override fun init(player: Player, contents: InventoryContents) {
        contents.fill(ClickableItem.empty(KItemStack(material = Material.GRAY_STAINED_GLASS_PANE, displayName = "")))

        contents.setProperty("shopifiedItem", null).setProperty("shopType", ShopType.SHOWCASE).setProperty("prevShopType", ShopType.SHOWCASE).setProperty("itemPricePer", -1)

        contents.set(0, 2, ClickableItem.of(getShopTypeSwitcherItem()) {
            switchShopType(contents)
        })

        contents.set(0, 4, ClickableItem.of(KItemStack(
                material = Material.CYAN_STAINED_GLASS_PANE,
                displayName = "&6Place the item to shopify here"
        )) {
            switchItemShopified(it, contents)
        })

        contents.set(0, 6, ClickableItem.of(KItemStack(
                material = Material.PURPLE_STAINED_GLASS_PANE,
                displayName = "&6Place the item to shopify here"
        )) {
            val shopType: ShopType = contents.property("shopType")
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

                contents.property("itemPricePer", price)
                cachedPrice = price
                return@createSignInput true
            }
        })

        contents.set(
                2,
                4,
                ClickableItem.of(KItemStack(
                        material = Material.ORANGE_STAINED_GLASS_PANE,
                        displayName = "&6DONE",
                        lore = listOf("&6These settings are changeable")
                )) {
                    val shopManager = PlayerShops.instance.shopManager
                    val block: Block = contents.property("shopBlock")
                    var shop: PlayerShop = PlayerShops.instance.shopManager.getPlayerShopFromBlock(block)
                            ?: return@of
                    if (cachedItemStack == null) {
                        player.sendMessage("§c§l[!] §eYou must enter a item to shopify!")
                        return@of
                    }
                    if (cachedType != null && shop.getType() != cachedType) {
                        shop = shopManager.shopFactory.convertShop(shop, cachedType!!)
                    }

                    if (cachedPrice == null && shop is MoneyShop) {
                        player.sendMessage("§c§l[!] §eYou must enter a price!")
                        return@of
                    }
                    if (shop is MoneyShop) {
                        shop.price = cachedPrice!!
                    }

                    shop.item = cachedItemStack!!

                    shopManager.setPlayerShopBlockData(block, shop)
                    player.sendMessage("§b§l[INFO] §eInitialized shop!")
                    inventory.close(player)
                }
        )
    }

    override fun update(player: Player, contents: InventoryContents) {
        val prevShopType: ShopType = contents.property("prevShopType")
        val currShopType: ShopType = contents.property("shopType")

        if (prevShopType != currShopType) {
            contents.set(0, 2, ClickableItem.of(getShopTypeSwitcherItem(currShopType)) {
                switchShopType(contents)
            })
        }
    }

    private fun switchShopType(contents: InventoryContents) {
        val currType: ShopType = contents.property("shopType")
        val nextType: ShopType = currType.next()
        contents.property("prevShopType", currType)
        contents.property("shopType", nextType)
        cachedType = nextType
    }

    private fun switchItemShopified(e: InventoryClickEvent, contents: InventoryContents) {
        val cursor = e.cursor

        if (cursor != null && cursor.type != Material.AIR) {
            contents.set(0, 4, ClickableItem.of(cursor) {
                switchItemShopified(e, contents)
            })
            contents.property("shopifiedItem", cursor)
            cachedItemStack = cursor
        }
        e.whoClicked.sendMessage("§c§l[!] §eYou must be holding an item when trying to switch the item shopified!")
    }

    private fun getShopTypeSwitcherItem(shopType: ShopType? = null): ItemStack {
        return KItemStack(
                material = Material.LIME_STAINED_GLASS_PANE,
                displayName = "&6" + if (shopType == null) ShopType.SHOWCASE.toTitleCase() else shopType.toTitleCase(),
                lore = listOf("&bClick to alternate between shop types")
        )
    }

    companion object {
        val inventory: SmartInventory = SmartInventory.builder()
                .id("shopInitializerInventory")
                .provider(ShopInitializationInventory(PlayerShops.instance))
                .size(3, 9)
                .title("${ChatColor.BLUE}Setup your shop!")
                .closeable(false)
                .build()
    }
}