package com.ariasaproject.advancerofrpg.graphics.g3d.particles.influencers;

import com.ariasaproject.advancerofrpg.assets.AssetContainer;
import com.ariasaproject.advancerofrpg.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.ariasaproject.advancerofrpg.graphics.g3d.particles.ParticleChannels;
import com.ariasaproject.advancerofrpg.graphics.g3d.particles.ResourceData;
import com.ariasaproject.advancerofrpg.graphics.g3d.particles.values.PointSpawnShapeValue;
import com.ariasaproject.advancerofrpg.graphics.g3d.particles.values.SpawnShapeValue;
import com.ariasaproject.advancerofrpg.utils.Json;
import com.ariasaproject.advancerofrpg.utils.JsonValue;

/**
 * It's an {@link Influencer} which controls where the particles will be
 * spawned.
 *
 * @author Inferno
 */
public class SpawnInfluencer extends Influencer {

	public SpawnShapeValue spawnShapeValue;
	FloatChannel positionChannel;
	FloatChannel rotationChannel;

	public SpawnInfluencer() {
		spawnShapeValue = new PointSpawnShapeValue();
	}

	public SpawnInfluencer(SpawnShapeValue spawnShapeValue) {
		this.spawnShapeValue = spawnShapeValue;
	}

	public SpawnInfluencer(SpawnInfluencer source) {
		spawnShapeValue = source.spawnShapeValue.copy();
	}

	@Override
	public void init() {
		spawnShapeValue.init();
	}

	@Override
	public void allocateChannels() {
		positionChannel = controller.particles.addChannel(ParticleChannels.Position);
		rotationChannel = controller.particles.addChannel(ParticleChannels.Rotation3D);
	}

	@Override
	public void start() {
		spawnShapeValue.start();
	}

	@Override
	public void activateParticles(int startIndex, int count) {
		for (int i = startIndex * positionChannel.strideSize, c = i
				+ count * positionChannel.strideSize; i < c; i += positionChannel.strideSize) {
			spawnShapeValue.spawn(TMP_V1, controller.emitter.percent);
			TMP_V1.mul(controller.transform);
			positionChannel.data[i + ParticleChannels.XOffset] = TMP_V1.x;
			positionChannel.data[i + ParticleChannels.YOffset] = TMP_V1.y;
			positionChannel.data[i + ParticleChannels.ZOffset] = TMP_V1.z;
		}
		for (int i = startIndex * rotationChannel.strideSize, c = i
				+ count * rotationChannel.strideSize; i < c; i += rotationChannel.strideSize) {
			controller.transform.getRotation(TMP_Q, true);
			rotationChannel.data[i + ParticleChannels.XOffset] = TMP_Q.x;
			rotationChannel.data[i + ParticleChannels.YOffset] = TMP_Q.y;
			rotationChannel.data[i + ParticleChannels.ZOffset] = TMP_Q.z;
			rotationChannel.data[i + ParticleChannels.WOffset] = TMP_Q.w;
		}
	}

	@Override
	public SpawnInfluencer copy() {
		return new SpawnInfluencer(this);
	}

	@Override
	public void write(Json json) {
		json.writeValue("spawnShape", spawnShapeValue, SpawnShapeValue.class);
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		spawnShapeValue = json.readValue("spawnShape", SpawnShapeValue.class, jsonData);
	}

	@Override
	public void save(AssetContainer manager, ResourceData data) {
		spawnShapeValue.save(manager, data);
	}

	@Override
	public void load(AssetContainer manager, ResourceData data) {
		spawnShapeValue.load(manager, data);
	}
}
