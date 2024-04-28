package appeng.thirdparty.fabric;
/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

/**
 * Interface for reading quad data encoded by {@link MeshBuilder}. Enables models to do analysis, re-texturing or
 * translation without knowing the renderer's vertex formats and without retaining redundant information.
 *
 * <p>
 * Only the renderer should implement or extend this interface.
 */
public interface QuadView {
    /** Count of integers in a conventional (un-modded) block or item vertex. */
    int VANILLA_VERTEX_STRIDE = DefaultVertexFormat.BLOCK.getIntegerSize();

    /** Count of integers in a conventional (un-modded) block or item quad. */
    int VANILLA_QUAD_STRIDE = VANILLA_VERTEX_STRIDE * 4;

    /**
     * Retrieve geometric position, x coordinate.
     */
    float x(int vertexIndex);

    /**
     * Retrieve geometric position, y coordinate.
     */
    float y(int vertexIndex);

    /**
     * Retrieve geometric position, z coordinate.
     */
    float z(int vertexIndex);

    /**
     * Convenience: access x, y, z by index 0-2.
     */
    float posByIndex(int vertexIndex, int coordinateIndex);

    /**
     * Pass a non-null target to avoid allocation - will be returned with values. Otherwise returns a new instance.
     */
    Vector3f copyPos(int vertexIndex, @Nullable Vector3f target);

    /**
     * Retrieve vertex color.
     */
    int color(int vertexIndex);

    /**
     * Retrieve horizontal texture coordinates.
     */
    float u(int vertexIndex);

    /**
     * Retrieve vertical texture coordinates.
     */
    float v(int vertexIndex);

    /**
     * Whether this quad should be rendered with diffuse lighting
     */
    boolean hasShade();

    /**
     * Whether this quad should be rendered with ambient occlusion
     */
    boolean hasAmbientOcclusion();

    /**
     * Pass a non-null target to avoid allocation - will be returned with values. Otherwise returns a new instance.
     */
    default Vector2f copyUv(int vertexIndex, @Nullable Vector2f target) {
        if (target == null) {
            target = new Vector2f();
        }

        target.set(u(vertexIndex), v(vertexIndex));
        return target;
    }

    /**
     * Minimum block brightness. Zero if not set.
     */
    int lightmap(int vertexIndex);

    /**
     * If false, no vertex normal was provided. Lighting should use face normal in that case.
     */
    boolean hasNormal(int vertexIndex);

    /**
     * Will return {@link Float#NaN} if normal not present.
     */
    float normalX(int vertexIndex);

    /**
     * Will return {@link Float#NaN} if normal not present.
     */
    float normalY(int vertexIndex);

    /**
     * Will return {@link Float#NaN} if normal not present.
     */
    float normalZ(int vertexIndex);

    /**
     * Pass a non-null target to avoid allocation - will be returned with values. Otherwise returns a new instance.
     * Returns null if normal not present.
     */
    @Nullable
    Vector3f copyNormal(int vertexIndex, @Nullable Vector3f target);

    /**
     * If non-null, quad should not be rendered in-world if the opposite face of a neighbor block occludes it.
     *
     * @see MutableQuadView#cullFace(Direction)
     */
    @Nullable
    Direction cullFace();

    /**
     * Equivalent to {@link BakedQuad#getDirection()}. This is the face used for vanilla lighting calculations and will
     * be the block face to which the quad is most closely aligned. Always the same as cull face for quads that are on a
     * block face, but never null.
     */
    @NotNull
    Direction lightFace();

    /**
     * See {@link MutableQuadView#nominalFace(Direction)}.
     */
    Direction nominalFace();

    /**
     * Normal of the quad as implied by geometry. Will be invalid if quad vertices are not co-planar. Typically computed
     * lazily on demand and not encoded.
     *
     * <p>
     * Not typically needed by models. Exposed to enable standard lighting utility functions for use by renderers.
     */
    Vector3f faceNormal();

    /**
     * Retrieves the quad color index serialized with the quad.
     */
    int colorIndex();

    /**
     * Retrieves the integer tag encoded with this quad via {@link MutableQuadView#tag(int)}. Will return zero if no tag
     * was set. For use by models.
     */
    int tag();

    /**
     * Extracts all quad properties except material to the given {@link MutableQuadView} instance. Must be used before
     * calling {link QuadEmitter#emit()} on the target instance. Meant for re-texturing, analysis and static
     * transformation use cases.
     */
    void copyTo(MutableQuadView target);

    /**
     * Reads baked vertex data and outputs standard {@link BakedQuad#getVertices() baked quad vertex data} in the given
     * array and location.
     *
     * @param target      Target array for the baked quad data.
     *
     * @param targetIndex Starting position in target array - array must have at least 28 elements available at this
     *                    index.
     */
    void toVanilla(int[] target, int targetIndex);

    /**
     * Generates a new BakedQuad instance with texture coordinates and colors from the given sprite.
     *
     * @param sprite {@link MutableQuadView} does not serialize sprites so the sprite must be provided by the caller.
     *
     * @return A new baked quad instance with the closest-available appearance supported by vanilla features. Will
     *         retain emissive light maps, for example, but the standard Minecraft renderer will not use them.
     */
    default BakedQuad toBakedQuad(TextureAtlasSprite sprite) {
        int[] vertexData = new int[VANILLA_QUAD_STRIDE];
        toVanilla(vertexData, 0);
        // TODO material inspection: set color index to -1 if the material disables it
        return new BakedQuad(vertexData, colorIndex(), lightFace(), sprite, hasShade(), hasAmbientOcclusion());
    }

    default BakedQuad toBlockBakedQuad() {
        var finder = SpriteFinder.get(Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS));
        return toBakedQuad(finder.find(this));
    }
}