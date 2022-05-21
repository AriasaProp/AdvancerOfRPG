package com.ariasaproject.advancerofrpg.graphics.g3d.decals;

import com.ariasaproject.advancerofrpg.graphics.Mesh;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.graphics.VertexAttribute;
import com.ariasaproject.advancerofrpg.graphics.VertexAttributes;
import com.ariasaproject.advancerofrpg.graphics.glutils.ShaderProgram;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Disposable;
import com.ariasaproject.advancerofrpg.utils.Pool;
import com.ariasaproject.advancerofrpg.utils.SortedIntList;

public class DecalBatch implements Disposable {
    private static final int DEFAULT_SIZE = 1000;
    private final SortedIntList<Array<Decal>> groupList = new SortedIntList<Array<Decal>>();
    private final Pool<Array<Decal>> groupPool = new Pool<Array<Decal>>(16) {
        @Override
        protected Array<Decal> newObject() {
            return new Array<Decal>(false, 100);
        }
    };
    private final Array<Array<Decal>> usedGroups = new Array<Array<Decal>>(16);
    private float[] vertices;
    private Mesh mesh;
    private GroupStrategy groupStrategy;

    public DecalBatch(GroupStrategy groupStrategy) {
        this(DEFAULT_SIZE, groupStrategy);
    }

    public DecalBatch(int size, GroupStrategy groupStrategy) {
        initialize(size);
        setGroupStrategy(groupStrategy);
    }

    public void setGroupStrategy(GroupStrategy groupStrategy) {
        this.groupStrategy = groupStrategy;
    }

    public void initialize(int size) {
        vertices = new float[size * Decal.SIZE];
        mesh = new Mesh(false, size * 4, size * 6, new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"), new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_color"), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"));
        short[] indices = new short[size * 6];
        int v = 0;
        for (int i = 0; i < indices.length; i += 6, v += 4) {
            indices[i] = (short) (v);
            indices[i + 1] = (short) (v + 2);
            indices[i + 2] = (short) (v + 1);
            indices[i + 3] = (short) (v + 1);
            indices[i + 4] = (short) (v + 2);
            indices[i + 5] = (short) (v + 3);
        }
        mesh.setIndices(indices);
    }

    /**
     * @return maximum amount of decal objects this buffer can hold in memory
     */
    public int getSize() {
        return vertices.length / Decal.SIZE;
    }

    /**
     * Add a decal to the batch, marking it for later rendering
     *
     * @param decal Decal to add for rendering
     */
    public void add(Decal decal) {
        int groupIndex = groupStrategy.decideGroup(decal);
        Array<Decal> targetGroup = groupList.get(groupIndex);
        if (targetGroup == null) {
            targetGroup = groupPool.obtain();
            targetGroup.clear();
            usedGroups.add(targetGroup);
            groupList.insert(groupIndex, targetGroup);
        }
        targetGroup.add(decal);
    }

    /**
     * Flush this batch sending all contained decals to GL. After flushing the batch
     * is empty once again.
     */
    public void flush() {
        render();
        clear();
    }

    /**
     * Renders all decals to the buffer and flushes the buffer to the GL when
     * full/done
     */
    protected void render() {
        groupStrategy.beforeGroups();
        for (SortedIntList.Node<Array<Decal>> group : groupList) {
            groupStrategy.beforeGroup(group.index, group.value);
            ShaderProgram shader = groupStrategy.getGroupShader(group.index);
            render(shader, group.value);
            groupStrategy.afterGroup(group.index);
        }
        groupStrategy.afterGroups();
    }

    /**
     * Renders a group of vertices to the buffer, flushing them to GL when done/full
     *
     * @param decals Decals to render
     */
    private void render(ShaderProgram shader, Array<Decal> decals) {
        // batch vertices
        DecalMaterial lastMaterial = null;
        int idx = 0;
        for (Decal decal : decals) {
            if (lastMaterial == null || !lastMaterial.equals(decal.getMaterial())) {
                if (idx > 0) {
                    flush(shader, idx);
                    idx = 0;
                }
                decal.material.set();
                lastMaterial = decal.material;
            }
            decal.update();
            System.arraycopy(decal.vertices, 0, vertices, idx, decal.vertices.length);
            idx += decal.vertices.length;
            // if our batch is full we have to flush it
            if (idx == vertices.length) {
                flush(shader, idx);
                idx = 0;
            }
        }
        // at the end if there is stuff left in the batch we render that
        if (idx > 0) {
            flush(shader, idx);
        }
    }

    /**
     * Flushes vertices[0,verticesPosition[ to GL verticesPosition % Decal.SIZE must
     * equal 0
     *
     * @param verticesPosition Amount of elements from the vertices array to flush
     */
    protected void flush(ShaderProgram shader, int verticesPosition) {
        mesh.setVertices(vertices, 0, verticesPosition);
        mesh.render(shader, TGF.GL_TRIANGLES, 0, verticesPosition / 4);
    }

    /**
     * Remove all decals from batch
     */
    protected void clear() {
        groupList.clear();
        groupPool.freeAll(usedGroups);
        usedGroups.clear();
    }

    /**
     * Frees up memory by dropping the buffer and underlying resources. If the batch
     * is needed again after disposing it can be {@link #initialize(int)
     * initialized} again.
     */
    @Override
    public void dispose() {
        clear();
        vertices = null;
        mesh.dispose();
    }
}
