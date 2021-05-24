package com.seedfinding.neil;

import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.version.MCVersion;

import java.util.ArrayList;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		int nbThreads=Runtime.getRuntime().availableProcessors();
		if (args.length==1){
			try {
				nbThreads=Integer.parseInt(args[0]);
			}catch (NumberFormatException e){
				e.printStackTrace();
			}
		}

		int nbThreadsSqr= (int) Math.sqrt(nbThreads);
		System.out.printf("Using only %dx%d out of the %d threads, you should use a computer with a square amount of " +
				"thread so 1 4 9 16 25 36... or provide number of targeted threads as args",nbThreadsSqr,nbThreadsSqr,nbThreads);
		int halfSize=6_000_000;
		int minRange=-halfSize/16;
		int maxRange=halfSize/16;
		long worldSeed=-4172144997902289642L;
		MCVersion mcVersion=MCVersion.v1_12;
		Dimension dimension=Dimension.OVERWORLD;
		int stride=(maxRange-minRange)/nbThreadsSqr;
		// we will be missing right side and bottom side but who cares
		ArrayList<Thread> threads=new ArrayList<>();
		int threadId=0;
		for (int x = 0; x < nbThreadsSqr; x++) {
			for (int z = 0; z < nbThreadsSqr; z++) {
				int minX=minRange+stride*x;
				int maxX=minX+stride;
				int minZ=minRange+stride*z;
				int maxZ=minZ+stride;
				System.out.printf("Starting work for quadrant (%d,%d) which maps out to (%d,%d) to (%d,%d) %n",x,z,minX,minZ,maxX,maxZ);
				Thread thread=new Thread(new ChunkKernel(mcVersion,dimension,worldSeed,minX,minZ,stride,threadId++));
				thread.start();
				threads.add(thread);
			}
		}
		for (Thread thread : threads) {
			thread.join();
		}
	}
}
