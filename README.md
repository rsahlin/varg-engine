[WOLF] Vulkan Accelerated Render of GLTF
Copyright Rickard Sahlin
This project is licensed under the terms of the MIT license.

This project is a continuation of:
https://github.com/rsahlin/graphics-by-opengl
https://github.com/rsahlin/gltf-viewer

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

glTF Corset model	glTF Flight Helmet model	glTF Waterbottle model
Features
Supports PBR based on metallic/roughness parameters, fresnel power equation using Schlick approximation. Point light support.
Irradiance Map through spherical harmonics, these can be displayed as background.
Environment map, textured stored using KTX2 cubemap format - environment map can be displayed as background.
Fragment shading rate can be set on background and/or geometry.
Textures are stored in bundles (2D texture array) with same size and texture modes, shader usage of samplers is greatly reduced for most models.
MipMaps created on GPU.

glTF models are loaded using gltf-io.
Geometry data, textures and uniform data is pre-processed to create an optimized set of drawcalls.
Below is the glTF Sponza model which usually renders with some 100 drawcalls (check PlayCanvas/Babylon etc)
Here it is rendered in 3 drawcalls:

Example of drawcall optimization - 3 drawcalls
Mesh shader experiment using voxels:
Support for mesh shaders, this is a test implementation with a task shader that assembles the shape of the face.
Mesh shader outputs colored voxels that are rendered together with a glTF model.

1.3 million voxels
Incomplete todo list, in no particular order:

Use LWJGL3 to compile shaders to get rid of platform dependency to glslc.
Release APK with working viewer, for instance on Windows and Linux.
Add suport for glTF BASISU compressed textures.
Add support for glTF clearcoat extension.
Implement animations.
Sort alpha/transmission triangles.
Some notable design changes - the project is now split up into several parts:
The glTF io abstraction (gltf-io) which handles loading of glTF and mapping of JSON to java.
Image io and KTX (gltf-imageio) - adds support for image/textures using KTX
Vulkan rendering (varg-engine) - this takes care of Vulkan rendering

Engine is likely to be continously work in progress with more features and cool shaders implemented in the future.

Build instructions
Platform dependencies to Vulkan validation layers and glslc

This project uses maven to build.
In order to build, some project dependencies needs to be cloned and built beforehand.
To be sure of project dependencies - check the pom.

gltf-io
gltf-imageio