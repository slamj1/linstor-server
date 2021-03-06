package com.linbit.linstor.core.apicallhandler.controller;

import com.linbit.ExhaustedPoolException;
import com.linbit.ValueInUseException;
import com.linbit.ValueOutOfRangeException;
import com.linbit.linstor.ResourceConnection;
import com.linbit.linstor.ResourceDefinition;
import com.linbit.linstor.TcpPortNumber;
import com.linbit.linstor.annotation.ApiContext;
import com.linbit.linstor.annotation.PeerContext;
import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.ApiCallRcImpl;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.core.CoreModule;
import com.linbit.linstor.core.apicallhandler.ScopeRunner;
import com.linbit.linstor.core.apicallhandler.response.ApiAccessDeniedException;
import com.linbit.linstor.core.apicallhandler.response.ApiOperation;
import com.linbit.linstor.core.apicallhandler.response.ApiRcException;
import com.linbit.linstor.core.apicallhandler.response.ApiSQLException;
import com.linbit.linstor.core.apicallhandler.response.OperationDescription;
import com.linbit.linstor.core.apicallhandler.response.ResponseContext;
import com.linbit.linstor.core.apicallhandler.response.ResponseConverter;
import com.linbit.linstor.core.apicallhandler.response.ResponseUtils;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.locks.LockGuard;
import reactor.core.publisher.Flux;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;

import static com.linbit.linstor.core.apicallhandler.controller.CtrlRscConnectionApiCallHandler.getResourceConnectionDescriptionInline;

@Singleton
public class CtrlDrbdProxyEnableApiCallHandler
{
    private final AccessContext apiCtx;
    private final ScopeRunner scopeRunner;
    private final CtrlTransactionHelper ctrlTransactionHelper;
    private final CtrlSatelliteUpdateCaller ctrlSatelliteUpdateCaller;
    private final CtrlApiDataLoader ctrlApiDataLoader;
    private final CtrlRscConnectionHelper ctrlRscConnectionHelper;
    private final ResponseConverter responseConverter;
    private final ReadWriteLock nodesMapLock;
    private final ReadWriteLock rscDfnMapLock;
    private final Provider<AccessContext> peerAccCtx;

    @Inject
    public CtrlDrbdProxyEnableApiCallHandler(
        @ApiContext AccessContext apiCtxRef,
        ScopeRunner scopeRunnerRef,
        CtrlTransactionHelper ctrlTransactionHelperRef,
        CtrlSatelliteUpdateCaller ctrlSatelliteUpdateCallerRef,
        CtrlApiDataLoader ctrlApiDataLoaderRef,
        CtrlRscConnectionHelper ctrlRscConnectionHelperRef,
        ResponseConverter responseConverterRef,
        @Named(CoreModule.NODES_MAP_LOCK) ReadWriteLock nodesMapLockRef,
        @Named(CoreModule.RSC_DFN_MAP_LOCK) ReadWriteLock rscDfnMapLockRef,
        @PeerContext Provider<AccessContext> peerAccCtxRef
    )
    {
        apiCtx = apiCtxRef;
        scopeRunner = scopeRunnerRef;
        ctrlTransactionHelper = ctrlTransactionHelperRef;
        ctrlSatelliteUpdateCaller = ctrlSatelliteUpdateCallerRef;
        ctrlApiDataLoader = ctrlApiDataLoaderRef;
        ctrlRscConnectionHelper = ctrlRscConnectionHelperRef;
        responseConverter = responseConverterRef;
        nodesMapLock = nodesMapLockRef;
        rscDfnMapLock = rscDfnMapLockRef;
        peerAccCtx = peerAccCtxRef;
    }

    public Flux<ApiCallRc> enableProxy(
        UUID rscConnUuid,
        String nodeName1,
        String nodeName2,
        String rscNameStr,
        Integer port
    )
    {
        ResponseContext context = makeDrbdProxyContext(
            new ApiOperation(ApiConsts.MASK_MOD,
                new OperationDescription("enabling", "enabling")),
            nodeName1,
            nodeName2,
            rscNameStr
        );

        return scopeRunner
            .fluxInTransactionalScope(
                LockGuard.createDeferred(
                    nodesMapLock.writeLock(),
                    rscDfnMapLock.writeLock()
                ),
                () -> enableProxyInTransaction(
                    rscConnUuid,
                    nodeName1,
                    nodeName2,
                    rscNameStr,
                    port
                )
            )
            .transform(responses -> responseConverter.reportingExceptions(context, responses));
    }

    private Flux<ApiCallRc> enableProxyInTransaction(
        UUID rscConnUuid,
        String nodeName1,
        String nodeName2,
        String rscNameStr,
        Integer port
    )
    {
        ApiCallRcImpl responses = new ApiCallRcImpl();

        ResourceDefinition rscDfn = ctrlApiDataLoader.loadRscDfn(rscNameStr, true);

        ResourceConnection rscConn =
            ctrlRscConnectionHelper.loadOrCreateRscConn(rscConnUuid, nodeName1, nodeName2, rscNameStr);

        if (port == null)
        {
            autoAllocateTcpPort(rscConn);
        }
        else
        {
            setTcpPort(port, rscConn);
        }

        enableLocalProxyFlag(rscConn);

        ctrlTransactionHelper.commit();

        Flux<ApiCallRc> satelliteUpdateResponses = ctrlSatelliteUpdateCaller.updateSatellites(rscDfn)
            .transform(updateResponses -> ResponseUtils.translateDeploymentSuccess(
                updateResponses,
                "Notified {0} of proxy connection for {1}"
            ));

        responses.addEntry(ApiCallRcImpl.simpleEntry(
            ApiConsts.MODIFIED,
            "DRBD Proxy enabled on " + getResourceConnectionDescriptionInline(apiCtx, rscConn)
        ));

        return Flux
            .<ApiCallRc>just(responses)
            .concatWith(satelliteUpdateResponses)
            .onErrorResume(CtrlSatelliteUpdateCaller.DelayedApiRcException.class, ignored -> Flux.empty());
    }

    private void enableLocalProxyFlag(ResourceConnection rscConn)
    {
        try
        {
            rscConn.getStateFlags().enableFlags(peerAccCtx.get(), ResourceConnection.RscConnFlags.LOCAL_DRBD_PROXY);
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw new ApiAccessDeniedException(
                accDeniedExc,
                "enable local proxy flag of " + getResourceConnectionDescriptionInline(apiCtx, rscConn),
                ApiConsts.FAIL_ACC_DENIED_RSC_CONN
            );
        }
        catch (SQLException exc)
        {
            throw new ApiSQLException(exc);
        }
    }

    private void setTcpPort(int port, ResourceConnection rscConn)
    {
        try
        {
            rscConn.setPort(peerAccCtx.get(), new TcpPortNumber(port));
        }
        catch (ValueOutOfRangeException | ValueInUseException exc)
        {
            throw new ApiRcException(ApiCallRcImpl.simpleEntry(ApiConsts.FAIL_INVLD_RSC_PORT, String.format(
                "The specified TCP port '%d' is invalid.",
                port
            )), exc);
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw new ApiAccessDeniedException(
                accDeniedExc,
                "set TCP port of " + getResourceConnectionDescriptionInline(apiCtx, rscConn),
                ApiConsts.FAIL_ACC_DENIED_RSC_CONN
            );
        }
        catch (SQLException exc)
        {
            throw new ApiSQLException(exc);
        }
    }

    private void autoAllocateTcpPort(ResourceConnection rscConn)
    {
        try
        {
            rscConn.autoAllocatePort(peerAccCtx.get());
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
                "auto-allocate port for " + getResourceConnectionDescriptionInline(apiCtx, rscConn),
                ApiConsts.FAIL_ACC_DENIED_RSC_DFN
            );
        }
        catch (SQLException exc)
        {
            throw new ApiSQLException(exc);
        }
    }

    static ResponseContext makeDrbdProxyContext(
        ApiOperation operation,
        String nodeName1Str,
        String nodeName2Str,
        String rscNameStr
    )
    {
        Map<String, String> objRefs = new TreeMap<>();
        objRefs.put(ApiConsts.KEY_1ST_NODE, nodeName1Str);
        objRefs.put(ApiConsts.KEY_2ND_NODE, nodeName2Str);
        objRefs.put(ApiConsts.KEY_RSC_DFN, rscNameStr);

        String objectDescription =
            "DRBD Proxy on " + getResourceConnectionDescriptionInline(nodeName1Str, nodeName2Str, rscNameStr);

        return new ResponseContext(
            operation,
            objectDescription,
            objectDescription,
            ApiConsts.MASK_RSC_CONN,
            objRefs
        );
    }
}
