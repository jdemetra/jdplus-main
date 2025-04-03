// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: x13.proto
// Protobuf Java Version: 4.30.2

package jdplus.x13.base.protobuf;

/**
 * Protobuf enum {@code x13.CalendarSigma}
 */
public enum CalendarSigma
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>SIGMA_NONE = 0;</code>
   */
  SIGMA_NONE(0),
  /**
   * <code>SIGMA_SIGNIF = 1;</code>
   */
  SIGMA_SIGNIF(1),
  /**
   * <code>SIGMA_ALL = 2;</code>
   */
  SIGMA_ALL(2),
  /**
   * <code>SIGMA_SELECT = 3;</code>
   */
  SIGMA_SELECT(3),
  UNRECOGNIZED(-1),
  ;

  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 2,
      /* suffix= */ "",
      CalendarSigma.class.getName());
  }
  /**
   * <code>SIGMA_NONE = 0;</code>
   */
  public static final int SIGMA_NONE_VALUE = 0;
  /**
   * <code>SIGMA_SIGNIF = 1;</code>
   */
  public static final int SIGMA_SIGNIF_VALUE = 1;
  /**
   * <code>SIGMA_ALL = 2;</code>
   */
  public static final int SIGMA_ALL_VALUE = 2;
  /**
   * <code>SIGMA_SELECT = 3;</code>
   */
  public static final int SIGMA_SELECT_VALUE = 3;


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
  public static CalendarSigma valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static CalendarSigma forNumber(int value) {
    switch (value) {
      case 0: return SIGMA_NONE;
      case 1: return SIGMA_SIGNIF;
      case 2: return SIGMA_ALL;
      case 3: return SIGMA_SELECT;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<CalendarSigma>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      CalendarSigma> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<CalendarSigma>() {
          public CalendarSigma findValueByNumber(int number) {
            return CalendarSigma.forNumber(number);
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
    return jdplus.x13.base.protobuf.X13Protos.getDescriptor().getEnumTypes().get(2);
  }

  private static final CalendarSigma[] VALUES = values();

  public static CalendarSigma valueOf(
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

  private CalendarSigma(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:x13.CalendarSigma)
}

