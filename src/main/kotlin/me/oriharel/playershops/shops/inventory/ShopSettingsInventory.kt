package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.InventoryListener
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import me.oriharel.playershops.PlayerShops
import me.oriharel.playershops.shops.bank.VaultShopBank
import me.oriharel.playershops.shops.shop.PlayerShop
import me.oriharel.playershops.shops.shop.ShopSetting
import me.oriharel.playershops.shops.shop.ShopType
import me.oriharel.playershops.utilities.KItemStack
import me.oriharel.playershops.utilities.Utils.format
import me.oriharel.playershops.utilities.Utils.modifyMeta
import me.oriharel.playershops.utilities.Utils.openWithProperties
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack

class ShopSettingsInventory : NotUpdatableInventoryProvider {

    override fun init(player: Player, contents: InventoryContents) {
        val shop = InventoryConstants.ConstantUtilities.getShop(contents)!!

        contents.fill(ClickableItem.empty(InventoryConstants.Item.EMPTY_GRAY_STAINED_GLASS_PANE))
        contents.fillRect(0, 3, 2, 5, ClickableItem.empty(InventoryConstants.Item.EMPTY_YELLOW_STAINED_GLASS_PANE))
        contents.fillRow(3, ClickableItem.empty(InventoryConstants.Item.EMPTY_WHITE_STAINED_GLASS_PANE))

        contents.set(1, 4, ClickableItem.of(getShopifiedItem(shop)) {
            if (shop.item == null) openSetItemInventory(player, contents)
            else openStorageInventory(player, contents)
        })

        contents.set(3, 0, ClickableItem.of(bankItem) {
            openBankInventory(player, contents)
        })

        contents.set(3, 3, ClickableItem.of(getShopSwitcherItem(shop)) {
            switchShopType(player, shop, contents)
        })

        contents.set(3, 5, ClickableItem.of(getPriceItem(shop)) {
            changePrice(player, shop, contents)
        })

        contents.set(3, 8, ClickableItem.of(destructorItem) {
            destroyShop(player, shop)
        })


    }

    private fun destroyShop(player: Player, shop: PlayerShop) {
        if (shop.item?.amount != null && shop.item?.amount!! > 1) {
            player.sendMessage("§4§l[!] §cClear out your storage before removing your shop.")
            return
        }
        shop.destroy(player)
    }

    private fun switchShopType(player: Player, shop: PlayerShop, contents: InventoryContents) {
        val newShop = PlayerShops.INSTANCE.shopManager.shopFactory.convertShop(shop, shop.getType().next(), shop.bank ?: VaultShopBank(0, PlayerShops.INSTANCE.economy))
        InventoryConstants.ConstantUtilities.setShop(contents, newShop)
        shop.switch(newShop)
        init(player, contents)
    }

    private fun changePrice(player: Player, shop: PlayerShop, contents: InventoryContents) {
        PlayerShops.INSTANCE.createSignInput(player, "", "^ ^ ^", "Enter price", "") { p, strings ->
            val price = strings[0].toLongOrNull()

            if (price == null || price < 0) {
                p.sendMessage("§4§l[!] §r§cYou need to enter a price!")
                return@createSignInput true
            }

            shop.price = price
            shop.update()

            val useMobCoins = shop.settings.contains(ShopSetting.USE_MOB_COINS)
            val priceText = (if (!useMobCoins) "$" else "") + price.format() + (if (useMobCoins) " Zen Coins" else "") + "."
            p.sendMessage("§6§l[!] §ePlayer Shop price set to $priceText")

            contents.set(3, 5, ClickableItem.of(getPriceItem(shop)) {
                changePrice(player, shop, contents)
            })

            return@createSignInput true
        }
    }

    private fun openStorageInventory(player: Player, contents: InventoryContents) {
        StorageInventory.INVENTORY.openWithProperties(player, contents)
    }

    private fun openSetItemInventory(player: Player, contents: InventoryContents) {
        SetItemInventory.INVENTORY.openWithProperties(player, contents)
    }

    private fun openBankInventory(player: Player, contents: InventoryContents) {
        BankInventory.INVENTORY.openWithProperties(player, contents)
    }

    private fun getShopifiedItem(shop: PlayerShop): ItemStack {
        return shop.item?.modifyMeta {
            if (it.lore == null) it.lore = mutableListOf()
            it.lore!!.add("&e&l&o&m-----")
            it.lore!!.add("&6&l* &eQuantity: &fx${shop.amountInStock} / x${shop.storageSize.format()}")
            it.lore!!.add("&o&7(( &fLeft Click &7to set the item ))")
            it.lore!!.add("&o&7(( &fRight Click &7to set the item ))")
        } ?: KItemStack(
                material = Material.BARRIER,
                displayName = "&cAbsolutely nothing!",
                lore = listOf("&o&7(( &fClick &7to set the item ))")
        )
    }

    private fun getPriceItem(shop: PlayerShop): ItemStack {
        val useMobCoins = shop.settings.contains(ShopSetting.USE_MOB_COINS)
        val price = if (!useMobCoins && shop.price != null) "$" else "" + (shop.price?.format() ?: "n/a") + if (useMobCoins && shop.price != null) " Zen Coins" else ""
        return KItemStack(
                material = Material.EMERALD,
                displayName = "&2&l[!] &a&lSET PRICE &7(Click)",
                lore = listOf(
                        "",
                        "&2&l* &a&lCURRENT PRICE: &f&l$price",
                        "",
                        "&7&o(( &f&oClick &7to adjust the &a&oasking price",
                        "&7for your &cPlayer Shop&7. ))"
                )
        )
    }

    private fun getShopSwitcherItem(shop: PlayerShop): ItemStack {
        val type = shop.getType()
        return KItemStack(
                material = Material.OAK_SIGN,
                displayName = "&5&l[!] &dSET SHOP MODE &r&7(Click)",
                lore = listOf(
                        "",
                        getText("Buying", type == ShopType.BUY, "&5&l*"),
                        getText("Selling", type == ShopType.SELL, "&5&l*"),
                        getText("Showcasing", type == ShopType.SHOWCASE, "&5&l*"),
                        "",
                        "&7&o(( &fClick &7to cycle between modes ))"
                )

        )
    }

    private fun getText(text: String, highlighted: Boolean, prefix: String = ""): String {
        return if (highlighted) "$prefix &6⟩ &f$text &6⟨&r" else "$prefix &7$text"
    }


    private val bankItem: ItemStack = KItemStack(
            material = Material.JUKEBOX,
            displayName = "&2&l[!] &a&lSHOP BANK",
            lore = listOf(
                    "",
                    "&7&o(( &f&oClick &7to open your &c&oPlayer Shop's &7&obank&f&o. &7&o))"
            )
    )


    private val destructorItem: ItemStack = KItemStack(
            material = Material.BARRIER,
            displayName = "&4&l[!] &c&lREMOVE SHOP &r&7&l(Click)",
            lore = listOf(
                    "",
                    "&7Once clicked, your &cPlayer Shop will despawn&7,",
                    "&7and will be placed &ainto your inventory&7.",
                    "",
                    "&b&oNOTE: &f&oYou must empty all of the contents first."
            )
    )


    companion object {
        val INVENTORY: SmartInventory = SmartInventory.builder()
                .id(InventoryConstants.SettingsInventory.ID)
                .provider(ShopSettingsInventory())
                .size(InventoryConstants.SettingsInventory.ROWS, InventoryConstants.SettingsInventory.COLUMNS)
                .title(InventoryConstants.SettingsInventory.TITLE)
                .closeable(InventoryConstants.SettingsInventory.CLOSEABLE)
                .manager(PlayerShops.INSTANCE.inventoryManager)
                .build()
    }
}