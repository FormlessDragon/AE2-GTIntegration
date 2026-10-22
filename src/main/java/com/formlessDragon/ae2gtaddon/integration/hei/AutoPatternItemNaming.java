package com.formlessDragon.ae2gtaddon.integration.hei;

/**
 * Client-only HEI import preference of a pattern encoding terminal container. HEI writes the processing pattern slots
 * before sending fake slot updates to the server, so the preference never has to leave the client container.
 */
public interface AutoPatternItemNaming {

    /**
     * @return whether repeated input items are split into {@code name #1}, {@code name #2} and so on.
     */
    boolean ae2gtaddon$autoNamePatternItems();

    /**
     * @param autoName whether repeated input items should be split into separate named pattern slots.
     */
    void ae2gtaddon$setAutoNamePatternItems(boolean autoName);
}
