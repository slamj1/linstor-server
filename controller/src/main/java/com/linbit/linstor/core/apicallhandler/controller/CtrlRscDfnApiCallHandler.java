package com.linbit.linstor.core.apicallhandler.controller;

import com.linbit.ExhaustedPoolException;
import com.linbit.ImplementationError;
import com.linbit.ValueInUseException;
import com.linbit.ValueOutOfRangeException;
import com.linbit.linstor.InternalApiConsts;
import com.linbit.linstor.LinStorDataAlreadyExistsException;
import com.linbit.linstor.LinStorException;
import com.linbit.linstor.LinstorParsingUtils;
import com.linbit.linstor.Resource;
import com.linbit.linstor.ResourceDefinition;
import com.linbit.linstor.ResourceDefinition.TransportType;
import com.linbit.linstor.ResourceDefinitionData;
import com.linbit.linstor.ResourceDefinitionDataControllerFactory;
import com.linbit.linstor.ResourceDefinitionRepository;
import com.linbit.linstor.ResourceName;
import com.linbit.linstor.SnapshotDefinition;
import com.linbit.linstor.SnapshotVolumeDefinition;
import com.linbit.linstor.TcpPortNumber;
import com.linbit.linstor.VolumeDefinition;
import com.linbit.linstor.VolumeDefinition.VlmDfnApi;
import com.linbit.linstor.VolumeDefinitionData;
import com.linbit.linstor.VolumeNumber;
import com.linbit.linstor.VolumeNumberAlloc;
import com.linbit.linstor.annotation.ApiContext;
import com.linbit.linstor.annotation.PeerContext;
import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.ApiCallRcImpl;
import com.linbit.linstor.api.ApiCallRcImpl.ApiCallRcEntry;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.api.interfaces.serializer.CtrlClientSerializer;
import com.linbit.linstor.api.interfaces.serializer.CtrlStltSerializer;
import com.linbit.linstor.api.prop.LinStorObject;
import com.linbit.linstor.core.apicallhandler.response.ApiAccessDeniedException;
import com.linbit.linstor.core.apicallhandler.response.ApiOperation;
import com.linbit.linstor.core.apicallhandler.response.ApiRcException;
import com.linbit.linstor.core.apicallhandler.response.ApiSQLException;
import com.linbit.linstor.core.apicallhandler.response.ApiSuccessUtils;
import com.linbit.linstor.core.apicallhandler.response.ResponseContext;
import com.linbit.linstor.core.apicallhandler.response.ResponseConverter;
import com.linbit.linstor.logging.ErrorReporter;
import com.linbit.linstor.netcom.Peer;
import com.linbit.linstor.propscon.InvalidKeyException;
import com.linbit.linstor.propscon.InvalidValueException;
import com.linbit.linstor.propscon.Props;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.security.AccessType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Stream;

import static com.linbit.utils.StringUtils.firstLetterCaps;

@Singleton
public class CtrlRscDfnApiCallHandler
{
    private final ErrorReporter errorReporter;
    private final AccessContext apiCtx;
    private final CtrlVlmDfnApiCallHandler vlmDfnHandler;
    private final CtrlTransactionHelper ctrlTransactionHelper;
    private final CtrlPropsHelper ctrlPropsHelper;
    private final CtrlApiDataLoader ctrlApiDataLoader;
    private final ResourceDefinitionDataControllerFactory resourceDefinitionDataFactory;
    private final ResourceDefinitionRepository resourceDefinitionRepository;
    private final CtrlClientSerializer clientComSerializer;
    private final CtrlStltSerializer ctrlStltSerializer;
    private final CtrlSatelliteUpdater ctrlSatelliteUpdater;
    private final ResponseConverter responseConverter;
    private final Provider<Peer> peer;
    private final Provider<AccessContext> peerAccCtx;

    @Inject
    public CtrlRscDfnApiCallHandler(
        ErrorReporter errorReporterRef,
        @ApiContext AccessContext apiCtxRef,
        CtrlVlmDfnApiCallHandler vlmDfnHandlerRef,
        CtrlTransactionHelper ctrlTransactionHelperRef,
        CtrlPropsHelper ctrlPropsHelperRef,
        CtrlApiDataLoader ctrlApiDataLoaderRef,
        ResourceDefinitionDataControllerFactory resourceDefinitionDataFactoryRef,
        ResourceDefinitionRepository resourceDefinitionRepositoryRef,
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
        vlmDfnHandler = vlmDfnHandlerRef;
        ctrlTransactionHelper = ctrlTransactionHelperRef;
        ctrlPropsHelper = ctrlPropsHelperRef;
        ctrlApiDataLoader = ctrlApiDataLoaderRef;
        resourceDefinitionDataFactory = resourceDefinitionDataFactoryRef;
        resourceDefinitionRepository = resourceDefinitionRepositoryRef;
        clientComSerializer = clientComSerializerRef;
        ctrlStltSerializer = ctrlStltSerializerRef;
        ctrlSatelliteUpdater = ctrlSatelliteUpdaterRef;
        responseConverter = responseConverterRef;
        peer = peerRef;
        peerAccCtx = peerAccCtxRef;
    }

    public ApiCallRc createResourceDefinition(
        String rscNameStr,
        Integer portInt,
        String secret,
        String transportTypeStr,
        Map<String, String> props,
        List<VlmDfnApi> volDescrMap
    )
    {
        ApiCallRcImpl responses = new ApiCallRcImpl();
        ResponseContext context = makeResourceDefinitionContext(
            ApiOperation.makeCreateOperation(),
            rscNameStr
        );

        try
        {
            requireRscDfnMapChangeAccess();
            ResourceDefinitionData rscDfn = createRscDfn(rscNameStr, transportTypeStr, portInt, secret);

            ctrlPropsHelper.fillProperties(LinStorObject.RESOURCE_DEFINITION, props,
                ctrlPropsHelper.getProps(rscDfn), ApiConsts.FAIL_ACC_DENIED_RSC_DFN);

            List<VolumeDefinitionData> createdVlmDfns = vlmDfnHandler.createVlmDfns(rscDfn, volDescrMap);

            resourceDefinitionRepository.put(apiCtx, rscDfn.getName(), rscDfn);

            ctrlTransactionHelper.commit();

            for (VolumeDefinitionData vlmDfn : createdVlmDfns)
            {
                ApiCallRcEntry volSuccessEntry = new ApiCallRcEntry();
                volSuccessEntry.setReturnCode(ApiConsts.MASK_VLM_DFN | ApiConsts.CREATED);
                String successMessage = String.format(
                    "Volume definition with number '%d' successfully " +
                        " created in resource definition '%s'.",
                    vlmDfn.getVolumeNumber().value,
                    rscNameStr
                );
                volSuccessEntry.setMessage(successMessage);
                volSuccessEntry.putObjRef(ApiConsts.KEY_RSC_DFN, rscNameStr);
                volSuccessEntry.putObjRef(ApiConsts.KEY_VLM_NR, Integer.toString(vlmDfn.getVolumeNumber().value));
                volSuccessEntry.putObjRef(ApiConsts.KEY_MINOR_NR, Integer.toString(vlmDfn.getMinorNr(apiCtx).value));

                responses.addEntry(volSuccessEntry);

                errorReporter.logInfo(successMessage);
            }

            responseConverter.addWithOp(responses, context, ApiSuccessUtils.defaultCreatedEntry(
                rscDfn.getUuid(), getRscDfnDescriptionInline(rscDfn)));
        }
        catch (Exception | ImplementationError exc)
        {
            responses = responseConverter.reportException(peer.get(), context, exc);
        }

        return responses;
    }

    public ApiCallRc modifyRscDfn(
        UUID rscDfnUuid,
        String rscNameStr,
        Integer portInt,
        Map<String, String> overrideProps,
        Set<String> deletePropKeys
    )
    {
        ApiCallRcImpl responses = new ApiCallRcImpl();
        ResponseContext context = makeResourceDefinitionContext(
            ApiOperation.makeModifyOperation(),
            rscNameStr
        );

        try
        {
            requireRscDfnMapChangeAccess();

            ResourceName rscName = LinstorParsingUtils.asRscName(rscNameStr);
            ResourceDefinitionData rscDfn = ctrlApiDataLoader.loadRscDfn(rscName, true);
            if (rscDfnUuid != null && !rscDfnUuid.equals(rscDfn.getUuid()))
            {
                throw new ApiRcException(ApiCallRcImpl
                    .entryBuilder(
                        ApiConsts.FAIL_UUID_RSC_DFN,
                        "UUID-check failed"
                    )
                    .setDetails("local UUID: " + rscDfn.getUuid().toString() +
                        ", received UUID: " + rscDfnUuid.toString())
                    .build()
                );
            }
            if (portInt != null)
            {
                TcpPortNumber port = LinstorParsingUtils.asTcpPortNumber(portInt);
                rscDfn.setPort(peerAccCtx.get(), port);
            }
            if (!overrideProps.isEmpty() || !deletePropKeys.isEmpty())
            {
                Map<String, String> map = ctrlPropsHelper.getProps(rscDfn).map();

                ctrlPropsHelper.fillProperties(LinStorObject.RESOURCE_DEFINITION, overrideProps,
                    ctrlPropsHelper.getProps(rscDfn), ApiConsts.FAIL_ACC_DENIED_RSC_DFN);

                for (String delKey : deletePropKeys)
                {
                    String oldValue = map.remove(delKey);
                    if (oldValue == null)
                    {
                        responseConverter.addWithDetail(responses, context, ApiCallRcImpl.simpleEntry(
                            ApiConsts.WARN_DEL_UNSET_PROP,
                            "Could not delete property '" + delKey + "' as it did not exist. " +
                                                "This operation had no effect."
                        ));
                    }
                }
            }

            ctrlTransactionHelper.commit();

            responseConverter.addWithOp(responses, context, ApiSuccessUtils.defaultModifiedEntry(
                rscDfn.getUuid(), getRscDfnDescriptionInline(rscDfn)));
            responseConverter.addWithDetail(responses, context, ctrlSatelliteUpdater.updateSatellites(rscDfn));
        }
        catch (Exception | ImplementationError exc)
        {
            responses = responseConverter.reportException(peer.get(), context, exc);
        }

        return responses;
    }

    void handlePrimaryResourceRequest(
        long apiCallId,
        String rscNameStr,
        UUID rscUuid,
        boolean alreadyInitialized
    )
    {
        Peer currentPeer = peer.get();
        try
        {
            Resource res = ctrlApiDataLoader.loadRsc(currentPeer.getNode().getName().displayValue, rscNameStr, true);
            ResourceDefinitionData resDfn = (ResourceDefinitionData) res.getDefinition();

            Props resDfnProps = ctrlPropsHelper.getProps(resDfn);
            if (resDfnProps.getProp(InternalApiConsts.PROP_PRIMARY_SET) == null)
            {
                resDfnProps.setProp(
                    InternalApiConsts.PROP_PRIMARY_SET,
                    res.getAssignedNode().getName().value
                );

                ctrlTransactionHelper.commit();

                errorReporter.logTrace(
                    "Primary set for " + currentPeer.getNode().getName().getDisplayName() + "; " +
                        " already initialized: " + alreadyInitialized
                );

                ctrlSatelliteUpdater.updateSatellites(resDfn);

                if (!alreadyInitialized)
                {
                    currentPeer.sendMessage(
                        ctrlStltSerializer
                            .onewayBuilder(InternalApiConsts.API_PRIMARY_RSC)
                            .primaryRequest(rscNameStr, res.getUuid().toString(), false)
                            .build()
                    );
                }
            }
        }
        catch (InvalidKeyException | InvalidValueException | AccessDeniedException ignored)
        {
        }
        catch (SQLException sqlExc)
        {
            String errorMessage = String.format(
                "A database error occured while trying to rollback the deletion of " +
                    "resource definition '%s'.",
                rscNameStr
            );
            errorReporter.reportError(
                sqlExc,
                peerAccCtx.get(),
                currentPeer,
                errorMessage
            );
        }
    }

    byte[] listResourceDefinitions(long apiCallId)
    {
        ArrayList<ResourceDefinitionData.RscDfnApi> rscdfns = new ArrayList<>();
        try
        {
            for (ResourceDefinition rscdfn : resourceDefinitionRepository.getMapForView(peerAccCtx.get()).values())
            {
                try
                {
                    rscdfns.add(rscdfn.getApiData(peerAccCtx.get()));
                }
                catch (AccessDeniedException accDeniedExc)
                {
                    // don't add storpooldfn without access
                }
            }
        }
        catch (AccessDeniedException accDeniedExc)
        {
            // for now return an empty list.
        }

        return clientComSerializer
            .answerBuilder(ApiConsts.API_LST_RSC_DFN, apiCallId)
            .resourceDfnList(rscdfns)
            .build();
    }

    private void requireRscDfnMapChangeAccess()
    {
        try
        {
            resourceDefinitionRepository.requireAccess(
                peerAccCtx.get(),
                AccessType.CHANGE
            );
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw new ApiAccessDeniedException(
                accDeniedExc,
                "change any resource definitions",
                ApiConsts.FAIL_ACC_DENIED_RSC_DFN
            );
        }
    }

    private ResourceDefinitionData createRscDfn(
        String rscNameStr,
        String transportTypeStr,
        Integer portInt,
        String secret
    )
    {
        TransportType transportType;
        if (transportTypeStr == null || transportTypeStr.trim().equals(""))
        {
            transportType = TransportType.IP;
        }
        else
        {
            try
            {
                transportType = TransportType.byValue(transportTypeStr); // TODO needs exception handling
            }
            catch (IllegalArgumentException unknownValueExc)
            {
                throw new ApiRcException(ApiCallRcImpl.simpleEntry(
                    ApiConsts.FAIL_INVLD_TRANSPORT_TYPE,
                    "The given transport type '" + transportTypeStr + "' is invalid."
                ), unknownValueExc);
            }
        }
        ResourceName rscName = LinstorParsingUtils.asRscName(rscNameStr);

        ResourceDefinitionData rscDfn;
        try
        {
            rscDfn = resourceDefinitionDataFactory.create(
                peerAccCtx.get(),
                rscName,
                portInt,
                null, // RscDfnFlags
                secret,
                transportType
            );
        }
        catch (ValueOutOfRangeException | ValueInUseException exc)
        {
            throw new ApiRcException(ApiCallRcImpl.simpleEntry(ApiConsts.FAIL_INVLD_RSC_PORT, String.format(
                "The specified TCP port '%d' is invalid.",
                portInt
            )), exc);
        }
        catch (ExhaustedPoolException exc)
        {
            throw new ApiRcException(ApiCallRcImpl.simpleEntry(
                ApiConsts.FAIL_POOL_EXHAUSTED_TCP_PORT,
                "Could not find free TCP port"
            ), exc);
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw new ApiAccessDeniedException(
                accDeniedExc,
                "create " + getRscDfnDescriptionInline(rscNameStr),
                ApiConsts.FAIL_ACC_DENIED_RSC_DFN
            );
        }
        catch (SQLException sqlExc)
        {
            throw new ApiSQLException(sqlExc);
        }
        catch (LinStorDataAlreadyExistsException exc)
        {
            throw new ApiRcException(ApiCallRcImpl.simpleEntry(
                ApiConsts.FAIL_EXISTS_RSC_DFN,
                firstLetterCaps(getRscDfnDescriptionInline(rscNameStr)) + " already exists."
            ), exc);
        }
        return rscDfn;
    }

    static VolumeNumber getVlmNr(VlmDfnApi vlmDfnApi, ResourceDefinition rscDfn, AccessContext accCtx)
        throws ValueOutOfRangeException, LinStorException
    {
        VolumeNumber vlmNr;
        Integer vlmNrRaw = vlmDfnApi.getVolumeNr();
        if (vlmNrRaw == null)
        {
            try
            {
                // Avoid using volume numbers that are already in use by active volumes or snapshots.
                // Re-using snapshot volume numbers would result in confusion when restoring from the snapshot.

                Set<SnapshotVolumeDefinition> snapshotVlmDfns = new HashSet<>();
                for (SnapshotDefinition snapshotDfn : rscDfn.getSnapshotDfns(accCtx))
                {
                    snapshotVlmDfns.addAll(snapshotDfn.getAllSnapshotVolumeDefinitions(accCtx));
                }

                int[] occupiedVlmNrs = Stream.concat(
                    rscDfn.streamVolumeDfn(accCtx).map(VolumeDefinition::getVolumeNumber),
                    snapshotVlmDfns.stream().map(SnapshotVolumeDefinition::getVolumeNumber)
                )
                    .mapToInt(VolumeNumber::getValue)
                    .sorted()
                    .distinct()
                    .toArray();

                vlmNr = VolumeNumberAlloc.getFreeVolumeNumber(occupiedVlmNrs);
            }
            catch (AccessDeniedException accDeniedExc)
            {
                throw new ImplementationError(
                    "ApiCtx does not have enough privileges to iterate vlmDfns",
                    accDeniedExc
                );
            }
            catch (ExhaustedPoolException exhausedPoolExc)
            {
                throw new LinStorException(
                    "No more free volume numbers left in range " + VolumeNumber.VOLUME_NR_MIN + " - " +
                    VolumeNumber.VOLUME_NR_MAX,
                    exhausedPoolExc
                );
            }
        }
        else
        {
            vlmNr = new VolumeNumber(vlmNrRaw);
        }
        return vlmNr;
    }

    public static String getRscDfnDescription(ResourceDefinition rscDfn)
    {
        return getRscDfnDescription(rscDfn.getName().displayValue);
    }

    public static String getRscDfnDescription(String rscName)
    {
        return "Resource definition: " + rscName;
    }

    public static String getRscDfnDescriptionInline(ResourceDefinition rscDfn)
    {
        return getRscDfnDescriptionInline(rscDfn.getName());
    }

    public static String getRscDfnDescriptionInline(ResourceName rscName)
    {
        return getRscDfnDescriptionInline(rscName.displayValue);
    }

    public static String getRscDfnDescriptionInline(String rscName)
    {
        return "resource definition '" + rscName + "'";
    }

    static ResponseContext makeResourceDefinitionContext(
        ApiOperation operation,
        String rscNameStr
    )
    {
        Map<String, String> objRefs = new TreeMap<>();
        objRefs.put(ApiConsts.KEY_RSC_DFN, rscNameStr);

        return new ResponseContext(
            operation,
            getRscDfnDescription(rscNameStr),
            getRscDfnDescriptionInline(rscNameStr),
            ApiConsts.MASK_RSC_DFN,
            objRefs
        );
    }
}
