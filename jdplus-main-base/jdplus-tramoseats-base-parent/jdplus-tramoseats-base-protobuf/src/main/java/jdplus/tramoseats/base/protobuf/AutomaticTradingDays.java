// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: tramoseats.proto
// Protobuf Java Version: 4.30.2

package jdplus.tramoseats.base.protobuf;

/**
 * Protobuf enum {@code tramoseats.AutomaticTradingDays}
 */
public enum AutomaticTradingDays
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>TD_AUTO_NO = 0;</code>
   */
  TD_AUTO_NO(0),
  /**
   * <code>TD_AUTO_FTEST = 1;</code>
   */
  TD_AUTO_FTEST(1),
  /**
   * <code>TD_AUTO_WALD = 2;</code>
   */
  TD_AUTO_WALD(2),
  /**
   * <code>TD_AUTO_AIC = 3;</code>
   */
  TD_AUTO_AIC(3),
  /**
   * <code>TD_AUTO_BIC = 4;</code>
   */
  TD_AUTO_BIC(4),
  UNRECOGNIZED(-1),
  ;

  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 2,
      /* suffix= */ "",
      AutomaticTradingDays.class.getName());
  }
  /**
   * <code>TD_AUTO_NO = 0;</code>
   */
  public static final int TD_AUTO_NO_VALUE = 0;
  /**
   * <code>TD_AUTO_FTEST = 1;</code>
   */
  public static final int TD_AUTO_FTEST_VALUE = 1;
  /**
   * <code>TD_AUTO_WALD = 2;</code>
   */
  public static final int TD_AUTO_WALD_VALUE = 2;
  /**
   * <code>TD_AUTO_AIC = 3;</code>
   */
  public static final int TD_AUTO_AIC_VALUE = 3;
  /**
   * <code>TD_AUTO_BIC = 4;</code>
   */
  public static final int TD_AUTO_BIC_VALUE = 4;


  public final int getNumber() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalArgumentException(
          "Can't get the number of an unknown enum value.");
    }
    return value;
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   * @deprecated Use {@link #forNumber(int)} instead.
   */
  @java.lang.Deprecated
  public static AutomaticTradingDays valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static AutomaticTradingDays forNumber(int value) {
    switch (value) {
      case 0: return TD_AUTO_NO;
      case 1: return TD_AUTO_FTEST;
      case 2: return TD_AUTO_WALD;
      case 3: return TD_AUTO_AIC;
      case 4: return TD_AUTO_BIC;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<AutomaticTradingDays>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      AutomaticTradingDays> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<AutomaticTradingDays>() {
          public AutomaticTradingDays findValueByNumber(int number) {
            return AutomaticTradingDays.forNumber(number);
          }
        };

  public final com.google.protobuf.Descriptors.EnumValueDescriptor
      getValueDescriptor() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalStateException(
          "Can't get the descriptor of an unrecognized enum value.");
    }
    return getDescriptor().getValues().get(ordinal());
  }
  public final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptorForType() {
    return getDescriptor();
  }
  public static final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptor() {
    return jdplus.tramoseats.base.protobuf.TramoSeatsProtos.getDescriptor().getEnumTypes().get(2);
  }

  private static final AutomaticTradingDays[] VALUES = values();

  public static AutomaticTradingDays valueOf(
      com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
    if (desc.getType() != getDescriptor()) {
      throw new java.lang.IllegalArgumentException(
        "EnumValueDescriptor is not for this type.");
    }
    if (desc.getIndex() == -1) {
      return UNRECOGNIZED;
    }
    return VALUES[desc.getIndex()];
  }

  private final int value;

  private AutomaticTradingDays(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:tramoseats.AutomaticTradingDays)
}

