package net.minecraft.world.effect;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

class HealOrHarmMobEffect extends InstantenousMobEffect {
    private final boolean isHarm;

    public HealOrHarmMobEffect(MobEffectCategory pCategory, int pColor, boolean pIsHarm) {
        super(pCategory, pColor);
        this.isHarm = pIsHarm;
    }

    @Override
    public boolean applyEffectTick(ServerLevel p_364873_, LivingEntity p_300845_, int p_301393_) {
        if (this.isHarm == p_300845_.isInvertedHealAndHarm()) {
            p_300845_.heal((float)Math.max(4 << p_301393_, 0));
        } else {
            p_300845_.hurtServer(p_364873_, p_300845_.damageSources().magic(), (float)(6 << p_301393_));
        }

        return true;
    }

    @Override
    public void applyInstantenousEffect(ServerLevel p_365989_, @Nullable Entity p_298495_, @Nullable Entity p_298887_, LivingEntity p_298479_, int p_298172_, double p_298163_) {
        if (this.isHarm == p_298479_.isInvertedHealAndHarm()) {
            int i = (int)(p_298163_ * (double)(4 << p_298172_) + 0.5);
            p_298479_.heal((float)i);
        } else {
            int j = (int)(p_298163_ * (double)(6 << p_298172_) + 0.5);
            if (p_298495_ == null) {
                p_298479_.hurtServer(p_365989_, p_298479_.damageSources().magic(), (float)j);
            } else {
                p_298479_.hurtServer(p_365989_, p_298479_.damageSources().indirectMagic(p_298495_, p_298887_), (float)j);
            }
        }
    }
}