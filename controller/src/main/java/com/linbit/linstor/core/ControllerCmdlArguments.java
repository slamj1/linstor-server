package com.linbit.linstor.core;

public class ControllerCmdlArguments extends LinStorCmdlArguments
{
    private String inMemoryDbType;
    private int inMemoryDbPort;
    private String inMemoryDbAddress;

    public ControllerCmdlArguments()
    {
        inMemoryDbType = null;
        inMemoryDbAddress = null;
        inMemoryDbPort = 0;
    }

    public void setInMemoryDbType(final String inMemoryDbTypeRef)
    {
        inMemoryDbType = inMemoryDbTypeRef;
    }

    public void setInMemoryDbPort(final int inMemoryDbPortRef)
    {
        inMemoryDbPort = inMemoryDbPortRef;
    }

    public void setInMemoryDbAddress(final String inMemoryDbAddressRef)
    {
        inMemoryDbAddress = inMemoryDbAddressRef;
    }

    public String getInMemoryDbType()
    {
        return inMemoryDbType;
    }

    public int getInMemoryDbPort()
    {
        return inMemoryDbPort;
    }

    public String getInMemoryDbAddress()
    {
        return inMemoryDbAddress;
    }
}
