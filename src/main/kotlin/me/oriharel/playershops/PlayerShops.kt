package me.oriharel.playershops

import co.aikar.commands.BukkitCommandCompletionContext
import co.aikar.commands.BukkitCommandManager
import fr.minuskube.inv.InventoryManager
import me.oriharel.playershops.shops.shop.ShopType
import me.oriharel.playershops.utilities.SignMenuFactory
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.stream.Collectors


class PlayerShops : JavaPlugin() {

    lateinit var economy: Economy
    lateinit var inventoryManager: InventoryManager
    lateinit var shopManager: PlayerShopManager
    lateinit var signMenuFactory: SignMenuFactory
    lateinit var commandManager: BukkitCommandManager
    private val configCache: MutableMap<String, YamlConfiguration> = HashMap()

    override fun onLoad() {
        INSTANCE = this
    }

    override fun onEnable() {
        if (!setupEconomy()) {
            this.logger.severe("Disabled due to no Vault dependency found!")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        signMenuFactory = SignMenuFactory(this)
        shopManager = PlayerShopManager(this)
        inventoryManager = InventoryManager(this)
        inventoryManager.init()

    }

    private fun setupEconomy(): Boolean {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false
        }
        val rsp = server.servicesManager.getRegistration(Economy::class.java)
        this.economy = rsp!!.provider
        return true
    }

    fun createSignInput(target: Player, vararg defaultLines: String, response: (Player, Array<String>) -> Boolean) {
        val lines = mutableListOf(*defaultLines)

        if (defaultLines.size < 4) {
            for (i in defaultLines.size..4) {
                lines.add("")
            }
        } else if (defaultLines.size > 4) {
            while (lines.size != 4) {
                lines.removeAt(lines.lastIndex)
            }
        }

        signMenuFactory
                .newMenu(lines)
                .reopenIfFail()
                .response(response)
                .open(target)
    }

    fun getConfig(name: String): YamlConfiguration? {
        return if (configCache.containsKey(name)) {
            configCache[name]
        } else {
            val config: YamlConfiguration = YamlConfiguration.loadConfiguration(File(dataFolder, name))
            configCache[name] = config
            config
        }
    }

    private fun setupCommandManager() {
        commandManager = BukkitCommandManager(this)
        commandManager.commandCompletions.registerCompletion("shopTypes") {
            ShopType.values().map { v -> v.name }
        }
        commandManager.commandCompletions.registerCompletion("boolean") {
            mutableListOf("true", "false")
        }
        commandManager.registerCommand(ShopCommands(this))
    }

    companion object {
        lateinit var INSTANCE: PlayerShops
    }
}