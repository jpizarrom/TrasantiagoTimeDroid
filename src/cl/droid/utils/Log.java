/**
 * Copyright (C) 2011 Luis Saavedra
 *
 * This file is part of contactos-cl.
 *
 * contactos-cl is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * contactos-cl is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with contactos-cl.  If not, see <http://www.gnu.org/licenses/>.
 */
package cl.droid.utils;

import android.util.Config;

public class Log
{
    private final static String LOGTAG = "contactos-cl";
    private static final boolean DEBUG = true;

    public static final boolean LOGD = DEBUG ? true : Config.DEBUG;
    public static final boolean LOGV = DEBUG ? Config.DEBUG : false;

    public static void d(String tag, String msg)
    {
        if(LOGV) android.util.Log.d(LOGTAG, tag + ": " + msg);
    }

    public static void d(String tag, String msg, Throwable tr)
    {
        if(LOGV) android.util.Log.d(LOGTAG, tag + ": " + msg, tr);
    }

    public static void e(String tag, String msg)
    {
        android.util.Log.e(LOGTAG, tag + ": " + msg);
    }

    public static void e(String tag, String msg, Throwable tr)
    {
        android.util.Log.e(LOGTAG, tag + ": " + msg, tr);
    }

    public static void i(String tag, String msg)
    {
        if(LOGD) android.util.Log.i(LOGTAG, tag + ": " + msg);
    }

    public static void i(String tag, String msg, Throwable tr)
    {
        if(LOGD) android.util.Log.i(LOGTAG, tag + ": " + msg, tr);
    }

    public static void v(String tag, String msg)
	{
		if(LOGD) android.util.Log.v(LOGTAG, tag + ": " + msg);
	}

    public static void v(String tag, String msg, Throwable tr)
    {
        if(LOGD) android.util.Log.v(LOGTAG, tag + ": " + msg, tr);
    }
}