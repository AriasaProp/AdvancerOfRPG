package com.ariasaproject.advancerofrpg;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL41;
import org.lwjgl.opengl.GL43;
import org.lwjglx.debug.Log;

import com.ariasaproject.advancerofrpg.graphics.Cubemap;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.graphics.Texture;
import com.ariasaproject.advancerofrpg.graphics.TextureArray;
import com.ariasaproject.advancerofrpg.graphics.glutils.GLFrameBuffer;
import com.ariasaproject.advancerofrpg.graphics.glutils.TextureData;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.BufferUtils;
import com.ariasaproject.advancerofrpg.utils.IntArray;

class Lwjgl3GL30 implements TGF {
	public static final String TAG = "GL 3.0";
	int blendSFactor, blendDFactor, depthFunc, cullFace;
	boolean depthMask;
	float depthRangeNear, depthRangeFar;
	// chaced data graphic function reset when invalidate
    public final IntArray enabledCaps = new IntArray();
	String extensions, renderers;
	// chace value for once only
    float maxAnisotropicFilterLevel;
	protected int maxTextureSize;
	protected int maxTextureUnit;
	Array<int[]> meshes = new Array<int[]>();
	private final String shaderHeader = "#version 300\n" + "#define LOW lowp\n" + "#define MED mediump\n"
            + "#ifdef GL_FRAGMENT_PRECISION_HIGH\n" + "#define HIGH highp\n" + "#else\n" + "#define HIGH mediump\n"
            + "#endif\n";
	Array<int[]> shaderPrograms = new Array<int[]>();
	// future for managed generate and delete texture
    Array<TextureData> textures = new Array<TextureData>();

	private ByteBuffer buffer = null;
	private FloatBuffer floatBuffer = null;
	private IntBuffer intBuffer = null;

	@Override
    public boolean capabilitySwitch(final boolean enable, final int cap) {
        if (enabledCaps.contains(cap) != enable) {
            if (enable) {
                GL11.glEnable(cap);
                enabledCaps.add(cap);
            } else {
                GL11.glDisable(cap);
                enabledCaps.removeValue(cap);
            }
            return true;
        }
        return false;
    }

	@Override
    public void clear() {
        GL20.glUseProgram(0);
        for (final int[] handler : shaderPrograms) {
            GL20.glDeleteProgram(handler[0]);
            GL20.glDeleteShader(handler[1]);
            GL20.glDeleteShader(handler[2]);
        }
        shaderPrograms.clear();
        GL30.glBindVertexArray(0);
        for (final int[] handler : meshes) {
            GL30.glDeleteVertexArrays(1, handler, 0);
            GL20.glDeleteBuffers(2, handler, 1);
        }
        meshes.clear();
        enabledCaps.clear();

        Texture.clearAllTextures();
        Cubemap.clearAllCubemaps();
        TextureArray.clearAllTextureArrays();
        GLFrameBuffer.clearAllFrameBuffers();
    }

	@Override
    public int[] compileShaderProgram(String source, String prefix) throws IllegalArgumentException {
        final int[] handlers = new int[3];
        try {
            if (!source.contains("<break>"))
                throw new RuntimeException("Source is error, hasn't <break>");
            String[] so = source.split("<break>");
            handlers[0] = GL20.glCreateProgram();
            if (handlers[0] == 0)
                throw new RuntimeException("Failed create Shader Program");
            handlers[1] = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
            if (handlers[1] == 0)
                throw new RuntimeException("Failed create Vertex shader");
            GL20.glShaderSource(handlers[1], shaderHeader + prefix + so[0]);
            GL20.glCompileShader(handlers[1]);
            GL20.glGetShaderiv(handlers[1], GL20.GL_COMPILE_STATUS, ints, 0);
            if (ints[0] == 0)
                throw new RuntimeException(GL20.glGetShaderInfoLog(handlers[1]));
            handlers[2] = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
            if (handlers[2] == 0)
                throw new RuntimeException("Failed create Fragment shader");
            GL20.glShaderSource(handlers[2], shaderHeader + prefix + so[1]);
            GL20.glCompileShader(handlers[2]);
            GL20.glGetShaderiv(handlers[2], GL20.GL_COMPILE_STATUS, ints, 0);
            if (ints[0] == 0)
                throw new RuntimeException(GL20.glGetShaderInfoLog(handlers[2]));
            GL20.glAttachShader(handlers[0], handlers[1]);
            GL20.glAttachShader(handlers[0], handlers[2]);
            GL20.glLinkProgram(handlers[0]);
            GL20.glGetProgramiv(handlers[0], GL20.GL_LINK_STATUS, ints, 0);
            if (ints[0] == 0)
                throw new RuntimeException(GL20.glGetProgramInfoLog(handlers[0]));
            shaderPrograms.add(handlers);
        } catch (RuntimeException e) {
            Arrays.fill(handlers, -1);
            throw new IllegalArgumentException("Shader program compiling, " + e);
        }
        return handlers;
    }

	@Override
    public void destroyMesh(int[] handlers) {
        try {
            if (handlers[0] == -1)
                throw new RuntimeException("Handlers not initialize");
            GL30.glBindVertexArray(0);
            GL30.glDeleteVertexArrays(1, handlers, 0);
            GL20.glDeleteBuffers(2, handlers, 1);
            meshes.removeValue(handlers, true);
        } catch (RuntimeException e) {
            Log.e(TAG, "Vertex data delete \n" + e);
        }
        Arrays.fill(handlers, -1);
    }

	@Override
    public void destroyShaderProgram(int[] handlers) {
        if (handlers[0] == -1)
            return;
        GL20.glUseProgram(0);
        GL20.glDeleteProgram(handlers[0]);
        GL20.glDeleteShader(handlers[1]);
        GL20.glDeleteShader(handlers[2]);
        shaderPrograms.removeValue(handlers, true);
        Arrays.fill(handlers, -1);
    }

	private void ensureBufferCapacity (int numBytes) {
		if (buffer == null || buffer.capacity() < numBytes) {
			buffer = com.badlogic.gdx.utils.BufferUtils.newByteBuffer(numBytes);
			floatBuffer = buffer.asFloatBuffer();
			intBuffer = buffer.asIntBuffer();
		}
	}

	// mesh id generated and binded Vao instead
    @Override
    public int[] genMesh(final int max_v_data, final boolean v_static, final int max_i_data, final boolean i_static) {
        final int[] handlers = new int[3];
        try {
            // vao id
            GL30.glGenVertexArrays(1, handlers, 0);
            if (handlers[0] <= 0) {
                throw new RuntimeException("Failed create vao id");
            }
            // buffers ids
            GL20.glGenBuffers(2, handlers, 1);
            if (handlers[1] <= 0 || handlers[2] <= 0) {
                throw new RuntimeException("Failed create buffer data");
            }
            // binding vao
            GL30.glBindVertexArray(handlers[0]);
            // binding vertex
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, handlers[1]);
            GL20.glBufferData(GL15.GL_ARRAY_BUFFER, max_v_data, null,
                    v_static ? GL15.GL_STATIC_DRAW : GL15.GL_DYNAMIC_DRAW);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            // binding indices
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, handlers[2]);
            GL20.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, max_i_data, null,
                    i_static ? GL15.GL_STATIC_DRAW : GL15.GL_DYNAMIC_DRAW);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

            meshes.add(handlers);
        } catch (RuntimeException e) {
            Arrays.fill(handlers, -1);
            throw new IllegalStateException("failed generate mesh data \n" + e);
        }
        return handlers;
    }

	@Override
	public String getLog() {
		return "No Log";
	}

	@Override
    public float getMaxAnisotropicFilterLevel() {

        if (maxAnisotropicFilterLevel == 0) {
            if (supportsExtension("GL_EXT_texture_filter_anisotropic")) {
                final FloatBuffer buffer = BufferUtils.newFloatBuffer(1);
                buffer.position(0).limit(buffer.capacity());
                GL11.glGetFloatv(0x84FF, buffer);
                maxAnisotropicFilterLevel = buffer.get(0);
            } else
                maxAnisotropicFilterLevel = 1;
        }
        return maxAnisotropicFilterLevel;
    }

	@Override
    public int getMaxTextureSize() {

        if (maxTextureSize < 64) {
            GL20.glGetIntegerv(GL11.GL_MAX_TEXTURE_SIZE, ints, 0);
            maxTextureSize = ints[0];
        }
        return maxTextureSize;
    }

	@Override
    public int getMaxTextureUnit() {

        if (maxTextureUnit <= 0) {
            GL20.glGetIntegerv(GL20.GL_MAX_TEXTURE_IMAGE_UNITS, ints, 0);
            maxTextureUnit = ints[0];
        }
        return maxTextureUnit;
    }

	@Override
    public void glActiveTexture(int unit) {
        if (unit >= getMaxTextureUnit())
            throw new IllegalArgumentException("inputed units more than max units => " + getMaxTextureUnit());
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
    }

	public void glAttachShader (int program, int shader) {
		GL20.glAttachShader(program, shader);
	}

	@Override
	public void glBeginQuery (int target, int id) {
		GL15.glBeginQuery(target, id);
	}

	@Override
	public void glBeginTransformFeedback (int primitiveMode) {
		GL30.glBeginTransformFeedback(primitiveMode);
	}

	@Override
	public void glBindAttribLocation (int program, int index, String name) {
		GL20.glBindAttribLocation(program, index, name);
	}

	@Override
	public void glBindBuffer (int target, int buffer) {
		GL15.glBindBuffer(target, buffer);
	}

	@Override
	public void glBindBufferBase (int target, int index, int buffer) {
		GL30.glBindBufferBase(target, index, buffer);
	}

	@Override
	public void glBindBufferRange (int target, int index, int buffer, int offset, int size) {
		GL30.glBindBufferRange(target, index, buffer, offset, size);
	}

	@Override
	public void glBindFramebuffer (int target, int framebuffer) {
		//EXTFramebufferObject.glBindFramebufferEXT(target, framebuffer);
		GL30.glBindFramebuffer(target, framebuffer);
	}

	@Override
	public void glBindRenderbuffer (int target, int renderbuffer) {
		GL30.glBindRenderbuffer(target, renderbuffer);
		//EXTFramebufferObject.glBindRenderbufferEXT(target, renderbuffer);
	}

	@Override
	public void glBindSampler (int unit, int sampler) {
		GL33.glBindSampler(unit, sampler);
	}

	@Override
    public void glBindTexture(int target, int texture) {
        GL11.glBindTexture(target, texture);
    }

	@Override
	public void glBindTransformFeedback (int target, int id) {
		GL40.glBindTransformFeedback(target, id);
	}

	@Override
    public void glBindVertexArray(int v) {
        GL30.glBindVertexArray(v);
    }

	@Override
	public void glBlendColor (float red, float green, float blue, float alpha) {
		GL14.glBlendColor(red, green, blue, alpha);
	}
	@Override
    public void glBlendEquation(int mode) {
        GL14.glBlendEquation(mode);
    }

	@Override
	public void glBlendEquationSeparate (int modeRGB, int modeAlpha) {
		GL20.glBlendEquationSeparate(modeRGB, modeAlpha);
	}

	public void glBlendFunc (int sfactor, int dfactor) {
		GL11.glBlendFunc(sfactor, dfactor);
	}

	public void glBlendFuncSeparate (int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		GL14.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
	}

	@Override
	public void glBlitFramebuffer (int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1,
		int mask, int filter) {
		GL30.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
	}

	@Override
	public void glBufferData (int target, int size, Buffer data, int usage) {
		if (data == null)
			GL15.glBufferData(target, size, usage);
		else if (data instanceof ByteBuffer)
			GL15.glBufferData(target, (ByteBuffer)data, usage);
		else if (data instanceof IntBuffer)
			GL15.glBufferData(target, (IntBuffer)data, usage);
		else if (data instanceof FloatBuffer)
			GL15.glBufferData(target, (FloatBuffer)data, usage);
		else if (data instanceof DoubleBuffer)
			GL15.glBufferData(target, (DoubleBuffer)data, usage);
		else if (data instanceof ShortBuffer) //
			GL15.glBufferData(target, (ShortBuffer)data, usage);
	}

	@Override
	public void glBufferSubData (int target, int offset, int size, Buffer data) {
		if (data == null)
			throw new GdxRuntimeException("Using null for the data not possible, blame LWJGL");
		else if (data instanceof ByteBuffer)
			GL15.glBufferSubData(target, offset, (ByteBuffer)data);
		else if (data instanceof IntBuffer)
			GL15.glBufferSubData(target, offset, (IntBuffer)data);
		else if (data instanceof FloatBuffer)
			GL15.glBufferSubData(target, offset, (FloatBuffer)data);
		else if (data instanceof DoubleBuffer)
			GL15.glBufferSubData(target, offset, (DoubleBuffer)data);
		else if (data instanceof ShortBuffer) //
			GL15.glBufferSubData(target, offset, (ShortBuffer)data);
	}
	@Override
	public int glCheckFramebufferStatus (int target) {
		return GL30.glCheckFramebufferStatus(target);
		//return EXTFramebufferObject.glCheckFramebufferStatusEXT(target);
	}

	public void glClear (int mask) {
		GL11.glClear(mask);
	}

	@Override
	public void glClearBufferfi (int buffer, int drawbuffer, float depth, int stencil) {
		GL30.glClearBufferfi(buffer, drawbuffer, depth, stencil);
	}

	@Override
    public void glClearBufferfv(int buffer, int drawbuffer, float[] value, int offset) {
        GL30.glClearBufferfv(buffer, drawbuffer, value, offset);
    }

	@Override
	public void glClearBufferfv(int buffer, int drawbuffer, FloatBuffer value) {
		GL30.glClearBufferfv(buffer, drawbuffer, value);
	}

	@Override
    public void glClearBufferiv(int buffer, int drawbuffer, int[] value, int offset) {

        GL30.glClearBufferiv(buffer, drawbuffer, value, offset);
    }

	@Override
	public void glClearBufferiv(int buffer, int drawbuffer, IntBuffer value) {
		GL30.glClearBufferiv(buffer, drawbuffer, value);
	}

	@Override
    public void glClearBufferuiv(int buffer, int drawbuffer, int[] value, int offset) {

        GL30.glClearBufferuiv(buffer, drawbuffer, value, offset);
    }

	@Override
	public void glClearBufferuiv (int buffer, int drawbuffer, IntBuffer value) {
		GL30.glClearBufferuiv(buffer, drawbuffer, value);
	}

	public void glClearColor (float red, float green, float blue, float alpha) {
		GL11.glClearColor(red, green, blue, alpha);
	}

	// extra function
    @Override
    public void glClearColorMask(int mask, float red, float green, float blue, float alpha) {
        GL11.glClearColor(red, green, blue, alpha);
        GL11.glClear(mask);
    }

	@Override
    public void glClearDepth(float depth) {
        GL20.glClearDepthf(depth);
    }

	public void glClearDepthf (float depth) {
		GL11.glClearDepth(depth);
	}

	@Override
	public void glClearStencil (int s) {
		GL11.glClearStencil(s);
	}

	@Override
    public void glClearStencil(int s) {
        GL11.glClearStencil(s);
    }

	@Override
    public int glClientWaitSync(long sync, int flags, long timeout) {
        return GL30.glClientWaitSync(sync, flags, timeout);
    }

	public void glCompileShader (int shader) {
		GL20.glCompileShader(shader);
	}

	public void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data) {
		if (data instanceof ByteBuffer) {
			GL13.glCompressedTexImage2D(target, level, internalformat, width, height, border, (ByteBuffer)data);
		} else {
			throw new GdxRuntimeException("Can't use " + data.getClass().getName() + " with this method. Use ByteBuffer instead.");
		}
	}

	public void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int imageSize, Buffer data) {
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glCopyBufferSubData (int readTarget, int writeTarget, int readOffset, int writeOffset, int size) {
		GL31.glCopyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size);
	}

	@Override
	public void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height, int border) {
		GL11.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
	}

	@Override
    public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height,
                                 int border) {

        GL11.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
    }

	@Override
	public void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		GL11.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
	}

	@Override
    public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width,
                                    int height) {

        GL11.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
    }

	@Override
	public void glCopyTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width,
		int height) {
		GL12.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height);
	}

	public int glCreateProgram () {
		return GL20.glCreateProgram();
	}

	public int glCreateShader (int type) {
		return GL20.glCreateShader(type);
	}

	public void glCullFace (int mode) {
		GL11.glCullFace(mode);
	}

	@Override
	public void glDeleteBuffer (int buffer) {
		GL15.glDeleteBuffers(buffer);
	}

	@Override
    public void glDeleteBuffer(int buffer) {

        ints[0] = buffer;
        GL20.glDeleteBuffers(1, ints, 0);
    }

	@Override
	public void glDeleteBuffers (int n, IntBuffer buffers) {
		GL15.glDeleteBuffers(buffers);
	}

	@Override
    public void glDeleteBuffers(int n, IntBuffer buffers) {

        GL20.glDeleteBuffers(n, buffers);
    }

	@Override
	public void glDeleteFramebuffer (int framebuffer) {
		EXTFramebufferObject.glDeleteFramebuffersEXT(framebuffer);
	}

	@Override
	public void glDeleteFramebuffer (int framebuffer) {
		GL30.glDeleteFramebuffers(framebuffer);
	}

	@Override
	public void glDeleteFramebuffers (int n, IntBuffer framebuffers) {
		EXTFramebufferObject.glDeleteFramebuffersEXT(framebuffers);
	}

	@Override
	public void glDeleteFramebuffers (int n, IntBuffer framebuffers) {
		GL30.glDeleteFramebuffers(framebuffers);
	}

	@Override
	public void glDeleteProgram (int program) {
		GL20.glDeleteProgram(program);
	}

	@Override
    public void glDeleteProgram(int program) {
        GL20.glDeleteProgram(program);
    }

	@Override
	public void glDeleteQueries (int n, int[] ids, int offset) {
		for(int i = offset; i < offset + n; i++) {
			GL15.glDeleteQueries(ids[i]);
		}
	}

	@Override
	public void glDeleteQueries (int n, IntBuffer ids) {
		for(int i = 0; i < n; i++) {
			GL15.glDeleteQueries(ids.get());
		}
	}

	@Override
	public void glDeleteRenderbuffer (int renderbuffer) {
		EXTFramebufferObject.glDeleteRenderbuffersEXT(renderbuffer);
	}

	@Override
	public void glDeleteRenderbuffer (int renderbuffer) {
		GL30.glDeleteRenderbuffers(renderbuffer);
	}

	@Override
	public void glDeleteRenderbuffers (int n, IntBuffer renderbuffers) {
		EXTFramebufferObject.glDeleteRenderbuffersEXT(renderbuffers);
	}

	@Override
	public void glDeleteRenderbuffers (int n, IntBuffer renderbuffers) {
		GL30.glDeleteRenderbuffers(renderbuffers);
	}

	@Override
	public void glDeleteSamplers (int count, int[] samplers, int offset) {
		for(int i = offset; i < offset + count; i++) {
			GL33.glDeleteSamplers(samplers[i]);
		}
	}

	@Override
	public void glDeleteSamplers (int count, IntBuffer samplers) {
		GL33.glDeleteSamplers(samplers);
	}

	public void glDeleteShader (int shader) {
		GL20.glDeleteShader(shader);
	}

	@Override
    public void glDeleteSync(long sync) {
        GL30.glDeleteSync(sync);
    }

	@Override
	public void glDeleteTexture (int texture) {
		GL11.glDeleteTextures(texture);
	}

	@Override
    public void glDeleteTexture(int texture) {
        ints[0] = texture;
        GL20.glDeleteTextures(1, ints, 0);
    }

	public void glDeleteTextures (int n, IntBuffer textures) {
		GL11.glDeleteTextures(textures);
	}

	@Override
	public void glDeleteTransformFeedbacks (int n, int[] ids, int offset) {
		for(int i = offset; i < offset + n; i++) {
			GL40.glDeleteTransformFeedbacks(ids[i]);
		}
	}

	@Override
	public void glDeleteTransformFeedbacks (int n, IntBuffer ids) {
		GL40.glDeleteTransformFeedbacks(ids);
	}

	@Override
	public void glDeleteVertexArrays (int n, int[] arrays, int offset) {
		for(int i = offset; i < offset + n; i++) {
			GL30.glDeleteVertexArrays(arrays[i]);
		}
	}

	@Override
	public void glDeleteVertexArrays (int n, IntBuffer arrays) {
		GL30.glDeleteVertexArrays(arrays);
	}

	@Override
	public void glDepthFunc (int func) {
		GL11.glDepthFunc(func);
	}

	@Override
    public void glDepthFunc(int func) {
        if (func != depthFunc)
            GL11.glDepthFunc(func);
    }

	public void glDepthMask (boolean flag) {
		GL11.glDepthMask(flag);
	}

	@Override
	public void glDepthRangef (float zNear, float zFar) {
		GL11.glDepthRange(zNear, zFar);
	}

	@Override
    public void glDepthRangef(float near, float far) {

        if (depthRangeNear != near || depthRangeFar != far)
            GL20.glDepthRangef(near, far);
    }

	@Override
	public void glDetachShader (int program, int shader) {
		GL20.glDetachShader(program, shader);
	}

	@Override
    public void glDetachShader(int program, int shader) {

        GL20.glDetachShader(program, shader);
    }

	public void glDisable (int cap) {
		GL11.glDisable(cap);
	}

	@Override
	public void glDisableVertexAttribArray (int index) {
		GL20.glDisableVertexAttribArray(index);
	}

	@Override
    public void glDisableVertexAttribArray(int index) {

        GL20.glDisableVertexAttribArray(index);
    }

	@Override
	public void glDrawArrays (int mode, int first, int count) {
		GL11.glDrawArrays(mode, first, count);
	}

	@Override
    public void glDrawArrays(int mode, int first, int count) {

        GL11.glDrawArrays(mode, first, count);
    }

	@Override
	public void glDrawArraysInstanced (int mode, int first, int count, int instanceCount) {
		GL31.glDrawArraysInstanced(mode, first, count, instanceCount);
	}

	@Override
    public void glDrawBuffers(int n, int[] bufs, int offset) {

        GL30.glDrawBuffers(n, bufs, offset);
    }

	@Override
	public void glDrawBuffers (int n, IntBuffer bufs) {
		GL20.glDrawBuffers(bufs);
	}

	@Override
	public void glDrawElements (int mode, int count, int type, Buffer indices) {
		if (indices instanceof ShortBuffer && type == com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_SHORT)
			GL11.glDrawElements(mode, (ShortBuffer)indices);
		else if (indices instanceof ByteBuffer && type == com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_SHORT)
			GL11.glDrawElements(mode, ((ByteBuffer)indices).asShortBuffer());
		else if (indices instanceof ByteBuffer && type == com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_BYTE)
			GL11.glDrawElements(mode, (ByteBuffer)indices);
		else
			throw new GdxRuntimeException("Can't use " + indices.getClass().getName()
				+ " with this method. Use ShortBuffer or ByteBuffer instead. Blame LWJGL");
	}

	@Override
    public void glDrawElements(int mode, int count, int type, Buffer indices) {

        GL20.glDrawElements(mode, count, type, indices);
    }

	@Override
	public void glDrawElements (int mode, int count, int type, int indices) {
		GL11.glDrawElements(mode, count, type, indices);
	}

	@Override
    public void glDrawElements(int mode, int count, int type, int indices) {

        GL11.glDrawElements(mode, count, type, indices);
    }

	@Override
	public void glDrawElementsInstanced (int mode, int count, int type, int indicesOffset, int instanceCount) {
		GL31.glDrawElementsInstanced(mode, count, type, indicesOffset, instanceCount);
		
	}

	@Override
    public void glDrawElementsInstanced(int mode, int count, int type, java.nio.Buffer indices, int instanceCount) {
        GL30.glDrawElementsInstanced(mode, count, type, indices, instanceCount);
    }

	@Override
	public void glDrawRangeElements (int mode, int start, int end, int count, int type, Buffer indices) {
		if(indices instanceof ByteBuffer) GL12.glDrawRangeElements(mode, start, end, (ByteBuffer)indices);
		else if(indices instanceof ShortBuffer) GL12.glDrawRangeElements(mode, start, end, (ShortBuffer)indices);
		else if(indices instanceof IntBuffer) GL12.glDrawRangeElements(mode, start, end, (IntBuffer)indices);
		else throw new GdxRuntimeException("indices must be byte, short or int buffer");
	}

	@Override
	public void glDrawRangeElements (int mode, int start, int end, int count, int type, int offset) {
		GL12.glDrawRangeElements(mode, start, end, count, type, offset);
	}

	public void glEnable (int cap) {
		GL11.glEnable(cap);
	}

	@Override
	public void glEnableVertexAttribArray (int index) {
		GL20.glEnableVertexAttribArray(index);
	}

	@Override
    public void glEnableVertexAttribArray(int index) {

        GL20.glEnableVertexAttribArray(index);
    }

	@Override
	public void glEndQuery (int target) {
		GL15.glEndQuery(target);
	}

	@Override
	public void glEndTransformFeedback () {
		GL30.glEndTransformFeedback();
	}

	@Override
    public long glFenceSync(int condition, int flags) {

        return GL30.glFenceSync(condition, flags);
    }

	@Override
	public void glFinish () {
		GL11.glFinish();
	}

	@Override
    public void glFinish() {

        GL11.glFinish();
    }

	@Override
	public void glFlush () {
		GL11.glFlush();
	}

	@Override
    public void glFlush() {

        GL11.glFlush();
    }

	@Override
	public void glFlushMappedBufferRange (int target, int offset, int length) {
		GL30.glFlushMappedBufferRange(target, offset, length);
	}

	@Override
	public void glFramebufferRenderbuffer (int target, int attachment, int renderbuffertarget, int renderbuffer) {
		EXTFramebufferObject.glFramebufferRenderbufferEXT(target, attachment, renderbuffertarget, renderbuffer);
	}

	@Override
	public void glFramebufferRenderbuffer (int target, int attachment, int renderbuffertarget, int renderbuffer) {
		GL30.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
	}

	@Override
	public void glFramebufferTexture2D (int target, int attachment, int textarget, int texture, int level) {
		EXTFramebufferObject.glFramebufferTexture2DEXT(target, attachment, textarget, texture, level);
	}

	@Override
	public void glFramebufferTexture2D (int target, int attachment, int textarget, int texture, int level) {
		GL30.glFramebufferTexture2D(target, attachment, textarget, texture, level);
	}

	@Override
	public void glFramebufferTextureLayer (int target, int attachment, int texture, int level, int layer) {
		GL30.glFramebufferTextureLayer(target, attachment, texture, level, layer);
	}

	@Override
	public void glFrontFace (int mode) {
		GL11.glFrontFace(mode);
	}

	@Override
    public void glFrontFace(int mode) {

        GL11.glFrontFace(mode);
    }

	@Override
	public int glGenBuffer () {
		return GL15.glGenBuffers();
	}

	@Override
    public int glGenBuffer() {

        GL20.glGenBuffers(1, ints, 0);
        return ints[0];
    }

	@Override
	public void glGenBuffers (int n, IntBuffer buffers) {
		GL15.glGenBuffers(buffers);
	}

	@Override
    public void glGenBuffers(int n, IntBuffer buffers) {

        GL20.glGenBuffers(n, buffers);
    }

	@Override
	public void glGenerateMipmap (int target) {
		EXTFramebufferObject.glGenerateMipmapEXT(target);
	}

	@Override
	public void glGenerateMipmap (int target) {
		GL30.glGenerateMipmap(target);
	}

	@Override
	public int glGenFramebuffer () {
		return EXTFramebufferObject.glGenFramebuffersEXT();
	}

	@Override
	public int glGenFramebuffer () {
		return GL30.glGenFramebuffers();
	}

	@Override
	public void glGenFramebuffers (int n, IntBuffer framebuffers) {
		EXTFramebufferObject.glGenFramebuffersEXT(framebuffers);
	}

	@Override
	public void glGenFramebuffers (int n, IntBuffer framebuffers) {
		GL30.glGenFramebuffers(framebuffers);
	}

	@Override
	public void glGenQueries (int n, int[] ids, int offset) {
		for(int i = offset; i < offset + n; i++) {
			ids[i] = GL15.glGenQueries();
		}
	}

	@Override
	public void glGenQueries (int n, IntBuffer ids) {
		for(int i = 0; i < n; i++) {
			ids.put(GL15.glGenQueries());
		}
	}

	@Override
	public int glGenRenderbuffer () {
		return EXTFramebufferObject.glGenRenderbuffersEXT();
	}

	@Override
	public int glGenRenderbuffer () {
		return GL30.glGenRenderbuffers();
	}

	@Override
	public void glGenRenderbuffers (int n, IntBuffer renderbuffers) {
		EXTFramebufferObject.glGenRenderbuffersEXT(renderbuffers);
	}

	@Override
	public void glGenRenderbuffers (int n, IntBuffer renderbuffers) {
		GL30.glGenRenderbuffers(renderbuffers);
	}

	@Override
	public void glGenSamplers (int count, int[] samplers, int offset) {
		for(int i = offset; i < offset + count; i++) {
			samplers[i] = GL33.glGenSamplers();
		}
	}

	@Override
	public void glGenSamplers (int count, IntBuffer samplers) {
		GL33.glGenSamplers(samplers);
	}

	@Override
	public int glGenTexture () {
		return GL11.glGenTextures();
	}

	public void glGenTextures (int n, IntBuffer textures) {
		GL11.glGenTextures(textures);
	}

	@Override
	public void glGenTransformFeedbacks (int n, int[] ids, int offset) {
		for(int i = offset; i < offset + n; i++) {
			ids[i] = GL40.glGenTransformFeedbacks();
		}
	}
	@Override
	public void glGenTransformFeedbacks (int n, IntBuffer ids) {
		GL40.glGenTransformFeedbacks(ids);
	}

	@Override
	public void glGenVertexArrays (int n, int[] arrays, int offset) {
		for(int i = offset; i < offset + n; i++) {
			arrays[i] = GL30.glGenVertexArrays();
		}
	}

	@Override
	public void glGenVertexArrays (int n, IntBuffer arrays) {
		GL30.glGenVertexArrays(arrays);
	}
	@Override
    public String glGetActiveAttrib(int program, int index, IntBuffer size, Buffer type) {

        // it is assumed that size and type are both int buffers of length 1 with a
        // single integer at position 0
        // length
        ints[0] = 0;
        // size
        ints2[0] = size.get(0);
        // type
        ints3[0] = ((IntBuffer) type).get(0);
        GL20.glGetActiveAttrib(program, index, buffer.length, ints, 0, ints2, 0, ints3, 0, buffer, 0);
        return new String(buffer, 0, ints[0]);
    }

	public String glGetActiveAttrib (int program, int index, IntBuffer size, IntBuffer type) {
		return GL20.glGetActiveAttrib(program, index, 256, size, type);
	}

	@Override
    public String glGetActiveUniform(int program, int index, IntBuffer size, Buffer type) {

        // length
        ints[0] = 0;
        // size
        ints2[0] = size.get(0);
        // type
        ints3[0] = ((IntBuffer) type).get(0);
        GL20.glGetActiveUniform(program, index, buffer.length, ints, 0, ints2, 0, ints3, 0, buffer, 0);
        return new String(buffer, 0, ints[0]);
    }

	public String glGetActiveUniform (int program, int index, IntBuffer size, IntBuffer type) {
		return GL20.glGetActiveUniform(program, index, 256, size, type);
	}

	@Override
    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, int[] params, int offset) {

        GL30.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, params, offset);
    }

	@Override
	public void glGetActiveUniformBlockiv (int program, int uniformBlockIndex, int pname, IntBuffer params) {
		params.put(GL31.glGetActiveUniformBlocki(program, uniformBlockIndex, pname));
	}

	@Override
	public String glGetActiveUniformBlockName (int program, int uniformBlockIndex) {
		return GL31.glGetActiveUniformBlockName(program, uniformBlockIndex, 1024);
	}

	@Override
	public void glGetActiveUniformBlockName (int program, int uniformBlockIndex, Buffer length, Buffer uniformBlockName) {
		GL31.glGetActiveUniformBlockName(program, uniformBlockIndex, (IntBuffer)length, (ByteBuffer)uniformBlockName);
	}

	@Override
    public void glGetActiveUniformBlockName(int program, int uniformBlockIndex, int bufSize, int[] length,
                                            int lengthOffset, byte[] uniformBlockName, int uniformBlockNameOffset) {

        GL30.glGetActiveUniformBlockName(program, uniformBlockIndex, bufSize, length, lengthOffset, uniformBlockName,
                uniformBlockNameOffset);
    }

	@Override
    public void glGetActiveUniformsiv(int program, int uniformCount, int[] uniformIndices, int uniformIndicesOffset,
                                      int pname, int[] params, int paramsOffset) {

        GL30.glGetActiveUniformsiv(program, uniformCount, uniformIndices, uniformIndicesOffset, pname, params,
                paramsOffset);
    }

	@Override
	public void glGetActiveUniformsiv (int program, int uniformCount, IntBuffer uniformIndices, int pname, IntBuffer params) {
		GL31.glGetActiveUniformsiv(program, uniformIndices, pname, params);
	}

	@Override
	public void glGetAttachedShaders (int program, int maxcount, Buffer count, IntBuffer shaders) {
		GL20.glGetAttachedShaders(program, (IntBuffer)count, shaders);
	}

	@Override
    public void glGetAttachedShaders(int program, int maxcount, Buffer count, IntBuffer shaders) {

        GL20.glGetAttachedShaders(program, maxcount, (IntBuffer) count, shaders);
    }

	@Override
	public int glGetAttribLocation (int program, String name) {
		return GL20.glGetAttribLocation(program, name);
	}

	@Override
    public int glGetAttribLocation(int program, String name) {

        return GL20.glGetAttribLocation(program, name);
    }

	@Override
	public void glGetBooleanv (int pname, Buffer params) {
		GL11.glGetBooleanv(pname, (ByteBuffer)params);
	}

	@Override
    public void glGetBooleanv(int pname, Buffer params) {

        GL20.glGetBooleanv(pname, (IntBuffer) params);
    }

	@Override
    public void glGetBufferParameteri64v(int target, int pname, java.nio.LongBuffer params) {

        GL30.glGetBufferParameteri64v(target, pname, params);
    }

	@Override
	public void glGetBufferParameteri64v(int target, int pname, long[] params, int offset) {
		// TODO Auto-generated method stub
		
	}
	@Override
    public void glGetBufferParameteri64v(int target, int pname, long[] params, int offset) {

        GL30.glGetBufferParameteri64v(target, pname, params, offset);
    }

	@Override
	public void glGetBufferParameteri64v (int target, int pname, LongBuffer params) {
		params.put(GL32.glGetBufferParameteri64(target, pname));
	}

	@Override
	public void glGetBufferParameteriv (int target, int pname, IntBuffer params) {
		GL15.glGetBufferParameteriv(target, pname, params);
	}

	@Override
    public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {

        GL15.glGetBufferParameteriv(target, pname, params);
    }

	@Override
	public Buffer glGetBufferPointerv (int target, int pname) {
		// FIXME glGetBufferPointerv needs a proper translation
		// return GL15.glGetBufferPointer(target, pname);
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
    public java.nio.Buffer glGetBufferPointerv(int target, int pname) {
        return GL30.glGetBufferPointerv(target, pname);
    }

	@Override
	public int glGetError () {
		return GL11.glGetError();
	}

	@Override
	public void glGetFloatv (int pname, FloatBuffer params) {
		GL11.glGetFloatv(pname, params);
	}

	@Override
	public int glGetFragDataLocation (int program, String name) {
		return GL30.glGetFragDataLocation(program, name);
	}


	@Override
	public void glGetFramebufferAttachmentParameteriv (int target, int attachment, int pname, IntBuffer params) {
		//EXTFramebufferObject.glGetFramebufferAttachmentParameterivEXT(target, attachment, pname, params);
        GL20.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
	}

	@Override
    public void glGetInteger64i_v(int target, int index, java.nio.LongBuffer data) {

        GL30.glGetInteger64i_v(target, index, data);
    }

	@Override
    public void glGetInteger64i_v(int target, int index, long[] data, int offset) {

        GL30.glGetInteger64i_v(target, index, data, offset);
    }
	@Override
	public void glGetInteger64v (int pname, LongBuffer params) {
		GL32.glGetInteger64v(pname, params);
	}

	@Override
    public void glGetIntegeri_v(int target, int index, int[] data, int offset) {

        GL30.glGetIntegeri_v(target, index, data, offset);
    }

	@Override
    public void glGetIntegeri_v(int target, int index, java.nio.IntBuffer data) {

        GL30.glGetIntegeri_v(target, index, data);
    }

	@Override
    public int glGetIntegerv(int pname) {

        GL20.glGetIntegerv(pname, ints, 0);
        return ints[0];
    }

	@Override
	public void glGetIntegerv (int pname, IntBuffer params) {
		GL11.glGetIntegerv(pname, params);
	}

	@Override
    public void glGetIntegerv(int pname, IntBuffer params) {

        GL11.glGetIntegerv(pname, params);
    }

	@Override
	public void glGetInternalformativ(int target, int internalformat, int pname, int bufSize, int[] params, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public void glGetInternalformativ(int target, int internalformat, int pname, int bufSize, int[] params,
                                      int offset) {

        GL30.glGetInternalformativ(target, internalformat, pname, bufSize, params, offset);
    }

	@Override
	public void glGetInternalformativ(int target, int internalformat, int pname, int bufSize, IntBuffer params) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public void glGetInternalformativ(int target, int internalformat, int pname, int bufSize,
                                      java.nio.IntBuffer params) {

        GL30.glGetInternalformativ(target, internalformat, pname, bufSize, params);
    }

	@Override
	public void glGetProgramBinary(int program, int bufSize, int[] length, int lengthOffset, int[] binaryFormat, int binaryFormatOffset, Buffer binary) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public void glGetProgramBinary(int program, int bufSize, int[] length, int lengthOffset, int[] binaryFormat,
                                   int binaryFormatOffset, java.nio.Buffer binary) {

        GL30.glGetProgramBinary(program, bufSize, length, lengthOffset, binaryFormat, binaryFormatOffset, binary);
    }

	@Override
	public void glGetProgramBinary(int program, int bufSize, IntBuffer length, IntBuffer binaryFormat, Buffer binary) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public void glGetProgramBinary(int program, int bufSize, java.nio.IntBuffer length, java.nio.IntBuffer binaryFormat,
                                   java.nio.Buffer binary) {

        GL30.glGetProgramBinary(program, bufSize, length, binaryFormat, binary);
    }

	public String glGetProgramInfoLog (int program) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 10);
		buffer.order(ByteOrder.nativeOrder());
		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intBuffer = tmp.asIntBuffer();

		GL20.glGetProgramInfoLog(program, intBuffer, buffer);
		int numBytes = intBuffer.get(0);
		byte[] bytes = new byte[numBytes];
		buffer.get(bytes);
		return new String(bytes);
	}

	@Override
	public void glGetProgramiv (int program, int pname, IntBuffer params) {
		GL20.glGetProgramiv(program, pname, params);
	}

	@Override
    public void glGetProgramiv(int program, int pname, IntBuffer params) {

        GL20.glGetProgramiv(program, pname, params);
    }

	@Override
	public void glGetQueryiv(int target, int pname, int[] params, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public void glGetQueryiv(int target, int pname, int[] params, int offset) {

        GL30.glGetQueryiv(target, pname, params, offset);
    }

	@Override
	public void glGetQueryiv (int target, int pname, IntBuffer params) {
		GL15.glGetQueryiv(target, pname, params);
	}

	@Override
    public void glGetQueryiv(int target, int pname, java.nio.IntBuffer params) {

        GL15.glGetQueryiv(target, pname, params);
    }

	@Override
    public void glGetQueryObjectuiv(int id, int pname, int[] params, int offset) {

        GL30.glGetQueryObjectuiv(id, pname, params, offset);
    }
	@Override
	public void glGetQueryObjectuiv (int id, int pname, IntBuffer params) {
		GL15.glGetQueryObjectuiv(id, pname, params);
	}

	@Override
	public void glGetRenderbufferParameteriv (int target, int pname, IntBuffer params) {
		//EXTFramebufferObject.glGetRenderbufferParameterivEXT(target, pname, params);
		GL30.glGetRenderbufferParameteriv(target, pname, params);
	}

	@Override
    public void glGetSamplerParameterfv(int sampler, int pname, float[] params, int offset) {

        GL30.glGetSamplerParameterfv(sampler, pname, params, offset);
    }

	@Override
	public void glGetSamplerParameterfv (int sampler, int pname, FloatBuffer params) {
		GL33.glGetSamplerParameterfv(sampler, pname, params);		
	}

	@Override
    public void glGetSamplerParameterfv(int sampler, int pname, java.nio.FloatBuffer params) {

        GL30.glGetSamplerParameterfv(sampler, pname, params);
    }

	@Override
	public void glGetSamplerParameteriv(int sampler, int pname, int[] params, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public void glGetSamplerParameteriv(int sampler, int pname, int[] params, int offset) {

        GL30.glGetSamplerParameteriv(sampler, pname, params, offset);
    }
	@Override
	public void glGetSamplerParameteriv (int sampler, int pname, IntBuffer params) {
		GL33.glGetSamplerParameterIiv(sampler, pname, params);
	}

	@Override
    public void glGetSamplerParameteriv(int sampler, int pname, java.nio.IntBuffer params) {

        GL30.glGetSamplerParameteriv(sampler, pname, params);
    }

	@Override
	public String glGetShaderInfoLog (int shader) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 10);
		buffer.order(ByteOrder.nativeOrder());
		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intBuffer = tmp.asIntBuffer();

		GL20.glGetShaderInfoLog(shader, intBuffer, buffer);
		int numBytes = intBuffer.get(0);
		byte[] bytes = new byte[numBytes];
		buffer.get(bytes);
		return new String(bytes);
	}

	@Override
    public String glGetShaderInfoLog(int shader) {

        return GL20.glGetShaderInfoLog(shader);
    }

	@Override
	public void glGetShaderiv (int shader, int pname, IntBuffer params) {
		GL20.glGetShaderiv(shader, pname, params);
	}


	@Override
    public void glGetShaderiv(int shader, int pname, IntBuffer params) {

        GL20.glGetShaderiv(shader, pname, params);
    }

	@Override
	public void glGetShaderPrecisionFormat (int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
		throw new UnsupportedOperationException("unsupported, won't implement");
	}

	@Override
    public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {

        GL20.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
    }

	@Override
	public String glGetString (int name) {
		return GL11.glGetString(name);
	}

	@Override
    public String glGetString(int name) {

        return GL11.glGetString(name);
    }

	@Override
	public String glGetStringi (int name, int index) {
		return GL30.glGetStringi(name, index);
	}

	@Override
    public String glGetStringi(int name, int index) {

        return GL30.glGetStringi(name, index);
    }

	@Override
	public void glGetSynciv(long sync, int pname, int bufSize, int[] length, int lengthOffset, int[] values, int valuesOffset) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public void glGetSynciv(long sync, int pname, int bufSize, int[] length, int lengthOffset, int[] values,
                            int valuesOffset) {

        GL30.glGetSynciv(sync, pname, bufSize, length, lengthOffset, values, valuesOffset);
    }

	@Override
	public void glGetSynciv(long sync, int pname, int bufSize, IntBuffer length, IntBuffer values) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public void glGetSynciv(long sync, int pname, int bufSize, java.nio.IntBuffer length, java.nio.IntBuffer values) {

        GL30.glGetSynciv(sync, pname, bufSize, length, values);
    }

	@Override
	public void glGetTexParameterfv (int target, int pname, FloatBuffer params) {
		GL11.glGetTexParameterfv(target, pname, params);
	}


	@Override
    public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {

        GL11.glGetTexParameterfv(target, pname, params);
    }

	@Override
	public void glGetTexParameteriv (int target, int pname, IntBuffer params) {
		GL11.glGetTexParameteriv(target, pname, params);
	}

	@Override
    public void glGetTexParameteriv(int target, int pname, IntBuffer params) {

        GL11.glGetTexParameteriv(target, pname, params);
    }

	@Override
	public void glGetTransformFeedbackVarying(int program, int index, int bufsize, int[] length, int lengthOffset, int[] size, int sizeOffset, int[] type, int typeOffset, byte[] name, int nameOffset) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public void glGetTransformFeedbackVarying(int program, int index, int bufsize, int[] length, int lengthOffset,
                                              int[] size, int sizeOffset, int[] type, int typeOffset, byte[] name, int nameOffset) {

        GL30.glGetTransformFeedbackVarying(program, index, bufsize, length, lengthOffset, size, sizeOffset, type,
                typeOffset, name, nameOffset);
    }

	@Override
	public String glGetTransformFeedbackVarying(int program, int index, int[] size, int sizeOffset, int[] type, int typeOffset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public String glGetTransformFeedbackVarying(int program, int index, int[] size, int sizeOffset, int[] type,
                                                int typeOffset) {

        return GL30.glGetTransformFeedbackVarying(program, index, size, sizeOffset, type, typeOffset);
    }

	@Override
	public String glGetTransformFeedbackVarying(int program, int index, IntBuffer size, IntBuffer type) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
    public String glGetTransformFeedbackVarying(int program, int index, java.nio.IntBuffer size,
                                                java.nio.IntBuffer type) {

        return GL30.glGetTransformFeedbackVarying(program, index, size, type);
    }


	@Override
	public int glGetUniformBlockIndex (int program, String uniformBlockName) {
		return GL31.glGetUniformBlockIndex(program, uniformBlockName);
	}

	@Override
    public int glGetUniformBlockIndex(int program, String uniformBlockName) {

        return GL30.glGetUniformBlockIndex(program, uniformBlockName);
    }
	@Override
	public void glGetUniformfv (int program, int location, FloatBuffer params) {
		GL20.glGetUniformfv(program, location, params);
	}
	@Override
    public void glGetUniformfv(int program, int location, FloatBuffer params) {

        GL20.glGetUniformfv(program, location, params);
    }

	@Override
	public void glGetUniformIndices(int program, String[] uniformNames, int[] uniformIndices, int uniformIndicesOffset) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glGetUniformIndices(int program, String[] uniformNames, int[] uniformIndices,
                                    int uniformIndicesOffset) {

        GL30.glGetUniformIndices(program, uniformNames, uniformIndices, uniformIndicesOffset);
    }
    @Override
	public void glGetUniformIndices (int program, String[] uniformNames, IntBuffer uniformIndices) {
		GL31.glGetUniformIndices(program, uniformNames, uniformIndices);
	}
    @Override
    public void glGetUniformIndices(int program, String[] uniformNames, java.nio.IntBuffer uniformIndices) {

        GL30.glGetUniformIndices(program, uniformNames, uniformIndices);
    }
    @Override
	public void glGetUniformiv (int program, int location, IntBuffer params) {
		GL20.glGetUniformiv(program, location, params);
	}
    @Override
    public void glGetUniformiv(int program, int location, IntBuffer params) {

        GL20.glGetUniformiv(program, location, params);
    }
    @Override
	public int glGetUniformLocation (int program, String name) {
		return GL20.glGetUniformLocation(program, name);
	}
    @Override
    public int glGetUniformLocation(int program, String name) {

        return GL20.glGetUniformLocation(program, name);
    }
    @Override
	public void glGetUniformuiv(int program, int location, int[] params, int offset) {
		// TODO Auto-generated method stub
		
	}
    @Override
    public void glGetUniformuiv(int program, int location, int[] params, int offset) {

        GL30.glGetUniformuiv(program, location, params, offset);
    }
    @Override
	public void glGetUniformuiv (int program, int location, IntBuffer params) {
		GL30.glGetUniformuiv(program, location, params);
	}
    @Override
    public void glGetUniformuiv(int program, int location, java.nio.IntBuffer params) {

        GL30.glGetUniformuiv(program, location, params);
    }
    @Override
	public void glGetVertexAttribfv (int index, int pname, FloatBuffer params) {
		GL20.glGetVertexAttribfv(index, pname, params);
	}
    @Override
    public void glGetVertexAttribfv(int index, int pname, FloatBuffer params) {

        GL20.glGetVertexAttribfv(index, pname, params);
    }

    @Override
	public void glGetVertexAttribIiv(int index, int pname, int[] params, int offset) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glGetVertexAttribIiv(int index, int pname, int[] params, int offset) {

        GL30.glGetVertexAttribIiv(index, pname, params, offset);
    }

    @Override
	public void glGetVertexAttribIiv (int index, int pname, IntBuffer params) {
		GL30.glGetVertexAttribIiv(index, pname, params);
	}

    @Override
    public void glGetVertexAttribIiv(int index, int pname, java.nio.IntBuffer params) {

        GL30.glGetVertexAttribIiv(index, pname, params);
    }

    @Override
	public void glGetVertexAttribIuiv(int index, int pname, int[] params, int offset) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glGetVertexAttribIuiv(int index, int pname, int[] params, int offset) {

        GL30.glGetVertexAttribIuiv(index, pname, params, offset);
    }

    @Override
	public void glGetVertexAttribIuiv (int index, int pname, IntBuffer params) {
		GL30.glGetVertexAttribIuiv(index, pname, params);
	}

    @Override
    public void glGetVertexAttribIuiv(int index, int pname, java.nio.IntBuffer params) {

        GL30.glGetVertexAttribIuiv(index, pname, params);
    }

    @Override
	public void glGetVertexAttribiv (int index, int pname, IntBuffer params) {
		GL20.glGetVertexAttribiv(index, pname, params);
	}

    @Override
    public void glGetVertexAttribiv(int index, int pname, IntBuffer params) {

        GL20.glGetVertexAttribiv(index, pname, params);
    }

    public void glGetVertexAttribPointerv (int index, int pname, Buffer pointer) {
		throw new UnsupportedOperationException("unsupported, won't implement");
	}

    @Override
	public void glHint (int target, int mode) {
		GL11.glHint(target, mode);
	}

    @Override
    public void glHint(int target, int mode) {

        GL11.glHint(target, mode);
    }

    @Override
	public void glInvalidateFramebuffer(int target, int numAttachments, int[] attachments, int offset) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glInvalidateFramebuffer(int target, int numAttachments, int[] attachments, int offset) {

        GL30.glInvalidateFramebuffer(target, numAttachments, attachments, offset);
    }

    @Override
	public void glInvalidateFramebuffer (int target, int numAttachments, IntBuffer attachments) {
		GL43.glInvalidateFramebuffer(target, attachments);
	}

    @Override
    public void glInvalidateFramebuffer(int target, int numAttachments, java.nio.IntBuffer attachments) {

        GL30.glInvalidateFramebuffer(target, numAttachments, attachments);
    }

    @Override
	public void glInvalidateSubFramebuffer(int target, int numAttachments, int[] attachments, int offset, int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glInvalidateSubFramebuffer(int target, int numAttachments, int[] attachments, int offset, int x, int y,
                                           int width, int height) {

        GL30.glInvalidateSubFramebuffer(target, numAttachments, attachments, offset, x, y, width, height);
    }

    @Override
	public void glInvalidateSubFramebuffer (int target, int numAttachments, IntBuffer attachments, int x, int y, int width,
		int height) {
		GL43.glInvalidateSubFramebuffer(target, attachments, x, y, width, height);
	}

    @Override
    public void glInvalidateSubFramebuffer(int target, int numAttachments, java.nio.IntBuffer attachments, int x, int y,
                                           int width, int height) {

        GL30.glInvalidateSubFramebuffer(target, numAttachments, attachments, x, y, width, height);
    }

    public boolean glIsBuffer (int buffer) {
		return GL15.glIsBuffer(buffer);
	}

    public boolean glIsEnabled (int cap) {
		return GL11.glIsEnabled(cap);
	}

    @Override
	public boolean glIsFramebuffer (int framebuffer) {
		return EXTFramebufferObject.glIsFramebufferEXT(framebuffer);
	}

    @Override
	public boolean glIsFramebuffer (int framebuffer) {
		return GL30.glIsFramebuffer(framebuffer);
	}

    @Override
    public boolean glIsFramebuffer(int handler) {

        return GL20.glIsFramebuffer(handler);
    }

    public boolean glIsProgram (int program) {
		return GL20.glIsProgram(program);
	}

    @Override
	public boolean glIsQuery (int id) {
		return GL15.glIsQuery(id);
	}

    @Override
    public boolean glIsQuery(int id) {

        return GL15.glIsQuery(id);
    }

    public boolean glIsRenderbuffer (int renderbuffer) {
		return EXTFramebufferObject.glIsRenderbufferEXT(renderbuffer);
	}

    @Override
	public boolean glIsRenderbuffer (int renderbuffer) {
		return GL30.glIsRenderbuffer(renderbuffer);
	}

    @Override
	public boolean glIsSampler (int sampler) {
		return GL33.glIsSampler(sampler);
	}

    @Override
    public boolean glIsSampler(int sampler) {

        return GL30.glIsSampler(sampler);
    }

    public boolean glIsShader (int shader) {
		return GL20.glIsShader(shader);
	}

    @Override
	public boolean glIsSync(long sync) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public boolean glIsSync(long sync) {
        return GL30.glIsSync(sync);
    }

    @Override
	public boolean glIsTexture (int texture) {
		return GL11.glIsTexture(texture);
	}

    @Override
    public boolean glIsTexture(int handler) {
        return GL11.glIsTexture(handler);
    }

    @Override
	public boolean glIsTransformFeedback (int id) {
		return GL40.glIsTransformFeedback(id);
	}

    @Override
    public boolean glIsTransformFeedback(int id) {

        return GL30.glIsTransformFeedback(id);
    }

    @Override
	public boolean glIsVertexArray (int array) {
		return GL30.glIsVertexArray(array);
	}

    @Override
	public void glLineWidth (float width) {
		GL11.glLineWidth(width);
	}

    @Override
    public void glLineWidth(float width) {

        GL11.glLineWidth(width);
    }

    public void glLinkProgram (int program) {
		GL20.glLinkProgram(program);
	}
    
    @Override
	public Buffer glMapBufferRange(int target, int offset, int length, int access) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public java.nio.Buffer glMapBufferRange(int target, int offset, int length, int access) {

        return GL30.glMapBufferRange(target, offset, length, access);
    }

    @Override
	public void glPauseTransformFeedback () {
		GL40.glPauseTransformFeedback();
	}

    @Override
    public void glPauseTransformFeedback() {

        GL30.glPauseTransformFeedback();
    }

    @Override
	public void glPixelStorei (int pname, int param) {
		GL11.glPixelStorei(pname, param);
	}

    @Override
    public void glPixelStorei(int pname, int param) {

        GL11.glPixelStorei(pname, param);
    }

    @Override
	public void glPolygonOffset (float factor, float units) {
		GL11.glPolygonOffset(factor, units);
	}

    @Override
    public void glPolygonOffset(float factor, float units) {

        GL11.glPolygonOffset(factor, units);
    }

    @Override
	public void glProgramBinary(int program, int binaryFormat, Buffer binary, int length) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glProgramBinary(int program, int binaryFormat, java.nio.Buffer binary, int length) {

        GL30.glProgramBinary(program, binaryFormat, binary, length);
    }

    @Override
	public void glProgramParameteri (int program, int pname, int value) {
		GL41.glProgramParameteri(program, pname, value);
	}

    @Override
    public void glProgramParameteri(int program, int pname, int value) {

        GL30.glProgramParameteri(program, pname, value);
    }

    @Override
	public void glReadBuffer (int mode) {		
		GL11.glReadBuffer(mode);
	}

    @Override
	public void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels) {
		if (pixels instanceof ByteBuffer)
			GL11.glReadPixels(x, y, width, height, format, type, (ByteBuffer)pixels);
		else if (pixels instanceof ShortBuffer)
			GL11.glReadPixels(x, y, width, height, format, type, (ShortBuffer)pixels);
		else if (pixels instanceof IntBuffer)
			GL11.glReadPixels(x, y, width, height, format, type, (IntBuffer)pixels);
		else if (pixels instanceof FloatBuffer)
			GL11.glReadPixels(x, y, width, height, format, type, (FloatBuffer)pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
				+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer or FloatBuffer instead. Blame LWJGL");
	}

    @Override
    public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels) {

        GL20.glReadPixels(x, y, width, height, format, type, pixels);
    }

    @Override
	public void glReleaseShaderCompiler () {
		// nothing to do here
	}

    @Override
    public void glReleaseShaderCompiler() {

        GL20.glReleaseShaderCompiler();
    }

    @Override
	public void glRenderbufferStorage (int target, int internalformat, int width, int height) {
		EXTFramebufferObject.glRenderbufferStorageEXT(target, internalformat, width, height);
	}

    @Override
	public void glRenderbufferStorage (int target, int internalformat, int width, int height) {
		GL30.glRenderbufferStorage(target, internalformat, width, height);
	}

    @Override
    public void glRenderbufferStorage(int target, int internalformat, int width, int height) {

        GL20.glRenderbufferStorage(target, internalformat, width, height);
    }

    @Override
	public void glRenderbufferStorageMultisample (int target, int samples, int internalformat, int width, int height) {
		GL30.glRenderbufferStorageMultisample(target, samples, internalformat, width, height);
	}

    @Override
    public void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width, int height) {

        GL30.glRenderbufferStorageMultisample(target, samples, internalformat, width, height);
    }

    @Override
	public void glResumeTransformFeedback () {
		GL40.glResumeTransformFeedback();
	}

    @Override
    public void glResumeTransformFeedback() {

        GL30.glResumeTransformFeedback();
    }

    @Override
	public void glSampleCoverage (float value, boolean invert) {
		GL13.glSampleCoverage(value, invert);
	}

    @Override
    public void glSampleCoverage(float value, boolean invert) {

        GL13.glSampleCoverage(value, invert);
    }

    @Override
	public void glSamplerParameterf (int sampler, int pname, float param) {
		GL33.glSamplerParameterf(sampler, pname, param);
	}

    @Override
    public void glSamplerParameterf(int sampler, int pname, float param) {

        GL30.glSamplerParameterf(sampler, pname, param);
    }

    @Override
	public void glSamplerParameterfv(int sampler, int pname, float[] param, int offset) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glSamplerParameterfv(int sampler, int pname, float[] param, int offset) {

        GL30.glSamplerParameterfv(sampler, pname, param, offset);
    }

    @Override
	public void glSamplerParameterfv (int sampler, int pname, FloatBuffer param) {
		GL33.glSamplerParameterfv(sampler, pname, param);
	}

    @Override
    public void glSamplerParameterfv(int sampler, int pname, java.nio.FloatBuffer param) {

        GL30.glSamplerParameterfv(sampler, pname, param);
    }

    @Override
	public void glSamplerParameteri (int sampler, int pname, int param) {
		GL33.glSamplerParameteri(sampler, pname, param);
	}

    @Override
    public void glSamplerParameteri(int sampler, int pname, int param) {

        GL30.glSamplerParameteri(sampler, pname, param);
    }

    @Override
	public void glSamplerParameteriv(int sampler, int pname, int[] param, int offset) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glSamplerParameteriv(int sampler, int pname, int[] param, int offset) {

        GL30.glSamplerParameteriv(sampler, pname, param, offset);
    }

    @Override
	public void glSamplerParameteriv (int sampler, int pname, IntBuffer param) {
		GL33.glSamplerParameteriv(sampler, pname, param);
	}

    @Override
    public void glSamplerParameteriv(int sampler, int pname, java.nio.IntBuffer param) {

        GL30.glSamplerParameteriv(sampler, pname, param);
    }

    @Override
	public void glScissor (int x, int y, int width, int height) {
		GL11.glScissor(x, y, width, height);
	}

    @Override
    public void glScissor(int x, int y, int width, int height) {

        GL11.glScissor(x, y, width, height);
    }

    @Override
	public void glShaderBinary (int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
		throw new UnsupportedOperationException("unsupported, won't implement");
	}

    @Override
    public void glShaderBinary(int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {

        GL20.glShaderBinary(n, shaders, binaryformat, binary, length);
    }

    public void glShaderSource (int shader, String string) {
		GL20.glShaderSource(shader, string);
	}

    @Override
	public void glStencilFunc (int func, int ref, int mask) {
		GL11.glStencilFunc(func, ref, mask);
	}

    @Override
    public void glStencilFunc(int func, int ref, int mask) {

        GL11.glStencilFunc(func, ref, mask);
    }

    @Override
	public void glStencilFuncSeparate (int face, int func, int ref, int mask) {
		GL20.glStencilFuncSeparate(face, func, ref, mask);
	}

    @Override
    public void glStencilFuncSeparate(int face, int func, int ref, int mask) {

        GL20.glStencilFuncSeparate(face, func, ref, mask);
    }

    @Override
	public void glStencilMask (int mask) {
		GL11.glStencilMask(mask);
	}

    @Override
    public void glStencilMask(int mask) {

        GL11.glStencilMask(mask);
    }

    @Override
	public void glStencilMaskSeparate (int face, int mask) {
		GL20.glStencilMaskSeparate(face, mask);
	}

    @Override
    public void glStencilMaskSeparate(int face, int mask) {

        GL20.glStencilMaskSeparate(face, mask);
    }

    @Override
	public void glStencilOp (int fail, int zfail, int zpass) {
		GL11.glStencilOp(fail, zfail, zpass);
	}

    @Override
    public void glStencilOp(int fail, int zfail, int zpass) {

        GL11.glStencilOp(fail, zfail, zpass);
    }

    @Override
	public void glStencilOpSeparate (int face, int fail, int zfail, int zpass) {
		GL20.glStencilOpSeparate(face, fail, zfail, zpass);
	}

    @Override
    public void glStencilOpSeparate(int face, int fail, int zfail, int zpass) {

        GL20.glStencilOpSeparate(face, fail, zfail, zpass);
    }

    @Override
	public void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format, int type,
		Buffer pixels) {
		if (pixels == null)
			GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, (ByteBuffer)null);
		else if (pixels instanceof ByteBuffer)
			GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, (ByteBuffer)pixels);
		else if (pixels instanceof ShortBuffer)
			GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, (ShortBuffer)pixels);
		else if (pixels instanceof IntBuffer)
			GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, (IntBuffer)pixels);
		else if (pixels instanceof FloatBuffer)
			GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, (FloatBuffer)pixels);
		else if (pixels instanceof DoubleBuffer)
			GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, (DoubleBuffer)pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
				+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead. Blame LWJGL");
	}

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
                             int type, Buffer pixels) {

        GL20.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    @Override
	public void glTexImage3D (int target, int level, int internalformat, int width, int height, int depth, int border, int format,
			int type, Buffer pixels) {
		if (pixels == null)
			GL12.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, (ByteBuffer)null);
		else if (pixels instanceof ByteBuffer)
			GL12.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, (ByteBuffer)pixels);
		else if (pixels instanceof ShortBuffer)
			GL12.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, (ShortBuffer)pixels);
		else if (pixels instanceof IntBuffer)
			GL12.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, (IntBuffer)pixels);
		else if (pixels instanceof FloatBuffer)
			GL12.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, (FloatBuffer)pixels);
		else if (pixels instanceof DoubleBuffer)
			GL12.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, (DoubleBuffer)pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
					+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead. Blame LWJGL");
	}

    @Override
	public void glTexImage3D (int target, int level, int internalformat, int width, int height, int depth, int border, int format,
		int type, int offset) {
		GL12.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, offset);
	}

    @Override
	public void glTexParameterf (int target, int pname, float param) {
		GL11.glTexParameterf(target, pname, param);
	}

    @Override
    public void glTexParameterf(int target, int pname, float param) {

        GL11.glTexParameterf(target, pname, param);
    }

    @Override
	public void glTexParameterfv (int target, int pname, FloatBuffer params) {
		GL11.glTexParameterfv(target, pname, params);
	}

    @Override
    public void glTexParameterfv(int target, int pname, FloatBuffer params) {

        GL11.glTexParameterfv(target, pname, params);
    }

    @Override
	public void glTexParameteri (int target, int pname, int param) {
		GL11.glTexParameteri(target, pname, param);
	}

    @Override
    public void glTexParameteri(int target, int pname, int param) {

        GL11.glTexParameteri(target, pname, param);
    }

    @Override
	public void glTexParameteriv (int target, int pname, IntBuffer params) {
		GL11.glTexParameteriv(target, pname, params);
	}

    @Override
    public void glTexParameteriv(int target, int pname, IntBuffer params) {

        GL11.glTexParameteriv(target, pname, params);
    }

    @Override
	public void glTexStorage2D(int target, int levels, int internalformat, int width, int height) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glTexStorage2D(int target, int levels, int internalformat, int width, int height) {

        GL30.glTexStorage2D(target, levels, internalformat, width, height);
    }

    @Override
	public void glTexStorage3D(int target, int levels, int internalformat, int width, int height, int depth) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glTexStorage3D(int target, int levels, int internalformat, int width, int height, int depth) {

        GL30.glTexStorage3D(target, levels, internalformat, width, height, depth);
    }

    @Override
	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		Buffer pixels) {
		if (pixels instanceof ByteBuffer)
			GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (ByteBuffer)pixels);
		else if (pixels instanceof ShortBuffer)
			GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (ShortBuffer)pixels);
		else if (pixels instanceof IntBuffer)
			GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (IntBuffer)pixels);
		else if (pixels instanceof FloatBuffer)
			GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (FloatBuffer)pixels);
		else if (pixels instanceof DoubleBuffer)
			GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (DoubleBuffer)pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
				+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead. Blame LWJGL");
	}

    @Override
    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
                                int type, Buffer pixels) {

        GL20.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    @Override
	public void glTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
			int format, int type, Buffer pixels) {
		if (pixels instanceof ByteBuffer)
			GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, (ByteBuffer)pixels);
		else if (pixels instanceof ShortBuffer)
			GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, (ShortBuffer)pixels);
		else if (pixels instanceof IntBuffer)
			GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, (IntBuffer)pixels);
		else if (pixels instanceof FloatBuffer)
			GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, (FloatBuffer)pixels);
		else if (pixels instanceof DoubleBuffer)
			GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, (DoubleBuffer)pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
					+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead. Blame LWJGL");
	}

    @Override
	public void glTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
		int format, int type, int offset) {
		GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, offset);
	}

    @Override
	public void glTransformFeedbackVaryings (int program, String[] varyings, int bufferMode) {
		GL30.glTransformFeedbackVaryings(program, varyings, bufferMode);
	}

    @Override
    public void glTransformFeedbackVaryings(int program, String[] varyings, int bufferMode) {

        GL30.glTransformFeedbackVaryings(program, varyings, bufferMode);
    }

    @Override
	public void glUniform1f (int location, float x) {
		GL20.glUniform1f(location, x);
	}

    @Override
    public void glUniform1f(int location, float x) {

        GL20.glUniform1f(location, x);
    }

    @Override
	public void glUniform1fv (int location, int count, float[] v, int offset) {
		GL20.glUniform1fv(location, toFloatBuffer(v, offset, count));
	}

    @Override
    public void glUniform1fv(int location, int count, float[] v, int offset) {

        GL20.glUniform1fv(location, count, v, offset);
    }

    @Override
	public void glUniform1fv (int location, int count, FloatBuffer v) {
		GL20.glUniform1fv(location, v);
	}

    @Override
    public void glUniform1fv(int location, int count, FloatBuffer v) {

        GL20.glUniform1fv(location, count, v);
    }

    @Override
	public void glUniform1i (int location, int x) {
		GL20.glUniform1i(location, x);
	}

    @Override
    public void glUniform1i(int location, int x) {

        GL20.glUniform1i(location, x);
    }

    @Override
	public void glUniform1iv (int location, int count, int[] v, int offset) {
		GL20.glUniform1iv(location, toIntBuffer(v, offset, count));
	}

    @Override
    public void glUniform1iv(int location, int count, int[] v, int offset) {

        GL20.glUniform1iv(location, count, v, offset);
    }

    @Override
	public void glUniform1iv (int location, int count, IntBuffer v) {
		GL20.glUniform1iv(location, v);
	}

    @Override
    public void glUniform1iv(int location, int count, IntBuffer v) {

        GL20.glUniform1iv(location, count, v);
    }

    @Override
	public void glUniform1ui(int location, int v0) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glUniform1ui(int location, int v0) {

        GL30.glUniform1ui(location, v0);
    }

    @Override
	public void glUniform1uiv(int location, int count, int[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glUniform1uiv(int location, int count, int[] value, int offset) {

        GL30.glUniform1uiv(location, count, value, offset);
    }

    @Override
	public void glUniform1uiv (int location, int count, IntBuffer value) {
		GL30.glUniform1uiv(location, value);
	}

    @Override
    public void glUniform1uiv(int location, int count, java.nio.IntBuffer value) {

        GL30.glUniform1uiv(location, count, value);
    }

    @Override
	public void glUniform2f (int location, float x, float y) {
		GL20.glUniform2f(location, x, y);
	}

    @Override
    public void glUniform2f(int location, float x, float y) {

        GL20.glUniform2f(location, x, y);
    }

    @Override
	public void glUniform2fv (int location, int count, float[] v, int offset) {
		GL20.glUniform2fv(location, toFloatBuffer(v, offset, count << 1));
	}

    @Override
    public void glUniform2fv(int location, int count, float[] v, int offset) {

        GL20.glUniform2fv(location, count, v, offset);
    }

    @Override
	public void glUniform2fv (int location, int count, FloatBuffer v) {
		GL20.glUniform2fv(location, v);
	}

    @Override
    public void glUniform2fv(int location, int count, FloatBuffer v) {

        GL20.glUniform2fv(location, count, v);
    }

    @Override
	public void glUniform2i (int location, int x, int y) {
		GL20.glUniform2i(location, x, y);
	}

    @Override
    public void glUniform2i(int location, int x, int y) {

        GL20.glUniform2i(location, x, y);
    }

    @Override
	public void glUniform2iv (int location, int count, int[] v, int offset) {
		GL20.glUniform2iv(location, toIntBuffer(v, offset, count << 1));
	}

    @Override
    public void glUniform2iv(int location, int count, int[] v, int offset) {

        GL20.glUniform2iv(location, count, v, offset);
    }

    @Override
	public void glUniform2iv (int location, int count, IntBuffer v) {
		GL20.glUniform2iv(location, v);
	}

    @Override
    public void glUniform2iv(int location, int count, IntBuffer v) {

        GL20.glUniform2iv(location, count, v);
    }

    @Override
	public void glUniform2ui(int location, int v0, int v1) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glUniform2ui(int location, int v0, int v1) {

        GL30.glUniform2ui(location, v0, v1);
    }

    @Override
	public void glUniform2uiv(int location, int count, int[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glUniform2uiv(int location, int count, int[] value, int offset) {

        GL30.glUniform2uiv(location, count, value, offset);
    }

    @Override
	public void glUniform2uiv(int location, int count, IntBuffer value) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glUniform2uiv(int location, int count, java.nio.IntBuffer value) {

        GL30.glUniform2uiv(location, count, value);
    }

    @Override
	public void glUniform3f (int location, float x, float y, float z) {
		GL20.glUniform3f(location, x, y, z);
	}

    @Override
    public void glUniform3f(int location, float x, float y, float z) {

        GL20.glUniform3f(location, x, y, z);
    }

    @Override
	public void glUniform3fv (int location, int count, float[] v, int offset) {
		GL20.glUniform3fv(location, toFloatBuffer(v, offset, count * 3));
	}

    @Override
    public void glUniform3fv(int location, int count, float[] v, int offset) {

        GL20.glUniform3fv(location, count, v, offset);
    }

    @Override
	public void glUniform3fv (int location, int count, FloatBuffer v) {
		GL20.glUniform3fv(location, v);
	}

    @Override
    public void glUniform3fv(int location, int count, FloatBuffer v) {

        GL20.glUniform3fv(location, count, v);
    }

    @Override
	public void glUniform3i (int location, int x, int y, int z) {
		GL20.glUniform3i(location, x, y, z);
	}

    @Override
    public void glUniform3i(int location, int x, int y, int z) {

        GL20.glUniform3i(location, x, y, z);
    }

    @Override
	public void glUniform3iv (int location, int count, int[] v, int offset) {
		GL20.glUniform3iv(location, toIntBuffer(v, offset, count * 3));
	}
    
    @Override
    public void glUniform3iv(int location, int count, int[] v, int offset) {

        GL20.glUniform3iv(location, count, v, offset);
    }

    @Override
	public void glUniform3iv (int location, int count, IntBuffer v) {
		GL20.glUniform3iv(location, v);
	}

    @Override
    public void glUniform3iv(int location, int count, IntBuffer v) {

        GL20.glUniform3iv(location, count, v);
    }

    @Override
	public void glUniform3ui(int location, int v0, int v1, int v2) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glUniform3ui(int location, int v0, int v1, int v2) {

        GL30.glUniform3ui(location, v0, v1, v2);
    }

    @Override
	public void glUniform3uiv(int location, int count, int[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glUniform3uiv(int location, int count, int[] value, int offset) {

        GL30.glUniform3uiv(location, count, value, offset);
    }

    @Override
	public void glUniform3uiv (int location, int count, IntBuffer value) {
		GL30.glUniform3uiv(location, value);
	}

    @Override
    public void glUniform3uiv(int location, int count, java.nio.IntBuffer value) {

        GL30.glUniform3uiv(location, count, value);
    }

    @Override
	public void glUniform4f (int location, float x, float y, float z, float w) {
		GL20.glUniform4f(location, x, y, z, w);
	}

    @Override
    public void glUniform4f(int location, float x, float y, float z, float w) {

        GL20.glUniform4f(location, x, y, z, w);
    }

    @Override
	public void glUniform4fv (int location, int count, float[] v, int offset) {
		GL20.glUniform4fv(location, toFloatBuffer(v, offset, count << 2));
	}

    @Override
    public void glUniform4fv(int location, int count, float[] v, int offset) {

        GL20.glUniform4fv(location, count, v, offset);
    }

    @Override
	public void glUniform4fv (int location, int count, FloatBuffer v) {
		GL20.glUniform4fv(location, v);
	}

    @Override
    public void glUniform4fv(int location, int count, FloatBuffer v) {

        GL20.glUniform4fv(location, count, v);
    }

    @Override
	public void glUniform4i (int location, int x, int y, int z, int w) {
		GL20.glUniform4i(location, x, y, z, w);
	}

    @Override
    public void glUniform4i(int location, int x, int y, int z, int w) {

        GL20.glUniform4i(location, x, y, z, w);
    }

    @Override
	public void glUniform4iv (int location, int count, int[] v, int offset) {
		GL20.glUniform4iv(location, toIntBuffer(v, offset, count << 2));
	}

    @Override
    public void glUniform4iv(int location, int count, int[] v, int offset) {

        GL20.glUniform4iv(location, count, v, offset);
    }

    @Override
	public void glUniform4iv (int location, int count, IntBuffer v) {
		GL20.glUniform4iv(location, v);
	}

    @Override
    public void glUniform4iv(int location, int count, IntBuffer v) {

        GL20.glUniform4iv(location, count, v);
    }

    @Override
	public void glUniform4ui(int location, int v0, int v1, int v2, int v3) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glUniform4ui(int location, int v0, int v1, int v2, int v3) {

        GL30.glUniform4ui(location, v0, v1, v2, v3);
    }

    @Override
	public void glUniform4uiv(int location, int count, int[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glUniform4uiv(int location, int count, int[] value, int offset) {

        GL30.glUniform4uiv(location, count, value, offset);
    }

    @Override
	public void glUniform4uiv (int location, int count, IntBuffer value) {
		GL30.glUniform4uiv(location, value);
	}

    @Override
    public void glUniform4uiv(int location, int count, java.nio.IntBuffer value) {

        GL30.glUniform4uiv(location, count, value);
    }

    @Override
	public void glUniformBlockBinding (int program, int uniformBlockIndex, int uniformBlockBinding) {
		GL31.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
	}

    @Override
    public void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding) {

        GL30.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
    }

    @Override
	public void glUniformMatrix2fv (int location, int count, boolean transpose, float[] value, int offset) {
		GL20.glUniformMatrix2fv(location, transpose, toFloatBuffer(value, offset, count << 2));
	}

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, float[] value, int offset) {

        GL20.glUniformMatrix2fv(location, count, transpose, value, offset);
    }

    @Override
	public void glUniformMatrix2fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL20.glUniformMatrix2fv(location, transpose, value);
	}

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer value) {

        GL20.glUniformMatrix2fv(location, count, transpose, value);
    }

    @Override
	public void glUniformMatrix2x3fv(int location, int count, boolean transpose, float[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glUniformMatrix2x3fv(int location, int count, boolean transpose, float[] value, int offset) {

        GL30.glUniformMatrix2x3fv(location, count, transpose, value, offset);
    }

    @Override
	public void glUniformMatrix2x3fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL21.glUniformMatrix2x3fv(location, transpose, value);
	}

    @Override
    public void glUniformMatrix2x3fv(int location, int count, boolean transpose, java.nio.FloatBuffer value) {

        GL30.glUniformMatrix2x3fv(location, count, transpose, value);
    }

    @Override
	public void glUniformMatrix2x4fv(int location, int count, boolean transpose, float[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glUniformMatrix2x4fv(int location, int count, boolean transpose, float[] value, int offset) {

        GL30.glUniformMatrix2x4fv(location, count, transpose, value, offset);
    }

    @Override
	public void glUniformMatrix2x4fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL21.glUniformMatrix2x4fv(location, transpose, value);
	}

    @Override
    public void glUniformMatrix2x4fv(int location, int count, boolean transpose, java.nio.FloatBuffer value) {

        GL30.glUniformMatrix2x4fv(location, count, transpose, value);
    }

    @Override
	public void glUniformMatrix3fv (int location, int count, boolean transpose, float[] value, int offset) {
		GL20.glUniformMatrix3fv(location, transpose, toFloatBuffer(value, offset, count * 9));
	}

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, float[] value, int offset) {

        GL20.glUniformMatrix3fv(location, count, transpose, value, offset);
    }

    @Override
	public void glUniformMatrix3fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL20.glUniformMatrix3fv(location, transpose, value);
	}

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer value) {

        GL20.glUniformMatrix3fv(location, count, transpose, value);
    }

    @Override
	public void glUniformMatrix3x2fv(int location, int count, boolean transpose, float[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glUniformMatrix3x2fv(int location, int count, boolean transpose, float[] value, int offset) {

        GL30.glUniformMatrix3x2fv(location, count, transpose, value, offset);
    }

    @Override
	public void glUniformMatrix3x2fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL21.glUniformMatrix3x2fv(location, transpose, value);
	}

    @Override
    public void glUniformMatrix3x2fv(int location, int count, boolean transpose, java.nio.FloatBuffer value) {

        GL30.glUniformMatrix3x2fv(location, count, transpose, value);
    }

    @Override
	public void glUniformMatrix3x4fv(int location, int count, boolean transpose, float[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glUniformMatrix3x4fv(int location, int count, boolean transpose, float[] value, int offset) {

        GL30.glUniformMatrix3x4fv(location, count, transpose, value, offset);
    }

    @Override
	public void glUniformMatrix3x4fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL21.glUniformMatrix3x4fv(location, transpose, value);
	}

    @Override
    public void glUniformMatrix3x4fv(int location, int count, boolean transpose, java.nio.FloatBuffer value) {

        GL30.glUniformMatrix3x4fv(location, count, transpose, value);
    }

    @Override
	public void glUniformMatrix4fv (int location, int count, boolean transpose, float[] value, int offset) {
		GL20.glUniformMatrix4fv(location, transpose, toFloatBuffer(value, offset, count << 4));
	}

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int offset) {

        GL20.glUniformMatrix4fv(location, count, transpose, value, offset);
    }

    @Override
	public void glUniformMatrix4fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL20.glUniformMatrix4fv(location, transpose, value);
	}

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer value) {

        GL20.glUniformMatrix4fv(location, count, transpose, value);
    }

    @Override
	public void glUniformMatrix4x2fv(int location, int count, boolean transpose, float[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glUniformMatrix4x2fv(int location, int count, boolean transpose, float[] value, int offset) {

        GL30.glUniformMatrix4x2fv(location, count, transpose, value, offset);
    }

    @Override
	public void glUniformMatrix4x2fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL21.glUniformMatrix4x2fv(location, transpose, value);
	}

    @Override
    public void glUniformMatrix4x2fv(int location, int count, boolean transpose, java.nio.FloatBuffer value) {

        GL30.glUniformMatrix4x2fv(location, count, transpose, value);
    }

    @Override
	public void glUniformMatrix4x3fv(int location, int count, boolean transpose, float[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glUniformMatrix4x3fv(int location, int count, boolean transpose, float[] value, int offset) {

        GL30.glUniformMatrix4x3fv(location, count, transpose, value, offset);
    }

    @Override
	public void glUniformMatrix4x3fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL21.glUniformMatrix4x3fv(location, transpose, value);
	}

    @Override
    public void glUniformMatrix4x3fv(int location, int count, boolean transpose, java.nio.FloatBuffer value) {

        GL30.glUniformMatrix4x3fv(location, count, transpose, value);
    }

    @Override
	public boolean glUnmapBuffer (int target) {
		return GL15.glUnmapBuffer(target);
	}

    @Override
    public boolean glUnmapBuffer(int target) {

        return GL15.glUnmapBuffer(target);
    }

    @Override
	public void glUseProgram (int program) {
		GL20.glUseProgram(program);
	}

    @Override
    public void glUseProgram(int program) {

        GL20.glUseProgram(program);
    }

    @Override
	public void glValidateProgram (int program) {
		GL20.glValidateProgram(program);
	}

    @Override
    public void glValidateProgram(int program) {

        GL20.glValidateProgram(program);
    }

    @Override
	public String glVersion() {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public String glVersion() {
        return GL11.glGetString(GL11.GL_VERSION);
    }

    @Override
	public void glVertexAttrib1f (int indx, float x) {
		GL20.glVertexAttrib1f(indx, x);
	}

    @Override
    public void glVertexAttrib1f(int indx, float x) {

        GL20.glVertexAttrib1f(indx, x);
    }

    @Override
	public void glVertexAttrib1fv (int indx, FloatBuffer values) {
		GL20.glVertexAttrib1f(indx, values.get());
	}

    @Override
    public void glVertexAttrib1fv(int indx, FloatBuffer values) {

        GL20.glVertexAttrib1fv(indx, values);
    }

    @Override
	public void glVertexAttrib2f (int indx, float x, float y) {
		GL20.glVertexAttrib2f(indx, x, y);
	}

    @Override
    public void glVertexAttrib2f(int indx, float x, float y) {

        GL20.glVertexAttrib2f(indx, x, y);
    }

    @Override
	public void glVertexAttrib2fv (int indx, FloatBuffer values) {
		GL20.glVertexAttrib2f(indx, values.get(), values.get());
	}

    @Override
    public void glVertexAttrib2fv(int indx, FloatBuffer values) {

        GL20.glVertexAttrib2fv(indx, values);
    }

    @Override
	public void glVertexAttrib3f (int indx, float x, float y, float z) {
		GL20.glVertexAttrib3f(indx, x, y, z);
	}

    @Override
    public void glVertexAttrib3f(int indx, float x, float y, float z) {

        GL20.glVertexAttrib3f(indx, x, y, z);
    }

    @Override
	public void glVertexAttrib3fv (int indx, FloatBuffer values) {
		GL20.glVertexAttrib3f(indx, values.get(), values.get(), values.get());
	}

    @Override
    public void glVertexAttrib3fv(int indx, FloatBuffer values) {

        GL20.glVertexAttrib3fv(indx, values);
    }

    @Override
	public void glVertexAttrib4f (int indx, float x, float y, float z, float w) {
		GL20.glVertexAttrib4f(indx, x, y, z, w);
	}

    @Override
    public void glVertexAttrib4f(int indx, float x, float y, float z, float w) {

        GL20.glVertexAttrib4f(indx, x, y, z, w);
    }

    @Override
	public void glVertexAttrib4fv (int indx, FloatBuffer values) {
		GL20.glVertexAttrib4f(indx, values.get(), values.get(), values.get(), values.get());
	}

    @Override
    public void glVertexAttrib4fv(int indx, FloatBuffer values) {

        GL20.glVertexAttrib4fv(indx, values);
    }

    @Override
	public void glVertexAttribDivisor (int index, int divisor) {
		GL33.glVertexAttribDivisor(index, divisor);
	}

    @Override
    public void glVertexAttribDivisor(int index, int divisor) {

        GL30.glVertexAttribDivisor(index, divisor);
    }

    @Override
	public void glVertexAttribI4i (int index, int x, int y, int z, int w) {
		GL30.glVertexAttribI4i(index, x, y, z, w);
	}

    @Override
    public void glVertexAttribI4i(int index, int x, int y, int z, int w) {

        GL30.glVertexAttribI4i(index, x, y, z, w);
    }

    @Override
	public void glVertexAttribI4iv(int index, int[] v, int offset) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glVertexAttribI4iv(int index, int[] v, int offset) {

        GL30.glVertexAttribI4iv(index, v, offset);
    }

    @Override
	public void glVertexAttribI4iv(int index, IntBuffer v) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glVertexAttribI4iv(int index, java.nio.IntBuffer v) {

        GL30.glVertexAttribI4iv(index, v);
    }

    @Override
	public void glVertexAttribI4ui (int index, int x, int y, int z, int w) {
		GL30.glVertexAttribI4ui(index, x, y, z, w);
	}

    @Override
    public void glVertexAttribI4ui(int index, int x, int y, int z, int w) {

        GL30.glVertexAttribI4ui(index, x, y, z, w);
    }

    @Override
	public void glVertexAttribI4uiv(int index, int[] v, int offset) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glVertexAttribI4uiv(int index, int[] v, int offset) {

        GL30.glVertexAttribI4uiv(index, v, offset);
    }

    @Override
	public void glVertexAttribI4uiv(int index, IntBuffer v) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void glVertexAttribI4uiv(int index, java.nio.IntBuffer v) {

        GL30.glVertexAttribI4uiv(index, v);
    }

    @Override
	public void glVertexAttribIPointer (int index, int size, int type, int stride, int offset) {
		GL30.glVertexAttribIPointer(index, size, type, stride, offset);
	}

    @Override
    public void glVertexAttribIPointer(int index, int size, int type, int stride, int offset) {

        GL30.glVertexAttribIPointer(index, size, type, stride, offset);
    }

    @Override
	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, Buffer buffer) {
		if (buffer instanceof ByteBuffer) {
			if (type == GL_BYTE)				
				GL20.glVertexAttribPointer(indx, size, type, normalized, stride, (ByteBuffer)buffer);
			else if (type == GL_UNSIGNED_BYTE)
				GL20.glVertexAttribPointer(indx, size, type, normalized, stride, (ByteBuffer)buffer);
			else if (type == GL_SHORT)
				GL20.glVertexAttribPointer(indx, size, type, normalized, stride, ((ByteBuffer)buffer).asShortBuffer());
			else if (type == GL_UNSIGNED_SHORT)
				GL20.glVertexAttribPointer(indx, size, type, normalized, stride, ((ByteBuffer)buffer).asShortBuffer());
			else if (type == GL_FLOAT)
				GL20.glVertexAttribPointer(indx, size, type, normalized, stride, ((ByteBuffer)buffer).asFloatBuffer());
			else
				throw new GdxRuntimeException(
					"Can't use "
						+ buffer.getClass().getName()
						+ " with type "
						+ type
						+ " with this method. Use ByteBuffer and one of GL_BYTE, GL_UNSIGNED_BYTE, GL_SHORT, GL_UNSIGNED_SHORT or GL_FLOAT for type. Blame LWJGL");
		} else if (buffer instanceof FloatBuffer) {
			if (type == GL_FLOAT)
				GL20.glVertexAttribPointer(indx, size, type, normalized, stride, (FloatBuffer)buffer);
			else
				throw new GdxRuntimeException("Can't use " + buffer.getClass().getName() + " with type " + type
					+ " with this method.");
		} else
			throw new GdxRuntimeException("Can't use " + buffer.getClass().getName()
				+ " with this method. Use ByteBuffer instead. Blame LWJGL");
	}

    @Override
    public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, Buffer ptr) {

        GL20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
    }

    @Override
	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, int ptr) {
		GL20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
	}

    @Override
    public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int ptr) {

        GL20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
    }

    @Override
	public void glViewport (int x, int y, int width, int height) {
		GL11.glViewport(x, y, width, height);
	}

    @Override
    public void glViewport(int x, int y, int width, int height) {
        GL11.glViewport(x, y, width, height);
    }

    @Override
	public void glWaitSync(long sync, int flags, long timeout) {
		GL32.glWaitSync(sync, flags, timeout);
	}

    @Override
    public void glWaitSync(long sync, int flags, long timeout) {
        GL30.glWaitSync(sync, flags, timeout);
    }

    @Override
    public boolean limitGLESContext() {
        return false;
    }

    @Override
    public boolean setBlending(final boolean enabled, final int sFactor, final int dFactor) {
        boolean change = capabilitySwitch(enabled, GL11.GL_BLEND);
        if (enabled && (this.blendSFactor != sFactor || this.blendDFactor != dFactor)) {
            GL11.glBlendFunc(sFactor, dFactor);
            this.blendSFactor = sFactor;
            this.blendDFactor = dFactor;
            change |= true;
        }
        return change;
    }

    @Override
	public void setCullFace(int mode) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void setCullFace(int mode) {
        if (cullFace == mode)
            return;
        GL11.glCullFace(mode);
        cullFace = mode;
    }

    @Override
	public boolean setDepthMask(boolean depthMask) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public boolean setDepthMask(final boolean depthMask) {

        if (this.depthMask != depthMask) {
            GL11.glDepthMask(depthMask);
            return true;
        }
        return false;
    }

    @Override
	public boolean setDepthTest(int depthFunction, float depthRangeNear, float depthRangeFar) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public boolean setDepthTest(final int depthFunction, final float depthRangeNear, final float depthRangeFar) {

        final boolean enable = depthFunction != 0;
        boolean changed = capabilitySwitch(enable, GL11.GL_DEPTH_TEST);
        if (enable && (depthFunc != depthFunction || this.depthRangeNear != depthRangeNear
                || this.depthRangeFar != depthRangeFar)) {
            if (depthFunc != depthFunction)
                GL11.glDepthFunc(depthFunc = depthFunction);
            if (this.depthRangeNear != depthRangeNear || this.depthRangeFar != depthRangeFar)
                GL20.glDepthRangef(this.depthRangeNear = depthRangeNear, this.depthRangeFar = depthRangeFar);
            changed |= true;
        }
        return changed;
    }

    @Override
	public boolean supportsExtension(String extension) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public boolean supportsExtension(String extension) {

        if (extensions == null)
            extensions = GL11.glGetString(GL11.GL_EXTENSIONS);
        return extensions.contains(extension);
    }

    @Override
	public boolean supportsRenderer(String renderer) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public boolean supportsRenderer(String renderer) {

        if (renderers == null)
            renderers = GL11.glGetString(GL11.GL_RENDERER);
        return renderers.contains(renderer);
    }

    private FloatBuffer toFloatBuffer (float v[], int offset, int count) {
		ensureBufferCapacity(count << 2);
		floatBuffer.clear();
		floatBuffer.limit(count);
		floatBuffer.put(v,  offset, count);
		floatBuffer.position(0);
		return floatBuffer;
	}

    private IntBuffer toIntBuffer (int v[], int offset, int count) {
		ensureBufferCapacity(count << 2);
		intBuffer.clear();
		intBuffer.limit(count);
		intBuffer.put(v, offset, count);
		intBuffer.position(0);
		return intBuffer;
	}

    @Override
    public void validateAll() {
        shaderPrograms.clear();
        meshes.clear();
        // reset chaced value cause validate
        enabledCaps.clear();
        this.depthMask = false;
        this.depthFunc = GL11.GL_LEQUAL;
        this.depthRangeNear = 0;
        this.depthRangeFar = 1;
        this.blendSFactor = GL11.GL_ONE;
        this.blendDFactor = GL11.GL_ZERO;
        this.cullFace = GL11.GL_BACK;
        // in future directly regenerate here to efficiently

        Texture.invalidateAllTextures();
        Cubemap.invalidateAllCubemaps();
        TextureArray.invalidateAllTextureArrays();
        GLFrameBuffer.invalidateAllFrameBuffers();
    }

    @Override
	public boolean validMesh(int[] handlers) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public boolean validMesh(int[] handlers) {

        if (meshes.contains(handlers, false))
            return true;
        meshes.removeValue(handlers, false);
        Arrays.fill(handlers, -1);
        return false;
    }

    @Override
	public boolean validShaderProgram(int[] handlers) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public boolean validShaderProgram(int[] handlers) {
        if (shaderPrograms.contains(handlers, false))
            return true;
        shaderPrograms.removeValue(handlers, false);
        Arrays.fill(handlers, -1);
        return false;
    }
}
