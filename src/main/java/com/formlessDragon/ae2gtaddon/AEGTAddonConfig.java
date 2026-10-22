package com.formlessDragon.ae2gtaddon;

import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID, name = Tags.MOD_NAME)
public final class AEGTAddonConfig {

    private AEGTAddonConfig() {}

    @Config.Comment("Keep item name when transferring recipe from jei")
    public static boolean autoNamingKeepsItemName = false;
}
