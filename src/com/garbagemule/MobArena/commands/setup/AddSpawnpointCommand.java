package com.garbagemule.MobArena.commands.setup;

import com.garbagemule.MobArena.framework.Arena;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.garbagemule.MobArena.*;
import com.garbagemule.MobArena.commands.*;
import com.garbagemule.MobArena.framework.ArenaMaster;

@CommandInfo(
    name    = "addspawn",
    pattern = "addspawn(point)?",
    usage   = "/ma addspawn <point name>",
    desc    = "add a new spawnpoint for the selected arena",
    permission = "mobarena.setup.spawnpoints"
)
public class AddSpawnpointCommand implements Command
{
    @Override
    public boolean execute(ArenaMaster am, CommandSender sender, String... args) {
        if (!Commands.isPlayer(sender)) {
            Messenger.tell(sender, Msg.MISC_NOT_FROM_CONSOLE);
            return true;
        }

        // Require a point name
        if (args.length != 1 || args[0].matches("^[a-zA-Z][a-zA-Z0-9]*$")) return false;
        
        // Cast the sender.
        Player p = (Player) sender;

        // Make a world check first
        Arena arena = am.getSelectedArena();
        World aw = arena.getWorld();
        World pw = p.getLocation().getWorld();
        boolean changeWorld = !aw.getName().equals(pw.getName());

        // Change worlds to make sure the region check doesn't fail
        if (changeWorld) arena.setWorld(pw);
        
        // Make sure we're inside the region
        if (!am.getSelectedArena().getRegion().contains(p.getLocation())) {
            if (arena.getRegion().isDefined()) {
                Messenger.tell(sender, "You must be inside the arena region!");
            } else {
                Messenger.tell(sender, "You must first set the region points p1 and p2");
            }

            // Restore the world reference in the arena 
            if (changeWorld) arena.setWorld(aw);
        } else {
            // Add the spawnpoint
            am.getSelectedArena().getRegion().addSpawn(args[0], p.getLocation());

            // Notify the player if world changed
            if (changeWorld) {
                Messenger.tell(sender, "Changed world of arena '" + arena.configName() +
                        "' from '" + aw.getName() +
                        "' to '" + pw.getName() + "'");
            }
            
            // Then notify about point set
            Messenger.tell(sender, "Spawnpoint '" + args[0] + "' added for arena '" + am.getSelectedArena().configName() + "'");
            arena.getRegion().checkData(am.getPlugin(), sender, false, false, false, true);
        }
        return true;
    }
}
