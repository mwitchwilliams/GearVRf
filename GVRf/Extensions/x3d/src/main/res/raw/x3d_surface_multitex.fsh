
@MATERIAL_UNIFORMS

#ifdef HAS_ambientTexture
layout(location = 5) in vec2 ambient_coord;
#endif

#ifdef HAS_specularTexture
layout(location = 6) in vec2 specular_coord;
#endif

#ifdef HAS_emissiveTexture
layout(location = 7) in vec2 emissive_coord;
#endif

#ifdef HAS_normalTexture
layout(location = 10) in vec2 normal_coord;
#endif

layout(set = 0, binding = 5) uniform sampler2D ambientTexture;
layout(set = 0, binding = 4) uniform sampler2D diffuseTexture;
layout(set = 0, binding = 6) uniform sampler2D specularTexture;
layout(set = 0, binding = 9) uniform sampler2D normalTexture;
layout(set = 0, binding = 10) uniform sampler2D emissiveTexture;

struct Surface
{
   vec3 viewspaceNormal;
   vec4 ambient;
   vec4 diffuse;
   vec4 specular;
   vec4 emission;
};

Surface @ShaderName()
{
	vec4 diffuse = diffuse_color;
	vec4 emission = emissive_color;
	vec4 specular = specular_color;
	vec4 ambient = ambient_color;
	vec3 viewspaceNormal;
	vec4 temp;

#ifndef HAS_LIGHTSOURCES
    diffuse = emission;
#endif
#ifdef HAS_diffuseTexture
	diffuse *= texture(diffuseTexture, diffuse_coord.xy);
#endif
diffuse.xyz *= diffuse.w;
#ifdef HAS_LIGHTSOURCES
    #ifdef HAS_ambientTexture
        ambient *= texture(ambientTexture, ambient_coord.xy);
    #endif
    #ifdef HAS_specularTexture
        specular *= texture(specularTexture, specular_coord.xy);
    #endif
    #ifdef HAS_emissiveTexture
        emission = texture(emissiveTexture, emissive_coord.xy);
    #endif
    #ifdef HAS_normalTexture
        viewspaceNormal = texture(normalTexture, normal_coord.xy).xyz * 2.0 - 1.0;
    #else
        viewspaceNormal = viewspace_normal;
    #endif
#endif
	return Surface(viewspaceNormal, ambient, diffuse, specular, emission);
}
