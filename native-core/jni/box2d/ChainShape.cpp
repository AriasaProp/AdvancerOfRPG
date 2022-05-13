#include "ChainShape.h"
#include "Shape.h"
#include "Collision/Shapes/b2ChainShape.h"

ChainShape_M(jlong, newChainShape) (JNIEnv *, jclass)
{
	return (jlong)(new b2ChainShape());
}

ChainShape_M(void, jniCreateLoop) (JNIEnv *env, jobject obj, jfloatArray vals, jint offset, jint numVertices)
{
	jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
	b2Vec2* verticesOut = new b2Vec2[numVertices];
	for( int i = 0; i < numVertices; i++ )
		verticesOut[i] = b2Vec2(data[offset+(i<<1)], data[offset+(i<<1)+1]);
	((b2ChainShape*)env->GetLongField(obj, shapePtr))->CreateLoop( verticesOut, numVertices );
	delete[] verticesOut;
	env->ReleasePrimitiveArrayCritical(vals, data, 0);
}
	
ChainShape_M(void, jniCreateChain) (JNIEnv *env, jobject obj, jfloatArray vals, jint offset, jint numVertices)
{
	jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
	b2Vec2* verticesOut = new b2Vec2[numVertices];
	for( int i = 0; i < numVertices; i++ )
		verticesOut[i] = b2Vec2(data[offset+(i<<1)], data[offset+(i<<1)+1]);
	((b2ChainShape*)env->GetLongField(obj, shapePtr))->CreateChain( verticesOut, numVertices );
	delete[] verticesOut;
	env->ReleasePrimitiveArrayCritical(vals, data, 0);
}
ChainShape_M(void, setPrevVertex) (JNIEnv *env, jobject obj, jfloat x, jfloat y)
{
	((b2ChainShape*)env->GetLongField(obj, shapePtr))->SetPrevVertex(b2Vec2(x, y));
}
ChainShape_M(void, setNextVertex) (JNIEnv *env, jobject obj, jfloat x, jfloat y)
{
	((b2ChainShape*)env->GetLongField(obj, shapePtr))->SetNextVertex(b2Vec2(x, y));
}
ChainShape_M(jint, getVertexCount) (JNIEnv *env, jobject obj)
{
	return ((b2ChainShape*)env->GetLongField(obj, shapePtr))->GetVertexCount();
}

ChainShape_M(void, jniGetVertex) (JNIEnv *env, jobject obj, jint index, jfloatArray vals)
{
	jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
	const b2Vec2 v = ((b2ChainShape*)env->GetLongField(obj, shapePtr))->GetVertex( index );
	data[0] = v.x;
	data[1] = v.y;
	env->ReleasePrimitiveArrayCritical(vals, data, 0);
}

