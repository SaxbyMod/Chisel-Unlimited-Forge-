package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AnvilBlock extends FallingBlock {
    public static final MapCodec<AnvilBlock> CODEC = simpleCodec(AnvilBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape BASE = Block.box(2.0, 0.0, 2.0, 14.0, 4.0, 14.0);
    private static final VoxelShape X_LEG1 = Block.box(3.0, 4.0, 4.0, 13.0, 5.0, 12.0);
    private static final VoxelShape X_LEG2 = Block.box(4.0, 5.0, 6.0, 12.0, 10.0, 10.0);
    private static final VoxelShape X_TOP = Block.box(0.0, 10.0, 3.0, 16.0, 16.0, 13.0);
    private static final VoxelShape Z_LEG1 = Block.box(4.0, 4.0, 3.0, 12.0, 5.0, 13.0);
    private static final VoxelShape Z_LEG2 = Block.box(6.0, 5.0, 4.0, 10.0, 10.0, 12.0);
    private static final VoxelShape Z_TOP = Block.box(3.0, 10.0, 0.0, 13.0, 16.0, 16.0);
    private static final VoxelShape X_AXIS_AABB = Shapes.or(BASE, X_LEG1, X_LEG2, X_TOP);
    private static final VoxelShape Z_AXIS_AABB = Shapes.or(BASE, Z_LEG1, Z_LEG2, Z_TOP);
    private static final Component CONTAINER_TITLE = Component.translatable("container.repair");
    private static final float FALL_DAMAGE_PER_DISTANCE = 2.0F;
    private static final int FALL_DAMAGE_MAX = 40;

    @Override
    public MapCodec<AnvilBlock> codec() {
        return CODEC;
    }

    public AnvilBlock(BlockBehaviour.Properties p_48777_) {
        super(p_48777_);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getClockWise());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState p_48804_, Level p_48805_, BlockPos p_48806_, Player p_48807_, BlockHitResult p_48809_) {
        if (!p_48805_.isClientSide) {
            p_48807_.openMenu(p_48804_.getMenuProvider(p_48805_, p_48806_));
            p_48807_.awardStat(Stats.INTERACT_WITH_ANVIL);
        }

        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    protected MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        return new SimpleMenuProvider(
            (p_48785_, p_48786_, p_48787_) -> new AnvilMenu(p_48785_, p_48786_, ContainerLevelAccess.create(pLevel, pPos)), CONTAINER_TITLE
        );
    }

    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction direction = pState.getValue(FACING);
        return direction.getAxis() == Direction.Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
    }

    @Override
    protected void falling(FallingBlockEntity pFallingEntity) {
        pFallingEntity.setHurtsEntities(2.0F, 40);
    }

    @Override
    public void onLand(Level p_48793_, BlockPos p_48794_, BlockState p_48795_, BlockState p_48796_, FallingBlockEntity p_48797_) {
        if (!p_48797_.isSilent()) {
            p_48793_.levelEvent(1031, p_48794_, 0);
        }
    }

    @Override
    public void onBrokenAfterFall(Level p_152053_, BlockPos p_152054_, FallingBlockEntity p_152055_) {
        if (!p_152055_.isSilent()) {
            p_152053_.levelEvent(1029, p_152054_, 0);
        }
    }

    @Override
    public DamageSource getFallDamageSource(Entity p_254036_) {
        return p_254036_.damageSources().anvil(p_254036_);
    }

    @Nullable
    public static BlockState damage(BlockState pState) {
        if (pState.is(Blocks.ANVIL)) {
            return Blocks.CHIPPED_ANVIL.defaultBlockState().setValue(FACING, pState.getValue(FACING));
        } else {
            return pState.is(Blocks.CHIPPED_ANVIL) ? Blocks.DAMAGED_ANVIL.defaultBlockState().setValue(FACING, pState.getValue(FACING)) : null;
        }
    }

    @Override
    protected BlockState rotate(BlockState pState, Rotation pRot) {
        return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    protected boolean isPathfindable(BlockState p_48799_, PathComputationType p_48802_) {
        return false;
    }

    @Override
    public int getDustColor(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return pState.getMapColor(pReader, pPos).col;
    }
}