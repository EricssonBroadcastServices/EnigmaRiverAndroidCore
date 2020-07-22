package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.restriction.IContractRestriction;
import com.redbeemedia.enigma.core.restriction.IContractRestrictions;

/*package-protected*/ class EmptyContractRestrictions implements IContractRestrictions {
    @Override
    public <T> T getValue(IContractRestriction<T> restriction, T fallback) {
        return fallback;
    }
}
