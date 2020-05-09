package me.oriharel.playershops.shops

import me.oriharel.playershops.shops.bank.ShopBank
import me.oriharel.playershops.shops.bank.VaultShopBank
import me.oriharel.playershops.shops.bank.ZenCoinShopBank
import me.oriharel.playershops.shops.shop.*
import net.milkbowl.vault.economy.Economy
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import java.util.*

class PlayerShopFactory(private val economy: Economy) {
    @JvmOverloads
    fun <T : PlayerShop> createNewShop(
            shopType: ShopType,
            item: ItemStack?,
            block: Block?,
            owner: UUID?,
            settings: MutableSet<ShopSetting>,
            storageSize: Long,
            bankInitialFunds: Long = 0,
            price: Long? = null
    ): T {
        val bank: ShopBank =
                if (settings.contains(ShopSetting.USE_MOB_COINS))
                    ZenCoinShopBank(bankInitialFunds)
                else
                    VaultShopBank(bankInitialFunds, economy)


        return when (shopType) {
            ShopType.BUY -> BuyShop(bank, economy, price, storageSize, item, block, owner, mutableSetOf(), settings)
            ShopType.SELL -> SellShop(bank, economy, price, storageSize, item, block, owner, mutableSetOf(), settings)
            ShopType.SHOW -> ShowShop(bank, price, storageSize, item, block, owner, mutableSetOf(), settings)
        } as T
    }

    @JvmOverloads
    fun <T : PlayerShop> createShop(
            shopType: ShopType,
            bank: ShopBank? = null,
            price: Long?,
            storageSize: Long,
            itemStack: ItemStack?,
            block: Block?,
            owner: UUID,
            allowedMutators: MutableSet<UUID>,
            settings: MutableSet<ShopSetting>
    ): T {
        return when (shopType) {
            ShopType.BUY -> BuyShop(bank, economy, price, storageSize, itemStack, block, owner, allowedMutators, settings)
            ShopType.SELL -> SellShop(bank, economy, price, storageSize, itemStack, block, owner, allowedMutators, settings)
            ShopType.SHOW -> ShowShop(bank, price, storageSize, itemStack, block, owner, allowedMutators, settings)
        } as T
    }

    @JvmOverloads
    fun <T : PlayerShop> convertShop(
            shop: T,
            toType: ShopType,
            bank: ShopBank?,
            price: Long? = null
    ): T {
        val bankToUse: ShopBank? = bank ?: shop.bank

        return when (toType) {
            ShopType.BUY -> BuyShop(bankToUse, economy, price
                    ?: shop.price, shop.storageSize, shop.item, shop.block, shop.owner, shop.allowedMutators, shop.settings)
            ShopType.SELL -> SellShop(bankToUse, economy, price
                    ?: shop.price, shop.storageSize, shop.item, shop.block, shop.owner, shop.allowedMutators, shop.settings)
            ShopType.SHOW -> ShowShop(bankToUse, price
                    ?: shop.price, shop.storageSize, shop.item, shop.block, shop.owner, shop.allowedMutators, shop.settings)
        } as T
    }
}