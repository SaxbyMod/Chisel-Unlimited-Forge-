package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public class ShearsDispenseItemBehavior extends OptionalDispenseItemBehavior {
    @Override
    protected ItemStack execute(BlockSource p_301806_, ItemStack p_123581_) {
        ServerLevel serverlevel = p_301806_.level();
        if (!serverlevel.isClientSide()) {
            BlockPos blockpos = p_301806_.pos().relative(p_301806_.state().getValue(DispenserBlock.FACING));
            this.setSuccess(tryShearBeehive(serverlevel, blockpos) || tryShearLivingEntity(serverlevel, blockpos, p_123581_));
            if (this.isSuccess()) {
                p_123581_.hurtAndBreak(1, serverlevel, null, p_341008_ -> {
                });
            }
        }

        return p_123581_;
    }

    private static boolean tryShearBeehive(ServerLevel pLevel, BlockPos pPos) {
        BlockState blockstate = pLevel.getBlockState(pPos);
        if (blockstate.is(BlockTags.BEEHIVES, p_202454_ -> p_202454_.hasProperty(BeehiveBlock.HONEY_LEVEL) && p_202454_.getBlock() instanceof BeehiveBlock)) {
            int i = blockstate.getValue(BeehiveBlock.HONEY_LEVEL);
            if (i >= 5) {
                pLevel.playSound(null, pPos, SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
                BeehiveBlock.dropHoneycomb(pLevel, pPos);
                ((BeehiveBlock)blockstate.getBlock()).releaseBeesAndResetHoneyLevel(pLevel, blockstate, pPos, null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
                pLevel.gameEvent(null, GameEvent.SHEAR, pPos);
                return true;
            }
        }

        return false;
    }

    private static boolean tryShearLivingEntity(ServerLevel pLevel, BlockPos pPos, ItemStack pStack) {
        for (LivingEntity livingentity : pLevel.getEntitiesOfClass(LivingEntity.class, new AABB(pPos), EntitySelector.NO_SPECTATORS)) {
            if (livingentity instanceof Shearable shearable && shearable.readyForShearing()) {
                shearable.shear(pLevel, SoundSource.BLOCKS, pStack);
                pLevel.gameEvent(null, GameEvent.SHEAR, pPos);
                return true;
            }
        }

        return false;
    }
}