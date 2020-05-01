package me.oriharel.playershops.utilities

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.oriharel.playershops.serializers.PlayerShopTypeAdapter
import me.oriharel.playershops.serializers.ShopBankTypeAdapter
import me.oriharel.playershops.serializers.UUIDTypeAdapter
import me.oriharel.playershops.shops.PlayerShopFactory
import me.oriharel.playershops.shops.shop.PlayerShop
import me.oriharel.playershops.shops.bank.ShopBank
import net.milkbowl.vault.economy.Economy
import net.minecraft.server.v1_15_R1.NBTBase
import net.minecraft.server.v1_15_R1.NBTTagCompound
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.util.*

object Utils {

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

    fun Pair<Long, Long>.toUUID(): UUID {
        return UUID(this.first, this.second)
    }

    fun getItemStackUnhandledNBT(itemStack: ItemStack): MutableMap<String?, NBTBase?> {
        val metaReference = getItemStackMetaReference(itemStack)
        return ReflectionUtils.Fields.getFieldValueOfUnknownClass(metaReference, "org.bukkit.craftbukkit.v1_15_R1.inventory.CraftMetaItem", "unhandledTags")!!
    }

    fun ItemStack.unhandledNBT(): MutableMap<String?, NBTBase?> {
        return getItemStackUnhandledNBT(this)
    }

    fun getItemStackMetaReference(itemStack: ItemStack): ItemMeta {
        if (!itemStack.hasItemMeta()) itemStack.itemMeta = ItemStack(itemStack.type, itemStack.amount).itemMeta
        return ReflectionUtils.Fields.getFieldValueOfUnknownClass<ItemMeta>(itemStack, ItemStack::class.java, "meta")!!
    }

    fun ItemStack.modifyMeta(applier: (ItemMeta) -> Unit) {
        val metaRef: ItemMeta = getItemStackMetaReference(this)
        applier(metaRef)
    }

    fun ItemStack.updateNBT(applier: (MutableMap<String?, NBTBase?>) -> Unit) {
        val nbtRef: MutableMap<String?, NBTBase?> = getItemStackUnhandledNBT(this)
        applier(nbtRef)
    }

    fun ItemStack.getNBTClone() : NBTTagCompound {
        return CraftItemStack.asNMSCopy(this).orCreateTag
    }

    fun <BankType : ShopBank, ShopType : PlayerShop> getSerializer(economy: Economy, shopFactory: PlayerShopFactory): Gson {
        return GsonBuilder().registerTypeHierarchyAdapter(UUID::class.java, UUIDTypeAdapter())
                .registerTypeHierarchyAdapter(ShopBank::class.java, ShopBankTypeAdapter<BankType>(economy))
                .registerTypeHierarchyAdapter(PlayerShop::class.java, PlayerShopTypeAdapter<ShopType>(shopFactory)).create()
    }
}