package me.oriharel.playershops.shops.bank

import java.util.*

abstract class ShopBank(var balance: Long) {

    abstract fun takeFromAndDeposit(amount: Long, takeFrom: UUID)
    abstract fun giveToAndWithdraw(amount: Long, giveTo: UUID)

    protected fun takeFromAndDeposit(amount: Long, takeFrom: (amount: Long) -> Unit) {
        deposit(amount)
        takeFrom(amount)
    }

    protected fun giveToAndWithdraw(amount: Long, giveTo: (amount: Long) -> Unit) {
        withdraw(amount)
        giveTo(amount)
    }

    private fun deposit(amount: Long) {
        balance += amount
    }

    private fun withdraw(amount: Long) {
        balance -= amount
    }
}