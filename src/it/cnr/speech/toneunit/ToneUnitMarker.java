package it.cnr.speech.toneunit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedHashSet;

import it.cnr.speech.audiofeatures.Energy;

public class ToneUnitMarker {

	

	static File audioFileD = new File("./test.wav");
	static File outputFileD = null;
	static Integer minimumToneUnitsD = 3;
	static Integer maxIterationsD = 100;
	static Double initialEnergyThrPercD = 90d;
	static Double windowInSecD = 0.2;
	static Double minEnergyMultiplierD = 100d;//100d;
	
	public static void printNotes() {
		System.out.println("Parameters:");
		System.out.println("-i: path to the input audio file");
		System.out.println("-m: minimum number of tone unit to search for (="+minEnergyMultiplierD+")");
		System.out.println("-t: maximum number of iterations (="+maxIterationsD+")");
		System.out.println("-w: analysis window in seconds (="+windowInSecD+")");
		System.out.println("-e: energy threshold loss to set a marker (percent) (="+initialEnergyThrPercD+")");
		System.out.println("-p: multiplier for minimum energy to set a marker (percent) (="+minEnergyMultiplierD+" Set 0 to disable)");
		System.out.println("-o: output file (.LAB) to write (=<audiofilename>.lab)");
		System.out.println("-h: help");
	}
	
	public static void main(String[] args) throws Exception {

		System.out.println("TU Marker started");

		ToneUnitMarker tum = new ToneUnitMarker();
		File audioFile = null;
		File outputFile = null;
		Integer minimumToneUnits = null;
		Integer maxIterations = null;
		Double initialEnergyThrPerc = null;
		Double windowInSec = null;
		Double minEnergyMultiplier = null;
		if (args == null || args.length == 0) {
			printNotes();
			System.exit(2);
		}
		for (String a : args) {

			if (a.startsWith("-i")) {
				audioFile = new File(a.substring(2).trim().replace("\"", ""));
			} else if (a.startsWith("-m")) {
				minimumToneUnits = Integer.parseInt(a.substring(2).trim());
			} else if (a.startsWith("-t")) {
				maxIterations = Integer.parseInt(a.substring(2).trim());
			} else if (a.startsWith("-e")) {
				initialEnergyThrPerc = Double.parseDouble(a.substring(2).trim());
			} else if (a.startsWith("-w")) {
				windowInSec = Double.parseDouble(a.substring(2).trim());
			} else if (a.startsWith("-p")) {
				minEnergyMultiplier = Double.parseDouble(a.substring(2).trim());
			}else if (a.startsWith("-o")) {
				outputFile = new File(a.substring(2).trim());
			} else if (a.startsWith("-h")) {
				printNotes();
				System.exit(0);
			}
		}

		if (audioFile == null) {
			System.out.println("WARNING: Input file not provided with the -i option. Defaulting to test.wav");
			printNotes();
			//System.exit(2);
			audioFile = audioFileD;
		}
		if (minimumToneUnits == null) {
			System.out.println("WARNING: Defaulting minimumToneUnits to "+minimumToneUnitsD);
			minimumToneUnits = minimumToneUnitsD;
		}
		if (maxIterations == null) {
			System.out.println("WARNING: Defaulting maxIterations to "+maxIterationsD);
			maxIterations = maxIterationsD;
		}
		if (initialEnergyThrPerc == null) {
			System.out.println("WARNING: Defaulting initialEnergyThrPerc to "+initialEnergyThrPercD);
			//initialEnergyThrPerc = 90d;
			initialEnergyThrPerc = initialEnergyThrPercD;
		}
		if (windowInSec == null) {
			System.out.println("WARNING: Defaulting windowInSec to "+windowInSecD);
			windowInSec = windowInSecD;
		}
		if (minEnergyMultiplier == null) {
			System.out.println("WARNING: Defaulting minEnergyMultiplier to "+minEnergyMultiplier);
			minEnergyMultiplier = minEnergyMultiplierD;
		}
		if (outputFile == null) {
			outputFile = new File(audioFile.getAbsolutePath().replace(".wav", ".lab").replace(".WAV", ".lab"));
			System.out.println("WARNING: output file not provided. Defaulting to " + outputFile);
		}

		System.out.println("input File: " + audioFile.getAbsolutePath());
		System.out.println("minimumToneUnits: " + minimumToneUnits);
		System.out.println("maxIterations: " + maxIterations);
		System.out.println("initialEnergyThrPerc: " + initialEnergyThrPerc);
		System.out.println("windowInSec: " + windowInSec);
		System.out.println("minEnergyMultiplier: " + minEnergyMultiplier);
		
		System.out.println("output File: " + outputFile.getAbsolutePath());
		tum.toneUnitSegmentation(audioFile, outputFile, minimumToneUnits, maxIterations, initialEnergyThrPerc,
				windowInSec,minEnergyMultiplier);
	}

	public void toneUnitSegmentation(File audioFile, File outputFile, int minimumToneUnits, int maxIterations,
			double initialEnergyThrPerc, double windowInSec, double minEnergyMultiplier) throws Exception {

		Energy nrg = new Energy();
		LinkedHashSet<double[]> marks = nrg.estimateToneUnits(audioFile, minimumToneUnits, maxIterations,
				initialEnergyThrPerc, windowInSec, minEnergyMultiplier);

		vector2LabFile(marks, outputFile);

	}

	public static void vector2LabFile(LinkedHashSet<double[]> marks, File labfile) throws Exception {
		int nm = marks.size();
		System.out.println("Tone units found: " + nm);

		BufferedWriter bw = new BufferedWriter(new FileWriter(labfile));

		if (nm > 0) {
			int c = 0;
			for (double[] times : marks) {
				double time0 = times[0];
				double time1 = times[1];
				String labLine = time0 + " " + time1 + " TU"+c;
				bw.write(labLine + "\n");
				c++;
			}
		}
		bw.close();
	}

}
