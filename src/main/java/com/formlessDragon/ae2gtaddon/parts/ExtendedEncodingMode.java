package com.formlessDragon.ae2gtaddon.parts;

import java.util.List;
import java.util.Objects;

public enum ExtendedEncodingMode {
    MULTI_IN(32, 8),
    MULTI_OUT(8, 32);

    private final int inputSlots;
    private final int outputSlots;

    ExtendedEncodingMode(int inputSlots, int outputSlots) {
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
    }

    public int getInputSlots() {
        return this.inputSlots;
    }

    public int getOutputSlots() {
        return this.outputSlots;
    }

    public boolean fits(List<?> inputs, List<?> outputs) {
        return occupiedLength(inputs) <= this.inputSlots && occupiedLength(outputs) <= this.outputSlots;
    }

    public static ExtendedEncodingMode select(List<?> inputs, List<?> outputs, ExtendedEncodingMode preferred) {
        Objects.requireNonNull(preferred, "preferred");
        if (preferred.fits(inputs, outputs)) {
            return preferred;
        }
        for (ExtendedEncodingMode mode : values()) {
            if (mode.fits(inputs, outputs)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Processing recipe exceeds both 32 -> 8 and 8 -> 32 layouts");
    }

    public static int occupiedLength(List<?> stacks) {
        Objects.requireNonNull(stacks, "stacks");
        for (int index = stacks.size() - 1; index >= 0; index--) {
            if (stacks.get(index) != null) {
                return index + 1;
            }
        }
        return 0;
    }
}
