package me.jellysquid.mods.sodium.client.render.chunk.shader;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.hanetzer.chlorine.common.config.Config;
import me.jellysquid.mods.sodium.client.gl.SodiumVertexFormats.ChunkMeshAttribute;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexFormat;
import me.jellysquid.mods.sodium.client.gl.shader.*;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkGraphicsState;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderBackend;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.passes.WorldRenderPhase;
import net.minecraft.util.ResourceLocation;

import java.util.EnumMap;

public abstract class ChunkRenderShaderBackend<T extends ChunkGraphicsState, P extends ChunkProgram>
        implements ChunkRenderBackend<T> {
    private final EnumMap<ChunkFogMode, EnumMap<WorldRenderPhase, P>> programs = new EnumMap<>(ChunkFogMode.class);

    protected final GlVertexFormat<ChunkMeshAttribute> vertexFormat;

    protected P activeProgram;

    public ChunkRenderShaderBackend(GlVertexFormat<ChunkMeshAttribute> format) {
        this.vertexFormat = format;
    }

    @Override
    public final void createShaders() {
        for (ChunkFogMode fogMode : ChunkFogMode.values()) {
            EnumMap<WorldRenderPhase, P> mapForFogMode = new EnumMap<>(WorldRenderPhase.class);
            for (WorldRenderPhase phase : WorldRenderPhase.values()) {
                mapForFogMode.put(phase, this.createShader(fogMode, phase, this.vertexFormat));
            }
            this.programs.put(fogMode, mapForFogMode);
        }
    }

    private P createShader(ChunkFogMode fogMode, WorldRenderPhase phase, GlVertexFormat<ChunkMeshAttribute> format) {
        GlShader vertShader = this.createVertexShader(fogMode, phase);
        GlShader fragShader = this.createFragmentShader(fogMode, phase);

        ChunkProgramComponentBuilder components = new ChunkProgramComponentBuilder();
        components.fog = fogMode.getFactory();

        try {
            GlProgram.Builder builder = GlProgram.builder(new ResourceLocation("sodium", "chunk_shader"));
            builder.attachShader(vertShader);
            builder.attachShader(fragShader);

            builder.bindAttribute("a_Pos", format.getAttribute(ChunkMeshAttribute.POSITION));
            builder.bindAttribute("a_Color", format.getAttribute(ChunkMeshAttribute.COLOR));
            builder.bindAttribute("a_TexCoord", format.getAttribute(ChunkMeshAttribute.TEXTURE));
            builder.bindAttribute("a_LightCoord", format.getAttribute(ChunkMeshAttribute.LIGHT));

            this.modifyProgram(builder, components, format);

            return builder.build((program, name) -> this.createShaderProgram(program, name, components));
        } finally {
            vertShader.delete();
            fragShader.delete();
        }
    }

    protected abstract void modifyProgram(GlProgram.Builder builder, ChunkProgramComponentBuilder components,
                                          GlVertexFormat<ChunkMeshAttribute> format);

    private GlShader createVertexShader(ChunkFogMode fogMode, WorldRenderPhase phase) {
        return ShaderLoader.loadShader(ShaderType.VERTEX, new ResourceLocation("sodium", "chunk_glsl110.v.glsl"),
                this.createShaderConstants(fogMode, phase));
    }

    private GlShader createFragmentShader(ChunkFogMode fogMode, WorldRenderPhase phase) {
        return ShaderLoader.loadShader(ShaderType.FRAGMENT, new ResourceLocation("sodium", "chunk_glsl110.f.glsl"),
                this.createShaderConstants(fogMode, phase));
    }

    private ShaderConstants createShaderConstants(ChunkFogMode fogMode, WorldRenderPhase phase) {
        ShaderConstants.Builder builder = ShaderConstants.builder();
        fogMode.addConstants(builder);
        if (Config.CLIENT.useCompactVertexFormat.get()) {
            builder.define("COMPACT_VERTICES");
        }
        if (phase == WorldRenderPhase.OPAQUE) {
            builder.define("OPAQUE");
        }

        this.addShaderConstants(builder);

        return builder.build();
    }

    protected abstract void addShaderConstants(ShaderConstants.Builder builder);

    protected abstract P createShaderProgram(ResourceLocation name, int handle, ChunkProgramComponentBuilder components);

    protected void beginRender(MatrixStack matrixStack, WorldRenderPhase phase, BlockRenderPass pass) {
        this.activeProgram = this.programs.get(ChunkFogMode.getActiveMode()).get(phase);
        this.activeProgram.bind(matrixStack);
    }

    protected void endRender(MatrixStack matrixStack) {
        this.activeProgram.unbind();
    }

    @Override
    public void delete() {
        for (EnumMap<?, P> subMap : this.programs.values()) {
            for (P shader : subMap.values()) {
                shader.delete();
            }
        }
    }

    @Override
    public GlVertexFormat<ChunkMeshAttribute> getVertexFormat() {
        return this.vertexFormat;
    }
}
