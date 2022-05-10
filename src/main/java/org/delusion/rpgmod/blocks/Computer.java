package org.delusion.rpgmod.blocks;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.delusion.rpgmod.blockentities.ComputerBlockEntity;
import org.jetbrains.annotations.Nullable;

public class Computer extends BlockWithEntity {
    public static final BooleanProperty ON = BooleanProperty.of("on");
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public static final Computer INSTANCE = new Computer(AbstractBlock.Settings.of(Material.METAL)
            .requiresTool().strength(3.5f)
            .luminance((state) -> state.get(ON) ? 13 : 0));
    public static final Identifier ID = new Identifier("rpgmod", "computer");

    public Computer(AbstractBlock.Settings settings) {
        super(settings);

        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(ON, false));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }


    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, ON);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        world.setBlockState(pos, state.with(ON, !state.get(ON)));

        return ActionResult.SUCCESS;
    }



    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return ComputerBlockEntity.TYPE_INSTANCE.instantiate(pos, state);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
}
