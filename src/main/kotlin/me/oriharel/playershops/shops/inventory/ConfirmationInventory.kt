package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import fr.minuskube.inv.content.InventoryProvider
import me.oriharel.playershops.PlayerShops
import me.oriharel.playershops.utilities.KItemStack
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ConfirmationInventory(private val deny: ItemStack, private val confirm: ItemStack) : InventoryProvider {

    override fun init(player: Player, contents: InventoryContents) {
        val onClick = getOnClick(contents)

        contents.set(0, 2, ClickableItem.of(deny) {
            onClick(ConfirmationType.DENY)
        })
        contents.set(0, 6, ClickableItem.of(confirm) {
            onClick(ConfirmationType.CONFIRM)
        })
    }

    override fun update(p0: Player?, p1: InventoryContents?) {

    }

    private fun getOnClick(contents: InventoryContents): (ConfirmationType) -> Unit {
        return contents.property(InventoryConstants.ON_CLICK_CONTENT_ID)
    }

    companion object {
        val INVENTORY: SmartInventory = SmartInventory.builder()
                .id(InventoryConstants.ConfirmationInventory.ID)
                .closeable(false)
                .title(InventoryConstants.ConfirmationInventory.TITLE)
                .size(InventoryConstants.ConfirmationInventory.ROWS, InventoryConstants.ConfirmationInventory.COLUMNS)
                .closeable(InventoryConstants.ConfirmationInventory.CLOSEABLE)
                .parent(PurchaseInventory.INVENTORY)
                .provider(ConfirmationInventory(
                        deny = KItemStack(Material.RED_STAINED_GLASS_PANE, displayName = "&c&lDENY"),
                        confirm = KItemStack(Material.LIME_STAINED_GLASS, displayName = "&a&lCONFIRM")
                ))
                .manager(PlayerShops.INSTANCE.inventoryManager)
                .build()
    }
}