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
package com.sun.javatest.audit;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import com.sun.javatest.Status;
import com.sun.javatest.TestDescription;
import com.sun.javatest.TestResult;
import com.sun.javatest.tool.UIFactory;
import com.sun.javatest.util.HTMLWriter;

class SummaryPane extends AuditPane {
    SummaryPane(UIFactory uif) {
        super("smry", uif);

        htmlPane = new JEditorPane();
        htmlPane.setName("smry.html");
        htmlPane.setContentType("text/html");
        htmlPane.setEditable(false);
        htmlPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        uif.setAccessibleInfo(htmlPane, "smry.html");

        JScrollPane sp = uif.createScrollPane(htmlPane,
                                         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                         JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        setBody(sp);

        OK = uif.getI18NString("smry.state.OK");
        ERROR = uif.getI18NString("smry.state.error");
    }

    void show(Audit audit) {
        if (audit == currAudit)
            showBody();
        else {
            currAudit = audit;
            StringWriter sw = new StringWriter();
            try {
                out = new HTMLWriter(sw, uif.getI18NResourceBundle());
                writeReport();
                out.close();
            }
            catch (IOException e) {
                // can't happen, with StringWRiter
            }
            htmlPane.setText(sw.toString());
            showBody();
        }
    }

    private void writeReport() throws IOException {
        out.startTag(HTMLWriter.HTML);
        out.startTag(HTMLWriter.HEAD);
        //write("<title>" + ProductInfo.getName() + ": " + title + "</title>\n");
        out.endTag(HTMLWriter.HEAD);
        out.startTag(HTMLWriter.BODY);
        out.writeStyleAttr("font-family: SansSerif; font-size: 12pt");
        //write("<h1>" + ProductInfo.getName() + ": " + title + "</h1>");
        out.startTag(HTMLWriter.TABLE);
        out.startTag(HTMLWriter.TR);
        out.startTag(HTMLWriter.TH);
        out.writeAttr(HTMLWriter.ALIGN, HTMLWriter.RIGHT);
        out.writeI18N("smry.category");
        out.startTag(HTMLWriter.TH);
        out.writeAttr(HTMLWriter.ALIGN, HTMLWriter.LEFT);
        out.writeI18N("smry.state");
        out.startTag(HTMLWriter.TH);
        out.writeAttr(HTMLWriter.ALIGN, HTMLWriter.LEFT);
        out.writeI18N("smry.details");
        writeResultFileDetails();
        writeChecksumDetails();
        writeTestDescriptionDetails();
        writeTestCaseDetails();
        writeStatusDetails();
        writeDateStampDetails();
        //writeEnvDetails();
        out.endTag(HTMLWriter.TABLE);
        out.endTag(HTMLWriter.BODY);
        out.endTag(HTMLWriter.HTML);
    }

    private void writeResultFileDetails() throws IOException {
        out.startTag(HTMLWriter.TR);
        out.startTag(HTMLWriter.TD);
        out.writeAttr(HTMLWriter.ALIGN, HTMLWriter.RIGHT);
        out.writeAttr(HTMLWriter.VALIGN, HTMLWriter.TOP);
        out.startTag(HTMLWriter.B);
        out.writeI18N("smry.tr.head");
        out.endTag(HTMLWriter.B);
        out.startTag(HTMLWriter.TD);
        out.writeAttr(HTMLWriter.VALIGN, HTMLWriter.TOP);
        out.write(currAudit.isAllTestsOK() ? OK : ERROR);
        out.startTag(HTMLWriter.TD);

        TestDescription[] bad = currAudit.getBadTests();
        int count = (bad == null ? 0 : bad.length);
        if (count == 0)
            out.writeI18N("smry.tr.allOK");
        else
            out.writeI18N("smry.tr.count", new Integer(count));
    }

    private void writeChecksumDetails() throws IOException {
        out.startTag(HTMLWriter.TR);
        out.startTag(HTMLWriter.TD);
        out.writeAttr(HTMLWriter.ALIGN, HTMLWriter.RIGHT);
        out.writeAttr(HTMLWriter.VALIGN, HTMLWriter.TOP);
        out.startTag(HTMLWriter.B);
        out.writeI18N("smry.cs.head");
        out.endTag(HTMLWriter.B);
        out.startTag(HTMLWriter.TD);
        out.writeAttr(HTMLWriter.VALIGN, HTMLWriter.TOP);
        out.write(currAudit.isChecksumCountsOK() ? OK : ERROR);
        out.startTag(HTMLWriter.TD);

        int[] counts = currAudit.getChecksumCounts();
        int g = counts[TestResult.GOOD_CHECKSUM];
        int b = counts[TestResult.BAD_CHECKSUM];
        int n = counts[TestResult.NO_CHECKSUM];
        if (b == 0 && n == 0)
            out.writeI18N("smry.cs.allOK");
        else
            out.writeI18N("smry.cs.count",
                      new Object[] {
                          new Integer(g),
                          new Integer((g > 0) && (b + n > 0) ? 1 : 0),
                          new Integer(b),
                          new Integer((b > 0) && (n > 0) ? 1 : 0),
                          new Integer(n)
                              });

    }

    private void writeTestDescriptionDetails() throws IOException {
        out.startTag(HTMLWriter.TR);
        out.startTag(HTMLWriter.TD);
        out.writeAttr(HTMLWriter.ALIGN, HTMLWriter.RIGHT);
        out.writeAttr(HTMLWriter.VALIGN, HTMLWriter.TOP);
        out.startTag(HTMLWriter.B);
        out.writeI18N("smry.td.head");
        out.endTag(HTMLWriter.B);
        out.startTag(HTMLWriter.TD);
        out.writeAttr(HTMLWriter.VALIGN, HTMLWriter.TOP);
        out.write(currAudit.isAllTestDescriptionsOK() ? OK : ERROR);
        out.startTag(HTMLWriter.TD);

        TestResult[] bad = currAudit.getBadTestDescriptions();
        int count = (bad == null ? 0 : bad.length);
        if (count == 0)
            out.writeI18N("smry.td.allOK");
        else
            out.writeI18N("smry.td.count", new Integer(count));
    }

    private void writeTestCaseDetails() throws IOException {
        out.startTag(HTMLWriter.TR);
        out.startTag(HTMLWriter.TD);
        out.writeAttr(HTMLWriter.ALIGN, HTMLWriter.RIGHT);
        out.writeAttr(HTMLWriter.VALIGN, HTMLWriter.TOP);
        out.startTag(HTMLWriter.B);
        out.writeI18N("smry.tc.head");
        out.endTag(HTMLWriter.B);
        out.startTag(HTMLWriter.TD);
        out.writeAttr(HTMLWriter.VALIGN, HTMLWriter.TOP);
        out.write(currAudit.isAllTestsOK() ? OK : ERROR);
        out.startTag(HTMLWriter.TD);

        TestResult[] bad = currAudit.getBadTestCaseTests();
        int count = (bad == null ? 0 : bad.length);
        if (count == 0)
            out.writeI18N("smry.tc.allOK");
        else
            out.writeI18N("smry.tc.count", new Integer(count));
    }

    private void writeStatusDetails() throws IOException {
        out.startTag(HTMLWriter.TR);
        out.startTag(HTMLWriter.TD);
        out.writeAttr(HTMLWriter.ALIGN, HTMLWriter.RIGHT);
        out.writeAttr(HTMLWriter.VALIGN, HTMLWriter.TOP);
        out.startTag(HTMLWriter.B);
        out.writeI18N("smry.status.head");
        out.endTag(HTMLWriter.B);
        out.startTag(HTMLWriter.TD);
        out.writeAttr(HTMLWriter.VALIGN, HTMLWriter.TOP);
        out.write(currAudit.isStatusCountsOK() ? OK : ERROR);
        out.startTag(HTMLWriter.TD);

        int[] stats = currAudit.getStatusCounts();

        int p = stats[Status.PASSED];
        int f = stats[Status.FAILED];
        int e = stats[Status.ERROR];
        int nr = stats[Status.NOT_RUN];

        if (p + f + e + nr == 0)
            out.writeI18N("smry.status.noTests");
        else if (f + e + nr == 0)
            out.writeI18N("smry.status.allOK");
        else {
            out.writeI18N("smry.status.count",
                      new Object[] {
                          new Integer(p),
                          new Integer((p > 0) && (f + e + nr > 0) ? 1 : 0),
                          new Integer(f),
                          new Integer((f > 0) && (e + nr > 0) ? 1 : 0),
                          new Integer(e),
                          new Integer((e > 0) && (nr > 0) ? 1 : 0),
                          new Integer(nr)
                              });
        }
    }

    private void writeDateStampDetails() throws IOException {
        out.startTag(HTMLWriter.TR);
        out.startTag(HTMLWriter.TD);
        out.writeAttr(HTMLWriter.ALIGN, HTMLWriter.RIGHT);
        out.writeAttr(HTMLWriter.VALIGN, HTMLWriter.TOP);
        out.startTag(HTMLWriter.B);
        out.writeI18N("smry.dates.head");
        out.endTag(HTMLWriter.B);
        out.startTag(HTMLWriter.TD);
        out.writeAttr(HTMLWriter.VALIGN, HTMLWriter.TOP);
        out.write(currAudit.isDateStampsOK() ? OK : ERROR);
        out.startTag(HTMLWriter.TD);

        Date earliestStart = currAudit.getEarliestStartTime();
        Date latestStart = currAudit.getLatestStartTime();
        boolean badStarts = currAudit.hasBadStartTimes();

        if (earliestStart == null || latestStart == null) {
            out.writeI18N("smry.dates.noStamps");
        }
        else {
            Integer b = new Integer(badStarts ? 1 : 0);
            out.writeI18N("smry.dates.earliest",
                      new Object[] { earliestStart, b } );
            out.startTag("br");
            out.writeI18N("smry.dates.latest",
                      new Object[] { latestStart, b } );
            if (badStarts) {
                out.startTag("br");
                out.writeI18N("smry.dates.badDatesFound");
            }
        }
    }

    private void writeEnvDetails() throws IOException {
    }

    private JEditorPane htmlPane;
    private Audit currAudit;
    private HTMLWriter out;

    private String OK;
    private String ERROR;
}
