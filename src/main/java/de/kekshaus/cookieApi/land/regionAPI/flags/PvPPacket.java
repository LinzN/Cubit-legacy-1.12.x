package de.kekshaus.cookieApi.land.regionAPI.flags;

import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.kekshaus.cookieApi.land.regionAPI.region.RegionData;

public class PvPPacket {

	public PvPPacket() {

	}

	private RegionData activateData(RegionData regionData) {
		regionData.praseWGRegion().setFlag(DefaultFlag.PVP, StateFlag.State.DENY);
		regionData.praseWGRegion().setFlag(DefaultFlag.POTION_SPLASH, StateFlag.State.DENY);
		return regionData;

	}

	private RegionData deactivateData(RegionData regionData) {
		regionData.praseWGRegion().setFlag(DefaultFlag.PVP, StateFlag.State.ALLOW);
		regionData.praseWGRegion().setFlag(DefaultFlag.POTION_SPLASH, StateFlag.State.ALLOW);
		return regionData;

	}

	public boolean getState(RegionData regionData) {
		if (regionData.praseWGRegion().getFlag(DefaultFlag.PVP) == StateFlag.State.DENY) {
			return true;
		}
		return false;
	}

	public RegionData switchState(RegionData regionData, boolean value) {
		if (value) {
			return activateData(regionData);
		} else {
			return deactivateData(regionData);
		}
	}

	public RegionData switchState(RegionData regionData) {
		if (getState(regionData)) {
			return switchState(regionData, false);
		} else {
			return switchState(regionData, true);
		}
	}
}
