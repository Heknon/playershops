package me.oriharel.playershops

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


class PlayerShops : JavaPlugin() {

    lateinit var economy: Economy
    private val configCache: MutableMap<String, YamlConfiguration> = HashMap()

    override fun onEnable() {
        if (!setupEconomy()) {
            this.logger.severe("Disabled due to no Vault dependency found!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        
    }

    private fun setupEconomy(): Boolean {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false
        }
        val rsp = server.servicesManager.getRegistration(Economy::class.java)
        this.economy = rsp!!.provider
        return true
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
}