/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.SimpleUnbakedGeometry;
import net.minecraftforge.client.model.geometry.UnbakedGeometryHelper;

/**
 * A model composed of vanilla {@linkplain BlockElement block elements}.
 */
public class ElementsModel extends SimpleUnbakedGeometry<ElementsModel> {
    private final List<BlockElement> elements;

    public ElementsModel(List<BlockElement> elements) {
        this.elements = elements;
    }


    @Override
    protected void addQuads(IGeometryBakingContext context, IModelBuilder<?> modelBuilder, ModelBaker baker, TextureSlots textures, ModelState modelState) {
    //protected void addQuads(IGeometryBakingContext context, IModelBuilder<?> modelBuilder, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState) {
        // If there is a root transform, undo the ModelState transform, apply it, then re-apply the ModelState transform.
        // This is necessary because of things like UV locking, which should only respond to the ModelState, and as such
        // that is the only transform that should be applied during face bake.
        var postTransform = QuadTransformers.empty();
        var rootTransform = context.getRootTransform();
        if (!rootTransform.isIdentity())
            postTransform = UnbakedGeometryHelper.applyRootTransform(modelState, rootTransform);
        var sprites = baker.sprites();

        for (var element : elements) {
            for (var direction : element.faces.keySet()) {
                var face = element.faces.get(direction);
                var sprite = sprites.maybeMissing(textures, face.texture());
                var quad = SimpleBakedModel.bakeFace(element, face, sprite, direction, modelState);
                postTransform.processInPlace(quad);

                if (face.cullForDirection() == null)
                    modelBuilder.addUnculledFace(quad);
                else
                    modelBuilder.addCulledFace(modelState.getRotation().rotateTransform(face.cullForDirection()), quad);
            }
        }
    }

    public static final class Loader implements IGeometryLoader<ElementsModel> {
        public static final Loader INSTANCE = new Loader();
        private Loader() { }

        @Override
        public ElementsModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
            if (!jsonObject.has("elements"))
                throw new JsonParseException("An element model must have an \"elements\" member.");

            List<BlockElement> elements = new ArrayList<>();
            for (JsonElement element : GsonHelper.getAsJsonArray(jsonObject, "elements"))
                elements.add(deserializationContext.deserialize(element, BlockElement.class));

            return new ElementsModel(elements);
        }
    }
}
