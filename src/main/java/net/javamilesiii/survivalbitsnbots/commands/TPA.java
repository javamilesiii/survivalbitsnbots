package net.javamilesiii.survivalbitsnbots.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TPA {
    private static final Map<UUID, UUID> requests = new HashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tpa")
                // help argument
                .then(Commands.literal("help").executes(ctx -> {
                    ServerPlayer target = ctx.getSource().getPlayerOrException();
                    target.sendSystemMessage(Component.literal("Usage: /tpa <player> | /tpa accept | /tpa deny | /tpa cancel").withStyle(ChatFormatting.AQUA), false);
                    target.sendSystemMessage(Component.literal("Description: Send a teleport request to a player or manage incoming requests.").withStyle(ChatFormatting.YELLOW), false);
                    return 1;
                }))

                .then(Commands.literal("accept").executes(ctx -> {
                    ServerPlayer target = ctx.getSource().getPlayerOrException();
                    Optional<Map.Entry<UUID, UUID>> opt = requests.entrySet().stream()
                            .filter(e -> e.getValue().equals(target.getUUID()))
                            .findFirst();
                    if (opt.isEmpty()) {
                        target.sendSystemMessage(Component.literal("You have no pending teleport requests.").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    UUID requesterId = opt.get().getKey();
                    ServerPlayer requester = ctx.getSource().getServer().getPlayerList().getPlayer(requesterId);
                    if (requester == null) {
                        requests.remove(requesterId);
                        target.sendSystemMessage(Component.literal("Requesting player is no longer online.").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    requester.teleportTo(target.getX(), target.getY(), target.getZ());
                    requester.sendSystemMessage(Component.literal("Teleport request accepted.").withStyle(ChatFormatting.GREEN));
                    target.sendSystemMessage(Component.literal("You accepted the teleport request.").withStyle(ChatFormatting.GREEN));
                    requests.remove(requesterId);
                    return 1;
                }))

                .then(Commands.literal("deny").executes(ctx -> {
                    ServerPlayer target = ctx.getSource().getPlayerOrException();
                    Optional<UUID> key = requests.entrySet().stream()
                            .filter(e -> e.getValue().equals(target.getUUID()))
                            .map(Map.Entry::getKey)
                            .findFirst();
                    if (key.isEmpty()) {
                        target.sendSystemMessage(Component.literal("You have no pending teleport requests.").withStyle(ChatFormatting.RED));
                        return 0;
                    }
                    UUID requesterId = key.get();
                    ServerPlayer requester = ctx.getSource().getServer().getPlayerList().getPlayer(requesterId);
                    if (requester != null) {
                        requester.sendSystemMessage(Component.literal(target.getName().getString() + " denied your teleport request.").withStyle(ChatFormatting.YELLOW));
                    }
                    target.sendSystemMessage(Component.literal("Teleport request denied.").withStyle(ChatFormatting.YELLOW));
                    requests.remove(requesterId);
                    return 1;
                }))

                .then(Commands.literal("cancel").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    if (requests.containsKey(player.getUUID())) {
                        UUID targetId = requests.remove(player.getUUID());
                        ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayer(targetId);
                        if (target != null) {
                            target.sendSystemMessage(Component.literal(player.getName().getString() + " cancelled the teleport request.").withStyle(ChatFormatting.YELLOW));
                        }
                        player.sendSystemMessage(Component.literal("Teleport request cancelled.").withStyle(ChatFormatting.YELLOW));
                        return 1;
                    } else {
                        player.sendSystemMessage(Component.literal("You have no pending teleport request to cancel.").withStyle(ChatFormatting.RED));
                        return 0;
                    }
                }))

                .then(Commands.argument("target", EntityArgument.player()).executes(ctx -> {
                    ServerPlayer requester = ctx.getSource().getPlayerOrException();
                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");

                    if (requester.getUUID().equals(target.getUUID())) {
                        requester.sendSystemMessage(Component.literal("You cannot send a teleport request to yourself.").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    if (requests.containsKey(requester.getUUID())) {
                        requester.sendSystemMessage(Component.literal("You already have a pending teleport request.").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    requests.put(requester.getUUID(), target.getUUID());

                    MutableComponent accept = Component.literal("[Accept]")
                            .withStyle(style -> style.withColor(ChatFormatting.GREEN)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpa accept"))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Accept teleport request"))));
                    MutableComponent deny = Component.literal("[Deny]")
                            .withStyle(style -> style.withColor(ChatFormatting.RED)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpa deny"))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Deny teleport request"))));

                    MutableComponent message = Component.literal("You have received a teleport request from ")
                            .append(Component.literal(requester.getName().getString()).withStyle(ChatFormatting.AQUA))
                            .append(Component.literal(" "))
                            .append(accept)
                            .append(Component.literal(" "))
                            .append(deny);

                    target.sendSystemMessage(message);
                    requester.sendSystemMessage(Component.literal("Teleport request sent to " + target.getName().getString() + ".").withStyle(ChatFormatting.GREEN));
                    return 1;
                }))
        );
    }
}