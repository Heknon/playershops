package me.oriharel.playershops.utilities

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import fr.minuskube.inv.InventoryListener
import fr.minuskube.inv.InventoryManager
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import me.oriharel.playershops.PlayerShops
import me.oriharel.playershops.serializers.ItemStackTypeAdapter
import me.oriharel.playershops.serializers.PlayerShopTypeAdapter
import me.oriharel.playershops.serializers.ShopBankTypeAdapter
import me.oriharel.playershops.serializers.UUIDTypeAdapter
import me.oriharel.playershops.shops.PlayerShopFactory
import me.oriharel.playershops.shops.bank.ShopBank
import me.oriharel.playershops.shops.shop.PlayerShop
import me.oriharel.playershops.utilities.message.Message
import me.oriharel.playershops.utilities.message.Placeholder
import net.milkbowl.vault.economy.Economy
import net.minecraft.server.v1_15_R1.NBTBase
import net.minecraft.server.v1_15_R1.NBTTagCompound
import org.bukkit.*
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.text.DecimalFormat
import java.util.*

object Utils {

    private val decimalFormat: DecimalFormat = DecimalFormat("#,###")

    private fun longToLocation(packed: Long): Location {
        val x = (packed shl 37 shr 37).toInt()
        val y = (packed ushr 54).toInt()
        val z = (packed shl 10 shr 37).toInt()
        return Location(null, x.toDouble(), y.toDouble(), z.toDouble())
    }

    @JvmOverloads
    fun Long.toLocation(world: World? = null): Location {
        val loc = longToLocation(this)
        loc.world = world
        return loc
    }

    fun Location.toLong(): Long {
        val x = this.blockX
        val y = this.blockY
        val z = this.blockZ
        return x.toLong() and 0x7FFFFFF or (z.toLong() and 0x7FFFFFF shl 27) or (y.toLong() shl 54)
    }

    fun UUID.toBukkitWorld(): World? {
        return Bukkit.getWorld(this)
    }

    fun UUID.toOfflinePlayer(): OfflinePlayer {
        return Bukkit.getOfflinePlayer(this)
    }

    fun Pair<Long, Long>.toUUID(): UUID {
        return UUID(this.first, this.second)
    }

    fun OfflinePlayer.ifOnline(ifOnline: (Player) -> Unit): OfflinePlayer {
        if (this.isOnline) ifOnline(this.player!!)
        return this
    }

    fun Inventory.getItemAmountInInventory(item: ItemStack?): Int {
        if (item == null) return 0
        var amount = 0
        contents.forEach {
            if (it?.isSimilar(item) == true) {
                amount += it.amount
            }
        }
        return amount
    }

    fun Player.sendMessage(
            configName: String,
            configRoute: String,
            price: String = "",
            amount: String = "",
            thing: String = "",
            shopOwner: String = ""
    ) {
        Message(
                configName,
                configRoute,
                this,
                *getDefaultPlaceholders(price, amount, thing, shopOwner)
        ).send()
    }

    fun String.applyPlaceholders(vararg placeholders: Placeholder): String {
        return Message(this, *placeholders).appliedText
    }

    fun String.applyPlaceholders(price: String = "", amount: String = "", thing: String, shopOwner: String = ""): String {
        return this.applyPlaceholders(
                *getDefaultPlaceholders(price, amount, thing, shopOwner)
        )
    }

    private fun getDefaultPlaceholders(price: String = "", amount: String = "", thing: String, shopOwner: String = ""): Array<Placeholder> {
        return arrayOf(
                Placeholder("%price%", price),
                Placeholder("%amount%", amount),
                Placeholder("%thing%", thing),
                Placeholder("%shop_owner%", shopOwner)
        )
    }


    fun Player.giveItem(item: ItemStack, dropIfNoPlace: Boolean = false): Player {
        if (this.itemOnCursor.isSimilar(item) && this.itemOnCursor.amount + item.amount < 65) {
            this.itemOnCursor.amount += item.amount
            setItemOnCursor(itemOnCursor)
            return this
        }
        if (dropIfNoPlace && this.inventory.firstEmpty() == -1) {
            this.location.world?.dropItemNaturally(this.location, item)
        } else {
            this.inventory.addItem(item)
        }
        return this
    }

    fun SmartInventory.openWithContents(player: Player, contents: InventoryContents, delay: Long) {
        Bukkit.getScheduler().runTaskLater(PlayerShops.INSTANCE, Runnable {
            run {
                openWithContents(player, contents, checkForOldInventory = false)
            }
        }, delay)
    }

    fun SmartInventory.openWithContents(player: Player, contents: InventoryContents, preInitContentModifier: ((InventoryContents) -> Unit)? = null, checkForOldInventory: Boolean): Inventory {

        val oldInv = manager.getInventory(player)
        val listenersField = SmartInventory::class.java.getDeclaredField("listeners")
        val setContents = InventoryManager::class.java
                .getDeclaredMethod("setContents", Player::class.java, InventoryContents::class.java)
        val setInventory = InventoryManager::class.java
                .getDeclaredMethod("setInventory", Player::class.java, SmartInventory::class.java)
        setContents.isAccessible = true
        setInventory.isAccessible = true
        listenersField.isAccessible = true

        if (checkForOldInventory) oldInv.ifPresent {
            (listenersField.get(it) as List<InventoryListener<out Event>>)
                    .filter { listener: InventoryListener<out Event> -> listener.type == InventoryCloseEvent::class.java }
                    .forEach { listener: InventoryListener<out Event> ->
                        (listener as InventoryListener<InventoryCloseEvent>)
                                .accept(InventoryCloseEvent(player.openInventory))
                    }

        }

        oldInv.ifPresent {
            setInventory.invoke(manager, player, null)
        }

        setContents.invoke(manager, player, contents)
        preInitContentModifier?.invoke(contents)
        provider.init(player, contents)

        val opener = manager.findOpener(type)
                .orElseThrow { IllegalStateException("No opener found for the inventory type " + type.name) }
        val handle = opener.open(this, player)

        setInventory.invoke(manager, player, this)

        return handle
    }

    fun SmartInventory.openWithProperties(player: Player, properties: Map<String, Any>) {
        val newContents: InventoryContents = InventoryContents.Impl(this, player)
        newContents.pagination().page(0)
        openWithContents(player, newContents, {
            for (entry in properties) {
                it.setProperty(entry.key, entry.value)
            }
        }, false)
    }

    fun SmartInventory.openWithProperties(player: Player, contents: InventoryContents) {
        openWithProperties(player, contents.getPropertiesReference())
    }

    fun SmartInventory.openWithProperties(player: Player, properties: Map<String, Any>, delay: Long) {
        Bukkit.getScheduler().runTaskLater(PlayerShops.INSTANCE, Runnable {
            openWithProperties(player, properties)
        }, delay)
    }

    fun SmartInventory.openWithProperties(player: Player, contents: InventoryContents, delay: Long) {
        Bukkit.getScheduler().runTaskLater(PlayerShops.INSTANCE, Runnable {
            openWithProperties(player, contents)
        }, delay)
    }

    fun InventoryContents.getPropertiesReference(): Map<String, Any> {
        val impl = InventoryContents::class.java.declaredClasses[0]
        val field = impl.getDeclaredField("properties")
        field.isAccessible = true
        return field.get(this) as Map<String, Any>
    }

    fun String.extractNumber(): Number? {
        val re = "-\\d+|\\d+".toRegex()
        val extractedNumberVal = re.find(this)?.value
        return extractedNumberVal?.toInt()
    }

    fun getItemStackUnhandledNBT(itemStack: ItemStack): MutableMap<String?, NBTBase?> {
        val metaReference = getItemStackMetaReference(itemStack)
        return ReflectionUtils.Fields.getFieldValueOfUnknownClass(metaReference, "org.bukkit.craftbukkit.v1_15_R1.inventory.CraftMetaItem", "unhandledTags")!!
    }

    fun ItemStack.unhandledNBT(): MutableMap<String?, NBTBase?> {
        return getItemStackUnhandledNBT(this)
    }

    fun Inventory.availableSpace(item: ItemStack? = null): Int {
        var space = 0
        contents.forEach {
            if (it == null || it.type == Material.AIR) {
                space += 64
            } else if (it.isSimilar(item)) {
                space += 64 - it.amount
            }
        }
        return space
    }

    fun Inventory.amount(item: ItemStack?): Int {
        if (item == null) return 0
        var count = 0
        contents.forEach {
            if (it.isSimilar(item)) count += it.amount
        }
        return count
    }

    fun getItemStackMetaReference(itemStack: ItemStack): ItemMeta {
        val meta = ReflectionUtils.Fields.getFieldValueOfObject<ItemMeta>(itemStack, "meta")
        if (meta == null) itemStack.itemMeta = ItemStack(itemStack.type, itemStack.amount).itemMeta
        return ReflectionUtils.Fields.getFieldValueOfObject<ItemMeta>(itemStack, "meta")!!
    }

    fun ItemStack.modifyMeta(applier: ((ItemMeta) -> Unit)?): ItemStack {
        val meta: ItemMeta = itemMeta!!
        applier?.invoke(meta)
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', meta.displayName))
        if (meta.lore != null) meta.lore = meta.lore?.map { ChatColor.translateAlternateColorCodes('&', it) }
        itemMeta = meta
        return this
    }

    @JvmOverloads
    fun String?.toTitleCase(removeUnderscores: Boolean = true): String? {
        if (this == null) return null
        val stringBuilder = StringBuilder(this.length)
        var prevChar = '$'
        var firstChar = true

        for (c in this) {
            if (removeUnderscores && c == '_') stringBuilder.append(" ")
            else if (firstChar || (prevChar == ' ' && c != ' ') || (removeUnderscores && prevChar == '_' && (c != ' ' || c != '_'))) stringBuilder.append(c.toUpperCase())
            else stringBuilder.append(c.toLowerCase())
            prevChar = c
            firstChar = false
        }

        return stringBuilder.toString()
    }

    fun Number.format(): String {
        return decimalFormat.format(this)
    }

    fun <E : Enum<E>> Enum<E>.toTitleCase(): String? {
        return this.name.toTitleCase()
    }

    fun ItemStack.updateNBT(applier: ((MutableMap<String?, NBTBase?>) -> Unit)?): ItemStack {
        val nbtRef: MutableMap<String?, NBTBase?> = getItemStackUnhandledNBT(this)
        applier?.invoke(nbtRef)
        return this
    }

    fun ItemStack.getNBTClone(): NBTTagCompound {
        return CraftItemStack.asNMSCopy(this).orCreateTag
    }

    fun <BankType : ShopBank, ShopType : PlayerShop> getSerializer(economy: Economy, shopFactory: PlayerShopFactory): Gson {
        return GsonBuilder().registerTypeHierarchyAdapter(UUID::class.java, UUIDTypeAdapter())
                .registerTypeHierarchyAdapter(ShopBank::class.java, ShopBankTypeAdapter<BankType>(economy))
                .registerTypeHierarchyAdapter(PlayerShop::class.java, PlayerShopTypeAdapter<ShopType>(shopFactory))
                .registerTypeHierarchyAdapter(ItemStack::class.java, ItemStackTypeAdapter())
                .create()
    }
}