package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.content.InventoryContents
import me.oriharel.playershops.shops.shop.PlayerShop
import me.oriharel.playershops.shops.shop.ShopType
import me.oriharel.playershops.utilities.KItemStack
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

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
        const val TITLE: String = "Player Shop"
        const val ROWS: Int = 4
        const val COLUMNS: Int = 9
        const val CLOSEABLE: Boolean = true
    }

    object SetItemInventory {
        const val ID: String = "shopSetItemInventoryID"
        const val TITLE: String = "Set Item - Player Shop"
        const val ROWS: Int = 3
        const val COLUMNS: Int = 9
        const val CLOSEABLE: Boolean = true
    }

    object StorageInventory {
        const val ID: String = "shopStorageInventoryID"
        const val TITLE: String = "Storage - Player Shop"
        const val ROWS: Int = 3
        const val COLUMNS: Int = 9
        const val CLOSEABLE: Boolean = true
    }

    object SettingsInventory {
        const val ID: String = "shopSettingsInventoryID"
        const val TITLE: String = "Player Shop"
        const val ROWS: Int = 4
        const val COLUMNS: Int = 9
        const val CLOSEABLE: Boolean = true

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
        const val CLOSEABLE: Boolean = true
    }

    object Item {
        val EMPTY_GRAY_STAINED_GLASS_PANE = KItemStack(material = Material.GRAY_STAINED_GLASS_PANE, displayName = " ", metadataModifier = {
            it.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
        })
        val EMPTY_LIGHT_GRAY_STAINED_GLASS_PANE = KItemStack(material = Material.LIGHT_GRAY_STAINED_GLASS_PANE, displayName = " ", metadataModifier = {
            it.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
        })
        val EMPTY_YELLOW_STAINED_GLASS_PANE = KItemStack(material = Material.YELLOW_STAINED_GLASS_PANE, displayName = " ", metadataModifier = {
            it.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
        })
        val EMPTY_WHITE_STAINED_GLASS_PANE = KItemStack(material = Material.WHITE_STAINED_GLASS_PANE, displayName = " ", metadataModifier = {
            it.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
        })
    }

    object ConstantUtilities {

        fun getShop(contents: InventoryContents): PlayerShop? {
            return contents.property(PASSED_DOWN_SHOP_CONTENT_ID)
        }

        fun getUseMobCoins(contents: InventoryContents): Boolean? {
            return contents.property(USE_MOB_COINS_CONTENT_ID)
        }

        fun getUseBank(contents: InventoryContents): Boolean? {
            return contents.property(USE_BANK_CONTENT_ID)
        }

        fun <T> getOnClick(contents: InventoryContents): (T) -> Unit {
            return contents.property(ON_CLICK_CONTENT_ID)
        }

        fun getPrice(contents: InventoryContents): Long? {
            return contents.property(PRICE_CONTENT_ID)
        }

        fun getSelectedShopType(contents: InventoryContents): ShopType? {
            return contents.property(SHOP_TYPE_CONTENT_ID)
        }

        fun getShopifiedItem(contents: InventoryContents): ItemStack? {
            return contents.property(SHOPIFIED_ITEM_CONTENT_ID)
        }


        fun setShop(contents: InventoryContents, shop: PlayerShop?): ConstantUtilities {
            contents.setProperty(PASSED_DOWN_SHOP_CONTENT_ID, shop)
            return this
        }

        fun setUseMobCoins(contents: InventoryContents, useMobCoins: Boolean?): ConstantUtilities {
            contents.setProperty(USE_MOB_COINS_CONTENT_ID, useMobCoins)
            return this
        }

        fun setUseBank(contents: InventoryContents, useBank: Boolean?): ConstantUtilities {
            contents.setProperty(USE_BANK_CONTENT_ID, useBank)
            return this
        }

        fun <T> setOnClick(contents: InventoryContents, onClick: (T) -> Unit): ConstantUtilities {
            contents.setProperty(ON_CLICK_CONTENT_ID, onClick)
            return this
        }

        fun setPrice(contents: InventoryContents, price: Long?): ConstantUtilities {
            contents.setProperty(PRICE_CONTENT_ID, price)
            return this
        }

        fun setSelectedShopType(contents: InventoryContents, selectedShopType: ShopType?): ConstantUtilities {
            contents.setProperty(SHOP_TYPE_CONTENT_ID, selectedShopType)
            return this
        }

        fun setShopifiedItem(contents: InventoryContents, shopifiedItem: ItemStack?): ConstantUtilities {
            contents.setProperty(SHOPIFIED_ITEM_CONTENT_ID, shopifiedItem)
            return this
        }


    }
}