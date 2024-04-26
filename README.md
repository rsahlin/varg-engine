# [WOLF] Vulkan Accelerated Render of GLTF  
  
Copyright Rickard Sahlin  
This project is licensed under the terms of the MIT license.  
  
This project is a continuation of:  
https://github.com/rsahlin/graphics-by-opengl  
https://github.com/rsahlin/gltf-viewer  

# PBR and Physically Correct Shading  
  
  
First some words about 'Physically Based Rendering' - or PBR for short.  
  
I think it is time to move past the PBR moniker.  
Sure, it's served us well for the past 20 odd years - with it's origin in the 'dark ages' where _everyone_ was using fudge values to get renderers to create somewhat plausible results.  
Along came PBR as a concept and instead of having arbitrary values we got to choose parameters (loosely) based on physical parameters.  
This was a huge improvement and a major leap for designers, modellers and material technicians - basically anyone using content creation tools to produce models or materials.  

However....  

With physical properties chosen mostly based on what made sense in editors, we ended up with parameters such as:  
Metalness, or dielectrics/insulators  
Gloss  
Albedo or basecolor  
Roughness  

All of these make sense when used an editor by someone that wants to create the 3D appearance of something realistically looking.   
But, come on - metalness???
That's not a good parameter to put at the core of your physically correct render - maybe if you are communicating with a welder or a mine operation - but not for calculating the light interactions of the material.  

The solution?  
Let's take a holistic look at how these models come to be.  

## 3D Workflow
  
To end up with something awesome rendered on the screen we need to support a 3D workflow:  

### Content Creation  
The creation of the 3D data - this encompasses everything done in different editor such as 3D modelling packages or image tools.  
Here it makes perfect sense to use simple to understand names for parameters - I would argue that this is where most of the influence on PBR comes from.  
This is everything from material creation to modelling of 3D topology.  

### The Dataset 

This is the data that is saved/exported or converted from the content creation step.  
The dataset absolutely must be firmly based in the physics of light, not electricity or metallurgy.  
This should be totally removed from both the previous, content creation step, and the following render step.  
With current PBR proposals I would say that this step is somewhat broken, it's deviated from the light/surface interactions that it must model.  

### Rendering  

The final step, or at least the almost final step - before post-processing - taking the dataset and calculating an output that is physically correct.  
To do this correct we must take a step back and look at the single most important event in all light calculations:  
What happes when light travels between different media - in our case - as the light 'hits' the surface defined by the dataset.  
This has been known for over 100 years and is given by the Fresnel equations - the fulcrum of physically correct shading.    
  
  
So, let's use the following  
-IOR - with angle of incidence we get reflection and transmission.  
-Absorption factor - amount of transmitted light that is absorbed in the material.  
-Reflection color - how the material colors the reflected light.  
-Transmission color - how the material colors the transmitted light.  
-Surface dispersion - how the material disperses light on a micro-surface level. In my opinion this is an anomaly.  
It is needed for some cases of roughness, though I really dislike the roughness property - we are not _really_ modelling the surface roughness per se.  
What we are modelling is a theoretic model of how light is scattered by a micro-surface geometry.  
Using this model light is broken down into fractions - maybe somewhat plausible for some cases of 'diffuse' refraction - the incoming light is scaled [0.0 - 1.0] based on roughness.  
Clearly not what happens to reflected light - this issue is also connected to the omission of lightsource solid angle - for instance in the glTF datamodel.  
  
  
To put this into action we need to adress the last 2 stages of the 3D Workflow - namely the dataset and light shading.  

### Media (Surface) Light Property Dataset  
  
This is the dataset for the media properties when it comes to light calculations.  
In short - physical properties that model how light interacts with the surface and inside the media.    

### Fresnel Based Light Shading  
  
Physically correct light shading using the Media Light Property Dataset.  
  
**The first rule of Fresnel Based Light Shading**  
  
The first rule of Fresnel Based Light Shading is to start with the IOR and angle of incidence to calculate the Fresnel power function.  
Use the media reflection color for the reflective part and the media transmission color for re-emitted transmission.  
How much of the transmitted light that may be re-emitted is govererned by the absorption factor (for metals this value is 1).  
Transmitted light is affected by surface dispersion factor:  
-0 means no dispersion.  
-1 means fully dispersed over 2PI.  
Transmission intensity goes from 1 down to 1 / 2PI as dispersion goes up.  
Irradiance Map (Spherical Harmonics) is calculated in same way as the transmissive light.  
Environment map reflections use first rule of FBLS.  
  
**The second rule of Fresnel Based Light Shading**    
  
The second rule of Fresnel Based Light Shading is to never, and I really mean never, bake or combine factors affecting light distribution into colors.  
While this may seem like an optimization at first glance it prohibits proper light calculations.  
Examples of this is how glTF handles metals - the ior cannot be set so the reflective power is baked into the material color.  
This will give inconsistent result compared to having the metallic ior and color.   
  
Another example is glTF sheen extension - here there is no factor to specify amount of light that interacts with the perpendicular fibres.  
It's baked into the sheen color - this will also give inconsistent results, the factor is needed to calculate amount of light that proceeds to interact with the base material.  
  
# The VARG - Engine - A Fresnel Based Light Shader   
  
VARG is written in Java.  
To interface with Vulkan, which is a C based API, LWJGL3 is used.  
The dependencies are minimal - only GLFW and Vulkan packages are used, plus the needed Native libraries of course.  
The communication with LWJGL3 is separated into it's own module.  
Currently Windows and Linux platforms are supported - VARG can run on any platform that LWJGL3 [GLFW and Vulkan] can be built for.  
  
VARG supports compute and graphics pipelines, geometry is streamlined and made into drawcalls before being executed by the renderer.  
Physically based rendering is supported using Fresnel Based Light Shading.  
    
  
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
  
  
