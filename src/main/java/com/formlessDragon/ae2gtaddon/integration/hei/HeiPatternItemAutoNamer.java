package com.formlessDragon.ae2gtaddon.integration.hei;

import ae2.api.stacks.AEItemKey;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.GenericStack;
import com.formlessDragon.ae2gtaddon.AEGTAddonConfig;
import com.formlessDragon.ae2gtaddon.util.Util;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HeiPatternItemAutoNamer {

    public static final int MAX_CUSTOM_NAME_LENGTH = 32;

    private HeiPatternItemAutoNamer() {
    }

    public static boolean isEnabled(boolean toggleOn, @Nullable String recipeCategoryUid) {
        return toggleOn || Util.isAssemblyLineRecipe(recipeCategoryUid);
    }

    public static List<GenericStack> autoNameRepeatedItems(List<GenericStack> orderedInputs) {
        Map<Object, Integer> occurrences = new LinkedHashMap<>();
        for (GenericStack stack : orderedInputs) {
            Object group = groupOf(stack);
            if (group != null) {
                occurrences.merge(group, 1, Integer::sum);
            }
        }

        Map<Object, Integer> nextIndexByGroup = new HashMap<>();
        List<GenericStack> renamed = new ObjectArrayList<>(orderedInputs.size());
        for (GenericStack stack : orderedInputs) {
            Object group = groupOf(stack);
            Integer occurrencesInGroup = group == null ? null : occurrences.get(group);
            if (occurrencesInGroup == null || occurrencesInGroup < 2) {
                renamed.add(stack);
                continue;
            }

            int index = nextIndexByGroup.merge(group, 0, (pre, _) -> pre + 1);
            String name = buildName(ItemKeyRenameStrategy.INSTANCE.baseName(stack.what()), index, AEGTAddonConfig.autoNamingKeepsItemName);
            AEKey replacement = name == null ? null : ItemKeyRenameStrategy.INSTANCE.rename(stack.what(), name);
            renamed.add(replacement == null ? stack : new GenericStack(replacement, stack.amount()));
        }
        return renamed;
    }

    @Nullable
    static String buildName(@Nullable String baseName, int index, boolean keepItemName) {
        if (!keepItemName) {
            return "#" + index;
        }

        if (baseName == null || baseName.isEmpty()) {
            return null;
        }

        String suffix = " #" + index;
        int prefixLength = Math.min(baseName.length(), MAX_CUSTOM_NAME_LENGTH - suffix.length());
        return baseName.substring(0, prefixLength) + suffix;
    }

    @Nullable
    private static Object groupOf(@Nullable GenericStack stack) {
        if (stack == null || stack.what() == null) {
            return null;
        }
        return ItemKeyRenameStrategy.INSTANCE.groupKey(stack.what());
    }

    private enum ItemKeyRenameStrategy {
        INSTANCE;

        public Object groupKey(AEKey key) {
            ItemStack stack = stripCustomName(key);
            return stack == null ? null : AEItemKey.of(stack);
        }

        public String baseName(AEKey key) {
            ItemStack stack = stripCustomName(key);
            return stack == null ? null : stack.getDisplayName();
        }

        public AEKey rename(AEKey key, String name) {
            ItemStack stack = toPlainStack(key);
            if (stack == null) {
                return null;
            }
            stack.setStackDisplayName(name);
            return AEItemKey.of(stack);
        }

        @Nullable
        private static ItemStack stripCustomName(AEKey key) {
            ItemStack stack = toPlainStack(key);
            if (stack != null) {
                stack.clearCustomName();
            }
            return stack;
        }

        @Nullable
        private static ItemStack toPlainStack(AEKey key) {
            if (!(key instanceof AEItemKey itemKey)) {
                return null;
            }
            ItemStack stack = itemKey.toStack(1);
            return stack.isEmpty() ? null : stack;
        }
    }
}
