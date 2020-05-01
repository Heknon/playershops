package me.oriharel.playershops.shops.shop

import me.oriharel.playershops.shops.inventory.ShopInitializationInventory
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

abstract class PlayerShop(
        var item: ItemStack,
        val block: Block,
        val owner: UUID,
        val allowedMutators: MutableList<UUID>,
        val settings: MutableList<ShopSetting>
) {

    /**
     * What happens when the owner of a PlayerShop right clicks the PlayerShop
     */
    protected abstract fun openSettings()

    /**
     * What happens when any player right clicks the PlayerShop
     */
    protected abstract fun openPlayerGUI()

    /**
     * The implementation of what happens when a player clicks on the buy/sell
     */
    protected abstract fun run(amount: Int, player: Player)

    protected abstract fun onPlace()

    fun opeInitializationGUI(player: Player) {
        ShopInitializationInventory.inventory.open(player)
        ShopInitializationInventory.inventory.manager.getContents(player).get().setProperty("shopBlock", block)
    }

    fun getType(): ShopType {
        return if (this is BuyShop) ShopType.BUY else if (this is SellShop) ShopType.SELL else if (this is ShowcaseShop) ShopType.SHOWCASE else ShopType.BUY
    }

    fun open(opener: UUID) {
        if (opener == owner) {
            openSettings()
        } else {
            openPlayerGUI()
        }
    }


}