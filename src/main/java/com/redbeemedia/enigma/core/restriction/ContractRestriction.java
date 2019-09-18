package com.redbeemedia.enigma.core.restriction;

/**
 *  <p>{@link #FASTFORWARD_ENABLED} </p>
 *  <p>{@link #MAX_BITRATE} </p>
 *  <p>{@link #MAX_RES_HEIGHT} </p>
 *  <p>{@link #MIN_BITRATE} </p>
 *  <p>{@link #REWIND_ENABLED} </p>
 *  <p>{@link #TIMESHIFT_ENABLED} </p>
 */
public class ContractRestriction {
    public static final BasicContractRestriction<Boolean> FASTFORWARD_ENABLED = new BasicContractRestriction<>(Boolean.class, "ffEnabled");
    public static final BasicContractRestriction<Integer> MAX_BITRATE = new BasicContractRestriction<>(Integer.class, "maxBitrate");
    public static final BasicContractRestriction<Integer> MAX_RES_HEIGHT = new BasicContractRestriction<>(Integer.class, "maxResHeight");
    public static final BasicContractRestriction<Integer> MIN_BITRATE = new BasicContractRestriction<>(Integer.class, "minBitrate");
    public static final BasicContractRestriction<Boolean> REWIND_ENABLED = new BasicContractRestriction<>(Boolean.class, "rwEnabled");
    public static final BasicContractRestriction<Boolean> TIMESHIFT_ENABLED = new BasicContractRestriction<>(Boolean.class, "timeshiftEnabled");
}
