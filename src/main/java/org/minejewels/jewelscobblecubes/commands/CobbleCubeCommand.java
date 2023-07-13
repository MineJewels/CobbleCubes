package org.minejewels.jewelscobblecubes.commands;

import net.abyssdev.abysslib.command.AbyssCommand;
import net.abyssdev.abysslib.command.context.CommandContext;
import org.bukkit.command.CommandSender;
import org.eclipse.collections.api.factory.Lists;
import org.minejewels.jewelscobblecubes.JewelsCobbleCubes;

public class CobbleCubeCommand extends AbyssCommand<JewelsCobbleCubes, CommandSender> {

    public CobbleCubeCommand(final JewelsCobbleCubes plugin) {
        super(plugin, "cubes", "Base command for CobbleCubes!", Lists.immutable.of(
                "cobblecubes",
                "cube",
                "cobblecube",
                "jewelscubes"
        ).castToList(), CommandSender.class);
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {

        final CommandSender sender = context.getSender();

        if (!sender.hasPermission("cobblecubes.admin")) {
            this.plugin.getMessageCache().sendMessage(sender, "messages.no-permissions");
            return;
        }

        this.plugin.getMessageCache().sendMessage(sender, "messages.admin-help");
    }
}
