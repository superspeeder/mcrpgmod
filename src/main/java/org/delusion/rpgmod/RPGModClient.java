package org.delusion.rpgmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.delusion.rpgmod.character.CharacterStatType;
import org.delusion.rpgmod.character.CharacterStats;

@Environment(EnvType.CLIENT)
public class RPGModClient implements ClientModInitializer {

    private static CharacterStats characterStats;
    private boolean hasJoined = false;
    private boolean receivedStatTypeRegistry = false;

    public static ItemStack getItemStackForCurrentPlayerHead() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD, 1);
        if (MinecraftClient.getInstance().player.getGameProfile().isComplete() && !FabricLauncherBase.getLauncher().isDevelopment()) {
            NbtCompound compound = new NbtCompound();
            compound.putString("SkullOwner", MinecraftClient.getInstance().player.getGameProfile().getName());
            stack.setNbt(compound);
        }
        return stack;
    }

    public static CharacterStats getCharacterStats() {
        return characterStats;
    }

    public static void setCharacterStats(CharacterStats s) {
        characterStats = s;
    }

    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register(this::onJoin);
        CharacterStatType.setOnServer(false);
        ClientPlayNetworking.registerGlobalReceiver(CharacterStats.RESPONSE_PACKET_ID, CharacterStats::onResponse);
        ClientPlayNetworking.registerGlobalReceiver(CharacterStatType.CHARACTER_STATTYPE_REGISTRY_UPDATE_PACKET_ID, this::onRecieveCharacterStatTypes);
    }

    private void onJoin(ClientPlayNetworkHandler clientPlayNetworkHandler, PacketSender packetSender, MinecraftClient minecraftClient) {
        hasJoined = true;
        if (receivedStatTypeRegistry) {
            hasRequestedCharacterStats = true;
            ClientPlayNetworking.send(CharacterStats.REQUEST_PACKET_ID, PacketByteBufs.empty());
        }
    }

    private boolean hasRequestedCharacterStats = false;

    private void onRecieveCharacterStatTypes(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        CharacterStatType.clientRecievedRegistry(packetByteBuf);
        receivedStatTypeRegistry = true;

        if (!hasRequestedCharacterStats && hasJoined) {
            hasRequestedCharacterStats = true;
            ClientPlayNetworking.send(CharacterStats.REQUEST_PACKET_ID, PacketByteBufs.empty());
        }
    }
}


