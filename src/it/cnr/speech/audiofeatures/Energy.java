package it.cnr.speech.audiofeatures;

import java.io.File;
import java.util.LinkedHashSet;

import it.cnr.workflow.utils.UtilsMath;

public class Energy {

	AudioBits bits;
	short[] signal;
	public static String ENERGYSEGMENTATIONFILE = "energy_segmentation.csv";
	public static String DERIVATIVEFILE = "energy_derivative.csv";

	public LinkedHashSet<double[]> estimateToneUnits(File audioFile, int minimumToneUnits, int maxIterations,
			double initialEnergyThrPerc, double windowInSec, double minEnergyMultiplier) throws Exception {
		boolean normalize = true;
		double[] normalisedEnergyCurve = energyCurve((float) windowInSec, audioFile, normalize);
		double[] derivative = UtilsMath.derivative(normalisedEnergyCurve);
		double meanEnergy = UtilsMath.mean(normalisedEnergyCurve);
		double minEnergyC = UtilsMath.min(normalisedEnergyCurve);
		// Utils.writeSignal2File(derivative, new File(outputFolder,DERIVATIVEFILE));

		float sfrequency = bits.getAudioFormat().getSampleRate();
		int waveCounter = 0;
		int ntries = 0;
		int maxTries = maxIterations;
		int minNumberOfWavesToFind = minimumToneUnits;
		double maxSNR = 0;
		double energyThr = initialEnergyThrPerc / 100d;
		LinkedHashSet<double[]> marks = null;
		//double minEnergy = 0; //100*minEnergyC;
		double minEnergy = minEnergyMultiplier*minEnergyC;
		if (minEnergy==0)
			minEnergy = meanEnergy;
		
		System.out.println("signal minE:"+minEnergy+" meanE:"+meanEnergy);
		
		while (ntries < maxTries) {
			maxSNR = 0;
			double time0 = 0;
			waveCounter = 0;
			// BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFolder,
			// ENERGYSEGMENTATIONFILE), true));
			marks = new LinkedHashSet<double[]>();
			
			for (int i = 1; i < normalisedEnergyCurve.length; i++) {
				double currentEnrg = normalisedEnergyCurve[i];
				double prevEnrg = normalisedEnergyCurve[i-1];
				double nrgloss = ((prevEnrg - currentEnrg) / prevEnrg);
				// if (derivative[i - 1] < 0 && normalisedEnergyCurve[i] < energyThr) {
				if (nrgloss>0 && nrgloss > energyThr && currentEnrg<minEnergy) {

					double time1 = ((double)(i+1))* windowInSec; //(((double) (i) * Math.round(windowInSec * sfrequency)) / (double) sfrequency);
					// save signal segment t0 t1
//					int i0 = (int) (time0 * sfrequency);
//					int i1 = (int) (time1 * sfrequency);
					if (nrgloss > 0) {// exclude silence
						double[] timemark = new double[2];
						timemark[0] = time0;
						timemark[1] = time1;
						marks.add(timemark);
					}
					System.out.println("TU"+waveCounter+" E:"+currentEnrg+" L:"+nrgloss);
					
					double SNR = 10 * Math.log10(normalisedEnergyCurve[i - 1] / normalisedEnergyCurve[i]);
					if (SNR > maxSNR)
						maxSNR = SNR;
					time0 = time1;
					waveCounter++;
				}
				
			}
			// bw.close();
			if (waveCounter >= minNumberOfWavesToFind) {
				ntries = maxTries;
				System.out.println("Found enough TUs (" + waveCounter + ")");
			} else {
				ntries++;
				// System.out.println("Too few segments using energy threshold: " + energyThr +
				// " mean energy " + meanEnergy);
				System.out.println(
						"No segment found with energy threshold: " + energyThr + " - mean energy " + meanEnergy);
				energyThr = energyThr - (0.1*energyThr);
				System.out.println("Retrying segmentation using energy threshold: " + energyThr);
			}

		} // end while

		if (waveCounter == 0)
			System.out.println("No tone unit found");

		System.out.println("Estimated SNR: " + maxSNR);

		return marks;

	}

	public double[] energyCurve(float windowInMs, File audioFile) {
		return energyCurve(windowInMs, audioFile, true);
	}

	public double[] energyCurve(float windowInMs, File audioFile, boolean normalize) {
		// extract features
		bits = new AudioBits(audioFile);
		signal = bits.getShortVectorAudio();
		float sfrequency = bits.getAudioFormat().getSampleRate();
		double[] nrg = energyCurve(windowInMs, signal, sfrequency, normalize);
		// bits.deallocateAudio();
		return nrg;
	}

	public double[] energyCurve(float windowIns, short[] signal, float sfrequency, boolean normalize) {

		// initial energy
		int windowSamples = Math.round(windowIns * sfrequency);
		int steps = signal.length / windowSamples;

		// trace energy curve
		double[] energySignal = new double[steps];
		double maxEnergy = -Double.MAX_VALUE;
		for (int i = 0; i < steps; i++) {
			int currentIdx = i * windowSamples;
			short[] signalSlice = new short[windowSamples];
			for (int j = 0; j < windowSamples; j++) {
				signalSlice[j] = signal[currentIdx + j];
			}

			energySignal[i] = energy(signalSlice);

			if (energySignal[i] > maxEnergy)
				maxEnergy = energySignal[i];
		}

		if (normalize) {
			for (int i = 0; i < steps; i++) {
				energySignal[i] = energySignal[i] / maxEnergy;
			}
		}
		return energySignal;
	}

	public static double energy(short[] signal) {

		double energy = 0;
		for (int g = 0; g < signal.length; g++) {
			energy += signal[g] * signal[g];
		}
		energy = energy / (double) signal.length;
		return energy;
	}

}
