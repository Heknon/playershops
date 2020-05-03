package me.oriharel.playershops.shops.shop

import me.oriharel.playershops.shops.bank.ShopBank
import me.oriharel.playershops.utilities.Utils.ifOnline
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
        price: Long,
        item: ItemStack,
        block: Block,
        owner: UUID,
        allowedMutators: MutableList<UUID>,
        settings: MutableList<ShopSetting>
) : MoneyShop(
        bank,
        economy,
        price,
        item,
        block,
        owner,
        allowedMutators,
        settings
) {

    override fun run(amount: Int, player: Player) {
        if (!player.inventory.contains(item, amount)) {
            player.sendMessage("§c§l[!] §eYou do not have enough of the resource you are trying to sell!")
        }

        if (useInternalBank) {
            bank!!.giveToAndWithdraw(amount, player.uniqueId)
        } else {
            if (useZenCoins) {
                val profileOwner = MobCoinsAPI.getProfileManager().getProfile(owner)
                if (profileOwner.mobCoins - amount < 0) {
                    owner.toOfflinePlayer().ifOnline { it.sendMessage("§c§l[!] §eInsufficient funds! You are missing ${kotlin.math.abs(profileOwner.mobCoins - amount)} zen coins! Players can no longer buy from your shop!") }
                    player.sendMessage("§c§l[!] §eThe owner of this shop doesn't have enough money right now!")
                    return
                }
                val profile = MobCoinsAPI.getProfileManager().getProfile(player)
                profile.mobCoins = profile.mobCoins - amount
                profileOwner.mobCoins = profileOwner.mobCoins + amount
            } else {
                if (economy.getBalance(owner.toOfflinePlayer()) - amount.toDouble() < 0) {
                    owner.toOfflinePlayer().ifOnline { it.sendMessage("§c§l[!] §eInsufficient funds! You are missing ${kotlin.math.abs(economy.getBalance(player) - amount)}! Players can no longer buy from your shop!") }
                    player.sendMessage("§c§l[!] §eThe owner of this shop doesn't have enough money right now!")
                    return
                }
                economy.depositPlayer(player, amount.toDouble())
                economy.withdrawPlayer(owner.toOfflinePlayer(), amount.toDouble())
            }
        }

        val clone = item.clone()
        clone.amount = amount
        player.inventory.remove(clone)
        item.amount += amount
    }
}