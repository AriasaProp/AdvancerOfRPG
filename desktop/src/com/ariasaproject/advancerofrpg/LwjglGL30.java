package com.ariasaproject.advancerofrpg;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.IntArray;

public class LwjglGL30 implements TGF {
	// chace value for once only
	float maxAnisotropicFilterLevel;
	boolean limitGlesContext;
	String extensions, renderers;
	// chaced data graphic function reset when invalidate
	final IntArray enabledCaps = new IntArray();
	boolean depthMask;
	float depthRangeNear, depthRangeFar;
	int blendSFactor, blendDFactor, depthFunc, cullFace;

	public static final String TAG = "OPEN GL 3.0";
	private final String shaderHeader = "#version 300\n" + "#define LOW lowp\n" + "#define MED mediump\n"
			+ "#ifdef GL_FRAGMENT_PRECISION_HIGH\n" + "#define HIGH highp\n" + "#else\n" + "#define HIGH mediump\n"
			+ "#endif\n";
	
	public LwjglGL30() {
		String version = GL11.glGetString(TGF.GL_VERSION);
		int major = Integer.parseInt("" + version.charAt(0));
		int minor = Integer.parseInt("" + version.charAt(2));
		if (major < 3)
			throw new RuntimeException("minimum support is openGL 3.0");
	}
	@Override
	public void glActiveTexture(int unit) {
		if (unit >= getMaxTextureUnit())
			throw new IllegalArgumentException("inputed units more than max units => " + getMaxTextureUnit());
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
	}

	@Override
	public void glBindTexture(int target, int texture) {
		GL11.glBindTexture(target, texture);
	}

	@Override
	public void glBindAttribLocation(int program, int index, String name) {
		GL20.glBindAttribLocation(program, index, name);
	}

	@Override
	public void glBindBuffer(int target, int buffer) {
		GL15.glBindBuffer(target, buffer);
	}

	public void glBindFramebuffer(int target, int framebuffer) {
		EXTFramebufferObject.glBindFramebufferEXT(target, framebuffer);
	}

	public void glBindRenderbuffer(int target, int renderbuffer) {
		EXTFramebufferObject.glBindRenderbufferEXT(target, renderbuffer);
	}

	public void glBlendColor(float red, float green, float blue, float alpha) {
		GL14.glBlendColor(red, green, blue, alpha);
	}

	public void glBlendEquation(int mode) {
		GL14.glBlendEquation(mode);
	}

	public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
		GL20.glBlendEquationSeparate(modeRGB, modeAlpha);
	}

	public void glBlendFunc(int sfactor, int dfactor) {
		GL11.glBlendFunc(sfactor, dfactor);
	}

	public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		GL14.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
	}

	public void glBufferData(int target, int size, Buffer data, int usage) {
		if (data instanceof ByteBuffer)
			GL15.glBufferData(target, (ByteBuffer) data, usage);
		else if (data instanceof IntBuffer)
			GL15.glBufferData(target, (IntBuffer) data, usage);
		else if (data instanceof FloatBuffer)
			GL15.glBufferData(target, (FloatBuffer) data, usage);
		else if (data instanceof DoubleBuffer)
			GL15.glBufferData(target, (DoubleBuffer) data, usage);
		else if (data instanceof ShortBuffer) //
			GL15.glBufferData(target, (ShortBuffer) data, usage);
	}

	public void glBufferSubData(int target, int offset, int size, Buffer data) {
		if (data instanceof ByteBuffer)
			GL15.glBufferSubData(target, offset, (ByteBuffer) data);
		else if (data instanceof IntBuffer)
			GL15.glBufferSubData(target, offset, (IntBuffer) data);
		else if (data instanceof FloatBuffer)
			GL15.glBufferSubData(target, offset, (FloatBuffer) data);
		else if (data instanceof DoubleBuffer)
			GL15.glBufferSubData(target, offset, (DoubleBuffer) data);
		else if (data instanceof ShortBuffer) //
			GL15.glBufferSubData(target, offset, (ShortBuffer) data);
	}

	public int glCheckFramebufferStatus(int target) {
		return EXTFramebufferObject.glCheckFramebufferStatusEXT(target);
	}

	public void glClear(int mask) {
		GL11.glClear(mask);
	}

	public void glClearColor(float red, float green, float blue, float alpha) {
		GL11.glClearColor(red, green, blue, alpha);
	}

	public void glClearDepthf(float depth) {
		GL11.glClearDepth(depth);
	}

	public void glClearStencil(int s) {
		GL11.glClearStencil(s);
	}

	@Override
	public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
		GL11.glColorMask(red, green, blue, alpha);
	}

	@Override
	public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height,
			int border) {
		GL11.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
	}

	@Override
	public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width,
			int height) {
		GL11.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
	}

	@Override
	public void glCullFace(int mode) {
		if (cullFace == mode)
			return;
		GL11.glCullFace(mode);
		cullFace = mode;
	}

	@Override
	public void glDeleteBuffers(int n, IntBuffer buffers) {
		buffers.limit(n);
		GL15.glDeleteBuffers(buffers);
	}

	@Override
	public void glDeleteBuffer(int buffer) {
		GL15.glDeleteBuffers(buffer);
	}

	@Override
	public void glDeleteFramebuffers(int n, IntBuffer framebuffers) {
		framebuffers.limit(n);
		GL30.glDeleteFramebuffers(framebuffers);
	}

	@Override
	public void glDeleteFramebuffers(final int n, final int[] framebuffers, final int offset) {
		IntBuffer buffers = com.ariasaproject.advancerofrpg.utils.BufferUtils.newIntBuffer(n);
		buffers.put(framebuffers).flip();
		buffers.limit(n);
		buffers.position(offset);
		GL30.glDeleteFramebuffers(buffers);
	}

	@Override
	public void glDeleteFramebuffer(int framebuffer) {
		GL30.glDeleteFramebuffers(framebuffer);
	}

	@Override
	public void glDeleteProgram(int program) {
		GL20.glDeleteProgram(program);
	}

	@Override
	public void glDeleteRenderbuffers(int n, IntBuffer renderbuffers) {
		renderbuffers.limit(n);
		GL30.glDeleteRenderbuffers(renderbuffers);
	}

	@Override
	public void glDeleteRenderbuffer(int renderbuffer) {
		GL30.glDeleteRenderbuffers(renderbuffer);
	}

	@Override
	public void glDeleteTextures(int n, IntBuffer textures) {
		textures.limit(n);
		GL11.glDeleteTextures(textures);
	}

	@Override
	public void glDeleteTexture(int texture) {
		GL11.glDeleteTextures(texture);
	}

	@Override
	public void glDepthFunc(int func) {
		if (func != depthFunc)
			GL11.glDepthFunc(func);
	}

	@Override
	public void glDepthRangef(float near, float far) {
		if (depthRangeNear != near || depthRangeFar != far)
			GL11.glDepthRange((double) near, (double) far);
	}

	@Override
	public void glDetachShader(int program, int shader) {
		GL20.glDetachShader(program, shader);
	}

	@Override
	public void glDisableVertexAttribArray(int index) {
		GL20.glDisableVertexAttribArray(index);
	}

	@Override
	public void glDrawArrays(int mode, int first, int count) {
		GL11.glDrawArrays(mode, first, count);
	}

	@Override
	public void glDrawElements(int mode, int count, int type, Buffer indices) {
		switch (type) {
		case TGF.GL_BYTE: {
			ByteBuffer i = (ByteBuffer) indices;
			GL11.glDrawElements(mode, i);
		}
			break;
		case TGF.GL_SHORT: {
			ShortBuffer i = (ShortBuffer) indices;
			GL11.glDrawElements(mode, i);
		}
			break;
		case TGF.GL_INT: {
			IntBuffer i = (IntBuffer) indices;
			GL11.glDrawElements(mode, i);
		}
			break;
		default:
			throw new RuntimeException("TGF draw elements wrong type");
		}
	}

	@Override
	public void glDrawElements(int mode, int count, int type, int indices) {
		GL11.glDrawElements(mode, count, type, indices);
	}

	@Override
	public void glEnableVertexAttribArray(int index) {
		GL20.glEnableVertexAttribArray(index);
	}

	@Override
	public void glFinish() {
		GL11.glFinish();
	}

	@Override
	public void glFlush() {
		GL11.glFlush();
	}

	@Override
	public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {
		GL30.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
	}

	@Override
	public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
		GL30.glFramebufferTexture2D(target, attachment, textarget, texture, level);
	}

	@Override
	public void glFrontFace(int mode) {
		GL11.glFrontFace(mode);
	}

	@Override
	public void glGenBuffers(int n, IntBuffer buffers) {
		buffers.limit(n);
		GL15.glGenBuffers(buffers);
	}

	@Override
	public int glGenBuffer() {
		return GL15.glGenBuffers();
	}

	@Override
	public void glGenerateMipmap(int target) {
		GL30.glGenerateMipmap(target);
	}

	@Override
	public void glGenFramebuffers(int n, IntBuffer framebuffers) {
		framebuffers.limit(n);
		GL30.glGenFramebuffers(framebuffers);
	}

	@Override
	public void glGenFramebuffers(final int n, final int[] framebuffers, final int offset) {
		IntBuffer buffers = BufferUtils.createIntBuffer(n);
		buffers.put(framebuffers);
		buffers.flip();
		buffers.position(offset);
		buffers.limit(n);
		GL30.glGenFramebuffers(buffers);
	}

	@Override
	public int glGenFramebuffer() {
		return GL30.glGenFramebuffers();
	}

	@Override
	public void glGenRenderbuffers(int n, IntBuffer renderbuffers) {
		renderbuffers.limit(n);
		GL30.glGenFramebuffers(renderbuffers);
	}

	@Override
	public int glGenRenderbuffer() {
		return GL30.glGenRenderbuffers();
	}

	@Override
	public void glGenTextures(int n, IntBuffer textures) {
		textures.limit(n);
		GL11.glGenTextures(textures);
	}

	@Override
	public int glGenTexture() {
		return GL11.glGenTextures();
	}

	@Override
	public String glGetActiveAttrib(int program, int index, IntBuffer size, Buffer type) {
		return GL20.glGetActiveAttrib(program, index, size.get(0), (IntBuffer) type);
	}

	@Override
	public String glGetActiveUniform(int program, int index, IntBuffer size, Buffer type) {
		return GL20.glGetActiveUniform(program, index, size.get(0), (IntBuffer) type);
	}

	@Override
	public void glGetAttachedShaders(int program, int maxcount, Buffer count, IntBuffer shaders) {
		count.limit(maxcount);
		GL20.glGetAttachedShaders(program, (IntBuffer) count, shaders);
	}

	@Override
	public int glGetAttribLocation(int program, String name) {
		return GL20.glGetAttribLocation(program, name);
	}

	@Override
	public void glGetBooleanv(int pname, Buffer params) {
		GL11.glGetBoolean(pname, (ByteBuffer) params);
	}

	@Override
	public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
		GL15.glGetBufferParameteriv(target, pname, params);
	}

	@Override
	public int glGetError() {
		return GL11.glGetError();
	}

	@Override
	public void glGetFloatv(int pname, FloatBuffer params) {
		GL11.glGetFloat(pname, params);
	}

	@Override
	public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params) {
		GL20.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
	}

	@Override
	public void glGetIntegerv(int pname, IntBuffer params) {
		GL11.glGetInteger(pname, params);
	}

	@Override
	public int glGetIntegerv(int pname) {
		return GL11.glGetInteger(pname);
	}

	@Override
	public void glGetProgramiv(int program, int pname, IntBuffer params) {
		GL14.glGetProgramiv(program, pname, params);
	}

	@Override
	public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params) {
		GL20.glGetRenderbufferParameteriv(target, pname, params);
	}

	@Override
	public void glGetShaderiv(int shader, int pname, IntBuffer params) {

		GL20.glGetShaderiv(shader, pname, params);
	}

	@Override
	public String glGetShaderInfoLog(int shader) {

		return GL20.glGetShaderInfoLog(shader);
	}

	@Override
	public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {

		GL20.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
	}

	@Override
	public String glGetString(int name) {

		return GL20.glGetString(name);
	}

	@Override
	public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {

		GL20.glGetTexParameterfv(target, pname, params);
	}

	@Override
	public void glGetTexParameteriv(int target, int pname, IntBuffer params) {

		GL20.glGetTexParameteriv(target, pname, params);
	}

	@Override
	public void glGetUniformfv(int program, int location, FloatBuffer params) {

		GL20.glGetUniformfv(program, location, params);
	}

	@Override
	public void glGetUniformiv(int program, int location, IntBuffer params) {

		GL20.glGetUniformiv(program, location, params);
	}

	@Override
	public int glGetUniformLocation(int program, String name) {

		return GL20.glGetUniformLocation(program, name);
	}

	@Override
	public void glGetVertexAttribfv(int index, int pname, FloatBuffer params) {

		GL20.glGetVertexAttribfv(index, pname, params);
	}

	@Override
	public void glGetVertexAttribiv(int index, int pname, IntBuffer params) {

		GL20.glGetVertexAttribiv(index, pname, params);
	}

	@Override
	public void glHint(int target, int mode) {

		GL20.glHint(target, mode);
	}

	@Override
	public void glLineWidth(float width) {

		GL20.glLineWidth(width);
	}

	@Override
	public void glPixelStorei(int pname, int param) {

		GL20.glPixelStorei(pname, param);
	}

	@Override
	public void glPolygonOffset(float factor, float units) {

		GL20.glPolygonOffset(factor, units);
	}

	@Override
	public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels) {

		GL20.glReadPixels(x, y, width, height, format, type, pixels);
	}

	@Override
	public void glReleaseShaderCompiler() {

		GL20.glReleaseShaderCompiler();
	}

	@Override
	public void glRenderbufferStorage(int target, int internalformat, int width, int height) {

		GL20.glRenderbufferStorage(target, internalformat, width, height);
	}

	@Override
	public void glSampleCoverage(float value, boolean invert) {

		GL20.glSampleCoverage(value, invert);
	}

	@Override
	public void glScissor(int x, int y, int width, int height) {

		GL20.glScissor(x, y, width, height);
	}

	@Override
	public void glShaderBinary(int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {

		GL20.glShaderBinary(n, shaders, binaryformat, binary, length);
	}

	@Override
	public void glStencilFunc(int func, int ref, int mask) {

		GL20.glStencilFunc(func, ref, mask);
	}

	@Override
	public void glStencilFuncSeparate(int face, int func, int ref, int mask) {

		GL20.glStencilFuncSeparate(face, func, ref, mask);
	}

	@Override
	public void glStencilMask(int mask) {

		GL20.glStencilMask(mask);
	}

	@Override
	public void glStencilMaskSeparate(int face, int mask) {

		GL20.glStencilMaskSeparate(face, mask);
	}

	@Override
	public void glStencilOp(int fail, int zfail, int zpass) {

		GL20.glStencilOp(fail, zfail, zpass);
	}

	@Override
	public void glStencilOpSeparate(int face, int fail, int zfail, int zpass) {

		GL20.glStencilOpSeparate(face, fail, zfail, zpass);
	}

	@Override
	public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
			int type, Buffer pixels) {

		GL20.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
	}

	@Override
	public void glTexParameterf(int target, int pname, float param) {

		GL20.glTexParameterf(target, pname, param);
	}

	@Override
	public void glTexParameterfv(int target, int pname, FloatBuffer params) {

		GL20.glTexParameterfv(target, pname, params);
	}

	@Override
	public void glTexParameteri(int target, int pname, int param) {

		GL20.glTexParameteri(target, pname, param);
	}

	@Override
	public void glTexParameteriv(int target, int pname, IntBuffer params) {

		GL20.glTexParameteriv(target, pname, params);
	}

	@Override
	public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
			int type, Buffer pixels) {

		GL20.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
	}

	@Override
	public void glUniform1f(int location, float x) {

		GL20.glUniform1f(location, x);
	}

	@Override
	public void glUniform1fv(int location, int count, FloatBuffer v) {

		GL20.glUniform1fv(location, count, v);
	}

	@Override
	public void glUniform1fv(int location, int count, float[] v, int offset) {

		GL20.glUniform1fv(location, count, v, offset);
	}

	@Override
	public void glUniform1i(int location, int x) {

		GL20.glUniform1i(location, x);
	}

	@Override
	public void glUniform1iv(int location, int count, IntBuffer v) {

		GL20.glUniform1iv(location, count, v);
	}

	@Override
	public void glUniform1iv(int location, int count, int[] v, int offset) {

		GL20.glUniform1iv(location, count, v, offset);
	}

	@Override
	public void glUniform2f(int location, float x, float y) {

		GL20.glUniform2f(location, x, y);
	}

	@Override
	public void glUniform2fv(int location, int count, FloatBuffer v) {

		GL20.glUniform2fv(location, count, v);
	}

	@Override
	public void glUniform2fv(int location, int count, float[] v, int offset) {

		GL20.glUniform2fv(location, count, v, offset);
	}

	@Override
	public void glUniform2i(int location, int x, int y) {

		GL20.glUniform2i(location, x, y);
	}

	@Override
	public void glUniform2iv(int location, int count, IntBuffer v) {

		GL20.glUniform2iv(location, count, v);
	}

	@Override
	public void glUniform2iv(int location, int count, int[] v, int offset) {

		GL20.glUniform2iv(location, count, v, offset);
	}

	@Override
	public void glUniform3f(int location, float x, float y, float z) {

		GL20.glUniform3f(location, x, y, z);
	}

	@Override
	public void glUniform3fv(int location, int count, FloatBuffer v) {

		GL20.glUniform3fv(location, count, v);
	}

	@Override
	public void glUniform3fv(int location, int count, float[] v, int offset) {

		GL20.glUniform3fv(location, count, v, offset);
	}

	@Override
	public void glUniform3i(int location, int x, int y, int z) {

		GL20.glUniform3i(location, x, y, z);
	}

	@Override
	public void glUniform3iv(int location, int count, IntBuffer v) {

		GL20.glUniform3iv(location, count, v);
	}

	@Override
	public void glUniform3iv(int location, int count, int[] v, int offset) {

		GL20.glUniform3iv(location, count, v, offset);
	}

	@Override
	public void glUniform4f(int location, float x, float y, float z, float w) {

		GL20.glUniform4f(location, x, y, z, w);
	}

	@Override
	public void glUniform4fv(int location, int count, FloatBuffer v) {

		GL20.glUniform4fv(location, count, v);
	}

	@Override
	public void glUniform4fv(int location, int count, float[] v, int offset) {

		GL20.glUniform4fv(location, count, v, offset);
	}

	@Override
	public void glUniform4i(int location, int x, int y, int z, int w) {

		GL20.glUniform4i(location, x, y, z, w);
	}

	@Override
	public void glUniform4iv(int location, int count, IntBuffer v) {

		GL20.glUniform4iv(location, count, v);
	}

	@Override
	public void glUniform4iv(int location, int count, int[] v, int offset) {

		GL20.glUniform4iv(location, count, v, offset);
	}

	@Override
	public void glUniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer value) {

		GL20.glUniformMatrix2fv(location, count, transpose, value);
	}

	@Override
	public void glUniformMatrix2fv(int location, int count, boolean transpose, float[] value, int offset) {

		GL20.glUniformMatrix2fv(location, count, transpose, value, offset);
	}

	@Override
	public void glUniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer value) {

		GL20.glUniformMatrix3fv(location, count, transpose, value);
	}

	@Override
	public void glUniformMatrix3fv(int location, int count, boolean transpose, float[] value, int offset) {

		GL20.glUniformMatrix3fv(location, count, transpose, value, offset);
	}

	@Override
	public void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer value) {

		GL20.glUniformMatrix4fv(location, count, transpose, value);
	}

	@Override
	public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int offset) {

		GL20.glUniformMatrix4fv(location, count, transpose, value, offset);
	}

	@Override
	public void glUseProgram(int program) {

		GL20.glUseProgram(program);
	}

	@Override
	public void glValidateProgram(int program) {

		GL20.glValidateProgram(program);
	}

	@Override
	public void glVertexAttrib1f(int indx, float x) {

		GL20.glVertexAttrib1f(indx, x);
	}

	@Override
	public void glVertexAttrib1fv(int indx, FloatBuffer values) {

		GL20.glVertexAttrib1fv(indx, values);
	}

	@Override
	public void glVertexAttrib2f(int indx, float x, float y) {

		GL20.glVertexAttrib2f(indx, x, y);
	}

	@Override
	public void glVertexAttrib2fv(int indx, FloatBuffer values) {

		GL20.glVertexAttrib2fv(indx, values);
	}

	@Override
	public void glVertexAttrib3f(int indx, float x, float y, float z) {

		GL20.glVertexAttrib3f(indx, x, y, z);
	}

	@Override
	public void glVertexAttrib3fv(int indx, FloatBuffer values) {

		GL20.glVertexAttrib3fv(indx, values);
	}

	@Override
	public void glVertexAttrib4f(int indx, float x, float y, float z, float w) {

		GL20.glVertexAttrib4f(indx, x, y, z, w);
	}

	@Override
	public void glVertexAttrib4fv(int indx, FloatBuffer values) {

		GL20.glVertexAttrib4fv(indx, values);
	}

	@Override
	public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, Buffer ptr) {

		GL20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
	}

	@Override
	public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int ptr) {

		GL20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
	}

	@Override
	public void glDrawRangeElements(int mode, int start, int end, int count, int type, java.nio.Buffer indices) {

		GL30.glDrawRangeElements(mode, start, end, count, type, indices);
	}

	@Override
	public void glDrawRangeElements(int mode, int start, int end, int count, int type, int offset) {

		GL30.glDrawRangeElements(mode, start, end, count, type, offset);
	}

	@Override
	public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
			int format, int type, java.nio.Buffer pixels) {

		if (pixels == null)
			GL30.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, 0);
		else
			GL30.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
	}

	@Override
	public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
			int format, int type, int offset) {

		GL30.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, offset);
	}

	@Override
	public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
			int depth, int format, int type, java.nio.Buffer pixels) {

		GL30.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
	}

	@Override
	public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
			int depth, int format, int type, int offset) {

		GL30.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, offset);
	}

	@Override
	public void glCopyTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int x, int y,
			int width, int height) {

		GL30.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height);
	}

	@Override
	public void glCompressedTexImage3D(int target, int level, int internalformat, int width, int height, int depth,
			int border, int imageSize, java.nio.Buffer data) {

		GL30.glCompressedTexImage3D(target, level, internalformat, width, height, depth, border, imageSize, data);
	}

	@Override
	public void glCompressedTexImage3D(int target, int level, int internalformat, int width, int height, int depth,
			int border, int imageSize, int offset) {

		GL30.glCompressedTexImage3D(target, level, internalformat, width, height, depth, border, imageSize, offset);
	}

	@Override
	public void glCompressedTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width,
			int height, int depth, int format, int imageSize, java.nio.Buffer data) {

		GL30.glCompressedTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format,
				imageSize, data);
	}

	@Override
	public void glCompressedTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width,
			int height, int depth, int format, int imageSize, int offset) {

		GL30.glCompressedTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format,
				imageSize, offset);
	}

	@Override
	public void glGenQueries(int n, int[] ids, int offset) {

		GL30.glGenQueries(n, ids, offset);
	}

	@Override
	public void glGenQueries(int n, java.nio.IntBuffer ids) {

		GL30.glGenQueries(n, ids);
	}

	@Override
	public void glDeleteQueries(int n, int[] ids, int offset) {

		GL30.glDeleteQueries(n, ids, offset);
	}

	@Override
	public void glDeleteQueries(int n, java.nio.IntBuffer ids) {

		GL30.glDeleteQueries(n, ids);
	}

	@Override
	public boolean glIsQuery(int id) {

		return GL30.glIsQuery(id);
	}

	@Override
	public void glBeginQuery(int target, int id) {

		GL30.glBeginQuery(target, id);
	}

	@Override
	public void glEndQuery(int target) {

		GL30.glEndQuery(target);
	}

	@Override
	public void glGetQueryiv(int target, int pname, int[] params, int offset) {

		GL30.glGetQueryiv(target, pname, params, offset);
	}

	@Override
	public void glGetQueryiv(int target, int pname, java.nio.IntBuffer params) {

		GL30.glGetQueryiv(target, pname, params);
	}

	@Override
	public void glGetQueryObjectuiv(int id, int pname, int[] params, int offset) {

		GL30.glGetQueryObjectuiv(id, pname, params, offset);
	}

	@Override
	public void glGetQueryObjectuiv(int id, int pname, java.nio.IntBuffer params) {

		GL30.glGetQueryObjectuiv(id, pname, params);
	}

	@Override
	public boolean glUnmapBuffer(int target) {

		return GL30.glUnmapBuffer(target);
	}

	@Override
	public java.nio.Buffer glGetBufferPointerv(int target, int pname) {

		return GL30.glGetBufferPointerv(target, pname);
	}

	@Override
	public void glDrawBuffers(int n, int[] bufs, int offset) {

		GL30.glDrawBuffers(n, bufs, offset);
	}

	@Override
	public void glDrawBuffers(int n, java.nio.IntBuffer bufs) {

		GL30.glDrawBuffers(n, bufs);
	}

	@Override
	public void glReadBuffer(int func) {

		GL30.glReadBuffer(func);
	}

	@Override
	public void glUniformMatrix2x3fv(int location, int count, boolean transpose, float[] value, int offset) {

		GL30.glUniformMatrix2x3fv(location, count, transpose, value, offset);
	}

	@Override
	public void glUniformMatrix2x3fv(int location, int count, boolean transpose, java.nio.FloatBuffer value) {

		GL30.glUniformMatrix2x3fv(location, count, transpose, value);
	}

	@Override
	public void glUniformMatrix3x2fv(int location, int count, boolean transpose, float[] value, int offset) {

		GL30.glUniformMatrix3x2fv(location, count, transpose, value, offset);
	}

	@Override
	public void glUniformMatrix3x2fv(int location, int count, boolean transpose, java.nio.FloatBuffer value) {

		GL30.glUniformMatrix3x2fv(location, count, transpose, value);
	}

	@Override
	public void glUniformMatrix2x4fv(int location, int count, boolean transpose, float[] value, int offset) {

		GL30.glUniformMatrix2x4fv(location, count, transpose, value, offset);
	}

	@Override
	public void glUniformMatrix2x4fv(int location, int count, boolean transpose, java.nio.FloatBuffer value) {

		GL30.glUniformMatrix2x4fv(location, count, transpose, value);
	}

	@Override
	public void glUniformMatrix4x2fv(int location, int count, boolean transpose, float[] value, int offset) {

		GL30.glUniformMatrix4x2fv(location, count, transpose, value, offset);
	}

	@Override
	public void glUniformMatrix4x2fv(int location, int count, boolean transpose, java.nio.FloatBuffer value) {

		GL30.glUniformMatrix4x2fv(location, count, transpose, value);
	}

	@Override
	public void glUniformMatrix3x4fv(int location, int count, boolean transpose, float[] value, int offset) {

		GL30.glUniformMatrix3x4fv(location, count, transpose, value, offset);
	}

	@Override
	public void glUniformMatrix3x4fv(int location, int count, boolean transpose, java.nio.FloatBuffer value) {

		GL30.glUniformMatrix3x4fv(location, count, transpose, value);
	}

	@Override
	public void glUniformMatrix4x3fv(int location, int count, boolean transpose, float[] value, int offset) {

		GL30.glUniformMatrix4x3fv(location, count, transpose, value, offset);
	}

	@Override
	public void glUniformMatrix4x3fv(int location, int count, boolean transpose, java.nio.FloatBuffer value) {

		GL30.glUniformMatrix4x3fv(location, count, transpose, value);
	}

	@Override
	public void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1,
			int dstY1, int mask, int filter) {

		GL30.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
	}

	@Override
	public void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width, int height) {

		GL30.glRenderbufferStorageMultisample(target, samples, internalformat, width, height);
	}

	@Override
	public void glFramebufferTextureLayer(int target, int attachment, int texture, int level, int layer) {

		GL30.glFramebufferTextureLayer(target, attachment, texture, level, layer);
	}

	@Override
	public java.nio.Buffer glMapBufferRange(int target, int offset, int length, int access) {
		return GL30.glMapBufferRange(target, offset, length, access);
	}

	@Override
	public void glFlushMappedBufferRange(int target, int offset, int length) {

		GL30.glFlushMappedBufferRange(target, offset, length);
	}

	@Override
	public void glBindVertexArray(int array) {

		GL30.glBindVertexArray(array);
	}

	@Override
	public void glDeleteVertexArray(int h) {

		ints[0] = h;
		GL30.glDeleteVertexArrays(1, ints, 0);
	}

	@Override
	public int glGenVertexArray() {

		GL30.glGenVertexArrays(1, ints, 0);
		return ints[0];
	}

	@Override
	public boolean glIsVertexArray(int array) {

		return GL30.glIsVertexArray(array);
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
	public void glBeginTransformFeedback(int primitiveMode) {

		GL30.glBeginTransformFeedback(primitiveMode);
	}

	@Override
	public void glEndTransformFeedback() {

		GL30.glEndTransformFeedback();
	}

	@Override
	public void glBindBufferRange(int target, int index, int buffer, int offset, int size) {

		GL30.glBindBufferRange(target, index, buffer, offset, size);
	}

	@Override
	public void glBindBufferBase(int target, int index, int buffer) {

		GL30.glBindBufferBase(target, index, buffer);
	}

	@Override
	public void glTransformFeedbackVaryings(int program, String[] varyings, int bufferMode) {

		GL30.glTransformFeedbackVaryings(program, varyings, bufferMode);
	}

	@Override
	public void glGetTransformFeedbackVarying(int program, int index, int bufsize, int[] length, int lengthOffset,
			int[] size, int sizeOffset, int[] type, int typeOffset, byte[] name, int nameOffset) {

		GL30.glGetTransformFeedbackVarying(program, index, bufsize, length, lengthOffset, size, sizeOffset, type,
				typeOffset, name, nameOffset);
	}

	@Override
	public String glGetTransformFeedbackVarying(int program, int index, int[] size, int sizeOffset, int[] type,
			int typeOffset) {

		return GL30.glGetTransformFeedbackVarying(program, index, size, sizeOffset, type, typeOffset);
	}

	@Override
	public String glGetTransformFeedbackVarying(int program, int index, java.nio.IntBuffer size,
			java.nio.IntBuffer type) {

		return GL30.glGetTransformFeedbackVarying(program, index, size, type);
	}

	@Override
	public void glVertexAttribIPointer(int index, int size, int type, int stride, int offset) {

		GL30.glVertexAttribIPointer(index, size, type, stride, offset);
	}

	@Override
	public void glGetVertexAttribIiv(int index, int pname, int[] params, int offset) {

		GL30.glGetVertexAttribIiv(index, pname, params, offset);
	}

	@Override
	public void glGetVertexAttribIiv(int index, int pname, java.nio.IntBuffer params) {

		GL30.glGetVertexAttribIiv(index, pname, params);
	}

	@Override
	public void glGetVertexAttribIuiv(int index, int pname, int[] params, int offset) {

		GL30.glGetVertexAttribIuiv(index, pname, params, offset);
	}

	@Override
	public void glGetVertexAttribIuiv(int index, int pname, java.nio.IntBuffer params) {

		GL30.glGetVertexAttribIuiv(index, pname, params);
	}

	@Override
	public void glVertexAttribI4i(int index, int x, int y, int z, int w) {

		GL30.glVertexAttribI4i(index, x, y, z, w);
	}

	@Override
	public void glVertexAttribI4ui(int index, int x, int y, int z, int w) {

		GL30.glVertexAttribI4ui(index, x, y, z, w);
	}

	@Override
	public void glVertexAttribI4iv(int index, int[] v, int offset) {

		GL30.glVertexAttribI4iv(index, v, offset);
	}

	@Override
	public void glVertexAttribI4iv(int index, java.nio.IntBuffer v) {

		GL30.glVertexAttribI4iv(index, v);
	}

	@Override
	public void glVertexAttribI4uiv(int index, int[] v, int offset) {

		GL30.glVertexAttribI4uiv(index, v, offset);
	}

	@Override
	public void glVertexAttribI4uiv(int index, java.nio.IntBuffer v) {

		GL30.glVertexAttribI4uiv(index, v);
	}

	@Override
	public void glGetUniformuiv(int program, int location, int[] params, int offset) {

		GL30.glGetUniformuiv(program, location, params, offset);
	}

	@Override
	public void glGetUniformuiv(int program, int location, java.nio.IntBuffer params) {

		GL30.glGetUniformuiv(program, location, params);
	}

	@Override
	public int glGetFragDataLocation(int program, String name) {

		return GL30.glGetFragDataLocation(program, name);
	}

	@Override
	public void glUniform1ui(int location, int v0) {

		GL30.glUniform1ui(location, v0);
	}

	@Override
	public void glUniform2ui(int location, int v0, int v1) {

		GL30.glUniform2ui(location, v0, v1);
	}

	@Override
	public void glUniform3ui(int location, int v0, int v1, int v2) {

		GL30.glUniform3ui(location, v0, v1, v2);
	}

	@Override
	public void glUniform4ui(int location, int v0, int v1, int v2, int v3) {

		GL30.glUniform4ui(location, v0, v1, v2, v3);
	}

	@Override
	public void glUniform1uiv(int location, int count, int[] value, int offset) {

		GL30.glUniform1uiv(location, count, value, offset);
	}

	@Override
	public void glUniform1uiv(int location, int count, java.nio.IntBuffer value) {

		GL30.glUniform1uiv(location, count, value);
	}

	@Override
	public void glUniform2uiv(int location, int count, int[] value, int offset) {

		GL30.glUniform2uiv(location, count, value, offset);
	}

	@Override
	public void glUniform2uiv(int location, int count, java.nio.IntBuffer value) {

		GL30.glUniform2uiv(location, count, value);
	}

	@Override
	public void glUniform3uiv(int location, int count, int[] value, int offset) {

		GL30.glUniform3uiv(location, count, value, offset);
	}

	@Override
	public void glUniform3uiv(int location, int count, java.nio.IntBuffer value) {

		GL30.glUniform3uiv(location, count, value);
	}

	@Override
	public void glUniform4uiv(int location, int count, int[] value, int offset) {

		GL30.glUniform4uiv(location, count, value, offset);
	}

	@Override
	public void glUniform4uiv(int location, int count, java.nio.IntBuffer value) {

		GL30.glUniform4uiv(location, count, value);
	}

	@Override
	public void glClearBufferiv(int buffer, int drawbuffer, int[] value, int offset) {

		GL30.glClearBufferiv(buffer, drawbuffer, value, offset);
	}

	@Override
	public void glClearBufferiv(int buffer, int drawbuffer, java.nio.IntBuffer value) {

		GL30.glClearBufferiv(buffer, drawbuffer, value);
	}

	@Override
	public void glClearBufferuiv(int buffer, int drawbuffer, int[] value, int offset) {

		GL30.glClearBufferuiv(buffer, drawbuffer, value, offset);
	}

	@Override
	public void glClearBufferuiv(int buffer, int drawbuffer, java.nio.IntBuffer value) {

		GL30.glClearBufferuiv(buffer, drawbuffer, value);
	}

	@Override
	public void glClearBufferfv(int buffer, int drawbuffer, float[] value, int offset) {

		GL30.glClearBufferfv(buffer, drawbuffer, value, offset);
	}

	@Override
	public void glClearBufferfv(int buffer, int drawbuffer, java.nio.FloatBuffer value) {

		GL30.glClearBufferfv(buffer, drawbuffer, value);
	}

	@Override
	public void glClearBufferfi(int buffer, int drawbuffer, float depth, int stencil) {

		GL30.glClearBufferfi(buffer, drawbuffer, depth, stencil);
	}

	@Override
	public String glGetStringi(int name, int index) {

		return GL30.glGetStringi(name, index);
	}

	@Override
	public void glCopyBufferSubData(int readTarget, int writeTarget, int readOffset, int writeOffset, int size) {

		GL30.glCopyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size);
	}

	@Override
	public void glGetUniformIndices(int program, String[] uniformNames, int[] uniformIndices,
			int uniformIndicesOffset) {

		GL30.glGetUniformIndices(program, uniformNames, uniformIndices, uniformIndicesOffset);
	}

	@Override
	public void glGetUniformIndices(int program, String[] uniformNames, java.nio.IntBuffer uniformIndices) {

		GL30.glGetUniformIndices(program, uniformNames, uniformIndices);
	}

	@Override
	public void glGetActiveUniformsiv(int program, int uniformCount, int[] uniformIndices, int uniformIndicesOffset,
			int pname, int[] params, int paramsOffset) {

		GL30.glGetActiveUniformsiv(program, uniformCount, uniformIndices, uniformIndicesOffset, pname, params,
				paramsOffset);
	}

	@Override
	public void glGetActiveUniformsiv(int program, int uniformCount, java.nio.IntBuffer uniformIndices, int pname,
			java.nio.IntBuffer params) {

		GL30.glGetActiveUniformsiv(program, uniformCount, uniformIndices, pname, params);
	}

	@Override
	public int glGetUniformBlockIndex(int program, String uniformBlockName) {

		return GL30.glGetUniformBlockIndex(program, uniformBlockName);
	}

	@Override
	public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, int[] params, int offset) {

		GL30.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, params, offset);
	}

	@Override
	public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, java.nio.IntBuffer params) {

		GL30.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, params);
	}

	@Override
	public void glGetActiveUniformBlockName(int program, int uniformBlockIndex, int bufSize, int[] length,
			int lengthOffset, byte[] uniformBlockName, int uniformBlockNameOffset) {

		GL30.glGetActiveUniformBlockName(program, uniformBlockIndex, bufSize, length, lengthOffset, uniformBlockName,
				uniformBlockNameOffset);
	}

	@Override
	public void glGetActiveUniformBlockName(int program, int uniformBlockIndex, java.nio.Buffer length,
			java.nio.Buffer uniformBlockName) {

		GL30.glGetActiveUniformBlockName(program, uniformBlockIndex, length, uniformBlockName);
	}

	@Override
	public String glGetActiveUniformBlockName(int program, int uniformBlockIndex) {

		return GL30.glGetActiveUniformBlockName(program, uniformBlockIndex);
	}

	@Override
	public void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding) {

		GL30.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
	}

	@Override
	public void glDrawArraysInstanced(int mode, int first, int count, int instanceCount) {

		GL30.glDrawArraysInstanced(mode, first, count, instanceCount);
	}

	@Override
	public void glDrawElementsInstanced(int mode, int count, int type, java.nio.Buffer indices, int instanceCount) {

		GL30.glDrawElementsInstanced(mode, count, type, indices, instanceCount);
	}

	@Override
	public void glDrawElementsInstanced(int mode, int count, int type, int indicesOffset, int instanceCount) {

		GL30.glDrawElementsInstanced(mode, count, type, indicesOffset, instanceCount);
	}

	@Override
	public long glFenceSync(int condition, int flags) {

		return GL30.glFenceSync(condition, flags);
	}

	@Override
	public boolean glIsSync(long sync) {

		return GL30.glIsSync(sync);
	}

	@Override
	public void glDeleteSync(long sync) {

		GL30.glDeleteSync(sync);
	}

	@Override
	public int glClientWaitSync(long sync, int flags, long timeout) {

		return GL30.glClientWaitSync(sync, flags, timeout);
	}

	@Override
	public void glWaitSync(long sync, int flags, long timeout) {

		GL30.glWaitSync(sync, flags, timeout);
	}

	@Override
	public void glGetInteger64v(int pname, long[] params, int offset) {

		GL30.glGetInteger64v(pname, params, offset);
	}

	@Override
	public void glGetInteger64v(int pname, java.nio.LongBuffer params) {

		GL30.glGetInteger64v(pname, params);
	}

	@Override
	public void glGetSynciv(long sync, int pname, int bufSize, int[] length, int lengthOffset, int[] values,
			int valuesOffset) {

		GL30.glGetSynciv(sync, pname, bufSize, length, lengthOffset, values, valuesOffset);
	}

	@Override
	public void glGetSynciv(long sync, int pname, int bufSize, java.nio.IntBuffer length, java.nio.IntBuffer values) {

		GL30.glGetSynciv(sync, pname, bufSize, length, values);
	}

	@Override
	public void glGetInteger64i_v(int target, int index, long[] data, int offset) {

		GL30.glGetInteger64i_v(target, index, data, offset);
	}

	@Override
	public void glGetInteger64i_v(int target, int index, java.nio.LongBuffer data) {

		GL30.glGetInteger64i_v(target, index, data);
	}

	@Override
	public void glGetBufferParameteri64v(int target, int pname, long[] params, int offset) {

		GL30.glGetBufferParameteri64v(target, pname, params, offset);
	}

	@Override
	public void glGetBufferParameteri64v(int target, int pname, java.nio.LongBuffer params) {

		GL30.glGetBufferParameteri64v(target, pname, params);
	}

	@Override
	public void glGenSamplers(int count, int[] samplers, int offset) {

		GL30.glGenSamplers(count, samplers, offset);
	}

	@Override
	public void glGenSamplers(int count, java.nio.IntBuffer samplers) {

		GL30.glGenSamplers(count, samplers);
	}

	@Override
	public void glDeleteSamplers(int count, int[] samplers, int offset) {

		GL30.glDeleteSamplers(count, samplers, offset);
	}

	@Override
	public void glDeleteSamplers(int count, java.nio.IntBuffer samplers) {

		GL30.glDeleteSamplers(count, samplers);
	}

	@Override
	public boolean glIsSampler(int sampler) {

		return GL30.glIsSampler(sampler);
	}

	@Override
	public void glBindSampler(int unit, int sampler) {

		GL30.glBindSampler(unit, sampler);
	}

	@Override
	public void glSamplerParameteri(int sampler, int pname, int param) {

		GL30.glSamplerParameteri(sampler, pname, param);
	}

	@Override
	public void glSamplerParameteriv(int sampler, int pname, int[] param, int offset) {

		GL30.glSamplerParameteriv(sampler, pname, param, offset);
	}

	@Override
	public void glSamplerParameteriv(int sampler, int pname, java.nio.IntBuffer param) {

		GL30.glSamplerParameteriv(sampler, pname, param);
	}

	@Override
	public void glSamplerParameterf(int sampler, int pname, float param) {

		GL30.glSamplerParameterf(sampler, pname, param);
	}

	@Override
	public void glSamplerParameterfv(int sampler, int pname, float[] param, int offset) {

		GL30.glSamplerParameterfv(sampler, pname, param, offset);
	}

	@Override
	public void glSamplerParameterfv(int sampler, int pname, java.nio.FloatBuffer param) {

		GL30.glSamplerParameterfv(sampler, pname, param);
	}

	@Override
	public void glGetSamplerParameteriv(int sampler, int pname, int[] params, int offset) {

		GL30.glGetSamplerParameteriv(sampler, pname, params, offset);
	}

	@Override
	public void glGetSamplerParameteriv(int sampler, int pname, java.nio.IntBuffer params) {

		GL30.glGetSamplerParameteriv(sampler, pname, params);
	}

	@Override
	public void glGetSamplerParameterfv(int sampler, int pname, float[] params, int offset) {

		GL30.glGetSamplerParameterfv(sampler, pname, params, offset);
	}

	@Override
	public void glGetSamplerParameterfv(int sampler, int pname, java.nio.FloatBuffer params) {

		GL30.glGetSamplerParameterfv(sampler, pname, params);
	}

	@Override
	public void glVertexAttribDivisor(int index, int divisor) {

		GL30.glVertexAttribDivisor(index, divisor);
	}

	@Override
	public void glBindTransformFeedback(int target, int id) {

		GL30.glBindTransformFeedback(target, id);
	}

	@Override
	public void glDeleteTransformFeedbacks(int n, int[] ids, int offset) {

		GL30.glDeleteTransformFeedbacks(n, ids, offset);
	}

	@Override
	public void glDeleteTransformFeedbacks(int n, java.nio.IntBuffer ids) {

		GL30.glDeleteTransformFeedbacks(n, ids);
	}

	@Override
	public void glGenTransformFeedbacks(int n, int[] ids, int offset) {

		GL30.glGenTransformFeedbacks(n, ids, offset);
	}

	@Override
	public void glGenTransformFeedbacks(int n, java.nio.IntBuffer ids) {

		GL30.glGenTransformFeedbacks(n, ids);
	}

	@Override
	public boolean glIsTransformFeedback(int id) {

		return GL30.glIsTransformFeedback(id);
	}

	@Override
	public void glPauseTransformFeedback() {

		GL30.glPauseTransformFeedback();
	}

	@Override
	public void glResumeTransformFeedback() {

		GL30.glResumeTransformFeedback();
	}

	@Override
	public void glGetProgramBinary(int program, int bufSize, int[] length, int lengthOffset, int[] binaryFormat,
			int binaryFormatOffset, java.nio.Buffer binary) {

		GL30.glGetProgramBinary(program, bufSize, length, lengthOffset, binaryFormat, binaryFormatOffset, binary);
	}

	@Override
	public void glGetProgramBinary(int program, int bufSize, java.nio.IntBuffer length, java.nio.IntBuffer binaryFormat,
			java.nio.Buffer binary) {

		GL30.glGetProgramBinary(program, bufSize, length, binaryFormat, binary);
	}

	@Override
	public void glProgramBinary(int program, int binaryFormat, java.nio.Buffer binary, int length) {

		GL30.glProgramBinary(program, binaryFormat, binary, length);
	}

	@Override
	public void glProgramParameteri(int program, int pname, int value) {

		GL30.glProgramParameteri(program, pname, value);
	}

	@Override
	public void glInvalidateFramebuffer(int target, int numAttachments, int[] attachments, int offset) {

		GL30.glInvalidateFramebuffer(target, numAttachments, attachments, offset);
	}

	@Override
	public void glInvalidateFramebuffer(int target, int numAttachments, java.nio.IntBuffer attachments) {

		GL30.glInvalidateFramebuffer(target, numAttachments, attachments);
	}

	@Override
	public void glInvalidateSubFramebuffer(int target, int numAttachments, int[] attachments, int offset, int x, int y,
			int width, int height) {

		GL30.glInvalidateSubFramebuffer(target, numAttachments, attachments, offset, x, y, width, height);
	}

	@Override
	public void glInvalidateSubFramebuffer(int target, int numAttachments, java.nio.IntBuffer attachments, int x, int y,
			int width, int height) {

		GL30.glInvalidateSubFramebuffer(target, numAttachments, attachments, x, y, width, height);
	}

	@Override
	public void glTexStorage2D(int target, int levels, int internalformat, int width, int height) {

		GL30.glTexStorage2D(target, levels, internalformat, width, height);
	}

	@Override
	public void glTexStorage3D(int target, int levels, int internalformat, int width, int height, int depth) {

		GL30.glTexStorage3D(target, levels, internalformat, width, height, depth);
	}

	@Override
	public void glGetInternalformativ(int target, int internalformat, int pname, int bufSize, int[] params,
			int offset) {

		GL30.glGetInternalformativ(target, internalformat, pname, bufSize, params, offset);
	}

	@Override
	public void glGetInternalformativ(int target, int internalformat, int pname, int bufSize,
			java.nio.IntBuffer params) {

		GL30.glGetInternalformativ(target, internalformat, pname, bufSize, params);
	}

	@Override
	public boolean glIsFramebuffer(int handler) {

		return GL20.glIsFramebuffer(handler);
	}

	@Override
	public boolean glIsTexture(int handler) {

		return GL20.glIsTexture(handler);
	}

	// extra function
	@Override
	public void glClearColorMask(int mask, float red, float green, float blue, float alpha) {

		GL20.glClearColor(red, green, blue, alpha);
		GL20.glClear(mask);
	}

	@Override
	public void glViewport(int x, int y, int width, int height) {
		GL20.glViewport(x, y, width, height);
	}

	@Override
	public boolean supportsExtension(String extension) {
		if (extensions == null)
			extensions = GL20.glGetString(GL20.GL_EXTENSIONS);
		return extensions.contains(extension);
	}

	@Override
	public boolean supportsRenderer(String renderer) {
		if (renderers == null)
			renderers = GL20.glGetString(GL20.GL_RENDERER);
		return renderers.contains(renderer);
	}

	public String glVersion() {
		return GL20.glGetString(GL20.GL_RENDERER);
	}
	@Override
	public boolean limitGLESContext() {
		if (renderers == null) {
			renderers = GL20.glGetString(GL20.GL_RENDERER);
			limitGlesContext = renderers.startsWith("Adreno");
		}
		return limitGlesContext;
	}

	@Override
	public float getMaxAnisotropicFilterLevel() {
		if (maxAnisotropicFilterLevel == 0) {
			if (supportsExtension("GL_EXT_texture_filter_anisotropic")) {
				final FloatBuffer buffer = BufferUtils.newFloatBuffer(1);
				buffer.position(0).limit(buffer.capacity());
				GL20.glGetFloatv(0x84FF, buffer);
				maxAnisotropicFilterLevel = buffer.get(0);
			} else
				maxAnisotropicFilterLevel = 1;
		}
		return maxAnisotropicFilterLevel;
	}

	protected int maxTextureSize;

	@Override
	public int getMaxTextureSize() {
		if (maxTextureSize < 64) {
			GL20.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, ints, 0);
			maxTextureSize = ints[0];
		}
		return maxTextureSize;
	}

	protected int maxTextureUnit;

	@Override
	public int getMaxTextureUnit() {

		if (maxTextureUnit <= 0) {
			GL20.glGetIntegerv(GL20.GL_MAX_TEXTURE_IMAGE_UNITS, ints, 0);
			maxTextureUnit = ints[0];
		}
		return maxTextureUnit;
	}

	Array<int[]> shaderPrograms = new Array<int[]>();

	@Override
	public int[] compileShaderProgram(String source, String prefix) {

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
			Log.e(TAG, "Shader program compiling, " + e);
		}
		return handlers;
	}

	@Override
	public boolean validShaderProgram(int[] handlers) {

		if (shaderPrograms.contains(handlers, false))
			return true;
		shaderPrograms.removeValue(handlers, false);
		Arrays.fill(handlers, -1);
		return false;
	}

	@Override
	public void destroyShaderProgram(int[] handlers) {

		try {
			if (handlers[0] == -1)
				throw new RuntimeException("Handlers not initialize");
			GL20.glUseProgram(0);
			GL20.glDeleteProgram(handlers[0]);
			GL20.glDeleteShader(handlers[1]);
			GL20.glDeleteShader(handlers[2]);
			shaderPrograms.removeValue(handlers, true);
			Arrays.fill(handlers, -1);
		} catch (RuntimeException e) {

			Log.e(TAG, "shader program delete \n" + e);
		}
		Arrays.fill(handlers, -1);
	}

	Array<int[]> meshes = new Array<int[]>();

	@Override
	public int[] genMesh(final Buffer v_data, final boolean v_static, final Buffer i_data, final boolean i_static) {

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
			GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, handlers[1]);
			GL20.glBufferData(GL20.GL_ARRAY_BUFFER, v_data.capacity(), null,
					v_static ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW);
			v_data.limit(v_data.capacity());
			GL20.glBufferSubData(GL20.GL_ARRAY_BUFFER, 0, v_data.limit(), v_data);
			GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
			// binding indices
			GL20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, handlers[2]);
			GL20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, i_data.capacity(), null,
					i_static ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW);
			i_data.limit(i_data.capacity());
			GL20.glBufferSubData(GL20.GL_ELEMENT_ARRAY_BUFFER, 0, i_data.limit(), i_data);
			GL20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
			// unbind vao
			GL30.glBindVertexArray(0);
			meshes.add(handlers);
		} catch (RuntimeException e) {
			Arrays.fill(handlers, -1);
			Log.e(TAG, "failed generate mesh data \n" + e);
		}
		return handlers;
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
	public boolean capabilitySwitch(final boolean enable, final int cap) {
		if (enabledCaps.contains(cap) != enable) {
			if (enable) {
				GL20.glEnable(cap);
				enabledCaps.add(cap);
			} else {
				GL20.glDisable(cap);
				enabledCaps.removeValue(cap);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean setDepthMask(final boolean depthMask) {
		if (this.depthMask != depthMask) {
			GL20.glDepthMask(depthMask);
			return true;
		}
		return false;
	}

	@Override
	public boolean setDepthTest(final int depthFunction, final float depthRangeNear, final float depthRangeFar) {
		final boolean enable = depthFunction != 0;
		boolean changed = capabilitySwitch(enable, GL20.GL_DEPTH_TEST);
		if (enable && (depthFunc != depthFunction || this.depthRangeNear != depthRangeNear
				|| this.depthRangeFar != depthRangeFar)) {
			if (depthFunc != depthFunction)
				GL20.glDepthFunc(depthFunc = depthFunction);
			if (this.depthRangeNear != depthRangeNear || this.depthRangeFar != depthRangeFar)
				GL20.glDepthRangef(this.depthRangeNear = depthRangeNear, this.depthRangeFar = depthRangeFar);
			changed |= true;
		}
		return changed;
	}

	@Override
	public boolean setBlending(final boolean enabled, final int sFactor, final int dFactor) {
		boolean change = capabilitySwitch(enabled, GL20.GL_BLEND);
		if (enabled && (this.blendSFactor != sFactor || this.blendDFactor != dFactor)) {
			GL20.glBlendFunc(sFactor, dFactor);
			this.blendSFactor = sFactor;
			this.blendDFactor = dFactor;
			change |= true;
		}
		return change;
	}

	@Override
	public void reset() {
		shaderPrograms.clear();
		meshes.clear();
		// reset chaced value cause validate
		enabledCaps.clear();
		this.depthMask = false;
		this.depthFunc = GL20.GL_LEQUAL;
		this.depthRangeNear = 0;
		this.depthRangeFar = 1;
		this.blendSFactor = GL20.GL_ONE;
		this.blendDFactor = GL20.GL_ZERO;
		this.cullFace = GL20.GL_BACK;
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
	}

}
