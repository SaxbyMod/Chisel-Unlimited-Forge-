package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V3825 extends NamespacedSchema {
    public V3825(int p_330588_, Schema p_328304_) {
        super(p_330588_, p_328304_);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema pSchema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(pSchema);
        pSchema.register(map, "minecraft:ominous_item_spawner", () -> DSL.optionalFields("item", References.ITEM_STACK.in(pSchema)));
        return map;
    }
}