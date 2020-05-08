package me.oriharel.playershops.shops.shop

import com.gmail.filoghost.holographicdisplays.api.Hologram
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI
import me.oriharel.playershops.PlayerShops
import me.oriharel.playershops.shops.bank.ShopBank
import me.oriharel.playershops.shops.inventory.InventoryConstants
import me.oriharel.playershops.shops.inventory.ShopSettingsInventory
import me.oriharel.playershops.utilities.Utils.format
import me.oriharel.playershops.utilities.Utils.giveItem
import me.oriharel.playershops.utilities.Utils.openWithProperties
import me.oriharel.playershops.utilities.Utils.toOfflinePlayer
import me.oriharel.playershops.utilities.Utils.toTitleCase
import me.swanis.mobcoins.MobCoinsAPI
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import java.util.*

abstract class PlayerShop(
        var item: ItemStack?,
        var block: Block?,
        var owner: UUID?,
        var bank: ShopBank?,
        var price: Long?,
        val storageSize: Long,
        val allowedMutators: MutableSet<UUID>,
        val settings: MutableSet<ShopSetting>
) {

    /**
     * checks whether the shop is stocked.
     * removes one from amount since that one is shown as what the shop is holding
     */
    val isEmpty: Boolean
        get() =
            amountInStock <= 0

    /**
     * helper function for checking if the shop is stocked
     */
    val isNotEmpty: Boolean get() = !isEmpty

    /**
     * checks whether the shop is full.
     * checks if the amount of the item of the shops is equal to or larger than the storage size
     */
    val isFull: Boolean get() = amountInStock >= storageSize

    val isNotFull: Boolean get() = !isFull

    val storageRemaining: Long get() = storageSize - amountInStock

    /**
     * wrapper for getting the amount the item in stock
     */
    val amountInStock: Long get() = item?.amount?.minus(1)?.toLong() ?: 0L

    val balance: Number
        get() {
            val useInternalBank = settings.contains(ShopSetting.USE_INTERNAL_BANK)
            val useMobCoins = settings.contains(ShopSetting.USE_MOB_COINS)
            val ownerProfile = MobCoinsAPI.getProfileManager().getProfile(owner)

            return if (useInternalBank) bank!!.balance else if (useMobCoins) ownerProfile.mobCoins else PlayerShops.INSTANCE.economy.getBalance(owner?.toOfflinePlayer())
        }

    /**
     * What happens when the owner of a PlayerShop right clicks the PlayerShop
     */
    protected open fun openSettings(player: Player) {
        player.sendMessage("§c§l[!] §eOpening the settings GUI of your shop")
        ShopSettingsInventory.INVENTORY.openWithProperties(player, mutableMapOf(Pair(InventoryConstants.PASSED_DOWN_SHOP_CONTENT_ID, this)))
    }

    /**
     * What happens when any player right clicks the PlayerShop
     */
    protected abstract fun openPlayerGUI(player: Player)

    /**
     * The implementation of what happens when a player clicks on the buy/sell
     */
    abstract fun run(amount: Int, player: Player)

    fun onPlace(e: BlockPlaceEvent, playerShops: PlayerShops) {
        owner = e.player.uniqueId
        block = e.block
        playerShops.shopManager.setPlayerShopBlockState(e.block, this)
    }

    /**
     * destroy a shop. delete all of it's references and hologram.
     * if a player is passed, give him the shop item.
     */
    fun destroy(player: Player? = null) {
        val playerShops = PlayerShops.INSTANCE
        val shopManager = playerShops.shopManager
        val shopItem = shopManager.getShopItem(shop = this)!!
        clearHologram(playerShops)
        player?.giveItem(shopItem, false)
        shopManager.removePlayerShop(block)
    }

    /**
     * like destroy except the shop block isn't removed but replaced with data
     */
    fun switch(newShop: PlayerShop) {
        val playerShops = PlayerShops.INSTANCE
        val shopManager = playerShops.shopManager
        clearHologram(playerShops)
        shopManager.removePlayerShop(block)
        shopManager.setPlayerShopBlockState(newShop.block!!, newShop)
    }

    /**
     * updates the information of the machine stored in a block
     */
    fun update() {
        if (block != null) PlayerShops.INSTANCE.shopManager.setPlayerShopBlockState(block!!, this)
    }

    fun getType(): ShopType {
        return if (this is BuyShop) ShopType.BUY else if (this is SellShop) ShopType.SELL else if (this is ShowcaseShop) ShopType.SHOWCASE else ShopType.BUY
    }

    fun open(opener: UUID) {
        if (opener == owner) {
            openSettings(Bukkit.getPlayer(opener)!!)
        } else {
            openPlayerGUI(Bukkit.getPlayer(opener)!!)
        }
    }

    fun buildHologram(playerShops: PlayerShops) {
        val hologram = getHologram(playerShops)

        var itemText = "§r§7" + (item?.itemMeta?.displayName ?: "§cAbsolutely nothing!")
        if (this is MoneyShop) itemText += " §r§f(x${if (this is BuyShop) (storageSize - item!!.amount - 1).toString() else if (this is SellShop) (item!!.amount - 1).toString() else ""})"
        var index = 0
        clearHologram(playerShops, hologram)

        hologram.insertTextLine(index++, "§e${owner?.toOfflinePlayer()?.name} §dis ${getType().toTitleCase()}...")
        hologram.insertTextLine(index++, itemText)
        if (this is MoneyShop) hologram.insertTextLine(index++, "§fPrice: §a" + (if (!useZenCoins) "$" else "") + (price?.format()
                ?: "n/a") + (if (useZenCoins) " Zen Coins" else ""))
        hologram.insertItemLine(index, item)
    }

    fun clearHologram(playerShops: PlayerShops, hologram: Hologram? = null) {
        val holo = hologram ?: getHologram(playerShops)
        holo.clearLines()
    }

    private fun getHologram(playerShops: PlayerShops): Hologram {
        val placeLoc = block?.location?.add(0.5, 2.5, 0.5)
        return HologramsAPI.getHolograms(playerShops).find { it.location == placeLoc }
                ?: HologramsAPI.createHologram(playerShops, placeLoc)
    }

    override fun toString(): String {
        return "PlayerShop(item=$item, block=$block, owner=$owner, storageSize=$storageSize, allowedMutators=$allowedMutators, settings=$settings)"
    }


}