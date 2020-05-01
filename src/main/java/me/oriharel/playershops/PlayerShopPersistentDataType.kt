package me.oriharel.playershops

import me.oriharel.playershops.shops.shop.PlayerShop
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

class PlayerShopPersistentDataType<T : PlayerShop>(private val shopManager: PlayerShopManager) : PersistentDataType<String, T> {
    override fun getPrimitiveType(): Class<String> {
        return String::class.java
    }

    override fun getComplexType(): Class<T> {
        return PlayerShop::class.java as Class<T>
    }

    override fun toPrimitive(complex: T, context: PersistentDataAdapterContext): String {
        return shopManager.serializer.toJson(complex, complexType)
    }

    override fun fromPrimitive(primitive: String, context: PersistentDataAdapterContext): T {
        return shopManager.serializer.fromJson(primitive, complexType)
    }
}