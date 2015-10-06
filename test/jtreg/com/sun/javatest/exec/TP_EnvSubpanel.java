/*
 * $Id$
 *
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
package com.sun.javatest.exec;

import java.awt.EventQueue;
import com.sun.javatest.TestResult;
import com.sun.javatest.tool.UIFactory;

/**
 * A subpanel of TestPanel that displays the test enviornment for a test result.
 */

class TP_EnvSubpanel
    extends TP_PropertySubpanel
{
    TP_EnvSubpanel(UIFactory uif) {
        super(uif, "env");
    }

    protected void updateSubpanel(TestResult currTest) {
        if (subpanelTest != null)
            subpanelTest.removeObserver(observer);

        super.updateSubpanel(currTest);
        updateEntries();

        // if it is mutable, track updates
        if (subpanelTest.isMutable())  {
            subpanelTest.addObserver(observer);
        }
    }

    private void updateEntries() {
        try {
            updateEntries(subpanelTest.getEnvironment());
        }
        catch (TestResult.Fault f) {
            // quietly ignore
        }
    }

    private void updateEntriesLater(final TestResult tr) {
        if (tr == subpanelTest) {
            if (EventQueue.isDispatchThread())
                updateEntries();
            else {
                EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            if (tr == subpanelTest) {
                                updateEntries();
                            }
                        }
                    });
            }
        }
    }

    private TRObserver observer = new TRObserver();

    //------------------------------------------------------------------------------------

    private class TRObserver
        implements TestResult.Observer
    {
        public void completed(TestResult tr) {
            //System.err.println("TPES_TRO: completed: " + tr.getWorkRelativePath());
            updateEntriesLater(tr);
            tr.removeObserver(this);
        }

        public void createdSection(TestResult tr, TestResult.Section section) {
            // ignore
        }

        public void completedSection(TestResult tr, TestResult.Section section) {
            // ignore
        }

        public void createdOutput(TestResult tr, TestResult.Section section,
                                  String outputName) {
            // ignore
        }

        public void completedOutput(TestResult tr, TestResult.Section section,
                                    String outputName) {
            // ignore
        }

        public void updatedOutput(TestResult tr, TestResult.Section section,
                                  String outputName,
                                  int start, int end, String text) {
            // ignore
        }

        public void updatedProperty(TestResult tr, String name, String value) {
            // ignore
        }
    }

}
