/*
 * Copyright (C) 2018. MineGaming - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 *  You should have received a copy of the LGPLv3 license with
 *  this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.linzn.cubit.bukkit.command.universal.blockedit;

import de.linzn.cubit.bukkit.command.ICommand;
import de.linzn.cubit.bukkit.plugin.CubitBukkitPlugin;
import de.linzn.cubit.internal.cubitRegion.CubitType;
import de.linzn.cubit.internal.cubitRegion.region.CubitLand;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class EditResetUniversal implements ICommand {

    private CubitBukkitPlugin plugin;
    private String permNode;
    private CubitType type;
    private boolean isAdmin;
    private HashMap<UUID, Object[]> confirmTask;

    public EditResetUniversal(CubitBukkitPlugin plugin, String permNode, CubitType type, boolean isAdmin) {
        this.plugin = plugin;
        this.permNode = permNode;
        this.type = type;
        this.isAdmin = isAdmin;
        this.confirmTask = new HashMap<>();
    }

    @Override
    public boolean runCmd(final Command cmd, final CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            /* This is not possible from the server console */
            sender.sendMessage(plugin.getYamlManager().getLanguage().noConsoleMode);
            return true;
        }

        /* Build and get all variables */
        Player player = (Player) sender;

        if (!this.plugin.getYamlManager().getSettings().landUseSnapshots) {
            sender.sendMessage(plugin.getYamlManager().getLanguage().disabledSnapshots);
            return true;
        }

        if (!this.plugin.getBlockManager().getSnapshotHandler().hasValidAdapter()) {
            sender.sendMessage(plugin.getYamlManager().getLanguage().noValidWEAdapter);
            return true;
        }

        /* Permission Check */
        if (!player.hasPermission(this.permNode)) {
            sender.sendMessage(plugin.getYamlManager().getLanguage().errorNoPermission);
            return true;
        }

        Location loc = player.getLocation();

        /* Confirm task */
        /* Check if confirm task exist and confirm with "ok" and set old location */
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("ok") && confirmTask.containsKey(player.getUniqueId())) {
                loc = (Location) confirmTask.get(player.getUniqueId())[1];
            } else if (!args[1].equalsIgnoreCase("ok") && confirmTask.containsKey(player.getUniqueId())) {
                sender.sendMessage(this.plugin.getYamlManager().getLanguage().landEditConfirmTaskCancel);
                confirmTask.remove(player.getUniqueId());
                return true;
            }
        }

        /* Check if a confirm task is already exist and not "ok" */
        if (args.length < 2 && confirmTask.containsKey(player.getUniqueId())) {
            sender.sendMessage(this.plugin.getYamlManager().getLanguage().landEditConfirmTaskCancel);
            confirmTask.remove(player.getUniqueId());
            return true;
        }
        /* End confirm task */

        Chunk chunk = loc.getChunk();
        CubitLand cubitLand = plugin.getRegionManager().praseRegionData(loc.getWorld(), chunk.getX(),
                chunk.getZ());

        /*
         * Check if the player has permissions for this land or hat landadmin
         * permissions
         */

        if (!plugin.getRegionManager().isValidRegion(loc.getWorld(), chunk.getX(), chunk.getZ())) {
            sender.sendMessage(plugin.getYamlManager().getLanguage().errorNoLandFound);
            return true;
        }

        if (cubitLand.getCubitType() != type && type != CubitType.NOTYPE) {
            sender.sendMessage(
                    plugin.getYamlManager().getLanguage().errorNoValidLandFound.replace("{type}", type.toString()));
            return true;
        }

        if (!plugin.getRegionManager().hasLandPermission(cubitLand, player.getUniqueId()) && !this.isAdmin) {
            sender.sendMessage(plugin.getYamlManager().getLanguage().errorNoLandPermission.replace("{regionID}",
                    cubitLand.getLandName()));
            return true;
        }

        /* Add a confirm task */
        if (!this.confirmTask.containsKey(player.getUniqueId())) {
            sender.sendMessage(this.plugin.getYamlManager().getLanguage().landEditConfirmInfoReset);
            sender.sendMessage(this.plugin.getYamlManager().getLanguage().landEditConfirmTask.replace("{command}", cmd.getLabel()).replace("{subcommand}", args[0].toLowerCase()));
            int taskID = ThreadLocalRandom.current().nextInt(10, 50000 + 1);
            Object[] data = new Object[2];
            data[0] = taskID;
            /* custom data */
            data[1] = player.getLocation();
            /* end custom data */
            this.confirmTask.put(player.getUniqueId(), data);
            this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, () -> {
                if (confirmTask.containsKey(player.getUniqueId()) && (int) confirmTask.get(player.getUniqueId())[0] == taskID) {
                    sender.sendMessage(this.plugin.getYamlManager().getLanguage().landEditConfirmTaskCancel);
                    confirmTask.remove(player.getUniqueId());
                }
            }, 20L * 20);
            return true;
        } else {
            this.confirmTask.remove(player.getUniqueId());
        }
        /* end confirm task */

        if (!this.isAdmin) {
            if (!plugin.getYamlManager().getSettings().freeCubitLandWorld.contains(loc.getWorld().getName())) {
                double economyValue = plugin.getYamlManager().getSettings().landResetSnapshotPrice;
                if (!plugin.getVaultManager().hasEnougToBuy(player.getUniqueId(), economyValue)) {
                    sender.sendMessage(plugin.getYamlManager().getLanguage().notEnoughMoney.replace("{cost}",
                            "" + plugin.getVaultManager().formattingToEconomy(economyValue)));
                    return true;
                }

                if (!plugin.getVaultManager().transferMoney(player.getUniqueId(), null, economyValue)) {
                    /* If this task failed! This should never happen */
                    sender.sendMessage(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "RESET-ECONOMY"));
                    plugin.getLogger()
                            .warning(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "RESET-ECONOMY"));
                    return true;
                }
            }
        }

        if (!this.plugin.getBlockManager().getSnapshotHandler().resetChunk(chunk)) {
            /* If this task failed! This should never happen */
            sender.sendMessage(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "RESET-SNAPSHOT"));
            plugin.getLogger()
                    .warning(plugin.getYamlManager().getLanguage().errorInTask.replace("{error}", "RESET-SNAPSHOT"));
            return true;
        }

        sender.sendMessage(
                plugin.getYamlManager().getLanguage().resetSnapshot.replace("{regionID}", cubitLand.getLandName()));

        return true;

    }

}
