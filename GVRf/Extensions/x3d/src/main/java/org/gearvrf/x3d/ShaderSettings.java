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

package org.gearvrf.x3d;

import java.util.concurrent.Future;

import org.gearvrf.GVRMaterial;


import org.gearvrf.GVRTexture;
import org.gearvrf.x3d.data_types.MFString;
import org.gearvrf.x3d.data_types.SFFloat;
import org.gearvrf.x3d.data_types.SFVec2f;
import org.joml.Matrix3f;

/**
 * 
 * @author m1.williams
 * ShaderSettings retain Material values and Textures for X3D
 * This sets shader values in GearVRf's Vertex and Fragment
 * Shader.
 * Can also be set to overwrite or construct X3D programmatically
 * the shader values while still using GearVR's internal Shader.
 */


public class ShaderSettings
{
  private String nameAppearance = ""; // set if there is a DEF in Appearance
                                      // node
  private String nameMaterial = ""; // set if there is a DEF in Material node
  private String nameTextureTransform = ""; // set if there is a DEF in
  // TextureTransform node

  // Multi-Texture variables
  //TODO: just an initial version of MultiTexture.
  private boolean multiTexture = false; // set true if multi-texturing
  private String nameMultiTexture = ""; // set if there is a DEF in
  // MultiTexture node
  private MFString multiTexture_mode = new MFString(); // can be "Add", etc.
  private GVRTexture textureMap0 = null;
  private GVRTexture textureMap1 = null;


  private SFVec2f textureCenter = new SFVec2f( 0, 0 );
  private SFVec2f textureScale =  new SFVec2f( 1, 1 );
  private SFVec2f textureTranslation = new SFVec2f( 0, 0 );
  private SFFloat textureRotation = new SFFloat(0);

  public Matrix3f textureMatrix = null;
  public float ambientIntensity = 0.2f;
  public float[] diffuseColor =
  {
      0.8f, 0.8f, 0.8f
  };
  public float[] emissiveColor =
  {
      0, 0, 0
  };
  public float shininess = 0.2f;
  public float[] specularColor =
  {
      0, 0, 0
  };
  private float transparency = 0;

  public float[] modelMatrix = new float[16];


  public GVRTexture texture = null;

  public String fragmentShaderLights = "";
  
  public GVRMaterial material;

  public void initializeTextureMaterial(GVRMaterial m)
  {
	material = m;
    nameAppearance = ""; // set if there is a DEF in Appearance node
    nameMaterial = ""; // set if there is a DEF in Material node
    nameTextureTransform = ""; // set if there is a DEF in TextureTransform
    // node

    // initialize texture values
    textureCenter.setValue( 0, 0 );
    textureScale.setValue( 1, 1 );
    textureTranslation.setValue( 0, 0 );
    textureRotation.setValue(0);

    // initialize X3D Material values
    for (int i = 0; i < 3; i++)
    {
      diffuseColor[i] = 0.8f;
      emissiveColor[i] = 0;
      specularColor[i] = 0;
    }
    ambientIntensity = 0.2f;
    shininess = 0.2f;
    transparency = 0;

    // modelMatrix set to identity matrix
    for (int i = 0; i < 4; i++)
    {
      for (int j = 0; j < 4; j++)
      {
        modelMatrix[i * 4 + j] = 0;
        if (i == j)
          modelMatrix[i * 4 + j] = 1;
      }
    }

    multiTexture = false; // set true if multi-texturing
    nameMultiTexture = ""; // set if there is a DEF in
    // MultiTexture node
    multiTexture_mode.clear();
    textureMap0 = null;
    textureMap1 = null;

    texture = null;
    textureMatrix = null;
  }



  public ShaderSettings(GVRMaterial material)
  {
    initializeTextureMaterial(material);
  }

  public void appendFragmentShaderLights(String lightString)
  {
    fragmentShaderLights += lightString;
  }

  public void setAppearanceName(String name)
  {
    this.nameAppearance = name;
  }

  public String getAppearanceName()
  {
    return this.nameAppearance;
  }

  public void setMaterialName(String name)
  {
    this.nameMaterial = name;
  }

  public String getMaterialName()
  {
    return this.nameMaterial;
  }

  public void setTextureTransformName(String name)
  {
    this.nameTextureTransform = name;
  }

  public String getTextureTransformName()
  {
    return this.nameTextureTransform;
  }

  public void setDiffuseColor(float[] diffuseColor)
  {
    for (int i = 0; i < 3; i++)
    {
      this.diffuseColor[i] = diffuseColor[i];
    }
  }

  public void setEmmissiveColor(float[] emissiveColor)
  {
    for (int i = 0; i < 3; i++)
    {
      this.emissiveColor[i] = emissiveColor[i];
    }
  }

  public void setSpecularColor(float[] specularColor)
  {
    for (int i = 0; i < 3; i++)
    {
      this.specularColor[i] = specularColor[i];
    }
  }

  public void setAmbientIntensity(float ambientIntensity)
  {
    this.ambientIntensity = ambientIntensity;
  }

  public void setShininess(float shininess)
  {
    this.shininess = shininess;
  }

  public void setTransparency(float transparency)
  {
    this.transparency = transparency;
  }

  public float getTransparency()
  {
    return this.transparency;
  }

  public void setTexture(GVRTexture texture)
  {
    this.texture = texture;
    this.material.setTexture("diffuseTexture", texture);
  }

    protected void setTextureCenter(float[] textureCenter)
      {
        this.textureCenter.setValue(textureCenter[0], textureCenter[1]);
      }
    protected SFVec2f getTextureCenter()
    {
        return this.textureCenter;
    }

    protected void setTextureScale(float[] textureScale)
      {
        this.textureScale.setValue(textureScale[0], textureScale[1]);
      }
    protected SFVec2f getTextureScale()
    {
        return this.textureScale;
    }

    protected void setTextureRotation(float textureRotation) {
      this.textureRotation.setValue( textureRotation );
    }
    protected SFFloat getTextureRotation()
    {
        return this.textureRotation;
    }

    protected void setTextureTranslation(float[] textureTranslation)
    {
      this.textureTranslation.setValue(textureTranslation[0], textureTranslation[1]);
    }
    protected SFVec2f getTextureTranslation()
  {
      return this.textureTranslation;
  }

  protected void setMultiTexture(boolean multiTextureBoolean)
  {
    this.multiTexture = multiTextureBoolean;
  }
  protected boolean getMultiTexture()
  {
    return this.multiTexture;
  }
  protected void setMultiTextureName(String multiTextureString)
  {
    this.nameMultiTexture = multiTextureString;
  }
  protected String getMultiTextureName()
  {
    return this.nameMultiTexture;
  }
  protected void setMultiTextureMode(MFString mode)
  {
    this.multiTexture_mode = mode;
  }
  protected MFString getMultiTextureMode()
  {
    return this.multiTexture_mode;
  }
  protected void setMultiTextureGVRTexture(GVRTexture textureMap)
  {
    if (this.textureMap0 != null) this.textureMap1 = textureMap;
    else this.textureMap0 = textureMap;
  }
  protected void setMultiTextureGVRTexture(GVRTexture textureMap, int number)
  {
    if (number == 1) this.textureMap1 = textureMap;
    else this.textureMap0 = textureMap;
  }
  protected GVRTexture getMultiTextureGVRTexture(int number)
  {
    if (number == 1) return this.textureMap1;
    return this.textureMap0;
  }

}
