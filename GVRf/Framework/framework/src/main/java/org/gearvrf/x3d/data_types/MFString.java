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
package org.gearvrf.x3d.data_types;

import org.gearvrf.utility.Log;
import java.util.ArrayList;

/**
 * Defines the X3D MFString data type
 */
public class MFString implements MField {

    private static final String TAG = MFString.class.getSimpleName();

    private ArrayList<String> value = new ArrayList<String>();

    public MFString() {
    }

    public void clear() { value.clear(); }

    public void remove(int index) { value.remove(index);}

    public int size() {
        return value.size();
    }

    public MFString(String[] newValue) {
        setValue(newValue.length, newValue);
    }

    public MFString(String newValue) {
        String[] newValues = new String[1];
        newValues[0] = newValue;
        setValue(1, newValues);
    }

    public void append(String newValue) {
        value.add( newValue );
    }

    public String get1Value(int index) {
        try {
            return value.get(index);
        }
        catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "X3D MFString get1Value(index) out of bounds." + e);
        }
        catch (Exception e) {
            Log.e(TAG, "X3D MFString get1Value(index) exception " + e);
        }
        return "";
    }

    public void getValue(String[] valueDestination) {
        if ( valueDestination.length < value.size()) {
          Log.e(TAG, "X3D MFString ArrayIndexOutOfBoundsException");
          Log.e(TAG, "array size " + valueDestination.length + " < MFString value.size = " + size() );
        }
        else {
            for (int i = 0; i < valueDestination.length; i++) {
                valueDestination[i] = get1Value(i);
            }
        }
    }

    public void insertValue(int index, String newValue) {
        try {
            value.add( index, newValue );
        }
        catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "X3D MFString insertValue(int index, ...) out of bounds." + e);
        }
        catch (Exception e) {
            Log.e(TAG, "X3D MFString insertValue(int index, ...)  exception " + e);
        }
    }

    public void set1Value(int index, String newValue) {
        try {
            value.set( index, newValue );
        }
        catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "X3D MFString set1Value(int index, ...) out of bounds." + e);
        }
        catch (Exception e) {
            Log.e(TAG, "X3D MFString set1Value(int index, ...)  exception " + e);
        }
    }

    public void setValue(int numStrings, String[] newValues) {
        value.clear();
        if (numStrings == newValues.length) {
            for (int i = 0; i < newValues.length; i++) {
                value.add(newValues[i]);
            }
        }
        else {
            Log.e(TAG, "X3D MFString setValue() numStrings not equal total newValues");
        }
    }

}


