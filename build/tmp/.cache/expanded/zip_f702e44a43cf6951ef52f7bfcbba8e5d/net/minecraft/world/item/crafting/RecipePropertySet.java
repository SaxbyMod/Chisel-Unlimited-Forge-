package net.minecraft.world.item.crafting;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RecipePropertySet {
    public static final ResourceKey<? extends Registry<RecipePropertySet>> TYPE_KEY = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("recipe_property_set"));
    public static final ResourceKey<RecipePropertySet> SMITHING_BASE = registerVanilla("smithing_base");
    public static final ResourceKey<RecipePropertySet> SMITHING_TEMPLATE = registerVanilla("smithing_template");
    public static final ResourceKey<RecipePropertySet> SMITHING_ADDITION = registerVanilla("smithing_addition");
    public static final ResourceKey<RecipePropertySet> FURNACE_INPUT = registerVanilla("furnace_input");
    public static final ResourceKey<RecipePropertySet> BLAST_FURNACE_INPUT = registerVanilla("blast_furnace_input");
    public static final ResourceKey<RecipePropertySet> SMOKER_INPUT = registerVanilla("smoker_input");
    public static final ResourceKey<RecipePropertySet> CAMPFIRE_INPUT = registerVanilla("campfire_input");
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipePropertySet> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ITEM)
        .apply(ByteBufCodecs.list())
        .map(p_365348_ -> new RecipePropertySet(Set.copyOf(p_365348_)), p_363184_ -> List.copyOf(p_363184_.items));
    public static final RecipePropertySet EMPTY = new RecipePropertySet(Set.of());
    private final Set<Holder<Item>> items;

    private RecipePropertySet(Set<Holder<Item>> pItems) {
        this.items = pItems;
    }

    private static ResourceKey<RecipePropertySet> registerVanilla(String pName) {
        return ResourceKey.create(TYPE_KEY, ResourceLocation.withDefaultNamespace(pName));
    }

    public boolean test(ItemStack pStack) {
        return this.items.contains(pStack.getItemHolder());
    }

    static RecipePropertySet create(Collection<Ingredient> pIngredients) {
        Set<Holder<Item>> set = pIngredients.stream().flatMap(Ingredient::items).collect(Collectors.toUnmodifiableSet());
        return new RecipePropertySet(set);
    }
}