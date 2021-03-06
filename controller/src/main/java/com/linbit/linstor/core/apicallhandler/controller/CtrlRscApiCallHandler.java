package com.linbit.linstor.core.apicallhandler.controller;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.linbit.ImplementationError;
import com.linbit.InvalidNameException;
import com.linbit.ValueOutOfRangeException;
import com.linbit.linstor.InternalApiConsts;
import com.linbit.linstor.LinstorParsingUtils;
import com.linbit.linstor.Node;
import com.linbit.linstor.NodeName;
import com.linbit.linstor.NodeRepository;
import com.linbit.linstor.Resource;
import com.linbit.linstor.ResourceConnection;
import com.linbit.linstor.ResourceConnectionKey;
import com.linbit.linstor.ResourceData;
import com.linbit.linstor.ResourceDefinition;
import com.linbit.linstor.ResourceDefinitionRepository;
import com.linbit.linstor.ResourceName;
import com.linbit.linstor.StorPool;
import com.linbit.linstor.StorPoolName;
import com.linbit.linstor.Volume;
import com.linbit.linstor.VolumeNumber;
import com.linbit.linstor.annotation.ApiContext;
import com.linbit.linstor.annotation.PeerContext;
import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.ApiCallRcImpl;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.api.interfaces.serializer.CtrlClientSerializer;
import com.linbit.linstor.api.interfaces.serializer.CtrlStltSerializer;
import com.linbit.linstor.api.pojo.CapacityInfoPojo;
import com.linbit.linstor.api.pojo.RscConnPojo;
import com.linbit.linstor.api.pojo.VlmUpdatePojo;
import com.linbit.linstor.api.prop.LinStorObject;
import com.linbit.linstor.core.apicallhandler.response.ApiAccessDeniedException;
import com.linbit.linstor.core.apicallhandler.response.ApiOperation;
import com.linbit.linstor.core.apicallhandler.response.ApiRcException;
import com.linbit.linstor.core.apicallhandler.response.ApiSuccessUtils;
import com.linbit.linstor.core.apicallhandler.response.ResponseContext;
import com.linbit.linstor.core.apicallhandler.response.ResponseConverter;
import com.linbit.linstor.logging.ErrorReporter;
import com.linbit.linstor.netcom.Peer;
import com.linbit.linstor.propscon.Props;
import com.linbit.linstor.satellitestate.SatelliteState;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;

import static com.linbit.linstor.api.ApiConsts.API_LST_RSC;
import static com.linbit.linstor.api.ApiConsts.API_LST_RSC_CONN;
import static com.linbit.linstor.core.apicallhandler.controller.CtrlRscDfnApiCallHandler.getRscDfnDescriptionInline;
import static java.util.stream.Collectors.toList;

@Singleton
public class CtrlRscApiCallHandler
{
    private final ErrorReporter errorReporter;
    private final AccessContext apiCtx;
    private final CtrlTransactionHelper ctrlTransactionHelper;
    private final CtrlPropsHelper ctrlPropsHelper;
    private final CtrlApiDataLoader ctrlApiDataLoader;
    private final ResourceDefinitionRepository resourceDefinitionRepository;
    private final NodeRepository nodeRepository;
    private final CtrlClientSerializer clientComSerializer;
    private final CtrlStltSerializer ctrlStltSerializer;
    private final CtrlSatelliteUpdater ctrlSatelliteUpdater;
    private final ResponseConverter responseConverter;
    private final Provider<Peer> peer;
    private final Provider<AccessContext> peerAccCtx;

    @Inject
    public CtrlRscApiCallHandler(
        ErrorReporter errorReporterRef,
        @ApiContext AccessContext apiCtxRef,
        CtrlTransactionHelper ctrlTransactionHelperRef,
        CtrlPropsHelper ctrlPropsHelperRef,
        CtrlApiDataLoader ctrlApiDataLoaderRef,
        ResourceDefinitionRepository resourceDefinitionRepositoryRef,
        NodeRepository nodeRepositoryRef,
        CtrlClientSerializer clientComSerializerRef,
        CtrlStltSerializer ctrlStltSerializerRef,
        CtrlSatelliteUpdater ctrlSatelliteUpdaterRef,
        ResponseConverter responseConverterRef,
        Provider<Peer> peerRef,
        @PeerContext Provider<AccessContext> peerAccCtxRef
    )
    {
        errorReporter = errorReporterRef;
        apiCtx = apiCtxRef;
        ctrlTransactionHelper = ctrlTransactionHelperRef;
        ctrlPropsHelper = ctrlPropsHelperRef;
        ctrlApiDataLoader = ctrlApiDataLoaderRef;
        resourceDefinitionRepository = resourceDefinitionRepositoryRef;
        nodeRepository = nodeRepositoryRef;
        clientComSerializer = clientComSerializerRef;
        ctrlStltSerializer = ctrlStltSerializerRef;
        ctrlSatelliteUpdater = ctrlSatelliteUpdaterRef;
        responseConverter = responseConverterRef;
        peer = peerRef;
        peerAccCtx = peerAccCtxRef;
    }

    public ApiCallRc modifyResource(
        UUID rscUuid,
        String nodeNameStr,
        String rscNameStr,
        Map<String, String> overrideProps,
        Set<String> deletePropKeys
    )
    {
        ApiCallRcImpl responses = new ApiCallRcImpl();
        ResponseContext context = makeRscContext(
            ApiOperation.makeModifyOperation(),
            nodeNameStr,
            rscNameStr
        );

        try
        {
            ResourceData rsc = ctrlApiDataLoader.loadRsc(nodeNameStr, rscNameStr, true);

            if (rscUuid != null && !rscUuid.equals(rsc.getUuid()))
            {
                throw new ApiRcException(ApiCallRcImpl.simpleEntry(
                    ApiConsts.FAIL_UUID_RSC,
                    "UUID-check failed"
                ));
            }

            Props props = ctrlPropsHelper.getProps(rsc);
            Map<String, String> propsMap = props.map();

            ctrlPropsHelper.fillProperties(LinStorObject.RESOURCE, overrideProps, props, ApiConsts.FAIL_ACC_DENIED_RSC);

            for (String delKey : deletePropKeys)
            {
                propsMap.remove(delKey);
            }

            ctrlTransactionHelper.commit();

            responseConverter.addWithDetail(responses, context, ctrlSatelliteUpdater.updateSatellites(rsc));
            responseConverter.addWithOp(responses, context, ApiSuccessUtils.defaultModifiedEntry(
                rsc.getUuid(), getRscDescriptionInline(rsc)));
        }
        catch (Exception | ImplementationError exc)
        {
            responses = responseConverter.reportException(peer.get(), context, exc);
        }

        return responses;
    }

    byte[] listResources(
        long apiCallId,
        List<String> filterNodes,
        List<String> filterResources
    )
    {
        ArrayList<ResourceData.RscApi> rscs = new ArrayList<>();
        Map<NodeName, SatelliteState> satelliteStates = new HashMap<>();
        try
        {
            final List<String> upperFilterNodes = filterNodes.stream().map(String::toUpperCase).collect(toList());
            final List<String> upperFilterResources =
                filterResources.stream().map(String::toUpperCase).collect(toList());

            resourceDefinitionRepository.getMapForView(peerAccCtx.get()).values().stream()
                .filter(rscDfn -> upperFilterResources.isEmpty() ||
                    upperFilterResources.contains(rscDfn.getName().value))
                .forEach(rscDfn ->
                {
                    try
                    {
                        for (Resource rsc : rscDfn.streamResource(peerAccCtx.get())
                            .filter(rsc -> upperFilterNodes.isEmpty() ||
                                upperFilterNodes.contains(rsc.getAssignedNode().getName().value))
                            .collect(toList()))
                        {
                            rscs.add(rsc.getApiData(peerAccCtx.get(), null, null));
                            // fullSyncId and updateId null, as they are not going to be serialized anyways
                        }
                    }
                    catch (AccessDeniedException accDeniedExc)
                    {
                        // don't add storpooldfn without access
                    }
                }
                );

            // get resource states of all nodes
            for (final Node node : nodeRepository.getMapForView(peerAccCtx.get()).values())
            {
                if (upperFilterNodes.isEmpty() || upperFilterNodes.contains(node.getName().value))
                {
                    final Peer curPeer = node.getPeer(peerAccCtx.get());
                    if (curPeer != null)
                    {
                        Lock readLock = curPeer.getSatelliteStateLock().readLock();
                        readLock.lock();
                        try
                        {
                            final SatelliteState satelliteState = curPeer.getSatelliteState();

                            if (satelliteState != null)
                            {
                                final SatelliteState filterStates = new SatelliteState(satelliteState);

                                // states are already complete, we remove all resource that are not interesting from
                                // our clone
                                Set<ResourceName> removeSet = new TreeSet<>();
                                for (ResourceName rscName : filterStates.getResourceStates().keySet())
                                {
                                    if (!(upperFilterResources.isEmpty() ||
                                          upperFilterResources.contains(rscName.value)))
                                    {
                                        removeSet.add(rscName);
                                    }
                                }
                                removeSet.forEach(rscName -> filterStates.getResourceStates().remove(rscName));
                                satelliteStates.put(node.getName(), filterStates);
                            }
                        }
                        finally
                        {
                            readLock.unlock();
                        }
                    }
                }
            }
        }
        catch (AccessDeniedException accDeniedExc)
        {
            // for now return an empty list.
            errorReporter.reportError(accDeniedExc);
        }

        return clientComSerializer
                .answerBuilder(API_LST_RSC, apiCallId)
                .resourceList(rscs, satelliteStates)
                .build();
    }

    byte[] listResourceConnections(
        long apiCallId,
        final String rscNameString
    )
    {
        ResourceName rscName = null;
        List<ResourceConnection.RscConnApi> rscConns = new ArrayList<>();
        try
        {
            rscName = new ResourceName(rscNameString);

            ResourceDefinition rscDfn = resourceDefinitionRepository.get(apiCtx, rscName);

            if (rscDfn  != null)
            {
                for (Resource rsc : rscDfn.streamResource(apiCtx).collect(toList()))
                {
                    List<ResourceConnection> rscConnections = rsc.streamResourceConnections(apiCtx).collect(toList());
                    for (ResourceConnection rscConn : rscConnections)
                    {
                        if (rscConns.stream().noneMatch(con -> con.getUuid() == rscConn.getUuid()))
                        {
                            rscConns.add(rscConn.getApiData(apiCtx));
                        }
                    }
                }

                // lazy instance other resource connections
                List<Resource> resourceList = rscDfn.streamResource(apiCtx).collect(toList());
                for (int i = 0; i < resourceList.size(); i++)
                {
                    for (int j = 1; j < resourceList.size(); j++)
                    {
                        ResourceConnectionKey conKey =
                            new ResourceConnectionKey(resourceList.get(i), resourceList.get(j));

                        if (conKey.getSource() != conKey.getTarget() &&
                            rscConns.stream().noneMatch(con ->
                                con.getSourceNodeName().equalsIgnoreCase(
                                    conKey.getSource().getAssignedNode().getName().getName()) &&
                                con.getTargetNodeName().equalsIgnoreCase(
                                    conKey.getTarget().getAssignedNode().getName().getName()) &&
                                con.getResourceName().equalsIgnoreCase(rscNameString)))
                        {
                            rscConns.add(new RscConnPojo(
                                UUID.randomUUID(),
                                conKey.getSource().getAssignedNode().getName().getDisplayName(),
                                conKey.getTarget().getAssignedNode().getName().getDisplayName(),
                                rscDfn.getName().getDisplayName(),
                                new HashMap<>(),
                                0,
                                null
                            ));
                        }
                    }
                }
            }
            else
            {
                throw new ApiRcException(ApiCallRcImpl.simpleEntry(
                    ApiConsts.FAIL_NOT_FOUND_RSC_DFN,
                    String.format("Resource definition '%s' not found.", rscNameString)
                ));
            }
        }
        catch (InvalidNameException exc)
        {
            throw new ApiRcException(ApiCallRcImpl.simpleEntry(
                ApiConsts.FAIL_INVLD_RSC_NAME,
                "Invalid resource name used"
            ), exc);
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw new ApiAccessDeniedException(
                accDeniedExc,
                "access " + getRscDfnDescriptionInline(rscName.displayValue),
                ApiConsts.FAIL_ACC_DENIED_RSC_DFN
            );
        }

        return clientComSerializer
            .answerBuilder(API_LST_RSC_CONN, apiCallId)
            .resourceConnList(rscConns)
            .build();
    }

    public void respondResource(
        long apiCallId,
        String nodeNameStr,
        UUID rscUuid,
        String rscNameStr
    )
    {
        try
        {
            NodeName nodeName = new NodeName(nodeNameStr);

            Node node = nodeRepository.get(apiCtx, nodeName);

            if (node != null)
            {
                ResourceName rscName = new ResourceName(rscNameStr);
                Resource rsc = !node.isDeleted() ? node.getResource(apiCtx, rscName) : null;

                long fullSyncTimestamp = peer.get().getFullSyncId();
                long updateId = peer.get().getNextSerializerId();
                // TODO: check if the localResource has the same uuid as rscUuid
                if (rsc != null && !rsc.isDeleted())
                {
                    peer.get().sendMessage(
                        ctrlStltSerializer
                            .onewayBuilder(InternalApiConsts.API_APPLY_RSC)
                            .resourceData(rsc, fullSyncTimestamp, updateId)
                            .build()
                    );
                }
                else
                {
                    peer.get().sendMessage(
                        ctrlStltSerializer
                            .onewayBuilder(InternalApiConsts.API_APPLY_RSC_DELETED)
                            .deletedResourceData(rscNameStr, fullSyncTimestamp, updateId)
                            .build()
                    );
                }
            }
            else
            {
                errorReporter.reportError(
                    new ImplementationError(
                        "Satellite requested resource '" + rscNameStr + "' on node '" + nodeNameStr + "' " +
                            "but that node does not exist.",
                        null
                    )
                );
                peer.get().closeConnection();
            }
        }
        catch (InvalidNameException invalidNameExc)
        {
            errorReporter.reportError(
                new ImplementationError(
                    "Satellite requested data for invalid name (node or rsc name).",
                    invalidNameExc
                )
            );
        }
        catch (AccessDeniedException accDeniedExc)
        {
            errorReporter.reportError(
                new ImplementationError(
                    "Controller's api context has not enough privileges to gather requested resource data.",
                    accDeniedExc
                )
            );
        }
    }

    void updateVolumeData(
        String resourceName,
        List<VlmUpdatePojo> vlmUpdates,
        List<CapacityInfoPojo> capacityInfos
    )
    {
        try
        {
            NodeName nodeName = peer.get().getNode().getName();
            Map<StorPoolName, CapacityInfoPojo> storPoolToCapacityInfoMap = capacityInfos.stream().collect(
                Collectors.toMap(
                    freeSpacePojo -> LinstorParsingUtils.asStorPoolName(freeSpacePojo.getStorPoolName()),
                    Function.identity()
                )
            );
            ResourceDefinition rscDfn = resourceDefinitionRepository.get(apiCtx, new ResourceName(resourceName));
            Resource rsc = rscDfn.getResource(apiCtx, nodeName);

            for (VlmUpdatePojo vlmUpd : vlmUpdates)
            {
                try
                {
                    Volume vlm = rsc.getVolume(new VolumeNumber(vlmUpd.getVolumeNumber()));
                    if (vlm != null)
                    {
                        vlm.setBackingDiskPath(apiCtx, vlmUpd.getBlockDevicePath());
                        vlm.setMetaDiskPath(apiCtx, vlmUpd.getMetaDiskPath());
                        vlm.setDevicePath(apiCtx, vlmUpd.getDevicePath());
                        vlm.setNettoSize(apiCtx, vlmUpd.getRealSize());

                        Map<String, String> propsMap = vlm.getVolumeDefinition().getProps(apiCtx).map();
                        propsMap.clear();
                        propsMap.putAll(vlmUpd.getVlmDfnPropsMap());

                        StorPool storPool = vlm.getStorPool(apiCtx);
                        CapacityInfoPojo capacityInfo =
                            storPoolToCapacityInfoMap.get(storPool.getName());

                        storPool.getFreeSpaceTracker().vlmCreationFinished(
                            apiCtx,
                            vlm,
                            capacityInfo == null ? null : capacityInfo.getFreeCapacity(),
                            capacityInfo == null ? null : capacityInfo.getTotalCapacity()
                        );

                        if (capacityInfo == null && !storPool.getDriverKind().usesThinProvisioning())
                        {
                            errorReporter.logWarning(
                                String.format(
                                    "No freespace info for storage pool '%s' on node: %s",
                                    storPool.getName().value,
                                    nodeName.displayValue
                                )
                            );
                        }

                    }
                    else
                    {
                        errorReporter.logWarning(
                            String.format(
                                "Tried to update a non existing volume. Node: %s, Resource: %s, VolumeNr: %d",
                                nodeName.displayValue,
                                rscDfn.getName().displayValue,
                                vlmUpd.getVolumeNumber()
                            )
                        );
                    }
                }
                catch (ValueOutOfRangeException ignored)
                {
                }
            }
            ctrlTransactionHelper.commit();
        }
        catch (InvalidNameException | AccessDeniedException exc)
        {
            throw new ImplementationError(exc);
        }
    }

    public static String getRscDescription(Resource resource)
    {
        return getRscDescription(
            resource.getAssignedNode().getName().displayValue, resource.getDefinition().getName().displayValue);
    }

    public static String getRscDescription(String nodeNameStr, String rscNameStr)
    {
        return "Node: " + nodeNameStr + ", Resource: " + rscNameStr;
    }

    public static String getRscDescriptionInline(Resource rsc)
    {
        return getRscDescriptionInline(rsc.getAssignedNode(), rsc.getDefinition());
    }

    public static String getRscDescriptionInline(Node node, ResourceDefinition rscDfn)
    {
        return getRscDescriptionInline(node.getName().displayValue, rscDfn.getName().displayValue);
    }

    public static String getRscDescriptionInline(String nodeNameStr, String rscNameStr)
    {
        return "resource '" + rscNameStr + "' on node '" + nodeNameStr + "'";
    }

    static ResponseContext makeRscContext(
        ApiOperation operation,
        String nodeNameStr,
        String rscNameStr
    )
    {
        Map<String, String> objRefs = new TreeMap<>();
        objRefs.put(ApiConsts.KEY_NODE, nodeNameStr);
        objRefs.put(ApiConsts.KEY_RSC_DFN, rscNameStr);

        return new ResponseContext(
            operation,
            getRscDescription(nodeNameStr, rscNameStr),
            getRscDescriptionInline(nodeNameStr, rscNameStr),
            ApiConsts.MASK_RSC,
            objRefs
        );
    }
}
