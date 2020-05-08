package me.oriharel.playershops.shops.shop

import me.oriharel.playershops.shops.bank.ShopBank
import me.oriharel.playershops.shops.inventory.PurchaseInventory
import net.milkbowl.vault.economy.Economy
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

abstract class MoneyShop(
        bank: ShopBank?,
        price: Long?,
        val economy: Economy,
        item: ItemStack?,
        storageSize: Long,
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
    protected val useInternalBank: Boolean = settings.contains(ShopSetting.USE_INTERNAL_BANK)
    val useZenCoins: Boolean = settings.contains(ShopSetting.USE_MOB_COINS)

    override fun openPlayerGUI(player: Player) {
        PurchaseInventory.INVENTORY.open(player)
    }
}