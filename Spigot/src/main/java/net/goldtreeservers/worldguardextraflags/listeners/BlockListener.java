package net.goldtreeservers.worldguardextraflags.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.session.SessionManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;

import com.sk89q.worldguard.protection.flags.StateFlag.State;

import lombok.RequiredArgsConstructor;
import net.goldtreeservers.worldguardextraflags.flags.Flags;

import java.util.Set;

@RequiredArgsConstructor
public class BlockListener implements Listener
{
	private final WorldGuardPlugin worldGuardPlugin;
	private final RegionContainer regionContainer;
	private final SessionManager sessionManager;

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onBlockPlaceEvent(PlaceBlockEvent event) {
		Event.Result originalResult = event.getResult();
		Object cause = event.getCause().getRootCause();

		if (cause instanceof Player) {
			Player player = (Player)cause;
			LocalPlayer localPlayer = this.worldGuardPlugin.wrapPlayer(player);

			for (Block block : event.getBlocks()) {
				Material type = block.getType();
				if (type == Material.AIR)
					type = event.getEffectiveMaterial();

				ApplicableRegionSet regions = this.regionContainer.createQuery().getApplicableRegions(BukkitAdapter.adapt(block.getLocation()));

				Set<Material> state = regions.queryValue(localPlayer, Flags.ALLOW_BLOCK_PLACE);
				if (state != null && !state.contains(type)) {
					event.setResult(Event.Result.DENY);
				} else {
					Set<Material> state2 = regions.queryValue(localPlayer, Flags.DENY_BLOCK_PLACE);
					if (state2 != null && state2.contains(type)) {
						event.setResult(Event.Result.DENY);
					} else {
						event.setResult(originalResult);
					}
					return;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onBlockBreakEvent(BreakBlockEvent event) {
		Event.Result originalResult = event.getResult();
		Object cause = event.getCause().getRootCause();

		if (cause instanceof Player) {
			Player player = (Player)cause;
			LocalPlayer localPlayer = this.worldGuardPlugin.wrapPlayer(player);

			for (Block block : event.getBlocks()) {
				Material type = block.getType();

				if (this.sessionManager.hasBypass(localPlayer, (World) BukkitAdapter.adapt(block.getLocation()).getExtent()))
					return;

				if (type == Material.AIR)
					type = event.getEffectiveMaterial();

				ApplicableRegionSet regions = this.regionContainer.createQuery().getApplicableRegions(BukkitAdapter.adapt(block.getLocation()));

				Set<Material> state = regions.queryValue(localPlayer, Flags.ALLOW_BLOCK_BREAK);
				if (state != null && !state.contains(type)) {
					event.setResult(Event.Result.DENY);
				} else {
					Set<Material> state2 = regions.queryValue(localPlayer, Flags.DENY_BLOCK_BREAK);
					if (state2 != null && state2.contains(type)) {
						event.setResult(Event.Result.DENY);
					} else {
						event.setResult(originalResult);
					}
					return;
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityBlockFormEvent(EntityBlockFormEvent event)
	{
		BlockState newState = event.getNewState();
		if (newState.getType() == Material.FROSTED_ICE)
		{
			Location location = BukkitAdapter.adapt(newState.getLocation());

			LocalPlayer localPlayer;
			if (event.getEntity() instanceof Player player)
			{
				localPlayer = this.worldGuardPlugin.wrapPlayer(player);
				if (this.sessionManager.hasBypass(localPlayer, (World) location.getExtent()))
				{
					return;
				}
			}
			else
			{
				localPlayer = null;
			}

			if (this.regionContainer.createQuery().queryValue(location, localPlayer, Flags.FROSTWALKER) == State.DENY)
			{
				event.setCancelled(true);
			}
		}
	}
}
