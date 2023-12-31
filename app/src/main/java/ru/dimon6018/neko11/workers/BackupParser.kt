/*
 * Copyright (C) 2023 Dmitry Frolkov <dimon6018t@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.dimon6018.neko11.workers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import ru.dimon6018.neko11.ui.activities.NekoSettingsActivity.Companion.showRestoreFailedDialog
import java.io.IOException
import java.io.InputStream

class BackupParser {
    private var mPrefsManual: SharedPreferences? = null
    fun parse(`in`: InputStream, context: Context) {
        mPrefsManual = context.getSharedPreferences(PrefState.FILE_NAME, Context.MODE_PRIVATE)
        try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(`in`, null)
            parser.nextTag()
            readPreferences(parser, context)
            `in`.close()
        } catch (e: XmlPullParserException) {
            showRestoreFailedDialog(context, e.toString())
        } catch (e: IOException) {
            showRestoreFailedDialog(context, e.toString())
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readPreferences(parser: XmlPullParser, context: Context) {
        parser.require(XmlPullParser.START_TAG, ns, "map")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) { continue }
            val node = parser.name
            when (node) {
                "string" -> readName(parser, context)
                "int" -> readValueInt(parser, context)
                else -> skip(parser)
            }
        }
    }

    private fun readName(parser: XmlPullParser, context: Context) {
        try {
            val editor = mPrefsManual!!.edit()
            parser.require(XmlPullParser.START_TAG, ns, "string")
            val valueKey = parser.getAttributeValue(null, "name")
            val name = readTextName(parser)
            parser.require(XmlPullParser.END_TAG, ns, "string")
            editor.putString(valueKey, name)
            editor.apply()
            Log.e("parserText", "key: $valueKey value: $name")
        } catch (exception: Exception) {
            showRestoreFailedDialog(context, exception.toString())
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTextName(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    private fun readValueInt(parser: XmlPullParser, context: Context) {
        try {
            parser.require(XmlPullParser.START_TAG, ns, "int")
            val valueData = parser.getAttributeValue(null, "value")
            val valueKey = parser.getAttributeValue(null, "name")
            parser.nextTag()
            Log.e("parserInt", "key: " + valueKey + "value: " + valueData)
            val editor = mPrefsManual!!.edit()
            editor.putInt(valueKey, valueData.toInt())
            editor.apply()
        } catch (exception: Exception) {
            showRestoreFailedDialog(context, exception.toString())
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        check(parser.eventType == XmlPullParser.START_TAG)
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
    companion object {
        private val ns: String? = null
    }
}
