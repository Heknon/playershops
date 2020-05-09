package me.oriharel.playershops.shops.shop

import me.oriharel.playershops.shops.bank.ShopBank
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

internal class ShowShop(
        bank: ShopBank?,
        price: Long?,
        storageSize: Long,
        item: ItemStack?,
        block: Block?,
        owner: UUID?,
        allowedMutators: MutableSet<UUID>,
        settings: MutableSet<ShopSetting>
) : PlayerShop(
        item,
        block,
        owner,
        bank,
        price,
        storageSize,
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