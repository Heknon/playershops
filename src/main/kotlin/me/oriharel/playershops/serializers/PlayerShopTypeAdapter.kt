package me.oriharel.playershops.serializers

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import me.oriharel.playershops.shops.PlayerShopFactory
import me.oriharel.playershops.shops.bank.ShopBank
import me.oriharel.playershops.shops.shop.*
import me.oriharel.playershops.utilities.Utils.toBukkitWorld
import me.oriharel.playershops.utilities.Utils.toLocation
import me.oriharel.playershops.utilities.Utils.toLong
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Type
import java.util.*

open class PlayerShopTypeAdapter<T : PlayerShop>(protected val shopFactory: PlayerShopFactory) : JsonSerializer<T>, JsonDeserializer<T> {
    override fun serialize(shop: T, p1: Type?, ctx: JsonSerializationContext): JsonElement {
        val obj: JsonObject = JsonObject()

        val shopType: ShopType = if (shop is BuyShop) ShopType.BUY else if (shop is SellShop) ShopType.SELL else ShopType.SHOWCASE
        val bank: ShopBank? = if (shop is MoneyShop) shop.bank else null
        val price: Long? = if (shop is MoneyShop) shop.price else null

        obj.add("blockXYZ", JsonPrimitive(shop.block.location.toLong()))
        obj.add("type", JsonPrimitive(shopType.name))

        obj.add("allowedMutators", ctx.serialize(shop.allowedMutators, object : TypeToken<MutableList<UUID>>() {}.type))
        obj.add("settings", ctx.serialize(shop.settings, object : TypeToken<MutableList<UUID>>() {}.type))
        obj.add("worldUUID", ctx.serialize(shop.block.world.uid, UUID::class.java))
        obj.add("owner", ctx.serialize(shop.owner, UUID::class.java))
        obj.add("bank", ctx.serialize(bank, ShopBank::class.java))
        obj.add("item", ctx.serialize(shop.item.serialize()))
        obj.add("price", JsonPrimitive(price))

        return obj
    }

    override fun deserialize(json: JsonElement?, p1: Type?, ctx: JsonDeserializationContext): T {
        val obj: JsonObject = json!!.asJsonObject

        return shopFactory.createShop(
                block = obj.get("blockXYZ").asLong.toLocation(ctx.deserialize<UUID>(obj.get("worldUUID"), UUID::class.java)?.toBukkitWorld()).block,
                shopType = ShopType.valueOf(obj.get("type").asString),
                allowedMutators = ctx.deserialize(obj.get("allowedMutators"), object : TypeToken<MutableList<UUID>>() {}.type),
                settings = ctx.deserialize(obj.get("settings"), object : TypeToken<MutableList<UUID>>() {}.type),
                owner = ctx.deserialize(obj.get("owner"), UUID::class.java),
                bank = ctx.deserialize(obj.get("bank"), ShopBank::class.java),
                itemStack = ItemStack.deserialize(ctx.deserialize(obj.get("item"), Map::class.java)),
                price = obj.get("price").asLong
        )
    }


}