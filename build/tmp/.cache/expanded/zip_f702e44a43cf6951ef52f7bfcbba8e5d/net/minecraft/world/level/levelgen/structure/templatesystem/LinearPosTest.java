package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class LinearPosTest extends PosRuleTest {
    public static final MapCodec<LinearPosTest> CODEC = RecordCodecBuilder.mapCodec(
        p_74160_ -> p_74160_.group(
                    Codec.FLOAT.fieldOf("min_chance").orElse(0.0F).forGetter(p_163737_ -> p_163737_.minChance),
                    Codec.FLOAT.fieldOf("max_chance").orElse(0.0F).forGetter(p_163735_ -> p_163735_.maxChance),
                    Codec.INT.fieldOf("min_dist").orElse(0).forGetter(p_163733_ -> p_163733_.minDist),
                    Codec.INT.fieldOf("max_dist").orElse(0).forGetter(p_163731_ -> p_163731_.maxDist)
                )
                .apply(p_74160_, LinearPosTest::new)
    );
    private final float minChance;
    private final float maxChance;
    private final int minDist;
    private final int maxDist;

    public LinearPosTest(float pMinChance, float pMaxChance, int pMinDist, int pMaxDist) {
        if (pMinDist >= pMaxDist) {
            throw new IllegalArgumentException("Invalid range: [" + pMinDist + "," + pMaxDist + "]");
        } else {
            this.minChance = pMinChance;
            this.maxChance = pMaxChance;
            this.minDist = pMinDist;
            this.maxDist = pMaxDist;
        }
    }

    @Override
    public boolean test(BlockPos p_230296_, BlockPos p_230297_, BlockPos p_230298_, RandomSource p_230299_) {
        int i = p_230297_.distManhattan(p_230298_);
        float f = p_230299_.nextFloat();
        return f <= Mth.clampedLerp(this.minChance, this.maxChance, Mth.inverseLerp((float)i, (float)this.minDist, (float)this.maxDist));
    }

    @Override
    protected PosRuleTestType<?> getType() {
        return PosRuleTestType.LINEAR_POS_TEST;
    }
}