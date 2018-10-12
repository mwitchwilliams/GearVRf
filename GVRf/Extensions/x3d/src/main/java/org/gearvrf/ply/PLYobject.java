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

package org.gearvrf.ply;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Surface;

import org.gearvrf.GVRExternalScene;
import org.gearvrf.GVRResourceVolume;
import org.gearvrf.GVRScene;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.GVRShaderId;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObjectPlayer;
import org.gearvrf.utility.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.gearvrf.x3d.Utility;
import org.joml.Matrix3f;
import org.joml.Vector2f;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRCamera;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRIndexBuffer;
import org.gearvrf.GVRLODGroup;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPerspectiveCamera;
import org.gearvrf.GVRPointLight;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRRenderPass.GVRCullFaceEnum;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSpotLight;
import org.gearvrf.GVRSwitch;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.GVRTextureParameters.TextureWrapType;
import org.gearvrf.GVRTransform;
import org.gearvrf.GVRVertexBuffer;

import org.joml.Vector3f;
import org.joml.Quaternionf;
import java.util.EnumSet;

import com.google.android.exoplayer2.upstream.AssetDataSource;
import com.google.android.exoplayer2.upstream.DataSource;


public class PLYobject {


    /**
     * Allows developers to access the root of X3D scene graph
     * by calling: GVRSceneObject.getSceneObjectByName(X3D_ROOT_NODE);
     */
    public static final String X3D_ROOT_NODE = "x3d_root_node_";

    private static final String TAG = "PLYobject";


    protected final static int verticesComponent = 1;
    protected final static int normalsComponent = 2;
    protected final static int textureCoordComponent = 3;
    protected final static int indexedFaceSetComponent = 4;
    protected final static int normalIndexComponent = 5;
    protected final static int textureIndexComponent = 6;
    protected final static int interpolatorKeyComponent = 7;
    protected final static int interpolatorKeyValueComponent = 8;
    protected final static int LODComponent = 9;
    protected final static int elevationGridHeight = 10;
    private boolean reorganizeVerts = false;


    private GVRAssetLoader.AssetRequest assetRequest = null;
    private GVRContext gvrContext = null;
    private Context activityContext = null;

    private GVRSceneObject root = null;


    // When Translation object has multiple properties (center, scale, rotation
    // plus translation)
    // the mesh must be attached to the bottom of these multiply attached
    // GVRSceneObject's but
    // the currentSceneObject's parent must be with the original Transform's
    // parent.
    private GVRSceneObject currentSceneObject = null;


    private GVRSceneObject meshAttachedSceneObject = null;
    private GVRRenderData gvrRenderData = null;
    private GVRVertexBuffer gvrVertexBuffer = null;
    private GVRIndexBuffer gvrIndexBuffer = null;
    private GVRMaterial gvrMaterial = null;


    protected Vector<Key> keys = new Vector<Key>();
    protected Vector<KeyValue> keyValues = new Vector<KeyValue>();
    protected Vector<Float> floatArray = new Vector<Float>();

    protected Vector<Float> mInputPositions = new Vector<Float>();
    protected Vector<Integer> mPositionIndices = new Vector<Integer>();
    private Utility utility = null;

    //protected Utility.MeshCreatorX.FloatArray mInputPositions = new Utility.MeshCreatorX.FloatArray(64 * 3);


    private GVRCameraRig cameraRigAtRoot = null;


    /**
     * PLYobject parses and X3D file using Java SAX parser.
     * Constructor sets up camera rig structure and
     * enables getting to the root of the scene graph by
     * calling GVRSceneObject.getSceneObjectByName(X3D_ROOT_NODE);
     */
    /*********************************************/
    /********** PLYobject Constructor ************/
    /*********************************************/
    public PLYobject(GVRAssetLoader.AssetRequest assetRequest,
                     GVRSceneObject root) {
        Log.e("X3DDBG", "PLYobject constructor");
        try {
            this.assetRequest = assetRequest;
            this.gvrContext = assetRequest.getContext();
            this.activityContext = gvrContext.getContext();
            this.root = root;

            utility = new Utility();
            // Camera rig setup code based on GVRScene::init()
            GVRCamera leftCamera = new GVRPerspectiveCamera(gvrContext);
            leftCamera.setRenderMask(GVRRenderMaskBit.Left);

            GVRCamera rightCamera = new GVRPerspectiveCamera(gvrContext);
            rightCamera.setRenderMask(GVRRenderMaskBit.Right);

            GVRPerspectiveCamera centerCamera = new GVRPerspectiveCamera(gvrContext);
            centerCamera.setRenderMask(GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
            cameraRigAtRoot = GVRCameraRig.makeInstance(gvrContext);
            cameraRigAtRoot.getOwnerObject().setName("MainCamera");
            cameraRigAtRoot.attachLeftCamera(leftCamera);
            cameraRigAtRoot.attachRightCamera(rightCamera);
            cameraRigAtRoot.attachCenterCamera(centerCamera);
            gvrContext.getMainScene().setBackgroundColor(.2f, .4f, .4f, 1);  // black background default

            //);
        } catch (Exception e) {
            Log.e(TAG, "PLYobject constructor error: " + e);
        }
    } // end Constructor


    /*********************************************/
    /********** Utility Functions to *************/
    /************* Assist Parsing ****************/
    /*********************************************/


    protected void AddKeys(float key)

    {
        Key newKey = new Key(key);
        keys.add(newKey);
    }


    protected void AddKeyValues(float[] values)

    {
        KeyValue newKeyValue = new KeyValue(values);
        keyValues.add(newKeyValue);
    }


    private GVRSceneObject AddGVRSceneObject() {
        GVRSceneObject newObject = new GVRSceneObject(gvrContext);
        if (currentSceneObject == null)
            root.addChildObject(newObject);
        else
            currentSceneObject.addChildObject(newObject);
        return newObject;

    } // end AddGVRSceneObject

    /**
     * @author m1.williams
     *         Java SAX parser interface
     */
    class UserHandler extends DefaultHandler {

        String attributeValue = null;



        /**
         * Called by the Java SAX parser implementation
         * to parse the X3D nodes.
         */

        /*********************************************/
        /*********** Parse the X3D File **************/
        /*********************************************/
        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {

            /********** Transform **********/
            /*
            if (qName.equalsIgnoreCase("transform")) {

                attributeValue = attributes.getValue("USE");
                if (attributeValue != null) {
                    ReplicateGVRSceneObjStructure(attributeValue);
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

                    if ( proto != null) {
                        if ( proto.isProtoStateProtoBody()) {
                                if ( proto.getTransform() == null) {
                                Transform transform = new Transform(center, rotation,
                                        scale, scaleOrientation, translation, name );
                                proto.setTransform( transform );
                            }
                        }
                    }
                } // not a 'Transform USE="..."' node
            } // end <Transform> node
            */




            /********** Shape **********/
            /*
            else if (qName.equalsIgnoreCase("Shape")) {

                gvrRenderData = new GVRRenderData(gvrContext);
                gvrRenderData.setAlphaToCoverage(true);
                gvrRenderData.setRenderingOrder(GVRRenderingOrder.GEOMETRY);
                gvrRenderData.setCullFace(GVRCullFaceEnum.Back);
                shaderSettings.initializeTextureMaterial(new GVRMaterial(gvrContext, x3DShader));

                // Check if this Shape node is part of a Level-of-Detail
                // If there is an active Level-of-Detail (LOD)
                // add this Shape node to new GVRSceneObject as a
                // a direct child of this LOD.
                if (lodManager.isActive()) {
                    if ( lodManager.transformLODSceneObject == currentSceneObject ) {
                        // <Shape> node not under a <Transform> inside a LOD node
                        // so we need to attach it to a GVRSceneObject, and
                        // then attach it to the LOD
                        lodManager.shapeLODSceneObject = AddGVRSceneObject();
                        currentSceneObject = lodManager.shapeLODSceneObject;
                        lodManager.AddLODSceneObject( currentSceneObject );
                    }
                }

                attributeValue = attributes.getValue("USE");
                if (attributeValue != null) { // Shape node to be shared / re-used
                    DefinedItem useItem = null;
                    for (DefinedItem definedItem : mDefinedItems) {
                        if (attributeValue.equals(definedItem.getName())) {
                            useItem = definedItem;
                            break;
                        }
                    }
                    if (useItem != null) {
                        // GVRRenderingData doesn't seem to be shared, but instead has an
                        // owner.  Thus share the GVRMesh and GVRMaterial attached to
                        // GVRRenderingData.
                        GVRRenderData gvrRenderDataDEFined = useItem.getGVRRenderData();
                        gvrRenderData.setMaterial(gvrRenderDataDEFined.getMaterial());
                        gvrRenderData.setMesh(gvrRenderDataDEFined.getMesh());
                        gvrRenderingDataUSEd = true;
                    }
                    else {
                        Log.e(TAG, "Error: Shape USE='" + attributeValue + "'; No matching DEF='" + attributeValue + "'.");
                    }
                } else {

                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        DefinedItem definedItem = new DefinedItem(attributeValue);
                        definedItem.setGVRRenderData(gvrRenderData);
                        mDefinedItems.add(definedItem); // Array list of DEFined items
                        // Clones objects with USE
                    }
                    if ( proto != null) {
                        if ( proto.isProtoStateProtoBody()) {
                            if ( proto.getShape() == null) {
                                Shape shape = new Shape(proto, attributeValue);
                                proto.setShape( shape );
                                if ( proto.getTransform() != null) {
                                    proto.getTransform().setShape( shape );
                                }
                            }
                        }
                    }  //  end if proto != null
                }
            } // end <Shape> node
            */


            /********** Appearance **********/
            /*
            else if (qName.equalsIgnoreCase("Appearance")) {
                attributeValue = attributes.getValue("USE");
                if (attributeValue != null) { // shared Appearance node, GVRMaterial
                    DefinedItem useItem = null;
                    for (DefinedItem definedItem : mDefinedItems) {
                        if (attributeValue.equals(definedItem.getName())) {
                            useItem = definedItem;
                            break;
                        }
                    }
                    if (useItem != null) {
                        gvrMaterial = useItem.getGVRMaterial();
                        gvrRenderData.setMaterial(gvrMaterial);
                        gvrMaterialUSEd = true; // for DEFine and USE, we encounter a USE,
                        // and thus have set the material
                    }
                    else {
                        Log.e(TAG, "Error: Appearance USE='" + attributeValue + "'; No matching DEF='" + attributeValue + "'.");
                    }
                } else {
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        shaderSettings.setAppearanceName(attributeValue);
                    }
                    if ( proto != null ) {
                        if ( proto.getAppearance() == null) {
                            Appearance appearance = new Appearance(attributeValue);
                            proto.setAppearance(appearance);
                        }
                    }
                }
            } // end <Appearance> node
            */


            /********** Material **********/
            /*
            else if (qName.equalsIgnoreCase("material")) {
                attributeValue = attributes.getValue("USE");
                if (attributeValue != null) {
                    DefinedItem useItem = null;
                    for (DefinedItem definedItem : mDefinedItems) {
                        if (attributeValue.equals(definedItem.getName())) {
                            useItem = definedItem;
                            break;
                        }
                    }
                    if (useItem != null) {
                        gvrMaterial = useItem.getGVRMaterial();
                        gvrRenderData.setMaterial(gvrMaterial);
                        gvrMaterialUSEd = true; // for DEFine and USE, we encounter a USE,
                        // and thus have set the material
                    }
                    else {
                        Log.e(TAG, "Error: Material USE='" + attributeValue + "'; No matching DEF='" + attributeValue + "'.");
                    }

                } else {
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        shaderSettings.setMaterialName(attributeValue);
                    }
                    String diffuseColorAttribute = attributes.getValue("diffuseColor");
                    if (diffuseColorAttribute != null) {
                        float diffuseColor[] = utility.parseFixedLengthFloatString(diffuseColorAttribute,
                                3, true, false);
                        shaderSettings.setDiffuseColor(diffuseColor);
                    }
                    String specularColorAttribute = attributes.getValue("specularColor");
                    if (specularColorAttribute != null) {
                        float specularColor[] = utility.parseFixedLengthFloatString(specularColorAttribute,
                                3, true, false);
                        shaderSettings.setSpecularColor(specularColor);
                    }
                    String emissiveColorAttribute = attributes.getValue("emissiveColor");
                    if (emissiveColorAttribute != null) {
                        float emissiveColor[] = utility.parseFixedLengthFloatString(emissiveColorAttribute,
                                3, true, false);
                        shaderSettings.setEmmissiveColor(emissiveColor);
                    }
                    String ambientIntensityAttribute = attributes
                            .getValue("ambientIntensity");
                    if (ambientIntensityAttribute != null) {
                        Log.e(TAG, "Material ambientIntensity currently not implemented.");
                        shaderSettings
                                .setAmbientIntensity(utility.parseSingleFloatString(ambientIntensityAttribute,
                                        true, false));
                    }
                    String shininessAttribute = attributes.getValue("shininess");
                    if (shininessAttribute != null) {
                        shaderSettings
                                .setShininess(utility.parseSingleFloatString(shininessAttribute, true,
                                        false));
                    }
                    String transparencyAttribute = attributes.getValue("transparency");
                    if (transparencyAttribute != null) {

                        shaderSettings
                                .setTransparency(utility.parseSingleFloatString(transparencyAttribute,
                                        true, false));
                    }
                    if ( proto != null ) {
                        if ( proto.getAppearance() != null) {
                            if ( proto.getAppearance().getMaterial() == null) {
                                Material material = new Material(shaderSettings.ambientIntensity,
                                            shaderSettings.diffuseColor, shaderSettings.emissiveColor,
                                            shaderSettings.shininess, shaderSettings.specularColor,
                                            shaderSettings.getTransparency());
                                proto.getAppearance().setMaterial(material);
                            }
                            else {
                                Log.e(TAG, "Proto error: Material not starting null inside Appearance node");
                            }
                        }
                        else {
                            Log.e(TAG, "Proto error: Material set without Appearance");
                        }
                    }
                } // end ! USE attribute
            } // end <Material> node
            */



            /********** IndexedFaceSet **********/

            //TODO: eventually include IndexedLineSet **********/
            /*
            else if (qName.equalsIgnoreCase("IndexedFaceSet")) {
                attributeValue = attributes.getValue("USE");
                if (attributeValue != null) { // shared GVRIndexBuffer / GVRMesh
                    indexedSetUSEName = attributeValue;
                } else {
                    gvrIndexBuffer = new GVRIndexBuffer(gvrContext, 4, 0);
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        indexedSetDEFName = attributeValue;
                    }
                    attributeValue = attributes.getValue("solid");
                    if (attributeValue != null) {
                        if ( !utility.parseBooleanString(attributeValue)) {
                            Log.e(TAG, "IndexedFaceSet solid=false not implemented. ");
                        }
                    }
                    attributeValue = attributes.getValue("ccw");
                    if (attributeValue != null) {
                        if ( !utility.parseBooleanString(attributeValue)) {
                            Log.e(TAG, "IndexedFaceSet ccw=false attribute not implemented. ");
                        }
                    }
                    attributeValue = attributes.getValue("colorPerVertex");
                    if (attributeValue != null) {

                        Log.e(TAG,
                                "IndexedFaceSet colorPerVertex attribute not implemented. ");

                    }
                    attributeValue = attributes.getValue("normalPerVertex");
                    if (attributeValue != null) {

                        if ( !utility.parseBooleanString(attributeValue)) {
                            Log.e(TAG,
                                    "IndexedFaceSet normalPerVertex=false attribute not implemented. ");
                        }

                    }
                    String coordIndexAttribute = attributes.getValue("coordIndex");
                    if (coordIndexAttribute != null) {
                        utility.parseNumbersString(coordIndexAttribute,
                                PLYobject.indexedFaceSetComponent, 3);
                        reorganizeVerts = true;
                    }
                    String normalIndexAttribute = attributes.getValue("normalIndex");
                    if (normalIndexAttribute != null) {
                        utility.parseNumbersString(normalIndexAttribute,
                                PLYobject.normalIndexComponent, 3);
                    }
                    String texCoordIndexAttribute = attributes.getValue("texCoordIndex");
                    if (texCoordIndexAttribute != null) {
                        utility.parseNumbersString(texCoordIndexAttribute,
                                PLYobject.textureIndexComponent, 3);
                    }
                }
                if ( proto != null ) {
                    Geometry geometry = proto.getGeometry();
                    if (geometry == null) {
                        geometry = new Geometry();
                        proto.setGeometry(geometry);
                    }
                    IndexedFaceSet indexedFaceSet = new IndexedFaceSet();
                    geometry.setIndexedFaceSet( indexedFaceSet );

                    indexedFaceSet.setCoordIndex( utility.meshCreator.mPositionIndices.array() );
                    indexedFaceSet.setTexCoordIndex( utility.meshCreator.mTexcoordIndices.array() );
                    indexedFaceSet.setNormalIndex( utility.meshCreator.mNormalIndices.array() );

                } // end proto != null
            } // end <IndexedFaceSet> node
            */


            /********** Coordinate **********/
            /*
            else if (qName.equalsIgnoreCase("Coordinate")) {
                attributeValue = attributes.getValue("USE");
                if (attributeValue != null) { // Coordinate node to be shared / re-used
                    DefinedItem useItem = null;
                    for (DefinedItem definedItem : mDefinedItems) {
                        if (attributeValue.equals(definedItem.getName())) {
                            useItem = definedItem;
                            break;
                        }
                    }
                    if (useItem != null) {

                        // 'useItem' points to GVRMesh who's useItem.getGVRMesh Coordinates
                        // were DEFined earlier. We don't want to share the entire GVRMesh
                        // since the 2 meshes may have different Normals and
                        // Texture Coordinates.  So as an alternative, copy the vertices.
                        gvrVertexBuffer = useItem.getVertexBuffer();
                        reorganizeVerts = false;
                    }
                } // end USE Coordinate
                else {
                    // Not a 'Coordinate USE="..." node
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        DefinedItem definedItem = new DefinedItem(attributeValue);
                            utility.meshCreator.defineVertexBuffer(definedItem);
                        // Array list of DEFined items clones objects with USE
                        mDefinedItems.add(definedItem);
                    }
                    String pointAttribute = attributes.getValue("point");
                    if (pointAttribute != null) {
                        utility.parseNumbersString(pointAttribute, PLYobject.verticesComponent, 3);
                    }
                    if ( proto != null ) {
                        Geometry geometry = proto.getGeometry();
                        if (geometry != null) {
                            IndexedFaceSet indexedFaceSet = geometry.getIndexedFaceSet();
                            if (indexedFaceSet != null) {
                                Coordinate coordinate = new Coordinate();
                                indexedFaceSet.setCoord( coordinate );
                                float[] coordinateValues = utility.meshCreator.mInputPositions.array();

                                coordinate.setMeshCreatorInputPositions( coordinateValues);

                            }
                            else {
                                Log.e(TAG, "PROTO: <Coordinate> not inside <IndexedFaceSet>");
                            }
                        }
                        else {
                            Log.e(TAG, "PROTO: <Coordinate> not inside <IndexedFaceSet>");
                        }  // end geometry != null
                    }  // end proto != null
                } // end NOT a USE Coordinates condition
            } // end <Coordinate> node
            */


            /********** TextureCoordinate **********/
            /*
            else if (qName.equalsIgnoreCase("TextureCoordinate")) {
                attributeValue = attributes.getValue("USE");
                if (attributeValue != null) { // Coordinate node to be shared / re-used
                    DefinedItem useItem = null;
                    for (DefinedItem definedItem : mDefinedItems) {
                        if (attributeValue.equals(definedItem.getName())) {
                            useItem = definedItem;
                            break;
                        }
                    }
                    if (useItem != null) {

                        // 'useItem' points to GVRVertexBuffer who's useItem.getVertexBuffer
                        // TextureCoordinates were DEFined earlier.
                        // We don't want to share the entire GVRVertexBuffer since the
                        // the 2 meshes may have different Normals and Positions
                        // So as an alternative, copy the texture coordinates.
                        gvrVertexBuffer.setFloatArray("a_texcoord", useItem.getVertexBuffer().getFloatArray("a_texcoord"));
                        reorganizeVerts = false;
                    }
                } // end USE TextureCoordinate
                else {
                    // Not a 'TextureCoordinate USE="..." node
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        // This is a 'TextureCoordinate DEF="..." case, so save the item
                        DefinedItem definedItem = new DefinedItem(attributeValue);
                            definedItem.setVertexBuffer(gvrVertexBuffer);
                        // Array list of DEFined items clones objects with USE
                        mDefinedItems.add(definedItem);
                    }
                    // Have to flip the y texture coordinates because the image will be
                    // upside down
                    String pointAttribute = attributes.getValue("point");
                    if (pointAttribute != null) {
                        utility.parseNumbersString(pointAttribute, PLYobject.textureCoordComponent, 2);
                    }
                    if ( proto != null ) {
                        Geometry geometry = proto.getGeometry();
                        if (geometry != null) {
                            IndexedFaceSet indexedFaceSet = geometry.getIndexedFaceSet();
                            if (indexedFaceSet != null) {
                                TextureCoordinate textureCoordinate = new TextureCoordinate();
                                indexedFaceSet.setTetureCoordinate( textureCoordinate );
                                float[] textureCoordinateValues = utility.meshCreator.mInputTexCoords.array();
                                textureCoordinate.setMeshCreatorInputTexCoords( textureCoordinateValues );
                            }
                            else {
                                Log.e(TAG, "PROTO: <TextureCoordinate> not inside <IndexedFaceSet>");
                            }
                        }
                        else {
                            Log.e(TAG, "PROTO: <TextureCoordinate> not inside <IndexedFaceSet>");
                        }  // end geometry != null
                    }  // end proto != null

                } // end NOT a USE TextureCoordinate condition
            } // end <TextureCoordinate> node
            */


            /********** Normal **********/
            /*
            else if (qName.equalsIgnoreCase("Normal")) {
                attributeValue = attributes.getValue("USE");
                if (attributeValue != null) { // Coordinate node to be shared / re-used
                    DefinedItem useItem = null;
                    for (DefinedItem definedItem : mDefinedItems) {
                        if (attributeValue.equals(definedItem.getName())) {
                            useItem = definedItem;
                            break;
                        }
                    }
                    if (useItem != null) {

                        // 'useItem' points to GVRVertexBuffer who's useItem.getVertexBuffer Coordinates
                        // were DEFined earlier. We don't want to share the entire vertex buffer since
                        // the 2 vertex buffers may have different Normals and Texture Coordinates
                        // So as an alternative, copy the normals.
                        gvrVertexBuffer.setFloatArray("a_normal", useItem.getVertexBuffer().getFloatArray("a_normal"));
                        reorganizeVerts = false;
                    }
                } // end USE Normal
                else {
                    // Not a 'Normal USE="..." node
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        // This is a 'Normal DEF="..." case, so save the item
                        DefinedItem definedItem = new DefinedItem(attributeValue);
                            definedItem.setVertexBuffer(gvrVertexBuffer);
                        // Array list of DEFined items clones objects with USE
                        mDefinedItems.add(definedItem);
                    }
                    String vectorAttribute = attributes.getValue("vector");
                    if (vectorAttribute != null) {
                        utility.parseNumbersString(vectorAttribute, PLYobject.normalsComponent, 3);
                    }
                    if ( proto != null ) {
                        Geometry geometry = proto.getGeometry();
                        if (geometry != null) {
                            IndexedFaceSet indexedFaceSet = geometry.getIndexedFaceSet();
                            if (indexedFaceSet != null) {
                                Normal normal = new Normal();
                                indexedFaceSet.setNormal( normal );
                                float[] normalValues = utility.meshCreator.mInputNormals.array();
                                normal.setMeshCreatorInputNormals( normalValues );
                            }
                            else {
                                Log.e(TAG, "PROTO: <Normal> not inside <IndexedFaceSet>");
                            }
                        }
                        else {
                            Log.e(TAG, "PROTO: <Normal> not inside <IndexedFaceSet>");
                        }  // end geometry != null
                    }
                } // end NOT a USE Normals condition
            } // end <Normal> node
            */


            /********** LIGHTS **********/
            /********** PointLight **********/
            /*
            else if (qName.equalsIgnoreCase("PointLight")) {
                if (UNIVERSAL_LIGHTS && !blockLighting) {
                    attributeValue = attributes.getValue("USE");
                    if (attributeValue != null) { // shared PointLight
                        DefinedItem useItem = null;
                        for (DefinedItem definedItem : mDefinedItems) {
                            if (attributeValue.equals(definedItem.getName())) {
                                useItem = definedItem;
                                break;
                            }
                        }
                        if (useItem != null) {
                            // GVRf does not allow a light attached at two places
                            // so copy the attributes of the original light into the second
                            // light
                            GVRSceneObject definedSceneObject = useItem.getGVRSceneObject();
                            GVRPointLight definedPtLight = (GVRPointLight) definedSceneObject
                                    .getLight();
                            GVRTransform definedTransform = definedPtLight.getTransform();

                            GVRSceneObject newPtLightSceneObj = AddGVRSceneObject();
                            newPtLightSceneObj.getTransform()
                                    .setPosition(definedTransform.getPositionX(),
                                            definedTransform.getPositionY(),
                                            definedTransform.getPositionZ());
                            // attachLight does not allow a light to be shared so we need to
                            // just create a new light and share its attributes
                            GVRPointLight newPtLight = new GVRPointLight(gvrContext);
                            newPtLightSceneObj.attachLight(newPtLight);
                            float[] attribute = definedPtLight.getAmbientIntensity();
                            newPtLight.setAmbientIntensity(attribute[0], attribute[1],
                                    attribute[2], attribute[3]);
                            attribute = definedPtLight.getDiffuseIntensity();
                            newPtLight.setDiffuseIntensity(attribute[0], attribute[1],
                                    attribute[2], 1);
                            attribute = definedPtLight.getSpecularIntensity();
                            newPtLight.setSpecularIntensity(attribute[0], attribute[1],
                                    attribute[2], 1);
                            newPtLight
                                    .setAttenuation(definedPtLight.getAttenuationConstant(),
                                            definedPtLight.getAttenuationLinear(),
                                            definedPtLight.getAttenuationQuadratic());
                            newPtLight.enable();
                        }
                        else {
                            Log.e(TAG, "Error: PointLight USE='" + attributeValue + "'; No matching DEF='" + attributeValue + "'.");
                        }
                    } // end reuse a PointLight
                    else {
                        // add a new PointLight
                        float ambientIntensity = 0;
                        float[] attenuation =
                                {
                                        1, 0, 0
                                };
                        float[] color =
                                {
                                        1, 1, 1
                                };
                        boolean global = true;
                        float intensity = 1;
                        float[] location =
                                {
                                        0, 0, 0
                                };
                        boolean on = true;
                        float radius = 100;

                        GVRSceneObject newPtLightSceneObj = AddGVRSceneObject();
                        GVRPointLight newPtLight = new GVRPointLight(gvrContext);
                        newPtLightSceneObj.attachLight(newPtLight);

                        attributeValue = attributes.getValue("DEF");
                        if (attributeValue != null) {
                            newPtLightSceneObj.setName(attributeValue);
                            DefinedItem definedItem = new DefinedItem(attributeValue);
                            definedItem.setGVRSceneObject(newPtLightSceneObj);
                            mDefinedItems.add(definedItem); // Array list of DEFined items
                            // Clones objects with USE
                        }
                        attributeValue = attributes.getValue("ambientIntensity");
                        if (attributeValue != null) {
                            ambientIntensity = utility.parseSingleFloatString(attributeValue, true,
                                    false);
                        }
                        attributeValue = attributes.getValue("attenuation");
                        if (attributeValue != null) {
                            attenuation = utility.parseFixedLengthFloatString(attributeValue, 3,
                                    false, true);
                            if ((attenuation[0] == 0) && (attenuation[1] == 0)
                                    && (attenuation[2] == 0))
                                attenuation[0] = 1;
                        }
                        attributeValue = attributes.getValue("color");
                        if (attributeValue != null) {
                            color = utility.parseFixedLengthFloatString(attributeValue, 3, true,
                                    false);
                        }
                        attributeValue = attributes.getValue("global");
                        if (attributeValue != null) {
                            global = utility.parseBooleanString(attributeValue); // NOT IMPLEMENTED
                            Log.e(TAG, "Point Light global attribute not implemented. ");
                        }
                        attributeValue = attributes.getValue("intensity");
                        if (attributeValue != null) {
                            intensity = utility.parseSingleFloatString(attributeValue, true, false);
                        }
                        attributeValue = attributes.getValue("location");
                        if (attributeValue != null) {
                            location = utility.parseFixedLengthFloatString(attributeValue, 3, false,
                                    false);
                        }
                        attributeValue = attributes.getValue("on");
                        if (attributeValue != null) {
                            on = utility.parseBooleanString(attributeValue);
                        }
                        attributeValue = attributes.getValue("radius");
                        if (attributeValue != null) {
                            radius = utility.parseSingleFloatString(attributeValue, false, true);
                        }
                        // In x3d, ambientIntensity is only 1 value, not 3.
                        newPtLight.setAmbientIntensity(ambientIntensity, ambientIntensity,
                                ambientIntensity, 1);
                        newPtLight.setDiffuseIntensity(color[0] * intensity,
                                color[1] * intensity,
                                color[2] * intensity, 1);
                        // x3d doesn't have an equivalent for specular intensity
                        newPtLight.setSpecularIntensity(color[0] * intensity,
                                color[1] * intensity,
                                color[2] * intensity, 1);
                        newPtLight.setAttenuation(attenuation[0], attenuation[1],
                                attenuation[2]);
                        if (on)
                            newPtLight.enable();
                        else
                            newPtLight.disable();

                        GVRTransform newPtLightSceneObjTransform = newPtLightSceneObj
                                .getTransform();
                        newPtLightSceneObjTransform.setPosition(location[0], location[1],
                                location[2]);
                    } // end a new PointLight
                } // end if UNIVERSAL_LIGHTS

            } // end <PointLight> node
            */


            /********** DirectionalLight **********/
            /*
            else if (qName.equalsIgnoreCase("DirectionalLight")) {
                if (UNIVERSAL_LIGHTS && !blockLighting) {
                    attributeValue = attributes.getValue("USE");
                    if (attributeValue != null) { // shared PointLight
                        DefinedItem useItem = null;
                        for (DefinedItem definedItem : mDefinedItems) {
                            if (attributeValue.equals(definedItem.getName())) {
                                useItem = definedItem;
                                break;
                            }
                        }
                        if (useItem != null) {
                            // GVRf does not allow a light attached at two places
                            // so copy the attributes of the original light into the second
                            // light
                            GVRSceneObject definedSceneObject = useItem.getGVRSceneObject();
                            GVRDirectLight definedDirectLight = (GVRDirectLight) definedSceneObject
                                    .getLight();
                            GVRTransform definedTransform = definedDirectLight.getTransform();

                            GVRSceneObject newDirectLightSceneObj = AddGVRSceneObject();
                            newDirectLightSceneObj.getTransform()
                                    .setRotation(definedTransform.getRotationW(),
                                            definedTransform.getRotationX(),
                                            definedTransform.getRotationY(),
                                            definedTransform.getRotationZ());
                            // attachLight does not allow a light to be shared so we need to
                            // just create a new light and share its attributes
                            GVRDirectLight newDirectLight = new GVRDirectLight(gvrContext);
                            newDirectLightSceneObj.attachLight(newDirectLight);
                            float[] attribute = definedDirectLight.getAmbientIntensity();
                            newDirectLight.setAmbientIntensity(attribute[0], attribute[1],
                                    attribute[2], attribute[3]);
                            attribute = definedDirectLight.getDiffuseIntensity();
                            newDirectLight.setDiffuseIntensity(attribute[0], attribute[1],
                                    attribute[2], 1);
                            attribute = definedDirectLight.getSpecularIntensity();
                            newDirectLight.setSpecularIntensity(attribute[0], attribute[1],
                                    attribute[2], 1);
                            newDirectLight.enable();
                        }
                        else {
                            Log.e(TAG, "Error: DirectionalLight USE='" + attributeValue + "'; No matching DEF='" + attributeValue + "'.");
                        }
                    } // end reuse a DirectionalLight
                    else {
                        // add a new DirectionalLight
                        float ambientIntensity = 0;
                        float[] color =
                                {
                                        1, 1, 1
                                };
                        float[] direction =
                                {
                                        0, 0, -1
                                };
                        boolean global = true;
                        float intensity = 1;
                        boolean on = true;

                        GVRSceneObject newDirectionalLightSceneObj = AddGVRSceneObject();
                        GVRDirectLight newDirectionalLight = new GVRDirectLight(gvrContext);
                        newDirectionalLightSceneObj.attachLight(newDirectionalLight);
                        DefinedItem definedItem = null;

                        attributeValue = attributes.getValue("DEF");
                        if (attributeValue != null) {
                            newDirectionalLightSceneObj.setName(attributeValue);
                            definedItem = new DefinedItem(attributeValue);
                            definedItem.setGVRSceneObject(newDirectionalLightSceneObj);
                            mDefinedItems.add(definedItem); // Array list of DEFined items
                            // Clones objects with USE
                        }
                        attributeValue = attributes.getValue("ambientIntensity");
                        if (attributeValue != null) {
                            ambientIntensity = utility.parseSingleFloatString(attributeValue, true,
                                    false);
                        }
                        attributeValue = attributes.getValue("color");
                        if (attributeValue != null) {
                            color = utility.parseFixedLengthFloatString(attributeValue, 3, true,
                                    false);
                        }
                        attributeValue = attributes.getValue("direction");
                        if (attributeValue != null) {
                            direction = utility.parseFixedLengthFloatString(attributeValue, 3, false,
                                    false);
                        }
                        attributeValue = attributes.getValue("global");
                        if (attributeValue != null) {
                            Log.e(TAG,
                                    "DirectionalLight global attribute not currently implemented. ");
                        }
                        attributeValue = attributes.getValue("intensity");
                        if (attributeValue != null) {
                            intensity = utility.parseSingleFloatString(attributeValue, true, false);
                        }
                        attributeValue = attributes.getValue("on");
                        if (attributeValue != null) {
                            on = utility.parseBooleanString(attributeValue);
                        }

                        newDirectionalLight.setAmbientIntensity(1, 1, 1, 1);
                        newDirectionalLight.setDiffuseIntensity(color[0] * intensity,
                                color[1] * intensity,
                                color[2] * intensity, 1);
                        newDirectionalLight.setSpecularIntensity(1, 1, 1, 1);

                        if (on)
                            newDirectionalLight.enable();
                        else
                            newDirectionalLight.disable();

                        if (definedItem != null) definedItem.setDirection(direction);

                        Quaternionf q = animationInteractivityManager.ConvertDirectionalVectorToQuaternion(
                                new Vector3f(direction[0], direction[1], direction[2]));
                        // set direction in the Light's GVRScene
                        GVRTransform newDirectionalLightSceneObjTransform = newDirectionalLightSceneObj
                                .getTransform();
                        newDirectionalLightSceneObjTransform.setRotation(q.w, q.x, q.y,
                                q.z);
                    } // end if adding new Directional Light
                } // end if Universal Lights

            } // end <Directional Light> node
            */


            /********** SpotLight **********/
            /*
            else if (qName.equalsIgnoreCase("SpotLight")) {
                if (UNIVERSAL_LIGHTS && !blockLighting) {
                    attributeValue = attributes.getValue("USE");
                    if (attributeValue != null) { // shared PointLight
                        DefinedItem useItem = null;
                        for (DefinedItem definedItem : mDefinedItems) {
                            if (attributeValue.equals(definedItem.getName())) {
                                useItem = definedItem;
                                break;
                            }
                        }
                        if (useItem != null) {
                            // GVRf does not allow a light attached at two places
                            // so copy the attributes of the original light into the second
                            // light
                            GVRSceneObject definedSceneObject = useItem.getGVRSceneObject();
                            GVRSpotLight definedSpotLight = (GVRSpotLight) definedSceneObject
                                    .getLight();
                            GVRTransform definedTransform = definedSpotLight.getTransform();

                            GVRSceneObject newSpotLightSceneObj = AddGVRSceneObject();
                            newSpotLightSceneObj.getTransform()
                                    .setPosition(definedTransform.getPositionX(),
                                            definedTransform.getPositionY(),
                                            definedTransform.getPositionZ());
                            newSpotLightSceneObj.getTransform()
                                    .setRotation(definedTransform.getRotationW(),
                                            definedTransform.getRotationX(),
                                            definedTransform.getRotationY(),
                                            definedTransform.getRotationZ());
                            // attachLight does not allow a light to be shared so we need to
                            // just create a new light and share its attributes
                            GVRSpotLight newSpotLight = new GVRSpotLight(gvrContext);
                            newSpotLightSceneObj.attachLight(newSpotLight);
                            float[] attribute = definedSpotLight.getAmbientIntensity();
                            newSpotLight.setAmbientIntensity(attribute[0], attribute[1],
                                    attribute[2], attribute[3]);
                            attribute = definedSpotLight.getDiffuseIntensity();
                            newSpotLight.setDiffuseIntensity(attribute[0], attribute[1],
                                    attribute[2], 1);
                            attribute = definedSpotLight.getSpecularIntensity();
                            newSpotLight.setSpecularIntensity(attribute[0], attribute[1],
                                    attribute[2], 1);
                            newSpotLight
                                    .setAttenuation(definedSpotLight.getAttenuationConstant(),
                                            definedSpotLight.getAttenuationLinear(),
                                            definedSpotLight.getAttenuationQuadratic());
                            newSpotLight
                                    .setInnerConeAngle(definedSpotLight.getInnerConeAngle());
                            newSpotLight
                                    .setOuterConeAngle(definedSpotLight.getOuterConeAngle());
                            newSpotLight.enable();
                        }
                        else {
                            Log.e(TAG, "Error: SpotLight USE='" + attributeValue + "'; No matching DEF='" + attributeValue + "'.");
                        }
                    } // end reuse a SpotLight
                    else {
                        // add a new SpotLight
                        float ambientIntensity = 0;
                        float[] attenuation =
                                {
                                        1, 0, 0
                                };
                        float beamWidth = (float) Math.PI / 4; // range is 0 to PI
                        float[] color =
                                {
                                        1, 1, 1
                                };
                        float cutOffAngle = (float) Math.PI / 2; // range is 0 to PI
                        float[] direction =
                                {
                                        0, 0, -1
                                };
                        boolean global = true;
                        float intensity = 1;
                        float[] location =
                                {
                                        0, 0, 0
                                };
                        boolean on = true;
                        float radius = 100;

                        GVRSceneObject newSpotLightSceneObj = AddGVRSceneObject();
                        GVRSpotLight newSpotLight = new GVRSpotLight(gvrContext);
                        newSpotLightSceneObj.attachLight(newSpotLight);

                        DefinedItem definedItem = null;

                        attributeValue = attributes.getValue("DEF");
                        if (attributeValue != null) {
                            newSpotLightSceneObj.setName(attributeValue);
                            definedItem = new DefinedItem(attributeValue);
                            definedItem.setGVRSceneObject(newSpotLightSceneObj);
                            mDefinedItems.add(definedItem); // Array list of DEFined items
                            // Clones objects with USE
                        }
                        attributeValue = attributes.getValue("ambientIntensity");
                        if (attributeValue != null) {
                            ambientIntensity = utility.parseSingleFloatString(attributeValue, true,
                                    false);
                        }
                        attributeValue = attributes.getValue("attenuation");
                        if (attributeValue != null) {
                            attenuation = utility.parseFixedLengthFloatString(attributeValue, 3,
                                    false, true);
                            if ((attenuation[0] == 0) && (attenuation[1] == 0)
                                    && (attenuation[2] == 0))
                                attenuation[0] = 1;
                        }
                        attributeValue = attributes.getValue("beamWidth");
                        if (attributeValue != null) {
                            beamWidth = utility.parseSingleFloatString(attributeValue, false, true);
                            if (beamWidth > (float) Math.PI / 2) {
                                beamWidth = (float) Math.PI / 2;
                                Log.e(TAG, "Spot Light beamWidth cannot exceed PI/2.");
                            }
                        }
                        attributeValue = attributes.getValue("color");
                        if (attributeValue != null) {
                            color = utility.parseFixedLengthFloatString(attributeValue, 3, true,
                                    false);
                        }
                        attributeValue = attributes.getValue("cutOffAngle");
                        if (attributeValue != null) {
                            cutOffAngle = utility.parseSingleFloatString(attributeValue, false, true);
                            if (cutOffAngle > (float) Math.PI / 2) {
                                cutOffAngle = (float) Math.PI / 2;
                                Log.e(TAG, "Spot Light cutOffAngle cannot exceed PI/2.");
                            }
                        }
                        attributeValue = attributes.getValue("direction");
                        if (attributeValue != null) {
                            direction = utility.parseFixedLengthFloatString(attributeValue, 3, false,
                                    false);
                        }
                        attributeValue = attributes.getValue("global");
                        if (attributeValue != null) {
                            Log.e(TAG,
                                    "Spot Light global attribute not currently implemented. ");
                        }
                        attributeValue = attributes.getValue("intensity");
                        if (attributeValue != null) {
                            intensity = utility.parseSingleFloatString(attributeValue, true, false);
                        }
                        attributeValue = attributes.getValue("location");
                        if (attributeValue != null) {
                            location = utility.parseFixedLengthFloatString(attributeValue, 3, false,
                                    false);
                        }
                        attributeValue = attributes.getValue("on");
                        if (attributeValue != null) {
                            on = utility.parseBooleanString(attributeValue);
                        }
                        attributeValue = attributes.getValue("radius");
                        if (attributeValue != null) {
                            radius = utility.parseSingleFloatString(attributeValue, false, true);
                        }
                        // x3d only has a single value for ambient intensity
                        newSpotLight.setAmbientIntensity(ambientIntensity, ambientIntensity,
                                ambientIntensity, 1);
                        newSpotLight.setDiffuseIntensity(color[0] * intensity,
                                color[1] * intensity,
                                color[2] * intensity, 1);
                        newSpotLight.setSpecularIntensity(0, 0, 0, 1);
                        newSpotLight.setAttenuation(attenuation[0], attenuation[1],
                                attenuation[2]);

                        if (on)
                            newSpotLight.enable();
                        else
                            newSpotLight.disable();
                        newSpotLight.setInnerConeAngle(beamWidth * 180 / (float) Math.PI);
                        newSpotLight.setOuterConeAngle(cutOffAngle * 180 / (float) Math.PI);

                        if (definedItem != null) definedItem.setDirection(direction);
                        Quaternionf q = animationInteractivityManager.ConvertDirectionalVectorToQuaternion(
                                new Vector3f(direction[0], direction[1], direction[2]));
                        // set position and direction in the SpotLight's GVRScene
                        GVRTransform newSpotLightSceneObjTransform = newSpotLightSceneObj
                                .getTransform();
                        newSpotLightSceneObjTransform.setPosition(location[0], location[1],
                                location[2]);
                        newSpotLightSceneObjTransform.setRotation(q.w, q.x, q.y, q.z);
                    } // end adding a new SpotLight

                } // end if UNIVERSAL_LIGHTS

            } // end <SpotLight> node
            */


                /********** Viewpoint **********/
                /*
                else if (qName.equalsIgnoreCase("Viewpoint")) {
                    float[] centerOfRotation =
                            {
                                    0, 0, 0
                            };
                    String description = "";
                    float fieldOfView = (float) Math.PI / 4;
                    boolean jump = true;
                    String name = "";
                    float[] orientation =
                            {
                                    0, 0, 1, 0
                            };
                    float[] position =
                            {
                                    0, 0, 10
                            };
                    boolean retainUserOffsets = false;

                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("centerOfRotation");
                    if (attributeValue != null) {
                        centerOfRotation = utility.parseFixedLengthFloatString(attributeValue, 3,
                                false, false);
                        Log.e(TAG, "X3D Viewpoint centerOfRotation not implemented in GearVR.");
                    }
                    attributeValue = attributes.getValue("description");
                    if (attributeValue != null) {
                        description = attributeValue;
                    }
                    attributeValue = attributes.getValue("fieldOfView");
                    if (attributeValue != null) {
                        fieldOfView = utility.parseSingleFloatString(attributeValue, false, true);
                        if (fieldOfView > (float) Math.PI)
                            fieldOfView = (float) Math.PI;
                        Log.e(TAG, "X3D Viewpoint fieldOfView not implemented in GearVR. ");
                    }
                    attributeValue = attributes.getValue("jump");
                    if (attributeValue != null) {
                        jump = utility.parseBooleanString(attributeValue);
                    }
                    attributeValue = attributes.getValue("orientation");
                    if (attributeValue != null) {
                        orientation = utility.parseFixedLengthFloatString(attributeValue, 4, false,
                                false);
                    }
                    attributeValue = attributes.getValue("position");
                    if (attributeValue != null) {
                        position = utility.parseFixedLengthFloatString(attributeValue, 3, false,
                                false);
                    }
                    attributeValue = attributes.getValue("retainUserOffsets");
                    if (attributeValue != null) {
                        retainUserOffsets = utility.parseBooleanString(attributeValue);
                        Log.e(TAG, "Viewpoint retainUserOffsets attribute not implemented. ");
                    }
                    // Add viewpoint to the list.
                    // Since viewpoints can be under a Transform, save the parent.
                    Viewpoint viewpoint = new Viewpoint(centerOfRotation, description,
                            fieldOfView, jump, name, orientation, position, retainUserOffsets,
                            currentSceneObject);
                    viewpoints.add(viewpoint);

                    if ( !name.equals("") ) {
                        DefinedItem definedItem = new DefinedItem(name);
                        definedItem.setViewpoint(viewpoint);
                        mDefinedItems.add(definedItem); // Array list of DEFined items
                    }


                } // end <Viewpoint> node
                */




                /********** Anchor **********/
                /*
                else if (qName.equalsIgnoreCase("Anchor")) {
                    String name = "";
                    String description = "";
                    String[] parameter = null;
                    String url = "";
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("description");
                    if (attributeValue != null) {
                        description = attributeValue;
                    }
                    attributeValue = attributes.getValue("parameter");
                    if (attributeValue != null) {
                        parameter = utility.parseMFString(attributeValue);
                    }
                    attributeValue = attributes.getValue("url");
                    if (attributeValue != null) {

                        // TODO: issues with parsing

                        // multiple strings with special chars
                        url = attributeValue;
                    }
                    // Set the currentSensor pointer so that child objects will be added
                    // to the list of eye pointer objects.
                    currentSceneObject = AddGVRSceneObject();
                    currentSceneObject.setName(name);
                    Sensor sensor = new Sensor(name, Sensor.Type.ANCHOR,
                            currentSceneObject, true);
                    sensor.setAnchorURL(url);
                    sensors.add(sensor);
                    animationInteractivityManager.BuildInteractiveObjectFromAnchor(sensor, url);
                } // end <Anchor> node
                */


                /********** TouchSensor **********/
                /*
                else if (qName.equalsIgnoreCase("TouchSensor")) {
                    String name = "";
                    String description = "";
                    boolean enabled = true;
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("description");
                    if (attributeValue != null) {
                        description = attributeValue;
                    }
                    attributeValue = attributes.getValue("enabled");
                    if (attributeValue != null) {
                        enabled = utility.parseBooleanString(attributeValue);
                    }

                    Sensor sensor = new Sensor(name, Sensor.Type.TOUCH, currentSceneObject, enabled);
                    sensors.add(sensor);
                    // add colliders to all objects under the touch sensor
                    currentSceneObject.attachCollider(new GVRMeshCollider(gvrContext, true));
                } // end <TouchSensor> node
                 */


                /********** Script **********/
                /*
                else if (qName.equalsIgnoreCase("Script")) {
                    String name = "";
                    Boolean directOutput = false;
                    Boolean mustEvaluate = false;
                    String[] url = null;

                    // The EcmaScript / JavaScript will be parsed inside
                    // SAX's characters method
                    parseJavaScript = true;
                    //reset.  This will hold complete JavaScript function(s)
                    javaScriptCode = "";

                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("url");
                    if (attributeValue != null) {
                        url = utility.parseMFString(attributeValue);
                    }
                    attributeValue = attributes.getValue("directOutput");
                    if (attributeValue != null) {
                        directOutput = utility.parseBooleanString(attributeValue);
                    }
                    attributeValue = attributes.getValue("mustEvaluate");
                    if (attributeValue != null) {
                        mustEvaluate = utility.parseBooleanString(attributeValue);
                    }
                    currentScriptObject = new ScriptObject(name, directOutput, mustEvaluate, url);
                }  //  end <Script> node
                */


                /******* field (embedded inside <Script>) node *******/
                /*
                else if (qName.equalsIgnoreCase("field")) {

                    String name = "";
                    ScriptObject.AccessType accessType = ScriptObject.AccessType.INPUT_OUTPUT;
                    String type = "";
                    String value = "";

                    attributeValue = attributes.getValue("accessType");
                    if (attributeValue != null) {
                        if (attributeValue.equals("inputOnly")) {
                            accessType = ScriptObject.AccessType.INPUT_ONLY;
                        } else if (attributeValue.equals("outputOnly")) {
                            accessType = ScriptObject.AccessType.OUTPUT_ONLY;
                        } else if (attributeValue.equals("inputOutput")) {
                            accessType = ScriptObject.AccessType.INPUT_OUTPUT;
                        } else if (attributeValue.equals("initializeOnly")) {
                            accessType = ScriptObject.AccessType.INITIALIZE_ONLY;
                        }
                    }
                    attributeValue = attributes.getValue("name");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("type");
                    if (attributeValue != null) {
                        type = attributeValue;
                    }
                    attributeValue = attributes.getValue("value");
                    if (attributeValue != null) {
                        value = attributeValue;
                    }
                    if (currentScriptObject != null) {
                        currentScriptObject.addField(name, accessType, type);
                    }
                    else if ( proto != null ) {
                        if ( proto.isProtoStateProtoInterface() ) {
                            // Add this field to the list of Proto field's
                            proto.AddField(accessType, name, type, value);
                        }
                    }
                }  //  end <field> node
                */

                /********** MovieTexture **********/
                /*
                else if (qName.equalsIgnoreCase("MovieTexture")) {
                    attributeValue = attributes.getValue("USE");
                    if (attributeValue != null) {
                        DefinedItem useItem = null;
                        for (DefinedItem definedItem : mDefinedItems) {
                            if (attributeValue.equals(definedItem.getName())) {
                                useItem = definedItem;
                                break;
                            }
                        }
                        if (useItem != null) {
                            Log.e(TAG, "MovieTexture USE not implemented");
                            gvrTexture = useItem.getGVRTexture();
                            shaderSettings.setTexture(gvrTexture);
                        }
                        else {
                            Log.e(TAG, "Error: MovieTexture USE='" + attributeValue + "'; No matching DEF='" + attributeValue + "'.");
                        }
                    } else {
                        String description = "";
                        boolean loop = false;

                        String urlAttribute = attributes.getValue("url");
                        if (urlAttribute != null) {
                            String[] urlsString = utility.parseMFString(urlAttribute);

                            for (int i = 0; i < urlsString.length; i++) {
                                shaderSettings.movieTextures.add(urlsString[i]);
                            }
                        }
                        attributeValue = attributes.getValue("loop");
                        if (attributeValue != null) {
                            shaderSettings.setMovieTextureLoop(utility.parseBooleanString(attributeValue) );
                        }
                        String repeatSAttribute = attributes.getValue("repeatS");
                        if (repeatSAttribute != null) {
                                if (!utility.parseBooleanString(repeatSAttribute)) {
                                    //TODO: gvrTextureParameters.setWrapSType(TextureWrapType.GL_CLAMP_TO_EDGE);
                                }
                        }
                        String repeatTAttribute = attributes.getValue("repeatT");
                        if (repeatTAttribute != null) {
                                if (!utility.parseBooleanString(repeatTAttribute)) {
                                    //TODO: gvrTextureParameters.setWrapTType(TextureWrapType.GL_CLAMP_TO_EDGE);
                                }
                        }
                        shaderSettings.setMovieTextureName(attributes.getValue("DEF") );

                        if ( proto != null ) {
                            if ( proto.getAppearance() != null) {

                                if ( proto.getAppearance().getMovieTexture() == null) {
                                    String[] movieTextures = new String[1];
                                    movieTextures[0] = shaderSettings.movieTextures.get(0);
                                    MovieTexture movieTexture = new MovieTexture(loop, 1.0f, 1.0f,
                                            movieTextures);
                                    proto.getAppearance().setMovieTexture( movieTexture );
                                }
                                else {
                                    Log.e(TAG, "Proto error: MovieTexture not starting null inside Appearance node");
                                }
                            }
                            else {
                                Log.e(TAG, "Proto error: MovieTexture set without Appearance");
                            }
                        }

                    }
                } // end <MovieTexture> node
                */


                /********** Background **********/
                /*
                else if (qName.equalsIgnoreCase("Background")) {
                    float[] skycolor =
                            {
                                    0, 0, 0
                            };
                    String[] backUrl = {};
                    String[] bottomUrl = {};
                    String[] frontUrl = {};
                    String[] leftUrl = {};
                    String[] rightUrl = {};
                    String[] topUrl = {};
                    float transparency = 0;
                    float groundAngle = 0;

                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        Log.e(TAG, "Background DEF attribute not implemented. ");
                    }
                    attributeValue = attributes.getValue("groundColor");
                    if (attributeValue != null) {
                        Log.e(TAG, "Background groundColor attribute not implemented. ");
                    }
                    attributeValue = attributes.getValue("skyColor");
                    if (attributeValue != null) {
                        skycolor = utility.parseFixedLengthFloatString(attributeValue, 3, true,
                                false);
                    }
                    attributeValue = attributes.getValue("backUrl");
                    if (attributeValue != null) {
                        backUrl = utility.parseMFString(attributeValue);
                    }
                    attributeValue = attributes.getValue("bottomUrl");
                    if (attributeValue != null) {
                        bottomUrl = utility.parseMFString(attributeValue);
                    }
                    attributeValue = attributes.getValue("frontUrl");
                    if (attributeValue != null) {
                        frontUrl = utility.parseMFString(attributeValue);
                    }
                    attributeValue = attributes.getValue("leftUrl");
                    if (attributeValue != null) {
                        leftUrl = utility.parseMFString(attributeValue);
                    }
                    attributeValue = attributes.getValue("rightUrl");
                    if (attributeValue != null) {
                        rightUrl = utility.parseMFString(attributeValue);
                    }
                    attributeValue = attributes.getValue("topUrl");
                    if (attributeValue != null) {
                        topUrl = utility.parseMFString(attributeValue);
                    }
                    attributeValue = attributes.getValue("transparency");
                    if (attributeValue != null) {
                        transparency = utility.parseSingleFloatString(attributeValue, true, false);
                        Log.e(TAG, "Background transparency attribute not implemented. ");
                    }
                    attributeValue = attributes.getValue("groundAngle");
                    if (attributeValue != null) {
                        Log.e(TAG, "Background groundAngle attribute not implemented. ");
                        groundAngle = utility.parseSingleFloatString(attributeValue, false, true);
                        if (groundAngle > (float) Math.PI / 2) {
                            groundAngle = (float) Math.PI / 2;
                            Log.e(TAG, "Background groundAngle cannot exceed PI/2.");
                        }
                    }

                    // if url's defined, use cube mapping for the background
                    if ((backUrl.length > 0) && (bottomUrl.length > 0)
                            && (frontUrl.length > 0) && (leftUrl.length > 0)
                            && (rightUrl.length > 0) && (topUrl.length > 0)) {

                        ArrayList<GVRTexture> textureList = new ArrayList<GVRTexture>(6);
                        GVRAssetLoader loader = gvrContext.getAssetLoader();
                        String urlAttribute = backUrl[0].substring(0,
                                backUrl[0].indexOf("."));
                        int assetID = activityContext.getResources()
                                .getIdentifier(urlAttribute, "drawable",
                                        activityContext.getPackageName());
                        if (assetID != 0) {
                            textureList
                                    .add(loader.loadTexture(new GVRAndroidResource(
                                            gvrContext, assetID)));
                        }

                        urlAttribute = rightUrl[0].substring(0, rightUrl[0].indexOf("."));
                        assetID = activityContext.getResources()
                                .getIdentifier(urlAttribute, "drawable",
                                        activityContext.getPackageName());
                        if (assetID != 0) {
                            textureList
                                    .add(loader.loadTexture(new GVRAndroidResource(
                                            gvrContext, assetID)));
                        }

                        urlAttribute = frontUrl[0].substring(0, frontUrl[0].indexOf("."));
                        assetID = activityContext.getResources()
                                .getIdentifier(urlAttribute, "drawable",
                                        activityContext.getPackageName());
                        if (assetID != 0) {
                            textureList
                                    .add(loader.loadTexture(new GVRAndroidResource(
                                            gvrContext, assetID)));
                        }

                        urlAttribute = leftUrl[0].substring(0, leftUrl[0].indexOf("."));
                        assetID = activityContext.getResources()
                                .getIdentifier(urlAttribute, "drawable",
                                        activityContext.getPackageName());
                        if (assetID != 0) {
                            textureList
                                    .add(loader.loadTexture(new GVRAndroidResource(
                                            gvrContext, assetID)));
                        }

                        urlAttribute = topUrl[0].substring(0, topUrl[0].indexOf("."));
                        assetID = activityContext.getResources()
                                .getIdentifier(urlAttribute, "drawable",
                                        activityContext.getPackageName());
                        if (assetID != 0) {
                            textureList
                                    .add(loader.loadTexture(new GVRAndroidResource(
                                            gvrContext, assetID)));
                        }

                        urlAttribute = bottomUrl[0].substring(0, bottomUrl[0].indexOf("."));
                        assetID = activityContext.getResources()
                                .getIdentifier(urlAttribute, "drawable",
                                        activityContext.getPackageName());
                        if (assetID != 0) {
                            textureList
                                    .add(loader.loadTexture(new GVRAndroidResource(
                                            gvrContext, assetID)));
                        }

                        GVRCubeSceneObject mCubeEvironment = new GVRCubeSceneObject(
                                gvrContext, false, textureList);
                        mCubeEvironment.getRenderData().setMaterial(new GVRMaterial(gvrContext, x3DShader));
                        mCubeEvironment.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH,
                                CUBE_WIDTH);

                        root.addChildObject(mCubeEvironment);
                    } else {
                        // Not cubemapping, then set default skyColor
                        gvrContext.getMainScene().setBackgroundColor(skycolor[0], skycolor[1], skycolor[2], 1);
                    }

                } // end <Background> node
                */

            }  // end 'else { if stmt' at ROUTE, which should be deleted
        }  //  end startElement


        public void endElement(String uri, String localName, String qName)
                throws SAXException {
        }  // end endElement

    //} // end UserHandler
/*
        @Override
        public void characters(char ch[], int start, int length) throws SAXException {
            if (parseJavaScript) {
                // Each JavaScript must start with the importPackage line
                // to include the X3D Data types like SFColor
                String js = "";
                boolean leadingNonprintChars = true;
                for (int i = start; i < length; i++) {
                    if ((ch[i] == ' ') || (ch[i] == '\t')) {
                        if (!leadingNonprintChars && (ch[i] == ' ')) {
                            js += ch[i];
                        }
                    } else {
                        js += ch[i];
                        leadingNonprintChars = false;
                    }
                }
                javaScriptCode += js;
            }
        }  //  end characters method
        */



    public void Parse(InputStream inputStream ) {
        try {

            Log.e("X3DDBG", "PLYobject Parse." );

            InputStreamReader bufferedInputStream = new InputStreamReader( inputStream );
            BufferedReader  buffer = new BufferedReader( bufferedInputStream );
            String line;

            int cnt = 0;
            int elementVertex = 0;
            int elementFace = 0;
            boolean readHeader = true;

            currentSceneObject = AddGVRSceneObject();

            GVRCameraRig mainCameraRig = gvrContext.getMainScene().getMainCameraRig();
            mainCameraRig.getTransform().setPosition(0, 0, 10);

            boolean[] vertex3d = {false, false, false};
            boolean vertex3dFile = false;
            boolean[] color = {false, false, false};
            boolean vertexColor = false;
            while ( ((line = buffer.readLine()) != null ) && (readHeader) ){
                Log.e("X3DDBG", "line["+cnt+"] " + line);
                if (line.startsWith("property float x")) vertex3d[0] = true;
                else if (line.startsWith("property float y")) vertex3d[1] = true;
                else if (line.startsWith("property float z")) vertex3d[2] = true;
                else if (line.startsWith("property uchar red")) color[0] = true;
                else if (line.startsWith("property uchar green")) color[1] = true;
                else if (line.startsWith("property uchar blue")) color[2] = true;
                else if ( line.startsWith("element vertex")) {
                    elementVertex = utility.parseIntegerString( new String( line.substring("element vertex".length() )) );
                    Log.e("X3DDBG", "   elementVertex=" + elementVertex);
                }
                else if ( line.startsWith("element face")) {
                    elementFace = utility.parseIntegerString( new String( line.substring("element face".length() )) );
                    Log.e("X3DDBG", "   elementFace=" + elementFace );
                }
                else if ( line.startsWith("end_header")) {
                    Log.e("X3DDBG", "Finished reading ply header");
                    readHeader = false;
                }
                cnt++;
            }
            Log.e("X3DDBG", "+++++++++++");
            if ( vertex3d[0] && vertex3d[1] && vertex3d[2] )  vertex3dFile = true;
            Log.e("X3DDBG", "vertex 3d = " + vertex3dFile);
            if ( color[0] && color[1] && color[2] )  vertexColor = true;
            Log.e("X3DDBG", "vertex color = " + vertexColor);


            try {
                float[] vertex = new float[3];
                for (int i = 0; i < elementVertex; i++) {
                    // get the vertices
                    vertex = utility.parseFixedLengthFloatString(line, 3, false, false);
                    if (i < 5)
                        Log.e("X3DDBG", "Face: line[" + i + "] " + vertex[0] + ", " + vertex[1] + ", " + vertex[2]);
                    else if (elementVertex - 5 < i)
                        Log.e("X3DDBG", "Face: line[" + i + "] " + vertex[0] + ", " + vertex[1] + ", " + vertex[2]);
                    mInputPositions.add(new Float(vertex[0]));
                    mInputPositions.add(new Float(vertex[1]));
                    mInputPositions.add(new Float(vertex[2]));
                    // get the colors
                    if ( vertexColor ) {
                        String[] colors = line.split(" ");
                        if (i < 5)
                            Log.e("X3DDBG", "   Colors " + colors[3] + ", " + colors[4] + ", " + colors[5]);
                        else if (elementVertex - 5 < i)
                            Log.e("X3DDBG", "   Colors: line[" + i + "] " + colors[3] + ", " + colors[4] + ", " + colors[5]);
                    }


                    line = buffer.readLine();
                }

                int[] face = new int[3];
                int faces = 0;
                for (int i = 0; i < elementFace; i++) {
                    faces = utility.parseIntegerString(line);
                    for (int j = 0; j < faces; j++) {
                        line = line.substring(line.indexOf(" ") + 1);
                        face[j] = utility.parseIntegerString(line);
                    }
                    if (i < 5)
                        Log.e("X3DDBG", "Vertex: line[" + i + "] " + face[0] + ", " + face[1] + ", " + face[2]);
                    else if (elementFace - 5 < i)
                        Log.e("X3DDBG", "Vertex: line[" + i + "] " + face[0] + ", " + face[1] + ", " + face[2]);
                    mPositionIndices.add(new Integer(face[0]));
                    mPositionIndices.add(new Integer(face[1]));
                    mPositionIndices.add(new Integer(face[2]));
                    line = buffer.readLine();
                }
            }
            catch (Exception e) {
                Log.e(TAG, "Problem with ply parsing vertices and faces " + e);
            }

        } catch (Exception exception) {

            Log.e(TAG, "PLY Parsing Exception = " + exception);
        }
        Log.e("X3DDBG", "End PLY parse");

    } // end Parse
}
