package org.delusion.rpgmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.delusion.rpgmod.blockentities.ComputerBlockEntity;
import org.delusion.rpgmod.blocks.Computer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RPGMod implements ModInitializer {


	public static final Logger LOGGER = LoggerFactory.getLogger("rpgmod");

	public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
			new Identifier("rpgmod", "general"),
			() -> new ItemStack(Registry.ITEM.get(new Identifier("rpgmod", "custom_item"))));

	public static final Item CUSTOM_ITEM = new Item(new FabricItemSettings()
			.group(ITEM_GROUP));

	public static final Block EXAMPLE_BLOCK = new Block(FabricBlockSettings
			.of(Material.METAL).strength(4.0f).requiresTool());

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");

		Registry.register(Registry.ITEM, new Identifier("rpgmod", "custom_item"), CUSTOM_ITEM);
		Registry.register(Registry.BLOCK, new Identifier("rpgmod", "metal_block"), EXAMPLE_BLOCK);
		Registry.register(Registry.ITEM, new Identifier("rpgmod", "metal_block"),
				new BlockItem(EXAMPLE_BLOCK, new FabricItemSettings().group(ITEM_GROUP)));

		Registry.register(Registry.BLOCK, Computer.ID, Computer.INSTANCE);
		Registry.register(Registry.ITEM, Computer.ID,
				new BlockItem(Computer.INSTANCE, new FabricItemSettings().group(ITEM_GROUP)));
		Registry.register(Registry.BLOCK_ENTITY_TYPE, ComputerBlockEntity.ID, ComputerBlockEntity.TYPE_INSTANCE);


	}
}
