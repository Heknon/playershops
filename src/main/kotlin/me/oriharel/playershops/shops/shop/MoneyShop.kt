package me.oriharel.playershops.shops.shop

import me.oriharel.playershops.shops.bank.ShopBank
import me.oriharel.playershops.shops.inventory.PurchaseInventory
import net.milkbowl.vault.economy.Economy
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

abstract class MoneyShop(
        val bank: ShopBank?,
        val economy: Economy,
        var price: Long,
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
    protected val useInternalBank: Boolean = settings.contains(ShopSetting.USE_INTERNAL_BANK)
    protected val useZenCoins: Boolean = settings.contains(ShopSetting.USE_MOB_COINS)

    override fun openPlayerGUI(player: Player) {
        PurchaseInventory.inventory.open(player)
    }
}