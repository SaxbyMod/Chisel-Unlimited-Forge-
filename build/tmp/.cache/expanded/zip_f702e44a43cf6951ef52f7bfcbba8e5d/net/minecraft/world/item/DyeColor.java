package net.minecraft.world.item;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Contract;

public enum DyeColor implements StringRepresentable {
    WHITE(0, "white", 16383998, MapColor.SNOW, 15790320, 16777215),
    ORANGE(1, "orange", 16351261, MapColor.COLOR_ORANGE, 15435844, 16738335),
    MAGENTA(2, "magenta", 13061821, MapColor.COLOR_MAGENTA, 12801229, 16711935),
    LIGHT_BLUE(3, "light_blue", 3847130, MapColor.COLOR_LIGHT_BLUE, 6719955, 10141901),
    YELLOW(4, "yellow", 16701501, MapColor.COLOR_YELLOW, 14602026, 16776960),
    LIME(5, "lime", 8439583, MapColor.COLOR_LIGHT_GREEN, 4312372, 12582656),
    PINK(6, "pink", 15961002, MapColor.COLOR_PINK, 14188952, 16738740),
    GRAY(7, "gray", 4673362, MapColor.COLOR_GRAY, 4408131, 8421504),
    LIGHT_GRAY(8, "light_gray", 10329495, MapColor.COLOR_LIGHT_GRAY, 11250603, 13882323),
    CYAN(9, "cyan", 1481884, MapColor.COLOR_CYAN, 2651799, 65535),
    PURPLE(10, "purple", 8991416, MapColor.COLOR_PURPLE, 8073150, 10494192),
    BLUE(11, "blue", 3949738, MapColor.COLOR_BLUE, 2437522, 255),
    BROWN(12, "brown", 8606770, MapColor.COLOR_BROWN, 5320730, 9127187),
    GREEN(13, "green", 6192150, MapColor.COLOR_GREEN, 3887386, 65280),
    RED(14, "red", 11546150, MapColor.COLOR_RED, 11743532, 16711680),
    BLACK(15, "black", 1908001, MapColor.COLOR_BLACK, 1973019, 0);

    private static final IntFunction<DyeColor> BY_ID = ByIdMap.continuous(DyeColor::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    private static final Int2ObjectOpenHashMap<DyeColor> BY_FIREWORK_COLOR = new Int2ObjectOpenHashMap<>(
        Arrays.stream(values()).collect(Collectors.toMap(p_41064_ -> p_41064_.fireworkColor, p_41056_ -> (DyeColor)p_41056_))
    );
    public static final StringRepresentable.EnumCodec<DyeColor> CODEC = StringRepresentable.fromEnum(DyeColor::values);
    public static final StreamCodec<ByteBuf, DyeColor> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, DyeColor::getId);
    private final int id;
    private final String name;
    private final MapColor mapColor;
    private final int textureDiffuseColor;
    private final int fireworkColor;
    private final int textColor;
    private final net.minecraft.tags.TagKey<Item> dyesTag;
    private final net.minecraft.tags.TagKey<Item> dyedTag;

    private DyeColor(final int pId, final String pName, final int pTextureDefuseColor, final MapColor pMapColor, final int pFireworkColor, final int pTextColor) {
        this.id = pId;
        this.name = pName;
        this.mapColor = pMapColor;
        this.textColor = pTextColor;
        this.textureDiffuseColor = ARGB.opaque(pTextureDefuseColor);
        this.fireworkColor = pFireworkColor;
        this.dyesTag = net.minecraft.tags.ItemTags.create("c", "dyes/" + pName);
        this.dyedTag = net.minecraft.tags.ItemTags.create("c", "dyed/" + pName);
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getTextureDiffuseColor() {
        return this.textureDiffuseColor;
    }

    public MapColor getMapColor() {
        return this.mapColor;
    }

    public int getFireworkColor() {
        return this.fireworkColor;
    }

    public int getTextColor() {
        return this.textColor;
    }

    public static DyeColor byId(int pColorId) {
        return BY_ID.apply(pColorId);
    }

    @Nullable
    @Contract("_,!null->!null;_,null->_")
    public static DyeColor byName(String pTranslationKey, @Nullable DyeColor pFallback) {
        DyeColor dyecolor = CODEC.byName(pTranslationKey);
        return dyecolor != null ? dyecolor : pFallback;
    }

    @Nullable
    public static DyeColor byFireworkColor(int pFireworkColor) {
        return BY_FIREWORK_COLOR.get(pFireworkColor);
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /**
     * Gets the tag key representing the set of items which provide this dye colour.
     * @return A {@link net.minecraft.tags.TagKey<Item>} representing the set of items which provide this dye colour.
     */
    public net.minecraft.tags.TagKey<Item> getTag() {
        return dyesTag;
    }

    /**
     * Gets the tag key representing the set of items which are dyed with this colour.
     * @return A {@link net.minecraft.tags.TagKey<Item>} representing the set of items which are dyed with this colour.
     */
    public net.minecraft.tags.TagKey<Item> getDyedTag() {
        return dyedTag;
    }

    @Nullable
    public static DyeColor getColor(ItemStack stack) {
        return net.minecraftforge.common.ForgeHooks.getDyeColorFromItemStack(stack);
    }

    public static DyeColor getMixedColor(ServerLevel pLevel, DyeColor pFirst, DyeColor pSecond) {
        CraftingInput craftinginput = makeCraftColorInput(pFirst, pSecond);
        return pLevel.recipeAccess()
            .getRecipeFor(RecipeType.CRAFTING, craftinginput, pLevel)
            .map(p_375205_ -> p_375205_.value().assemble(craftinginput, pLevel.registryAccess()))
            .map(ItemStack::getItem)
            .filter(DyeItem.class::isInstance)
            .map(DyeItem.class::cast)
            .map(DyeItem::getDyeColor)
            .orElseGet(() -> pLevel.random.nextBoolean() ? pFirst : pSecond);
    }

    private static CraftingInput makeCraftColorInput(DyeColor pFirst, DyeColor pSecond) {
        return CraftingInput.of(2, 1, List.of(new ItemStack(DyeItem.byColor(pFirst)), new ItemStack(DyeItem.byColor(pSecond))));
    }
}
