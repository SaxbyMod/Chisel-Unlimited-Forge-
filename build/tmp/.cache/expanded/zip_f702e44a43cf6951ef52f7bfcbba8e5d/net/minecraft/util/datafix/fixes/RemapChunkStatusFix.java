package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class RemapChunkStatusFix extends DataFix {
    private final String name;
    private final UnaryOperator<String> mapper;

    public RemapChunkStatusFix(Schema pOutputSchema, String pName, UnaryOperator<String> pMapper) {
        super(pOutputSchema, false);
        this.name = pName;
        this.mapper = pMapper;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            this.name,
            this.getInputSchema().getType(References.CHUNK),
            p_283662_ -> p_283662_.update(
                    DSL.remainderFinder(),
                    p_284697_ -> p_284697_.update("Status", this::fixStatus)
                            .update("below_zero_retrogen", p_282869_ -> p_282869_.update("target_status", this::fixStatus))
                )
        );
    }

    private <T> Dynamic<T> fixStatus(Dynamic<T> pDynamic) {
        Optional<Dynamic<T>> optional = pDynamic.asString().result().map(NamespacedSchema::ensureNamespaced).map(this.mapper).map(pDynamic::createString);
        return DataFixUtils.orElse(optional, pDynamic);
    }
}