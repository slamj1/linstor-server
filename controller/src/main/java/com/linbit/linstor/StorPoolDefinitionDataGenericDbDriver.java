package com.linbit.linstor;

import com.linbit.ImplementationError;
import com.linbit.InvalidNameException;
import com.linbit.linstor.StorPoolDefinition.InitMaps;
import com.linbit.linstor.core.LinStor;
import com.linbit.linstor.dbdrivers.derby.DbConstants;
import com.linbit.linstor.dbdrivers.interfaces.StorPoolDefinitionDataDatabaseDriver;
import com.linbit.linstor.logging.ErrorReporter;
import com.linbit.linstor.propscon.PropsContainerFactory;
import com.linbit.linstor.security.ObjectProtection;
import com.linbit.linstor.security.ObjectProtectionDatabaseDriver;
import com.linbit.linstor.transaction.TransactionMgr;
import com.linbit.linstor.transaction.TransactionObjectFactory;
import com.linbit.utils.Pair;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Singleton
public class StorPoolDefinitionDataGenericDbDriver implements StorPoolDefinitionDataDatabaseDriver
{
    private static final String TBL_SPD = DbConstants.TBL_STOR_POOL_DEFINITIONS;

    private static final String SPD_UUID = DbConstants.UUID;
    private static final String SPD_NAME = DbConstants.POOL_NAME;
    private static final String SPD_DSP_NAME = DbConstants.POOL_DSP_NAME;

    private static final String SPD_SELECT =
        " SELECT " + SPD_UUID + ", " + SPD_NAME + ", " + SPD_DSP_NAME +
        " FROM " + TBL_SPD +
        " WHERE " + SPD_NAME + " = ?";
    private static final String SPD_SELECT_ALL =
        " SELECT " + SPD_UUID + ", " + SPD_NAME + ", " + SPD_DSP_NAME +
        " FROM " + TBL_SPD;
    private static final String SPD_INSERT =
        " INSERT INTO " + TBL_SPD +
        " (" + SPD_UUID + ", " + SPD_NAME + ", " + SPD_DSP_NAME + ")" +
        " VALUES (?, ?, ?)";
    private static final String SPD_DELETE =
        " DELETE FROM " + TBL_SPD +
        " WHERE " + SPD_NAME + " = ?";

    private final ErrorReporter errorReporter;

    private final ObjectProtectionDatabaseDriver objProtDriver;
    private final PropsContainerFactory propsContainerFactory;
    private final TransactionObjectFactory transObjFactory;
    private final Provider<TransactionMgr> transMgrProvider;

    private InitMaps disklessStorPoolDfnInitMaps;

    @Inject
    public StorPoolDefinitionDataGenericDbDriver(
        ErrorReporter errorReporterRef,
        ObjectProtectionDatabaseDriver objProtDriverRef,
        PropsContainerFactory propsContainerFactoryRef,
        TransactionObjectFactory transObjFactoryRef,
        Provider<TransactionMgr> transMgrProviderRef
    )
    {
        errorReporter = errorReporterRef;
        objProtDriver = objProtDriverRef;
        propsContainerFactory = propsContainerFactoryRef;
        transObjFactory = transObjFactoryRef;
        transMgrProvider = transMgrProviderRef;
    }

    @Override
    public StorPoolDefinitionData createDefaultDisklessStorPool() throws SQLException
    {
        if (disklessStorPoolDfnInitMaps != null)
        {
            throw new ImplementationError("Only one call to this method is allowed", null);
        }

        StorPoolDefinitionData disklessStorPoolDfn;
        try (PreparedStatement stmt = getConnection().prepareStatement(SPD_SELECT))
        {
            stmt.setString(1, LinStor.DISKLESS_STOR_POOL_NAME.toUpperCase());
            try (ResultSet resultSet = stmt.executeQuery())
            {
                if (resultSet.next())
                {
                    Pair<StorPoolDefinitionData, InitMaps> pair = restoreStorPoolDfn(resultSet);
                    disklessStorPoolDfn = pair.objA;
                    disklessStorPoolDfnInitMaps = pair.objB;
                }
                else
                {
                    StorPoolName storPoolName;
                    try
                    {
                        storPoolName = new StorPoolName(LinStor.DISKLESS_STOR_POOL_NAME);
                    }
                    catch (InvalidNameException exc)
                    {
                        throw new ImplementationError("Invalid hardcoded default diskless stor pool name", exc);
                    }
                    Map<NodeName, StorPool> storPoolsMap = new TreeMap<>();
                    disklessStorPoolDfnInitMaps = new StorPoolDfnInitMap(storPoolsMap);
                    disklessStorPoolDfn = new StorPoolDefinitionData(
                        UUID.randomUUID(),
                        getObjectProtection(storPoolName),
                        storPoolName,
                        this,
                        propsContainerFactory,
                        transObjFactory,
                        transMgrProvider,
                        storPoolsMap
                    );
                }
            }
        }
        return disklessStorPoolDfn;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void create(StorPoolDefinitionData storPoolDefinitionData) throws SQLException
    {
        errorReporter.logTrace("Creating StorPoolDefinition %s", getId(storPoolDefinitionData));

        try (PreparedStatement stmt = getConnection().prepareStatement(SPD_INSERT))
        {
            stmt.setString(1, storPoolDefinitionData.getUuid().toString());
            stmt.setString(2, storPoolDefinitionData.getName().value);
            stmt.setString(3, storPoolDefinitionData.getName().displayValue);
            stmt.executeUpdate();
        }
        errorReporter.logTrace("StorPoolDefinition created %s", getId(storPoolDefinitionData));
    }

    public Map<StorPoolDefinitionData, InitMaps> loadAll() throws SQLException
    {
        errorReporter.logTrace("Loading all StorPoolDefinitions");
        Map<StorPoolDefinitionData, InitMaps> storPoolMap = new TreeMap<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(SPD_SELECT_ALL))
        {
            try (ResultSet resultSet = stmt.executeQuery())
            {
                while (resultSet.next())
                {
                    Pair<StorPoolDefinitionData, InitMaps> pair = restoreStorPoolDfn(resultSet);
                    storPoolMap.put(pair.objA, pair.objB);
                }
            }
        }
        errorReporter.logTrace("Loaded %d StorPoolDefinitions", storPoolMap.size());
        return storPoolMap;
    }

    private Pair<StorPoolDefinitionData, InitMaps> restoreStorPoolDfn(ResultSet resultSet) throws SQLException
    {
        Pair<StorPoolDefinitionData, InitMaps> retPair = new Pair<>();

        StorPoolDefinitionData storPoolDefinition = null;
        StorPoolName storPoolName;
        try
        {
            storPoolName = new StorPoolName(resultSet.getString(SPD_DSP_NAME));
        }
        catch (InvalidNameException invalidNameExc)
        {
            throw new LinStorSqlRuntimeException(
                String.format(
                    "A display StorPoolName of a stored StorPoolDefinition in the table %s could not be restored. " +
                        "(invalid display StorPoolName=%s)",
                    TBL_SPD,
                    resultSet.getString(SPD_DSP_NAME)
                ),
                invalidNameExc
            );
        }

        UUID uuid = java.util.UUID.fromString(resultSet.getString(SPD_UUID));

        ObjectProtection objProt = getObjectProtection(storPoolName);

        Map<NodeName, StorPool> storPoolsMap = new TreeMap<>();
        storPoolDefinition = new StorPoolDefinitionData(
            uuid,
            objProt,
            storPoolName,
            this,
            propsContainerFactory,
            transObjFactory,
            transMgrProvider,
            storPoolsMap
        );

        retPair.objA = storPoolDefinition;
        retPair.objB = new StorPoolDfnInitMap(storPoolsMap);

        errorReporter.logTrace("StorPoolDefinition loaded from DB %s", getId(storPoolName));
        return retPair;
    }

    private ObjectProtection getObjectProtection(StorPoolName storPoolName)
        throws SQLException, ImplementationError
    {
        ObjectProtection objProt = objProtDriver.loadObjectProtection(
            ObjectProtection.buildPathSPD(storPoolName),
            false // no need to log a warning, as we would fail then anyways
        );
        if (objProt == null)
        {
            throw new ImplementationError(
                "StorPoolDefinition's DB entry exists, but is missing an entry in ObjProt table! " +
                getId(storPoolName), null
            );
        }
        return objProt;
    }

    @Override
    public void delete(StorPoolDefinitionData storPoolDefinitionData) throws SQLException
    {
        errorReporter.logTrace("Deleting StorPoolDefinition %s", getId(storPoolDefinitionData));
        try (PreparedStatement stmt = getConnection().prepareStatement(SPD_DELETE))
        {
            stmt.setString(1, storPoolDefinitionData.getName().value);
            stmt.executeUpdate();
        }
        errorReporter.logTrace("StorPoolDefinition deleted %s", getId(storPoolDefinitionData));
    }

    private Connection getConnection()
    {
        return transMgrProvider.get().getConnection();
    }

    private String getId(StorPoolDefinitionData storPoolDefinition)
    {
        return getId(storPoolDefinition.getName().displayValue);
    }

    private String getId(StorPoolName storPoolName)
    {
        return getId(storPoolName.displayValue);
    }

    private String getId(String name)
    {
        return " (StorPoolName=" + name + ")";
    }

    private class StorPoolDfnInitMap implements StorPoolDefinition.InitMaps
    {
        private Map<NodeName, StorPool> storPoolsMap;

        StorPoolDfnInitMap(Map<NodeName, StorPool> storPoolsMapRef)
        {
            storPoolsMap = storPoolsMapRef;
        }

        @Override
        public Map<NodeName, StorPool> getStorPoolMap()
        {
            return storPoolsMap;
        }
    }
}
