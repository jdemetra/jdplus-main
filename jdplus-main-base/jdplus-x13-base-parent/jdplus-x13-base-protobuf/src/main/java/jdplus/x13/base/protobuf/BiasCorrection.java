// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: x13.proto
// Protobuf Java Version: 4.30.2

package jdplus.x13.base.protobuf;

/**
 * Protobuf enum {@code x13.BiasCorrection}
 */
public enum BiasCorrection
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>BIAS_NONE = 0;</code>
   */
  BIAS_NONE(0),
  /**
   * <code>BIAS_LEGACY = 1;</code>
   */
  BIAS_LEGACY(1),
  /**
   * <code>BIAS_SMOOTH = 2;</code>
   */
  BIAS_SMOOTH(2),
  /**
   * <code>BIAS_RATIO = 3;</code>
   */
  BIAS_RATIO(3),
  UNRECOGNIZED(-1),
  ;

  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 2,
      /* suffix= */ "",
      BiasCorrection.class.getName());
  }
  /**
   * <code>BIAS_NONE = 0;</code>
   */
  public static final int BIAS_NONE_VALUE = 0;
  /**
   * <code>BIAS_LEGACY = 1;</code>
   */
  public static final int BIAS_LEGACY_VALUE = 1;
  /**
   * <code>BIAS_SMOOTH = 2;</code>
   */
  public static final int BIAS_SMOOTH_VALUE = 2;
  /**
   * <code>BIAS_RATIO = 3;</code>
   */
  public static final int BIAS_RATIO_VALUE = 3;


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
  public static BiasCorrection valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static BiasCorrection forNumber(int value) {
    switch (value) {
      case 0: return BIAS_NONE;
      case 1: return BIAS_LEGACY;
      case 2: return BIAS_SMOOTH;
      case 3: return BIAS_RATIO;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<BiasCorrection>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      BiasCorrection> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<BiasCorrection>() {
          public BiasCorrection findValueByNumber(int number) {
            return BiasCorrection.forNumber(number);
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
    return jdplus.x13.base.protobuf.X13Protos.getDescriptor().getEnumTypes().get(3);
  }

  private static final BiasCorrection[] VALUES = values();

  public static BiasCorrection valueOf(
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

  private BiasCorrection(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:x13.BiasCorrection)
}

