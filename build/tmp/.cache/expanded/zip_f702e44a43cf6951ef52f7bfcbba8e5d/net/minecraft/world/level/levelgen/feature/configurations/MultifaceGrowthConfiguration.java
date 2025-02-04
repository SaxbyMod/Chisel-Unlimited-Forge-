package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceSpreadeableBlock;

public class MultifaceGrowthConfiguration implements FeatureConfiguration {
    public static final Codec<MultifaceGrowthConfiguration> CODEC = RecordCodecBuilder.create(
        p_225407_ -> p_225407_.group(
                    BuiltInRegistries.BLOCK
                        .byNameCodec()
                        .fieldOf("block")
                        .flatXmap(MultifaceGrowthConfiguration::apply, DataResult::success)
                        .orElse((MultifaceSpreadeableBlock)Blocks.GLOW_LICHEN)
                        .forGetter(p_375354_ -> p_375354_.placeBlock),
                    Codec.intRange(1, 64).fieldOf("search_range").orElse(10).forGetter(p_225422_ -> p_225422_.searchRange),
                    Codec.BOOL.fieldOf("can_place_on_floor").orElse(false).forGetter(p_225420_ -> p_225420_.canPlaceOnFloor),
                    Codec.BOOL.fieldOf("can_place_on_ceiling").orElse(false).forGetter(p_225418_ -> p_225418_.canPlaceOnCeiling),
                    Codec.BOOL.fieldOf("can_place_on_wall").orElse(false).forGetter(p_225416_ -> p_225416_.canPlaceOnWall),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_spreading").orElse(0.5F).forGetter(p_225414_ -> p_225414_.chanceOfSpreading),
                    RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("can_be_placed_on").forGetter(p_225409_ -> p_225409_.canBePlacedOn)
                )
                .apply(p_225407_, MultifaceGrowthConfiguration::new)
    );
    public final MultifaceSpreadeableBlock placeBlock;
    public final int searchRange;
    public final boolean canPlaceOnFloor;
    public final boolean canPlaceOnCeiling;
    public final boolean canPlaceOnWall;
    public final float chanceOfSpreading;
    public final HolderSet<Block> canBePlacedOn;
    private final ObjectArrayList<Direction> validDirections;

    private static DataResult<MultifaceSpreadeableBlock> apply(Block pBlock) {
        return pBlock instanceof MultifaceSpreadeableBlock multifacespreadeableblock
            ? DataResult.success(multifacespreadeableblock)
            : DataResult.error(() -> "Growth block should be a multiface spreadeable block");
    }

    public MultifaceGrowthConfiguration(
        MultifaceSpreadeableBlock pPlaceBlock,
        int pSearchRange,
        boolean pCanPlaceOnFloor,
        boolean pCanPlaceOnCeiling,
        boolean pCanPlaceOnWall,
        float pChanceOfSpreading,
        HolderSet<Block> pCanBePlacedOn
    ) {
        this.placeBlock = pPlaceBlock;
        this.searchRange = pSearchRange;
        this.canPlaceOnFloor = pCanPlaceOnFloor;
        this.canPlaceOnCeiling = pCanPlaceOnCeiling;
        this.canPlaceOnWall = pCanPlaceOnWall;
        this.chanceOfSpreading = pChanceOfSpreading;
        this.canBePlacedOn = pCanBePlacedOn;
        this.validDirections = new ObjectArrayList<>(6);
        if (pCanPlaceOnCeiling) {
            this.validDirections.add(Direction.UP);
        }

        if (pCanPlaceOnFloor) {
            this.validDirections.add(Direction.DOWN);
        }

        if (pCanPlaceOnWall) {
            Direction.Plane.HORIZONTAL.forEach(this.validDirections::add);
        }
    }

    public List<Direction> getShuffledDirectionsExcept(RandomSource pRandom, Direction pDirection) {
        return Util.toShuffledList(this.validDirections.stream().filter(p_225412_ -> p_225412_ != pDirection), pRandom);
    }

    public List<Direction> getShuffledDirections(RandomSource pRandom) {
        return Util.shuffledCopy(this.validDirections, pRandom);
    }
}