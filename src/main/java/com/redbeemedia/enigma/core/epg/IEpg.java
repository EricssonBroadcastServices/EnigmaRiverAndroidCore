package com.redbeemedia.enigma.core.epg;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.error.Error;

import java.net.MalformedURLException;
import java.util.List;

public interface IEpg {
    void loadData(IBusinessUnit businessUnit, long utcMillis, int daysBack, int daysForward) throws MalformedURLException;
    void getPrograms(String channelId, long fromMillis, long toMillis, IProgramListRequestResultHandler resultHandler);

    interface IProgramListRequestResultHandler {
        void onList(List<IProgram> programs);
        void onError(Error error);
    }
}
