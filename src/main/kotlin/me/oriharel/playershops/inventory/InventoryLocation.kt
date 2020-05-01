package me.oriharel.playershops.inventory

data class InventoryLocation(val row: Int, val column: Int) {
    fun getIndex() = row * column
    companion object {
        fun fromIndex(index: Int): InventoryLocation {
            return InventoryLocation(index / 9, index % 9)
        }
    }
}