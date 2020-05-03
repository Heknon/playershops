package me.oriharel.playershops.utilities

import me.oriharel.playershops.utilities.Utils.modifyMeta
import me.oriharel.playershops.utilities.Utils.updateNBT
import net.minecraft.server.v1_15_R1.NBTBase
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class KItemStack @JvmOverloads constructor(
        material: Material,
        amount: Int = 1,
        displayName: String? = null,
        lore: List<String>? = null,
        metadataModifier: ((ItemMeta) -> Unit)? = null,
        nbtModifier: ((Map<String?, NBTBase?>) -> Unit)? = null
) : ItemStack(
        material,
        amount
) {

    init {
        this.modifyMeta {
            if (displayName != null) it.setDisplayName(displayName)
            if (lore != null) it.lore = lore
        }.modifyMeta(metadataModifier).updateNBT(nbtModifier)
    }


}