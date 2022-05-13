package com.ariasaproject.advancerofrpg.math;

import java.io.Serializable;

/**
 * A point in a 3D grid, with integer x and y coordinates
 *
 * @author badlogic
 */
public class IntegerVector3 implements Serializable {
	private static final long serialVersionUID = 5922187982746752830L;

	public int x;
	public int y;
	public int z;

	/**
	 * Constructs a 3D grid point with all coordinates pointing to the origin (0, 0,
	 * 0).
	 */
	public IntegerVector3() {
	}

	/**
	 * Constructs a 3D grid point.
	 *
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 */
	public IntegerVector3(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Copy constructor
	 *
	 * @param point The 3D grid point to make a copy of.
	 */
	public IntegerVector3(IntegerVector3 point) {
		this.x = point.x;
		this.y = point.y;
		this.z = point.z;
	}

	/**
	 * Sets the coordinates of this 3D grid point to that of another.
	 *
	 * @param point The 3D grid point to copy coordinates of.
	 * @return this GridPoint3 for chaining.
	 */
	public IntegerVector3 set(IntegerVector3 point) {
		this.x = point.x;
		this.y = point.y;
		this.z = point.z;
		return this;
	}

	/**
	 * Sets the coordinates of this GridPoint3D.
	 *
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @return this GridPoint3D for chaining.
	 */
	public IntegerVector3 set(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	/**
	 * @param other The other point
	 * @return the squared distance between this point and the other point.
	 */
	public float dst2(IntegerVector3 other) {
		int xd = other.x - x;
		int yd = other.y - y;
		int zd = other.z - z;
		return xd * xd + yd * yd + zd * zd;
	}

	/**
	 * @param x The x-coordinate of the other point
	 * @param y The y-coordinate of the other point
	 * @param z The z-coordinate of the other point
	 * @return the squared distance between this point and the other point.
	 */
	public float dst2(int x, int y, int z) {
		int xd = x - this.x;
		int yd = y - this.y;
		int zd = z - this.z;
		return xd * xd + yd * yd + zd * zd;
	}

	/**
	 * @param other The other point
	 * @return the distance between this point and the other vector.
	 */
	public float dst(IntegerVector3 other) {
		int xd = other.x - x;
		int yd = other.y - y;
		int zd = other.z - z;
		return (float) Math.sqrt(xd * xd + yd * yd + zd * zd);
	}

	/**
	 * @param x The x-coordinate of the other point
	 * @param y The y-coordinate of the other point
	 * @param z The z-coordinate of the other point
	 * @return the distance between this point and the other point.
	 */
	public float dst(int x, int y, int z) {
		int xd = x - this.x;
		int yd = y - this.y;
		int zd = z - this.z;
		return (float) Math.sqrt(xd * xd + yd * yd + zd * zd);
	}

	/**
	 * Adds another 3D grid point to this point.
	 *
	 * @param other The other point
	 * @return this 3d grid point for chaining.
	 */
	public IntegerVector3 add(IntegerVector3 other) {
		x += other.x;
		y += other.y;
		z += other.z;
		return this;
	}

	/**
	 * Adds another 3D grid point to this point.
	 *
	 * @param x The x-coordinate of the other point
	 * @param y The y-coordinate of the other point
	 * @param z The z-coordinate of the other point
	 * @return this 3d grid point for chaining.
	 */
	public IntegerVector3 add(int x, int y, int z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	/**
	 * Subtracts another 3D grid point from this point.
	 *
	 * @param other The other point
	 * @return this 3d grid point for chaining.
	 */
	public IntegerVector3 sub(IntegerVector3 other) {
		x -= other.x;
		y -= other.y;
		z -= other.z;
		return this;
	}

	/**
	 * Subtracts another 3D grid point from this point.
	 *
	 * @param x The x-coordinate of the other point
	 * @param y The y-coordinate of the other point
	 * @param z The z-coordinate of the other point
	 * @return this 3d grid point for chaining.
	 */
	public IntegerVector3 sub(int x, int y, int z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}

	/**
	 * @return a copy of this grid point
	 */
	public IntegerVector3 cpy() {
		return new IntegerVector3(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || o.getClass() != this.getClass())
			return false;
		IntegerVector3 g = (IntegerVector3) o;
		return this.x == g.x && this.y == g.y && this.z == g.z;
	}

	@Override
	public int hashCode() {
		final int prime = 17;
		int result = 1;
		result = prime * result + this.x;
		result = prime * result + this.y;
		result = prime * result + this.z;
		return result;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}
}
