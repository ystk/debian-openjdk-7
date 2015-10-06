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
package com.sun.javatest.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Date;

import com.sun.javatest.ProductInfo;
import com.sun.javatest.util.HTMLWriter;
import com.sun.javatest.util.I18NResourceBundle;

class ReportWriter extends HTMLWriter
{
    ReportWriter(Writer out) throws IOException {
        super(out);
    }

    ReportWriter(Writer out, I18NResourceBundle i18n) throws IOException {
        super(out, i18n);
        this.i18n = i18n;
    }

    /**
     * Creates a new ReportWriter.
     * @param out       the output stream
     */
    ReportWriter(Writer out, String title, I18NResourceBundle i18n) throws IOException {
        super(out, "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">", i18n);
        this.i18n = i18n;

        startTag(HTMLWriter.HTML);
        startTag(HTMLWriter.HEAD);
        startTag(HTMLWriter.TITLE);
        writeI18N("reportWriter.product.name", new Object[] {
            ProductInfo.getName(), title } );
        endTag(HTMLWriter.TITLE);
        writeStyle();
        endTag(HTMLWriter.HEAD);
        startTag(HTMLWriter.BODY);
        startTag(HTMLWriter.H1);
        writeI18N("reportWriter.product.name", new Object[] {
            ProductInfo.getName(), title });
        endTag(HTMLWriter.H1);
    }

    public void close() throws IOException {
        Date now = new Date();
        String name = ProductInfo.getName();
        String version = ProductInfo.getVersion();

        // for i18n
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        Date date = ProductInfo.getBuildDate();
        String build_date;
        if (date != null)
            build_date = df.format(date);
        else if (i18n != null)
            build_date = i18n.getString("reportWriter.noDate");
        else
            build_date = "unknown";     // last resort

        String build_version = ProductInfo.getBuildJavaVersion();

        startTag(HTMLWriter.P);
        startTag(HTMLWriter.HR);
        startTag(HTMLWriter.SMALL);
        writeI18N("reportWriter.generatedOn", now);
        startTag(HTMLWriter.BR);
        writeI18N("reportWriter.productInfo", new Object[] {
            name,  version,  build_date, build_version } );
        endTag(HTMLWriter.SMALL);
        endTag(HTMLWriter.BODY);
        endTag(HTMLWriter.HTML);

        super.flush();
        super.close();
    }

    public static void initializeDirectory(File dir) throws IOException {
        File cssFile = new File(dir, CSS_FILENAME);

        if (dir.exists() && dir.canWrite() && !cssFile.exists()) {
            FileWriter fw = new FileWriter(cssFile);

            fw.write("h1 {font-size: 18pt;\n");
            fw.write("      font-family: SansSerif;\n");
            fw.write("      bgcolor: white;\n");
            fw.write("      margin-left: 3}\n");

            fw.write("h2 {font-size: 15pt;\n");
            fw.write("      font-family: SansSerif;\n");
            fw.write("      bgcolor: white;\n");
            fw.write("      margin-left: 3}\n");

            fw.write("body , h3 {font-size: 12pt;\n"); //12
            fw.write("           font-family: SansSerif;\n");
            fw.write("           bgcolor: white;\n");
            fw.write("           margin-left: 3}\n");

            fw.close();
        }
    }

    void writeStyle() throws IOException {
        startTag(HTMLWriter.LINK);
        writeAttr(HTMLWriter.REL, "stylesheet");
        writeAttr(HTMLWriter.HREF, "report.css");
        writeAttr(HTMLWriter.TYPE, "text/css");
        endEmptyTag(HTMLWriter.LINK);
    }

    void writeStyleSheetProperty(String key, String value, boolean separator) throws IOException {
        if (separator)
            writeI18N("reportWriter.keyValue.separator",
                      new Object[] { key, value } );
        else
            writeI18N("reportWriter.keyValue",
                      new Object[] { key, value } );
        newLine();
    }

    void writeTD(String body) throws IOException {
        startTag(HTMLWriter.TD);
        write(body);
        endTag(HTMLWriter.TD);
    }

    void writeTH(String heading) throws IOException {
        writeTH(heading, null);
    }

    void writeTH(String heading, String scope) throws IOException {
        startTag(HTMLWriter.TH);
        if (scope != null) {
            writeAttr(HTMLWriter.SCOPE, scope);
        }
        write(heading);
        endTag(HTMLWriter.TH);
    }

    /**
     * Write a warning, writing the warning text in red.
     * @param text the warning text to be written
     * @throws IOException if there is a problem closing the underlying stream
     */
    void writeWarning(String text) throws IOException {
        startTag(FONT);
        writeAttr(COLOR, i18n.getString("reportWriter.warn.clr"));
        write(text);
        endTag(FONT);
    }

    private I18NResourceBundle i18n;
    private static final String BODY = "body";
    private static final String FONT_SIZE = "font-size";
    private static final String FONT_FAMILY = "font-family";
    private static final String BGCOLOR = "bgcolor";
    private static final String MARGIN_LEFT = "margin-left";
    private static final String SANSSERIF = "SansSerif";
    private static final String WHITE = "white";
    private static final String _12PT = "12pt";
    private static final String CSS_FILENAME="report.css";
}
