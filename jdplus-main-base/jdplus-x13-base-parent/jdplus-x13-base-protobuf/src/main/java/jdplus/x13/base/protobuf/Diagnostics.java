// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: x13.proto
// Protobuf Java Version: 4.30.2

package jdplus.x13.base.protobuf;

/**
 * Protobuf type {@code x13.Diagnostics}
 */
public final class Diagnostics extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:x13.Diagnostics)
    DiagnosticsOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 2,
      /* suffix= */ "",
      Diagnostics.class.getName());
  }
  // Use Diagnostics.newBuilder() to construct.
  private Diagnostics(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private Diagnostics() {
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return jdplus.x13.base.protobuf.X13Protos.internal_static_x13_Diagnostics_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return jdplus.x13.base.protobuf.X13Protos.internal_static_x13_Diagnostics_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            jdplus.x13.base.protobuf.Diagnostics.class, jdplus.x13.base.protobuf.Diagnostics.Builder.class);
  }

  private int bitField0_;
  public static final int MSTATISTICS_FIELD_NUMBER = 1;
  private jdplus.x13.base.protobuf.MStatistics mstatistics_;
  /**
   * <code>.x13.MStatistics mstatistics = 1;</code>
   * @return Whether the mstatistics field is set.
   */
  @java.lang.Override
  public boolean hasMstatistics() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>.x13.MStatistics mstatistics = 1;</code>
   * @return The mstatistics.
   */
  @java.lang.Override
  public jdplus.x13.base.protobuf.MStatistics getMstatistics() {
    return mstatistics_ == null ? jdplus.x13.base.protobuf.MStatistics.getDefaultInstance() : mstatistics_;
  }
  /**
   * <code>.x13.MStatistics mstatistics = 1;</code>
   */
  @java.lang.Override
  public jdplus.x13.base.protobuf.MStatisticsOrBuilder getMstatisticsOrBuilder() {
    return mstatistics_ == null ? jdplus.x13.base.protobuf.MStatistics.getDefaultInstance() : mstatistics_;
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (((bitField0_ & 0x00000001) != 0)) {
      output.writeMessage(1, getMstatistics());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (((bitField0_ & 0x00000001) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getMstatistics());
    }
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof jdplus.x13.base.protobuf.Diagnostics)) {
      return super.equals(obj);
    }
    jdplus.x13.base.protobuf.Diagnostics other = (jdplus.x13.base.protobuf.Diagnostics) obj;

    if (hasMstatistics() != other.hasMstatistics()) return false;
    if (hasMstatistics()) {
      if (!getMstatistics()
          .equals(other.getMstatistics())) return false;
    }
    if (!getUnknownFields().equals(other.getUnknownFields())) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    if (hasMstatistics()) {
      hash = (37 * hash) + MSTATISTICS_FIELD_NUMBER;
      hash = (53 * hash) + getMstatistics().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static jdplus.x13.base.protobuf.Diagnostics parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static jdplus.x13.base.protobuf.Diagnostics parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static jdplus.x13.base.protobuf.Diagnostics parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static jdplus.x13.base.protobuf.Diagnostics parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static jdplus.x13.base.protobuf.Diagnostics parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static jdplus.x13.base.protobuf.Diagnostics parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static jdplus.x13.base.protobuf.Diagnostics parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static jdplus.x13.base.protobuf.Diagnostics parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static jdplus.x13.base.protobuf.Diagnostics parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static jdplus.x13.base.protobuf.Diagnostics parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static jdplus.x13.base.protobuf.Diagnostics parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static jdplus.x13.base.protobuf.Diagnostics parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(jdplus.x13.base.protobuf.Diagnostics prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessage.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code x13.Diagnostics}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:x13.Diagnostics)
      jdplus.x13.base.protobuf.DiagnosticsOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return jdplus.x13.base.protobuf.X13Protos.internal_static_x13_Diagnostics_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return jdplus.x13.base.protobuf.X13Protos.internal_static_x13_Diagnostics_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              jdplus.x13.base.protobuf.Diagnostics.class, jdplus.x13.base.protobuf.Diagnostics.Builder.class);
    }

    // Construct using jdplus.x13.base.protobuf.Diagnostics.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessage
              .alwaysUseFieldBuilders) {
        internalGetMstatisticsFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      mstatistics_ = null;
      if (mstatisticsBuilder_ != null) {
        mstatisticsBuilder_.dispose();
        mstatisticsBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return jdplus.x13.base.protobuf.X13Protos.internal_static_x13_Diagnostics_descriptor;
    }

    @java.lang.Override
    public jdplus.x13.base.protobuf.Diagnostics getDefaultInstanceForType() {
      return jdplus.x13.base.protobuf.Diagnostics.getDefaultInstance();
    }

    @java.lang.Override
    public jdplus.x13.base.protobuf.Diagnostics build() {
      jdplus.x13.base.protobuf.Diagnostics result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public jdplus.x13.base.protobuf.Diagnostics buildPartial() {
      jdplus.x13.base.protobuf.Diagnostics result = new jdplus.x13.base.protobuf.Diagnostics(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(jdplus.x13.base.protobuf.Diagnostics result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.mstatistics_ = mstatisticsBuilder_ == null
            ? mstatistics_
            : mstatisticsBuilder_.build();
        to_bitField0_ |= 0x00000001;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof jdplus.x13.base.protobuf.Diagnostics) {
        return mergeFrom((jdplus.x13.base.protobuf.Diagnostics)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(jdplus.x13.base.protobuf.Diagnostics other) {
      if (other == jdplus.x13.base.protobuf.Diagnostics.getDefaultInstance()) return this;
      if (other.hasMstatistics()) {
        mergeMstatistics(other.getMstatistics());
      }
      this.mergeUnknownFields(other.getUnknownFields());
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 10: {
              input.readMessage(
                  internalGetMstatisticsFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            default: {
              if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                done = true; // was an endgroup tag
              }
              break;
            } // default:
          } // switch (tag)
        } // while (!done)
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.unwrapIOException();
      } finally {
        onChanged();
      } // finally
      return this;
    }
    private int bitField0_;

    private jdplus.x13.base.protobuf.MStatistics mstatistics_;
    private com.google.protobuf.SingleFieldBuilder<
        jdplus.x13.base.protobuf.MStatistics, jdplus.x13.base.protobuf.MStatistics.Builder, jdplus.x13.base.protobuf.MStatisticsOrBuilder> mstatisticsBuilder_;
    /**
     * <code>.x13.MStatistics mstatistics = 1;</code>
     * @return Whether the mstatistics field is set.
     */
    public boolean hasMstatistics() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.x13.MStatistics mstatistics = 1;</code>
     * @return The mstatistics.
     */
    public jdplus.x13.base.protobuf.MStatistics getMstatistics() {
      if (mstatisticsBuilder_ == null) {
        return mstatistics_ == null ? jdplus.x13.base.protobuf.MStatistics.getDefaultInstance() : mstatistics_;
      } else {
        return mstatisticsBuilder_.getMessage();
      }
    }
    /**
     * <code>.x13.MStatistics mstatistics = 1;</code>
     */
    public Builder setMstatistics(jdplus.x13.base.protobuf.MStatistics value) {
      if (mstatisticsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        mstatistics_ = value;
      } else {
        mstatisticsBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.x13.MStatistics mstatistics = 1;</code>
     */
    public Builder setMstatistics(
        jdplus.x13.base.protobuf.MStatistics.Builder builderForValue) {
      if (mstatisticsBuilder_ == null) {
        mstatistics_ = builderForValue.build();
      } else {
        mstatisticsBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.x13.MStatistics mstatistics = 1;</code>
     */
    public Builder mergeMstatistics(jdplus.x13.base.protobuf.MStatistics value) {
      if (mstatisticsBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          mstatistics_ != null &&
          mstatistics_ != jdplus.x13.base.protobuf.MStatistics.getDefaultInstance()) {
          getMstatisticsBuilder().mergeFrom(value);
        } else {
          mstatistics_ = value;
        }
      } else {
        mstatisticsBuilder_.mergeFrom(value);
      }
      if (mstatistics_ != null) {
        bitField0_ |= 0x00000001;
        onChanged();
      }
      return this;
    }
    /**
     * <code>.x13.MStatistics mstatistics = 1;</code>
     */
    public Builder clearMstatistics() {
      bitField0_ = (bitField0_ & ~0x00000001);
      mstatistics_ = null;
      if (mstatisticsBuilder_ != null) {
        mstatisticsBuilder_.dispose();
        mstatisticsBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.x13.MStatistics mstatistics = 1;</code>
     */
    public jdplus.x13.base.protobuf.MStatistics.Builder getMstatisticsBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return internalGetMstatisticsFieldBuilder().getBuilder();
    }
    /**
     * <code>.x13.MStatistics mstatistics = 1;</code>
     */
    public jdplus.x13.base.protobuf.MStatisticsOrBuilder getMstatisticsOrBuilder() {
      if (mstatisticsBuilder_ != null) {
        return mstatisticsBuilder_.getMessageOrBuilder();
      } else {
        return mstatistics_ == null ?
            jdplus.x13.base.protobuf.MStatistics.getDefaultInstance() : mstatistics_;
      }
    }
    /**
     * <code>.x13.MStatistics mstatistics = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilder<
        jdplus.x13.base.protobuf.MStatistics, jdplus.x13.base.protobuf.MStatistics.Builder, jdplus.x13.base.protobuf.MStatisticsOrBuilder> 
        internalGetMstatisticsFieldBuilder() {
      if (mstatisticsBuilder_ == null) {
        mstatisticsBuilder_ = new com.google.protobuf.SingleFieldBuilder<
            jdplus.x13.base.protobuf.MStatistics, jdplus.x13.base.protobuf.MStatistics.Builder, jdplus.x13.base.protobuf.MStatisticsOrBuilder>(
                getMstatistics(),
                getParentForChildren(),
                isClean());
        mstatistics_ = null;
      }
      return mstatisticsBuilder_;
    }

    // @@protoc_insertion_point(builder_scope:x13.Diagnostics)
  }

  // @@protoc_insertion_point(class_scope:x13.Diagnostics)
  private static final jdplus.x13.base.protobuf.Diagnostics DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new jdplus.x13.base.protobuf.Diagnostics();
  }

  public static jdplus.x13.base.protobuf.Diagnostics getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Diagnostics>
      PARSER = new com.google.protobuf.AbstractParser<Diagnostics>() {
    @java.lang.Override
    public Diagnostics parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      Builder builder = newBuilder();
      try {
        builder.mergeFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(builder.buildPartial());
      } catch (com.google.protobuf.UninitializedMessageException e) {
        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(e)
            .setUnfinishedMessage(builder.buildPartial());
      }
      return builder.buildPartial();
    }
  };

  public static com.google.protobuf.Parser<Diagnostics> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Diagnostics> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public jdplus.x13.base.protobuf.Diagnostics getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

