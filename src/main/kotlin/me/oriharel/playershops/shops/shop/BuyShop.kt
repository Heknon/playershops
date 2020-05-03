package me.oriharel.playershops.shops.shop

import me.oriharel.playershops.shops.bank.ShopBank
import me.oriharel.playershops.shops.inventory.ShopSettingsInventory
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

internal class BuyShop(
        bank: ShopBank?,
        price: Long,
        item: ItemStack,
        block: Block,
        owner: UUID,
        allowedMutators: MutableList<UUID>,
        settings: MutableList<ShopSetting>
) : MoneyShop(
        bank,
        price,
        item,
        block,
        owner,
        allowedMutators,
        settings
) {

    override fun openPlayerGUI(player: Player) {

    }

    override fun run(amount: Int, player: Player) {
    }
}