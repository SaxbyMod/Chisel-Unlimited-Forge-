package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Streams;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Selector {
    private final Condition condition;
    private final MultiVariant variant;

    public Selector(Condition pCondition, MultiVariant pVariant) {
        this.condition = pCondition;
        this.variant = pVariant;
    }

    public MultiVariant getVariant() {
        return this.variant;
    }

    public Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> pDefinition) {
        return this.condition.getPredicate(pDefinition);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<Selector> {
        public Selector deserialize(JsonElement pJson, Type pType, JsonDeserializationContext pContext) throws JsonParseException {
            JsonObject jsonobject = pJson.getAsJsonObject();
            return new Selector(this.getSelector(jsonobject), pContext.deserialize(jsonobject.get("apply"), MultiVariant.class));
        }

        private Condition getSelector(JsonObject pJson) {
            return pJson.has("when") ? getCondition(GsonHelper.getAsJsonObject(pJson, "when")) : Condition.TRUE;
        }

        @VisibleForTesting
        static Condition getCondition(JsonObject pJson) {
            Set<Entry<String, JsonElement>> set = pJson.entrySet();
            if (set.isEmpty()) {
                throw new JsonParseException("No elements found in selector");
            } else if (set.size() == 1) {
                if (pJson.has("OR")) {
                    List<Condition> list1 = Streams.stream(GsonHelper.getAsJsonArray(pJson, "OR"))
                        .map(p_112038_ -> getCondition(p_112038_.getAsJsonObject()))
                        .collect(Collectors.toList());
                    return new OrCondition(list1);
                } else if (pJson.has("AND")) {
                    List<Condition> list = Streams.stream(GsonHelper.getAsJsonArray(pJson, "AND"))
                        .map(p_112028_ -> getCondition(p_112028_.getAsJsonObject()))
                        .collect(Collectors.toList());
                    return new AndCondition(list);
                } else {
                    return getKeyValueCondition(set.iterator().next());
                }
            } else {
                return new AndCondition(set.stream().map(Selector.Deserializer::getKeyValueCondition).collect(Collectors.toList()));
            }
        }

        private static Condition getKeyValueCondition(Entry<String, JsonElement> pEntry) {
            return new KeyValueCondition(pEntry.getKey(), pEntry.getValue().getAsString());
        }
    }
}