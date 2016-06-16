package de.kekshaus.cubit.land.commandSuite.landCommands.main;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.kekshaus.cubit.land.Landplugin;
import de.kekshaus.cubit.land.api.regionAPI.IPacket;
import de.kekshaus.cubit.land.api.regionAPI.region.LandTypes;
import de.kekshaus.cubit.land.api.regionAPI.region.RegionData;
import de.kekshaus.cubit.land.commandSuite.ILandCmd;

public class FlagLand implements ILandCmd {

	private Landplugin plugin;
	private IPacket packet;
	private boolean isAdmin;
	private String permNode;

	public FlagLand(Landplugin plugin, IPacket packet, boolean isAdmin, String permNode) {
		this.plugin = plugin;
		this.packet = packet;
		this.isAdmin = isAdmin;
		this.permNode = permNode;
	}

	@Override
	public boolean runCmd(final CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			/* This is not possible from the server console */
			sender.sendMessage(plugin.getYamlManager().getLanguage().noConsoleMode);
			return true;
		}

		/* Build and get all variables */
		Player player = (Player) sender;

		/* Permission Check */
		if (!player.hasPermission(this.permNode)) {
			sender.sendMessage(plugin.getYamlManager().getLanguage().errorNoPermission);
			return true;
		}

		final Location loc = player.getLocation();
		final Chunk chunk = loc.getChunk();
		RegionData regionData = plugin.getLandManager().praseRegionData(loc.getWorld(), chunk.getX(), chunk.getZ());

		/*
		 * Check if the player has permissions for this land or hat landadmin
		 * permissions
		 */
		if (!plugin.getLandManager().isLand(loc.getWorld(), chunk.getX(), chunk.getZ())) {
			sender.sendMessage(plugin.getYamlManager().getLanguage().errorNoLandFound);
			return true;
		}

		if (regionData.getLandType() != LandTypes.WORLD) {
			sender.sendMessage(plugin.getYamlManager().getLanguage().errorNoValidLandFound.replace("{type}",
					LandTypes.WORLD.toString()));
			return true;
		}

		if (!plugin.getLandManager().hasLandPermission(regionData, player.getUniqueId()) && this.isAdmin == false) {
			sender.sendMessage(plugin.getYamlManager().getLanguage().errorNoLandPermission.replace("{regionID}",
					regionData.getRegionName()));
			return true;
		}

		/* Command based switch */
		if (args.length < 2) {
		} else if (args[1].toString().equalsIgnoreCase("on")) {

			packet.switchState(regionData, true, true);
			String stateString = plugin.getLandManager().getStringState(packet.getState(regionData));
			sender.sendMessage(plugin.getYamlManager().getLanguage().flagSwitchSuccess
					.replace("{flag}", packet.getPacketName()).replace("{value}", stateString));

			return true;
		} else if (args[1].toString().equalsIgnoreCase("off")) {

			packet.switchState(regionData, false, true);
			String stateString = plugin.getLandManager().getStringState(packet.getState(regionData));
			sender.sendMessage(plugin.getYamlManager().getLanguage().flagSwitchSuccess
					.replace("{flag}", packet.getPacketName()).replace("{value}", stateString));

			return true;
		}
		/* Switch flag-state to the opposite value */
		packet.switchState(regionData, true);
		String stateString = plugin.getLandManager().getStringState(packet.getState(regionData));
		sender.sendMessage(plugin.getYamlManager().getLanguage().flagSwitchSuccess
				.replace("{flag}", packet.getPacketName()).replace("{value}", stateString));

		return true;
	}

}
