package com.ariasaproject.advancerofrpg.graphics.g3d.utils;

import com.ariasaproject.advancerofrpg.graphics.g3d.ModelInstance;
import com.ariasaproject.advancerofrpg.graphics.g3d.model.Animation;
import com.ariasaproject.advancerofrpg.graphics.g3d.model.Node;
import com.ariasaproject.advancerofrpg.graphics.g3d.model.NodeAnimation;
import com.ariasaproject.advancerofrpg.graphics.g3d.model.NodeKeyframe;
import com.ariasaproject.advancerofrpg.math.MathUtils;
import com.ariasaproject.advancerofrpg.math.Quaternion;
import com.ariasaproject.advancerofrpg.math.Vector3;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.ObjectMap;
import com.ariasaproject.advancerofrpg.utils.ObjectMap.Entry;
import com.ariasaproject.advancerofrpg.utils.Pool;
import com.ariasaproject.advancerofrpg.math.Transform;

public class AnimationController {
	public AnimationDesc current;
	public AnimationDesc queued;
	public float queuedTransitionTime;
	public AnimationDesc previous;
	public float transitionCurrentTime;
	public float transitionTargetTime;
	public boolean inAction;
	public boolean paused;
	public boolean allowSameAnimation;
	private boolean justChangedAnimation = false;

	private final static ObjectMap<Node, Transform> transforms = new ObjectMap<Node, Transform>();
	private final static Transform tmpT = new Transform();

	public final ModelInstance target;
	private final Pool<Transform> transformPool = new Pool<Transform>() {
		@Override
		protected Transform newObject() {
			return new Transform();
		}
	};
	private boolean applying = false;

	protected final Pool<AnimationDesc> animationPool = new Pool<AnimationDesc>() {
		@Override
		protected AnimationDesc newObject() {
			return new AnimationDesc();
		}
	};
	public AnimationController(final ModelInstance target) {
		this.target = target;
	}


	final static <T> int getFirstKeyframeIndexAtTime(final Array<NodeKeyframe<T>> arr, final float time) {
		final int lastIndex = arr.size - 1;
		// edges cases : time out of range always return first index
		if (lastIndex <= 0 || time < arr.get(0).keytime || time > arr.get(lastIndex).keytime) {
			return 0;
		}
		// binary search
		int minIndex = 0;
		int maxIndex = lastIndex;
		while (minIndex < maxIndex) {
			int i = (minIndex + maxIndex) / 2;
			if (time > arr.get(i + 1).keytime) {
				minIndex = i + 1;
			} else if (time < arr.get(i).keytime) {
				maxIndex = i - 1;
			} else {
				return i;
			}
		}
		return minIndex;
	}

	private final static Transform getNodeAnimationTransform(final NodeAnimation nodeAnim, final float time) {
		final Transform transform = tmpT;
		if (nodeAnim.translation == null)
			transform.translation.set(nodeAnim.node.translation);
		else if (nodeAnim.translation.size == 1)
			transform.translation.set(nodeAnim.translation.get(0).value);
		else {
			int index = getFirstKeyframeIndexAtTime(nodeAnim.translation, time);
			final NodeKeyframe firstKeyframe = nodeAnim.translation.get(index);
			transform.translation.set((Vector3) firstKeyframe.value);
			if (++index < nodeAnim.translation.size) {
				final NodeKeyframe<Vector3> secondKeyframe = nodeAnim.translation.get(index);
				final float t = (time - firstKeyframe.keytime) / (secondKeyframe.keytime - firstKeyframe.keytime);
				transform.translation.lerp(secondKeyframe.value, t);
			}
		}
		if (nodeAnim.rotation == null) {
			transform.rotation.set(nodeAnim.node.rotation);
		} else if (nodeAnim.rotation.size == 1)
			transform.rotation.set(nodeAnim.rotation.get(0).value);
		else {
			int index = getFirstKeyframeIndexAtTime(nodeAnim.rotation, time);
			final NodeKeyframe firstKeyframe = nodeAnim.rotation.get(index);
			transform.rotation.set((Quaternion) firstKeyframe.value);
			if (++index < nodeAnim.rotation.size) {
				final NodeKeyframe<Quaternion> secondKeyframe = nodeAnim.rotation.get(index);
				final float t = (time - firstKeyframe.keytime) / (secondKeyframe.keytime - firstKeyframe.keytime);
				transform.rotation.slerp(secondKeyframe.value, t);
			}
		}
		if (nodeAnim.scaling == null) {
			transform.scale.set(nodeAnim.node.scale);
		} else if (nodeAnim.scaling.size == 1) {
			transform.scale.set(nodeAnim.scaling.get(0).value);
		} else {
			int index = getFirstKeyframeIndexAtTime(nodeAnim.scaling, time);
			final NodeKeyframe firstKeyframe = nodeAnim.scaling.get(index);
			transform.scale.set((Vector3) firstKeyframe.value);
			if (++index < nodeAnim.scaling.size) {
				final NodeKeyframe<Vector3> secondKeyframe = nodeAnim.scaling.get(index);
				final float t = (time - firstKeyframe.keytime) / (secondKeyframe.keytime - firstKeyframe.keytime);
				transform.scale.lerp(secondKeyframe.value, t);
			}
		}
		return transform;
	}

	/**
	 * Begin applying multiple animations to the instance, must followed by one or
	 * more calls to { {@link #apply(Animation, float, float)} and finally
	 * {{@link #end()}.
	 */
	protected void begin() {
		if (applying)
			throw new RuntimeException("You must call end() after each call to being()");
		applying = true;
	}

	protected void apply(final Animation animation, final float time, final float weight) {
		if (!applying)
			throw new RuntimeException("You must call begin() before adding an animation");
		for (final Node node : transforms.keys())
			node.isAnimated = false;
		for (final NodeAnimation nodeAnim : animation.nodeAnimations) {
			final Node node = nodeAnim.node;
			node.isAnimated = true;
			final Transform transform = getNodeAnimationTransform(nodeAnim, time);
			Transform t = transforms.get(node, null);
			if (t != null) {
				t.lerp(transform, weight);
			} else {
				transforms.put(node, transformPool.obtain().set(node.translation, node.rotation, node.scale).lerp(transform, weight));
			}
		}
		for (final ObjectMap.Entry<Node, Transform> e : transforms.entries()) {
			if (!e.key.isAnimated) {
				e.key.isAnimated = true;
				e.value.lerp(e.key.translation, e.key.rotation, e.key.scale, weight);
			}
		}
	}

	/**
	 * End applying multiple animations to the instance and update it to reflect the
	 * changes.
	 */
	protected void end() {
		if (!applying)
			throw new RuntimeException("You must call begin() first");
		for (Entry<Node, Transform> entry : transforms.entries()) {
			entry.key.localTransform.set(entry.value.getMatrix());
			transformPool.free(entry.value);
		}
		transforms.clear();
		target.calculateTransforms();
		applying = false;
	}

	/**
	 * Apply a single animation to the {@link ModelInstance} and update the it to
	 * reflect the changes.
	 */
	protected void applyAnimation(final Animation animation, final float time) {
		if (applying)
			throw new RuntimeException("Call end() first");
		for (final NodeAnimation nodeAnim : animation.nodeAnimations) {
			final Node node = nodeAnim.node;
			node.isAnimated = true;
			final Transform transform = getNodeAnimationTransform(nodeAnim, time);
			node.localTransform.set(transform.getMatrix());
		}
		target.calculateTransforms();
	}

	/**
	 * Apply two animations, blending the second onto to first using weight.
	 */
	protected void applyAnimations(final Animation anim1, final float time1, final Animation anim2, final float time2,
								   final float weight) {
		if (anim2 == null || weight == 0.f)
			applyAnimation(anim1, time1);
		else if (anim1 == null || weight == 1.f)
			applyAnimation(anim2, time2);
		else if (applying)
			throw new RuntimeException("Call end() first");
		else {
			begin();
			apply(anim1, time1, 1.f);
			apply(anim2, time2, weight);
			end();
		}
	}

	protected void removeAnimation(final Animation animation) {
		for (final NodeAnimation nodeAnim : animation.nodeAnimations) {
			nodeAnim.node.isAnimated = false;
		}
	}

	private AnimationDesc obtain(final Animation anim, float offset, float duration, int loopCount, float speed,
								 final AnimationListener listener) {
		if (anim == null)
			return null;
		final AnimationDesc result = animationPool.obtain();
		result.animation = anim;
		result.listener = listener;
		result.loopCount = loopCount;
		result.speed = speed;
		result.offset = offset;
		result.duration = duration < 0 ? (anim.duration - offset) : duration;
		result.time = speed < 0 ? result.duration : 0.f;
		return result;
	}

	private AnimationDesc obtain(final String id, float offset, float duration, int loopCount, float speed,
								 final AnimationListener listener) {
		if (id == null)
			return null;
		final Animation anim = target.getAnimation(id);
		if (anim == null)
			throw new RuntimeException("Unknown animation: " + id);
		return obtain(anim, offset, duration, loopCount, speed, listener);
	}

	private AnimationDesc obtain(final AnimationDesc anim) {
		return obtain(anim.animation, anim.offset, anim.duration, anim.loopCount, anim.speed, anim.listener);
	}

	/**
	 * Update any animations currently being played.
	 *
	 * @param delta The time elapsed since last update, change this to alter the
	 *              overall speed (can be negative).
	 */
	public void update(float delta) {
		if (paused)
			return;
		if (previous != null && ((transitionCurrentTime += delta) >= transitionTargetTime)) {
			removeAnimation(previous.animation);
			justChangedAnimation = true;
			animationPool.free(previous);
			previous = null;
		}
		if (justChangedAnimation) {
			target.calculateTransforms();
			justChangedAnimation = false;
		}
		if (current == null || current.loopCount == 0 || current.animation == null)
			return;
		final float remain = current.update(delta);
		if (remain != 0f && queued != null) {
			inAction = false;
			animate(queued, queuedTransitionTime);
			queued = null;
			update(remain);
			return;
		}
		if (previous != null)
			applyAnimations(previous.animation, previous.offset + previous.time, current.animation,
							current.offset + current.time, transitionCurrentTime / transitionTargetTime);
		else
			applyAnimation(current.animation, current.offset + current.time);
	}

	/**
	 * Set the active animation, replacing any current animation.
	 *
	 * @param id The ID of the {@link Animation} within the {@link ModelInstance}.
	 * @return The {@link AnimationDesc} which can be read to get the progress of
	 *         the animation. Will be invalid when the animation is completed.
	 */
	public AnimationDesc setAnimation(final String id) {
		return setAnimation(id, 1, 1.0f, null);
	}

	/**
	 * Set the active animation, replacing any current animation.
	 *
	 * @param id        The ID of the {@link Animation} within the
	 *                  {@link ModelInstance}.
	 * @param loopCount The number of times to loop the animation, zero to play the
	 *                  animation only once, negative to continuously loop the
	 *                  animation.
	 * @return The {@link AnimationDesc} which can be read to get the progress of
	 *         the animation. Will be invalid when the animation is completed.
	 */
	public AnimationDesc setAnimation(final String id, int loopCount) {
		return setAnimation(id, loopCount, 1.0f, null);
	}

	/**
	 * Set the active animation, replacing any current animation.
	 *
	 * @param id       The ID of the {@link Animation} within the
	 *                 {@link ModelInstance}.
	 * @param listener The {@link AnimationListener} which will be informed when the
	 *                 animation is looped or completed.
	 * @return The {@link AnimationDesc} which can be read to get the progress of
	 *         the animation. Will be invalid when the animation is completed.
	 */
	public AnimationDesc setAnimation(final String id, final AnimationListener listener) {
		return setAnimation(id, 1, 1.0f, listener);
	}

	/**
	 * Set the active animation, replacing any current animation.
	 *
	 * @param id        The ID of the {@link Animation} within the
	 *                  {@link ModelInstance}.
	 * @param loopCount The number of times to loop the animation, zero to play the
	 *                  animation only once, negative to continuously loop the
	 *                  animation.
	 * @param listener  The {@link AnimationListener} which will be informed when
	 *                  the animation is looped or completed.
	 * @return The {@link AnimationDesc} which can be read to get the progress of
	 *         the animation. Will be invalid when the animation is completed.
	 */
	public AnimationDesc setAnimation(final String id, int loopCount, final AnimationListener listener) {
		return setAnimation(id, loopCount, 1.0f, listener);
	}

	/**
	 * Set the active animation, replacing any current animation.
	 *
	 * @param id        The ID of the {@link Animation} within the
	 *                  {@link ModelInstance}.
	 * @param loopCount The number of times to loop the animation, zero to play the
	 *                  animation only once, negative to continuously loop the
	 *                  animation.
	 * @param speed     The speed at which the animation should be played. Default
	 *                  is 1.0f. A value of 2.0f will play the animation at twice
	 *                  the normal speed, a value of 0.5f will play the animation at
	 *                  half the normal speed, etc. This value can be negative,
	 *                  causing the animation to played in reverse. This value
	 *                  cannot be zero.
	 * @param listener  The {@link AnimationListener} which will be informed when
	 *                  the animation is looped or completed.
	 * @return The {@link AnimationDesc} which can be read to get the progress of
	 *         the animation. Will be invalid when the animation is completed.
	 */
	public AnimationDesc setAnimation(final String id, int loopCount, float speed, final AnimationListener listener) {
		return setAnimation(id, 0f, -1f, loopCount, speed, listener);
	}

	/**
	 * Set the active animation, replacing any current animation.
	 *
	 * @param id        The ID of the {@link Animation} within the
	 *                  {@link ModelInstance}.
	 * @param offset    The offset in seconds to the start of the animation.
	 * @param duration  The duration in seconds of the animation (or negative to
	 *                  play till the end of the animation).
	 * @param loopCount The number of times to loop the animation, zero to play the
	 *                  animation only once, negative to continuously loop the
	 *                  animation.
	 * @param speed     The speed at which the animation should be played. Default
	 *                  is 1.0f. A value of 2.0f will play the animation at twice
	 *                  the normal speed, a value of 0.5f will play the animation at
	 *                  half the normal speed, etc. This value can be negative,
	 *                  causing the animation to played in reverse. This value
	 *                  cannot be zero.
	 * @param listener  The {@link AnimationListener} which will be informed when
	 *                  the animation is looped or completed.
	 * @return The {@link AnimationDesc} which can be read to get the progress of
	 *         the animation. Will be invalid when the animation is completed.
	 */
	public AnimationDesc setAnimation(final String id, float offset, float duration, int loopCount, float speed,
									  final AnimationListener listener) {
		return setAnimation(obtain(id, offset, duration, loopCount, speed, listener));
	}

	/**
	 * Set the active animation, replacing any current animation.
	 */
	protected AnimationDesc setAnimation(final Animation anim, float offset, float duration, int loopCount, float speed,
										 final AnimationListener listener) {
		return setAnimation(obtain(anim, offset, duration, loopCount, speed, listener));
	}

	/**
	 * Set the active animation, replacing any current animation.
	 */
	protected AnimationDesc setAnimation(final AnimationDesc anim) {
		if (current == null)
			current = anim;
		else {
			if (!allowSameAnimation && anim != null && current.animation == anim.animation)
				anim.time = current.time;
			else
				removeAnimation(current.animation);
			animationPool.free(current);
			current = anim;
		}
		justChangedAnimation = true;
		return anim;
	}

	/**
	 * Changes the current animation by blending the new on top of the old during
	 * the transition time.
	 *
	 * @param id             The ID of the {@link Animation} within the
	 *                       {@link ModelInstance}.
	 * @param transitionTime The time to transition the new animation on top of the
	 *                       currently playing animation (if any).
	 * @return The {@link AnimationDesc} which can be read to get the progress of
	 *         the animation. Will be invalid when the animation is completed.
	 */
	public AnimationDesc animate(final String id, float transitionTime) {
		return animate(id, 1, 1.0f, null, transitionTime);
	}

	/**
	 * Changes the current animation by blending the new on top of the old during
	 * the transition time.
	 *
	 * @param id             The ID of the {@link Animation} within the
	 *                       {@link ModelInstance}.
	 * @param listener       The {@link AnimationListener} which will be informed
	 *                       when the animation is looped or completed.
	 * @param transitionTime The time to transition the new animation on top of the
	 *                       currently playing animation (if any).
	 * @return The {@link AnimationDesc} which can be read to get the progress of
	 *         the animation. Will be invalid when the animation is completed.
	 */
	public AnimationDesc animate(final String id, final AnimationListener listener, float transitionTime) {
		return animate(id, 1, 1.0f, listener, transitionTime);
	}

	/**
	 * Changes the current animation by blending the new on top of the old during
	 * the transition time.
	 *
	 * @param id             The ID of the {@link Animation} within the
	 *                       {@link ModelInstance}.
	 * @param loopCount      The number of times to loop the animation, zero to play
	 *                       the animation only once, negative to continuously loop
	 *                       the animation.
	 * @param listener       The {@link AnimationListener} which will be informed
	 *                       when the animation is looped or completed.
	 * @param transitionTime The time to transition the new animation on top of the
	 *                       currently playing animation (if any).
	 * @return The {@link AnimationDesc} which can be read to get the progress of
	 *         the animation. Will be invalid when the animation is completed.
	 */
	public AnimationDesc animate(final String id, int loopCount, final AnimationListener listener,
								 float transitionTime) {
		return animate(id, loopCount, 1.0f, listener, transitionTime);
	}

	/**
	 * Changes the current animation by blending the new on top of the old during
	 * the transition time.
	 *
	 * @param id             The ID of the {@link Animation} within the
	 *                       {@link ModelInstance}.
	 * @param loopCount      The number of times to loop the animation, zero to play
	 *                       the animation only once, negative to continuously loop
	 *                       the animation.
	 * @param speed          The speed at which the animation should be played.
	 *                       Default is 1.0f. A value of 2.0f will play the
	 *                       animation at twice the normal speed, a value of 0.5f
	 *                       will play the animation at half the normal speed, etc.
	 *                       This value can be negative, causing the animation to
	 *                       played in reverse. This value cannot be zero.
	 * @param listener       The {@link AnimationListener} which will be informed
	 *                       when the animation is looped or completed.
	 * @param transitionTime The time to transition the new animation on top of the
	 *                       currently playing animation (if any).
	 * @return The {@link AnimationDesc} which can be read to get the progress of
	 *         the animation. Will be invalid when the animation is completed.
	 */
	public AnimationDesc animate(final String id, int loopCount, float speed, final AnimationListener listener,
								 float transitionTime) {
		return animate(id, 0f, -1f, loopCount, speed, listener, transitionTime);
	}

	/**
	 * Changes the current animation by blending the new on top of the old during
	 * the transition time.
	 *
	 * @param id             The ID of the {@link Animation} within the
	 *                       {@link ModelInstance}.
	 * @param offset         The offset in seconds to the start of the animation.
	 * @param duration       The duration in seconds of the animation (or negative
	 *                       to play till the end of the animation).
	 * @param loopCount      The number of times to loop the animation, zero to play
	 *                       the animation only once, negative to continuously loop
	 *                       the animation.
	 * @param speed          The speed at which the animation should be played.
	 *                       Default is 1.0f. A value of 2.0f will play the
	 *                       animation at twice the normal speed, a value of 0.5f
	 *                       will play the animation at half the normal speed, etc.
	 *                       This value can be negative, causing the animation to
	 *                       played in reverse. This value cannot be zero.
	 * @param listener       The {@link AnimationListener} which will be informed
	 *                       when the animation is looped or completed.
	 * @param transitionTime The time to transition the new animation on top of the
	 *                       currently playing animation (if any).
	 * @return The {@link AnimationDesc} which can be read to get the progress of
	 *         the animation. Will be invalid when the animation is completed.
	 */
	public AnimationDesc animate(final String id, float offset, float duration, int loopCount, float speed,
								 final AnimationListener listener, float transitionTime) {
		return animate(obtain(id, offset, duration, loopCount, speed, listener), transitionTime);
	}

	/**
	 * Changes the current animation by blending the new on top of the old during
	 * the transition time.
	 */
	protected AnimationDesc animate(final Animation anim, float offset, float duration, int loopCount, float speed,
									final AnimationListener listener, float transitionTime) {
		return animate(obtain(anim, offset, duration, loopCount, speed, listener), transitionTime);
	}

	/**
	 * Changes the current animation by blending the new on top of the old during
	 * the transition time.
	 */
	protected AnimationDesc animate(final AnimationDesc anim, float transitionTime) {
		if (current == null)
			current = anim;
		else if (inAction)
			queue(anim, transitionTime);
		else if (!allowSameAnimation && anim != null && current.animation == anim.animation) {
			anim.time = current.time;
			animationPool.free(current);
			current = anim;
		} else {
			if (previous != null) {
				removeAnimation(previous.animation);
				animationPool.free(previous);
			}
			previous = current;
			current = anim;
			transitionCurrentTime = 0f;
			transitionTargetTime = transitionTime;
		}
		return anim;
	}

	/**
	 * Queue an animation to be applied when the {@link #current} animation is
	 * finished. If the current animation is continuously looping it will be
	 * synchronized on next loop.
	 *
	 * @param id             The ID of the {@link Animation} within the
	 *                       {@link ModelInstance}.
	 * @param loopCount      The number of times to loop the animation, zero to play
	 *                       the animation only once, negative to continuously loop
	 *                       the animation.
	 * @param speed          The speed at which the animation should be played.
	 *                       Default is 1.0f. A value of 2.0f will play the
	 *                       animation at twice the normal speed, a value of 0.5f
	 *                       will play the animation at half the normal speed, etc.
	 *                       This value can be negative, causing the animation to
	 *                       played in reverse. This value cannot be zero.
	 * @param listener       The {@link AnimationListener} which will be informed
	 *                       when the animation is looped or completed.
	 * @param transitionTime The time to transition the new animation on top of the
	 *                       currently playing animation (if any).
	 * @return The {@link AnimationDesc} which can be read to get the progress of
	 *         the animation. Will be invalid when the animation is completed.
	 */
	public AnimationDesc queue(final String id, int loopCount, float speed, final AnimationListener listener,
							   float transitionTime) {
		return queue(id, 0f, -1f, loopCount, speed, listener, transitionTime);
	}

	/**
	 * Queue an animation to be applied when the {@link #current} animation is
	 * finished. If the current animation is continuously looping it will be
	 * synchronized on next loop.
	 *
	 * @param id             The ID of the {@link Animation} within the
	 *                       {@link ModelInstance}.
	 * @param offset         The offset in seconds to the start of the animation.
	 * @param duration       The duration in seconds of the animation (or negative
	 *                       to play till the end of the animation).
	 * @param loopCount      The number of times to loop the animation, zero to play
	 *                       the animation only once, negative to continuously loop
	 *                       the animation.
	 * @param speed          The speed at which the animation should be played.
	 *                       Default is 1.0f. A value of 2.0f will play the
	 *                       animation at twice the normal speed, a value of 0.5f
	 *                       will play the animation at half the normal speed, etc.
	 *                       This value can be negative, causing the animation to
	 *                       played in reverse. This value cannot be zero.
	 * @param listener       The {@link AnimationListener} which will be informed
	 *                       when the animation is looped or completed.
	 * @param transitionTime The time to transition the new animation on top of the
	 *                       currently playing animation (if any).
	 * @return The {@link AnimationDesc} which can be read to get the progress of
	 *         the animation. Will be invalid when the animation is completed.
	 */
	public AnimationDesc queue(final String id, float offset, float duration, int loopCount, float speed,
							   final AnimationListener listener, float transitionTime) {
		return queue(obtain(id, offset, duration, loopCount, speed, listener), transitionTime);
	}

	/**
	 * Queue an animation to be applied when the current is finished. If current is
	 * continuous it will be synced on next loop.
	 */
	protected AnimationDesc queue(final Animation anim, float offset, float duration, int loopCount, float speed,
								  final AnimationListener listener, float transitionTime) {
		return queue(obtain(anim, offset, duration, loopCount, speed, listener), transitionTime);
	}

	/**
	 * Queue an animation to be applied when the current is finished. If current is
	 * continuous it will be synced on next loop.
	 */
	protected AnimationDesc queue(final AnimationDesc anim, float transitionTime) {
		if (current == null || current.loopCount == 0)
			animate(anim, transitionTime);
		else {
			if (queued != null)
				animationPool.free(queued);
			queued = anim;
			queuedTransitionTime = transitionTime;
			if (current.loopCount < 0)
				current.loopCount = 1;
		}
		return anim;
	}

	/**
	 * Apply an action animation on top of the current animation.
	 *
	 * @param id             The ID of the {@link Animation} within the
	 *                       {@link ModelInstance}.
	 * @param loopCount      The number of times to loop the animation, zero to play
	 *                       the animation only once, negative to continuously loop
	 *                       the animation.
	 * @param speed          The speed at which the animation should be played.
	 *                       Default is 1.0f. A value of 2.0f will play the
	 *                       animation at twice the normal speed, a value of 0.5f
	 *                       will play the animation at half the normal speed, etc.
	 *                       This value can be negative, causing the animation to
	 *                       played in reverse. This value cannot be zero.
	 * @param listener       The {@link AnimationListener} which will be informed
	 *                       when the animation is looped or completed.
	 * @param transitionTime The time to transition the new animation on top of the
	 *                       currently playing animation (if any).
	 * @return The {@link AnimationDesc} which can be read to get the progress of
	 *         the animation. Will be invalid when the animation is completed.
	 */
	public AnimationDesc action(final String id, int loopCount, float speed, final AnimationListener listener,
								float transitionTime) {
		return action(id, 0, -1f, loopCount, speed, listener, transitionTime);
	}

	/**
	 * Apply an action animation on top of the current animation.
	 *
	 * @param id             The ID of the {@link Animation} within the
	 *                       {@link ModelInstance}.
	 * @param offset         The offset in seconds to the start of the animation.
	 * @param duration       The duration in seconds of the animation (or negative
	 *                       to play till the end of the animation).
	 * @param loopCount      The number of times to loop the animation, zero to play
	 *                       the animation only once, negative to continuously loop
	 *                       the animation.
	 * @param speed          The speed at which the animation should be played.
	 *                       Default is 1.0f. A value of 2.0f will play the
	 *                       animation at twice the normal speed, a value of 0.5f
	 *                       will play the animation at half the normal speed, etc.
	 *                       This value can be negative, causing the animation to
	 *                       played in reverse. This value cannot be zero.
	 * @param listener       The {@link AnimationListener} which will be informed
	 *                       when the animation is looped or completed.
	 * @param transitionTime The time to transition the new animation on top of the
	 *                       currently playing animation (if any).
	 * @return The {@link AnimationDesc} which can be read to get the progress of
	 *         the animation. Will be invalid when the animation is completed.
	 */
	public AnimationDesc action(final String id, float offset, float duration, int loopCount, float speed,
								final AnimationListener listener, float transitionTime) {
		return action(obtain(id, offset, duration, loopCount, speed, listener), transitionTime);
	}

	/**
	 * Apply an action animation on top of the current animation.
	 */
	protected AnimationDesc action(final Animation anim, float offset, float duration, int loopCount, float speed,
								   final AnimationListener listener, float transitionTime) {
		return action(obtain(anim, offset, duration, loopCount, speed, listener), transitionTime);
	}

	/**
	 * Apply an action animation on top of the current animation.
	 */
	protected AnimationDesc action(final AnimationDesc anim, float transitionTime) {
		if (anim.loopCount < 0)
			throw new RuntimeException("An action cannot be continuous");
		if (current == null || current.loopCount == 0)
			animate(anim, transitionTime);
		else {
			AnimationDesc toQueue = inAction ? null : obtain(current);
			inAction = false;
			animate(anim, transitionTime);
			inAction = true;
			if (toQueue != null)
				queue(toQueue, transitionTime);
		}
		return anim;
	}

	/**
	 * Listener that will be informed when an animation is looped or completed.
	 *
	 * @author Xoppa
	 */
	public interface AnimationListener {
		/**
		 * Gets called when an animation is completed.
		 *
		 * @param animation The animation which just completed.
		 */
		void onEnd(final AnimationDesc animation);

		/**
		 * Gets called when an animation is looped. The {@link AnimationDesc#loopCount}
		 * is updated prior to this call and can be read or written to alter the number
		 * of remaining loops.
		 *
		 * @param animation The animation which just looped.
		 */
		void onLoop(final AnimationDesc animation);
	}

	/**
	 * Class describing how to play and {@link Animation}. You can read the values
	 * within this class to get the progress of the animation. Do not change the
	 * values. Only valid when the animation is currently played.
	 *
	 * @author Xoppa
	 */
	public static class AnimationDesc {
		/**
		 * Listener which will be informed when the animation is looped or ended.
		 */
		public AnimationListener listener;
		/**
		 * The animation to be applied.
		 */
		public Animation animation;
		/**
		 * The speed at which to play the animation (can be negative), 1.0 for normal
		 * speed.
		 */
		public float speed;
		/**
		 * The current animation time.
		 */
		public float time;
		/**
		 * The offset within the animation (animation time = offsetTime + time)
		 */
		public float offset;
		/**
		 * The duration of the animation
		 */
		public float duration;
		/**
		 * The number of remaining loops, negative for continuous, zero if stopped.
		 */
		public int loopCount;

		protected AnimationDesc() {
		}

		/**
		 * @return the remaining time or 0 if still animating.
		 */
		protected float update(float delta) {
			if (loopCount != 0 && animation != null) {
				int loops;
				final float diff = speed * delta;
				if (!MathUtils.isZero(duration)) {
					time += diff;
					loops = (int) Math.abs(time / duration);
					if (time < 0f) {
						loops++;
						while (time < 0f)
							time += duration;
					}
					time = Math.abs(time % duration);
				} else
					loops = 1;
				for (int i = 0; i < loops; i++) {
					if (loopCount > 0)
						loopCount--;
					if (loopCount != 0 && listener != null)
						listener.onLoop(this);
					if (loopCount == 0) {
						final float result = ((loops - 1) - i) * duration + (diff < 0f ? duration - time : time);
						time = (diff < 0f) ? 0f : duration;
						if (listener != null)
							listener.onEnd(this);
						return result;
					}
				}
				return 0f;
			} else
				return delta;
		}
	}
}
