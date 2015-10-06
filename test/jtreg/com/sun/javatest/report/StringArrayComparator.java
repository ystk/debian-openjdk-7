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

import java.util.Comparator;

class StringArrayComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        String[] a = (String[])o1;
        String[] b = (String[])o2;
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            int c = compare(a[i], b[i]);
            if (c != 0)
                return c;
        }
        return (a.length < b.length ? -1 : a.length == b.length ? 0 : +1);
    }

    private static int compare(String a, String b) {
        if (a == null && b == null)
            return 0;

        if (a == null)
            return -1;

        if (b == null)
            return +1;

        return a.compareTo(b);
    }
}
