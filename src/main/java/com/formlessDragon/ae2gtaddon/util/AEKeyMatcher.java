package com.formlessDragon.ae2gtaddon.util;

import ae2.api.stacks.AEKey;

import java.util.function.Predicate;

public interface AEKeyMatcher extends Predicate<AEKey> {

    @Override
    boolean test(AEKey aeKey);
}
