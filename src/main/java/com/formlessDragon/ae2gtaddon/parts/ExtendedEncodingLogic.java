package com.formlessDragon.ae2gtaddon.parts;

import ae2.api.crafting.PatternDetailsHelper;
import ae2.api.inventories.InternalInventory;
import ae2.api.stacks.GenericStack;
import ae2.core.AELog;
import ae2.core.definitions.AEItems;
import ae2.crafting.pattern.AEProcessingPattern;
import ae2.helpers.IPatternTerminalLogicHost;
import ae2.parts.encoding.EncodingMode;
import ae2.parts.encoding.PatternEncodingLogic;
import ae2.util.ConfigInventory;
import ae2.util.inv.AppEngInternalInventory;
import ae2.util.inv.filter.AEItemDefinitionFilter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class ExtendedEncodingLogic extends PatternEncodingLogic {
    public static final int PROCESSING_INPUT_SLOTS = 32;
    public static final int PROCESSING_OUTPUT_SLOTS = 32;

    private final IPatternTerminalLogicHost host;
    private final AppEngInternalInventory blankPatternInv = new AppEngInternalInventory(this, 1);
    private final AppEngInternalInventory encodedPatternInv = new AppEngInternalInventory(this, 1);
    private final ExtendedEncodingDraft draft = new ExtendedEncodingDraft(this::saveChanges);
    private boolean loading;

    public ExtendedEncodingLogic(IPatternTerminalLogicHost host) {
        super(host);
        this.host = Objects.requireNonNull(host, "host");
        this.blankPatternInv.setFilter(new AEItemDefinitionFilter(AEItems.BLANK_PATTERN));
        this.encodedPatternInv.setMaxStackSize(0, 1);
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inventory, int slot) {
        if (!this.loading && inventory == this.encodedPatternInv) {
            loadEncodedPattern(this.encodedPatternInv.getStackInSlot(0));
        }
        saveChanges();
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inventory) {
        saveChanges();
    }

    @Override
    public boolean isClientSide() {
        return this.host.getLevel() == null || this.host.getLevel().isRemote;
    }

    @Override
    public void saveChanges() {
        if (!this.loading) {
            this.host.markForSave();
        }
    }

    private void loadEncodedPattern(ItemStack pattern) {
        if (pattern.isEmpty() || AEItems.BLANK_PATTERN.is(pattern)) {
            return;
        }
        try {
            var details = PatternDetailsHelper.decodePattern(pattern, this.host.getLevel());
            if (details instanceof AEProcessingPattern processing) {
                loadProcessingPattern(processing);
            } else {
                AELog.warn("Extended terminal ignored unsupported pattern %s", pattern);
            }
        } catch (IllegalArgumentException exception) {
            AELog.warn(exception, "Extended terminal rejected a processing pattern that exceeds its capacity");
        }
    }

    public boolean canLoadPattern(ItemStack stack) {
        var details = PatternDetailsHelper.decodePattern(stack, this.host.getLevel());
        if (!(details instanceof AEProcessingPattern pattern)) {
            return false;
        }
        return ExtendedEncodingMode.MULTI_IN.fits(pattern.getSparseInputs(), pattern.getSparseOutputs())
            || ExtendedEncodingMode.MULTI_OUT.fits(pattern.getSparseInputs(), pattern.getSparseOutputs());
    }

    public void loadProcessingPattern(AEProcessingPattern pattern) {
        loadProcessingPattern(pattern.getSparseInputs(), pattern.getSparseOutputs());
    }

    public void loadProcessingPattern(List<GenericStack> inputs, List<GenericStack> outputs) {
        this.draft.load(inputs, outputs);
    }

    public boolean canSwitchTo(ExtendedEncodingMode mode) {
        return this.draft.canSwitchTo(mode);
    }

    public ExtendedEncodingMode getExtendedMode() {
        return this.draft.mode();
    }

    public void setExtendedMode(ExtendedEncodingMode mode) {
        this.draft.setMode(mode);
    }

    public boolean isDraftValid() {
        try {
            this.draft.snapshot();
            return true;
        } catch (IllegalArgumentException exception) {
            AELog.warn(exception, "Refusing to encode an invalid extended terminal draft");
            return false;
        }
    }

    @Nullable
    public ItemStack encodeProcessingPattern(@Nullable String recipeTypeUid) {
        var snapshot = this.draft.snapshot();
        return snapshot.isEncodable()
            ? PatternDetailsHelper.encodeProcessingPattern(snapshot.inputs(), snapshot.outputs(), recipeTypeUid)
            : null;
    }

    @Override
    public EncodingMode getMode() {
        return EncodingMode.PROCESSING;
    }

    @Override
    public void setMode(EncodingMode mode) {
        if (Objects.requireNonNull(mode, "mode") != EncodingMode.PROCESSING) {
            AELog.warn("Rejected crafting mode for an extended processing terminal");
        }
    }

    @Override
    public ConfigInventory getEncodedInputInv() {
        return this.draft.inputs();
    }

    @Override
    public ConfigInventory getEncodedOutputInv() {
        return this.draft.outputs();
    }

    @Override
    public InternalInventory getBlankPatternInv() {
        return this.blankPatternInv;
    }

    @Override
    public InternalInventory getEncodedPatternInv() {
        return this.encodedPatternInv;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        this.loading = true;
        try {
            this.blankPatternInv.readFromNBT(data, "blankPattern");
            this.encodedPatternInv.readFromNBT(data, "encodedPattern");
            this.draft.inputs().readFromChildTag(data, "encodedInputs");
            this.draft.outputs().readFromChildTag(data, "encodedOutputs");
            this.draft.restoreMode(data.getString("extendedMode"));
        } finally {
            this.loading = false;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        data.setString("mode", EncodingMode.PROCESSING.name());
        data.setString("extendedMode", this.draft.mode().name());
        this.blankPatternInv.writeToNBT(data, "blankPattern");
        this.encodedPatternInv.writeToNBT(data, "encodedPattern");
        this.draft.inputs().writeToChildTag(data, "encodedInputs");
        this.draft.outputs().writeToChildTag(data, "encodedOutputs");
    }
}
