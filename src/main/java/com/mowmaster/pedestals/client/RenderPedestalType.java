package com.mowmaster.pedestals.Client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;

import net.minecraft.client.renderer.RenderType.CompositeState;

public class RenderPedestalType extends RenderType
{

    public RenderPedestalType(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
    }

    private static final LineStateShard THICK_LINES = new LineStateShard(OptionalDouble.of(3.0D));

    public static final RenderType OVERLAY_LINES = RenderType.create(
            "overlay_lines",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.LINES,
            256,
            false,
            true,
            CompositeState.builder()
                    .setLineState(THICK_LINES)

                    .createCompositeState(true)

    );

    /*DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
        RenderType.State.getBuilder().line(THICK_LINES)
                    .layer(field_239235_M_)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .texture(NO_TEXTURE)
                    .depthTest(DEPTH_ALWAYS)
                    .cull(CULL_DISABLED)
                    .lightmap(LIGHTMAP_DISABLED)
                    .writeMask(COLOR_WRITE)
                    .build(false)*/
}
