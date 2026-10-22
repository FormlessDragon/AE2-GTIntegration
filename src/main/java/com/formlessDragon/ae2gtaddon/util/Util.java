package com.formlessDragon.ae2gtaddon.util;

import ae2.api.stacks.AEItemKey;
import ae2.api.stacks.AEKey;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

public final class Util {

    public static final String ASSEMBLY_LINE = "gregtech:assembly_line";

    public static OptionalInt getCircuitConfiguration(AEKey what) {
        if (!(what instanceof AEItemKey itemKey)) {
            return OptionalInt.empty();
        }
        ItemStack stack = itemKey.toStack();
        if (!IntCircuitIngredient.isIntegratedCircuit(stack)) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(IntCircuitIngredient.getCircuitConfiguration(stack));
    }

    public static boolean isAssemblyLineRecipe(@Nullable String recipeCategoryUid) {
        return ASSEMBLY_LINE.equals(recipeCategoryUid);
    }
}
