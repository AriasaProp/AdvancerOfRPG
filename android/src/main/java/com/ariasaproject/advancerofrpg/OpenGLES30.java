package com.ariasaproject.advancerofrpg;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import com.ariasaproject.advancerofrpg.graphics.Cubemap;
import com.ariasaproject.advancerofrpg.graphics.Texture;
import com.ariasaproject.advancerofrpg.graphics.TextureArray;
import com.ariasaproject.advancerofrpg.graphics.glutils.GLFrameBuffer;
import com.ariasaproject.advancerofrpg.graphics.glutils.TextureData;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.BufferUtils;
import com.ariasaproject.advancerofrpg.utils.IntArray;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;


public class OpenGLES30 implements AndroidTGF {
    public static final String TAG = "GLES 3.0";
    // chaced data graphic function reset when invalidate
    public final IntArray enabledCaps = new IntArray();
    private final String shaderHeader = "#version 300 es\n" + "#define LOW lowp\n" + "#define MED mediump\n"
            + "#ifdef GL_FRAGMENT_PRECISION_HIGH\n" + "#define HIGH highp\n" + "#else\n" + "#define HIGH mediump\n"
            + "#endif\n";
    protected int maxTextureSize;
    protected int maxTextureUnit;
    // chace value for once only
    float maxAnisotropicFilterLevel;
    boolean limitGlesContext;
    String extensions, renderers;
    boolean depthMask;
    float depthRangeNear, depthRangeFar;
    int blendSFactor, blendDFactor, depthFunc, cullFace;
    // future for managed generate and delete texture
    Array<TextureData> textures = new Array<TextureData>();
    Array<int[]> shaderPrograms = new Array<int[]>();
    Array<int[]> meshes = new Array<int[]>();

    @Override
    public void glActiveTexture(int unit) {
        if (unit >= getMaxTextureUnit())
            throw new IllegalArgumentException("inputed units more than max units => " + getMaxTextureUnit());
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + unit);
    }

    @Override
    public void glBindTexture(int target, int texture) {

        GLES20.glBindTexture(target, texture);
    }

    @Override
    public void glBindAttribLocation(int program, int index, String name) {

        GLES20.glBindAttribLocation(program, index, name);
    }

    @Override
    public void glBindBuffer(int target, int buffer) {

        GLES20.glBindBuffer(target, buffer);
    }

    @Override
    public void glBindFramebuffer(int target, int framebuffer) {

        GLES20.glBindFramebuffer(target, framebuffer);
    }

    @Override
    public void glBindRenderbuffer(int target, int renderbuffer) {

        GLES20.glBindRenderbuffer(target, renderbuffer);
    }

    @Override
    public void glBindVertexArray(int v) {
        GLES30.glBindVertexArray(v);
    }

    @Override
    public void glBlendColor(float red, float green, float blue, float alpha) {

        GLES20.glBlendColor(red, green, blue, alpha);
    }

    @Override
    public void glBlendEquation(int mode) {
        GLES20.glBlendEquation(mode);
    }

    @Override
    public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
        GLES20.glBlendEquationSeparate(modeRGB, modeAlpha);
    }

    @Override
    public void glBufferData(int target, int size, Buffer data, int usage) {

        GLES20.glBufferData(target, size, data, usage);
    }

    @Override
    public void glBufferSubData(int target, int offset, int size, Buffer data) {

        GLES20.glBufferSubData(target, offset, size, data);
    }

    @Override
    public int glCheckFramebufferStatus(int target) {

        return GLES20.glCheckFramebufferStatus(target);
    }

    @Override
    public void glClearDepth(float depth) {
        GLES20.glClearDepthf(depth);
    }

    @Override
    public void glClearStencil(int s) {
        GLES20.glClearStencil(s);
    }

    @Override
    public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height,
                                 int border) {

        GLES20.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
    }

    @Override
    public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width,
                                    int height) {

        GLES20.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
    }

    @Override
    public void setCullFace(int mode) {
        if (cullFace == mode)
            return;
        GLES20.glCullFace(mode);
        cullFace = mode;
    }

    @Override
    public void glDeleteBuffers(int n, IntBuffer buffers) {

        GLES20.glDeleteBuffers(n, buffers);
    }

    @Override
    public void glDeleteBuffer(int buffer) {

        ints[0] = buffer;
        GLES20.glDeleteBuffers(1, ints, 0);
    }

    @Override
    public void glDeleteFramebuffers(int n, IntBuffer framebuffers) {

        GLES20.glDeleteFramebuffers(n, framebuffers);
    }

    @Override
    public void glDeleteFramebuffer(int framebuffer) {

        ints[0] = framebuffer;
        GLES20.glDeleteFramebuffers(1, ints, 0);
    }

    @Override
    public void glDeleteProgram(int program) {

        GLES20.glDeleteProgram(program);
    }

    @Override
    public void glDeleteRenderbuffers(int n, IntBuffer renderbuffers) {

        GLES20.glDeleteRenderbuffers(n, renderbuffers);
    }

    @Override
    public void glDeleteRenderbuffer(int renderbuffer) {

        ints[0] = renderbuffer;
        GLES20.glDeleteRenderbuffers(1, ints, 0);
    }

    @Override
    public void glDeleteTexture(int texture) {
        ints[0] = texture;
        GLES20.glDeleteTextures(1, ints, 0);
    }

    @Override
    public void glDepthFunc(int func) {
        if (func != depthFunc)
            GLES20.glDepthFunc(func);
    }

    @Override
    public void glDepthRangef(float near, float far) {

        if (depthRangeNear != near || depthRangeFar != far)
            GLES20.glDepthRangef(near, far);
    }

    @Override
    public void glDetachShader(int program, int shader) {

        GLES20.glDetachShader(program, shader);
    }

    @Override
    public void glDisableVertexAttribArray(int index) {

        GLES20.glDisableVertexAttribArray(index);
    }

    @Override
    public void glDrawArrays(int mode, int first, int count) {

        GLES20.glDrawArrays(mode, first, count);
    }

    @Override
    public void glDrawElements(int mode, int count, int type, Buffer indices) {

        GLES20.glDrawElements(mode, count, type, indices);
    }

    @Override
    public void glDrawElements(int mode, int count, int type, int indices) {

        GLES20.glDrawElements(mode, count, type, indices);
    }

    @Override
    public void glEnableVertexAttribArray(int index) {

        GLES20.glEnableVertexAttribArray(index);
    }

    @Override
    public void glFinish() {

        GLES20.glFinish();
    }

    @Override
    public void glFlush() {

        GLES20.glFlush();
    }

    @Override
    public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {

        GLES20.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
    }

    @Override
    public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {

        GLES20.glFramebufferTexture2D(target, attachment, textarget, texture, level);
    }

    @Override
    public void glFrontFace(int mode) {

        GLES20.glFrontFace(mode);
    }

    @Override
    public void glGenBuffers(int n, IntBuffer buffers) {

        GLES20.glGenBuffers(n, buffers);
    }

    @Override
    public int glGenBuffer() {

        GLES20.glGenBuffers(1, ints, 0);
        return ints[0];
    }

    @Override
    public void glGenerateMipmap(int target) {
        GLES20.glGenerateMipmap(target);
    }

    @Override
    public void glGenFramebuffers(int n, IntBuffer framebuffers) {
        GLES20.glGenFramebuffers(n, framebuffers);
    }
    
    @Override
    public int glGenFramebuffer() {

        GLES20.glGenFramebuffers(1, ints, 0);
        return ints[0];
    }

    @Override
    public void glGenRenderbuffers(int n, IntBuffer renderbuffers) {

        GLES20.glGenRenderbuffers(n, renderbuffers);
    }

    @Override
    public int glGenRenderbuffer() {

        GLES20.glGenRenderbuffers(1, ints, 0);
        return ints[0];
    }

    @Override
    public int glGenTexture() {
        GLES20.glGenTextures(1, ints, 0);
        return ints[0];
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
        GLES20.glGetActiveAttrib(program, index, buffer.length, ints, 0, ints2, 0, ints3, 0, buffer, 0);
        return new String(buffer, 0, ints[0]);
    }

    @Override
    public String glGetActiveUniform(int program, int index, IntBuffer size, Buffer type) {

        // length
        ints[0] = 0;
        // size
        ints2[0] = size.get(0);
        // type
        ints3[0] = ((IntBuffer) type).get(0);
        GLES20.glGetActiveUniform(program, index, buffer.length, ints, 0, ints2, 0, ints3, 0, buffer, 0);
        return new String(buffer, 0, ints[0]);
    }

    @Override
    public void glGetAttachedShaders(int program, int maxcount, Buffer count, IntBuffer shaders) {

        GLES20.glGetAttachedShaders(program, maxcount, (IntBuffer) count, shaders);
    }

    @Override
    public int glGetAttribLocation(int program, String name) {

        return GLES20.glGetAttribLocation(program, name);
    }

    @Override
    public void glGetBooleanv(int pname, Buffer params) {

        GLES20.glGetBooleanv(pname, (IntBuffer) params);
    }

    @Override
    public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {

        GLES20.glGetBufferParameteriv(target, pname, params);
    }

    @Override
    public int glGetError() {

        return GLES20.glGetError();
    }

    @Override
    public void glGetFloatv(int pname, FloatBuffer params) {

        GLES20.glGetFloatv(pname, params);
    }

    @Override
    public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params) {

        GLES20.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
    }

    @Override
    public void glGetIntegerv(int pname, IntBuffer params) {

        GLES20.glGetIntegerv(pname, params);
    }

    @Override
    public int glGetIntegerv(int pname) {

        GLES20.glGetIntegerv(pname, ints, 0);
        return ints[0];
    }

    @Override
    public void glGetProgramiv(int program, int pname, IntBuffer params) {

        GLES20.glGetProgramiv(program, pname, params);
    }

    @Override
    public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params) {

        GLES20.glGetRenderbufferParameteriv(target, pname, params);
    }

    @Override
    public void glGetShaderiv(int shader, int pname, IntBuffer params) {

        GLES20.glGetShaderiv(shader, pname, params);
    }

    @Override
    public String glGetShaderInfoLog(int shader) {

        return GLES20.glGetShaderInfoLog(shader);
    }

    @Override
    public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {

        GLES20.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
    }

    @Override
    public String glGetString(int name) {

        return GLES20.glGetString(name);
    }

    @Override
    public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {

        GLES20.glGetTexParameterfv(target, pname, params);
    }

    @Override
    public void glGetTexParameteriv(int target, int pname, IntBuffer params) {

        GLES20.glGetTexParameteriv(target, pname, params);
    }

    @Override
    public void glGetUniformfv(int program, int location, FloatBuffer params) {

        GLES20.glGetUniformfv(program, location, params);
    }

    @Override
    public void glGetUniformiv(int program, int location, IntBuffer params) {

        GLES20.glGetUniformiv(program, location, params);
    }

    @Override
    public int glGetUniformLocation(int program, String name) {

        return GLES20.glGetUniformLocation(program, name);
    }

    @Override
    public void glGetVertexAttribfv(int index, int pname, FloatBuffer params) {

        GLES20.glGetVertexAttribfv(index, pname, params);
    }

    @Override
    public void glGetVertexAttribiv(int index, int pname, IntBuffer params) {

        GLES20.glGetVertexAttribiv(index, pname, params);
    }

    @Override
    public void glHint(int target, int mode) {

        GLES20.glHint(target, mode);
    }

    @Override
    public void glLineWidth(float width) {

        GLES20.glLineWidth(width);
    }

    @Override
    public void glPixelStorei(int pname, int param) {

        GLES20.glPixelStorei(pname, param);
    }

    @Override
    public void glPolygonOffset(float factor, float units) {

        GLES20.glPolygonOffset(factor, units);
    }

    @Override
    public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels) {

        GLES20.glReadPixels(x, y, width, height, format, type, pixels);
    }

    @Override
    public void glReleaseShaderCompiler() {

        GLES20.glReleaseShaderCompiler();
    }

    @Override
    public void glRenderbufferStorage(int target, int internalformat, int width, int height) {
        GLES20.glRenderbufferStorage(target, internalformat, width, height);
    }

    @Override
    public void glScissor(int x, int y, int width, int height) {
        GLES20.glScissor(x, y, width, height);
    }

    @Override
    public void glShaderBinary(int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
        GLES20.glShaderBinary(n, shaders, binaryformat, binary, length);
    }

    @Override
    public void glStencilFunc(int func, int ref, int mask) {
        GLES20.glStencilFunc(func, ref, mask);
    }

    @Override
    public void glStencilFuncSeparate(int face, int func, int ref, int mask) {
        GLES20.glStencilFuncSeparate(face, func, ref, mask);
    }

    @Override
    public void glStencilMask(int mask) {

        GLES20.glStencilMask(mask);
    }

    @Override
    public void glStencilMaskSeparate(int face, int mask) {

        GLES20.glStencilMaskSeparate(face, mask);
    }

    @Override
    public void glStencilOp(int fail, int zfail, int zpass) {

        GLES20.glStencilOp(fail, zfail, zpass);
    }

    @Override
    public void glStencilOpSeparate(int face, int fail, int zfail, int zpass) {

        GLES20.glStencilOpSeparate(face, fail, zfail, zpass);
    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
                             int type, Buffer pixels) {

        GLES20.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    @Override
    public void glTexParameterf(int target, int pname, float param) {

        GLES20.glTexParameterf(target, pname, param);
    }

    @Override
    public void glTexParameterfv(int target, int pname, FloatBuffer params) {

        GLES20.glTexParameterfv(target, pname, params);
    }

    @Override
    public void glTexParameteri(int target, int pname, int param) {

        GLES20.glTexParameteri(target, pname, param);
    }

    @Override
    public void glTexParameteriv(int target, int pname, IntBuffer params) {

        GLES20.glTexParameteriv(target, pname, params);
    }

    @Override
    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
                                int type, Buffer pixels) {

        GLES20.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    @Override
    public void glUniform1f(int location, float x) {

        GLES20.glUniform1f(location, x);
    }

    @Override
    public void glUniform1fv(int location, int count, FloatBuffer v) {

        GLES20.glUniform1fv(location, count, v);
    }

    @Override
    public void glUniform1fv(int location, int count, float[] v, int offset) {

        GLES20.glUniform1fv(location, count, v, offset);
    }

    @Override
    public void glUniform1i(int location, int x) {

        GLES20.glUniform1i(location, x);
    }

    @Override
    public void glUniform1iv(int location, int count, IntBuffer v) {

        GLES20.glUniform1iv(location, count, v);
    }

    @Override
    public void glUniform1iv(int location, int count, int[] v, int offset) {

        GLES20.glUniform1iv(location, count, v, offset);
    }

    @Override
    public void glUniform2f(int location, float x, float y) {

        GLES20.glUniform2f(location, x, y);
    }

    @Override
    public void glUniform2fv(int location, int count, FloatBuffer v) {

        GLES20.glUniform2fv(location, count, v);
    }

    @Override
    public void glUniform2fv(int location, int count, float[] v, int offset) {

        GLES20.glUniform2fv(location, count, v, offset);
    }

    @Override
    public void glUniform2i(int location, int x, int y) {

        GLES20.glUniform2i(location, x, y);
    }

    @Override
    public void glUniform2iv(int location, int count, IntBuffer v) {

        GLES20.glUniform2iv(location, count, v);
    }

    @Override
    public void glUniform2iv(int location, int count, int[] v, int offset) {

        GLES20.glUniform2iv(location, count, v, offset);
    }

    @Override
    public void glUniform3f(int location, float x, float y, float z) {

        GLES20.glUniform3f(location, x, y, z);
    }

    @Override
    public void glUniform3fv(int location, int count, FloatBuffer v) {

        GLES20.glUniform3fv(location, count, v);
    }

    @Override
    public void glUniform3fv(int location, int count, float[] v, int offset) {

        GLES20.glUniform3fv(location, count, v, offset);
    }

    @Override
    public void glUniform3i(int location, int x, int y, int z) {

        GLES20.glUniform3i(location, x, y, z);
    }

    @Override
    public void glUniform3iv(int location, int count, IntBuffer v) {

        GLES20.glUniform3iv(location, count, v);
    }

    @Override
    public void glUniform3iv(int location, int count, int[] v, int offset) {

        GLES20.glUniform3iv(location, count, v, offset);
    }

    @Override
    public void glUniform4f(int location, float x, float y, float z, float w) {

        GLES20.glUniform4f(location, x, y, z, w);
    }

    @Override
    public void glUniform4fv(int location, int count, FloatBuffer v) {

        GLES20.glUniform4fv(location, count, v);
    }

    @Override
    public void glUniform4fv(int location, int count, float[] v, int offset) {

        GLES20.glUniform4fv(location, count, v, offset);
    }

    @Override
    public void glUniform4i(int location, int x, int y, int z, int w) {

        GLES20.glUniform4i(location, x, y, z, w);
    }

    @Override
    public void glUniform4iv(int location, int count, IntBuffer v) {

        GLES20.glUniform4iv(location, count, v);
    }

    @Override
    public void glUniform4iv(int location, int count, int[] v, int offset) {

        GLES20.glUniform4iv(location, count, v, offset);
    }

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer value) {

        GLES20.glUniformMatrix2fv(location, count, transpose, value);
    }

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, float[] value, int offset) {

        GLES20.glUniformMatrix2fv(location, count, transpose, value, offset);
    }

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer value) {

        GLES20.glUniformMatrix3fv(location, count, transpose, value);
    }

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, float[] value, int offset) {

        GLES20.glUniformMatrix3fv(location, count, transpose, value, offset);
    }

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer value) {

        GLES20.glUniformMatrix4fv(location, count, transpose, value);
    }

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int offset) {

        GLES20.glUniformMatrix4fv(location, count, transpose, value, offset);
    }

    @Override
    public void glUseProgram(int program) {
        GLES20.glUseProgram(program);
    }

    @Override
    public void glValidateProgram(int program) {
        GLES20.glValidateProgram(program);
    }

    @Override
    public void glVertexAttrib1f(int indx, float x) {
        GLES20.glVertexAttrib1f(indx, x);
    }

    @Override
    public void glVertexAttrib1fv(int indx, FloatBuffer values) {
        GLES20.glVertexAttrib1fv(indx, values);
    }

    @Override
    public void glVertexAttrib2f(int indx, float x, float y) {
        GLES20.glVertexAttrib2f(indx, x, y);
    }

    @Override
    public void glVertexAttrib2fv(int indx, FloatBuffer values) {
        GLES20.glVertexAttrib2fv(indx, values);
    }

    @Override
    public void glVertexAttrib3f(int indx, float x, float y, float z) {
        GLES20.glVertexAttrib3f(indx, x, y, z);
    }

    @Override
    public void glVertexAttrib3fv(int indx, FloatBuffer values) {
        GLES20.glVertexAttrib3fv(indx, values);
    }

    @Override
    public void glVertexAttrib4f(int indx, float x, float y, float z, float w) {
        GLES20.glVertexAttrib4f(indx, x, y, z, w);
    }

    @Override
    public void glVertexAttrib4fv(int indx, FloatBuffer values) {

        GLES20.glVertexAttrib4fv(indx, values);
    }

    @Override
    public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, Buffer ptr) {

        GLES20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
    }

    @Override
    public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int ptr) {

        GLES20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
    }

    @Override
    public void glDrawRangeElements(int mode, int start, int end, int count, int type, java.nio.Buffer indices) {

        GLES30.glDrawRangeElements(mode, start, end, count, type, indices);
    }

    @Override
    public void glDrawRangeElements(int mode, int start, int end, int count, int type, int offset) {

        GLES30.glDrawRangeElements(mode, start, end, count, type, offset);
    }

    @Override
    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
                             int format, int type, java.nio.Buffer pixels) {

        if (pixels == null)
            GLES30.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, 0);
        else
            GLES30.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
    }

    @Override
    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border,
                             int format, int type, int offset) {

        GLES30.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, offset);
    }

    @Override
    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
                                int depth, int format, int type, java.nio.Buffer pixels) {

        GLES30.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    @Override
    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
                                int depth, int format, int type, int offset) {

        GLES30.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, offset);
    }

    @Override
    public void glCopyTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int x, int y,
                                    int width, int height) {

        GLES30.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height);
    }

    @Override
    public void glGenQueries(int n, int[] ids, int offset) {

        GLES30.glGenQueries(n, ids, offset);
    }

    @Override
    public void glGenQueries(int n, java.nio.IntBuffer ids) {

        GLES30.glGenQueries(n, ids);
    }

    @Override
    public void glDeleteQueries(int n, int[] ids, int offset) {

        GLES30.glDeleteQueries(n, ids, offset);
    }

    @Override
    public void glDeleteQueries(int n, java.nio.IntBuffer ids) {

        GLES30.glDeleteQueries(n, ids);
    }

    @Override
    public boolean glIsQuery(int id) {

        return GLES30.glIsQuery(id);
    }

    @Override
    public void glBeginQuery(int target, int id) {

        GLES30.glBeginQuery(target, id);
    }

    @Override
    public void glEndQuery(int target) {

        GLES30.glEndQuery(target);
    }

    @Override
    public void glGetQueryiv(int target, int pname, int[] params, int offset) {

        GLES30.glGetQueryiv(target, pname, params, offset);
    }

    @Override
    public void glGetQueryiv(int target, int pname, java.nio.IntBuffer params) {

        GLES30.glGetQueryiv(target, pname, params);
    }

    @Override
    public void glGetQueryObjectuiv(int id, int pname, int[] params, int offset) {

        GLES30.glGetQueryObjectuiv(id, pname, params, offset);
    }

    @Override
    public void glGetQueryObjectuiv(int id, int pname, java.nio.IntBuffer params) {

        GLES30.glGetQueryObjectuiv(id, pname, params);
    }

    @Override
    public boolean glUnmapBuffer(int target) {

        return GLES30.glUnmapBuffer(target);
    }

    @Override
    public java.nio.Buffer glGetBufferPointerv(int target, int pname) {
        return GLES30.glGetBufferPointerv(target, pname);
    }

    @Override
    public void glDrawBuffers(int n, int[] bufs, int offset) {

        GLES30.glDrawBuffers(n, bufs, offset);
    }

    @Override
    public void glDrawBuffers(int n, java.nio.IntBuffer bufs) {

        GLES30.glDrawBuffers(n, bufs);
    }

    @Override
    public void glReadBuffer(int func) {

        GLES30.glReadBuffer(func);
    }

    @Override
    public void glUniformMatrix2x3fv(int location, int count, boolean transpose, float[] value, int offset) {

        GLES30.glUniformMatrix2x3fv(location, count, transpose, value, offset);
    }

    @Override
    public void glUniformMatrix2x3fv(int location, int count, boolean transpose, java.nio.FloatBuffer value) {

        GLES30.glUniformMatrix2x3fv(location, count, transpose, value);
    }

    @Override
    public void glUniformMatrix3x2fv(int location, int count, boolean transpose, float[] value, int offset) {

        GLES30.glUniformMatrix3x2fv(location, count, transpose, value, offset);
    }

    @Override
    public void glUniformMatrix3x2fv(int location, int count, boolean transpose, java.nio.FloatBuffer value) {

        GLES30.glUniformMatrix3x2fv(location, count, transpose, value);
    }

    @Override
    public void glUniformMatrix2x4fv(int location, int count, boolean transpose, float[] value, int offset) {

        GLES30.glUniformMatrix2x4fv(location, count, transpose, value, offset);
    }

    @Override
    public void glUniformMatrix2x4fv(int location, int count, boolean transpose, java.nio.FloatBuffer value) {

        GLES30.glUniformMatrix2x4fv(location, count, transpose, value);
    }

    @Override
    public void glUniformMatrix4x2fv(int location, int count, boolean transpose, float[] value, int offset) {

        GLES30.glUniformMatrix4x2fv(location, count, transpose, value, offset);
    }

    @Override
    public void glUniformMatrix4x2fv(int location, int count, boolean transpose, java.nio.FloatBuffer value) {

        GLES30.glUniformMatrix4x2fv(location, count, transpose, value);
    }

    @Override
    public void glUniformMatrix3x4fv(int location, int count, boolean transpose, float[] value, int offset) {

        GLES30.glUniformMatrix3x4fv(location, count, transpose, value, offset);
    }

    @Override
    public void glUniformMatrix3x4fv(int location, int count, boolean transpose, java.nio.FloatBuffer value) {

        GLES30.glUniformMatrix3x4fv(location, count, transpose, value);
    }

    @Override
    public void glUniformMatrix4x3fv(int location, int count, boolean transpose, float[] value, int offset) {

        GLES30.glUniformMatrix4x3fv(location, count, transpose, value, offset);
    }

    @Override
    public void glUniformMatrix4x3fv(int location, int count, boolean transpose, java.nio.FloatBuffer value) {

        GLES30.glUniformMatrix4x3fv(location, count, transpose, value);
    }

    @Override
    public void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1,
                                  int dstY1, int mask, int filter) {

        GLES30.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
    }

    @Override
    public void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width, int height) {

        GLES30.glRenderbufferStorageMultisample(target, samples, internalformat, width, height);
    }

    @Override
    public void glFramebufferTextureLayer(int target, int attachment, int texture, int level, int layer) {

        GLES30.glFramebufferTextureLayer(target, attachment, texture, level, layer);
    }

    @Override
    public java.nio.Buffer glMapBufferRange(int target, int offset, int length, int access) {

        return GLES30.glMapBufferRange(target, offset, length, access);
    }

    @Override
    public void glFlushMappedBufferRange(int target, int offset, int length) {

        GLES30.glFlushMappedBufferRange(target, offset, length);
    }
    
    @Override
    public void glGetIntegeri_v(int target, int index, int[] data, int offset) {

        GLES30.glGetIntegeri_v(target, index, data, offset);
    }

    @Override
    public void glGetIntegeri_v(int target, int index, java.nio.IntBuffer data) {

        GLES30.glGetIntegeri_v(target, index, data);
    }

    @Override
    public void glBeginTransformFeedback(int primitiveMode) {

        GLES30.glBeginTransformFeedback(primitiveMode);
    }

    @Override
    public void glEndTransformFeedback() {

        GLES30.glEndTransformFeedback();
    }

    @Override
    public void glBindBufferRange(int target, int index, int buffer, int offset, int size) {

        GLES30.glBindBufferRange(target, index, buffer, offset, size);
    }

    @Override
    public void glBindBufferBase(int target, int index, int buffer) {

        GLES30.glBindBufferBase(target, index, buffer);
    }

    @Override
    public void glTransformFeedbackVaryings(int program, String[] varyings, int bufferMode) {

        GLES30.glTransformFeedbackVaryings(program, varyings, bufferMode);
    }

    @Override
    public void glGetTransformFeedbackVarying(int program, int index, int bufsize, int[] length, int lengthOffset,
                                              int[] size, int sizeOffset, int[] type, int typeOffset, byte[] name, int nameOffset) {

        GLES30.glGetTransformFeedbackVarying(program, index, bufsize, length, lengthOffset, size, sizeOffset, type,
                typeOffset, name, nameOffset);
    }

    @Override
    public String glGetTransformFeedbackVarying(int program, int index, int[] size, int sizeOffset, int[] type,
                                                int typeOffset) {

        return GLES30.glGetTransformFeedbackVarying(program, index, size, sizeOffset, type, typeOffset);
    }

    @Override
    public String glGetTransformFeedbackVarying(int program, int index, java.nio.IntBuffer size,
                                                java.nio.IntBuffer type) {

        return GLES30.glGetTransformFeedbackVarying(program, index, size, type);
    }

    @Override
    public void glVertexAttribIPointer(int index, int size, int type, int stride, int offset) {

        GLES30.glVertexAttribIPointer(index, size, type, stride, offset);
    }

    @Override
    public void glGetVertexAttribIiv(int index, int pname, int[] params, int offset) {

        GLES30.glGetVertexAttribIiv(index, pname, params, offset);
    }

    @Override
    public void glGetVertexAttribIiv(int index, int pname, java.nio.IntBuffer params) {

        GLES30.glGetVertexAttribIiv(index, pname, params);
    }

    @Override
    public void glGetVertexAttribIuiv(int index, int pname, int[] params, int offset) {

        GLES30.glGetVertexAttribIuiv(index, pname, params, offset);
    }

    @Override
    public void glGetVertexAttribIuiv(int index, int pname, java.nio.IntBuffer params) {

        GLES30.glGetVertexAttribIuiv(index, pname, params);
    }

    @Override
    public void glVertexAttribI4i(int index, int x, int y, int z, int w) {

        GLES30.glVertexAttribI4i(index, x, y, z, w);
    }

    @Override
    public void glVertexAttribI4ui(int index, int x, int y, int z, int w) {

        GLES30.glVertexAttribI4ui(index, x, y, z, w);
    }

    @Override
    public void glVertexAttribI4iv(int index, int[] v, int offset) {

        GLES30.glVertexAttribI4iv(index, v, offset);
    }

    @Override
    public void glVertexAttribI4iv(int index, java.nio.IntBuffer v) {

        GLES30.glVertexAttribI4iv(index, v);
    }

    @Override
    public void glVertexAttribI4uiv(int index, int[] v, int offset) {

        GLES30.glVertexAttribI4uiv(index, v, offset);
    }

    @Override
    public void glVertexAttribI4uiv(int index, java.nio.IntBuffer v) {

        GLES30.glVertexAttribI4uiv(index, v);
    }

    @Override
    public void glGetUniformuiv(int program, int location, int[] params, int offset) {

        GLES30.glGetUniformuiv(program, location, params, offset);
    }

    @Override
    public void glGetUniformuiv(int program, int location, java.nio.IntBuffer params) {

        GLES30.glGetUniformuiv(program, location, params);
    }

    @Override
    public int glGetFragDataLocation(int program, String name) {

        return GLES30.glGetFragDataLocation(program, name);
    }

    @Override
    public void glUniform1ui(int location, int v0) {

        GLES30.glUniform1ui(location, v0);
    }

    @Override
    public void glUniform2ui(int location, int v0, int v1) {

        GLES30.glUniform2ui(location, v0, v1);
    }

    @Override
    public void glUniform3ui(int location, int v0, int v1, int v2) {

        GLES30.glUniform3ui(location, v0, v1, v2);
    }

    @Override
    public void glUniform4ui(int location, int v0, int v1, int v2, int v3) {

        GLES30.glUniform4ui(location, v0, v1, v2, v3);
    }

    @Override
    public void glUniform1uiv(int location, int count, int[] value, int offset) {

        GLES30.glUniform1uiv(location, count, value, offset);
    }

    @Override
    public void glUniform1uiv(int location, int count, java.nio.IntBuffer value) {

        GLES30.glUniform1uiv(location, count, value);
    }

    @Override
    public void glUniform2uiv(int location, int count, int[] value, int offset) {

        GLES30.glUniform2uiv(location, count, value, offset);
    }

    @Override
    public void glUniform2uiv(int location, int count, java.nio.IntBuffer value) {

        GLES30.glUniform2uiv(location, count, value);
    }

    @Override
    public void glUniform3uiv(int location, int count, int[] value, int offset) {

        GLES30.glUniform3uiv(location, count, value, offset);
    }

    @Override
    public void glUniform3uiv(int location, int count, java.nio.IntBuffer value) {

        GLES30.glUniform3uiv(location, count, value);
    }

    @Override
    public void glUniform4uiv(int location, int count, int[] value, int offset) {

        GLES30.glUniform4uiv(location, count, value, offset);
    }

    @Override
    public void glUniform4uiv(int location, int count, java.nio.IntBuffer value) {

        GLES30.glUniform4uiv(location, count, value);
    }

    @Override
    public void glClearBufferiv(int buffer, int drawbuffer, int[] value, int offset) {

        GLES30.glClearBufferiv(buffer, drawbuffer, value, offset);
    }

    @Override
    public void glClearBufferiv(int buffer, int drawbuffer, java.nio.IntBuffer value) {

        GLES30.glClearBufferiv(buffer, drawbuffer, value);
    }

    @Override
    public void glClearBufferuiv(int buffer, int drawbuffer, int[] value, int offset) {

        GLES30.glClearBufferuiv(buffer, drawbuffer, value, offset);
    }

    @Override
    public void glClearBufferuiv(int buffer, int drawbuffer, java.nio.IntBuffer value) {

        GLES30.glClearBufferuiv(buffer, drawbuffer, value);
    }

    @Override
    public void glClearBufferfv(int buffer, int drawbuffer, float[] value, int offset) {
        GLES30.glClearBufferfv(buffer, drawbuffer, value, offset);
    }

    @Override
    public void glClearBufferfv(int buffer, int drawbuffer, java.nio.FloatBuffer value) {

        GLES30.glClearBufferfv(buffer, drawbuffer, value);
    }

    @Override
    public void glClearBufferfi(int buffer, int drawbuffer, float depth, int stencil) {

        GLES30.glClearBufferfi(buffer, drawbuffer, depth, stencil);
    }

    @Override
    public String glGetStringi(int name, int index) {

        return GLES30.glGetStringi(name, index);
    }

    @Override
    public void glCopyBufferSubData(int readTarget, int writeTarget, int readOffset, int writeOffset, int size) {

        GLES30.glCopyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size);
    }

    @Override
    public void glGetUniformIndices(int program, String[] uniformNames, int[] uniformIndices,
                                    int uniformIndicesOffset) {

        GLES30.glGetUniformIndices(program, uniformNames, uniformIndices, uniformIndicesOffset);
    }

    @Override
    public void glGetUniformIndices(int program, String[] uniformNames, java.nio.IntBuffer uniformIndices) {

        GLES30.glGetUniformIndices(program, uniformNames, uniformIndices);
    }

    @Override
    public void glGetActiveUniformsiv(int program, int uniformCount, int[] uniformIndices, int uniformIndicesOffset,
                                      int pname, int[] params, int paramsOffset) {

        GLES30.glGetActiveUniformsiv(program, uniformCount, uniformIndices, uniformIndicesOffset, pname, params,
                paramsOffset);
    }

    @Override
    public void glGetActiveUniformsiv(int program, int uniformCount, java.nio.IntBuffer uniformIndices, int pname,
                                      java.nio.IntBuffer params) {

        GLES30.glGetActiveUniformsiv(program, uniformCount, uniformIndices, pname, params);
    }

    @Override
    public int glGetUniformBlockIndex(int program, String uniformBlockName) {

        return GLES30.glGetUniformBlockIndex(program, uniformBlockName);
    }

    @Override
    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, int[] params, int offset) {

        GLES30.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, params, offset);
    }

    @Override
    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, java.nio.IntBuffer params) {

        GLES30.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, params);
    }

    @Override
    public void glGetActiveUniformBlockName(int program, int uniformBlockIndex, int bufSize, int[] length,
                                            int lengthOffset, byte[] uniformBlockName, int uniformBlockNameOffset) {

        GLES30.glGetActiveUniformBlockName(program, uniformBlockIndex, bufSize, length, lengthOffset, uniformBlockName,
                uniformBlockNameOffset);
    }

    @Override
    public void glGetActiveUniformBlockName(int program, int uniformBlockIndex, java.nio.Buffer length,
                                            java.nio.Buffer uniformBlockName) {

        GLES30.glGetActiveUniformBlockName(program, uniformBlockIndex, length, uniformBlockName);
    }

    @Override
    public String glGetActiveUniformBlockName(int program, int uniformBlockIndex) {

        return GLES30.glGetActiveUniformBlockName(program, uniformBlockIndex);
    }

    @Override
    public void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding) {

        GLES30.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
    }

    @Override
    public void glDrawArraysInstanced(int mode, int first, int count, int instanceCount) {

        GLES30.glDrawArraysInstanced(mode, first, count, instanceCount);
    }

    @Override
    public void glDrawElementsInstanced(int mode, int count, int type, java.nio.Buffer indices, int instanceCount) {
        GLES30.glDrawElementsInstanced(mode, count, type, indices, instanceCount);
    }

    @Override
    public void glDrawElementsInstanced(int mode, int count, int type, int indicesOffset, int instanceCount) {

        GLES30.glDrawElementsInstanced(mode, count, type, indicesOffset, instanceCount);
    }

    @Override
    public long glFenceSync(int condition, int flags) {

        return GLES30.glFenceSync(condition, flags);
    }

    @Override
    public boolean glIsSync(long sync) {
        return GLES30.glIsSync(sync);
    }

    @Override
    public void glDeleteSync(long sync) {
        GLES30.glDeleteSync(sync);
    }

    @Override
    public int glClientWaitSync(long sync, int flags, long timeout) {
        return GLES30.glClientWaitSync(sync, flags, timeout);
    }

    @Override
    public void glWaitSync(long sync, int flags, long timeout) {
        GLES30.glWaitSync(sync, flags, timeout);
    }

    @Override
    public void glGetInteger64v(int pname, long[] params, int offset) {

        GLES30.glGetInteger64v(pname, params, offset);
    }

    @Override
    public void glGetInteger64v(int pname, java.nio.LongBuffer params) {

        GLES30.glGetInteger64v(pname, params);
    }

    @Override
    public void glGetSynciv(long sync, int pname, int bufSize, int[] length, int lengthOffset, int[] values,
                            int valuesOffset) {

        GLES30.glGetSynciv(sync, pname, bufSize, length, lengthOffset, values, valuesOffset);
    }

    @Override
    public void glGetSynciv(long sync, int pname, int bufSize, java.nio.IntBuffer length, java.nio.IntBuffer values) {

        GLES30.glGetSynciv(sync, pname, bufSize, length, values);
    }

    @Override
    public void glGetInteger64i_v(int target, int index, long[] data, int offset) {

        GLES30.glGetInteger64i_v(target, index, data, offset);
    }

    @Override
    public void glGetInteger64i_v(int target, int index, java.nio.LongBuffer data) {

        GLES30.glGetInteger64i_v(target, index, data);
    }

    @Override
    public void glGetBufferParameteri64v(int target, int pname, long[] params, int offset) {

        GLES30.glGetBufferParameteri64v(target, pname, params, offset);
    }

    @Override
    public void glGetBufferParameteri64v(int target, int pname, java.nio.LongBuffer params) {

        GLES30.glGetBufferParameteri64v(target, pname, params);
    }

    @Override
    public void glGenSamplers(int count, int[] samplers, int offset) {

        GLES30.glGenSamplers(count, samplers, offset);
    }

    @Override
    public void glGenSamplers(int count, java.nio.IntBuffer samplers) {

        GLES30.glGenSamplers(count, samplers);
    }

    @Override
    public void glDeleteSamplers(int count, int[] samplers, int offset) {

        GLES30.glDeleteSamplers(count, samplers, offset);
    }

    @Override
    public void glDeleteSamplers(int count, java.nio.IntBuffer samplers) {

        GLES30.glDeleteSamplers(count, samplers);
    }

    @Override
    public boolean glIsSampler(int sampler) {

        return GLES30.glIsSampler(sampler);
    }

    @Override
    public void glBindSampler(int unit, int sampler) {

        GLES30.glBindSampler(unit, sampler);
    }

    @Override
    public void glSamplerParameteri(int sampler, int pname, int param) {

        GLES30.glSamplerParameteri(sampler, pname, param);
    }

    @Override
    public void glSamplerParameteriv(int sampler, int pname, int[] param, int offset) {

        GLES30.glSamplerParameteriv(sampler, pname, param, offset);
    }

    @Override
    public void glSamplerParameteriv(int sampler, int pname, java.nio.IntBuffer param) {

        GLES30.glSamplerParameteriv(sampler, pname, param);
    }

    @Override
    public void glSamplerParameterf(int sampler, int pname, float param) {

        GLES30.glSamplerParameterf(sampler, pname, param);
    }

    @Override
    public void glSamplerParameterfv(int sampler, int pname, float[] param, int offset) {

        GLES30.glSamplerParameterfv(sampler, pname, param, offset);
    }

    @Override
    public void glSamplerParameterfv(int sampler, int pname, java.nio.FloatBuffer param) {

        GLES30.glSamplerParameterfv(sampler, pname, param);
    }

    @Override
    public void glGetSamplerParameteriv(int sampler, int pname, int[] params, int offset) {

        GLES30.glGetSamplerParameteriv(sampler, pname, params, offset);
    }

    @Override
    public void glGetSamplerParameteriv(int sampler, int pname, java.nio.IntBuffer params) {

        GLES30.glGetSamplerParameteriv(sampler, pname, params);
    }

    @Override
    public void glGetSamplerParameterfv(int sampler, int pname, float[] params, int offset) {

        GLES30.glGetSamplerParameterfv(sampler, pname, params, offset);
    }

    @Override
    public void glGetSamplerParameterfv(int sampler, int pname, java.nio.FloatBuffer params) {

        GLES30.glGetSamplerParameterfv(sampler, pname, params);
    }

    @Override
    public void glVertexAttribDivisor(int index, int divisor) {

        GLES30.glVertexAttribDivisor(index, divisor);
    }

    @Override
    public void glBindTransformFeedback(int target, int id) {

        GLES30.glBindTransformFeedback(target, id);
    }

    @Override
    public void glDeleteTransformFeedbacks(int n, int[] ids, int offset) {

        GLES30.glDeleteTransformFeedbacks(n, ids, offset);
    }

    @Override
    public void glDeleteTransformFeedbacks(int n, java.nio.IntBuffer ids) {

        GLES30.glDeleteTransformFeedbacks(n, ids);
    }

    @Override
    public void glGenTransformFeedbacks(int n, int[] ids, int offset) {

        GLES30.glGenTransformFeedbacks(n, ids, offset);
    }

    @Override
    public void glGenTransformFeedbacks(int n, java.nio.IntBuffer ids) {

        GLES30.glGenTransformFeedbacks(n, ids);
    }

    @Override
    public boolean glIsTransformFeedback(int id) {

        return GLES30.glIsTransformFeedback(id);
    }

    @Override
    public void glPauseTransformFeedback() {

        GLES30.glPauseTransformFeedback();
    }

    @Override
    public void glResumeTransformFeedback() {

        GLES30.glResumeTransformFeedback();
    }

    @Override
    public void glGetProgramBinary(int program, int bufSize, int[] length, int lengthOffset, int[] binaryFormat,
                                   int binaryFormatOffset, java.nio.Buffer binary) {

        GLES30.glGetProgramBinary(program, bufSize, length, lengthOffset, binaryFormat, binaryFormatOffset, binary);
    }

    @Override
    public void glGetProgramBinary(int program, int bufSize, java.nio.IntBuffer length, java.nio.IntBuffer binaryFormat,
                                   java.nio.Buffer binary) {

        GLES30.glGetProgramBinary(program, bufSize, length, binaryFormat, binary);
    }

    @Override
    public void glProgramBinary(int program, int binaryFormat, java.nio.Buffer binary, int length) {

        GLES30.glProgramBinary(program, binaryFormat, binary, length);
    }

    @Override
    public void glProgramParameteri(int program, int pname, int value) {

        GLES30.glProgramParameteri(program, pname, value);
    }

    @Override
    public void glInvalidateFramebuffer(int target, int numAttachments, int[] attachments, int offset) {

        GLES30.glInvalidateFramebuffer(target, numAttachments, attachments, offset);
    }

    @Override
    public void glInvalidateFramebuffer(int target, int numAttachments, java.nio.IntBuffer attachments) {

        GLES30.glInvalidateFramebuffer(target, numAttachments, attachments);
    }

    @Override
    public void glInvalidateSubFramebuffer(int target, int numAttachments, int[] attachments, int offset, int x, int y,
                                           int width, int height) {

        GLES30.glInvalidateSubFramebuffer(target, numAttachments, attachments, offset, x, y, width, height);
    }

    @Override
    public void glInvalidateSubFramebuffer(int target, int numAttachments, java.nio.IntBuffer attachments, int x, int y,
                                           int width, int height) {

        GLES30.glInvalidateSubFramebuffer(target, numAttachments, attachments, x, y, width, height);
    }

    @Override
    public void glTexStorage2D(int target, int levels, int internalformat, int width, int height) {

        GLES30.glTexStorage2D(target, levels, internalformat, width, height);
    }

    @Override
    public void glTexStorage3D(int target, int levels, int internalformat, int width, int height, int depth) {

        GLES30.glTexStorage3D(target, levels, internalformat, width, height, depth);
    }

    @Override
    public void glGetInternalformativ(int target, int internalformat, int pname, int bufSize, int[] params,
                                      int offset) {

        GLES30.glGetInternalformativ(target, internalformat, pname, bufSize, params, offset);
    }

    @Override
    public void glGetInternalformativ(int target, int internalformat, int pname, int bufSize,
                                      java.nio.IntBuffer params) {

        GLES30.glGetInternalformativ(target, internalformat, pname, bufSize, params);
    }

    @Override
    public boolean glIsFramebuffer(int handler) {

        return GLES20.glIsFramebuffer(handler);
    }

    @Override
    public boolean glIsTexture(int handler) {
        return GLES20.glIsTexture(handler);
    }

    // extra function
    @Override
    public void glClearColorMask(int mask, float red, float green, float blue, float alpha) {
        GLES20.glClearColor(red, green, blue, alpha);
        GLES20.glClear(mask);
    }

    @Override
    public void glViewport(int x, int y, int width, int height) {
        GLES20.glViewport(x, y, width, height);
    }

    @Override
    public boolean supportsExtension(String extension) {

        if (extensions == null)
            extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        return extensions.contains(extension);
    }

    @Override
    public boolean supportsRenderer(String renderer) {

        if (renderers == null)
            renderers = GLES20.glGetString(GLES20.GL_RENDERER);
        return renderers.contains(renderer);
    }

    @Override
    public String glVersion() {
        return GLES20.glGetString(GLES20.GL_VERSION);
    }

    @Override
    public boolean limitGLESContext() {

        if (renderers == null) {
            renderers = GLES20.glGetString(GLES20.GL_RENDERER);
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
                GLES20.glGetFloatv(0x84FF, buffer);
                maxAnisotropicFilterLevel = buffer.get(0);
            } else
                maxAnisotropicFilterLevel = 1;
        }
        return maxAnisotropicFilterLevel;
    }

    @Override
    public int getMaxTextureSize() {

        if (maxTextureSize < 64) {
            GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, ints, 0);
            maxTextureSize = ints[0];
        }
        return maxTextureSize;
    }

    @Override
    public int getMaxTextureUnit() {

        if (maxTextureUnit <= 0) {
            GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_IMAGE_UNITS, ints, 0);
            maxTextureUnit = ints[0];
        }
        return maxTextureUnit;
    }

    @Override
    public int[] compileShaderProgram(String source, String prefix) throws IllegalArgumentException {
        final int[] handlers = new int[3];
        try {
            if (!source.contains("<break>"))
                throw new RuntimeException("Source is error, hasn't <break>");
            String[] so = source.split("<break>");
            handlers[0] = GLES20.glCreateProgram();
            if (handlers[0] == 0)
                throw new RuntimeException("Failed create Shader Program");
            handlers[1] = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
            if (handlers[1] == 0)
                throw new RuntimeException("Failed create Vertex shader");
            GLES20.glShaderSource(handlers[1], shaderHeader + prefix + so[0]);
            GLES20.glCompileShader(handlers[1]);
            GLES20.glGetShaderiv(handlers[1], GLES20.GL_COMPILE_STATUS, ints, 0);
            if (ints[0] == 0)
                throw new RuntimeException(GLES20.glGetShaderInfoLog(handlers[1]));
            handlers[2] = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
            if (handlers[2] == 0)
                throw new RuntimeException("Failed create Fragment shader");
            GLES20.glShaderSource(handlers[2], shaderHeader + prefix + so[1]);
            GLES20.glCompileShader(handlers[2]);
            GLES20.glGetShaderiv(handlers[2], GLES20.GL_COMPILE_STATUS, ints, 0);
            if (ints[0] == 0)
                throw new RuntimeException(GLES20.glGetShaderInfoLog(handlers[2]));
            GLES20.glAttachShader(handlers[0], handlers[1]);
            GLES20.glAttachShader(handlers[0], handlers[2]);
            GLES20.glLinkProgram(handlers[0]);
            GLES20.glGetProgramiv(handlers[0], GLES20.GL_LINK_STATUS, ints, 0);
            if (ints[0] == 0)
                throw new RuntimeException(GLES20.glGetProgramInfoLog(handlers[0]));
            shaderPrograms.add(handlers);
        } catch (RuntimeException e) {
            Arrays.fill(handlers, -1);
            throw new IllegalArgumentException("Shader program compiling, " + e);
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
        if (handlers[0] == -1)
            return;
        GLES20.glUseProgram(0);
        GLES20.glDeleteProgram(handlers[0]);
        GLES20.glDeleteShader(handlers[1]);
        GLES20.glDeleteShader(handlers[2]);
        shaderPrograms.removeValue(handlers, true);
        Arrays.fill(handlers, -1);
    }

    // mesh id generated and binded Vao instead
    @Override
    public int[] genMesh(final int max_v_data, final boolean v_static, final int max_i_data, final boolean i_static) {
        final int[] handlers = new int[3];
        try {
            // vao id
            GLES30.glGenVertexArrays(1, handlers, 0);
            if (handlers[0] <= 0) {
                throw new RuntimeException("Failed create vao id");
            }
            // buffers ids
            GLES20.glGenBuffers(2, handlers, 1);
            if (handlers[1] <= 0 || handlers[2] <= 0) {
                throw new RuntimeException("Failed create buffer data");
            }
            // binding vao
            GLES30.glBindVertexArray(handlers[0]);
            // binding vertex
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, handlers[1]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, max_v_data, null,
                    v_static ? GLES20.GL_STATIC_DRAW : GLES20.GL_DYNAMIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            // binding indices
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, handlers[2]);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, max_i_data, null,
                    i_static ? GLES20.GL_STATIC_DRAW : GLES20.GL_DYNAMIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

            meshes.add(handlers);
        } catch (RuntimeException e) {
            Arrays.fill(handlers, -1);
            throw new IllegalStateException("failed generate mesh data \n" + e);
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
            GLES30.glBindVertexArray(0);
            GLES30.glDeleteVertexArrays(1, handlers, 0);
            GLES20.glDeleteBuffers(2, handlers, 1);
            meshes.removeValue(handlers, true);
        } catch (RuntimeException e) {
            Log.e(TAG, "Vertex data delete \n" + e);
        }
        Arrays.fill(handlers, -1);
    }

    @Override
    public void capabilitySwitch(final boolean enable, final int cap) {
        if (enabledCaps.contains(cap) != enable) {
            if (enable) {
                GLES20.glEnable(cap);
                enabledCaps.add(cap);
            } else {
                GLES20.glDisable(cap);
                enabledCaps.removeValue(cap);
            }
        }
    }

    @Override
    public void setDepthMask(final boolean depthMask) {
        if (this.depthMask != depthMask)
            GLES20.glDepthMask(depthMask);
        
    }

    @Override
    public void setDepthTest(final int depthFunction, final float depthRangeNear, final float depthRangeFar) {
        final boolean enable = depthFunction != 0;
        capabilitySwitch(enable, GLES20.GL_DEPTH_TEST);
        if (enable && (depthFunc != depthFunction || this.depthRangeNear != depthRangeNear
                || this.depthRangeFar != depthRangeFar)) {
            if (depthFunc != depthFunction)
                GLES20.glDepthFunc(depthFunc = depthFunction);
            if (this.depthRangeNear != depthRangeNear || this.depthRangeFar != depthRangeFar)
                GLES20.glDepthRangef(this.depthRangeNear = depthRangeNear, this.depthRangeFar = depthRangeFar);
        }
    }

    @Override
    public void setBlending(final boolean enabled, final int sFactor, final int dFactor) {
        capabilitySwitch(enabled, GLES20.GL_BLEND);
        if (enabled && (this.blendSFactor != sFactor || this.blendDFactor != dFactor)) {
            GLES20.glBlendFunc(sFactor, dFactor);
            this.blendSFactor = sFactor;
            this.blendDFactor = dFactor;
        }
    }

    @Override
    public void validateAll() {
        shaderPrograms.clear();
        meshes.clear();
        // reset chaced value cause validate
        enabledCaps.clear();
        this.depthMask = false;
        this.depthFunc = GLES20.GL_LEQUAL;
        this.depthRangeNear = 0;
        this.depthRangeFar = 1;
        this.blendSFactor = GLES20.GL_ONE;
        this.blendDFactor = GLES20.GL_ZERO;
        this.cullFace = GLES20.GL_BACK;
        // in future directly regenerate here to efficiently

        Texture.invalidateAllTextures();
        Cubemap.invalidateAllCubemaps();
        TextureArray.invalidateAllTextureArrays();
        GLFrameBuffer.invalidateAllFrameBuffers();
    }

    @Override
    public void clear() {
        GLES20.glUseProgram(0);
        for (final int[] handler : shaderPrograms) {
            GLES20.glDeleteProgram(handler[0]);
            GLES20.glDeleteShader(handler[1]);
            GLES20.glDeleteShader(handler[2]);
        }
        shaderPrograms.clear();
        GLES30.glBindVertexArray(0);
        for (final int[] handler : meshes) {
            GLES30.glDeleteVertexArrays(1, handler, 0);
            GLES20.glDeleteBuffers(2, handler, 1);
        }
        meshes.clear();
        enabledCaps.clear();

        Texture.clearAllTextures();
        Cubemap.clearAllCubemaps();
        TextureArray.clearAllTextureArrays();
        GLFrameBuffer.clearAllFrameBuffers();
    }

    @Override
    public String getLog() {
        String caps = "";

        return caps;
    }
}
