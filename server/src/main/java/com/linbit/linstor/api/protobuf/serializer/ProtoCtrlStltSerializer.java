package com.linbit.linstor.api.protobuf.serializer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.linbit.linstor.annotation.ApiContext;
import com.linbit.linstor.api.interfaces.serializer.CtrlStltSerializer;
import com.linbit.linstor.core.CtrlSecurityObjects;
import com.linbit.linstor.core.LinStor;
import com.linbit.linstor.logging.ErrorReporter;
import com.linbit.linstor.propscon.Props;
import com.linbit.linstor.security.AccessContext;

@Singleton
public class ProtoCtrlStltSerializer extends ProtoCommonSerializer
    implements CtrlStltSerializer
{
    private final CtrlSecurityObjects secObjs;
    private final Props ctrlConf;

    @Inject
    public ProtoCtrlStltSerializer(
        ErrorReporter errReporter,
        @ApiContext AccessContext serializerCtx,
        CtrlSecurityObjects secObjsRef,
        @Named(LinStor.SATELLITE_PROPS) Props ctrlConfRef)
    {
        super(errReporter, serializerCtx);
        secObjs = secObjsRef;
        ctrlConf = ctrlConfRef;
    }

    @Override
    public CtrlStltSerializerBuilder headerlessBuilder()
    {
        return builder(null, null, false);
    }

    @Override
    public CtrlStltSerializerBuilder onewayBuilder(String msgContent)
    {
        return builder(msgContent, null, false);
    }

    @Override
    public CtrlStltSerializerBuilder apiCallBuilder(String msgContent, Long apiCallId)
    {
        checkApiCallIdNotNull(apiCallId);
        return builder(msgContent, apiCallId, false);
    }

    @Override
    public CtrlStltSerializerBuilder answerBuilder(String msgContent, Long apiCallId)
    {
        checkApiCallIdNotNull(apiCallId);
        return builder(msgContent, apiCallId, true);
    }

    @Override
    public CtrlStltSerializerBuilder completionBuilder(Long apiCallId)
    {
        checkApiCallIdNotNull(apiCallId);
        return builder(null, apiCallId, false);
    }

    private CtrlStltSerializerBuilder builder(String msgContent, Long apiCallId, boolean isAnswer)
    {
        return new ProtoCtrlStltSerializerBuilder(
            errorReporter, serializerCtx, secObjs, ctrlConf, msgContent, apiCallId, isAnswer);
    }
}
