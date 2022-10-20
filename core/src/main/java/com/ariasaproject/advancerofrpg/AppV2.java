package com.ariasaproject.advancerofrpg;

import com.ariasaproject.advancerofrpg.graphics.Graphics;
import com.ariasaproject.advancerofrpg.graphics.TGF;
//import com.ariasaproject.advancerofrpg.utils.BufferUtils;

import java.util.Random;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ByteOrder;

public class AppV2 {

    public AppV2() { }
    /*
    final String shaderSrc = "precision MED float;\n"+
        "void main() {\n"+
        "  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);\n"+
        "}\n"+
        "<break>\n"+
        "in vec4 a_position;\n"+
        "void main() {\n"+
        "  gl_Position = a_position;\n"+
        "}\n";
        */
    int[] shaderHandlers;
    FloatBuffer triangleBuff;
    int a_pos_pointer;
    public void create() {
				ByteBuffer bb = ByteBuffer.allocateDirect(6 * 4);
        bb.order(ByteOrder.nativeOrder());
        triangleBuff = bb.asFloatBuffer();
        triangleBuff.put(new float[]{0.0f, 300.0f, -300.0f, -300.0f, 300.0f, -300.0f});
        triangleBuff.position(0);
    	  TGF tg = GraphFunc.tgf;
    	  //shaderHandlers = tg.compileShaderProgram(shaderSrc, "");
				a_pos_pointer = tg.glGetAttribLocation(shaderHandlers[0], "a_position");
    		resume();
    }
    public void resize(int width, int height) {
    	  TGF tg = GraphFunc.tgf;
    		tg.glViewport(0, 0, width, height);
    }
    public void resume() {
    	  TGF tg = GraphFunc.tgf;
    	  /*
    		if (tg.validShaderProgram(shaderHandlers)) {
    			  shaderHandlers = null;
    	  		shaderHandlers = tg.compileShaderProgram(shaderSrc, "");
    		}
    		*/
    }
    public void render(float delta) {
    	  TGF tg = GraphFunc.tgf;
				tg.glClearColorMask(TGF.GL_COLOR_BUFFER_BIT|TGF.GL_DEPTH_BUFFER_BIT|TGF.GL_STENCIL_BUFFER_BIT, 0, 1, 0, 1);
				/*
				tg.glUseProgram(shaderHandlers[0]);
				tg.glVertexAttribPointer(a_pos_pointer, 2, TGF.GL_FLOAT, false, 0 ,triangleBuff);
    		tg.glEnableVertexAttribArray(a_pos_pointer);
    		
    		tg.glDrawArrays(TGF.GL_TRIANGLES, 0, 3);
    		
    		tg.glDisableVertexAttribArray(a_pos_pointer);
				tg.glUseProgram(0);
				*/
    }

    public void pause() {
    }

    public void destroy() {
  	  	TGF tg = GraphFunc.tgf;
    		//tg.destroyShaderProgram(shaderHandlers);
    		shaderHandlers = null;
    }
}