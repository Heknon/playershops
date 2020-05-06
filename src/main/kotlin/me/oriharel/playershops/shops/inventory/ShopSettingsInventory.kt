package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.InventoryListener
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import me.oriharel.playershops.PlayerShops
import me.oriharel.playershops.shops.shop.PlayerShop
import me.oriharel.playershops.utilities.KItemStack
import me.oriharel.playershops.utilities.Utils.format
import me.oriharel.playershops.utilities.Utils.giveItem
import me.oriharel.playershops.utilities.Utils.modifyMeta
import me.oriharel.playershops.utilities.Utils.openWithProperties
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack

class ShopSettingsInventory : NotUpdatableInventoryProvider {

    override fun init(player: Player, contents: InventoryContents) {
        contents.fillRect(0, 0, 2, 8, ClickableItem.empty(InventoryConstants.Item.EMPTY_GRAY_STAINED_GLASS_PANE))
        contents.fillRect(0, 3, 2, 5, ClickableItem.empty(InventoryConstants.Item.EMPTY_YELLOW_STAINED_GLASS_PANE))
        contents.fillRow(3, ClickableItem.empty(InventoryConstants.Item.EMPTY_WHITE_STAINED_GLASS_PANE))

        Bukkit.getScheduler().runTaskLater(PlayerShops.INSTANCE, Runnable {
            run {
                val shop = InventoryConstants.ConstantUtilities.getShop(contents)!!
                val shopifiedItem = InventoryConstants.ConstantUtilities.getShopifiedItem(contents)

                contents.set(1, 4, ClickableItem.of(getShopifiedItem(contents)) {
                    if (shopifiedItem == null) openSetItemInventory(player, contents)
                    else openStorageInventory(player, contents)
                })

                contents.set(3, 0, ClickableItem.of(getDestructorItem()) {
                    destroyShop(player, contents)
                })
            }
        }, 1)
    }

    private fun destroyShop(player: Player, contents: InventoryContents) {
        val shop: PlayerShop? = InventoryConstants.ConstantUtilities.getShop(contents)

        if (shop?.item?.amount != null && shop.item?.amount!! > 1) {
            player.sendMessage("§4§l[!] §cClear out your storage before removing your shop.")
            return
        }
        shop?.destroy(player)
    }

    private fun openStorageInventory(player: Player, contents: InventoryContents) {
        StorageInventory.INVENTORY.openWithProperties(player, contents)
    }

    private fun openSetItemInventory(player: Player, contents: InventoryContents) {
        SetItemInventory.INVENTORY.openWithProperties(player, contents)
    }

    private fun getShopifiedItem(contents: InventoryContents): ItemStack {
        val currShopifiedItem = InventoryConstants.ConstantUtilities.getShopifiedItem(contents)
        val shop = InventoryConstants.ConstantUtilities.getShop(contents)

        return currShopifiedItem?.modifyMeta {
            it.lore = mutableListOf()
            it.lore!!.add("&e&l&o&m-----")
            it.lore!!.add("&6&l* &eQuantity: &fx${shop?.item?.amount?.minus(1)?.format() ?: 0} / x${shop?.storageSize?.format() ?: 0}")
            it.lore!!.add("&o&7(( &fLeft Click &7to set the item ))")
            it.lore!!.add("&o&7(( &fRight Click &7to set the item ))")
        } ?: KItemStack(
                material = Material.BARRIER,
                displayName = "&cAbsolutely nothing!",
                lore = listOf("&o&7(( &fClick &7to set the item ))")
        )
    }

    private fun getDestructorItem(): ItemStack {
        return KItemStack(
                material = Material.BARRIER,
                displayName = "&4&l[!] &cREMOVE SHOP &r&7(Click)",
                lore = listOf(
                        "",
                        "&7Once clicked, your &cPlayer Shop will despawn&7,",
                        "&7and will be placed &ainto your inventory&7.",
                        "",
                        "&b&oNOTE: &fYou must empty all of the contents first."
                )
        )
    }


    companion object {
        val INVENTORY: SmartInventory = SmartInventory.builder()
                .id(InventoryConstants.SettingsInventory.ID)
                .provider(ShopSettingsInventory())
                .size(InventoryConstants.SettingsInventory.ROWS, InventoryConstants.SettingsInventory.COLUMNS)
                .title(InventoryConstants.SettingsInventory.TITLE)
                .closeable(InventoryConstants.SettingsInventory.CLOSEABLE)
                .listener(InventoryListener(InventoryCloseEvent::class.java) {
                    val contents = PlayerShops.INSTANCE.inventoryManager.getContents(it.player as Player)!!.get()
                    val shop = InventoryConstants.ConstantUtilities.getShop(contents) ?: return@InventoryListener
                    val useMobCoins = InventoryConstants.ConstantUtilities.getUseMobCoins(contents) ?: false
                    val useBank = InventoryConstants.ConstantUtilities.getUseBank(contents) ?: false


                    shop.buildHologram(PlayerShops.INSTANCE)
                    PlayerShops.INSTANCE.shopManager.setPlayerShopBlockState(shop.block!!, shop)

                })
                .manager(PlayerShops.INSTANCE.inventoryManager)
                .build()
    }
}