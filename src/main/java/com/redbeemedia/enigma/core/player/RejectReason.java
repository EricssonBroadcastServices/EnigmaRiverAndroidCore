package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;

/*package-protected*/ class RejectReason implements IControlResultHandler.IRejectReason {
    private final IControlResultHandler.RejectReasonType type;
    private final String details;

    public RejectReason(IControlResultHandler.RejectReasonType type, String details) {
        this.type = type;
        this.details = details;
    }


    @Override
    public IControlResultHandler.RejectReasonType getType() {
        return type;
    }

    @Override
    public String getDetails() {
        return details;
    }

    public static IControlResultHandler.IRejectReason illegal(String details) {
        return new RejectReason(IControlResultHandler.RejectReasonType.ILLEGAL_ARGUMENT, details);
    }

    public static IControlResultHandler.IRejectReason inapplicable(String details) {
        return new RejectReason(IControlResultHandler.RejectReasonType.INAPPLICABLE_FOR_CURRENT_STREAM, details);
    }

    public static IControlResultHandler.IRejectReason contractRestriction(String details) {
        return new RejectReason(IControlResultHandler.RejectReasonType.CONTRACT_RESTRICTION_LIMITATION, details);
    }

    public static IControlResultHandler.IRejectReason other(String details) {
        return new RejectReason(IControlResultHandler.RejectReasonType.OTHER, details);
    }
}
