package com.ariasaproject.advancerofrpg.graphics.g3d.utils;

import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.Mesh;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.graphics.VertexAttributes;
import com.ariasaproject.advancerofrpg.graphics.g3d.Material;
import com.ariasaproject.advancerofrpg.graphics.g3d.Model;
import com.ariasaproject.advancerofrpg.graphics.g3d.model.MeshPart;
import com.ariasaproject.advancerofrpg.graphics.g3d.model.Node;
import com.ariasaproject.advancerofrpg.graphics.g3d.model.NodePart;
import com.ariasaproject.advancerofrpg.graphics.g3d.utils.shapebuilders.ArrowShapeBuilder;
import com.ariasaproject.advancerofrpg.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.ariasaproject.advancerofrpg.graphics.g3d.utils.shapebuilders.CapsuleShapeBuilder;
import com.ariasaproject.advancerofrpg.graphics.g3d.utils.shapebuilders.ConeShapeBuilder;
import com.ariasaproject.advancerofrpg.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.ariasaproject.advancerofrpg.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.ariasaproject.advancerofrpg.math.Matrix4;
import com.ariasaproject.advancerofrpg.math.Vector3;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Disposable;

public class ModelBuilder {
	private final Array<MeshBuilder> builders = new Array<MeshBuilder>();
	private final Matrix4 tmpTransform = new Matrix4();
	private Model model;
	private Node node;

	public static void rebuildReferences(final Model model) {
		model.materials.clear();
		model.meshes.clear();
		model.meshParts.clear();
		for (final Node node : model.nodes)
			rebuildReferences(model, node);
	}

	private static void rebuildReferences(final Model model, final Node node) {
		for (final NodePart mpm : node.parts) {
			if (!model.materials.contains(mpm.material, true))
				model.materials.add(mpm.material);
			if (!model.meshParts.contains(mpm.meshPart, true)) {
				model.meshParts.add(mpm.meshPart);
				if (!model.meshes.contains(mpm.meshPart.mesh, true))
					model.meshes.add(mpm.meshPart.mesh);
				model.manageDisposable(mpm.meshPart.mesh);
			}
		}
		for (final Node child : node.getChildren())
			rebuildReferences(model, child);
	}

	private MeshBuilder getBuilder(final VertexAttributes attributes) {
		for (final MeshBuilder mb : builders)
			if (mb.getAttributes().equals(attributes) && mb.lastIndex() < Short.MAX_VALUE / 2)
				return mb;
		final MeshBuilder result = new MeshBuilder();
		result.begin(attributes);
		builders.add(result);
		return result;
	}

	public void begin() {
		if (model != null)
			throw new RuntimeException("Call end() first");
		node = null;
		model = new Model();
		builders.clear();
	}

	public Model end() {
		if (model == null)
			throw new RuntimeException("Call begin() first");
		final Model result = model;
		endnode();
		model = null;
		for (final MeshBuilder mb : builders)
			mb.end();
		builders.clear();
		rebuildReferences(result);
		return result;
	}

	private void endnode() {
		if (node != null) {
			node = null;
		}
	}

	/**
	 * Adds the {@link Node} to the model and sets it active for building. Use any
	 * of the part(...) method to add a NodePart.
	 */
	protected Node node(final Node node) {
		if (model == null)
			throw new RuntimeException("Call begin() first");
		endnode();
		model.nodes.add(node);
		this.node = node;
		return node;
	}

	/**
	 * Add a node to the model. Use any of the part(...) method to add a NodePart.
	 *
	 * @return The node being created.
	 */
	public Node node() {
		final Node node = new Node();
		node(node);
		node.id = "node" + model.nodes.size;
		return node;
	}

	/**
	 * Adds the nodes of the specified model to a new node of the model being build.
	 * After this method the given model can no longer be used. Do not call the
	 * {@link Model#dispose()} method on that model.
	 *
	 * @return The newly created node containing the nodes of the given model.
	 */
	public Node node(final String id, final Model model) {
		final Node node = new Node();
		node.id = id;
		node.addChildren(model.nodes);
		node(node);
		for (final Disposable disposable : model.getManagedDisposables())
			manage(disposable);
		return node;
	}

	/**
	 * Add the {@link Disposable} object to the model, causing it to be disposed
	 * when the model is disposed.
	 */
	public void manage(final Disposable disposable) {
		if (model == null)
			throw new RuntimeException("Call begin() first");
		model.manageDisposable(disposable);
	}

	/**
	 * Adds the specified MeshPart to the current Node. The Mesh will be managed by
	 * the model and disposed when the model is disposed. The resources the Material
	 * might contain are not managed, use {@link #manage(Disposable)} to add those
	 * to the model.
	 */
	public void part(final MeshPart meshpart, final Material material) {
		if (node == null)
			node();
		node.parts.add(new NodePart(meshpart, material));
	}

	/**
	 * Adds the specified mesh part to the current node. The Mesh will be managed by
	 * the model and disposed when the model is disposed. The resources the Material
	 * might contain are not managed, use {@link #manage(Disposable)} to add those
	 * to the model.
	 *
	 * @return The added MeshPart.
	 */
	public MeshPart part(final String id, final Mesh mesh, int primitiveType, int offset, int size,
			final Material material) {
		final MeshPart meshPart = new MeshPart();
		meshPart.id = id;
		meshPart.primitiveType = primitiveType;
		meshPart.mesh = mesh;
		meshPart.offset = offset;
		meshPart.size = size;
		part(meshPart, material);
		return meshPart;
	}

	/**
	 * Adds the specified mesh part to the current node. The Mesh will be managed by
	 * the model and disposed when the model is disposed. The resources the Material
	 * might contain are not managed, use {@link #manage(Disposable)} to add those
	 * to the model.
	 *
	 * @return The added MeshPart.
	 */
	public MeshPart part(final String id, final Mesh mesh, int primitiveType, final Material material) {
		return part(id, mesh, primitiveType, 0, mesh.getNumIndices(), material);
	}

	/**
	 * Creates a new MeshPart within the current Node and returns a
	 * {@link MeshPartBuilder} which can be used to build the shape of the part. If
	 * possible a previously used {@link MeshPartBuilder} will be reused, to reduce
	 * the number of mesh binds. Therefore you can only build one part at a time.
	 * The resources the Material might contain are not managed, use
	 * {@link #manage(Disposable)} to add those to the model.
	 *
	 * @return The {@link MeshPartBuilder} you can use to build the MeshPart.
	 */
	public MeshPartBuilder part(final String id, int primitiveType, final VertexAttributes attributes,
			final Material material) {
		final MeshBuilder builder = getBuilder(attributes);
		part(builder.part(id, primitiveType), material);
		return builder;
	}

	/**
	 * Creates a new MeshPart within the current Node and returns a
	 * {@link MeshPartBuilder} which can be used to build the shape of the part. If
	 * possible a previously used {@link MeshPartBuilder} will be reused, to reduce
	 * the number of mesh binds. Therefore you can only build one part at a time.
	 * The resources the Material might contain are not managed, use
	 * {@link #manage(Disposable)} to add those to the model.
	 *
	 * @param attributes bitwise mask of the
	 *                   {@link com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage},
	 *                   only Position, Color, Normal and TextureCoordinates is
	 *                   supported.
	 * @return The {@link MeshPartBuilder} you can use to build the MeshPart.
	 */
	public MeshPartBuilder part(final String id, int primitiveType, final long attributes, final Material material) {
		return part(id, primitiveType, MeshBuilder.createAttributes(attributes), material);
	}

	/**
	 * Convenience method to create a model with a single node containing a box
	 * shape. The resources the Material might contain are not managed, use
	 * {@link Model#manageDisposable(Disposable)} to add those to the model.
	 *
	 * @param attributes bitwise mask of the
	 *                   {@link com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage},
	 *                   only Position, Color, Normal and TextureCoordinates is
	 *                   supported.
	 */
	public Model createBox(float width, float height, float depth, final Material material, final long attributes) {
		return createBox(width, height, depth, TGF.GL_TRIANGLES, material, attributes);
	}

	/**
	 * Convenience method to create a model with a single node containing a box
	 * shape. The resources the Material might contain are not managed, use
	 * {@link Model#manageDisposable(Disposable)} to add those to the model.
	 *
	 * @param attributes bitwise mask of the
	 *                   {@link com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage},
	 *                   only Position, Color, Normal and TextureCoordinates is
	 *                   supported.
	 */
	public Model createBox(float width, float height, float depth, int primitiveType, final Material material,
			final long attributes) {
		begin();
		MeshPartBuilder builder = part("box", primitiveType, attributes, material);
		BoxShapeBuilder.build(builder, width, height, depth);
		return end();
	}

	/**
	 * Convenience method to create a model with a single node containing a
	 * rectangle shape. The resources the Material might contain are not managed,
	 * use {@link Model#manageDisposable(Disposable)} to add those to the model.
	 *
	 * @param attributes bitwise mask of the
	 *                   {@link com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage},
	 *                   only Position, Color, Normal and TextureCoordinates is
	 *                   supported.
	 */
	public Model createRect(float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11,
			float z11, float x01, float y01, float z01, float normalX, float normalY, float normalZ,
			final Material material, final long attributes) {
		return createRect(x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01, normalX, normalY, normalZ,
				TGF.GL_TRIANGLES, material, attributes);
	}

	/**
	 * Convenience method to create a model with a single node containing a
	 * rectangle shape. The resources the Material might contain are not managed,
	 * use {@link Model#manageDisposable(Disposable)} to add those to the model.
	 *
	 * @param attributes bitwise mask of the
	 *                   {@link com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage},
	 *                   only Position, Color, Normal and TextureCoordinates is
	 *                   supported.
	 */
	public Model createRect(float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11,
			float z11, float x01, float y01, float z01, float normalX, float normalY, float normalZ, int primitiveType,
			final Material material, final long attributes) {
		begin();
		part("rect", primitiveType, attributes, material).rect(x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01,
				z01, normalX, normalY, normalZ);
		return end();
	}

	/**
	 * Convenience method to create a model with a single node containing a cylinder
	 * shape. The resources the Material might contain are not managed, use
	 * {@link Model#manageDisposable(Disposable)} to add those to the model.
	 *
	 * @param attributes bitwise mask of the
	 *                   {@link com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage},
	 *                   only Position, Color, Normal and TextureCoordinates is
	 *                   supported.
	 */
	public Model createCylinder(float width, float height, float depth, int divisions, final Material material,
			final long attributes) {
		return createCylinder(width, height, depth, divisions, TGF.GL_TRIANGLES, material, attributes);
	}

	/**
	 * Convenience method to create a model with a single node containing a cylinder
	 * shape. The resources the Material might contain are not managed, use
	 * {@link Model#manageDisposable(Disposable)} to add those to the model.
	 *
	 * @param attributes bitwise mask of the
	 *                   {@link com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage},
	 *                   only Position, Color, Normal and TextureCoordinates is
	 *                   supported.
	 */
	public Model createCylinder(float width, float height, float depth, int divisions, int primitiveType,
			final Material material, final long attributes) {
		return createCylinder(width, height, depth, divisions, primitiveType, material, attributes, 0, 360);
	}

	/**
	 * Convenience method to create a model with a single node containing a cylinder
	 * shape. The resources the Material might contain are not managed, use
	 * {@link Model#manageDisposable(Disposable)} to add those to the model.
	 *
	 * @param attributes bitwise mask of the
	 *                   {@link com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage},
	 *                   only Position, Color, Normal and TextureCoordinates is
	 *                   supported.
	 */
	public Model createCylinder(float width, float height, float depth, int divisions, final Material material,
			final long attributes, float angleFrom, float angleTo) {
		return createCylinder(width, height, depth, divisions, TGF.GL_TRIANGLES, material, attributes, angleFrom,
				angleTo);
	}

	/**
	 * Convenience method to create a model with a single node containing a cylinder
	 * shape. The resources the Material might contain are not managed, use
	 * {@link Model#manageDisposable(Disposable)} to add those to the model.
	 *
	 * @param attributes bitwise mask of the
	 *                   {@link com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage},
	 *                   only Position, Color, Normal and TextureCoordinates is
	 *                   supported.
	 */
	public Model createCylinder(float width, float height, float depth, int divisions, int primitiveType,
			final Material material, final long attributes, float angleFrom, float angleTo) {
		begin();
		MeshPartBuilder builder = part("cylinder", primitiveType, attributes, material);
		CylinderShapeBuilder.build(builder, width, height, depth, divisions, angleFrom, angleTo);
		return end();
	}

	/**
	 * Convenience method to create a model with a single node containing a cone
	 * shape. The resources the Material might contain are not managed, use
	 * {@link Model#manageDisposable(Disposable)} to add those to the model.
	 *
	 * @param attributes bitwise mask of the
	 *                   {@link com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage},
	 *                   only Position, Color, Normal and TextureCoordinates is
	 *                   supported.
	 */
	public Model createCone(float width, float height, float depth, int divisions, final Material material,
			final long attributes) {
		return createCone(width, height, depth, divisions, TGF.GL_TRIANGLES, material, attributes);
	}

	/**
	 * Convenience method to create a model with a single node containing a cone
	 * shape. The resources the Material might contain are not managed, use
	 * {@link Model#manageDisposable(Disposable)} to add those to the model.
	 *
	 * @param attributes bitwise mask of the
	 *                   {@link com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage},
	 *                   only Position, Color, Normal and TextureCoordinates is
	 *                   supported.
	 */
	public Model createCone(float width, float height, float depth, int divisions, int primitiveType,
			final Material material, final long attributes) {
		return createCone(width, height, depth, divisions, primitiveType, material, attributes, 0, 360);
	}

	/**
	 * Convenience method to create a model with a single node containing a cone
	 * shape. The resources the Material might contain are not managed, use
	 * {@link Model#manageDisposable(Disposable)} to add those to the model.
	 *
	 * @param attributes bitwise mask of the
	 *                   {@link com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage},
	 *                   only Position, Color, Normal and TextureCoordinates is
	 *                   supported.
	 */
	public Model createCone(float width, float height, float depth, int divisions, final Material material,
			final long attributes, float angleFrom, float angleTo) {
		return createCone(width, height, depth, divisions, TGF.GL_TRIANGLES, material, attributes, angleFrom, angleTo);
	}

	/**
	 * Convenience method to create a model with a single node containing a cone
	 * shape. The resources the Material might contain are not managed, use
	 * {@link Model#manageDisposable(Disposable)} to add those to the model.
	 *
	 * @param attributes bitwise mask of the
	 *                   {@link com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage},
	 *                   only Position, Color, Normal and TextureCoordinates is
	 *                   supported.
	 */
	public Model createCone(float width, float height, float depth, int divisions, int primitiveType,
			final Material material, final long attributes, float angleFrom, float angleTo) {
		begin();
		MeshPartBuilder builder = part("cone", primitiveType, attributes, material);
		ConeShapeBuilder.build(builder, width, height, depth, divisions, angleFrom, angleTo);
		return end();
	}

	/**
	 * Convenience method to create a model with a single node containing a sphere
	 * shape. The resources the Material might contain are not managed, use
	 * {@link Model#manageDisposable(Disposable)} to add those to the model.
	 *
	 * @param attributes bitwise mask of the
	 *                   {@link com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage},
	 *                   only Position, Color, Normal and TextureCoordinates is
	 *                   supported.
	 */
	public Model createSphere(float width, float height, float depth, int divisionsU, int divisionsV,
			final Material material, final long attributes) {
		return createSphere(width, height, depth, divisionsU, divisionsV, TGF.GL_TRIANGLES, material, attributes);
	}

	/**
	 * Convenience method to create a model with a single node containing a sphere
	 * shape. The resources the Material might contain are not managed, use
	 * {@link Model#manageDisposable(Disposable)} to add those to the model.
	 *
	 * @param attributes bitwise mask of the
	 *                   {@link com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage},
	 *                   only Position, Color, Normal and TextureCoordinates is
	 *                   supported.
	 */
	public Model createSphere(float width, float height, float depth, int divisionsU, int divisionsV, int primitiveType,
			final Material material, final long attributes) {
		return createSphere(width, height, depth, divisionsU, divisionsV, primitiveType, material, attributes, 0, 360,
				0, 180);
	}

	/**
	 * Convenience method to create a model with a single node containing a sphere
	 * shape. The resources the Material might contain are not managed, use
	 * {@link Model#manageDisposable(Disposable)} to add those to the model.
	 *
	 * @param attributes bitwise mask of the
	 *                   {@link com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage},
	 *                   only Position, Color, Normal and TextureCoordinates is
	 *                   supported.
	 */
	public Model createSphere(float width, float height, float depth, int divisionsU, int divisionsV,
			final Material material, final long attributes, float angleUFrom, float angleUTo, float angleVFrom,
			float angleVTo) {
		return createSphere(width, height, depth, divisionsU, divisionsV, TGF.GL_TRIANGLES, material, attributes,
				angleUFrom, angleUTo, angleVFrom, angleVTo);
	}

	/**
	 * Convenience method to create a model with a single node containing a sphere
	 * shape. The resources the Material might contain are not managed, use
	 * {@link Model#manageDisposable(Disposable)} to add those to the model.
	 *
	 * @param attributes bitwise mask of the
	 *                   {@link com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage},
	 *                   only Position, Color, Normal and TextureCoordinates is
	 *                   supported.
	 */
	public Model createSphere(float width, float height, float depth, int divisionsU, int divisionsV, int primitiveType,
			final Material material, final long attributes, float angleUFrom, float angleUTo, float angleVFrom,
			float angleVTo) {
		begin();
		MeshPartBuilder builder = part("sphere", primitiveType, attributes, material);
		SphereShapeBuilder.build(builder, width, height, depth, divisionsU, divisionsV, angleUFrom, angleUTo,
				angleVFrom, angleVTo);
		return end();
	}

	/**
	 * Convenience method to create a model with a single node containing a capsule
	 * shape. The resources the Material might contain are not managed, use
	 * {@link Model#manageDisposable(Disposable)} to add those to the model.
	 *
	 * @param attributes bitwise mask of the
	 *                   {@link com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage},
	 *                   only Position, Color, Normal and TextureCoordinates is
	 *                   supported.
	 */
	public Model createCapsule(float radius, float height, int divisions, final Material material,
			final long attributes) {
		return createCapsule(radius, height, divisions, TGF.GL_TRIANGLES, material, attributes);
	}

	/**
	 * Convenience method to create a model with a single node containing a capsule
	 * shape. The resources the Material might contain are not managed, use
	 * {@link Model#manageDisposable(Disposable)} to add those to the model.
	 *
	 * @param attributes bitwise mask of the
	 *                   {@link com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage},
	 *                   only Position, Color, Normal and TextureCoordinates is
	 *                   supported.
	 */
	public Model createCapsule(float radius, float height, int divisions, int primitiveType, final Material material,
			final long attributes) {
		begin();
		MeshPartBuilder build = part("capsule", primitiveType, attributes, material);
		CapsuleShapeBuilder.build(build, radius, height, divisions);
		return end();
	}

	/**
	 * Convenience method to create a model with three orthonormal vectors shapes.
	 * The resources the Material might contain are not managed, use
	 * {@link Model#manageDisposable(Disposable)} to add those to the model.
	 *
	 * @param axisLength    Length of each axis.
	 * @param capLength     is the height of the cap in percentage, must be in (0,1)
	 * @param stemThickness is the percentage of stem diameter compared to cap
	 *                      diameter, must be in (0,1]
	 * @param divisions     the amount of vertices used to generate the cap and stem
	 *                      ellipsoidal bases
	 */
	public Model createXYZCoordinates(float axisLength, float capLength, float stemThickness, int divisions,
			int primitiveType, Material material, long attributes) {
		begin();
		MeshPartBuilder partBuilder;
		Node node = node();
		partBuilder = part("xyz", primitiveType, attributes, material);
		partBuilder.setColor(Color.RED);
		ArrowShapeBuilder.build(partBuilder, 0, 0, 0, axisLength, 0, 0, capLength, stemThickness, divisions);
		partBuilder.setColor(Color.GREEN);
		ArrowShapeBuilder.build(partBuilder, 0, 0, 0, 0, axisLength, 0, capLength, stemThickness, divisions);
		partBuilder.setColor(Color.BLUE);
		ArrowShapeBuilder.build(partBuilder, 0, 0, 0, 0, 0, axisLength, capLength, stemThickness, divisions);
		return end();
	}

	public Model createXYZCoordinates(float axisLength, Material material, long attributes) {
		return createXYZCoordinates(axisLength, 0.1f, 0.1f, 5, TGF.GL_TRIANGLES, material, attributes);
	}

	public Model createArrow(float x1, float y1, float z1, float x2, float y2, float z2, float capLength,
			float stemThickness, int divisions, int primitiveType, Material material, long attributes) {
		begin();
		MeshPartBuilder build = part("arrow", primitiveType, attributes, material);
		ArrowShapeBuilder.build(build, x1, y1, z1, x2, y2, z2, capLength, stemThickness, divisions);
		return end();
	}

	public Model createArrow(Vector3 from, Vector3 to, Material material, long attributes) {
		return createArrow(from.x, from.y, from.z, to.x, to.y, to.z, 0.1f, 0.1f, 5, TGF.GL_TRIANGLES, material,
				attributes);
	}

	public Model createLineGrid(int xDivisions, int zDivisions, float xSize, float zSize, Material material,
			long attributes) {
		begin();
		MeshPartBuilder partBuilder = part("lines", TGF.GL_LINES, attributes, material);
		float xlength = xDivisions * xSize, zlength = zDivisions * zSize, hxlength = xlength / 2,
				hzlength = zlength / 2;
		float x1 = -hxlength, y1 = 0, z1 = hzlength, x2 = -hxlength, y2 = 0, z2 = -hzlength;
		for (int i = 0; i <= xDivisions; ++i) {
			partBuilder.line(x1, y1, z1, x2, y2, z2);
			x1 += xSize;
			x2 += xSize;
		}
		x1 = -hxlength;
		y1 = 0;
		z1 = -hzlength;
		x2 = hxlength;
		y2 = 0;
		z2 = -hzlength;
		for (int j = 0; j <= zDivisions; ++j) {
			partBuilder.line(x1, y1, z1, x2, y2, z2);
			z1 += zSize;
			z2 += zSize;
		}
		return end();
	}

}
