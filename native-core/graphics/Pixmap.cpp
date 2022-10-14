#include "Pixmap.h"
#include <malloc.h>// malloc
#include <stdarg.h>
#include <stddef.h> // ptrdiff_t on osx
#include <stdlib.h>
#include <string.h>
#include <math.h>  // ldexp
#include <stdio.h>
#include <assert.h>
#include <setjmp.h>

#ifdef _MSC_VER
#pragma warning (disable : 4611) // warning C4611: interaction between '_setjmp' and C++ object destruction is non-portable
typedef unsigned short uint16_t;
typedef signed short int16_t;
typedef unsigned int uint32_t;
typedef signed int int32_t;
#define inline __forceinline
#define IGNORED(v)  (void)(v)
#else
#define IGNORED(v)  (void)sizeof(v)
#define _lrotl(x, y)  (((x) << (y)) | ((x) >> (32 - (y))))

#include <stdint.h>

#endif

//private function
#define MAX(a, b) (((a)>(b))?(a):(b))
#define MIN(a, b) (((a)<(b))?(a):(b))

enum {
    reqFormat_default = 0, // only used for req_comp
    reqFormat_grey = 1, reqFormat_grey_alpha = 2, reqFormat_rgb = 3, reqFormat_rgb_alpha = 4
};
//begin initialize stbi decompresser
namespace stbi {

// x86/x64 detection
#if defined(__x86_64__) || defined(_M_X64)
#define X64_TARGET
#elif defined(__i386) || defined(_M_IX86)
#define X86_TARGET
#endif

#if defined(__GNUC__) && (defined(X86_TARGET) || defined(X64_TARGET)) && !defined(__SSE2__)
#define STBI_NO_SIMD
#endif

#if defined(__MINGW32__) && defined(X86_TARGET) && !defined(STBI_MINGW_ENABLE_SSE2) && !defined(STBI_NO_SIMD)
#define STBI_NO_SIMD
#endif

#if !defined(STBI_NO_SIMD) && defined(X86_TARGET)
#define STBI_SSE2

#include <emmintrin.h>

#ifdef _MSC_VER
#if _MSC_VER >= 1400  // not VC6
#include <intrin.h> // __cpuid
    static int cpuid3(void)
    {
        int info[4];
        __cpuid(info,1);
        return info[3];
    }
#else
    static int cpuid3(void)
    {
        int res;
        __asm
        {
            mov eax,1
            cpuid
            mov res,edx
        }
        return res;
    }
#endif

#define STBI_SIMD_ALIGN(type, name) __declspec(align(16)) type name

    static int sse2_available()
    {
        int info3 = cpuid3();
        return ((info3 >> 26) & 1) != 0;
    }
#else // assume GCC-style if not VC++
#define STBI_SIMD_ALIGN(type, name) type name __attribute__((aligned(16)))

    static int sse2_available() {
#if defined(__GNUC__) && (__GNUC__ * 100 + __GNUC_MINOR__) >= 408 // GCC 4.8 or later
                                                                                                                                // GCC 4.8+ has a nice way to do this
	return __builtin_cpu_supports("sse2");
#else
        // portable way to do this, preferably without using GCC inline ASM?
        // just bail for now.
        return 0;
#endif
    }

#endif
#endif

// ARM NEON
#if defined(STBI_NO_SIMD) && defined(STBI_NEON)
#undef STBI_NEON
#endif

#ifdef STBI_NEON
#include <arm_neon.h>
#define STBI_SIMD_ALIGN(type, name) type name __attribute__((aligned(16)))
#endif

#ifndef STBI_SIMD_ALIGN
#define STBI_SIMD_ALIGN(type, name) type name
#endif

    struct stbi_io_callbacks {
        int (*read)(void *user, char *data,
                    int size); // fill 'data' with 'size' bytes.  return number of bytes actually read
        void (*skip)(void *user,
                     int n); // skip the next 'n' bytes, or 'unget' the last -n bytes if negative
        int (*eof)(void *user);     // returns nonzero if we are at end of file/data
    };

    struct context {
        uint32_t img_x, img_y;
        int img_n, img_out_n;

        stbi_io_callbacks io;
        void *io_user_data;

        int read_from_callbacks;
        int buflen;
        unsigned char buffer_start[128];

        unsigned char *img_buffer, *img_buffer_end;
        unsigned char *img_buffer_original, *img_buffer_original_end;
    };

    static void refill_buffer(context *s) {
        int n = (s->io.read)(s->io_user_data, (char *) s->buffer_start, s->buflen);
        if (n == 0) {
            s->read_from_callbacks = 0;
            s->img_buffer = s->buffer_start;
            s->img_buffer_end = s->buffer_start + 1;
            *s->img_buffer = 0;
        } else {
            s->img_buffer = s->buffer_start;
            s->img_buffer_end = s->buffer_start + n;
        }
    }

// initialize a memory-decode context
    static void start_mem(context *s, unsigned char const *buffer, int len) {
        s->io.read = NULL;
        s->read_from_callbacks = 0;
        s->img_buffer = s->img_buffer_original = (unsigned char *) buffer;
        s->img_buffer_end = s->img_buffer_original_end = (unsigned char *) buffer + len;
    }

// initialize a callback-based context
    static void start_callbacks(context *s, stbi_io_callbacks *c, void *user) {
        s->io = *c;
        s->io_user_data = user;
        s->buflen = sizeof(s->buffer_start);
        s->read_from_callbacks = 1;
        s->img_buffer_original = s->buffer_start;
        refill_buffer(s);
        s->img_buffer_original_end = s->img_buffer_end;
    }

    static int stdio_read(void *user, char *data, int size) {
        return (int) fread(data, 1, size, (FILE *) user);
    }

    static void stdio_skip(void *user, int n) {
        fseek((FILE *) user, n, SEEK_CUR);
    }

    static int stdio_eof(void *user) {
        return feof((FILE *) user);
    }

    static stbi_io_callbacks stdio_callbacks = {stdio_read, stdio_skip, stdio_eof};

    static void start_file(context *s, FILE *f) {
        start_callbacks(s, &stdio_callbacks, (void *) f);
    }

//static void stop_file(context *s) { }

    static void rewind(context *s) {
        s->img_buffer = s->img_buffer_original;
        s->img_buffer_end = s->img_buffer_original_end;
    }

    static int jpeg_test(context *s);

    static unsigned char *
    jpeg_load(context *s, int *x, int *y, int *comp, int req_comp);

    static int jpeg_info(context *s, int *x, int *y, int *comp);

    static int png_test(context *s);

    static unsigned char *png_load(context *s, int *x, int *y, int *comp, int req_comp);

    static int png_info(context *s, int *x, int *y, int *comp);

#ifndef STBI_NO_BMP

    static int bmp_test(context *s);

    static unsigned char *bmp_load(context *s, int *x, int *y, int *comp, int req_comp);

    static int bmp_info(context *s, int *x, int *y, int *comp);

#endif

#ifndef STBI_NO_TGA

    static int tga_test(context *s);

    static unsigned char *tga_load(context *s, int *x, int *y, int *comp, int req_comp);

    static int tga_info(context *s, int *x, int *y, int *comp);

#endif

#ifndef STBI_NO_PSD

    static int psd_test(context *s);

    static unsigned char *psd_load(context *s, int *x, int *y, int *comp, int req_comp);

    static int psd_info(context *s, int *x, int *y, int *comp);

#endif

    static int hdr_test(context *s);

    static float *hdr_load(context *s, int *x, int *y, int *comp, int req_comp);

    static int hdr_info(context *s, int *x, int *y, int *comp);

#ifndef STBI_NO_PIC

    static int pic_test(context *s);

    static unsigned char *pic_load(context *s, int *x, int *y, int *comp, int req_comp);

    static int pic_info(context *s, int *x, int *y, int *comp);

#endif

#ifndef STBI_NO_GIF

    static int gif_test(context *s);

    static unsigned char *gif_load(context *s, int *x, int *y, int *comp, int req_comp);

    static int gif_info(context *s, int *x, int *y, int *comp);

#endif

#ifndef STBI_NO_PNM

    static int pnm_test(context *s);

    static unsigned char *pnm_load(context *s, int *x, int *y, int *comp, int req_comp);

    static int pnm_info(context *s, int *x, int *y, int *comp);

#endif

// this is not threadsafe
    static const char *g_failure_reason;

    static int err(const char *x, const char *y) {
        const char r = *x + *" : " + *y;
        g_failure_reason = &r;
        return 0;
    }

    void stbi_image_free(void *retval_from_stbi_load) {
        free(retval_from_stbi_load);
    }

#ifndef STBI_NO_LINEAR

    static float *ldr_to_hdr(unsigned char *data, int x, int y, int comp);

#endif

    static unsigned char *hdr_to_ldr(float *data, int x, int y, int comp);

    static int vertically_flip_on_load = 0;

    void stbi_set_flip_vertically_on_load(int flag_true_if_should_flip) {
        vertically_flip_on_load = flag_true_if_should_flip;
    }

    static unsigned char *
    load_main(context *s, int *x, int *y, int *comp, int req_comp) {
        if (jpeg_test(s))
            return jpeg_load(s, x, y, comp, req_comp);
        if (png_test(s))
            return png_load(s, x, y, comp, req_comp);
#ifndef STBI_NO_BMP
        if (bmp_test(s))
            return bmp_load(s, x, y, comp, req_comp);
#endif
#ifndef STBI_NO_GIF
        if (gif_test(s))
            return gif_load(s, x, y, comp, req_comp);
#endif
#ifndef STBI_NO_PSD
        if (psd_test(s))
            return psd_load(s, x, y, comp, req_comp);
#endif
#ifndef STBI_NO_PIC
        if (pic_test(s))
            return pic_load(s, x, y, comp, req_comp);
#endif
#ifndef STBI_NO_PNM
        if (pnm_test(s))
            return pnm_load(s, x, y, comp, req_comp);
#endif
        if (hdr_test(s)) {
            float *hdr = hdr_load(s, x, y, comp, req_comp);
            return hdr_to_ldr(hdr, *x, *y, req_comp ? req_comp : *comp);
        }

#ifndef STBI_NO_TGA
        // test tga last because it's a crappy test!
        if (tga_test(s))
            return tga_load(s, x, y, comp, req_comp);
#endif

        err("unknown image type", "Image not of any known type, or corrupt");
        return 0;
    }

    static unsigned char *
    load_flip(context *s, int *x, int *y, int *comp, int req_comp) {
        unsigned char *result = load_main(s, x, y, comp, req_comp);

        if (vertically_flip_on_load && result != NULL) {
            int w = *x, h = *y;
            int depth = req_comp ? req_comp : *comp;
            int row, col, z;
            unsigned char temp;

            // @OPTIMIZE: use a bigger temp buffer and memcpy multiple pixels at once
            for (row = 0; row < (h >> 1); row++) {
                for (col = 0; col < w; col++) {
                    for (z = 0; z < depth; z++) {
                        temp = result[(row * w + col) * depth + z];
                        result[(row * w + col) * depth + z] = result[
                                ((h - row - 1) * w + col) * depth + z];
                        result[((h - row - 1) * w + col) * depth + z] = temp;
                    }
                }
            }
        }

        return result;
    }

    static void float_postprocess(float *result, int *x, int *y, int *comp, int req_comp) {
        if (vertically_flip_on_load && result != NULL) {
            int w = *x, h = *y;
            int depth = req_comp ? req_comp : *comp;
            int row, col, z;
            float temp;

            // @OPTIMIZE: use a bigger temp buffer and memcpy multiple pixels at once
            for (row = 0; row < (h >> 1); row++) {
                for (col = 0; col < w; col++) {
                    for (z = 0; z < depth; z++) {
                        temp = result[(row * w + col) * depth + z];
                        result[(row * w + col) * depth + z] = result[
                                ((h - row - 1) * w + col) * depth + z];
                        result[((h - row - 1) * w + col) * depth + z] = temp;
                    }
                }
            }
        }
    }

    static FILE *fopen(char const *filename, char const *mode) {
        FILE *f;
#if defined(_MSC_VER) && _MSC_VER >= 1400
        if (0 != fopen_s(&f, filename, mode)) f=0;
#else
        f = fopen(filename, mode);
#endif
        return f;
    }

    unsigned char *stbi_load_from_file(FILE *f, int *x, int *y, int *comp, int req_comp) {
        unsigned char *result;
        context s;
        start_file(&s, f);
        result = load_flip(&s, x, y, comp, req_comp);
        if (result) {
            // need to 'unget' all the characters in the IO buffer
            fseek(f, -(int) (s.img_buffer_end - s.img_buffer), SEEK_CUR);
        }
        return result;
    }

    unsigned char *stbi_load(char const *filename, int *x, int *y, int *comp, int req_comp) {
        FILE *f = fopen(filename, "rb");
        unsigned char *result;
        if (!f) {
            err("can't fopen", "Unable to open file");
            return NULL;
        }
        result = stbi_load_from_file(f, x, y, comp, req_comp);
        fclose(f);
        return result;
    }

    unsigned char *
    stbi_load_from_memory(unsigned char const *buffer, int len, int *x, int *y, int *comp,
                          int req_comp) {
        context s;
        start_mem(&s, buffer, len);
        return load_flip(&s, x, y, comp, req_comp);
    }

    unsigned char *
    stbi_load_from_callbacks(stbi_io_callbacks const *clbk, void *user, int *x, int *y, int *comp,
                             int req_comp) {
        context s;
        start_callbacks(&s, (stbi_io_callbacks *) clbk, user);
        return load_flip(&s, x, y, comp, req_comp);
    }

#ifndef STBI_NO_LINEAR

    static float *loadf_main(context *s, int *x, int *y, int *comp, int req_comp) {
        unsigned char *data;
        if (hdr_test(s)) {
            float *hdr_data = hdr_load(s, x, y, comp, req_comp);
            if (hdr_data)
                float_postprocess(hdr_data, x, y, comp, req_comp);
            return hdr_data;
        }

        data = load_flip(s, x, y, comp, req_comp);
        if (data)
            return ldr_to_hdr(data, *x, *y, req_comp ? req_comp : *comp);
        err("unknown image type", "Image not of any known type, or corrupt");
        return 0;
    }

    float *stbi_loadf_from_memory(unsigned char const *buffer, int len, int *x, int *y, int *comp,
                                  int req_comp) {
        context s;
        start_mem(&s, buffer, len);
        return loadf_main(&s, x, y, comp, req_comp);
    }

    float *
    stbi_loadf_from_callbacks(stbi_io_callbacks const *clbk, void *user, int *x, int *y, int *comp,
                              int req_comp) {
        context s;
        start_callbacks(&s, (stbi_io_callbacks *) clbk, user);
        return loadf_main(&s, x, y, comp, req_comp);
    }

    float *stbi_loadf_from_file(FILE *f, int *x, int *y, int *comp, int req_comp) {
        context s;
        start_file(&s, f);
        return loadf_main(&s, x, y, comp, req_comp);
    }

    float *stbi_loadf(char const *filename, int *x, int *y, int *comp, int req_comp) {
        float *result;
        FILE *f = fopen(filename, "rb");
        if (!f) {
            err("can't fopen", "Unable to open file");
            return 0;
        }
        result = stbi_loadf_from_file(f, x, y, comp, req_comp);
        fclose(f);
        return result;
    }

#endif // !STBI_NO_LINEAR

    int stbi_is_hdr_from_memory(unsigned char const *buffer, int len) {
        context s;
        start_mem(&s, buffer, len);
        return hdr_test(&s);
    }

    int stbi_is_hdr_from_file(FILE *f) {
        context s;
        start_file(&s, f);
        return hdr_test(&s);
    }

    int stbi_is_hdr(char const *filename) {
        FILE *f = fopen(filename, "rb");
        int result = 0;
        if (f) {
            result = stbi_is_hdr_from_file(f);
            fclose(f);
        }
        return result;
    }

    int stbi_is_hdr_from_callbacks(stbi_io_callbacks const *clbk, void *user) {
        context s;
        start_callbacks(&s, (stbi_io_callbacks *) clbk, user);
        return hdr_test(&s);
    }

    enum {
        SCAN_load = 0, SCAN_type, SCAN_header
    };

    inline static unsigned char get8(context *s) {
        if (s->img_buffer < s->img_buffer_end)
            return *s->img_buffer++;
        if (s->read_from_callbacks) {
            refill_buffer(s);
            return *s->img_buffer++;
        }
        return 0;
    }

    inline static int at_eof(context *s) {
        if (s->io.read) {
            if (!(s->io.eof)(s->io_user_data))
                return 0;
            // if feof() is true, check if buffer = end
            // special case: we've only got the special 0 character at the end
            if (s->read_from_callbacks == 0)
                return 1;
        }

        return s->img_buffer >= s->img_buffer_end;
    }

    static void skip(context *s, int n) {
        if (n < 0) {
            s->img_buffer = s->img_buffer_end;
            return;
        }
        if (s->io.read) {
            int blen = (int) (s->img_buffer_end - s->img_buffer);
            if (blen < n) {
                s->img_buffer = s->img_buffer_end;
                (s->io.skip)(s->io_user_data, n - blen);
                return;
            }
        }
        s->img_buffer += n;
    }

    static int getn(context *s, unsigned char *buffer, int n) {
        if (s->io.read) {
            int blen = (int) (s->img_buffer_end - s->img_buffer);
            if (blen < n) {
                int res, count;

                memcpy(buffer, s->img_buffer, blen);

                count = (s->io.read)(s->io_user_data, (char *) buffer + blen, n - blen);
                res = (count == (n - blen));
                s->img_buffer = s->img_buffer_end;
                return res;
            }
        }

        if (s->img_buffer + n <= s->img_buffer_end) {
            memcpy(buffer, s->img_buffer, n);
            s->img_buffer += n;
            return 1;
        } else
            return 0;
    }

    static int get16be(context *s) {
        int z = get8(s);
        return (z << 8) + get8(s);
    }

    static uint32_t get32be(context *s) {
        uint32_t z = get16be(s);
        return (z << 16) + get16be(s);
    }

#if !defined(STBI_NO_BMP) || !defined(STBI_NO_TGA) || !defined(STBI_NO_GIF)

    static int get16le(context *s) {
        int z = get8(s);
        return z + (get8(s) << 8);
    }

#ifndef STBI_NO_BMP

    static uint32_t get32le(context *s) {
        uint32_t z = get16le(s);
        return z + (get16le(s) << 16);
    }

#endif
#endif

#define BYTECAST(x)  ((unsigned char) ((x) & 255))

    static unsigned char compute_y(int r, int g, int b) {
        return (unsigned char) (((r * 77) + (g * 150) + (29 * b)) >> 8);
    }

    static unsigned char *
    convert_format(unsigned char *data, int img_n, int req_comp, unsigned int x,
                   unsigned int y) {
        int i, j;
        unsigned char *good;

        if (req_comp == img_n)
            return data;
        assert(req_comp >= 1 && req_comp <= 4);

        good = (unsigned char *) malloc(req_comp * x * y);
        if (good == NULL) {
            free(data);
            err("outofmem", "Out of memory");
            return 0;
        }

        for (j = 0; j < (int) y; ++j) {
            unsigned char *src = data + j * x * img_n;
            unsigned char *dest = good + j * x * req_comp;

#define COMBO(a, b) ((a)*8+(b))
#define CASE(a, b) case COMBO(a,b): for(i=x-1; i >= 0; --i, src += a, dest += b)
            switch (COMBO(img_n, req_comp)) {
                CASE(1, 2) dest[0] = src[0], dest[1] = 255;
                    break;
                CASE(1, 3) dest[0] = dest[1] = dest[2] = src[0];
                    break;
                CASE(1, 4) dest[0] = dest[1] = dest[2] = src[0], dest[3] = 255;
                    break;
                CASE(2, 1) dest[0] = src[0];
                    break;
                CASE(2, 3) dest[0] = dest[1] = dest[2] = src[0];
                    break;
                CASE(2, 4) dest[0] = dest[1] = dest[2] = src[0], dest[3] = src[1];
                    break;
                CASE(3, 4) dest[0] = src[0], dest[1] = src[1], dest[2] = src[2], dest[3] = 255;
                    break;
                CASE(3, 1) dest[0] = compute_y(src[0], src[1], src[2]);
                    break;
                CASE(3, 2) dest[0] = compute_y(src[0], src[1], src[2]), dest[1] = 255;
                    break;
                CASE(4, 1) dest[0] = compute_y(src[0], src[1], src[2]);
                    break;
                CASE(4, 2) dest[0] = compute_y(src[0], src[1], src[2]), dest[1] = src[3];
                    break;
                CASE(4, 3) dest[0] = src[0], dest[1] = src[1], dest[2] = src[2];
                    break;
                default:
                    assert(0);
            }
#undef CASE
#undef COMBO
        }

        free(data);
        return good;
    }

#ifndef STBI_NO_LINEAR

    static float *ldr_to_hdr(unsigned char *data, int x, int y, int comp) {
        int i, k, n;
        float *output = (float *) malloc(x * y * comp * sizeof(float));
        if (output == NULL) {
            free(data);
            err("outofmem", "Out of memory");
            return 0;
        }
        // compute number of non-alpha components
        if (comp & 1)
            n = comp;
        else
            n = comp - 1;
        for (i = 0; i < x * y; ++i) {
            for (k = 0; k < n; ++k) {
                //ldr -> hdr gamma is raised by 2.2f, and scale is equal
                output[i * comp + k] = (float) (pow(data[i * comp + k] / 255.0f, 2.2f));
            }
            if (k < comp)
                output[i * comp + k] = data[i * comp + k] / 255.0f;
        }
        free(data);
        return output;
    }

#endif

#define float2int(x)   ((int) (x))

    static unsigned char *hdr_to_ldr(float *data, int x, int y, int comp) {
        int i, k, n;
        unsigned char *output = (unsigned char *) malloc(x * y * comp);
        if (output == NULL) {
            free(data);
            err("outofmem", "Out of memory");
            return 0;
        }
        // compute number of non-alpha components
        if (comp & 1)
            n = comp;
        else
            n = comp - 1;
        for (i = 0; i < x * y; ++i) {
            for (k = 0; k < n; ++k) {
                //hdr -> ldr gamma is rooted by 2.2, and scale is equal
                float z = (float) pow(data[i * comp + k], 1.0f / 2.2f) * 255 + 0.5f;
                if (z < 0)
                    z = 0;
                if (z > 255)
                    z = 255;
                output[i * comp + k] = (unsigned char) float2int(z);
            }
            if (k < comp) {
                float z = data[i * comp + k] * 255 + 0.5f;
                if (z < 0)
                    z = 0;
                if (z > 255)
                    z = 255;
                output[i * comp + k] = (unsigned char) float2int(z);
            }
        }
        free(data);
        return output;
    }

#define FAST_BITS   9  // larger handles more cases; smaller stomps less cache

    struct huffman {
        unsigned char fast[1 << FAST_BITS];
        // weirdly, repacking this into AoS is a 10% speed loss, instead of a win
        uint16_t code[256];
        unsigned char values[256];
        unsigned char size[257];
        unsigned int maxcode[18];
        int delta[17];   // old 'firstsymbol' - old 'firstcode'
    };

    struct jpeg {
        context *s;
        huffman huff_dc[4];
        huffman huff_ac[4];
        unsigned char dequant[4][64];
        int16_t fast_ac[4][1 << FAST_BITS];

        // sizes for components, interleaved MCUs
        int img_h_max, img_v_max;
        int img_mcu_x, img_mcu_y;
        int img_mcu_w, img_mcu_h;

        // definition of jpeg image component
        struct {
            int id;
            int h, v;
            int tq;
            int hd, ha;
            int dc_pred;

            int x, y, w2, h2;
            unsigned char *data;
            void *raw_data, *raw_coeff;
            unsigned char *linebuf;
            short *coeff;   // progressive only
            int coeff_w, coeff_h; // number of 8x8 coefficient blocks
        } img_comp[4];

        uint32_t code_buffer; // jpeg entropy-coded buffer
        int code_bits;   // number of valid bits
        unsigned char marker;      // marker seen while filling entropy buffer
        int nomore;      // flag if we saw a marker so must stop

        int progressive;
        int spec_start;
        int spec_end;
        int succ_high;
        int succ_low;
        int eob_run;

        int scan_n, order[4];
        int restart_interval, todo;

        // kernels
        void (*idct_block_kernel)(unsigned char *out, int out_stride, short data[64]);

        void
        (*YCbCr_to_RGB_kernel)(unsigned char *out, const unsigned char *y, const unsigned char *pcb,
                               const unsigned char *pcr, int count, int step);

        unsigned char *(*resample_row_hv_2_kernel)(unsigned char *out, unsigned char *in_near,
                                                   unsigned char *in_far, int w, int hs);
    };

    static int build_huffman(huffman *h, int *count) {
        int i, j, k = 0, code;
        // build size list for each symbol (from JPEG spec)
        for (i = 0; i < 16; ++i)
            for (j = 0; j < count[i]; ++j)
                h->size[k++] = (unsigned char) (i + 1);
        h->size[k] = 0;

        // compute actual symbols (from jpeg spec)
        code = 0;
        k = 0;
        for (j = 1; j <= 16; ++j) {
            // compute delta to add to code to compute symbol id
            h->delta[j] = k - code;
            if (h->size[k] == j) {
                while (h->size[k] == j)
                    h->code[k++] = (uint16_t) (code++);
                if (code - 1 >= (1 << j))
                    return err("bad code lengths", "Corrupt JPEG");
            }
            // compute largest code + 1 for this size, preshifted as needed later
            h->maxcode[j] = code << (16 - j);
            code <<= 1;
        }
        h->maxcode[j] = 0xffffffff;

        // build non-spec acceleration table; 255 is flag for not-accelerated
        memset(h->fast, 255, 1 << FAST_BITS);
        for (i = 0; i < k; ++i) {
            int s = h->size[i];
            if (s <= FAST_BITS) {
                int c = h->code[i] << (FAST_BITS - s);
                int m = 1 << (FAST_BITS - s);
                for (j = 0; j < m; ++j) {
                    h->fast[c + j] = (unsigned char) i;
                }
            }
        }
        return 1;
    }

// build a table that decodes both magnitude and value of small ACs in
// one go.
    static void build_fast_ac(int16_t *fast_ac, huffman *h) {
        int i;
        for (i = 0; i < (1 << FAST_BITS); ++i) {
            unsigned char fast = h->fast[i];
            fast_ac[i] = 0;
            if (fast < 255) {
                int rs = h->values[fast];
                int run = (rs >> 4) & 15;
                int magbits = rs & 15;
                int len = h->size[fast];

                if (magbits && len + magbits <= FAST_BITS) {
                    // magnitude code followed by receive_extend code
                    int k = ((i << len) & ((1 << FAST_BITS) - 1)) >> (FAST_BITS - magbits);
                    int m = 1 << (magbits - 1);
                    if (k < m)
                        k += (-1 << magbits) + 1;
                    // if the result is small enough, we can fit it in fast_ac table
                    if (k >= -128 && k <= 127)
                        fast_ac[i] = (int16_t) ((k << 8) + (run << 4) + (len + magbits));
                }
            }
        }
    }

    static void grow_buffer_unsafe(jpeg *j) {
        do {
            int b = j->nomore ? 0 : get8(j->s);
            if (b == 0xff) {
                int c = get8(j->s);
                if (c != 0) {
                    j->marker = (unsigned char) c;
                    j->nomore = 1;
                    return;
                }
            }
            j->code_buffer |= b << (24 - j->code_bits);
            j->code_bits += 8;
        } while (j->code_bits <= 24);
    }

// (1 << n) - 1
    static uint32_t bmask[17] = {0, 1, 3, 7, 15, 31, 63, 127, 255, 511, 1023, 2047, 4095,
                                 8191, 16383, 32767, 65535};

// decode a jpeg huffman value from the bitstream
    inline static int jpeg_huff_decode(jpeg *j, huffman *h) {
        unsigned int temp;
        int c, k;

        if (j->code_bits < 16)
            grow_buffer_unsafe(j);

        // look at the top FAST_BITS and determine what symbol ID it is,
        // if the code is <= FAST_BITS
        c = (j->code_buffer >> (32 - FAST_BITS)) & ((1 << FAST_BITS) - 1);
        k = h->fast[c];
        if (k < 255) {
            int s = h->size[k];
            if (s > j->code_bits)
                return -1;
            j->code_buffer <<= s;
            j->code_bits -= s;
            return h->values[k];
        }

        // naive test is to shift the code_buffer down so k bits are
        // valid, then test against maxcode. To speed this up, we've
        // preshifted maxcode left so that it has (16-k) 0s at the
        // end; in other words, regardless of the number of bits, it
        // wants to be compared against something shifted to have 16;
        // that way we don't need to shift inside the loop.
        temp = j->code_buffer >> 16;
        for (k = FAST_BITS + 1;; ++k)
            if (temp < h->maxcode[k])
                break;
        if (k == 17) {
            // error! code not found
            j->code_bits -= 16;
            return -1;
        }

        if (k > j->code_bits)
            return -1;

        // convert the huffman code to the symbol id
        c = ((j->code_buffer >> (32 - k)) & bmask[k]) + h->delta[k];
        assert((((j->code_buffer) >> (32 - h->size[c])) & bmask[h->size[c]]) == h->code[c]);

        // convert the id to a symbol
        j->code_bits -= k;
        j->code_buffer <<= k;
        return h->values[c];
    }

// bias[n] = (-1<<n) + 1
    static int const jbias[16] = {0, -1, -3, -7, -15, -31, -63, -127, -255, -511, -1023,
                                  -2047, -4095, -8191, -16383, -32767};

// combined JPEG 'receive' and JPEG 'extend', since baseline
// always extends everything it receives.
    inline static int extend_receive(jpeg *j, int n) {
        unsigned int k;
        int sgn;
        if (j->code_bits < n)
            grow_buffer_unsafe(j);

        sgn = (int32_t) j->code_buffer >> 31; // sign bit is always in MSB
        k = _lrotl(j->code_buffer, n);
        assert(n >= 0 && n < (int) (sizeof(bmask) / sizeof(*bmask)));
        j->code_buffer = k & ~bmask[n];
        k &= bmask[n];
        j->code_bits -= n;
        return k + (jbias[n] & ~sgn);
    }

// get some unsigned bits
    inline static int jpeg_get_bits(jpeg *j, int n) {
        unsigned int k;
        if (j->code_bits < n)
            grow_buffer_unsafe(j);
        k = _lrotl(j->code_buffer, n);
        j->code_buffer = k & ~bmask[n];
        k &= bmask[n];
        j->code_bits -= n;
        return k;
    }

    inline static int jpeg_get_bit(jpeg *j) {
        unsigned int k;
        if (j->code_bits < 1)
            grow_buffer_unsafe(j);
        k = j->code_buffer;
        j->code_buffer <<= 1;
        --j->code_bits;
        return k & 0x80000000;
    }

// given a value that's at position X in the zigzag stream,
// where does it appear in the 8x8 matrix coded as row-major?
    static unsigned char jpeg_dezigzag[
            64 + 15] = {0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32, 25, 18, 11, 4, 5, 12, 19, 26, 33, 40,
                        48, 41, 34, 27, 20, 13, 6, 7, 14, 21, 28, 35, 42, 49, 56, 57, 50, 43, 36,
                        29, 22, 15, 23, 30, 37, 44, 51, 58, 59, 52, 45, 38, 31, 39, 46, 53, 60, 61,
                        54, 47, 55, 62, 63,
// let corrupt input sample past end
                        63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63};

// decode one 64-entry block--
    static int
    jpeg_decode_block(jpeg *j, short data[64], huffman *hdc, huffman *hac,
                      int16_t *fac, int b, unsigned char *dequant) {
        int diff, dc, k;
        int t;

        if (j->code_bits < 16)
            grow_buffer_unsafe(j);
        t = jpeg_huff_decode(j, hdc);
        if (t < 0)
            return err("bad huffman code", "Corrupt JPEG");

        // 0 all the ac values now so we can do it 32-bits at a time
        memset(data, 0, 64 * sizeof(data[0]));

        diff = t ? extend_receive(j, t) : 0;
        dc = j->img_comp[b].dc_pred + diff;
        j->img_comp[b].dc_pred = dc;
        data[0] = (short) (dc * dequant[0]);

        // decode AC components, see JPEG spec
        k = 1;
        do {
            unsigned int zig;
            int c, r, s;
            if (j->code_bits < 16)
                grow_buffer_unsafe(j);
            c = (j->code_buffer >> (32 - FAST_BITS)) & ((1 << FAST_BITS) - 1);
            r = fac[c];
            if (r)   // fast-AC path
            {
                k += (r >> 4) & 15; // run
                s = r & 15; // combined length
                j->code_buffer <<= s;
                j->code_bits -= s;
                // decode into unzigzag'd location
                zig = jpeg_dezigzag[k++];
                data[zig] = (short) ((r >> 8) * dequant[zig]);
            } else {
                int rs = jpeg_huff_decode(j, hac);
                if (rs < 0)
                    return err("bad huffman code", "Corrupt JPEG");
                s = rs & 15;
                r = rs >> 4;
                if (s == 0) {
                    if (rs != 0xf0)
                        break; // end block
                    k += 16;
                } else {
                    k += r;
                    // decode into unzigzag'd location
                    zig = jpeg_dezigzag[k++];
                    data[zig] = (short) (extend_receive(j, s) * dequant[zig]);
                }
            }
        } while (k < 64);
        return 1;
    }

    static int
    jpeg_decode_block_prog_dc(jpeg *j, short data[64], huffman *hdc, int b) {
        int diff, dc;
        int t;
        if (j->spec_end != 0)
            return err("can't merge dc and ac", "Corrupt JPEG");

        if (j->code_bits < 16)
            grow_buffer_unsafe(j);

        if (j->succ_high == 0) {
            // first scan for DC coefficient, must be first
            memset(data, 0, 64 * sizeof(data[0])); // 0 all the ac values now
            t = jpeg_huff_decode(j, hdc);
            diff = t ? extend_receive(j, t) : 0;

            dc = j->img_comp[b].dc_pred + diff;
            j->img_comp[b].dc_pred = dc;
            data[0] = (short) (dc << j->succ_low);
        } else {
            // refinement scan for DC coefficient
            if (jpeg_get_bit(j))
                data[0] += (short) (1 << j->succ_low);
        }
        return 1;
    }

// @OPTIMIZE: store non-zigzagged during the decode passes,
// and only de-zigzag when dequantizing
    static int jpeg_decode_block_prog_ac(jpeg *j, short data[64], huffman *hac,
                                         int16_t *fac) {
        int k;
        if (j->spec_start == 0)
            return err("can't merge dc and ac", "Corrupt JPEG");

        if (j->succ_high == 0) {
            int shift = j->succ_low;

            if (j->eob_run) {
                --j->eob_run;
                return 1;
            }

            k = j->spec_start;
            do {
                unsigned int zig;
                int c, r, s;
                if (j->code_bits < 16)
                    grow_buffer_unsafe(j);
                c = (j->code_buffer >> (32 - FAST_BITS)) & ((1 << FAST_BITS) - 1);
                r = fac[c];
                if (r)   // fast-AC path
                {
                    k += (r >> 4) & 15; // run
                    s = r & 15; // combined length
                    j->code_buffer <<= s;
                    j->code_bits -= s;
                    zig = jpeg_dezigzag[k++];
                    data[zig] = (short) ((r >> 8) << shift);
                } else {
                    int rs = jpeg_huff_decode(j, hac);
                    if (rs < 0)
                        return err("bad huffman code", "Corrupt JPEG");
                    s = rs & 15;
                    r = rs >> 4;
                    if (s == 0) {
                        if (r < 15) {
                            j->eob_run = (1 << r);
                            if (r)
                                j->eob_run += jpeg_get_bits(j, r);
                            --j->eob_run;
                            break;
                        }
                        k += 16;
                    } else {
                        k += r;
                        zig = jpeg_dezigzag[k++];
                        data[zig] = (short) (extend_receive(j, s) << shift);
                    }
                }
            } while (k <= j->spec_end);
        } else {
            // refinement scan for these AC coefficients

            short bit = (short) (1 << j->succ_low);

            if (j->eob_run) {
                --j->eob_run;
                for (k = j->spec_start; k <= j->spec_end; ++k) {
                    short *p = &data[jpeg_dezigzag[k]];
                    if (*p != 0)
                        if (jpeg_get_bit(j))
                            if ((*p & bit) == 0) {
                                if (*p > 0)
                                    *p += bit;
                                else
                                    *p -= bit;
                            }
                }
            } else {
                k = j->spec_start;
                do {
                    int r, s;
                    int rs = jpeg_huff_decode(j,
                                              hac); // @OPTIMIZE see if we can use the fast path here, advance-by-r is so slow, eh
                    if (rs < 0)
                        return err("bad huffman code", "Corrupt JPEG");
                    s = rs & 15;
                    r = rs >> 4;
                    if (s == 0) {
                        if (r < 15) {
                            j->eob_run = (1 << r) - 1;
                            if (r)
                                j->eob_run += jpeg_get_bits(j, r);
                            r = 64; // force end of block
                        } else {
                            // r=15 s=0 should write 16 0s, so we just do
                            // a run of 15 0s and then write s (which is 0),
                            // so we don't have to do anything special here
                        }
                    } else {
                        if (s != 1)
                            return err("bad huffman code", "Corrupt JPEG");
                        // sign bit
                        if (jpeg_get_bit(j))
                            s = bit;
                        else
                            s = -bit;
                    }

                    // advance by r
                    while (k <= j->spec_end) {
                        short *p = &data[jpeg_dezigzag[k++]];
                        if (*p != 0) {
                            if (jpeg_get_bit(j))
                                if ((*p & bit) == 0) {
                                    if (*p > 0)
                                        *p += bit;
                                    else
                                        *p -= bit;
                                }
                        } else {
                            if (r == 0) {
                                *p = (short) s;
                                break;
                            }
                            --r;
                        }
                    }
                } while (k <= j->spec_end);
            }
        }
        return 1;
    }

// take a -128..127 value and clamp it and convert to 0..255
    inline static unsigned char clamp(int x) {
        // trick to use a single test to catch both cases
        if ((unsigned int) x > 255) {
            if (x < 0)
                return 0;
            if (x > 255)
                return 255;
        }
        return (unsigned char) x;
    }

#define f2f(x)  ((int) (((x) * 4096 + 0.5)))
#define fsh(x)  ((x) << 12)

// derived from jidctint -- DCT_ISLOW
#define IDCT_1D(s0, s1, s2, s3, s4, s5, s6, s7) \
       int t0,t1,t2,t3,p1,p2,p3,p4,p5,x0,x1,x2,x3; \
       p2 = s2;                                    \
       p3 = s6;                                    \
       p1 = (p2+p3) * f2f(0.5411961f);       \
       t2 = p1 + p3*f2f(-1.847759065f);      \
       t3 = p1 + p2*f2f( 0.765366865f);      \
       p2 = s0;                                    \
       p3 = s4;                                    \
       t0 = fsh(p2+p3);                      \
       t1 = fsh(p2-p3);                      \
       x0 = t0+t3;                                 \
       x3 = t0-t3;                                 \
       x1 = t1+t2;                                 \
       x2 = t1-t2;                                 \
       t0 = s7;                                    \
       t1 = s5;                                    \
       t2 = s3;                                    \
       t3 = s1;                                    \
       p3 = t0+t2;                                 \
       p4 = t1+t3;                                 \
       p1 = t0+t3;                                 \
       p2 = t1+t2;                                 \
       p5 = (p3+p4)*f2f( 1.175875602f);      \
       t0 = t0*f2f( 0.298631336f);           \
       t1 = t1*f2f( 2.053119869f);           \
       t2 = t2*f2f( 3.072711026f);           \
       t3 = t3*f2f( 1.501321110f);           \
       p1 = p5 + p1*f2f(-0.899976223f);      \
       p2 = p5 + p2*f2f(-2.562915447f);      \
       p3 = p3*f2f(-1.961570560f);           \
       p4 = p4*f2f(-0.390180644f);           \
       t3 += p1+p4;                                \
       t2 += p2+p3;                                \
       t1 += p2+p4;                                \
       t0 += p1+p3;

    static void idct_block(unsigned char *out, int out_stride, short data[64]) {
        int i, val[64], *v = val;
        unsigned char *o;
        short *d = data;

        // columns
        for (i = 0; i < 8; ++i, ++d, ++v) {
            // if all zeroes, shortcut -- this avoids dequantizing 0s and IDCTing
            if (d[8] == 0 && d[16] == 0 && d[24] == 0 && d[32] == 0 && d[40] == 0 && d[48] == 0 &&
                d[56] == 0) {
                //    no shortcut                 0     seconds
                //    (1|2|3|4|5|6|7)==0          0     seconds
                //    all separate               -0.047 seconds
                //    1 && 2|3 && 4|5 && 6|7:    -0.047 seconds
                int dcterm = d[0] << 2;
                v[0] = v[8] = v[16] = v[24] = v[32] = v[40] = v[48] = v[56] = dcterm;
            } else {
                IDCT_1D(d[0], d[8], d[16], d[24], d[32], d[40], d[48], d[56])
                // constants scaled things up by 1<<12; let's bring them back
                // down, but keep 2 extra bits of precision
                x0 += 512;
                x1 += 512;
                x2 += 512;
                x3 += 512;
                v[0] = (x0 + t3) >> 10;
                v[56] = (x0 - t3) >> 10;
                v[8] = (x1 + t2) >> 10;
                v[48] = (x1 - t2) >> 10;
                v[16] = (x2 + t1) >> 10;
                v[40] = (x2 - t1) >> 10;
                v[24] = (x3 + t0) >> 10;
                v[32] = (x3 - t0) >> 10;
            }
        }

        for (i = 0, v = val, o = out; i < 8; ++i, v += 8, o += out_stride) {
            // no fast case since the first 1D IDCT spread components out
            IDCT_1D(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7])
            // constants scaled things up by 1<<12, plus we had 1<<2 from first
            // loop, plus horizontal and vertical each scale by sqrt(8) so together
            // we've got an extra 1<<3, so 1<<17 total we need to remove.
            // so we want to round that, which means adding 0.5 * 1<<17,
            // aka 65536. Also, we'll end up with -128 to 127 that we want
            // to encode as 0..255 by adding 128, so we'll add that before the shift
            x0 += 65536 + (128 << 17);
            x1 += 65536 + (128 << 17);
            x2 += 65536 + (128 << 17);
            x3 += 65536 + (128 << 17);
            // tried computing the shifts into temps, or'ing the temps to see
            // if any were out of range, but that was slower
            o[0] = clamp((x0 + t3) >> 17);
            o[7] = clamp((x0 - t3) >> 17);
            o[1] = clamp((x1 + t2) >> 17);
            o[6] = clamp((x1 - t2) >> 17);
            o[2] = clamp((x2 + t1) >> 17);
            o[5] = clamp((x2 - t1) >> 17);
            o[3] = clamp((x3 + t0) >> 17);
            o[4] = clamp((x3 - t0) >> 17);
        }
    }

#ifdef STBI_SSE2

// sse2 integer IDCT. not the fastest possible implementation but it
// produces bit-identical results to the generic C version so it's
// fully "transparent".
    static void idct_simd(unsigned char *out, int out_stride, short data[64]) {
        // This is constructed to match our regular (generic) integer IDCT exactly.
        __m128i row0, row1, row2, row3, row4, row5, row6, row7;
        __m128i tmp;

        // dot product constant: even elems=x, odd elems=y
#define dct_const(x, y)  _mm_setr_epi16((x),(y),(x),(y),(x),(y),(x),(y))

        // out(0) = c0[even]*x + c0[odd]*y   (c0, x, y 16-bit, out 32-bit)
        // out(1) = c1[even]*x + c1[odd]*y
#define dct_rot(out0, out1, x, y, c0, c1) \
          __m128i c0##lo = _mm_unpacklo_epi16((x),(y)); \
          __m128i c0##hi = _mm_unpackhi_epi16((x),(y)); \
          __m128i out0##_l = _mm_madd_epi16(c0##lo, c0); \
          __m128i out0##_h = _mm_madd_epi16(c0##hi, c0); \
          __m128i out1##_l = _mm_madd_epi16(c0##lo, c1); \
          __m128i out1##_h = _mm_madd_epi16(c0##hi, c1)

        // out = in << 12  (in 16-bit, out 32-bit)
#define dct_widen(out, in) \
          __m128i out##_l = _mm_srai_epi32(_mm_unpacklo_epi16(_mm_setzero_si128(), (in)), 4); \
          __m128i out##_h = _mm_srai_epi32(_mm_unpackhi_epi16(_mm_setzero_si128(), (in)), 4)

        // wide add
#define dct_wadd(out, a, b) \
          __m128i out##_l = _mm_add_epi32(a##_l, b##_l); \
          __m128i out##_h = _mm_add_epi32(a##_h, b##_h)

        // wide sub
#define dct_wsub(out, a, b) \
          __m128i out##_l = _mm_sub_epi32(a##_l, b##_l); \
          __m128i out##_h = _mm_sub_epi32(a##_h, b##_h)

        // butterfly a/b, add bias, then shift by "s" and pack
#define dct_bfly32o(out0, out1, a, b, bias, s) \
          { \
             __m128i abiased_l = _mm_add_epi32(a##_l, bias); \
             __m128i abiased_h = _mm_add_epi32(a##_h, bias); \
             dct_wadd(sum, abiased, b); \
             dct_wsub(dif, abiased, b); \
             out0 = _mm_packs_epi32(_mm_srai_epi32(sum_l, s), _mm_srai_epi32(sum_h, s)); \
             out1 = _mm_packs_epi32(_mm_srai_epi32(dif_l, s), _mm_srai_epi32(dif_h, s)); \
          }

        // 8-bit interleave step (for transposes)
#define dct_interleave8(a, b) \
          tmp = a; \
          a = _mm_unpacklo_epi8(a, b); \
          b = _mm_unpackhi_epi8(tmp, b)

        // 16-bit interleave step (for transposes)
#define dct_interleave16(a, b) \
          tmp = a; \
          a = _mm_unpacklo_epi16(a, b); \
          b = _mm_unpackhi_epi16(tmp, b)

#define dct_pass(bias, shift) \
          { \
             /* even part */ \
             dct_rot(t2e,t3e, row2,row6, rot0_0,rot0_1); \
             __m128i sum04 = _mm_add_epi16(row0, row4); \
             __m128i dif04 = _mm_sub_epi16(row0, row4); \
             dct_widen(t0e, sum04); \
             dct_widen(t1e, dif04); \
             dct_wadd(x0, t0e, t3e); \
             dct_wsub(x3, t0e, t3e); \
             dct_wadd(x1, t1e, t2e); \
             dct_wsub(x2, t1e, t2e); \
             /* odd part */ \
             dct_rot(y0o,y2o, row7,row3, rot2_0,rot2_1); \
             dct_rot(y1o,y3o, row5,row1, rot3_0,rot3_1); \
             __m128i sum17 = _mm_add_epi16(row1, row7); \
             __m128i sum35 = _mm_add_epi16(row3, row5); \
             dct_rot(y4o,y5o, sum17,sum35, rot1_0,rot1_1); \
             dct_wadd(x4, y0o, y4o); \
             dct_wadd(x5, y1o, y5o); \
             dct_wadd(x6, y2o, y5o); \
             dct_wadd(x7, y3o, y4o); \
             dct_bfly32o(row0,row7, x0,x7,bias,shift); \
             dct_bfly32o(row1,row6, x1,x6,bias,shift); \
             dct_bfly32o(row2,row5, x2,x5,bias,shift); \
             dct_bfly32o(row3,row4, x3,x4,bias,shift); \
          }

        __m128i rot0_0 = dct_const(f2f(0.5411961f),
                                   f2f(0.5411961f) + f2f(-1.847759065f));
        __m128i rot0_1 = dct_const(f2f(0.5411961f) + f2f(0.765366865f),
                                   f2f(0.5411961f));
        __m128i rot1_0 = dct_const(f2f(1.175875602f) + f2f(-0.899976223f),
                                   f2f(1.175875602f));
        __m128i rot1_1 = dct_const(f2f(1.175875602f),
                                   f2f(1.175875602f) + f2f(-2.562915447f));
        __m128i rot2_0 = dct_const(f2f(-1.961570560f) + f2f(0.298631336f),
                                   f2f(-1.961570560f));
        __m128i rot2_1 = dct_const(f2f(-1.961570560f),
                                   f2f(-1.961570560f) + f2f(3.072711026f));
        __m128i rot3_0 = dct_const(f2f(-0.390180644f) + f2f(2.053119869f),
                                   f2f(-0.390180644f));
        __m128i rot3_1 = dct_const(f2f(-0.390180644f),
                                   f2f(-0.390180644f) + f2f(1.501321110f));

        // rounding biases in column/row passes, see idct_block for explanation.
        __m128i bias_0 = _mm_set1_epi32(512);
        __m128i bias_1 = _mm_set1_epi32(65536 + (128 << 17));

        // load
        row0 = _mm_load_si128((const __m128i *) (data + 0 * 8));
        row1 = _mm_load_si128((const __m128i *) (data + 1 * 8));
        row2 = _mm_load_si128((const __m128i *) (data + 2 * 8));
        row3 = _mm_load_si128((const __m128i *) (data + 3 * 8));
        row4 = _mm_load_si128((const __m128i *) (data + 4 * 8));
        row5 = _mm_load_si128((const __m128i *) (data + 5 * 8));
        row6 = _mm_load_si128((const __m128i *) (data + 6 * 8));
        row7 = _mm_load_si128((const __m128i *) (data + 7 * 8));

        // column pass
        dct_pass(bias_0, 10);

        {
            // 16bit 8x8 transpose pass 1
            dct_interleave16(row0, row4);
            dct_interleave16(row1, row5);
            dct_interleave16(row2, row6);
            dct_interleave16(row3, row7);

            // transpose pass 2
            dct_interleave16(row0, row2);
            dct_interleave16(row1, row3);
            dct_interleave16(row4, row6);
            dct_interleave16(row5, row7);

            // transpose pass 3
            dct_interleave16(row0, row1);
            dct_interleave16(row2, row3);
            dct_interleave16(row4, row5);
            dct_interleave16(row6, row7);
        }

        // row pass
        dct_pass(bias_1, 17);

        {
            // pack
            __m128i p0 = _mm_packus_epi16(row0, row1);// a0a1a2a3...a7b0b1b2b3...b7
            __m128i p1 = _mm_packus_epi16(row2, row3);
            __m128i p2 = _mm_packus_epi16(row4, row5);
            __m128i p3 = _mm_packus_epi16(row6, row7);

            // 8bit 8x8 transpose pass 1
            dct_interleave8(p0, p2);// a0e0a1e1...
            dct_interleave8(p1, p3);// c0g0c1g1...

            // transpose pass 2
            dct_interleave8(p0, p1);// a0c0e0g0...
            dct_interleave8(p2, p3);// b0d0f0h0...

            // transpose pass 3
            dct_interleave8(p0, p2);// a0b0c0d0...
            dct_interleave8(p1, p3);// a4b4c4d4...

            // store
            _mm_storel_epi64((__m128i *) out, p0);
            out += out_stride;
            _mm_storel_epi64((__m128i *) out, _mm_shuffle_epi32(p0, 0x4e));
            out += out_stride;
            _mm_storel_epi64((__m128i *) out, p2);
            out += out_stride;
            _mm_storel_epi64((__m128i *) out, _mm_shuffle_epi32(p2, 0x4e));
            out += out_stride;
            _mm_storel_epi64((__m128i *) out, p1);
            out += out_stride;
            _mm_storel_epi64((__m128i *) out, _mm_shuffle_epi32(p1, 0x4e));
            out += out_stride;
            _mm_storel_epi64((__m128i *) out, p3);
            out += out_stride;
            _mm_storel_epi64((__m128i *) out, _mm_shuffle_epi32(p3, 0x4e));
        }

#undef dct_const
#undef dct_rot
#undef dct_widen
#undef dct_wadd
#undef dct_wsub
#undef dct_bfly32o
#undef dct_interleave8
#undef dct_interleave16
#undef dct_pass
    }

#endif // STBI_SSE2

#ifdef STBI_NEON
                                                                                                                            static void idct_simd(unsigned char *out, int out_stride, short data[64])
{
	int16x8_t row0, row1, row2, row3, row4, row5, row6, row7;

	int16x4_t rot0_0 = vdup_n_s16(f2f(0.5411961f));
	int16x4_t rot0_1 = vdup_n_s16(f2f(-1.847759065f));
	int16x4_t rot0_2 = vdup_n_s16(f2f( 0.765366865f));
	int16x4_t rot1_0 = vdup_n_s16(f2f( 1.175875602f));
	int16x4_t rot1_1 = vdup_n_s16(f2f(-0.899976223f));
	int16x4_t rot1_2 = vdup_n_s16(f2f(-2.562915447f));
	int16x4_t rot2_0 = vdup_n_s16(f2f(-1.961570560f));
	int16x4_t rot2_1 = vdup_n_s16(f2f(-0.390180644f));
	int16x4_t rot3_0 = vdup_n_s16(f2f( 0.298631336f));
	int16x4_t rot3_1 = vdup_n_s16(f2f( 2.053119869f));
	int16x4_t rot3_2 = vdup_n_s16(f2f( 3.072711026f));
	int16x4_t rot3_3 = vdup_n_s16(f2f( 1.501321110f));

#define dct_long_mul(out, inq, coeff) \
	   int32x4_t out##_l = vmull_s16(vget_low_s16(inq), coeff); \
	   int32x4_t out##_h = vmull_s16(vget_high_s16(inq), coeff)

#define dct_long_mac(out, acc, inq, coeff) \
	   int32x4_t out##_l = vmlal_s16(acc##_l, vget_low_s16(inq), coeff); \
	   int32x4_t out##_h = vmlal_s16(acc##_h, vget_high_s16(inq), coeff)

#define dct_widen(out, inq) \
	   int32x4_t out##_l = vshll_n_s16(vget_low_s16(inq), 12); \
	   int32x4_t out##_h = vshll_n_s16(vget_high_s16(inq), 12)

	// wide add
#define dct_wadd(out, a, b) \
	   int32x4_t out##_l = vaddq_s32(a##_l, b##_l); \
	   int32x4_t out##_h = vaddq_s32(a##_h, b##_h)

	// wide sub
#define dct_wsub(out, a, b) \
	   int32x4_t out##_l = vsubq_s32(a##_l, b##_l); \
	   int32x4_t out##_h = vsubq_s32(a##_h, b##_h)

	// butterfly a/b, then shift using "shiftop" by "s" and pack
#define dct_bfly32o(out0,out1, a,b,shiftop,s) \
	   { \
		  dct_wadd(sum, a, b); \
		  dct_wsub(dif, a, b); \
		  out0 = vcombine_s16(shiftop(sum_l, s), shiftop(sum_h, s)); \
		  out1 = vcombine_s16(shiftop(dif_l, s), shiftop(dif_h, s)); \
	   }

#define dct_pass(shiftop, shift) \
	   { \
		  /* even part */ \
		  int16x8_t sum26 = vaddq_s16(row2, row6); \
		  dct_long_mul(p1e, sum26, rot0_0); \
		  dct_long_mac(t2e, p1e, row6, rot0_1); \
		  dct_long_mac(t3e, p1e, row2, rot0_2); \
		  int16x8_t sum04 = vaddq_s16(row0, row4); \
		  int16x8_t dif04 = vsubq_s16(row0, row4); \
		  dct_widen(t0e, sum04); \
		  dct_widen(t1e, dif04); \
		  dct_wadd(x0, t0e, t3e); \
		  dct_wsub(x3, t0e, t3e); \
		  dct_wadd(x1, t1e, t2e); \
		  dct_wsub(x2, t1e, t2e); \
		  /* odd part */ \
		  int16x8_t sum15 = vaddq_s16(row1, row5); \
		  int16x8_t sum17 = vaddq_s16(row1, row7); \
		  int16x8_t sum35 = vaddq_s16(row3, row5); \
		  int16x8_t sum37 = vaddq_s16(row3, row7); \
		  int16x8_t sumodd = vaddq_s16(sum17, sum35); \
		  dct_long_mul(p5o, sumodd, rot1_0); \
		  dct_long_mac(p1o, p5o, sum17, rot1_1); \
		  dct_long_mac(p2o, p5o, sum35, rot1_2); \
		  dct_long_mul(p3o, sum37, rot2_0); \
		  dct_long_mul(p4o, sum15, rot2_1); \
		  dct_wadd(sump13o, p1o, p3o); \
		  dct_wadd(sump24o, p2o, p4o); \
		  dct_wadd(sump23o, p2o, p3o); \
		  dct_wadd(sump14o, p1o, p4o); \
		  dct_long_mac(x4, sump13o, row7, rot3_0); \
		  dct_long_mac(x5, sump24o, row5, rot3_1); \
		  dct_long_mac(x6, sump23o, row3, rot3_2); \
		  dct_long_mac(x7, sump14o, row1, rot3_3); \
		  dct_bfly32o(row0,row7, x0,x7,shiftop,shift); \
		  dct_bfly32o(row1,row6, x1,x6,shiftop,shift); \
		  dct_bfly32o(row2,row5, x2,x5,shiftop,shift); \
		  dct_bfly32o(row3,row4, x3,x4,shiftop,shift); \
	   }

	// load
	row0 = vld1q_s16(data + 0*8);
	row1 = vld1q_s16(data + 1*8);
	row2 = vld1q_s16(data + 2*8);
	row3 = vld1q_s16(data + 3*8);
	row4 = vld1q_s16(data + 4*8);
	row5 = vld1q_s16(data + 5*8);
	row6 = vld1q_s16(data + 6*8);
	row7 = vld1q_s16(data + 7*8);

	// add DC bias
	row0 = vaddq_s16(row0, vsetq_lane_s16(1024, vdupq_n_s16(0), 0));

	// column pass
	dct_pass(vrshrn_n_s32, 10);

	// 16bit 8x8 transpose
	{
		// these three map to a single VTRN.16, VTRN.32, and VSWP, respectively.
		// whether compilers actually get this is another story, sadly.
#define dct_trn16(x, y) { int16x8x2_t t = vtrnq_s16(x, y); x = t.val[0]; y = t.val[1]; }
#define dct_trn32(x, y) { int32x4x2_t t = vtrnq_s32(vreinterpretq_s32_s16(x), vreinterpretq_s32_s16(y)); x = vreinterpretq_s16_s32(t.val[0]); y = vreinterpretq_s16_s32(t.val[1]); }
#define dct_trn64(x, y) { int16x8_t x0 = x; int16x8_t y0 = y; x = vcombine_s16(vget_low_s16(x0), vget_low_s16(y0)); y = vcombine_s16(vget_high_s16(x0), vget_high_s16(y0)); }

		// pass 1
		dct_trn16(row0, row1);// a0b0a2b2a4b4a6b6
		dct_trn16(row2, row3);
		dct_trn16(row4, row5);
		dct_trn16(row6, row7);

		// pass 2
		dct_trn32(row0, row2);// a0b0c0d0a4b4c4d4
		dct_trn32(row1, row3);
		dct_trn32(row4, row6);
		dct_trn32(row5, row7);

		// pass 3
		dct_trn64(row0, row4);// a0b0c0d0e0f0g0h0
		dct_trn64(row1, row5);
		dct_trn64(row2, row6);
		dct_trn64(row3, row7);

#undef dct_trn16
#undef dct_trn32
#undef dct_trn64
	}

	// row pass
	// vrshrn_n_s32 only supports shifts up to 16, we need
	// 17. so do a non-rounding shift of 16 first then follow
	// up with a rounding shift by 1.
	dct_pass(vshrn_n_s32, 16);

	{
		// pack and round
		uint8x8_t p0 = vqrshrun_n_s16(row0, 1);
		uint8x8_t p1 = vqrshrun_n_s16(row1, 1);
		uint8x8_t p2 = vqrshrun_n_s16(row2, 1);
		uint8x8_t p3 = vqrshrun_n_s16(row3, 1);
		uint8x8_t p4 = vqrshrun_n_s16(row4, 1);
		uint8x8_t p5 = vqrshrun_n_s16(row5, 1);
		uint8x8_t p6 = vqrshrun_n_s16(row6, 1);
		uint8x8_t p7 = vqrshrun_n_s16(row7, 1);

		// again, these can translate into one instruction, but often don't.
#define dct_trn8_8(x, y) { uint8x8x2_t t = vtrn_u8(x, y); x = t.val[0]; y = t.val[1]; }
#define dct_trn8_16(x, y) { uint16x4x2_t t = vtrn_u16(vreinterpret_u16_u8(x), vreinterpret_u16_u8(y)); x = vreinterpret_u8_u16(t.val[0]); y = vreinterpret_u8_u16(t.val[1]); }
#define dct_trn8_32(x, y) { uint32x2x2_t t = vtrn_u32(vreinterpret_u32_u8(x), vreinterpret_u32_u8(y)); x = vreinterpret_u8_u32(t.val[0]); y = vreinterpret_u8_u32(t.val[1]); }

		// sadly can't use interleaved stores here since we only write
		// 8 bytes to each scan line!

		// 8x8 8-bit transpose pass 1
		dct_trn8_8(p0, p1);
		dct_trn8_8(p2, p3);
		dct_trn8_8(p4, p5);
		dct_trn8_8(p6, p7);

		// pass 2
		dct_trn8_16(p0, p2);
		dct_trn8_16(p1, p3);
		dct_trn8_16(p4, p6);
		dct_trn8_16(p5, p7);

		// pass 3
		dct_trn8_32(p0, p4);
		dct_trn8_32(p1, p5);
		dct_trn8_32(p2, p6);
		dct_trn8_32(p3, p7);

		// store
		vst1_u8(out, p0);
		out += out_stride;
		vst1_u8(out, p1);
		out += out_stride;
		vst1_u8(out, p2);
		out += out_stride;
		vst1_u8(out, p3);
		out += out_stride;
		vst1_u8(out, p4);
		out += out_stride;
		vst1_u8(out, p5);
		out += out_stride;
		vst1_u8(out, p6);
		out += out_stride;
		vst1_u8(out, p7);

#undef dct_trn8_8
#undef dct_trn8_16
#undef dct_trn8_32
	}

#undef dct_long_mul
#undef dct_long_mac
#undef dct_widen
#undef dct_wadd
#undef dct_wsub
#undef dct_bfly32o
#undef dct_pass
}

#endif // STBI_NEON

#define MARKER_none  0xff

// if there's a pending marker from the entropy stream, return that
// otherwise, fetch from the stream and get a marker. if there's no
// marker, return 0xff, which is never a valid marker value
    static unsigned char get_marker(jpeg *j) {
        unsigned char x;
        if (j->marker != MARKER_none) {
            x = j->marker;
            j->marker = MARKER_none;
            return x;
        }
        x = get8(j->s);
        if (x != 0xff)
            return MARKER_none;
        while (x == 0xff)
            x = get8(j->s);
        return x;
    }

// in each scan, we'll have scan_n components, and the order
// of the components is specified by order[]
#define RESTART(x)     ((x) >= 0xd0 && (x) <= 0xd7)

// after a restart interval, jpeg_reset the entropy decoder and
// the dc prediction
    static void jpeg_reset(jpeg *j) {
        j->code_bits = 0;
        j->code_buffer = 0;
        j->nomore = 0;
        j->img_comp[0].dc_pred = j->img_comp[1].dc_pred = j->img_comp[2].dc_pred = 0;
        j->marker = MARKER_none;
        j->todo = j->restart_interval ? j->restart_interval : 0x7fffffff;
        j->eob_run = 0;
        // no more than 1<<31 MCUs if no restart_interal? that's plenty safe,
        // since we don't even allow 1<<30 pixels
    }

    static int parse_entropy_coded_data(jpeg *z) {
        jpeg_reset(z);
        if (!z->progressive) {
            if (z->scan_n == 1) {
                int i, j;
                STBI_SIMD_ALIGN(short, data[64]);
                int n = z->order[0];
                // non-interleaved data, we just need to process one block at a time,
                // in trivial scanline order
                // number of blocks to do just depends on how many actual "pixels" this
                // component has, independent of interleaved MCU blocking and such
                int w = (z->img_comp[n].x + 7) >> 3;
                int h = (z->img_comp[n].y + 7) >> 3;
                for (j = 0; j < h; ++j) {
                    for (i = 0; i < w; ++i) {
                        int ha = z->img_comp[n].ha;
                        if (!jpeg_decode_block(z, data, z->huff_dc + z->img_comp[n].hd,
                                               z->huff_ac + ha, z->fast_ac[ha], n,
                                               z->dequant[z->img_comp[n].tq]))
                            return 0;
                        z->idct_block_kernel(
                                z->img_comp[n].data + z->img_comp[n].w2 * j * 8 + i * 8,
                                z->img_comp[n].w2, data);
                        // every data block is an MCU, so countdown the restart interval
                        if (--z->todo <= 0) {
                            if (z->code_bits < 24)
                                grow_buffer_unsafe(z);
                            // if it's NOT a restart, then just bail, so we get corrupt data
                            // rather than no data
                            if (!RESTART(z->marker))
                                return 1;
                            jpeg_reset(z);
                        }
                    }
                }
                return 1;
            } else     // interleaved
            {
                int i, j, k, x, y;
                STBI_SIMD_ALIGN(short, data[64]);
                for (j = 0; j < z->img_mcu_y; ++j) {
                    for (i = 0; i < z->img_mcu_x; ++i) {
                        // scan an interleaved mcu... process scan_n components in order
                        for (k = 0; k < z->scan_n; ++k) {
                            int n = z->order[k];
                            // scan out an mcu's worth of this component; that's just determined
                            // by the basic H and V specified for the component
                            for (y = 0; y < z->img_comp[n].v; ++y) {
                                for (x = 0; x < z->img_comp[n].h; ++x) {
                                    int x2 = (i * z->img_comp[n].h + x) * 8;
                                    int y2 = (j * z->img_comp[n].v + y) * 8;
                                    int ha = z->img_comp[n].ha;
                                    if (!jpeg_decode_block(z, data,
                                                           z->huff_dc + z->img_comp[n].hd,
                                                           z->huff_ac + ha, z->fast_ac[ha], n,
                                                           z->dequant[z->img_comp[n].tq]))
                                        return 0;
                                    z->idct_block_kernel(
                                            z->img_comp[n].data + z->img_comp[n].w2 * y2 + x2,
                                            z->img_comp[n].w2, data);
                                }
                            }
                        }
                        // after all interleaved components, that's an interleaved MCU,
                        // so now count down the restart interval
                        if (--z->todo <= 0) {
                            if (z->code_bits < 24)
                                grow_buffer_unsafe(z);
                            if (!RESTART(z->marker))
                                return 1;
                            jpeg_reset(z);
                        }
                    }
                }
                return 1;
            }
        } else {
            if (z->scan_n == 1) {
                int i, j;
                int n = z->order[0];
                // non-interleaved data, we just need to process one block at a time,
                // in trivial scanline order
                // number of blocks to do just depends on how many actual "pixels" this
                // component has, independent of interleaved MCU blocking and such
                int w = (z->img_comp[n].x + 7) >> 3;
                int h = (z->img_comp[n].y + 7) >> 3;
                for (j = 0; j < h; ++j) {
                    for (i = 0; i < w; ++i) {
                        short *data = z->img_comp[n].coeff + 64 * (i + j * z->img_comp[n].coeff_w);
                        if (z->spec_start == 0) {
                            if (!jpeg_decode_block_prog_dc(z, data,
                                                           &z->huff_dc[z->img_comp[n].hd], n))
                                return 0;
                        } else {
                            int ha = z->img_comp[n].ha;
                            if (!jpeg_decode_block_prog_ac(z, data, &z->huff_ac[ha],
                                                           z->fast_ac[ha]))
                                return 0;
                        }
                        // every data block is an MCU, so countdown the restart interval
                        if (--z->todo <= 0) {
                            if (z->code_bits < 24)
                                grow_buffer_unsafe(z);
                            if (!RESTART(z->marker))
                                return 1;
                            jpeg_reset(z);
                        }
                    }
                }
                return 1;
            } else     // interleaved
            {
                int i, j, k, x, y;
                for (j = 0; j < z->img_mcu_y; ++j) {
                    for (i = 0; i < z->img_mcu_x; ++i) {
                        // scan an interleaved mcu... process scan_n components in order
                        for (k = 0; k < z->scan_n; ++k) {
                            int n = z->order[k];
                            // scan out an mcu's worth of this component; that's just determined
                            // by the basic H and V specified for the component
                            for (y = 0; y < z->img_comp[n].v; ++y) {
                                for (x = 0; x < z->img_comp[n].h; ++x) {
                                    int x2 = (i * z->img_comp[n].h + x);
                                    int y2 = (j * z->img_comp[n].v + y);
                                    short *data = z->img_comp[n].coeff +
                                                  64 * (x2 + y2 * z->img_comp[n].coeff_w);
                                    if (!jpeg_decode_block_prog_dc(z, data,
                                                                   &z->huff_dc[z->img_comp[n].hd],
                                                                   n))
                                        return 0;
                                }
                            }
                        }
                        // after all interleaved components, that's an interleaved MCU,
                        // so now count down the restart interval
                        if (--z->todo <= 0) {
                            if (z->code_bits < 24)
                                grow_buffer_unsafe(z);
                            if (!RESTART(z->marker))
                                return 1;
                            jpeg_reset(z);
                        }
                    }
                }
                return 1;
            }
        }
    }

    static void jpeg_dequantize(short *data, unsigned char *dequant) {
        int i;
        for (i = 0; i < 64; ++i)
            data[i] *= dequant[i];
    }

    static void jpeg_finish(jpeg *z) {
        if (z->progressive) {
            // dequantize and idct the data
            int i, j, n;
            for (n = 0; n < z->s->img_n; ++n) {
                int w = (z->img_comp[n].x + 7) >> 3;
                int h = (z->img_comp[n].y + 7) >> 3;
                for (j = 0; j < h; ++j) {
                    for (i = 0; i < w; ++i) {
                        short *data = z->img_comp[n].coeff + 64 * (i + j * z->img_comp[n].coeff_w);
                        jpeg_dequantize(data, z->dequant[z->img_comp[n].tq]);
                        z->idct_block_kernel(
                                z->img_comp[n].data + z->img_comp[n].w2 * j * 8 + i * 8,
                                z->img_comp[n].w2, data);
                    }
                }
            }
        }
    }

    static int process_marker(jpeg *z, int m) {
        int L;
        switch (m) {
            case MARKER_none: // no marker found
                return err("expected marker", "Corrupt JPEG");

            case 0xDD: // DRI - specify restart interval
                if (get16be(z->s) != 4)
                    return err("bad DRI len", "Corrupt JPEG");
                z->restart_interval = get16be(z->s);
                return 1;

            case 0xDB: // DQT - define quantization table
                L = get16be(z->s) - 2;
                while (L > 0) {
                    int q = get8(z->s);
                    int p = q >> 4;
                    int t = q & 15, i;
                    if (p != 0)
                        return err("bad DQT type", "Corrupt JPEG");
                    if (t > 3)
                        return err("bad DQT table", "Corrupt JPEG");
                    for (i = 0; i < 64; ++i)
                        z->dequant[t][jpeg_dezigzag[i]] = get8(z->s);
                    L -= 65;
                }
                return L == 0;

            case 0xC4: // DHT - define huffman table
                L = get16be(z->s) - 2;
                while (L > 0) {
                    unsigned char *v;
                    int sizes[16], i, n = 0;
                    int q = get8(z->s);
                    int tc = q >> 4;
                    int th = q & 15;
                    if (tc > 1 || th > 3)
                        return err("bad DHT header", "Corrupt JPEG");
                    for (i = 0; i < 16; ++i) {
                        sizes[i] = get8(z->s);
                        n += sizes[i];
                    }
                    L -= 17;
                    if (tc == 0) {
                        if (!build_huffman(z->huff_dc + th, sizes))
                            return 0;
                        v = z->huff_dc[th].values;
                    } else {
                        if (!build_huffman(z->huff_ac + th, sizes))
                            return 0;
                        v = z->huff_ac[th].values;
                    }
                    for (i = 0; i < n; ++i)
                        v[i] = get8(z->s);
                    if (tc != 0)
                        build_fast_ac(z->fast_ac[th], z->huff_ac + th);
                    L -= n;
                }
                return L == 0;
        }
        // check for comment block or APP blocks
        if ((m >= 0xE0 && m <= 0xEF) || m == 0xFE) {
            skip(z->s, get16be(z->s) - 2);
            return 1;
        }
        return 0;
    }

// after we see SOS
    static int process_scan_header(jpeg *z) {
        int i;
        int Ls = get16be(z->s);
        z->scan_n = get8(z->s);
        if (z->scan_n < 1 || z->scan_n > 4 || z->scan_n > (int) z->s->img_n)
            return err("bad SOS component count", "Corrupt JPEG");
        if (Ls != 6 + 2 * z->scan_n)
            return err("bad SOS len", "Corrupt JPEG");
        for (i = 0; i < z->scan_n; ++i) {
            int id = get8(z->s), which;
            int q = get8(z->s);
            for (which = 0; which < z->s->img_n; ++which)
                if (z->img_comp[which].id == id)
                    break;
            if (which == z->s->img_n)
                return 0; // no match
            z->img_comp[which].hd = q >> 4;
            if (z->img_comp[which].hd > 3)
                return err("bad DC huff", "Corrupt JPEG");
            z->img_comp[which].ha = q & 15;
            if (z->img_comp[which].ha > 3)
                return err("bad AC huff", "Corrupt JPEG");
            z->order[i] = which;
        }

        {
            int aa;
            z->spec_start = get8(z->s);
            z->spec_end = get8(z->s); // should be 63, but might be 0
            aa = get8(z->s);
            z->succ_high = (aa >> 4);
            z->succ_low = (aa & 15);
            if (z->progressive) {
                if (z->spec_start > 63 || z->spec_end > 63 || z->spec_start > z->spec_end ||
                    z->succ_high > 13 || z->succ_low > 13)
                    return err("bad SOS", "Corrupt JPEG");
            } else {
                if (z->spec_start != 0)
                    return err("bad SOS", "Corrupt JPEG");
                if (z->succ_high != 0 || z->succ_low != 0)
                    return err("bad SOS", "Corrupt JPEG");
                z->spec_end = 63;
            }
        }

        return 1;
    }

    static int process_frame_header(jpeg *z, int scan) {
        context *s = z->s;
        int Lf, p, i, q, h_max = 1, v_max = 1, c;
        Lf = get16be(s);
        if (Lf < 11)
            return err("bad SOF len", "Corrupt JPEG"); // JPEG
        p = get8(s);
        if (p != 8)
            return err("only 8-bit",
                       "JPEG format not supported: 8-bit only"); // JPEG baseline
        s->img_y = get16be(s);
        if (s->img_y == 0)
            return err("no header height",
                       "JPEG format not supported: delayed height"); // Legal, but we don't handle it--but neither does IJG
        s->img_x = get16be(s);
        if (s->img_x == 0)
            return err("0 width", "Corrupt JPEG"); // JPEG requires
        c = get8(s);
        if (c != 3 && c != 1)
            return err("bad component count", "Corrupt JPEG"); // JFIF requires
        s->img_n = c;
        for (i = 0; i < c; ++i) {
            z->img_comp[i].data = NULL;
            z->img_comp[i].linebuf = NULL;
        }

        if (Lf != 8 + 3 * s->img_n)
            return err("bad SOF len", "Corrupt JPEG");

        for (i = 0; i < s->img_n; ++i) {
            z->img_comp[i].id = get8(s);
            if (z->img_comp[i].id != i + 1)   // JFIF requires
                if (z->img_comp[i].id !=
                    i) // some version of jpegtran outputs non-JFIF-compliant files!
                    return err("bad component ID", "Corrupt JPEG");
            q = get8(s);
            z->img_comp[i].h = (q >> 4);
            if (!z->img_comp[i].h || z->img_comp[i].h > 4)
                return err("bad H", "Corrupt JPEG");
            z->img_comp[i].v = q & 15;
            if (!z->img_comp[i].v || z->img_comp[i].v > 4)
                return err("bad V", "Corrupt JPEG");
            z->img_comp[i].tq = get8(s);
            if (z->img_comp[i].tq > 3)
                return err("bad TQ", "Corrupt JPEG");
        }

        if (scan != SCAN_load)
            return 1;

        if ((1 << 30) / s->img_x / s->img_n < s->img_y)
            return err("too large", "Image too large to decode");

        for (i = 0; i < s->img_n; ++i) {
            if (z->img_comp[i].h > h_max)
                h_max = z->img_comp[i].h;
            if (z->img_comp[i].v > v_max)
                v_max = z->img_comp[i].v;
        }

        // compute interleaved mcu info
        z->img_h_max = h_max;
        z->img_v_max = v_max;
        z->img_mcu_w = h_max * 8;
        z->img_mcu_h = v_max * 8;
        z->img_mcu_x = (s->img_x + z->img_mcu_w - 1) / z->img_mcu_w;
        z->img_mcu_y = (s->img_y + z->img_mcu_h - 1) / z->img_mcu_h;

        for (i = 0; i < s->img_n; ++i) {
            // number of effective pixels (e.g. for non-interleaved MCU)
            z->img_comp[i].x = (s->img_x * z->img_comp[i].h + h_max - 1) / h_max;
            z->img_comp[i].y = (s->img_y * z->img_comp[i].v + v_max - 1) / v_max;
            // to simplify generation, we'll allocate enough memory to decode
            // the bogus oversized data from using interleaved MCUs and their
            // big blocks (e.g. a 16x16 iMCU on an image of width 33); we won't
            // discard the extra data until colorspace conversion
            z->img_comp[i].w2 = z->img_mcu_x * z->img_comp[i].h * 8;
            z->img_comp[i].h2 = z->img_mcu_y * z->img_comp[i].v * 8;
            z->img_comp[i].raw_data = malloc(z->img_comp[i].w2 * z->img_comp[i].h2 + 15);

            if (z->img_comp[i].raw_data == NULL) {
                for (--i; i >= 0; --i) {
                    free(z->img_comp[i].raw_data);
                    z->img_comp[i].raw_data = NULL;
                }
                return err("outofmem", "Out of memory");
            }
            // align blocks for idct using mmx/sse
            z->img_comp[i].data = (unsigned char *) (((size_t) z->img_comp[i].raw_data + 15) & ~15);
            z->img_comp[i].linebuf = NULL;
            if (z->progressive) {
                z->img_comp[i].coeff_w = (z->img_comp[i].w2 + 7) >> 3;
                z->img_comp[i].coeff_h = (z->img_comp[i].h2 + 7) >> 3;
                z->img_comp[i].raw_coeff = malloc(
                        z->img_comp[i].coeff_w * z->img_comp[i].coeff_h * 64 * sizeof(short) + 15);
                z->img_comp[i].coeff = (short *) (((size_t) z->img_comp[i].raw_coeff + 15) & ~15);
            } else {
                z->img_comp[i].coeff = 0;
                z->img_comp[i].raw_coeff = 0;
            }
        }

        return 1;
    }

// use comparisons since in some cases we handle more than one case (e.g. SOF)
#define DNL(x)         ((x) == 0xdc)
#define SOI(x)         ((x) == 0xd8)
#define EOI(x)         ((x) == 0xd9)
#define SOF(x)         ((x) == 0xc0 || (x) == 0xc1 || (x) == 0xc2)
#define SOS(x)         ((x) == 0xda)

#define SOF_progressive(x)   ((x) == 0xc2)

    static int decode_jpeg_header(jpeg *z, int scan) {
        int m;
        z->marker = MARKER_none; // initialize cached marker to empty
        m = get_marker(z);
        if (!SOI(m))
            return err("no SOI", "Corrupt JPEG");
        if (scan == SCAN_type)
            return 1;
        m = get_marker(z);
        while (!SOF(m)) {
            if (!process_marker(z, m))
                return 0;
            m = get_marker(z);
            while (m == MARKER_none) {
                // some files have extra padding after their blocks, so ok, we'll scan
                if (at_eof(z->s))
                    return err("no SOF", "Corrupt JPEG");
                m = get_marker(z);
            }
        }
        z->progressive = SOF_progressive(m);
        if (!process_frame_header(z, scan))
            return 0;
        return 1;
    }

// decode image to YCbCr format
    static int decode_jpeg_image(jpeg *j) {
        int m;
        for (m = 0; m < 4; m++) {
            j->img_comp[m].raw_data = NULL;
            j->img_comp[m].raw_coeff = NULL;
        }
        j->restart_interval = 0;
        if (!decode_jpeg_header(j, SCAN_load))
            return 0;
        m = get_marker(j);
        while (!EOI(m)) {
            if (SOS(m)) {
                if (!process_scan_header(j))
                    return 0;
                if (!parse_entropy_coded_data(j))
                    return 0;
                if (j->marker == MARKER_none) {
                    // handle 0s at the end of image data from IP Kamera 9060
                    while (!at_eof(j->s)) {
                        int x = get8(j->s);
                        if (x == 255) {
                            j->marker = get8(j->s);
                            break;
                        } else if (x != 0) {
                            return err("junk before marker", "Corrupt JPEG");
                        }
                    }
                    // if we reach eof without hitting a marker, get_marker() below will fail and we'll eventually return 0
                }
            } else {
                if (!process_marker(j, m))
                    return 0;
            }
            m = get_marker(j);
        }
        if (j->progressive)
            jpeg_finish(j);
        return 1;
    }

// static jfif-centered resampling (across block boundaries)

    typedef unsigned char *(*resample_row_func)(unsigned char *out, unsigned char *in0,
                                                unsigned char *in1, int w, int hs);

#define div4(x) ((unsigned char) ((x) >> 2))

    static unsigned char *
    resample_row_1(unsigned char *out, unsigned char *in_near, unsigned char *in_far, int w,
                   int hs) {
        IGNORED(out);
        IGNORED(in_far);
        IGNORED(w);
        IGNORED(hs);
        return in_near;
    }

    static unsigned char *
    resample_row_v_2(unsigned char *out, unsigned char *in_near, unsigned char *in_far, int w,
                     int hs) {
        // need to generate two samples vertically for every one in input
        int i;
        IGNORED(hs);
        for (i = 0; i < w; ++i)
            out[i] = div4(3 * in_near[i] + in_far[i] + 2);
        return out;
    }

    static unsigned char *
    resample_row_h_2(unsigned char *out, unsigned char *in_near, unsigned char *in_far, int w,
                     int hs) {
        // need to generate two samples horizontally for every one in input
        int i;
        unsigned char *input = in_near;

        if (w == 1) {
            // if only one sample, can't do any interpolation
            out[0] = out[1] = input[0];
            return out;
        }

        out[0] = input[0];
        out[1] = div4(input[0] * 3 + input[1] + 2);
        for (i = 1; i < w - 1; ++i) {
            int n = 3 * input[i] + 2;
            out[i * 2 + 0] = div4(n + input[i - 1]);
            out[i * 2 + 1] = div4(n + input[i + 1]);
        }
        out[i * 2 + 0] = div4(input[w - 2] * 3 + input[w - 1] + 2);
        out[i * 2 + 1] = input[w - 1];

        IGNORED(in_far);
        IGNORED(hs);

        return out;
    }

#define div16(x) ((unsigned char) ((x) >> 4))

    static unsigned char *
    resample_row_hv_2(unsigned char *out, unsigned char *in_near, unsigned char *in_far,
                      int w, int hs) {
        // need to generate 2x2 samples for every one in input
        int i, t0, t1;
        if (w == 1) {
            out[0] = out[1] = div4(3 * in_near[0] + in_far[0] + 2);
            return out;
        }

        t1 = 3 * in_near[0] + in_far[0];
        out[0] = div4(t1 + 2);
        for (i = 1; i < w; ++i) {
            t0 = t1;
            t1 = 3 * in_near[i] + in_far[i];
            out[i * 2 - 1] = div16(3 * t0 + t1 + 8);
            out[i * 2] = div16(3 * t1 + t0 + 8);
        }
        out[w * 2 - 1] = div4(t1 + 2);

        IGNORED(hs);

        return out;
    }

#if defined(STBI_SSE2) || defined(STBI_NEON)

    static unsigned char *
    resample_row_hv_2_simd(unsigned char *out, unsigned char *in_near, unsigned char *in_far,
                           int w, int hs) {
        // need to generate 2x2 samples for every one in input
        int i = 0, t0, t1;

        if (w == 1) {
            out[0] = out[1] = div4(3 * in_near[0] + in_far[0] + 2);
            return out;
        }

        t1 = 3 * in_near[0] + in_far[0];
        // process groups of 8 pixels for as long as we can.
        // note we can't handle the last pixel in a row in this loop
        // because we need to handle the filter boundary conditions.
        for (; i < ((w - 1) & ~7); i += 8) {
#if defined(STBI_SSE2)
            // load and perform the vertical filtering pass
            // this uses 3*x + y = 4*x + (y - x)
            __m128i zero = _mm_setzero_si128();
            __m128i farb = _mm_loadl_epi64((__m128i *) (in_far + i));
            __m128i nearb = _mm_loadl_epi64((__m128i *) (in_near + i));
            __m128i farw = _mm_unpacklo_epi8(farb, zero);
            __m128i nearw = _mm_unpacklo_epi8(nearb, zero);
            __m128i diff = _mm_sub_epi16(farw, nearw);
            __m128i nears = _mm_slli_epi16(nearw, 2);
            __m128i curr = _mm_add_epi16(nears, diff);// current row

            // horizontal filter works the same based on shifted vers of current
            // row. "prev" is current row shifted right by 1 pixel; we need to
            // insert the previous pixel value (from t1).
            // "next" is current row shifted left by 1 pixel, with first pixel
            // of next block of 8 pixels added in.
            __m128i prv0 = _mm_slli_si128(curr, 2);
            __m128i nxt0 = _mm_srli_si128(curr, 2);
            __m128i prev = _mm_insert_epi16(prv0, t1, 0);
            __m128i next = _mm_insert_epi16(nxt0, 3 * in_near[i + 8] + in_far[i + 8], 7);

            // horizontal filter, polyphase implementation since it's convenient:
            // even pixels = 3*cur + prev = cur*4 + (prev - cur)
            // odd  pixels = 3*cur + next = cur*4 + (next - cur)
            // note the shared term.
            __m128i bias = _mm_set1_epi16(8);
            __m128i curs = _mm_slli_epi16(curr, 2);
            __m128i prvd = _mm_sub_epi16(prev, curr);
            __m128i nxtd = _mm_sub_epi16(next, curr);
            __m128i curb = _mm_add_epi16(curs, bias);
            __m128i even = _mm_add_epi16(prvd, curb);
            __m128i odd = _mm_add_epi16(nxtd, curb);

            // interleave even and odd pixels, then undo scaling.
            __m128i int0 = _mm_unpacklo_epi16(even, odd);
            __m128i int1 = _mm_unpackhi_epi16(even, odd);
            __m128i de0 = _mm_srli_epi16(int0, 4);
            __m128i de1 = _mm_srli_epi16(int1, 4);

            // pack and write output
            __m128i outv = _mm_packus_epi16(de0, de1);
            _mm_storeu_si128((__m128i *) (out + i * 2), outv);
#elif defined(STBI_NEON)
                                                                                                                                    // this uses 3*x + y = 4*x + (y - x)
		uint8x8_t farb = vld1_u8(in_far + i);
		uint8x8_t nearb = vld1_u8(in_near + i);
		int16x8_t diff = vreinterpretq_s16_u16(vsubl_u8(farb, nearb));
		int16x8_t nears = vreinterpretq_s16_u16(vshll_n_u8(nearb, 2));
		int16x8_t curr = vaddq_s16(nears, diff);// current row

		// horizontal filter works the same based on shifted vers of current
		// row. "prev" is current row shifted right by 1 pixel; we need to
		// insert the previous pixel value (from t1).
		// "next" is current row shifted left by 1 pixel, with first pixel
		// of next block of 8 pixels added in.
		int16x8_t prv0 = vextq_s16(curr, curr, 7);
		int16x8_t nxt0 = vextq_s16(curr, curr, 1);
		int16x8_t prev = vsetq_lane_s16(t1, prv0, 0);
		int16x8_t next = vsetq_lane_s16(3*in_near[i+8] + in_far[i+8], nxt0, 7);

		// horizontal filter, polyphase implementation since it's convenient:
		// even pixels = 3*cur + prev = cur*4 + (prev - cur)
		// odd  pixels = 3*cur + next = cur*4 + (next - cur)
		// note the shared term.
		int16x8_t curs = vshlq_n_s16(curr, 2);
		int16x8_t prvd = vsubq_s16(prev, curr);
		int16x8_t nxtd = vsubq_s16(next, curr);
		int16x8_t even = vaddq_s16(curs, prvd);
		int16x8_t odd = vaddq_s16(curs, nxtd);

		// undo scaling and round, then store with even/odd phases interleaved
		uint8x8x2_t o;
		o.val[0] = vqrshrun_n_s16(even, 4);
		o.val[1] = vqrshrun_n_s16(odd, 4);
		vst2_u8(out + i*2, o);
#endif

            // "previous" value for next iter
            t1 = 3 * in_near[i + 7] + in_far[i + 7];
        }

        t0 = t1;
        t1 = 3 * in_near[i] + in_far[i];
        out[i * 2] = div16(3 * t1 + t0 + 8);

        for (++i; i < w; ++i) {
            t0 = t1;
            t1 = 3 * in_near[i] + in_far[i];
            out[i * 2 - 1] = div16(3 * t0 + t1 + 8);
            out[i * 2] = div16(3 * t1 + t0 + 8);
        }
        out[w * 2 - 1] = div4(t1 + 2);

        IGNORED(hs);

        return out;
    }

#endif

    static unsigned char *
    resample_row_generic(unsigned char *out, unsigned char *in_near, unsigned char *in_far,
                         int w, int hs) {
        // resample with nearest-neighbor
        int i, j;
        IGNORED(in_far);
        for (i = 0; i < w; ++i)
            for (j = 0; j < hs; ++j)
                out[i * hs + j] = in_near[i];
        return out;
    }

#ifdef STBI_JPEG_OLD
                                                                                                                            // this is the same YCbCr-to-RGB calculation that stb_image has used
// historically before the algorithm changes in 1.49
#define float2fixed(x)  ((int) ((x) * 65536 + 0.5))
static void YCbCr_to_RGB_row(unsigned char *out, const unsigned char *y, const unsigned char *pcb, const unsigned char *pcr, int count, int step)
{
	int i;
	for (i=0; i < count; ++i)
	{
		int y_fixed = (y[i] << 16) + 32768; // rounding
		int r,g,b;
		int cr = pcr[i] - 128;
		int cb = pcb[i] - 128;
		r = y_fixed + cr*float2fixed(1.40200f);
		g = y_fixed - cr*float2fixed(0.71414f) - cb*float2fixed(0.34414f);
		b = y_fixed + cb*float2fixed(1.77200f);
		r >>= 16;
		g >>= 16;
		b >>= 16;
		if ((unsigned) r > 255)
		{
			if (r < 0) r = 0;
			else r = 255;
		}
		if ((unsigned) g > 255)
		{
			if (g < 0) g = 0;
			else g = 255;
		}
		if ((unsigned) b > 255)
		{
			if (b < 0) b = 0;
			else b = 255;
		}
		out[0] = (unsigned char)r;
		out[1] = (unsigned char)g;
		out[2] = (unsigned char)b;
		out[3] = 255;
		out += step;
	}
}
#else
// this is a reduced-precision calculation of YCbCr-to-RGB introduced
// to make sure the code produces the same results in both SIMD and scalar
#define float2fixed(x)  (((int) ((x) * 4096.0f + 0.5f)) << 8)

    static void
    YCbCr_to_RGB_row(unsigned char *out, const unsigned char *y, const unsigned char *pcb,
                     const unsigned char *pcr, int count, int step) {
        int i;
        for (i = 0; i < count; ++i) {
            int y_fixed = (y[i] << 20) + (1 << 19); // rounding
            int r, g, b;
            int cr = pcr[i] - 128;
            int cb = pcb[i] - 128;
            r = y_fixed + cr * float2fixed(1.40200f);
            g = y_fixed + (cr * -float2fixed(0.71414f)) +
                ((cb * -float2fixed(0.34414f)) & 0xffff0000);
            b = y_fixed + cb * float2fixed(1.77200f);
            r >>= 20;
            g >>= 20;
            b >>= 20;
            if ((unsigned) r > 255) {
                if (r < 0)
                    r = 0;
                else
                    r = 255;
            }
            if ((unsigned) g > 255) {
                if (g < 0)
                    g = 0;
                else
                    g = 255;
            }
            if ((unsigned) b > 255) {
                if (b < 0)
                    b = 0;
                else
                    b = 255;
            }
            out[0] = (unsigned char) r;
            out[1] = (unsigned char) g;
            out[2] = (unsigned char) b;
            out[3] = 255;
            out += step;
        }
    }

#endif

#if defined(STBI_SSE2) || defined(STBI_NEON)

    static void
    YCbCr_to_RGB_simd(unsigned char *out, unsigned char const *y, unsigned char const *pcb,
                      unsigned char const *pcr, int count, int step) {
        int i = 0;

#ifdef STBI_SSE2
        // step == 3 is pretty ugly on the final interleave, and i'm not convinced
        // it's useful in practice (you wouldn't use it for textures, for example).
        // so just accelerate step == 4 case.
        if (step == 4) {
            // this is a fairly straightforward implementation and not super-optimized.
            __m128i signflip = _mm_set1_epi8(-0x80);
            __m128i cr_const0 = _mm_set1_epi16((short) (1.40200f * 4096.0f + 0.5f));
            __m128i cr_const1 = _mm_set1_epi16(-(short) (0.71414f * 4096.0f + 0.5f));
            __m128i cb_const0 = _mm_set1_epi16(-(short) (0.34414f * 4096.0f + 0.5f));
            __m128i cb_const1 = _mm_set1_epi16((short) (1.77200f * 4096.0f + 0.5f));
            __m128i y_bias = _mm_set1_epi8((char) (unsigned char) 128);
            __m128i xw = _mm_set1_epi16(255);// alpha channel

            for (; i + 7 < count; i += 8) {
                // load
                __m128i y_bytes = _mm_loadl_epi64((__m128i *) (y + i));
                __m128i cr_bytes = _mm_loadl_epi64((__m128i *) (pcr + i));
                __m128i cb_bytes = _mm_loadl_epi64((__m128i *) (pcb + i));
                __m128i cr_biased = _mm_xor_si128(cr_bytes, signflip);// -128
                __m128i cb_biased = _mm_xor_si128(cb_bytes, signflip);// -128

                // unpack to short (and left-shift cr, cb by 8)
                __m128i yw = _mm_unpacklo_epi8(y_bias, y_bytes);
                __m128i crw = _mm_unpacklo_epi8(_mm_setzero_si128(), cr_biased);
                __m128i cbw = _mm_unpacklo_epi8(_mm_setzero_si128(), cb_biased);

                // color transform
                __m128i yws = _mm_srli_epi16(yw, 4);
                __m128i cr0 = _mm_mulhi_epi16(cr_const0, crw);
                __m128i cb0 = _mm_mulhi_epi16(cb_const0, cbw);
                __m128i cb1 = _mm_mulhi_epi16(cbw, cb_const1);
                __m128i cr1 = _mm_mulhi_epi16(crw, cr_const1);
                __m128i rws = _mm_add_epi16(cr0, yws);
                __m128i gwt = _mm_add_epi16(cb0, yws);
                __m128i bws = _mm_add_epi16(yws, cb1);
                __m128i gws = _mm_add_epi16(gwt, cr1);

                // descale
                __m128i rw = _mm_srai_epi16(rws, 4);
                __m128i bw = _mm_srai_epi16(bws, 4);
                __m128i gw = _mm_srai_epi16(gws, 4);

                // back to byte, set up for transpose
                __m128i brb = _mm_packus_epi16(rw, bw);
                __m128i gxb = _mm_packus_epi16(gw, xw);

                // transpose to interleave channels
                __m128i t0 = _mm_unpacklo_epi8(brb, gxb);
                __m128i t1 = _mm_unpackhi_epi8(brb, gxb);
                __m128i o0 = _mm_unpacklo_epi16(t0, t1);
                __m128i o1 = _mm_unpackhi_epi16(t0, t1);

                // store
                _mm_storeu_si128((__m128i *) (out + 0), o0);
                _mm_storeu_si128((__m128i *) (out + 16), o1);
                out += 32;
            }
        }
#endif

#ifdef STBI_NEON
                                                                                                                                // in this version, step=3 support would be easy to add. but is there
	// demand?
	if (step == 4)
	{
		// this is a fairly straightforward implementation and not super-optimized.
		uint8x8_t signflip = vdup_n_u8(0x80);
		int16x8_t cr_const0 = vdupq_n_s16( (short) ( 1.40200f*4096.0f+0.5f));
		int16x8_t cr_const1 = vdupq_n_s16( - (short) ( 0.71414f*4096.0f+0.5f));
		int16x8_t cb_const0 = vdupq_n_s16( - (short) ( 0.34414f*4096.0f+0.5f));
		int16x8_t cb_const1 = vdupq_n_s16( (short) ( 1.77200f*4096.0f+0.5f));

		for (; i+7 < count; i += 8)
		{
			// load
			uint8x8_t y_bytes = vld1_u8(y + i);
			uint8x8_t cr_bytes = vld1_u8(pcr + i);
			uint8x8_t cb_bytes = vld1_u8(pcb + i);
			int8x8_t cr_biased = vreinterpret_s8_u8(vsub_u8(cr_bytes, signflip));
			int8x8_t cb_biased = vreinterpret_s8_u8(vsub_u8(cb_bytes, signflip));

			// expand to s16
			int16x8_t yws = vreinterpretq_s16_u16(vshll_n_u8(y_bytes, 4));
			int16x8_t crw = vshll_n_s8(cr_biased, 7);
			int16x8_t cbw = vshll_n_s8(cb_biased, 7);

			// color transform
			int16x8_t cr0 = vqdmulhq_s16(crw, cr_const0);
			int16x8_t cb0 = vqdmulhq_s16(cbw, cb_const0);
			int16x8_t cr1 = vqdmulhq_s16(crw, cr_const1);
			int16x8_t cb1 = vqdmulhq_s16(cbw, cb_const1);
			int16x8_t rws = vaddq_s16(yws, cr0);
			int16x8_t gws = vaddq_s16(vaddq_s16(yws, cb0), cr1);
			int16x8_t bws = vaddq_s16(yws, cb1);

			// undo scaling, round, convert to byte
			uint8x8x4_t o;
			o.val[0] = vqrshrun_n_s16(rws, 4);
			o.val[1] = vqrshrun_n_s16(gws, 4);
			o.val[2] = vqrshrun_n_s16(bws, 4);
			o.val[3] = vdup_n_u8(255);

			// store, interleaving r/g/b/a
			vst4_u8(out, o);
			out += 8*4;
		}
	}
#endif

        for (; i < count; ++i) {
            int y_fixed = (y[i] << 20) + (1 << 19); // rounding
            int r, g, b;
            int cr = pcr[i] - 128;
            int cb = pcb[i] - 128;
            r = y_fixed + cr * float2fixed(1.40200f);
            g = y_fixed + cr * -float2fixed(0.71414f) +
                ((cb * -float2fixed(0.34414f)) & 0xffff0000);
            b = y_fixed + cb * float2fixed(1.77200f);
            r >>= 20;
            g >>= 20;
            b >>= 20;
            if ((unsigned) r > 255) {
                if (r < 0) r = 0;
                else r = 255;
            }
            if ((unsigned) g > 255) {
                if (g < 0) g = 0;
                else g = 255;
            }
            if ((unsigned) b > 255) {
                if (b < 0) b = 0;
                else b = 255;
            }
            out[0] = (unsigned char) r;
            out[1] = (unsigned char) g;
            out[2] = (unsigned char) b;
            out[3] = 255;
            out += step;
        }
    }

#endif

// set up the kernels
    static void setup_jpeg(jpeg *j) {
        j->idct_block_kernel = idct_block;
        j->YCbCr_to_RGB_kernel = YCbCr_to_RGB_row;
        j->resample_row_hv_2_kernel = resample_row_hv_2;

#ifdef STBI_SSE2
        if (sse2_available()) {
            j->idct_block_kernel = idct_simd;
#ifndef STBI_JPEG_OLD
            j->YCbCr_to_RGB_kernel = YCbCr_to_RGB_simd;
#endif
            j->resample_row_hv_2_kernel = resample_row_hv_2_simd;
        }
#endif

#ifdef STBI_NEON
                                                                                                                                j->idct_block_kernel = idct_simd;
#ifndef STBI_JPEG_OLD
	j->YCbCr_to_RGB_kernel = YCbCr_to_RGB_simd;
#endif
	j->resample_row_hv_2_kernel = resample_row_hv_2_simd;
#endif
    }

// clean up the temporary component buffers
    static void cleanup_jpeg(jpeg *j) {
        int i;
        for (i = 0; i < j->s->img_n; ++i) {
            if (j->img_comp[i].raw_data) {
                free(j->img_comp[i].raw_data);
                j->img_comp[i].raw_data = NULL;
                j->img_comp[i].data = NULL;
            }
            if (j->img_comp[i].raw_coeff) {
                free(j->img_comp[i].raw_coeff);
                j->img_comp[i].raw_coeff = 0;
                j->img_comp[i].coeff = 0;
            }
            if (j->img_comp[i].linebuf) {
                free(j->img_comp[i].linebuf);
                j->img_comp[i].linebuf = NULL;
            }
        }
    }

    struct resample {
        resample_row_func resample;
        unsigned char *line0, *line1;
        int hs, vs;   // expansion factor in each axis
        int w_lores; // horizontal pixels pre-expansion
        int ystep;   // how far through vertical expansion we are
        int ypos;    // which pre-expansion row we're on
    };

    static unsigned char *
    load_jpeg_image(jpeg *z, int *out_x, int *out_y, int *comp, int req_comp) {
        int n, decode_n;
        z->s->img_n = 0; // make cleanup_jpeg safe

        // validate req_comp
        if (req_comp < 0 || req_comp > 4) {
            err("bad req_comp", "Internal error");
            return 0;
        }

        // load a jpeg image from whichever source, but leave in YCbCr format
        if (!decode_jpeg_image(z)) {
            cleanup_jpeg(z);
            return NULL;
        }

        // determine actual number of components to generate
        n = req_comp ? req_comp : z->s->img_n;

        if (z->s->img_n == 3 && n < 3)
            decode_n = 1;
        else
            decode_n = z->s->img_n;

        // resample and color-convert
        {
            int k;
            unsigned int i, j;
            unsigned char *output;
            unsigned char *coutput[4];

            resample res_comp[4];

            for (k = 0; k < decode_n; ++k) {
                resample *r = &res_comp[k];

                // allocate line buffer big enough for upsampling off the edges
                // with upsample factor of 4
                z->img_comp[k].linebuf = (unsigned char *) malloc(z->s->img_x + 3);
                if (!z->img_comp[k].linebuf) {
                    cleanup_jpeg(z);
                    err("outofmem", "Out of memory");
                    return 0;
                }

                r->hs = z->img_h_max / z->img_comp[k].h;
                r->vs = z->img_v_max / z->img_comp[k].v;
                r->ystep = r->vs >> 1;
                r->w_lores = (z->s->img_x + r->hs - 1) / r->hs;
                r->ypos = 0;
                r->line0 = r->line1 = z->img_comp[k].data;

                if (r->hs == 1 && r->vs == 1)
                    r->resample = resample_row_1;
                else if (r->hs == 1 && r->vs == 2)
                    r->resample = resample_row_v_2;
                else if (r->hs == 2 && r->vs == 1)
                    r->resample = resample_row_h_2;
                else if (r->hs == 2 && r->vs == 2)
                    r->resample = z->resample_row_hv_2_kernel;
                else
                    r->resample = resample_row_generic;
            }

            // can't error after this so, this is safe
            output = (unsigned char *) malloc(n * z->s->img_x * z->s->img_y + 1);
            if (!output) {
                cleanup_jpeg(z);
                err("outofmem", "Out of memory");
                return 0;
            }

            // now go ahead and resample
            for (j = 0; j < z->s->img_y; ++j) {
                unsigned char *out = output + n * z->s->img_x * j;
                for (k = 0; k < decode_n; ++k) {
                    resample *r = &res_comp[k];
                    int y_bot = r->ystep >= (r->vs >> 1);
                    coutput[k] = r->resample(z->img_comp[k].linebuf,
                                             y_bot ? r->line1 : r->line0,
                                             y_bot ? r->line0 : r->line1, r->w_lores, r->hs);
                    if (++r->ystep >= r->vs) {
                        r->ystep = 0;
                        r->line0 = r->line1;
                        if (++r->ypos < z->img_comp[k].y)
                            r->line1 += z->img_comp[k].w2;
                    }
                }
                if (n >= 3) {
                    unsigned char *y = coutput[0];
                    if (z->s->img_n == 3) {
                        z->YCbCr_to_RGB_kernel(out, y, coutput[1], coutput[2], z->s->img_x, n);
                    } else
                        for (i = 0; i < z->s->img_x; ++i) {
                            out[0] = out[1] = out[2] = y[i];
                            out[3] = 255; // not used if n==3
                            out += n;
                        }
                } else {
                    unsigned char *y = coutput[0];
                    if (n == 1)
                        for (i = 0; i < z->s->img_x; ++i)
                            out[i] = y[i];
                    else
                        for (i = 0; i < z->s->img_x; ++i)
                            *out++ = y[i], *out++ = 255;
                }
            }
            cleanup_jpeg(z);
            *out_x = z->s->img_x;
            *out_y = z->s->img_y;
            if (comp)
                *comp = z->s->img_n; // report original components, not output
            return output;
        }
    }

    static unsigned char *
    jpeg_load(context *s, int *x, int *y, int *comp, int req_comp) {
        jpeg j;
        j.s = s;
        setup_jpeg(&j);
        return load_jpeg_image(&j, x, y, comp, req_comp);
    }

    static int jpeg_test(context *s) {
        int r;
        jpeg j;
        j.s = s;
        setup_jpeg(&j);
        r = decode_jpeg_header(&j, SCAN_type);
        rewind(s);
        return r;
    }

    static int jpeg_info_raw(jpeg *j, int *x, int *y, int *comp) {
        if (!decode_jpeg_header(j, SCAN_header)) {
            rewind(j->s);
            return 0;
        }
        if (x)
            *x = j->s->img_x;
        if (y)
            *y = j->s->img_y;
        if (comp)
            *comp = j->s->img_n;
        return 1;
    }

    static int jpeg_info(context *s, int *x, int *y, int *comp) {
        jpeg j;
        j.s = s;
        return jpeg_info_raw(&j, x, y, comp);
    }

#ifndef STBI_NO_ZLIB

// fast-way is faster to check than jpeg huffman, but slow way is slower
#define ZFAST_BITS  9 // accelerate all cases in default tables
#define ZFAST_MASK  ((1 << ZFAST_BITS) - 1)

// zlib-style huffman encoding
// (jpegs packs from left, zlib from right, so can't share code)
    struct zhuffman {
        uint16_t fast[1 << ZFAST_BITS];
        uint16_t firstcode[16];
        int maxcode[17];
        uint16_t firstsymbol[16];
        unsigned char size[288];
        uint16_t value[288];
    };

    inline static int bitreverse16(int n) {
        n = ((n & 0xAAAA) >> 1) | ((n & 0x5555) << 1);
        n = ((n & 0xCCCC) >> 2) | ((n & 0x3333) << 2);
        n = ((n & 0xF0F0) >> 4) | ((n & 0x0F0F) << 4);
        n = ((n & 0xFF00) >> 8) | ((n & 0x00FF) << 8);
        return n;
    }

    inline static int bit_reverse(int v, int bits) {
        assert(bits <= 16);
        // to bit reverse n bits, reverse 16 and shift
        // e.g. 11 bits, bit reverse and shift away 5
        return bitreverse16(v) >> (16 - bits);
    }

    static int zbuild_huffman(zhuffman *z, unsigned char *sizelist, int num) {
        int i, k = 0;
        int code, next_code[16], sizes[17];

        // DEFLATE spec for generating codes
        memset(sizes, 0, sizeof(sizes));
        memset(z->fast, 0, sizeof(z->fast));
        for (i = 0; i < num; ++i)
            ++sizes[sizelist[i]];
        sizes[0] = 0;
        for (i = 1; i < 16; ++i)
            if (sizes[i] > (1 << i))
                return err("bad sizes", "Corrupt PNG");
        code = 0;
        for (i = 1; i < 16; ++i) {
            next_code[i] = code;
            z->firstcode[i] = (uint16_t) code;
            z->firstsymbol[i] = (uint16_t) k;
            code = (code + sizes[i]);
            if (sizes[i])
                if (code - 1 >= (1 << i))
                    return err("bad codelengths", "Corrupt PNG");
            z->maxcode[i] = code << (16 - i); // preshift for inner loop
            code <<= 1;
            k += sizes[i];
        }
        z->maxcode[16] = 0x10000; // sentinel
        for (i = 0; i < num; ++i) {
            int s = sizelist[i];
            if (s) {
                int c = next_code[s] - z->firstcode[s] + z->firstsymbol[s];
                uint16_t fastv = (uint16_t) ((s << 9) | i);
                z->size[c] = (unsigned char) s;
                z->value[c] = (uint16_t) i;
                if (s <= ZFAST_BITS) {
                    int j = bit_reverse(next_code[s], s);
                    while (j < (1 << ZFAST_BITS)) {
                        z->fast[j] = fastv;
                        j += (1 << s);
                    }
                }
                ++next_code[s];
            }
        }
        return 1;
    }

// zlib-from-memory implementation for PNG reading
//    because PNG allows splitting the zlib stream arbitrarily,
//    and it's annoying structurally to have PNG call ZLIB call PNG,
//    we require PNG read all the IDATs and combine them into a single
//    memory buffer

    struct zbuf {
        unsigned char *zbuffer, *zbuffer_end;
        int num_bits;
        uint32_t code_buffer;

        char *zout;
        char *zout_start;
        char *zout_end;
        int z_expandable;

        zhuffman z_length, z_distance;
    };

    inline static unsigned char zget8(zbuf *z) {
        if (z->zbuffer >= z->zbuffer_end)
            return 0;
        return *z->zbuffer++;
    }

    static void fill_bits(zbuf *z) {
        do {
            assert(z->code_buffer < (1U << z->num_bits));
            z->code_buffer |= (unsigned int) zget8(z) << z->num_bits;
            z->num_bits += 8;
        } while (z->num_bits <= 24);
    }

    inline static unsigned int zreceive(zbuf *z, int n) {
        unsigned int k;
        if (z->num_bits < n)
            fill_bits(z);
        k = z->code_buffer & ((1 << n) - 1);
        z->code_buffer >>= n;
        z->num_bits -= n;
        return k;
    }

    static int zhuffman_decode_slowpath(zbuf *a, zhuffman *z) {
        int b, s, k;
        // not resolved by fast table, so compute it the slow way
        // use jpeg approach, which requires MSbits at top
        k = bit_reverse(a->code_buffer, 16);
        for (s = ZFAST_BITS + 1;; ++s)
            if (k < z->maxcode[s])
                break;
        if (s == 16)
            return -1; // invalid code!
        // code size is s, so:
        b = (k >> (16 - s)) - z->firstcode[s] + z->firstsymbol[s];
        assert(z->size[b] == s);
        a->code_buffer >>= s;
        a->num_bits -= s;
        return z->value[b];
    }

    inline static int zhuffman_decode(zbuf *a, zhuffman *z) {
        int b, s;
        if (a->num_bits < 16)
            fill_bits(a);
        b = z->fast[a->code_buffer & ZFAST_MASK];
        if (b) {
            s = b >> 9;
            a->code_buffer >>= s;
            a->num_bits -= s;
            return b & 511;
        }
        return zhuffman_decode_slowpath(a, z);
    }

    static int zexpand(zbuf *z, char *zout, int n) // need to make room for n bytes
    {
        char *q;
        int cur, limit;
        z->zout = zout;
        if (!z->z_expandable)
            return err("output buffer limit", "Corrupt PNG");
        cur = (int) (z->zout - z->zout_start);
        limit = (int) (z->zout_end - z->zout_start);
        while (cur + n > limit)
            limit *= 2;
        q = (char *) realloc(z->zout_start, limit);
        if (q == NULL)
            return err("outofmem", "Out of memory");
        z->zout_start = q;
        z->zout = q + cur;
        z->zout_end = q + limit;
        return 1;
    }

    static int zlength_base[31] = {3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 17, 19, 23, 27, 31,
                                   35, 43, 51, 59, 67, 83, 99, 115, 131, 163, 195, 227, 258,
                                   0, 0};

    static int zlength_extra[31] = {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3,
                                    3, 4, 4, 4, 4, 5, 5, 5, 5, 0, 0, 0};

    static int zdist_base[32] = {1, 2, 3, 4, 5, 7, 9, 13, 17, 25, 33, 49, 65, 97, 129, 193,
                                 257, 385, 513, 769, 1025, 1537, 2049, 3073, 4097, 6145, 8193,
                                 12289, 16385, 24577, 0, 0};

    static int zdist_extra[32] = {0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8,
                                  9, 9, 10, 10, 11, 11, 12, 12, 13, 13};

    static int parse_huffman_block(zbuf *a) {
        char *zout = a->zout;
        for (;;) {
            int z = zhuffman_decode(a, &a->z_length);
            if (z < 256) {
                if (z < 0)
                    return err("bad huffman code", "Corrupt PNG"); // error in huffman codes
                if (zout >= a->zout_end) {
                    if (!zexpand(a, zout, 1))
                        return 0;
                    zout = a->zout;
                }
                *zout++ = (char) z;
            } else {
                unsigned char *p;
                int len, dist;
                if (z == 256) {
                    a->zout = zout;
                    return 1;
                }
                z -= 257;
                len = zlength_base[z];
                if (zlength_extra[z])
                    len += zreceive(a, zlength_extra[z]);
                z = zhuffman_decode(a, &a->z_distance);
                if (z < 0)
                    return err("bad huffman code", "Corrupt PNG");
                dist = zdist_base[z];
                if (zdist_extra[z])
                    dist += zreceive(a, zdist_extra[z]);
                if (zout - a->zout_start < dist)
                    return err("bad dist", "Corrupt PNG");
                if (zout + len > a->zout_end) {
                    if (!zexpand(a, zout, len))
                        return 0;
                    zout = a->zout;
                }
                p = (unsigned char *) (zout - dist);
                if (dist == 1)   // run of one byte; common in images.
                {
                    unsigned char v = *p;
                    if (len) {
                        do
                            *zout++ = v;
                        while (--len);
                    }
                } else {
                    if (len) {
                        do
                            *zout++ = *p++;
                        while (--len);
                    }
                }
            }
        }
    }

    static int compute_huffman_codes(zbuf *a) {
        static unsigned char length_dezigzag[19] = {16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3,
                                                    13, 2, 14, 1, 15};
        zhuffman z_codelength;
        unsigned char lencodes[286 + 32 + 137]; //padding for maximum single op
        unsigned char codelength_sizes[19];
        int i, n;

        int hlit = zreceive(a, 5) + 257;
        int hdist = zreceive(a, 5) + 1;
        int hclen = zreceive(a, 4) + 4;

        memset(codelength_sizes, 0, sizeof(codelength_sizes));
        for (i = 0; i < hclen; ++i) {
            int s = zreceive(a, 3);
            codelength_sizes[length_dezigzag[i]] = (unsigned char) s;
        }
        if (!zbuild_huffman(&z_codelength, codelength_sizes, 19))
            return 0;

        n = 0;
        while (n < hlit + hdist) {
            int c = zhuffman_decode(a, &z_codelength);
            if (c < 0 || c >= 19)
                return err("bad codelengths", "Corrupt PNG");
            if (c < 16)
                lencodes[n++] = (unsigned char) c;
            else if (c == 16) {
                c = zreceive(a, 2) + 3;
                memset(lencodes + n, lencodes[n - 1], c);
                n += c;
            } else if (c == 17) {
                c = zreceive(a, 3) + 3;
                memset(lencodes + n, 0, c);
                n += c;
            } else {
                assert(c == 18);
                c = zreceive(a, 7) + 11;
                memset(lencodes + n, 0, c);
                n += c;
            }
        }
        if (n != hlit + hdist)
            return err("bad codelengths", "Corrupt PNG");
        if (!zbuild_huffman(&a->z_length, lencodes, hlit))
            return 0;
        if (!zbuild_huffman(&a->z_distance, lencodes + hlit, hdist))
            return 0;
        return 1;
    }

    static int parse_uncomperssed_block(zbuf *a) {
        unsigned char header[4];
        int len, nlen, k;
        if (a->num_bits & 7)
            zreceive(a, a->num_bits & 7); // discard
        // drain the bit-packed data into header
        k = 0;
        while (a->num_bits > 0) {
            header[k++] = (unsigned char) (a->code_buffer & 255); // suppress MSVC run-time check
            a->code_buffer >>= 8;
            a->num_bits -= 8;
        }
        assert(a->num_bits == 0);
        // now fill header the normal way
        while (k < 4)
            header[k++] = zget8(a);
        len = header[1] * 256 + header[0];
        nlen = header[3] * 256 + header[2];
        if (nlen != (len ^ 0xffff))
            return err("zlib corrupt", "Corrupt PNG");
        if (a->zbuffer + len > a->zbuffer_end)
            return err("read past buffer", "Corrupt PNG");
        if (a->zout + len > a->zout_end)
            if (!zexpand(a, a->zout, len))
                return 0;
        memcpy(a->zout, a->zbuffer, len);
        a->zbuffer += len;
        a->zout += len;
        return 1;
    }

    static int parse_zlib_header(zbuf *a) {
        int cmf = zget8(a);
        int cm = cmf & 15;
        /* int cinfo = cmf >> 4; */
        int flg = zget8(a);
        if ((cmf * 256 + flg) % 31 != 0)
            return err("bad zlib header", "Corrupt PNG"); // zlib spec
        if (flg & 32)
            return err("no preset dict",
                       "Corrupt PNG"); // preset dictionary not allowed in png
        if (cm != 8)
            return err("bad compression", "Corrupt PNG"); // DEFLATE required for png
        // window = 1 << (8 + cinfo)... but who cares, we fully buffer output
        return 1;
    }

    static unsigned char zdefault_length[288], zdefault_distance[32];

    static void initialize() {
        //decompresser static data to initialize
        int i;   // use <= to match clearly with spec
        for (i = 0; i <= 143; ++i)
            zdefault_length[i] = 8;
        for (; i <= 255; ++i)
            zdefault_length[i] = 9;
        for (; i <= 279; ++i)
            zdefault_length[i] = 7;
        for (; i <= 287; ++i)
            zdefault_length[i] = 8;
        for (i = 0; i <= 31; ++i)
            zdefault_distance[i] = 5;
    }

    static int parse_zlib(zbuf *a, int parse_header) {
        int final, type;
        if (parse_header)
            if (!parse_zlib_header(a))
                return 0;
        a->num_bits = 0;
        a->code_buffer = 0;
        do {
            final = zreceive(a, 1);
            type = zreceive(a, 2);
            if (type == 0) {
                if (!parse_uncomperssed_block(a))
                    return 0;
            } else if (type == 3) {
                return 0;
            } else {
                if (type == 1) {
                    if (!zbuild_huffman(&a->z_length, zdefault_length, 288))
                        return 0;
                    if (!zbuild_huffman(&a->z_distance, zdefault_distance, 32))
                        return 0;
                } else {
                    if (!compute_huffman_codes(a))
                        return 0;
                }
                if (!parse_huffman_block(a))
                    return 0;
            }
        } while (!final);
        return 1;
    }

    static int do_zlib(zbuf *a, char *obuf, int olen, int exp, int parse_header) {
        a->zout_start = obuf;
        a->zout = obuf;
        a->zout_end = obuf + olen;
        a->z_expandable = exp;

        return parse_zlib(a, parse_header);
    }

    char *
    stbi_zlib_decode_malloc_guesssize(const char *buffer, int len, int initial_size, int *outlen) {
        zbuf a;
        char *p = (char *) malloc(initial_size);
        if (p == NULL)
            return NULL;
        a.zbuffer = (unsigned char *) buffer;
        a.zbuffer_end = (unsigned char *) buffer + len;
        if (do_zlib(&a, p, initial_size, 1, 1)) {
            if (outlen)
                *outlen = (int) (a.zout - a.zout_start);
            return a.zout_start;
        } else {
            free(a.zout_start);
            return NULL;
        }
    }

    char *stbi_zlib_decode_malloc(char const *buffer, int len, int *outlen) {
        return stbi_zlib_decode_malloc_guesssize(buffer, len, 16384, outlen);
    }

    char *
    stbi_zlib_decode_malloc_guesssize_headerflag(const char *buffer, int len, int initial_size,
                                                 int *outlen, int parse_header) {
        zbuf a;
        char *p = (char *) malloc(initial_size);
        if (p == NULL)
            return NULL;
        a.zbuffer = (unsigned char *) buffer;
        a.zbuffer_end = (unsigned char *) buffer + len;
        if (do_zlib(&a, p, initial_size, 1, parse_header)) {
            if (outlen)
                *outlen = (int) (a.zout - a.zout_start);
            return a.zout_start;
        } else {
            free(a.zout_start);
            return NULL;
        }
    }

    int stbi_zlib_decode_buffer(char *obuffer, int olen, char const *ibuffer, int ilen) {
        zbuf a;
        a.zbuffer = (unsigned char *) ibuffer;
        a.zbuffer_end = (unsigned char *) ibuffer + ilen;
        if (do_zlib(&a, obuffer, olen, 0, 1))
            return (int) (a.zout - a.zout_start);
        else
            return -1;
    }

    char *stbi_zlib_decode_noheader_malloc(char const *buffer, int len, int *outlen) {
        zbuf a;
        char *p = (char *) malloc(16384);
        if (p == NULL)
            return NULL;
        a.zbuffer = (unsigned char *) buffer;
        a.zbuffer_end = (unsigned char *) buffer + len;
        if (do_zlib(&a, p, 16384, 1, 0)) {
            if (outlen)
                *outlen = (int) (a.zout - a.zout_start);
            return a.zout_start;
        } else {
            free(a.zout_start);
            return NULL;
        }
    }

    int stbi_zlib_decode_noheader_buffer(char *obuffer, int olen, const char *ibuffer, int ilen) {
        zbuf a;
        a.zbuffer = (unsigned char *) ibuffer;
        a.zbuffer_end = (unsigned char *) ibuffer + ilen;
        if (do_zlib(&a, obuffer, olen, 0, 0))
            return (int) (a.zout - a.zout_start);
        else
            return -1;
    }

#endif

    struct pngchunk {
        uint32_t length;
        uint32_t type;
    };

    static pngchunk get_chunk_header(context *s) {
        pngchunk c;
        c.length = get32be(s);
        c.type = get32be(s);
        return c;
    }

    static int check_png_header(context *s) {
        static unsigned char png_sig[8] = {137, 80, 78, 71, 13, 10, 26, 10};
        int i;
        for (i = 0; i < 8; ++i)
            if (get8(s) != png_sig[i])
                return err("bad png sig", "Not a PNG");
        return 1;
    }

    struct png {
        context *s;
        unsigned char *idata, *expanded, *out;
    };

    enum {
        F_none = 0,
        F_sub = 1,
        F_up = 2,
        F_avg = 3,
        F_paeth = 4, // synthetic filters used for first scanline to avoid needing a dummy row of 0s
        F_avg_first,
        F_paeth_first
    };

    static unsigned char first_row_filter[5] = {F_none, F_sub, F_none,
                                                F_avg_first, F_paeth_first};

    static int paeth(int a, int b, int c) {
        int p = a + b - c;
        int pa = abs(p - a);
        int pb = abs(p - b);
        int pc = abs(p - c);
        if (pa <= pb && pa <= pc)
            return a;
        if (pb <= pc)
            return b;
        return c;
    }

    static unsigned char depth_scale_table[9] = {0, 0xff, 0x55, 0, 0x11, 0, 0, 0, 0x01};

// create the png data from post-deflated data
    static int
    create_png_image_raw(png *a, unsigned char *raw, uint32_t raw_len, int out_n,
                         uint32_t x, uint32_t y, int depth, int color) {
        context *s = a->s;
        uint32_t i, j, stride = x * out_n;
        uint32_t img_len, img_width_bytes;
        int k;
        int img_n = s->img_n; // copy it into a local for later

        assert(out_n == s->img_n || out_n == s->img_n + 1);
        a->out = (unsigned char *) malloc(
                x * y * out_n); // extra bytes to write off the end into
        if (!a->out)
            return err("outofmem", "Out of memory");

        img_width_bytes = (((img_n * x * depth) + 7) >> 3);
        img_len = (img_width_bytes + 1) * y;
        if (s->img_x == x && s->img_y == y) {
            if (raw_len != img_len)
                return err("not enough pixels", "Corrupt PNG");
        } else     // interlaced:
        {
            if (raw_len < img_len)
                return err("not enough pixels", "Corrupt PNG");
        }

        for (j = 0; j < y; ++j) {
            unsigned char *cur = a->out + stride * j;
            unsigned char *prior = cur - stride;
            int filter = *raw++;
            int filter_bytes = img_n;
            int width = x;
            if (filter > 4)
                return err("invalid filter", "Corrupt PNG");

            if (depth < 8) {
                assert(img_width_bytes <= x);
                cur += x * out_n -
                       img_width_bytes; // store output to the rightmost img_len bytes, so we can decode in place
                filter_bytes = 1;
                width = img_width_bytes;
            }

            // if first row, use special filter that doesn't sample previous row
            if (j == 0)
                filter = first_row_filter[filter];

            // handle first byte explicitly
            for (k = 0; k < filter_bytes; ++k) {
                switch (filter) {
                    case F_none:
                        cur[k] = raw[k];
                        break;
                    case F_sub:
                        cur[k] = raw[k];
                        break;
                    case F_up:
                        cur[k] = BYTECAST(raw[k] + prior[k]);
                        break;
                    case F_avg:
                        cur[k] = BYTECAST(raw[k] + (prior[k] >> 1));
                        break;
                    case F_paeth:
                        cur[k] = BYTECAST(raw[k] + paeth(0, prior[k], 0));
                        break;
                    case F_avg_first:
                        cur[k] = raw[k];
                        break;
                    case F_paeth_first:
                        cur[k] = raw[k];
                        break;
                }
            }

            if (depth == 8) {
                if (img_n != out_n)
                    cur[img_n] = 255; // first pixel
                raw += img_n;
                cur += out_n;
                prior += out_n;
            } else {
                raw += 1;
                cur += 1;
                prior += 1;
            }

            if (depth < 8 || img_n == out_n) {
                int nk = (width - 1) * img_n;
                switch (filter) {
                    case F_none:
                        memcpy(cur, raw, nk);
                        break;
                    case F_sub:
                        for (k = 0; k < nk; ++k)
                            cur[k] = BYTECAST(raw[k] + cur[k - filter_bytes]);
                        break;
                    case F_up:
                        for (k = 0; k < nk; ++k)
                            cur[k] = BYTECAST(raw[k] + prior[k]);
                        break;
                    case F_avg:
                        for (k = 0; k < nk; ++k)
                            cur[k] = BYTECAST(
                                    raw[k] + ((prior[k] + cur[k - filter_bytes]) >> 1));
                        break;
                    case F_paeth:
                        for (k = 0; k < nk; ++k)
                            cur[k] = BYTECAST(raw[k] +
                                              paeth(cur[k - filter_bytes], prior[k],
                                                    prior[k - filter_bytes]));
                        break;
                    case F_avg_first:
                        for (k = 0; k < nk; ++k)
                            cur[k] = BYTECAST(raw[k] + (cur[k - filter_bytes] >> 1));
                        break;
                    case F_paeth_first:
                        for (k = 0; k < nk; ++k)
                            cur[k] = BYTECAST(
                                    raw[k] + paeth(cur[k - filter_bytes], 0, 0));
                        break;
                }
                raw += nk;
            } else {
                assert(img_n + 1 == out_n);
                switch (filter) {
                    case F_none:
                        for (i = x - 1; i >= 1;
                             --i, cur[img_n] = 255, raw += img_n, cur += out_n, prior += out_n)
                            for (k = 0; k < img_n; ++k)
                                cur[k] = raw[k];
                        break;
                    case F_sub:
                        for (i = x - 1; i >= 1;
                             --i, cur[img_n] = 255, raw += img_n, cur += out_n, prior += out_n)
                            for (k = 0; k < img_n; ++k)
                                cur[k] = BYTECAST(raw[k] + cur[k - out_n]);
                        break;
                    case F_up:
                        for (i = x - 1; i >= 1;
                             --i, cur[img_n] = 255, raw += img_n, cur += out_n, prior += out_n)
                            for (k = 0; k < img_n; ++k)
                                cur[k] = BYTECAST(raw[k] + prior[k]);
                        break;
                    case F_avg:
                        for (i = x - 1; i >= 1;
                             --i, cur[img_n] = 255, raw += img_n, cur += out_n, prior += out_n)
                            for (k = 0; k < img_n; ++k)
                                cur[k] = BYTECAST(
                                        raw[k] + ((prior[k] + cur[k - out_n]) >> 1));
                        break;
                    case F_paeth:
                        for (i = x - 1; i >= 1;
                             --i, cur[img_n] = 255, raw += img_n, cur += out_n, prior += out_n)
                            for (k = 0; k < img_n; ++k)
                                cur[k] = BYTECAST(raw[k] +
                                                  paeth(cur[k - out_n], prior[k],
                                                        prior[k - out_n]));
                        break;
                    case F_avg_first:
                        for (i = x - 1; i >= 1;
                             --i, cur[img_n] = 255, raw += img_n, cur += out_n, prior += out_n)
                            for (k = 0; k < img_n; ++k)
                                cur[k] = BYTECAST(raw[k] + (cur[k - out_n] >> 1));
                        break;
                    case F_paeth_first:
                        for (i = x - 1; i >= 1;
                             --i, cur[img_n] = 255, raw += img_n, cur += out_n, prior += out_n)
                            for (k = 0; k < img_n; ++k)
                                cur[k] = BYTECAST(raw[k] + paeth(cur[k - out_n], 0, 0));
                        break;
                }
            }
        }
        if (depth < 8) {
            for (j = 0; j < y; ++j) {
                unsigned char *cur = a->out + stride * j;
                unsigned char *in = a->out + stride * j + x * out_n - img_width_bytes;
                unsigned char scale =
                        (color == 0) ? depth_scale_table[depth] : 1;

                if (depth == 4) {
                    for (k = x * img_n; k >= 2; k -= 2, ++in) {
                        *cur++ = scale * ((*in >> 4));
                        *cur++ = scale * ((*in) & 0x0f);
                    }
                    if (k > 0)
                        *cur++ = scale * ((*in >> 4));
                } else if (depth == 2) {
                    for (k = x * img_n; k >= 4; k -= 4, ++in) {
                        *cur++ = scale * ((*in >> 6));
                        *cur++ = scale * ((*in >> 4) & 0x03);
                        *cur++ = scale * ((*in >> 2) & 0x03);
                        *cur++ = scale * ((*in) & 0x03);
                    }
                    if (k > 0)
                        *cur++ = scale * ((*in >> 6));
                    if (k > 1)
                        *cur++ = scale * ((*in >> 4) & 0x03);
                    if (k > 2)
                        *cur++ = scale * ((*in >> 2) & 0x03);
                } else if (depth == 1) {
                    for (k = x * img_n; k >= 8; k -= 8, ++in) {
                        *cur++ = scale * ((*in >> 7));
                        *cur++ = scale * ((*in >> 6) & 0x01);
                        *cur++ = scale * ((*in >> 5) & 0x01);
                        *cur++ = scale * ((*in >> 4) & 0x01);
                        *cur++ = scale * ((*in >> 3) & 0x01);
                        *cur++ = scale * ((*in >> 2) & 0x01);
                        *cur++ = scale * ((*in >> 1) & 0x01);
                        *cur++ = scale * ((*in) & 0x01);
                    }
                    if (k > 0)
                        *cur++ = scale * ((*in >> 7));
                    if (k > 1)
                        *cur++ = scale * ((*in >> 6) & 0x01);
                    if (k > 2)
                        *cur++ = scale * ((*in >> 5) & 0x01);
                    if (k > 3)
                        *cur++ = scale * ((*in >> 4) & 0x01);
                    if (k > 4)
                        *cur++ = scale * ((*in >> 3) & 0x01);
                    if (k > 5)
                        *cur++ = scale * ((*in >> 2) & 0x01);
                    if (k > 6)
                        *cur++ = scale * ((*in >> 1) & 0x01);
                }
                if (img_n != out_n) {
                    int q;
                    // insert alpha = 255
                    cur = a->out + stride * j;
                    if (img_n == 1) {
                        for (q = x - 1; q >= 0; --q) {
                            cur[q * 2 + 1] = 255;
                            cur[q * 2 + 0] = cur[q];
                        }
                    } else {
                        assert(img_n == 3);
                        for (q = x - 1; q >= 0; --q) {
                            cur[q * 4 + 3] = 255;
                            cur[q * 4 + 2] = cur[q * 3 + 2];
                            cur[q * 4 + 1] = cur[q * 3 + 1];
                            cur[q * 4 + 0] = cur[q * 3 + 0];
                        }
                    }
                }
            }
        }

        return 1;
    }

    static int
    create_png_image(png *a, unsigned char *image_data, uint32_t image_data_len,
                     int out_n, int depth, int color, int interlaced) {
        unsigned char *final;
        int p;
        if (!interlaced)
            return create_png_image_raw(a, image_data, image_data_len, out_n, a->s->img_x,
                                        a->s->img_y, depth, color);

        // de-interlacing
        final = (unsigned char *) malloc(a->s->img_x * a->s->img_y * out_n);
        for (p = 0; p < 7; ++p) {
            int xorig[] = {0, 4, 0, 2, 0, 1, 0};
            int yorig[] = {0, 0, 4, 0, 2, 0, 1};
            int xspc[] = {8, 8, 4, 4, 2, 2, 1};
            int yspc[] = {8, 8, 8, 4, 4, 2, 2};
            int i, j, x, y;
            // pass1_x[4] = 0, pass1_x[5] = 1, pass1_x[12] = 1
            x = (a->s->img_x - xorig[p] + xspc[p] - 1) / xspc[p];
            y = (a->s->img_y - yorig[p] + yspc[p] - 1) / yspc[p];
            if (x && y) {
                uint32_t img_len = ((((a->s->img_n * x * depth) + 7) >> 3) + 1) * y;
                if (!create_png_image_raw(a, image_data, image_data_len, out_n, x, y, depth,
                                          color)) {
                    free(final);
                    return 0;
                }
                for (j = 0; j < y; ++j) {
                    for (i = 0; i < x; ++i) {
                        int out_y = j * yspc[p] + yorig[p];
                        int out_x = i * xspc[p] + xorig[p];
                        memcpy(final + out_y * a->s->img_x * out_n + out_x * out_n,
                               a->out + (j * x + i) * out_n, out_n);
                    }
                }
                free(a->out);
                image_data += img_len;
                image_data_len -= img_len;
            }
        }
        a->out = final;

        return 1;
    }

    static int compute_transparency(png *z, unsigned char tc[3], int out_n) {
        context *s = z->s;
        uint32_t i, pixel_count = s->img_x * s->img_y;
        unsigned char *p = z->out;

        // compute color-based transparency, assuming we've
        // already got 255 as the alpha value in the output
        assert(out_n == 2 || out_n == 4);

        if (out_n == 2) {
            for (i = 0; i < pixel_count; ++i) {
                p[1] = (p[0] == tc[0] ? 0 : 255);
                p += 2;
            }
        } else {
            for (i = 0; i < pixel_count; ++i) {
                if (p[0] == tc[0] && p[1] == tc[1] && p[2] == tc[2])
                    p[3] = 0;
                p += 4;
            }
        }
        return 1;
    }

    static int
    expand_png_palette(png *a, unsigned char *palette, int len, int pal_img_n) {
        uint32_t i, pixel_count = a->s->img_x * a->s->img_y;
        unsigned char *p, *temp_out, *orig = a->out;

        p = (unsigned char *) malloc(pixel_count * pal_img_n);
        if (p == NULL)
            return err("outofmem", "Out of memory");

        // between here and free(out) below, exitting would leak
        temp_out = p;

        if (pal_img_n == 3) {
            for (i = 0; i < pixel_count; ++i) {
                int n = orig[i] * 4;
                p[0] = palette[n];
                p[1] = palette[n + 1];
                p[2] = palette[n + 2];
                p += 3;
            }
        } else {
            for (i = 0; i < pixel_count; ++i) {
                int n = orig[i] * 4;
                p[0] = palette[n];
                p[1] = palette[n + 1];
                p[2] = palette[n + 2];
                p[3] = palette[n + 3];
                p += 4;
            }
        }
        free(a->out);
        a->out = temp_out;

        IGNORED(len);

        return 1;
    }

    static int unpremultiply_on_load = 0;
    static int de_iphone_flag = 0;

    void stbi_set_unpremultiply_on_load(int flag_true_if_should_unpremultiply) {
        unpremultiply_on_load = flag_true_if_should_unpremultiply;
    }

    void stbi_convert_iphone_png_to_rgb(int flag_true_if_should_convert) {
        de_iphone_flag = flag_true_if_should_convert;
    }

    static void de_iphone(png *z) {
        context *s = z->s;
        uint32_t i, pixel_count = s->img_x * s->img_y;
        unsigned char *p = z->out;

        if (s->img_out_n == 3)    // convert bgr to rgb
        {
            for (i = 0; i < pixel_count; ++i) {
                unsigned char t = p[0];
                p[0] = p[2];
                p[2] = t;
                p += 3;
            }
        } else {
            assert(s->img_out_n == 4);
            if (unpremultiply_on_load) {
                // convert bgr to rgb and unpremultiply
                for (i = 0; i < pixel_count; ++i) {
                    unsigned char a = p[3];
                    unsigned char t = p[0];
                    if (a) {
                        p[0] = p[2] * 255 / a;
                        p[1] = p[1] * 255 / a;
                        p[2] = t * 255 / a;
                    } else {
                        p[0] = p[2];
                        p[2] = t;
                    }
                    p += 4;
                }
            } else {
                // convert bgr to rgb
                for (i = 0; i < pixel_count; ++i) {
                    unsigned char t = p[0];
                    p[0] = p[2];
                    p[2] = t;
                    p += 4;
                }
            }
        }
    }

#define PNG_TYPE(a, b, c, d)  (((a) << 24) + ((b) << 16) + ((c) << 8) + (d))

    static int parse_png_file(png *z, int scan, int req_comp) {
        unsigned char palette[1024], pal_img_n = 0;
        unsigned char has_trans = 0, tc[3];
        uint32_t ioff = 0, idata_limit = 0, i, pal_len = 0;
        int first = 1, k, interlace = 0, color = 0, depth = 0, is_iphone = 0;
        context *s = z->s;

        z->expanded = NULL;
        z->idata = NULL;
        z->out = NULL;

        if (!check_png_header(s))
            return 0;

        if (scan == SCAN_type)
            return 1;

        for (;;) {
            pngchunk c = get_chunk_header(s);
            switch (c.type) {
                case PNG_TYPE('C', 'g', 'B', 'I'):
                    is_iphone = 1;
                    skip(s, c.length);
                    break;
                case PNG_TYPE('I', 'H', 'D', 'R'): {
                    int comp, filter;
                    if (!first)
                        return err("multiple IHDR", "Corrupt PNG");
                    first = 0;
                    if (c.length != 13)
                        return err("bad IHDR len", "Corrupt PNG");
                    s->img_x = get32be(s);
                    if (s->img_x > (1 << 24))
                        return err("too large", "Very large image (corrupt?)");
                    s->img_y = get32be(s);
                    if (s->img_y > (1 << 24))
                        return err("too large", "Very large image (corrupt?)");
                    depth = get8(s);
                    if (depth != 1 && depth != 2 && depth != 4 && depth != 8)
                        return err("1/2/4/8-bit only", "PNG not supported: 1/2/4/8-bit only");
                    color = get8(s);
                    if (color > 6)
                        return err("bad ctype", "Corrupt PNG");
                    if (color == 3)
                        pal_img_n = 3;
                    else if (color & 1)
                        return err("bad ctype", "Corrupt PNG");
                    comp = get8(s);
                    if (comp)
                        return err("bad comp method", "Corrupt PNG");
                    filter = get8(s);
                    if (filter)
                        return err("bad filter method", "Corrupt PNG");
                    interlace = get8(s);
                    if (interlace > 1)
                        return err("bad interlace method", "Corrupt PNG");
                    if (!s->img_x || !s->img_y)
                        return err("0-pixel image", "Corrupt PNG");
                    if (!pal_img_n) {
                        s->img_n = (color & 2 ? 3 : 1) + (color & 4 ? 1 : 0);
                        if ((1 << 30) / s->img_x / s->img_n < s->img_y)
                            return err("too large", "Image too large to decode");
                        if (scan == SCAN_header)
                            return 1;
                    } else {
                        // if paletted, then pal_n is our final components, and
                        // img_n is # components to decompress/filter.
                        s->img_n = 1;
                        if ((1 << 30) / s->img_x / 4 < s->img_y)
                            return err("too large", "Corrupt PNG");
                        // if SCAN_header, have to scan to see if we have a tRNS
                    }
                    break;
                }

                case PNG_TYPE('P', 'L', 'T', 'E'): {
                    if (first)
                        return err("first not IHDR", "Corrupt PNG");
                    if (c.length > 256 * 3)
                        return err("invalid PLTE", "Corrupt PNG");
                    pal_len = c.length / 3;
                    if (pal_len * 3 != c.length)
                        return err("invalid PLTE", "Corrupt PNG");
                    for (i = 0; i < pal_len; ++i) {
                        palette[i * 4 + 0] = get8(s);
                        palette[i * 4 + 1] = get8(s);
                        palette[i * 4 + 2] = get8(s);
                        palette[i * 4 + 3] = 255;
                    }
                    break;
                }

                case PNG_TYPE('t', 'R', 'N', 'S'): {
                    if (first)
                        return err("first not IHDR", "Corrupt PNG");
                    if (z->idata)
                        return err("tRNS after IDAT", "Corrupt PNG");
                    if (pal_img_n) {
                        if (scan == SCAN_header) {
                            s->img_n = 4;
                            return 1;
                        }
                        if (pal_len == 0)
                            return err("tRNS before PLTE", "Corrupt PNG");
                        if (c.length > pal_len)
                            return err("bad tRNS len", "Corrupt PNG");
                        pal_img_n = 4;
                        for (i = 0; i < c.length; ++i)
                            palette[i * 4 + 3] = get8(s);
                    } else {
                        if (!(s->img_n & 1))
                            return err("tRNS with alpha", "Corrupt PNG");
                        if (c.length != (uint32_t) s->img_n * 2)
                            return err("bad tRNS len", "Corrupt PNG");
                        has_trans = 1;
                        for (k = 0; k < s->img_n; ++k)
                            tc[k] = (unsigned char) (get16be(s) & 255) *
                                    depth_scale_table[depth]; // non 8-bit images will be larger
                    }
                    break;
                }

                case PNG_TYPE('I', 'D', 'A', 'T'): {
                    if (first)
                        return err("first not IHDR", "Corrupt PNG");
                    if (pal_img_n && !pal_len)
                        return err("no PLTE", "Corrupt PNG");
                    if (scan == SCAN_header) {
                        s->img_n = pal_img_n;
                        return 1;
                    }
                    if ((int) (ioff + c.length) < (int) ioff)
                        return 0;
                    if (ioff + c.length > idata_limit) {
                        unsigned char *p;
                        if (idata_limit == 0)
                            idata_limit = c.length > 4096 ? c.length : 4096;
                        while (ioff + c.length > idata_limit)
                            idata_limit *= 2;
                        p = (unsigned char *) realloc(z->idata, idata_limit);
                        if (p == NULL)
                            return err("outofmem", "Out of memory");
                        z->idata = p;
                    }
                    if (!getn(s, z->idata + ioff, c.length))
                        return err("outofdata", "Corrupt PNG");
                    ioff += c.length;
                    break;
                }

                case PNG_TYPE('I', 'E', 'N', 'D'): {
                    uint32_t raw_len, bpl;
                    if (first)
                        return err("first not IHDR", "Corrupt PNG");
                    if (scan != SCAN_load)
                        return 1;
                    if (z->idata == NULL)
                        return err("no IDAT", "Corrupt PNG");
                    // initial guess for decoded data size to avoid unnecessary reallocs
                    bpl = (s->img_x * depth + 7) / 8; // bytes per line, per component
                    raw_len = bpl * s->img_y * s->img_n /* pixels */
                              + s->img_y /* filter mode per row */;
                    z->expanded = (unsigned char *) stbi_zlib_decode_malloc_guesssize_headerflag(
                            (char *) z->idata, ioff, raw_len, (int *) &raw_len, !is_iphone);
                    if (z->expanded == NULL)
                        return 0; // zlib should set error
                    free(z->idata);
                    z->idata = NULL;
                    if ((req_comp == s->img_n + 1 && req_comp != 3 && !pal_img_n) || has_trans)
                        s->img_out_n = s->img_n + 1;
                    else
                        s->img_out_n = s->img_n;
                    if (!create_png_image(z, z->expanded, raw_len, s->img_out_n, depth, color,
                                          interlace))
                        return 0;
                    if (has_trans)
                        if (!compute_transparency(z, tc, s->img_out_n))
                            return 0;
                    if (is_iphone && de_iphone_flag && s->img_out_n > 2)
                        de_iphone(z);
                    if (pal_img_n) {
                        // pal_img_n == 3 or 4
                        s->img_n = pal_img_n; // record the actual colors we had
                        s->img_out_n = pal_img_n;
                        if (req_comp >= 3)
                            s->img_out_n = req_comp;
                        if (!expand_png_palette(z, palette, pal_len, s->img_out_n))
                            return 0;
                    }
                    free(z->expanded);
                    z->expanded = NULL;
                    return 1;
                }

                default:
                    // if critical, fail
                    if (first)
                        return err("first not IHDR", "Corrupt PNG");
                    if ((c.type & (1 << 29)) == 0) {
                        // not threadsafe
                        static char invalid_chunk[] = "XXXX PNG chunk not known";
                        invalid_chunk[0] = BYTECAST(c.type >> 24);
                        invalid_chunk[1] = BYTECAST(c.type >> 16);
                        invalid_chunk[2] = BYTECAST(c.type >> 8);
                        invalid_chunk[3] = BYTECAST(c.type >> 0);
                        return err(invalid_chunk,
                                   "PNG not supported: unknown PNG chunk type");
                    }
                    skip(s, c.length);
                    break;
            }
            // end of PNG chunk, read and skip CRC
            get32be(s);
        }
    }

    static unsigned char *do_png(png *p, int *x, int *y, int *n, int req_comp) {
        unsigned char *result = NULL;
        if (req_comp < 0 || req_comp > 4) {
            err("bad req_comp", "Internal error");
            return 0;
        }
        if (parse_png_file(p, SCAN_load, req_comp)) {
            result = p->out;
            p->out = NULL;
            if (req_comp && req_comp != p->s->img_out_n) {
                result = convert_format(result, p->s->img_out_n, req_comp, p->s->img_x,
                                        p->s->img_y);
                p->s->img_out_n = req_comp;
                if (result == NULL)
                    return result;
            }
            *x = p->s->img_x;
            *y = p->s->img_y;
            if (n)
                *n = p->s->img_out_n;
        }
        free(p->out);
        p->out = NULL;
        free(p->expanded);
        p->expanded = NULL;
        free(p->idata);
        p->idata = NULL;

        return result;
    }

    static unsigned char *
    png_load(context *s, int *x, int *y, int *comp, int req_comp) {
        png p;
        p.s = s;
        return do_png(&p, x, y, comp, req_comp);
    }

    static int png_test(context *s) {
        int r;
        r = check_png_header(s);
        rewind(s);
        return r;
    }

    static int png_info_raw(png *p, int *x, int *y, int *comp) {
        if (!parse_png_file(p, SCAN_header, 0)) {
            rewind(p->s);
            return 0;
        }
        if (x)
            *x = p->s->img_x;
        if (y)
            *y = p->s->img_y;
        if (comp)
            *comp = p->s->img_n;
        return 1;
    }

    static int png_info(context *s, int *x, int *y, int *comp) {
        png p;
        p.s = s;
        return png_info_raw(&p, x, y, comp);
    }

// Microsoft/Windows BMP image

#ifndef STBI_NO_BMP

    static int bmp_test_raw(context *s) {
        int r;
        int sz;
        if (get8(s) != 'B')
            return 0;
        if (get8(s) != 'M')
            return 0;
        get32le(s); // discard filesize
        get16le(s); // discard reserved
        get16le(s); // discard reserved
        get32le(s); // discard data offset
        sz = get32le(s);
        r = (sz == 12 || sz == 40 || sz == 56 || sz == 108 || sz == 124);
        return r;
    }

    static int bmp_test(context *s) {
        int r = bmp_test_raw(s);
        rewind(s);
        return r;
    }

// returns 0..31 for the highest set bit
    static int high_bit(unsigned int z) {
        int n = 0;
        if (z == 0)
            return -1;
        if (z >= 0x10000)
            n += 16, z >>= 16;
        if (z >= 0x00100)
            n += 8, z >>= 8;
        if (z >= 0x00010)
            n += 4, z >>= 4;
        if (z >= 0x00004)
            n += 2, z >>= 2;
        if (z >= 0x00002)
            n += 1, z >>= 1;
        return n;
    }

    static int bitcount(unsigned int a) {
        a = (a & 0x55555555) + ((a >> 1) & 0x55555555); // max 2
        a = (a & 0x33333333) + ((a >> 2) & 0x33333333); // max 4
        a = (a + (a >> 4)) & 0x0f0f0f0f; // max 8 per 4, now 8 bits
        a = (a + (a >> 8)); // max 16 per 8 bits
        a = (a + (a >> 16)); // max 32 per 8 bits
        return a & 0xff;
    }

    static int shiftsigned(int v, int shift, int bits) {
        int result;
        int z = 0;

        if (shift < 0)
            v <<= -shift;
        else
            v >>= shift;
        result = v;

        z = bits;
        while (z < 8) {
            result += v >> z;
            z += bits;
        }
        return result;
    }

    static unsigned char *
    bmp_load(context *s, int *x, int *y, int *comp, int req_comp) {
        unsigned char *out;
        unsigned int mr = 0, mg = 0, mb = 0, ma = 0, all_a = 255;
        unsigned char pal[256][4];
        int psize = 0, i, j, compress = 0, width;
        int bpp, flip_vertically, pad, target, offset, hsz;
        if (get8(s) != 'B' || get8(s) != 'M') {
            err("not BMP", "Corrupt BMP");
            return 0;
        }
        get32le(s); // discard filesize
        get16le(s); // discard reserved
        get16le(s); // discard reserved
        offset = get32le(s);
        hsz = get32le(s);
        if (hsz != 12 && hsz != 40 && hsz != 56 && hsz != 108 && hsz != 124) {
            err("unknown BMP", "BMP type not supported: unknown");
            return 0;
        }
        if (hsz == 12) {
            s->img_x = get16le(s);
            s->img_y = get16le(s);
        } else {
            s->img_x = get32le(s);
            s->img_y = get32le(s);
        }
        if (get16le(s) != 1) {
            err("bad BMP", "bad BMP");
            return 0;
        }
        bpp = get16le(s);
        if (bpp == 1) {
            err("monochrome", "BMP type not supported: 1-bit");
            return 0;
        }
        flip_vertically = ((int) s->img_y) > 0;
        s->img_y = abs((int) s->img_y);
        if (hsz == 12) {
            if (bpp < 24)
                psize = (offset - 14 - 24) / 3;
        } else {
            compress = get32le(s);
            if (compress == 1 || compress == 2) {
                err("BMP RLE", "BMP type not supported: RLE");
                return 0;
            }
            get32le(s); // discard sizeof
            get32le(s); // discard hres
            get32le(s); // discard vres
            get32le(s); // discard colorsused
            get32le(s); // discard max important
            if (hsz == 40 || hsz == 56) {
                if (hsz == 56) {
                    get32le(s);
                    get32le(s);
                    get32le(s);
                    get32le(s);
                }
                if (bpp == 16 || bpp == 32) {
                    mr = mg = mb = 0;
                    if (compress == 0) {
                        if (bpp == 32) {
                            mr = 0xffu << 16;
                            mg = 0xffu << 8;
                            mb = 0xffu << 0;
                            ma = 0xffu << 24;
                            all_a = 0; // if all_a is 0 at end, then we loaded alpha channel but it was all 0
                        } else {
                            mr = 31u << 10;
                            mg = 31u << 5;
                            mb = 31u << 0;
                        }
                    } else if (compress == 3) {
                        mr = get32le(s);
                        mg = get32le(s);
                        mb = get32le(s);
                        // not documented, but generated by photoshop and handled by mspaint
                        if (mr == mg && mg == mb) {
                            err("bad BMP", "bad BMP");
                            return 0;
                        }
                    } else {
                        err("bad BMP", "bad BMP");
                        return 0;
                    }
                }
            } else {
                assert(hsz == 108 || hsz == 124);
                mr = get32le(s);
                mg = get32le(s);
                mb = get32le(s);
                ma = get32le(s);
                get32le(s); // discard color space
                for (i = 0; i < 12; ++i)
                    get32le(s); // discard color space parameters
                if (hsz == 124) {
                    get32le(s); // discard rendering intent
                    get32le(s); // discard offset of profile data
                    get32le(s); // discard size of profile data
                    get32le(s); // discard reserved
                }
            }
            if (bpp < 16)
                psize = (offset - 14 - hsz) >> 2;
        }
        s->img_n = ma ? 4 : 3;
        if (req_comp && req_comp >= 3) // we can directly decode 3 or 4
            target = req_comp;
        else
            target = s->img_n; // if they want monochrome, we'll post-convert
        out = (unsigned char *) malloc(target * s->img_x * s->img_y);
        if (!out) {
            err("outofmem", "Out of memory");
            return 0;
        }
        if (bpp < 16) {
            int z = 0;
            if (psize == 0 || psize > 256) {
                free(out);
                err("invalid", "Corrupt BMP");
                return 0;
            }
            for (i = 0; i < psize; ++i) {
                pal[i][2] = get8(s);
                pal[i][1] = get8(s);
                pal[i][0] = get8(s);
                if (hsz != 12)
                    get8(s);
                pal[i][3] = 255;
            }
            skip(s, offset - 14 - hsz - psize * (hsz == 12 ? 3 : 4));
            if (bpp == 4)
                width = (s->img_x + 1) >> 1;
            else if (bpp == 8)
                width = s->img_x;
            else {
                free(out);
                err("bad bpp", "Corrupt BMP");
                return 0;
            }
            pad = (-width) & 3;
            for (j = 0; j < (int) s->img_y; ++j) {
                for (i = 0; i < (int) s->img_x; i += 2) {
                    int v = get8(s), v2 = 0;
                    if (bpp == 4) {
                        v2 = v & 15;
                        v >>= 4;
                    }
                    out[z++] = pal[v][0];
                    out[z++] = pal[v][1];
                    out[z++] = pal[v][2];
                    if (target == 4)
                        out[z++] = 255;
                    if (i + 1 == (int) s->img_x)
                        break;
                    v = (bpp == 8) ? get8(s) : v2;
                    out[z++] = pal[v][0];
                    out[z++] = pal[v][1];
                    out[z++] = pal[v][2];
                    if (target == 4)
                        out[z++] = 255;
                }
                skip(s, pad);
            }
        } else {
            int rshift = 0, gshift = 0, bshift = 0, ashift = 0, rcount = 0,
                    gcount = 0, bcount = 0, acount = 0;
            int z = 0;
            int easy = 0;
            skip(s, offset - 14 - hsz);
            if (bpp == 24)
                width = 3 * s->img_x;
            else if (bpp == 16)
                width = 2 * s->img_x;
            else
                /* bpp = 32 and pad = 0 */width = 0;
            pad = (-width) & 3;
            if (bpp == 24) {
                easy = 1;
            } else if (bpp == 32) {
                if (mb == 0xff && mg == 0xff00 && mr == 0x00ff0000 && ma == 0xff000000)
                    easy = 2;
            }
            if (!easy) {
                if (!mr || !mg || !mb) {
                    free(out);
                    err("bad masks", "Corrupt BMP");
                    return 0;
                }
                // right shift amt to put high bit in position #7
                rshift = high_bit(mr) - 7;
                rcount = bitcount(mr);
                gshift = high_bit(mg) - 7;
                gcount = bitcount(mg);
                bshift = high_bit(mb) - 7;
                bcount = bitcount(mb);
                ashift = high_bit(ma) - 7;
                acount = bitcount(ma);
            }
            for (j = 0; j < (int) s->img_y; ++j) {
                if (easy) {
                    for (i = 0; i < (int) s->img_x; ++i) {
                        unsigned char a;
                        out[z + 2] = get8(s);
                        out[z + 1] = get8(s);
                        out[z + 0] = get8(s);
                        z += 3;
                        a = (easy == 2 ? get8(s) : 255);
                        all_a |= a;
                        if (target == 4)
                            out[z++] = a;
                    }
                } else {
                    for (i = 0; i < (int) s->img_x; ++i) {
                        uint32_t v = (
                                bpp == 16 ? (uint32_t) get16le(s) : get32le(s));
                        int a;
                        out[z++] = BYTECAST(shiftsigned(v & mr, rshift, rcount));
                        out[z++] = BYTECAST(shiftsigned(v & mg, gshift, gcount));
                        out[z++] = BYTECAST(shiftsigned(v & mb, bshift, bcount));
                        a = (ma ? shiftsigned(v & ma, ashift, acount) : 255);
                        all_a |= a;
                        if (target == 4)
                            out[z++] = BYTECAST(a);
                    }
                }
                skip(s, pad);
            }
        }

        // if alpha channel is all 0s, replace with all 255s
        if (target == 4 && all_a == 0)
            for (i = 4 * s->img_x * s->img_y - 1; i >= 0; i -= 4)
                out[i] = 255;

        if (flip_vertically) {
            unsigned char t;
            for (j = 0; j < (int) s->img_y >> 1; ++j) {
                unsigned char *p1 = out + j * s->img_x * target;
                unsigned char *p2 = out + (s->img_y - 1 - j) * s->img_x * target;
                for (i = 0; i < (int) s->img_x * target; ++i) {
                    t = p1[i], p1[i] = p2[i], p2[i] = t;
                }
            }
        }

        if (req_comp && req_comp != target) {
            out = convert_format(out, target, req_comp, s->img_x, s->img_y);
            if (out == NULL)
                return out; // convert_format frees input on failure
        }

        *x = s->img_x;
        *y = s->img_y;
        if (comp)
            *comp = s->img_n;
        return out;
    }

#endif

// Targa Truevision - TGA
// by Jonathan Dummer
#ifndef STBI_NO_TGA

    static int tga_info(context *s, int *x, int *y, int *comp) {
        int tga_w, tga_h, tga_comp;
        int sz;
        get8(s);                   // discard Offset
        sz = get8(s);              // color type
        if (sz > 1) {
            rewind(s);
            return 0;      // only RGB or indexed allowed
        }
        sz = get8(s);              // image type
        // only RGB or grey allowed, +/- RLE
        if ((sz != 1) && (sz != 2) && (sz != 3) && (sz != 9) && (sz != 10) && (sz != 11))
            return 0;
        skip(s, 9);
        tga_w = get16le(s);
        if (tga_w < 1) {
            rewind(s);
            return 0;   // test width
        }
        tga_h = get16le(s);
        if (tga_h < 1) {
            rewind(s);
            return 0;   // test height
        }
        sz = get8(s);               // bits per pixel
        // only RGB or RGBA or grey allowed
        if ((sz != 8) && (sz != 16) && (sz != 24) && (sz != 32)) {
            rewind(s);
            return 0;
        }
        tga_comp = sz;
        if (x)
            *x = tga_w;
        if (y)
            *y = tga_h;
        if (comp)
            *comp = tga_comp / 8;
        return 1;                   // seems to have passed everything
    }

    static int tga_test(context *s) {
        int res;
        int sz;
        get8(s);      //   discard Offset
        sz = get8(s);   //   color type
        if (sz > 1)
            return 0;   //   only RGB or indexed allowed
        sz = get8(s);   //   image type
        if ((sz != 1) && (sz != 2) && (sz != 3) && (sz != 9) && (sz != 10) && (sz != 11))
            return 0;   //   only RGB or grey allowed, +/- RLE
        get16be(s);      //   discard palette start
        get16be(s);      //   discard palette length
        get8(s);         //   discard bits per palette color entry
        get16be(s);      //   discard x origin
        get16be(s);      //   discard y origin
        if (get16be(s) < 1)
            return 0;      //   test width
        if (get16be(s) < 1)
            return 0;      //   test height
        sz = get8(s);   //   bits per pixel
        if ((sz != 8) && (sz != 16) && (sz != 24) && (sz != 32))
            res = 0;
        else
            res = 1;
        rewind(s);
        return res;
    }

    static unsigned char *
    tga_load(context *s, int *x, int *y, int *comp, int req_comp) {
        //   read in the TGA header stuff
        int tga_offset = get8(s);
        int tga_indexed = get8(s);
        int tga_image_type = get8(s);
        int tga_is_RLE = 0;
        int tga_palette_start = get16le(s);
        int tga_palette_len = get16le(s);
        int tga_palette_bits = get8(s);
        int tga_x_origin = get16le(s);
        int tga_y_origin = get16le(s);
        int tga_width = get16le(s);
        int tga_height = get16le(s);
        int tga_bits_per_pixel = get8(s);
        int tga_comp = tga_bits_per_pixel / 8;
        int tga_inverted = get8(s);
        //   image data
        unsigned char *tga_data;
        unsigned char *tga_palette = NULL;
        int i, j;
        unsigned char raw_data[4];
        int RLE_count = 0;
        int RLE_repeating = 0;
        int read_next_pixel = 1;

        //   do a tiny bit of precessing
        if (tga_image_type >= 8) {
            tga_image_type -= 8;
            tga_is_RLE = 1;
        }
        /* int tga_alpha_bits = tga_inverted & 15; */
        tga_inverted = 1 - ((tga_inverted >> 5) & 1);

        //   error check
        if ( //(tga_indexed) ||
                (tga_width < 1) || (tga_height < 1) || (tga_image_type < 1) ||
                (tga_image_type > 3) || ((tga_bits_per_pixel != 8) && (tga_bits_per_pixel != 16) &&
                                         (tga_bits_per_pixel != 24) &&
                                         (tga_bits_per_pixel != 32))) {
            return NULL; // we don't report this as a bad TGA because we don't even know if it's TGA
        }

        //   If I'm paletted, then I'll use the number of bits from the palette
        if (tga_indexed) {
            tga_comp = tga_palette_bits / 8;
        }

        //   tga info
        *x = tga_width;
        *y = tga_height;
        if (comp)
            *comp = tga_comp;

        tga_data = (unsigned char *) malloc((size_t) tga_width * tga_height * tga_comp);
        if (!tga_data) {
            err("outofmem", "Out of memory");
            return 0;
        }

        // skip to the data's starting position (offset usually = 0)
        skip(s, tga_offset);

        if (!tga_indexed && !tga_is_RLE) {
            for (i = 0; i < tga_height; ++i) {
                int row = tga_inverted ? tga_height - i - 1 : i;
                unsigned char *tga_row = tga_data + row * tga_width * tga_comp;
                getn(s, tga_row, tga_width * tga_comp);
            }
        } else {
            //   do I need to load a palette?
            if (tga_indexed) {
                //   any data to skip? (offset usually = 0)
                skip(s, tga_palette_start);
                //   load the palette
                tga_palette = (unsigned char *) malloc(
                        tga_palette_len * tga_palette_bits / 8);
                if (!tga_palette) {
                    free(tga_data);
                    err("outofmem", "Out of memory");
                    return 0;
                }
                if (!getn(s, tga_palette, tga_palette_len * tga_palette_bits / 8)) {
                    free(tga_data);
                    free(tga_palette);
                    err("bad palette", "Corrupt TGA");
                    return 0;
                }
            }
            //   load the data
            for (i = 0; i < tga_width * tga_height; ++i) {
                //   if I'm in RLE mode, do I need to get a RLE pngchunk?
                if (tga_is_RLE) {
                    if (RLE_count == 0) {
                        //   yep, get the next byte as a RLE command
                        int RLE_cmd = get8(s);
                        RLE_count = 1 + (RLE_cmd & 127);
                        RLE_repeating = RLE_cmd >> 7;
                        read_next_pixel = 1;
                    } else if (!RLE_repeating) {
                        read_next_pixel = 1;
                    }
                } else {
                    read_next_pixel = 1;
                }
                //   OK, if I need to read a pixel, do it now
                if (read_next_pixel) {
                    //   load however much data we did have
                    if (tga_indexed) {
                        //   read in 1 byte, then perform the lookup
                        int pal_idx = get8(s);
                        if (pal_idx >= tga_palette_len) {
                            //   invalid index
                            pal_idx = 0;
                        }
                        pal_idx *= tga_bits_per_pixel / 8;
                        for (j = 0; j * 8 < tga_bits_per_pixel; ++j) {
                            raw_data[j] = tga_palette[pal_idx + j];
                        }
                    } else {
                        //   read in the data raw
                        for (j = 0; j * 8 < tga_bits_per_pixel; ++j) {
                            raw_data[j] = get8(s);
                        }
                    }
                    //   clear the reading flag for the next pixel
                    read_next_pixel = 0;
                } // end of reading a pixel

                // copy data
                for (j = 0; j < tga_comp; ++j)
                    tga_data[i * tga_comp + j] = raw_data[j];

                //   in case we're in RLE mode, keep counting down
                --RLE_count;
            }
            //   do I need to invert the image?
            if (tga_inverted) {
                for (j = 0; j * 2 < tga_height; ++j) {
                    int index1 = j * tga_width * tga_comp;
                    int index2 = (tga_height - 1 - j) * tga_width * tga_comp;
                    for (i = tga_width * tga_comp; i > 0; --i) {
                        unsigned char temp = tga_data[index1];
                        tga_data[index1] = tga_data[index2];
                        tga_data[index2] = temp;
                        ++index1;
                        ++index2;
                    }
                }
            }
            //   clear my palette, if I had one
            if (tga_palette != NULL) {
                free(tga_palette);
            }
        }

        // swap RGB
        if (tga_comp >= 3) {
            unsigned char *tga_pixel = tga_data;
            for (i = 0; i < tga_width * tga_height; ++i) {
                unsigned char temp = tga_pixel[0];
                tga_pixel[0] = tga_pixel[2];
                tga_pixel[2] = temp;
                tga_pixel += tga_comp;
            }
        }

        // convert to target component count
        if (req_comp && req_comp != tga_comp)
            tga_data = convert_format(tga_data, tga_comp, req_comp, tga_width, tga_height);

        //   the things I do to get rid of an error message, and yet keep
        //   Microsoft's C compilers happy... [8^(
        tga_palette_start = tga_palette_len = tga_palette_bits = tga_x_origin = tga_y_origin = 0;
        //   OK, done
        return tga_data;
    }

#endif

#ifndef STBI_NO_PSD

    static int psd_test(context *s) {
        int r = (get32be(s) == 0x38425053);
        rewind(s);
        return r;
    }

    static unsigned char *
    psd_load(context *s, int *x, int *y, int *comp, int req_comp) {
        int pixelCount;
        int channelCount, compression;
        int channel, i, count, len;
        int bitdepth;
        int w, h;
        unsigned char *out;

        // Check identifier
        if (get32be(s) != 0x38425053)    // "8BPS"
        {
            err("not PSD", "Corrupt PSD image");
            return 0;
        }
        // Check file type version.
        if (get16be(s) != 1) {
            err("wrong version", "Unsupported version of PSD image");
            return 0;
        }
        // Skip 6 reserved bytes.
        skip(s, 6);

        // Read the number of channels (R, G, B, A, etc).
        channelCount = get16be(s);
        if (channelCount < 0 || channelCount > 16) {
            err("wrong channel count", "Unsupported number of channels in PSD image");
            return 0;
        }

        // Read the rows and columns of the image.
        h = get32be(s);
        w = get32be(s);

        // Make sure the depth is 8 bits.
        bitdepth = get16be(s);
        if (bitdepth != 8 && bitdepth != 16) {
            err("unsupported bit depth", "PSD bit depth is not 8 or 16 bit");
            return 0;
        }
        // Make sure the color mode is RGB.
        // Valid options are:
        //   0: Bitmap
        //   1: Grayscale
        //   2: Indexed color
        //   3: RGB color
        //   4: CMYK color
        //   7: Multichannel
        //   8: Duotone
        //   9: Lab color
        if (get16be(s) != 3) {
            err("wrong color format", "PSD is not in RGB color format");
            return 0;
        }

        // Skip the Mode Data.  (It's the palette for indexed color; other info for other modes.)
        skip(s, get32be(s));

        // Skip the image resources.  (resolution, pen tool paths, etc)
        skip(s, get32be(s));

        // Skip the reserved data.
        skip(s, get32be(s));

        // Find out if the data is compressed.
        // Known values:
        //   0: no compression
        //   1: RLE compressed
        compression = get16be(s);
        if (compression > 1) {
            err("bad compression", "PSD has an unknown compression format");
            return 0;
        }

        // Create the destination image.
        out = (unsigned char *) malloc(4 * w * h);
        if (!out) {
            err("outofmem", "Out of memory");
            return 0;
        }
        pixelCount = w * h;

        // Initialize the data to zero.
        //memset( out, 0, pixelCount * 4 );

        // Finally, the image data.
        if (compression) {
            // RLE as used by .PSD and .TIFF
            // Loop until you get the number of unpacked bytes you are expecting:
            //     Read the next source byte into n.
            //     If n is between 0 and 127 inclusive, copy the next n+1 bytes literally.
            //     Else if n is between -127 and -1 inclusive, copy the next byte -n+1 times.
            //     Else if n is 128, noop.
            // Endloop

            // The RLE-compressed data is preceeded by a 2-byte data count for each row in the data,
            // which we're going to just skip.
            skip(s, h * channelCount * 2);

            // Read the RLE data by channel.
            for (channel = 0; channel < 4; channel++) {
                unsigned char *p;

                p = out + channel;
                if (channel >= channelCount) {
                    // Fill this channel with default data.
                    for (i = 0; i < pixelCount; i++, p += 4)
                        *p = (channel == 3 ? 255 : 0);
                } else {
                    // Read the RLE data.
                    count = 0;
                    while (count < pixelCount) {
                        len = get8(s);
                        if (len == 128) {
                            // No-op.
                        } else if (len < 128) {
                            // Copy next len+1 bytes literally.
                            len++;
                            count += len;
                            while (len) {
                                *p = get8(s);
                                p += 4;
                                len--;
                            }
                        } else if (len > 128) {
                            unsigned char val;
                            // Next -len+1 bytes in the dest are replicated from next source byte.
                            // (Interpret len as a negative 8-bit int.)
                            len ^= 0x0FF;
                            len += 2;
                            val = get8(s);
                            count += len;
                            while (len) {
                                *p = val;
                                p += 4;
                                len--;
                            }
                        }
                    }
                }
            }

        } else {
            // We're at the raw image data.  It's each channel in order (Red, Green, Blue, Alpha, ...)
            // where each channel consists of an 8-bit value for each pixel in the image.

            // Read the data by channel.
            for (channel = 0; channel < 4; channel++) {
                unsigned char *p;

                p = out + channel;
                if (channel >= channelCount) {
                    // Fill this channel with default data.
                    unsigned char val = channel == 3 ? 255 : 0;
                    for (i = 0; i < pixelCount; i++, p += 4)
                        *p = val;
                } else {
                    // Read the data.
                    if (bitdepth == 16) {
                        for (i = 0; i < pixelCount; i++, p += 4)
                            *p = (unsigned char) (get16be(s) >> 8);
                    } else {
                        for (i = 0; i < pixelCount; i++, p += 4)
                            *p = get8(s);
                    }
                }
            }
        }

        if (req_comp && req_comp != 4) {
            out = convert_format(out, 4, req_comp, w, h);
            if (out == NULL)
                return out; // convert_format frees input on failure
        }

        if (comp)
            *comp = 4;
        *y = h;
        *x = w;

        return out;
    }

#endif

// *************************************************************************************************
// Softimage PIC loader
// by Tom Seddon
//
// See http://softimage.wiki.softimage.com/index.php/INFO:_PIC_file_format
// See http://ozviz.wasp.uwa.edu.au/~pbourke/dataformats/softimagepic/

#ifndef STBI_NO_PIC

    static int pic_is4(context *s, const char *str) {
        int i;
        for (i = 0; i < 4; ++i)
            if (get8(s) != (unsigned char) str[i])
                return 0;

        return 1;
    }

    static int pic_test_core(context *s) {
        int i;

        if (!pic_is4(s, "\x53\x80\xF6\x34"))
            return 0;

        for (i = 0; i < 84; ++i)
            get8(s);

        if (!pic_is4(s, "PICT"))
            return 0;

        return 1;
    }

    struct pic_packet {
        unsigned char size, type, channel;
    };

    static unsigned char *readval(context *s, int channel, unsigned char *dest) {
        int mask = 0x80, i;

        for (i = 0; i < 4; ++i, mask >>= 1) {
            if (channel & mask) {
                if (at_eof(s)) {
                    err("bad file", "PIC file too short");
                    return 0;
                }
                dest[i] = get8(s);
            }
        }

        return dest;
    }

    static void copyval(int channel, unsigned char *dest, const unsigned char *src) {
        int mask = 0x80, i;

        for (i = 0; i < 4; ++i, mask >>= 1)
            if (channel & mask)
                dest[i] = src[i];
    }

    static unsigned char *
    pic_load_core(context *s, int width, int height, int *comp, unsigned char *result) {
        int act_comp = 0, num_packets = 0, y, chained;
        pic_packet packets[10];

        // this will (should...) cater for even some bizarre stuff like having data
        // for the same channel in multiple packets.
        do {
            pic_packet *packet;

            if (num_packets == sizeof(packets) / sizeof(packets[0])) {
                err("bad format", "too many packets");
                return 0;
            }
            packet = &packets[num_packets++];

            chained = get8(s);
            packet->size = get8(s);
            packet->type = get8(s);
            packet->channel = get8(s);

            act_comp |= packet->channel;

            if (at_eof(s)) {
                err("bad file", "file too short (reading packets)");
                return 0;
            }
            if (packet->size != 8) {
                err("bad format", "packet isn't 8bpp");
                return 0;
            }
        } while (chained);

        *comp = (act_comp & 0x10 ? 4 : 3); // has alpha channel?

        for (y = 0; y < height; ++y) {
            int packet_idx;

            for (packet_idx = 0; packet_idx < num_packets; ++packet_idx) {
                pic_packet *packet = &packets[packet_idx];
                unsigned char *dest = result + y * width * 4;

                switch (packet->type) {
                    default:
                        err("bad format", "packet has bad compression type");
                        return 0;

                    case 0:   //uncompressed
                    {
                        int x;

                        for (x = 0; x < width; ++x, dest += 4)
                            if (!readval(s, packet->channel, dest))
                                return 0;
                        break;
                    }

                    case 1: //Pure RLE
                    {
                        int left = width, i;

                        while (left > 0) {
                            unsigned char count, value[4];

                            count = get8(s);
                            if (at_eof(s)) {
                                err("bad file", "file too short (pure read count)");
                                return 0;
                            }
                            if (count > left)
                                count = (unsigned char) left;

                            if (!readval(s, packet->channel, value))
                                return 0;

                            for (i = 0; i < count; ++i, dest += 4)
                                copyval(packet->channel, dest, value);
                            left -= count;
                        }
                    }
                        break;

                    case 2:   //Mixed RLE
                    {
                        int left = width;
                        while (left > 0) {
                            int count = get8(s), i;
                            if (at_eof(s)) {
                                err("bad file", "file too short (mixed read count)");
                                return 0;
                            }

                            if (count >= 128)   // Repeated
                            {
                                unsigned char value[4];

                                if (count == 128)
                                    count = get16be(s);
                                else
                                    count -= 127;
                                if (count > left) {
                                    err("bad file", "scanline overrun");
                                    return 0;
                                }

                                if (!readval(s, packet->channel, value))
                                    return 0;

                                for (i = 0; i < count; ++i, dest += 4)
                                    copyval(packet->channel, dest, value);
                            } else     // Raw
                            {
                                ++count;
                                if (count > left) {
                                    err("bad file", "scanline overrun");
                                    return 0;
                                }

                                for (i = 0; i < count; ++i, dest += 4)
                                    if (!readval(s, packet->channel, dest))
                                        return 0;
                            }
                            left -= count;
                        }
                        break;
                    }
                }
            }
        }

        return result;
    }

    static unsigned char *
    pic_load(context *s, int *px, int *py, int *comp, int req_comp) {
        unsigned char *result;
        int i, x, y;

        for (i = 0; i < 92; ++i)
            get8(s);

        x = get16be(s);
        y = get16be(s);
        if (at_eof(s)) {
            err("bad file", "file too short (pic header)");
            return 0;
        }
        if ((1 << 28) / x < y) {
            err("too large", "Image too large to decode");
            return 0;
        }

        get32be(s); //skip `ratio'
        get16be(s); //skip `fields'
        get16be(s); //skip `pad'

        // intermediate buffer is RGBA
        result = (unsigned char *) malloc(x * y * 4);
        memset(result, 0xff, x * y * 4);

        if (!pic_load_core(s, x, y, comp, result)) {
            free(result);
            result = 0;
        }
        *px = x;
        *py = y;
        if (req_comp == 0)
            req_comp = *comp;
        result = convert_format(result, 4, req_comp, x, y);

        return result;
    }

    static int pic_test(context *s) {
        int r = pic_test_core(s);
        rewind(s);
        return r;
    }

#endif

// *************************************************************************************************
// GIF loader -- public domain by Jean-Marc Lienher -- simplified/shrunk by stb

#ifndef STBI_NO_GIF
    struct gif_lzw {
        int16_t prefix;
        unsigned char first;
        unsigned char suffix;
    };

    struct gif {
        int w, h;
        unsigned char *out, *old_out;         // output buffer (always 4 components)
        int flags, bgindex, ratio, transparent, eflags, delay;
        unsigned char pal[256][4];
        unsigned char lpal[256][4];
        gif_lzw codes[4096];
        unsigned char *color_table;
        int parse, step;
        int lflags;
        int start_x, start_y;
        int max_x, max_y;
        int cur_x, cur_y;
        int line_size;
    };

    static int gif_test_raw(context *s) {
        int sz;
        if (get8(s) != 'G' || get8(s) != 'I' || get8(s) != 'F' ||
            get8(s) != '8')
            return 0;
        sz = get8(s);
        if (sz != '9' && sz != '7')
            return 0;
        if (get8(s) != 'a')
            return 0;
        return 1;
    }

    static int gif_test(context *s) {
        int r = gif_test_raw(s);
        rewind(s);
        return r;
    }

    static void
    gif_parse_colortable(context *s, unsigned char pal[256][4], int num_entries,
                         int transp) {
        int i;
        for (i = 0; i < num_entries; ++i) {
            pal[i][2] = get8(s);
            pal[i][1] = get8(s);
            pal[i][0] = get8(s);
            pal[i][3] = transp == i ? 0 : 255;
        }
    }

    static int gif_header(context *s, gif *g, int *comp, int is_info) {
        unsigned char version;
        if (get8(s) != 'G' || get8(s) != 'I' || get8(s) != 'F' ||
            get8(s) != '8')
            return err("not GIF", "Corrupt GIF");

        version = get8(s);
        if (version != '7' && version != '9')
            return err("not GIF", "Corrupt GIF");
        if (get8(s) != 'a')
            return err("not GIF", "Corrupt GIF");

        g_failure_reason = "";
        g->w = get16le(s);
        g->h = get16le(s);
        g->flags = get8(s);
        g->bgindex = get8(s);
        g->ratio = get8(s);
        g->transparent = -1;

        if (comp != 0)
            *comp = 4; // can't actually tell whether it's 3 or 4 until we parse the comments

        if (is_info)
            return 1;

        if (g->flags & 0x80)
            gif_parse_colortable(s, g->pal, 2 << (g->flags & 7), -1);

        return 1;
    }

    static int gif_info_raw(context *s, int *x, int *y, int *comp) {
        gif g;
        if (!gif_header(s, &g, comp, 1)) {
            rewind(s);
            return 0;
        }
        if (x)
            *x = g.w;
        if (y)
            *y = g.h;
        return 1;
    }

    static void out_gif_code(gif *g, uint16_t code) {
        unsigned char *p, *c;

        // recurse to decode the prefixes, since the linked-list is backwards,
        // and working backwards through an interleaved image would be nasty
        if (g->codes[code].prefix >= 0)
            out_gif_code(g, g->codes[code].prefix);

        if (g->cur_y >= g->max_y)
            return;

        p = &g->out[g->cur_x + g->cur_y];
        c = &g->color_table[g->codes[code].suffix * 4];

        if (c[3] >= 128) {
            p[0] = c[2];
            p[1] = c[1];
            p[2] = c[0];
            p[3] = c[3];
        }
        g->cur_x += 4;

        if (g->cur_x >= g->max_x) {
            g->cur_x = g->start_x;
            g->cur_y += g->step;

            while (g->cur_y >= g->max_y && g->parse > 0) {
                g->step = (1 << g->parse) * g->line_size;
                g->cur_y = g->start_y + (g->step >> 1);
                --g->parse;
            }
        }
    }

    static unsigned char *process_gif_raster(context *s, gif *g) {
        unsigned char lzw_cs;
        int32_t len, init_code;
        uint32_t first;
        int32_t codesize, codemask, avail, oldcode, bits, valid_bits, clear;
        gif_lzw *p;

        lzw_cs = get8(s);
        if (lzw_cs > 12)
            return NULL;
        clear = 1 << lzw_cs;
        first = 1;
        codesize = lzw_cs + 1;
        codemask = (1 << codesize) - 1;
        bits = 0;
        valid_bits = 0;
        for (init_code = 0; init_code < clear; init_code++) {
            g->codes[init_code].prefix = -1;
            g->codes[init_code].first = (unsigned char) init_code;
            g->codes[init_code].suffix = (unsigned char) init_code;
        }

        // support no starting clear code
        avail = clear + 2;
        oldcode = -1;

        len = 0;
        for (;;) {
            if (valid_bits < codesize) {
                if (len == 0) {
                    len = get8(s); // start new block
                    if (len == 0)
                        return g->out;
                }
                --len;
                bits |= (int32_t) get8(s) << valid_bits;
                valid_bits += 8;
            } else {
                int32_t code = bits & codemask;
                bits >>= codesize;
                valid_bits -= codesize;
                // @OPTIMIZE: is there some way we can accelerate the non-clear path?
                if (code == clear)    // clear code
                {
                    codesize = lzw_cs + 1;
                    codemask = (1 << codesize) - 1;
                    avail = clear + 2;
                    oldcode = -1;
                    first = 0;
                } else if (code == clear + 1)     // end of stream code
                {
                    skip(s, len);
                    while ((len = get8(s)) > 0)
                        skip(s, len);
                    return g->out;
                } else if (code <= avail) {
                    if (first) {
                        err("no clear code", "Corrupt GIF");
                        return 0;
                    }

                    if (oldcode >= 0) {
                        p = &g->codes[avail++];
                        if (avail > 4096) {
                            err("too many codes", "Corrupt GIF");
                            return 0;
                        }
                        p->prefix = (int16_t) oldcode;
                        p->first = g->codes[oldcode].first;
                        p->suffix =
                                (code == avail) ? p->first : g->codes[code].first;
                    } else if (code == avail) {
                        err("illegal code in raster", "Corrupt GIF");
                        return 0;
                    }
                    out_gif_code(g, (uint16_t) code);

                    if ((avail & codemask) == 0 && avail <= 0x0FFF) {
                        codesize++;
                        codemask = (1 << codesize) - 1;
                    }

                    oldcode = code;
                } else {
                    err("illegal code in raster", "Corrupt GIF");
                    return 0;
                }
            }
        }
    }

    static void fill_gif_background(gif *g, int x0, int y0, int x1, int y1) {
        int x, y;
        unsigned char *c = g->pal[g->bgindex];
        for (y = y0; y < y1; y += 4 * g->w) {
            for (x = x0; x < x1; x += 4) {
                unsigned char *p = &g->out[y + x];
                p[0] = c[2];
                p[1] = c[1];
                p[2] = c[0];
                p[3] = 0;
            }
        }
    }

// this function is designed to support animated gifs, although stb_image doesn't support it
    static unsigned char *
    gif_load_next(context *s, gif *g, int *comp, int req_comp) {
        int i;
        unsigned char *prev_out = 0;

        if (g->out == 0 && !gif_header(s, g, comp, 0))
            return 0; // g_failure_reason set by gif_header

        prev_out = g->out;
        g->out = (unsigned char *) malloc(4 * g->w * g->h);
        if (g->out == 0) {
            err("outofmem", "Out of memory");
            return 0;
        }
        switch ((g->eflags & 0x1C) >> 2) {
            case 0: // unspecified (also always used on 1st frame)
                fill_gif_background(g, 0, 0, 4 * g->w, 4 * g->w * g->h);
                break;
            case 1: // do not dispose
                if (prev_out)
                    memcpy(g->out, prev_out, 4 * g->w * g->h);
                g->old_out = prev_out;
                break;
            case 2: // dispose to background
                if (prev_out)
                    memcpy(g->out, prev_out, 4 * g->w * g->h);
                fill_gif_background(g, g->start_x, g->start_y, g->max_x, g->max_y);
                break;
            case 3: // dispose to previous
                if (g->old_out) {
                    for (i = g->start_y; i < g->max_y; i += 4 * g->w)
                        memcpy(&g->out[i + g->start_x], &g->old_out[i + g->start_x],
                               g->max_x - g->start_x);
                }
                break;
        }

        for (;;) {
            switch (get8(s)) {
                case 0x2C: /* Image Descriptor */
                {
                    int prev_trans = -1;
                    int32_t x, y, w, h;
                    unsigned char *o;

                    x = get16le(s);
                    y = get16le(s);
                    w = get16le(s);
                    h = get16le(s);
                    if (((x + w) > (g->w)) || ((y + h) > (g->h))) {
                        err("bad Image Descriptor", "Corrupt GIF");
                        return 0;
                    }
                    g->line_size = g->w * 4;
                    g->start_x = x * 4;
                    g->start_y = y * g->line_size;
                    g->max_x = g->start_x + w * 4;
                    g->max_y = g->start_y + h * g->line_size;
                    g->cur_x = g->start_x;
                    g->cur_y = g->start_y;

                    g->lflags = get8(s);

                    if (g->lflags & 0x40) {
                        g->step = 8 * g->line_size; // first interlaced spacing
                        g->parse = 3;
                    } else {
                        g->step = g->line_size;
                        g->parse = 0;
                    }

                    if (g->lflags & 0x80) {
                        gif_parse_colortable(s, g->lpal, 2 << (g->lflags & 7),
                                             g->eflags & 0x01 ? g->transparent : -1);
                        g->color_table = (unsigned char *) g->lpal;
                    } else if (g->flags & 0x80) {
                        if (g->transparent >= 0 && (g->eflags & 0x01)) {
                            prev_trans = g->pal[g->transparent][3];
                            g->pal[g->transparent][3] = 0;
                        }
                        g->color_table = (unsigned char *) g->pal;
                    } else {
                        err("missing color table", "Corrupt GIF");
                        return 0;
                    }
                    o = process_gif_raster(s, g);
                    if (o == NULL)
                        return NULL;

                    if (prev_trans != -1)
                        g->pal[g->transparent][3] = (unsigned char) prev_trans;

                    return o;
                }

                case 0x21: // Comment Extension.
                {
                    int len;
                    if (get8(s) == 0xF9)   // Graphic Control Extension.
                    {
                        len = get8(s);
                        if (len == 4) {
                            g->eflags = get8(s);
                            g->delay = get16le(s);
                            g->transparent = get8(s);
                        } else {
                            skip(s, len);
                            break;
                        }
                    }
                    while ((len = get8(s)) != 0)
                        skip(s, len);
                    break;
                }

                case 0x3B: // gif stream termination code
                    return (unsigned char *) s; // using '1' causes warning on some compilers

                default:
                    err("unknown code", "Corrupt GIF");
                    return 0;
            }
        }

        IGNORED(req_comp);
    }

    static unsigned char *
    gif_load(context *s, int *x, int *y, int *comp, int req_comp) {
        unsigned char *u = 0;
        gif g;
        memset(&g, 0, sizeof(g));

        u = gif_load_next(s, &g, comp, req_comp);
        if (u == (unsigned char *) s)
            u = 0;  // end of animated gif marker
        if (u) {
            *x = g.w;
            *y = g.h;
            if (req_comp && req_comp != 4)
                u = convert_format(u, 4, req_comp, g.w, g.h);
        } else if (g.out)
            free(g.out);

        return u;
    }

    static int gif_info(context *s, int *x, int *y, int *comp) {
        return gif_info_raw(s, x, y, comp);
    }

#endif

    static int hdr_test_core(context *s) {
        const char *signature = "#?RADIANCE\n";
        int i;
        for (i = 0; signature[i]; ++i)
            if (get8(s) != signature[i])
                return 0;
        return 1;
    }

    static int hdr_test(context *s) {
        int r = hdr_test_core(s);
        rewind(s);
        return r;
    }

#define HDR_BUFLEN  1024

    static char *hdr_gettoken(context *z, char *buffer) {
        int len = 0;
        char c = '\0';

        c = (char) get8(z);

        while (!at_eof(z) && c != '\n') {
            buffer[len++] = c;
            if (len == HDR_BUFLEN - 1) {
                // flush to end of line
                while (!at_eof(z) && get8(z) != '\n');
                break;
            }
            c = (char) get8(z);
        }

        buffer[len] = 0;
        return buffer;
    }

    static void hdr_convert(float *output, unsigned char *input, int req_comp) {
        if (input[3] != 0) {
            float f1;
            // Exponent
            f1 = (float) ldexp(1.0f, input[3] - (int) (128 + 8));
            if (req_comp <= 2)
                output[0] = (input[0] + input[1] + input[2]) * f1 / 3;
            else {
                output[0] = input[0] * f1;
                output[1] = input[1] * f1;
                output[2] = input[2] * f1;
            }
            if (req_comp == 2)
                output[1] = 1;
            if (req_comp == 4)
                output[3] = 1;
        } else {
            switch (req_comp) {
                case 4:
                    output[3] = 1; /* fallthrough */
                    /* no break */
                    /* no break */
                case 3:
                    output[0] = output[1] = output[2] = 0;
                    break;
                case 2:
                    output[1] = 1; /* fallthrough */
                    /* no break */
                    /* no break */
                case 1:
                    output[0] = 0;
                    break;
            }
        }
    }

    static float *hdr_load(context *s, int *x, int *y, int *comp, int req_comp) {
        char buffer[HDR_BUFLEN];
        char *token;
        int valid = 0;
        int width, height;
        unsigned char *scanline;
        float *hdr_data;
        int len;
        unsigned char count, value;
        int i, j, k, c1, c2, z;

        // Check identifier
        if (strcmp(hdr_gettoken(s, buffer), "#?RADIANCE") != 0) {
            err("not HDR", "Corrupt HDR image");
            return 0;
        }
        // Parse header
        for (;;) {
            token = hdr_gettoken(s, buffer);
            if (token[0] == 0)
                break;
            if (strcmp(token, "FORMAT=32-bit_rle_rgbe") == 0)
                valid = 1;
        }

        if (!valid) {
            err("unsupported format", "Unsupported HDR format");
            return 0;
        }

        // Parse width and height
        // can't use sscanf() if we're not using stdio!
        token = hdr_gettoken(s, buffer);
        if (strncmp(token, "-Y ", 3)) {
            err("unsupported data layout", "Unsupported HDR format");
            return 0;
        }
        token += 3;
        height = (int) strtol(token, &token, 10);
        while (*token == ' ')
            ++token;
        if (strncmp(token, "+X ", 3)) {
            err("unsupported data layout", "Unsupported HDR format");
            return 0;
        }
        token += 3;
        width = (int) strtol(token, NULL, 10);

        *x = width;
        *y = height;

        if (comp)
            *comp = 3;
        if (req_comp == 0)
            req_comp = 3;

        // Read data
        hdr_data = (float *) malloc(height * width * req_comp * sizeof(float));

        // Load image data
        // image data is stored as some number of sca
        if (width < 8 || width >= 32768) {
            // Read flat data
            for (j = 0; j < height; ++j) {
                for (i = 0; i < width; ++i) {
                    unsigned char rgbe[4];
                    main_decode_loop:
                    getn(s, rgbe, 4);
                    hdr_convert(hdr_data + j * width * req_comp + i * req_comp, rgbe,
                                req_comp);
                }
            }
        } else {
            // Read RLE-encoded data
            scanline = NULL;

            for (j = 0; j < height; ++j) {
                c1 = get8(s);
                c2 = get8(s);
                len = get8(s);
                if (c1 != 2 || c2 != 2 || (len & 0x80)) {
                    // not run-length encoded, so we have to actually use THIS data as a decoded
                    // pixel (note this can't be a valid pixel--one of RGB must be >= 128)
                    unsigned char rgbe[4];
                    rgbe[0] = (unsigned char) c1;
                    rgbe[1] = (unsigned char) c2;
                    rgbe[2] = (unsigned char) len;
                    rgbe[3] = (unsigned char) get8(s);
                    hdr_convert(hdr_data, rgbe, req_comp);
                    i = 1;
                    j = 0;
                    free(scanline);
                    goto main_decode_loop;
                    // yes, this makes no sense
                }
                len <<= 8;
                len |= get8(s);
                if (len != width) {
                    free(hdr_data);
                    free(scanline);
                    err("invalid decoded scanline length", "corrupt HDR");
                    return 0;
                }
                if (scanline == NULL)
                    scanline = (unsigned char *) malloc(width * 4);

                for (k = 0; k < 4; ++k) {
                    i = 0;
                    while (i < width) {
                        count = get8(s);
                        if (count > 128) {
                            // Run
                            value = get8(s);
                            count -= 128;
                            for (z = 0; z < count; ++z)
                                scanline[i++ * 4 + k] = value;
                        } else {
                            // Dump
                            for (z = 0; z < count; ++z)
                                scanline[i++ * 4 + k] = get8(s);
                        }
                    }
                }
                for (i = 0; i < width; ++i)
                    hdr_convert(hdr_data + (j * width + i) * req_comp, scanline + i * 4,
                                req_comp);
            }
            free(scanline);
        }

        return hdr_data;
    }

    static int hdr_info(context *s, int *x, int *y, int *comp) {
        char buffer[HDR_BUFLEN];
        char *token;
        int valid = 0;

        if (strcmp(hdr_gettoken(s, buffer), "#?RADIANCE") != 0) {
            rewind(s);
            return 0;
        }

        for (;;) {
            token = hdr_gettoken(s, buffer);
            if (token[0] == 0)
                break;
            if (strcmp(token, "FORMAT=32-bit_rle_rgbe") == 0)
                valid = 1;
        }

        if (!valid) {
            rewind(s);
            return 0;
        }
        token = hdr_gettoken(s, buffer);
        if (strncmp(token, "-Y ", 3)) {
            rewind(s);
            return 0;
        }
        token += 3;
        *y = (int) strtol(token, &token, 10);
        while (*token == ' ')
            ++token;
        if (strncmp(token, "+X ", 3)) {
            rewind(s);
            return 0;
        }
        token += 3;
        *x = (int) strtol(token, NULL, 10);
        *comp = 3;
        return 1;
    }

#ifndef STBI_NO_BMP

    static int bmp_info(context *s, int *x, int *y, int *comp) {
        int hsz;
        if (get8(s) != 'B' || get8(s) != 'M') {
            rewind(s);
            return 0;
        }
        skip(s, 12);
        hsz = get32le(s);
        if (hsz != 12 && hsz != 40 && hsz != 56 && hsz != 108 && hsz != 124) {
            rewind(s);
            return 0;
        }
        if (hsz == 12) {
            *x = get16le(s);
            *y = get16le(s);
        } else {
            *x = get32le(s);
            *y = get32le(s);
        }
        if (get16le(s) != 1) {
            rewind(s);
            return 0;
        }
        *comp = get16le(s) / 8;
        return 1;
    }

#endif

#ifndef STBI_NO_PSD

    static int psd_info(context *s, int *x, int *y, int *comp) {
        int channelCount;
        if (get32be(s) != 0x38425053) {
            rewind(s);
            return 0;
        }
        if (get16be(s) != 1) {
            rewind(s);
            return 0;
        }
        skip(s, 6);
        channelCount = get16be(s);
        if (channelCount < 0 || channelCount > 16) {
            rewind(s);
            return 0;
        }
        *y = get32be(s);
        *x = get32be(s);
        if (get16be(s) != 8) {
            rewind(s);
            return 0;
        }
        if (get16be(s) != 3) {
            rewind(s);
            return 0;
        }
        *comp = 4;
        return 1;
    }

#endif

#ifndef STBI_NO_PIC

    static int pic_info(context *s, int *x, int *y, int *comp) {
        int act_comp = 0, num_packets = 0, chained;
        pic_packet packets[10];

        if (!pic_is4(s, "\x53\x80\xF6\x34")) {
            rewind(s);
            return 0;
        }

        skip(s, 88);

        *x = get16be(s);
        *y = get16be(s);
        if (at_eof(s)) {
            rewind(s);
            return 0;
        }
        if ((*x) != 0 && (1 << 28) / (*x) < (*y)) {
            rewind(s);
            return 0;
        }

        skip(s, 8);

        do {
            pic_packet *packet;

            if (num_packets == sizeof(packets) / sizeof(packets[0]))
                return 0;

            packet = &packets[num_packets++];
            chained = get8(s);
            packet->size = get8(s);
            packet->type = get8(s);
            packet->channel = get8(s);
            act_comp |= packet->channel;

            if (at_eof(s)) {
                rewind(s);
                return 0;
            }
            if (packet->size != 8) {
                rewind(s);
                return 0;
            }
        } while (chained);

        *comp = (act_comp & 0x10 ? 4 : 3);

        return 1;
    }

#endif

#ifndef STBI_NO_PNM

    static int pnm_test(context *s) {
        char p, t;
        p = (char) get8(s);
        t = (char) get8(s);
        if (p != 'P' || (t != '5' && t != '6')) {
            rewind(s);
            return 0;
        }
        return 1;
    }

    static unsigned char *
    pnm_load(context *s, int *x, int *y, int *comp, int req_comp) {
        unsigned char *out;
        if (!pnm_info(s, (int *) &s->img_x, (int *) &s->img_y, (int *) &s->img_n))
            return 0;
        *x = s->img_x;
        *y = s->img_y;
        *comp = s->img_n;

        out = (unsigned char *) malloc(s->img_n * s->img_x * s->img_y);
        if (!out) {
            err("outofmem", "Out of memory");
            return 0;
        }
        getn(s, out, s->img_n * s->img_x * s->img_y);

        if (req_comp && req_comp != s->img_n) {
            out = convert_format(out, s->img_n, req_comp, s->img_x, s->img_y);
            if (out == NULL)
                return out; // convert_format frees input on failure
        }
        return out;
    }

    static int pnm_isspace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\v' || c == '\f' || c == '\r';
    }

    static void pnm_skip_whitespace(context *s, char *c) {
        while (!at_eof(s) && pnm_isspace(*c))
            *c = (char) get8(s);
    }

    static int pnm_isdigit(char c) {
        return c >= '0' && c <= '9';
    }

    static int pnm_getinteger(context *s, char *c) {
        int value = 0;

        while (!at_eof(s) && pnm_isdigit(*c)) {
            value = value * 10 + (*c - '0');
            *c = (char) get8(s);
        }

        return value;
    }

    static int pnm_info(context *s, int *x, int *y, int *comp) {
        int maxv;
        char c, p, t;

        rewind(s);

        // Get identifier
        p = (char) get8(s);
        t = (char) get8(s);
        if (p != 'P' || (t != '5' && t != '6')) {
            rewind(s);
            return 0;
        }

        *comp = (t == '6') ? 3 : 1; // '5' is 1-component .pgm; '6' is 3-component .ppm

        c = (char) get8(s);
        pnm_skip_whitespace(s, &c);

        *x = pnm_getinteger(s, &c); // read width
        pnm_skip_whitespace(s, &c);

        *y = pnm_getinteger(s, &c); // read height
        pnm_skip_whitespace(s, &c);

        maxv = pnm_getinteger(s, &c);  // read max value

        if (maxv > 255)
            return err("max value > 255", "PPM image not 8-bit");
        else
            return 1;
    }

#endif

    static int info_main(context *s, int *x, int *y, int *comp) {
        if (jpeg_info(s, x, y, comp))
            return 1;
        if (png_info(s, x, y, comp))
            return 1;
#ifndef STBI_NO_GIF
        if (gif_info(s, x, y, comp))
            return 1;
#endif
#ifndef STBI_NO_BMP
        if (bmp_info(s, x, y, comp))
            return 1;
#endif

#ifndef STBI_NO_PSD
        if (psd_info(s, x, y, comp))
            return 1;
#endif

#ifndef STBI_NO_PIC
        if (pic_info(s, x, y, comp))
            return 1;
#endif

#ifndef STBI_NO_PNM
        if (pnm_info(s, x, y, comp))
            return 1;
#endif
        if (hdr_info(s, x, y, comp))
            return 1;

        // test tga last because it's a crappy test!
#ifndef STBI_NO_TGA
        if (tga_info(s, x, y, comp))
            return 1;
#endif
        return err("unknown image type", "Image not of any known type, or corrupt");
    }

    int stbi_info_from_file(FILE *f, int *x, int *y, int *comp) {
        int r;
        context s;
        long pos = ftell(f);
        start_file(&s, f);
        r = info_main(&s, x, y, comp);
        fseek(f, pos, SEEK_SET);
        return r;
    }

    int stbi_info(char const *filename, int *x, int *y, int *comp) {
        FILE *f = fopen(filename, "rb");
        int result;
        if (!f)
            return err("can't fopen", "Unable to open file");
        result = stbi_info_from_file(f, x, y, comp);
        fclose(f);
        return result;
    }

    int stbi_info_from_memory(unsigned char const *buffer, int len, int *x, int *y, int *comp) {
        context s;
        start_mem(&s, buffer, len);
        return info_main(&s, x, y, comp);
    }

    int
    stbi_info_from_callbacks(stbi_io_callbacks const *c, void *user, int *x, int *y, int *comp) {
        context s;
        start_callbacks(&s, (stbi_io_callbacks *) c, user);
        return info_main(&s, x, y, comp);
    }
} // end stbi namespace

//begin initialize jpgd decompresser
namespace jpgd {
#define JPGD_SUPPORT_FREQ_DOMAIN_UPSAMPLING 1
#define JPGD_TRUE (1)
#define JPGD_FALSE (0)

//define value
    const char *failure_reason(void);

    unsigned char *
    decompress_jpeg_image_from_memory(const unsigned char *pSrc_data, int src_data_size, int *width,
                                      int *height, int actual_comps, int req_comps);

    unsigned char *
    decompress_jpeg_image_from_file(const char *pSrc_filename, int *width, int *height,
                                    int *actual_comps, int req_comps);

// Success/failure error codes.
    enum jpgd_status {
        JPGD_SUCCESS = 0,
        JPGD_FAILED = -1,
        JPGD_DONE = 1,
        JPGD_BAD_DHT_COUNTS = -256,
        JPGD_BAD_DHT_INDEX,
        JPGD_BAD_DHT_MARKER,
        JPGD_BAD_DQT_MARKER,
        JPGD_BAD_DQT_TABLE,
        JPGD_BAD_PRECISION,
        JPGD_BAD_HEIGHT,
        JPGD_BAD_WIDTH,
        JPGD_TOO_MANY_COMPONENTS,
        JPGD_BAD_SOF_LENGTH,
        JPGD_BAD_VARIABLE_MARKER,
        JPGD_BAD_DRI_LENGTH,
        JPGD_BAD_SOS_LENGTH,
        JPGD_BAD_SOS_COMP_ID,
        JPGD_W_EXTRA_BYTES_BEFORE_MARKER,
        JPGD_NO_ARITHMITIC_SUPPORT,
        JPGD_UNEXPECTED_MARKER,
        JPGD_NOT_JPEG,
        JPGD_UNSUPPORTED_MARKER,
        JPGD_BAD_DQT_LENGTH,
        JPGD_TOO_MANY_BLOCKS,
        JPGD_UNDEFINED_QUANT_TABLE,
        JPGD_UNDEFINED_HUFF_TABLE,
        JPGD_NOT_SINGLE_SCAN,
        JPGD_UNSUPPORTED_COLORSPACE,
        JPGD_UNSUPPORTED_SAMP_FACTORS,
        JPGD_DECODE_ERROR,
        JPGD_BAD_RESTART_MARKER,
        JPGD_ASSERTION_ERROR,
        JPGD_BAD_SOS_SPECTRAL,
        JPGD_BAD_SOS_SUCCESSIVE,
        JPGD_STREAM_READ,
        JPGD_NOTENOUGHMEM
    };

    class jpeg_decoder_stream {
    public:
        jpeg_decoder_stream() {
        }

        virtual ~jpeg_decoder_stream() {
        }

        virtual int read(unsigned char *pBuf, int max_bytes_to_read, bool *pEOF_flag) = 0;
    };

// stdio FILE stream class.
    class jpeg_decoder_file_stream : public jpeg_decoder_stream {
        jpeg_decoder_file_stream(const jpeg_decoder_file_stream &);

        jpeg_decoder_file_stream &operator=(const jpeg_decoder_file_stream &);

        FILE *m_pFile;
        bool m_eof_flag, m_error_flag;

    public:
        jpeg_decoder_file_stream();

        virtual ~jpeg_decoder_file_stream();

        bool open(const char *Pfilename);

        void close();

        virtual int read(unsigned char *pBuf, int max_bytes_to_read, bool *pEOF_flag);
    };

// Memory stream class.
    class jpeg_decoder_mem_stream : public jpeg_decoder_stream {
        const unsigned char *m_pSrc_data;
        unsigned int m_ofs, m_size;

    public:
        jpeg_decoder_mem_stream() :
                m_pSrc_data(NULL), m_ofs(0), m_size(0) {
        }

        jpeg_decoder_mem_stream(const unsigned char *pSrc_data, unsigned int size) :
                m_pSrc_data(pSrc_data), m_ofs(0), m_size(size) {
        }

        virtual ~jpeg_decoder_mem_stream() {
        }

        bool open(const unsigned char *pSrc_data, unsigned int size);

        void close() {
            m_pSrc_data = NULL;
            m_ofs = 0;
            m_size = 0;
        }

        virtual int read(unsigned char *pBuf, int max_bytes_to_read, bool *pEOF_flag);
    };

// Loads JPEG file from a jpeg_decoder_stream.
    unsigned char *
    decompress_jpeg_image_from_stream(jpeg_decoder_stream *pStream, int *width, int *height,
                                      int *actual_comps, int req_comps);

    enum {
        JPGD_IN_BUF_SIZE = 8192,
        MAX_BLOCKS_PER_MCU = 10,
        MAX_HUFF_TABLES = 8,
        MAX_QUANT_TABLES = 4,
        MAX_COMPONENTS = 4,
        MAX_COMPS_IN_SCAN = 4,
        MAX_BLOCKS_PER_ROW = 8192,
        MAX_HEIGHT = 16384,
        MAX_WIDTH = 16384
    };

    class jpeg_decoder {
    public:
        jpeg_decoder(jpeg_decoder_stream *pStream);

        ~jpeg_decoder();

        int begin_decoding();

        int decode(const void **pScan_line, unsigned int *pScan_line_len);

        inline jpgd_status get_error_code() const {
            return m_error_code;
        }

        inline int get_width() const {
            return m_image_x_size;
        }

        inline int get_height() const {
            return m_image_y_size;
        }

        inline int get_num_components() const {
            return m_comps_in_frame;
        }

        inline int get_bytes_per_pixel() const {
            return m_dest_bytes_per_pixel;
        }

        inline int get_bytes_per_scan_line() const {
            return m_image_x_size * get_bytes_per_pixel();
        }

        // Returns the total number of bytes actually consumed by the decoder (which should equal the actual size of the JPEG file).
        inline int get_total_bytes_read() const {
            return m_total_bytes_read;
        }

    private:
        jpeg_decoder(const jpeg_decoder &);

        jpeg_decoder &operator=(const jpeg_decoder &);

        typedef void (*pDecode_block_func)(jpeg_decoder *, int, int, int);

        struct huff_tables {
            bool ac_table;
            unsigned int look_up[256];
            unsigned int look_up2[256];
            unsigned char code_size[256];
            unsigned int tree[512];
        };

        struct coeff_buf {
            unsigned char *pData;
            int block_num_x, block_num_y;
            int block_len_x, block_len_y;
            int block_size;
        };

        struct mem_block {
            mem_block *m_pNext;
            size_t m_used_count;
            size_t m_size;
            char m_data[1];
        };

        jmp_buf m_jmp_state;
        mem_block *m_pMem_blocks;
        int m_image_x_size;
        int m_image_y_size;
        jpeg_decoder_stream *m_pStream;
        int m_progressive_flag;
        unsigned char m_huff_ac[MAX_HUFF_TABLES];
        unsigned char *m_huff_num[MAX_HUFF_TABLES]; // pointer to number of Huffman codes per bit size
        unsigned char *m_huff_val[MAX_HUFF_TABLES]; // pointer to Huffman codes per bit size
        signed short *m_quant[MAX_QUANT_TABLES]; // pointer to quantization tables
        int m_scan_type; // Gray, Yh1v1, Yh1v2, Yh2v1, Yh2v2 (CMYK111, CMYK4114 no longer supported)
        int m_comps_in_frame;                         // # of components in frame
        int m_comp_h_samp[MAX_COMPONENTS]; // component's horizontal sampling factor
        int m_comp_v_samp[MAX_COMPONENTS];   // component's vertical sampling factor
        int m_comp_quant[MAX_COMPONENTS]; // component's quantization table selector
        int m_comp_ident[MAX_COMPONENTS];        // component's ID
        int m_comp_h_blocks[MAX_COMPONENTS];
        int m_comp_v_blocks[MAX_COMPONENTS];
        int m_comps_in_scan;                          // # of components in scan
        int m_comp_list[MAX_COMPS_IN_SCAN];      // components in this scan
        int m_comp_dc_tab[MAX_COMPONENTS]; // component's DC Huffman coding table selector
        int m_comp_ac_tab[MAX_COMPONENTS]; // component's AC Huffman coding table selector
        int m_spectral_start;                         // spectral selection start
        int m_spectral_end;                           // spectral selection end
        int m_successive_low;                        // successive approximation low
        int m_successive_high;                      // successive approximation high
        int m_max_mcu_x_size;                         // MCU's max. X size in pixels
        int m_max_mcu_y_size;                         // MCU's max. Y size in pixels
        int m_blocks_per_mcu;
        int m_max_blocks_per_row;
        int m_mcus_per_row, m_mcus_per_col;
        int m_mcu_org[MAX_BLOCKS_PER_MCU];
        int m_total_lines_left;                       // total # lines left in image
        int m_mcu_lines_left;                      // total # lines left in this MCU
        int m_real_dest_bytes_per_scan_line;
        int m_dest_bytes_per_scan_line;               // rounded up
        int m_dest_bytes_per_pixel;                   // 4 (RGB) or 1 (Y)
        huff_tables *m_pHuff_tabs[MAX_HUFF_TABLES];
        coeff_buf *m_dc_coeffs[MAX_COMPONENTS];
        coeff_buf *m_ac_coeffs[MAX_COMPONENTS];
        int m_eob_run;
        int m_block_y_mcu[MAX_COMPONENTS];
        unsigned char *m_pIn_buf_ofs;
        int m_in_buf_left;
        int m_tem_flag;
        bool m_eof_flag;
        unsigned char m_in_buf_pad_start[128];
        unsigned char m_in_buf[JPGD_IN_BUF_SIZE + 128];
        unsigned char m_in_buf_pad_end[128];
        int m_bits_left;
        unsigned int m_bit_buf;
        int m_restart_interval;
        int m_restarts_left;
        int m_next_restart_num;
        int m_max_mcus_per_row;
        int m_max_blocks_per_mcu;
        int m_expanded_blocks_per_mcu;
        int m_expanded_blocks_per_row;
        int m_expanded_blocks_per_component;
        bool m_freq_domain_chroma_upsample;
        int m_max_mcus_per_col;
        unsigned int m_last_dc_val[MAX_COMPONENTS];
        signed short *m_pMCU_coefficients;
        int m_mcu_block_max_zag[MAX_BLOCKS_PER_MCU];
        unsigned char *m_pSample_buf;
        int m_crr[256];
        int m_cbb[256];
        int m_crg[256];
        int m_cbg[256];
        unsigned char *m_pScan_line_0;
        unsigned char *m_pScan_line_1;
        jpgd_status m_error_code;
        bool m_ready_flag;
        int m_total_bytes_read;

        void free_all_blocks();

        void stop_decoding(jpgd_status status);

        void *alloc(size_t n, bool zero = false);

        void word_clear(void *p, unsigned short c, unsigned int n);

        void prep_in_buffer();

        void read_dht_marker();

        void read_dqt_marker();

        void read_sof_marker();

        void skip_variable_marker();

        void read_dri_marker();

        void read_sos_marker();

        int next_marker();

        int process_markers();

        void locate_soi_marker();

        void locate_sof_marker();

        int locate_sos_marker();

        void init(jpeg_decoder_stream *pStream);

        void create_look_ups();

        void fix_in_buffer();

        void transform_mcu(int mcu_row);

        void transform_mcu_expand(int mcu_row);

        coeff_buf *
        coeff_buf_open(int block_num_x, int block_num_y, int block_len_x, int block_len_y);

        inline signed short *coeff_buf_getp(coeff_buf *cb, int block_x, int block_y);

        void load_next_row();

        void decode_next_row();

        void make_huff_table(int index, huff_tables *pH);

        void check_quant_tables();

        void check_huff_tables();

        void calc_mcu_block_order();

        int init_scan();

        void init_frame();

        void process_restart();

        void decode_scan(pDecode_block_func decode_block_func);

        void init_progressive();

        void init_sequential();

        void decode_start();

        void decode_init(jpeg_decoder_stream *pStream);

        void H2V2Convert();

        void H2V1Convert();

        void H1V2Convert();

        void H1V1Convert();

        void gray_convert();

        void expanded_convert();

        void find_eoi();

        inline unsigned int get_char();

        inline unsigned int get_char(bool *pPadding_flag);

        inline void stuff_char(unsigned char q);

        inline unsigned char get_octet();

        inline unsigned int get_bits(int num_bits);

        inline unsigned int get_bits_no_markers(int numbits);

        inline int huff_decode(huff_tables *pH);

        inline int huff_decode(huff_tables *pH, int &extrabits);

        static inline unsigned char clamp(int i);

        static void
        decode_block_dc_first(jpeg_decoder *pD, int component_id, int block_x, int block_y);

        static void
        decode_block_dc_refine(jpeg_decoder *pD, int component_id, int block_x, int block_y);

        static void
        decode_block_ac_first(jpeg_decoder *pD, int component_id, int block_x, int block_y);

        static void
        decode_block_ac_refine(jpeg_decoder *pD, int component_id, int block_x, int block_y);
    };

// DCT coefficients are stored in this sequence.
    static int g_ZAG[64] = {0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32, 25, 18, 11, 4, 5, 12, 19, 26, 33,
                            40, 48, 41, 34, 27, 20, 13, 6, 7, 14, 21, 28, 35, 42, 49, 56, 57, 50,
                            43, 36, 29, 22, 15, 23, 30, 37, 44, 51, 58, 59, 52, 45, 38, 31, 39, 46,
                            53, 60, 61, 54, 47, 55, 62, 63};
    enum JPEG_MARKER {
        M_SOF0 = 0xC0,
        M_SOF1 = 0xC1,
        M_SOF2 = 0xC2,
        M_SOF3 = 0xC3,
        M_SOF5 = 0xC5,
        M_SOF6 = 0xC6,
        M_SOF7 = 0xC7,
        M_JPG = 0xC8,
        M_SOF9 = 0xC9,
        M_SOF10 = 0xCA,
        M_SOF11 = 0xCB,
        M_SOF13 = 0xCD,
        M_SOF14 = 0xCE,
        M_SOF15 = 0xCF,
        M_DHT = 0xC4,
        M_DAC = 0xCC,
        M_RST0 = 0xD0,
        M_RST1 = 0xD1,
        M_RST2 = 0xD2,
        M_RST3 = 0xD3,
        M_RST4 = 0xD4,
        M_RST5 = 0xD5,
        M_RST6 = 0xD6,
        M_RST7 = 0xD7,
        M_SOI = 0xD8,
        M_EOI = 0xD9,
        M_SOS = 0xDA,
        M_DQT = 0xDB,
        M_DNL = 0xDC,
        M_DRI = 0xDD,
        M_DHP = 0xDE,
        M_EXP = 0xDF,
        M_APP0 = 0xE0,
        M_APP15 = 0xEF,
        M_JPG0 = 0xF0,
        M_JPG13 = 0xFD,
        M_COM = 0xFE,
        M_TEM = 0x01,
        M_ERROR = 0x100,
        RST0 = 0xD0
    };
    enum JPEG_SUBSAMPLING {
        JPGD_GRAYSCALE = 0, JPGD_YH1V1, JPGD_YH2V1, JPGD_YH1V2, JPGD_YH2V2
    };

#define CONST_BITS  13
#define PASS1_BITS  2
#define SCALEDONE ((signed int)1)

#define FIX_0_298631336  ((signed int)2446)        /* FIX(0.298631336) */
#define FIX_0_390180644  ((signed int)3196)        /* FIX(0.390180644) */
#define FIX_0_541196100  ((signed int)4433)        /* FIX(0.541196100) */
#define FIX_0_765366865  ((signed int)6270)        /* FIX(0.765366865) */
#define FIX_0_899976223  ((signed int)7373)        /* FIX(0.899976223) */
#define FIX_1_175875602  ((signed int)9633)        /* FIX(1.175875602) */
#define FIX_1_501321110  ((signed int)12299)       /* FIX(1.501321110) */
#define FIX_1_847759065  ((signed int)15137)       /* FIX(1.847759065) */
#define FIX_1_961570560  ((signed int)16069)       /* FIX(1.961570560) */
#define FIX_2_053119869  ((signed int)16819)       /* FIX(2.053119869) */
#define FIX_2_562915447  ((signed int)20995)       /* FIX(2.562915447) */
#define FIX_3_072711026  ((signed int)25172)       /* FIX(3.072711026) */

#define DESCALE(x, n)  (((x) + (SCALEDONE << ((n)-1))) >> (n))
#define DESCALE_ZEROSHIFT(x, n)  (((x) + (128 << (n)) + (SCALEDONE << ((n)-1))) >> (n))

#define MULTIPLY(var, cnst)  ((var) * (cnst))

#define CLAMP(i) ((static_cast<unsigned int>(i) > 255) ? (((~i) >> 31) & 0xFF) : (i))

    static const char *err_reason;

    template<int NONZERO_COLS>
    struct Row {
        static void idct(int *pTemp, const signed short *pSrc) {
#define ACCESS_COL(x) (((x) < NONZERO_COLS) ? (int)pSrc[x] : 0)

            const int z2 = ACCESS_COL(2), z3 = ACCESS_COL(6);

            const int z1 = MULTIPLY(z2 + z3, FIX_0_541196100);
            const int tmp2 = z1 + MULTIPLY(z3, -FIX_1_847759065);
            const int tmp3 = z1 + MULTIPLY(z2, FIX_0_765366865);

            const int tmp0 = (ACCESS_COL(0) + ACCESS_COL(4)) << CONST_BITS;
            const int tmp1 = (ACCESS_COL(0) - ACCESS_COL(4)) << CONST_BITS;

            const int tmp10 = tmp0 + tmp3, tmp13 = tmp0 - tmp3, tmp11 = tmp1 + tmp2,
                    tmp12 = tmp1 - tmp2;

            const int atmp0 = ACCESS_COL(7), atmp1 = ACCESS_COL(5),
                    atmp2 = ACCESS_COL(3), atmp3 = ACCESS_COL(1);

            const int bz1 = atmp0 + atmp3, bz2 = atmp1 + atmp2, bz3 = atmp0 + atmp2,
                    bz4 = atmp1 + atmp3;
            const int bz5 = MULTIPLY(bz3 + bz4, FIX_1_175875602);

            const int az1 = MULTIPLY(bz1, -FIX_0_899976223);
            const int az2 = MULTIPLY(bz2, -FIX_2_562915447);
            const int az3 = MULTIPLY(bz3, -FIX_1_961570560) + bz5;
            const int az4 = MULTIPLY(bz4, -FIX_0_390180644) + bz5;

            const int btmp0 = MULTIPLY(atmp0, FIX_0_298631336) + az1 + az3;
            const int btmp1 = MULTIPLY(atmp1, FIX_2_053119869) + az2 + az4;
            const int btmp2 = MULTIPLY(atmp2, FIX_3_072711026) + az2 + az3;
            const int btmp3 = MULTIPLY(atmp3, FIX_1_501321110) + az1 + az4;

            pTemp[0] = DESCALE(tmp10 + btmp3, CONST_BITS - PASS1_BITS);
            pTemp[7] = DESCALE(tmp10 - btmp3, CONST_BITS - PASS1_BITS);
            pTemp[1] = DESCALE(tmp11 + btmp2, CONST_BITS - PASS1_BITS);
            pTemp[6] = DESCALE(tmp11 - btmp2, CONST_BITS - PASS1_BITS);
            pTemp[2] = DESCALE(tmp12 + btmp1, CONST_BITS - PASS1_BITS);
            pTemp[5] = DESCALE(tmp12 - btmp1, CONST_BITS - PASS1_BITS);
            pTemp[3] = DESCALE(tmp13 + btmp0, CONST_BITS - PASS1_BITS);
            pTemp[4] = DESCALE(tmp13 - btmp0, CONST_BITS - PASS1_BITS);
        }
    };

    template<>
    struct Row<0> {
        static void idct(int *pTemp, const signed short *pSrc) {
#ifdef _MSC_VER
                                                                                                                                    pTemp;
		pSrc;
#endif
        }
    };

    template<>
    struct Row<1> {
        static void idct(int *pTemp, const signed short *pSrc) {
            const int dcval = (pSrc[0] << PASS1_BITS);

            pTemp[0] = dcval;
            pTemp[1] = dcval;
            pTemp[2] = dcval;
            pTemp[3] = dcval;
            pTemp[4] = dcval;
            pTemp[5] = dcval;
            pTemp[6] = dcval;
            pTemp[7] = dcval;
        }
    };

// Compiler creates a fast path 1D IDCT for X non-zero rows
    template<int NONZERO_ROWS>
    struct Col {
        static void idct(unsigned char *pDst_ptr, const int *pTemp) {
            // ACCESS_ROW() will be optimized at compile time to either an array access, or 0.
#define ACCESS_ROW(x) (((x) < NONZERO_ROWS) ? pTemp[x * 8] : 0)

            const int z2 = ACCESS_ROW(2);
            const int z3 = ACCESS_ROW(6);

            const int z1 = MULTIPLY(z2 + z3, FIX_0_541196100);
            const int tmp2 = z1 + MULTIPLY(z3, -FIX_1_847759065);
            const int tmp3 = z1 + MULTIPLY(z2, FIX_0_765366865);

            const int tmp0 = (ACCESS_ROW(0) + ACCESS_ROW(4)) << CONST_BITS;
            const int tmp1 = (ACCESS_ROW(0) - ACCESS_ROW(4)) << CONST_BITS;

            const int tmp10 = tmp0 + tmp3, tmp13 = tmp0 - tmp3, tmp11 = tmp1 + tmp2,
                    tmp12 = tmp1 - tmp2;

            const int atmp0 = ACCESS_ROW(7), atmp1 = ACCESS_ROW(5),
                    atmp2 = ACCESS_ROW(3), atmp3 = ACCESS_ROW(1);

            const int bz1 = atmp0 + atmp3, bz2 = atmp1 + atmp2, bz3 = atmp0 + atmp2,
                    bz4 = atmp1 + atmp3;
            const int bz5 = MULTIPLY(bz3 + bz4, FIX_1_175875602);

            const int az1 = MULTIPLY(bz1, -FIX_0_899976223);
            const int az2 = MULTIPLY(bz2, -FIX_2_562915447);
            const int az3 = MULTIPLY(bz3, -FIX_1_961570560) + bz5;
            const int az4 = MULTIPLY(bz4, -FIX_0_390180644) + bz5;

            const int btmp0 = MULTIPLY(atmp0, FIX_0_298631336) + az1 + az3;
            const int btmp1 = MULTIPLY(atmp1, FIX_2_053119869) + az2 + az4;
            const int btmp2 = MULTIPLY(atmp2, FIX_3_072711026) + az2 + az3;
            const int btmp3 = MULTIPLY(atmp3, FIX_1_501321110) + az1 + az4;

            int i = DESCALE_ZEROSHIFT(tmp10 + btmp3, CONST_BITS + PASS1_BITS + 3);
            pDst_ptr[8 * 0] = (unsigned char) CLAMP(i);

            i = DESCALE_ZEROSHIFT(tmp10 - btmp3, CONST_BITS + PASS1_BITS + 3);
            pDst_ptr[8 * 7] = (unsigned char) CLAMP(i);

            i = DESCALE_ZEROSHIFT(tmp11 + btmp2, CONST_BITS + PASS1_BITS + 3);
            pDst_ptr[8 * 1] = (unsigned char) CLAMP(i);

            i = DESCALE_ZEROSHIFT(tmp11 - btmp2, CONST_BITS + PASS1_BITS + 3);
            pDst_ptr[8 * 6] = (unsigned char) CLAMP(i);

            i = DESCALE_ZEROSHIFT(tmp12 + btmp1, CONST_BITS + PASS1_BITS + 3);
            pDst_ptr[8 * 2] = (unsigned char) CLAMP(i);

            i = DESCALE_ZEROSHIFT(tmp12 - btmp1, CONST_BITS + PASS1_BITS + 3);
            pDst_ptr[8 * 5] = (unsigned char) CLAMP(i);

            i = DESCALE_ZEROSHIFT(tmp13 + btmp0, CONST_BITS + PASS1_BITS + 3);
            pDst_ptr[8 * 3] = (unsigned char) CLAMP(i);

            i = DESCALE_ZEROSHIFT(tmp13 - btmp0, CONST_BITS + PASS1_BITS + 3);
            pDst_ptr[8 * 4] = (unsigned char) CLAMP(i);
        }
    };

    template<>
    struct Col<1> {
        static void idct(unsigned char *pDst_ptr, const int *pTemp) {
            int dcval = DESCALE_ZEROSHIFT(pTemp[0], PASS1_BITS + 3);
            const unsigned char dcval_clamped = (unsigned char) CLAMP(dcval);
            pDst_ptr[0 * 8] = dcval_clamped;
            pDst_ptr[1 * 8] = dcval_clamped;
            pDst_ptr[2 * 8] = dcval_clamped;
            pDst_ptr[3 * 8] = dcval_clamped;
            pDst_ptr[4 * 8] = dcval_clamped;
            pDst_ptr[5 * 8] = dcval_clamped;
            pDst_ptr[6 * 8] = dcval_clamped;
            pDst_ptr[7 * 8] = dcval_clamped;
        }
    };

    static const unsigned char s_idct_row_table[] = {1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0,
                                                     2, 1, 0, 0, 0, 0, 0, 0, 2, 1, 1, 0, 0, 0, 0, 0,
                                                     2, 2, 1, 0, 0, 0, 0, 0, 3, 2, 1, 0, 0, 0, 0, 0,
                                                     4, 2, 1, 0, 0, 0, 0, 0, 4, 3, 1, 0, 0, 0, 0, 0,
                                                     4, 3, 2, 0, 0, 0, 0, 0, 4, 3, 2, 1, 0, 0, 0, 0,
                                                     4, 3, 2, 1, 1, 0, 0, 0, 4, 3, 2, 2, 1, 0, 0, 0,
                                                     4, 3, 3, 2, 1, 0, 0, 0, 4, 4, 3, 2, 1, 0, 0, 0,
                                                     5, 4, 3, 2, 1, 0, 0, 0, 6, 4, 3, 2, 1, 0, 0, 0,
                                                     6, 5, 3, 2, 1, 0, 0, 0, 6, 5, 4, 2, 1, 0, 0, 0,
                                                     6, 5, 4, 3, 1, 0, 0, 0, 6, 5, 4, 3, 2, 0, 0, 0,
                                                     6, 5, 4, 3, 2, 1, 0, 0, 6, 5, 4, 3, 2, 1, 1, 0,
                                                     6, 5, 4, 3, 2, 2, 1, 0, 6, 5, 4, 3, 3, 2, 1, 0,
                                                     6, 5, 4, 4, 3, 2, 1, 0, 6, 5, 5, 4, 3, 2, 1, 0,
                                                     6, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0,
                                                     8, 6, 5, 4, 3, 2, 1, 0, 8, 7, 5, 4, 3, 2, 1, 0,
                                                     8, 7, 6, 4, 3, 2, 1, 0, 8, 7, 6, 5, 3, 2, 1, 0,
                                                     8, 7, 6, 5, 4, 2, 1, 0, 8, 7, 6, 5, 4, 3, 1, 0,
                                                     8, 7, 6, 5, 4, 3, 2, 0, 8, 7, 6, 5, 4, 3, 2, 1,
                                                     8, 7, 6, 5, 4, 3, 2, 2, 8, 7, 6, 5, 4, 3, 3, 2,
                                                     8, 7, 6, 5, 4, 4, 3, 2, 8, 7, 6, 5, 5, 4, 3, 2,
                                                     8, 7, 6, 6, 5, 4, 3, 2, 8, 7, 7, 6, 5, 4, 3, 2,
                                                     8, 8, 7, 6, 5, 4, 3, 2, 8, 8, 8, 6, 5, 4, 3, 2,
                                                     8, 8, 8, 7, 5, 4, 3, 2, 8, 8, 8, 7, 6, 4, 3, 2,
                                                     8, 8, 8, 7, 6, 5, 3, 2, 8, 8, 8, 7, 6, 5, 4, 2,
                                                     8, 8, 8, 7, 6, 5, 4, 3, 8, 8, 8, 7, 6, 5, 4, 4,
                                                     8, 8, 8, 7, 6, 5, 5, 4, 8, 8, 8, 7, 6, 6, 5, 4,
                                                     8, 8, 8, 7, 7, 6, 5, 4, 8, 8, 8, 8, 7, 6, 5, 4,
                                                     8, 8, 8, 8, 8, 6, 5, 4, 8, 8, 8, 8, 8, 7, 5, 4,
                                                     8, 8, 8, 8, 8, 7, 6, 4, 8, 8, 8, 8, 8, 7, 6, 5,
                                                     8, 8, 8, 8, 8, 7, 6, 6, 8, 8, 8, 8, 8, 7, 7, 6,
                                                     8, 8, 8, 8, 8, 8, 7, 6, 8, 8, 8, 8, 8, 8, 8, 6,
                                                     8, 8, 8, 8, 8, 8, 8, 7, 8, 8, 8, 8, 8, 8, 8,
                                                     8,};

    static const unsigned char s_idct_col_table[] = {1, 1, 2, 3, 3, 3, 3, 3, 3, 4, 5, 5, 5, 5, 5, 5,
                                                     5, 5, 5, 5, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
                                                     7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
                                                     8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
                                                     8};

    void idct(const signed short *pSrc_ptr, unsigned char *pDst_ptr, int block_max_zag) {
        assert(block_max_zag >= 1);
        assert(block_max_zag <= 64);

        if (block_max_zag <= 1) {
            int k = ((pSrc_ptr[0] + 4) >> 3) + 128;
            k = CLAMP(k);
            k = k | (k << 8);
            k = k | (k << 16);

            for (int i = 8; i > 0; i--) {
                *(int *) &pDst_ptr[0] = k;
                *(int *) &pDst_ptr[4] = k;
                pDst_ptr += 8;
            }
            return;
        }

        int temp[64];

        const signed short *pSrc = pSrc_ptr;
        int *pTemp = temp;

        const unsigned char *pRow_tab = &s_idct_row_table[(block_max_zag - 1) * 8];
        int i;
        for (i = 8; i > 0; i--, pRow_tab++) {
            switch (*pRow_tab) {
                case 0:
                    Row<0>::idct(pTemp, pSrc);
                    break;
                case 1:
                    Row<1>::idct(pTemp, pSrc);
                    break;
                case 2:
                    Row<2>::idct(pTemp, pSrc);
                    break;
                case 3:
                    Row<3>::idct(pTemp, pSrc);
                    break;
                case 4:
                    Row<4>::idct(pTemp, pSrc);
                    break;
                case 5:
                    Row<5>::idct(pTemp, pSrc);
                    break;
                case 6:
                    Row<6>::idct(pTemp, pSrc);
                    break;
                case 7:
                    Row<7>::idct(pTemp, pSrc);
                    break;
                case 8:
                    Row<8>::idct(pTemp, pSrc);
                    break;
                default:
                    break;
            }

            pSrc += 8;
            pTemp += 8;
        }

        pTemp = temp;

        const int nonzero_rows = s_idct_col_table[block_max_zag - 1];
        for (i = 8; i > 0; i--) {
            switch (nonzero_rows) {
                case 1:
                    Col<1>::idct(pDst_ptr, pTemp);
                    break;
                case 2:
                    Col<2>::idct(pDst_ptr, pTemp);
                    break;
                case 3:
                    Col<3>::idct(pDst_ptr, pTemp);
                    break;
                case 4:
                    Col<4>::idct(pDst_ptr, pTemp);
                    break;
                case 5:
                    Col<5>::idct(pDst_ptr, pTemp);
                    break;
                case 6:
                    Col<6>::idct(pDst_ptr, pTemp);
                    break;
                case 7:
                    Col<7>::idct(pDst_ptr, pTemp);
                    break;
                case 8:
                    Col<8>::idct(pDst_ptr, pTemp);
                    break;
                default:
                    break;
            }

            pTemp++;
            pDst_ptr++;
        }
    }

    void idct_4x4(const signed short *pSrc_ptr, unsigned char *pDst_ptr) {
        int temp[64];
        int *pTemp = temp;
        const signed short *pSrc = pSrc_ptr;

        for (int i = 4; i > 0; i--) {
            Row<4>::idct(pTemp, pSrc);
            pSrc += 8;
            pTemp += 8;
        }

        pTemp = temp;
        for (int i = 8; i > 0; i--) {
            Col<4>::idct(pDst_ptr, pTemp);
            pTemp++;
            pDst_ptr++;
        }
    }

// Retrieve one character from the input stream.
    inline unsigned int jpeg_decoder::get_char() {
        // Any bytes remaining in buffer?
        if (!m_in_buf_left) {
            // Try to get more bytes.
            prep_in_buffer();
            // Still nothing to get?
            if (!m_in_buf_left) {
                // Pad the end of the stream with 0xFF 0xD9 (EOI marker)
                int t = m_tem_flag;
                m_tem_flag ^= 1;
                if (t)
                    return 0xD9;
                else
                    return 0xFF;
            }
        }

        unsigned int c = *m_pIn_buf_ofs++;
        m_in_buf_left--;

        return c;
    }

// Same as previous method, except can indicate if the character is a pad character or not.
    inline unsigned int jpeg_decoder::get_char(bool *pPadding_flag) {
        if (!m_in_buf_left) {
            prep_in_buffer();
            if (!m_in_buf_left) {
                *pPadding_flag = true;
                int t = m_tem_flag;
                m_tem_flag ^= 1;
                if (t)
                    return 0xD9;
                else
                    return 0xFF;
            }
        }

        *pPadding_flag = false;

        unsigned int c = *m_pIn_buf_ofs++;
        m_in_buf_left--;

        return c;
    }

// Inserts a previously retrieved character back into the input buffer.
    inline void jpeg_decoder::stuff_char(unsigned char q) {
        *(--m_pIn_buf_ofs) = q;
        m_in_buf_left++;
    }

// Retrieves one character from the input stream, but does not read past markers. Will continue to return 0xFF when a marker is encountered.
    inline unsigned char jpeg_decoder::get_octet() {
        bool padding_flag;
        int c = get_char(&padding_flag);

        if (c == 0xFF) {
            if (padding_flag)
                return 0xFF;

            c = get_char(&padding_flag);
            if (padding_flag) {
                stuff_char(0xFF);
                return 0xFF;
            }

            if (c == 0x00)
                return 0xFF;
            else {
                stuff_char(static_cast<unsigned char>(c));
                stuff_char(0xFF);
                return 0xFF;
            }
        }

        return static_cast<unsigned char>(c);
    }

// Retrieves a variable number of bits from the input stream. Does not recognize markers.
    inline unsigned int jpeg_decoder::get_bits(int num_bits) {
        if (!num_bits)
            return 0;

        unsigned int i = m_bit_buf >> (32 - num_bits);

        if ((m_bits_left -= num_bits) <= 0) {
            m_bit_buf <<= (num_bits += m_bits_left);

            unsigned int c1 = get_char();
            unsigned int c2 = get_char();
            m_bit_buf = (m_bit_buf & 0xFFFF0000) | (c1 << 8) | c2;

            m_bit_buf <<= -m_bits_left;

            m_bits_left += 16;

            assert(m_bits_left >= 0);
        } else
            m_bit_buf <<= num_bits;

        return i;
    }

// Retrieves a variable number of bits from the input stream. Markers will not be read into the input bit buffer. Instead, an infinite number of all 1's will be returned when a marker is encountered.
    inline unsigned int jpeg_decoder::get_bits_no_markers(int num_bits) {
        if (!num_bits)
            return 0;

        unsigned int i = m_bit_buf >> (32 - num_bits);

        if ((m_bits_left -= num_bits) <= 0) {
            m_bit_buf <<= (num_bits += m_bits_left);

            if ((m_in_buf_left < 2) || (m_pIn_buf_ofs[0] == 0xFF) || (m_pIn_buf_ofs[1] == 0xFF)) {
                unsigned int c1 = get_octet();
                unsigned int c2 = get_octet();
                m_bit_buf |= (c1 << 8) | c2;
            } else {
                m_bit_buf |= ((unsigned int) m_pIn_buf_ofs[0] << 8) | m_pIn_buf_ofs[1];
                m_in_buf_left -= 2;
                m_pIn_buf_ofs += 2;
            }

            m_bit_buf <<= -m_bits_left;

            m_bits_left += 16;

            assert(m_bits_left >= 0);
        } else
            m_bit_buf <<= num_bits;

        return i;
    }

// Decodes a Huffman encoded symbol.
    inline int jpeg_decoder::huff_decode(huff_tables *pH) {
        int symbol;

        // Check first 8-bits: do we have a complete symbol?
        if ((symbol = pH->look_up[m_bit_buf >> 24]) < 0) {
            // Decode more bits, use a tree traversal to find symbol.
            int ofs = 23;
            do {
                symbol = pH->tree[-(int) (symbol + ((m_bit_buf >> ofs) & 1))];
                ofs--;
            } while (symbol < 0);

            get_bits_no_markers(8 + (23 - ofs));
        } else
            get_bits_no_markers(pH->code_size[symbol]);

        return symbol;
    }

// Decodes a Huffman encoded symbol.
    inline int jpeg_decoder::huff_decode(huff_tables *pH, int &extra_bits) {
        int symbol;

        // Check first 8-bits: do we have a complete symbol?
        if ((symbol = pH->look_up2[m_bit_buf >> 24]) < 0) {
            // Use a tree traversal to find symbol.
            int ofs = 23;
            do {
                symbol = pH->tree[-(int) (symbol + ((m_bit_buf >> ofs) & 1))];
                ofs--;
            } while (symbol < 0);

            get_bits_no_markers(8 + (23 - ofs));

            extra_bits = get_bits_no_markers(symbol & 0xF);
        } else {
            assert(((symbol >> 8) & 31) == pH->code_size[symbol & 255] + (
                    (symbol & 0x8000) ? (symbol & 15) : 0));

            if (symbol & 0x8000) {
                get_bits_no_markers((symbol >> 8) & 31);
                extra_bits = symbol >> 16;
            } else {
                int code_size = (symbol >> 8) & 31;
                int num_extra_bits = symbol & 0xF;
                int bits = code_size + num_extra_bits;
                if (bits <= (m_bits_left + 16))
                    extra_bits = get_bits_no_markers(bits) & ((1 << num_extra_bits) - 1);
                else {
                    get_bits_no_markers(code_size);
                    extra_bits = get_bits_no_markers(num_extra_bits);
                }
            }

            symbol &= 0xFF;
        }

        return symbol;
    }

// Tables and macro used to fully decode the DPCM differences.
    static const int s_extend_test[16] = {0, 0x0001, 0x0002, 0x0004, 0x0008, 0x0010, 0x0020, 0x0040,
                                          0x0080, 0x0100, 0x0200, 0x0400, 0x0800, 0x1000, 0x2000,
                                          0x4000};
    static const int s_extend_offset[16] = {0, (int) ((-1u) << 1) + 1, (int) ((-1u) << 2) + 1,
                                            (int) ((-1u) << 3) + 1, (int) ((-1u) << 4) + 1,
                                            (int) ((-1u) << 5) + 1, (int) ((-1u) << 6) + 1,
                                            (int) ((-1u) << 7) + 1, (int) ((-1u) << 8) + 1,
                                            (int) ((-1u) << 9) + 1, (int) ((-1u) << 10) + 1,
                                            (int) ((-1u) << 11) + 1, (int) ((-1u) << 12) + 1,
                                            (int) ((-1u) << 13) + 1, (int) ((-1u) << 14) + 1,
                                            (int) ((-1u) << 15) + 1};
#define JPGD_HUFF_EXTEND(x, s) (((x) < s_extend_test[s & 15]) ? ((x) + s_extend_offset[s & 15]) : (x))

    inline unsigned char jpeg_decoder::clamp(int i) {
        if (static_cast<unsigned int>(i) > 255)
            i = (((~i) >> 31) & 0xFF);

        return static_cast<unsigned char>(i);
    }

    namespace DCT_Upsample {
        struct Matrix44 {
            typedef int Element_Type;
            enum {
                NUM_ROWS = 4, NUM_COLS = 4
            };

            Element_Type v[NUM_ROWS][NUM_COLS];

            inline int rows() const {
                return NUM_ROWS;
            }

            inline int cols() const {
                return NUM_COLS;
            }

            inline const Element_Type &at(int r, int c) const {
                return v[r][c];
            }

            inline Element_Type &at(int r, int c) {
                return v[r][c];
            }

            inline Matrix44() {
            }

            inline Matrix44 &operator+=(const Matrix44 &a) {
                for (int r = 0; r < NUM_ROWS; r++) {
                    at(r, 0) += a.at(r, 0);
                    at(r, 1) += a.at(r, 1);
                    at(r, 2) += a.at(r, 2);
                    at(r, 3) += a.at(r, 3);
                }
                return *this;
            }

            inline Matrix44 &operator-=(const Matrix44 &a) {
                for (int r = 0; r < NUM_ROWS; r++) {
                    at(r, 0) -= a.at(r, 0);
                    at(r, 1) -= a.at(r, 1);
                    at(r, 2) -= a.at(r, 2);
                    at(r, 3) -= a.at(r, 3);
                }
                return *this;
            }

            friend inline Matrix44 operator+(const Matrix44 &a, const Matrix44 &b) {
                Matrix44 ret;
                for (int r = 0; r < NUM_ROWS; r++) {
                    ret.at(r, 0) = a.at(r, 0) + b.at(r, 0);
                    ret.at(r, 1) = a.at(r, 1) + b.at(r, 1);
                    ret.at(r, 2) = a.at(r, 2) + b.at(r, 2);
                    ret.at(r, 3) = a.at(r, 3) + b.at(r, 3);
                }
                return ret;
            }

            friend inline Matrix44 operator-(const Matrix44 &a, const Matrix44 &b) {
                Matrix44 ret;
                for (int r = 0; r < NUM_ROWS; r++) {
                    ret.at(r, 0) = a.at(r, 0) - b.at(r, 0);
                    ret.at(r, 1) = a.at(r, 1) - b.at(r, 1);
                    ret.at(r, 2) = a.at(r, 2) - b.at(r, 2);
                    ret.at(r, 3) = a.at(r, 3) - b.at(r, 3);
                }
                return ret;
            }

            static inline void
            add_and_store(signed short *pDst, const Matrix44 &a, const Matrix44 &b) {
                for (int r = 0; r < 4; r++) {
                    pDst[0 * 8 + r] = static_cast<signed short>(a.at(r, 0) + b.at(r, 0));
                    pDst[1 * 8 + r] = static_cast<signed short>(a.at(r, 1) + b.at(r, 1));
                    pDst[2 * 8 + r] = static_cast<signed short>(a.at(r, 2) + b.at(r, 2));
                    pDst[3 * 8 + r] = static_cast<signed short>(a.at(r, 3) + b.at(r, 3));
                }
            }

            static inline void
            sub_and_store(signed short *pDst, const Matrix44 &a, const Matrix44 &b) {
                for (int r = 0; r < 4; r++) {
                    pDst[0 * 8 + r] = static_cast<signed short>(a.at(r, 0) - b.at(r, 0));
                    pDst[1 * 8 + r] = static_cast<signed short>(a.at(r, 1) - b.at(r, 1));
                    pDst[2 * 8 + r] = static_cast<signed short>(a.at(r, 2) - b.at(r, 2));
                    pDst[3 * 8 + r] = static_cast<signed short>(a.at(r, 3) - b.at(r, 3));
                }
            }
        };

        const int FRACT_BITS = 10;
        const int SCALE = 1 << FRACT_BITS;

        typedef int Temp_Type;
#define D(i) (((i) + (SCALE >> 1)) >> FRACT_BITS)
#define F(i) ((int)((i) * SCALE + .5f))

// Any decent C++ compiler will optimize this at compile time to a 0, or an array access.
#define AT(c, r) ((((c)>=NUM_COLS)||((r)>=NUM_ROWS)) ? 0 : pSrc[(c)+(r)*8])

// NUM_ROWS/NUM_COLS = # of non-zero rows/cols in input matrix
        template<int NUM_ROWS, int NUM_COLS>
        struct P_Q {
            static void calc(Matrix44 &P, Matrix44 &Q, const signed short *pSrc) {
                // 4x8 = 4x8 times 8x8, matrix 0 is constant
                const Temp_Type X000 = AT(0, 0);
                const Temp_Type X001 = AT(0, 1);
                const Temp_Type X002 = AT(0, 2);
                const Temp_Type X003 = AT(0, 3);
                const Temp_Type X004 = AT(0, 4);
                const Temp_Type X005 = AT(0, 5);
                const Temp_Type X006 = AT(0, 6);
                const Temp_Type X007 = AT(0, 7);
                const Temp_Type X010 = D(F(0.415735f) * AT(1, 0) + F(0.791065f) * AT(3, 0) +
                                         F(-0.352443f) * AT(5, 0) + F(0.277785f) * AT(7, 0));
                const Temp_Type X011 = D(F(0.415735f) * AT(1, 1) + F(0.791065f) * AT(3, 1) +
                                         F(-0.352443f) * AT(5, 1) + F(0.277785f) * AT(7, 1));
                const Temp_Type X012 = D(F(0.415735f) * AT(1, 2) + F(0.791065f) * AT(3, 2) +
                                         F(-0.352443f) * AT(5, 2) + F(0.277785f) * AT(7, 2));
                const Temp_Type X013 = D(F(0.415735f) * AT(1, 3) + F(0.791065f) * AT(3, 3) +
                                         F(-0.352443f) * AT(5, 3) + F(0.277785f) * AT(7, 3));
                const Temp_Type X014 = D(F(0.415735f) * AT(1, 4) + F(0.791065f) * AT(3, 4) +
                                         F(-0.352443f) * AT(5, 4) + F(0.277785f) * AT(7, 4));
                const Temp_Type X015 = D(F(0.415735f) * AT(1, 5) + F(0.791065f) * AT(3, 5) +
                                         F(-0.352443f) * AT(5, 5) + F(0.277785f) * AT(7, 5));
                const Temp_Type X016 = D(F(0.415735f) * AT(1, 6) + F(0.791065f) * AT(3, 6) +
                                         F(-0.352443f) * AT(5, 6) + F(0.277785f) * AT(7, 6));
                const Temp_Type X017 = D(F(0.415735f) * AT(1, 7) + F(0.791065f) * AT(3, 7) +
                                         F(-0.352443f) * AT(5, 7) + F(0.277785f) * AT(7, 7));
                const Temp_Type X020 = AT(4, 0);
                const Temp_Type X021 = AT(4, 1);
                const Temp_Type X022 = AT(4, 2);
                const Temp_Type X023 = AT(4, 3);
                const Temp_Type X024 = AT(4, 4);
                const Temp_Type X025 = AT(4, 5);
                const Temp_Type X026 = AT(4, 6);
                const Temp_Type X027 = AT(4, 7);
                const Temp_Type X030 = D(F(0.022887f) * AT(1, 0) + F(-0.097545f) * AT(3, 0) +
                                         F(0.490393f) * AT(5, 0) + F(0.865723f) * AT(7, 0));
                const Temp_Type X031 = D(F(0.022887f) * AT(1, 1) + F(-0.097545f) * AT(3, 1) +
                                         F(0.490393f) * AT(5, 1) + F(0.865723f) * AT(7, 1));
                const Temp_Type X032 = D(F(0.022887f) * AT(1, 2) + F(-0.097545f) * AT(3, 2) +
                                         F(0.490393f) * AT(5, 2) + F(0.865723f) * AT(7, 2));
                const Temp_Type X033 = D(F(0.022887f) * AT(1, 3) + F(-0.097545f) * AT(3, 3) +
                                         F(0.490393f) * AT(5, 3) + F(0.865723f) * AT(7, 3));
                const Temp_Type X034 = D(F(0.022887f) * AT(1, 4) + F(-0.097545f) * AT(3, 4) +
                                         F(0.490393f) * AT(5, 4) + F(0.865723f) * AT(7, 4));
                const Temp_Type X035 = D(F(0.022887f) * AT(1, 5) + F(-0.097545f) * AT(3, 5) +
                                         F(0.490393f) * AT(5, 5) + F(0.865723f) * AT(7, 5));
                const Temp_Type X036 = D(F(0.022887f) * AT(1, 6) + F(-0.097545f) * AT(3, 6) +
                                         F(0.490393f) * AT(5, 6) + F(0.865723f) * AT(7, 6));
                const Temp_Type X037 = D(F(0.022887f) * AT(1, 7) + F(-0.097545f) * AT(3, 7) +
                                         F(0.490393f) * AT(5, 7) + F(0.865723f) * AT(7, 7));

                // 4x4 = 4x8 times 8x4, matrix 1 is constant
                P.at(0, 0) = X000;
                P.at(0, 1) = D(X001 * F(0.415735f) + X003 * F(0.791065f) + X005 * F(-0.352443f) +
                               X007 * F(0.277785f));
                P.at(0, 2) = X004;
                P.at(0, 3) = D(X001 * F(0.022887f) + X003 * F(-0.097545f) + X005 * F(0.490393f) +
                               X007 * F(0.865723f));
                P.at(1, 0) = X010;
                P.at(1, 1) = D(X011 * F(0.415735f) + X013 * F(0.791065f) + X015 * F(-0.352443f) +
                               X017 * F(0.277785f));
                P.at(1, 2) = X014;
                P.at(1, 3) = D(X011 * F(0.022887f) + X013 * F(-0.097545f) + X015 * F(0.490393f) +
                               X017 * F(0.865723f));
                P.at(2, 0) = X020;
                P.at(2, 1) = D(X021 * F(0.415735f) + X023 * F(0.791065f) + X025 * F(-0.352443f) +
                               X027 * F(0.277785f));
                P.at(2, 2) = X024;
                P.at(2, 3) = D(X021 * F(0.022887f) + X023 * F(-0.097545f) + X025 * F(0.490393f) +
                               X027 * F(0.865723f));
                P.at(3, 0) = X030;
                P.at(3, 1) = D(X031 * F(0.415735f) + X033 * F(0.791065f) + X035 * F(-0.352443f) +
                               X037 * F(0.277785f));
                P.at(3, 2) = X034;
                P.at(3, 3) = D(X031 * F(0.022887f) + X033 * F(-0.097545f) + X035 * F(0.490393f) +
                               X037 * F(0.865723f));
                // 40 muls 24 adds

                // 4x4 = 4x8 times 8x4, matrix 1 is constant
                Q.at(0, 0) = D(X001 * F(0.906127f) + X003 * F(-0.318190f) + X005 * F(0.212608f) +
                               X007 * F(-0.180240f));
                Q.at(0, 1) = X002;
                Q.at(0, 2) = D(X001 * F(-0.074658f) + X003 * F(0.513280f) + X005 * F(0.768178f) +
                               X007 * F(-0.375330f));
                Q.at(0, 3) = X006;
                Q.at(1, 0) = D(X011 * F(0.906127f) + X013 * F(-0.318190f) + X015 * F(0.212608f) +
                               X017 * F(-0.180240f));
                Q.at(1, 1) = X012;
                Q.at(1, 2) = D(X011 * F(-0.074658f) + X013 * F(0.513280f) + X015 * F(0.768178f) +
                               X017 * F(-0.375330f));
                Q.at(1, 3) = X016;
                Q.at(2, 0) = D(X021 * F(0.906127f) + X023 * F(-0.318190f) + X025 * F(0.212608f) +
                               X027 * F(-0.180240f));
                Q.at(2, 1) = X022;
                Q.at(2, 2) = D(X021 * F(-0.074658f) + X023 * F(0.513280f) + X025 * F(0.768178f) +
                               X027 * F(-0.375330f));
                Q.at(2, 3) = X026;
                Q.at(3, 0) = D(X031 * F(0.906127f) + X033 * F(-0.318190f) + X035 * F(0.212608f) +
                               X037 * F(-0.180240f));
                Q.at(3, 1) = X032;
                Q.at(3, 2) = D(X031 * F(-0.074658f) + X033 * F(0.513280f) + X035 * F(0.768178f) +
                               X037 * F(-0.375330f));
                Q.at(3, 3) = X036;
                // 40 muls 24 adds
            }
        };

        template<int NUM_ROWS, int NUM_COLS>
        struct R_S {
            static void calc(Matrix44 &R, Matrix44 &S, const signed short *pSrc) {
                // 4x8 = 4x8 times 8x8, matrix 0 is constant
                const Temp_Type X100 = D(F(0.906127f) * AT(1, 0) + F(-0.318190f) * AT(3, 0) +
                                         F(0.212608f) * AT(5, 0) + F(-0.180240f) * AT(7, 0));
                const Temp_Type X101 = D(F(0.906127f) * AT(1, 1) + F(-0.318190f) * AT(3, 1) +
                                         F(0.212608f) * AT(5, 1) + F(-0.180240f) * AT(7, 1));
                const Temp_Type X102 = D(F(0.906127f) * AT(1, 2) + F(-0.318190f) * AT(3, 2) +
                                         F(0.212608f) * AT(5, 2) + F(-0.180240f) * AT(7, 2));
                const Temp_Type X103 = D(F(0.906127f) * AT(1, 3) + F(-0.318190f) * AT(3, 3) +
                                         F(0.212608f) * AT(5, 3) + F(-0.180240f) * AT(7, 3));
                const Temp_Type X104 = D(F(0.906127f) * AT(1, 4) + F(-0.318190f) * AT(3, 4) +
                                         F(0.212608f) * AT(5, 4) + F(-0.180240f) * AT(7, 4));
                const Temp_Type X105 = D(F(0.906127f) * AT(1, 5) + F(-0.318190f) * AT(3, 5) +
                                         F(0.212608f) * AT(5, 5) + F(-0.180240f) * AT(7, 5));
                const Temp_Type X106 = D(F(0.906127f) * AT(1, 6) + F(-0.318190f) * AT(3, 6) +
                                         F(0.212608f) * AT(5, 6) + F(-0.180240f) * AT(7, 6));
                const Temp_Type X107 = D(F(0.906127f) * AT(1, 7) + F(-0.318190f) * AT(3, 7) +
                                         F(0.212608f) * AT(5, 7) + F(-0.180240f) * AT(7, 7));
                const Temp_Type X110 = AT(2, 0);
                const Temp_Type X111 = AT(2, 1);
                const Temp_Type X112 = AT(2, 2);
                const Temp_Type X113 = AT(2, 3);
                const Temp_Type X114 = AT(2, 4);
                const Temp_Type X115 = AT(2, 5);
                const Temp_Type X116 = AT(2, 6);
                const Temp_Type X117 = AT(2, 7);
                const Temp_Type X120 = D(F(-0.074658f) * AT(1, 0) + F(0.513280f) * AT(3, 0) +
                                         F(0.768178f) * AT(5, 0) + F(-0.375330f) * AT(7, 0));
                const Temp_Type X121 = D(F(-0.074658f) * AT(1, 1) + F(0.513280f) * AT(3, 1) +
                                         F(0.768178f) * AT(5, 1) + F(-0.375330f) * AT(7, 1));
                const Temp_Type X122 = D(F(-0.074658f) * AT(1, 2) + F(0.513280f) * AT(3, 2) +
                                         F(0.768178f) * AT(5, 2) + F(-0.375330f) * AT(7, 2));
                const Temp_Type X123 = D(F(-0.074658f) * AT(1, 3) + F(0.513280f) * AT(3, 3) +
                                         F(0.768178f) * AT(5, 3) + F(-0.375330f) * AT(7, 3));
                const Temp_Type X124 = D(F(-0.074658f) * AT(1, 4) + F(0.513280f) * AT(3, 4) +
                                         F(0.768178f) * AT(5, 4) + F(-0.375330f) * AT(7, 4));
                const Temp_Type X125 = D(F(-0.074658f) * AT(1, 5) + F(0.513280f) * AT(3, 5) +
                                         F(0.768178f) * AT(5, 5) + F(-0.375330f) * AT(7, 5));
                const Temp_Type X126 = D(F(-0.074658f) * AT(1, 6) + F(0.513280f) * AT(3, 6) +
                                         F(0.768178f) * AT(5, 6) + F(-0.375330f) * AT(7, 6));
                const Temp_Type X127 = D(F(-0.074658f) * AT(1, 7) + F(0.513280f) * AT(3, 7) +
                                         F(0.768178f) * AT(5, 7) + F(-0.375330f) * AT(7, 7));
                const Temp_Type X130 = AT(6, 0);
                const Temp_Type X131 = AT(6, 1);
                const Temp_Type X132 = AT(6, 2);
                const Temp_Type X133 = AT(6, 3);
                const Temp_Type X134 = AT(6, 4);
                const Temp_Type X135 = AT(6, 5);
                const Temp_Type X136 = AT(6, 6);
                const Temp_Type X137 = AT(6, 7);
                // 80 muls 48 adds

                // 4x4 = 4x8 times 8x4, matrix 1 is constant
                R.at(0, 0) = X100;
                R.at(0, 1) = D(X101 * F(0.415735f) + X103 * F(0.791065f) + X105 * F(-0.352443f) +
                               X107 * F(0.277785f));
                R.at(0, 2) = X104;
                R.at(0, 3) = D(X101 * F(0.022887f) + X103 * F(-0.097545f) + X105 * F(0.490393f) +
                               X107 * F(0.865723f));
                R.at(1, 0) = X110;
                R.at(1, 1) = D(X111 * F(0.415735f) + X113 * F(0.791065f) + X115 * F(-0.352443f) +
                               X117 * F(0.277785f));
                R.at(1, 2) = X114;
                R.at(1, 3) = D(X111 * F(0.022887f) + X113 * F(-0.097545f) + X115 * F(0.490393f) +
                               X117 * F(0.865723f));
                R.at(2, 0) = X120;
                R.at(2, 1) = D(X121 * F(0.415735f) + X123 * F(0.791065f) + X125 * F(-0.352443f) +
                               X127 * F(0.277785f));
                R.at(2, 2) = X124;
                R.at(2, 3) = D(X121 * F(0.022887f) + X123 * F(-0.097545f) + X125 * F(0.490393f) +
                               X127 * F(0.865723f));
                R.at(3, 0) = X130;
                R.at(3, 1) = D(X131 * F(0.415735f) + X133 * F(0.791065f) + X135 * F(-0.352443f) +
                               X137 * F(0.277785f));
                R.at(3, 2) = X134;
                R.at(3, 3) = D(X131 * F(0.022887f) + X133 * F(-0.097545f) + X135 * F(0.490393f) +
                               X137 * F(0.865723f));
                // 40 muls 24 adds
                // 4x4 = 4x8 times 8x4, matrix 1 is constant
                S.at(0, 0) = D(X101 * F(0.906127f) + X103 * F(-0.318190f) + X105 * F(0.212608f) +
                               X107 * F(-0.180240f));
                S.at(0, 1) = X102;
                S.at(0, 2) = D(X101 * F(-0.074658f) + X103 * F(0.513280f) + X105 * F(0.768178f) +
                               X107 * F(-0.375330f));
                S.at(0, 3) = X106;
                S.at(1, 0) = D(X111 * F(0.906127f) + X113 * F(-0.318190f) + X115 * F(0.212608f) +
                               X117 * F(-0.180240f));
                S.at(1, 1) = X112;
                S.at(1, 2) = D(X111 * F(-0.074658f) + X113 * F(0.513280f) + X115 * F(0.768178f) +
                               X117 * F(-0.375330f));
                S.at(1, 3) = X116;
                S.at(2, 0) = D(X121 * F(0.906127f) + X123 * F(-0.318190f) + X125 * F(0.212608f) +
                               X127 * F(-0.180240f));
                S.at(2, 1) = X122;
                S.at(2, 2) = D(X121 * F(-0.074658f) + X123 * F(0.513280f) + X125 * F(0.768178f) +
                               X127 * F(-0.375330f));
                S.at(2, 3) = X126;
                S.at(3, 0) = D(X131 * F(0.906127f) + X133 * F(-0.318190f) + X135 * F(0.212608f) +
                               X137 * F(-0.180240f));
                S.at(3, 1) = X132;
                S.at(3, 2) = D(X131 * F(-0.074658f) + X133 * F(0.513280f) + X135 * F(0.768178f) +
                               X137 * F(-0.375330f));
                S.at(3, 3) = X136;
                // 40 muls 24 adds
            }
        };
    } // end namespace DCT_Upsample

// Unconditionally frees all allocated m_blocks.
    void jpeg_decoder::free_all_blocks() {
        m_pStream = NULL;
        for (mem_block *b = m_pMem_blocks; b;) {
            mem_block *n = b->m_pNext;
            free(b);
            b = n;
        }
        m_pMem_blocks = NULL;
    }

    void jpeg_decoder::stop_decoding(jpgd_status status) {
        m_error_code = status;
        free_all_blocks();
        longjmp(m_jmp_state, status);
    }

    void *jpeg_decoder::alloc(size_t nSize, bool zero) {
        nSize = (MAX(nSize, 1) + 3) & ~3;
        char *rv = NULL;
        for (mem_block *b = m_pMem_blocks; b; b = b->m_pNext) {
            if ((b->m_used_count + nSize) <= b->m_size) {
                rv = b->m_data + b->m_used_count;
                b->m_used_count += nSize;
                break;
            }
        }
        if (!rv) {
            int capacity = MAX(32768 - 256, (nSize + 2047) & ~2047);
            mem_block *b = (mem_block *) malloc(sizeof(mem_block) + capacity);
            if (!b) {
                stop_decoding(JPGD_NOTENOUGHMEM);
            }
            b->m_pNext = m_pMem_blocks;
            m_pMem_blocks = b;
            b->m_used_count = nSize;
            b->m_size = capacity;
            rv = b->m_data;
        }
        if (zero)
            memset(rv, 0, nSize);
        return rv;
    }

    void jpeg_decoder::word_clear(void *p, unsigned short c, unsigned int n) {
        unsigned char *pD = (unsigned char *) p;
        const unsigned char l = c & 0xFF, h = (c >> 8) & 0xFF;
        while (n) {
            pD[0] = l;
            pD[1] = h;
            pD += 2;
            n--;
        }
    }

// Refill the input buffer.
// This method will sit in a loop until (A) the buffer is full or (B)
// the stream's read() method reports and end of file condition.
    void jpeg_decoder::prep_in_buffer() {
        m_in_buf_left = 0;
        m_pIn_buf_ofs = m_in_buf;

        if (m_eof_flag)
            return;

        do {
            int bytes_read = m_pStream->read(m_in_buf + m_in_buf_left,
                                             JPGD_IN_BUF_SIZE - m_in_buf_left, &m_eof_flag);
            if (bytes_read == -1)
                stop_decoding(JPGD_STREAM_READ);

            m_in_buf_left += bytes_read;
        } while ((m_in_buf_left < JPGD_IN_BUF_SIZE) && (!m_eof_flag));

        m_total_bytes_read += m_in_buf_left;

        // Pad the end of the block with M_EOI (prevents the decompressor from going off the rails if the stream is invalid).
        // (This dates way back to when this decompressor was written in C/asm, and the all-asm Huffman decoder did some fancy things to increase perf.)
        word_clear(m_pIn_buf_ofs + m_in_buf_left, 0xD9FF, 64);
    }

// Read a Huffman code table.
    void jpeg_decoder::read_dht_marker() {
        int i, index, count;
        unsigned char huff_num[17];
        unsigned char huff_val[256];

        unsigned int num_left = get_bits(16);

        if (num_left < 2)
            stop_decoding(JPGD_BAD_DHT_MARKER);

        num_left -= 2;

        while (num_left) {
            index = get_bits(8);

            huff_num[0] = 0;

            count = 0;

            for (i = 1; i <= 16; i++) {
                huff_num[i] = static_cast<unsigned char>(get_bits(8));
                count += huff_num[i];
            }

            if (count > 255)
                stop_decoding(JPGD_BAD_DHT_COUNTS);

            for (i = 0; i < count; i++)
                huff_val[i] = static_cast<unsigned char>(get_bits(8));

            i = 1 + 16 + count;

            if (num_left < (unsigned int) i)
                stop_decoding(JPGD_BAD_DHT_MARKER);

            num_left -= i;

            if ((index & 0x10) > 0x10)
                stop_decoding(JPGD_BAD_DHT_INDEX);

            index = (index & 0x0F) + ((index & 0x10) >> 4) * (MAX_HUFF_TABLES >> 1);

            if (index >= MAX_HUFF_TABLES)
                stop_decoding(JPGD_BAD_DHT_INDEX);

            if (!m_huff_num[index])
                m_huff_num[index] = (unsigned char *) alloc(17);

            if (!m_huff_val[index])
                m_huff_val[index] = (unsigned char *) alloc(256);

            m_huff_ac[index] = (index & 0x10) != 0;
            memcpy(m_huff_num[index], huff_num, 17);
            memcpy(m_huff_val[index], huff_val, 256);
        }
    }

// Read a quantization table.
    void jpeg_decoder::read_dqt_marker() {
        int n, i, prec;
        unsigned int num_left;
        unsigned int temp;

        num_left = get_bits(16);

        if (num_left < 2)
            stop_decoding(JPGD_BAD_DQT_MARKER);

        num_left -= 2;

        while (num_left) {
            n = get_bits(8);
            prec = n >> 4;
            n &= 0x0F;

            if (n >= MAX_QUANT_TABLES)
                stop_decoding(JPGD_BAD_DQT_TABLE);

            if (!m_quant[n])
                m_quant[n] = (signed short *) alloc(64 * sizeof(signed short));

            // read quantization entries, in zag order
            for (i = 0; i < 64; i++) {
                temp = get_bits(8);

                if (prec)
                    temp = (temp << 8) + get_bits(8);

                m_quant[n][i] = static_cast<signed short>(temp);
            }

            i = 64 + 1;

            if (prec)
                i += 64;

            if (num_left < (unsigned int) i)
                stop_decoding(JPGD_BAD_DQT_LENGTH);

            num_left -= i;
        }
    }

// Read the start of frame (SOF) marker.
    void jpeg_decoder::read_sof_marker() {
        int i;
        unsigned int num_left;

        num_left = get_bits(16);

        if (get_bits(8) != 8) /* precision: sorry, only 8-bit precision is supported right now */
            stop_decoding(JPGD_BAD_PRECISION);

        m_image_y_size = get_bits(16);

        if ((m_image_y_size < 1) || (m_image_y_size > MAX_HEIGHT))
            stop_decoding(JPGD_BAD_HEIGHT);

        m_image_x_size = get_bits(16);

        if ((m_image_x_size < 1) || (m_image_x_size > MAX_WIDTH))
            stop_decoding(JPGD_BAD_WIDTH);

        m_comps_in_frame = get_bits(8);

        if (m_comps_in_frame > MAX_COMPONENTS)
            stop_decoding(JPGD_TOO_MANY_COMPONENTS);

        if (num_left != (unsigned int) (m_comps_in_frame * 3 + 8))
            stop_decoding(JPGD_BAD_SOF_LENGTH);

        for (i = 0; i < m_comps_in_frame; i++) {
            m_comp_ident[i] = get_bits(8);
            m_comp_h_samp[i] = get_bits(4);
            m_comp_v_samp[i] = get_bits(4);
            m_comp_quant[i] = get_bits(8);
        }
    }

// Used to skip unrecognized markers.
    void jpeg_decoder::skip_variable_marker() {
        unsigned int num_left;

        num_left = get_bits(16);

        if (num_left < 2)
            stop_decoding(JPGD_BAD_VARIABLE_MARKER);

        num_left -= 2;

        while (num_left) {
            get_bits(8);
            num_left--;
        }
    }

// Read a define restart interval (DRI) marker.
    void jpeg_decoder::read_dri_marker() {
        if (get_bits(16) != 4)
            stop_decoding(JPGD_BAD_DRI_LENGTH);

        m_restart_interval = get_bits(16);
    }

// Read a start of scan (SOS) marker.
    void jpeg_decoder::read_sos_marker() {
        unsigned int num_left;
        int i, ci, n, c, cc;

        num_left = get_bits(16);

        n = get_bits(8);

        m_comps_in_scan = n;

        num_left -= 3;

        if ((num_left != (unsigned int) (n * 2 + 3)) || (n < 1) || (n > MAX_COMPS_IN_SCAN))
            stop_decoding(JPGD_BAD_SOS_LENGTH);

        for (i = 0; i < n; i++) {
            cc = get_bits(8);
            c = get_bits(8);
            num_left -= 2;

            for (ci = 0; ci < m_comps_in_frame; ci++)
                if (cc == m_comp_ident[ci])
                    break;

            if (ci >= m_comps_in_frame)
                stop_decoding(JPGD_BAD_SOS_COMP_ID);

            m_comp_list[i] = ci;
            m_comp_dc_tab[ci] = (c >> 4) & 15;
            m_comp_ac_tab[ci] = (c & 15) + (MAX_HUFF_TABLES >> 1);
        }

        m_spectral_start = get_bits(8);
        m_spectral_end = get_bits(8);
        m_successive_high = get_bits(4);
        m_successive_low = get_bits(4);

        if (!m_progressive_flag) {
            m_spectral_start = 0;
            m_spectral_end = 63;
        }

        num_left -= 3;

        while (num_left) /* read past whatever is num_left */
        {
            get_bits(8);
            num_left--;
        }
    }

// Finds the next marker.
    int jpeg_decoder::next_marker() {
        unsigned int c, bytes;

        bytes = 0;

        do {
            do {
                bytes++;
                c = get_bits(8);
            } while (c != 0xFF);

            do {
                c = get_bits(8);
            } while (c == 0xFF);

        } while (c == 0);

        // If bytes > 0 here, there where extra bytes before the marker (not good).

        return c;
    }

// Process markers. Returns when an SOFx, SOI, EOI, or SOS marker is
// encountered.
    int jpeg_decoder::process_markers()   // @suppress("No return")
    {
        int c;

        for (;;) {
            c = next_marker();

            switch (c) {
                case M_SOF0:
                case M_SOF1:
                case M_SOF2:
                case M_SOF3:
                case M_SOF5:
                case M_SOF6:
                case M_SOF7:
                    //      case M_JPG:
                case M_SOF9:
                case M_SOF10:
                case M_SOF11:
                case M_SOF13:
                case M_SOF14:
                case M_SOF15:
                case M_SOI:
                case M_EOI:
                case M_SOS: {
                    return c;
                }
                case M_DHT: {
                    read_dht_marker();
                    break;
                }
                    // No arithmitic support - dumb patents!
                case M_DAC: {
                    stop_decoding(JPGD_NO_ARITHMITIC_SUPPORT);
                    break;
                }
                case M_DQT: {
                    read_dqt_marker();
                    break;
                }
                case M_DRI: {
                    read_dri_marker();
                    break;
                }
                    //case M_APP0:  /* no need to read the JFIF marker */

                case M_JPG:
                case M_RST0: /* no parameters */
                case M_RST1:
                case M_RST2:
                case M_RST3:
                case M_RST4:
                case M_RST5:
                case M_RST6:
                case M_RST7:
                case M_TEM: {
                    stop_decoding(JPGD_UNEXPECTED_MARKER);
                    break;
                }
                default: /* must be DNL, DHP, EXP, APPn, JPGn, COM, or RESn or APP0 */
                {
                    skip_variable_marker();
                    break;
                }
            }
        }
    }

// Finds the start of image (SOI) marker.
// This code is rather defensive: it only checks the first 512 bytes to avoid
// false positives.
    void jpeg_decoder::locate_soi_marker() {
        unsigned int lastchar, thischar;
        unsigned int bytesleft;

        lastchar = get_bits(8);

        thischar = get_bits(8);

        /* ok if it's a normal JPEG file without a special header */

        if ((lastchar == 0xFF) && (thischar == M_SOI))
            return;

        bytesleft = 4096; //512;

        for (;;) {
            if (--bytesleft == 0)
                stop_decoding(JPGD_NOT_JPEG);

            lastchar = thischar;

            thischar = get_bits(8);

            if (lastchar == 0xFF) {
                if (thischar == M_SOI)
                    break;
                else if (thischar ==
                         M_EOI) // get_bits will keep returning M_EOI if we read past the end
                    stop_decoding(JPGD_NOT_JPEG);
            }
        }

        // Check the next character after marker: if it's not 0xFF, it can't be the start of the next marker, so the file is bad.
        thischar = (m_bit_buf >> 24) & 0xFF;

        if (thischar != 0xFF)
            stop_decoding(JPGD_NOT_JPEG);
    }

// Find a start of frame (SOF) marker.
    void jpeg_decoder::locate_sof_marker() {
        locate_soi_marker();

        int c = process_markers();

        switch (c) {
            case M_SOF2:
                m_progressive_flag = JPGD_TRUE;
                /* no break */
            case M_SOF0: /* baseline DCT */
            case M_SOF1: /* extended sequential DCT */
            {
                read_sof_marker();
                break;
            }
            case M_SOF9: /* Arithmitic coding */
            {
                stop_decoding(JPGD_NO_ARITHMITIC_SUPPORT);
                break;
            }
            default: {
                stop_decoding(JPGD_UNSUPPORTED_MARKER);
                break;
            }
        }
    }

// Find a start of scan (SOS) marker.
    int jpeg_decoder::locate_sos_marker() {
        int c;

        c = process_markers();

        if (c == M_EOI)
            return JPGD_FALSE;
        else if (c != M_SOS)
            stop_decoding(JPGD_UNEXPECTED_MARKER);

        read_sos_marker();

        return JPGD_TRUE;
    }

// Reset everything to default/uninitialized state.
    void jpeg_decoder::init(jpeg_decoder_stream *pStream) {
        m_pMem_blocks = NULL;
        m_error_code = JPGD_SUCCESS;
        m_ready_flag = false;
        m_image_x_size = m_image_y_size = 0;
        m_pStream = pStream;
        m_progressive_flag = JPGD_FALSE;

        memset(m_huff_ac, 0, sizeof(m_huff_ac));
        memset(m_huff_num, 0, sizeof(m_huff_num));
        memset(m_huff_val, 0, sizeof(m_huff_val));
        memset(m_quant, 0, sizeof(m_quant));

        m_scan_type = 0;
        m_comps_in_frame = 0;

        memset(m_comp_h_samp, 0, sizeof(m_comp_h_samp));
        memset(m_comp_v_samp, 0, sizeof(m_comp_v_samp));
        memset(m_comp_quant, 0, sizeof(m_comp_quant));
        memset(m_comp_ident, 0, sizeof(m_comp_ident));
        memset(m_comp_h_blocks, 0, sizeof(m_comp_h_blocks));
        memset(m_comp_v_blocks, 0, sizeof(m_comp_v_blocks));

        m_comps_in_scan = 0;
        memset(m_comp_list, 0, sizeof(m_comp_list));
        memset(m_comp_dc_tab, 0, sizeof(m_comp_dc_tab));
        memset(m_comp_ac_tab, 0, sizeof(m_comp_ac_tab));

        m_spectral_start = 0;
        m_spectral_end = 0;
        m_successive_low = 0;
        m_successive_high = 0;
        m_max_mcu_x_size = 0;
        m_max_mcu_y_size = 0;
        m_blocks_per_mcu = 0;
        m_max_blocks_per_row = 0;
        m_mcus_per_row = 0;
        m_mcus_per_col = 0;
        m_expanded_blocks_per_component = 0;
        m_expanded_blocks_per_mcu = 0;
        m_expanded_blocks_per_row = 0;
        m_freq_domain_chroma_upsample = false;

        memset(m_mcu_org, 0, sizeof(m_mcu_org));

        m_total_lines_left = 0;
        m_mcu_lines_left = 0;
        m_real_dest_bytes_per_scan_line = 0;
        m_dest_bytes_per_scan_line = 0;
        m_dest_bytes_per_pixel = 0;

        memset(m_pHuff_tabs, 0, sizeof(m_pHuff_tabs));

        memset(m_dc_coeffs, 0, sizeof(m_dc_coeffs));
        memset(m_ac_coeffs, 0, sizeof(m_ac_coeffs));
        memset(m_block_y_mcu, 0, sizeof(m_block_y_mcu));

        m_eob_run = 0;

        memset(m_block_y_mcu, 0, sizeof(m_block_y_mcu));

        m_pIn_buf_ofs = m_in_buf;
        m_in_buf_left = 0;
        m_eof_flag = false;
        m_tem_flag = 0;

        memset(m_in_buf_pad_start, 0, sizeof(m_in_buf_pad_start));
        memset(m_in_buf, 0, sizeof(m_in_buf));
        memset(m_in_buf_pad_end, 0, sizeof(m_in_buf_pad_end));

        m_restart_interval = 0;
        m_restarts_left = 0;
        m_next_restart_num = 0;

        m_max_mcus_per_row = 0;
        m_max_blocks_per_mcu = 0;
        m_max_mcus_per_col = 0;

        memset(m_last_dc_val, 0, sizeof(m_last_dc_val));
        m_pMCU_coefficients = NULL;
        m_pSample_buf = NULL;

        m_total_bytes_read = 0;

        m_pScan_line_0 = NULL;
        m_pScan_line_1 = NULL;

        // Ready the input buffer.
        prep_in_buffer();

        // Prime the bit buffer.
        m_bits_left = 16;
        m_bit_buf = 0;

        get_bits(16);
        get_bits(16);

        for (int i = 0; i < MAX_BLOCKS_PER_MCU; i++)
            m_mcu_block_max_zag[i] = 64;
    }

#define SCALEBITS 16
#define ONE_HALF  ((int) 1 << (SCALEBITS-1))
#define FIX(x)    ((int) ((x) * (1L<<SCALEBITS) + 0.5f))

// Create a few tables that allow us to quickly convert YCbCr to RGB.
    void jpeg_decoder::create_look_ups() {
        for (int i = 0; i <= 255; i++) {
            int k = i - 128;
            m_crr[i] = (FIX(1.40200f) * k + ONE_HALF) >> SCALEBITS;
            m_cbb[i] = (FIX(1.77200f) * k + ONE_HALF) >> SCALEBITS;
            m_crg[i] = (-FIX(0.71414f)) * k;
            m_cbg[i] = (-FIX(0.34414f)) * k + ONE_HALF;
        }
    }

// This method throws back into the stream any bytes that where read
// into the bit buffer during initial marker scanning.
    void jpeg_decoder::fix_in_buffer() {
        // In case any 0xFF's where pulled into the buffer during marker scanning.
        assert((m_bits_left & 7) == 0);

        if (m_bits_left == 16)
            stuff_char((unsigned char) (m_bit_buf & 0xFF));

        if (m_bits_left >= 8)
            stuff_char((unsigned char) ((m_bit_buf >> 8) & 0xFF));

        stuff_char((unsigned char) ((m_bit_buf >> 16) & 0xFF));
        stuff_char((unsigned char) ((m_bit_buf >> 24) & 0xFF));

        m_bits_left = 16;
        get_bits_no_markers(16);
        get_bits_no_markers(16);
    }

    void jpeg_decoder::transform_mcu(int mcu_row) {
        signed short *pSrc_ptr = m_pMCU_coefficients;
        unsigned char *pDst_ptr = m_pSample_buf + mcu_row * m_blocks_per_mcu * 64;

        for (int mcu_block = 0; mcu_block < m_blocks_per_mcu; mcu_block++) {
            idct(pSrc_ptr, pDst_ptr, m_mcu_block_max_zag[mcu_block]);
            pSrc_ptr += 64;
            pDst_ptr += 64;
        }
    }

    static const unsigned char s_max_rc[64] = {17, 18, 34, 50, 50, 51, 52, 52, 52, 68, 84, 84, 84,
                                               84, 85, 86, 86, 86, 86, 86, 102, 118, 118, 118, 118,
                                               118, 118, 119, 120, 120, 120, 120, 120, 120, 120,
                                               136, 136, 136, 136, 136, 136, 136, 136, 136, 136,
                                               136, 136, 136, 136, 136, 136, 136, 136, 136, 136,
                                               136, 136, 136, 136, 136, 136, 136, 136, 136};

    void jpeg_decoder::transform_mcu_expand(int mcu_row) {
        signed short *pSrc_ptr = m_pMCU_coefficients;
        unsigned char *pDst_ptr = m_pSample_buf + mcu_row * m_expanded_blocks_per_mcu * 64;

        // Y IDCT
        int mcu_block;
        for (mcu_block = 0; mcu_block < m_expanded_blocks_per_component;
             mcu_block++) {
            idct(pSrc_ptr, pDst_ptr, m_mcu_block_max_zag[mcu_block]);
            pSrc_ptr += 64;
            pDst_ptr += 64;
        }

        // Chroma IDCT, with upsampling
        signed short temp_block[64];

        for (int i = 0; i < 2; i++) {
            DCT_Upsample::Matrix44 P, Q, R, S;

            assert(m_mcu_block_max_zag[mcu_block] >= 1);
            assert(m_mcu_block_max_zag[mcu_block] <= 64);

            int max_zag = m_mcu_block_max_zag[mcu_block++] - 1;
            if (max_zag <= 0)
                max_zag = 0; // should never happen, only here to shut up static analysis
            switch (s_max_rc[max_zag]) {
                case 1 * 16 + 1:
                    DCT_Upsample::P_Q<1, 1>::calc(P, Q, pSrc_ptr);
                    DCT_Upsample::R_S<1, 1>::calc(R, S, pSrc_ptr);
                    break;
                case 1 * 16 + 2:
                    DCT_Upsample::P_Q<1, 2>::calc(P, Q, pSrc_ptr);
                    DCT_Upsample::R_S<1, 2>::calc(R, S, pSrc_ptr);
                    break;
                case 2 * 16 + 2:
                    DCT_Upsample::P_Q<2, 2>::calc(P, Q, pSrc_ptr);
                    DCT_Upsample::R_S<2, 2>::calc(R, S, pSrc_ptr);
                    break;
                case 3 * 16 + 2:
                    DCT_Upsample::P_Q<3, 2>::calc(P, Q, pSrc_ptr);
                    DCT_Upsample::R_S<3, 2>::calc(R, S, pSrc_ptr);
                    break;
                case 3 * 16 + 3:
                    DCT_Upsample::P_Q<3, 3>::calc(P, Q, pSrc_ptr);
                    DCT_Upsample::R_S<3, 3>::calc(R, S, pSrc_ptr);
                    break;
                case 3 * 16 + 4:
                    DCT_Upsample::P_Q<3, 4>::calc(P, Q, pSrc_ptr);
                    DCT_Upsample::R_S<3, 4>::calc(R, S, pSrc_ptr);
                    break;
                case 4 * 16 + 4:
                    DCT_Upsample::P_Q<4, 4>::calc(P, Q, pSrc_ptr);
                    DCT_Upsample::R_S<4, 4>::calc(R, S, pSrc_ptr);
                    break;
                case 5 * 16 + 4:
                    DCT_Upsample::P_Q<5, 4>::calc(P, Q, pSrc_ptr);
                    DCT_Upsample::R_S<5, 4>::calc(R, S, pSrc_ptr);
                    break;
                case 5 * 16 + 5:
                    DCT_Upsample::P_Q<5, 5>::calc(P, Q, pSrc_ptr);
                    DCT_Upsample::R_S<5, 5>::calc(R, S, pSrc_ptr);
                    break;
                case 5 * 16 + 6:
                    DCT_Upsample::P_Q<5, 6>::calc(P, Q, pSrc_ptr);
                    DCT_Upsample::R_S<5, 6>::calc(R, S, pSrc_ptr);
                    break;
                case 6 * 16 + 6:
                    DCT_Upsample::P_Q<6, 6>::calc(P, Q, pSrc_ptr);
                    DCT_Upsample::R_S<6, 6>::calc(R, S, pSrc_ptr);
                    break;
                case 7 * 16 + 6:
                    DCT_Upsample::P_Q<7, 6>::calc(P, Q, pSrc_ptr);
                    DCT_Upsample::R_S<7, 6>::calc(R, S, pSrc_ptr);
                    break;
                case 7 * 16 + 7:
                    DCT_Upsample::P_Q<7, 7>::calc(P, Q, pSrc_ptr);
                    DCT_Upsample::R_S<7, 7>::calc(R, S, pSrc_ptr);
                    break;
                case 7 * 16 + 8:
                    DCT_Upsample::P_Q<7, 8>::calc(P, Q, pSrc_ptr);
                    DCT_Upsample::R_S<7, 8>::calc(R, S, pSrc_ptr);
                    break;
                case 8 * 16 + 8:
                    DCT_Upsample::P_Q<8, 8>::calc(P, Q, pSrc_ptr);
                    DCT_Upsample::R_S<8, 8>::calc(R, S, pSrc_ptr);
                    break;
                default:
                    assert(false);
            }

            DCT_Upsample::Matrix44 a(P + Q);
            P -= Q;
            DCT_Upsample::Matrix44 &b = P;
            DCT_Upsample::Matrix44 c(R + S);
            R -= S;
            DCT_Upsample::Matrix44 &d = R;

            DCT_Upsample::Matrix44::add_and_store(temp_block, a, c);
            idct_4x4(temp_block, pDst_ptr);
            pDst_ptr += 64;

            DCT_Upsample::Matrix44::sub_and_store(temp_block, a, c);
            idct_4x4(temp_block, pDst_ptr);
            pDst_ptr += 64;

            DCT_Upsample::Matrix44::add_and_store(temp_block, b, d);
            idct_4x4(temp_block, pDst_ptr);
            pDst_ptr += 64;

            DCT_Upsample::Matrix44::sub_and_store(temp_block, b, d);
            idct_4x4(temp_block, pDst_ptr);
            pDst_ptr += 64;

            pSrc_ptr += 64;
        }
    }

// Loads and dequantizes the next row of (already decoded) coefficients.
// Progressive images only.
    void jpeg_decoder::load_next_row() {
        int i;
        signed short *p;
        signed short *q;
        int mcu_row, mcu_block, row_block = 0;
        int component_num, component_id;
        int block_x_mcu[MAX_COMPONENTS];

        memset(block_x_mcu, 0, MAX_COMPONENTS * sizeof(int));

        for (mcu_row = 0; mcu_row < m_mcus_per_row; mcu_row++) {
            int block_x_mcu_ofs = 0, block_y_mcu_ofs = 0;

            for (mcu_block = 0; mcu_block < m_blocks_per_mcu; mcu_block++) {
                component_id = m_mcu_org[mcu_block];
                q = m_quant[m_comp_quant[component_id]];

                p = m_pMCU_coefficients + 64 * mcu_block;

                signed short *pAC = coeff_buf_getp(m_ac_coeffs[component_id],
                                                   block_x_mcu[component_id] + block_x_mcu_ofs,
                                                   m_block_y_mcu[component_id] + block_y_mcu_ofs);
                signed short *pDC = coeff_buf_getp(m_dc_coeffs[component_id],
                                                   block_x_mcu[component_id] + block_x_mcu_ofs,
                                                   m_block_y_mcu[component_id] + block_y_mcu_ofs);
                p[0] = pDC[0];
                memcpy(&p[1], &pAC[1], 63 * sizeof(signed short));

                for (i = 63; i > 0; i--)
                    if (p[g_ZAG[i]])
                        break;

                m_mcu_block_max_zag[mcu_block] = i + 1;

                for (; i >= 0; i--)
                    if (p[g_ZAG[i]])
                        p[g_ZAG[i]] = static_cast<signed short>(p[g_ZAG[i]] * q[i]);

                row_block++;

                if (m_comps_in_scan == 1)
                    block_x_mcu[component_id]++;
                else {
                    if (++block_x_mcu_ofs == m_comp_h_samp[component_id]) {
                        block_x_mcu_ofs = 0;

                        if (++block_y_mcu_ofs == m_comp_v_samp[component_id]) {
                            block_y_mcu_ofs = 0;

                            block_x_mcu[component_id] += m_comp_h_samp[component_id];
                        }
                    }
                }
            }

            if (m_freq_domain_chroma_upsample)
                transform_mcu_expand(mcu_row);
            else
                transform_mcu(mcu_row);
        }

        if (m_comps_in_scan == 1)
            m_block_y_mcu[m_comp_list[0]]++;
        else {
            for (component_num = 0; component_num < m_comps_in_scan;
                 component_num++) {
                component_id = m_comp_list[component_num];

                m_block_y_mcu[component_id] += m_comp_v_samp[component_id];
            }
        }
    }

// Restart interval processing.
    void jpeg_decoder::process_restart() {
        int i;
        int c = 0;

        // Align to a byte boundry
        // FIXME: Is this really necessary? get_bits_no_markers() never reads in markers!
        //get_bits_no_markers(m_bits_left & 7);

        // Let's scan a little bit to find the marker, but not _too_ far.
        // 1536 is a "fudge factor" that determines how much to scan.
        for (i = 1536; i > 0; i--)
            if (get_char() == 0xFF)
                break;

        if (i == 0)
            stop_decoding(JPGD_BAD_RESTART_MARKER);

        for (; i > 0; i--)
            if ((c = get_char()) != 0xFF)
                break;

        if (i == 0)
            stop_decoding(JPGD_BAD_RESTART_MARKER);

        // Is it the expected marker? If not, something bad happened.
        if (c != (m_next_restart_num + M_RST0))
            stop_decoding(JPGD_BAD_RESTART_MARKER);

        // Reset each component's DC prediction values.
        memset(&m_last_dc_val, 0, m_comps_in_frame * sizeof(unsigned int));

        m_eob_run = 0;

        m_restarts_left = m_restart_interval;

        m_next_restart_num = (m_next_restart_num + 1) & 7;

        // Get the bit buffer going again...

        m_bits_left = 16;
        get_bits_no_markers(16);
        get_bits_no_markers(16);
    }

    static inline int dequantize_ac(int c, int q) {
        c *= q;
        return c;
    }

// Decodes and dequantizes the next row of coefficients.
    void jpeg_decoder::decode_next_row() {
        int row_block = 0;

        for (int mcu_row = 0; mcu_row < m_mcus_per_row; mcu_row++) {
            if ((m_restart_interval) && (m_restarts_left == 0))
                process_restart();

            signed short *p = m_pMCU_coefficients;
            for (int mcu_block = 0; mcu_block < m_blocks_per_mcu;
                 mcu_block++, p += 64) {
                int component_id = m_mcu_org[mcu_block];
                signed short *q = m_quant[m_comp_quant[component_id]];

                int r, s;
                s = huff_decode(m_pHuff_tabs[m_comp_dc_tab[component_id]], r);
                s = JPGD_HUFF_EXTEND(r, s);

                m_last_dc_val[component_id] = (s += m_last_dc_val[component_id]);

                p[0] = static_cast<signed short>(s * q[0]);

                int prev_num_set = m_mcu_block_max_zag[mcu_block];

                huff_tables *pH = m_pHuff_tabs[m_comp_ac_tab[component_id]];

                int k;
                for (k = 1; k < 64; k++) {
                    int extra_bits;
                    s = huff_decode(pH, extra_bits);

                    r = s >> 4;
                    s &= 15;

                    if (s) {
                        if (r) {
                            if ((k + r) > 63)
                                stop_decoding(JPGD_DECODE_ERROR);

                            if (k < prev_num_set) {
                                int n = MIN(r, prev_num_set - k);
                                int kt = k;
                                while (n--)
                                    p[g_ZAG[kt++]] = 0;
                            }

                            k += r;
                        }

                        s = JPGD_HUFF_EXTEND(extra_bits, s);

                        assert(k < 64);

                        p[g_ZAG[k]] = static_cast<signed short>(dequantize_ac(s, q[k])); //s * q[k];
                    } else {
                        if (r == 15) {
                            if ((k + 16) > 64)
                                stop_decoding(JPGD_DECODE_ERROR);

                            if (k < prev_num_set) {
                                int n = MIN(16, prev_num_set - k);
                                int kt = k;
                                while (n--) {
                                    assert(kt <= 63);
                                    p[g_ZAG[kt++]] = 0;
                                }
                            }

                            k += 16 - 1; // - 1 because the loop counter is k
                            assert(p[g_ZAG[k]] == 0);
                        } else
                            break;
                    }
                }

                if (k < prev_num_set) {
                    int kt = k;
                    while (kt < prev_num_set)
                        p[g_ZAG[kt++]] = 0;
                }

                m_mcu_block_max_zag[mcu_block] = k;

                row_block++;
            }

            if (m_freq_domain_chroma_upsample)
                transform_mcu_expand(mcu_row);
            else
                transform_mcu(mcu_row);

            m_restarts_left--;
        }
    }

// YCbCr H1V1 (1x1:1:1, 3 m_blocks per MCU) to RGB
    void jpeg_decoder::H1V1Convert() {
        int row = m_max_mcu_y_size - m_mcu_lines_left;
        unsigned char *d = m_pScan_line_0;
        unsigned char *s = m_pSample_buf + row * 8;

        for (int i = m_max_mcus_per_row; i > 0; i--) {
            for (int j = 0; j < 8; j++) {
                int y = s[j];
                int cb = s[64 + j];
                int cr = s[128 + j];

                d[0] = clamp(y + m_crr[cr]);
                d[1] = clamp(y + ((m_crg[cr] + m_cbg[cb]) >> 16));
                d[2] = clamp(y + m_cbb[cb]);
                d[3] = 255;

                d += 4;
            }

            s += 64 * 3;
        }
    }

// YCbCr H2V1 (2x1:1:1, 4 m_blocks per MCU) to RGB
    void jpeg_decoder::H2V1Convert() {
        int row = m_max_mcu_y_size - m_mcu_lines_left;
        unsigned char *d0 = m_pScan_line_0;
        unsigned char *y = m_pSample_buf + row * 8;
        unsigned char *c = m_pSample_buf + 2 * 64 + row * 8;

        for (int i = m_max_mcus_per_row; i > 0; i--) {
            for (int l = 0; l < 2; l++) {
                for (int j = 0; j < 4; j++) {
                    int cb = c[0];
                    int cr = c[64];

                    int rc = m_crr[cr];
                    int gc = ((m_crg[cr] + m_cbg[cb]) >> 16);
                    int bc = m_cbb[cb];

                    int yy = y[j << 1];
                    d0[0] = clamp(yy + rc);
                    d0[1] = clamp(yy + gc);
                    d0[2] = clamp(yy + bc);
                    d0[3] = 255;

                    yy = y[(j << 1) + 1];
                    d0[4] = clamp(yy + rc);
                    d0[5] = clamp(yy + gc);
                    d0[6] = clamp(yy + bc);
                    d0[7] = 255;

                    d0 += 8;

                    c++;
                }
                y += 64;
            }

            y += 64 * 4 - 64 * 2;
            c += 64 * 4 - 8;
        }
    }

// YCbCr H2V1 (1x2:1:1, 4 m_blocks per MCU) to RGB
    void jpeg_decoder::H1V2Convert() {
        int row = m_max_mcu_y_size - m_mcu_lines_left;
        unsigned char *d0 = m_pScan_line_0;
        unsigned char *d1 = m_pScan_line_1;
        unsigned char *y;
        unsigned char *c;

        if (row < 8)
            y = m_pSample_buf + row * 8;
        else
            y = m_pSample_buf + 64 * 1 + (row & 7) * 8;

        c = m_pSample_buf + 64 * 2 + (row >> 1) * 8;

        for (int i = m_max_mcus_per_row; i > 0; i--) {
            for (int j = 0; j < 8; j++) {
                int cb = c[0 + j];
                int cr = c[64 + j];

                int rc = m_crr[cr];
                int gc = ((m_crg[cr] + m_cbg[cb]) >> 16);
                int bc = m_cbb[cb];

                int yy = y[j];
                d0[0] = clamp(yy + rc);
                d0[1] = clamp(yy + gc);
                d0[2] = clamp(yy + bc);
                d0[3] = 255;

                yy = y[8 + j];
                d1[0] = clamp(yy + rc);
                d1[1] = clamp(yy + gc);
                d1[2] = clamp(yy + bc);
                d1[3] = 255;

                d0 += 4;
                d1 += 4;
            }

            y += 64 * 4;
            c += 64 * 4;
        }
    }

// YCbCr H2V2 (2x2:1:1, 6 m_blocks per MCU) to RGB
    void jpeg_decoder::H2V2Convert() {
        int row = m_max_mcu_y_size - m_mcu_lines_left;
        unsigned char *d0 = m_pScan_line_0;
        unsigned char *d1 = m_pScan_line_1;
        unsigned char *y;
        unsigned char *c;

        if (row < 8)
            y = m_pSample_buf + row * 8;
        else
            y = m_pSample_buf + 64 * 2 + (row & 7) * 8;

        c = m_pSample_buf + 64 * 4 + (row >> 1) * 8;

        for (int i = m_max_mcus_per_row; i > 0; i--) {
            for (int l = 0; l < 2; l++) {
                for (int j = 0; j < 8; j += 2) {
                    int cb = c[0];
                    int cr = c[64];

                    int rc = m_crr[cr];
                    int gc = ((m_crg[cr] + m_cbg[cb]) >> 16);
                    int bc = m_cbb[cb];

                    int yy = y[j];
                    d0[0] = clamp(yy + rc);
                    d0[1] = clamp(yy + gc);
                    d0[2] = clamp(yy + bc);
                    d0[3] = 255;

                    yy = y[j + 1];
                    d0[4] = clamp(yy + rc);
                    d0[5] = clamp(yy + gc);
                    d0[6] = clamp(yy + bc);
                    d0[7] = 255;

                    yy = y[j + 8];
                    d1[0] = clamp(yy + rc);
                    d1[1] = clamp(yy + gc);
                    d1[2] = clamp(yy + bc);
                    d1[3] = 255;

                    yy = y[j + 8 + 1];
                    d1[4] = clamp(yy + rc);
                    d1[5] = clamp(yy + gc);
                    d1[6] = clamp(yy + bc);
                    d1[7] = 255;

                    d0 += 8;
                    d1 += 8;

                    c++;
                }
                y += 64;
            }

            y += 64 * 6 - 64 * 2;
            c += 64 * 6 - 8;
        }
    }

// Y (1 block per MCU) to 8-bit grayscale
    void jpeg_decoder::gray_convert() {
        int row = m_max_mcu_y_size - m_mcu_lines_left;
        unsigned char *d = m_pScan_line_0;
        unsigned char *s = m_pSample_buf + row * 8;

        for (int i = m_max_mcus_per_row; i > 0; i--) {
            *(unsigned int *) d = *(unsigned int *) s;
            *(unsigned int *) (&d[4]) = *(unsigned int *) (&s[4]);

            s += 64;
            d += 8;
        }
    }

    void jpeg_decoder::expanded_convert() {
        int row = m_max_mcu_y_size - m_mcu_lines_left;

        unsigned char *Py = m_pSample_buf + (row / 8) * 64 * m_comp_h_samp[0] + (row & 7) * 8;

        unsigned char *d = m_pScan_line_0;

        for (int i = m_max_mcus_per_row; i > 0; i--) {
            for (int k = 0; k < m_max_mcu_x_size; k += 8) {
                const int Y_ofs = k * 8;
                const int Cb_ofs = Y_ofs + 64 * m_expanded_blocks_per_component;
                const int Cr_ofs = Y_ofs + 64 * m_expanded_blocks_per_component * 2;
                for (int j = 0; j < 8; j++) {
                    int y = Py[Y_ofs + j];
                    int cb = Py[Cb_ofs + j];
                    int cr = Py[Cr_ofs + j];

                    d[0] = clamp(y + m_crr[cr]);
                    d[1] = clamp(y + ((m_crg[cr] + m_cbg[cb]) >> 16));
                    d[2] = clamp(y + m_cbb[cb]);
                    d[3] = 255;

                    d += 4;
                }
            }

            Py += 64 * m_expanded_blocks_per_mcu;
        }
    }

// Find end of image (EOI) marker, so we can return to the user the exact size of the input stream.
    void jpeg_decoder::find_eoi() {
        if (!m_progressive_flag) {
            // Attempt to read the EOI marker.
            //get_bits_no_markers(m_bits_left & 7);

            // Prime the bit buffer
            m_bits_left = 16;
            get_bits(16);
            get_bits(16);

            // The next marker _should_ be EOI
            process_markers();
        }

        m_total_bytes_read -= m_in_buf_left;
    }

    int jpeg_decoder::decode(const void **pScan_line, unsigned int *pScan_line_len) {
        if ((m_error_code) || (!m_ready_flag))
            return JPGD_FAILED;

        if (m_total_lines_left == 0)
            return JPGD_DONE;

        if (m_mcu_lines_left == 0) {
            if (setjmp(m_jmp_state))
                return JPGD_FAILED;

            if (m_progressive_flag)
                load_next_row();
            else
                decode_next_row();

            // Find the EOI marker if that was the last row.
            if (m_total_lines_left <= m_max_mcu_y_size)
                find_eoi();

            m_mcu_lines_left = m_max_mcu_y_size;
        }

        if (m_freq_domain_chroma_upsample) {
            expanded_convert();
            *pScan_line = m_pScan_line_0;
        } else {
            switch (m_scan_type) {
                case JPGD_YH2V2: {
                    if ((m_mcu_lines_left & 1) == 0) {
                        H2V2Convert();
                        *pScan_line = m_pScan_line_0;
                    } else
                        *pScan_line = m_pScan_line_1;

                    break;
                }
                case JPGD_YH2V1: {
                    H2V1Convert();
                    *pScan_line = m_pScan_line_0;
                    break;
                }
                case JPGD_YH1V2: {
                    if ((m_mcu_lines_left & 1) == 0) {
                        H1V2Convert();
                        *pScan_line = m_pScan_line_0;
                    } else
                        *pScan_line = m_pScan_line_1;

                    break;
                }
                case JPGD_YH1V1: {
                    H1V1Convert();
                    *pScan_line = m_pScan_line_0;
                    break;
                }
                case JPGD_GRAYSCALE: {
                    gray_convert();
                    *pScan_line = m_pScan_line_0;

                    break;
                }
                default:
                    break;
            }
        }

        *pScan_line_len = m_real_dest_bytes_per_scan_line;

        m_mcu_lines_left--;
        m_total_lines_left--;

        return JPGD_SUCCESS;
    }

// Creates the tables needed for efficient Huffman decoding.
    void jpeg_decoder::make_huff_table(int index, huff_tables *pH) {
        int p, i, l, si;
        unsigned char huffsize[257];
        unsigned int huffcode[257];
        unsigned int code;
        unsigned int subtree;
        int code_size;
        int lastp;
        int nextfreeentry;
        int currententry;

        pH->ac_table = m_huff_ac[index] != 0;

        p = 0;

        for (l = 1; l <= 16; l++) {
            for (i = 1; i <= m_huff_num[index][l]; i++)
                huffsize[p++] = static_cast<unsigned char>(l);
        }

        huffsize[p] = 0;

        lastp = p;

        code = 0;
        si = huffsize[0];
        p = 0;

        while (huffsize[p]) {
            while (huffsize[p] == si) {
                huffcode[p++] = code;
                code++;
            }

            code <<= 1;
            si++;
        }

        memset(pH->look_up, 0, sizeof(pH->look_up));
        memset(pH->look_up2, 0, sizeof(pH->look_up2));
        memset(pH->tree, 0, sizeof(pH->tree));
        memset(pH->code_size, 0, sizeof(pH->code_size));

        nextfreeentry = -1;

        p = 0;

        while (p < lastp) {
            i = m_huff_val[index][p];
            code = huffcode[p];
            code_size = huffsize[p];

            pH->code_size[i] = static_cast<unsigned char>(code_size);

            if (code_size <= 8) {
                code <<= (8 - code_size);

                for (l = 1 << (8 - code_size); l > 0; l--) {
                    assert(i < 256);

                    pH->look_up[code] = i;

                    bool has_extrabits = false;
                    int extra_bits = 0;
                    int num_extra_bits = i & 15;

                    int bits_to_fetch = code_size;
                    if (num_extra_bits) {
                        int total_codesize = code_size + num_extra_bits;
                        if (total_codesize <= 8) {
                            has_extrabits = true;
                            extra_bits =
                                    ((1 << num_extra_bits) - 1) & (code >> (8 - total_codesize));
                            assert(extra_bits <= 0x7FFF);
                            bits_to_fetch += num_extra_bits;
                        }
                    }

                    if (!has_extrabits)
                        pH->look_up2[code] = i | (bits_to_fetch << 8);
                    else
                        pH->look_up2[code] = i | 0x8000 | (extra_bits << 16) | (bits_to_fetch << 8);

                    code++;
                }
            } else {
                subtree = (code >> (code_size - 8)) & 0xFF;

                currententry = pH->look_up[subtree];

                if (currententry == 0) {
                    pH->look_up[subtree] = currententry = nextfreeentry;
                    pH->look_up2[subtree] = currententry = nextfreeentry;

                    nextfreeentry -= 2;
                }

                code <<= (16 - (code_size - 8));

                for (l = code_size; l > 9; l--) {
                    if ((code & 0x8000) == 0)
                        currententry--;

                    if (pH->tree[-currententry - 1] == 0) {
                        pH->tree[-currententry - 1] = nextfreeentry;

                        currententry = nextfreeentry;

                        nextfreeentry -= 2;
                    } else
                        currententry = pH->tree[-currententry - 1];

                    code <<= 1;
                }

                if ((code & 0x8000) == 0)
                    currententry--;

                pH->tree[-currententry - 1] = i;
            }

            p++;
        }
    }

// Verifies the quantization tables needed for this scan are available.
    void jpeg_decoder::check_quant_tables() {
        for (int i = 0; i < m_comps_in_scan; i++)
            if (m_quant[m_comp_quant[m_comp_list[i]]] == NULL)
                stop_decoding(JPGD_UNDEFINED_QUANT_TABLE);
    }

// Verifies that all the Huffman tables needed for this scan are available.
    void jpeg_decoder::check_huff_tables() {
        for (int i = 0; i < m_comps_in_scan; i++) {
            if ((m_spectral_start == 0) && (m_huff_num[m_comp_dc_tab[m_comp_list[i]]] == NULL))
                stop_decoding(JPGD_UNDEFINED_HUFF_TABLE);

            if ((m_spectral_end > 0) && (m_huff_num[m_comp_ac_tab[m_comp_list[i]]] == NULL))
                stop_decoding(JPGD_UNDEFINED_HUFF_TABLE);
        }

        for (int i = 0; i < MAX_HUFF_TABLES; i++)
            if (m_huff_num[i]) {
                if (!m_pHuff_tabs[i])
                    m_pHuff_tabs[i] = (huff_tables *) alloc(sizeof(huff_tables));

                make_huff_table(i, m_pHuff_tabs[i]);
            }
    }

// Determines the component order inside each MCU.
// Also calcs how many MCU's are on each row, etc.
    void jpeg_decoder::calc_mcu_block_order() {
        int component_num, component_id;
        int max_h_samp = 0, max_v_samp = 0;

        for (component_id = 0; component_id < m_comps_in_frame; component_id++) {
            if (m_comp_h_samp[component_id] > max_h_samp)
                max_h_samp = m_comp_h_samp[component_id];

            if (m_comp_v_samp[component_id] > max_v_samp)
                max_v_samp = m_comp_v_samp[component_id];
        }

        for (component_id = 0; component_id < m_comps_in_frame; component_id++) {
            m_comp_h_blocks[component_id] =
                    ((((m_image_x_size * m_comp_h_samp[component_id]) + (max_h_samp - 1)) /
                      max_h_samp) + 7) / 8;
            m_comp_v_blocks[component_id] =
                    ((((m_image_y_size * m_comp_v_samp[component_id]) + (max_v_samp - 1)) /
                      max_v_samp) + 7) / 8;
        }

        if (m_comps_in_scan == 1) {
            m_mcus_per_row = m_comp_h_blocks[m_comp_list[0]];
            m_mcus_per_col = m_comp_v_blocks[m_comp_list[0]];
        } else {
            m_mcus_per_row = (((m_image_x_size + 7) / 8) + (max_h_samp - 1)) / max_h_samp;
            m_mcus_per_col = (((m_image_y_size + 7) / 8) + (max_v_samp - 1)) / max_v_samp;
        }

        if (m_comps_in_scan == 1) {
            m_mcu_org[0] = m_comp_list[0];

            m_blocks_per_mcu = 1;
        } else {
            m_blocks_per_mcu = 0;

            for (component_num = 0; component_num < m_comps_in_scan;
                 component_num++) {
                int num_blocks;

                component_id = m_comp_list[component_num];

                num_blocks = m_comp_h_samp[component_id] * m_comp_v_samp[component_id];

                while (num_blocks--)
                    m_mcu_org[m_blocks_per_mcu++] = component_id;
            }
        }
    }

// Starts a new scan.
    int jpeg_decoder::init_scan() {
        if (!locate_sos_marker())
            return JPGD_FALSE;

        calc_mcu_block_order();

        check_huff_tables();

        check_quant_tables();

        memset(m_last_dc_val, 0, m_comps_in_frame * sizeof(unsigned int));

        m_eob_run = 0;

        if (m_restart_interval) {
            m_restarts_left = m_restart_interval;
            m_next_restart_num = 0;
        }

        fix_in_buffer();

        return JPGD_TRUE;
    }

// Starts a frame. Determines if the number of components or sampling factors
// are supported.
    void jpeg_decoder::init_frame() {
        int i;

        if (m_comps_in_frame == 1) {
            if ((m_comp_h_samp[0] != 1) || (m_comp_v_samp[0] != 1))
                stop_decoding(JPGD_UNSUPPORTED_SAMP_FACTORS);

            m_scan_type = JPGD_GRAYSCALE;
            m_max_blocks_per_mcu = 1;
            m_max_mcu_x_size = 8;
            m_max_mcu_y_size = 8;
        } else if (m_comps_in_frame == 3) {
            if (((m_comp_h_samp[1] != 1) || (m_comp_v_samp[1] != 1)) ||
                ((m_comp_h_samp[2] != 1) || (m_comp_v_samp[2] != 1)))
                stop_decoding(JPGD_UNSUPPORTED_SAMP_FACTORS);

            if ((m_comp_h_samp[0] == 1) && (m_comp_v_samp[0] == 1)) {
                m_scan_type = JPGD_YH1V1;

                m_max_blocks_per_mcu = 3;
                m_max_mcu_x_size = 8;
                m_max_mcu_y_size = 8;
            } else if ((m_comp_h_samp[0] == 2) && (m_comp_v_samp[0] == 1)) {
                m_scan_type = JPGD_YH2V1;
                m_max_blocks_per_mcu = 4;
                m_max_mcu_x_size = 16;
                m_max_mcu_y_size = 8;
            } else if ((m_comp_h_samp[0] == 1) && (m_comp_v_samp[0] == 2)) {
                m_scan_type = JPGD_YH1V2;
                m_max_blocks_per_mcu = 4;
                m_max_mcu_x_size = 8;
                m_max_mcu_y_size = 16;
            } else if ((m_comp_h_samp[0] == 2) && (m_comp_v_samp[0] == 2)) {
                m_scan_type = JPGD_YH2V2;
                m_max_blocks_per_mcu = 6;
                m_max_mcu_x_size = 16;
                m_max_mcu_y_size = 16;
            } else
                stop_decoding(JPGD_UNSUPPORTED_SAMP_FACTORS);
        } else
            stop_decoding(JPGD_UNSUPPORTED_COLORSPACE);

        m_max_mcus_per_row = (m_image_x_size + (m_max_mcu_x_size - 1)) / m_max_mcu_x_size;
        m_max_mcus_per_col = (m_image_y_size + (m_max_mcu_y_size - 1)) / m_max_mcu_y_size;

        // These values are for the *destination* pixels: after conversion.
        if (m_scan_type == JPGD_GRAYSCALE)
            m_dest_bytes_per_pixel = 1;
        else
            m_dest_bytes_per_pixel = 4;

        m_dest_bytes_per_scan_line = ((m_image_x_size + 15) & 0xFFF0) * m_dest_bytes_per_pixel;

        m_real_dest_bytes_per_scan_line = (m_image_x_size * m_dest_bytes_per_pixel);

        // Initialize two scan line buffers.
        m_pScan_line_0 = (unsigned char *) alloc(m_dest_bytes_per_scan_line, true);
        if ((m_scan_type == JPGD_YH1V2) || (m_scan_type == JPGD_YH2V2))
            m_pScan_line_1 = (unsigned char *) alloc(m_dest_bytes_per_scan_line, true);

        m_max_blocks_per_row = m_max_mcus_per_row * m_max_blocks_per_mcu;

        // Should never happen
        if (m_max_blocks_per_row > MAX_BLOCKS_PER_ROW)
            stop_decoding(JPGD_ASSERTION_ERROR);

        // Allocate the coefficient buffer, enough for one MCU
        m_pMCU_coefficients = (signed short *) alloc(
                m_max_blocks_per_mcu * 64 * sizeof(signed short));

        for (i = 0; i < m_max_blocks_per_mcu; i++)
            m_mcu_block_max_zag[i] = 64;

        m_expanded_blocks_per_component = m_comp_h_samp[0] * m_comp_v_samp[0];
        m_expanded_blocks_per_mcu = m_expanded_blocks_per_component * m_comps_in_frame;
        m_expanded_blocks_per_row = m_max_mcus_per_row * m_expanded_blocks_per_mcu;
        // Freq. domain chroma upsampling is only supported for H2V2 subsampling factor (the most common one I've seen).
        m_freq_domain_chroma_upsample = false;
#if JPGD_SUPPORT_FREQ_DOMAIN_UPSAMPLING
        m_freq_domain_chroma_upsample = (m_expanded_blocks_per_mcu == 4 * 3);
#endif

        if (m_freq_domain_chroma_upsample)
            m_pSample_buf = (unsigned char *) alloc(m_expanded_blocks_per_row * 64);
        else
            m_pSample_buf = (unsigned char *) alloc(m_max_blocks_per_row * 64);

        m_total_lines_left = m_image_y_size;

        m_mcu_lines_left = 0;

        create_look_ups();
    }

// The coeff_buf series of methods originally stored the coefficients
// into a "virtual" file which was located in EMS, XMS, or a disk file. A cache
// was used to make this process more efficient. Now, we can store the entire
// thing in RAM.
    jpeg_decoder::coeff_buf *
    jpeg_decoder::coeff_buf_open(int block_num_x, int block_num_y, int block_len_x,
                                 int block_len_y) {
        coeff_buf *cb = (coeff_buf *) alloc(sizeof(coeff_buf));

        cb->block_num_x = block_num_x;
        cb->block_num_y = block_num_y;
        cb->block_len_x = block_len_x;
        cb->block_len_y = block_len_y;
        cb->block_size = (block_len_x * block_len_y) * sizeof(signed short);
        cb->pData = (unsigned char *) alloc(cb->block_size * block_num_x * block_num_y, true);
        return cb;
    }

    inline signed short *jpeg_decoder::coeff_buf_getp(coeff_buf *cb, int block_x, int block_y) {
        assert((block_x < cb->block_num_x) && (block_y < cb->block_num_y));
        return (signed short *) (cb->pData + block_x * cb->block_size +
                                 block_y * (cb->block_size * cb->block_num_x));
    }

// The following methods decode the various types of m_blocks encountered
// in progressively encoded images.
    void jpeg_decoder::decode_block_dc_first(jpeg_decoder *pD, int component_id, int block_x,
                                             int block_y) {
        int s, r;
        signed short *p = pD->coeff_buf_getp(pD->m_dc_coeffs[component_id], block_x, block_y);

        if ((s = pD->huff_decode(pD->m_pHuff_tabs[pD->m_comp_dc_tab[component_id]])) != 0) {
            r = pD->get_bits_no_markers(s);
            s = JPGD_HUFF_EXTEND(r, s);
        }

        pD->m_last_dc_val[component_id] = (s += pD->m_last_dc_val[component_id]);

        p[0] = static_cast<signed short>(s << pD->m_successive_low);
    }

    void jpeg_decoder::decode_block_dc_refine(jpeg_decoder *pD, int component_id, int block_x,
                                              int block_y) {
        if (pD->get_bits_no_markers(1)) {
            signed short *p = pD->coeff_buf_getp(pD->m_dc_coeffs[component_id], block_x, block_y);

            p[0] |= (1 << pD->m_successive_low);
        }
    }

    void jpeg_decoder::decode_block_ac_first(jpeg_decoder *pD, int component_id, int block_x,
                                             int block_y) {
        int k, s, r;

        if (pD->m_eob_run) {
            pD->m_eob_run--;
            return;
        }

        signed short *p = pD->coeff_buf_getp(pD->m_ac_coeffs[component_id], block_x, block_y);

        for (k = pD->m_spectral_start; k <= pD->m_spectral_end; k++) {
            s = pD->huff_decode(pD->m_pHuff_tabs[pD->m_comp_ac_tab[component_id]]);

            r = s >> 4;
            s &= 15;

            if (s) {
                if ((k += r) > 63)
                    pD->stop_decoding(JPGD_DECODE_ERROR);

                r = pD->get_bits_no_markers(s);
                s = JPGD_HUFF_EXTEND(r, s);

                p[g_ZAG[k]] = static_cast<signed short>(s << pD->m_successive_low);
            } else {
                if (r == 15) {
                    if ((k += 15) > 63)
                        pD->stop_decoding(JPGD_DECODE_ERROR);
                } else {
                    pD->m_eob_run = 1 << r;

                    if (r)
                        pD->m_eob_run += pD->get_bits_no_markers(r);

                    pD->m_eob_run--;

                    break;
                }
            }
        }
    }

    void jpeg_decoder::decode_block_ac_refine(jpeg_decoder *pD, int component_id, int block_x,
                                              int block_y) {
        int s, k, r;
        int p1 = 1 << pD->m_successive_low;
        int m1 = (-1) << pD->m_successive_low;
        signed short *p = pD->coeff_buf_getp(pD->m_ac_coeffs[component_id], block_x, block_y);

        assert(pD->m_spectral_end <= 63);

        k = pD->m_spectral_start;

        if (pD->m_eob_run == 0) {
            for (; k <= pD->m_spectral_end; k++) {
                s = pD->huff_decode(pD->m_pHuff_tabs[pD->m_comp_ac_tab[component_id]]);

                r = s >> 4;
                s &= 15;

                if (s) {
                    if (s != 1)
                        pD->stop_decoding(JPGD_DECODE_ERROR);

                    if (pD->get_bits_no_markers(1))
                        s = p1;
                    else
                        s = m1;
                } else {
                    if (r != 15) {
                        pD->m_eob_run = 1 << r;

                        if (r)
                            pD->m_eob_run += pD->get_bits_no_markers(r);

                        break;
                    }
                }

                do {
                    signed short *this_coef = p + g_ZAG[k & 63];

                    if (*this_coef != 0) {
                        if (pD->get_bits_no_markers(1)) {
                            if ((*this_coef & p1) == 0) {
                                if (*this_coef >= 0)
                                    *this_coef = static_cast<signed short>(*this_coef + p1);
                                else
                                    *this_coef = static_cast<signed short>(*this_coef + m1);
                            }
                        }
                    } else {
                        if (--r < 0)
                            break;
                    }

                    k++;

                } while (k <= pD->m_spectral_end);

                if ((s) && (k < 64)) {
                    p[g_ZAG[k]] = static_cast<signed short>(s);
                }
            }
        }

        if (pD->m_eob_run > 0) {
            for (; k <= pD->m_spectral_end; k++) {
                signed short *this_coef =
                        p + g_ZAG[k & 63]; // logical AND to shut up static code analysis

                if (*this_coef != 0) {
                    if (pD->get_bits_no_markers(1)) {
                        if ((*this_coef & p1) == 0) {
                            if (*this_coef >= 0)
                                *this_coef = static_cast<signed short>(*this_coef + p1);
                            else
                                *this_coef = static_cast<signed short>(*this_coef + m1);
                        }
                    }
                }
            }

            pD->m_eob_run--;
        }
    }

// Decode a scan in a progressively encoded image.
    void jpeg_decoder::decode_scan(pDecode_block_func decode_block_func) {
        int mcu_row, mcu_col, mcu_block;
        int block_x_mcu[MAX_COMPONENTS], m_block_y_mcu[MAX_COMPONENTS];

        memset(m_block_y_mcu, 0, sizeof(m_block_y_mcu));

        for (mcu_col = 0; mcu_col < m_mcus_per_col; mcu_col++) {
            int component_num, component_id;

            memset(block_x_mcu, 0, sizeof(block_x_mcu));

            for (mcu_row = 0; mcu_row < m_mcus_per_row; mcu_row++) {
                int block_x_mcu_ofs = 0, block_y_mcu_ofs = 0;

                if ((m_restart_interval) && (m_restarts_left == 0))
                    process_restart();

                for (mcu_block = 0; mcu_block < m_blocks_per_mcu; mcu_block++) {
                    component_id = m_mcu_org[mcu_block];

                    decode_block_func(this, component_id,
                                      block_x_mcu[component_id] + block_x_mcu_ofs,
                                      m_block_y_mcu[component_id] + block_y_mcu_ofs);

                    if (m_comps_in_scan == 1)
                        block_x_mcu[component_id]++;
                    else {
                        if (++block_x_mcu_ofs == m_comp_h_samp[component_id]) {
                            block_x_mcu_ofs = 0;

                            if (++block_y_mcu_ofs == m_comp_v_samp[component_id]) {
                                block_y_mcu_ofs = 0;
                                block_x_mcu[component_id] += m_comp_h_samp[component_id];
                            }
                        }
                    }
                }

                m_restarts_left--;
            }

            if (m_comps_in_scan == 1)
                m_block_y_mcu[m_comp_list[0]]++;
            else {
                for (component_num = 0; component_num < m_comps_in_scan;
                     component_num++) {
                    component_id = m_comp_list[component_num];
                    m_block_y_mcu[component_id] += m_comp_v_samp[component_id];
                }
            }
        }
    }

// Decode a progressively encoded image.
    void jpeg_decoder::init_progressive() {
        int i;

        if (m_comps_in_frame == 4)
            stop_decoding(JPGD_UNSUPPORTED_COLORSPACE);

        // Allocate the coefficient buffers.
        for (i = 0; i < m_comps_in_frame; i++) {
            m_dc_coeffs[i] = coeff_buf_open(m_max_mcus_per_row * m_comp_h_samp[i],
                                            m_max_mcus_per_col * m_comp_v_samp[i], 1, 1);
            m_ac_coeffs[i] = coeff_buf_open(m_max_mcus_per_row * m_comp_h_samp[i],
                                            m_max_mcus_per_col * m_comp_v_samp[i], 8, 8);
        }

        for (;;) {
            int dc_only_scan, refinement_scan;
            pDecode_block_func decode_block_func;

            if (!init_scan())
                break;

            dc_only_scan = (m_spectral_start == 0);
            refinement_scan = (m_successive_high != 0);

            if ((m_spectral_start > m_spectral_end) || (m_spectral_end > 63))
                stop_decoding(JPGD_BAD_SOS_SPECTRAL);

            if (dc_only_scan) {
                if (m_spectral_end)
                    stop_decoding(JPGD_BAD_SOS_SPECTRAL);
            } else if (m_comps_in_scan != 1) /* AC scans can only contain one component */
                stop_decoding(JPGD_BAD_SOS_SPECTRAL);

            if ((refinement_scan) && (m_successive_low != m_successive_high - 1))
                stop_decoding(JPGD_BAD_SOS_SUCCESSIVE);

            if (dc_only_scan) {
                if (refinement_scan)
                    decode_block_func = decode_block_dc_refine;
                else
                    decode_block_func = decode_block_dc_first;
            } else {
                if (refinement_scan)
                    decode_block_func = decode_block_ac_refine;
                else
                    decode_block_func = decode_block_ac_first;
            }

            decode_scan(decode_block_func);

            m_bits_left = 16;
            get_bits(16);
            get_bits(16);
        }

        m_comps_in_scan = m_comps_in_frame;

        for (i = 0; i < m_comps_in_frame; i++)
            m_comp_list[i] = i;

        calc_mcu_block_order();
    }

    void jpeg_decoder::init_sequential() {
        if (!init_scan())
            stop_decoding(JPGD_UNEXPECTED_MARKER);
    }

    void jpeg_decoder::decode_start() {
        init_frame();

        if (m_progressive_flag)
            init_progressive();
        else
            init_sequential();
    }

    void jpeg_decoder::decode_init(jpeg_decoder_stream *pStream) {
        init(pStream);
        locate_sof_marker();
    }

    jpeg_decoder::jpeg_decoder(jpeg_decoder_stream *pStream) {
        if (setjmp(m_jmp_state))
            return;
        decode_init(pStream);
    }

    int jpeg_decoder::begin_decoding() {
        if (m_ready_flag)
            return JPGD_SUCCESS;

        if (m_error_code)
            return JPGD_FAILED;

        if (setjmp(m_jmp_state))
            return JPGD_FAILED;

        decode_start();

        m_ready_flag = true;

        return JPGD_SUCCESS;
    }

    jpeg_decoder::~jpeg_decoder() {
        free_all_blocks();
    }

    jpeg_decoder_file_stream::jpeg_decoder_file_stream() {
        m_pFile = NULL;
        m_eof_flag = false;
        m_error_flag = false;
    }

    void jpeg_decoder_file_stream::close() {
        if (m_pFile) {
            fclose(m_pFile);
            m_pFile = NULL;
        }

        m_eof_flag = false;
        m_error_flag = false;
    }

    jpeg_decoder_file_stream::~jpeg_decoder_file_stream() {
        close();
    }

    bool jpeg_decoder_file_stream::open(const char *Pfilename) {
        close();

        m_eof_flag = false;
        m_error_flag = false;

#if defined(_MSC_VER)
                                                                                                                                m_pFile = NULL;
	fopen_s(&m_pFile, Pfilename, "rb");
#else
        m_pFile = fopen(Pfilename, "rb");
#endif
        return m_pFile != NULL;
    }

    int
    jpeg_decoder_file_stream::read(unsigned char *pBuf, int max_bytes_to_read, bool *pEOF_flag) {
        if (!m_pFile)
            return -1;

        if (m_eof_flag) {
            *pEOF_flag = true;
            return 0;
        }

        if (m_error_flag)
            return -1;

        int bytes_read = static_cast<int>(fread(pBuf, 1, max_bytes_to_read, m_pFile));
        if (bytes_read < max_bytes_to_read) {
            if (ferror(m_pFile)) {
                m_error_flag = true;
                return -1;
            }

            m_eof_flag = true;
            *pEOF_flag = true;
        }

        return bytes_read;
    }

    bool jpeg_decoder_mem_stream::open(const unsigned char *pSrc_data, unsigned int size) {
        close();
        m_pSrc_data = pSrc_data;
        m_ofs = 0;
        m_size = size;
        return true;
    }

    int jpeg_decoder_mem_stream::read(unsigned char *pBuf, int max_bytes_to_read, bool *pEOF_flag) {
        *pEOF_flag = false;

        if (!m_pSrc_data)
            return -1;

        unsigned int bytes_remaining = m_size - m_ofs;
        if ((unsigned int) max_bytes_to_read > bytes_remaining) {
            max_bytes_to_read = bytes_remaining;
            *pEOF_flag = true;
        }

        memcpy(pBuf, m_pSrc_data + m_ofs, max_bytes_to_read);
        m_ofs += max_bytes_to_read;

        return max_bytes_to_read;
    }

    unsigned char *
    decompress_jpeg_image_from_stream(jpeg_decoder_stream *pStream, int *width, int *height,
                                      int *actual_comps, int req_comps) {
        if (!actual_comps) {
            err_reason = "no actual_comps";
            return NULL;
        }
        *actual_comps = 0;

        if (!pStream) {
            err_reason = "stream == NULL";
            return NULL;
        }
        if (!width) {
            err_reason = "width == NULL";
            return NULL;
        }
        if (!height) {
            err_reason = "height == NULL";
            return NULL;
        }

        if ((req_comps != 1) && (req_comps != 3) && (req_comps != 4)) {
            err_reason = "req_comps not 1, 3 or 4";
            return NULL;
        }

        jpeg_decoder decoder(pStream);
        if (decoder.get_error_code() != JPGD_SUCCESS) {
            err_reason = "decoder init failed for stream";
            return NULL;
        }

        const int image_width = decoder.get_width(),
                image_height = decoder.get_height();
        *width = image_width;
        *height = image_height;
        *actual_comps = decoder.get_num_components();

        if (decoder.begin_decoding() != JPGD_SUCCESS) {
            err_reason = "begin decoding failed";
            return NULL;
        }

        const int dst_bpl = image_width * req_comps;

        unsigned char *pImage_data = (unsigned char *) malloc(dst_bpl * image_height);
        if (!pImage_data) {
            err_reason = "image data == NULL";
            return NULL;
        }

        for (int y = 0; y < image_height; y++) {
            const unsigned char *pScan_line;
            unsigned int scan_line_len;
            if (decoder.decode((const void **) &pScan_line, &scan_line_len) != JPGD_SUCCESS) {
                free(pImage_data);
                err_reason = "line scanning failed";
                return NULL;
            }

            unsigned char *pDst = pImage_data + y * dst_bpl;

            if (((req_comps == 1) && (decoder.get_num_components() == 1)) ||
                ((req_comps == 4) && (decoder.get_num_components() == 3)))
                memcpy(pDst, pScan_line, dst_bpl);
            else if (decoder.get_num_components() == 1) {
                if (req_comps == 3) {
                    for (int x = 0; x < image_width; x++) {
                        unsigned char luma = pScan_line[x];
                        pDst[0] = luma;
                        pDst[1] = luma;
                        pDst[2] = luma;
                        pDst += 3;
                    }
                } else {
                    for (int x = 0; x < image_width; x++) {
                        unsigned char luma = pScan_line[x];
                        pDst[0] = luma;
                        pDst[1] = luma;
                        pDst[2] = luma;
                        pDst[3] = 255;
                        pDst += 4;
                    }
                }
            } else if (decoder.get_num_components() == 3) {
                if (req_comps == 1) {
                    const int YR = 19595, YG = 38470, YB = 7471;
                    for (int x = 0; x < image_width; x++) {
                        int r = pScan_line[x * 4 + 0];
                        int g = pScan_line[x * 4 + 1];
                        int b = pScan_line[x * 4 + 2];
                        *pDst++ = static_cast<unsigned char>((r * YR + g * YG + b * YB + 32768)
                                >> 16);
                    }
                } else {
                    for (int x = 0; x < image_width; x++) {
                        pDst[0] = pScan_line[x * 4 + 0];
                        pDst[1] = pScan_line[x * 4 + 1];
                        pDst[2] = pScan_line[x * 4 + 2];
                        pDst += 3;
                    }
                }
            }
        }

        return pImage_data;
    }

    const char *failure_reason(void) {
        return err_reason;
    }

    unsigned char *
    decompress_jpeg_image_from_memory(const unsigned char *pSrc_data, int src_data_size, int *width,
                                      int *height, int *actual_comps, int req_comps) {
        jpeg_decoder_mem_stream mem_stream(pSrc_data, src_data_size);
        return decompress_jpeg_image_from_stream(&mem_stream, width, height, actual_comps,
                                                 req_comps);
    }

    unsigned char *
    decompress_jpeg_image_from_file(const char *pSrc_filename, int *width, int *height,
                                    int *actual_comps, int req_comps) {
        jpeg_decoder_file_stream file_stream;
        if (!file_stream.open(pSrc_filename))
            return NULL;
        return decompress_jpeg_image_from_stream(&file_stream, width, height, actual_comps,
                                                 req_comps);
    }
} // namespace jpgd
// endinitialize jpgd decompresser

static jfieldID basePtr;

Pixmap_M(void, initialize)(JNIEnv *env, jclass clazz) {
    //initialize field ID of JNI
    basePtr = env->GetFieldID(clazz, "basePtr", "J");

    stbi::initialize();
}

static inline uint8_t pixel_size(Pixmap::Format format) {
    switch (format) {
        case Pixmap::Format_Alpha:
            return 1;
        case Pixmap::Format_Luminance_Alpha:
        case Pixmap::Format_RGB565:
        case Pixmap::Format_RGBA4444:
            return 2;
        case Pixmap::Format_RGB888:
            return 3;
        default:
        case Pixmap::Format_RGBA8888:
            return 4;
    }
}

Pixmap::Pixmap(unsigned int w, unsigned int h, Pixmap::Format f, bool s) :
        width(w), height(h), format(f),
        pixels((unsigned char *) malloc(w * h * pixel_size(f))), scale(s) {
}

Pixmap::Pixmap(unsigned int w, unsigned int h, Pixmap::Format f, const unsigned char *p, bool s) :
        width(w), height(h), format(f), pixels(p), scale(s) {
}

Pixmap::~Pixmap() {
    free((void *) pixels);
}

//pixels binary function

typedef uint32_t (*to_pixel_format_func)(uint32_t);

static inline uint32_t RGBA8888_to_alpha(uint32_t color) {
    return color & 0xff;
}

static inline uint32_t RGBA8888_to_luminance_alpha(uint32_t color) {
    //54.213 + 182.376 + 18.411 = 255 then alpha
    return ((uint32_t) (0.2126f * ((color & 0xff000000) >> 24) + 0.7152f * ((color & 0xff0000) >> 16) + 0.0722f * ((color & 0xff00) >> 8)) & 0xff) | ((color & 0xff) << 8);
    //0xaall
}

static inline uint32_t RGBA8888_to_RGB888(uint32_t color) {
    return color >> 8;
}

static inline uint32_t RGBA8888_to_RGBA8888(uint32_t color) {
    return color;
}

static inline uint32_t RGBA8888_to_RGB565(uint32_t color) {
    return (((color & 0xff000000) >> 16) & 0xf800) | (((color & 0xff0000) >> 13) & 0x7e0) | (((color & 0xff00) >> 11) & 0x1f);
}

static inline uint32_t RGBA8888_to_RGBA4444(uint32_t color) {
    return (((color & 0xff000000) >> 16) & 0xf000) | (((color & 0xff0000) >> 12) & 0xf00) | (((color & 0xff00) >> 8) & 0xf0) | (((color & 0xff) >> 4) & 0xf);
}

static inline to_pixel_format_func to_pixel_func_ptr(Pixmap::Format format) {
    switch (format) {
        case Pixmap::Format_Alpha:
            return &RGBA8888_to_alpha;
        case Pixmap::Format_Luminance_Alpha:
            return &RGBA8888_to_luminance_alpha;
        case Pixmap::Format_RGB888:
            return &RGBA8888_to_RGB888;
        case Pixmap::Format_RGBA8888:
            return &RGBA8888_to_RGBA8888;
        case Pixmap::Format_RGB565:
            return &RGBA8888_to_RGB565;
        case Pixmap::Format_RGBA4444:
            return &RGBA8888_to_RGBA4444;
        default:
            return 0;
    }
}

typedef uint32_t (*to_RGBA8888_func)(uint32_t);

static inline uint32_t alpha_to_RGBA888(uint32_t color) {
    return 0xffffff00 | (color & 0xff);
}

static inline uint32_t luminance_alpha_to_RGBA888(uint32_t color) {
    return ((color & 0xff) << 24) | //luminance r
           ((color & 0xff) << 16) | //luminance g
           ((color & 0xff) << 8) | //luminance b
           ((color & 0xff00) >> 8);//luminance a
}

static inline uint32_t RGB888_to_RGBA888(uint32_t color) {
    return 0x000000ff | (color << 8); //rrggbbaa
}

static inline uint32_t RGBA8888_to_RGBA888(uint32_t color) {
    return color;
}

static inline uint32_t RGB565_to_RGBA888(uint32_t color) {
    return 0xff | // A nothing
           ((uint32_t) (((color & 0xf800) >> 11) * 0xff / 0x1f) << 24) | // R5
           ((uint32_t) (((color & 0x7e0) >> 5) * 0xff / 0x3f) << 16) | //G6
           ((uint32_t) ((color & 0x1f) * 0xff / 0x1f) << 8); //B5
}

static inline uint32_t RGBA4444_to_RGBA888(uint32_t color) {
    return ((color & 0xf000) * 0x11000) | // 0xrr000000
           ((color & 0xf00) * 0x1100) | // 0x00gg0000
           ((color & 0xf0) * 0x110) | // 0x0000bb00
           ((color & 0xf) * 0x11); // 0x000000aa
}

static inline to_RGBA8888_func to_RGBA8888_func_ptr(Pixmap::Format format) {
    switch (format) {
        case Pixmap::Format_Alpha:
            return &alpha_to_RGBA888;
        case Pixmap::Format_Luminance_Alpha:
            return &luminance_alpha_to_RGBA888;
        case Pixmap::Format_RGB888:
            return &RGB888_to_RGBA888;
        case Pixmap::Format_RGBA8888:
            return &RGBA8888_to_RGBA888;
        case Pixmap::Format_RGB565:
            return &RGB565_to_RGBA888;
        case Pixmap::Format_RGBA4444:
            return &RGBA4444_to_RGBA888;
        default:
            return 0;
    }
}

typedef void (*set_pixel_func)(unsigned char *, uint32_t);

static inline void set_pixel_alpha(unsigned char *pixel_addr, uint32_t color) {
    *pixel_addr = (unsigned char) (color & 0xff);
}

static inline void set_pixel_luminance_alpha(unsigned char *pixel_addr, uint32_t color) {
    *(unsigned short *) pixel_addr = (unsigned short) color;
}

static inline void set_pixel_RGB888(unsigned char *pixel_addr, uint32_t color) {
    pixel_addr[0] = (color & 0xff0000) >> 16;
    pixel_addr[1] = (color & 0xff00) >> 8;
    pixel_addr[2] = (color & 0xff);
}

static inline void set_pixel_RGBA8888(unsigned char *pixel_addr, uint32_t color) {
    *(uint32_t *) pixel_addr =
            ((color & 0xff000000) >> 24) | ((color & 0xff0000) >> 8) | ((color & 0xff00) << 8) |
            ((color & 0xff) << 24);
}

static inline void set_pixel_RGB565(unsigned char *pixel_addr, uint32_t color) {
    *(uint16_t *) pixel_addr = (uint16_t) color;
}

static inline void set_pixel_RGBA4444(unsigned char *pixel_addr, uint32_t color) {
    *(uint16_t *) pixel_addr = (uint16_t) color;
}

static inline set_pixel_func set_pixel_func_ptr(Pixmap::Format format) {
    switch (format) {
        default: // better idea for a default?
        case Pixmap::Format_Alpha:
            return &set_pixel_alpha;
        case Pixmap::Format_Luminance_Alpha:
            return &set_pixel_luminance_alpha;
        case Pixmap::Format_RGB888:
            return &set_pixel_RGB888;
        case Pixmap::Format_RGBA8888:
            return &set_pixel_RGBA8888;
        case Pixmap::Format_RGB565:
            return &set_pixel_RGB565;
        case Pixmap::Format_RGBA4444:
            return &set_pixel_RGBA4444;
    }
}

typedef uint32_t (*get_pixel_func)(unsigned char *);

static inline uint32_t get_pixel_alpha(unsigned char *pixel_addr) {
    return *pixel_addr;
}

static inline uint32_t get_pixel_luminance_alpha(unsigned char *pixel_addr) {
    return (((uint32_t) pixel_addr[0]) << 8) | pixel_addr[1];
}

static inline uint32_t get_pixel_RGB888(unsigned char *pixel_addr) {
    return (((uint32_t) pixel_addr[0]) << 16) | (((uint32_t) pixel_addr[1]) << 8) | (pixel_addr[2]);
}

static inline uint32_t get_pixel_RGBA8888(unsigned char *pixel_addr) {
    return (((uint32_t) pixel_addr[0]) << 24) | (((uint32_t) pixel_addr[1]) << 16) |
           (((uint32_t) pixel_addr[2]) << 8) | pixel_addr[3];
}

static inline uint32_t get_pixel_RGB565(unsigned char *pixel_addr) {
    return *(uint16_t *) pixel_addr;
}

static inline uint32_t get_pixel_RGBA4444(unsigned char *pixel_addr) {
    return *(uint16_t *) pixel_addr;
}

static inline get_pixel_func get_pixel_func_ptr(Pixmap::Format format) {
    switch (format) {
        default:
        case Pixmap::Format_Alpha:
            return &get_pixel_alpha;
        case Pixmap::Format_Luminance_Alpha:
            return &get_pixel_luminance_alpha;
        case Pixmap::Format_RGB888:
            return &get_pixel_RGB888;
        case Pixmap::Format_RGBA8888:
            return &get_pixel_RGBA8888;
        case Pixmap::Format_RGB565:
            return &get_pixel_RGB565;
        case Pixmap::Format_RGBA4444:
            return &get_pixel_RGBA4444;
    }
}

Pixmap_M(jobject, load)(JNIEnv *env, jclass clazz, jlongArray nativeData, jbyteArray buffer,
                        jint offset, jint len) {
    const unsigned char *p_buffer = (const unsigned char *) env->GetPrimitiveArrayCritical(buffer, 0);
    uint32_t width, height, format;
    const unsigned char *pixels = stbi::stbi_load_from_memory(p_buffer + offset, len,
                                                              (int *) &width,
                                                              (int *) &height, (int *) &format, reqFormat_rgb_alpha);
    if (pixels == NULL)
        pixels = jpgd::decompress_jpeg_image_from_memory(p_buffer + offset, len, (int *) &width,
                                                         (int *) &height, (int *) &format, reqFormat_rgb);
    env->ReleasePrimitiveArrayCritical(buffer, (char *) p_buffer, 0);
    if (pixels == NULL)
        return 0;
    Pixmap *pixmap = new Pixmap(width, height, (Pixmap::Format) format, pixels, true);
    jobject pixel_buffer = env->NewDirectByteBuffer((void *) pixmap->pixels,
                                                    pixmap->width * pixmap->height *
                                                    pixel_size(pixmap->format));
    jlong *p_native_data = (jlong *) env->GetPrimitiveArrayCritical(nativeData, 0);
    p_native_data[0] = (jlong) pixmap;
    p_native_data[1] = pixmap->width;
    p_native_data[2] = pixmap->height;
    p_native_data[3] = pixmap->format;
    env->ReleasePrimitiveArrayCritical(nativeData, p_native_data, 0);

    return pixel_buffer;
}

Pixmap_M(jobject, newPixmap)(JNIEnv *env, jclass clazz, jlongArray vals, jint width, jint height,
                             jint format) {
    Pixmap *pixmap = new Pixmap(width, height, Pixmap::Format(format), true);
    if (!pixmap)
        return 0;
    if (!pixmap->pixels) {
        delete pixmap;
        return 0;
    }

    jobject pixel_buffer = env->NewDirectByteBuffer((void *) pixmap->pixels,
                                                    pixmap->width * pixmap->height *
                                                    pixel_size(pixmap->format));
    jlong *data = (jlong *) env->GetPrimitiveArrayCritical(vals, 0);
    data[0] = (jlong) pixmap;
    data[1] = pixmap->width;
    data[2] = pixmap->height;
    data[3] = pixmap->format;
    env->ReleasePrimitiveArrayCritical(vals, data, 0);
    return pixel_buffer;
}

Pixmap_M(void, free)(JNIEnv *env, jobject object) {
    free((void *) env->GetLongField(object, basePtr));
}

Pixmap_M(void, clear)(JNIEnv *env, jobject object, jint color) {
    Pixmap *data = (Pixmap *) env->GetLongField(object, basePtr);
    color = to_pixel_func_ptr(data->format)(color);
    int pixels = data->width * data->height;

    switch (data->format) {
        default:
            break;
        case Pixmap::Format_Alpha:
            memset((void *) data->pixels, color, pixels);
            break;
        case Pixmap::Format_RGB888: {
            unsigned char *ptr = (unsigned char *) data->pixels;
            unsigned char r = (color & 0xff0000) >> 16;
            unsigned char g = (color & 0xff00) >> 8;
            unsigned char b = (color & 0xff);
            for (; pixels > 0; pixels--) {
                *ptr = r;
                ptr++;
                *ptr = g;
                ptr++;
                *ptr = b;
                ptr++;
            }
        }
            break;
        case Pixmap::Format_RGBA8888: {
            uint32_t *ptr = (uint32_t *) data->pixels;
            color = ((color & 0x000000ff) << 24) | ((color & 0x0000ff00) << 8) |
                    ((color & 0x00ff0000) >> 8) | ((color & 0xff000000) >> 24);
            for (; pixels > 0; pixels--) {
                *ptr = color;
                ptr++;
            }
        }
            break;
        case Pixmap::Format_Luminance_Alpha:
        case Pixmap::Format_RGB565:
        case Pixmap::Format_RGBA4444: {
            unsigned short *ptr = (unsigned short *) data->pixels;
            unsigned short c = color & 0xffff;
            for (; pixels > 0; pixels--) {
                *ptr = c;
                ptr++;
            }
        }
            break;
    }
}

Pixmap_M(void, setPixel)(JNIEnv *env, jobject object, jint x, jint y, jint col) {
    if (x < 0 || y < 0) return;
    Pixmap *pixmap = (Pixmap *) env->GetLongField(object, basePtr);
    if (x >= pixmap->width || y >= pixmap->height) return;
    pixmap->pixels += (x + pixmap->width * y) * pixel_size(pixmap->format);
    col = to_pixel_func_ptr(pixmap->format)(col);
    set_pixel_func_ptr(pixmap->format)((unsigned char *) pixmap->pixels, col);
}

Pixmap_M(jint, getPixel)(JNIEnv *env, jobject object, jint x, jint y) {
    Pixmap *pixmap = (Pixmap *) env->GetLongField(object, basePtr);
    if (x < 0 || y < 0 || x >= pixmap->width || y >= pixmap->height)
        return 0;
    unsigned char *ptr =
            (unsigned char *) pixmap->pixels + (x + pixmap->width * y) * pixel_size(pixmap->format);
    return to_RGBA8888_func_ptr(pixmap->format)(get_pixel_func_ptr(pixmap->format)(ptr));
}

Pixmap_M(void, drawPixmap)(JNIEnv *env, jobject object, jlong srcP, jint srcX, jint srcY,
                           jint srcWidth, jint srcHeight, jint dstX, jint dstY, jint dstWidth,
                           jint dstHeight) {
    Pixmap *src = (Pixmap *) srcP, *dst = (Pixmap *) env->GetLongField(object, basePtr);

    //setter
    set_pixel_func pset = set_pixel_func_ptr(dst->format);
    //formatter
    to_RGBA8888_func pfbase = to_RGBA8888_func_ptr(src->format);
    to_pixel_format_func pfdst = to_pixel_func_ptr(dst->format);
    //getter
    get_pixel_func pget = get_pixel_func_ptr(src->format);

    uint32_t sbpp = pixel_size(src->format);
    uint32_t dbpp = pixel_size(dst->format);
    uint32_t spitch = sbpp * src->width;
    uint32_t dpitch = dbpp * dst->width;

    unsigned int sx = 0;
    unsigned int sy = 0;
    unsigned int dx = 0;
    unsigned int dy = 0;

    if (srcWidth == dstWidth && srcHeight == dstHeight) {
        for (sy = srcY, dy = dstY; sy < srcY + srcHeight; sy++, dy++) {
            if (sy >= src->height || dy >= dst->height)
                break;
            for (sx = srcX, dx = dstX; sx < srcX + srcWidth; sx++, dx++) {
                if (sx >= src->width || dx >= dst->width)
                    break;
                const void *src_ptr = src->pixels + sx * sbpp + sy * spitch;
                const void *dst_ptr = dst->pixels + dx * dbpp + dy * dpitch;
                uint32_t src_col = pfbase(pget((unsigned char *) src_ptr));
                pset((unsigned char *) dst_ptr, pfdst(src_col));
            }
        }
    } else {
        int i = 0;
        int j = 0;
        if (dst->scale) {
            //Scale Type : LINEAR
            uint32_t x_ratio = (srcWidth << 16) / dstWidth + 1;
            uint32_t y_ratio = (srcHeight << 16) / dstHeight + 1;

            for (; i < dstHeight; i++) {
                sy = ((i * y_ratio) >> 16) + srcY;
                dy = i + dstY;
                if (sy < 0 || dy < 0)
                    continue;
                if (sy >= src->height || dy >= dst->height)
                    break;

                for (j = 0; j < dstWidth; j++) {
                    sx = ((j * x_ratio) >> 16) + srcX;
                    dx = j + dstX;
                    if (sx < 0 || dx < 0)
                        continue;
                    if (sx >= src->width || dx >= dst->width)
                        break;

                    unsigned char *src_ptr =
                            (unsigned char *) src->pixels + sx * sbpp + sy * spitch;
                    unsigned char *dst_ptr =
                            (unsigned char *) dst->pixels + dx * dbpp + dy * dpitch;
                    uint32_t src_col = pfbase(pget(src_ptr));
                    pset(dst_ptr, pfdst(src_col));
                }
            }
        } else {
            //Scale Type : NEAREST
            float x_ratio = ((float) srcWidth - 1) / dstWidth;
            float y_ratio = ((float) srcHeight - 1) / dstHeight;
            float x_diff = 0;
            float y_diff = 0;

            for (; i < dstHeight; i++) {
                sy = (unsigned int) (i * y_ratio) + srcY;
                dy = i + dstY;
                y_diff = (y_ratio * i + srcY) - sy;
                if (sy < 0 || dy < 0)
                    continue;
                if (sy >= src->height || dy >= dst->height)
                    break;

                for (j = 0; j < dstWidth; j++) {
                    sx = (unsigned int) (j * x_ratio) + srcX;
                    dx = j + dstX;
                    x_diff = (x_ratio * j + srcX) - sx;
                    if (sx < 0 || dx < 0)
                        continue;
                    if (sx >= src->width || dx >= dst->width)
                        break;

                    unsigned char *dst_ptr = (unsigned char *) dst->pixels
                                             + dx * dbpp + dy * dpitch;
                    unsigned char *src_ptr = (unsigned char *) src->pixels
                                             + sx * sbpp + sy * spitch;
                    uint32_t c1 = 0, c2 = 0, c3 = 0, c4 = 0;
                    c1 = pfbase(pget(src_ptr));
                    if (sx + 1 < srcWidth)
                        c2 = pfbase(pget(src_ptr + sbpp));
                    else
                        c2 = c1;
                    if (sy + 1 < srcHeight)
                        c3 = pfbase(pget(src_ptr + spitch));
                    else
                        c3 = c1;
                    if (sx + 1 < srcWidth && sy + 1 < srcHeight)
                        c4 = pfbase(pget(src_ptr + spitch + sbpp));
                    else
                        c4 = c1;

                    float ta = (1 - x_diff) * (1 - y_diff);
                    float tb = (x_diff) * (1 - y_diff);
                    float tc = (1 - x_diff) * (y_diff);
                    float td = (x_diff) * (y_diff);

                    uint32_t r = (uint32_t) (((c1 & 0xff000000) >> 24) * ta
                                             + ((c2 & 0xff000000) >> 24) * tb
                                             + ((c3 & 0xff000000) >> 24) * tc
                                             + ((c4 & 0xff000000) >> 24) * td) & 0xff;
                    uint32_t g = (uint32_t) (((c1 & 0xff0000) >> 16) * ta
                                             + ((c2 & 0xff0000) >> 16) * tb
                                             + ((c3 & 0xff0000) >> 16) * tc
                                             + ((c4 & 0xff0000) >> 16) * td) & 0xff;
                    uint32_t b = (uint32_t) (((c1 & 0xff00) >> 8) * ta
                                             + ((c2 & 0xff00) >> 8) * tb
                                             + ((c3 & 0xff00) >> 8) * tc
                                             + ((c4 & 0xff00) >> 8) * td) & 0xff;
                    uint32_t a = (uint32_t) ((c1 & 0xff) * ta + (c2 & 0xff) * tb
                                             + (c3 & 0xff) * tc + (c4 & 0xff) * td) & 0xff;

                    uint32_t src_col = (r << 24) | (g << 16) | (b << 8) | a;

                    pset(dst_ptr, pfdst(src_col));
                }
            }
        }
    }
}

Pixmap_M(void, setScaleType)(JNIEnv *env, jobject object, jboolean scale) {
    ((Pixmap *) env->GetLongField(object, basePtr))->scale = scale;
}

Pixmap_M(jstring, getFailureReason)(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(
            stbi::g_failure_reason ? stbi::g_failure_reason : jpgd::failure_reason());
}
