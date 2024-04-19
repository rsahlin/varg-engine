# [WOLF] Vulkan Accelerated Render of GLTF  
  
Copyright Rickard Sahlin  
This project is licensed under the terms of the MIT license.  
  
This project is a continuation of:  
https://github.com/rsahlin/graphics-by-opengl  
https://github.com/rsahlin/gltf-viewer  
  
  
## VARG - Engine  
  
VARG is written in Java.  
To interface with Vulkan, which is a C based API, LWJGL3 is used.  
The dependencies are minimal - only GLFW and Vulkan packages are used, plus the needed Native libraries of course.  
The communication with LWJGL3 is separated into it's own module.  
Currently Windows and Linux platforms are supported - VARG can run on any platform that LWJGL3 [GLFW and Vulkan] can be built for.  
  
VARG supports compute and graphics pipelines, geometry is streamlined and made into drawcalls before being executed by the renderer.  
Physically based rendering is supported using a custom BRDF - most importantly the BRDF uses Fresnel power equations for all light/material interactions.  
It's based on the metal/roughness parameters of glTF.  
  
SPIR-V compilation is currently done on target, using the platform command 'glslc'.  
Hash values are used to differentiate shader permutations.  
Shaders can be pre-built if wanted.  

<table>
  <tr>
    <td> <img src="https://github.com/rsahlin/varg-engine/assets/3063192/63367478-b1a9-44a9-bd41-d079b6cf5900"  alt="glTF Corset model" width = 360px></td>
    <td> <img src="https://github.com/rsahlin/varg-engine/assets/3063192/48054c8a-2f4b-45b0-8d10-5d9cb43997cc"  alt="glTF Flight Helmet model" width = 360px></td>
    <td> <img src="https://github.com/rsahlin/varg-engine/assets/3063192/b68fcafd-89e2-4207-9a88-1e74174531dd"  alt="glTF Waterbottle model " width = 360px></td>
  </tr>
</table>

### Features  
  
Supports PBR based on metallic/roughness parameters, fresnel power equation using Schlick approximation. Point light support.  
Irradiance Map through spherical harmonics, these can be displayed as background.  
Environment map, textured stored using KTX2 cubemap format - environment map can be displayed as background.  
Fragment shading rate can be set on background and/or geometry.  
Textures are stored in bundles (2D texture array) with same size and texture modes, shader usage of samplers is greatly reduced for most models.  
MipMaps created on GPU.  
  
glTF models are loaded using gltf-io.  
  
## Drawcall optimization  
  
Geometry data, textures and uniform data is pre-processed to create an optimized set of drawcalls.  
Below are two examples of this:  
  
10 000 asteroids - a model with 10 000 nodes, meshes and materials - normally 10 000 drawcalls.    
The glTF Sponza model which usually renders with some 100 drawcalls (check PlayCanvas/Babylon etc)  
    
<table>
  <tr>
    <td> <img src="https://github.com/rsahlin/varg-engine/assets/3063192/3839ca6e-90ad-4306-8abd-a0fe0233cedc"  alt="Example of drawcall optimization - 1 drawcall" height = 360px></td>
    <td> <img src="https://github.com/rsahlin/varg-engine/assets/3063192/6dece0d1-3bdb-4c30-9120-45896140db76"  alt="Example of drawcall optimization - 3 drawcalls" width = 360px></td>
  </tr>
</table>
  
  
## Framegrabber  
  
Load any number of glTF models - render a given set of camera angles and save the output in PNG or JPG format.  
The framebuffer content will be fetched after render of one frame.  
This means that the saved image will contain what you actually see on the screen, if using multisamplebuffer the output will be the resolved buffer.  
Pixel data is converted to 8 bit RGB and saved either as PNG or JPEG.  
Any number of camera angles can be configured, once these are output the grabber will move on to the next glTF in the list.  


<table>
  <tr>
    <td> <img src="https://github.com/rsahlin/varg-engine/assets/3063192/91ac4821-201c-4dd7-9da6-dd6c3a648dd0"  alt="Framegrabber output using camera Y axis rotation" width = 250px></td>
    <td> <img src="https://github.com/rsahlin/varg-engine/assets/3063192/df036968-4fdb-4918-9a35-99a01a901cd1"  alt="Framegrabber output using camera Y axis rotation" width = 250px></td>
    <td> <img src="https://github.com/rsahlin/varg-engine/assets/3063192/4c08c942-afd3-4e21-82ff-63008e246980"  alt="Framegrabber output using camera Y axis rotation" width = 250px></td>
    <td> <img src="https://github.com/rsahlin/varg-engine/assets/3063192/f2c0815c-0969-4fb4-873b-7e04981522a4"  alt="Framegrabber output using camera Y axis rotation" width = 250px></td>
    <td> <img src="https://github.com/rsahlin/varg-engine/assets/3063192/6fadd0b6-8dae-46bd-87b5-60d019ce7a37"  alt="Framegrabber output using camera Y axis rotation" width = 250px></td>
    <td> <img src="https://github.com/rsahlin/varg-engine/assets/3063192/b31235ed-0491-420c-aa00-6526a28f367a"  alt="Framegrabber output using camera Y axis rotation" width = 250px></td>
    <td> <img src="https://github.com/rsahlin/varg-engine/assets/3063192/3251f5ca-567b-49df-87d2-a792a25b75a5"  alt="Framegrabber output using camera Y axis rotation" width = 250px></td>
    <td> <img src="https://github.com/rsahlin/varg-engine/assets/3063192/e3a17ade-4a00-4c29-a64d-c4eec2aa2841"  alt="Framegrabber output using camera Y axis rotation" width = 250px></td>
    <td> <img src="https://github.com/rsahlin/varg-engine/assets/3063192/faafcc72-e048-4301-9f4c-22ec7ce04c32"  alt="Framegrabber output using camera Y axis rotation" width = 250px></td>
    <td> <img src="https://github.com/rsahlin/varg-engine/assets/3063192/cdf01a59-c970-4554-affd-719de974b70f"  alt="Framegrabber output using camera Y axis rotation" width = 250px></td>
  </tr>
</table>
  
  
  
## Mesh shader experiment using voxels:  

Support for mesh shaders, this is a test implementation with a task shader that assembles the shape of the face.  
Mesh shader outputs colored voxels that are rendered together with a glTF model.  
  
1.3 million voxels together with a glTF model  
<table>
  <tr>
    <td> <img src="https://github.com/rsahlin/varg-engine/assets/3063192/7d185e34-49b5-4c89-95e7-b68323a3ece4"  alt="1.3 million voxels" width = 360px></td>
  </tr>
</table>
  
  
### Incomplete todo list, in no particular order:  
  
* Use LWJGL3 to compile shaders to get rid of platform dependency to glslc.  
* Release APK with working viewer, for instance on Windows and Linux.  
* Add suport for glTF BASISU compressed textures.  
* Add support for glTF clearcoat extension.  
* Implement animations.  
* Sort alpha/transmission triangles.
  
  
Some notable design changes - the project is now split up into several parts:  
The glTF io abstraction (gltf-io) which handles loading of glTF and mapping of JSON to java.  
Image io and KTX (gltf-imageio) - adds support for image/textures using KTX  
Vulkan rendering (varg-engine) - this takes care of Vulkan rendering  
  
Engine is likely to be continously work in progress with more features and cool shaders implemented in the future.  
  
### Build instructions  
Platform dependencies to Vulkan validation layers and glslc  
  
This project uses maven to build.  
In order to build, some project dependencies needs to be cloned and built beforehand.  
To be sure of project dependencies - check the pom.  
  
gltf-io - build using 'mvn clean compile install -DskipTests'  
gltf-imageio - build using 'mvn clean compile install -DskipTests'  
  
build varg-engine using 'mvn clean compile install -DskipTests'  
  
  
