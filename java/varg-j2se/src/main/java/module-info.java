module vargj2se {

    requires transitive gltfio;
    requires transitive ktximageio;
    requires transitive gson;
    requires org.eclipse.jdt.annotation;
    requires java.desktop;

    exports org.varg;
    exports org.varg.shader.voxels;
    exports org.varg.window;
    exports org.varg.gltf;
    exports org.varg.pipeline;
    exports org.varg.renderer;
    exports org.varg.scene;
    exports org.varg.shader;
    exports org.varg.vulkan;
    exports org.varg.vulkan.extensions;
    exports org.varg.vulkan.structs;
    exports org.varg.vulkan.image;
    exports org.varg.uniform;
    exports org.varg.vulkan.memory;
    exports org.varg.vulkan.descriptor;
    exports org.varg.assets;
    exports org.varg.vulkan.framebuffer;
    exports org.varg.vulkan.pipeline;
    exports org.varg.vulkan.renderpass;
    exports org.varg.vulkan.vertex;
    exports org.varg.vulkan.cmd;

    opens org.varg.gltf to gson;
    opens org.varg.vulkan.vertex to gson;

}
