/*
 *  Copyright (c) 2017 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 *  associated documentation files (the "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 *  following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 *  LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 *  NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *  @author Dennis Lang  (Jan-2017)
 *  @see http://landenlabs.com
 *
 */

package com.landenlabs.all_log.alog;

import android.text.TextUtils;
import android.util.Log;

/**
 * Basic optimized  ALog API:
 * <ul>
 *     <li>tagMsg(String tag, String msg)</li>
 *     <li>tagMsg(String tag, String msg, Throwable tr)</li>
 *     <li>tagJoin(String tag, Object... args)</li>
 *     <li>tagFmt(String tag, String fmt, Object... args)</li>
 *     <li>tagCat(String tag, String separator, Object... args)</li>
 * </ul>
 * <p>
 * Slower methods will automatically generate TAG from stack trace.
 * Alternate logging API, which uses a cascaded API to control and delay presentation.
 * <ul>
 *     <li>tag(String tag)</li>
 *     <li>self()</li>
 *     <li>msg(String msg)</li>
 *     <li>msg(String msg, Throwable tr)</li>
 *     <li>fmt(String fmt, Object... args)</li>
 *     <li>cat(String separator, Object... args)</li>
 *     <li>ex(Throwable tr)</li>
 * </ul>
 * <p>
 * <b>Examples:</b>
 * <br><pre><font color="#006000">
 *    // Optimized methods, caller provides TAG.
 *    ALog.d.tagMsg(TAG, "log this message");
 *    ALog.d.tagMsg(TAG, "log this message with exception", ex);
 *    ALog.d.tagJoin(TAG, "Data", badData, " should be", goodData);
 *    ALog.d.tagFmt(TAG, "First:%s Last:%s", firstName, lastName);
 * </font><font color="#a06000">
 *    // Slower calls will generate TAG from stack trace.
 *    ALog.d.msg("log this message");
 *    ALog.d.msg("log this message with exception", ex);
 *    ALog.d.fmt("First:%s Last:%s", firstName, lastName);
 *</font>
 *    // Cascaded usage:
 *    ALog.e.tag("MyFooClass").fmt("First:%s Last:%s", mFirst, mLast);
 *
 *    // Redirect to file.
 *    ALog.i.out(ALogFileWriter.Default).tag("FooBar").cat(" ", item.name, item.desc, item.tag);

 * </pre>
 * @see AppLog
 * @author Dennis Lang
 *
 */

public enum ALog {

    // Logging levels (2=V, 3=D, 4=I, 5=W 6=E 7=A)

    // ==== Log levels to system log file.

    /** Verbose log priority level 2 */
    v(ALog.VERBOSE),
    /** Debug log priority level 3 */
    d(ALog.DEBUG),
    /** Info log priority level 4 */
    i(ALog.INFO),
    /** Warning log priority level 5 */
    w(ALog.WARN),
    /** Error log priority level 6 */
    e(ALog.ERROR),
    /** Assert log priority level 7 */
    a(ALog.ASSERT),

    /** Disabled log priority level 8 */
    none(ALog.ASSERT+1),

    // Log levels to private log file.
    fv(ALog.VERBOSE, ALogFileWriter.Default),
    fd(ALog.DEBUG, ALogFileWriter.Default),
    fi(ALog.INFO, ALogFileWriter.Default),
    fw(ALog.WARN, ALogFileWriter.Default),
    fe(ALog.ERROR, ALogFileWriter.Default),
    fa(ALog.ASSERT, ALogFileWriter.Default),
    ;

    /**
     * Log priorite levels.
     */
    public static final int VERBOSE = Log.VERBOSE;
    public static final int DEBUG = Log.DEBUG;
    public static final int INFO = Log.INFO;
    public static final int WARN = Log.WARN;
    public static final int ERROR = Log.ERROR;
    public static final int ASSERT = Log.ASSERT;
    public static final int NOLOGGING = Log.ASSERT+1;

    /**
     * Global  Minimum priority level to log, defaults to VERBOSE.
     */
    public static int minLevel = VERBOSE;

    private final int mLevel;
    private final ALogOut mOut = new ALogOut();
    private static ThreadLocal<String> mThreadTag = new ThreadLocal<String>();


    ALog(int level) {
        mLevel = level;
    }

    ALog(int level, ALogOut.LogPrinter logPrn) {
        mLevel = level;
        mOut.outPrn = logPrn;
    }

    /**
     * Replace default output log target with custom output log target.
     * <p>
     * Example:
     * <br><font color="green">
     *   ALog.i.out(ALogFileWriter.Default).tag("FooBar").cat(" ", "aaaa", "bbbbb", "ccccc");
     * </font>
     * @param logPrn Output print target
     * @return ALog chained instance
     */
    private ALog out(ALogOut.LogPrinter logPrn) {
        mOut.outPrn = logPrn;
        return this;
    }

    // =============================================================================================
    // Common API for logging messages.
    // =============================================================================================

    /**
     * If valid log level, Print tag and msg.
     *
     * @param tagStr Tag to print to log output target.
     * @param msgStr Message to print to log output target.
     */
    public ALog tagMsg(String tagStr, String msgStr) {
        if (mLevel >= minLevel) {
            tag(tagStr).msg(msgStr);
        }
        return this;
    }

    /**
     * If valid log level, Print tag with args joined together
     *
     * @param tagStr
     * @param args
     */
    public ALog tagJoin(String tagStr, Object... args) {
        if (mLevel >= minLevel) {
            tag(tagStr).cat("", args);
        }
        return this;
    }

    /**
     * If valid log level, Print tag and msg.
     *
     * @param tagStr Tag to print to log output target.
     * @param msgStr Message to print to log output target.
     * @param tr Trowable stack trace added to output target.
     */
    public ALog tagMsg(String tagStr, String msgStr, Throwable tr) {
        if (mLevel >= minLevel) {
            tag(tagStr).cat("\n", msgStr, Log.getStackTraceString(tr));
        }
        return this;
    }

    /**
     * If valid log level, format message and print.
     * <p>
     * Example:
     * <br> <font color="green">
     *   AppLog.LOG.d().tagFmt(tag, "First:%s Last:%s", firstName, lastName);
     * </font>
     *
     * @param tagStr Tag to print to log output target.
     * @param fmt  Format used by String.format to build message to print to log output target.
     * @param args Optional arguments passed to String.format(fmt, ....)
     */
    public ALog tagFmt(String tagStr, String fmt, Object... args) {
        if (mLevel >= minLevel) {
            String msgStr = String.format(fmt, args);
            println(mLevel, tagStr, msgStr);
        }
        return this;
    }

    /**
     * If valid log level, Concatenate strings with <b>separator</b>
     * <p>
     * Example:
     * <br><font color="green">
     *     AppLog.LOG.d().tagCat(tag, " to ", fromTag, toTag);
     *     AppLog.LOG.d().tagJoin(tag, fromTag, " to ", toTag);
     * <br>
     *     AppLog.LOG.d().tagCat(tag, ", ", firstName, middleName, lastName);
     * </font>
     *
     * @param tagStr Tag to print to log output target.
     * @param separator String place between argument values.
     * @param args One or more object to stringize.
     */
    public ALog tagCat(String tagStr, String separator, Object... args) {
        if (mLevel >= minLevel) {
            String msgStr = TextUtils.join(separator, args);
            println(mLevel, tagStr, msgStr);
        }
        return this;
    }


    /**
     * If valid log level, Log Throwable message and stacktrace.
     *
     * @param tr Throwable logged, message and stack.
     */
    public ALog tagTr(String tagStr, Throwable tr) {
        if (mLevel >= minLevel) {
            tagCat(tagStr, "\n", tr.getLocalizedMessage(), Log.getStackTraceString(tr));
        }
        return this;
    }

    // =============================================================================================
    // Tag manipulation
    // =============================================================================================

    /**
     * Set log tag, if not set or set with empty string, ALog will auto generate a log from stack trace.
     * @param tagStr Tag to use in subsequent log printing.
     * @return ALog chained instance
     *
     * @see #self()
     */
    public ALog tag(String tagStr) {
        if (mLevel >= minLevel) {
            // mTag = tagStr;                     // !!! This is not Thread Safe !!!!
            mThreadTag.set(tagStr);
        }
        return this;
    }

    /**
     * Set tag to automatically identify self (class which is calling ALog by stack inspection).
     * <br><font color="red">
     * Warning - Stack inspection is very slow.
     * </font>
     *
     * @return ALog chained instance
     * @see #tag(String)
     */
    public ALog self() {
        if (mLevel >= minLevel) {
            // mTag = null;                    // !!! This is not Thread Safe !!!!
            mThreadTag.set(null);
        }
        return this;
    }

    // =============================================================================================
    // Slow methods which automatically generate TAG from stack trace.
    // =============================================================================================

    /**
     * If valid log level, Print msg with any previously set tag.
     * <p><font color="#ff0000">
     * Warning - Slower then tagMsg(tag, msg) because Tag generated from stack.
     * </font><p>
     * @param msgStr  Message to print to log output target
     * @see #minLevel
     */
    public ALog msg(String msgStr) {
        if (mLevel >= minLevel) {
            println(mLevel, findTag(), msgStr);
        }
        return this;
    }

    /**
     * If valid log level, Print msg with Throwable and any previously set tag.
     * <p><font color="#ff0000">
     * Warning - Slower then tagMsg(tag, msg, tr) because Tag generated from stack.
     * </font><p>
     * @param msgStr Message to print to log output target
     * @param tr Throwable stack trace logged.
     */
    public ALog msg(String msgStr, Throwable tr) {
        if (mLevel >= minLevel) {
            cat("\n", msgStr, Log.getStackTraceString(tr));
        }
        return this;
    }

    /**
     * If valid log level, format message and print.
     * <p>
     * Example:
     * <br> <font color="green">
     *   AppLog.LOG.d().fmt("First:%s Last:%s", firstName, lastName);
     * </font>
     * <p><font color="#ff0000">
     * Warning - Slower then tagFmt(tag, fmt, ...) because Tag generated from stack.
     * </font><p>
     * @param fmt  Format used by String.format to build message to print to log output target.
     * @param args Optional arguments passed to String.format(fmt, ....)
     */
    public ALog fmt(String fmt, Object... args) {
        if (mLevel >= minLevel) {
            String msgStr = String.format(fmt, args);
            println(mLevel, findTag(), msgStr);
        }
        return this;
    }

    /**
     * If valid log level, Concatenate strings with <b>separator</b>
     * <p>
     * Example:
     * <br><font color="green">
     *     AppLog.LOG.d().cat(" to ", fromTag, toTag);
     * <br>
     *     AppLog.LOG.d().cat(", ", firstName, middleName, lastName);
     * </font>
     *
     * @param separator String place between argument values.
     * @param args One or more object to stringize.
     */
    public ALog cat(String separator, Object... args) {
        if (mLevel >= minLevel) {
            String msgStr = TextUtils.join(separator, args);
            println(mLevel, findTag(), msgStr);
        }
        return this;
    }

    /**
     * If valid log level, Log Throwable message and stacktrace.
     *
     * @param tr Throwable logged, message and stack.
     */
    public ALog tr(Throwable tr) {
        if (mLevel >= minLevel) {
            cat("\n", tr.getLocalizedMessage(), Log.getStackTraceString(tr));
        }
        return this;
    }

    // =============================================================================================
    // Utility methods.
    // =============================================================================================

    /**
     * Print level, tag and message to output target.
     *
     * @param level
     * @param tag
     * @param msg
     */
    private void println(int level, String tag, String msg) {
        mOut.outPrn.println(mLevel, tag, msg);
    }


    // Helper to make Log tag from stack, provide class and line number.
    private static final String NAME = ALog.class.getCanonicalName();

    /**
     * Make a Log tag by locating class calling ALog.
     *
     * @return  "filename:lineNumber"
     */
    private static String makeTag() {
        String tag = "";
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        for (int idx = 0; idx < ste.length; idx++) {
            StackTraceElement elem = ste[idx];
            if (elem.getMethodName().equals("makeTag") && elem.getClassName().equals(NAME)) {
                while (++idx < ste.length) {
                    elem = ste[idx];
                    if (!elem.getClassName().equals(NAME))
                        break;
                }
                tag = "("+elem.getFileName() + ":" + elem.getLineNumber()+")";
                return tag;
            }
        }
        return tag;
    }

    /**
     * Get previously set <b>tag</b> or generate a tag by inspecting the stacktrace.
     * @return User provided tag or "filename:lineNumber"
     */
    private String findTag() {
        String tag = mThreadTag.get();
        return (tag != null) ? tag : makeTag();
    }
}
