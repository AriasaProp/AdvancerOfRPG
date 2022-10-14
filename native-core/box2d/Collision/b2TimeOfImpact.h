#ifndef B2_TIME_OF_IMPACT_H
#define B2_TIME_OF_IMPACT_H

#include "../Common/b2Math.h"
#include "b2Distance.h"

/// Input parameters for b2TimeOfImpact
struct b2TOIInput {
    b2DistanceProxy proxyA;
    b2DistanceProxy proxyB;
    b2Sweep sweepA;
    b2Sweep sweepB;
    float32 tMax;        // defines sweep interval [0, tMax]
};

// Output parameters for b2TimeOfImpact.
struct b2TOIOutput {
    enum State {
        e_unknown,
        e_failed,
        e_overlapped,
        e_touching,
        e_separated
    };

    State state;
    float32 t;
};

void b2TimeOfImpact(b2TOIOutput *output, const b2TOIInput *input);

#endif
