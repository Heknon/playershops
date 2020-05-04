package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import fr.minuskube.inv.content.InventoryProvider
import me.oriharel.playershops.PlayerShops
import me.oriharel.playershops.shops.shop.Depositable
import me.oriharel.playershops.shops.shop.MoneyShop
import me.oriharel.playershops.shops.shop.PlayerShop
import me.oriharel.playershops.utilities.KItemStack
import me.oriharel.playershops.utilities.Utils.format
import me.swanis.mobcoins.MobCoinsAPI
import org.bukkit.Material
import org.bukkit.entity.Player
import kotlin.math.abs

class BankInventory(private val playerShops: PlayerShops) : InventoryProvider {

    override fun init(player: Player, contents: InventoryContents) {
        // TODO: Withdraw, See amount of money in, deposit
        val shop = getShop(contents) as MoneyShop
        val bank = shop.bank!!
        val useMobCoins = getUseMobCoins(contents)

        contents.set(1, 2,
                ClickableItem.of(
                        KItemStack(
                                material = Material.MAGMA_CREAM
                        )) {
                    playerShops.createSignInput(
                            player,
                            "&6Amount: &9",
                            "&bEnter the of",
                            "&b" + if (useMobCoins) "Zen Coins" else "Money",
                            "&b to withdraw"
                    ) { _, strings ->
                        val amount: Long? = strings[0].toLongOrNull()

                        when {
                            amount == null -> {
                                player.sendMessage("§b§l[INFO] §eYou must enter a valid number to withdraw!")
                                return@createSignInput true
                            }
                            amount < 0 -> {
                                player.sendMessage("§b§l[INFO] §eYou must enter a number above zero!")
                                return@createSignInput true
                            }
                            bank.balance - amount < 0 -> {
                                player.sendMessage("§b§l[INFO] §eYou don't have enough funds in the bank to withdraw that amount!")
                                return@createSignInput true
                            }
                            else -> {
                                bank.giveToAndWithdraw(amount, player.uniqueId)
                                player.sendMessage("§b§l[INFO] §eYou've withdrawn ${amount.format()} from your shop's bank")
                                init(player, contents)
                                return@createSignInput true
                            }
                        }
                    }
                })

        // Setup to view of the amount of money in the bank
        contents.set(1, 4,
                ClickableItem.empty(
                        KItemStack(
                                material = Material.EMERALD_BLOCK,
                                displayName = "&6BALANCE",
                                lore = listOf(
                                        "&d${bank.balance.format()}&e " + if (useMobCoins) "Zen Coins" else "Money"
                                )
                        )))

        contents.set(1, 6,
                ClickableItem.of(
                        KItemStack(
                                material = Material.PAPER
                        )) {
                    if (shop !is Depositable) {
                        player.sendMessage("§c§l[!] §eYou cannot deposit into this shop!")
                        return@of
                    }
                    playerShops.createSignInput(
                            player,
                            "&6Amount: &9",
                            "&bEnter the of",
                            "&b" + if (useMobCoins) "Zen Coins" else "Money",
                            "&b to deposit"
                    ) { _, strings ->
                        val amount: Long? = strings[0].toLongOrNull()
                        val userBal: Long =
                                if (useMobCoins) MobCoinsAPI.getProfileManager().getProfile(player).mobCoins.toLong()
                                else playerShops.economy.getBalance(player).toLong()

                        when {
                            amount == null -> {
                                player.sendMessage("§b§l[INFO] §eYou must enter a valid number to deposit!")
                                return@createSignInput true
                            }
                            amount < 0 -> {
                                player.sendMessage("§b§l[INFO] §eYou must enter a number above zero!")
                                return@createSignInput true
                            }
                            userBal - amount < 0 -> {
                                player.sendMessage("§b§l[INFO] §eInsufficient funds! You are missing ${abs(userBal - amount).format()}")
                                return@createSignInput true
                            }
                            else -> {
                                bank.takeFromAndDeposit(amount, player.uniqueId)
                                player.sendMessage("§b§l[INFO] §eYou've deposited ${amount.format()} into your shop's bank")
                                init(player, contents)
                                return@createSignInput true
                            }
                        }
                    }
                })

    }

    override fun update(player: Player, contents: InventoryContents) {

    }

    private fun getShop(contents: InventoryContents): PlayerShop {
        return contents.property(InventoryConstants.PASSED_DOWN_SHOP_CONTENT_ID)
    }

    private fun getUseMobCoins(contents: InventoryContents): Boolean {
        return contents.property(InventoryConstants.USE_MOB_COINS_CONTENT_ID)
    }

    companion object {
        val INVENTORY: SmartInventory = SmartInventory.builder()
                .id(InventoryConstants.BankInventory.ID)
                .provider(BankInventory(PlayerShops.INSTANCE))
                .size(InventoryConstants.BankInventory.ROWS, InventoryConstants.BankInventory.COLUMNS)
                .closeable(InventoryConstants.BankInventory.CLOSEABLE)
                .title(InventoryConstants.BankInventory.TITLE)
                .build()
    }
}