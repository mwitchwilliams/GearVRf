@MATERIAL_UNIFORMS

layout ( set = 0, binding = 10 ) uniform sampler2D diffuseTexture;

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

#ifdef HAS_diffuseTexture
#ifdef HAS_LIGHTSOURCES
	diffuse *= texture(diffuseTexture, diffuse_coord.xy);
    diffuse.xyz *= diffuse.w;
#else
    diffuse = vec4(0, 0, 0, 1);
    specular = vec4(0, 0, 0, 1);
    ambient = vec4(0, 0, 0, 1);
	emission *= texture(diffuseTexture, diffuse_coord.xy);
    emission.xyz *= emission.w;
#endif
#endif
	viewspaceNormal = viewspace_normal;
	return Surface(viewspaceNormal, ambient, diffuse, specular, emission);
}
