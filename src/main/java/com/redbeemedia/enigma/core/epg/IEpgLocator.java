package com.redbeemedia.enigma.core.epg;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;

public interface IEpgLocator {
    IEpg getEpg(IBusinessUnit businessUnit);
}
