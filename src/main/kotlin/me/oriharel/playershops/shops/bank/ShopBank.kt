package me.oriharel.playershops.shops.bank

import java.util.*

abstract class ShopBank(var balance: Long) {

    abstract fun takeFromAndDeposit(amount: Int, takeFrom: UUID)
    abstract fun giveToAndWithdraw(amount: Int, giveTo: UUID)

    protected fun takeFromAndDeposit(amount: Int, takeFrom: (amount: Int) -> Unit) {
        deposit(amount)
        takeFrom(amount)
    }

    protected fun giveToAndWithdraw(amount: Int, giveTo: (amount: Int) -> Unit) {
        withdraw(amount)
        giveTo(amount)
    }

    private fun deposit(amount: Int) {
        balance += amount
    }

    private fun withdraw(amount: Int) {
        balance -= amount
    }
}