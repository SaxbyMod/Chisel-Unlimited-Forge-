package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class FancyTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<FancyTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(p_70136_ -> trunkPlacerParts(p_70136_).apply(p_70136_, FancyTrunkPlacer::new));
    private static final double TRUNK_HEIGHT_SCALE = 0.618;
    private static final double CLUSTER_DENSITY_MAGIC = 1.382;
    private static final double BRANCH_SLOPE = 0.381;
    private static final double BRANCH_LENGTH_MAGIC = 0.328;

    public FancyTrunkPlacer(int p_70094_, int p_70095_, int p_70096_) {
        super(p_70094_, p_70095_, p_70096_);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.FANCY_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
        LevelSimulatedReader p_226093_,
        BiConsumer<BlockPos, BlockState> p_226094_,
        RandomSource p_226095_,
        int p_226096_,
        BlockPos p_226097_,
        TreeConfiguration p_226098_
    ) {
        int i = 5;
        int j = p_226096_ + 2;
        int k = Mth.floor((double)j * 0.618);
        setDirtAt(p_226093_, p_226094_, p_226095_, p_226097_.below(), p_226098_);
        double d0 = 1.0;
        int l = Math.min(1, Mth.floor(1.382 + Math.pow(1.0 * (double)j / 13.0, 2.0)));
        int i1 = p_226097_.getY() + k;
        int j1 = j - 5;
        List<FancyTrunkPlacer.FoliageCoords> list = Lists.newArrayList();
        list.add(new FancyTrunkPlacer.FoliageCoords(p_226097_.above(j1), i1));

        for (; j1 >= 0; j1--) {
            float f = treeShape(j, j1);
            if (!(f < 0.0F)) {
                for (int k1 = 0; k1 < l; k1++) {
                    double d1 = 1.0;
                    double d2 = 1.0 * (double)f * ((double)p_226095_.nextFloat() + 0.328);
                    double d3 = (double)(p_226095_.nextFloat() * 2.0F) * Math.PI;
                    double d4 = d2 * Math.sin(d3) + 0.5;
                    double d5 = d2 * Math.cos(d3) + 0.5;
                    BlockPos blockpos = p_226097_.offset(Mth.floor(d4), j1 - 1, Mth.floor(d5));
                    BlockPos blockpos1 = blockpos.above(5);
                    if (this.makeLimb(p_226093_, p_226094_, p_226095_, blockpos, blockpos1, false, p_226098_)) {
                        int l1 = p_226097_.getX() - blockpos.getX();
                        int i2 = p_226097_.getZ() - blockpos.getZ();
                        double d6 = (double)blockpos.getY() - Math.sqrt((double)(l1 * l1 + i2 * i2)) * 0.381;
                        int j2 = d6 > (double)i1 ? i1 : (int)d6;
                        BlockPos blockpos2 = new BlockPos(p_226097_.getX(), j2, p_226097_.getZ());
                        if (this.makeLimb(p_226093_, p_226094_, p_226095_, blockpos2, blockpos, false, p_226098_)) {
                            list.add(new FancyTrunkPlacer.FoliageCoords(blockpos, blockpos2.getY()));
                        }
                    }
                }
            }
        }

        this.makeLimb(p_226093_, p_226094_, p_226095_, p_226097_, p_226097_.above(k), true, p_226098_);
        this.makeBranches(p_226093_, p_226094_, p_226095_, j, p_226097_, list, p_226098_);
        List<FoliagePlacer.FoliageAttachment> list1 = Lists.newArrayList();

        for (FancyTrunkPlacer.FoliageCoords fancytrunkplacer$foliagecoords : list) {
            if (this.trimBranches(j, fancytrunkplacer$foliagecoords.getBranchBase() - p_226097_.getY())) {
                list1.add(fancytrunkplacer$foliagecoords.attachment);
            }
        }

        return list1;
    }

    private boolean makeLimb(
        LevelSimulatedReader pLevel,
        BiConsumer<BlockPos, BlockState> pBlockSetter,
        RandomSource pRandom,
        BlockPos pBasePos,
        BlockPos pOffsetPos,
        boolean pModifyWorld,
        TreeConfiguration pConfig
    ) {
        if (!pModifyWorld && Objects.equals(pBasePos, pOffsetPos)) {
            return true;
        } else {
            BlockPos blockpos = pOffsetPos.offset(-pBasePos.getX(), -pBasePos.getY(), -pBasePos.getZ());
            int i = this.getSteps(blockpos);
            float f = (float)blockpos.getX() / (float)i;
            float f1 = (float)blockpos.getY() / (float)i;
            float f2 = (float)blockpos.getZ() / (float)i;

            for (int j = 0; j <= i; j++) {
                BlockPos blockpos1 = pBasePos.offset(
                    Mth.floor(0.5F + (float)j * f), Mth.floor(0.5F + (float)j * f1), Mth.floor(0.5F + (float)j * f2)
                );
                if (pModifyWorld) {
                    this.placeLog(
                        pLevel,
                        pBlockSetter,
                        pRandom,
                        blockpos1,
                        pConfig,
                        p_360618_ -> p_360618_.trySetValue(RotatedPillarBlock.AXIS, this.getLogAxis(pBasePos, blockpos1))
                    );
                } else if (!this.isFree(pLevel, blockpos1)) {
                    return false;
                }
            }

            return true;
        }
    }

    private int getSteps(BlockPos pPos) {
        int i = Mth.abs(pPos.getX());
        int j = Mth.abs(pPos.getY());
        int k = Mth.abs(pPos.getZ());
        return Math.max(i, Math.max(j, k));
    }

    private Direction.Axis getLogAxis(BlockPos pPos, BlockPos pOtherPos) {
        Direction.Axis direction$axis = Direction.Axis.Y;
        int i = Math.abs(pOtherPos.getX() - pPos.getX());
        int j = Math.abs(pOtherPos.getZ() - pPos.getZ());
        int k = Math.max(i, j);
        if (k > 0) {
            if (i == k) {
                direction$axis = Direction.Axis.X;
            } else {
                direction$axis = Direction.Axis.Z;
            }
        }

        return direction$axis;
    }

    private boolean trimBranches(int pMaxHeight, int pCurrentHeight) {
        return (double)pCurrentHeight >= (double)pMaxHeight * 0.2;
    }

    private void makeBranches(
        LevelSimulatedReader pLevel,
        BiConsumer<BlockPos, BlockState> pBlockSetter,
        RandomSource pRandom,
        int pMaxHeight,
        BlockPos pPos,
        List<FancyTrunkPlacer.FoliageCoords> pFoliageCoords,
        TreeConfiguration pConfig
    ) {
        for (FancyTrunkPlacer.FoliageCoords fancytrunkplacer$foliagecoords : pFoliageCoords) {
            int i = fancytrunkplacer$foliagecoords.getBranchBase();
            BlockPos blockpos = new BlockPos(pPos.getX(), i, pPos.getZ());
            if (!blockpos.equals(fancytrunkplacer$foliagecoords.attachment.pos()) && this.trimBranches(pMaxHeight, i - pPos.getY())) {
                this.makeLimb(pLevel, pBlockSetter, pRandom, blockpos, fancytrunkplacer$foliagecoords.attachment.pos(), true, pConfig);
            }
        }
    }

    private static float treeShape(int pHeight, int pCurrentY) {
        if ((float)pCurrentY < (float)pHeight * 0.3F) {
            return -1.0F;
        } else {
            float f = (float)pHeight / 2.0F;
            float f1 = f - (float)pCurrentY;
            float f2 = Mth.sqrt(f * f - f1 * f1);
            if (f1 == 0.0F) {
                f2 = f;
            } else if (Math.abs(f1) >= f) {
                return 0.0F;
            }

            return f2 * 0.5F;
        }
    }

    static class FoliageCoords {
        final FoliagePlacer.FoliageAttachment attachment;
        private final int branchBase;

        public FoliageCoords(BlockPos pAttachmentPos, int pBranchBase) {
            this.attachment = new FoliagePlacer.FoliageAttachment(pAttachmentPos, 0, false);
            this.branchBase = pBranchBase;
        }

        public int getBranchBase() {
            return this.branchBase;
        }
    }
}