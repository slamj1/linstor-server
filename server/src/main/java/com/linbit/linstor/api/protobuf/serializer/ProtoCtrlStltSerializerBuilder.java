package com.linbit.linstor.api.protobuf.serializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.protobuf.ByteString;
import com.linbit.linstor.NetInterface;
import com.linbit.linstor.Node;
import com.linbit.linstor.NodeConnection;
import com.linbit.linstor.Resource;
import com.linbit.linstor.ResourceConnection;
import com.linbit.linstor.ResourceDefinition;
import com.linbit.linstor.Snapshot;
import com.linbit.linstor.SnapshotDefinition;
import com.linbit.linstor.SnapshotVolume;
import com.linbit.linstor.SnapshotVolumeDefinition;
import com.linbit.linstor.StorPool;
import com.linbit.linstor.StorPoolDefinition;
import com.linbit.linstor.TcpPortNumber;
import com.linbit.linstor.Volume;
import com.linbit.linstor.VolumeDefinition;
import com.linbit.linstor.api.SpaceInfo;
import com.linbit.linstor.api.interfaces.serializer.CtrlStltSerializer;
import com.linbit.linstor.api.protobuf.ProtoMapUtils;
import com.linbit.linstor.api.protobuf.ProtoStorPoolFreeSpaceUtils;
import com.linbit.linstor.core.CtrlSecurityObjects;
import com.linbit.linstor.logging.ErrorReporter;
import com.linbit.linstor.propscon.Props;
import com.linbit.linstor.proto.LinStorMapEntryOuterClass.LinStorMapEntry;
import com.linbit.linstor.proto.NetInterfaceOuterClass;
import com.linbit.linstor.proto.NodeOuterClass;
import com.linbit.linstor.proto.StorPoolFreeSpaceOuterClass;
import com.linbit.linstor.proto.VlmDfnOuterClass.VlmDfn;
import com.linbit.linstor.proto.VlmOuterClass.Vlm;
import com.linbit.linstor.proto.javainternal.MsgIntApplyRscSuccessOuterClass;
import com.linbit.linstor.proto.javainternal.MsgIntApplyStorPoolSuccessOuterClass.MsgIntApplyStorPoolSuccess;
import com.linbit.linstor.proto.javainternal.MsgIntAuthOuterClass;
import com.linbit.linstor.proto.javainternal.MsgIntControllerDataOuterClass.MsgIntControllerData;
import com.linbit.linstor.proto.javainternal.MsgIntCryptKeyOuterClass.MsgIntCryptKey;
import com.linbit.linstor.proto.javainternal.MsgIntFullSyncOuterClass.MsgIntFullSync;
import com.linbit.linstor.proto.javainternal.MsgIntNodeDataOuterClass.MsgIntNodeData;
import com.linbit.linstor.proto.javainternal.MsgIntNodeDataOuterClass.NetIf;
import com.linbit.linstor.proto.javainternal.MsgIntNodeDataOuterClass.NodeConn;
import com.linbit.linstor.proto.javainternal.MsgIntNodeDeletedDataOuterClass.MsgIntNodeDeletedData;
import com.linbit.linstor.proto.javainternal.MsgIntObjectIdOuterClass.MsgIntObjectId;
import com.linbit.linstor.proto.javainternal.MsgIntPrimaryOuterClass;
import com.linbit.linstor.proto.javainternal.MsgIntRscDataOuterClass.MsgIntOtherRscData;
import com.linbit.linstor.proto.javainternal.MsgIntRscDataOuterClass.MsgIntRscData;
import com.linbit.linstor.proto.javainternal.MsgIntRscDataOuterClass.RscConnectionData;
import com.linbit.linstor.proto.javainternal.MsgIntRscDeletedDataOuterClass.MsgIntRscDeletedData;
import com.linbit.linstor.proto.javainternal.MsgIntSnapshotDataOuterClass;
import com.linbit.linstor.proto.javainternal.MsgIntSnapshotDataOuterClass.MsgIntSnapshotData;
import com.linbit.linstor.proto.javainternal.MsgIntSnapshotEndedDataOuterClass;
import com.linbit.linstor.proto.javainternal.MsgIntStorPoolDataOuterClass.MsgIntStorPoolData;
import com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.stateflags.FlagsHelper;
import com.linbit.utils.Base64;

import static java.util.stream.Collectors.toList;

public class ProtoCtrlStltSerializerBuilder extends ProtoCommonSerializerBuilder
    implements CtrlStltSerializer.CtrlStltSerializerBuilder
{
    private final CtrlSerializerHelper ctrlSerializerHelper;
    private final ResourceSerializerHelper rscSerializerHelper;
    private final SnapshotSerializerHelper snapshotSerializerHelper;
    private final NodeSerializerHelper nodeSerializerHelper;
    private final CtrlSecurityObjects secObjs;

    public ProtoCtrlStltSerializerBuilder(
        ErrorReporter errReporter,
        AccessContext serializerCtx,
        CtrlSecurityObjects secObjsRef,
        Props ctrlConfRef,
        final String apiCall,
        Long apiCallId,
        boolean isAnswer
    )
    {
        super(errReporter, serializerCtx, apiCall, apiCallId, isAnswer);
        secObjs = secObjsRef;

        ctrlSerializerHelper = new CtrlSerializerHelper(ctrlConfRef);
        rscSerializerHelper = new ResourceSerializerHelper();
        snapshotSerializerHelper = new SnapshotSerializerHelper();
        nodeSerializerHelper = new NodeSerializerHelper();
    }

    /*
     * Controller -> Satellite
     */
    @Override
    public ProtoCtrlStltSerializerBuilder authMessage(UUID nodeUuid, String nodeName, byte[] sharedSecret)
    {
        try
        {
            MsgIntAuthOuterClass.MsgIntAuth.newBuilder()
                .setNodeUuid(nodeUuid.toString())
                .setNodeName(nodeName)
                .setSharedSecret(ByteString.copyFrom(sharedSecret))
                .build()
                .writeDelimitedTo(baos);
        }
        catch (IOException exc)
        {
            handleIOException(exc);
        }
        return this;
    }

    // no fullSync- or update-id needed
    @Override
    public ProtoCtrlStltSerializerBuilder changedNode(UUID nodeUuid, String nodeName)
    {
        appendObjectId(nodeUuid, nodeName);
        return this;

    }

    // no fullSync- or update-id needed
    @Override
    public ProtoCtrlStltSerializerBuilder changedResource(UUID rscUuid, String rscName)
    {
        appendObjectId(rscUuid, rscName);
        return this;
    }

    // no fullSync- or update-id needed
    @Override
    public ProtoCtrlStltSerializerBuilder changedStorPool(UUID storPoolUuid, String storPoolName)
    {
        appendObjectId(storPoolUuid, storPoolName);
        return this;
    }

    @Override
    public ProtoCtrlStltSerializerBuilder changedSnapshot(
        String rscName,
        UUID snapshotUuid,
        String snapshotName
    )
    {
        appendObjectId(null, rscName);
        appendObjectId(snapshotUuid, snapshotName);
        return this;
    }

    @Override
    public ProtoCtrlStltSerializerBuilder controllerData(
        long fullSyncTimestamp,
        long serializerid
    )
    {
        try
        {
            ctrlSerializerHelper
                .buildControllerDataMsg(fullSyncTimestamp, serializerid)
                .writeDelimitedTo(baos);
        }
        catch (IOException exc)
        {
            handleIOException(exc);
        }
        return this;
    }

    @Override
    public ProtoCtrlStltSerializerBuilder nodeData(
        Node node,
        Collection<Node> relatedNodes,
        long fullSyncTimestamp,
        long serializerId
    )
    {
        try
        {
            nodeSerializerHelper
                .buildNodeDataMsg(node, relatedNodes, fullSyncTimestamp, serializerId)
                .writeDelimitedTo(baos);
        }
        catch (IOException exc)
        {
            handleIOException(exc);
        }
        catch (AccessDeniedException exc)
        {
            handleAccessDeniedException(exc);
        }
        return this;
    }

    @Override
    public ProtoCtrlStltSerializerBuilder deletedNodeData(
        String nodeNameStr,
        long fullSyncTimestamp,
        long updateId
    )
    {
        try
        {
            MsgIntNodeDeletedData.newBuilder()
                .setNodeName(nodeNameStr)
                .setFullSyncId(fullSyncTimestamp)
                .setUpdateId(updateId)
                .build()
                .writeDelimitedTo(baos);
        }
        catch (IOException exc)
        {
            handleIOException(exc);
        }
        return this;
    }

    @Override
    public ProtoCtrlStltSerializerBuilder resourceData(
        Resource localResource,
        long fullSyncTimestamp,
        long updateId
    )
    {
        try
        {
            rscSerializerHelper
                .buildResourceDataMsg(localResource, fullSyncTimestamp, updateId)
                .writeDelimitedTo(baos);
        }
        catch (IOException exc)
        {
            handleIOException(exc);
        }
        catch (AccessDeniedException exc)
        {
            handleAccessDeniedException(exc);
        }
        return this;
    }

    @Override
    public ProtoCtrlStltSerializerBuilder deletedResourceData(
        String rscNameStr,
        long fullSyncTimestamp,
        long updateId
    )
    {
        try
        {
            MsgIntRscDeletedData.newBuilder()
                .setRscName(rscNameStr)
                .setFullSyncId(fullSyncTimestamp)
                .setUpdateId(updateId)
                .build()
                .writeDelimitedTo(baos);
        }
        catch (IOException exc)
        {
            handleIOException(exc);
        }
        return this;
    }

    @Override
    public ProtoCtrlStltSerializerBuilder storPoolData(
        StorPool storPool,
        long fullSyncTimestamp,
        long updateId
    )
    {
        try
        {
            buildStorPoolDataMsg(storPool, fullSyncTimestamp, updateId)
                .writeDelimitedTo(baos);
        }
        catch (IOException exc)
        {
            handleIOException(exc);
        }
        catch (AccessDeniedException exc)
        {
            handleAccessDeniedException(exc);
        }
        return this;
    }

    @Override
    public ProtoCtrlStltSerializerBuilder deletedStorPoolData(
        String storPoolNameStr,
        long fullSyncTimestamp,
        long updateId
    )
    {
        try
        {
            MsgIntStorPoolDeletedData.newBuilder()
                .setStorPoolName(storPoolNameStr)
                .setFullSyncId(fullSyncTimestamp)
                .setUpdateId(updateId)
                .build()
                .writeDelimitedTo(baos);
        }
        catch (IOException exc)
        {
            handleIOException(exc);
        }
        return this;
    }

    @Override
    public ProtoCtrlStltSerializerBuilder snapshotData(
        Snapshot snapshot, long fullSyncId, long updateId
    )
    {
        try
        {
            snapshotSerializerHelper
                .buildSnapshotDataMsg(snapshot, fullSyncId, updateId)
                .writeDelimitedTo(baos);
        }
        catch (IOException exc)
        {
            handleIOException(exc);
        }
        catch (AccessDeniedException exc)
        {
            handleAccessDeniedException(exc);
        }
        return this;
    }

    @Override
    public ProtoCtrlStltSerializerBuilder endedSnapshotData(
        String resourceNameStr, String snapshotNameStr, long fullSyncId, long updateId
    )
    {
        try
        {
            MsgIntSnapshotEndedDataOuterClass.MsgIntSnapshotEndedData.newBuilder()
                .setRscName(resourceNameStr)
                .setSnapshotName(snapshotNameStr)
                .setFullSyncId(fullSyncId)
                .setUpdateId(updateId)
                .build()
                .writeDelimitedTo(baos);
        }
        catch (IOException exc)
        {
            handleIOException(exc);
        }
        return this;
    }

    @Override
    public ProtoCtrlStltSerializerBuilder fullSync(
        Set<Node> nodeSet,
        Set<StorPool> storPools,
        Set<Resource> resources,
        Set<Snapshot> snapshots,
        long fullSyncTimestamp,
        long updateId
    )
    {
        try
        {
            ArrayList<MsgIntNodeData> serializedNodes = new ArrayList<>();
            ArrayList<MsgIntStorPoolData> serializedStorPools = new ArrayList<>();
            ArrayList<MsgIntRscData> serializedRscs = new ArrayList<>();
            ArrayList<MsgIntSnapshotData> serializedSnapshots = new ArrayList<>();

            MsgIntControllerData serializedController = ctrlSerializerHelper.buildControllerDataMsg(
                fullSyncTimestamp,
                updateId
            );

            LinkedList<Node> nodes = new LinkedList<>(nodeSet);

            while (!nodes.isEmpty())
            {
                Node node = nodes.removeFirst();
                serializedNodes.add(
                    nodeSerializerHelper.buildNodeDataMsg(
                        node,
                        nodes,
                        fullSyncTimestamp,
                        updateId
                    )
                );
            }
            for (StorPool storPool : storPools)
            {
                serializedStorPools.add(
                    buildStorPoolDataMsg(
                        storPool,
                        fullSyncTimestamp,
                        updateId
                    )
                );
            }
            for (Resource rsc : resources)
            {
                if (rsc.iterateVolumes().hasNext())
                {
                    serializedRscs.add(
                        rscSerializerHelper.buildResourceDataMsg(
                            rsc,
                            fullSyncTimestamp,
                            updateId
                        )
                    );
                }
            }
            for (Snapshot snapshot : snapshots)
            {
                serializedSnapshots.add(
                    snapshotSerializerHelper.buildSnapshotDataMsg(
                        snapshot,
                        fullSyncTimestamp,
                        updateId
                    )
                );
            }

            String encodedMasterKey = "";
            byte[] cryptKey = secObjs.getCryptKey();
            if (cryptKey != null)
            {
                encodedMasterKey = Base64.encode(cryptKey);
            }
            MsgIntFullSync.newBuilder()
                .addAllNodes(serializedNodes)
                .addAllStorPools(serializedStorPools)
                .addAllRscs(serializedRscs)
                .addAllSnapshots(serializedSnapshots)
                .setFullSyncTimestamp(fullSyncTimestamp)
                .setMasterKey(encodedMasterKey)
                .setCtrlData(serializedController)
                .build()
                .writeDelimitedTo(baos);
        }
        catch (IOException exc)
        {
            handleIOException(exc);
        }
        catch (AccessDeniedException exc)
        {
            handleAccessDeniedException(exc);
        }
        return this;
    }

    /*
     * Satellite -> Controller
     */
    @Override
    public ProtoCtrlStltSerializerBuilder primaryRequest(String rscName, String rscUuid, boolean alreadyInitialized)
    {
        try
        {
            MsgIntPrimaryOuterClass.MsgIntPrimary.newBuilder()
                .setRscName(rscName)
                .setRscUuid(rscUuid)
                .setAlreadyInitialized(alreadyInitialized)
                .build()
                .writeDelimitedTo(baos);
        }
        catch (IOException exc)
        {
            handleIOException(exc);
        }
        return this;
    }

    @Override
    public ProtoCtrlStltSerializerBuilder notifyResourceApplied(
        Resource resource,
        Map<StorPool, SpaceInfo> freeSpaceMap
    )
    {
        try
        {
            List<MsgIntApplyRscSuccessOuterClass.VlmData> vlmDatas = new ArrayList<>();
            for (Volume vlm : resource.streamVolumes().collect(toList()))
            {
                vlmDatas.add(buildResourceAppliedVolume(vlm));
            }

            MsgIntApplyRscSuccessOuterClass.MsgIntApplyRscSuccess.newBuilder()
                .setRscId(
                    MsgIntObjectId.newBuilder()
                        .setUuid(resource.getUuid().toString())
                        .setName(resource.getDefinition().getName().displayValue)
                        .build()
                )
                .addAllFreeSpace(
                    ProtoStorPoolFreeSpaceUtils.getAllStorPoolFreeSpaces(freeSpaceMap)
                )
                .addAllVlmData(vlmDatas)
                .build()
                .writeDelimitedTo(baos);
        }
        catch (IOException exc)
        {
            handleIOException(exc);
        }
        catch (AccessDeniedException exc)
        {
            handleAccessDeniedException(exc);
        }
        return this;
    }

    private MsgIntApplyRscSuccessOuterClass.VlmData buildResourceAppliedVolume(Volume vlm)
        throws AccessDeniedException
    {
        MsgIntApplyRscSuccessOuterClass.VlmData.Builder builder = MsgIntApplyRscSuccessOuterClass.VlmData.newBuilder()
            .setVlmNr(vlm.getVolumeDefinition().getVolumeNumber().value)
            .addAllVlmDfnProps(asLinStorList(vlm.getVolumeDefinition().getProps(serializerCtx)));

        String backingDiskPath = vlm.getBackingDiskPath(serializerCtx);
        if (backingDiskPath != null)
        {
            builder.setBlockDevicePath(backingDiskPath);
        }

        String metaDiskPath = vlm.getMetaDiskPath(serializerCtx);
        if (metaDiskPath != null)
        {
            builder.setMetaDisk(metaDiskPath);
        }

        String devicePath = vlm.getDevicePath(serializerCtx);
        if (devicePath != null)
        {
            builder.setDevicePath(devicePath);
        }

        if (vlm.isNettoSizeSet(serializerCtx))
        {
            builder.setRealSize(vlm.getNettoSize(serializerCtx));
        }

        return builder.build();
    }

    @Override
    public ProtoCtrlStltSerializerBuilder requestNodeUpdate(UUID nodeUuid, String nodeName)
    {
        appendObjectId(nodeUuid, nodeName);
        return this;
    }

    @Override
    public ProtoCtrlStltSerializerBuilder requestControllerUpdate()
    {
        return requestNodeUpdate(UUID.randomUUID(), "thisnamedoesnotmatter");
    }

    @Override
    public ProtoCtrlStltSerializerBuilder requestResourceUpdate(UUID rscUuid, String nodeName, String rscName)
    {
        appendObjectId(null, nodeName);
        appendObjectId(rscUuid, rscName);
        return this;
    }

    @Override
    public ProtoCtrlStltSerializerBuilder requestStoragePoolUpdate(UUID storPoolUuid, String storPoolName)
    {
        appendObjectId(storPoolUuid, storPoolName);
        return this;
    }

    @Override
    public ProtoCtrlStltSerializerBuilder requestSnapshotUpdate(
        String rscName, UUID snapshotUuid, String snapshotName
    )
    {
        appendObjectId(null, rscName);
        appendObjectId(snapshotUuid, snapshotName);
        return this;
    }

    @Override
    public CtrlStltSerializer.CtrlStltSerializerBuilder updateFreeCapacity(
        UUID storPoolUuid, String storPoolName, SpaceInfo spaceInfo
    )
    {
        try
        {
            MsgIntApplyStorPoolSuccess.newBuilder()
                .setFreeSpace(StorPoolFreeSpaceOuterClass.StorPoolFreeSpace.newBuilder()
                    .setStorPoolUuid(storPoolUuid.toString())
                    .setStorPoolName(storPoolName)
                    .setFreeCapacity(spaceInfo.freeCapacity)
                    .setTotalCapacity(spaceInfo.totalCapacity)
                    .build()
                )
                .build()
                .writeDelimitedTo(baos);
        }
        catch (IOException exc)
        {
            handleIOException(exc);
        }
        return this;
    }

    @Override
    public ProtoCtrlStltSerializerBuilder cryptKey(
        byte[] cryptKey,
        long fullSyncTimestamp,
        long updateId
    )
    {
        try
        {
            MsgIntCryptKey.newBuilder()
                .setCryptKey(ByteString.copyFrom(cryptKey))
                .setFullSyncId(fullSyncTimestamp)
                .setUpdateId(updateId)
                .build()
                .writeDelimitedTo(baos);
        }
        catch (IOException exc)
        {
            handleIOException(exc);
        }
        return this;
    }

    /*
     * Helper methods
     */
    private void appendObjectId(UUID objUuid, String objName)
    {
        try
        {
            MsgIntObjectId.Builder msgBuilder = MsgIntObjectId.newBuilder();
            if (objUuid != null)
            {
                msgBuilder.setUuid(objUuid.toString());
            }
            msgBuilder
                .setName(objName)
                .build()
                .writeDelimitedTo(baos);
        }
        catch (IOException exc)
        {
            handleIOException(exc);
        }
    }

    private MsgIntStorPoolData buildStorPoolDataMsg(StorPool storPool, long fullSyncTimestamp, long updateId)
        throws AccessDeniedException
    {
        StorPoolDefinition storPoolDfn = storPool.getDefinition(serializerCtx);
        return MsgIntStorPoolData.newBuilder()
            .setStorPoolUuid(storPool.getUuid().toString())
            .setNodeUuid(storPool.getNode().getUuid().toString())
            .setStorPoolDfnUuid(storPoolDfn.getUuid().toString())
            .setStorPoolName(storPool.getName().displayValue)
            .setDriver(storPool.getDriverName())
            .setFreeSpaceMgrName(storPool.getFreeSpaceTracker().getName().displayValue)
            .addAllStorPoolProps(asLinStorList(storPool.getProps(serializerCtx)))
            .addAllStorPoolDfnProps(asLinStorList(storPoolDfn.getProps(serializerCtx)))
            .setFullSyncId(fullSyncTimestamp)
            .setUpdateId(updateId)
            .build();
    }

    private List<LinStorMapEntry> asLinStorList(Props props)
    {
        return ProtoMapUtils.fromMap(props.map());
    }

    private class NodeSerializerHelper
    {
        private MsgIntNodeData buildNodeDataMsg(
            Node node,
            Collection<Node> relatedNodes,
            long fullSyncTimestamp,
            long updateId
        )
            throws AccessDeniedException
        {
            return MsgIntNodeData.newBuilder()
                .setNodeUuid(node.getUuid().toString())
                .setNodeName(node.getName().displayValue)
                .setNodeFlags(node.getFlags().getFlagsBits(serializerCtx))
                .setNodeType(node.getNodeType(serializerCtx).name())
                .addAllNodeNetIfs(
                    getNetIfs(node)
                )
                .addAllNodeConns(
                    getNodeConns(node, relatedNodes)
                )
                .addAllNodeProps(
                    ProtoMapUtils.fromMap(node.getProps(serializerCtx).map())
                )
                .setFullSyncId(fullSyncTimestamp)
                .setUpdateId(updateId)
                .build();
        }

        private Iterable<? extends NetIf> getNetIfs(Node node) throws AccessDeniedException
        {
            ArrayList<NetIf> netIfs = new ArrayList<>();
            for (NetInterface netIf : node.streamNetInterfaces(serializerCtx).collect(toList()))
            {
                netIfs.add(
                    NetIf.newBuilder()
                        .setNetIfUuid(netIf.getUuid().toString())
                        .setNetIfName(netIf.getName().displayValue)
                        .setNetIfAddr(netIf.getAddress(serializerCtx).getAddress())
                        .build()
                );
            }
            return netIfs;
        }

        private ArrayList<NodeConn> getNodeConns(Node node, Collection<Node> otherNodes)
            throws AccessDeniedException
        {
            ArrayList<NodeConn> nodeConns = new ArrayList<>();
            for (Node otherNode : otherNodes)
            {
                NodeConnection nodeConnection = node.getNodeConnection(serializerCtx, otherNode);
                String otherName;

                if (nodeConnection != null)
                {
                    if (nodeConnection.getSourceNode(serializerCtx) == node)
                    {
                        otherName = otherNode.getName().displayValue;
                    }
                    else
                    {
                        otherName = node.getName().displayValue;
                    }

                    nodeConns.add(
                        NodeConn.newBuilder()
                            .setOtherNodeUuid(otherNode.getUuid().toString())
                            .setOtherNodeName(otherName)
                            .setOtherNodeType(otherNode.getNodeType(serializerCtx).name())
                            .setOtherNodeFlags(otherNode.getFlags().getFlagsBits(serializerCtx))
                            .setNodeConnUuid(nodeConnection.getUuid().toString())
                            .addAllNodeConnProps(
                                ProtoMapUtils.fromMap(
                                    nodeConnection.getProps(serializerCtx).map()
                                )
                            )
                            .build()
                    );
                }
            }
            return nodeConns;
        }
    }

    private class CtrlSerializerHelper
    {
        private Props ctrlConfProps;

        CtrlSerializerHelper(
            final Props ctrlConfRef
        )
        {
            ctrlConfProps = ctrlConfRef;
        }

        private MsgIntControllerData buildControllerDataMsg(
            long fullSyncTimestamp,
            long updateId)
        {
            return MsgIntControllerData.newBuilder()
                .addAllControllerProps(ProtoMapUtils.fromMap(ctrlConfProps.map()))
                .setFullSyncId(fullSyncTimestamp)
                .setUpdateId(updateId)
                .build();
        }
    }

    private class ResourceSerializerHelper
    {
        private MsgIntRscData buildResourceDataMsg(Resource localResource, long fullSyncTimestamp, long updateId)
            throws AccessDeniedException
        {
            List<Resource> otherResources = new ArrayList<>();
            Iterator<Resource> rscIterator = localResource.getDefinition().iterateResource(serializerCtx);
            while (rscIterator.hasNext())
            {
                Resource rsc = rscIterator.next();
                if (!rsc.equals(localResource))
                {
                    otherResources.add(rsc);
                }
            }

            ResourceDefinition rscDfn = localResource.getDefinition();
            String rscName = rscDfn.getName().displayValue;
            Map<String, String> rscDfnProps = rscDfn.getProps(serializerCtx).map();
            Map<String, String> rscProps = localResource.getProps(serializerCtx).map();

            return MsgIntRscData.newBuilder()
                .setRscName(rscName)
                .setRscDfnUuid(rscDfn.getUuid().toString())
                .setRscDfnPort(rscDfn.getPort(serializerCtx).value)
                .setRscDfnFlags(rscDfn.getFlags().getFlagsBits(serializerCtx))
                .setRscDfnSecret(rscDfn.getSecret(serializerCtx))
                .addAllRscDfnProps(ProtoMapUtils.fromMap(rscDfnProps))
                .setLocalRscUuid(localResource.getUuid().toString())
                .setLocalRscFlags(localResource.getStateFlags().getFlagsBits(serializerCtx))
                .setLocalRscNodeId(localResource.getNodeId().value)
                .addAllLocalRscProps(ProtoMapUtils.fromMap(rscProps))
                .addAllVlmDfns(
                    buildVlmDfnMessages(localResource)
                )
                .addAllLocalVolumes(
                    buildVlmMessages(localResource)
                )
                .addAllOtherResources(
                    buildOtherResources(otherResources)
                )
                .addAllRscConnections(buildRscConnections(localResource))
                .setRscDfnTransportType(rscDfn.getTransportType(serializerCtx).name())
                .setFullSyncId(fullSyncTimestamp)
                .setUpdateId(updateId)
                .build();
        }

        private Iterable<? extends RscConnectionData> buildRscConnections(Resource localResource)
            throws AccessDeniedException
        {
            List<RscConnectionData> list = new ArrayList<>();

            for (ResourceConnection conn : localResource.streamResourceConnections(serializerCtx)
                .collect(Collectors.toList()))
            {
                RscConnectionData.Builder builder = RscConnectionData.newBuilder()
                    .setUuid(conn.getUuid().toString())
                    .setNode1(conn.getSourceResource(serializerCtx).getAssignedNode().getName().getDisplayName())
                    .setNode2(conn.getTargetResource(serializerCtx).getAssignedNode().getName().getDisplayName())
                    .addAllProps(ProtoMapUtils.fromMap(conn.getProps(serializerCtx).map()))
                    .setFlags(conn.getStateFlags().getFlagsBits(serializerCtx));

                TcpPortNumber port = conn.getPort(serializerCtx);
                if (port != null)
                {
                    builder.setPort(port.value);
                }

                list.add(builder.build());
            }
            return list;
        }

        private Iterable<? extends VlmDfn> buildVlmDfnMessages(Resource localResource)
            throws AccessDeniedException
        {
            List<VlmDfn> list = new ArrayList<>();

            Iterator<Volume> localVolIterator = localResource.iterateVolumes();
            while (localVolIterator.hasNext())
            {
                Volume vol = localVolIterator.next();
                VolumeDefinition vlmDfn = vol.getVolumeDefinition();

                Map<String, String> vlmDfnProps = vlmDfn.getProps(serializerCtx).map();
                list.add(
                    VlmDfn.newBuilder()
                        .setVlmDfnUuid(vlmDfn.getUuid().toString())
                        .setVlmNr(vlmDfn.getVolumeNumber().value)
                        .setVlmSize(vlmDfn.getVolumeSize(serializerCtx))
                        .setVlmMinor(vlmDfn.getMinorNr(serializerCtx).value)
                        .addAllVlmFlags(
                            FlagsHelper.toStringList(
                                VolumeDefinition.VlmDfnFlags.class,
                                vlmDfn.getFlags().getFlagsBits(serializerCtx)
                            )
                        )
                        .addAllVlmProps(ProtoMapUtils.fromMap(vlmDfnProps))
                        .build()
                );
            }

            return list;
        }

        private List<Vlm> buildVlmMessages(Resource rsc)
            throws AccessDeniedException
        {
            List<Vlm> vlmList = new ArrayList<>();

            Iterator<Volume> volIterator = rsc.iterateVolumes();
            while (volIterator.hasNext())
            {
                Volume vol = volIterator.next();
                Map<String, String> volProps = vol.getProps(serializerCtx).map();
                StorPool vlmStorPool = vol.getStorPool(serializerCtx);
                Vlm.Builder builder = Vlm.newBuilder()
                    .setVlmDfnUuid(vol.getVolumeDefinition().getUuid().toString())
                    .setVlmUuid(vol.getUuid().toString())
                    .setVlmNr(vol.getVolumeDefinition().getVolumeNumber().value)
                    .setVlmMinorNr(vol.getVolumeDefinition().getMinorNr(serializerCtx).value)
                    .addAllVlmFlags(Volume.VlmFlags.toStringList(vol.getFlags().getFlagsBits(serializerCtx)))
                    .setStorPoolUuid(vlmStorPool.getUuid().toString())
                    .setStorPoolName(vlmStorPool.getName().displayValue)
                    .setStorPoolDriverName(vlmStorPool.getDriverName())
                    .setStorPoolDfnUuid(vlmStorPool.getDefinition(serializerCtx).getUuid().toString())
                    .addAllStorPoolDfnProps(
                        ProtoMapUtils.fromMap(
                            vlmStorPool.getDefinition(serializerCtx).getProps(serializerCtx).map())
                    )
                    .addAllStorPoolProps(
                        ProtoMapUtils.fromMap(
                            vlmStorPool.getProps(serializerCtx).map())
                    )
                    .addAllVlmProps(ProtoMapUtils.fromMap(volProps));
                String blockDev = vol.getBackingDiskPath(serializerCtx);
                if (blockDev != null)
                {
                    builder.setBackingDisk(blockDev);
                }
                String metaDisk = vol.getMetaDiskPath(serializerCtx);
                if (metaDisk != null)
                {
                    builder.setMetaDisk(metaDisk);
                }
                if (vol.isNettoSizeSet(serializerCtx))
                {
                    builder.setRealSize(vol.getNettoSize(serializerCtx));
                }

                vlmList.add(builder.build());
            }
            return vlmList;
        }

        private List<MsgIntOtherRscData> buildOtherResources(List<Resource> otherResources)
            throws AccessDeniedException
        {
            List<MsgIntOtherRscData> list = new ArrayList<>();

            for (Resource rsc : otherResources)
            {
                Node node = rsc.getAssignedNode();
                Map<String, String> rscProps = rsc.getProps(serializerCtx).map();
                list.add(
                    MsgIntOtherRscData.newBuilder()
                        .setNode(buildOtherNode(node))
                        .setNodeFlags(node.getFlags().getFlagsBits(serializerCtx))
                        .setRscUuid(rsc.getUuid().toString())
                        .setRscNodeId(rsc.getNodeId().value)
                        .setRscFlags(rsc.getStateFlags().getFlagsBits(serializerCtx))
                        .addAllRscProps(ProtoMapUtils.fromMap(rscProps))
                        .addAllLocalVlms(
                            buildVlmMessages(rsc)
                        )
                        .build()
                );
            }

            return list;
        }

        private NodeOuterClass.Node buildOtherNode(Node node) throws AccessDeniedException
        {
            Map<String, String> nodeProps = node.getProps(serializerCtx).map();
            return NodeOuterClass.Node.newBuilder()
                .setUuid(node.getUuid().toString())
                .setName(node.getName().displayValue)
                .setType(node.getNodeType(serializerCtx).name())
                .addAllProps(ProtoMapUtils.fromMap(nodeProps))
                .addAllNetInterfaces(buildNodeNetInterfaces(node))
                .build();
        }

        private Iterable<? extends NetInterfaceOuterClass.NetInterface> buildNodeNetInterfaces(Node node)
            throws AccessDeniedException
        {
            List<NetInterfaceOuterClass.NetInterface> protoNetIfs = new ArrayList<>();

            for (NetInterface netIf : node.streamNetInterfaces(serializerCtx).collect(toList()))
            {
                protoNetIfs.add(
                    NetInterfaceOuterClass.NetInterface.newBuilder()
                        .setUuid(netIf.getUuid().toString())
                        .setName(netIf.getName().displayValue)
                        .setAddress(netIf.getAddress(serializerCtx).getAddress())
                        .build()
                );
            }

            return protoNetIfs;
        }
    }

    private class SnapshotSerializerHelper
    {
        private MsgIntSnapshotData buildSnapshotDataMsg(Snapshot snapshot, long fullSyncId, long updateId)
            throws AccessDeniedException
        {
            SnapshotDefinition snapshotDfn = snapshot.getSnapshotDefinition();
            ResourceDefinition rscDfn = snapshotDfn.getResourceDefinition();

            List<MsgIntSnapshotDataOuterClass.SnapshotVlmDfn> snapshotVlmDfns = new ArrayList<>();
            for (SnapshotVolumeDefinition snapshotVolumeDefinition :
                snapshotDfn.getAllSnapshotVolumeDefinitions(serializerCtx))
            {
                snapshotVlmDfns.add(
                    MsgIntSnapshotDataOuterClass.SnapshotVlmDfn.newBuilder()
                        .setSnapshotVlmDfnUuid(snapshotVolumeDefinition.getUuid().toString())
                        .setVlmNr(snapshotVolumeDefinition.getVolumeNumber().value)
                        .setVlmSize(snapshotVolumeDefinition.getVolumeSize(serializerCtx))
                        .setFlags(snapshotVolumeDefinition.getFlags().getFlagsBits(serializerCtx))
                        .build()
                );
            }

            List<MsgIntSnapshotDataOuterClass.SnapshotVlm> snapshotVlms = new ArrayList<>();
            for (SnapshotVolume snapshotVolume : snapshot.getAllSnapshotVolumes(serializerCtx))
            {
                StorPool storPool = snapshotVolume.getStorPool(serializerCtx);
                snapshotVlms.add(
                    MsgIntSnapshotDataOuterClass.SnapshotVlm.newBuilder()
                        .setSnapshotVlmUuid(snapshotVolume.getUuid().toString())
                        .setSnapshotVlmDfnUuid(snapshotDfn.getUuid().toString())
                        .setVlmNr(snapshotVolume.getVolumeNumber().value)
                        .setStorPoolUuid(storPool.getUuid().toString())
                        .setStorPoolName(storPool.getName().displayValue)
                        .build()
                );
            }

            return MsgIntSnapshotData.newBuilder()
                .setRscName(rscDfn.getName().displayValue)
                .setRscDfnUuid(rscDfn.getUuid().toString())
                .setRscDfnPort(rscDfn.getPort(serializerCtx).value)
                .setRscDfnFlags(rscDfn.getFlags().getFlagsBits(serializerCtx))
                .setRscDfnSecret(rscDfn.getSecret(serializerCtx))
                .setRscDfnTransportType(rscDfn.getTransportType(serializerCtx).name())
                .addAllRscDfnProps(ProtoMapUtils.fromMap(rscDfn.getProps(serializerCtx).map()))
                .setSnapshotUuid(snapshot.getUuid().toString())
                .setSnapshotName(snapshotDfn.getName().displayValue)
                .setSnapshotDfnUuid(snapshotDfn.getUuid().toString())
                .addAllSnapshotVlmDfns(snapshotVlmDfns)
                .setSnapshotDfnFlags(snapshotDfn.getFlags().getFlagsBits(serializerCtx))
                .addAllSnapshotVlms(snapshotVlms)
                .setFlags(snapshot.getFlags().getFlagsBits(serializerCtx))
                .setSuspendResource(snapshot.getSuspendResource(serializerCtx))
                .setTakeSnapshot(snapshot.getTakeSnapshot(serializerCtx))
                .setFullSyncId(fullSyncId)
                .setUpdateId(updateId)
                .build();
        }
    }
}
