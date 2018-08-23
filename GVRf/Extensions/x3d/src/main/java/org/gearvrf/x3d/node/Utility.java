/* Copyright 2016 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.x3d.node;

import org.gearvrf.GVRSceneObject;
import org.gearvrf.utility.Log;
import org.gearvrf.x3d.ScriptObject;
import org.gearvrf.x3d.X3Dobject;
import org.gearvrf.x3d.data_types.MFString;
import org.gearvrf.x3d.data_types.MFVec3f;
import org.gearvrf.x3d.data_types.SFBool;
import org.gearvrf.x3d.data_types.SFColor;
import org.gearvrf.x3d.data_types.SFFloat;
import org.gearvrf.x3d.data_types.SFInt32;
import org.gearvrf.x3d.data_types.SFRotation;
import org.gearvrf.x3d.data_types.SFString;
import org.gearvrf.x3d.data_types.SFTime;
import org.gearvrf.x3d.data_types.SFVec2f;
import org.gearvrf.x3d.data_types.SFVec3f;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Vector;

/**
 *
 */

public class Utility
{

    private static final String TAG = Utility.class.getSimpleName();

    X3Dobject mX3DObject;

    public Utility()
    {
    }

    public Utility(X3Dobject x3dObject)
    {
      mX3DObject = x3dObject;
    }


    protected float[] parseFixedLengthFloatString(String numberString,
                                                int componentCount, boolean constrained0to1, boolean zeroOrGreater) {
        StringReader sr = new StringReader(numberString);
        StreamTokenizer st = new StreamTokenizer(sr);
        st.parseNumbers();
        int tokenType;
        float componentFloat[] = new float[componentCount];
        try {
            for (int i = 0; i < componentCount; i++) {
                if ((tokenType = st.nextToken()) == StreamTokenizer.TT_NUMBER) {
                    componentFloat[i] = (float) st.nval;
                } else { // check for an exponent 'e'
                    if (tokenType == StreamTokenizer.TT_WORD) {
                        String word = st.sval;
                        if (word.startsWith("e-")) { // negative exponent
                            String exponentString = word.substring(2, word.length());
                            try {
                                --i; // with this exponent, we are still working with the
                                // previous number
                                Integer exponentInt = Integer.parseInt(exponentString);
                                componentFloat[i] *= (float) Math
                                        .pow(10, -exponentInt.intValue());
                            } catch (NumberFormatException e) {
                                Log.e(TAG,
                                        "parsing fixed length string, exponent number conversion error: "
                                                + exponentString);
                            }
                        } else if (word.equalsIgnoreCase("e")) { // exponent with plus sign
                            tokenType = st.nextToken();
                            if (tokenType == 43) { // "+" plus sign
                                if ((tokenType = st.nextToken()) == StreamTokenizer.TT_NUMBER) {
                                    --i; // with this exponent, we are still working with the
                                    // previous number
                                    float exponent = (float) st.nval;
                                    componentFloat[i] *= (float) Math.pow(10, exponent);
                                } else {
                                    st.pushBack();
                                    Log.e(TAG,
                                            "Error: exponent in X3D parser with fixed length float");
                                }
                            } else
                                st.pushBack();
                        } else
                            st.pushBack();
                    }
                } // end check for 'e' exponent
                if (constrained0to1) {
                    if (componentFloat[i] < 0)
                        componentFloat[i] = 0;
                    else if (componentFloat[i] > 1)
                        componentFloat[i] = 1;
                } else if (zeroOrGreater) {
                    if (componentFloat[i] < 0)
                        componentFloat[i] = 0;
                }
            } // end for-loop
        } // end 'try'
        catch (IOException e) {
            Log.d(TAG, "Error parsing fixed length float string: " + e);
        }
        return componentFloat;
    } // end parseFixedLengthFloatString

    protected float parseSingleFloatString(String numberString,
                                         boolean constrained0to1, boolean zeroOrGreater) {
        float[] value = parseFixedLengthFloatString(numberString, 1,
                constrained0to1,
                zeroOrGreater);
        return value[0];
    }  //  end parseSingleFloatString

    protected boolean parseBooleanString(String booleanString) {
        StringReader sr = new StringReader(booleanString);
        StreamTokenizer st = new StreamTokenizer(sr);
        boolean value = false;
        int tokenType;
        try {
            tokenType = st.nextToken();
            if (tokenType == StreamTokenizer.TT_WORD) {
                if (st.sval.equalsIgnoreCase("true"))
                    value = true;
            }
        } catch (IOException e) {
            Log.e(TAG, "Boolean Error: " + e);
            e.printStackTrace();
        }
        return value;
    }  //  end parseBooleanString

    protected int parseIntegerString(String numberString) {
        StringReader sr = new StringReader(numberString);
        StreamTokenizer st = new StreamTokenizer(sr);
        st.parseNumbers();
        int tokenType;
        int returnValue = 0;

        try {
            if ((tokenType = st.nextToken()) != StreamTokenizer.TT_EOF) {
                if (tokenType == StreamTokenizer.TT_NUMBER) {
                    returnValue = (int) st.nval;
                }
            }
        }
        catch (IOException e) {
            Log.e(TAG, "Error: parseIntegerString - " + e);
        }
        return returnValue;
    } // end parseIntegerString

    // multi-field string
    protected String[] parseMFString(String mfString) {
        Vector<String> strings = new Vector<String>();

        StringReader sr = new StringReader(mfString);
        StreamTokenizer st = new StreamTokenizer(sr);
        st.quoteChar('"');
        st.quoteChar('\'');
        String[] mfStrings = null;

        int tokenType;
        try {
            while ((tokenType = st.nextToken()) != StreamTokenizer.TT_EOF) {

                strings.add(st.sval);

            }
        } catch (IOException e) {

            Log.d(TAG, "String parsing Error: " + e);

            e.printStackTrace();
        }
        mfStrings = new String[strings.size()];
        for (int i = 0; i < strings.size(); i++) {
            mfStrings[i] = strings.get(i);
        }
        return mfStrings;
    } // end parseMFString




} // end Utility
