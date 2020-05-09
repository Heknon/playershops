package me.oriharel.playershops.shops.shop

enum class ShopType(val value: Int) {
    BUY(0),
    SELL(1),
    SHOW(2);

    fun next(): ShopType {
        return values[(this.ordinal + 1) % values.size]
    }

    companion object {
        private val values = values()
        private val types = values.associateBy { it.ordinal }

        fun findByValue(value: Int) = types[value]
    }
}