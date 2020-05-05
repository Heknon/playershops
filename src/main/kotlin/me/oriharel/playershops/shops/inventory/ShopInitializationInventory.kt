package me.oriharel.playershops.shops.inventory

import com.google.common.base.Preconditions
import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import fr.minuskube.inv.content.InventoryProvider
import me.oriharel.playershops.PlayerShops
import me.oriharel.playershops.shops.bank.VaultShopBank
import me.oriharel.playershops.shops.bank.ZenCoinShopBank
import me.oriharel.playershops.shops.shop.MoneyShop
import me.oriharel.playershops.shops.shop.PlayerShop
import me.oriharel.playershops.shops.shop.ShopSetting
import me.oriharel.playershops.shops.shop.ShopType
import me.oriharel.playershops.utilities.KItemStack
import me.oriharel.playershops.utilities.Utils.format
import me.oriharel.playershops.utilities.Utils.getNBTClone
import me.oriharel.playershops.utilities.Utils.giveItem
import me.oriharel.playershops.utilities.Utils.openWithContents
import me.oriharel.playershops.utilities.Utils.toTitleCase
import net.minecraft.server.v1_15_R1.NBTTagByte
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class ShopInitializationInventory(private val playerShops: PlayerShops) : InventoryProvider {

    override fun init(player: Player, contents: InventoryContents) {
        Bukkit.getScheduler().runTaskLater(playerShops, Runnable {
            run {
                val shop: PlayerShop? = InventoryConstants.ConstantUtilities.getShop(contents)

                Preconditions.checkState(shop is PlayerShop || shop == null, "An invalid shop has been passed down!")

                print(shop?.getType())

                InventoryConstants.ConstantUtilities.run {
                    val prevPrice = getPrice(contents)

                    print(getShopifiedItem(contents))
                    setShopifiedItem(contents, getShopifiedItem(contents) ?: shop?.item)
                    setSelectedShopType(contents, getSelectedShopType(contents) ?: shop?.getType() ?: ShopType.SHOWCASE)
                    setPrice(contents, prevPrice ?: if (shop is MoneyShop) shop.price ?: -1 else -1)
                    setUseMobCoins(contents, getUseMobCoins(contents)
                            ?: shop?.settings?.contains(ShopSetting.USE_MOB_COINS) ?: false)


                    contents.fill(
                            ClickableItem.empty(
                                    KItemStack(
                                            material = Material.GRAY_STAINED_GLASS_PANE,
                                            displayName = ""
                                    )))

                    contents.set(0, 2,
                            ClickableItem.of(
                                    getShopTypeSwitcherItem(contents)) {
                                switchShopType(contents)
                            })

                    buildAndSetDefaultShopifiedItem(contents)

                    contents.set(0, 6,
                            ClickableItem.of(
                                    KItemStack(
                                            material = if (getPrice(contents) != -1L) Material.EMERALD_BLOCK else Material.PURPLE_STAINED_GLASS_PANE,
                                            displayName = if (getPrice(contents) == -1L) "&9Set the cost of the item!"
                                            else "&6Cost: &e${getPrice(contents)?.format()}" +
                                                    if (getUseMobCoins(contents)!!) " Zen Coins"
                                                    else " Money"
                                    )) {
                                val type = getSelectedShopType(contents)
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
            }
        }, 1)
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
            shop = shopManager.shopFactory.convertShop(shop, shopType!!, VaultShopBank(0, playerShops.economy))
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

    private fun buildAndSetDefaultShopifiedItem(contents: InventoryContents) {
        contents.set(0, 4,
                ClickableItem.of(
                        InventoryConstants.ConstantUtilities.getShopifiedItem(contents)
                                ?: InventoryConstants.ConstantUtilities.getShop(contents)?.item ?: KItemStack(
                                        material = Material.CYAN_STAINED_GLASS_PANE,
                                        displayName = "&6Place the item to shopify here",
                                        nbtModifier = {
                                            it["default"] = NBTTagByte.a(true)
                                        }
                                )) {
                    switchItemShopified(it, contents)
                })
    }

    private fun switchShopType(contents: InventoryContents) {
        val currType: ShopType? = InventoryConstants.ConstantUtilities.getSelectedShopType(contents)
        val nextType: ShopType? = currType?.next()
        InventoryConstants.ConstantUtilities.setSelectedShopType(contents, nextType)

        contents.set(0, 2, ClickableItem.of(getShopTypeSwitcherItem(contents)) {
            switchShopType(contents)
        })
    }

    private fun switchItemShopified(e: InventoryClickEvent, contents: InventoryContents) {
        val cursor = e.cursor
        val clone = cursor?.clone()
        val itemIn = InventoryConstants.ConstantUtilities.getShopifiedItem(contents)

        clone?.amount = 1

        if (cursor != null && cursor.type != Material.AIR) {
            cursor.amount -= 1
            if (itemIn != null) {
                (e.whoClicked as Player).giveItem(itemIn)
            }
            contents.set(0, 4, ClickableItem.of(clone) {
                switchItemShopified(e, contents)
            })

            InventoryConstants.ConstantUtilities.setShopifiedItem(contents, clone)
            return
        } else if (itemIn?.getNBTClone()?.hasKey("default") == false) {
            e.whoClicked.setItemOnCursor(InventoryConstants.ConstantUtilities.getShopifiedItem(contents))
            buildAndSetDefaultShopifiedItem(contents)
            return
        }

        e.whoClicked.sendMessage("§c§l[!] §eYou must be holding an item when trying to switch the item shopified!")
    }

    private fun changePrice(contents: InventoryContents, player: Player) {
        val shopType: ShopType? = InventoryConstants.ConstantUtilities.getSelectedShopType(contents)
        playerShops.createSignInput(player, "&6Price: &9", "&6Enter " + shopType?.toTitleCase() + " price", "", "") { p, strings ->
            val split = strings[0].split("§9")
            val price: Long? = if (split.size < 2) null else split[1].replace("\\D".toRegex(), "").toLongOrNull()

            if (price == null) {
                p.sendMessage("§c&l[!] &eInvalid number!")
                INVENTORY.openWithContents(player, contents, 1)
                return@createSignInput true
            } else if (price < 0) {
                INVENTORY.openWithContents(player, contents, 1)
                p.sendMessage("§c&l[!] &ePrice must be a non-negative number!")
                return@createSignInput true
            }

            InventoryConstants.ConstantUtilities.setPrice(contents, price)
            INVENTORY.openWithContents(player, contents, 1)
            return@createSignInput true
        }
    }

    private fun getShopTypeSwitcherItem(contents: InventoryContents): ItemStack {
        return KItemStack(
                material = Material.LIME_STAINED_GLASS_PANE,
                displayName = "&6" + InventoryConstants.ConstantUtilities.getSelectedShopType(contents),
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