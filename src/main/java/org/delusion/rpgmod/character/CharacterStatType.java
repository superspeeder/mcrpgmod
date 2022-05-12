package org.delusion.rpgmod.character;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.delusion.rpgmod.RPGMod;

import java.util.*;

/**
 * Type for the character statistic entries. While this may seem like something we would normally choose to make registrable,
 * we have chosen not to in order to accommodate situations in which these may be something one wants to customize at
 * runtime (e.g. a story/world specific statistic). These are sent to clients when registered or when a client joins.
 */
public class CharacterStatType {

    private static Map<Identifier, CharacterStatType> statTypes = new HashMap<>();
    private static boolean onServer = false;


    public static void setOnServer(boolean onServer_) {
        onServer = onServer_;
    }

    public static void sendRegistryToClient(PacketSender packetSender) {
        RegistryStorage storage = new RegistryStorage(statTypes);
        String jsonData = RPGMod.GSON.toJson(storage);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(jsonData);
        packetSender.sendPacket(CHARACTER_STATTYPE_REGISTRY_UPDATE_PACKET_ID, buf);
    }

    public static Collection<CharacterStatType> getAll() {
        return statTypes.values();
    }

    static class RegistryStorage {
        Map<Identifier, CharacterStatType> statTypes = new HashMap<>();

        public RegistryStorage(Map<Identifier, CharacterStatType> statTypes) {
            this.statTypes = statTypes;
        }

        public RegistryStorage() {
        }
    }

    private int defaultValue;
    private Identifier internalName;
    private Text displayName;

    public static CharacterStatType get(Identifier identifier) {
        return statTypes.get(identifier);
    }

    public static void clientRecievedRegistry(PacketByteBuf packetByteBuf) {
        String jsondata = packetByteBuf.readString();
        RPGMod.LOGGER.info(jsondata);
        RegistryStorage storage = RPGMod.GSON.fromJson(jsondata, RegistryStorage.class);
        statTypes.putAll(storage.statTypes);
        storage.statTypes.forEach((identifier, characterStatType) -> {
            
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CharacterStatType that = (CharacterStatType) o;
        return Objects.equals(internalName, that.internalName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(internalName);
    }

    public int defaultValue() {
        return defaultValue;
    }

    public Text getDisplayName() {
        return displayName;
    }

    public Identifier getInternalName() {
        return internalName;
    }

    /**
     *
     * @param defaultValue
     * @param internalName This must be unique, if not then all checks will fail.
     * @param displayName
     */
    public CharacterStatType(int defaultValue, Identifier internalName, Text displayName) {
        this.defaultValue = defaultValue;
        this.internalName = internalName;
        this.displayName = displayName;

        statTypes.put(internalName, this);

        RPGMod.LOGGER.info("Registering new stat type: {}", internalName);

        if (onServer) {
            RPGMod.server.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> {
                if (ServerPlayNetworking.canSend(serverPlayerEntity, CHARACTER_STATTYPE_REGISTRY_UPDATE_PACKET_ID)) {
                    RegistryStorage tStorage = new RegistryStorage();
                    tStorage.statTypes.put(internalName, this);
                    String jsonData = RPGMod.GSON.toJson(tStorage);
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeString(jsonData);
                    ServerPlayNetworking.send(serverPlayerEntity, CHARACTER_STATTYPE_REGISTRY_UPDATE_PACKET_ID, buf);
                }
            });
        }
    }

    public CharacterStatType(int defaultValue, Identifier internalName) {
        this(defaultValue, internalName, new TranslatableText(internalName.getNamespace() + ".character.stats.type." + internalName.getPath()));
    }


    public static final Identifier CHARACTER_STATTYPE_REGISTRY_UPDATE_PACKET_ID = new Identifier("rpgmod", "character_stat_types.registry_update");


}
