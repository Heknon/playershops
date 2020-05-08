package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.InventoryListener
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import me.oriharel.playershops.PlayerShops
import me.oriharel.playershops.utilities.KItemStack
import me.oriharel.playershops.utilities.Utils.openWithProperties
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemFlag

class SetItemInventory : NotUpdatableInventoryProvider {
    override fun init(player: Player, contents: InventoryContents) {
        contents.fill(ClickableItem.empty(InventoryConstants.Item.EMPTY_GRAY_STAINED_GLASS_PANE))
        contents.fillRect(0, 3, 2, 5, ClickableItem.empty(InventoryConstants.Item.EMPTY_YELLOW_STAINED_GLASS_PANE))


        contents.set(1, 4, ClickableItem.empty(KItemStack(
                material = Material.PAINTING,
                displayName = "&4&l[!] &cSET SHOP ITEM &r&7(Information)",
                lore = listOf(
                        "",
                        "&fClick &7the item in your&a inventory &7that",
                        "&7you want to select for your &cPlayer Shop"
                ),
                metadataModifier = {
                    it.addEnchant(Enchantment.ARROW_FIRE, 1, false)
                    it.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                }
        )))

    }


    companion object {
        val INVENTORY: SmartInventory = SmartInventory.builder()
                .id(InventoryConstants.SetItemInventory.ID)
                .title(InventoryConstants.SetItemInventory.TITLE)
                .size(InventoryConstants.SetItemInventory.ROWS, InventoryConstants.SetItemInventory.COLUMNS)
                .closeable(InventoryConstants.SetItemInventory.CLOSEABLE)
                .parent(ShopSettingsInventory.INVENTORY)
                .provider(SetItemInventory())
                .manager(PlayerShops.INSTANCE.inventoryManager)
                .listener(InventoryListener(InventoryCloseEvent::class.java) {
                    ShopSettingsInventory.INVENTORY.manager.getContents(it.player as Player).ifPresent { contents ->
                        ShopSettingsInventory.INVENTORY.openWithProperties(it.player as Player, contents)
                    }
                })
                .build()
    }
}