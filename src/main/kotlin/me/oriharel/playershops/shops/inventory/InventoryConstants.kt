package me.oriharel.playershops.shops.inventory

object InventoryConstants {
    const val PASSED_DOWN_SHOP_CONTENT_ID: String = "passedDownShop"
    const val USE_MOB_COINS_CONTENT_ID: String = "useMobCoins"
    const val USE_BANK_CONTENT_ID: String = "useBank"
    const val ON_CLICK_CONTENT_ID = "onClickCallback"
    const val PRICE_CONTENT_ID: String = "selectedShopPrice"
    const val SHOP_TYPE_CONTENT_ID: String = "selectedShopType"
    const val SHOPIFIED_ITEM_CONTENT_ID: String = "selectedShopifiedItem"

    object PurchaseInventory {
        const val ID: String = "shopPurchaseInventoryID"
        const val TITLE: String = "§1SHOP"
        const val ROWS: Int = 1
        const val COLUMNS: Int = 9
        const val CLOSEABLE: Boolean = true
    }

    object InitializationInventory {
        const val ID: String = "shopInitializationInventoryID"
        const val TITLE: String = "§1Setup your shop!"
        const val ROWS: Int = 3
        const val COLUMNS: Int = 9
        const val CLOSEABLE: Boolean = false
    }

    object SettingsInventory {
        const val ID: String = "shopSettingsInventoryID"
        const val TITLE: String = "§1Settings"
        const val ROWS: Int = 3
        const val COLUMNS: Int = 9
        const val CLOSEABLE: Boolean = false
    }

    object BankInventory {
        const val ID: String = "shopBankInventoryID"
        const val TITLE: String = "§1Bank"
        const val ROWS: Int = 3
        const val COLUMNS: Int = 9
        const val CLOSEABLE: Boolean = true
    }

    object ConfirmationInventory {
        const val ID: String = "confirmationUtilityInventoryID"
        const val TITLE: String = "Confirm"
        const val ROWS: Int = 1
        const val COLUMNS: Int = 9
        const val CLOSEABLE: Boolean = false
    }
}