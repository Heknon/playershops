package me.oriharel.playershops.shops.inventory

import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.InventoryListener
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import me.oriharel.playershops.PlayerShops
import me.oriharel.playershops.shops.shop.Depositable
import me.oriharel.playershops.shops.shop.PlayerShop
import me.oriharel.playershops.shops.shop.ShopSetting
import me.oriharel.playershops.utilities.KItemStack
import me.oriharel.playershops.utilities.Utils.format
import me.oriharel.playershops.utilities.Utils.sendMessage
import me.swanis.mobcoins.MobCoinsAPI
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack

class BankInventory(private val playerShops: PlayerShops) : NotUpdatableInventoryProvider {

    override fun init(player: Player, contents: InventoryContents) {
        val shop = InventoryConstants.ConstantUtilities.getShop(contents)!!
        val bank = shop.bank!!
        val useMobCoins = InventoryConstants.ConstantUtilities.getUseMobCoins(contents)
                ?: shop.settings.contains(ShopSetting.USE_MOB_COINS)
        val useBank = InventoryConstants.ConstantUtilities.getUseBank(contents)
                ?: shop.settings.contains(ShopSetting.USE_INTERNAL_BANK)
        InventoryConstants.ConstantUtilities.setUseBank(contents, useBank)

        contents.fill(ClickableItem.empty(InventoryConstants.Item.EMPTY_GRAY_STAINED_GLASS_PANE))

        contents.set(0, 8, ClickableItem.of(getUseBankItem(useBank)) {
            InventoryConstants.ConstantUtilities.setUseBank(contents, !useBank)
            init(player, contents)
        })

        contents.set(1, 2,
                ClickableItem.of(
                        KItemStack(
                                material = Material.MAGMA_CREAM,
                                displayName = "&3WITHDRAW"
                        )) {
                    handleWithdraw(player, shop, contents)
                })

        // Setup to view of the amount of money in the bank
        contents.set(1, 4,
                ClickableItem.empty(
                        KItemStack(
                                material = Material.EMERALD_BLOCK,
                                displayName = "&3BALANCE",
                                lore = listOf("&b${bank.balance.format()}&e " + if (useMobCoins) "Zen Coins" else "Money")
                        )))

        contents.set(1, 6,
                ClickableItem.of(
                        KItemStack(
                                material = Material.PAPER,
                                displayName = "&3DEPOSIT"
                        )) {
                    if (shop !is Depositable) {
                        player.sendMessage("messages.yml", "DepositAttemptToUnDepositableShop")
                        return@of
                    }
                    handleDeposit(player, shop, contents)
                })


    }

    private fun handleDeposit(player: Player, shop: PlayerShop, contents: InventoryContents) {
        playerShops.createSignInput(
                player,
                "&6Amount: &9",
                "&bEnter the of",
                "&b" + if (shop.settings.contains(ShopSetting.USE_MOB_COINS)) "Zen Coins" else "Money",
                "&b to deposit"
        ) { _, strings ->
            val amount: Long? = strings[0].toLongOrNull()
            val userBal: Long =
                    if (shop.settings.contains(ShopSetting.USE_MOB_COINS)) MobCoinsAPI.getProfileManager().getProfile(player).mobCoins.toLong()
                    else playerShops.economy.getBalance(player).toLong()

            when {
                amount == null -> {
                    player.sendMessage("messages.yml", "InvalidNumber")
                    return@createSignInput true
                }
                amount < 0 -> {
                    player.sendMessage("messages.yml", "InvalidNumber")
                    return@createSignInput true
                }
                userBal - amount < 0 -> {
                    player.sendMessage("messages.yml", "InsufficientFunds")
                    return@createSignInput true
                }
                else -> {
                    shop.bank!!.takeFromAndDeposit(amount, player.uniqueId)
                    player.sendMessage("messages.yml", "DepositToBank")
                    init(player, contents)
                    return@createSignInput true
                }
            }
        }
    }

    private fun handleWithdraw(player: Player, shop: PlayerShop, contents: InventoryContents) {
        playerShops.createSignInput(
                player,
                "",
                "Enter withdraw amount",
                "Withdrawing " + if (shop.settings.contains(ShopSetting.USE_MOB_COINS)) "Zen Coins" else "Money",
                ""
        ) { _, strings ->
            val amount = strings[0].toLongOrNull()

            when {
                amount == null -> {
                    player.sendMessage("messages.yml", "InvalidNumber")
                    return@createSignInput true
                }
                amount < 0 -> {
                    player.sendMessage("messages.yml", "InvalidNumber")
                    return@createSignInput true
                }
                shop.bank!!.balance - amount < 0 -> {
                    player.sendMessage("messages.yml", "InsufficientBankFunds", amount = amount.format())
                    return@createSignInput true
                }
                else -> {
                    shop.bank!!.giveToAndWithdraw(amount, player.uniqueId)
                    player.sendMessage("messages.yml", "WithdrawFromBank", amount = amount.format())
                    init(player, contents)
                    return@createSignInput true
                }
            }
        }
    }

    private fun getUseBankItem(useBank: Boolean): ItemStack {
        return KItemStack(
                material = if (useBank) Material.LIME_STAINED_GLASS_PANE else Material.RED_STAINED_GLASS_PANE,
                displayName = if (useBank) "&6USING BANK" else "&6CLICK TO ENABLE BANK"
        )
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
                        shop.settings.add(ShopSetting.USE_INTERNAL_BANK)
                    } else {
                        shop.settings.remove(ShopSetting.USE_INTERNAL_BANK)
                    }

                    shop.update()
                })
                .manager(PlayerShops.INSTANCE.inventoryManager)
                .build()
    }
}