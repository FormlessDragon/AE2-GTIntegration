package com.formlessDragon.ae2gtaddon.ids;

import com.formlessDragon.ae2gtaddon.Tags;
import net.minecraft.util.ResourceLocation;

public final class ItemIds {

    public static final ResourceLocation CIRCUIT_CARD = id("circuit_card");
    public static final ResourceLocation EXTENDED_ENCODING_TERM = id("extended_encoding_terminal");

    private static ResourceLocation id(String id) {
        return new ResourceLocation(Tags.MOD_ID, id);
    }
}
