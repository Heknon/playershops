package me.oriharel.playershops.inventory

data class InventoryLocation(val row: Int, val column: Int) {
    fun getIndex() = row * column
}