package me.oriharel.playershops.utilities

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.BlockPosition
import com.comphenix.protocol.wrappers.nbt.NbtCompound
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.stream.IntStream

class SignMenuFactory(private val plugin: Plugin) {
    private val inputReceivers: MutableMap<Player, Menu>

    init {
        inputReceivers = HashMap()
        listen()
    }

    fun newMenu(text: List<String>): Menu {
        Objects.requireNonNull(text, "text")
        return Menu(text)
    }

    private fun listen() {
        ProtocolLibrary.getProtocolManager().addPacketListener(object : PacketAdapter(plugin, PacketType.Play.Client.UPDATE_SIGN) {
            override fun onPacketReceiving(event: PacketEvent) {
                val player = event.player
                val menu = inputReceivers.remove(player) ?: return
                event.isCancelled = true
                val success = menu.response.invoke(player, event.packet.stringArrays.read(0))
                if (!success && menu.opensOnFail()) {
                    Bukkit.getScheduler().runTaskLater(plugin, Runnable { menu.open(player) }, 2L)
                }
                player.sendBlockChange(menu.position.toLocation(player.world), Material.AIR.createBlockData())
            }
        })
    }

    inner class Menu internal constructor(private val text: List<String>) {
        lateinit var response: (Player, Array<String>) -> Boolean
        private var reopenIfFail = false

        lateinit var position: BlockPosition
            private set

        fun opensOnFail(): Boolean {
            return reopenIfFail
        }

        fun reopenIfFail(): Menu {
            reopenIfFail = true
            return this
        }

        fun response(response: (Player, Array<String>) -> Boolean): Menu {
            this.response = response
            return this
        }

        fun open(player: Player) {
            val location = player.location
            position = BlockPosition(location.blockX, location.blockY - 5, location.blockZ)
            player.sendBlockChange(position.toLocation(location.world), Material.OAK_SIGN.createBlockData())
            val openSign = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR)
            val signData = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.TILE_ENTITY_DATA)
            openSign.blockPositionModifier.write(0, position)
            val signNBT = signData.nbtModifier.read(0) as NbtCompound
            IntStream.range(0, SIGN_LINES).forEach { line: Int -> signNBT.put("Text" + (line + 1), if (text.size > line) String.format(NBT_FORMAT, color(text[line])) else " ") }
            signNBT.put("x", position.x)
            signNBT.put("y", position.y)
            signNBT.put("z", position.z)
            signNBT.put("id", NBT_BLOCK_ID)
            signData.blockPositionModifier.write(0, position)
            signData.integers.write(0, ACTION_INDEX)
            signData.nbtModifier.write(0, signNBT)
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, signData)
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, openSign)
            } catch (exception: InvocationTargetException) {
                exception.printStackTrace()
            }
            inputReceivers[player] = this
        }

        private fun color(input: String): String {
            return ChatColor.translateAlternateColorCodes('&', input)
        }

    }

    companion object {
        private const val ACTION_INDEX = 9
        private const val SIGN_LINES = 4
        private const val NBT_FORMAT = "{\"text\":\"%s\"}"
        private const val NBT_BLOCK_ID = "minecraft:sign"
    }


}