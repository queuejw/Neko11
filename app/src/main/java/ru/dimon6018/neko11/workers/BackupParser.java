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
package ru.dimon6018.neko11.workers;

import static android.content.Context.MODE_PRIVATE;
import static ru.dimon6018.neko11.workers.PrefState.FILE_NAME;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

public class BackupParser {
    private static final String ns = null;
    public SharedPreferences mPrefsManual;

    public void parse(InputStream in, Context context) throws XmlPullParserException, IOException {
        mPrefsManual = context.getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            readPreferences(parser);
            in.close();
        } catch (XmlPullParserException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readPreferences(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "map");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String node = parser.getName();
            switch (node) {
                case "string" -> readName(parser);
                case "int" -> readValueint(parser);
                default -> skip(parser);
            }
        }
    }
    private void readName(XmlPullParser parser) throws IOException, XmlPullParserException {
        SharedPreferences.Editor editor = mPrefsManual.edit();
        parser.require(XmlPullParser.START_TAG, ns, "string");
        String ValueKey = parser.getAttributeValue(null, "name");
        String name = readTextName(parser);
        parser.require(XmlPullParser.END_TAG, ns, "string");
        editor.putString(ValueKey, name);
        editor.apply();
        Log.e("parserText", "key: " + ValueKey + " value: " + name);
        //Set the Preference again after it is fetched;
    }
    private String readTextName(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void readValueint(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "int");
        String ValueData = parser.getAttributeValue(null, "value");
        String ValueKey = parser.getAttributeValue(null, "name");
        parser.nextTag();
        Log.e("parserInt", "key: " + ValueKey + "value: " + ValueData);
        SharedPreferences.Editor editor = mPrefsManual.edit();
        editor.putInt(ValueKey, Integer.parseInt(ValueData));
        editor.apply();
        //Set the Preference again after it is fetched
    }
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG -> depth--;
                case XmlPullParser.START_TAG -> depth++;
            }
        }
    }

}
