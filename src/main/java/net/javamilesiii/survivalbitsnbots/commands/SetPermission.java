package net.javamilesiii.survivalbitsnbots.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class SetPermission {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("perm")
                .requires(source -> source.hasPermission(4))
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("level", IntegerArgumentType.integer(0, 4))
                                .executes(ctx -> {
                                    CommandSourceStack executor = ctx.getSource();
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                    int level = IntegerArgumentType.getInteger(ctx, "level");

                                    return 1;
                                })
                        )
                )
        );
    }
}
