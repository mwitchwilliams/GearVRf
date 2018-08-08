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
import org.gearvrf.GVRTransform;
import org.gearvrf.utility.Log;
import org.gearvrf.x3d.DefinedItem;
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
import org.xml.sax.Attributes;

import static org.gearvrf.x3d.X3Dobject.TRANSFORM_CENTER_;
import static org.gearvrf.x3d.X3Dobject.TRANSFORM_NEGATIVE_CENTER_;
import static org.gearvrf.x3d.X3Dobject.TRANSFORM_NEGATIVE_SCALE_ORIENTATION_;
import static org.gearvrf.x3d.X3Dobject.TRANSFORM_ROTATION_;
import static org.gearvrf.x3d.X3Dobject.TRANSFORM_SCALE_;
import static org.gearvrf.x3d.X3Dobject.TRANSFORM_TRANSLATION_;

/**
 *
 */

public class Transform
{

    private static final String TAG = Transform.class.getSimpleName();

    X3Dobject mX3DObject;
    Utility utility = new Utility();


    public Transform(GVRSceneObject currentSceneObject, Attributes attributes)
    {
                String attributeValue = attributes.getValue("USE");
                if (attributeValue != null) {
                    //ReplicateGVRSceneObjStructure(attributeValue);
                } // end USE Transform
                else {
                    // Not a 'Transform USE="..." node
                    // so initialize with default values

                    String name = "";
                    float[] center =
                            {
                                    0, 0, 0
                            };
                    float[] rotation =
                            {
                                    0, 0, 1, 0
                            };
                    float[] scaleOrientation =
                            {
                                    0, 0, 1, 0
                            };
                    float[] scale =
                            {
                                    1, 1, 1
                            };
                    float[] translation =
                            {
                                    0, 0, 0
                            };

                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    // Order for Transformations:
                    // P' = T * C * R * SR * S * -SR * -C * P
                    // T=Translation, C=Center, R=Rotation, SR=ScaleOrientation, S=Scale,
                    // and P will the Point
                    // Parsing Center value must occur before Rotation
                    String translationAttribute = attributes.getValue("translation");
                    if (translationAttribute != null) {
                        translation = utility.parseFixedLengthFloatString(translationAttribute, 3,
                                false, false);
                    }
                    String centerAttribute = attributes.getValue("center");
                    if (centerAttribute != null) {
                        center = utility.parseFixedLengthFloatString(centerAttribute, 3, false,
                                false);
                    }
                    String rotationAttribute = attributes.getValue("rotation");
                    if (rotationAttribute != null) {
                        rotation = utility.parseFixedLengthFloatString(rotationAttribute, 4, false,
                                false);
                    }
                    String scaleOrientationAttribute = attributes
                            .getValue("scaleOrientation");
                    if (scaleOrientationAttribute != null) {
                        scaleOrientation = utility.parseFixedLengthFloatString(scaleOrientationAttribute,
                                4, false, false);
                    }
                    attributeValue = attributes.getValue("scale");
                    if (attributeValue != null) {
                        scale = utility.parseFixedLengthFloatString(attributeValue, 3, false, false);
                    }

                    currentSceneObject = AddGVRSceneObject();
                    if (name.isEmpty()) {
                        // There is no DEF, thus no animation or interactivity applied to
                        // this Transform.
                        // Therefore, just set the values in a single GVRSceneObject
                        GVRTransform transform = currentSceneObject.getTransform();
                        transform.setPosition(translation[0], translation[1],
                                translation[2]);
                        transform.rotateByAxisWithPivot((float) Math.toDegrees(rotation[3]),
                                rotation[0], rotation[1],
                                rotation[2], center[0], center[1],
                                center[2]);
                        transform.setScale(scale[0], scale[1], scale[2]);
                    } else {

                        // There is a 'DEF="...."' parameter so save GVRSceneObject
                        // to the DefinedItem's array list in case it's referenced
                        // somewhere else in the X3D file.

                        // Array list of DEFined items
                        // Clones objects with USE
                        // This transform may be animated later, which means we must have
                        // separate GVRSceneObjects
                        // for each transformation plus center and scaleOrientation if
                        // needed
                        // Order for Transformations:
                        // P' = T * C * R * SR * S * -SR * -C * P
                        // First add the translation
                        currentSceneObject.getTransform()
                                .setPosition(translation[0], translation[1], translation[2]);
                        currentSceneObject.setName(name + TRANSFORM_TRANSLATION_);
                        // now check if we have a center value.
                        if ((center[0] != 0) || (center[1] != 0) || (center[2] != 0)) {
                            currentSceneObject = AddGVRSceneObject();
                            currentSceneObject.getTransform()
                                    .setPosition(center[0], center[1], center[2]);
                            currentSceneObject.setName(name + TRANSFORM_CENTER_);
                        }
                        // add rotation
                        currentSceneObject = AddGVRSceneObject();
                        currentSceneObject.getTransform()
                                .setRotationByAxis((float) Math.toDegrees(rotation[3]),
                                        rotation[0], rotation[1], rotation[2]);
                        currentSceneObject.setName(name + TRANSFORM_ROTATION_);
                        // now check if we have a scale orientation value.
                        if ((scaleOrientation[0] != 0) || (scaleOrientation[1] != 0)
                                || (scaleOrientation[2] != 1) || (scaleOrientation[3] != 0)) {
                            currentSceneObject = AddGVRSceneObject();
                            currentSceneObject.getTransform()
                                    .setRotationByAxis((float) Math
                                                    .toDegrees(scaleOrientation[3]), scaleOrientation[0],
                                            scaleOrientation[1], scaleOrientation[2]);
                            currentSceneObject.setName(name + TRANSFORM_SCALE_ORIENTATION_);
                        }
                        // add rotation
                        currentSceneObject = AddGVRSceneObject();
                        currentSceneObject.getTransform().setScale(scale[0], scale[1],
                                scale[2]);
                        currentSceneObject.setName(name + TRANSFORM_SCALE_);
                        // if we had a scale orientation, now we have to negate it.
                        if ((scaleOrientation[0] != 0) || (scaleOrientation[1] != 0)
                                || (scaleOrientation[2] != 1) || (scaleOrientation[3] != 0)) {
                            currentSceneObject = AddGVRSceneObject();
                            currentSceneObject.getTransform()
                                    .setRotationByAxis((float) Math
                                                    .toDegrees(-scaleOrientation[3]), scaleOrientation[0],
                                            scaleOrientation[1], scaleOrientation[2]);
                            currentSceneObject
                                    .setName(name + TRANSFORM_NEGATIVE_SCALE_ORIENTATION_);
                        }
                        // now check if we have a center value.
                        if ((center[0] != 0) || (center[1] != 0) || (center[2] != 0)) {
                            currentSceneObject = AddGVRSceneObject();
                            currentSceneObject.getTransform()
                                    .setPosition(-center[0], -center[1], -center[2]);
                            currentSceneObject.setName(name + TRANSFORM_NEGATIVE_CENTER_);
                        }
                        // Actual object that will have GVRendering and GVRMesh attached
                        currentSceneObject = AddGVRSceneObject();
                        currentSceneObject.setName(name);

                        // save the object for Interactivity, Animation and Scripts
                        // The AxisAngle is saved for Scripts which is how X3D describes
                        // rotations, not quaternions.
                        DefinedItem definedItem = new DefinedItem(name, rotation[3],
                                rotation[0], rotation[1], rotation[2]);
                        definedItem.setGVRSceneObject(currentSceneObject);
                        mDefinedItems.add(definedItem); // Array list of DEFined items
                    } // end if DEF name and thus possible animation / interactivity

                    // Check if there is an active Level-of-Detail (LOD)
                    // and if so add this currentSceneObject if is
                    // a direct child of this LOD.
                    if (lodManager.isActive()) lodManager.AddLODSceneObject( currentSceneObject );

                } // not a 'Transform USE="..."' node
    }


} // end Transform
