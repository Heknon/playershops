package me.oriharel.playershops

import me.oriharel.playershops.shops.PlayerShopFactory
import me.oriharel.playershops.shops.bank.ShopBank
import me.oriharel.playershops.shops.shop.PlayerShop
import me.oriharel.playershops.utilities.Utils
import me.oriharel.playershops.utilities.Utils.toLong
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.block.TileState
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.collections.HashMap

class PlayerShopManager(internal val playerShops: PlayerShops) {
    val shopCache: MutableMap<UUID, MutableMap<Long, PlayerShop>> = HashMap()
    val shopFactory: PlayerShopFactory = PlayerShopFactory(playerShops.economy)
    private val playerShopNamespacedKey: NamespacedKey = NamespacedKey(playerShops, "playerShop")
    private val playerShopPersistentDataType: PersistentDataType<String, PlayerShop> = PlayerShopPersistentDataType(this)
    val serializer = Utils.getSerializer<ShopBank, PlayerShop>(playerShops.economy, shopFactory)

    fun getShopItem(item: ItemStack? = null, shop: PlayerShop? = null): ShopItem? {
        return if (item != null && shop == null) {
            ShopItem(item, serializer)
        } else if (item == null && shop != null) {
            ShopItem(shop, playerShops, serializer)
        } else if (item != null && shop != null) {
            ShopItem(item, shop, serializer)
        } else {
            null
        }
    }

    fun getPlayerShopFromBlock(block: Block?): PlayerShop? {
        if (block == null) return null
        if (block.state !is TileState) return null
        if (shopCache.containsKey(block.world.uid)) {
            val worldCache: MutableMap<Long, PlayerShop> = shopCache[block.world.uid]!!
            val loc: Long = block.location.toLong()

            if (worldCache.containsKey(loc)) {
                return worldCache[loc]!!
            }

            val shop: PlayerShop? = getPlayerShopFromBlock(block)

            if (shop != null) worldCache[loc] = shop
            if (shop == null) playerShops.logger.fine("PlayerShop not found at block $block")
            return shop
        }
        val shop: PlayerShop? = getPlayerShopFromBlockState(block)

        if (shop != null) shopCache[block.world.uid] = mutableMapOf(Pair(block.location.toLong(), shop))
        if (shop == null) playerShops.logger.fine("PlayerShop not found at block $block")
        return shop
    }

    private fun getPlayerShopFromBlockState(block: Block): PlayerShop? {
        val blockState: BlockState = block.state
        if (blockState !is TileState) return null
        return blockState.persistentDataContainer.get(playerShopNamespacedKey, playerShopPersistentDataType)
    }

    fun setPlayerShopBlockState(block: Block, shop: PlayerShop): Boolean {
        val blockState: BlockState = block.state
        if (blockState !is TileState) return false

        blockState.persistentDataContainer.set(playerShopNamespacedKey, playerShopPersistentDataType, shop)
        if (!shopCache.containsKey(block.world.uid)) shopCache[block.world.uid] = mutableMapOf()
        shopCache[block.world.uid]!![block.location.toLong()] = shop
        return true
    }


}