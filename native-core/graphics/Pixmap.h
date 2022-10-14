#ifndef Included_Pixmap
#define Included_Pixmap

#include <jni.h>

extern "C" {
struct Pixmap {
public:
    enum Format {
        Format_Alpha,
        Format_Luminance_Alpha,
        Format_RGB888,
        Format_RGBA8888,
        Format_RGB565,
        Format_RGBA4444
    };
    const unsigned int width, height;
    const Format format;
    const unsigned char *pixels;
    bool scale; // true = Linear , false = Nearest

    Pixmap(unsigned int, unsigned int, Format, bool);

    Pixmap(unsigned int, unsigned int, Format, const unsigned char *, bool);

    ~Pixmap();
};

#define Pixmap_M(R, M) JNIEXPORT R JNICALL Java_com_ariasaproject_advancerofrpg_graphics_Pixmap_##M
Pixmap_M(void, initialize)(JNIEnv *, jclass);
Pixmap_M(jobject, loadFromInternalFilePath)(JNIEnv *, jclass, jlongArray, jstring);
Pixmap_M(jobject, load)(JNIEnv *, jclass, jlongArray, jbyteArray, jint, jint);
Pixmap_M(jobject, newPixmap)(JNIEnv *, jclass, jlongArray, jint, jint, jint);
Pixmap_M(void, free)(JNIEnv *, jobject);
Pixmap_M(void, clear)(JNIEnv *, jobject, jint);
Pixmap_M(void, setPixel)(JNIEnv *, jobject, jint, jint, jint);
Pixmap_M(jint, getPixel)(JNIEnv *, jobject, jint, jint);
Pixmap_M(void, drawPixmap)(JNIEnv *, jobject, jlong, jint, jint, jint, jint, jint, jint, jint,
                           jint);
Pixmap_M(void, setScaleType)(JNIEnv *, jobject, jboolean); //true is Linear, false is nearest
Pixmap_M(jstring, getFailureReason)(JNIEnv *, jclass);

}
#endif //Included_Pixmap
