package net.minecraft.client;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.ClipboardManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.TextureUtil;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyboardHandler {
    public static final int DEBUG_CRASH_TIME = 10000;
    private final Minecraft minecraft;
    private final ClipboardManager clipboardManager = new ClipboardManager();
    private long debugCrashKeyTime = -1L;
    private long debugCrashKeyReportedTime = -1L;
    private long debugCrashKeyReportedCount = -1L;
    private boolean handledDebugKey;

    public KeyboardHandler(Minecraft pMinecraft) {
        this.minecraft = pMinecraft;
    }

    private boolean handleChunkDebugKeys(int pKeyCode) {
        switch (pKeyCode) {
            case 69:
                this.minecraft.sectionPath = !this.minecraft.sectionPath;
                this.debugFeedback("SectionPath: {0}", this.minecraft.sectionPath ? "shown" : "hidden");
                return true;
            case 70:
                boolean flag1 = FogRenderer.toggleFog();
                this.debugFeedback("Fog: {0}", flag1 ? "enabled" : "disabled");
                return true;
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 77:
            case 78:
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            default:
                return false;
            case 76:
                this.minecraft.smartCull = !this.minecraft.smartCull;
                this.debugFeedback("SmartCull: {0}", this.minecraft.smartCull ? "enabled" : "disabled");
                return true;
            case 79:
                boolean flag = this.minecraft.debugRenderer.toggleRenderOctree();
                this.debugFeedback("Frustum culling Octree: {0}", flag ? "enabled" : "disabled");
                return true;
            case 85:
                if (Screen.hasShiftDown()) {
                    this.minecraft.levelRenderer.killFrustum();
                    this.debugFeedback("Killed frustum");
                } else {
                    this.minecraft.levelRenderer.captureFrustum();
                    this.debugFeedback("Captured frustum");
                }

                return true;
            case 86:
                this.minecraft.sectionVisibility = !this.minecraft.sectionVisibility;
                this.debugFeedback("SectionVisibility: {0}", this.minecraft.sectionVisibility ? "enabled" : "disabled");
                return true;
            case 87:
                this.minecraft.wireframe = !this.minecraft.wireframe;
                this.debugFeedback("WireFrame: {0}", this.minecraft.wireframe ? "enabled" : "disabled");
                return true;
        }
    }

    private void debugComponent(ChatFormatting pFormatting, Component pMessage) {
        this.minecraft
            .gui
            .getChat()
            .addMessage(
                Component.empty()
                    .append(Component.translatable("debug.prefix").withStyle(pFormatting, ChatFormatting.BOLD))
                    .append(CommonComponents.SPACE)
                    .append(pMessage)
            );
    }

    private void debugFeedbackComponent(Component pMessage) {
        this.debugComponent(ChatFormatting.YELLOW, pMessage);
    }

    private void debugFeedbackTranslated(String pMessage, Object... pArgs) {
        this.debugFeedbackComponent(Component.translatableEscape(pMessage, pArgs));
    }

    private void debugWarningTranslated(String pMessage, Object... pArgs) {
        this.debugComponent(ChatFormatting.RED, Component.translatableEscape(pMessage, pArgs));
    }

    private void debugFeedback(String pMessage, Object... pArgs) {
        this.debugFeedbackComponent(Component.literal(MessageFormat.format(pMessage, pArgs)));
    }

    private boolean handleDebugKeys(int pKey) {
        if (this.debugCrashKeyTime > 0L && this.debugCrashKeyTime < Util.getMillis() - 100L) {
            return true;
        } else {
            switch (pKey) {
                case 49:
                    this.minecraft.getDebugOverlay().toggleProfilerChart();
                    return true;
                case 50:
                    this.minecraft.getDebugOverlay().toggleFpsCharts();
                    return true;
                case 51:
                    this.minecraft.getDebugOverlay().toggleNetworkCharts();
                    return true;
                case 65:
                    this.minecraft.levelRenderer.allChanged();
                    this.debugFeedbackTranslated("debug.reload_chunks.message");
                    return true;
                case 66:
                    boolean flag = !this.minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes();
                    this.minecraft.getEntityRenderDispatcher().setRenderHitBoxes(flag);
                    this.debugFeedbackTranslated(flag ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off");
                    return true;
                case 67:
                    if (this.minecraft.player.isReducedDebugInfo()) {
                        return false;
                    } else {
                        ClientPacketListener clientpacketlistener = this.minecraft.player.connection;
                        if (clientpacketlistener == null) {
                            return false;
                        }

                        this.debugFeedbackTranslated("debug.copy_location.message");
                        this.setClipboard(
                            String.format(
                                Locale.ROOT,
                                "/execute in %s run tp @s %.2f %.2f %.2f %.2f %.2f",
                                this.minecraft.player.level().dimension().location(),
                                this.minecraft.player.getX(),
                                this.minecraft.player.getY(),
                                this.minecraft.player.getZ(),
                                this.minecraft.player.getYRot(),
                                this.minecraft.player.getXRot()
                            )
                        );
                        return true;
                    }
                case 68:
                    if (this.minecraft.gui != null) {
                        this.minecraft.gui.getChat().clearMessages(false);
                    }

                    return true;
                case 71:
                    boolean flag1 = this.minecraft.debugRenderer.switchRenderChunkborder();
                    this.debugFeedbackTranslated(flag1 ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off");
                    return true;
                case 72:
                    this.minecraft.options.advancedItemTooltips = !this.minecraft.options.advancedItemTooltips;
                    this.debugFeedbackTranslated(this.minecraft.options.advancedItemTooltips ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off");
                    this.minecraft.options.save();
                    return true;
                case 73:
                    if (!this.minecraft.player.isReducedDebugInfo()) {
                        this.copyRecreateCommand(this.minecraft.player.hasPermissions(2), !Screen.hasShiftDown());
                    }

                    return true;
                case 76:
                    if (this.minecraft.debugClientMetricsStart(this::debugFeedbackComponent)) {
                        this.debugFeedbackTranslated("debug.profiling.start", 10);
                    }

                    return true;
                case 78:
                    if (!this.minecraft.player.hasPermissions(2)) {
                        this.debugFeedbackTranslated("debug.creative_spectator.error");
                    } else if (!this.minecraft.player.isSpectator()) {
                        this.minecraft.player.connection.sendUnsignedCommand("gamemode spectator");
                    } else {
                        this.minecraft
                            .player
                            .connection
                            .sendUnsignedCommand("gamemode " + MoreObjects.firstNonNull(this.minecraft.gameMode.getPreviousPlayerMode(), GameType.CREATIVE).getName());
                    }

                    return true;
                case 80:
                    this.minecraft.options.pauseOnLostFocus = !this.minecraft.options.pauseOnLostFocus;
                    this.minecraft.options.save();
                    this.debugFeedbackTranslated(this.minecraft.options.pauseOnLostFocus ? "debug.pause_focus.on" : "debug.pause_focus.off");
                    return true;
                case 81:
                    this.debugFeedbackTranslated("debug.help.message");
                    ChatComponent chatcomponent = this.minecraft.gui.getChat();
                    chatcomponent.addMessage(Component.translatable("debug.reload_chunks.help"));
                    chatcomponent.addMessage(Component.translatable("debug.show_hitboxes.help"));
                    chatcomponent.addMessage(Component.translatable("debug.copy_location.help"));
                    chatcomponent.addMessage(Component.translatable("debug.clear_chat.help"));
                    chatcomponent.addMessage(Component.translatable("debug.chunk_boundaries.help"));
                    chatcomponent.addMessage(Component.translatable("debug.advanced_tooltips.help"));
                    chatcomponent.addMessage(Component.translatable("debug.inspect.help"));
                    chatcomponent.addMessage(Component.translatable("debug.profiling.help"));
                    chatcomponent.addMessage(Component.translatable("debug.creative_spectator.help"));
                    chatcomponent.addMessage(Component.translatable("debug.pause_focus.help"));
                    chatcomponent.addMessage(Component.translatable("debug.help.help"));
                    chatcomponent.addMessage(Component.translatable("debug.dump_dynamic_textures.help"));
                    chatcomponent.addMessage(Component.translatable("debug.reload_resourcepacks.help"));
                    chatcomponent.addMessage(Component.translatable("debug.pause.help"));
                    chatcomponent.addMessage(Component.translatable("debug.gamemodes.help"));
                    return true;
                case 83:
                    Path path = this.minecraft.gameDirectory.toPath().toAbsolutePath();
                    Path path1 = TextureUtil.getDebugTexturePath(path);
                    this.minecraft.getTextureManager().dumpAllSheets(path1);
                    Component component = Component.literal(path.relativize(path1).toString())
                        .withStyle(ChatFormatting.UNDERLINE)
                        .withStyle(p_276097_ -> p_276097_.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path1.toFile().toString())));
                    this.debugFeedbackTranslated("debug.dump_dynamic_textures", component);
                    return true;
                case 84:
                    this.debugFeedbackTranslated("debug.reload_resourcepacks.message");
                    this.minecraft.reloadResourcePacks();
                    return true;
                case 293:
                    if (!this.minecraft.player.hasPermissions(2)) {
                        this.debugFeedbackTranslated("debug.gamemodes.error");
                    } else {
                        this.minecraft.setScreen(new GameModeSwitcherScreen());
                    }

                    return true;
                default:
                    return false;
            }
        }
    }

    private void copyRecreateCommand(boolean pPrivileged, boolean pAskServer) {
        HitResult hitresult = this.minecraft.hitResult;
        if (hitresult != null) {
            switch (hitresult.getType()) {
                case BLOCK:
                    BlockPos blockpos = ((BlockHitResult)hitresult).getBlockPos();
                    Level level = this.minecraft.player.level();
                    BlockState blockstate = level.getBlockState(blockpos);
                    if (pPrivileged) {
                        if (pAskServer) {
                            this.minecraft.player.connection.getDebugQueryHandler().queryBlockEntityTag(blockpos, p_90947_ -> {
                                this.copyCreateBlockCommand(blockstate, blockpos, p_90947_);
                                this.debugFeedbackTranslated("debug.inspect.server.block");
                            });
                        } else {
                            BlockEntity blockentity = level.getBlockEntity(blockpos);
                            CompoundTag compoundtag1 = blockentity != null ? blockentity.saveWithoutMetadata(level.registryAccess()) : null;
                            this.copyCreateBlockCommand(blockstate, blockpos, compoundtag1);
                            this.debugFeedbackTranslated("debug.inspect.client.block");
                        }
                    } else {
                        this.copyCreateBlockCommand(blockstate, blockpos, null);
                        this.debugFeedbackTranslated("debug.inspect.client.block");
                    }
                    break;
                case ENTITY:
                    Entity entity = ((EntityHitResult)hitresult).getEntity();
                    ResourceLocation resourcelocation = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                    if (pPrivileged) {
                        if (pAskServer) {
                            this.minecraft.player.connection.getDebugQueryHandler().queryEntityTag(entity.getId(), p_90921_ -> {
                                this.copyCreateEntityCommand(resourcelocation, entity.position(), p_90921_);
                                this.debugFeedbackTranslated("debug.inspect.server.entity");
                            });
                        } else {
                            CompoundTag compoundtag = entity.saveWithoutId(new CompoundTag());
                            this.copyCreateEntityCommand(resourcelocation, entity.position(), compoundtag);
                            this.debugFeedbackTranslated("debug.inspect.client.entity");
                        }
                    } else {
                        this.copyCreateEntityCommand(resourcelocation, entity.position(), null);
                        this.debugFeedbackTranslated("debug.inspect.client.entity");
                    }
            }
        }
    }

    private void copyCreateBlockCommand(BlockState pState, BlockPos pPos, @Nullable CompoundTag pCompound) {
        StringBuilder stringbuilder = new StringBuilder(BlockStateParser.serialize(pState));
        if (pCompound != null) {
            stringbuilder.append(pCompound);
        }

        String s = String.format(Locale.ROOT, "/setblock %d %d %d %s", pPos.getX(), pPos.getY(), pPos.getZ(), stringbuilder);
        this.setClipboard(s);
    }

    private void copyCreateEntityCommand(ResourceLocation pEntityId, Vec3 pPos, @Nullable CompoundTag pCompound) {
        String s;
        if (pCompound != null) {
            pCompound.remove("UUID");
            pCompound.remove("Pos");
            pCompound.remove("Dimension");
            String s1 = NbtUtils.toPrettyComponent(pCompound).getString();
            s = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f %s", pEntityId, pPos.x, pPos.y, pPos.z, s1);
        } else {
            s = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f", pEntityId, pPos.x, pPos.y, pPos.z);
        }

        this.setClipboard(s);
    }

    public void keyPress(long pWindowPointer, int pKey, int pScanCode, int pAction, int pModifiers) {
        if (pWindowPointer == this.minecraft.getWindow().getWindow()) {
            this.minecraft.getFramerateLimitTracker().onInputReceived();
            boolean flag = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292);
            if (this.debugCrashKeyTime > 0L) {
                if (!InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67) || !flag) {
                    this.debugCrashKeyTime = -1L;
                }
            } else if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67) && flag) {
                this.handledDebugKey = true;
                this.debugCrashKeyTime = Util.getMillis();
                this.debugCrashKeyReportedTime = Util.getMillis();
                this.debugCrashKeyReportedCount = 0L;
            }

            Screen screen = this.minecraft.screen;
            if (screen != null) {
                switch (pKey) {
                    case 258:
                        this.minecraft.setLastInputType(InputType.KEYBOARD_TAB);
                    case 259:
                    case 260:
                    case 261:
                    default:
                        break;
                    case 262:
                    case 263:
                    case 264:
                    case 265:
                        this.minecraft.setLastInputType(InputType.KEYBOARD_ARROW);
                }
            }

            if (pAction == 1 && (!(this.minecraft.screen instanceof KeyBindsScreen) || ((KeyBindsScreen)screen).lastKeySelection <= Util.getMillis() - 20L)) {
                if (this.minecraft.options.keyFullscreen.matches(pKey, pScanCode)) {
                    this.minecraft.getWindow().toggleFullScreen();
                    this.minecraft.options.fullscreen().set(this.minecraft.getWindow().isFullscreen());
                    return;
                }

                if (this.minecraft.options.keyScreenshot.matches(pKey, pScanCode)) {
                    if (Screen.hasControlDown()) {
                    }

                    Screenshot.grab(
                        this.minecraft.gameDirectory,
                        this.minecraft.getMainRenderTarget(),
                        p_90917_ -> this.minecraft.execute(() -> this.minecraft.gui.getChat().addMessage(p_90917_))
                    );
                    return;
                }
            }

            if (pAction != 0) {
                boolean flag1 = screen == null || !(screen.getFocused() instanceof EditBox) || !((EditBox)screen.getFocused()).canConsumeInput();
                if (flag1) {
                    if (Screen.hasControlDown() && pKey == 66 && this.minecraft.getNarrator().isActive() && this.minecraft.options.narratorHotkey().get()) {
                        boolean flag2 = this.minecraft.options.narrator().get() == NarratorStatus.OFF;
                        this.minecraft.options.narrator().set(NarratorStatus.byId(this.minecraft.options.narrator().get().getId() + 1));
                        this.minecraft.options.save();
                        if (screen != null) {
                            screen.updateNarratorStatus(flag2);
                        }
                    }

                    LocalPlayer localplayer = this.minecraft.player;
                }
            }

            if (screen != null) {
                try {
                    if (pAction != 1 && pAction != 2) {
                        if (pAction == 0 && net.minecraftforge.client.ForgeHooksClient.onScreenKeyReleased(screen, pKey, pScanCode, pModifiers)) {
                            return;
                        }
                    } else {
                        screen.afterKeyboardAction();
                        if (net.minecraftforge.client.ForgeHooksClient.onScreenKeyPressed(screen, pKey, pScanCode, pModifiers)) {
                            return;
                        }
                    }
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.forThrowable(throwable, "keyPressed event handler");
                    screen.fillCrashDetails(crashreport);
                    CrashReportCategory crashreportcategory = crashreport.addCategory("Key");
                    crashreportcategory.setDetail("Key", pKey);
                    crashreportcategory.setDetail("Scancode", pScanCode);
                    crashreportcategory.setDetail("Mods", pModifiers);
                    throw new ReportedException(crashreport);
                }
            }

            InputConstants.Key inputconstants$key;
            boolean flag3;
            boolean flag6;
            label197: {
                inputconstants$key = InputConstants.getKey(pKey, pScanCode);
                flag3 = this.minecraft.screen == null;
                label153:
                if (!flag3) {
                    if (this.minecraft.screen instanceof PauseScreen pausescreen && !pausescreen.showsPauseMenu()) {
                        break label153;
                    }

                    flag6 = false;
                    break label197;
                }

                flag6 = true;
            }

            boolean flag4 = flag6;
            if (pAction == 0) {
                KeyMapping.set(inputconstants$key, false);
                if (flag4 && pKey == 292) {
                    if (this.handledDebugKey) {
                        this.handledDebugKey = false;
                    } else {
                        this.minecraft.getDebugOverlay().toggleOverlay();
                    }
                }
            } else {
                boolean flag5 = false;
                if (flag4) {
                    if (pKey == 293 && this.minecraft.gameRenderer != null) {
                        this.minecraft.gameRenderer.togglePostEffect();
                    }

                    if (pKey == 256) {
                        this.minecraft.pauseGame(flag);
                        flag5 |= flag;
                    }

                    flag5 |= flag && this.handleDebugKeys(pKey);
                    this.handledDebugKey |= flag5;
                    if (pKey == 290) {
                        this.minecraft.options.hideGui = !this.minecraft.options.hideGui;
                    }

                    if (this.minecraft.getDebugOverlay().showProfilerChart() && !flag && pKey >= 48 && pKey <= 57) {
                        this.minecraft.getDebugOverlay().getProfilerPieChart().profilerPieChartKeyPress(pKey - 48);
                    }
                }

                if (flag3) {
                    if (flag5) {
                        KeyMapping.set(inputconstants$key, false);
                    } else {
                        KeyMapping.set(inputconstants$key, true);
                        KeyMapping.click(inputconstants$key);
                    }
                }
            }
            net.minecraftforge.client.ForgeHooksClient.onKeyInput(pKey, pScanCode, pAction, pModifiers);
        }
    }

    private void charTyped(long pWindowPointer, int pCodePoint, int pModifiers) {
        if (pWindowPointer == this.minecraft.getWindow().getWindow()) {
            Screen screen = this.minecraft.screen;
            if (screen != null && this.minecraft.getOverlay() == null) {
                try {
                    if (Character.isBmpCodePoint(pCodePoint)) {
                        net.minecraftforge.client.ForgeHooksClient.onScreenCharTyped(screen, (char)pCodePoint, pModifiers);
                    } else if (Character.isValidCodePoint(pCodePoint)) {
                        net.minecraftforge.client.ForgeHooksClient.onScreenCharTyped(screen, Character.highSurrogate(pCodePoint), pModifiers);
                        net.minecraftforge.client.ForgeHooksClient.onScreenCharTyped(screen, Character.lowSurrogate(pCodePoint), pModifiers);
                    }
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.forThrowable(throwable, "charTyped event handler");
                    screen.fillCrashDetails(crashreport);
                    CrashReportCategory crashreportcategory = crashreport.addCategory("Key");
                    crashreportcategory.setDetail("Codepoint", pCodePoint);
                    crashreportcategory.setDetail("Mods", pModifiers);
                    throw new ReportedException(crashreport);
                }
            }
        }
    }

    public void setup(long pWindow) {
        InputConstants.setupKeyboardCallbacks(
            pWindow,
            (p_90939_, p_90940_, p_90941_, p_90942_, p_90943_) -> this.minecraft.execute(() -> this.keyPress(p_90939_, p_90940_, p_90941_, p_90942_, p_90943_)),
            (p_90935_, p_90936_, p_90937_) -> this.minecraft.execute(() -> this.charTyped(p_90935_, p_90936_, p_90937_))
        );
    }

    public String getClipboard() {
        return this.clipboardManager.getClipboard(this.minecraft.getWindow().getWindow(), (p_90878_, p_90879_) -> {
            if (p_90878_ != 65545) {
                this.minecraft.getWindow().defaultErrorCallback(p_90878_, p_90879_);
            }
        });
    }

    public void setClipboard(String pString) {
        if (!pString.isEmpty()) {
            this.clipboardManager.setClipboard(this.minecraft.getWindow().getWindow(), pString);
        }
    }

    public void tick() {
        if (this.debugCrashKeyTime > 0L) {
            long i = Util.getMillis();
            long j = 10000L - (i - this.debugCrashKeyTime);
            long k = i - this.debugCrashKeyReportedTime;
            if (j < 0L) {
                if (Screen.hasControlDown()) {
                    Blaze3D.youJustLostTheGame();
                }

                String s = "Manually triggered debug crash";
                CrashReport crashreport = new CrashReport("Manually triggered debug crash", new Throwable("Manually triggered debug crash"));
                CrashReportCategory crashreportcategory = crashreport.addCategory("Manual crash details");
                NativeModuleLister.addCrashSection(crashreportcategory);
                throw new ReportedException(crashreport);
            }

            if (k >= 1000L) {
                if (this.debugCrashKeyReportedCount == 0L) {
                    this.debugFeedbackTranslated("debug.crash.message");
                } else {
                    this.debugWarningTranslated("debug.crash.warning", Mth.ceil((float)j / 1000.0F));
                }

                this.debugCrashKeyReportedTime = i;
                this.debugCrashKeyReportedCount++;
            }
        }
    }
}
