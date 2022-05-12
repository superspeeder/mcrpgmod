package org.delusion.rpgmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.delusion.rpgmod.blocks.Computer;
import org.delusion.rpgmod.character.CharacterStatType;
import org.delusion.rpgmod.character.CharacterStats;
import org.delusion.rpgmod.screens.ComputerScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RPGMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("rpgmod");

	public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
			new Identifier("rpgmod", "general"),
			() -> new ItemStack(Registry.ITEM.get(new Identifier("rpgmod", "custom_item"))));

	public static final Item CUSTOM_ITEM = new Item(new FabricItemSettings()
			.group(ITEM_GROUP));

	public static final Block METAL_BLOCK = new Block(FabricBlockSettings
			.of(Material.METAL).strength(4.0f).requiresTool());
	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Identifier.class, new Identifier.Serializer())
			.registerTypeAdapter(Text.class, new Text.Serializer())
			.create();
	public static MinecraftServer server = null;

	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, new Identifier("rpgmod", "custom_item"), CUSTOM_ITEM);
		Registry.register(Registry.BLOCK, new Identifier("rpgmod", "metal_block"), METAL_BLOCK);
		Registry.register(Registry.ITEM, new Identifier("rpgmod", "metal_block"),
				new BlockItem(METAL_BLOCK, new FabricItemSettings().group(ITEM_GROUP)));

		Registry.register(Registry.BLOCK, Computer.ID, Computer.INSTANCE);
		Registry.register(Registry.ITEM, Computer.ID,
				new BlockItem(Computer.INSTANCE, new FabricItemSettings().group(ITEM_GROUP)));

		ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
		ServerPlayConnectionEvents.JOIN.register(this::onClientJoin);

	}

	private void onClientJoin(ServerPlayNetworkHandler serverPlayNetworkHandler, PacketSender packetSender, MinecraftServer minecraftServer) {
		CharacterStatType.sendRegistryToClient(packetSender);
	}

	private void onServerStarting(MinecraftServer minecraftServer) {
		server = minecraftServer;
		CharacterStatType.setOnServer(true);
		ServerPlayNetworking.registerGlobalReceiver(CharacterStats.REQUEST_PACKET_ID, CharacterStats::onRequest);
		ServerPlayNetworking.registerGlobalReceiver(ComputerScreen.CLIENT_CLICKED_PLAYERSTATS_BUTTON_PACKET_ID, ComputerScreen::onClientClickPSButton);
	}


}
