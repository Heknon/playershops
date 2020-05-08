package me.oriharel.playershops

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.oriharel.playershops.shops.shop.ShopSetting
import me.oriharel.playershops.shops.shop.ShopType
import me.oriharel.playershops.utilities.Utils.giveItem
import org.bukkit.command.CommandSender

@CommandAlias("playershops")
class ShopCommands(private val playerShops: PlayerShops) : BaseCommand() {
    @Subcommand("give")
    @CommandPermission("playershops.give")
    @CommandCompletion("@players @shopTypes @range:100-200 @range:1-10 @boolean @boolean")
    @Syntax("<player_name> <shop_type> <storage_size> <amount> <use_zen_coins> <use_internal_bank>")
    fun giveShop(executor: CommandSender, player: OnlinePlayer, shopType: ShopType, storageSize: Long, amount: Int = 1, useZenCoins: Boolean = false, useInternalBank: Boolean = true) {
        val settings: MutableSet<ShopSetting> = mutableSetOf()
        if (useZenCoins) settings.add(ShopSetting.USE_MOB_COINS)
        if (useInternalBank) settings.add(ShopSetting.USE_INTERNAL_BANK)
        val item = playerShops.shopManager.getShopItem(
                shop = playerShops.shopManager.shopFactory.createNewShop(
                        shopType,
                        null,
                        null,
                        null,
                        settings,
                        storageSize,
                        0,
                        null
                ))
        item?.amount = amount
        player.player.giveItem(item!!)
    }

}