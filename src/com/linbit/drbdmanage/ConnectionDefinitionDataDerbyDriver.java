package com.linbit.drbdmanage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.linbit.ImplementationError;
import com.linbit.InvalidNameException;
import com.linbit.SingleColumnDatabaseDriver;
import com.linbit.TransactionMgr;
import com.linbit.drbdmanage.core.DrbdManage;
import com.linbit.drbdmanage.dbdrivers.PrimaryKey;
import com.linbit.drbdmanage.dbdrivers.derby.DerbyConstants;
import com.linbit.drbdmanage.dbdrivers.interfaces.ConnectionDefinitionDataDatabaseDriver;
import com.linbit.drbdmanage.dbdrivers.interfaces.NodeDataDatabaseDriver;
import com.linbit.drbdmanage.dbdrivers.interfaces.ResourceDefinitionDataDatabaseDriver;
import com.linbit.drbdmanage.propscon.SerialGenerator;
import com.linbit.drbdmanage.security.AccessContext;
import com.linbit.drbdmanage.security.AccessDeniedException;
import com.linbit.drbdmanage.security.ObjectProtection;
import com.linbit.drbdmanage.security.ObjectProtectionDatabaseDriver;
import com.linbit.utils.UuidUtils;

public class ConnectionDefinitionDataDerbyDriver implements ConnectionDefinitionDataDatabaseDriver
{
    private static final String TBL_CON_DFN = DerbyConstants.TBL_CONNECTION_DEFINITIONS;

    private static final String CON_UUID = DerbyConstants.UUID;
    private static final String CON_RES_NAME = DerbyConstants.RESOURCE_NAME;
    private static final String CON_NODE_SRC = DerbyConstants.NODE_NAME_SRC;
    private static final String CON_NODE_DST = DerbyConstants.NODE_NAME_DST;
    private static final String CON_NR = DerbyConstants.CON_NR;

    private static final String CON_SELECT =
        " SELECT " + CON_UUID + ", " + CON_RES_NAME + ", " +
                     CON_NODE_SRC + ", " + CON_NODE_DST + ", " + CON_NR +
        " FROM " + TBL_CON_DFN +
        " WHERE "+ CON_RES_NAME + " = ? AND " +
                   CON_NODE_SRC + " = ? AND " +
                   CON_NODE_DST + " = ?";
    private static final String CON_SELECT_BY_RES_DFN =
        " SELECT " + CON_UUID + ", " + CON_RES_NAME + ", " +
                     CON_NODE_SRC + ", " + CON_NODE_DST + ", " + CON_NR +
        " FROM " + TBL_CON_DFN +
        " WHERE "+ CON_RES_NAME + " = ?";

    private static final String CON_INSERT =
        " INSERT INTO " + TBL_CON_DFN +
        " VALUES (?, ?, ?, ?, ?)";

    private static final String CON_DELETE =
        " DELETE FROM " + TBL_CON_DFN +
        " WHERE "+ CON_RES_NAME + " = ? AND " +
                   CON_NODE_SRC + " = ? AND " +
                   CON_NODE_DST + " = ?";

    private static final String CON_UPDATE_CON_NR =
        " UPDATE " + TBL_CON_DFN +
        " SET " + CON_NR + " = ?" +
        " WHERE " + CON_RES_NAME + " = ? AND " +
                    CON_NODE_SRC + " = ? AND " +
                    CON_NODE_DST + " = ?";

    private static Map<PrimaryKey, ConnectionDefinitionData> conDfnCache = new HashMap<>();

    private final AccessContext dbCtx;
    private final ResourceName resName;
    private final NodeName srcNodeName;
    private final NodeName dstNodeName;

    private final SingleColumnDatabaseDriver<Integer> conNrDriver;

    public ConnectionDefinitionDataDerbyDriver(
        AccessContext accCtx,
        ResourceName resNameRef,
        NodeName srcNodeNameRef,
        NodeName dstsrcNodeNameRef
    )
    {
        dbCtx = accCtx;
        resName = resNameRef;
        srcNodeName = srcNodeNameRef;
        dstNodeName = dstsrcNodeNameRef;

        conNrDriver = new ConnectionNumberDriver();
    }

    @Override
    public ConnectionDefinitionData load(
        SerialGenerator serialGen,
        TransactionMgr transMgr
    )
        throws SQLException
    {
        PreparedStatement stmt = transMgr.dbCon.prepareStatement(CON_SELECT);
        stmt.setString(1, resName.value);
        stmt.setString(2, srcNodeName.value);
        stmt.setString(3, dstNodeName.value);

        ResultSet resultSet = stmt.executeQuery();

        ConnectionDefinitionData ret = cacheGet(resName, srcNodeName, dstNodeName);
        if (ret == null)
        {
            if (resultSet.next())
            {
                ret = restoreConnectionDefinition(resultSet, serialGen, transMgr, dbCtx);
            }
        }
        else
        {
            if (!resultSet.next())
            {
                // XXX: user deleted db entry during runtime - throw exception?
                // or just remove the item from the cache + detach item from parent (if needed) + warn the user?
            }
        }

        resultSet.close();
        stmt.close();

        return ret;
    }

    private static ConnectionDefinitionData restoreConnectionDefinition(
        ResultSet resultSet,
        SerialGenerator serialGen,
        TransactionMgr transMgr,
        AccessContext accCtx
    )
        throws SQLException
    {
        UUID uuid = UuidUtils.asUuid(resultSet.getBytes(CON_UUID));
        ResourceName resName;
        NodeName srcNodeName;
        NodeName dstNodeName;
        int conNr = resultSet.getInt(CON_NR);

        try
        {
            resName = new ResourceName(resultSet.getString(CON_RES_NAME));
            srcNodeName = new NodeName(resultSet.getString(CON_NODE_SRC));
            dstNodeName = new NodeName(resultSet.getString(CON_NODE_DST));
        }
        catch (InvalidNameException invalidNameExc)
        {
            throw new DrbdSqlRuntimeException(
                "A resource or node name in the table " + TBL_CON_DFN +
                    " has been modified in the database to an illegal string.",
                invalidNameExc
            );
        }

        ObjectProtectionDatabaseDriver objProtDriver = DrbdManage.getObjectProtectionDatabaseDriver(
            ObjectProtection.buildPath(resName, srcNodeName, dstNodeName)
        );
        ObjectProtection objProt = objProtDriver.loadObjectProtection(transMgr.dbCon);

        ResourceDefinitionDataDatabaseDriver resDriver = DrbdManage.getResourceDefinitionDataDatabaseDriver(
            resName
        );
        ResourceDefinitionData resDfn = resDriver.load(serialGen, transMgr);

        NodeDataDatabaseDriver srcNodeDriver = DrbdManage.getNodeDataDatabaseDriver(srcNodeName);
        NodeData nodeSrc = srcNodeDriver.load(serialGen, transMgr);
        NodeDataDatabaseDriver dstNodeDriver = DrbdManage.getNodeDataDatabaseDriver(dstNodeName);
        NodeData nodeDst = dstNodeDriver.load(serialGen, transMgr);

        ConnectionDefinitionData conData = new ConnectionDefinitionData(uuid, objProt, resDfn, nodeSrc, nodeDst, conNr);
        cache(conData, accCtx);
        return conData;
    }

    public static List<ConnectionDefinition> loadAllConnectionsByResourceDefinition(
        ResourceName resName,
        SerialGenerator serialGen,
        TransactionMgr transMgr,
        AccessContext accCtx
    )
        throws SQLException
    {
        PreparedStatement stmt = transMgr.dbCon.prepareStatement(CON_SELECT_BY_RES_DFN);
        stmt.setString(1, resName.value);
        ResultSet resultSet = stmt.executeQuery();
        List<ConnectionDefinition> connections = new ArrayList<>();
        while (resultSet.next())
        {
            ConnectionDefinitionData conDfn = restoreConnectionDefinition(resultSet, serialGen, transMgr, accCtx);
            connections.add(conDfn);
        }
        resultSet.close();
        stmt.close();
        return connections;
    }

    @Override
    public void create(Connection con, ConnectionDefinitionData conDfnData) throws SQLException
    {
        try (PreparedStatement stmt = con.prepareStatement(CON_INSERT))
        {
            stmt.setBytes(1, UuidUtils.asByteArray(conDfnData.getUuid()));
            stmt.setString(2, resName.value);
            stmt.setString(3, srcNodeName.value);
            stmt.setString(4, dstNodeName.value);
            stmt.setInt(5, conDfnData.getConnectionNumber(dbCtx));

            stmt.executeUpdate();
            cache(conDfnData, dbCtx);
        }
        catch (AccessDeniedException accessDeniedExc)
        {
            throw new ImplementationError(
                "Database's access context has no permission to get storPoolDefinition",
                accessDeniedExc
            );
        }
    }

    @Override
    public void delete(Connection con) throws SQLException
    {
        try (PreparedStatement stmt = con.prepareStatement(CON_DELETE))
        {
            stmt.setString(1, resName.value);
            stmt.setString(2, srcNodeName.value);
            stmt.setString(3, dstNodeName.value);

            stmt.executeUpdate();
            cacheRemove(resName, srcNodeName, dstNodeName);
        }
    }

    @Override
    public SingleColumnDatabaseDriver<Integer> getConnectionNumberDriver()
    {
        return conNrDriver;
    }

    private static synchronized boolean cache(ConnectionDefinitionData con, AccessContext accCtx)
    {
        PrimaryKey pk = getPk(con, accCtx);
        boolean contains = conDfnCache.containsKey(pk);
        if (!contains)
        {
            conDfnCache.put(pk, con);
        }
        return !contains;
    }

    private static ConnectionDefinitionData cacheGet(
        ResourceName resName,
        NodeName srcNodeName,
        NodeName dstNodeName
    )
    {
        return conDfnCache.get(new PrimaryKey(resName.value, srcNodeName.value, dstNodeName.value));
    }

    /**
     * this method should only be called by tests or if you want a full-reload from the database
     */
    static synchronized void clearCache()
    {
        conDfnCache.clear();
    }

    private static synchronized void cacheRemove(
        ResourceName resName,
        NodeName srcNodeName,
        NodeName dstNodeName
    )
    {
        conDfnCache.remove(new PrimaryKey(resName.value, srcNodeName.value, dstNodeName.value));
    }

    private static PrimaryKey getPk(ConnectionDefinitionData con, AccessContext accCtx)
    {
        try
        {
            return new PrimaryKey(
                con.getResourceDefinition(accCtx).getName().value,
                con.getSourceNode(accCtx).getName().value,
                con.getTargetNode(accCtx).getName().value
            );
        }
        catch (AccessDeniedException accessDeniedExc)
        {
            throw new ImplementationError(
                "Database's access context has no permission to access ConnectionDefinitionData",
                accessDeniedExc
            );
        }
    }

    private class ConnectionNumberDriver implements SingleColumnDatabaseDriver<Integer>
    {
        @Override
        public void update(Connection con, Integer newConNr) throws SQLException
        {
            PreparedStatement stmt = con.prepareStatement(CON_UPDATE_CON_NR);
            stmt.setInt(1, newConNr);
            stmt.setString(2, resName.value);
            stmt.setString(3, srcNodeName.value);
            stmt.setString(4, dstNodeName.value);
            stmt.executeUpdate();
            stmt.close();
        }
    }
}