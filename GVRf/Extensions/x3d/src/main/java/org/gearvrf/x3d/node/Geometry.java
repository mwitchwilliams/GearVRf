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

import org.gearvrf.utility.Log;

public class Geometry extends X3DGeometryNode
{

    private static final String TAG = Geometry.class.getSimpleName();

    private Box box = null;
    private Cone cone = null;
    private Cylinder cylinder = null;
    private Sphere sphere = null;

    //private IndexedFaceSet indexedFaceSet = null;
    //private Text text = null;

    public Geometry() {
    }

    public Geometry(String _DEF) {
        setDEF(_DEF);
    }

    /**
     * Provide Box.
     * @param newValue
     */
    public Box getBox() {
        return box;
    }

    /**
     * Provide Cone.
     * @param newValue
     */
    public Cone getCone() {
        return cone;
    }

    /**
     * Provide Cylinder.
     * @param newValue
     */
    public Cylinder getCylinder() {
        return cylinder;
    }

    /**
     * Provide Sphere.
     * @param newValue
     */
    public Sphere getSphere() {
        return sphere;
    }

    /**
     * Assign String value to inputOutput SFString field named DEF.
     * @param newValue
     */
    public void setDEF(String newValue) {
        super.setDEF(newValue);
    }

    /**
     * Assign String value to inputOutput SFString field named USE.
     * @param newValue
     */
    public void setUSE(String newValue) {
        super.setUSE(newValue);
    }

    /**
     * Assign X3DBoxNode instance (using a properly typed node) to inputOutput SFNode field material.
     */
    public void setBox(Box newValue) {
        box = newValue;
    }

    /**
     * Assign X3DConeNode instance (using a properly typed node) to inputOutput SFNode field material.
     */
    public void setCone(Cone newValue) {
        cone = newValue;
    }

    /**
     * Assign X3DCylinderNode instance (using a properly typed node) to inputOutput SFNode field material.
     */
    public void setCylinder(Cylinder newValue) {
        cylinder = newValue;
    }

    /**
     * Assign X3DSphereNode instance (using a properly typed node) to inputOutput SFNode field material.
     */
    public void setSphere(Sphere newValue) {
        sphere = newValue;
    }

} // end Geometry
