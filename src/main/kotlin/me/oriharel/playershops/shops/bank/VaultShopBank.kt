package me.oriharel.playershops.shops.bank

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import java.util.*

internal class VaultShopBank(balance: Long, @Transient private val economy: Economy) : ShopBank(balance) {
    override fun takeFromAndDeposit(amount: Int, takeFrom: UUID) {
        takeFromAndDeposit(amount) {
            economy.withdrawPlayer(Bukkit.getOfflinePlayer(takeFrom), it.toDouble())
        }
    }

    override fun giveToAndWithdraw(amount: Int, giveTo: UUID) {
        giveToAndWithdraw(amount) {
            economy.depositPlayer(Bukkit.getOfflinePlayer(giveTo), it.toDouble())
        }
    }
}