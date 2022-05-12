package org.delusion.rpgmod.character;

import com.google.gson.Gson;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import org.delusion.rpgmod.RPGMod;
import org.delusion.rpgmod.RPGModClient;
import org.delusion.rpgmod.utils.TypeSafeSerializableObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CharacterStats  {


    public static final Identifier RESPONSE_PACKET_ID =
            new Identifier("rpgmod", "character_stats.response_packet_id");
    public static final Identifier REQUEST_PACKET_ID =
            new Identifier("rpgmod", "character_stats.request_packet_id");

    public static void resetUserStats(UUID uuid) {
        CharacterStats statsFor = getStatsFor(uuid);
        statsFor.reset(uuid);
    }

    public void setUserStatsAndSend(UUID uuid, CharacterStatType type, int value) {
        if (RPGMod.server != null) {
            setStatistic(type, value);
            ServerPlayerEntity plr = RPGMod.server.getPlayerManager().getPlayer(uuid);
            if (plr != null) {
                PacketByteBuf buf = PacketByteBufs.create();

                buf.writeString(RPGMod.GSON.toJson(new Storage(getStatsFor(uuid).statValues)));
                ServerPlayNetworking.send(plr, RESPONSE_PACKET_ID, buf);
            }
        }
    }

    private void reset(UUID uuid) {
        if (RPGMod.server != null) {
            statValues.keySet().forEach(characterStatType -> setStatistic(characterStatType, characterStatType.defaultValue()));

            ServerPlayerEntity plr = RPGMod.server.getPlayerManager().getPlayer(uuid);
            if (plr != null) {
                PacketByteBuf buf = PacketByteBufs.create();

                buf.writeString(RPGMod.GSON.toJson(new Storage(getStatsFor(uuid).statValues)));
                ServerPlayNetworking.send(plr, RESPONSE_PACKET_ID, buf);
            }
        }
    }


    public static class Loader extends PersistentState {
        public static final String ID = "rpgmod-character-statistics.json";

        public Map<UUID, CharacterStats> getStatsSet() {
            return statsSet.statsSet;
        }

        static class StatsSet {
            Map<UUID, CharacterStats> statsSet = new HashMap<>();

            public StatsSet(Map<UUID, CharacterStats> statsSet) {
                this.statsSet = statsSet;
            }

            public StatsSet(Storage storage) {
                storage.statsSet.forEach((uuid, storage1) -> {
                    statsSet.put(uuid, new CharacterStats(storage1));
                });
            }

            public StatsSet() {
            }

            static class Storage {
                Map<UUID, CharacterStats.Storage> statsSet = new HashMap<>();

                public Storage(Map<UUID, CharacterStats> statsSet) {
                    statsSet.forEach((uuid, characterStats) -> this.statsSet.put(uuid, new CharacterStats.Storage(characterStats.statValues)));
                }

                public Storage(StatsSet statsSet) {
                    this(statsSet.statsSet);
                }
            }
        }

        StatsSet statsSet = new StatsSet();

        public Loader(String json) {
            RPGMod.LOGGER.info(json);
            statsSet = new StatsSet(RPGMod.GSON.fromJson(json, StatsSet.Storage.class));
        }

        public Loader() {
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt) {
            nbt.putString(ID, RPGMod.GSON.toJson(new StatsSet.Storage(statsSet)));
            return nbt;
        }

    }

    private static Loader persistantStateLoader;
    public static void loadStatsFromDisk(World world) {
        if (world.isClient()) return;
        persistantStateLoader = ((ServerWorld)world).getPersistentStateManager()
                .getOrCreate(nbtCompound -> new Loader(nbtCompound.getString(Loader.ID)),
                        Loader::new, Loader.ID);
        RPGMod.LOGGER.info("Loaded Character Stats for {} characters", persistantStateLoader.statsSet.statsSet.size());

    }
    private Map<CharacterStatType, Integer> statValues = new HashMap<>();

    static class Storage {
        private Map<Identifier, Integer> statValues = new HashMap<>();

        public Storage(Map<CharacterStatType, Integer> map) {
            map.forEach((characterStatType, o) -> {
                if (characterStatType != null) statValues.put(characterStatType.getInternalName(), o);
                else {
                    RPGMod.LOGGER.warn("encountered a null stat type, skipping");
                }
            });
        }
    }

    public CharacterStats() {
    }


    public CharacterStats(Map<CharacterStatType, Integer> statValues) {
        this.statValues = statValues;
    }

    public CharacterStats(Storage storage) {
        storage.statValues.forEach((identifier, o) -> statValues.put(CharacterStatType.get(identifier), ((Number)o).intValue()));
    }

    public void setStatistic(CharacterStatType stat, int value) {
        if (stat == null) return;
        statValues.put(stat, value);
        persistantStateLoader.setDirty(true);
    }

    public int getStatistic(CharacterStatType stat) {
        return statValues.getOrDefault(stat, stat.defaultValue());
    }




    public static void onRequest(MinecraftServer minecraftServer,
                                 ServerPlayerEntity serverPlayerEntity,
                                 ServerPlayNetworkHandler serverPlayNetworkHandler,
                                 PacketByteBuf packetByteBuf,
                                 PacketSender packetSender) {
        PacketByteBuf buf = PacketByteBufs.create();

        if (persistantStateLoader == null) {
            loadStatsFromDisk(minecraftServer.getOverworld());
        }

        buf.writeString(RPGMod.GSON.toJson(new Storage(getStatsFor(serverPlayerEntity.getGameProfile().getId()).statValues)));
        packetSender.sendPacket(RESPONSE_PACKET_ID, buf);
    }

    public static CharacterStats getStatsFor(UUID id) {
        if (!persistantStateLoader.getStatsSet().containsKey(id)) {
            RPGMod.LOGGER.info("Missing stats for character with UUID {}, creating blank stats object", id);
            persistantStateLoader.getStatsSet().put(id, new CharacterStats());
            persistantStateLoader.setDirty(true);
        }

        return persistantStateLoader.getStatsSet().get(id);
    }

    public static void onResponse(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        String json = packetByteBuf.readString();
        RPGModClient.setCharacterStats(new CharacterStats(RPGMod.GSON.fromJson(json, CharacterStats.Storage.class)));
        RPGMod.LOGGER.info("Received Character Stats for {}", minecraftClient.player.getGameProfile().getName());
        minecraftClient.player.sendMessage(Text.of("You currently have " + RPGModClient.getCharacterStats().getStatistic(CharacterStatTypes.EXPERIENCE) + " experience"), false);
    }
}
