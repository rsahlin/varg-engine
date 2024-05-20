This is to test that Fresnell light reflection, not transmission, is calculated correctly.  
Assuming light is moving from a media with an IOR of 1.0 (air) to an IOR of 1.5 - this is the default of glTF.  
Reflected light is calculated at normal incidence:  
R0 = ( (IOR1 - IOR2) / (IOR1 + IOR2) ) ^2 = 0.04  
  
To test this a model "fresnelReflectionTest_09.glb" using one directional light facing directly into the screen at intensity 0.9 lumen/m2 is used.  
In glTF the illumination from a directional light is spread out evenly in all directions.  
  
This light hits a material that has roughness 0.0 and an albedo/basecolor of 0.0 - thus no light will be re-transmitted or diffused.  
There are 6 black material quads, facing the camera, divided on the topmost two rows, background color is set to dark green so that the quads can be seen.  
Assuming an SNORM framebuffer [0.0 - 1.0] and no exposure, or 1.0, since the camera in glTF does not have aperture, shutter, ISO - it's simply a viewport.  
  
The lowest row is for comparison, they are emissive, black metal - the only light leaving will be the emissive light.  
The emissive light is, going from left to right [0.9 * 0.04] - [0.9] - [1-0]  
To be rendered physically correct the reflected light from the black material quads shall be the same as the leftmost emissive material (sRGB 0x353535).  
  
The second model is the same but with a higher intensity light (20 lumen/m2) which will give 80% of max light at normal incidence (sRGB 0xE7E7E7)  

Note that in order for render to be physically correct the light intensity of reflected light, in linear space, must be limited to:  
"fresnelReflectionTest_09.glb" : (0.9 * 0.04) = 0.036  
"fresnelReflectionTest_20.glb" : (20.0 * 0.04) = 0.8  

