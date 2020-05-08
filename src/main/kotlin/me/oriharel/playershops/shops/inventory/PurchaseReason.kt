package me.oriharel.playershops.shops.inventory

enum class PurchaseReason {
    SUCCESS,
    NO_INVENTORY_SPACE,
    INSUFFICIENT_FUNDS,
    SHOP_INSUFFICIENT_FUNDS,
    SHOP_EMPTY,
    NO_SHOP_SPACE
}