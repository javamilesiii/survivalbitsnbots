package net.javamilesiii.survivalbitsnbots.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.javamilesiii.survivalbitsnbots.prefix.PlayerPrefix;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.function.Supplier;

public class SetTeamColor {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("teamColor")
                .requires(source -> source.hasPermission(2))
                // help argument
                .then(Commands.literal("help").executes(ctx -> {
                    Component usage = Component.literal("Usage: /teamColor <team> <level>").withStyle(ChatFormatting.AQUA);
                    ctx.getSource().sendSuccess((Supplier<Component>) usage, false);
                    return 1;
                }))

                .then(Commands.argument("team", StringArgumentType.word())
                        .then(Commands.argument("color", StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayer executor = ctx.getSource().getPlayerOrException();
                                    String team = StringArgumentType.getString(ctx, "team").toLowerCase();
                                    String colorName = StringArgumentType.getString(ctx, "color").toUpperCase();
                                    ChatFormatting color;
                                    try {
                                        color = ChatFormatting.valueOf(colorName);
                                    } catch (IllegalArgumentException e) {
                                        executor.sendSystemMessage(Component.literal("Invalid color.").withStyle(ChatFormatting.RED));
                                        return 0;
                                    }
                                    switch (team){
                                        case "admin":
                                            PlayerPrefix.setAdminColor(color);
                                            executor.sendSystemMessage(Component.literal("Set admin team color to " + colorName).withStyle(ChatFormatting.GREEN));
                                            break;
                                        case "creator":
                                            PlayerPrefix.setCreatorColor(color);
                                            executor.sendSystemMessage(Component.literal("Set creator team color to " + colorName).withStyle(ChatFormatting.GREEN));
                                            break;
                                        case "member":
                                            PlayerPrefix.setMemberColor(color);
                                            executor.sendSystemMessage(Component.literal("Set member team color to " + colorName).withStyle(ChatFormatting.GREEN));
                                            break;
                                        case "server":
                                            PlayerPrefix.setServerColor(color);
                                            executor.sendSystemMessage(Component.literal("Set server team color to " + colorName).withStyle(ChatFormatting.GREEN));
                                            break;
                                        default:
                                            executor.sendSystemMessage(Component.literal("Invalid team. Use 'admin', 'creator', 'member', or 'server'.").withStyle(ChatFormatting.RED));
                                            return 0;
                                    }
                                    PlayerPrefix.reassignAllPlayers(executor.getServer());
                                    return 1;
                                })
                        )
                )
        );
    }
}
