package com.ariasaproject.advancerofrpg;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import com.ariasaproject.advancerofrpg.graphics.TGF;

public class LWJGLTGF implements TGF {

	@Override
	public void glActiveTexture(int unit) {
		
	}

	@Override
	public void glBindAttribLocation(int program, int index, String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glBindBuffer(int target, int buffer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glBindFramebuffer(int target, int framebuffer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glBindRenderbuffer(int target, int renderbuffer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glBindTexture(int target, int texture) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glBlendColor(float red, float green, float blue, float alpha) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glBlendEquation(int mode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glBufferData(int target, int size, Buffer data, int usage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glBufferSubData(int target, int offset, int size, Buffer data) {
		// TODO Auto-generated method stub

	}

	@Override
	public int glCheckFramebufferStatus(int target) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void glClearDepthf(float depth) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glClearStencil(int s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height, int border) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCullFace(int mode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDeleteBuffers(int n, IntBuffer buffers) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDeleteBuffer(int buffer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDeleteFramebuffers(int n, IntBuffer framebuffers) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDeleteFramebuffers(int n, int[] framebuffers, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDeleteFramebuffer(int framebuffer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDeleteProgram(int program) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDeleteRenderbuffers(int n, IntBuffer renderbuffers) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDeleteRenderbuffer(int renderbuffer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDeleteTexture(int texture) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDepthFunc(int func) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDepthRangef(float near, float far) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDetachShader(int program, int shader) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDisableVertexAttribArray(int index) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawArrays(int mode, int first, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawElements(int mode, int count, int type, Buffer indices) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawElements(int mode, int count, int type, int indices) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glEnableVertexAttribArray(int index) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glFinish() {
		// TODO Auto-generated method stub

	}

	@Override
	public void glFlush() {
		// TODO Auto-generated method stub

	}

	@Override
	public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glFrontFace(int mode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGenBuffers(int n, IntBuffer buffers) {
		// TODO Auto-generated method stub

	}

	@Override
	public int glGenBuffer() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void glGenerateMipmap(int target) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGenFramebuffers(int n, IntBuffer framebuffers) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGenFramebuffers(int n, int[] framebuffers, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public int glGenFramebuffer() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void glGenRenderbuffers(int n, IntBuffer renderbuffers) {
		// TODO Auto-generated method stub

	}

	@Override
	public int glGenRenderbuffer() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int glGenTexture() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String glGetActiveAttrib(int program, int index, IntBuffer size, Buffer type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String glGetActiveUniform(int program, int index, IntBuffer size, Buffer type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void glGetAttachedShaders(int program, int maxcount, Buffer count, IntBuffer shaders) {
		// TODO Auto-generated method stub

	}

	@Override
	public int glGetAttribLocation(int program, String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void glGetBooleanv(int pname, Buffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public int glGetError() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void glGetFloatv(int pname, FloatBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetIntegerv(int pname, IntBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public int glGetIntegerv(int pname) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void glGetProgramiv(int program, int pname, IntBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetShaderiv(int shader, int pname, IntBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public String glGetShaderInfoLog(int shader) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
		// TODO Auto-generated method stub

	}

	@Override
	public String glGetString(int name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetUniformfv(int program, int location, FloatBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetUniformiv(int program, int location, IntBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public int glGetUniformLocation(int program, String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void glGetVertexAttribfv(int index, int pname, FloatBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetVertexAttribiv(int index, int pname, IntBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glHint(int target, int mode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glLineWidth(float width) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glPixelStorei(int pname, int param) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glPolygonOffset(float factor, float units) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glReleaseShaderCompiler() {
		// TODO Auto-generated method stub

	}

	@Override
	public void glRenderbufferStorage(int target, int internalformat, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glSampleCoverage(float value, boolean invert) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glScissor(int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glShaderBinary(int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glStencilFunc(int func, int ref, int mask) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glStencilFuncSeparate(int face, int func, int ref, int mask) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glStencilMask(int mask) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glStencilMaskSeparate(int face, int mask) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glStencilOp(int fail, int zfail, int zpass) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glStencilOpSeparate(int face, int fail, int zfail, int zpass) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glTexParameterf(int target, int pname, float param) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glTexParameterfv(int target, int pname, FloatBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glTexParameteri(int target, int pname, int param) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glTexParameteriv(int target, int pname, IntBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform1f(int location, float x) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform1fv(int location, int count, FloatBuffer v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform1fv(int location, int count, float[] v, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform1i(int location, int x) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform1iv(int location, int count, IntBuffer v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform1iv(int location, int count, int[] v, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform2f(int location, float x, float y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform2fv(int location, int count, FloatBuffer v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform2fv(int location, int count, float[] v, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform2i(int location, int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform2iv(int location, int count, IntBuffer v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform2iv(int location, int count, int[] v, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform3f(int location, float x, float y, float z) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform3fv(int location, int count, FloatBuffer v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform3fv(int location, int count, float[] v, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform3i(int location, int x, int y, int z) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform3iv(int location, int count, IntBuffer v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform3iv(int location, int count, int[] v, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform4f(int location, float x, float y, float z, float w) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform4fv(int location, int count, FloatBuffer v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform4fv(int location, int count, float[] v, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform4i(int location, int x, int y, int z, int w) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform4iv(int location, int count, IntBuffer v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform4iv(int location, int count, int[] v, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniformMatrix2fv(int location, int count, boolean transpose, float[] value, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniformMatrix3fv(int location, int count, boolean transpose, float[] value, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUseProgram(int program) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glValidateProgram(int program) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glVertexAttrib1f(int indx, float x) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glVertexAttrib1fv(int indx, FloatBuffer values) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glVertexAttrib2f(int indx, float x, float y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glVertexAttrib2fv(int indx, FloatBuffer values) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glVertexAttrib3f(int indx, float x, float y, float z) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glVertexAttrib3fv(int indx, FloatBuffer values) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glVertexAttrib4f(int indx, float x, float y, float z, float w) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glVertexAttrib4fv(int indx, FloatBuffer values) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, Buffer ptr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int ptr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawRangeElements(int mode, int start, int end, int count, int type, Buffer indices) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawRangeElements(int mode, int start, int end, int count, int type, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, Buffer pixels) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, Buffer pixels) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glCopyTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGenQueries(int n, int[] ids, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGenQueries(int n, IntBuffer ids) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDeleteQueries(int n, int[] ids, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDeleteQueries(int n, IntBuffer ids) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean glIsQuery(int id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void glBeginQuery(int target, int id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glEndQuery(int target) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetQueryiv(int target, int pname, int[] params, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetQueryiv(int target, int pname, IntBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetQueryObjectuiv(int id, int pname, int[] params, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetQueryObjectuiv(int id, int pname, IntBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean glUnmapBuffer(int target) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Buffer glGetBufferPointerv(int target, int pname) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void glDrawBuffers(int n, int[] bufs, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawBuffers(int n, IntBuffer bufs) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glReadBuffer(int func) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniformMatrix2x3fv(int location, int count, boolean transpose, float[] value, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniformMatrix2x3fv(int location, int count, boolean transpose, FloatBuffer value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniformMatrix3x2fv(int location, int count, boolean transpose, float[] value, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniformMatrix3x2fv(int location, int count, boolean transpose, FloatBuffer value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniformMatrix2x4fv(int location, int count, boolean transpose, float[] value, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniformMatrix2x4fv(int location, int count, boolean transpose, FloatBuffer value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniformMatrix4x2fv(int location, int count, boolean transpose, float[] value, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniformMatrix4x2fv(int location, int count, boolean transpose, FloatBuffer value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniformMatrix3x4fv(int location, int count, boolean transpose, float[] value, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniformMatrix3x4fv(int location, int count, boolean transpose, FloatBuffer value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniformMatrix4x3fv(int location, int count, boolean transpose, float[] value, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniformMatrix4x3fv(int location, int count, boolean transpose, FloatBuffer value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glFramebufferTextureLayer(int target, int attachment, int texture, int level, int layer) {
		// TODO Auto-generated method stub

	}

	@Override
	public Buffer glMapBufferRange(int target, int offset, int length, int access) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void glFlushMappedBufferRange(int target, int offset, int length) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glBindVertexArray(int array) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDeleteVertexArray(int h) {
		// TODO Auto-generated method stub

	}

	@Override
	public int glGenVertexArray() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean glIsVertexArray(int array) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void glGetIntegeri_v(int target, int index, int[] data, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetIntegeri_v(int target, int index, IntBuffer data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glBeginTransformFeedback(int primitiveMode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glEndTransformFeedback() {
		// TODO Auto-generated method stub

	}

	@Override
	public void glBindBufferRange(int target, int index, int buffer, int offset, int size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glBindBufferBase(int target, int index, int buffer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glTransformFeedbackVaryings(int program, String[] varyings, int bufferMode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetTransformFeedbackVarying(int program, int index, int bufsize, int[] length, int lengthOffset, int[] size, int sizeOffset, int[] type, int typeOffset, byte[] name, int nameOffset) {
		// TODO Auto-generated method stub

	}

	@Override
	public String glGetTransformFeedbackVarying(int program, int index, int[] size, int sizeOffset, int[] type, int typeOffset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String glGetTransformFeedbackVarying(int program, int index, IntBuffer size, IntBuffer type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void glVertexAttribIPointer(int index, int size, int type, int stride, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetVertexAttribIiv(int index, int pname, int[] params, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetVertexAttribIiv(int index, int pname, IntBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetVertexAttribIuiv(int index, int pname, int[] params, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetVertexAttribIuiv(int index, int pname, IntBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glVertexAttribI4i(int index, int x, int y, int z, int w) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glVertexAttribI4ui(int index, int x, int y, int z, int w) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glVertexAttribI4iv(int index, int[] v, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glVertexAttribI4iv(int index, IntBuffer v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glVertexAttribI4uiv(int index, int[] v, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glVertexAttribI4uiv(int index, IntBuffer v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetUniformuiv(int program, int location, int[] params, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetUniformuiv(int program, int location, IntBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public int glGetFragDataLocation(int program, String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void glUniform1ui(int location, int v0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform2ui(int location, int v0, int v1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform3ui(int location, int v0, int v1, int v2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform4ui(int location, int v0, int v1, int v2, int v3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform1uiv(int location, int count, int[] value, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform1uiv(int location, int count, IntBuffer value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform2uiv(int location, int count, int[] value, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform2uiv(int location, int count, IntBuffer value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform3uiv(int location, int count, int[] value, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform3uiv(int location, int count, IntBuffer value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform4uiv(int location, int count, int[] value, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glUniform4uiv(int location, int count, IntBuffer value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glClearBufferiv(int buffer, int drawbuffer, int[] value, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glClearBufferiv(int buffer, int drawbuffer, IntBuffer value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glClearBufferuiv(int buffer, int drawbuffer, int[] value, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glClearBufferuiv(int buffer, int drawbuffer, IntBuffer value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glClearBufferfv(int buffer, int drawbuffer, float[] value, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glClearBufferfv(int buffer, int drawbuffer, FloatBuffer value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glClearBufferfi(int buffer, int drawbuffer, float depth, int stencil) {
		// TODO Auto-generated method stub

	}

	@Override
	public String glGetStringi(int name, int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void glCopyBufferSubData(int readTarget, int writeTarget, int readOffset, int writeOffset, int size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetUniformIndices(int program, String[] uniformNames, int[] uniformIndices, int uniformIndicesOffset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetUniformIndices(int program, String[] uniformNames, IntBuffer uniformIndices) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetActiveUniformsiv(int program, int uniformCount, int[] uniformIndices, int uniformIndicesOffset, int pname, int[] params, int paramsOffset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetActiveUniformsiv(int program, int uniformCount, IntBuffer uniformIndices, int pname, IntBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public int glGetUniformBlockIndex(int program, String uniformBlockName) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, int[] params, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, IntBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetActiveUniformBlockName(int program, int uniformBlockIndex, int bufSize, int[] length, int lengthOffset, byte[] uniformBlockName, int uniformBlockNameOffset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetActiveUniformBlockName(int program, int uniformBlockIndex, Buffer length, Buffer uniformBlockName) {
		// TODO Auto-generated method stub

	}

	@Override
	public String glGetActiveUniformBlockName(int program, int uniformBlockIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawArraysInstanced(int mode, int first, int count, int instanceCount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawElementsInstanced(int mode, int count, int type, Buffer indices, int instanceCount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawElementsInstanced(int mode, int count, int type, int indicesOffset, int instanceCount) {
		// TODO Auto-generated method stub

	}

	@Override
	public long glFenceSync(int condition, int flags) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean glIsSync(long sync) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void glDeleteSync(long sync) {
		// TODO Auto-generated method stub

	}

	@Override
	public int glClientWaitSync(long sync, int flags, long timeout) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void glWaitSync(long sync, int flags, long timeout) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetInteger64v(int pname, long[] params, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetInteger64v(int pname, LongBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetSynciv(long sync, int pname, int bufSize, int[] length, int lengthOffset, int[] values, int valuesOffset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetSynciv(long sync, int pname, int bufSize, IntBuffer length, IntBuffer values) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetInteger64i_v(int target, int index, long[] data, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetInteger64i_v(int target, int index, LongBuffer data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetBufferParameteri64v(int target, int pname, long[] params, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetBufferParameteri64v(int target, int pname, LongBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGenSamplers(int count, int[] samplers, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGenSamplers(int count, IntBuffer samplers) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDeleteSamplers(int count, int[] samplers, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDeleteSamplers(int count, IntBuffer samplers) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean glIsSampler(int sampler) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void glBindSampler(int unit, int sampler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glSamplerParameteri(int sampler, int pname, int param) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glSamplerParameteriv(int sampler, int pname, int[] param, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glSamplerParameteriv(int sampler, int pname, IntBuffer param) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glSamplerParameterf(int sampler, int pname, float param) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glSamplerParameterfv(int sampler, int pname, float[] param, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glSamplerParameterfv(int sampler, int pname, FloatBuffer param) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetSamplerParameteriv(int sampler, int pname, int[] params, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetSamplerParameteriv(int sampler, int pname, IntBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetSamplerParameterfv(int sampler, int pname, float[] params, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetSamplerParameterfv(int sampler, int pname, FloatBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glVertexAttribDivisor(int index, int divisor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glBindTransformFeedback(int target, int id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDeleteTransformFeedbacks(int n, int[] ids, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDeleteTransformFeedbacks(int n, IntBuffer ids) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGenTransformFeedbacks(int n, int[] ids, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGenTransformFeedbacks(int n, IntBuffer ids) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean glIsTransformFeedback(int id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void glPauseTransformFeedback() {
		// TODO Auto-generated method stub

	}

	@Override
	public void glResumeTransformFeedback() {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetProgramBinary(int program, int bufSize, int[] length, int lengthOffset, int[] binaryFormat, int binaryFormatOffset, Buffer binary) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetProgramBinary(int program, int bufSize, IntBuffer length, IntBuffer binaryFormat, Buffer binary) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glProgramBinary(int program, int binaryFormat, Buffer binary, int length) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glProgramParameteri(int program, int pname, int value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glInvalidateFramebuffer(int target, int numAttachments, int[] attachments, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glInvalidateFramebuffer(int target, int numAttachments, IntBuffer attachments) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glInvalidateSubFramebuffer(int target, int numAttachments, int[] attachments, int offset, int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glInvalidateSubFramebuffer(int target, int numAttachments, IntBuffer attachments, int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glTexStorage2D(int target, int levels, int internalformat, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glTexStorage3D(int target, int levels, int internalformat, int width, int height, int depth) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetInternalformativ(int target, int internalformat, int pname, int bufSize, int[] params, int offset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glGetInternalformativ(int target, int internalformat, int pname, int bufSize, IntBuffer params) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean glIsFramebuffer(int handler) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean glIsTexture(int handler) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void glClearColorMask(int mask, float red, float green, float blue, float alpha) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glViewport(int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean supportsExtension(String extension) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsRenderer(String renderer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String glVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean limitGLESContext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float getMaxAnisotropicFilterLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxTextureSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxTextureUnit() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] compileShaderProgram(String source, String prefix) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean validShaderProgram(int[] handlers) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void destroyShaderProgram(int[] handlers) {
		// TODO Auto-generated method stub

	}

	@Override
	public int[] genMesh(int max_v_data, boolean v_static, int max_i_data, boolean i_static) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean validMesh(int[] handlers) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void destroyMesh(int[] outHandlers) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean capabilitySwitch(boolean enable, int cap) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setDepthMask(boolean depthMask) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setDepthTest(int depthFunction, float depthRangeNear, float depthRangeFar) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setBlending(boolean enabled, int sFactor, int dFactor) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getLog() {
		// TODO Auto-generated method stub
		return null;
	}

}
