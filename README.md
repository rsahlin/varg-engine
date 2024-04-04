# [WOLF] Vulkan Accelerated Render of GLTF  

Copyright Rickard Sahlin  
This project is licensed under the terms of the MIT license.  

This project is a continuation of https://github.com/rsahlin/graphics-by-opengl  

Some notable design changes - the project is now split up into several parts:  
The glTF io abstraction (gltf-io) which handles loading of glTF and mapping of JSON to java.  
Image io and KTX (gltf-imageio) - adds support for image/textures using KTX  
Vulkan rendering (varg-engine) - this takes care of Vulkan rendering    


# [WOLF] VARG  

VARG is written in Java (not Javascript) a modern and object oriented programming language.  
In order to interface with Vulkan, which is a C based API, LWJGL3 is used.  
The communication with LWJGL3 is separated into it's own module.  
VARG supports compute and graphics pipelines, geometry is streamlined and made into drawcalls before being executed by the renderer.  
Physically based rendering is supported using a custom BRDF - most importantly the BRDF uses Fresnel power equations for all light/material interactions.  
It's based on the metal/roughness parameters of glTF. 

## Build instructions

This project uses maven to build.  
In order to build, some project dependencies needs to be cloned and built beforehand.  
To be sure of project dependencies- check the pom.  

gltf-io
gltf-imageio  

