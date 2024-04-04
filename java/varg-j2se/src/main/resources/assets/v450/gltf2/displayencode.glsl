#define INCLUDED_DISPLAYENCODE

const float m1 = 0.159301;   //2610.0/16384.0 * 0.25;
const float m2 = 78.84375;   //2523.0/4096.0 * 128.0;
const float c1 = 0.8359375;  //3424.0/4096.0;
const float c2 = 18.8515625; //2413.0/4096.0 * 32.0;
const float c3 = 18.6875;    //2392.0/4096.0 * 32.0;

const float oneByM2 = (1.0 / m2);
const float oneByM1 = (1.0 / m1);
const float MAX_PQ = 10000;
const float ONE_BY_MAX_PQ = 1.0 / 10000;
const vec3 GAMMA = vec3(2.4);
const float ONE_BY_GAMMA = 1.0 / 2.4;

vec3 luminance(in vec3 rgb) {
    return rgb * uniforms.displayEncoding.rgb;
}

float maxLuma(in vec3 rgb) {
    return rgb.r * uniforms.displayEncoding.r + rgb.g * uniforms.displayEncoding.g + rgb.b * uniforms.displayEncoding.b;
}
/**
 * Applies displayencoding on the rgb value using luminance
 * @param rgb The rgb value to apply displayencoding to
 * @param whitePoint The max luminance in the scene
 */
vec3 displayencode_reinhard_extended(in vec3 rgb, in float whitePoint) {
    float luminanceIn = maxLuma(rgb);
    float luminanceOut = (luminanceIn * (1.0 + (luminanceIn / (whitePoint * whitePoint)))) / (1.0 + luminanceIn);
    float factor = luminanceOut / whitePoint;
    return (rgb * factor);
}

/**
 * Applies the rec.2390 OOTF - this normalizes linear light to display light
 * Input color must be in range [0.0 - 1.0]
 * Output is display light
 */
vec3 BT_2390_OOTF(in vec3 color, in float rangeExtension, in vec3 gamma) {
    vec3 nonlinearColor = max(vec3(0), vec3(1.099 * pow(rangeExtension * color, vec3(0.45))) - vec3(0.099));
    return pow(nonlinearColor, gamma);
}
/**
 * The OETF maps display light into the non-linear PQ signal value
 * OETF = ((c1 + c2 * Y ^m1) / (1 + c3 * Y ^m1)) ^m2
 * Y = FD * ONE_BY_MAX_PQ
 * FD is the luminance of a displayed linear component (RD, GD, BD; YD; or ID)
 */
vec3 BT_2100_OETF(in vec3 color) {
    vec3 normalized = color * ONE_BY_MAX_PQ;
#ifdef OOTF_2390
    normalized = BT_2390_OOTF(normalized, 46, GAMMA);
#endif
    vec3 Ypow = pow(normalized, vec3(m1));
    return pow((c1 + c2 * Ypow) / (1 + c3 * Ypow), vec3(m2)); 
}

vec3 SRGB(in vec3 color) {
    return  pow(vec3(color * 1.055), vec3(ONE_BY_GAMMA)) - vec3(0.055);
}

vec3 BRDF_SCALE(in vec3 color, in float maxComponent) {
  float factor = min(1, (maxComponent / max(color.r, max(color.g, color.b))));
  return factor * color;
}

vec3 displayencode(in vec3 sceneColor, in float whitePoint) {
#ifdef COLORSPACE_PQ
    return BT_2100_OETF(BRDF_SCALE(sceneColor, whitePoint));
#else
#ifdef COLORSPACE_SRGB
    return SRGB(BRDF_SCALE(sceneColor, whitePoint) / whitePoint);
#else
    return sceneColor / whitePoint;
#endif
#endif


#ifdef RAW_HSL 
vec3 rgb2hsl(in vec3 c ){
  float h = 0.0;
    float s = 0.0;
    float l = 0.0;
    float r = c.r;
    float g = c.g;
    float b = c.b;
    float cMin = min( r, min( g, b ) );
    float cMax = max( r, max( g, b ) );

    l = ( cMax + cMin ) / 2.0;
    if ( cMax > cMin ) {
        float cDelta = cMax - cMin;
        
        //s = l < .05 ? cDelta / ( cMax + cMin ) : cDelta / ( 2.0 - ( cMax + cMin ) ); Original
        s = l < .0 ? cDelta / ( cMax + cMin ) : cDelta / ( 2.0 - ( cMax + cMin ) );
        
        if ( r == cMax ) {
            h = ( g - b ) / cDelta;
        } else if ( g == cMax ) {
            h = 2.0 + ( b - r ) / cDelta;
        } else {
            h = 4.0 + ( r - g ) / cDelta;
        }

        if ( h < 0.0) {
            h += 6.0;
        }
        h = h / 6.0;
    }
    return vec3( h, s, l );
}
#endif

#ifdef RAW_CHROMA
vec3 chroma(in vec3 rgb) {
    float total = rgb.r + rgb.g + rgb.b;
    return rgb / total;
}
#endif



}
