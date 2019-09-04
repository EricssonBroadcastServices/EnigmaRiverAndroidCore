package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;

/*package-protected*/ class ControlResultHandlerAdapter implements IPlayerImplementationControlResultHandler {
    private Runnable onDone = null;
    private boolean done = false;
    private final IControlResultHandler resultHandler;

    public ControlResultHandlerAdapter(IControlResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }

    @Override
    public void onRejected(IControlResultHandler.IRejectReason rejectReason) {
        resultHandler.onRejected(rejectReason);
    }

    @Override
    public void onCancelled() {
        resultHandler.onCancelled();
    }

    @Override
    public void onError(Error error) {
        resultHandler.onError(error);
    }

    @Override
    public void onDone() {
        resultHandler.onDone();
        synchronized (this) {
            if (this.onDone != null) {
                this.onDone.run();
                this.onDone = null;
            }
            this.done = true;
        }
    }

    public synchronized ControlResultHandlerAdapter runWhenDone(Runnable runnable) {
        if(this.done) {
            runnable.run();
        } else {
            this.onDone = runnable;
        }
        return this;
    }
}
