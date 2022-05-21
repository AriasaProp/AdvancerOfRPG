package com.ariasaproject.advancerofrpg.graphics.g3d.particles.influencers;

import com.ariasaproject.advancerofrpg.graphics.g3d.particles.ParticleChannels;
import com.ariasaproject.advancerofrpg.graphics.g3d.particles.ParticleControllerComponent;

/**
 * It's an {@link Influencer} which controls the scale of the particles.
 *
 * @author Inferno
 */
public class ScaleInfluencer extends SimpleInfluencer {

    public ScaleInfluencer() {
        super();
        valueChannelDescriptor = ParticleChannels.Scale;
    }

    public ScaleInfluencer(ScaleInfluencer scaleInfluencer) {
        super(scaleInfluencer);
    }

    @Override
    public void activateParticles(int startIndex, int count) {
        if (value.isRelative()) {
            for (int i = startIndex * valueChannel.strideSize, a = startIndex * interpolationChannel.strideSize, c = i + count * valueChannel.strideSize; i < c; i += valueChannel.strideSize, a += interpolationChannel.strideSize) {
                float start = value.newLowValue() * controller.scale.x;
                float diff = value.newHighValue() * controller.scale.x;
                interpolationChannel.data[a + ParticleChannels.InterpolationStartOffset] = start;
                interpolationChannel.data[a + ParticleChannels.InterpolationDiffOffset] = diff;
                valueChannel.data[i] = start + diff * value.getScale(0);
            }
        } else {
            for (int i = startIndex * valueChannel.strideSize, a = startIndex * interpolationChannel.strideSize, c = i + count * valueChannel.strideSize; i < c; i += valueChannel.strideSize, a += interpolationChannel.strideSize) {
                float start = value.newLowValue() * controller.scale.x;
                float diff = value.newHighValue() * controller.scale.x - start;
                interpolationChannel.data[a + ParticleChannels.InterpolationStartOffset] = start;
                interpolationChannel.data[a + ParticleChannels.InterpolationDiffOffset] = diff;
                valueChannel.data[i] = start + diff * value.getScale(0);
            }
        }
    }

    @Override
    public ParticleControllerComponent copy() {
        return new ScaleInfluencer(this);
    }

}
