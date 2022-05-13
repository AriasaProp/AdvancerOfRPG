package com.ariasaproject.advancerofrpg.graphics.glutils;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.Cubemap;
import com.ariasaproject.advancerofrpg.graphics.Pixmap;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.graphics.Texture.TextureFilter;
import com.ariasaproject.advancerofrpg.graphics.Texture.TextureWrap;

public class FrameBufferCubemap extends GLFrameBuffer<Cubemap> {
	private static final Cubemap.CubemapSide[] cubemapSides = Cubemap.CubemapSide.values();
	private int currentSide;

	FrameBufferCubemap() {
	}

	protected FrameBufferCubemap(GLFrameBufferBuilder<? extends GLFrameBuffer<Cubemap>> bufferBuilder) {
		super(bufferBuilder);
	}

	public FrameBufferCubemap(Pixmap.Format format, int width, int height, boolean hasDepth) {
		this(format, width, height, hasDepth, false);
	}

	public FrameBufferCubemap(Pixmap.Format format, int width, int height, boolean hasDepth, boolean hasStencil) {
		FrameBufferCubemapBuilder frameBufferBuilder = new FrameBufferCubemapBuilder(width, height);
		frameBufferBuilder.addBasicColorTextureAttachment(format);
		if (hasDepth)
			frameBufferBuilder.addBasicDepthRenderBuffer();
		if (hasStencil)
			frameBufferBuilder.addBasicStencilRenderBuffer();
		this.bufferBuilder = frameBufferBuilder;
		build();
	}

	@Override
	protected Cubemap createTexture(FrameBufferTextureAttachmentSpec attachmentSpec) {
		GLOnlyTextureData data = new GLOnlyTextureData(bufferBuilder.width, bufferBuilder.height, 0,
				attachmentSpec.internalFormat, attachmentSpec.format, attachmentSpec.type);
		Cubemap result = new Cubemap(data, data, data, data, data, data);
		result.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		result.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		return result;
	}

	@Override
	protected void disposeColorTexture(Cubemap colorTexture) {
		colorTexture.dispose();
	}

	@Override
	protected void attachFrameBufferColorTexture(Cubemap texture) {
		int glHandle = texture.getTextureObjectHandle();
		Cubemap.CubemapSide[] sides = Cubemap.CubemapSide.values();
		for (Cubemap.CubemapSide side : sides) {
			GraphFunc.tgf.glFramebufferTexture2D(TGF.GL_FRAMEBUFFER, TGF.GL_COLOR_ATTACHMENT0, side.glEnum, glHandle, 0);
		}
	}

	/**
	 * Makes the frame buffer current so everything gets drawn to it, must be
	 * followed by call to either {@link #nextSide()} or
	 * {@link #bindSide(com.ariasaproject.advancerofrpg.graphics.Cubemap.CubemapSide)}
	 * to activate the side to render onto.
	 */
	@Override
	public void bind() {
		currentSide = -1;
		super.bind();
	}

	/**
	 * Bind the next side of cubemap and return false if no more side. Should be
	 * called in between a call to {@link #begin()} and #end to cycle to each side
	 * of the cubemap to render on.
	 */
	public boolean nextSide() {
		if (currentSide > 5) {
			throw new RuntimeException("No remaining sides.");
		} else if (currentSide == 5) {
			return false;
		}
		currentSide++;
		bindSide(getSide());
		return true;
	}

	/**
	 * Bind the side, making it active to render on. Should be called in between a
	 * call to {@link #begin()} and {@link #end()}.
	 *
	 * @param side The side to bind
	 */
	protected void bindSide(final Cubemap.CubemapSide side) {
		GraphFunc.tgf.glFramebufferTexture2D(TGF.GL_FRAMEBUFFER, TGF.GL_COLOR_ATTACHMENT0, side.glEnum,
				getColorBufferTexture().getTextureObjectHandle(), 0);
	}

	/**
	 * Get the currently bound side.
	 */
	public Cubemap.CubemapSide getSide() {
		return currentSide < 0 ? null : cubemapSides[currentSide];
	}
}
