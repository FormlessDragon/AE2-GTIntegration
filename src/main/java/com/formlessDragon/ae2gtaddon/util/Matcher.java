package com.formlessDragon.ae2gtaddon.util;

import ae2.api.stacks.AEItemKey;
import ae2.api.stacks.AEKey;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.util.AssemblyLineManager;
import net.minecraft.item.ItemStack;

public enum Matcher implements AEKeyMatcher {
    DataItem {
        @Override
        public boolean test(AEKey what) {
            if (!(what instanceof AEItemKey key)) {
                return false;
            }

            ItemStack stack = key.toStack();
            return !stack.isEmpty() && AssemblyLineManager.isStackDataItem(stack, true);
        }
    },
    Circuit {
        @Override
        public boolean test(AEKey what) {
            if (!(what instanceof AEItemKey key)) {
                return false;
            }

            return IntCircuitIngredient.isIntegratedCircuit(key.toStack());
        }
    }
}
