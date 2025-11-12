package net.javamilesiii.survivalbitsnbots.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.javamilesiii.survivalbitsnbots.prefix.PlayerPrefix;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class PrefixCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("prefix")
                .requires(source -> source.hasPermission(4))
                .then(Commands.argument("type", StringArgumentType.word())
                        .then(Commands.argument("level", IntegerArgumentType.integer(0, 4))
                                .executes(ctx -> {
                                    CommandSourceStack executor = ctx.getSource();
                                    String type = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "type");
                                    int level = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "level");
                                    switch (type) {
                                        case "admin":
                                            PlayerPrefix.setAdminPermissionLevel(level);
                                            executor.sendSystemMessage(Component.literal("Set admin permission level to " + level).withStyle(ChatFormatting.GREEN));
                                            break;
                                        case "creator":
                                            PlayerPrefix.setCreatorPermissionLevel(level);
                                            executor.sendSystemMessage(Component.literal("Set creator permission level to " + level).withStyle(ChatFormatting.GREEN));
                                            break;
                                        case "member":
                                            PlayerPrefix.setMemberPermissionLevel(level);
                                            executor.sendSystemMessage(Component.literal("Set member permission level to " + level).withStyle(ChatFormatting.GREEN));
                                            break;
                                        default:
                                            executor.sendSystemMessage(Component.literal("Invalid type. Use 'admin', 'creator', or 'member'.").withStyle(ChatFormatting.RED));
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
