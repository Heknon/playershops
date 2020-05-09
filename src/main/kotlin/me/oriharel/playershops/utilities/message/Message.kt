package me.oriharel.playershops.utilities.message

import me.oriharel.playershops.PlayerShops
import org.bukkit.ChatColor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.*
import kotlin.collections.HashMap

/**
 * Used to abstract logic behind sending and formatting messages with placeholders or color formatting
 */
class Message : Text {
    private var recipients: List<Player?>
    private var placeholders: MutableList<Placeholder>

    constructor(text: String, recipients: List<Player?>, placeholders: MutableList<Placeholder>) : super(text) {
        this.recipients = recipients
        this.placeholders = placeholders
    }

    constructor(text: String, recipient: Player?, vararg placeholder: Placeholder) : super(text) {
        recipients = listOf(recipient)
        placeholders = mutableListOf(*placeholder)
    }

    constructor(text: String, vararg placeholder: Placeholder) : super(text) {
        recipients = emptyList()
        placeholders = mutableListOf(*placeholder)
    }

    constructor(text: String, recipients: List<Player?>) : super(text) {
        this.recipients = recipients
        placeholders = ArrayList()
    }

    constructor(configName: String, configRoute: String, recipient: Player?, vararg placeholders: Placeholder) : super(handleConfigCache(configName, configRoute)) {
        recipients = listOf(recipient)
        this.placeholders = mutableListOf(*placeholders)
    }

    constructor(configName: String, configRoute: String, recipient: Player?, placeholders: MutableList<Placeholder>) : super(handleConfigCache(configName, configRoute)) {
        recipients = listOf(recipient)
        this.placeholders = placeholders
    }

    /**
     * Replace a placeholder that is already in the list of this messages placeholders. Used mostly for replacing already committed values.
     * Will still add the replacement placeholder if if the placeholder was not found
     * @param placeholder the placeholder string to find
     * @param replacement replacement
     * @return this
     */
    fun replacePlaceholder(placeholder: String?, replacement: Placeholder): Message {
        placeholders.removeIf { p: Placeholder? -> p?.placeholder.equals(placeholder, ignoreCase = true) }
        placeholders.add(replacement)
        return this
    }

    fun send(): Message {
        val textWithAppliedPlaceholders = ChatColor.translateAlternateColorCodes('&', appliedText)
        if (textWithAppliedPlaceholders.equals("disabled", ignoreCase = true)) return this
        for (recipient in recipients) {
            recipient!!.sendMessage(textWithAppliedPlaceholders)
        }
        return this
    }

    val appliedText: String
        get() = ChatColor.translateAlternateColorCodes('&', applyPlaceholders(text!!).text!!)

    private fun applyPlaceholders(text: String): Text {
        var txt = text
        for (placeholder in placeholders) {
            txt = applyPlaceholder(txt, placeholder)
        }
        return Text(txt)
    }

    private fun applyPlaceholder(text: String, placeholder: Placeholder?): String {
        return text.replace(placeholder?.placeholder!!, placeholder.replacement!!)
    }

    companion object {
        private val CONFIG_MESSAGE_CACHE: MutableMap<String, String?> = HashMap()
        private val CONFIG_CACHE: MutableMap<String, YamlConfiguration?> = HashMap()

        fun getConfigMessage(configName: String, routeName: String): String? =
                ChatColor.translateAlternateColorCodes('&', handleConfigCache(configName, routeName)!!)

        private fun handleConfigCache(configName: String, routeName: String): String? {
            val configLoad: YamlConfiguration? = CONFIG_CACHE[configName]
                    ?: CONFIG_CACHE.put(configName, YamlConfiguration.loadConfiguration(File(PlayerShops.INSTANCE.dataFolder, configName)))
                            .let { CONFIG_CACHE[configName] }
            val cacheKey = "$configName|$routeName"

            return CONFIG_MESSAGE_CACHE[cacheKey] ?: CONFIG_MESSAGE_CACHE.getOrPut(cacheKey, {
                configLoad?.getString(routeName)
            }).let { CONFIG_MESSAGE_CACHE[cacheKey] }
        }
    }
}