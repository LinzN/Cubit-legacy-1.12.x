package de.kekshaus.cookieApi.land;

import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import de.kekshaus.cookieApi.land.api.blockAPI.BlockManager;
import de.kekshaus.cookieApi.land.api.particleAPI.ParticleManager;
import de.kekshaus.cookieApi.land.api.regionAPI.LandManager;
import de.kekshaus.cookieApi.land.api.sqlAPI.SqlManager;
import de.kekshaus.cookieApi.land.commandSuite.SetupCommands;

public class Landplugin extends JavaPlugin {

	private static Landplugin inst;
	private WorldGuardPlugin wgPl;
	private WorldEdit wePl;
	private LandManager landMrg;
	private BlockManager blockMrg;
	private ParticleManager particleMrg;
	private SqlManager sqlMrg;

	public void onEnable() {
		inst = this;
		wgPl = WorldGuardPlugin.inst();
		wePl = WorldEdit.getInstance();
		setupManagers();
		if (!sqlMrg.link()) {
			this.setEnabled(false);
		}
		new SetupCommands(this);
	}

	public void onDisable() {
	}

	private void setupManagers() {
		sqlMrg = new SqlManager(this);
		landMrg = new LandManager(this);
		blockMrg = new BlockManager(this);
		particleMrg = new ParticleManager(this);

	}

	public static Landplugin inst() {
		return inst;
	}

	public LandManager getLandManager() {
		return landMrg;
	}

	public BlockManager getBlockManager() {
		return blockMrg;
	}

	public ParticleManager getParticleManager() {
		return particleMrg;
	}

	public SqlManager getSqlManager() {
		return sqlMrg;
	}

	public WorldGuardPlugin getWorldGuardPlugin() {
		return this.wgPl;
	}

	public WorldEdit getWorldEdit() {
		return this.wePl;
	}

}
