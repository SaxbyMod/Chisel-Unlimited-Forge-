package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetItemFunction extends LootItemConditionalFunction {
    public static final MapCodec<SetItemFunction> CODEC = RecordCodecBuilder.mapCodec(
        p_335262_ -> commonFields(p_335262_)
                .and(Item.CODEC.fieldOf("item").forGetter(p_334713_ -> p_334713_.item))
                .apply(p_335262_, SetItemFunction::new)
    );
    private final Holder<Item> item;

    private SetItemFunction(List<LootItemCondition> pConditions, Holder<Item> pItem) {
        super(pConditions);
        this.item = pItem;
    }

    @Override
    public LootItemFunctionType<SetItemFunction> getType() {
        return LootItemFunctions.SET_ITEM;
    }

    @Override
    public ItemStack run(ItemStack p_330993_, LootContext p_332197_) {
        return p_330993_.transmuteCopy(this.item.value());
    }
}