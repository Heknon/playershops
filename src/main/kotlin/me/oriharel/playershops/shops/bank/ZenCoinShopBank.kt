package me.oriharel.playershops.shops.bank

import me.swanis.mobcoins.MobCoinsAPI
import me.swanis.mobcoins.profile.Profile
import java.util.*

internal class ZenCoinShopBank(balance: Long) : ShopBank(balance) {
    override fun takeFromAndDeposit(amount: Long, takeFrom: UUID) {
        takeFromAndDeposit(amount) {
            val profile: Profile = getPlayerMobCoinProfile(takeFrom)

            profile.mobCoins = profile.mobCoins - it.toInt()
        }
    }

    override fun giveToAndWithdraw(amount: Long, giveTo: UUID) {
        giveToAndWithdraw(amount) {
            val profile: Profile = getPlayerMobCoinProfile(giveTo)

            profile.mobCoins = profile.mobCoins + it.toInt()
        }
    }

    private fun getPlayerMobCoinProfile(uuid: UUID): Profile {
        return MobCoinsAPI.getProfileManager().getProfile(uuid)
    }
}