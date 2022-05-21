package com.ariasaproject.advancerofrpg.graphics.g3d;

import com.ariasaproject.advancerofrpg.utils.Array;

public class Material extends Attributes {
    private static int counter = 0;

    public String id;

    /**
     * Create an empty material
     */
    public Material() {
        this("mtl" + (++counter));
    }

    /**
     * Create an empty material
     */
    public Material(final String id) {
        this.id = id;
    }

    /**
     * Create a material with the specified attributes
     */
    public Material(final Attribute... attributes) {
        this();
        set(attributes);
    }

    /**
     * Create a material with the specified attributes
     */
    public Material(final String id, final Attribute... attributes) {
        this(id);
        set(attributes);
    }

    /**
     * Create a material with the specified attributes
     */
    public Material(final Array<Attribute> attributes) {
        this();
        set(attributes);
    }

    /**
     * Create a material with the specified attributes
     */
    public Material(final String id, final Array<Attribute> attributes) {
        this(id);
        set(attributes);
    }

    /**
     * Create a material which is an exact copy of the specified material
     */
    public Material(final Material copyFrom) {
        this(copyFrom.id, copyFrom);
    }

    /**
     * Create a material which is an exact copy of the specified material
     */
    public Material(final String id, final Material copyFrom) {
        this(id);
        for (Attribute attr : copyFrom)
            set(attr.copy());
    }

    /**
     * Create a copy of this material
     */
    public Material copy() {
        return new Material(this);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + 3 * id.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof Material) && ((other == this) || ((((Material) other).id.equals(id)) && super.equals(other)));
    }
}
