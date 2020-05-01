package me.oriharel.playershops.serializers

import com.google.gson.*
import java.lang.reflect.Type
import java.util.*

class UUIDTypeAdapter : JsonSerializer<UUID>, JsonDeserializer<UUID> {
    override fun serialize(p0: UUID?, p1: Type?, p2: JsonSerializationContext?): JsonElement {
        val obj: JsonObject = JsonObject()

        obj.add("msBits", JsonPrimitive(p0?.mostSignificantBits))
        obj.add("lsBits", JsonPrimitive(p0?.leastSignificantBits))

        return obj
    }

    override fun deserialize(p0: JsonElement?, p1: Type?, p2: JsonDeserializationContext?): UUID {
        val obj: JsonObject = p0?.asJsonObject!!

        return UUID(obj.get("msBits")?.asLong!!, obj.get("lsBits")?.asLong!!)
    }
}