package com.ariasaproject.advancerofrpg.graphics.glutils;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.math.Matrix3;
import com.ariasaproject.advancerofrpg.math.Matrix4;
import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.math.Vector3;
import com.ariasaproject.advancerofrpg.utils.BufferUtils;
import com.ariasaproject.advancerofrpg.utils.Disposable;
import com.ariasaproject.advancerofrpg.utils.ObjectIntMap;

public class ShaderProgram implements Disposable {
	static final IntBuffer params = BufferUtils.newIntBuffer(1);
	static final IntBuffer type = BufferUtils.newIntBuffer(4);
	
	private final ObjectIntMap<String> uniforms = new ObjectIntMap<String>();
	private final ObjectIntMap<String> uniformTypes = new ObjectIntMap<String>();
	private final ObjectIntMap<String> uniformSizes = new ObjectIntMap<String>();
	private final ObjectIntMap<String> attributes = new ObjectIntMap<String>();
	private final ObjectIntMap<String> attributeTypes = new ObjectIntMap<String>();
	private final ObjectIntMap<String> attributeSizes = new ObjectIntMap<String>();
	private final String shaderSource, prefix;
	private int[] handlers;
	private String[] uniformNames;
	private String[] attributeNames;

	public ShaderProgram(FileHandle fileShader) {
		this(fileShader.readString(), "");
	}

	public ShaderProgram(FileHandle fileShader, String prefix) {
		this(fileShader.readString(), prefix);
	}

	public ShaderProgram(String source, String prefix) {
		this.shaderSource = source;
		this.prefix = prefix;
		final TGF tgf = GraphFunc.tgf;
		handlers = tgf.compileShaderProgram(source, prefix);
		//prepare all attribute variable name in this shader
		tgf.glGetProgramiv(handlers[0], TGF.GL_ACTIVE_ATTRIBUTES, params);
		int numAttributes = params.get(0);
		attributeNames = new String[numAttributes];
		for (int i = 0; i < numAttributes; i++) {
			params.put(0, 1);
			String name = tgf.glGetActiveAttrib(handlers[0], i, params, type);
			int location = tgf.glGetAttribLocation(handlers[0], name);
			attributes.put(name, location);
			attributeTypes.put(name, type.get(0));
			attributeSizes.put(name, params.get(0));
			attributeNames[i] = name;
		}
		//prepare all uniform variable name in this shader
		tgf.glGetProgramiv(handlers[0], TGF.GL_ACTIVE_UNIFORMS, params);
		int numUniforms = params.get(0);
		uniformNames = new String[numUniforms];
		for (int i = 0; i < numUniforms; i++) {
			params.put(0, 1);
			String name = tgf.glGetActiveUniform(handlers[0], i, params, type);
			int location = tgf.glGetUniformLocation(handlers[0], name);
			uniforms.put(name, location);
			uniformTypes.put(name, type.get(0));
			uniformSizes.put(name, params.get(0));
			uniformNames[i] = name;
		}
	}

	private int fetchAttributeLocation(String name) {
		TGF tgf = GraphFunc.tgf;
		int location;
		if ((location = attributes.get(name, -2)) == -2) {
			location = tgf.glGetAttribLocation(handlers[0], name);
			attributes.put(name, location);
		}
		return location;
	}

	private int fetchUniformLocation(String name) {
		return fetchUniformLocation(name, true);
	}

	public int fetchUniformLocation(String name, boolean pedantic) {
		TGF tgf = GraphFunc.tgf;
		int location;
		if ((location = uniforms.get(name, -2)) == -2) {
			location = tgf.glGetUniformLocation(handlers[0], name);
			if (location == -1 && pedantic) {
				throw new IllegalArgumentException("no uniform with name '" + name + "' in shader");
			}
			uniforms.put(name, location);
		}
		return location;
	}

	public void setUniformi(String name, int value) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		int location = fetchUniformLocation(name);
		tgf.glUniform1i(location, value);
	}

	public void setUniformi(int location, int value) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glUniform1i(location, value);
	}

	public void setUniformiv(int location, int[] value, int offset, int count) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glUniform1iv(location, count, value, offset);
	}

	public void setUniformiv(String name, int[] value, int offset, int count) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		int location = fetchUniformLocation(name);
		tgf.glUniform1iv(location, count, value, offset);
	}

	public void setUniformi(String name, int value1, int value2) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		int location = fetchUniformLocation(name);
		tgf.glUniform2i(location, value1, value2);
	}

	public void setUniformi(int location, int value1, int value2) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glUniform2i(location, value1, value2);
	}

	public void setUniformi(String name, int value1, int value2, int value3) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		int location = fetchUniformLocation(name);
		tgf.glUniform3i(location, value1, value2, value3);
	}

	public void setUniformi(int location, int value1, int value2, int value3) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glUniform3i(location, value1, value2, value3);
	}

	public void setUniformi(String name, int value1, int value2, int value3, int value4) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		int location = fetchUniformLocation(name);
		tgf.glUniform4i(location, value1, value2, value3, value4);
	}

	public void setUniformi(int location, int value1, int value2, int value3, int value4) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glUniform4i(location, value1, value2, value3, value4);
	}

	public void setUniformf(String name, float value) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		int location = fetchUniformLocation(name);
		tgf.glUniform1f(location, value);
	}

	public void setUniformf(int location, float value) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glUniform1f(location, value);
	}

	public void setUniformf(String name, float value1, float value2) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		int location = fetchUniformLocation(name);
		tgf.glUniform2f(location, value1, value2);
	}

	public void setUniformf(int location, float value1, float value2) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glUniform2f(location, value1, value2);
	}

	public void setUniformf(String name, float value1, float value2, float value3) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		int location = fetchUniformLocation(name);
		tgf.glUniform3f(location, value1, value2, value3);
	}

	public void setUniformf(int location, float value1, float value2, float value3) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glUniform3f(location, value1, value2, value3);
	}

	public void setUniformf(String name, float value1, float value2, float value3, float value4) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		int location = fetchUniformLocation(name);
		tgf.glUniform4f(location, value1, value2, value3, value4);
	}

	public void setUniformf(int location, float value1, float value2, float value3, float value4) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glUniform4f(location, value1, value2, value3, value4);
	}

	public void setUniform1fv(String name, float[] values, int offset, int length) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		int location = fetchUniformLocation(name);
		tgf.glUniform1fv(location, length, values, offset);
	}

	public void setUniform1fv(int location, float[] values, int offset, int length) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glUniform1fv(location, length, values, offset);
	}

	public void setUniform2fv(String name, float[] values, int offset, int vecCount) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		int location = fetchUniformLocation(name);
		tgf.glUniform2fv(location, vecCount, values, offset);
	}

	public void setUniform2fv(int location, float[] values, int offset, int vecCount) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glUniform2fv(location, vecCount, values, offset);
	}

	public void setUniform3fv(String name, float[] values, int offset, int vecCount) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glUniform3fv(fetchUniformLocation(name), vecCount, values, offset);
	}

	public void setUniform3fv(int location, float[] values, int offset, int vecCount) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glUniform3fv(location, vecCount, values, offset);
	}

	public void setUniform4fv(String name, float[] values, int offset, int vecCount) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glUniform4fv(fetchUniformLocation(name), vecCount, values, offset);
	}

	public void setUniform4fv(int location, float[] values, int offset, int vecCount) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glUniform4fv(location, vecCount, values, offset);
	}

	public void setUniformMatrix(String name, Matrix4 matrix) {
		setUniformMatrix(name, matrix, false);
	}

	public void setUniformMatrix(String name, Matrix4 matrix, boolean transpose) {
		setUniformMatrix(fetchUniformLocation(name), matrix, transpose);
	}

	public void setUniformMatrix(int location, Matrix4 matrix) {
		setUniformMatrix(location, matrix, false);
	}

	public void setUniformMatrix(int location, Matrix4 matrix, boolean transpose) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glUniformMatrix4fv(location, 1, transpose, matrix.val, 0);
	}

	public void setUniformMatrix(String name, Matrix3 matrix) {
		setUniformMatrix(name, matrix, false);
	}

	public void setUniformMatrix(String name, Matrix3 matrix, boolean transpose) {
		setUniformMatrix(fetchUniformLocation(name), matrix, transpose);
	}

	public void setUniformMatrix(int location, Matrix3 matrix) {
		setUniformMatrix(location, matrix, false);
	}

	public void setUniformMatrix(int location, Matrix3 matrix, boolean transpose) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glUniformMatrix3fv(location, 1, transpose, matrix.val, 0);
	}

	public void setUniformMatrix3fv(String name, FloatBuffer buffer, int count, boolean transpose) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		buffer.position(0);
		int location = fetchUniformLocation(name);
		tgf.glUniformMatrix3fv(location, count, transpose, buffer);
	}
	public void setUniformMatrix3fv(String name, float[] values, int offset, int matCount) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		int location = fetchUniformLocation(name);
		tgf.glUniformMatrix3fv(location, matCount, false, values, offset);
	}

	public void setUniformMatrix4fv(String name, FloatBuffer buffer, int count, boolean transpose) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		buffer.position(0);
		int location = fetchUniformLocation(name);
		tgf.glUniformMatrix4fv(location, count, transpose, buffer);
	}

	public void setUniformMatrix4fv(int location, float[] values, int offset, int matCount) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glUniformMatrix4fv(location, matCount, false, values, offset);
	}

	public void setUniformMatrix4fv(String name, float[] values, int offset, int matCount) {
		setUniformMatrix4fv(fetchUniformLocation(name), values, offset, matCount);
	}

	public void setUniformf(String name, Vector2 values) {
		setUniformf(name, values.x, values.y);
	}

	public void setUniformf(int location, Vector2 values) {
		setUniformf(location, values.x, values.y);
	}

	public void setUniformf(String name, Vector3 values) {
		setUniformf(name, values.x, values.y, values.z);
	}

	public void setUniformf(int location, Vector3 values) {
		setUniformf(location, values.x, values.y, values.z);
	}

	public void setUniformf(String name, Color values) {
		setUniformf(name, values.r, values.g, values.b, values.a);
	}

	public void setUniformf(int location, Color values) {
		setUniformf(location, values.r, values.g, values.b, values.a);
	}

	public void setVertexAttribute(String name, int size, int type, boolean normalize, int stride, Buffer buffer) {
		checkManaged();
		int location = fetchAttributeLocation(name);
		if (location == -1)
			return;
		GraphFunc.tgf.glVertexAttribPointer(location, size, type, normalize, stride, buffer);
	}

	public void setVertexAttribute(int location, int size, int type, boolean normalize, int stride, Buffer buffer) {
		checkManaged();
		GraphFunc.tgf.glVertexAttribPointer(location, size, type, normalize, stride, buffer);
	}

	public void setVertexAttribute(String name, int size, int type, boolean normalize, int stride, int offset) {
		checkManaged();
		int location = fetchAttributeLocation(name);
		if (location == -1)
			return;
		GraphFunc.tgf.glVertexAttribPointer(location, size, type, normalize, stride, offset);
	}

	public void setVertexAttribute(int location, int size, int type, boolean normalize, int stride, int offset) {
		checkManaged();
		GraphFunc.tgf.glVertexAttribPointer(location, size, type, normalize, stride, offset);
	}
	private static int currentUsedProgram = -1;

	public void bind() {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glUseProgram(handlers[0]);
		currentUsedProgram = handlers[0];
	}

	private void checkManaged() {
		TGF tgf = GraphFunc.tgf;
		if (!tgf.validShaderProgram(handlers))
			handlers = tgf.compileShaderProgram(shaderSource, prefix);
	}

	@Override
	public void dispose() {
		TGF tgf = GraphFunc.tgf;
		tgf.destroyShaderProgram(handlers);
	}

	public void disableVertexAttribute(String name) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		int location = fetchAttributeLocation(name);
		if (location == -1)
			return;
		tgf.glDisableVertexAttribArray(location);
	}

	public void disableVertexAttribute(int location) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glDisableVertexAttribArray(location);
	}

	public void enableVertexAttribute(String name) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		int location = fetchAttributeLocation(name);
		if (location == -1)
			return;
		tgf.glEnableVertexAttribArray(location);
	}

	public void enableVertexAttribute(int location) {
		TGF tgf = GraphFunc.tgf;
		checkManaged();
		tgf.glEnableVertexAttribArray(location);
	}

	public void setAttributef(String name, float value1, float value2, float value3, float value4) {
		TGF tgf = GraphFunc.tgf;
		int location = fetchAttributeLocation(name);
		tgf.glVertexAttrib4f(location, value1, value2, value3, value4);
	}

	public boolean hasAttribute(String name) {
		return attributes.containsKey(name);
	}

	public int getAttributeType(String name) {
		return attributeTypes.get(name, 0);
	}

	public int getAttributeLocation(String name) {
		return attributes.get(name, -1);
	}

	public int getAttributeSize(String name) {
		return attributeSizes.get(name, 0);
	}

	public boolean hasUniform(String name) {
		return uniforms.containsKey(name);
	}

	public int getUniformType(String name) {
		return uniformTypes.get(name, 0);
	}

	public int getUniformLocation(String name) {
		return uniforms.get(name, -1);
	}

	public int getUniformSize(String name) {
		return uniformSizes.get(name, 0);
	}

	public String[] getAttributes() {
		return attributeNames;
	}

	public String[] getUniforms() {
		return uniformNames;
	}
}
