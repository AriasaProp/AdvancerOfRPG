package com.ariasaproject.advancerofrpg.graphics;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

//TGF = Translated Graphics Function
public interface TGF {
    public static final int GL_DEPTH_BUFFER_BIT = 0x00000100;
    public static final int GL_STENCIL_BUFFER_BIT = 0x00000400;
    public static final int GL_COLOR_BUFFER_BIT = 0x00004000;
    public static final int GL_FALSE = 0;
    public static final int GL_TRUE = 1;
    public static final int GL_POINTS = 0x0000;
    public static final int GL_LINES = 0x0001;
    public static final int GL_LINE_LOOP = 0x0002;
    public static final int GL_LINE_STRIP = 0x0003;
    public static final int GL_TRIANGLES = 0x0004;
    public static final int GL_TRIANGLE_STRIP = 0x0005;
    public static final int GL_TRIANGLE_FAN = 0x0006;
    public static final int GL_ZERO = 0;
    public static final int GL_ONE = 1;
    public static final int GL_SRC_COLOR = 0x0300;
    public static final int GL_ONE_MINUS_SRC_COLOR = 0x0301;
    public static final int GL_SRC_ALPHA = 0x0302;
    public static final int GL_ONE_MINUS_SRC_ALPHA = 0x0303;
    public static final int GL_DST_ALPHA = 0x0304;
    public static final int GL_ONE_MINUS_DST_ALPHA = 0x0305;
    public static final int GL_DST_COLOR = 0x0306;
    public static final int GL_ONE_MINUS_DST_COLOR = 0x0307;
    public static final int GL_SRC_ALPHA_SATURATE = 0x0308;
    public static final int GL_FUNC_ADD = 0x8006;
    public static final int GL_BLEND_EQUATION = 0x8009;
    public static final int GL_BLEND_EQUATION_RGB = 0x8009;
    public static final int GL_BLEND_EQUATION_ALPHA = 0x883D;
    public static final int GL_FUNC_SUBTRACT = 0x800A;
    public static final int GL_FUNC_REVERSE_SUBTRACT = 0x800B;
    public static final int GL_BLEND_DST_RGB = 0x80C8;
    public static final int GL_BLEND_SRC_RGB = 0x80C9;
    public static final int GL_BLEND_DST_ALPHA = 0x80CA;
    public static final int GL_BLEND_SRC_ALPHA = 0x80CB;
    public static final int GL_CONSTANT_COLOR = 0x8001;
    public static final int GL_ONE_MINUS_CONSTANT_COLOR = 0x8002;
    public static final int GL_CONSTANT_ALPHA = 0x8003;
    public static final int GL_ONE_MINUS_CONSTANT_ALPHA = 0x8004;
    public static final int GL_BLEND_COLOR = 0x8005;
    public static final int GL_ARRAY_BUFFER = 0x8892;
    public static final int GL_ELEMENT_ARRAY_BUFFER = 0x8893;
    public static final int GL_ARRAY_BUFFER_BINDING = 0x8894;
    public static final int GL_ELEMENT_ARRAY_BUFFER_BINDING = 0x8895;
    public static final int GL_STREAM_DRAW = 0x88E0;
    public static final int GL_STATIC_DRAW = 0x88E4;
    public static final int GL_DYNAMIC_DRAW = 0x88E8;
    public static final int GL_BUFFER_SIZE = 0x8764;
    public static final int GL_BUFFER_USAGE = 0x8765;
    public static final int GL_CURRENT_VERTEX_ATTRIB = 0x8626;
    public static final int GL_FRONT = 0x0404;
    public static final int GL_BACK = 0x0405;
    public static final int GL_FRONT_AND_BACK = 0x0408;
    public static final int GL_TEXTURE_2D = 0x0DE1;
    public static final int GL_CULL_FACE = 0x0B44;
    public static final int GL_BLEND = 0x0BE2;
    public static final int GL_DITHER = 0x0BD0;
    public static final int GL_STENCIL_TEST = 0x0B90;
    public static final int GL_DEPTH_TEST = 0x0B71;
    public static final int GL_SCISSOR_TEST = 0x0C11;
    public static final int GL_POLYGON_OFFSET_FILL = 0x8037;
    public static final int GL_SAMPLE_ALPHA_TO_COVERAGE = 0x809E;
    public static final int GL_SAMPLE_COVERAGE = 0x80A0;
    public static final int GL_NO_ERROR = 0;
    public static final int GL_INVALID_ENUM = 0x0500;
    public static final int GL_INVALID_VALUE = 0x0501;
    public static final int GL_INVALID_OPERATION = 0x0502;
    public static final int GL_OUT_OF_MEMORY = 0x0505;
    public static final int GL_CW = 0x0900;
    public static final int GL_CCW = 0x0901;
    public static final int GL_LINE_WIDTH = 0x0B21;
    public static final int GL_ALIASED_POINT_SIZE_RANGE = 0x846D;
    public static final int GL_ALIASED_LINE_WIDTH_RANGE = 0x846E;
    public static final int GL_CULL_FACE_MODE = 0x0B45;
    public static final int GL_FRONT_FACE = 0x0B46;
    public static final int GL_DEPTH_RANGE = 0x0B70;
    public static final int GL_DEPTH_WRITEMASK = 0x0B72;
    public static final int GL_DEPTH_CLEAR_VALUE = 0x0B73;
    public static final int GL_DEPTH_FUNC = 0x0B74;
    public static final int GL_STENCIL_CLEAR_VALUE = 0x0B91;
    public static final int GL_STENCIL_FUNC = 0x0B92;
    public static final int GL_STENCIL_FAIL = 0x0B94;
    public static final int GL_STENCIL_PASS_DEPTH_FAIL = 0x0B95;
    public static final int GL_STENCIL_PASS_DEPTH_PASS = 0x0B96;
    public static final int GL_STENCIL_REF = 0x0B97;
    public static final int GL_STENCIL_VALUE_MASK = 0x0B93;
    public static final int GL_STENCIL_WRITEMASK = 0x0B98;
    public static final int GL_STENCIL_BACK_FUNC = 0x8800;
    public static final int GL_STENCIL_BACK_FAIL = 0x8801;
    public static final int GL_STENCIL_BACK_PASS_DEPTH_FAIL = 0x8802;
    public static final int GL_STENCIL_BACK_PASS_DEPTH_PASS = 0x8803;
    public static final int GL_STENCIL_BACK_REF = 0x8CA3;
    public static final int GL_STENCIL_BACK_VALUE_MASK = 0x8CA4;
    public static final int GL_STENCIL_BACK_WRITEMASK = 0x8CA5;
    public static final int GL_VIEWPORT = 0x0BA2;
    public static final int GL_SCISSOR_BOX = 0x0C10;
    public static final int GL_COLOR_CLEAR_VALUE = 0x0C22;
    public static final int GL_COLOR_WRITEMASK = 0x0C23;
    public static final int GL_UNPACK_ALIGNMENT = 0x0CF5;
    public static final int GL_PACK_ALIGNMENT = 0x0D05;
    public static final int GL_MAX_TEXTURE_SIZE = 0x0D33;
    public static final int GL_MAX_TEXTURE_UNITS = 0x84E2;
    public static final int GL_MAX_VIEWPORT_DIMS = 0x0D3A;
    public static final int GL_SUBPIXEL_BITS = 0x0D50;
    public static final int GL_RED_BITS = 0x0D52;
    public static final int GL_GREEN_BITS = 0x0D53;
    public static final int GL_BLUE_BITS = 0x0D54;
    public static final int GL_ALPHA_BITS = 0x0D55;
    public static final int GL_DEPTH_BITS = 0x0D56;
    public static final int GL_STENCIL_BITS = 0x0D57;
    public static final int GL_POLYGON_OFFSET_UNITS = 0x2A00;
    public static final int GL_POLYGON_OFFSET_FACTOR = 0x8038;
    public static final int GL_TEXTURE_BINDING_2D = 0x8069;
    public static final int GL_SAMPLE_BUFFERS = 0x80A8;
    public static final int GL_SAMPLES = 0x80A9;
    public static final int GL_SAMPLE_COVERAGE_VALUE = 0x80AA;
    public static final int GL_SAMPLE_COVERAGE_INVERT = 0x80AB;
    public static final int GL_DONT_CARE = 0x1100;
    public static final int GL_FASTEST = 0x1101;
    public static final int GL_NICEST = 0x1102;
    public static final int GL_GENERATE_MIPMAP = 0x8191;
    public static final int GL_GENERATE_MIPMAP_HINT = 0x8192;
    public static final int GL_BYTE = 0x1400;
    public static final int GL_UNSIGNED_BYTE = 0x1401;
    public static final int GL_SHORT = 0x1402;
    public static final int GL_UNSIGNED_SHORT = 0x1403;
    public static final int GL_INT = 0x1404;
    public static final int GL_UNSIGNED_INT = 0x1405;
    public static final int GL_FLOAT = 0x1406;
    public static final int GL_FIXED = 0x140C;
    public static final int GL_DEPTH_COMPONENT = 0x1902;
    public static final int GL_ALPHA = 0x1906;
    public static final int GL_RGB = 0x1907;
    public static final int GL_RGBA = 0x1908;
    public static final int GL_LUMINANCE = 0x1909;
    public static final int GL_LUMINANCE_ALPHA = 0x190A;
    public static final int GL_UNSIGNED_SHORT_4_4_4_4 = 0x8033;
    public static final int GL_UNSIGNED_SHORT_5_5_5_1 = 0x8034;
    public static final int GL_UNSIGNED_SHORT_5_6_5 = 0x8363;
    public static final int GL_FRAGMENT_SHADER = 0x8B30;
    public static final int GL_MAX_VERTEX_ATTRIBS = 0x8869;
    public static final int GL_MAX_VERTEX_UNIFORM_VECTORS = 0x8DFB;
    public static final int GL_MAX_VARYING_VECTORS = 0x8DFC;
    public static final int GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS = 0x8B4D;
    public static final int GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS = 0x8B4C;
    public static final int GL_MAX_TEXTURE_IMAGE_UNITS = 0x8872;
    public static final int GL_MAX_FRAGMENT_UNIFORM_VECTORS = 0x8DFD;
    public static final int GL_SHADER_TYPE = 0x8B4F;
    public static final int GL_DELETE_STATUS = 0x8B80;
    public static final int GL_LINK_STATUS = 0x8B82;
    public static final int GL_VALIDATE_STATUS = 0x8B83;
    public static final int GL_ATTACHED_SHADERS = 0x8B85;
    public static final int GL_ACTIVE_UNIFORMS = 0x8B86;
    public static final int GL_ACTIVE_UNIFORM_MAX_LENGTH = 0x8B87;
    public static final int GL_ACTIVE_ATTRIBUTES = 0x8B89;
    public static final int GL_ACTIVE_ATTRIBUTE_MAX_LENGTH = 0x8B8A;
    public static final int GL_SHADING_LANGUAGE_VERSION = 0x8B8C;
    public static final int GL_CURRENT_PROGRAM = 0x8B8D;
    public static final int GL_NEVER = 0x0200;
    public static final int GL_LESS = 0x0201;
    public static final int GL_EQUAL = 0x0202;
    public static final int GL_LEQUAL = 0x0203;
    public static final int GL_GREATER = 0x0204;
    public static final int GL_NOTEQUAL = 0x0205;
    public static final int GL_GEQUAL = 0x0206;
    public static final int GL_ALWAYS = 0x0207;
    public static final int GL_KEEP = 0x1E00;
    public static final int GL_REPLACE = 0x1E01;
    public static final int GL_INCR = 0x1E02;
    public static final int GL_DECR = 0x1E03;
    public static final int GL_INVERT = 0x150A;
    public static final int GL_INCR_WRAP = 0x8507;
    public static final int GL_DECR_WRAP = 0x8508;
    public static final int GL_VENDOR = 0x1F00;
    public static final int GL_RENDERER = 0x1F01;
    public static final int GL_VERSION = 0x1F02;
    public static final int GL_EXTENSIONS = 0x1F03;
    public static final int GL_NEAREST = 0x2600;
    public static final int GL_LINEAR = 0x2601;
    public static final int GL_NEAREST_MIPMAP_NEAREST = 0x2700;
    public static final int GL_LINEAR_MIPMAP_NEAREST = 0x2701;
    public static final int GL_NEAREST_MIPMAP_LINEAR = 0x2702;
    public static final int GL_LINEAR_MIPMAP_LINEAR = 0x2703;
    public static final int GL_TEXTURE_MAG_FILTER = 0x2800;
    public static final int GL_TEXTURE_MIN_FILTER = 0x2801;
    public static final int GL_TEXTURE_WRAP_S = 0x2802;
    public static final int GL_TEXTURE_WRAP_T = 0x2803;
    public static final int GL_TEXTURE = 0x1702;
    public static final int GL_TEXTURE_CUBE_MAP = 0x8513;
    public static final int GL_TEXTURE_BINDING_CUBE_MAP = 0x8514;
    public static final int GL_TEXTURE_CUBE_MAP_POSITIVE_X = 0x8515;
    public static final int GL_TEXTURE_CUBE_MAP_NEGATIVE_X = 0x8516;
    public static final int GL_TEXTURE_CUBE_MAP_POSITIVE_Y = 0x8517;
    public static final int GL_TEXTURE_CUBE_MAP_NEGATIVE_Y = 0x8518;
    public static final int GL_TEXTURE_CUBE_MAP_POSITIVE_Z = 0x8519;
    public static final int GL_TEXTURE_CUBE_MAP_NEGATIVE_Z = 0x851A;
    public static final int GL_MAX_CUBE_MAP_TEXTURE_SIZE = 0x851C;
    public static final int GL_ACTIVE_TEXTURE = 0x84E0;
    public static final int GL_REPEAT = 0x2901;
    public static final int GL_CLAMP_TO_EDGE = 0x812F;
    public static final int GL_MIRRORED_REPEAT = 0x8370;
    public static final int GL_FLOAT_VEC2 = 0x8B50;
    public static final int GL_FLOAT_VEC3 = 0x8B51;
    public static final int GL_FLOAT_VEC4 = 0x8B52;
    public static final int GL_INT_VEC2 = 0x8B53;
    public static final int GL_INT_VEC3 = 0x8B54;
    public static final int GL_INT_VEC4 = 0x8B55;
    public static final int GL_BOOL = 0x8B56;
    public static final int GL_BOOL_VEC2 = 0x8B57;
    public static final int GL_BOOL_VEC3 = 0x8B58;
    public static final int GL_BOOL_VEC4 = 0x8B59;
    public static final int GL_FLOAT_MAT2 = 0x8B5A;
    public static final int GL_FLOAT_MAT3 = 0x8B5B;
    public static final int GL_FLOAT_MAT4 = 0x8B5C;
    public static final int GL_SAMPLER_2D = 0x8B5E;
    public static final int GL_SAMPLER_CUBE = 0x8B60;
    public static final int GL_VERTEX_ATTRIB_ARRAY_ENABLED = 0x8622;
    public static final int GL_VERTEX_ATTRIB_ARRAY_SIZE = 0x8623;
    public static final int GL_VERTEX_ATTRIB_ARRAY_STRIDE = 0x8624;
    public static final int GL_VERTEX_ATTRIB_ARRAY_TYPE = 0x8625;
    public static final int GL_VERTEX_ATTRIB_ARRAY_NORMALIZED = 0x886A;
    public static final int GL_VERTEX_ATTRIB_ARRAY_POINTER = 0x8645;
    public static final int GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING = 0x889F;
    public static final int GL_IMPLEMENTATION_COLOR_READ_TYPE = 0x8B9A;
    public static final int GL_IMPLEMENTATION_COLOR_READ_FORMAT = 0x8B9B;
    public static final int GL_COMPILE_STATUS = 0x8B81;
    public static final int GL_INFO_LOG_LENGTH = 0x8B84;
    public static final int GL_SHADER_SOURCE_LENGTH = 0x8B88;
    public static final int GL_SHADER_COMPILER = 0x8DFA;
    public static final int GL_SHADER_BINARY_FORMATS = 0x8DF8;
    public static final int GL_NUM_SHADER_BINARY_FORMATS = 0x8DF9;
    public static final int GL_LOW_FLOAT = 0x8DF0;
    public static final int GL_MEDIUM_FLOAT = 0x8DF1;
    public static final int GL_HIGH_FLOAT = 0x8DF2;
    public static final int GL_LOW_INT = 0x8DF3;
    public static final int GL_MEDIUM_INT = 0x8DF4;
    public static final int GL_HIGH_INT = 0x8DF5;
    public static final int GL_FRAMEBUFFER = 0x8D40;
    public static final int GL_RENDERBUFFER = 0x8D41;
    public static final int GL_RGBA4 = 0x8056;
    public static final int GL_RGB5_A1 = 0x8057;
    public static final int GL_RGB565 = 0x8D62;
    public static final int GL_DEPTH_COMPONENT16 = 0x81A5;
    public static final int GL_STENCIL_INDEX = 0x1901;
    public static final int GL_STENCIL_INDEX8 = 0x8D48;
    public static final int GL_RENDERBUFFER_WIDTH = 0x8D42;
    public static final int GL_RENDERBUFFER_HEIGHT = 0x8D43;
    public static final int GL_RENDERBUFFER_INTERNAL_FORMAT = 0x8D44;
    public static final int GL_RENDERBUFFER_RED_SIZE = 0x8D50;
    public static final int GL_RENDERBUFFER_GREEN_SIZE = 0x8D51;
    public static final int GL_RENDERBUFFER_BLUE_SIZE = 0x8D52;
    public static final int GL_RENDERBUFFER_ALPHA_SIZE = 0x8D53;
    public static final int GL_RENDERBUFFER_DEPTH_SIZE = 0x8D54;
    public static final int GL_RENDERBUFFER_STENCIL_SIZE = 0x8D55;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE = 0x8CD0;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME = 0x8CD1;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL = 0x8CD2;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE = 0x8CD3;
    public static final int GL_COLOR_ATTACHMENT0 = 0x8CE0;
    public static final int GL_DEPTH_ATTACHMENT = 0x8D00;
    public static final int GL_STENCIL_ATTACHMENT = 0x8D20;
    public static final int GL_NONE = 0;
    public static final int GL_FRAMEBUFFER_COMPLETE = 0x8CD5;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 0x8CD6;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 0x8CD7;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS = 0x8CD9;
    public static final int GL_FRAMEBUFFER_UNSUPPORTED = 0x8CDD;
    public static final int GL_FRAMEBUFFER_BINDING = 0x8CA6;
    public static final int GL_RENDERBUFFER_BINDING = 0x8CA7;
    public static final int GL_MAX_RENDERBUFFER_SIZE = 0x84E8;
    public static final int GL_INVALID_FRAMEBUFFER_OPERATION = 0x0506;
    public static final int GL_VERTEX_PROGRAM_POINT_SIZE = 0x8642;
    // open gl3

    public static final int GL_READ_BUFFER = 0x0C02;
    public static final int GL_UNPACK_ROW_LENGTH = 0x0CF2;
    public static final int GL_UNPACK_SKIP_ROWS = 0x0CF3;
    public static final int GL_UNPACK_SKIP_PIXELS = 0x0CF4;
    public static final int GL_PACK_ROW_LENGTH = 0x0D02;
    public static final int GL_PACK_SKIP_ROWS = 0x0D03;
    public static final int GL_PACK_SKIP_PIXELS = 0x0D04;
    public static final int GL_COLOR = 0x1800;
    public static final int GL_DEPTH = 0x1801;
    public static final int GL_STENCIL = 0x1802;
    public static final int GL_RED = 0x1903;
    public static final int GL_RGB8 = 0x8051;
    public static final int GL_RGBA8 = 0x8058;
    public static final int GL_RGB10_A2 = 0x8059;
    public static final int GL_TEXTURE_BINDING_3D = 0x806A;
    public static final int GL_UNPACK_SKIP_IMAGES = 0x806D;
    public static final int GL_UNPACK_IMAGE_HEIGHT = 0x806E;
    public static final int GL_TEXTURE_3D = 0x806F;
    public static final int GL_TEXTURE_WRAP_R = 0x8072;
    public static final int GL_MAX_3D_TEXTURE_SIZE = 0x8073;
    public static final int GL_UNSIGNED_INT_2_10_10_10_REV = 0x8368;
    public static final int GL_MAX_ELEMENTS_VERTICES = 0x80E8;
    public static final int GL_MAX_ELEMENTS_INDICES = 0x80E9;
    public static final int GL_TEXTURE_MIN_LOD = 0x813A;
    public static final int GL_TEXTURE_MAX_LOD = 0x813B;
    public static final int GL_TEXTURE_BASE_LEVEL = 0x813C;
    public static final int GL_TEXTURE_MAX_LEVEL = 0x813D;
    public static final int GL_MIN = 0x8007;
    public static final int GL_MAX = 0x8008;
    public static final int GL_DEPTH_COMPONENT24 = 0x81A6;
    public static final int GL_MAX_TEXTURE_LOD_BIAS = 0x84FD;
    public static final int GL_TEXTURE_COMPARE_MODE = 0x884C;
    public static final int GL_TEXTURE_COMPARE_FUNC = 0x884D;
    public static final int GL_CURRENT_QUERY = 0x8865;
    public static final int GL_QUERY_RESULT = 0x8866;
    public static final int GL_QUERY_RESULT_AVAILABLE = 0x8867;
    public static final int GL_BUFFER_MAPPED = 0x88BC;
    public static final int GL_BUFFER_MAP_POINTER = 0x88BD;
    public static final int GL_STREAM_READ = 0x88E1;
    public static final int GL_STREAM_COPY = 0x88E2;
    public static final int GL_STATIC_READ = 0x88E5;
    public static final int GL_STATIC_COPY = 0x88E6;
    public static final int GL_DYNAMIC_READ = 0x88E9;
    public static final int GL_DYNAMIC_COPY = 0x88EA;
    public static final int GL_MAX_DRAW_BUFFERS = 0x8824;
    public static final int GL_DRAW_BUFFER0 = 0x8825;
    public static final int GL_DRAW_BUFFER1 = 0x8826;
    public static final int GL_DRAW_BUFFER2 = 0x8827;
    public static final int GL_DRAW_BUFFER3 = 0x8828;
    public static final int GL_DRAW_BUFFER4 = 0x8829;
    public static final int GL_DRAW_BUFFER5 = 0x882A;
    public static final int GL_DRAW_BUFFER6 = 0x882B;
    public static final int GL_DRAW_BUFFER7 = 0x882C;
    public static final int GL_DRAW_BUFFER8 = 0x882D;
    public static final int GL_DRAW_BUFFER9 = 0x882E;
    public static final int GL_DRAW_BUFFER10 = 0x882F;
    public static final int GL_DRAW_BUFFER11 = 0x8830;
    public static final int GL_DRAW_BUFFER12 = 0x8831;
    public static final int GL_DRAW_BUFFER13 = 0x8832;
    public static final int GL_DRAW_BUFFER14 = 0x8833;
    public static final int GL_DRAW_BUFFER15 = 0x8834;
    public static final int GL_MAX_FRAGMENT_UNIFORM_COMPONENTS = 0x8B49;
    public static final int GL_MAX_VERTEX_UNIFORM_COMPONENTS = 0x8B4A;
    public static final int GL_SAMPLER_3D = 0x8B5F;
    public static final int GL_SAMPLER_2D_SHADOW = 0x8B62;
    public static final int GL_FRAGMENT_SHADER_DERIVATIVE_HINT = 0x8B8B;
    public static final int GL_PIXEL_PACK_BUFFER = 0x88EB;
    public static final int GL_PIXEL_UNPACK_BUFFER = 0x88EC;
    public static final int GL_PIXEL_PACK_BUFFER_BINDING = 0x88ED;
    public static final int GL_PIXEL_UNPACK_BUFFER_BINDING = 0x88EF;
    public static final int GL_FLOAT_MAT2x3 = 0x8B65;
    public static final int GL_FLOAT_MAT2x4 = 0x8B66;
    public static final int GL_FLOAT_MAT3x2 = 0x8B67;
    public static final int GL_FLOAT_MAT3x4 = 0x8B68;
    public static final int GL_FLOAT_MAT4x2 = 0x8B69;
    public static final int GL_FLOAT_MAT4x3 = 0x8B6A;
    public static final int GL_SRGB = 0x8C40;
    public static final int GL_SRGB8 = 0x8C41;
    public static final int GL_SRGB8_ALPHA8 = 0x8C43;
    public static final int GL_COMPARE_REF_TO_TEXTURE = 0x884E;
    public static final int GL_MAJOR_VERSION = 0x821B;
    public static final int GL_MINOR_VERSION = 0x821C;
    public static final int GL_NUM_EXTENSIONS = 0x821D;
    public static final int GL_RGBA32F = 0x8814;
    public static final int GL_RGB32F = 0x8815;
    public static final int GL_RGBA16F = 0x881A;
    public static final int GL_RGB16F = 0x881B;
    public static final int GL_VERTEX_ATTRIB_ARRAY_INTEGER = 0x88FD;
    public static final int GL_MAX_ARRAY_TEXTURE_LAYERS = 0x88FF;
    public static final int GL_MIN_PROGRAM_TEXEL_OFFSET = 0x8904;
    public static final int GL_MAX_PROGRAM_TEXEL_OFFSET = 0x8905;
    public static final int GL_MAX_VARYING_COMPONENTS = 0x8B4B;
    public static final int GL_TEXTURE_2D_ARRAY = 0x8C1A;
    public static final int GL_TEXTURE_BINDING_2D_ARRAY = 0x8C1D;
    public static final int GL_R11F_G11F_B10F = 0x8C3A;
    public static final int GL_UNSIGNED_INT_10F_11F_11F_REV = 0x8C3B;
    public static final int GL_RGB9_E5 = 0x8C3D;
    public static final int GL_UNSIGNED_INT_5_9_9_9_REV = 0x8C3E;
    public static final int GL_TRANSFORM_FEEDBACK_VARYING_MAX_LENGTH = 0x8C76;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER_MODE = 0x8C7F;
    public static final int GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS = 0x8C80;
    public static final int GL_TRANSFORM_FEEDBACK_VARYINGS = 0x8C83;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER_START = 0x8C84;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER_SIZE = 0x8C85;
    public static final int GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN = 0x8C88;
    public static final int GL_RASTERIZER_DISCARD = 0x8C89;
    public static final int GL_MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS = 0x8C8A;
    public static final int GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS = 0x8C8B;
    public static final int GL_INTERLEAVED_ATTRIBS = 0x8C8C;
    public static final int GL_SEPARATE_ATTRIBS = 0x8C8D;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER = 0x8C8E;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER_BINDING = 0x8C8F;
    public static final int GL_RGBA32UI = 0x8D70;
    public static final int GL_RGB32UI = 0x8D71;
    public static final int GL_RGBA16UI = 0x8D76;
    public static final int GL_RGB16UI = 0x8D77;
    public static final int GL_RGBA8UI = 0x8D7C;
    public static final int GL_RGB8UI = 0x8D7D;
    public static final int GL_RGBA32I = 0x8D82;
    public static final int GL_RGB32I = 0x8D83;
    public static final int GL_RGBA16I = 0x8D88;
    public static final int GL_RGB16I = 0x8D89;
    public static final int GL_RGBA8I = 0x8D8E;
    public static final int GL_RGB8I = 0x8D8F;
    public static final int GL_RED_INTEGER = 0x8D94;
    public static final int GL_RGB_INTEGER = 0x8D98;
    public static final int GL_RGBA_INTEGER = 0x8D99;
    public static final int GL_SAMPLER_2D_ARRAY = 0x8DC1;
    public static final int GL_SAMPLER_2D_ARRAY_SHADOW = 0x8DC4;
    public static final int GL_SAMPLER_CUBE_SHADOW = 0x8DC5;
    public static final int GL_UNSIGNED_INT_VEC2 = 0x8DC6;
    public static final int GL_UNSIGNED_INT_VEC3 = 0x8DC7;
    public static final int GL_UNSIGNED_INT_VEC4 = 0x8DC8;
    public static final int GL_INT_SAMPLER_2D = 0x8DCA;
    public static final int GL_INT_SAMPLER_3D = 0x8DCB;
    public static final int GL_INT_SAMPLER_CUBE = 0x8DCC;
    public static final int GL_INT_SAMPLER_2D_ARRAY = 0x8DCF;
    public static final int GL_UNSIGNED_INT_SAMPLER_2D = 0x8DD2;
    public static final int GL_UNSIGNED_INT_SAMPLER_3D = 0x8DD3;
    public static final int GL_UNSIGNED_INT_SAMPLER_CUBE = 0x8DD4;
    public static final int GL_UNSIGNED_INT_SAMPLER_2D_ARRAY = 0x8DD7;
    public static final int GL_BUFFER_ACCESS_FLAGS = 0x911F;
    public static final int GL_BUFFER_MAP_LENGTH = 0x9120;
    public static final int GL_BUFFER_MAP_OFFSET = 0x9121;
    public static final int GL_DEPTH_COMPONENT32F = 0x8CAC;
    public static final int GL_DEPTH32F_STENCIL8 = 0x8CAD;
    public static final int GL_FLOAT_32_UNSIGNED_INT_24_8_REV = 0x8DAD;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING = 0x8210;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_COMPONENT_TYPE = 0x8211;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_RED_SIZE = 0x8212;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_GREEN_SIZE = 0x8213;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_BLUE_SIZE = 0x8214;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_ALPHA_SIZE = 0x8215;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_DEPTH_SIZE = 0x8216;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_STENCIL_SIZE = 0x8217;
    public static final int GL_FRAMEBUFFER_DEFAULT = 0x8218;
    public static final int GL_FRAMEBUFFER_UNDEFINED = 0x8219;
    public static final int GL_DEPTH_STENCIL_ATTACHMENT = 0x821A;
    public static final int GL_DEPTH_STENCIL = 0x84F9;
    public static final int GL_UNSIGNED_INT_24_8 = 0x84FA;
    public static final int GL_DEPTH24_STENCIL8 = 0x88F0;
    public static final int GL_UNSIGNED_NORMALIZED = 0x8C17;
    public static final int GL_DRAW_FRAMEBUFFER_BINDING = GL_FRAMEBUFFER_BINDING;
    public static final int GL_READ_FRAMEBUFFER = 0x8CA8;
    public static final int GL_DRAW_FRAMEBUFFER = 0x8CA9;
    public static final int GL_READ_FRAMEBUFFER_BINDING = 0x8CAA;
    public static final int GL_RENDERBUFFER_SAMPLES = 0x8CAB;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LAYER = 0x8CD4;
    public static final int GL_MAX_COLOR_ATTACHMENTS = 0x8CDF;
    public static final int GL_COLOR_ATTACHMENT1 = 0x8CE1;
    public static final int GL_COLOR_ATTACHMENT2 = 0x8CE2;
    public static final int GL_COLOR_ATTACHMENT3 = 0x8CE3;
    public static final int GL_COLOR_ATTACHMENT4 = 0x8CE4;
    public static final int GL_COLOR_ATTACHMENT5 = 0x8CE5;
    public static final int GL_COLOR_ATTACHMENT6 = 0x8CE6;
    public static final int GL_COLOR_ATTACHMENT7 = 0x8CE7;
    public static final int GL_COLOR_ATTACHMENT8 = 0x8CE8;
    public static final int GL_COLOR_ATTACHMENT9 = 0x8CE9;
    public static final int GL_COLOR_ATTACHMENT10 = 0x8CEA;
    public static final int GL_COLOR_ATTACHMENT11 = 0x8CEB;
    public static final int GL_COLOR_ATTACHMENT12 = 0x8CEC;
    public static final int GL_COLOR_ATTACHMENT13 = 0x8CED;
    public static final int GL_COLOR_ATTACHMENT14 = 0x8CEE;
    public static final int GL_COLOR_ATTACHMENT15 = 0x8CEF;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE = 0x8D56;
    public static final int GL_MAX_SAMPLES = 0x8D57;
    public static final int GL_HALF_FLOAT = 0x140B;
    public static final int GL_MAP_READ_BIT = 0x0001;
    public static final int GL_MAP_WRITE_BIT = 0x0002;
    public static final int GL_MAP_INVALIDATE_RANGE_BIT = 0x0004;
    public static final int GL_MAP_INVALIDATE_BUFFER_BIT = 0x0008;
    public static final int GL_MAP_FLUSH_EXPLICIT_BIT = 0x0010;
    public static final int GL_MAP_UNSYNCHRONIZED_BIT = 0x0020;
    public static final int GL_RG = 0x8227;
    public static final int GL_RG_INTEGER = 0x8228;
    public static final int GL_R8 = 0x8229;
    public static final int GL_RG8 = 0x822B;
    public static final int GL_R16F = 0x822D;
    public static final int GL_R32F = 0x822E;
    public static final int GL_RG16F = 0x822F;
    public static final int GL_RG32F = 0x8230;
    public static final int GL_R8I = 0x8231;
    public static final int GL_R8UI = 0x8232;
    public static final int GL_R16I = 0x8233;
    public static final int GL_R16UI = 0x8234;
    public static final int GL_R32I = 0x8235;
    public static final int GL_R32UI = 0x8236;
    public static final int GL_RG8I = 0x8237;
    public static final int GL_RG8UI = 0x8238;
    public static final int GL_RG16I = 0x8239;
    public static final int GL_RG16UI = 0x823A;
    public static final int GL_RG32I = 0x823B;
    public static final int GL_RG32UI = 0x823C;
    public static final int GL_VERTEX_ARRAY_BINDING = 0x85B5;
    public static final int GL_R8_SNORM = 0x8F94;
    public static final int GL_RG8_SNORM = 0x8F95;
    public static final int GL_RGB8_SNORM = 0x8F96;
    public static final int GL_RGBA8_SNORM = 0x8F97;
    public static final int GL_SIGNED_NORMALIZED = 0x8F9C;
    public static final int GL_PRIMITIVE_RESTART_FIXED_INDEX = 0x8D69;
    public static final int GL_COPY_READ_BUFFER = 0x8F36;
    public static final int GL_COPY_WRITE_BUFFER = 0x8F37;
    public static final int GL_COPY_READ_BUFFER_BINDING = GL_COPY_READ_BUFFER;
    public static final int GL_COPY_WRITE_BUFFER_BINDING = GL_COPY_WRITE_BUFFER;
    public static final int GL_UNIFORM_BUFFER = 0x8A11;
    public static final int GL_UNIFORM_BUFFER_BINDING = 0x8A28;
    public static final int GL_UNIFORM_BUFFER_START = 0x8A29;
    public static final int GL_UNIFORM_BUFFER_SIZE = 0x8A2A;
    public static final int GL_MAX_VERTEX_UNIFORM_BLOCKS = 0x8A2B;
    public static final int GL_MAX_FRAGMENT_UNIFORM_BLOCKS = 0x8A2D;
    public static final int GL_MAX_COMBINED_UNIFORM_BLOCKS = 0x8A2E;
    public static final int GL_MAX_UNIFORM_BUFFER_BINDINGS = 0x8A2F;
    public static final int GL_MAX_UNIFORM_BLOCK_SIZE = 0x8A30;
    public static final int GL_MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS = 0x8A31;
    public static final int GL_MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS = 0x8A33;
    public static final int GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT = 0x8A34;
    public static final int GL_ACTIVE_UNIFORM_BLOCK_MAX_NAME_LENGTH = 0x8A35;
    public static final int GL_ACTIVE_UNIFORM_BLOCKS = 0x8A36;
    public static final int GL_UNIFORM_TYPE = 0x8A37;
    public static final int GL_UNIFORM_SIZE = 0x8A38;
    public static final int GL_UNIFORM_NAME_LENGTH = 0x8A39;
    public static final int GL_UNIFORM_BLOCK_INDEX = 0x8A3A;
    public static final int GL_UNIFORM_OFFSET = 0x8A3B;
    public static final int GL_UNIFORM_ARRAY_STRIDE = 0x8A3C;
    public static final int GL_UNIFORM_MATRIX_STRIDE = 0x8A3D;
    public static final int GL_UNIFORM_IS_ROW_MAJOR = 0x8A3E;
    public static final int GL_UNIFORM_BLOCK_BINDING = 0x8A3F;
    public static final int GL_UNIFORM_BLOCK_DATA_SIZE = 0x8A40;
    public static final int GL_UNIFORM_BLOCK_NAME_LENGTH = 0x8A41;
    public static final int GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS = 0x8A42;
    public static final int GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES = 0x8A43;
    public static final int GL_UNIFORM_BLOCK_REFERENCED_BY_VERTEX_SHADER = 0x8A44;
    public static final int GL_UNIFORM_BLOCK_REFERENCED_BY_FRAGMENT_SHADER = 0x8A46;
    // GL_INVALID_INDEX is defined as 0xFFFFFFFFu in C.
    public static final int GL_INVALID_INDEX = -1;
    public static final int GL_MAX_VERTEX_OUTPUT_COMPONENTS = 0x9122;
    public static final int GL_MAX_FRAGMENT_INPUT_COMPONENTS = 0x9125;
    public static final int GL_MAX_SERVER_WAIT_TIMEOUT = 0x9111;
    public static final int GL_OBJECT_TYPE = 0x9112;
    public static final int GL_SYNC_CONDITION = 0x9113;
    public static final int GL_SYNC_STATUS = 0x9114;
    public static final int GL_SYNC_FLAGS = 0x9115;
    public static final int GL_SYNC_FENCE = 0x9116;
    public static final int GL_SYNC_GPU_COMMANDS_COMPLETE = 0x9117;
    public static final int GL_UNSIGNALED = 0x9118;
    public static final int GL_SIGNALED = 0x9119;
    public static final int GL_ALREADY_SIGNALED = 0x911A;
    public static final int GL_TIMEOUT_EXPIRED = 0x911B;
    public static final int GL_CONDITION_SATISFIED = 0x911C;
    public static final int GL_WAIT_FAILED = 0x911D;
    public static final int GL_SYNC_FLUSH_COMMANDS_BIT = 0x00000001;
    public static final int GL_VERTEX_ATTRIB_ARRAY_DIVISOR = 0x88FE;
    public static final int GL_ANY_SAMPLES_PASSED = 0x8C2F;
    public static final int GL_ANY_SAMPLES_PASSED_CONSERVATIVE = 0x8D6A;
    public static final int GL_SAMPLER_BINDING = 0x8919;
    public static final int GL_RGB10_A2UI = 0x906F;
    public static final int GL_TEXTURE_SWIZZLE_R = 0x8E42;
    public static final int GL_TEXTURE_SWIZZLE_G = 0x8E43;
    public static final int GL_TEXTURE_SWIZZLE_B = 0x8E44;
    public static final int GL_TEXTURE_SWIZZLE_A = 0x8E45;
    public static final int GL_GREEN = 0x1904;
    public static final int GL_BLUE = 0x1905;
    public static final int GL_INT_2_10_10_10_REV = 0x8D9F;
    public static final int GL_TRANSFORM_FEEDBACK = 0x8E22;
    public static final int GL_TRANSFORM_FEEDBACK_PAUSED = 0x8E23;
    public static final int GL_TRANSFORM_FEEDBACK_ACTIVE = 0x8E24;
    public static final int GL_TRANSFORM_FEEDBACK_BINDING = 0x8E25;
    public static final int GL_PROGRAM_BINARY_RETRIEVABLE_HINT = 0x8257;
    public static final int GL_PROGRAM_BINARY_LENGTH = 0x8741;
    public static final int GL_NUM_PROGRAM_BINARY_FORMATS = 0x87FE;
    public static final int GL_PROGRAM_BINARY_FORMATS = 0x87FF;
    public static final int GL_TEXTURE_IMMUTABLE_FORMAT = 0x912F;
    public static final int GL_MAX_ELEMENT_INDEX = 0x8D6B;
    public static final int GL_NUM_SAMPLE_COUNTS = 0x9380;
    public static final int GL_TEXTURE_IMMUTABLE_LEVELS = 0x82DF;
    // GL_TIMEOUT_IGNORED is defined as 0xFFFFFFFFFFFFFFFFull in C.
    public final long GL_TIMEOUT_IGNORED = -1;
    // Extensions
    public static final int GL_COVERAGE_BUFFER_BIT_NV = 0x8000;
    // temp data
    final int[] ints = new int[1], ints2 = new int[1], ints3 = new int[1];
    final byte[] buffer = new byte[512];

    public void glActiveTexture(int unit);

    public void glBindAttribLocation(int program, int index, String name);

    public void glBindBuffer(int target, int buffer);

    public void glBindFramebuffer(int target, int framebuffer);

    public void glBindRenderbuffer(int target, int renderbuffer);

    public void glBindTexture(int target, int texture);

    public void glBindVertexArray(int v);

    public void glBlendColor(float red, float green, float blue, float alpha);

    public void glBlendEquation(int mode);

    public void glBlendEquationSeparate(int modeRGB, int modeAlpha);

    public void glBufferData(int target, int size, Buffer data, int usage);

    public void glBufferSubData(int target, int offset, int size, Buffer data);

    public int glCheckFramebufferStatus(int target);

    public void glClearDepth(float depth);

    public void glClearStencil(int s);

    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha);

    public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height, int border);

    public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height);

    public void setCullFace(int mode);

    public void glDeleteBuffers(int n, IntBuffer buffers);

    public void glDeleteBuffer(int buffer);

    public void glDeleteFramebuffers(int n, IntBuffer framebuffers);

    public void glDeleteFramebuffer(int framebuffer);

    public void glDeleteProgram(int program);

    public void glDeleteRenderbuffers(int n, IntBuffer renderbuffers);

    public void glDeleteRenderbuffer(int renderbuffer);

    public void glDeleteTexture(int texture);

    public void glDepthFunc(int func);

    public void glDepthRangef(float near, float far);

    public void glDetachShader(int program, int shader);

    public void glDisableVertexAttribArray(int index);

    public void glDrawArrays(int mode, int first, int count);

    public void glDrawElements(int mode, int count, int type, Buffer indices);

    public void glDrawElements(int mode, int count, int type, int indices);

    public void glEnableVertexAttribArray(int index);

    public void glFinish();

    public void glFlush();

    public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer);

    public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level);

    public void glFrontFace(int mode);

    public void glGenBuffers(int n, IntBuffer buffers);

    public int glGenBuffer();

    public void glGenerateMipmap(int target);

    public void glGenFramebuffers(int n, IntBuffer framebuffers);

    public int glGenFramebuffer();

    public void glGenRenderbuffers(int n, IntBuffer renderbuffers);

    public int glGenRenderbuffer();

    public int glGenTexture();

    public String glGetActiveAttrib(int program, int index, IntBuffer size, Buffer type);

    public String glGetActiveUniform(int program, int index, IntBuffer size, Buffer type);

    public void glGetAttachedShaders(int program, int maxcount, Buffer count, IntBuffer shaders);

    public int glGetAttribLocation(int program, String name);

    public void glGetBooleanv(int pname, Buffer params);

    public void glGetBufferParameteriv(int target, int pname, IntBuffer params);

    public int glGetError();

    public void glGetFloatv(int pname, FloatBuffer params);

    public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params);

    public void glGetIntegerv(int pname, IntBuffer params);

    public int glGetIntegerv(int pname);

    public void glGetProgramiv(int program, int pname, IntBuffer params);

    public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params);

    public void glGetShaderiv(int shader, int pname, IntBuffer params);

    public String glGetShaderInfoLog(int shader);

    public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, IntBuffer range, IntBuffer precision);

    public String glGetString(int name);

    public void glGetTexParameterfv(int target, int pname, FloatBuffer params);

    public void glGetTexParameteriv(int target, int pname, IntBuffer params);

    public void glGetUniformfv(int program, int location, FloatBuffer params);

    public void glGetUniformiv(int program, int location, IntBuffer params);

    public int glGetUniformLocation(int program, String name);

    public void glGetVertexAttribfv(int index, int pname, FloatBuffer params);

    public void glGetVertexAttribiv(int index, int pname, IntBuffer params);

    public void glHint(int target, int mode);

    public void glLineWidth(float width);

    public void glPixelStorei(int pname, int param);

    public void glPolygonOffset(float factor, float units);

    public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels);

    public void glReleaseShaderCompiler();

    public void glRenderbufferStorage(int target, int internalformat, int width, int height);

    public void glSampleCoverage(float value, boolean invert);

    public void glScissor(int x, int y, int width, int height);

    public void glShaderBinary(int n, IntBuffer shaders, int binaryformat, Buffer binary, int length);

    public void glStencilFunc(int func, int ref, int mask);

    public void glStencilFuncSeparate(int face, int func, int ref, int mask);

    public void glStencilMask(int mask);

    public void glStencilMaskSeparate(int face, int mask);

    public void glStencilOp(int fail, int zfail, int zpass);

    public void glStencilOpSeparate(int face, int fail, int zfail, int zpass);

    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels);

    public void glTexParameterf(int target, int pname, float param);

    public void glTexParameterfv(int target, int pname, FloatBuffer params);

    public void glTexParameteri(int target, int pname, int param);

    public void glTexParameteriv(int target, int pname, IntBuffer params);

    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels);

    public void glUniform1f(int location, float x);

    public void glUniform1fv(int location, int count, FloatBuffer v);

    public void glUniform1fv(int location, int count, float[] v, int offset);

    public void glUniform1i(int location, int x);

    public void glUniform1iv(int location, int count, IntBuffer v);

    public void glUniform1iv(int location, int count, int[] v, int offset);

    public void glUniform2f(int location, float x, float y);

    public void glUniform2fv(int location, int count, FloatBuffer v);

    public void glUniform2fv(int location, int count, float[] v, int offset);

    public void glUniform2i(int location, int x, int y);

    public void glUniform2iv(int location, int count, IntBuffer v);

    public void glUniform2iv(int location, int count, int[] v, int offset);

    public void glUniform3f(int location, float x, float y, float z);

    public void glUniform3fv(int location, int count, FloatBuffer v);

    public void glUniform3fv(int location, int count, float[] v, int offset);

    public void glUniform3i(int location, int x, int y, int z);

    public void glUniform3iv(int location, int count, IntBuffer v);

    public void glUniform3iv(int location, int count, int[] v, int offset);

    public void glUniform4f(int location, float x, float y, float z, float w);

    public void glUniform4fv(int location, int count, FloatBuffer v);

    public void glUniform4fv(int location, int count, float[] v, int offset);

    public void glUniform4i(int location, int x, int y, int z, int w);

    public void glUniform4iv(int location, int count, IntBuffer v);

    public void glUniform4iv(int location, int count, int[] v, int offset);

    public void glUniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer value);

    public void glUniformMatrix2fv(int location, int count, boolean transpose, float[] value, int offset);

    public void glUniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer value);

    public void glUniformMatrix3fv(int location, int count, boolean transpose, float[] value, int offset);

    public void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer value);

    public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int offset);

    public void glUseProgram(int program);

    public void glValidateProgram(int program);

    public void glVertexAttrib1f(int indx, float x);

    public void glVertexAttrib1fv(int indx, FloatBuffer values);

    public void glVertexAttrib2f(int indx, float x, float y);

    public void glVertexAttrib2fv(int indx, FloatBuffer values);

    public void glVertexAttrib3f(int indx, float x, float y, float z);

    public void glVertexAttrib3fv(int indx, FloatBuffer values);

    public void glVertexAttrib4f(int indx, float x, float y, float z, float w);

    public void glVertexAttrib4fv(int indx, FloatBuffer values);

    public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, Buffer ptr);

    public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int ptr);

    public void glDrawRangeElements(int mode, int start, int end, int count, int type, java.nio.Buffer indices);

    public void glDrawRangeElements(int mode, int start, int end, int count, int type, int offset);

    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, java.nio.Buffer pixels);

    public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, int offset);

    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, java.nio.Buffer pixels);

    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, int offset);

    public void glCopyTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width, int height);

    public void glGenQueries(int n, int[] ids, int offset);

    public void glGenQueries(int n, java.nio.IntBuffer ids);

    public void glDeleteQueries(int n, int[] ids, int offset);

    public void glDeleteQueries(int n, java.nio.IntBuffer ids);

    public boolean glIsQuery(int id);

    public void glBeginQuery(int target, int id);

    public void glEndQuery(int target);

    public void glGetQueryiv(int target, int pname, int[] params, int offset);

    public void glGetQueryiv(int target, int pname, java.nio.IntBuffer params);

    public void glGetQueryObjectuiv(int id, int pname, int[] params, int offset);

    public void glGetQueryObjectuiv(int id, int pname, java.nio.IntBuffer params);

    public boolean glUnmapBuffer(int target);

    public java.nio.Buffer glGetBufferPointerv(int target, int pname);

    public void glDrawBuffers(int n, int[] bufs, int offset);

    public void glDrawBuffers(int n, java.nio.IntBuffer bufs);

    public void glReadBuffer(int func);

    public void glUniformMatrix2x3fv(int location, int count, boolean transpose, float[] value, int offset);

    public void glUniformMatrix2x3fv(int location, int count, boolean transpose, java.nio.FloatBuffer value);

    public void glUniformMatrix3x2fv(int location, int count, boolean transpose, float[] value, int offset);

    public void glUniformMatrix3x2fv(int location, int count, boolean transpose, java.nio.FloatBuffer value);

    public void glUniformMatrix2x4fv(int location, int count, boolean transpose, float[] value, int offset);

    public void glUniformMatrix2x4fv(int location, int count, boolean transpose, java.nio.FloatBuffer value);

    public void glUniformMatrix4x2fv(int location, int count, boolean transpose, float[] value, int offset);

    public void glUniformMatrix4x2fv(int location, int count, boolean transpose, java.nio.FloatBuffer value);

    public void glUniformMatrix3x4fv(int location, int count, boolean transpose, float[] value, int offset);

    public void glUniformMatrix3x4fv(int location, int count, boolean transpose, java.nio.FloatBuffer value);

    public void glUniformMatrix4x3fv(int location, int count, boolean transpose, float[] value, int offset);

    public void glUniformMatrix4x3fv(int location, int count, boolean transpose, java.nio.FloatBuffer value);

    public void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter);

    public void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width, int height);

    public void glFramebufferTextureLayer(int target, int attachment, int texture, int level, int layer);

    public java.nio.Buffer glMapBufferRange(int target, int offset, int length, int access);

    public void glFlushMappedBufferRange(int target, int offset, int length);

    public void glGetIntegeri_v(int target, int index, int[] data, int offset);

    public void glGetIntegeri_v(int target, int index, java.nio.IntBuffer data);

    public void glBeginTransformFeedback(int primitiveMode);

    public void glEndTransformFeedback();

    public void glBindBufferRange(int target, int index, int buffer, int offset, int size);

    public void glBindBufferBase(int target, int index, int buffer);

    public void glTransformFeedbackVaryings(int program, String[] varyings, int bufferMode);

    public void glGetTransformFeedbackVarying(int program, int index, int bufsize, int[] length, int lengthOffset, int[] size, int sizeOffset, int[] type, int typeOffset, byte[] name, int nameOffset);

    public String glGetTransformFeedbackVarying(int program, int index, int[] size, int sizeOffset, int[] type, int typeOffset);

    public String glGetTransformFeedbackVarying(int program, int index, java.nio.IntBuffer size, java.nio.IntBuffer type);

    public void glVertexAttribIPointer(int index, int size, int type, int stride, int offset);

    public void glGetVertexAttribIiv(int index, int pname, int[] params, int offset);

    public void glGetVertexAttribIiv(int index, int pname, java.nio.IntBuffer params);

    public void glGetVertexAttribIuiv(int index, int pname, int[] params, int offset);

    public void glGetVertexAttribIuiv(int index, int pname, java.nio.IntBuffer params);

    public void glVertexAttribI4i(int index, int x, int y, int z, int w);

    public void glVertexAttribI4ui(int index, int x, int y, int z, int w);

    public void glVertexAttribI4iv(int index, int[] v, int offset);

    public void glVertexAttribI4iv(int index, java.nio.IntBuffer v);

    public void glVertexAttribI4uiv(int index, int[] v, int offset);

    public void glVertexAttribI4uiv(int index, java.nio.IntBuffer v);

    public void glGetUniformuiv(int program, int location, int[] params, int offset);

    public void glGetUniformuiv(int program, int location, java.nio.IntBuffer params);

    public int glGetFragDataLocation(int program, String name);

    public void glUniform1ui(int location, int v0);

    public void glUniform2ui(int location, int v0, int v1);

    public void glUniform3ui(int location, int v0, int v1, int v2);

    public void glUniform4ui(int location, int v0, int v1, int v2, int v3);

    public void glUniform1uiv(int location, int count, int[] value, int offset);

    public void glUniform1uiv(int location, int count, java.nio.IntBuffer value);

    public void glUniform2uiv(int location, int count, int[] value, int offset);

    public void glUniform2uiv(int location, int count, java.nio.IntBuffer value);

    public void glUniform3uiv(int location, int count, int[] value, int offset);

    public void glUniform3uiv(int location, int count, java.nio.IntBuffer value);

    public void glUniform4uiv(int location, int count, int[] value, int offset);

    public void glUniform4uiv(int location, int count, java.nio.IntBuffer value);

    public void glClearBufferiv(int buffer, int drawbuffer, int[] value, int offset);

    public void glClearBufferiv(int buffer, int drawbuffer, java.nio.IntBuffer value);

    public void glClearBufferuiv(int buffer, int drawbuffer, int[] value, int offset);

    public void glClearBufferuiv(int buffer, int drawbuffer, java.nio.IntBuffer value);

    public void glClearBufferfv(int buffer, int drawbuffer, float[] value, int offset);

    public void glClearBufferfv(int buffer, int drawbuffer, java.nio.FloatBuffer value);

    public void glClearBufferfi(int buffer, int drawbuffer, float depth, int stencil);

    public String glGetStringi(int name, int index);

    public void glCopyBufferSubData(int readTarget, int writeTarget, int readOffset, int writeOffset, int size);

    public void glGetUniformIndices(int program, String[] uniformNames, int[] uniformIndices, int uniformIndicesOffset);

    public void glGetUniformIndices(int program, String[] uniformNames, java.nio.IntBuffer uniformIndices);

    public void glGetActiveUniformsiv(int program, int uniformCount, int[] uniformIndices, int uniformIndicesOffset, int pname, int[] params, int paramsOffset);

    public void glGetActiveUniformsiv(int program, int uniformCount, java.nio.IntBuffer uniformIndices, int pname, java.nio.IntBuffer params);

    public int glGetUniformBlockIndex(int program, String uniformBlockName);

    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, int[] params, int offset);

    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, java.nio.IntBuffer params);

    public void glGetActiveUniformBlockName(int program, int uniformBlockIndex, int bufSize, int[] length, int lengthOffset, byte[] uniformBlockName, int uniformBlockNameOffset);

    public void glGetActiveUniformBlockName(int program, int uniformBlockIndex, java.nio.Buffer length, java.nio.Buffer uniformBlockName);

    public String glGetActiveUniformBlockName(int program, int uniformBlockIndex);

    public void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding);

    public void glDrawArraysInstanced(int mode, int first, int count, int instanceCount);

    public void glDrawElementsInstanced(int mode, int count, int type, java.nio.Buffer indices, int instanceCount);

    public void glDrawElementsInstanced(int mode, int count, int type, int indicesOffset, int instanceCount);

    public long glFenceSync(int condition, int flags);

    public boolean glIsSync(long sync);

    public void glDeleteSync(long sync);

    public int glClientWaitSync(long sync, int flags, long timeout);

    public void glWaitSync(long sync, int flags, long timeout);

    public void glGetInteger64v(int pname, long[] params, int offset);

    public void glGetInteger64v(int pname, java.nio.LongBuffer params);

    public void glGetSynciv(long sync, int pname, int bufSize, int[] length, int lengthOffset, int[] values, int valuesOffset);

    public void glGetSynciv(long sync, int pname, int bufSize, java.nio.IntBuffer length, java.nio.IntBuffer values);

    public void glGetInteger64i_v(int target, int index, long[] data, int offset);

    public void glGetInteger64i_v(int target, int index, java.nio.LongBuffer data);

    public void glGetBufferParameteri64v(int target, int pname, long[] params, int offset);

    public void glGetBufferParameteri64v(int target, int pname, java.nio.LongBuffer params);

    public void glGenSamplers(int count, int[] samplers, int offset);

    public void glGenSamplers(int count, java.nio.IntBuffer samplers);

    public void glDeleteSamplers(int count, int[] samplers, int offset);

    public void glDeleteSamplers(int count, java.nio.IntBuffer samplers);

    public boolean glIsSampler(int sampler);

    public void glBindSampler(int unit, int sampler);

    public void glSamplerParameteri(int sampler, int pname, int param);

    public void glSamplerParameteriv(int sampler, int pname, int[] param, int offset);

    public void glSamplerParameteriv(int sampler, int pname, java.nio.IntBuffer param);

    public void glSamplerParameterf(int sampler, int pname, float param);

    public void glSamplerParameterfv(int sampler, int pname, float[] param, int offset);

    public void glSamplerParameterfv(int sampler, int pname, java.nio.FloatBuffer param);

    public void glGetSamplerParameteriv(int sampler, int pname, int[] params, int offset);

    public void glGetSamplerParameteriv(int sampler, int pname, java.nio.IntBuffer params);

    public void glGetSamplerParameterfv(int sampler, int pname, float[] params, int offset);

    public void glGetSamplerParameterfv(int sampler, int pname, java.nio.FloatBuffer params);

    public void glVertexAttribDivisor(int index, int divisor);

    public void glBindTransformFeedback(int target, int id);

    public void glDeleteTransformFeedbacks(int n, int[] ids, int offset);

    public void glDeleteTransformFeedbacks(int n, java.nio.IntBuffer ids);

    public void glGenTransformFeedbacks(int n, int[] ids, int offset);

    public void glGenTransformFeedbacks(int n, java.nio.IntBuffer ids);

    public boolean glIsTransformFeedback(int id);

    public void glPauseTransformFeedback();

    public void glResumeTransformFeedback();

    public void glGetProgramBinary(int program, int bufSize, int[] length, int lengthOffset, int[] binaryFormat, int binaryFormatOffset, java.nio.Buffer binary);

    public void glGetProgramBinary(int program, int bufSize, java.nio.IntBuffer length, java.nio.IntBuffer binaryFormat, java.nio.Buffer binary);

    public void glProgramBinary(int program, int binaryFormat, java.nio.Buffer binary, int length);

    public void glProgramParameteri(int program, int pname, int value);

    public void glInvalidateFramebuffer(int target, int numAttachments, int[] attachments, int offset);

    public void glInvalidateFramebuffer(int target, int numAttachments, java.nio.IntBuffer attachments);

    public void glInvalidateSubFramebuffer(int target, int numAttachments, int[] attachments, int offset, int x, int y, int width, int height);

    public void glInvalidateSubFramebuffer(int target, int numAttachments, java.nio.IntBuffer attachments, int x, int y, int width, int height);

    public void glTexStorage2D(int target, int levels, int internalformat, int width, int height);

    public void glTexStorage3D(int target, int levels, int internalformat, int width, int height, int depth);

    public void glGetInternalformativ(int target, int internalformat, int pname, int bufSize, int[] params, int offset);

    public void glGetInternalformativ(int target, int internalformat, int pname, int bufSize, java.nio.IntBuffer params);

    public boolean glIsFramebuffer(int handler);

    public boolean glIsTexture(int handler);

    // extra function
    public void glClearColorMask(int mask, float red, float green, float blue, float alpha);

    public void glViewport(int x, int y, int width, int height);

    public boolean supportsExtension(String extension);

    public boolean supportsRenderer(String renderer);

    public String glVersion();

    public float getMaxAnisotropicFilterLevel();

    public int getMaxTextureSize();

    public int getMaxTextureUnit();

    public int[] compileShaderProgram(String source, String prefix) throws IllegalArgumentException;

    public boolean validShaderProgram(final int[] handlers);

    public void destroyShaderProgram(final int[] handlers);

    public int[] genMesh(final int max_v_data, final boolean v_static, final int max_i_data, final boolean i_static);

    public boolean validMesh(final int[] handlers);

    public void destroyMesh(final int[] outHandlers);

    public boolean capabilitySwitch(final boolean enable, final int cap);

    public boolean setDepthMask(final boolean depthMask);

    public boolean setDepthTest(final int depthFunction, final float depthRangeNear, final float depthRangeFar);

    public boolean setBlending(final boolean enabled, final int sFactor, final int dFactor);

    public void clear();

    public String getLog();

}
