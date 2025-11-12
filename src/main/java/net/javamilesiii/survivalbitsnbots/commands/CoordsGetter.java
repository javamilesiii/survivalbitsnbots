package net.javamilesiii.survivalbitsnbots.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class CoordsGetter {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("coords")
                .then(Commands.argument("target", EntityArgument.player())
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                            CommandSourceStack executor = ctx.getSource();
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");

                            double x = target.getX();
                            double y = target.getY();
                            double z = target.getZ();
                            String dims = target.level().dimension().location().toString();
                            String coordsMessage = String.format("Player %s is at: X=%.1f Y=%.1f Z=%.1f in dimension %s",
                                    target.getName().getString(), x, y, z, dims);
                            executor.sendSystemMessage(Component.literal(coordsMessage).withStyle(ChatFormatting.GREEN));

                            return 1;
                        })
                )
        );
    }
}