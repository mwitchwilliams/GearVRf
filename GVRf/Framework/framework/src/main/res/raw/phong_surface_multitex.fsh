
@MATERIAL_UNIFORMS

#ifdef HAS_ambientTexture
layout(location = 5) in vec2 ambient_coord;
#endif

#ifdef HAS_opacityTexture
layout(location = 9) in vec2 opacity_coord;
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

#ifdef HAS_lightMapTexture
layout(location = 8) in vec2 lightmap_coord;
#endif

layout(set = 0, binding = 6) uniform sampler2D ambientTexture;
layout(set = 0, binding = 5) uniform sampler2D diffuseTexture;
layout(set = 0, binding = 7) uniform sampler2D specularTexture;
layout(set = 0, binding = 8) uniform sampler2D opacityTexture;
layout(set = 0, binding = 9) uniform sampler2D lightmapTexture;
layout(set = 0, binding = 11) uniform sampler2D emissiveTexture;
layout(set = 0, binding = 10) uniform sampler2D normalTexture;

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

#ifdef HAS_ambientTexture
	ambient *= texture(ambientTexture, ambient_coord.xy);
#endif
#ifdef HAS_diffuseTexture
	diffuse *= texture(diffuseTexture, diffuse_coord.xy);
#endif
#ifdef HAS_opacityTexture
	diffuse.w *= texture(opacityTexture, opacity_coord.xy).a;
#endif
diffuse.xyz *= diffuse.w;
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

#ifdef HAS_lightMapTexture
	vec2 lcoord = (lightmap_coord * u_lightMap_scale) + u_lightMap_offset;
	diffuse *= texture(lightMapTexture, vec2(lcoord.x, 1 - lcoord.y));
	return Surface(viewspaceNormal, ambient, vec4(0.0, 0.0, 0.0, 0.0), specular, emission);
#else
	return Surface(viewspaceNormal, ambient, diffuse, specular, emission);
#endif
}
