package net.javamilesiii.survivalbitsnbots.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class TPHERE {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tphere")
                // help argument
                .then(Commands.literal("help").executes(ctx -> {
                    Component usage = Component.literal("Usage: /tphere <player>").withStyle(ChatFormatting.AQUA);
                    Component perm = Component.literal("Permission: op level 2").withStyle(ChatFormatting.YELLOW);
                    ctx.getSource().sendSuccess((Supplier<Component>) usage, false);
                    ctx.getSource().sendSuccess((Supplier<Component>) perm, false);
                    return 1;
                }))

                .then(Commands.argument("target", EntityArgument.player())
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                            ServerPlayer executor = ctx.getSource().getPlayerOrException();
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");

                            if (executor.getUUID().equals(target.getUUID())) {
                                executor.sendSystemMessage(Component.literal("You cannot teleport yourself.").withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            target.teleportTo(executor.getX(), executor.getY(), executor.getZ());
                            executor.sendSystemMessage(Component.literal("Teleported " + target.getName().getString() + " to you.").withStyle(ChatFormatting.GREEN));
                            target.sendSystemMessage(Component.literal("You have been teleported to " + executor.getName().getString() + ".").withStyle(ChatFormatting.YELLOW));
                            return 1;
                        })
                )
        );
    }
}