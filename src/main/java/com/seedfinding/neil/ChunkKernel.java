package com.seedfinding.neil;

import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.biome.Biomes;
import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.util.data.Pair;
import kaptainwutax.mcutils.version.MCVersion;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ChunkKernel implements Runnable {
	public static Predicate<Biome> SANDY = b -> b == Biomes.BEACH;
	public static Predicate<Biome> OTHER = b -> b != Biomes.BEACH;
	public static HashMap<Pair<Integer, Integer>, Predicate<Biome>> MASK = new HashMap<Pair<Integer, Integer>, Predicate<Biome>>() {{
		// test seed 1 on v1.12 (Chunk matched at (0,11) : /tp @p 0 ~ 176 )
//		put(new Pair<>(4,0),OTHER);
//		put(new Pair<>(5,0),SANDY);
//		put(new Pair<>(6,0),SANDY);
//		put(new Pair<>(7,0),SANDY);
//		put(new Pair<>(8,0),SANDY);
//
//		put(new Pair<>(4,1),OTHER);
//		put(new Pair<>(5,1),OTHER);
//		put(new Pair<>(6,1),SANDY);
//		put(new Pair<>(7,1),SANDY);
//		put(new Pair<>(8,1),SANDY);


		put(new Pair<>(3, 4), OTHER);
		put(new Pair<>(3, 5), SANDY);
		put(new Pair<>(3, 6), SANDY);
		put(new Pair<>(3, 7), SANDY);
		put(new Pair<>(3, 8), SANDY);

		put(new Pair<>(4, 3), SANDY);
		put(new Pair<>(4, 4), OTHER);
		put(new Pair<>(4, 5), OTHER);
		put(new Pair<>(4, 6), SANDY);
		put(new Pair<>(4, 7), SANDY);
		put(new Pair<>(4, 8), SANDY);

		put(new Pair<>(5, 3), OTHER);
		put(new Pair<>(5, 4), OTHER);
		put(new Pair<>(5, 5), OTHER);
		put(new Pair<>(5, 6), OTHER);
		put(new Pair<>(5, 7), OTHER);
		put(new Pair<>(5, 8), SANDY);

		put(new Pair<>(6, 3), SANDY);
		put(new Pair<>(6, 4), OTHER);
		put(new Pair<>(6, 5), OTHER);
		put(new Pair<>(6, 6), OTHER);
		put(new Pair<>(6, 7), OTHER);
		put(new Pair<>(6, 8), OTHER);

		put(new Pair<>(7, 3), SANDY);
		put(new Pair<>(7, 4), OTHER);
		put(new Pair<>(7, 5), OTHER);
		put(new Pair<>(7, 6), OTHER);
		put(new Pair<>(7, 7), OTHER);
		put(new Pair<>(7, 8), OTHER);

		put(new Pair<>(8, 3), SANDY);
		put(new Pair<>(8, 4), SANDY);
		put(new Pair<>(8, 5), OTHER);
		put(new Pair<>(8, 6), SANDY);
		put(new Pair<>(8, 7), SANDY);
		put(new Pair<>(8, 8), OTHER);

		put(new Pair<>(9, 3), SANDY);
		put(new Pair<>(9, 4), SANDY);
		put(new Pair<>(9, 5), SANDY);
		put(new Pair<>(9, 6), SANDY);
		put(new Pair<>(9, 7), SANDY);
		put(new Pair<>(9, 8), OTHER);
	}};


	private final BiomeSource biomeSource;
	private final int startX;
	private final int startZ;
	private final int stride;
	private final int threadId;

	public ChunkKernel(MCVersion mcVersion, Dimension dimension, long worldseed, int startX, int startZ, int stride, int threadId) {
		this.biomeSource = BiomeSource.of(dimension, mcVersion, worldseed);
		this.startX = startX;
		this.startZ = startZ;
		this.stride = stride;
		this.threadId = threadId;
	}

	public static void execute(BiomeSource biomeSource, int chunkX, int chunkZ) {
		for (Map.Entry<Pair<Integer, Integer>, Predicate<Biome>> entry : MASK.entrySet()) {
			Pair<Integer, Integer> offset = entry.getKey();
			int posX = (chunkX << 4) + offset.getFirst();
			int posZ = (chunkZ << 4) + offset.getSecond();
			Biome biome = biomeSource.getBiome(posX, 0, posZ);
			if (!entry.getValue().test(biome)) return;
		}
		File file = new File(chunkX + " " + chunkZ + ".found");
		try {
			if (!file.createNewFile()) throw new Exception("Not valid, gtfo") ;
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.printf("Chunk matched at (%d,%d) : /tp @p %d ~ %d %n", chunkX, chunkZ, chunkX << 4, chunkZ << 4);
	}

	@Override
	public void run() {
		int precision = 1000;
		int percent = stride / precision;
		long startTime = System.nanoTime();
		int current = 0;
		double average = 0;
		for (int x = 0; x < stride; x++) {
			for (int z = 0; z < stride; z++) {
				execute(this.biomeSource, startX + x, startZ + z);
			}
			if (threadId == 0 && (x % percent) == 0) {
				long currentTime = System.nanoTime();
				double seconds = (currentTime - startTime) / 1e9;
				int minutesLeft = (int) ((precision - current) * seconds / current / 60);
				System.out.printf("%f%% done in %.2f seconds, ETA: %d hours %d min %n", x / (double) stride * 100, seconds, minutesLeft / 60, minutesLeft % 60);
				current++;
			}
		}
	}
}
