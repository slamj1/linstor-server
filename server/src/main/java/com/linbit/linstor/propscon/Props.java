package com.linbit.linstor.propscon;

import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.transaction.TransactionObject;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Common interface for Containers that hold linstor property maps
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public interface Props extends TransactionObject, Iterable<Map.Entry<String, String>>
{
    String PATH_SEPARATOR = "/";

    String getProp(String key)
        throws InvalidKeyException;
    String getPropWithDefault(String key, String defaultValue) throws InvalidKeyException;
    String getProp(String key, String namespace)
        throws InvalidKeyException;
    String getPropWithDefault(String key, String namespace, String defaultValue) throws InvalidKeyException;

    String setProp(String key, String value)
        throws InvalidKeyException, InvalidValueException, AccessDeniedException, SQLException;
    String setProp(String key, String value, String namespace)
        throws InvalidKeyException, InvalidValueException, AccessDeniedException, SQLException;

    String removeProp(String key)
        throws InvalidKeyException, AccessDeniedException, SQLException;
    String removeProp(String key, String namespace)
        throws InvalidKeyException, AccessDeniedException, SQLException;

    void loadAll() throws SQLException, AccessDeniedException;

    void clear() throws AccessDeniedException, SQLException;

    void delete() throws AccessDeniedException, SQLException;

    int size();
    boolean isEmpty();

    String getPath();

    Map<String, String> map();
    Set<Map.Entry<String, String>> entrySet();
    Set<String> keySet();
    Collection<String> values();

    @Override
    Iterator<Map.Entry<String, String>> iterator();
    Iterator<String> keysIterator();
    Iterator<String> valuesIterator();

    Optional<Props> getNamespace(String namespace);
    Iterator<String> iterateNamespaces();
}
