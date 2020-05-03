package me.oriharel.playershops.shops.shop

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

internal class ShowcaseShop(
        item: ItemStack,
        block: Block,
        owner: UUID,
        allowedMutators: MutableList<UUID>,
        settings: MutableList<ShopSetting>
) : PlayerShop(
        item,
        block,
        owner,
        allowedMutators,
        settings
) {
    override fun openPlayerGUI(player: Player) {
        return
    }

    override fun run(amount: Int, player: Player) {
        return
    }

}