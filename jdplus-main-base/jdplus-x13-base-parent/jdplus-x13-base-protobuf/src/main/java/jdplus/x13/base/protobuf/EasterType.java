// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: x13.proto
// Protobuf Java Version: 4.30.2

package jdplus.x13.base.protobuf;

/**
 * Protobuf enum {@code x13.EasterType}
 */
public enum EasterType
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>EASTER_UNUSED = 0;</code>
   */
  EASTER_UNUSED(0),
  /**
   * <code>EASTER_STANDARD = 1;</code>
   */
  EASTER_STANDARD(1),
  /**
   * <code>EASTER_JULIAN = 2;</code>
   */
  EASTER_JULIAN(2),
  /**
   * <code>EASTER_SC = 3;</code>
   */
  EASTER_SC(3),
  UNRECOGNIZED(-1),
  ;

  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 2,
      /* suffix= */ "",
      EasterType.class.getName());
  }
  /**
   * <code>EASTER_UNUSED = 0;</code>
   */
  public static final int EASTER_UNUSED_VALUE = 0;
  /**
   * <code>EASTER_STANDARD = 1;</code>
   */
  public static final int EASTER_STANDARD_VALUE = 1;
  /**
   * <code>EASTER_JULIAN = 2;</code>
   */
  public static final int EASTER_JULIAN_VALUE = 2;
  /**
   * <code>EASTER_SC = 3;</code>
   */
  public static final int EASTER_SC_VALUE = 3;


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
  public static EasterType valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static EasterType forNumber(int value) {
    switch (value) {
      case 0: return EASTER_UNUSED;
      case 1: return EASTER_STANDARD;
      case 2: return EASTER_JULIAN;
      case 3: return EASTER_SC;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<EasterType>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      EasterType> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<EasterType>() {
          public EasterType findValueByNumber(int number) {
            return EasterType.forNumber(number);
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
    return jdplus.x13.base.protobuf.X13Protos.getDescriptor().getEnumTypes().get(5);
  }

  private static final EasterType[] VALUES = values();

  public static EasterType valueOf(
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

  private EasterType(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:x13.EasterType)
}

