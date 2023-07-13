package org.minejewels.jewelscobblecubes.commands.sub;

import net.abyssdev.abysslib.command.AbyssSubCommand;
import net.abyssdev.abysslib.command.context.CommandContext;
import net.abyssdev.abysslib.nbt.NBTUtils;
import net.abyssdev.abysslib.placeholder.PlaceholderReplacer;
import net.abyssdev.abysslib.utils.Utils;
import net.abyssdev.abysslib.utils.WordUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.eclipse.collections.api.factory.Sets;
import org.minejewels.jewelscobblecubes.JewelsCobbleCubes;
import org.minejewels.jewelscobblecubes.cube.CobbleCube;

import java.util.Optional;
import java.util.Set;

public class CobbleCubeGiveCommand extends AbyssSubCommand<JewelsCobbleCubes> {

    public CobbleCubeGiveCommand(final JewelsCobbleCubes plugin) {
        super(plugin, 3, plugin.getMessageCache().getMessage("messages.invalid-arguments"));
    }

    @Override
    public Set<String> aliases() {
        return Sets.immutable.of(
                "give",
                "add",
                "giveplayer"
        ).castToSet();
    }

    @Override
    public void execute(final CommandContext<?> context) {

        final CommandSender sender = context.getSender();

        if (!sender.hasPermission("cobblecubes.admin")) {
            this.plugin.getMessageCache().sendMessage(sender, "messages.no-permissions");
            return;
        }

        final Player target = context.asPlayer(0);

        if (target == null) {
            this.getInvalid().send(sender);
            return;
        }

        final Optional<CobbleCube> optionalCube = this.plugin.getCubeRegistry().get(context.asString(1).toLowerCase());

        if (!optionalCube.isPresent()) {
            this.getInvalid().send(sender);
            return;
        }

        final CobbleCube cube = optionalCube.get();

        final int amount = context.asInt(2);

        if (amount <= 0) {
            this.getInvalid().send(sender);
            return;
        }

        ItemStack item = cube.getCubeItem().clone();
        item.setAmount(amount);

        if (target.getInventory().firstEmpty() == -1) {
            target.getWorld().dropItem(target.getLocation(), item);
            return;
        }

        item = NBTUtils.get().setString(item, "COBBLECUBE", cube.getType().toLowerCase());

        target.getInventory().addItem(item);

        final PlaceholderReplacer replacer = new PlaceholderReplacer()
                .addPlaceholder("%player%", target.getName())
                .addPlaceholder("%amount%", Utils.format(amount))
                .addPlaceholder("%type%", WordUtils.formatText(cube.getType()
                        .replace("_", " ")
                        .replace("-", " ")));

        this.plugin.getMessageCache().sendMessage(sender, "messages.given-cube", replacer);
        this.plugin.getMessageCache().sendMessage(target, "messages.received-cube", replacer);
    }
}
