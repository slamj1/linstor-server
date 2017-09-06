package com.linbit.drbdmanage.security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

import com.linbit.ImplementationError;
import com.linbit.InvalidNameException;
import com.linbit.SingleColumnDatabaseDriver;
import com.linbit.drbdmanage.DrbdSqlRuntimeException;
import com.linbit.drbdmanage.dbdrivers.PrimaryKey;


public class ObjectProtectionDerbyDriver implements ObjectProtectionDatabaseDriver
{
    private static final String TBL_OP      = SecurityDbFields.TBL_OBJ_PROT;
    private static final String TBL_ACL     = SecurityDbFields.TBL_ACL_MAP;
    private static final String TBL_ROLES   = SecurityDbFields.TBL_ROLES;

    private static final String OP_OBJECT_PATH      = SecurityDbFields.OBJECT_PATH;
    private static final String OP_CREATOR          = SecurityDbFields.CRT_IDENTITY_NAME;
    private static final String OP_OWNER            = SecurityDbFields.OWNER_ROLE_NAME;
    private static final String OP_SEC_TYPE_NAME    = SecurityDbFields.SEC_TYPE_NAME;

    private static final String ROLE_NAME           = SecurityDbFields.ROLE_NAME;
    private static final String ROLE_PRIVILEGES     = SecurityDbFields.ROLE_PRIVILEGES;

    private static final String ACL_OBJECT_PATH = SecurityDbFields.OBJECT_PATH;
    private static final String ACL_ROLE_NAME   = SecurityDbFields.ROLE_NAME;
    private static final String ACL_ACCESS_TYPE = SecurityDbFields.ACCESS_TYPE;

    // ObjectProtection SQL statements
    private static final String OP_INSERT =
        " INSERT INTO " + TBL_OP +
        " VALUES (?, ?, ?, ?)";
    private static final String OP_UPDATE =
        " UPDATE " + TBL_OP +
        " SET " + OP_CREATOR       + " = ?, " +
        "     " + OP_OWNER         + " = ?, " +
        "     " + OP_SEC_TYPE_NAME + " = ? " +
        " WHERE " + OP_OBJECT_PATH + " = ?";
    private static final String OP_UPDATE_IDENTITY =
        " UPDATE " + TBL_OP +
        " SET " + OP_CREATOR +       " = ? " +
        " WHERE " + OP_OBJECT_PATH + " = ?";
    private static final String OP_UPDATE_ROLE =
        " UPDATE " + TBL_OP +
        " SET " + OP_OWNER +         " = ? " +
        " WHERE " + OP_OBJECT_PATH + " = ?";
    private static final String OP_UPDATE_SEC_TYPE =
        " UPDATE " + TBL_OP +
        " SET " + OP_SEC_TYPE_NAME + " = ? " +
        " WHERE " + OP_OBJECT_PATH + " = ?";
    private static final String OP_DELETE =
        " DELETE FROM " + TBL_OP +
        " WHERE " + OP_OBJECT_PATH + " = ?";
    private static final String OP_LOAD =
        " SELECT " +
        "     OP." + OP_CREATOR + ", " +
        "     OP." + OP_OWNER + ", " +
        "     OP." + OP_SEC_TYPE_NAME + ", " +
        "     ROLE." + ROLE_PRIVILEGES +
        " FROM " +
        "     " + TBL_OP + " AS OP " +
        "     LEFT JOIN " + TBL_ROLES + " AS ROLE ON OP." + OP_OWNER + " = ROLE." + ROLE_NAME +
        " WHERE " +
        "     OP." + OP_OBJECT_PATH + " = ?";




    private static final String ACL_INSERT =
        " INSERT INTO " + TBL_ACL +
        " VALUES (?, ?, ?)";
    private static final String ACL_UPDATE =
        " UPDATE " + TBL_ACL +
        " SET " + ACL_ACCESS_TYPE + " = ? " +
        " WHERE " + ACL_OBJECT_PATH + " = ? AND " +
        "       " + ACL_ROLE_NAME +   " = ?";
    private static final String ACL_DELETE =
        " DELETE FROM " + TBL_ACL +
        " WHERE " + ACL_OBJECT_PATH + " = ? AND " +
        "       " + ACL_ROLE_NAME + " = ?";
    private static final String ACL_LOAD =
        " SELECT " +
        "     ACL." + ACL_ROLE_NAME + ", " +
        "     ACL." + ACL_ACCESS_TYPE +
        " FROM " +
        "     " + TBL_ACL + " AS ACL " +
        " WHERE " + ACL_OBJECT_PATH + " = ?";

    private static Map<PrimaryKey, ObjectProtection> objProtCache = new Hashtable<>();

    private String objPath;
    private SingleColumnDatabaseDriver<Identity> identityDriver;
    private SingleColumnDatabaseDriver<Role> roleDriver;
    private SingleColumnDatabaseDriver<SecurityType> securityTypeDriver;
    private AccessContext dbCtx;

    public ObjectProtectionDerbyDriver(AccessContext accCtx, String objectPath)
    {
        dbCtx = accCtx;
        objPath = objectPath;
        identityDriver = new IdentityDerbyDriver();
        roleDriver = new RoleDerbyDriver();
        securityTypeDriver = new SecurityTypeDerbyDriver();
    }

    @Override
    public void insertOp(Connection con, ObjectProtection objProt) throws SQLException
    {
        PreparedStatement stmt = con.prepareStatement(OP_INSERT);

        stmt.setString(1, objPath);
        stmt.setString(2, objProt.getCreator().name.value);
        stmt.setString(3, objProt.getOwner().name.value);
        stmt.setString(4, objProt.getSecurityType().name.value);

        stmt.executeUpdate();
        stmt.close();
        cache(objProt, objPath);
    }

    @Override
    public void updateOp(Connection con, ObjectProtection objProt) throws SQLException
    {
        PreparedStatement stmt = con.prepareStatement(OP_UPDATE);

        stmt.setString(1, objProt.getCreator().name.value);
        stmt.setString(2, objProt.getOwner().name.value);
        stmt.setString(3, objProt.getSecurityType().name.value);
        stmt.setString(4, objPath);

        stmt.executeUpdate();
        stmt.close();
    }

    @Override
    public void deleteOp(Connection con) throws SQLException
    {
        PreparedStatement stmt = con.prepareStatement(OP_DELETE);

        stmt.setString(1, objPath);

        stmt.executeUpdate();
        stmt.close();
        cacheRemove(objPath);
    }

    @Override
    public void insertAcl(Connection con, Role role, AccessType grantedAccess) throws SQLException
    {
        PreparedStatement stmt = con.prepareStatement(ACL_INSERT);

        stmt.setString(1, objPath);
        stmt.setString(2, role.name.value);
        stmt.setLong(3, grantedAccess.getAccessMask());

        stmt.executeUpdate();
        stmt.close();

    }

    @Override
    public void updateAcl(Connection con, Role role, AccessType grantedAccess) throws SQLException
    {
        PreparedStatement stmt = con.prepareStatement(ACL_UPDATE);

        stmt.setLong(1, grantedAccess.getAccessMask());
        stmt.setString(2, objPath);
        stmt.setString(3, role.name.value);

        stmt.executeUpdate();
        stmt.close();
    }

    @Override
    public void deleteAcl(Connection con, Role role) throws SQLException
    {
        PreparedStatement stmt = con.prepareStatement(ACL_DELETE);

        stmt.setString(1, objPath);
        stmt.setString(2, role.name.value);

        stmt.executeUpdate();
        stmt.close();
    }

    @Override
    public ObjectProtection loadObjectProtection(Connection con) throws SQLException
    {
        PreparedStatement opLoadStmt = con.prepareStatement(OP_LOAD);

        opLoadStmt.setString(1, objPath);

        ResultSet opResultSet = opLoadStmt.executeQuery();

        ObjectProtection objProt = cacheGet(objPath);
        if (objProt == null)
        {
            if (opResultSet.next())
            {
                try
                {
                    Identity identity = Identity.get(new IdentityName(opResultSet.getString(1)));
                    Role role = Role.get(new RoleName(opResultSet.getString(2)));
                    SecurityType secType = SecurityType.get(new SecTypeName(opResultSet.getString(3)));
                    PrivilegeSet privLimitSet = new PrivilegeSet(opResultSet.getLong(4));
                    AccessContext accCtx = new AccessContext(identity, role, secType, privLimitSet);
                    objProt = new ObjectProtection(accCtx, this);
                }
                catch (InvalidNameException invalidNameExc)
                {
                    opResultSet.close();
                    opLoadStmt.close();
                    throw new DrbdSqlRuntimeException(
                        "A name has been modified in the database to an illegal string.",
                        invalidNameExc
                    );
                }
                opResultSet.close();
                opLoadStmt.close();
                // restore ACL
                PreparedStatement aclLoadStmt = con.prepareStatement(ACL_LOAD);
                aclLoadStmt.setString(1, objPath);
                ResultSet aclResultSet = aclLoadStmt.executeQuery();
                try
                {
                    if (cache(objProt, objPath))
                    {
                        while (aclResultSet.next())
                        {
                            Role role = Role.get(new RoleName(aclResultSet.getString(1)));
                            AccessType type = AccessType.get(aclResultSet.getInt(2));

                            objProt.addAclEntry(dbCtx, role, type);
                        }
                    }
                    else
                    {
                        objProt = cacheGet(objPath);
                    }
                    aclResultSet.close();
                    aclLoadStmt.close();
                }
                catch (InvalidNameException invalidNameExc)
                {
                    aclLoadStmt.close();
                    aclResultSet.close();
                    throw new DrbdSqlRuntimeException(
                        "A name has been modified in the database to an illegal string.",
                        invalidNameExc
                    );
                }
                catch (AccessDeniedException accessDeniedExc)
                {
                    aclLoadStmt.close();
                    aclResultSet.close();
                    throw new ImplementationError(
                        " Database's accessContext has insufficient rights to restore object protection",
                        accessDeniedExc
                    );
                }
            }
            else
            {
                if (!opResultSet.next())
                {
                    // XXX: user deleted db entry during runtime - throw exception?
                    // or just remove the item from the cache + detach item from parent (if needed) + warn the user?
                }
            }
        }
        opResultSet.close();
        opLoadStmt.close();

        return objProt;
    }

    @Override
    public SingleColumnDatabaseDriver<Identity> getIdentityDatabaseDrier()
    {
        return identityDriver;
    }

    @Override
    public SingleColumnDatabaseDriver<Role> getRoleDatabaseDriver()
    {
        return roleDriver;
    }

    @Override
    public SingleColumnDatabaseDriver<SecurityType> getSecurityTypeDriver()
    {
        return securityTypeDriver;
    }

    private synchronized static boolean cache(ObjectProtection objProt, String objPath)
    {
        PrimaryKey pk = new PrimaryKey(objPath);
        boolean contains = objProtCache.containsKey(pk);
        if (!contains)
        {
            objProtCache.put(pk, objProt);
        }
        return !contains;
    }

    private static ObjectProtection cacheGet(String objPath)
    {
        return objProtCache.get(new PrimaryKey(objPath));
    }

    private synchronized static void cacheRemove(String objPath)
    {
        objProtCache.remove(new PrimaryKey(objPath));
    }

    static synchronized void clearCache()
    {
        objProtCache.clear();
    }

    private class IdentityDerbyDriver implements SingleColumnDatabaseDriver<Identity>
    {
        @Override
        public void update(Connection con, Identity element) throws SQLException
        {
            PreparedStatement stmt = con.prepareStatement(OP_UPDATE_IDENTITY);

            stmt.setString(1, element.name.value);
            stmt.setString(2, objPath);

            stmt.executeUpdate();
            stmt.close();
        }
    }

    private class RoleDerbyDriver implements SingleColumnDatabaseDriver<Role>
    {
        @Override
        public void update(Connection con, Role element) throws SQLException
        {
            PreparedStatement stmt = con.prepareStatement(OP_UPDATE_ROLE);

            stmt.setString(1, element.name.value);
            stmt.setString(2, objPath);

            stmt.executeUpdate();
            stmt.close();
        }
    }

    private class SecurityTypeDerbyDriver implements SingleColumnDatabaseDriver<SecurityType>
    {
        @Override
        public void update(Connection con, SecurityType element) throws SQLException
        {
            PreparedStatement stmt = con.prepareStatement(OP_UPDATE_SEC_TYPE);

            stmt.setString(1, element.name.value);
            stmt.setString(2, objPath);

            stmt.executeUpdate();
            stmt.close();
        }
    }
}