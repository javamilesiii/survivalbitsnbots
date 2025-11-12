package net.javamilesiii.survivalbitsnbots.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class InvSee {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("invsee")
                .then(Commands.argument("target", EntityArgument.player())
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                            if (ctx.getSource().getPlayerOrException() instanceof ServerPlayer) {
                                ServerPlayer executor = ctx.getSource().getPlayerOrException();
                                SimpleContainer container = new SimpleContainer(target.getInventory().items.toArray(ItemStack[]::new));
                                executor.openMenu(new SimpleMenuProvider(
                                        (id, inv, ply) -> new ChestMenu(MenuType.GENERIC_9x4, id, inv, container, 4),
                                        Component.literal(target.getName().getString() + "'s Inventory")
                                ));
                            } else {
                                CommandSourceStack executor = ctx.getSource();
                                ItemStack[] items = target.getInventory().items.toArray(ItemStack[]::new);
                                executor.sendSystemMessage(Component.literal("You cannot see the inventory of " + target.getName().getString() + ".").withStyle(ChatFormatting.RED));
                                for (ItemStack item : items) {
                                    if (item != null) {
                                        executor.sendSystemMessage(Component.literal(" - " + item.getHoverName().getString()).withStyle(ChatFormatting.YELLOW));
                                    }
                                }
                            }
                            return 1;
                        })
                )
        );
    }
}
