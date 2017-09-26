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


/***************************************************************************
 * GL program for rendering a object with an error.
 ***************************************************************************/

#ifndef SOLID_COLOR_SHADER_H_
#define SOLID_COLOR_SHADER_H_

#include "shaderbase.h"

namespace gvr {
class Color;
class GLProgram;
class RenderData;

class ErrorShader: public ShaderBase {
public:
    ErrorShader();
    virtual ~ErrorShader();

    virtual void render(RenderState* rstate, RenderData* render_data, Material* material);

private:
    ErrorShader(const ErrorShader& error_shader);
    ErrorShader(ErrorShader&& error_shader);
    ErrorShader& operator=(const ErrorShader& error_shader);
    ErrorShader& operator=(ErrorShader&& error_shader);

private:
    GLProgram* program_;
    GLint u_mvp_;
    GLint u_color_;
};

}
#endif
