package com.formlessDragon.ae2gtaddon.mixin.ae2;

import ae2.client.gui.AEBaseGui;
import ae2.client.gui.Icon;
import ae2.client.gui.Tooltip;
import ae2.client.gui.WidgetContainer;
import ae2.client.gui.me.patternencode.ProcessingEncodingPanel;
import ae2.client.gui.widgets.ActionButton;
import ae2.client.gui.widgets.Scrollbar;
import ae2.client.gui.widgets.ToggleButton;
import ae2.container.me.patternencode.ContainerPatternEncodingTerm;
import ae2.integration.Integrations;
import com.formlessDragon.ae2gtaddon.integration.hei.AutoPatternItemNaming;
import com.formlessDragon.ae2gtaddon.integration.hei.PatternEncodingPanelContainerAccess;
import com.formlessDragon.ae2gtaddon.integration.hei.ProcessingPatternCircuitImportMode;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

@Mixin(value = ProcessingEncodingPanel.class, remap = false)
public abstract class MixinProcessingEncodingPanel {
    @Unique
    private static final int AE2GTADDON_BUTTON_SPACING = 10;

    @Unique
    private static final float AE2GTADDON_HALF_SIZE_ICON_SCALE = 0.5F;

    @Shadow
    @Final
    private ActionButton clearBtn;

    @Unique
    private ToggleButton ae2gtaddon$ignoreCircuitButton;

    @Unique
    private ToggleButton ae2gtaddon$autoNameButton;

    @Unique
    private ProcessingPatternCircuitImportMode ae2gtaddon$importMode;

    @Unique
    private AutoPatternItemNaming ae2gtaddon$namingMode;

    @Unique
    private ContainerPatternEncodingTerm ae2gtaddon$container;

    @Redirect(
        method = "<init>(Lae2/client/gui/AEBaseGui;Lae2/client/gui/WidgetContainer;)V",
        at = @At(
            value = "INVOKE",
            target = "Lae2/client/gui/widgets/Scrollbar;setRange(III)V"),
        require = 1)
    private void ae2gtaddon$includeAllOutputSlots(
        Scrollbar scrollbar, int min, int max, int pageSize
    ) {
        int outputSlots = ((PatternEncodingPanelContainerAccess) this)
            .ae2gtaddon$getContainer()
            .getProcessingOutputSlots().length;
        scrollbar.setRange(min, Math.max(max, outputSlots - pageSize), pageSize);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void ae2gtaddon$addHeiImportButtons(
        AEBaseGui<?> screen, WidgetContainer widgets, CallbackInfo ci
    ) {
        this.ae2gtaddon$container = ((PatternEncodingPanelContainerAccess) this).ae2gtaddon$getContainer();
        this.ae2gtaddon$importMode = (ProcessingPatternCircuitImportMode) this.ae2gtaddon$container;
        this.ae2gtaddon$namingMode = (AutoPatternItemNaming) this.ae2gtaddon$container;

        this.ae2gtaddon$ignoreCircuitButton = new ToggleButton(
            Icon.S_SUBSTITUTION_DISABLED,
            Icon.S_SUBSTITUTION_ENABLED,
            this::ae2gtaddon$setIgnoreCircuit);
        this.ae2gtaddon$ignoreCircuitButton.setHalfSize(true);
        this.ae2gtaddon$ignoreCircuitButton.setDisableBackground(true);
        this.ae2gtaddon$ignoreCircuitButton.setTooltipOn(List.of(
            new TextComponentTranslation("gui.tooltips.ae2gtaddon.IgnoreCircuitHeiImport"),
            new TextComponentTranslation("gui.tooltips.ae2gtaddon.IgnoreCircuitHeiImportOn")));
        this.ae2gtaddon$ignoreCircuitButton.setTooltipOff(List.of(
            new TextComponentTranslation("gui.tooltips.ae2gtaddon.IgnoreCircuitHeiImport"),
            new TextComponentTranslation("gui.tooltips.ae2gtaddon.IgnoreCircuitHeiImportOff")));

        this.ae2gtaddon$autoNameButton = new ToggleButton(
            Icon.PATTERN_AUTO_FILL_ON,
            Icon.PATTERN_AUTO_FILL_OFF,
            this::ae2gtaddon$setAutoNameItems);
        this.ae2gtaddon$autoNameButton.setHalfSize(true);
        this.ae2gtaddon$autoNameButton.setDisableBackground(true);
        this.ae2gtaddon$autoNameButton.setIconScale(AE2GTADDON_HALF_SIZE_ICON_SCALE);
        this.ae2gtaddon$autoNameButton.setTooltipOn(List.of(
            new TextComponentTranslation("gui.tooltips.ae2gtaddon.AutoNameHeiImport"),
            new TextComponentTranslation("gui.tooltips.ae2gtaddon.AutoNameHeiImportOn")));
        this.ae2gtaddon$autoNameButton.setTooltipOff(List.of(
            new TextComponentTranslation("gui.tooltips.ae2gtaddon.AutoNameHeiImport"),
            new TextComponentTranslation("gui.tooltips.ae2gtaddon.AutoNameHeiImportOff")));
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"))
    private void ae2gtaddon$updateHeiImportButtons(CallbackInfo ci) {
        this.ae2gtaddon$ignoreCircuitButton.setState(this.ae2gtaddon$shouldIgnoreCircuit());
        this.ae2gtaddon$ignoreCircuitButton.x = this.clearBtn.x;
        this.ae2gtaddon$ignoreCircuitButton.y = this.clearBtn.y + AE2GTADDON_BUTTON_SPACING;

        this.ae2gtaddon$autoNameButton.setState(this.ae2gtaddon$namingMode.ae2gtaddon$autoNamePatternItems());
        this.ae2gtaddon$autoNameButton.x = this.clearBtn.x;
        this.ae2gtaddon$autoNameButton.y = this.clearBtn.y + 3 * AE2GTADDON_BUTTON_SPACING;
    }

    @Inject(method = "setVisible", at = @At("TAIL"))
    private void ae2gtaddon$setHeiImportButtonsVisible(boolean visible, CallbackInfo ci) {
        this.ae2gtaddon$ignoreCircuitButton.setVisibility(visible);
        this.ae2gtaddon$autoNameButton.setVisibility(visible);
    }

    @Unique
    private boolean ae2gtaddon$shouldIgnoreCircuit() {
        return this.ae2gtaddon$importMode.ae2gtaddon$ignoreCircuit();
    }

    @Unique
    private void ae2gtaddon$setIgnoreCircuit(boolean ignoreCircuit) {
        this.ae2gtaddon$importMode.ae2gtaddon$setIgnoreCircuit(ignoreCircuit);
    }

    @Unique
    private void ae2gtaddon$setAutoNameItems(boolean autoName) {
        this.ae2gtaddon$namingMode.ae2gtaddon$setAutoNamePatternItems(autoName);
        if (autoName) {
            // Auto naming keeps every occurrence in its own slot, so use the existing HEI merge switch to match.
            this.ae2gtaddon$container.setMergeHeiProcessingInputs(false);
        }
    }

    @Unique
    @SuppressWarnings("all")
    public void populateScreen(Consumer<GuiButton> addWidget, Rectangle bounds, AEBaseGui<?> baseGui) {
        if(Integrations.hei().isEnabled()) {
            addWidget.accept(this.ae2gtaddon$ignoreCircuitButton);
            addWidget.accept(this.ae2gtaddon$autoNameButton);
        }
    }

    @Unique
    @SuppressWarnings("all")
    public boolean wantsAllMouseDownEvents() {
        return true;
    }

    @Unique
    @SuppressWarnings("all")
    public Tooltip getTooltip(int mouseX, int mouseY) {
        if(!Integrations.hei().isEnabled()) {
            return null;
        }

        if(this.ae2gtaddon$autoNameButton.visible && this.ae2gtaddon$autoNameButton.isMouseOver()) {
            return new Tooltip(ae2gtaddon$autoNameButton.getTooltipMessage());
        }
        if(this.ae2gtaddon$ignoreCircuitButton.visible && this.ae2gtaddon$ignoreCircuitButton.isMouseOver()) {
            return new Tooltip(ae2gtaddon$ignoreCircuitButton.getTooltipMessage());
        }
        return null;
    }
}
