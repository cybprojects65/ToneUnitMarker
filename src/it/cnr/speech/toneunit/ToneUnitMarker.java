package it.cnr.speech.toneunit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedHashSet;

import it.cnr.speech.audiofeatures.AudioBits;
import it.cnr.speech.audiofeatures.AudioWaveGenerator;
import it.cnr.speech.audiofeatures.Energy;
import it.cnr.speech.statistics.AudioStats;

public class ToneUnitMarker {

	

	static File audioFileD = new File("./test.wav");
	static File outputFileD = null;
	static Integer minimumToneUnitsD = 3;
	static Integer maxIterationsD = 100;
	static Double initialEnergyThrPercD = 90d;
	static Double windowInSecD = 0.2;
	static Double minEnergyMultiplierD = 0d;//100d;
	
	public static void printNotes() {
		System.out.println("Parameters:");
		System.out.println("-i: path to the input audio file");
		System.out.println("-m: minimum number of tone unit to search for (="+minimumToneUnitsD+")");
		System.out.println("-t: maximum number of iterations (="+maxIterationsD+")");
		System.out.println("-w: analysis window in seconds (="+windowInSecD+")");
		System.out.println("-e: energy threshold loss to set a marker (percent) (="+initialEnergyThrPercD+")");
		System.out.println("-p: quartile of the energy distribution to use as the minimum energy threshold (0=25th percentile; 1=50th; 2=75th; 3=100th)");
		System.out.println("-o: output file (.LAB) to write (=<audiofilename>.lab)");
		System.out.println("-s: start time (in seconds) of  the analysis on the input file (=-1 entire file)");
		System.out.println("-d: end time (in seconds) of the analysis on the input file (=-1 entire file)");
		System.out.println("-L: maximum length of the Tone Unit in s (suggested value=30) - This an alternative to the search for the minimum number of tone units");
		System.out.println("-S: option to save or not the detected TUs when using maximum length search (default=true)");
		System.out.println("-F: The folder in which the TUs will be saved when in max-length mode");
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
		
		Double maxLength = null;
		Boolean saveSegments = null;
		File outputFolder = null;

		Double t0 = -1d;
		Double t1 = -1d;
		
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
			}else if (a.startsWith("-s")) {
				t0 = Double.parseDouble(a.substring(2).trim());
			}else if (a.startsWith("-d")) {
				t1 = Double.parseDouble(a.substring(2).trim());
			}
			else if (a.startsWith("-L")) {
				maxLength = Double.parseDouble(a.substring(2).trim());
			}
			else if (a.startsWith("-S")) {
				saveSegments = Boolean.parseBoolean(a.substring(2).trim());
			}
			else if (a.startsWith("-F")) {
				outputFolder = new File(a.substring(2).trim().replace("\"", ""));
			}
			else if (a.startsWith("-h")) {
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
		
		System.out.println("start time: " + minEnergyMultiplier);
		System.out.println("end time: " + minEnergyMultiplier);
		
		System.out.println("output File: " + outputFile.getAbsolutePath());
		if (maxLength==null) {
			
			if (minimumToneUnits == null) {
				System.out.println("WARNING: Defaulting minimumToneUnits to "+minimumToneUnitsD);
				minimumToneUnits = minimumToneUnitsD;
			}
			tum.toneUnitSegmentation(audioFile, outputFile, minimumToneUnits, maxIterations, initialEnergyThrPerc,
				windowInSec,minEnergyMultiplier,t0,t1);
		}else {
			if (saveSegments == null) {
				saveSegments = true;
				System.out.println("WARNING: Defaulting saveSegments to true");
			}
			if (saveSegments && outputFolder == null) {
				System.out.println("ERROR: no output folder specified");
				System.exit(0);
			}
			tum.toneUnitSegmentation(audioFile, outputFile, maxLength.doubleValue(), maxIterations, 
				initialEnergyThrPerc, windowInSec, minEnergyMultiplier, 
				t0, t1, saveSegments.booleanValue(), outputFolder);
		}
	}

	public void toneUnitSegmentation(File audioFile, File outputFile, int minimumToneUnits, int maxIterations,
			double initialEnergyThrPerc, double windowInSec, double minEnergyMultiplier, double t0, double t1) throws Exception {

		Energy nrg = new Energy();
		LinkedHashSet<double[]> marks = nrg.estimateToneUnits(audioFile, minimumToneUnits, maxIterations,
				initialEnergyThrPerc, windowInSec, minEnergyMultiplier, t0,t1);

		vector2LabFile(marks, outputFile);
		AudioStats as = new AudioStats();
		as.generateStats(marks, audioFile);
	}

	public void toneUnitSegmentation(File audioFile, File outputFile, double maxLength, int maxIterations, 
			double initialEnergyThrPerc, double windowInSec, double minEnergyMultiplier, 
			double t0, double t1, boolean saveSegments, File outputFolder) throws Exception {
		
		Energy nrg = new Energy();
		
		int iteration = 0;
		LinkedHashSet<double[]> marks = null;
		int minimumToneUnits = 1;
		
		while (iteration < maxIterations) {
		
			marks = nrg.estimateToneUnits(audioFile, minimumToneUnits, maxIterations,
				initialEnergyThrPerc, windowInSec, minEnergyMultiplier, t0,t1);
		
			boolean valid = true;
			
			for (double[] times : marks) {
				double time0 = times[0];
				double time1 = times[1];
				if ((time1-time0)>maxLength) {
					System.out.println("#Too long TU: ["+time0+";"+time1+"] _ duration:"+(time1-time0)+"s");
					valid = false;
					break;
				}
			}
			if (marks.size()==0)
				valid = false;
			if (!valid) {
				iteration = iteration+1;
				minimumToneUnits = minimumToneUnits+1;
				System.out.println("->COULD NOT SATISFY THE MAX LENGTH OF "+maxLength+"s REQUIREMENT FOR EACH TU - RETRYING WITH "+minimumToneUnits+" minimum TUs");
			}else {
				System.out.println("->->MAX LENGTH WAS SUCCESSFULLY SATISFIED FOR EACH TU - USED "+minimumToneUnits+" minimum TUs");
				break;
			}
		}
		
		if (iteration==maxIterations) {
			System.out.println("###################################################################################");
			System.out.println("IMPOSSIBLE TO FIND A SUITABLE SEPARATION INTO TONE UNITS - USING ABRUPT SEPARATION");
			System.out.println("###################################################################################");
			//separate audio into 30s chunks
			LinkedHashSet<double[]> abruptmarks = new LinkedHashSet<>();
			double t00 = 0;
			double t01 = t00+maxLength;

			AudioBits bits = new AudioBits(audioFile);
			short[] signal = bits.getShortVectorAudio();
			float sfrequency = bits.getAudioFormat().getSampleRate();
			bits.ais.close();
			double tmax = (double) signal.length/(double)sfrequency;
			while (t01<=tmax) {
				double [] segment = {t00,t01};
				abruptmarks.add(segment);
				t00=t01;
				t01 = t00+maxLength;
			}
			double [] segment = {t00,tmax};
			abruptmarks.add(segment);
			marks = abruptmarks;
			System.out.println("Cut-off TU boundaries. Obtained "+marks.size()+" tone units");
		}else {
			System.out.println("Found "+marks.size()+" tone units");
			//greedy accumulation of intervals
			LinkedHashSet<double[]> greedymarks = new LinkedHashSet<>();
			double t00 = 0;
			double t01 = 0;
			for (double[] times : marks) {
				double time0 = times[0];
				double time1 = times[1];
				if ((time1-t00)>maxLength) {
					double [] segment = {t00,t01};
					greedymarks.add(segment);
					t00 = time0;
					t01 = time1;
				}else {
					t01 = time1;
				}
			}
			double [] segment = {t00,t01};
			greedymarks.add(segment);
			marks = greedymarks;
			System.out.println("Aggregated TU boundaries. Obtained "+marks.size()+" tone units");
		}
		
		System.out.println("Saving lab file..");
		
		vector2LabFile(marks, outputFile);
		AudioStats as = new AudioStats();
		as.generateStats(marks, audioFile);
		
		if (saveSegments) {
			if (!outputFolder.exists()) 
				outputFolder.mkdir();
			
			System.out.println("Saving segments to "+outputFolder.getAbsolutePath());
			AudioBits bits = new AudioBits(audioFile);
			short[] signal = bits.getShortVectorAudio();
			float sfrequency = bits.getAudioFormat().getSampleRate();
			bits.ais.close();
			int waveCounter = 1;
			for (double[] times : marks) {
				double time0 = times[0];
				double time1 = times[1]+(windowInSec/2d);
				int i0 = (int) (time0 * sfrequency);
				int i1 = Math.min((int) (time1 * sfrequency),(signal.length-1));
				short[] subsignal = new short[i1 - i0 + 1];
				for (int k = i0; k <= i1; k++) {
					subsignal[k - i0] = signal[k];			
				}
				try {
					//System.out.println("SNR = "+SNR);
					File outputWaveFile = new File(outputFolder, audioFile.getName().replace(".wav", "")+"_" + waveCounter + ".wav");
					AudioWaveGenerator.generateWaveFromSamplesWithSameFormat(subsignal, outputWaveFile, bits.getAudioFormat());
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.print(""+waveCounter+". ");
				waveCounter = waveCounter+1;
				
			}
			System.out.println("");
		}//end saveSegments
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
