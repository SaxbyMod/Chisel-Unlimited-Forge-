package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class SinglePoolElement extends StructurePoolElement {
    private static final Comparator<StructureTemplate.JigsawBlockInfo> HIGHEST_SELECTION_PRIORITY_FIRST = Comparator.comparingInt(StructureTemplate.JigsawBlockInfo::selectionPriority)
        .reversed();
    private static final Codec<Either<ResourceLocation, StructureTemplate>> TEMPLATE_CODEC = Codec.of(
        SinglePoolElement::encodeTemplate, ResourceLocation.CODEC.map(Either::left)
    );
    public static final MapCodec<SinglePoolElement> CODEC = RecordCodecBuilder.mapCodec(
        p_341933_ -> p_341933_.group(templateCodec(), processorsCodec(), projectionCodec(), overrideLiquidSettingsCodec()).apply(p_341933_, SinglePoolElement::new)
    );
    protected final Either<ResourceLocation, StructureTemplate> template;
    protected final Holder<StructureProcessorList> processors;
    protected final Optional<LiquidSettings> overrideLiquidSettings;

    private static <T> DataResult<T> encodeTemplate(Either<ResourceLocation, StructureTemplate> pTemplate, DynamicOps<T> pOps, T pValues) {
        Optional<ResourceLocation> optional = pTemplate.left();
        return optional.isEmpty()
            ? DataResult.error(() -> "Can not serialize a runtime pool element")
            : ResourceLocation.CODEC.encode(optional.get(), pOps, pValues);
    }

    protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Holder<StructureProcessorList>> processorsCodec() {
        return StructureProcessorType.LIST_CODEC.fieldOf("processors").forGetter(p_210464_ -> p_210464_.processors);
    }

    protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Optional<LiquidSettings>> overrideLiquidSettingsCodec() {
        return LiquidSettings.CODEC.optionalFieldOf("override_liquid_settings").forGetter(p_341934_ -> p_341934_.overrideLiquidSettings);
    }

    protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Either<ResourceLocation, StructureTemplate>> templateCodec() {
        return TEMPLATE_CODEC.fieldOf("location").forGetter(p_210431_ -> p_210431_.template);
    }

    protected SinglePoolElement(
        Either<ResourceLocation, StructureTemplate> pTemplate,
        Holder<StructureProcessorList> pProcessors,
        StructureTemplatePool.Projection pProjection,
        Optional<LiquidSettings> pOverrideLiquidSettings
    ) {
        super(pProjection);
        this.template = pTemplate;
        this.processors = pProcessors;
        this.overrideLiquidSettings = pOverrideLiquidSettings;
    }

    @Override
    public Vec3i getSize(StructureTemplateManager p_227313_, Rotation p_227314_) {
        StructureTemplate structuretemplate = this.getTemplate(p_227313_);
        return structuretemplate.getSize(p_227314_);
    }

    private StructureTemplate getTemplate(StructureTemplateManager pStructureTemplateManager) {
        return this.template.map(pStructureTemplateManager::getOrCreate, Function.identity());
    }

    public List<StructureTemplate.StructureBlockInfo> getDataMarkers(StructureTemplateManager pStructureTemplateManager, BlockPos pPos, Rotation pRotation, boolean pRelativePosition) {
        StructureTemplate structuretemplate = this.getTemplate(pStructureTemplateManager);
        List<StructureTemplate.StructureBlockInfo> list = structuretemplate.filterBlocks(
            pPos, new StructurePlaceSettings().setRotation(pRotation), Blocks.STRUCTURE_BLOCK, pRelativePosition
        );
        List<StructureTemplate.StructureBlockInfo> list1 = Lists.newArrayList();

        for (StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : list) {
            CompoundTag compoundtag = structuretemplate$structureblockinfo.nbt();
            if (compoundtag != null) {
                StructureMode structuremode = StructureMode.valueOf(compoundtag.getString("mode"));
                if (structuremode == StructureMode.DATA) {
                    list1.add(structuretemplate$structureblockinfo);
                }
            }
        }

        return list1;
    }

    @Override
    public List<StructureTemplate.JigsawBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager p_227320_, BlockPos p_227321_, Rotation p_227322_, RandomSource p_227323_) {
        List<StructureTemplate.JigsawBlockInfo> list = this.getTemplate(p_227320_).getJigsaws(p_227321_, p_227322_);
        Util.shuffle(list, p_227323_);
        sortBySelectionPriority(list);
        return list;
    }

    @VisibleForTesting
    static void sortBySelectionPriority(List<StructureTemplate.JigsawBlockInfo> pStructureBlockInfos) {
        pStructureBlockInfos.sort(HIGHEST_SELECTION_PRIORITY_FIRST);
    }

    @Override
    public BoundingBox getBoundingBox(StructureTemplateManager p_227316_, BlockPos p_227317_, Rotation p_227318_) {
        StructureTemplate structuretemplate = this.getTemplate(p_227316_);
        return structuretemplate.getBoundingBox(new StructurePlaceSettings().setRotation(p_227318_), p_227317_);
    }

    @Override
    public boolean place(
        StructureTemplateManager p_227302_,
        WorldGenLevel p_227303_,
        StructureManager p_227304_,
        ChunkGenerator p_227305_,
        BlockPos p_227306_,
        BlockPos p_227307_,
        Rotation p_227308_,
        BoundingBox p_227309_,
        RandomSource p_227310_,
        LiquidSettings p_342078_,
        boolean p_227311_
    ) {
        StructureTemplate structuretemplate = this.getTemplate(p_227302_);
        StructurePlaceSettings structureplacesettings = this.getSettings(p_227308_, p_227309_, p_342078_, p_227311_);
        if (!structuretemplate.placeInWorld(p_227303_, p_227306_, p_227307_, structureplacesettings, p_227310_, 18)) {
            return false;
        } else {
            for (StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : StructureTemplate.processBlockInfos(
                p_227303_, p_227306_, p_227307_, structureplacesettings, this.getDataMarkers(p_227302_, p_227306_, p_227308_, false)
            )) {
                this.handleDataMarker(p_227303_, structuretemplate$structureblockinfo, p_227306_, p_227308_, p_227310_, p_227309_);
            }

            return true;
        }
    }

    protected StructurePlaceSettings getSettings(Rotation pRotation, BoundingBox pBoundingBox, LiquidSettings pLiquidSettings, boolean pOffset) {
        StructurePlaceSettings structureplacesettings = new StructurePlaceSettings();
        structureplacesettings.setBoundingBox(pBoundingBox);
        structureplacesettings.setRotation(pRotation);
        structureplacesettings.setKnownShape(true);
        structureplacesettings.setIgnoreEntities(false);
        structureplacesettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        structureplacesettings.setFinalizeEntities(true);
        structureplacesettings.setLiquidSettings(this.overrideLiquidSettings.orElse(pLiquidSettings));
        if (!pOffset) {
            structureplacesettings.addProcessor(JigsawReplacementProcessor.INSTANCE);
        }

        this.processors.value().list().forEach(structureplacesettings::addProcessor);
        this.getProjection().getProcessors().forEach(structureplacesettings::addProcessor);
        return structureplacesettings;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return StructurePoolElementType.SINGLE;
    }

    @Override
    public String toString() {
        return "Single[" + this.template + "]";
    }
}