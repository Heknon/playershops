package me.oriharel.playershops.serializers

import com.google.gson.*
import me.oriharel.playershops.shops.bank.ShopBank
import me.oriharel.playershops.shops.bank.VaultShopBank
import me.oriharel.playershops.shops.bank.ZenCoinShopBank
import net.milkbowl.vault.economy.Economy
import java.lang.reflect.Type

class ShopBankTypeAdapter<T : ShopBank>(private val economy: Economy) : JsonSerializer<T>, JsonDeserializer<T> {
    override fun serialize(bank: T, p1: Type?, ctx: JsonSerializationContext?): JsonElement {
        val obj: JsonObject = JsonObject()

        obj.add("balance", JsonPrimitive(bank.balance))
        obj.add("type", JsonPrimitive(bank::class.simpleName))

        return obj
    }

    override fun deserialize(elem: JsonElement?, p1: Type?, ctx: JsonDeserializationContext): T {
        val obj: JsonObject = elem?.asJsonObject!!

        val balance: Long = obj.get("balance").asLong

        return when (val type: String = obj.get("type").asString) {
            "VaultShopBank" -> VaultShopBank(balance, economy)
            "ZenCoinShopBank" -> ZenCoinShopBank(balance)
            else -> throw RuntimeException("Couldn't find type of ShopBank with simple name of $type.")
        } as T
    }
}