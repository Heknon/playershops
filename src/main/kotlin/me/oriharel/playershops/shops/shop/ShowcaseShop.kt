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
    override fun openSettings() {
        TODO("Not yet implemented")
    }

    override fun openPlayerGUI() {
        TODO("Not yet implemented")
    }

    override fun run(amount: Int, player: Player) {
        TODO("Not yet implemented")
    }

    override fun onPlace() {
        TODO("Not yet implemented")
    }

    override fun opeInitializationGUI(player: Player) {
        TODO("Not yet implemented")
    }

}