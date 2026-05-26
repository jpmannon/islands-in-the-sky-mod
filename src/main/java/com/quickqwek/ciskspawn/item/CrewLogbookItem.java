package com.quickqwek.ciskspawn.item;

import com.quickqwek.ciskspawn.entity.AzerionEntity;
import com.quickqwek.ciskspawn.entity.GeeraEntity;
import com.quickqwek.ciskspawn.entity.JoelleEntity;
import com.quickqwek.ciskspawn.entity.RamoneEntity;
import com.quickqwek.ciskspawn.entity.ScoriaEntity;
import com.quickqwek.ciskspawn.entity.StorykeeperEntity;
import com.quickqwek.ciskspawn.entity.VelhoEntity;
import com.quickqwek.ciskspawn.network.MortimerDialoguePayload;
import com.quickqwek.ciskspawn.server.PlayerStatsTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrewLogbookItem extends Item {
    private static final String TAB_KEY = "logbook_tab";

    public CrewLogbookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            openLogbook(serverPlayer);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static void handleAction(ServerPlayer player, String action) {
        if (!(player.getMainHandItem().getItem() instanceof CrewLogbookItem)
                && !(player.getOffhandItem().getItem() instanceof CrewLogbookItem)) {
            return;
        }
        CompoundTag data = player.getPersistentData();
        switch (action) {
            case "log_crew" -> data.putInt(TAB_KEY, 0);
            case "log_clues" -> data.putInt(TAB_KEY, 1);
            case "log_systems" -> data.putInt(TAB_KEY, 2);
            case "log_notes" -> data.putInt(TAB_KEY, 3);
            default -> {
                return;
            }
        }
        openLogbook(player);
    }

    private static void openLogbook(ServerPlayer player) {
        int tab = player.getPersistentData().getInt(TAB_KEY);
        PacketDistributor.sendToPlayer(player, new MortimerDialoguePayload(
                -1,
                "Crew Logbook",
                buildTabText(player, tab),
                "Crew",
                "Clues",
                buildFooter(tab)
        ));
    }

    private static String buildTabText(ServerPlayer player, int tab) {
        List<CrewEntry> crew = findNearbyCrew(player);
        return switch (tab) {
            case 1 -> buildCluesTab(player, crew);
            case 2 -> buildSystemsTab(player);
            case 3 -> buildNotesTab(player, crew);
            default -> buildCrewTab(player, crew);
        };
    }

    private static List<CrewEntry> findNearbyCrew(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        List<CrewEntry> entries = new ArrayList<>();
        List<Entity> nearby = player.level().getEntitiesOfClass(
                Entity.class,
                player.getBoundingBox().inflate(64.0D),
                CrewLogbookItem::isCrewEntity
        );
        for (Entity entity : nearby) {
            CrewEntry entry = entryFor(entity, playerUUID);
            if (entry != null) entries.add(entry);
        }
        entries.sort((a, b) -> a.name.compareTo(b.name));
        return entries;
    }

    private static boolean isCrewEntity(Entity entity) {
        return entity instanceof StorykeeperEntity
                || entity instanceof GeeraEntity
                || entity instanceof ScoriaEntity
                || entity instanceof AzerionEntity
                || entity instanceof JoelleEntity
                || entity instanceof RamoneEntity
                || entity instanceof VelhoEntity;
    }

    private static CrewEntry entryFor(Entity entity, UUID playerUUID) {
        if (entity instanceof StorykeeperEntity mortimer) return new CrewEntry("Mortimer", "Aeromancer mentor", mortimer.getTrustForPlayer(playerUUID));
        if (entity instanceof GeeraEntity geera) return new CrewEntry("Geera", "Fisherwoman and dock trader", geera.getTrustForPlayer(playerUUID));
        if (entity instanceof ScoriaEntity scoria) return new CrewEntry("Scoria", "Engineer pretending to be a banker", scoria.getTrustForPlayer(playerUUID));
        if (entity instanceof AzerionEntity azerion) return new CrewEntry("Azerion", "CBC artillery instructor", azerion.getTrustForPlayer(playerUUID));
        if (entity instanceof JoelleEntity joelle) return new CrewEntry("Joelle", "Chef and restaurant keeper", joelle.getTrustForPlayer(playerUUID));
        if (entity instanceof RamoneEntity ramone) return new CrewEntry("Ramone", "Gardener and retired skyfarer", ramone.getTrustForPlayer(playerUUID));
        if (entity instanceof VelhoEntity velho) return new CrewEntry("Velho", "Enchanter and craftsman", velho.getTrustForPlayer(playerUUID));
        return null;
    }

    private static String buildCrewTab(ServerPlayer player, List<CrewEntry> crew) {
        String shipName = PlayerStatsTracker.getShipName(player);
        StringBuilder text = new StringBuilder("CREW\n\n");
        text.append("Nearby crew within 64 blocks:\n");
        if (crew.isEmpty()) {
            text.append("No crew nearby. The book feels a little too quiet.\n");
        } else {
            for (CrewEntry entry : crew) {
                text.append(entry.name)
                        .append(": ")
                        .append(entry.role)
                        .append(". Trust ")
                        .append(entry.trust)
                        .append("/100 (")
                        .append(tier(entry.trust))
                        .append(").\n");
            }
        }
        text.append("\nPlayer record:\n");
        text.append("Deaths: ").append(PlayerStatsTracker.getDeaths(player)).append("\n");
        text.append("Islands discovered: ").append(PlayerStatsTracker.getIslandsDiscovered(player)).append("\n");
        text.append("Ship: ").append(shipName == null ? "Unnamed" : shipName).append("\n");
        return text.toString();
    }

    private static String buildCluesTab(ServerPlayer player, List<CrewEntry> crew) {
        int islands = PlayerStatsTracker.getIslandsDiscovered(player);
        StringBuilder text = new StringBuilder("CLUES\n\n");
        text.append("Scoria's banker story is still suspicious. If his trust is rising, the trail points toward propulsion work, levitite stabilizers, and a reveal meant for Mortimer.\n\n");
        text.append("Azerion has maintenance records and preferences. Velho seems to know more about both than he says.\n\n");
        text.append("Exploration record: ").append(islands).append(" islands discovered. ");
        if (islands >= 15) text.append("The book marks your routes as experienced skyfarer territory.");
        else if (islands >= 5) text.append("The margins are starting to fill with useful routes.");
        else text.append("Most of the map is still waiting.");
        text.append("\n\nNearby clue sources: ");
        text.append(crew.isEmpty() ? "none nearby." : namesOnly(crew));
        return text.toString();
    }

    private static String buildSystemsTab(ServerPlayer player) {
        return "SYSTEMS\n\n"
                + "Trust tiers: 0-14 Stranger, 15-34 Acquaintance, 35-54 Friend, 55-79 Trusted, 80+ Crew or Family.\n\n"
                + "Known player stats:\n"
                + "Deaths: " + PlayerStatsTracker.getDeaths(player) + "\n"
                + "Islands discovered: " + PlayerStatsTracker.getIslandsDiscovered(player) + "\n"
                + "Ship name: " + (PlayerStatsTracker.getShipName(player) == null ? "Unnamed" : PlayerStatsTracker.getShipName(player)) + "\n\n"
                + "Training systems: Geera handles fishing and dock economy. Azerion handles cannon discipline. Velho handles enchanting and magical systems. Joelle and Ramone anchor the restaurant and garden loop.";
    }

    private static String buildNotesTab(ServerPlayer player, List<CrewEntry> crew) {
        StringBuilder text = new StringBuilder("NOTES\n\n");
        text.append("Current ship: ").append(PlayerStatsTracker.getShipName(player) == null ? "Unnamed" : PlayerStatsTracker.getShipName(player)).append("\n");
        text.append("Survival record: ").append(PlayerStatsTracker.getDeaths(player)).append(" deaths logged.\n");
        text.append("Exploration record: ").append(PlayerStatsTracker.getIslandsDiscovered(player)).append(" islands discovered.\n\n");
        if (crew.isEmpty()) {
            text.append("No nearby crew notes available. Stand closer to the people you want the book to read.");
        } else {
            text.append("Nearby relationship notes:\n");
            for (CrewEntry entry : crew) {
                text.append(entry.name).append(" currently reads as ").append(tier(entry.trust)).append(".\n");
            }
        }
        return text.toString();
    }

    private static String namesOnly(List<CrewEntry> crew) {
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < crew.size(); i++) {
            if (i > 0) names.append(", ");
            names.append(crew.get(i).name);
        }
        names.append(".");
        return names.toString();
    }

    private static String buildFooter(int tab) {
        return switch (tab) {
            case 1 -> "Clues refresh from nearby crew and exploration stats.";
            case 2 -> "Systems page uses live player records.";
            case 3 -> "Notes page summarizes current relationship readings.";
            default -> "Crew page reads nearby NPC trust and player stats.";
        };
    }

    private static String tier(int trust) {
        if (trust >= 80) return "Crew";
        if (trust >= 55) return "Trusted";
        if (trust >= 35) return "Friend";
        if (trust >= 15) return "Acquaintance";
        return "Stranger";
    }

    private record CrewEntry(String name, String role, int trust) {}
}
