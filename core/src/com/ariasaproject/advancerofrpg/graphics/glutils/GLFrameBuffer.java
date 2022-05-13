package com.ariasaproject.advancerofrpg.graphics.glutils;

import java.nio.IntBuffer;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.GLTexture;
import com.ariasaproject.advancerofrpg.graphics.Pixmap;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.BufferUtils;
import com.ariasaproject.advancerofrpg.utils.Disposable;

public abstract class GLFrameBuffer<T extends GLTexture> implements Disposable {
	protected final static Array<GLFrameBuffer> buffers = new Array<GLFrameBuffer>();
	protected final static int GL_DEPTH24_STENCIL8_OES = 0x88F0;
	protected Array<T> textureAttachments = new Array<T>();
	protected int framebufferHandle;
	protected int depthbufferHandle;
	protected int stencilbufferHandle;
	protected int depthStencilPackedBufferHandle;
	protected boolean hasDepthStencilPackedBuffer;
	protected boolean isMRT;

	protected GLFrameBufferBuilder<? extends GLFrameBuffer<T>> bufferBuilder;

	GLFrameBuffer() {
	}

	protected GLFrameBuffer(GLFrameBufferBuilder<? extends GLFrameBuffer<T>> bufferBuilder) {
		this.bufferBuilder = bufferBuilder;
		build();
	}

	public static void unbind() {
		GraphFunc.tgf.glBindFramebuffer(TGF.GL_FRAMEBUFFER, 0);
	}

	public static void invalidateAllFrameBuffers() {
		for (GLFrameBuffer buffer : buffers) {
			buffer.build();
		}
	}

	public static void clearAllFrameBuffers() {
		buffers.clear();
	}

	public T getColorBufferTexture() {
		return textureAttachments.first();
	}

	public Array<T> getTextureAttachments() {
		return textureAttachments;
	}

	protected abstract T createTexture(FrameBufferTextureAttachmentSpec attachmentSpec);

	protected abstract void disposeColorTexture(T colorTexture);

	protected abstract void attachFrameBufferColorTexture(T texture);

	protected void build() {
		TGF tgf = GraphFunc.tgf;
		framebufferHandle = tgf.glGenFramebuffer();
		tgf.glBindFramebuffer(TGF.GL_FRAMEBUFFER, framebufferHandle);
		int width = bufferBuilder.width;
		int height = bufferBuilder.height;
		if (bufferBuilder.hasDepthRenderBuffer) {
			depthbufferHandle = tgf.glGenRenderbuffer();
			tgf.glBindRenderbuffer(TGF.GL_RENDERBUFFER, depthbufferHandle);
			tgf.glRenderbufferStorage(TGF.GL_RENDERBUFFER, bufferBuilder.depthRenderBufferSpec.internalFormat, width,
					height);
		}
		if (bufferBuilder.hasStencilRenderBuffer) {
			stencilbufferHandle = tgf.glGenRenderbuffer();
			tgf.glBindRenderbuffer(TGF.GL_RENDERBUFFER, stencilbufferHandle);
			tgf.glRenderbufferStorage(TGF.GL_RENDERBUFFER, bufferBuilder.stencilRenderBufferSpec.internalFormat, width,
					height);
		}
		if (bufferBuilder.hasPackedStencilDepthRenderBuffer) {
			depthStencilPackedBufferHandle = tgf.glGenRenderbuffer();
			tgf.glBindRenderbuffer(TGF.GL_RENDERBUFFER, depthStencilPackedBufferHandle);
			tgf.glRenderbufferStorage(TGF.GL_RENDERBUFFER,
					bufferBuilder.packedStencilDepthRenderBufferSpec.internalFormat, width, height);
		}
		isMRT = bufferBuilder.textureAttachmentSpecs.size > 1;
		int colorTextureCounter = 0;
		if (isMRT) {
			for (FrameBufferTextureAttachmentSpec attachmentSpec : bufferBuilder.textureAttachmentSpecs) {
				T texture = createTexture(attachmentSpec);
				textureAttachments.add(texture);
				if (attachmentSpec.isColorTexture()) {
					tgf.glFramebufferTexture2D(TGF.GL_FRAMEBUFFER, TGF.GL_COLOR_ATTACHMENT0 + colorTextureCounter,
							TGF.GL_TEXTURE_2D, texture.getTextureObjectHandle(), 0);
					colorTextureCounter++;
				} else if (attachmentSpec.isDepth) {
					tgf.glFramebufferTexture2D(TGF.GL_FRAMEBUFFER, TGF.GL_DEPTH_ATTACHMENT, TGF.GL_TEXTURE_2D,
							texture.getTextureObjectHandle(), 0);
				} else if (attachmentSpec.isStencil) {
					tgf.glFramebufferTexture2D(TGF.GL_FRAMEBUFFER, TGF.GL_STENCIL_ATTACHMENT, TGF.GL_TEXTURE_2D,
							texture.getTextureObjectHandle(), 0);
				}
			}
		} else {
			T texture = createTexture(bufferBuilder.textureAttachmentSpecs.first());
			textureAttachments.add(texture);
			tgf.glBindTexture(texture.glTarget, texture.getTextureObjectHandle());
		}
		if (isMRT) {
			final IntBuffer buffer = BufferUtils.newIntBuffer(colorTextureCounter);
			for (int i = 0; i < colorTextureCounter; i++) {
				buffer.put(TGF.GL_COLOR_ATTACHMENT0 + i);
			}
			buffer.position(0);
			tgf.glDrawBuffers(colorTextureCounter, buffer);
		} else {
			attachFrameBufferColorTexture(textureAttachments.first());
		}
		if (bufferBuilder.hasDepthRenderBuffer) {
			tgf.glFramebufferRenderbuffer(TGF.GL_FRAMEBUFFER, TGF.GL_DEPTH_ATTACHMENT, TGF.GL_RENDERBUFFER,
					depthbufferHandle);
		}
		if (bufferBuilder.hasStencilRenderBuffer) {
			tgf.glFramebufferRenderbuffer(TGF.GL_FRAMEBUFFER, TGF.GL_STENCIL_ATTACHMENT, TGF.GL_RENDERBUFFER,
					stencilbufferHandle);
		}
		if (bufferBuilder.hasPackedStencilDepthRenderBuffer) {
			tgf.glFramebufferRenderbuffer(TGF.GL_FRAMEBUFFER, TGF.GL_DEPTH_STENCIL_ATTACHMENT, TGF.GL_RENDERBUFFER,
					depthStencilPackedBufferHandle);
		}
		tgf.glBindRenderbuffer(TGF.GL_RENDERBUFFER, 0);
		for (T texture : textureAttachments) {
			tgf.glBindTexture(texture.glTarget, 0);
		}
		int result = tgf.glCheckFramebufferStatus(TGF.GL_FRAMEBUFFER);
		if (result == TGF.GL_FRAMEBUFFER_UNSUPPORTED && bufferBuilder.hasDepthRenderBuffer
				&& bufferBuilder.hasStencilRenderBuffer
				&& (GraphFunc.tgf.supportsExtension("GL_OES_packed_depth_stencil")
						|| GraphFunc.tgf.supportsExtension("GL_Ext_packed_depth_stencil"))) {
			if (bufferBuilder.hasDepthRenderBuffer) {
				tgf.glDeleteRenderbuffer(depthbufferHandle);
				depthbufferHandle = 0;
			}
			if (bufferBuilder.hasStencilRenderBuffer) {
				tgf.glDeleteRenderbuffer(stencilbufferHandle);
				stencilbufferHandle = 0;
			}
			if (bufferBuilder.hasPackedStencilDepthRenderBuffer) {
				tgf.glDeleteRenderbuffer(depthStencilPackedBufferHandle);
				depthStencilPackedBufferHandle = 0;
			}
			depthStencilPackedBufferHandle = tgf.glGenRenderbuffer();
			hasDepthStencilPackedBuffer = true;
			tgf.glBindRenderbuffer(TGF.GL_RENDERBUFFER, depthStencilPackedBufferHandle);
			tgf.glRenderbufferStorage(TGF.GL_RENDERBUFFER, GL_DEPTH24_STENCIL8_OES, width, height);
			tgf.glBindRenderbuffer(TGF.GL_RENDERBUFFER, 0);
			tgf.glFramebufferRenderbuffer(TGF.GL_FRAMEBUFFER, TGF.GL_DEPTH_ATTACHMENT, TGF.GL_RENDERBUFFER,
					depthStencilPackedBufferHandle);
			tgf.glFramebufferRenderbuffer(TGF.GL_FRAMEBUFFER, TGF.GL_STENCIL_ATTACHMENT, TGF.GL_RENDERBUFFER,
					depthStencilPackedBufferHandle);
			result = tgf.glCheckFramebufferStatus(TGF.GL_FRAMEBUFFER);
		}
		tgf.glBindFramebuffer(TGF.GL_FRAMEBUFFER, 0);
		if (result != TGF.GL_FRAMEBUFFER_COMPLETE) {
			for (T texture : textureAttachments) {
				disposeColorTexture(texture);
			}
			if (hasDepthStencilPackedBuffer) {
				tgf.glDeleteBuffer(depthStencilPackedBufferHandle);
			} else {
				if (bufferBuilder.hasDepthRenderBuffer)
					tgf.glDeleteRenderbuffer(depthbufferHandle);
				if (bufferBuilder.hasStencilRenderBuffer)
					tgf.glDeleteRenderbuffer(stencilbufferHandle);
			}
			tgf.glDeleteFramebuffer(framebufferHandle);
			if (result == TGF.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT)
				throw new IllegalStateException("Frame buffer couldn't be constructed: incomplete attachment");
			if (result == TGF.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS)
				throw new IllegalStateException("Frame buffer couldn't be constructed: incomplete dimensions");
			if (result == TGF.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT)
				throw new IllegalStateException("Frame buffer couldn't be constructed: missing attachment");
			if (result == TGF.GL_FRAMEBUFFER_UNSUPPORTED)
				throw new IllegalStateException(
						"Frame buffer couldn't be constructed: unsupported combination of formats");
			throw new IllegalStateException("Frame buffer couldn't be constructed: unknown error " + result);
		}
		buffers.add(this);
	}

	@Override
	public void dispose() {
		TGF tgf = GraphFunc.tgf;
		for (T texture : textureAttachments) {
			disposeColorTexture(texture);
		}
		if (hasDepthStencilPackedBuffer) {
			tgf.glDeleteRenderbuffer(depthStencilPackedBufferHandle);
		} else {
			if (bufferBuilder.hasDepthRenderBuffer)
				tgf.glDeleteRenderbuffer(depthbufferHandle);
			if (bufferBuilder.hasStencilRenderBuffer)
				tgf.glDeleteRenderbuffer(stencilbufferHandle);
		}
		tgf.glDeleteFramebuffer(framebufferHandle);
		buffers.removeValue(this, true);
	}

	public void bind() {
		GraphFunc.tgf.glBindFramebuffer(TGF.GL_FRAMEBUFFER, framebufferHandle);
	}

	public void begin() {
		bind();
		setFrameBufferViewport();
	}

	protected void setFrameBufferViewport() {
		GraphFunc.tgf.glViewport(0, 0, bufferBuilder.width, bufferBuilder.height);
	}

	public void end() {
		end(0, 0, GraphFunc.app.getGraphics().getWidth(), GraphFunc.app.getGraphics().getHeight());
	}

	public void end(int x, int y, int width, int height) {
		unbind();
		GraphFunc.tgf.glViewport(x, y, width, height);
	}

	public int getFramebufferHandle() {
		return framebufferHandle;
	}

	public int getDepthBufferHandle() {
		return depthbufferHandle;
	}

	public int getStencilBufferHandle() {
		return stencilbufferHandle;
	}

	protected int getDepthStencilPackedBuffer() {
		return depthStencilPackedBufferHandle;
	}

	public int getHeight() {
		return bufferBuilder.height;
	}

	public int getWidth() {
		return bufferBuilder.width;
	}

	protected static class FrameBufferTextureAttachmentSpec {
		int internalFormat, format, type;
		boolean isFloat, isGpuOnly;
		boolean isDepth;
		boolean isStencil;

		public FrameBufferTextureAttachmentSpec(int internalformat, int format, int type) {
			this.internalFormat = internalformat;
			this.format = format;
			this.type = type;
		}

		public boolean isColorTexture() {
			return !isDepth && !isStencil;
		}
	}

	protected static class FrameBufferRenderBufferAttachmentSpec {
		int internalFormat;

		public FrameBufferRenderBufferAttachmentSpec(int internalFormat) {
			this.internalFormat = internalFormat;
		}
	}

	protected static abstract class GLFrameBufferBuilder<U extends GLFrameBuffer<? extends GLTexture>> {
		protected int width, height;

		protected Array<FrameBufferTextureAttachmentSpec> textureAttachmentSpecs = new Array<FrameBufferTextureAttachmentSpec>();

		protected FrameBufferRenderBufferAttachmentSpec stencilRenderBufferSpec;
		protected FrameBufferRenderBufferAttachmentSpec depthRenderBufferSpec;
		protected FrameBufferRenderBufferAttachmentSpec packedStencilDepthRenderBufferSpec;

		protected boolean hasStencilRenderBuffer;
		protected boolean hasDepthRenderBuffer;
		protected boolean hasPackedStencilDepthRenderBuffer;

		public GLFrameBufferBuilder(int width, int height) {
			this.width = width;
			this.height = height;
		}

		public GLFrameBufferBuilder<U> addColorTextureAttachment(int internalFormat, int format, int type) {
			textureAttachmentSpecs.add(new FrameBufferTextureAttachmentSpec(internalFormat, format, type));
			return this;
		}

		public GLFrameBufferBuilder<U> addBasicColorTextureAttachment(Pixmap.Format format) {
			int glFormat = format.GLFormat;
			int glType = format.GLType;
			return addColorTextureAttachment(glFormat, glFormat, glType);
		}

		public GLFrameBufferBuilder<U> addFloatAttachment(int internalFormat, int format, int type, boolean gpuOnly) {
			FrameBufferTextureAttachmentSpec spec = new FrameBufferTextureAttachmentSpec(internalFormat, format, type);
			spec.isFloat = true;
			spec.isGpuOnly = gpuOnly;
			textureAttachmentSpecs.add(spec);
			return this;
		}

		public GLFrameBufferBuilder<U> addDepthTextureAttachment(int internalFormat, int type) {
			FrameBufferTextureAttachmentSpec spec = new FrameBufferTextureAttachmentSpec(internalFormat,
					TGF.GL_DEPTH_COMPONENT, type);
			spec.isDepth = true;
			textureAttachmentSpecs.add(spec);
			return this;
		}

		public GLFrameBufferBuilder<U> addStencilTextureAttachment(int internalFormat, int type) {
			FrameBufferTextureAttachmentSpec spec = new FrameBufferTextureAttachmentSpec(internalFormat,
					TGF.GL_STENCIL_ATTACHMENT, type);
			spec.isStencil = true;
			textureAttachmentSpecs.add(spec);
			return this;
		}

		public GLFrameBufferBuilder<U> addDepthRenderBuffer(int internalFormat) {
			depthRenderBufferSpec = new FrameBufferRenderBufferAttachmentSpec(internalFormat);
			hasDepthRenderBuffer = true;
			return this;
		}

		public GLFrameBufferBuilder<U> addStencilRenderBuffer(int internalFormat) {
			stencilRenderBufferSpec = new FrameBufferRenderBufferAttachmentSpec(internalFormat);
			hasStencilRenderBuffer = true;
			return this;
		}

		public GLFrameBufferBuilder<U> addStencilDepthPackedRenderBuffer(int internalFormat) {
			packedStencilDepthRenderBufferSpec = new FrameBufferRenderBufferAttachmentSpec(internalFormat);
			hasPackedStencilDepthRenderBuffer = true;
			return this;
		}

		public GLFrameBufferBuilder<U> addBasicDepthRenderBuffer() {
			return addDepthRenderBuffer(TGF.GL_DEPTH_COMPONENT16);
		}

		public GLFrameBufferBuilder<U> addBasicStencilRenderBuffer() {
			return addStencilRenderBuffer(TGF.GL_STENCIL_INDEX8);
		}

		public GLFrameBufferBuilder<U> addBasicStencilDepthPackedRenderBuffer() {
			return addStencilDepthPackedRenderBuffer(TGF.GL_DEPTH24_STENCIL8);
		}

		public abstract U build();
	}

	public static class FrameBufferBuilder extends GLFrameBufferBuilder<FrameBuffer> {
		public FrameBufferBuilder(int width, int height) {
			super(width, height);
		}

		@Override
		public FrameBuffer build() {
			return new FrameBuffer(this);
		}
	}

	public static class FloatFrameBufferBuilder extends GLFrameBufferBuilder<FloatFrameBuffer> {
		public FloatFrameBufferBuilder(int width, int height) {
			super(width, height);
		}

		@Override
		public FloatFrameBuffer build() {
			return new FloatFrameBuffer(this);
		}
	}

	public static class FrameBufferCubemapBuilder extends GLFrameBufferBuilder<FrameBufferCubemap> {
		public FrameBufferCubemapBuilder(int width, int height) {
			super(width, height);
		}

		@Override
		public FrameBufferCubemap build() {
			return new FrameBufferCubemap(this);
		}
	}
}
