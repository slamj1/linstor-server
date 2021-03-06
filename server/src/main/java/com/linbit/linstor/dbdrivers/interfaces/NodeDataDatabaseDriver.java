package com.linbit.linstor.dbdrivers.interfaces;

import java.sql.SQLException;

import com.linbit.SingleColumnDatabaseDriver;
import com.linbit.linstor.NodeData;
import com.linbit.linstor.Node.NodeFlag;
import com.linbit.linstor.Node.NodeType;
import com.linbit.linstor.stateflags.StateFlagsPersistence;

/**
 * Database driver for {@link NodeData}.
 *
 * @author Gabor Hernadi &lt;gabor.hernadi@linbit.com&gt;
 */
public interface NodeDataDatabaseDriver
{
    /**
     * Persists the given {@link NodeData} into the database.
     *
     * @param nodeData
     *  The data to be stored (including the primary key)
     * @throws SQLException
     */
    void create(NodeData nodeData) throws SQLException;

    /**
     * Removes the given {@link NodeData} from the database
     *
     * @param node
     *  The data identifying the database entry to delete
     * @throws SQLException
     */
    void delete(NodeData node) throws SQLException;

    /**
     * A special sub-driver to update the persisted {@link NodeFlag}s. The data record
     * is specified by the primary key stored as instance variables.
     */
    StateFlagsPersistence<NodeData> getStateFlagPersistence();

    /**
     * A special sub-driver to update the persisted {@link NodeType}. The data record
     * is specified by the primary key stored as instance variables.
     */
    SingleColumnDatabaseDriver<NodeData, NodeType> getNodeTypeDriver();
}
