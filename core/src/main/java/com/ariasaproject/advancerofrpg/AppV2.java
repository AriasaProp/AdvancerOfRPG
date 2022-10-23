package com.ariasaproject.advancerofrpg;

import com.ariasaproject.advancerofrpg.graphics.Graphics;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.utils.BufferUtils;

import java.util.Random;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ByteOrder;

public class AppV2 {

    public AppV2() {}
    final String shaderSrc = "layout(location = 0) in vec4 a_position;\n"+
        "void main() {\n"+
        "  gl_Position = a_position;\n"+
        "}\n"+
        "<break>\n"+
				"layout(location = 0) out vec4 gl_FragColor;\n"+
        "void main() {\n"+
        "  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);\n"+
        "}\n";
        
    public float r = 0, g = 0, b = 0;
    int shaderHandlers;
    Buffer triangleBuff;
    int triBuff;
    public void create() {
				triangleBuff = BufferUtils.newDisposableByteBuffer(24);// 6 floats
				BufferUtils.copy(new float[]{.0f, .5f, -.5f, -.5f, .5f, -.5f}, 0, triangleBuff, 6);
    	  TGF tg = GraphFunc.tgf;
    	  shaderHandlers = tg.compileShaderProgram(shaderSrc, "");
    		resume();
    }
    public void resize(int width, int height) {
    	  TGF tg = GraphFunc.tgf;
    		tg.glViewport(0, 0, width, height);
    }
    public void resume() {
    	  TGF tg = GraphFunc.tgf;
    		if (tg.validShaderProgram(shaderHandlers)) {
    	  		shaderHandlers = tg.compileShaderProgram(shaderSrc, "");
    		}
    		triBuff = tg.glGenBuffer();
    		tg.glBindBuffer(TGF.GL_ARRAY_BUFFER, triBuff);
    		tg.glBufferData(TGF.GL_ARRAY_BUFFER, 24, triangleBuff, TGF.GL_STATIC_DRAW);
				
				tg.glUseProgram(shaderHandlers);
				tg.glVertexAttribPointer(0, 2, TGF.GL_FLOAT, false, 0 , 0);
    		tg.glEnableVertexAttribArray(0);
    }
		Random rand = new Random();
    public void render(float delta) {
    		if (GraphFunc.app.getInput().justTouched()) {
    				r = rand.nextFloat();
    				g = rand.nextFloat();
    				b = rand.nextFloat();
    		}
    	  TGF tg = GraphFunc.tgf;
				tg.glClearColorMask(TGF.GL_COLOR_BUFFER_BIT|TGF.GL_DEPTH_BUFFER_BIT|TGF.GL_STENCIL_BUFFER_BIT, r, g, b, 1);
    		tg.glDrawArrays(TGF.GL_TRIANGLES, 0, 3);
				
    }

    public void pause() {
    	  TGF tg = GraphFunc.tgf;
    		tg.glDisableVertexAttribArray(0);
				tg.glUseProgram(0);
				
    		tg.glBindBuffer(TGF.GL_ARRAY_BUFFER, 0);
    		tg.glDeleteBuffer(triBuff);
    }

    public void destroy() {
  	  	TGF tg = GraphFunc.tgf;
  	  	tg.destroyShaderProgram(shaderHandlers);
    		BufferUtils.freeMemory(triangleBuff);
    }
}