package me.oriharel.playershops.shops.shop

import me.oriharel.playershops.shops.bank.ShopBank
import me.oriharel.playershops.utilities.Utils.ifOnline
import me.oriharel.playershops.utilities.Utils.sendMessage
import me.oriharel.playershops.utilities.Utils.toOfflinePlayer
import me.swanis.mobcoins.MobCoinsAPI
import net.milkbowl.vault.economy.Economy
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

internal class BuyShop(
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
), Depositable {

    override fun run(amount: Int, player: Player) {
        if (!player.inventory.contains(item, amount)) {
            return
        }

        if (useInternalBank) {
            bank!!.giveToAndWithdraw(price!! * amount, player.uniqueId)
        } else {
            if (useZenCoins) {
                val profileOwner = MobCoinsAPI.getProfileManager().getProfile(owner)
                if (profileOwner.mobCoins - amount < 0) {
                    owner?.toOfflinePlayer()?.ifOnline {
                        it.sendMessage("messages.yml", "PurchaseAttemptOwnerHasInsufficientFunds", amount = kotlin.math.abs(profileOwner.mobCoins - amount).toString(), thing = "Zen Coins")
                    }
                    player.sendMessage("messages.yml", "ShopHasInsufficientFunds")
                    return
                }
                val profile = MobCoinsAPI.getProfileManager().getProfile(player)
                profile.mobCoins = profile.mobCoins - amount
                profileOwner.mobCoins = profileOwner.mobCoins + amount
            } else {
                if (economy.getBalance(owner?.toOfflinePlayer()) - amount.toDouble() < 0) {
                    owner?.toOfflinePlayer()?.ifOnline {
                        it.sendMessage("messages.yml", "PurchaseAttemptOwnerHasInsufficientFunds", amount = kotlin.math.abs(economy.getBalance(player) - amount).toString(), thing = "")
                    }
                    player.sendMessage("messages.yml", "ShopHasInsufficientFunds")
                    return
                }
                economy.depositPlayer(player, amount.toDouble())
                economy.withdrawPlayer(owner?.toOfflinePlayer(), amount.toDouble())
            }
        }

        val clone = item!!.clone()
        clone.amount = amount
        player.inventory.remove(clone)
        item!!.amount += amount
    }
}