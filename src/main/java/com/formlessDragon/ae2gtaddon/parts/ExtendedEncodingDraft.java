package com.formlessDragon.ae2gtaddon.parts;

import ae2.api.stacks.GenericStack;
import ae2.core.AELog;
import ae2.util.ConfigInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ExtendedEncodingDraft {
    private final ConfigInventory inputs;
    private final ConfigInventory outputs;
    private final Runnable save;
    private ExtendedEncodingMode mode = ExtendedEncodingMode.MULTI_IN;

    public ExtendedEncodingDraft(Runnable save) {
        this.save = Objects.requireNonNull(save, "save");
        this.inputs = ConfigInventory.configStacks(32).allowOverstacking(true).changeListener(save).build();
        this.outputs = ConfigInventory.configStacks(32).allowOverstacking(true).changeListener(save).build();
    }

    public ConfigInventory inputs() { return this.inputs; }
    public ConfigInventory outputs() { return this.outputs; }
    public ExtendedEncodingMode mode() { return this.mode; }

    public boolean canSwitchTo(ExtendedEncodingMode target) {
        return Objects.requireNonNull(target, "target").fits(stacks(this.inputs), stacks(this.outputs));
    }

    public void setMode(ExtendedEncodingMode target) {
        if (!canSwitchTo(target)) {
            throw new IllegalArgumentException("Switching processing layout would hide occupied slots");
        }
        this.mode = target;
        this.save.run();
    }

    public void load(List<GenericStack> inputs, List<GenericStack> outputs) {
        ExtendedEncodingMode target = ExtendedEncodingMode.select(inputs, outputs, this.mode);
        validate(inputs, target.getInputSlots());
        validate(outputs, target.getOutputSlots());
        this.mode = target;
        fill(this.inputs, inputs);
        fill(this.outputs, outputs);
        this.save.run();
    }

    public void restoreMode(String serialized) {
        ExtendedEncodingMode preferred = ExtendedEncodingMode.MULTI_IN;
        if (!serialized.isEmpty()) {
            try {
                preferred = ExtendedEncodingMode.valueOf(serialized);
            } catch (IllegalArgumentException exception) {
                AELog.warn(exception, "Invalid extended terminal layout %s; recovering from saved slots", serialized);
            }
        }
        this.mode = preferred;
        try {
            this.mode = ExtendedEncodingMode.select(stacks(this.inputs), stacks(this.outputs), preferred);
        } catch (IllegalArgumentException exception) {
            AELog.warn(exception, "Saved terminal draft exceeds both layouts; preserving contents for correction");
        }
    }

    public Snapshot snapshot() {
        List<GenericStack> inputs = stacks(this.inputs);
        List<GenericStack> outputs = stacks(this.outputs);
        validate(inputs, this.mode.getInputSlots());
        validate(outputs, this.mode.getOutputSlots());
        return new Snapshot(trim(inputs), trim(outputs));
    }

    private static List<GenericStack> trim(List<GenericStack> stacks) {
        return java.util.Collections.unmodifiableList(new ArrayList<>(
            stacks.subList(0, ExtendedEncodingMode.occupiedLength(stacks))));
    }

    private static List<GenericStack> stacks(ConfigInventory inventory) {
        List<GenericStack> stacks = new ArrayList<>(inventory.size());
        for (int index = 0; index < inventory.size(); index++) {
            stacks.add(inventory.getStack(index));
        }
        return stacks;
    }

    private static void validate(List<GenericStack> stacks, int capacity) {
        if (ExtendedEncodingMode.occupiedLength(stacks) > capacity) {
            throw new IllegalArgumentException("Processing draft exceeds layout capacity");
        }
        for (GenericStack stack : stacks) {
            if (stack != null && (stack.amount() <= 0)) {
                throw new IllegalArgumentException("Processing ingredient amount is out of range");
            }
        }
    }

    private static void fill(ConfigInventory inventory, List<GenericStack> stacks) {
        inventory.beginBatch();
        try {
            for (int index = 0; index < inventory.size(); index++) {
                inventory.setStack(index, index < stacks.size() ? stacks.get(index) : null);
            }
        } finally {
            inventory.endBatch();
        }
    }

    public record Snapshot(List<GenericStack> inputs, List<GenericStack> outputs) {
        public boolean isEncodable() {
            return !this.inputs.isEmpty() && !this.outputs.isEmpty() && this.outputs.getFirst() != null;
        }
    }
}
