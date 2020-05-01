package me.oriharel.playershops

import com.google.gson.Gson
import me.oriharel.playershops.shops.shop.PlayerShop
import me.oriharel.playershops.utilities.Utils.getNBTClone
import me.oriharel.playershops.utilities.Utils.modifyMeta
import me.oriharel.playershops.utilities.Utils.updateNBT
import net.minecraft.server.v1_15_R1.NBTTagString
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack

class ShopItem : ItemStack {
    val shop: PlayerShop

    constructor(stack: ItemStack, serializer: Gson) : super(stack) {
        val shopJson: String = getNBTClone().getString("playerShop")
        this.shop = serializer.fromJson(shopJson, PlayerShop::class.java)
    }

    constructor(stack: ItemStack, shop: PlayerShop, serializer: Gson) : super(stack) {
        this.shop = shop
        updateNBT {
            it["playerShop"] = NBTTagString.a(serializer.toJson(shop, shop.javaClass))
        }
    }

    constructor(shop: PlayerShop, playerShops: PlayerShops, serializer: Gson) : super(getDefaultShopItem(playerShops)) {
        this.shop = shop
        updateNBT {
            it["playerShop"] = NBTTagString.a(serializer.toJson(shop, shop.javaClass))
        }
    }

    companion object {
        private var shopItemCache: ItemStack? = null

        /**
         * @param force whether or not should overwrite cache
         */
        @JvmOverloads
        fun getDefaultShopItem(playerShops: PlayerShops, force: Boolean = false): ItemStack {
            if (shopItemCache != null && !force) return shopItemCache!!

            val configLoad: YamlConfiguration = playerShops.getConfig("config.yml")!!

            val lore: List<String> = configLoad.getStringList("shop_default_lore").map { ChatColor.translateAlternateColorCodes('&', it) }
            val material: Material? = Material.getMaterial(configLoad.getString("shop_default_material") ?: "")
            val displayName: String? = configLoad.getString("shop_default_display_name")
            val item: ItemStack = ItemStack(material!!, 1)
            item.modifyMeta {
                it.lore = lore
                it.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName!!))
            }
            return item
        }
    }
}