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
package com.sun.javatest;

import com.sun.javatest.util.I18NResourceBundle;

/**
 * A test filter that filters out tests that appear in an {@link ExcludeList}.
 */
public class ExcludeListFilter extends TestFilter
{
    /**
     * Create a test filter that filters out tests that appear in an exclude list.
     * @param t The exclude list to be used to filter out tests.
     */
    public ExcludeListFilter(ExcludeList t) {
        table = t;
    }

    /**
     * Get the exclude list used to filter out tests for this filter.
     * @return The exclude list used to filter out tests for this filter.
     */
    public ExcludeList getExcludeList() {
        return table;
    }

    public String getName() {
        return i18n.getString("excludeFilter.name");
    }

    public String getDescription() {
        return i18n.getString("excludeFilter.description");
    }

    public String getReason() {
        return i18n.getString("excludeFilter.reason");
    }

    public boolean accepts(TestDescription td) {
        return !table.excludesAllOf(td);
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof ExcludeListFilter))
            return false;

        ExcludeListFilter other = (ExcludeListFilter) o;
        return table.equals(other.table);
    }

    private ExcludeList table;
    private static I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(ExcludeListFilter.class);
}
