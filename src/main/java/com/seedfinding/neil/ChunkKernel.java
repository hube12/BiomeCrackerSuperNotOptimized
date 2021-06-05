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
	public static HashMap<Pair<Integer, Integer>, Predicate<Biome>> MASK1 = new HashMap<Pair<Integer, Integer>, Predicate<Biome>>() {
		{
			put(new Pair<>(7, 4), OTHER),
					put(new Pair<>(8, 3), SANDY),
					put(new Pair<>(8, 4), OTHER),
					put(new Pair<>(9, 3), SANDY),
					put(new Pair<>(9, 4), SANDY),
					put(new Pair<>(9, 5), OTHER),
					put(new Pair<>(10, 4), SANDY),
					put(new Pair<>(10, 5), OTHER),
					put(new Pair<>(11, 4), SANDY),
					put(new Pair<>(11, 5), OTHER),
					put(new Pair<>(11, 6), OTHER),
					put(new Pair<>(12, 5), OTHER),
					put(new Pair<>(13, 5), OTHER),
		}
	};
	public static HashMap<Pair<Integer, Integer>, Predicate<Biome>> MASK2 = new HashMap<Pair<Integer, Integer>, Predicate<Biome>>() {
		{
			put(new Pair<>(8, 5), OTHER),
					put(new Pair<>(8, 6), OTHER),
					put(new Pair<>(8, 7), OTHER),
					put(new Pair<>(8, 8), OTHER),
					put(new Pair<>(8, 9), OTHER),
					put(new Pair<>(8, 10), OTHER),
					put(new Pair<>(8, 11), OTHER),
					put(new Pair<>(8, 12), OTHER),
					put(new Pair<>(9, 5), SANDY),
					put(new Pair<>(9, 6), SANDY),
					put(new Pair<>(9, 7), OTHER),
					put(new Pair<>(9, 8), OTHER),
					put(new Pair<>(9, 9), OTHER),
					put(new Pair<>(9, 10), SANDY),
					put(new Pair<>(9, 11), SANDY),
					put(new Pair<>(9, 12), SANDY),
					put(new Pair<>(9, 13), SANDY),
					put(new Pair<>(9, 14), SANDY),
					put(new Pair<>(9, 15), SANDY),
					put(new Pair<>(10, 6), SANDY),
					put(new Pair<>(10, 7), SANDY),
					put(new Pair<>(10, 8), OTHER),
					put(new Pair<>(10, 9), SANDY),
					put(new Pair<>(10, 10), SANDY),
					put(new Pair<>(10, 11), SANDY),
					put(new Pair<>(10, 12), SANDY),
					put(new Pair<>(10, 14), SANDY),
					put(new Pair<>(10, 15), SANDY),
					put(new Pair<>(11, 7), SANDY),
					put(new Pair<>(11, 8), SANDY),
					put(new Pair<>(11, 9), SANDY),
					put(new Pair<>(11, 10), SANDY),
					put(new Pair<>(11, 11), SANDY),
					put(new Pair<>(11, 14), SANDY),
					put(new Pair<>(11, 15), SANDY),
					put(new Pair<>(12, 8), SANDY),
					put(new Pair<>(12, 9), SANDY),
					put(new Pair<>(12, 10), SANDY),
					put(new Pair<>(12, 14), SANDY),
					put(new Pair<>(12, 15), SANDY),
					put(new Pair<>(13, 15), SANDY),
		}
	};
	public static HashMap<Pair<Integer, Integer>, Predicate<Biome>> MASK3 = new HashMap<Pair<Integer, Integer>, Predicate<Biome>>() {{
		put(new Pair<>(5, 4), OTHER),
				put(new Pair<>(5, 5), OTHER),
				put(new Pair<>(5, 6), OTHER),
				put(new Pair<>(6, 5), SANDY),
				put(new Pair<>(7, 5), SANDY),
				put(new Pair<>(7, 6), SANDY),
				put(new Pair<>(7, 7), OTHER),
				put(new Pair<>(8, 6), SANDY),
				put(new Pair<>(8, 7), OTHER),
				put(new Pair<>(9, 6), SANDY),
				put(new Pair<>(9, 7), SANDY),
				put(new Pair<>(10, 6), SANDY),
				put(new Pair<>(10, 7), OTHER),
				put(new Pair<>(11, 6), SANDY),
				put(new Pair<>(11, 7), OTHER),
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
		executeSingle(biomeSource,chunkX,chunkZ,MASK1);
		executeSingle(biomeSource,chunkX,chunkZ,MASK2);
		executeSingle(biomeSource,chunkX,chunkZ,MASK3);
	}

	public static void executeSingle(BiomeSource biomeSource, int chunkX, int chunkZ, HashMap<Pair<Integer, Integer>, Predicate<Biome>> hashSet) {
		for (Map.Entry<Pair<Integer, Integer>, Predicate<Biome>> entry : hashSet.entrySet()) {
			Pair<Integer, Integer> offset = entry.getKey();
			int posX = (chunkX << 4) + offset.getFirst();
			int posZ = (chunkZ << 4) + offset.getSecond();
			Biome biome = biomeSource.getBiome(posX, 0, posZ);
			if (!entry.getValue().test(biome)) return;
		}
		File file = new File(chunkX + " " + chunkZ + ".found");
		try {
			if (!file.createNewFile()) throw new Exception("Not valid, gtfo");
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
