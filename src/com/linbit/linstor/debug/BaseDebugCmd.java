package com.linbit.linstor.debug;

import com.linbit.AutoIndent;
import com.linbit.ErrorCheck;
import com.linbit.linstor.CommonDebugControl;
import com.linbit.linstor.CoreServices;
import com.linbit.linstor.LinStorException;
import com.linbit.linstor.core.LinStor;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Base class for debug console commands
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public abstract class BaseDebugCmd implements CommonDebugCmd
{
    public static final int SPRT_TEXT_LENGTH = 78;

    public static final char SPRT_TEXT_CHAR = '\u2550';
    public static final String PFX_SUB      = "\u251C\u2500";
    public static final String PFX_SUB_LAST = "\u2514\u2500";
    public static final String PFX_VLINE    = "\u2502 ";

    final Set<String> cmdNames;
    final String      cmdInfo;
    final String      cmdDescr;

    final Map<String, String> paramDescr;
    final String      undeclDescr;

    final boolean acceptsUndeclared = false;

    private boolean initialized;
    private String  sprtText;

    final Map<String, String> dspNameMap;

    LinStor             linStor;
    CoreServices        coreSvcs;
    CommonDebugControl  cmnDebugCtl;
    DebugConsole        debugCon;

    public BaseDebugCmd(
        String[]            cmdNamesRef,
        String              cmdInfoRef,
        String              cmdDescrRef,
        Map<String, String> paramDescrRef,
        String              undeclDescrRef,
        boolean             acceptsUndeclaredFlag
    )
    {
        ErrorCheck.ctorNotNull(this.getClass(), String[].class, cmdNamesRef);
        cmdNames    = new TreeSet<>();
        for (String name : cmdNamesRef)
        {
            ErrorCheck.ctorNotNull(this.getClass(), String.class, name);
            cmdNames.add(name);
        }
        dspNameMap  = new TreeMap<>();
        for (String name : cmdNames)
        {
            dspNameMap.put(name.toUpperCase(), name);
        }
        cmdInfo     = cmdInfoRef;
        cmdDescr    = cmdDescrRef;
        paramDescr  = paramDescrRef;
        undeclDescr = undeclDescrRef;
        initialized = false;
        coreSvcs    = null;
        sprtText    = null;
    }

    @Override
    public void commonInitialize(
        LinStor             linStorRef,
        CoreServices        coreSvcsRef,
        CommonDebugControl  cmnDebugCtlRef,
        DebugConsole        debugConRef
    )
    {
        linStor     = linStorRef;
        coreSvcs    = coreSvcsRef;
        cmnDebugCtl = cmnDebugCtlRef;
        debugCon    = debugConRef;
        initialized = true;
    }

    @Override
    public Set<String> getCmdNames()
    {
        Set<String> namesCpy = new TreeSet<>();
        namesCpy.addAll(cmdNames);
        return namesCpy;
    }

    @Override
    public String getDisplayName(String upperCaseCmdName)
    {
        return dspNameMap.get(upperCaseCmdName);
    }

    @Override
    public String getCmdInfo()
    {
        return cmdInfo;
    }

    @Override
    public String getCmdDescription()
    {
        return cmdDescr;
    }

    @Override
    public Map<String, String> getParametersDescription()
    {
        Map<String, String> paramCopy = null;
        if (paramDescr != null)
        {
            // Copy the map to prevent modification of the original map
            paramCopy = new TreeMap<>();
            for (Map.Entry<String, String> paramEntry : paramDescr.entrySet())
            {
                paramCopy.put(paramEntry.getKey(), paramEntry.getValue());
            }
        }
        return paramCopy;
    }

    @Override
    public String getUndeclaredParametersDescription()
    {
        return undeclDescr;
    }

    @Override
    public boolean acceptsUndeclaredParameters()
    {
        return acceptsUndeclared;
    }

    public void printMissingParamError(
        PrintStream debugErr,
        String paramName
    )
    {
        printError(
            debugErr,
            String.format(
                "The required parameter '%s' is not present.",
                paramName
            ),
            null,
            "Reenter the command including the required parameter.",
            null
        );
    }

    public void printMultiMissingParamError(
        PrintStream debugErr,
        Map<String, String> parameters,
        String... paramNameList
    )
    {
        Set<String> missingParams = new TreeSet<>();
        for (String paramName : paramNameList)
        {
            if (parameters.get(paramName) == null)
            {
                missingParams.add(paramName);
            }
        }
        String errorText = null;
        String correctionText = null;
        if (missingParams.size() == 1)
        {
            Iterator<String> paramIter = missingParams.iterator();
            errorText = String.format(
                "The required parameter '%s' is not present.",
                paramIter.next()
            );
            correctionText = "Reenter the command including the required parameter.";
        }
        else
        if (missingParams.size() > 0)
        {
            StringBuilder errorTextBld = new StringBuilder();
            errorTextBld.append("The following required parameters are not present:\n");
            for (String paramName : missingParams)
            {
                errorTextBld.append(String.format("    %s\n", paramName));
            }
            errorText = errorTextBld.toString();
            correctionText = "Reenter the command including the required parameters.";
        }
        if (errorText != null && correctionText != null)
        {
            printError(
                debugErr,
                errorText,
                null,
                correctionText,
                null
            );
        }
    }

    public void printDmException(PrintStream debugErr, LinStorException dmExc)
    {
        String descText = dmExc.getDescriptionText();
        if (descText == null)
        {
            descText = dmExc.getMessage();
            if (descText == null)
            {
                descText = "(Uncommented exception of type " +
                           dmExc.getClass().getCanonicalName() + ")";
            }
        }

        printError(
            debugErr,
            descText,
            dmExc.getCauseText(),
            dmExc.getCorrectionText(),
            dmExc.getDetailsText()
        );
    }

    public void printError(
        PrintStream debugErr,
        String errorText,
        String causeText,
        String correctionText,
        String errorDetailsText
    )
    {
        if (errorText != null)
        {
            debugErr.println("Error:");
            AutoIndent.printWithIndent(debugErr, 4, errorText);
        }
        if (causeText != null)
        {
            debugErr.println("Cause:");
            AutoIndent.printWithIndent(debugErr, 4, causeText);
        }
        if (correctionText != null)
        {
            debugErr.println("Correction:");
            AutoIndent.printWithIndent(debugErr, 4, correctionText);
        }
        if (errorDetailsText != null)
        {
            debugErr.println("Error details:");
            AutoIndent.printWithIndent(debugErr, 4, errorDetailsText);
        }
    }

    public void printSectionSeparator(PrintStream output)
    {
        if (sprtText == null)
        {
            char[] sprtTextData = new char[SPRT_TEXT_LENGTH];
            Arrays.fill(sprtTextData, SPRT_TEXT_CHAR);
            sprtText = new String(sprtTextData);
        }
        output.println(sprtText);
    }
}