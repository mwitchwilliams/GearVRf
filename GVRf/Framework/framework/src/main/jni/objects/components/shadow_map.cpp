/* Copyright 2015 Samsung Electronics Co., LTD
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
#include "shadow_map.h"
#include "gl/gl_render_texture.h"

namespace gvr {
class Renderer;
    ShadowMap::ShadowMap(ShaderData* mtl)
            : RenderTarget((RenderTexture*)nullptr, false),
              mLayerIndex(-1),
              mShadowMaterial(mtl)
    {

    }

    void ShadowMap::setLayerIndex(int layerIndex)
    {
        mLayerIndex = layerIndex;

        if (mRenderTexture)
        {
            LOGV("ShadowMap::setLayerIndex %d", layerIndex);
            mRenderTexture->setLayerIndex(mLayerIndex);
        }
    }


    void  ShadowMap::beginRendering(Renderer* renderer)
    {
        RenderTarget::beginRendering(renderer);
        mRenderState.render_mask = 1;
        mRenderState.is_shadow = true;
        mRenderState.shadow_map = nullptr;
        mRenderState.material_override = mShadowMaterial;
        LOGV("ShadowMap::beginRendering %s", mRenderState.material_override->getUniformDescriptor());
    }

}