package net.goldtreeservers.worldguardextraflags.wg;

import java.util.HashSet;
import java.util.Set;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class WorldGuardUtils
{
	public static final String PREVENT_TELEPORT_LOOP_META = "WGEFP: TLP";
	
	@SuppressWarnings("unchecked")
	public static boolean hasNoTeleportLoop(Plugin plugin, Player player, Object location)
	{
		MetadataValue result = player.getMetadata(WorldGuardUtils.PREVENT_TELEPORT_LOOP_META).stream()
				.filter((p) -> p.getOwningPlugin().equals(plugin))
				.findFirst()
				.orElse(null);
		
		if (result == null)
		{
			result = new FixedMetadataValue(plugin, new HashSet<>());
			
			player.setMetadata(WorldGuardUtils.PREVENT_TELEPORT_LOOP_META, result);
			
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					player.removeMetadata(WorldGuardUtils.PREVENT_TELEPORT_LOOP_META, plugin);
				}
			}.runTask(plugin);
		}
		
		Set<Object> set = (Set<Object>)result.value();
		if (set.add(location))
		{
			return true;
		}
		
		return false;
	}

	public static void displayDenyMessage(LocalPlayer localPlayer, ApplicableRegionSet regions, String what)
	{
		String message = regions.queryValue(localPlayer, com.sk89q.worldguard.protection.flags.Flags.DENY_MESSAGE);
		localPlayer.printError(message.replace("%what%", what));
	}
}
