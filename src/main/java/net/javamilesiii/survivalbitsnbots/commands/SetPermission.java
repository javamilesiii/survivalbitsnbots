package net.javamilesiii.survivalbitsnbots.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class SetPermission {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("perm")
                .requires(source -> source.hasPermission(4))
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("level", IntegerArgumentType.integer(0, 4))
                                .executes(ctx -> {
                                    ServerPlayer executor = ctx.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                    int level = IntegerArgumentType.getInteger(ctx, "level");

                                    return 1;
                                })
                        )
                )
        );
    }
}
