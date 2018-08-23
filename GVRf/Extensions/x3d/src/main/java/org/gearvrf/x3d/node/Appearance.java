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

public class Appearance extends X3DNode
{

    private static final String TAG = Appearance.class.getSimpleName();

    private Material material = null;
    //private Texture texture = null;
    //private TextureTransform textureTransform = null;

    public Appearance() {
    }

    public Appearance(String _DEF) {
        setDEF(_DEF);
    }

    /**
     * Provide X3DMaterialNode instance (using a properly typed node) from inputOutput SFNode field material.
     * @param newValue
     */
    public Material getMaterial() {
        return material;
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
     * Assign X3DMaterialNode instance (using a properly typed node) to inputOutput SFNode field material.
     */
    public void setMaterial(Material newValue) {
        material = newValue;
    }


    //TODO: the following methods are not implemented
    /*
    addShaders(X3DNode[] newValue)
    getFillProperties()
    getLineProperties()
    getMetadata()
    getShaders()
    getTexture()
    getTextureTransform()
    setCssClass(java.lang.String newValue)
    setFillProperties(FillProperties newValue)
    setLineProperties(LineProperties newValue)
    setMetadata(X3DMetadataObject newValue)
    void  setShaders(X3DNode newValue)
    Appearance  setShaders(X3DNode[] newValue)
    Appearance  setTexture(X3DTextureNode newValue)
    Appearance  setTextureTransform(X3DTextureTransformNode newValue)
    */
} // end Appearance
