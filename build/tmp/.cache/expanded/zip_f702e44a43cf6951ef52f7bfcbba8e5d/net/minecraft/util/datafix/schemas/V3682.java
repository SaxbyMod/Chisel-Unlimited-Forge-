package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V3682 extends NamespacedSchema {
    public V3682(int p_311189_, Schema p_309665_) {
        super(p_311189_, p_309665_);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema pSchema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(pSchema);
        pSchema.register(map, "minecraft:crafter", () -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(pSchema))));
        return map;
    }
}