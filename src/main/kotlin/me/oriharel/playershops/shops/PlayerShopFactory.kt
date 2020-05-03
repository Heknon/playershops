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
            item: ItemStack,
            block: Block,
            owner: UUID,
            settings: MutableList<ShopSetting>,
            bankInitialFunds: Long = 0,
            price: Long? = null
    ): T {
        val bank: ShopBank? = if (settings.contains(ShopSetting.USE_INTERNAL_BANK))
            if (settings.contains(ShopSetting.USE_MOB_COINS))
                ZenCoinShopBank(bankInitialFunds)
            else
                VaultShopBank(bankInitialFunds, economy)
        else null

        return when (shopType) {
            ShopType.BUY -> BuyShop(bank, economy, price!!, item, block, owner, mutableListOf(), settings)
            ShopType.SELL -> SellShop(bank, economy, price!!, item, block, owner, mutableListOf(), settings)
            ShopType.SHOWCASE -> ShowcaseShop(item, block, owner, mutableListOf(), settings)
        } as T
    }

    @JvmOverloads
    fun <T : PlayerShop> createShop(
            shopType: ShopType,
            bank: ShopBank? = null,
            price: Long?,
            itemStack: ItemStack,
            block: Block,
            owner: UUID,
            allowedMutators: MutableList<UUID>,
            settings: MutableList<ShopSetting>
    ): T {
        return when (shopType) {
            ShopType.BUY -> BuyShop(bank, economy, price!!, itemStack, block, owner, allowedMutators, settings)
            ShopType.SELL -> SellShop(bank, economy, price!!, itemStack, block, owner, allowedMutators, settings)
            ShopType.SHOWCASE -> ShowcaseShop(itemStack, block, owner, allowedMutators, settings)
        } as T
    }

    @JvmOverloads
    fun <T : PlayerShop> convertShop(
            shop: T,
            toType: ShopType,
            bank: ShopBank? = null,
            price: Long? = null
    ): T {
        val bankToUse: ShopBank? = if (shop is MoneyShop && bank == null) shop.bank else bank

        return when (toType) {
            ShopType.BUY -> BuyShop(bankToUse, economy, price!!, shop.item, shop.block, shop.owner, shop.allowedMutators, shop.settings)
            ShopType.SELL -> SellShop(bankToUse, economy, price!!, shop.item, shop.block, shop.owner, shop.allowedMutators, shop.settings)
            ShopType.SHOWCASE -> ShowcaseShop(shop.item, shop.block, shop.owner, shop.allowedMutators, shop.settings)
        } as T
    }
}