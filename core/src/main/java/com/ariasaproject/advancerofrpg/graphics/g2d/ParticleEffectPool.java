package com.ariasaproject.advancerofrpg.graphics.g2d;

import com.ariasaproject.advancerofrpg.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Pool;

public class ParticleEffectPool extends Pool<PooledEffect> {
    private final ParticleEffect effect;

    public ParticleEffectPool(ParticleEffect effect, int initialCapacity, int max) {
        super(initialCapacity, max);
        this.effect = effect;
    }

    public ParticleEffectPool(ParticleEffect effect, int initialCapacity, int max, boolean preFill) {
        super(initialCapacity, max, preFill);
        this.effect = effect;
    }

    @Override
    protected PooledEffect newObject() {
        PooledEffect pooledEffect = new PooledEffect(effect);
        pooledEffect.start();
        return pooledEffect;
    }

    @Override
    public void free(PooledEffect effect) {
        super.free(effect);
        effect.reset(false); // copy parameters exactly to avoid introducing error
        if (effect.xSizeScale != this.effect.xSizeScale || effect.ySizeScale != this.effect.ySizeScale || effect.motionScale != this.effect.motionScale) {
            Array<ParticleEmitter> emitters = effect.getEmitters();
            Array<ParticleEmitter> templateEmitters = this.effect.getEmitters();
            for (int i = 0; i < emitters.size; i++) {
                ParticleEmitter emitter = emitters.get(i);
                ParticleEmitter templateEmitter = templateEmitters.get(i);
                emitter.matchSize(templateEmitter);
                emitter.matchMotion(templateEmitter);
            }
            effect.xSizeScale = this.effect.xSizeScale;
            effect.ySizeScale = this.effect.ySizeScale;
            effect.motionScale = this.effect.motionScale;
        }
    }

    public class PooledEffect extends ParticleEffect {
        PooledEffect(ParticleEffect effect) {
            super(effect);
        }

        public void free() {
            ParticleEffectPool.this.free(this);
        }
    }
}
