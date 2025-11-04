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

@EventBusSubscriber(modid = Survivalbitsnbots.MODID, value = Dist.DEDICATED_SERVER)
public class PlayerPrefix {
    private static final String MOD_PREFIX = "survivalbitsnbots_";
    private static final String ADMIN_TEAM = MOD_PREFIX + "Admin";
    private static final String CREATOR_TEAM = MOD_PREFIX + "Creator";
    private static final String MEMBER_TEAM = MOD_PREFIX + "Member";
    private static final String SERVER_TEAM = MOD_PREFIX + "Server";

    private static int adminPermissionLevel = 4;
    private static int creatorPermissionLevel = 1;
    private static int memberPermissionLevel = 0;

    private static ChatFormatting adminColor = ChatFormatting.DARK_RED;
    private static ChatFormatting creatorColor = ChatFormatting.GOLD;
    private static ChatFormatting memberColor = ChatFormatting.DARK_AQUA;
    private static ChatFormatting serverColor = ChatFormatting.DARK_GRAY;

    private static final Component ADMIN_PREFIX = Component.literal("[Admin] ").withStyle(adminColor);
    private static final Component CREATOR_PREFIX = Component.literal("[Creator] ").withStyle(creatorColor);
    private static final Component MEMBER_PREFIX = Component.literal("[Member] ").withStyle(memberColor);
    private static final Component SERVER_PREFIX = Component.literal("[Server] ").withStyle(serverColor);

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
        if (player.hasPermissions(adminPermissionLevel)) return ADMIN_TEAM;
        if (player.hasPermissions(creatorPermissionLevel)) return CREATOR_TEAM;
        if (player.hasPermissions(memberPermissionLevel)) return MEMBER_TEAM;

        return SERVER_TEAM;
    }

    private static Component getTeamPrefixComponent(String team) {
        return switch (team) {
            case ADMIN_TEAM -> ADMIN_PREFIX;
            case CREATOR_TEAM -> CREATOR_PREFIX;
            case MEMBER_TEAM -> MEMBER_PREFIX;
            default -> SERVER_PREFIX;
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

        Component message = Component.empty().append(prefix).append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.WHITE)).append(Component.literal(": ")).append(Component.literal(event.getMessage().getString()));

        player.server.getPlayerList().broadcastSystemMessage(message, false);
    }

    public static void reassignAllPlayers(net.minecraft.server.MinecraftServer server) {
        if (server == null) return;
        server.getPlayerList().getPlayers().forEach(player -> {
            if (player instanceof ServerPlayer sp) {
                assignTeam(sp);
            }
        });
    }

    public static void setAdminPermissionLevel(int level) {
        adminPermissionLevel = level;
    }

    public static void setCreatorPermissionLevel(int level) {
        creatorPermissionLevel = level;
    }

    public static void setMemberPermissionLevel(int level) {
        memberPermissionLevel = level;
    }

    public static void setAdminColor(ChatFormatting color) {
        adminColor = color;
    }

    public static void setCreatorColor(ChatFormatting color) {
        creatorColor = color;
    }

    public static void setMemberColor(ChatFormatting color) {
        memberColor = color;
    }

    public static void setServerColor(ChatFormatting color) {
        serverColor = color;
    }
}