package me.oriharel.playershops.serializers

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.lang.reflect.Type


class ItemStackTypeAdapter : JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {
    override fun serialize(item: ItemStack?, type: Type, ctx: JsonSerializationContext): JsonElement {
        val obj = JsonObject()
        if (item == null) return obj
        obj.add("type", JsonPrimitive(item.type.toString()))
        obj.add("amount", JsonPrimitive(item.amount))
        obj.add("meta", if (item.itemMeta == null) null else ctx.serialize(item.itemMeta!!.serialize(), object : TypeToken<Map<String, Any>>() {}.type))
        return obj

    }

    override fun deserialize(element: JsonElement, type: Type, ctx: JsonDeserializationContext): ItemStack {
        val obj = element.asJsonObject
        val item = ItemStack(Material.getMaterial(obj.get("type").asString)!!, obj.get("amount").asInt)
        item.itemMeta =
                Class.forName("org.bukkit.craftbukkit.v1_15_R1.inventory.CraftMetaItem").classes[0]
                        .getMethod("deserialize", object : TypeToken<Map<String, Any>>() {}.rawType)
                        .invoke(null, ctx.deserialize(
                                obj.get("meta"),
                                object : TypeToken<Map<String, Any>>() {}.type
                        )) as ItemMeta
        return item

    }
}