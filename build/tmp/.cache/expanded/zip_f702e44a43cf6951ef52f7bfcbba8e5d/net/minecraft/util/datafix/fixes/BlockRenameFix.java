package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class BlockRenameFix extends DataFix {
    private final String name;

    public BlockRenameFix(Schema pOutputSchema, String pName) {
        super(pOutputSchema, false);
        this.name = pName;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.BLOCK_NAME);
        Type<Pair<String, String>> type1 = DSL.named(References.BLOCK_NAME.typeName(), NamespacedSchema.namespacedString());
        if (!Objects.equals(type, type1)) {
            throw new IllegalStateException("block type is not what was expected.");
        } else {
            TypeRewriteRule typerewriterule = this.fixTypeEverywhere(
                this.name + " for block", type1, p_14923_ -> p_145145_ -> p_145145_.mapSecond(this::renameBlock)
            );
            TypeRewriteRule typerewriterule1 = this.fixTypeEverywhereTyped(
                this.name + " for block_state",
                this.getInputSchema().getType(References.BLOCK_STATE),
                p_14913_ -> p_14913_.update(DSL.remainderFinder(), this::fixBlockState)
            );
            TypeRewriteRule typerewriterule2 = this.fixTypeEverywhereTyped(
                this.name + " for flat_block_state",
                this.getInputSchema().getType(References.FLAT_BLOCK_STATE),
                p_326554_ -> p_326554_.update(
                        DSL.remainderFinder(),
                        p_326555_ -> DataFixUtils.orElse(p_326555_.asString().result().map(this::fixFlatBlockState).map(p_326555_::createString), p_326555_)
                    )
            );
            return TypeRewriteRule.seq(typerewriterule, typerewriterule1, typerewriterule2);
        }
    }

    private Dynamic<?> fixBlockState(Dynamic<?> pDynamic) {
        Optional<String> optional = pDynamic.get("Name").asString().result();
        return optional.isPresent() ? pDynamic.set("Name", pDynamic.createString(this.renameBlock(optional.get()))) : pDynamic;
    }

    private String fixFlatBlockState(String pName) {
        int i = pName.indexOf(91);
        int j = pName.indexOf(123);
        int k = pName.length();
        if (i > 0) {
            k = i;
        }

        if (j > 0) {
            k = Math.min(k, j);
        }

        String s = pName.substring(0, k);
        String s1 = this.renameBlock(s);
        return s1 + pName.substring(k);
    }

    protected abstract String renameBlock(String pName);

    public static DataFix create(Schema pOutputSchema, String pName, final Function<String, String> pRenamer) {
        return new BlockRenameFix(pOutputSchema, pName) {
            @Override
            protected String renameBlock(String p_333306_) {
                return pRenamer.apply(p_333306_);
            }
        };
    }
}