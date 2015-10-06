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
package com.sun.javatest.logging;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.event.EventListenerList;

public class ObservedFile extends File {
    public ObservedFile(String name) {
        super(name);
        recordInexName = name + ".rec.index";
        loggersInexName = name + ".log.index";
    }

    synchronized public void addFileListener(FileListener listener) {
        list.add(FileListener.class, listener );
    }

    synchronized private FileListener[] getFileListeners() {
        return list.getListeners(FileListener.class);
    }

    synchronized public void removeFileListener(FileListener listener) {
        list.remove(FileListener.class, listener);
    }

    synchronized void fireFileEvent(FileEvent fileEvent) {
        FileListener[] fl = getFileListeners();
        for (FileListener aFl : fl) {
            aFl.fileModified(fileEvent);
        }
    }

    synchronized public boolean backup() {
        File to = new File(getAbsolutePath()+"~");
        File toRecInd = new File(getRecordInexName()+"~");
        File toLogInd = new File(getLoggersInexName()+"~");

        // need for windows:
        to.delete();
        toRecInd.delete();
        toLogInd.delete();

        renameTo(to);
        getRecordInexFile().renameTo(toRecInd);
        getLoggersInexFile().renameTo(toLogInd);

        boolean retval = !exists() && !getRecordInexFile().exists() && !getLoggersInexFile().exists();

        return retval;
    }

    private String getRecordInexName() {
        return recordInexName;
    }

    private String getLoggersInexName() {
        return loggersInexName;
    }

    File getRecordInexFile() {
        return new File(recordInexName);
    }

    File getLoggersInexFile() {
        return new File(loggersInexName);
    }

    synchronized void addToIndex(LogRecord record, long startOff, long endOff, String logName) {
        if (debug) System.out.println("OF - added record");
        int logNum = -1;
        String line = "";
        boolean found = false;
        RandomAccessFile logs = null, recs = null;
        try {
            logs = new RandomAccessFile(getLoggersInexFile(), "rw");
            while (line != null && logs.getFilePointer() < logs.length()) {
                line = logs.readUTF();
                logNum++;
                if (logName.equals(line)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                // add new logger
                logNum++;
                logs.writeUTF(logName);
            }
            logs.close();

            // recored index
            recs = new RandomAccessFile(getRecordInexFile(), "rw");
            recs.seek(recs.length());
            recs.writeInt(logNum);
            recs.writeLong(record.getMillis());
            recs.writeInt(record.getLevel().intValue());
            recs.writeLong(startOff);
            recs.writeLong(endOff);
            recs.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally{
            try {
                if (logs != null) logs.close();
                if (recs != null) recs.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    synchronized void readLoggers(ArrayList<String> loggers) {
        String line = "";
        try {
            RandomAccessFile logs = new RandomAccessFile(getLoggersInexFile(), "r");
            while (line != null && logs.getFilePointer() < logs.length()) {
                line = logs.readUTF();
                loggers.add(line);
            }
            logs.close();
        } catch (IOException e) {
            // it's ok
        }
    }

    synchronized void readRecords(ArrayList<LogModel.LiteLogRecord> records) {
        try {
            RandomAccessFile recs = new RandomAccessFile(getRecordInexFile(), "r");
            while (recs.getFilePointer() < recs.length()) {
                LogModel.LiteLogRecord r = new LogModel.LiteLogRecord();
                r.loggerID = recs.readInt();
                r.time = recs.readLong();
                r.severety = recs.readInt();
                r.startOff = recs.readLong();
                r.endOff = recs.readLong();

                // ajust level
                if (r.severety != Level.SEVERE.intValue()
                && r.severety != Level.WARNING.intValue()
                && r.severety != Level.INFO.intValue()) {
                    r.severety = Level.FINE.intValue();
                }

                records.add(r);
                if (debug) System.out.println("OF - read record ");
            }

            recs.close();
        } catch (IOException e) {
            // it's ok
        }

    }

    private EventListenerList list = new EventListenerList();
    private final static boolean debug = false;
    private String recordInexName;
    private String loggersInexName;

    public boolean needToBackup() {
        return length() != 0 || getLoggersInexFile().length() != 0 || getRecordInexFile().length() != 0;
    }
}
