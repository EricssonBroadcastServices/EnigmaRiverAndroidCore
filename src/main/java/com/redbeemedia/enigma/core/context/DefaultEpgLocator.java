package com.redbeemedia.enigma.core.context;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.epg.IEpg;
import com.redbeemedia.enigma.core.epg.IEpgLocator;
import com.redbeemedia.enigma.core.epg.impl.DirectEpg;
import com.redbeemedia.enigma.core.util.OpenContainer;

import java.util.Objects;

/*package-protected*/ class DefaultEpgLocator implements IEpgLocator {
    private final OpenContainer<IBusinessUnit> businessUnitContainer = new OpenContainer<>(null);
    private final OpenContainer<IEpg> epgContainer = new OpenContainer<>(null);

    @Override
    public IEpg getEpg(IBusinessUnit businessUnit) {
        if(businessUnit == null) {
            throw new NullPointerException();
        }
        synchronized (businessUnitContainer) {
            if(businessUnitContainer.value == null) {
                businessUnitContainer.value = businessUnit;
            } else {
                if(!Objects.equals(businessUnitContainer.value, businessUnit)) {
                    throw new IllegalArgumentException("Please use a different EpgLocator. This one only supports on epg from one BusinessUnit.");
                }
            }
        }
        synchronized (epgContainer) {
            if(epgContainer.value == null) {
                epgContainer.value = new DirectEpg(businessUnit);
            }
            return epgContainer.value;
        }
    }
}
