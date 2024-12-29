package net.minecraft.world.entity.projectile.windcharge;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BreezeWindCharge extends AbstractWindCharge {
    private static final float RADIUS = 3.0F;

    public BreezeWindCharge(EntityType<? extends AbstractWindCharge> p_328102_, Level p_329873_) {
        super(p_328102_, p_329873_);
    }

    public BreezeWindCharge(Breeze pBreeze, Level pLevel) {
        super(EntityType.BREEZE_WIND_CHARGE, pLevel, pBreeze, pBreeze.getX(), pBreeze.getFiringYPosition(), pBreeze.getZ());
    }

    @Override
    protected void explode(Vec3 p_343355_) {
        this.level()
            .explode(
                this,
                null,
                EXPLOSION_DAMAGE_CALCULATOR,
                p_343355_.x(),
                p_343355_.y(),
                p_343355_.z(),
                3.0F,
                false,
                Level.ExplosionInteraction.TRIGGER,
                ParticleTypes.GUST_EMITTER_SMALL,
                ParticleTypes.GUST_EMITTER_LARGE,
                SoundEvents.BREEZE_WIND_CHARGE_BURST
            );
    }
}