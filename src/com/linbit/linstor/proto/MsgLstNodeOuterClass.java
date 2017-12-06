// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: MsgLstNode.proto

package com.linbit.linstor.proto;

public final class MsgLstNodeOuterClass {
  private MsgLstNodeOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface MsgLstNodeOrBuilder extends
      // @@protoc_insertion_point(interface_extends:com.linbit.linstor.proto.MsgLstNode)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <pre>
     * Nodes
     * </pre>
     *
     * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
     */
    java.util.List<com.linbit.linstor.proto.NodeOuterClass.Node> 
        getNodesList();
    /**
     * <pre>
     * Nodes
     * </pre>
     *
     * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
     */
    com.linbit.linstor.proto.NodeOuterClass.Node getNodes(int index);
    /**
     * <pre>
     * Nodes
     * </pre>
     *
     * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
     */
    int getNodesCount();
    /**
     * <pre>
     * Nodes
     * </pre>
     *
     * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
     */
    java.util.List<? extends com.linbit.linstor.proto.NodeOuterClass.NodeOrBuilder> 
        getNodesOrBuilderList();
    /**
     * <pre>
     * Nodes
     * </pre>
     *
     * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
     */
    com.linbit.linstor.proto.NodeOuterClass.NodeOrBuilder getNodesOrBuilder(
        int index);
  }
  /**
   * <pre>
   * linstor - List nodes
   * </pre>
   *
   * Protobuf type {@code com.linbit.linstor.proto.MsgLstNode}
   */
  public  static final class MsgLstNode extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:com.linbit.linstor.proto.MsgLstNode)
      MsgLstNodeOrBuilder {
    // Use MsgLstNode.newBuilder() to construct.
    private MsgLstNode(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private MsgLstNode() {
      nodes_ = java.util.Collections.emptyList();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private MsgLstNode(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                nodes_ = new java.util.ArrayList<com.linbit.linstor.proto.NodeOuterClass.Node>();
                mutable_bitField0_ |= 0x00000001;
              }
              nodes_.add(
                  input.readMessage(com.linbit.linstor.proto.NodeOuterClass.Node.PARSER, extensionRegistry));
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        if (((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
          nodes_ = java.util.Collections.unmodifiableList(nodes_);
        }
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.linbit.linstor.proto.MsgLstNodeOuterClass.internal_static_com_linbit_linstor_proto_MsgLstNode_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.linbit.linstor.proto.MsgLstNodeOuterClass.internal_static_com_linbit_linstor_proto_MsgLstNode_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode.class, com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode.Builder.class);
    }

    public static final int NODES_FIELD_NUMBER = 1;
    private java.util.List<com.linbit.linstor.proto.NodeOuterClass.Node> nodes_;
    /**
     * <pre>
     * Nodes
     * </pre>
     *
     * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
     */
    public java.util.List<com.linbit.linstor.proto.NodeOuterClass.Node> getNodesList() {
      return nodes_;
    }
    /**
     * <pre>
     * Nodes
     * </pre>
     *
     * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
     */
    public java.util.List<? extends com.linbit.linstor.proto.NodeOuterClass.NodeOrBuilder> 
        getNodesOrBuilderList() {
      return nodes_;
    }
    /**
     * <pre>
     * Nodes
     * </pre>
     *
     * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
     */
    public int getNodesCount() {
      return nodes_.size();
    }
    /**
     * <pre>
     * Nodes
     * </pre>
     *
     * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
     */
    public com.linbit.linstor.proto.NodeOuterClass.Node getNodes(int index) {
      return nodes_.get(index);
    }
    /**
     * <pre>
     * Nodes
     * </pre>
     *
     * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
     */
    public com.linbit.linstor.proto.NodeOuterClass.NodeOrBuilder getNodesOrBuilder(
        int index) {
      return nodes_.get(index);
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      for (int i = 0; i < getNodesCount(); i++) {
        if (!getNodes(i).isInitialized()) {
          memoizedIsInitialized = 0;
          return false;
        }
      }
      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      for (int i = 0; i < nodes_.size(); i++) {
        output.writeMessage(1, nodes_.get(i));
      }
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      for (int i = 0; i < nodes_.size(); i++) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, nodes_.get(i));
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode)) {
        return super.equals(obj);
      }
      com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode other = (com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode) obj;

      boolean result = true;
      result = result && getNodesList()
          .equals(other.getNodesList());
      result = result && unknownFields.equals(other.unknownFields);
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      if (getNodesCount() > 0) {
        hash = (37 * hash) + NODES_FIELD_NUMBER;
        hash = (53 * hash) + getNodesList().hashCode();
      }
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * <pre>
     * linstor - List nodes
     * </pre>
     *
     * Protobuf type {@code com.linbit.linstor.proto.MsgLstNode}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:com.linbit.linstor.proto.MsgLstNode)
        com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNodeOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.linbit.linstor.proto.MsgLstNodeOuterClass.internal_static_com_linbit_linstor_proto_MsgLstNode_descriptor;
      }

      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.linbit.linstor.proto.MsgLstNodeOuterClass.internal_static_com_linbit_linstor_proto_MsgLstNode_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode.class, com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode.Builder.class);
      }

      // Construct using com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
          getNodesFieldBuilder();
        }
      }
      public Builder clear() {
        super.clear();
        if (nodesBuilder_ == null) {
          nodes_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000001);
        } else {
          nodesBuilder_.clear();
        }
        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.linbit.linstor.proto.MsgLstNodeOuterClass.internal_static_com_linbit_linstor_proto_MsgLstNode_descriptor;
      }

      public com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode getDefaultInstanceForType() {
        return com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode.getDefaultInstance();
      }

      public com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode build() {
        com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode buildPartial() {
        com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode result = new com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode(this);
        int from_bitField0_ = bitField0_;
        if (nodesBuilder_ == null) {
          if (((bitField0_ & 0x00000001) == 0x00000001)) {
            nodes_ = java.util.Collections.unmodifiableList(nodes_);
            bitField0_ = (bitField0_ & ~0x00000001);
          }
          result.nodes_ = nodes_;
        } else {
          result.nodes_ = nodesBuilder_.build();
        }
        onBuilt();
        return result;
      }

      public Builder clone() {
        return (Builder) super.clone();
      }
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.setField(field, value);
      }
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode) {
          return mergeFrom((com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode other) {
        if (other == com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode.getDefaultInstance()) return this;
        if (nodesBuilder_ == null) {
          if (!other.nodes_.isEmpty()) {
            if (nodes_.isEmpty()) {
              nodes_ = other.nodes_;
              bitField0_ = (bitField0_ & ~0x00000001);
            } else {
              ensureNodesIsMutable();
              nodes_.addAll(other.nodes_);
            }
            onChanged();
          }
        } else {
          if (!other.nodes_.isEmpty()) {
            if (nodesBuilder_.isEmpty()) {
              nodesBuilder_.dispose();
              nodesBuilder_ = null;
              nodes_ = other.nodes_;
              bitField0_ = (bitField0_ & ~0x00000001);
              nodesBuilder_ = 
                com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                   getNodesFieldBuilder() : null;
            } else {
              nodesBuilder_.addAllMessages(other.nodes_);
            }
          }
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        for (int i = 0; i < getNodesCount(); i++) {
          if (!getNodes(i).isInitialized()) {
            return false;
          }
        }
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private java.util.List<com.linbit.linstor.proto.NodeOuterClass.Node> nodes_ =
        java.util.Collections.emptyList();
      private void ensureNodesIsMutable() {
        if (!((bitField0_ & 0x00000001) == 0x00000001)) {
          nodes_ = new java.util.ArrayList<com.linbit.linstor.proto.NodeOuterClass.Node>(nodes_);
          bitField0_ |= 0x00000001;
         }
      }

      private com.google.protobuf.RepeatedFieldBuilderV3<
          com.linbit.linstor.proto.NodeOuterClass.Node, com.linbit.linstor.proto.NodeOuterClass.Node.Builder, com.linbit.linstor.proto.NodeOuterClass.NodeOrBuilder> nodesBuilder_;

      /**
       * <pre>
       * Nodes
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
       */
      public java.util.List<com.linbit.linstor.proto.NodeOuterClass.Node> getNodesList() {
        if (nodesBuilder_ == null) {
          return java.util.Collections.unmodifiableList(nodes_);
        } else {
          return nodesBuilder_.getMessageList();
        }
      }
      /**
       * <pre>
       * Nodes
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
       */
      public int getNodesCount() {
        if (nodesBuilder_ == null) {
          return nodes_.size();
        } else {
          return nodesBuilder_.getCount();
        }
      }
      /**
       * <pre>
       * Nodes
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
       */
      public com.linbit.linstor.proto.NodeOuterClass.Node getNodes(int index) {
        if (nodesBuilder_ == null) {
          return nodes_.get(index);
        } else {
          return nodesBuilder_.getMessage(index);
        }
      }
      /**
       * <pre>
       * Nodes
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
       */
      public Builder setNodes(
          int index, com.linbit.linstor.proto.NodeOuterClass.Node value) {
        if (nodesBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureNodesIsMutable();
          nodes_.set(index, value);
          onChanged();
        } else {
          nodesBuilder_.setMessage(index, value);
        }
        return this;
      }
      /**
       * <pre>
       * Nodes
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
       */
      public Builder setNodes(
          int index, com.linbit.linstor.proto.NodeOuterClass.Node.Builder builderForValue) {
        if (nodesBuilder_ == null) {
          ensureNodesIsMutable();
          nodes_.set(index, builderForValue.build());
          onChanged();
        } else {
          nodesBuilder_.setMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <pre>
       * Nodes
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
       */
      public Builder addNodes(com.linbit.linstor.proto.NodeOuterClass.Node value) {
        if (nodesBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureNodesIsMutable();
          nodes_.add(value);
          onChanged();
        } else {
          nodesBuilder_.addMessage(value);
        }
        return this;
      }
      /**
       * <pre>
       * Nodes
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
       */
      public Builder addNodes(
          int index, com.linbit.linstor.proto.NodeOuterClass.Node value) {
        if (nodesBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureNodesIsMutable();
          nodes_.add(index, value);
          onChanged();
        } else {
          nodesBuilder_.addMessage(index, value);
        }
        return this;
      }
      /**
       * <pre>
       * Nodes
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
       */
      public Builder addNodes(
          com.linbit.linstor.proto.NodeOuterClass.Node.Builder builderForValue) {
        if (nodesBuilder_ == null) {
          ensureNodesIsMutable();
          nodes_.add(builderForValue.build());
          onChanged();
        } else {
          nodesBuilder_.addMessage(builderForValue.build());
        }
        return this;
      }
      /**
       * <pre>
       * Nodes
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
       */
      public Builder addNodes(
          int index, com.linbit.linstor.proto.NodeOuterClass.Node.Builder builderForValue) {
        if (nodesBuilder_ == null) {
          ensureNodesIsMutable();
          nodes_.add(index, builderForValue.build());
          onChanged();
        } else {
          nodesBuilder_.addMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <pre>
       * Nodes
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
       */
      public Builder addAllNodes(
          java.lang.Iterable<? extends com.linbit.linstor.proto.NodeOuterClass.Node> values) {
        if (nodesBuilder_ == null) {
          ensureNodesIsMutable();
          com.google.protobuf.AbstractMessageLite.Builder.addAll(
              values, nodes_);
          onChanged();
        } else {
          nodesBuilder_.addAllMessages(values);
        }
        return this;
      }
      /**
       * <pre>
       * Nodes
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
       */
      public Builder clearNodes() {
        if (nodesBuilder_ == null) {
          nodes_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000001);
          onChanged();
        } else {
          nodesBuilder_.clear();
        }
        return this;
      }
      /**
       * <pre>
       * Nodes
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
       */
      public Builder removeNodes(int index) {
        if (nodesBuilder_ == null) {
          ensureNodesIsMutable();
          nodes_.remove(index);
          onChanged();
        } else {
          nodesBuilder_.remove(index);
        }
        return this;
      }
      /**
       * <pre>
       * Nodes
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
       */
      public com.linbit.linstor.proto.NodeOuterClass.Node.Builder getNodesBuilder(
          int index) {
        return getNodesFieldBuilder().getBuilder(index);
      }
      /**
       * <pre>
       * Nodes
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
       */
      public com.linbit.linstor.proto.NodeOuterClass.NodeOrBuilder getNodesOrBuilder(
          int index) {
        if (nodesBuilder_ == null) {
          return nodes_.get(index);  } else {
          return nodesBuilder_.getMessageOrBuilder(index);
        }
      }
      /**
       * <pre>
       * Nodes
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
       */
      public java.util.List<? extends com.linbit.linstor.proto.NodeOuterClass.NodeOrBuilder> 
           getNodesOrBuilderList() {
        if (nodesBuilder_ != null) {
          return nodesBuilder_.getMessageOrBuilderList();
        } else {
          return java.util.Collections.unmodifiableList(nodes_);
        }
      }
      /**
       * <pre>
       * Nodes
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
       */
      public com.linbit.linstor.proto.NodeOuterClass.Node.Builder addNodesBuilder() {
        return getNodesFieldBuilder().addBuilder(
            com.linbit.linstor.proto.NodeOuterClass.Node.getDefaultInstance());
      }
      /**
       * <pre>
       * Nodes
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
       */
      public com.linbit.linstor.proto.NodeOuterClass.Node.Builder addNodesBuilder(
          int index) {
        return getNodesFieldBuilder().addBuilder(
            index, com.linbit.linstor.proto.NodeOuterClass.Node.getDefaultInstance());
      }
      /**
       * <pre>
       * Nodes
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Node nodes = 1;</code>
       */
      public java.util.List<com.linbit.linstor.proto.NodeOuterClass.Node.Builder> 
           getNodesBuilderList() {
        return getNodesFieldBuilder().getBuilderList();
      }
      private com.google.protobuf.RepeatedFieldBuilderV3<
          com.linbit.linstor.proto.NodeOuterClass.Node, com.linbit.linstor.proto.NodeOuterClass.Node.Builder, com.linbit.linstor.proto.NodeOuterClass.NodeOrBuilder> 
          getNodesFieldBuilder() {
        if (nodesBuilder_ == null) {
          nodesBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
              com.linbit.linstor.proto.NodeOuterClass.Node, com.linbit.linstor.proto.NodeOuterClass.Node.Builder, com.linbit.linstor.proto.NodeOuterClass.NodeOrBuilder>(
                  nodes_,
                  ((bitField0_ & 0x00000001) == 0x00000001),
                  getParentForChildren(),
                  isClean());
          nodes_ = null;
        }
        return nodesBuilder_;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:com.linbit.linstor.proto.MsgLstNode)
    }

    // @@protoc_insertion_point(class_scope:com.linbit.linstor.proto.MsgLstNode)
    private static final com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode();
    }

    public static com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    @java.lang.Deprecated public static final com.google.protobuf.Parser<MsgLstNode>
        PARSER = new com.google.protobuf.AbstractParser<MsgLstNode>() {
      public MsgLstNode parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
          return new MsgLstNode(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<MsgLstNode> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<MsgLstNode> getParserForType() {
      return PARSER;
    }

    public com.linbit.linstor.proto.MsgLstNodeOuterClass.MsgLstNode getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_com_linbit_linstor_proto_MsgLstNode_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_com_linbit_linstor_proto_MsgLstNode_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\020MsgLstNode.proto\022\030com.linbit.linstor.p" +
      "roto\032\nNode.proto\";\n\nMsgLstNode\022-\n\005nodes\030" +
      "\001 \003(\0132\036.com.linbit.linstor.proto.NodeP\000"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.linbit.linstor.proto.NodeOuterClass.getDescriptor(),
        }, assigner);
    internal_static_com_linbit_linstor_proto_MsgLstNode_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_com_linbit_linstor_proto_MsgLstNode_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_com_linbit_linstor_proto_MsgLstNode_descriptor,
        new java.lang.String[] { "Nodes", });
    com.linbit.linstor.proto.NodeOuterClass.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}