package net.javamilesiii.survivalbitsnbots.prefix;

import net.javamilesiii.survivalbitsnbots.Survivalbitsnbots;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Objects;

/**
 * Server-seitige Team/Prefix-Verwaltung.
 * - erstellt/aktualisiert Teams auf dem Server-Scoreboard
 * - weist Spieler beim Login dem richtigen Team zu (sichtbar in Tab & Nametag)
 * - formatiert Chat (Event abfangen und eigene Broadcast)
 */
@EventBusSubscriber(modid = Survivalbitsnbots.MODID, value = Dist.DEDICATED_SERVER)
public class PlayerPrefix {
    private static final String MOD_PREFIX = "survivalbitsnbots_";
    private static final String ADMIN_TEAM = MOD_PREFIX + "Admin";
    private static final String CREATOR_TEAM = MOD_PREFIX + "Creator";
    private static final String MEMBER_TEAM = MOD_PREFIX + "Member";

    private static final Component ADMIN_PREFIX = Component.literal("[Admin] ").withStyle(ChatFormatting.DARK_RED);
    private static final Component CREATOR_PREFIX = Component.literal("[Creator] ").withStyle(ChatFormatting.GOLD);
    private static final Component MEMBER_PREFIX = Component.literal("[Member] ").withStyle(ChatFormatting.DARK_AQUA);

    private static void ensureTeam(Scoreboard scoreboard, String name, Component prefix) {
        if (scoreboard == null || name == null) return;
        PlayerTeam team = scoreboard.getPlayerTeam(name);
        if (team == null) {
            team = scoreboard.addPlayerTeam(name);
            Survivalbitsnbots.LOGGER.debug("Created team {}", name);
        }
        Component current = team.getPlayerPrefix();
        String cur = current.getString();
        String want = prefix == null ? "" : prefix.getString();
        if (!Objects.equals(cur, want)) {
            team.setPlayerPrefix(prefix);
            Survivalbitsnbots.LOGGER.debug("Set prefix for team {} -> {}", name, want);
        }
    }

    private static void setupTeams(Scoreboard scoreboard) {
        if (scoreboard == null) return;
        ensureTeam(scoreboard, ADMIN_TEAM, ADMIN_PREFIX);
        ensureTeam(scoreboard, CREATOR_TEAM, CREATOR_PREFIX);
        ensureTeam(scoreboard, MEMBER_TEAM, MEMBER_PREFIX);
    }

    private static String getPlayerTeamName(ServerPlayer player) {
        // Achtung: hasPermissions(int) prüft OP-Level, nicht Permission-Plugins.
        if (player.hasPermissions(4)) return ADMIN_TEAM;
        if (player.hasPermissions(2)) return CREATOR_TEAM;
        return MEMBER_TEAM;
    }

    private static Component getTeamPrefixComponent(String team) {
        return switch (team) {
            case ADMIN_TEAM -> ADMIN_PREFIX;
            case CREATOR_TEAM -> CREATOR_PREFIX;
            default -> MEMBER_PREFIX;
        };
    }

    private static void assignTeam(ServerPlayer player) {
        if (player == null) return;
        Scoreboard scoreboard = player.server.getScoreboard();
        setupTeams(scoreboard);

        String target = getPlayerTeamName(player);
        PlayerTeam newTeam = scoreboard.getPlayerTeam(target);
        if (newTeam == null) return;

        String entry = player.getScoreboardName(); // use scoreboard name for consistency
        PlayerTeam current = scoreboard.getPlayersTeam(entry);
        if (current == newTeam) return;

        if (current != null) {
            scoreboard.removePlayerFromTeam(entry, current);
        }
        scoreboard.addPlayerToTeam(entry, newTeam);
        Survivalbitsnbots.LOGGER.debug("Assigned {} to team {}", entry, target);
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            assignTeam(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();

        String teamName = getPlayerTeamName(player);
        Component prefix = getTeamPrefixComponent(teamName);

        // Cancel default behavior and broadcast a component-based message
        event.setCanceled(true);

        Component message = Component.empty().append(prefix).append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.WHITE)).append(Component.literal(": ")).append(Component.literal(event.getMessage().getString()).withStyle(ChatFormatting.GRAY));

        player.server.getPlayerList().broadcastSystemMessage(message, false);
    }
}