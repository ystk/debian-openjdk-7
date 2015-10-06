/*
 * Copyright 1998-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.javatest.regtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;

import com.sun.javatest.TestResult;
import com.sun.javatest.Status;

/**
 * Action is an abstract base class providing the ability to control the
 * behaviour of each step in a JDK test description.  This class requires that
 * all derived classes implement the <em>init</em> method (where arguements are
 * processed and other initializations occur) and the <em>run</em> method (where
 * the actual work for the action occurs.  In addition to these methods, the
 * Action abstract class contains a variety of protected methods for parsing and
 * logging.  All static strings used in Action implementations are also defined
 * here.
 *
 * @author Iris A Garcia
 */
public abstract class Action
{
    /**
     * The null constructor.
     */
    public Action() {
    } // Action()

    /**
     * This method does initial processing of the options and arguments for the
     * action.  Processing is determined by the requirements of run() which is
     * determined by the tag specification.
     *
     * @param opts The options for the action.
     * @param args The arguments for the actions.
     * @param reason Indication of why this action was invoked.
     * @param script The script.
     * @exception  ParseException If the options or arguments are not expected
     *             for the action or are improperly formated.
     */
    public abstract void init(String[][] opts, String[] args, String reason,
                              RegressionScript script)
        throws ParseException;

    /**
     * The method that does the work of the action.  The necessary work for the
     * given action is defined by the tag specification.
     *
     * @return     The result of the action.
     * @exception  TestRunException If an unexpected error occurs while running
     *             the test.
     */
    public abstract Status run() throws TestRunException;

    /**
     * Get any source files directly referenced by this action.
     **/
    public File[] getSourceFiles() {
        return null;
    }

   //------------------- parsing -----------------------------------------------

    /**
     * This method parses the <em>timeout</em> action option used by several
     * actions.  It verifies that the value of the timeout is a valid number.
     *
     * @param value The proposed value of the timeout.
     * @return     An integer representation of the passed value for the
     *             timeout scaled by the timeout factor.
     * @exception  ParseException If the string does not have a valid
     *             interpretation as a number.
     */
    protected int parseTimeout(String value) throws ParseException {
        if (value == null)
            throw new ParseException(PARSE_TIMEOUT_NONE);
        try {
            return script.getActionTimeout(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            throw new ParseException(PARSE_TIMEOUT_BAD_INT + value);
        }
    } // parseTimeout()

    /**
     * This method parses the <em>fail</em> action option used by several
     * actions.  It verifies that there is no associated value for the option.
     *
     * @param value The proposed value of the fail.
     * @return     True if there is no associated value.
     * @exception  ParseException If there is an associated value.
     */
    protected boolean parseFail(String value) throws ParseException {
        if (value != null)
            throw new ParseException(PARSE_FAIL_UEXPECT + value);
        return true;
    } // parseFail()

    //--------------------------------------------------------------------------

    /**
     * Add a grant entry to the policy file so that JavaTest can read
     * JTwork/classes.  The remaining entries in the policy file should remain
     * the same.
     *
     * @param fileName The absolute name of the original policy file.
     * @return     A string indicating the absolute name of the modified policy
     *             file.
     */
    protected String addGrantEntry(String fileName) throws TestRunException {
        File newPolicy = new File(script.absTestScratchDir(),
                                  (new File(fileName).getName()) + "_new");

        FileWriter fw;

        try {
            fw = new FileWriter(newPolicy);
            try {
                fw.write("// The following grant entries were added by JavaTest.  Do not edit." + LINESEP);
                fw.write("grant {" + LINESEP);
                fw.write("    permission java.io.FilePermission \""
                        + script.absTestClsTopDir().getPath().replace('\\' + FILESEP, "{/}")
                        + "${/}-\"" + ", \"read\";" + LINESEP);
                fw.write("};" + LINESEP);
                String[] javatestClassPath = StringArray.splitSeparator(PATHSEP, script.getJavaTestClassPath());
                for (int i = 0; i < javatestClassPath.length; i++) {
                    File f = new File(javatestClassPath[i]);
                    fw.write("grant codebase \"" + f.toURI().toURL() + "\" {" + LINESEP);
                    fw.write("    permission java.security.AllPermission;" + LINESEP);
                    fw.write("};" + LINESEP);
                }
                fw.write(LINESEP);

                fw.write("// original policy file:" + LINESEP);
                fw.write("// " + fileName + LINESEP);

                BufferedReader in = new BufferedReader(new FileReader(fileName));
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        fw.write(line + LINESEP);
                    }
                } finally {
                    in.close();
                }
                in.close();
            } finally {
                fw.close();
            }
        } catch (IOException e) {
            throw new TestRunException(POLICY_WRITE_PROB + newPolicy.toString());
        } catch (SecurityException e) {
            throw new TestRunException(POLICY_SM_PROB + newPolicy.toString());
        }

        return newPolicy.toString();
    } // addGrantEntry()

    /**
     * This method parses the <em>policy</em> action option used by several
     * actions.  It verifies that the indicated policy file exists in the
     * directory containing the defining file of the test.
     *
     * @param value The proposed filename for the policy file.
     * @return     A string indicating the absolute name of the policy file for
     *             the test.
     * @exception  ParseException If the passed filename is null, the empty
     *             string, or does not exist.
     */
    protected String parsePolicy(String value) throws ParseException {
        if ((value == null) || value.equals(""))
            throw new ParseException(MAIN_NO_POLICY_NAME);
        File policyFile = new File(script.absTestSrcDir(), value);
        if (!policyFile.exists())
            throw new ParseException(MAIN_CANT_FIND_POLICY + policyFile);
        return policyFile.toString();
    } // parsePolicy()

    /**
     * This method parses the <em>secure</em> action option used to provide the
     * name of a subclass to be installed as the security manager.  No
     * verification of the existence of the .class is done.
     *
     * @param value The proposed class name for the security manager.
     * @return    A string indicating the absolute name of the security manager
     *            class.
     * @exception ParseException If the passed classname is null, the empty
     *            string
     */
    protected String parseSecure(String value) throws ParseException {
        if ((value == null) || value.equals(""))
            throw new ParseException(MAIN_NO_SECURE_NAME);
        return value;
    } // parseSecure()

    //----------redirect streams------------------------------------------------

    // if we wanted to allow more concurrency, we could try and acquire a lock here
    Status redirectOutput(PrintStream out, PrintStream err) {
        synchronized(this) {
            SecurityManager sc = System.getSecurityManager();
            if (sc instanceof RegressionSecurityManager) {
                boolean prev = ((RegressionSecurityManager) sc).setAllowSetIO(true);
                System.setOut(out);
                System.setErr(err);
                ((RegressionSecurityManager) sc).setAllowSetIO(prev);
            } else {
                //return Status.error(MAIN_SECMGR_BAD);
            }
        }
        return Status.passed("OK");
    } // redirectOutput()

    //----------logging methods-------------------------------------------------

    /**
     * Return a recording area for the action.  The initial contents of the
     * default message area are set and will be of the form:
     * <pre>
     * command: action [command_args]
     * reason: [reason_string]
     * </pre>
     *
     * @param action The name of the action currently being processed.
     * @param args An array containing the action's arguments.
     * @param reason A reason string.
     * @return     The record area for the action.
     */
    protected TestResult.Section startAction(String action, String[] args, String reason) {
        startTime = (new Date()).getTime();
        return  startAction(action, StringArray.join(args, " "), reason);
    } // startAction()

    /**
     * Return a recording area for the action.  The initial contents of the
     * default message area are set and will be of the form:
     * <pre>
     * command: action [command_args]
     * reason: [reason_string]
     * </pre>
     *
     * @param action The name of the action currently being processed.
     * @param args The string containing the action's arguments.
     * @param reason A reason string.
     * @return     The record area for the action.
     */
    protected TestResult.Section startAction(String action, String args, String reason) {
        TestResult.Section section = script.getTestResult().createSection(action);
        PrintWriter pw = section.getMessageWriter();

        String basic = LOG_COMMAND + action + " " + args;
        pw.println(basic);
        pw.println(LOG_REASON + reason);

        startTime = (new Date()).getTime();
        return section;
    } // startAction()

    /**
     * Set the status for the passed action. After this call, the recording area
     * for the action become immutable.
     *
     * @param status The final status of the action.
     * @param section The record area for the action.
     */
    protected void endAction(Status status, TestResult.Section section) {
        long elapsedTime = (new Date()).getTime() - startTime;
        PrintWriter pw = section.getMessageWriter();
        pw.println(LOG_ELAPSED_TIME + ((double)elapsedTime/1000.0));
        section.setStatus(status);
    } // endAction()

    //----------workarounds-------------------------------------------------------

    /**
     * This method pushes the full, constructed command for the action to the
     * log.  The constructed command contains the the action and its arguments
     * modified to run in another process.  The command may also contain
     * additional things necessary to run the action according to spec.  This
     * may include things such as a modified classpath, absolute names of files,
     * and environment variables.
     *
     * Used primarily for debugging purposes.
     *
     * @param action The name of the action currently being processed.
     * @param cmdArgs An array of the command to pass to ProcessCommand.
     * @see com.sun.javatest.lib.ProcessCommand#run
     */
    protected void JTCmd(String action, String[] cmdArgs, TestResult.Section section) {
        PrintWriter pw = section.getMessageWriter();
        pw.println(LOG_JT_COMMAND + action);
        for (int i = 0; i < cmdArgs.length; i++)
            pw.print("'" + cmdArgs[i] + "' ");
        pw.println();
    } // JTCmd()

    //----------workarounds-------------------------------------------------------

    /**
     * Given a string, change "\\" into "\\\\" for windows platforms.  This method
     * must be called exactly once before the string is used to start a new
     * process.
     *
     * @param s    The string to translate.
     * @return     For Windows systems, a modified string.  For all other
     *             systems including i386 (win32 sparc and Linux), the same
     *             string.
     */
    String[] quoteBackslash(String[] s) {
        String bs = "\\";
        String[] retVal = new String[s.length];
        if (System.getProperty("file.separator").equals(bs)) {
            for (int i = 0; i < s.length; i++) {
                String victim = s[i];
                StringBuffer sb = new StringBuffer();
                for (int j = 0; j < victim.length(); j++) {
                    String c = String.valueOf(victim.charAt(j));
                    sb.append(c);
                    if (c.equals(bs))
                        sb.append(c);
                }
                retVal[i] = sb.toString();
            }
        } else
            retVal = s;

        return retVal;
    } // quoteBackslash()

    /**
     * Single quote the given string.  This method should be used if the string
     * contains characters which should not be interpreted by the shell.
     *
     * @param s    The string to translate.
     * @return     The same string, surrounded by "'".
     */
    String singleQuoteString(String s) {
        StringBuffer b = new StringBuffer();
        b.append("'").append(s).append("'");
        return(b.toString());
    } // singleQuoteString()

    //----------misc statics----------------------------------------------------

    protected static final String FILESEP  = System.getProperty("file.separator");
    protected static final String LINESEP  = System.getProperty("line.separator");
    protected static final String PATHSEP  = System.getProperty("path.separator");
    protected static final String JAVAHOME = System.getProperty("java.home");

    // This is a hack to deal with the fact that the implementation of
    // Runtime.exec() for Windows stringifies the arguments.
    protected static final String EXECQUOTE = (System.getProperty("os.name").startsWith("Windows") ? "\"" : "");

    protected static final String
        REASON_ASSUMED_ACTION = "ASSUMED_ACTION",
        REASON_USER_SPECIFIED = "USER_SPECIFIED",
        REASON_ASSUMED_BUILD  = "ASSUMED_BUILD",
        REASON_FILE_TOO_OLD   = "FILE_OUT_OF_DATE";

    protected static final String
        SREASON_ASSUMED_ACTION= "Assumed action based on file name: run ",
        SREASON_USER_SPECIFIED= "User specified action: run ",
        SREASON_ASSUMED_BUILD = "Named class compiled on demand",
        SREASON_FILE_TOO_OLD  = ".class file out of date or does not exist";

    // These are all of the error messages used in all actions.
    protected static final String
        PARSE_TIMEOUT_NONE    = "No timeout value",
        PARSE_TIMEOUT_BAD_INT = "Bad integer specification: ",
        PARSE_FAIL_UEXPECT    = "Unexpected value for `fail': ",

        // policy and security manager
        PARSE_BAD_OPT_JDK     = "Option not allowed using provided test JDK: ",
        PARSE_NO_POLICY_NAME  = "No policy file name",
        PARSE_CANT_FIND_POLICY= "Can't find policy file: ",
        PARSE_NO_SECURE_NAME  = "No security manager file name",
        PARSE_POLICY_OTHERVM  = "`/policy' requires use of `/othervm'",
        PARSE_SECURE_OTHERVM  = "`/secure' requires use of `/othervm'",
        PARSE_TIMEOUT_MANUAL  = "`/manual' disables use of `/timeout'",

        POLICY_WRITE_PROB     = "Problems writing new policy file: ",
        POLICY_SM_PROB        = "Unable to create new policy file: ",

        LOG_COMMAND           = "command: ",
        LOG_RESULT            = " result: ",
        LOG_JT_COMMAND        = "JavaTest command: ",
        LOG_REASON            = "reason: ",
        LOG_ELAPSED_TIME      = "elapsed time (seconds): ",
        //LOG_JDK               = "JDK under test: ",

        // COMMON
        // used in:  shell, main, applet
        EXEC_FAIL             = "Execution failed",
        EXEC_FAIL_EXPECT      = "Execution failed as expected",
        EXEC_PASS             = "Execution successful",
        EXEC_PASS_UNEXPECT    = "Execution passed unexpectedly",
        EXEC_ERROR_CLEANUP    = "Error while cleaning up threads after test",
        CHECK_PASS            = "Test description appears acceptable",

        // used in:  compile, main
        SAMEVM_CANT_RESET_SECMGR= "Cannot reset security manager",
        SAMEVM_CANT_RESET_PROPS = "Cannot reset system properties",

        // used in:compile, main
        AGENTVM_CANT_GET_VM      = "Cannot get VM for test",

        UNEXPECT_SYS_EXIT     = "Unexpected exit from test",
        CANT_FIND_SRC         = "Can't file source file: ",

        // applet
        APPLET_ONE_ARG_REQ    = "`applet' requires exactly one file argument",
        APPLET_BAD_VAL_MANUAL = "Bad value for `manual' option: ",
        APPLET_BAD_OPT        = "Bad option for applet: ",
        APPLET_CANT_FIND_HTML = "Can't find HTML file: ",
        APPLET_HTML_READ_PROB = "Problem reading HTML file: ",
        APPLET_MISS_ENDBODY   = "No </body> tag in ",
        APPLET_MISS_APPLET    = "No <applet> tag in ",
        APPLET_MISS_ENDAPPLET = "No </applet> tag in ",
        APPLET_MISS_REQ_ATTRIB= " missing required attribute ",
        APPLET_ARCHIVE_USUPP  = "`archive' not supported in file: ",
        APPLET_MISS_REQ_PARAM = "Missing required name or value for param in <param> tag",
        APPLET_CANT_WRITE_ARGS= "Can't write `applet' argument file",
        APPLET_SECMGR_FILEOPS = "Unable to create applet argument file",

        APPLET_USER_EVAL      = ", user evaluated",
        APPLET_MANUAL_TEST    = "Manual test",

        // build
        BUILD_UNEXPECT_OPT    = "Unexpected options for `build'",
        BUILD_NO_CLASSNAME    = "No classname(s) provided for `build'",
        BUILD_BAD_CLASSNAME   = "Bad classname provided for `build': ",
        BUILD_NO_COMP_NEED    = "No need to compile: ",
        BUILD_UP_TO_DATE      = "All files up to date",
        BUILD_SUCC            = "Build successful",
        BUILD_LIB_LIST        = " in directory-list: ",
        BUILD_FUTURE_SOURCE   = "WARNING: file %s has a modification time in the future: %s",
        BUILD_FUTURE_SOURCE_2 = "Unexpected results may occur",

        // clean
        CLEAN_SUCC            = "Clean successful",
        CLEAN_UNEXPECT_OPT    = "Unexpected option(s) for `clean'",
        CLEAN_NO_CLASSNAME    = "No classname(s) provided for `clean'",
        CLEAN_BAD_CLASSNAME   = "Bad classname provided for `clean': ",
        CLEAN_RM_FAILED       = "`clean' unable to delete file: ",
        CLEAN_SECMGR_PROB     = "Problem deleting directory contents: ",

        // compile
        COMPILE_NO_CLASSNAME  = "No classname provided for `compile'",
        COMPILE_NO_DOT_JAVA   = "No classname ending with `.java' found",
        COMPILE_BAD_OPT       = "Bad option for compile: ",
        COMPILE_OPT_DISALLOW  = "Compile option not allowed: ",
        COMPILE_NO_REF_NAME   = "No reference file name",
        COMPILE_CANT_FIND_REF = "Can't find reference file: ",
        COMPILE_GOLD_FAIL     = "Output does not match reference file: ",
        COMPILE_GOLD_LINE     = ", line ",
        COMPILE_GOLD_READ_PROB= "Problem reading reference file: ",

        COMPILE_PASS_UNEXPECT = "Compilation passed unexpectedly",
        COMPILE_PASS          = "Compilation successful",
        COMPILE_FAIL_EXPECT   = "Compilation failed as expected",
        COMPILE_FAIL          = "Compilation failed",
        COMPILE_CANT_RESET_SECMGR= "Cannot reset security manager",
        COMPILE_CANT_RESET_PROPS = "Cannot reset system properties",

        // ignore
        IGNORE_UNEXPECT_OPTS  = "Unexpected option(s) for `ignore'",
        IGNORE_TEST_IGNORED   = "Test ignored",
        IGNORE_TEST_IGNORED_C = "Test ignored: ",
        IGNORE_TEST_SUPPRESSED   = "@ignore suppressed by command line option",
        IGNORE_TEST_SUPPRESSED_C = "@ignore suppressed by command line option: ",

        // junit
        JUNIT_NO_DRIVER        = "No JUnit 4 driver (install junit.jar next to jtreg.jar)",
        JUNIT_NO_CLASSNAME     = "No class provided for `junit'",
        JUNIT_BAD_MAIN_ARG     = "Bad argument provided for class in `junit'",

        // main
        MAIN_NO_CLASSNAME     = "No class provided for `main'",
        MAIN_MANUAL_NO_VAL    = "Arguments to `manual' option not supported: ",
        MAIN_BAD_OPT          = "Bad option for main: ",
        MAIN_CANT_FIND_SECURE = "Can't find security manager file name: ",
        MAIN_BAD_OPT_JDK      = "Option not allowed using provided test JDK: ",
        MAIN_NO_POLICY_NAME   = "No policy file name",
        MAIN_CANT_FIND_POLICY = "Can't find policy file: ",
        MAIN_POLICY_OTHERVM   = "`/policy' requires use of `/othervm'",
        MAIN_NO_SECURE_NAME   = "No security manager file name",
        MAIN_SECURE_OTHERVM   = "`/secure' requires use of `/othervm'",
        MAIN_UNEXPECT_VMOPT   = ": vm option(s) found, need to specify /othervm",
        MAIN_POLICY_WRITE_PROB= "Problems writing new policy file: ",
        MAIN_POLICY_SM_PROB   = "Unable to create new policy file: ",
        MAIN_CANT_RESET_SECMGR= "Cannot reset security manager",
        MAIN_CANT_RESET_PROPS = "Cannot reset system properties",

        //    runOtherJVM
        MAIN_CANT_WRITE_ARGS  = "Can't write `main' argument file",
        MAIN_SECMGR_FILEOPS   = "Unable to create `main' argument file",

        //    runSameJVM
        MAIN_SECMGR_BAD       = "JavaTest not running its own security manager",
        MAIN_THREAD_INTR      = "Thread interrupted: ",
        MAIN_THREW_EXCEPT     = "`main' threw exception: ",
        MAIN_CANT_LOAD_TEST   = "Can't load test: ",
        MAIN_CANT_FIND_MAIN   = "Can't find `main' method",

        // shell
        SHELL_NO_SCRIPT_NAME  = "No script name provided for `shell'",
        SHELL_MANUAL_NO_VAL   = "Arguments to `manual' option not supported: ",
        SHELL_BAD_OPTION      = "Bad option for shell: ";

    //----------member variables------------------------------------------------

    private long   startTime;

    protected String reason;
    protected RegressionScript script;

    protected static final boolean showCmd = Boolean.getBoolean("javatest.regtest.showCmd");
}
