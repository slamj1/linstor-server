package com.linbit.extproc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import com.linbit.ChildProcessTimeoutException;
import com.linbit.linstor.logging.ErrorReporter;
import com.linbit.timer.Action;
import com.linbit.timer.Timer;
import com.linbit.utils.StringUtils;

/**
 * Runs an external command, logs and saves its output
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public class ExtCmd extends ChildProcessHandler
{
    private static final Pattern SPACE_PATTERN = Pattern.compile(" ");

    private OutputReceiver  outReceiver;
    private OutputReceiver  errReceiver;
    private ErrorReporter   errLog;
    private long            startTime;

    private String[] command;
    private String commandStr;

    public ExtCmd(Timer<String, Action<String>> timer, ErrorReporter errLogRef)
    {
        super(timer);
        outReceiver = null;
        errReceiver = null;
        errLog = errLogRef;
    }

    public void asyncExec(String... command)
        throws IOException
    {
        exec(ProcessBuilder.Redirect.INHERIT, command);
    }

    public void pipeAsyncExec(ProcessBuilder.Redirect stdinRedirect, String... command)
        throws IOException
    {
        exec(ProcessBuilder.Redirect.PIPE, command);
    }

    public OutputData exec(String... command)
        throws IOException, ChildProcessTimeoutException
    {
        exec(ProcessBuilder.Redirect.INHERIT, command);
        return syncProcess();
    }

    public OutputData pipeExec(ProcessBuilder.Redirect stdinRedirect, String... command)
        throws IOException, ChildProcessTimeoutException
    {
        exec(stdinRedirect, command);
        return syncProcess();
    }

    public OutputStream exec(ProcessBuilder.Redirect stdinRedirect, String... command)
        throws IOException
    {
        this.command = command;
        commandStr = StringUtils.join(" ", command);

        errLog.logDebug("Executing command: %s", commandStr);

        ProcessBuilder pBuilder = new ProcessBuilder();
        pBuilder.command(command);
        pBuilder.redirectError(ProcessBuilder.Redirect.PIPE);
        pBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        pBuilder.redirectInput(stdinRedirect);
        Process child = pBuilder.start();
        startTime = System.currentTimeMillis();
        setChild(child);
        outReceiver = new OutputReceiver(child.getInputStream(), errLog);
        errReceiver = new OutputReceiver(child.getErrorStream(), errLog);
        new Thread(outReceiver).start();
        new Thread(errReceiver).start();

        return child.getOutputStream();
    }

    public OutputData syncProcess() throws IOException, ChildProcessTimeoutException
    {
        int exitCode = waitFor();
        outReceiver.finish();
        errReceiver.finish();
        OutputData outData = new OutputData(
            command,
            outReceiver.getData(),
            errReceiver.getData(),
            exitCode
        );

        errLog.logTrace(
            "External command finished in %dms: %s",
            (System.currentTimeMillis() - startTime),
            commandStr
        );

        return outData;
    }

    public static class OutputData
    {
        public final String[] executedCommand;
        public final byte[] stdoutData;
        public final byte[] stderrData;
        public final int exitCode;

        protected OutputData(String[] executeCmd, byte[] out, byte[] err, int retCode)
        {
            executedCommand = executeCmd;
            stdoutData = out;
            stderrData = err;
            exitCode = retCode;
        }

        public InputStream getStdoutStream()
        {
            return new ByteArrayInputStream(stdoutData);
        }

        public InputStream getStderrStream()
        {
            return new ByteArrayInputStream(stderrData);
        }
    }
}
