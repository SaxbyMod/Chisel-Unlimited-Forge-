package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;

public class CopperBulbBlock extends Block {
    public static final MapCodec<CopperBulbBlock> CODEC = simpleCodec(CopperBulbBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    @Override
    protected MapCodec<? extends CopperBulbBlock> codec() {
        return CODEC;
    }

    public CopperBulbBlock(BlockBehaviour.Properties p_311115_) {
        super(p_311115_);
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, Boolean.valueOf(false)).setValue(POWERED, Boolean.valueOf(false)));
    }

    @Override
    protected void onPlace(BlockState p_309678_, Level p_311953_, BlockPos p_309986_, BlockState p_310956_, boolean p_311576_) {
        if (p_310956_.getBlock() != p_309678_.getBlock() && p_311953_ instanceof ServerLevel serverlevel) {
            this.checkAndFlip(p_309678_, serverlevel, p_309986_);
        }
    }

    @Override
    protected void neighborChanged(BlockState p_312656_, Level p_310732_, BlockPos p_312930_, Block p_310377_, @Nullable Orientation p_363580_, boolean p_310529_) {
        if (p_310732_ instanceof ServerLevel serverlevel) {
            this.checkAndFlip(p_312656_, serverlevel, p_312930_);
        }
    }

    public void checkAndFlip(BlockState pState, ServerLevel pLevel, BlockPos pPos) {
        boolean flag = pLevel.hasNeighborSignal(pPos);
        if (flag != pState.getValue(POWERED)) {
            BlockState blockstate = pState;
            if (!pState.getValue(POWERED)) {
                blockstate = pState.cycle(LIT);
                pLevel.playSound(null, pPos, blockstate.getValue(LIT) ? SoundEvents.COPPER_BULB_TURN_ON : SoundEvents.COPPER_BULB_TURN_OFF, SoundSource.BLOCKS);
            }

            pLevel.setBlock(pPos, blockstate.setValue(POWERED, Boolean.valueOf(flag)), 3);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_312159_) {
        p_312159_.add(LIT, POWERED);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState p_313187_) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState p_311902_, Level p_311245_, BlockPos p_313180_) {
        return p_311245_.getBlockState(p_313180_).getValue(LIT) ? 15 : 0;
    }
}