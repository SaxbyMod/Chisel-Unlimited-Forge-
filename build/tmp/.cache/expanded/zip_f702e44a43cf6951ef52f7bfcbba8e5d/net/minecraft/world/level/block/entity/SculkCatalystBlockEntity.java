package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.Optionull;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;

public class SculkCatalystBlockEntity extends BlockEntity implements GameEventListener.Provider<SculkCatalystBlockEntity.CatalystListener> {
    private final SculkCatalystBlockEntity.CatalystListener catalystListener;

    public SculkCatalystBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityType.SCULK_CATALYST, pPos, pBlockState);
        this.catalystListener = new SculkCatalystBlockEntity.CatalystListener(pBlockState, new BlockPositionSource(pPos));
    }

    public static void serverTick(Level pLevel, BlockPos pPos, BlockState pState, SculkCatalystBlockEntity pSculkCatalyst) {
        pSculkCatalyst.catalystListener.getSculkSpreader().updateCursors(pLevel, pPos, pLevel.getRandom(), true);
    }

    @Override
    protected void loadAdditional(CompoundTag p_334885_, HolderLookup.Provider p_332157_) {
        super.loadAdditional(p_334885_, p_332157_);
        this.catalystListener.sculkSpreader.load(p_334885_);
    }

    @Override
    protected void saveAdditional(CompoundTag p_222789_, HolderLookup.Provider p_332461_) {
        this.catalystListener.sculkSpreader.save(p_222789_);
        super.saveAdditional(p_222789_, p_332461_);
    }

    public SculkCatalystBlockEntity.CatalystListener getListener() {
        return this.catalystListener;
    }

    public static class CatalystListener implements GameEventListener {
        public static final int PULSE_TICKS = 8;
        final SculkSpreader sculkSpreader;
        private final BlockState blockState;
        private final PositionSource positionSource;

        public CatalystListener(BlockState pBlockState, PositionSource pPositionSource) {
            this.blockState = pBlockState;
            this.positionSource = pPositionSource;
            this.sculkSpreader = SculkSpreader.createLevelSpreader();
        }

        @Override
        public PositionSource getListenerSource() {
            return this.positionSource;
        }

        @Override
        public int getListenerRadius() {
            return 8;
        }

        @Override
        public GameEventListener.DeliveryMode getDeliveryMode() {
            return GameEventListener.DeliveryMode.BY_DISTANCE;
        }

        @Override
        public boolean handleGameEvent(ServerLevel p_283470_, Holder<GameEvent> p_332335_, GameEvent.Context p_283014_, Vec3 p_282350_) {
            if (p_332335_.is(GameEvent.ENTITY_DIE) && p_283014_.sourceEntity() instanceof LivingEntity livingentity) {
                if (!livingentity.wasExperienceConsumed()) {
                    DamageSource damagesource = livingentity.getLastDamageSource();
                    int i = livingentity.getExperienceReward(p_283470_, Optionull.map(damagesource, DamageSource::getEntity));
                    if (livingentity.shouldDropExperience() && i > 0) {
                        this.sculkSpreader.addCursors(BlockPos.containing(p_282350_.relative(Direction.UP, 0.5)), i);
                        this.tryAwardItSpreadsAdvancement(p_283470_, livingentity);
                    }

                    livingentity.skipDropExperience();
                    this.positionSource
                        .getPosition(p_283470_)
                        .ifPresent(p_360495_ -> this.bloom(p_283470_, BlockPos.containing(p_360495_), this.blockState, p_283470_.getRandom()));
                }

                return true;
            } else {
                return false;
            }
        }

        @VisibleForTesting
        public SculkSpreader getSculkSpreader() {
            return this.sculkSpreader;
        }

        private void bloom(ServerLevel pLevel, BlockPos pPos, BlockState pState, RandomSource pRandom) {
            pLevel.setBlock(pPos, pState.setValue(SculkCatalystBlock.PULSE, Boolean.valueOf(true)), 3);
            pLevel.scheduleTick(pPos, pState.getBlock(), 8);
            pLevel.sendParticles(
                ParticleTypes.SCULK_SOUL,
                (double)pPos.getX() + 0.5,
                (double)pPos.getY() + 1.15,
                (double)pPos.getZ() + 0.5,
                2,
                0.2,
                0.0,
                0.2,
                0.0
            );
            pLevel.playSound(null, pPos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 0.6F + pRandom.nextFloat() * 0.4F);
        }

        private void tryAwardItSpreadsAdvancement(Level pLevel, LivingEntity pEntity) {
            if (pEntity.getLastHurtByMob() instanceof ServerPlayer serverplayer) {
                DamageSource damagesource = pEntity.getLastDamageSource() == null ? pLevel.damageSources().playerAttack(serverplayer) : pEntity.getLastDamageSource();
                CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.trigger(serverplayer, pEntity, damagesource);
            }
        }
    }
}