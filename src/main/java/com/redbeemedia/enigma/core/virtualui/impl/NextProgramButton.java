package com.redbeemedia.enigma.core.virtualui.impl;

import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.util.OpenContainer;
import com.redbeemedia.enigma.core.util.OpenContainerUtil;

/*package-protected*/ class NextProgramButton extends AbstractVirtualButtonImpl {
    private final OpenContainer<IProgram> currentProgram = new OpenContainer<>(null);
    public NextProgramButton(IVirtualButtonContainer container) {
        super(container);
        container.getEnigmaPlayer().addListener(new BaseEnigmaPlayerListener() {
            @Override
            public void onProgramChanged(IProgram from, IProgram to) {
                OpenContainerUtil.setValueSynchronized(currentProgram, to, (oldValue, newValue) -> refresh());
            }
        });
    }

    @Override
    protected boolean calculateEnabled(IVirtualButtonContainer container) {
        return true;
    }

    @Override
    protected boolean calculateRelevant(IVirtualButtonContainer container) {
        return OpenContainerUtil.getValueSynchronized(currentProgram) != null;
    }

    @Override
    protected void onClick(IVirtualButtonContainer container) {
        ITimelinePosition endBound = container.getEnigmaPlayer().getTimeline().getCurrentEndBound();
        if(endBound != null) {
            container.getPlayerControls().seekTo(endBound);
        }
    }
}
