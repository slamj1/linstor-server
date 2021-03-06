package com.linbit.linstor.security;

import com.linbit.InvalidNameException;
import com.linbit.linstor.ControllerDatabase;
import com.linbit.linstor.InitializationException;
import com.linbit.linstor.annotation.SystemContext;
import com.linbit.linstor.logging.ErrorReporter;

import javax.inject.Inject;
import java.sql.SQLException;

public class DbSecurityInitializer
{
    private final ErrorReporter errorReporter;
    private final AccessContext initCtx;
    private final ControllerDatabase controllerDatabase;
    private final DbAccessor dbAccessor;

    @Inject
    public DbSecurityInitializer(
        ErrorReporter errorReporterRef,
        @SystemContext AccessContext initCtxRef,
        ControllerDatabase controllerDatabaseRef,
        DbAccessor dbAccessorRef
    )
    {

        errorReporter = errorReporterRef;
        initCtx = initCtxRef;
        controllerDatabase = controllerDatabaseRef;
        dbAccessor = dbAccessorRef;
    }

    public void initialize()
        throws InitializationException
    {
        // Load security identities, roles, domains/types, etc.
        errorReporter.logInfo("Loading security objects");
        try
        {
            Initializer.load(initCtx, controllerDatabase, dbAccessor);
        }
        catch (SQLException | InvalidNameException | AccessDeniedException exc)
        {
            throw new InitializationException("Failed to load security objects", exc);
        }

        errorReporter.logInfo(
            "Current security level is %s",
            SecurityLevel.get().name()
        );
    }
}
