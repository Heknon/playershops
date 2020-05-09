package me.oriharel.playershops.shops.shop

import me.oriharel.playershops.shops.bank.ShopBank
import me.oriharel.playershops.utilities.Utils.giveItem
import me.oriharel.playershops.utilities.Utils.sendMessage
import me.oriharel.playershops.utilities.Utils.toOfflinePlayer
import me.oriharel.playershops.utilities.Utils.toTitleCase
import me.swanis.mobcoins.MobCoinsAPI
import net.milkbowl.vault.economy.Economy
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*


internal class SellShop(
        bank: ShopBank?,
        economy: Economy,
        price: Long?,
        storageSize: Long,
        item: ItemStack?,
        block: Block?,
        owner: UUID?,
        allowedMutators: MutableSet<UUID>,
        settings: MutableSet<ShopSetting>
) : MoneyShop(
        bank,
        price,
        economy,
        item,
        storageSize,
        block,
        owner,
        allowedMutators,
        settings
) {

    override fun run(amount: Int, player: Player) {

        if (item!!.amount - amount < 0) {
            player.sendMessage("§c§l[!] §eThis shop does not have $amount ${item!!.type.toTitleCase()}")
            return
        }

        if (useInternalBank) {
            bank!!.takeFromAndDeposit(price!! * amount, player.uniqueId)
        } else {
            if (useZenCoins) {
                val profile = MobCoinsAPI.getProfileManager().getProfile(player)
                if (profile.mobCoins - amount < 0) {
                    player.sendMessage("messages.yml", "InsufficientFunds")
                    return
                }
                val profileOwner = MobCoinsAPI.getProfileManager().getProfile(owner)
                profile.mobCoins = profile.mobCoins - amount
                profileOwner.mobCoins = profileOwner.mobCoins + amount
            } else {
                if (economy.getBalance(player) - amount.toDouble() < 0) {
                    player.sendMessage("messages.yml", "InsufficientFunds")
                    return
                }
                economy.withdrawPlayer(player, amount.toDouble())
                economy.depositPlayer(owner?.toOfflinePlayer(), amount.toDouble())
            }
            val clone: ItemStack = item!!.clone()

            item!!.amount = item!!.amount - amount
            clone.amount = amount
            player.giveItem(clone)
        }
    }
}