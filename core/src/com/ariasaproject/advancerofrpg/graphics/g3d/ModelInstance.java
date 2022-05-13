package com.ariasaproject.advancerofrpg.graphics.g3d;

import com.ariasaproject.advancerofrpg.graphics.g3d.model.Animation;
import com.ariasaproject.advancerofrpg.graphics.g3d.model.Node;
import com.ariasaproject.advancerofrpg.graphics.g3d.model.NodeAnimation;
import com.ariasaproject.advancerofrpg.graphics.g3d.model.NodeKeyframe;
import com.ariasaproject.advancerofrpg.graphics.g3d.model.NodePart;
import com.ariasaproject.advancerofrpg.math.Matrix4;
import com.ariasaproject.advancerofrpg.math.Quaternion;
import com.ariasaproject.advancerofrpg.math.Vector3;
import com.ariasaproject.advancerofrpg.math.collision.BoundingBox;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.ArrayMap;
import com.ariasaproject.advancerofrpg.utils.Pool;

public class ModelInstance implements RenderableProvider {
	public static boolean defaultShareKeyframes = true;
	public final Array<Material> materials = new Array<Material>();
	public final Array<Node> nodes = new Array<Node>();
	public final Array<Animation> animations = new Array<Animation>();
	public final Model model;
	public Matrix4 transform;
	public Object userData;

	public ModelInstance(final Model model) {
		this(model, (String[]) null);
	}
	public ModelInstance(final Model model, final String nodeId, boolean mergeTransform) {
		this(model, new Matrix4(), nodeId, false, false, mergeTransform);
	}
	public ModelInstance(final Model model, final Matrix4 transform, final String nodeId, boolean mergeTransform) {
		this(model, transform, nodeId, false, false, mergeTransform);
	}
	public ModelInstance(final Model model, final String nodeId, boolean parentTransform, boolean mergeTransform) {
		this(model, new Matrix4(), nodeId, true, parentTransform, mergeTransform);
	}
	public ModelInstance(final Model model, final Matrix4 transform, final String nodeId, boolean parentTransform, boolean mergeTransform) {
		this(model, transform, nodeId, true, parentTransform, mergeTransform);
	}
	public ModelInstance(final Model model, final String nodeId, boolean recursive, boolean parentTransform, boolean mergeTransform) {
		this(model, new Matrix4(), nodeId, recursive, parentTransform, mergeTransform);
	}
	public ModelInstance(final Model model, final Matrix4 transform, final String nodeId, boolean recursive, boolean parentTransform, boolean mergeTransform) {
		this(model, transform, nodeId, recursive, parentTransform, mergeTransform, defaultShareKeyframes);
	}

	public ModelInstance(final Model model, final Matrix4 transform, final String nodeId, boolean recursive, boolean parentTransform, boolean mergeTransform, boolean shareKeyframes) {
		this.model = model;
		this.transform = transform;
		Node copy, node = model.getNode(nodeId, recursive);
		this.nodes.add(copy = node.copy());
		if (mergeTransform) {
			this.transform.mul(parentTransform ? node.globalTransform : node.localTransform);
			copy.translation.set(0, 0, 0);
			copy.rotation.idt();
			copy.scale.set(1, 1, 1);
		} else if (parentTransform && copy.hasParent())
			this.transform.mul(node.getParent().globalTransform);
		invalidate();
		copyAnimations(model.animations, shareKeyframes);
		calculateTransforms();
	}
	public ModelInstance(final Model model, final String... rootNodeIds) {
		this(model, new Matrix4(), rootNodeIds);
	}
	
	public ModelInstance(final Model model, final Matrix4 transform, final String... rootNodeIds) {
		this.model = model;
		this.transform = transform;
		if (rootNodeIds == null)
			copyNodes(model.nodes);
		else
			copyNodes(model.nodes, rootNodeIds);
		copyAnimations(model.animations, defaultShareKeyframes);
		calculateTransforms();
	}
	public ModelInstance(final Model model, final Array<String> rootNodeIds) {
		this(model, new Matrix4(), rootNodeIds);
	}
	public ModelInstance(final Model model, final Matrix4 transform, final Array<String> rootNodeIds) {
		this(model, transform, rootNodeIds, defaultShareKeyframes);
	}
	public ModelInstance(final Model model, final Matrix4 transform, final Array<String> rootNodeIds,
			boolean shareKeyframes) {
		this.model = model;
		this.transform = transform;
		copyNodes(model.nodes, rootNodeIds);
		copyAnimations(model.animations, shareKeyframes);
		calculateTransforms();
	}

	public ModelInstance(final Model model, Vector3 position) {
		this(model);
		this.transform.set(position);
	}

	/**
	 * Constructs a new ModelInstance at the specified position.
	 */
	public ModelInstance(final Model model, float x, float y, float z) {
		this(model);
		this.transform.set(x, y, z);
	}

	/**
	 * Constructs a new ModelInstance with the specified transform.
	 */
	public ModelInstance(final Model model, Matrix4 transform) {
		this(model, transform, (String[]) null);
	}

	/**
	 * Constructs a new ModelInstance which is an copy of the specified
	 * ModelInstance.
	 */
	public ModelInstance(ModelInstance copyFrom) {
		this(copyFrom, copyFrom.transform.cpy());
	}

	/**
	 * Constructs a new ModelInstance which is an copy of the specified
	 * ModelInstance.
	 */
	public ModelInstance(ModelInstance copyFrom, final Matrix4 transform) {
		this(copyFrom, transform, defaultShareKeyframes);
	}

	/**
	 * Constructs a new ModelInstance which is an copy of the specified
	 * ModelInstance.
	 */
	public ModelInstance(ModelInstance copyFrom, final Matrix4 transform, boolean shareKeyframes) {
		this.model = copyFrom.model;
		this.transform = transform == null ? new Matrix4() : transform;
		copyNodes(copyFrom.nodes);
		copyAnimations(copyFrom.animations, shareKeyframes);
		calculateTransforms();
	}

	/**
	 * @return A newly created ModelInstance which is a copy of this ModelInstance
	 */
	public ModelInstance copy() {
		return new ModelInstance(this);
	}

	private void copyNodes(Array<Node> nodes) {
		for (int i = 0, n = nodes.size; i < n; ++i) {
			final Node node = nodes.get(i);
			this.nodes.add(node.copy());
		}
		invalidate();
	}

	private void copyNodes(Array<Node> nodes, final String... nodeIds) {
		for (int i = 0, n = nodes.size; i < n; ++i) {
			final Node node = nodes.get(i);
			for (final String nodeId : nodeIds) {
				if (nodeId.equals(node.id)) {
					this.nodes.add(node.copy());
					break;
				}
			}
		}
		invalidate();
	}

	private void copyNodes(Array<Node> nodes, final Array<String> nodeIds) {
		for (int i = 0, n = nodes.size; i < n; ++i) {
			final Node node = nodes.get(i);
			for (final String nodeId : nodeIds) {
				if (nodeId.equals(node.id)) {
					this.nodes.add(node.copy());
					break;
				}
			}
		}
		invalidate();
	}

	/**
	 * Makes sure that each {@link NodePart} of the {@link Node} and its sub-nodes,
	 * doesn't reference a node outside this node tree and that all materials are
	 * listed in the {@link #materials} array.
	 */
	private void invalidate(Node node) {
		for (int i = 0, n = node.parts.size; i < n; ++i) {
			NodePart part = node.parts.get(i);
			ArrayMap<Node, Matrix4> bindPose = part.invBoneBindTransforms;
			if (bindPose != null) {
				for (int j = 0; j < bindPose.size; ++j) {
					bindPose.keys[j] = getNode(bindPose.keys[j].id);
				}
			}
			if (!materials.contains(part.material, true)) {
				final int midx = materials.indexOf(part.material, false);
				if (midx < 0)
					materials.add(part.material = part.material.copy());
				else
					part.material = materials.get(midx);
			}
		}
		for (int i = 0, n = node.getChildCount(); i < n; ++i) {
			invalidate(node.getChild(i));
		}
	}

	/**
	 * Makes sure that each {@link NodePart} of each {@link Node} doesn't reference
	 * a node outside this node tree and that all materials are listed in the
	 * {@link #materials} array.
	 */
	private void invalidate() {
		for (int i = 0, n = nodes.size; i < n; ++i) {
			invalidate(nodes.get(i));
		}
	}

	/**
	 * Copy source animations to this ModelInstance
	 *
	 * @param source Iterable collection of source animations {@link Animation}
	 */
	public void copyAnimations(final Iterable<Animation> source) {
		for (final Animation anim : source) {
			copyAnimation(anim, defaultShareKeyframes);
		}
	}

	/**
	 * Copy source animations to this ModelInstance
	 *
	 * @param source         Iterable collection of source animations
	 *                       {@link Animation}
	 * @param shareKeyframes Shallow copy of {@link NodeKeyframe}'s if it's true,
	 *                       otherwise make a deep copy.
	 */
	public void copyAnimations(final Iterable<Animation> source, boolean shareKeyframes) {
		for (final Animation anim : source) {
			copyAnimation(anim, shareKeyframes);
		}
	}

	/**
	 * Copy the source animation to this ModelInstance
	 *
	 * @param sourceAnim The source animation {@link Animation}
	 */
	public void copyAnimation(Animation sourceAnim) {
		copyAnimation(sourceAnim, defaultShareKeyframes);
	}

	/**
	 * Copy the source animation to this ModelInstance
	 *
	 * @param sourceAnim     The source animation {@link Animation}
	 * @param shareKeyframes Shallow copy of {@link NodeKeyframe}'s if it's true,
	 *                       otherwise make a deep copy.
	 */
	public void copyAnimation(Animation sourceAnim, boolean shareKeyframes) {
		Animation animation = new Animation();
		animation.id = sourceAnim.id;
		animation.duration = sourceAnim.duration;
		for (final NodeAnimation nanim : sourceAnim.nodeAnimations) {
			final Node node = getNode(nanim.node.id);
			if (node == null)
				continue;
			NodeAnimation nodeAnim = new NodeAnimation();
			nodeAnim.node = node;
			if (shareKeyframes) {
				nodeAnim.translation = nanim.translation;
				nodeAnim.rotation = nanim.rotation;
				nodeAnim.scaling = nanim.scaling;
			} else {
				if (nanim.translation != null) {
					nodeAnim.translation = new Array<NodeKeyframe<Vector3>>();
					for (final NodeKeyframe<Vector3> kf : nanim.translation)
						nodeAnim.translation.add(new NodeKeyframe<Vector3>(kf.keytime, kf.value));
				}
				if (nanim.rotation != null) {
					nodeAnim.rotation = new Array<NodeKeyframe<Quaternion>>();
					for (final NodeKeyframe<Quaternion> kf : nanim.rotation)
						nodeAnim.rotation.add(new NodeKeyframe<Quaternion>(kf.keytime, kf.value));
				}
				if (nanim.scaling != null) {
					nodeAnim.scaling = new Array<NodeKeyframe<Vector3>>();
					for (final NodeKeyframe<Vector3> kf : nanim.scaling)
						nodeAnim.scaling.add(new NodeKeyframe<Vector3>(kf.keytime, kf.value));
				}
			}
			if (nodeAnim.translation != null || nodeAnim.rotation != null || nodeAnim.scaling != null)
				animation.nodeAnimations.add(nodeAnim);
		}
		if (animation.nodeAnimations.size > 0)
			animations.add(animation);
	}

	/**
	 * Traverses the Node hierarchy and collects {@link Renderable} instances for
	 * every node with a graphical representation. Renderables are obtained from the
	 * provided pool. The resulting array can be rendered via a {@link ModelBatch}.
	 *
	 * @param renderables the output array
	 * @param pool        the pool to obtain Renderables from
	 */
	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		for (Node node : nodes) {
			getRenderables(node, renderables, pool);
		}
	}

	protected void getRenderables(Node node, Array<Renderable> renderables, Pool<Renderable> pool) {
		if (node.parts.size > 0) {
			for (NodePart nodePart : node.parts) {
				if (!nodePart.enabled)
					continue;
				Renderable out = pool.obtain();
				nodePart.setRenderable(out);
				if (nodePart.bones == null && transform != null)
					out.worldTransform.set(transform).mul(node.globalTransform);
				else if (transform != null)
					out.worldTransform.set(transform);
				else
					out.worldTransform.idt();
				renderables.add(out);
			}
		}
		for (Node child : node.getChildren()) {
			getRenderables(child, renderables, pool);
		}
	}

	/**
	 * Calculates the local and world transform of all {@link Node} instances in
	 * this model, recursively. First each {@link Node#localTransform} transform is
	 * calculated based on the translation, rotation and scale of each Node. Then
	 * each {@link Node#calculateWorldTransform()} is calculated, based on the
	 * parent's world transform and the local transform of each Node. Finally, the
	 * animation bone matrices are updated accordingly.
	 * </p>
	 * <p>
	 * This method can be used to recalculate all transforms if any of the Node's
	 * local properties (translation, rotation, scale) was modified.
	 */
	public void calculateTransforms() {
		final int n = nodes.size;
		for (int i = 0; i < n; i++) {
			nodes.get(i).calculateTransforms(true);
		}
		for (int i = 0; i < n; i++) {
			nodes.get(i).calculateBoneTransforms(true);
		}
	}

	/**
	 * Calculate the bounding box of this model instance. This is a potential slow
	 * operation, it is advised to cache the result.
	 *
	 * @param out the {@link BoundingBox} that will be set with the bounds.
	 * @return the out parameter for chaining
	 */
	public BoundingBox calculateBoundingBox(final BoundingBox out) {
		out.inf();
		return extendBoundingBox(out);
	}

	/**
	 * Extends the bounding box with the bounds of this model instance. This is a
	 * potential slow operation, it is advised to cache the result.
	 *
	 * @param out the {@link BoundingBox} that will be extended with the bounds.
	 * @return the out parameter for chaining
	 */
	public BoundingBox extendBoundingBox(final BoundingBox out) {
		final int n = nodes.size;
		for (int i = 0; i < n; i++)
			nodes.get(i).extendBoundingBox(out);
		return out;
	}

	/**
	 * @param id The ID of the animation to fetch (case sensitive).
	 * @return The {@link Animation} with the specified id, or null if not
	 *         available.
	 */
	public Animation getAnimation(final String id) {
		return getAnimation(id, false);
	}

	/**
	 * @param id         The ID of the animation to fetch.
	 * @param ignoreCase whether to use case sensitivity when comparing the
	 *                   animation id.
	 * @return The {@link Animation} with the specified id, or null if not
	 *         available.
	 */
	public Animation getAnimation(final String id, boolean ignoreCase) {
		final int n = animations.size;
		Animation animation;
		if (ignoreCase) {
			for (int i = 0; i < n; i++)
				if ((animation = animations.get(i)).id.equalsIgnoreCase(id))
					return animation;
		} else {
			for (int i = 0; i < n; i++)
				if ((animation = animations.get(i)).id.equals(id))
					return animation;
		}
		return null;
	}

	/**
	 * @param id The ID of the material to fetch.
	 * @return The {@link Material} with the specified id, or null if not available.
	 */
	public Material getMaterial(final String id) {
		return getMaterial(id, true);
	}

	/**
	 * @param id         The ID of the material to fetch.
	 * @param ignoreCase whether to use case sensitivity when comparing the material
	 *                   id.
	 * @return The {@link Material} with the specified id, or null if not available.
	 */
	public Material getMaterial(final String id, boolean ignoreCase) {
		final int n = materials.size;
		Material material;
		if (ignoreCase) {
			for (int i = 0; i < n; i++)
				if ((material = materials.get(i)).id.equalsIgnoreCase(id))
					return material;
		} else {
			for (int i = 0; i < n; i++)
				if ((material = materials.get(i)).id.equals(id))
					return material;
		}
		return null;
	}

	/**
	 * @param id The ID of the node to fetch.
	 * @return The {@link Node} with the specified id, or null if not found.
	 */
	public Node getNode(final String id) {
		return getNode(id, true);
	}

	/**
	 * @param id        The ID of the node to fetch.
	 * @param recursive false to fetch a root node only, true to search the entire
	 *                  node tree for the specified node.
	 * @return The {@link Node} with the specified id, or null if not found.
	 */
	public Node getNode(final String id, boolean recursive) {
		return getNode(id, recursive, false);
	}

	/**
	 * @param id         The ID of the node to fetch.
	 * @param recursive  false to fetch a root node only, true to search the entire
	 *                   node tree for the specified node.
	 * @param ignoreCase whether to use case sensitivity when comparing the node id.
	 * @return The {@link Node} with the specified id, or null if not found.
	 */
	public Node getNode(final String id, boolean recursive, boolean ignoreCase) {
		return Node.getNode(nodes, id, recursive, ignoreCase);
	}
}
