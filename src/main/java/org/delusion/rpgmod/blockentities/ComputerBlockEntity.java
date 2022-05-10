package org.delusion.rpgmod.blockentities;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.delusion.rpgmod.blocks.Computer;

public class ComputerBlockEntity extends BlockEntity {
    public static final BlockEntityType<ComputerBlockEntity> TYPE_INSTANCE = FabricBlockEntityTypeBuilder.create(ComputerBlockEntity::new, Computer.INSTANCE).build(null);
    public static final Identifier ID = new Identifier("rpgmod", "computer");

    public ComputerBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE_INSTANCE, pos, state);

    }

    public ComputerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
