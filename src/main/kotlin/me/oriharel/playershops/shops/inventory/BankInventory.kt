package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.InventoryListener
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import fr.minuskube.inv.content.InventoryProvider
import me.oriharel.playershops.PlayerShops
import me.oriharel.playershops.shops.shop.Depositable
import me.oriharel.playershops.shops.shop.MoneyShop
import me.oriharel.playershops.shops.shop.ShopSetting
import me.oriharel.playershops.utilities.KItemStack
import me.oriharel.playershops.utilities.Utils.format
import me.oriharel.playershops.utilities.Utils.openWithContents
import me.swanis.mobcoins.MobCoinsAPI
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import kotlin.math.abs

class BankInventory(private val playerShops: PlayerShops) : InventoryProvider {

    override fun init(player: Player, contents: InventoryContents) {
        Bukkit.getScheduler().runTaskLater(playerShops, Runnable {
            val shop = InventoryConstants.ConstantUtilities.getShop(contents) as MoneyShop
            val bank = shop.bank!!
            val useMobCoins = InventoryConstants.ConstantUtilities.getUseMobCoins(contents)
                    ?: shop.settings?.contains(ShopSetting.USE_MOB_COINS)!!
            val useBank = InventoryConstants.ConstantUtilities.getUseBank(contents)
                    ?: shop.settings?.contains(ShopSetting.USE_INTERNAL_BANK)!!
            InventoryConstants.ConstantUtilities.setUseBank(contents, useBank)

            contents.set(0, 8, ClickableItem.of(
                    KItemStack(
                            material = if (useBank) Material.LIME_STAINED_GLASS_PANE else Material.RED_STAINED_GLASS_PANE,
                            displayName = if (useBank) "&6USING BANK" else "&6CLICK TO ENABLE BANK"
                    )
            ) {
                InventoryConstants.ConstantUtilities.setUseBank(contents, !useBank)
                init(player, contents)
            })

            contents.set(1, 2,
                    ClickableItem.of(
                            KItemStack(
                                    material = Material.MAGMA_CREAM,
                                    displayName = "&3WITHDRAW"
                            )) {
                        playerShops.createSignInput(
                                player,
                                "&6Amount: &9",
                                "&bEnter the of",
                                "&b" + if (useMobCoins) "Zen Coins" else "Money",
                                "&b to withdraw"
                        ) { _, strings ->
                            val split = strings[0].split("§9")
                            val re = "-\\d+|\\d+".toRegex()
                            val amount: Long? = if (split.size < 2) null else re.find(split[1])?.value?.toLongOrNull()

                            when {
                                amount == null -> {
                                    player.sendMessage("§b§l[INFO] §eYou must enter a valid number to withdraw!")
                                    return@createSignInput true
                                }
                                amount < 0 -> {
                                    player.sendMessage("§b§l[INFO] §eYou must enter a number above zero!")
                                    return@createSignInput true
                                }
                                bank.balance - amount < 0 -> {
                                    player.sendMessage("§b§l[INFO] §eYou don't have enough funds in the bank to withdraw that amount!")
                                    return@createSignInput true
                                }
                                else -> {
                                    bank.giveToAndWithdraw(amount, player.uniqueId)
                                    player.sendMessage("§b§l[INFO] §eYou've withdrawn ${amount.format()} from your shop's bank")
                                    PlayerShops.INSTANCE.shopManager.setPlayerShopBlockState(shop.block!!, shop)
                                    init(player, contents)
                                    return@createSignInput true
                                }
                            }
                        }
                    })

            // Setup to view of the amount of money in the bank
            contents.set(1, 4,
                    ClickableItem.empty(
                            KItemStack(
                                    material = Material.EMERALD_BLOCK,
                                    displayName = "&3BALANCE",
                                    lore = listOf(
                                            "&b${bank.balance.format()}&e " + if (useMobCoins) "Zen Coins" else "Money"
                                    )
                            )))

            contents.set(1, 6,
                    ClickableItem.of(
                            KItemStack(
                                    material = Material.PAPER,
                                    displayName = "&3DEPOSIT"
                            )) {
                        if (shop !is Depositable) {
                            player.sendMessage("§c§l[!] §eYou cannot deposit into this shop!")
                            return@of
                        }
                        playerShops.createSignInput(
                                player,
                                "&6Amount: &9",
                                "&bEnter the of",
                                "&b" + if (useMobCoins) "Zen Coins" else "Money",
                                "&b to deposit"
                        ) { _, strings ->
                            val split = strings[0].split("§9")
                            val re = "-\\d+|\\d+".toRegex()
                            val amount: Long? = if (split.size < 2) null else re.find(split[1])?.value?.toLongOrNull()
                            val userBal: Long =
                                    if (useMobCoins) MobCoinsAPI.getProfileManager().getProfile(player).mobCoins.toLong()
                                    else playerShops.economy.getBalance(player).toLong()

                            when {
                                amount == null -> {
                                    player.sendMessage("§b§l[INFO] §eYou must enter a valid number to deposit!")
                                    return@createSignInput true
                                }
                                amount < 0 -> {
                                    player.sendMessage("§b§l[INFO] §eYou must enter a number above zero!")
                                    return@createSignInput true
                                }
                                userBal - amount < 0 -> {
                                    player.sendMessage("§b§l[INFO] §eInsufficient funds! You are missing ${abs(userBal - amount).format()}")
                                    return@createSignInput true
                                }
                                else -> {
                                    bank.takeFromAndDeposit(amount, player.uniqueId)
                                    player.sendMessage("§b§l[INFO] §eYou've deposited ${amount.format()} into your shop's bank")
                                    PlayerShops.INSTANCE.shopManager.setPlayerShopBlockState(shop.block!!, shop)
                                    init(player, contents)
                                    return@createSignInput true
                                }
                            }
                        }
                    })
        }, 1L)


    }

    override fun update(player: Player, contents: InventoryContents) {

    }

    companion object {
        val INVENTORY: SmartInventory = SmartInventory.builder()
                .id(InventoryConstants.BankInventory.ID)
                .provider(BankInventory(PlayerShops.INSTANCE))
                .size(InventoryConstants.BankInventory.ROWS, InventoryConstants.BankInventory.COLUMNS)
                .closeable(InventoryConstants.BankInventory.CLOSEABLE)
                .title(InventoryConstants.BankInventory.TITLE)
                .listener(InventoryListener(InventoryCloseEvent::class.java) {
                    val contents = PlayerShops.INSTANCE.inventoryManager.getContents(it.player as Player)!!.get()
                    val shop = InventoryConstants.ConstantUtilities.getShop(contents)!!
                    val useBank = InventoryConstants.ConstantUtilities.getUseBank(contents)!!

                    if (useBank) {
                        shop.settings?.add(ShopSetting.USE_INTERNAL_BANK)
                    } else {
                        shop.settings?.remove(ShopSetting.USE_INTERNAL_BANK)
                    }

                    shop.buildHologram(PlayerShops.INSTANCE)
                    PlayerShops.INSTANCE.shopManager.setPlayerShopBlockState(shop.block!!, shop)
                })
                .manager(PlayerShops.INSTANCE.inventoryManager)
                .build()
    }
}