package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsSlotOptionsScreen extends RealmsScreen {
    private static final int DEFAULT_DIFFICULTY = 2;
    public static final List<Difficulty> DIFFICULTIES = ImmutableList.of(Difficulty.PEACEFUL, Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD);
    private static final int DEFAULT_GAME_MODE = 0;
    public static final List<GameType> GAME_MODES = ImmutableList.of(GameType.SURVIVAL, GameType.CREATIVE, GameType.ADVENTURE);
    private static final Component NAME_LABEL = Component.translatable("mco.configure.world.edit.slot.name");
    static final Component SPAWN_PROTECTION_TEXT = Component.translatable("mco.configure.world.spawnProtection");
    private EditBox nameEdit;
    protected final RealmsConfigureWorldScreen parentScreen;
    private int column1X;
    private int columnWidth;
    private final RealmsWorldOptions options;
    private final RealmsServer.WorldType worldType;
    private Difficulty difficulty;
    private GameType gameMode;
    private final String defaultSlotName;
    private String worldName;
    private boolean pvp;
    private boolean spawnMonsters;
    int spawnProtection;
    private boolean commandBlocks;
    private boolean forceGameMode;
    RealmsSlotOptionsScreen.SettingsSlider spawnProtectionButton;

    public RealmsSlotOptionsScreen(RealmsConfigureWorldScreen pParent, RealmsWorldOptions pOptions, RealmsServer.WorldType pWorldType, int pActiveSlot) {
        super(Component.translatable("mco.configure.world.buttons.options"));
        this.parentScreen = pParent;
        this.options = pOptions;
        this.worldType = pWorldType;
        this.difficulty = findByIndex(DIFFICULTIES, pOptions.difficulty, 2);
        this.gameMode = findByIndex(GAME_MODES, pOptions.gameMode, 0);
        this.defaultSlotName = pOptions.getDefaultSlotName(pActiveSlot);
        this.setWorldName(pOptions.getSlotName(pActiveSlot));
        if (pWorldType == RealmsServer.WorldType.NORMAL) {
            this.pvp = pOptions.pvp;
            this.spawnProtection = pOptions.spawnProtection;
            this.forceGameMode = pOptions.forceGameMode;
            this.spawnMonsters = pOptions.spawnMonsters;
            this.commandBlocks = pOptions.commandBlocks;
        } else {
            this.pvp = true;
            this.spawnProtection = 0;
            this.forceGameMode = false;
            this.spawnMonsters = true;
            this.commandBlocks = true;
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parentScreen);
    }

    private static <T> T findByIndex(List<T> pList, int pIndex, int pFallback) {
        try {
            return pList.get(pIndex);
        } catch (IndexOutOfBoundsException indexoutofboundsexception) {
            return pList.get(pFallback);
        }
    }

    private static <T> int findIndex(List<T> pList, T pObject, int pFallback) {
        int i = pList.indexOf(pObject);
        return i == -1 ? pFallback : i;
    }

    @Override
    public void init() {
        this.columnWidth = 170;
        this.column1X = this.width / 2 - this.columnWidth;
        int i = this.width / 2 + 10;
        if (this.worldType != RealmsServer.WorldType.NORMAL) {
            Component component;
            if (this.worldType == RealmsServer.WorldType.ADVENTUREMAP) {
                component = Component.translatable("mco.configure.world.edit.subscreen.adventuremap");
            } else if (this.worldType == RealmsServer.WorldType.INSPIRATION) {
                component = Component.translatable("mco.configure.world.edit.subscreen.inspiration");
            } else {
                component = Component.translatable("mco.configure.world.edit.subscreen.experience");
            }

            this.addLabel(new RealmsLabel(component, this.width / 2, 26, 16711680));
        }

        this.nameEdit = this.addWidget(
            new EditBox(this.minecraft.font, this.column1X, row(1), this.columnWidth, 20, null, Component.translatable("mco.configure.world.edit.slot.name"))
        );
        this.nameEdit.setMaxLength(10);
        this.nameEdit.setValue(this.worldName);
        this.nameEdit.setResponder(this::setWorldName);
        CycleButton<Boolean> cyclebutton3 = this.addRenderableWidget(
            CycleButton.onOffBuilder(this.pvp)
                .create(
                    i, row(1), this.columnWidth, 20, Component.translatable("mco.configure.world.pvp"), (p_167546_, p_167547_) -> this.pvp = p_167547_
                )
        );
        this.addRenderableWidget(
            CycleButton.builder(GameType::getShortDisplayName)
                .withValues(GAME_MODES)
                .withInitialValue(this.gameMode)
                .create(
                    this.column1X,
                    row(3),
                    this.columnWidth,
                    20,
                    Component.translatable("selectWorld.gameMode"),
                    (p_167515_, p_167516_) -> this.gameMode = p_167516_
                )
        );
        this.spawnProtectionButton = this.addRenderableWidget(new RealmsSlotOptionsScreen.SettingsSlider(i, row(3), this.columnWidth, this.spawnProtection, 0.0F, 16.0F));
        Component component1 = Component.translatable("mco.configure.world.spawn_toggle.message");
        CycleButton<Boolean> cyclebutton = CycleButton.onOffBuilder(this.difficulty != Difficulty.PEACEFUL && this.spawnMonsters)
            .create(
                i,
                row(5),
                this.columnWidth,
                20,
                Component.translatable("mco.configure.world.spawnMonsters"),
                this.confirmDangerousOption(component1, p_231327_ -> this.spawnMonsters = p_231327_)
            );
        this.addRenderableWidget(
            CycleButton.builder(Difficulty::getDisplayName)
                .withValues(DIFFICULTIES)
                .withInitialValue(this.difficulty)
                .create(this.column1X, row(5), this.columnWidth, 20, Component.translatable("options.difficulty"), (p_167519_, p_167520_) -> {
                    this.difficulty = p_167520_;
                    if (this.worldType == RealmsServer.WorldType.NORMAL) {
                        boolean flag = this.difficulty != Difficulty.PEACEFUL;
                        cyclebutton.active = flag;
                        cyclebutton.setValue(flag && this.spawnMonsters);
                    }
                })
        );
        this.addRenderableWidget(cyclebutton);
        CycleButton<Boolean> cyclebutton1 = this.addRenderableWidget(
            CycleButton.onOffBuilder(this.forceGameMode)
                .create(
                    this.column1X,
                    row(7),
                    this.columnWidth,
                    20,
                    Component.translatable("mco.configure.world.forceGameMode"),
                    (p_167534_, p_167535_) -> this.forceGameMode = p_167535_
                )
        );
        CycleButton<Boolean> cyclebutton2 = this.addRenderableWidget(
            CycleButton.onOffBuilder(this.commandBlocks)
                .create(
                    i,
                    row(7),
                    this.columnWidth,
                    20,
                    Component.translatable("mco.configure.world.commandBlocks"),
                    (p_167522_, p_167523_) -> this.commandBlocks = p_167523_
                )
        );
        if (this.worldType != RealmsServer.WorldType.NORMAL) {
            cyclebutton3.active = false;
            cyclebutton.active = false;
            this.spawnProtectionButton.active = false;
            cyclebutton2.active = false;
            cyclebutton1.active = false;
        }

        if (this.difficulty == Difficulty.PEACEFUL) {
            cyclebutton.active = false;
        }

        this.addRenderableWidget(
            Button.builder(Component.translatable("mco.configure.world.buttons.done"), p_89910_ -> this.saveSettings())
                .bounds(this.column1X, row(13), this.columnWidth, 20)
                .build()
        );
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, p_340725_ -> this.onClose()).bounds(i, row(13), this.columnWidth, 20).build());
    }

    private CycleButton.OnValueChange<Boolean> confirmDangerousOption(Component pQuestion, Consumer<Boolean> pOnPress) {
        return (p_340728_, p_340729_) -> {
            if (p_340729_) {
                pOnPress.accept(true);
            } else {
                this.minecraft.setScreen(RealmsPopups.warningPopupScreen(this, pQuestion, p_340724_ -> {
                    pOnPress.accept(false);
                    p_340724_.onClose();
                }));
            }
        };
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(this.getTitle(), this.createLabelNarration());
    }

    @Override
    public void render(GuiGraphics p_283210_, int p_283172_, int p_281531_, float p_283191_) {
        super.render(p_283210_, p_283172_, p_281531_, p_283191_);
        p_283210_.drawCenteredString(this.font, this.title, this.width / 2, 17, -1);
        p_283210_.drawString(this.font, NAME_LABEL, this.column1X + this.columnWidth / 2 - this.font.width(NAME_LABEL) / 2, row(0) - 5, -1);
        this.nameEdit.render(p_283210_, p_283172_, p_281531_, p_283191_);
    }

    private void setWorldName(String pName) {
        if (pName.equals(this.defaultSlotName)) {
            this.worldName = "";
        } else {
            this.worldName = pName;
        }
    }

    private void saveSettings() {
        int i = findIndex(DIFFICULTIES, this.difficulty, 2);
        int j = findIndex(GAME_MODES, this.gameMode, 0);
        if (this.worldType != RealmsServer.WorldType.ADVENTUREMAP
            && this.worldType != RealmsServer.WorldType.EXPERIENCE
            && this.worldType != RealmsServer.WorldType.INSPIRATION) {
            boolean flag = this.worldType == RealmsServer.WorldType.NORMAL && this.difficulty != Difficulty.PEACEFUL && this.spawnMonsters;
            this.parentScreen
                .saveSlotSettings(
                    new RealmsWorldOptions(
                        this.pvp,
                        flag,
                        this.spawnProtection,
                        this.commandBlocks,
                        i,
                        j,
                        this.options.hardcore,
                        this.forceGameMode,
                        this.worldName,
                        this.options.version,
                        this.options.compatibility
                    )
                );
        } else {
            this.parentScreen
                .saveSlotSettings(
                    new RealmsWorldOptions(
                        this.options.pvp,
                        this.options.spawnMonsters,
                        this.options.spawnProtection,
                        this.options.commandBlocks,
                        i,
                        j,
                        this.options.hardcore,
                        this.options.forceGameMode,
                        this.worldName,
                        this.options.version,
                        this.options.compatibility
                    )
                );
        }
    }

    @OnlyIn(Dist.CLIENT)
    class SettingsSlider extends AbstractSliderButton {
        private final double minValue;
        private final double maxValue;

        public SettingsSlider(final int pX, final int pY, final int pWidth, final int pValue, final float pMinValue, final float pMaxValue) {
            super(pX, pY, pWidth, 20, CommonComponents.EMPTY, 0.0);
            this.minValue = (double)pMinValue;
            this.maxValue = (double)pMaxValue;
            this.value = (double)((Mth.clamp((float)pValue, pMinValue, pMaxValue) - pMinValue) / (pMaxValue - pMinValue));
            this.updateMessage();
        }

        @Override
        public void applyValue() {
            if (RealmsSlotOptionsScreen.this.spawnProtectionButton.active) {
                RealmsSlotOptionsScreen.this.spawnProtection = (int)Mth.lerp(Mth.clamp(this.value, 0.0, 1.0), this.minValue, this.maxValue);
            }
        }

        @Override
        protected void updateMessage() {
            this.setMessage(
                CommonComponents.optionNameValue(
                    RealmsSlotOptionsScreen.SPAWN_PROTECTION_TEXT,
                    (Component)(RealmsSlotOptionsScreen.this.spawnProtection == 0
                        ? CommonComponents.OPTION_OFF
                        : Component.literal(String.valueOf(RealmsSlotOptionsScreen.this.spawnProtection)))
                )
            );
        }
    }
}