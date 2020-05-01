package me.oriharel.playershops.inventory

interface DataHolder<T> {
    var dataHeld: T
    fun construct(payload: T): InventoryPage
}