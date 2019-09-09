package com.redbeemedia.enigma.core.player.controls;

import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.util.IInternalCallbackObject;

public interface IControlResultHandler extends IInternalCallbackObject {
    void onRejected(IRejectReason reason);
    void onCancelled();
    void onError(Error error);
    void onDone();

    interface IRejectReason {
        RejectReasonType getType();
        String getDetails();
    }

    class RejectReasonType {
        public static final RejectReasonType INAPPLICABLE_FOR_CURRENT_STREAM = new RejectReasonType("INAPPLICABLE_FOR_CURRENT_STREAM");
        public static final RejectReasonType CONTRACT_RESTRICTION_LIMITATION = new RejectReasonType("CONTRACT_RESTRICTION_LIMITATION");
        public static final RejectReasonType ILLEGAL_ARGUMENT = new RejectReasonType("ILLEGAL_ARGUMENT");
        public static final RejectReasonType INCORRECT_STATE = new RejectReasonType("INCORRECT_STATE");
        public static final RejectReasonType OTHER = new RejectReasonType("OTHER");

        private final String name;
        private RejectReasonType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
