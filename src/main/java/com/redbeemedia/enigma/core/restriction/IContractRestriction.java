package com.redbeemedia.enigma.core.restriction;

public interface IContractRestriction<T> {
    T getValue(IContractRestrictionsValueSource valueSource);
}
