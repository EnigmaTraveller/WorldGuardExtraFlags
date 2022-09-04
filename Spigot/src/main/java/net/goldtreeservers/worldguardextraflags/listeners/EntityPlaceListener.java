package net.goldtreeservers.worldguardextraflags.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.session.SessionManager;
import lombok.RequiredArgsConstructor;
import net.goldtreeservers.worldguardextraflags.flags.Flags;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;

import java.util.Set;

import static net.goldtreeservers.worldguardextraflags.wg.WorldGuardUtils.displayDenyMessage;

@RequiredArgsConstructor
public class EntityPlaceListener implements Listener {
    private final WorldGuardPlugin worldGuardPlugin;
    private final RegionContainer regionContainer;
    private final SessionManager sessionManager;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityPlacement(EntityPlaceEvent e) {
        handleEntityEvent(e.getPlayer(), e, e.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityHang(HangingPlaceEvent e) {
        handleEntityEvent(e.getPlayer(), e, e.getEntity());
    }

    private void handleEntityEvent(Player playerWhoPlaced, Cancellable event, Entity entity) {
        LocalPlayer localPlayer = this.worldGuardPlugin.wrapPlayer(playerWhoPlaced);
        ApplicableRegionSet regions = this.regionContainer.createQuery().getApplicableRegions(BukkitAdapter.adapt(entity.getLocation()));
        if (this.sessionManager.hasBypass(localPlayer, (World) BukkitAdapter.adapt(entity.getLocation()).getExtent()))
            return;

        EntityType entityType = BukkitAdapter.adapt(entity.getType());
        Set<EntityType> allowedEntityPlacements = regions.queryValue(localPlayer, Flags.ALLOW_ENTITY_PLACE);
        if (allowedEntityPlacements != null && !allowedEntityPlacements.contains(entityType)) {
            event.setCancelled(true);
            playerWhoPlaced.updateInventory();
            displayDenyMessage(localPlayer, regions, "place an " + entity.getName());
            return;
        }

        Set<EntityType> deniedEntityPlacements = regions.queryValue(localPlayer, Flags.DENY_ENTITY_PLACE);
        if (deniedEntityPlacements != null && deniedEntityPlacements.contains(entity.getType())) {
            event.setCancelled(true);
            playerWhoPlaced.updateInventory();
            displayDenyMessage(localPlayer, regions, "place an " + entity.getName());
            return;
        }
    }

}
