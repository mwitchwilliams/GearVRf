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

import java.util.ArrayList;

/**
 *
 */

public class Proto
{

    private static final String TAG = Proto.class.getSimpleName();

    private enum data_types {
        MFString, MFVec3f, SFBool, SFColor, SFFloat, SFInt32, SFRotation,
        SFString, SFTime, SFVec2f, SFVec3f, SFNode }
    private enum proto_States {
        None, ProtoDeclare, ProtoInterface, ProtoBody, ProtoIS }
    private proto_States proto_State = proto_States.None;

    public class Field {
        private String mName = "";
        private ScriptObject.AccessType mAccessType = null;
        private data_types mType;
        private float[] mFloatValue;     // SFColor, SFFloat, SFRotation, SFvec2f, SFVec3f
        private boolean mBooleanValue;   // SFBool
        private String[] mStringValue;   // SFString and MFString
        private int mIntValue;           // SFInt32
        private long mLongValue;         // SFTime

        public Field(String name)
        {
            mName = name;
        }

    }

    private X3Dobject mX3Dobject;
    private Utility mUtility;
    private GVRSceneObject mGVRSceneObject = null;
    private String mName;
    private ScriptObject mScriptObject;
    private ArrayList<Field> mFieldObjects = new ArrayList<Field>();

    private Appearance mAppearance = null;


    public Proto(X3Dobject x3dObject)
    {
        mX3Dobject = x3dObject;
        mScriptObject = new ScriptObject();
        mUtility = new Utility();
        mAppearance = null;
    }

    public Proto(X3Dobject x3dObject, GVRSceneObject gvrSceneObject)
    {
        mGVRSceneObject = gvrSceneObject;
        mX3Dobject = x3dObject;
        mScriptObject = new ScriptObject();
        mUtility = new Utility();
        mAppearance = null;
    }

    public Proto(X3Dobject x3dObject, GVRSceneObject gvrSceneObject, String name)
    {
        mGVRSceneObject = gvrSceneObject;
        mName = name;
        mX3Dobject = x3dObject;
        mScriptObject = new ScriptObject();
        mUtility = new Utility();
        mAppearance = null;
    }

    public GVRSceneObject getMainGVRSceneObject() {
        return mGVRSceneObject;
    }

    public void setGVRSceneObject(GVRSceneObject gvrSceneObject) {
        mGVRSceneObject = gvrSceneObject;
    }

    public Appearance getAppearance() {
        return mAppearance;
    }

    public void createAppearance() {
        mAppearance = new Appearance();
    }

    public boolean isProtoStateNone() {
        if ( proto_State == proto_States.None) return true;
        else return false;
    }

    public void setProtoStateNone() {
        proto_State = proto_States.None;
    }

    public boolean isProtoStateProtoDeclare() {
        if ( proto_State == proto_States.ProtoDeclare) return true;
        else return false;
    }

    public void setProtoStateProtoDeclare() {
        proto_State = proto_States.ProtoDeclare;
    }

    public boolean isProtoStateProtoInterface() {
        if ( proto_State == proto_States.ProtoInterface) return true;
        else return false;
    }

    public void setProtoStateProtoInterface() {
        proto_State = proto_States.ProtoInterface;
    }

    public boolean isProtoStateProtoBody() {
        if ( proto_State == proto_States.ProtoBody) return true;
        else return false;
    }

    public void setProtoStateProtoBody() {
        proto_State = proto_States.ProtoBody;
    }

    public boolean isProtoStateProtoIS() {
        if ( proto_State == proto_States.ProtoIS) return true;
        else return false;
    }

    public void setProtoStateProtoIS() {
        proto_State = proto_States.ProtoIS;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public void AddField(ScriptObject.AccessType accessType, String name, String type, String value) {
        boolean error = false;
        Field field = new Field( name );
        field.mAccessType = accessType;
        if (type.equalsIgnoreCase("SFBool")) {
            field.mType = data_types.SFBool;
            if ( !value.isEmpty() ) {
                field.mBooleanValue = mUtility.parseBooleanString( value );
            }
        }
        else if  (type.equalsIgnoreCase("SFFloat")) {
            field.mType = data_types.SFFloat;
            if ( !value.isEmpty() ) {
                field.mFloatValue = new float[1];
                field.mFloatValue[0] = mUtility.parseSingleFloatString(value, false, false);
            }
        }
        else if  (type.equalsIgnoreCase("SFColor")) {
            field.mType = data_types.SFColor;
            if ( !value.isEmpty() ) {
                field.mFloatValue = new float[3];
                field.mFloatValue = mUtility.parseFixedLengthFloatString(value, 3, true, true);
            }
        }
        else if  (type.equalsIgnoreCase("SFVec3f")) {
            field.mType = data_types.SFVec3f;
            if ( !value.isEmpty() ) {
                field.mFloatValue = new float[3];
                field.mFloatValue = mUtility.parseFixedLengthFloatString(value, 3, false, false);
            }
        }
        else if  (type.equalsIgnoreCase("SFRotation")) {
            field.mType = data_types.SFRotation;
            if ( !value.isEmpty() ) {
                field.mFloatValue = new float[4];
                field.mFloatValue = mUtility.parseFixedLengthFloatString(value, 4, false, false);
            }
        }
        else if  (type.equalsIgnoreCase("SFVec2f")) {
            field.mType = data_types.SFVec2f;
            if ( !value.isEmpty() ) {
                field.mFloatValue = new float[2];
                field.mFloatValue = mUtility.parseFixedLengthFloatString(value, 2, false, false);
            }
        }
        else if  (type.equalsIgnoreCase("SFString")) {
            field.mType = data_types.SFString;
            if ( !value.isEmpty() ) {
                field.mStringValue = new String[1];
                field.mStringValue = mUtility.parseMFString(value);
            }
        }
        else if  (type.equalsIgnoreCase("SFTime")) {
            field.mType = data_types.SFTime;
            if ( !value.isEmpty() ) {
                field.mLongValue = (long) mUtility.parseSingleFloatString(value, false, true);
            }
        }
        else if  (type.equalsIgnoreCase("SFInt32")) {
            field.mType = data_types.SFInt32;
            if ( !value.isEmpty() ) {
                field.mIntValue = mUtility.parseIntegerString(value);
            }
        }
        else if  (type.equalsIgnoreCase("MFVec3f")) {
            field.mType = data_types.MFVec3f;
            if ( !value.isEmpty() ) {
                //TODO: not implemented in Utility.
            }
        }
        else if  (type.equalsIgnoreCase("MFString")) {
            field.mType = data_types.MFString;
            if ( !value.isEmpty() ) {
                String[] mfString = mUtility.parseMFString(value);
                field.mStringValue = new String[mfString.length];
                for (int i = 0; i < mfString.length; i++) {
                    field.mStringValue[i] = mfString[i];
                }
            }
        }
        else if  (type.equalsIgnoreCase("SFNode")) {
            field.mType = data_types.SFNode;
            Log.e("X3DDBG", "Proto, got SFNode");
            if ( !value.isEmpty() ) {
                //TODO: not implemented in Utility.
            }
        }
        else {
            Log.e(TAG, "Error, X3D Data Type  " + type + " not supported.");
            error = true;
        }
        if ( !error ) {
            mFieldObjects.add( field );
        }
    }  // end AddField

    public Field getField(String name) {
        for (int i = 0; i < mFieldObjects.size(); i++) {
            Field field = mFieldObjects.get(i);
            if (field.mName.equalsIgnoreCase(name)) {
                return field;
            }

        }
        return null;
    }

} // end Proto
