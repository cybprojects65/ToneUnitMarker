package it.cnr.speech.statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;


import it.cnr.speech.audiofeatures.AudioBits;
import it.cnr.speech.audiofeatures.Energy;
import it.cnr.speech.audiofeatures.PitchExtractor;
import weka.core.Utils;

public class AudioStats {

	public static short[] cutSignal(short [] signal, double samplingPeriodInSec, double t0, double t1) {
		int startIdx = 0;
		int endIdx = 0;
		for (int ti=0;ti<signal.length;ti++) {
			double windowtime = samplingPeriodInSec*ti;
			if ((windowtime+samplingPeriodInSec)>t0 && startIdx==0) {
				startIdx = ti;
			}
			if ((windowtime+samplingPeriodInSec)>t1 && endIdx == 0) {
				endIdx = ti;
				break;
			}
		}
		
		if (endIdx==0)
			endIdx = signal.length-1;
		
		if (startIdx>=endIdx)
			endIdx = Math.min(startIdx+2,signal.length-1);
		
        int segmentLength = endIdx - startIdx+1;
        short[] segment = new short[segmentLength];
        System.arraycopy(signal, startIdx, segment, 0, segmentLength);
		return segment;
	}
	
	public static double[] cutPitch(Double [] pitch, double samplingPeriodInSec, double t0, double t1) {
		int startIdx = 0;
		int endIdx = 0;
		for (int ti=0;ti<pitch.length;ti++) {
			double windowtime = samplingPeriodInSec*ti;
			if ((windowtime+samplingPeriodInSec)>t0 && startIdx==0) {
				startIdx = ti;
			}
			if ((windowtime+samplingPeriodInSec)>t1 && endIdx == 0) {
				endIdx = ti;
				break;
			}
		}
		
		if (endIdx==0)
			endIdx = pitch.length-1;
		
		if (startIdx>=endIdx)
			endIdx = Math.min(startIdx+2,pitch.length-1);
		
        int segmentLength = endIdx - startIdx+1;
        double[] segment = new double[segmentLength];
        for (int i=0;i<segmentLength;i++) {
        	if (pitch[startIdx+i].isNaN() || pitch[startIdx+i].isInfinite())
        		segment[i] = 0;
        	else
        		segment[i] = pitch[startIdx+i];
        }
		return segment;
	}
	
	LinkedHashMap<Integer, double[]> markers = new LinkedHashMap<>();
	LinkedHashMap<Integer, Double> energies = new LinkedHashMap<>();
	LinkedHashMap<Integer, Double> pitches = new LinkedHashMap<>();
	LinkedHashMap<Integer, Double> durations = new LinkedHashMap<>();
	LinkedHashMap<Integer, Double> energies_by_duration = new LinkedHashMap<>();
	LinkedHashMap<Integer, Double> pitches_by_duration = new LinkedHashMap<>();
	
	
	public void generateStats(LinkedHashSet<double[]> marks, File audiofile) throws Exception {
		int nm = marks.size();

		if (nm > 0) {
			AudioBits bits = new AudioBits(audiofile);
			short [] signal = bits.getShortVectorAudio();
			float sfrequency = bits.getAudioFormat().getSampleRate();
			double samplingPeriodInSec = 1d/sfrequency;
			bits.ais.close();
			PitchExtractor pitchEx = new PitchExtractor();
			pitchEx.calculatePitch(audiofile.getAbsolutePath());
			Double [] pitch = pitchEx.pitchCurve;
			
			int c = 0;
			for (double[] times : marks) {
				double time0 = times[0];
				double time1 = times[1];
				short[] segment = cutSignal(signal, samplingPeriodInSec, time0, time1);
				double[] pitchsegment = cutPitch(pitch, pitchEx.pitchWindowSec, time0, time1);
				double avgenergy = Energy.energy(segment);
				double avgpitch = Utils.mean(pitchsegment);
				double duration_sec = time1-time0;
				double energy_by_duration =  avgenergy/duration_sec;
				double pitch_by_duration =  avgpitch/duration_sec;
				
				markers.put(c,times);
				energies.put(c, avgenergy);
				pitches.put(c, avgpitch);
				durations.put(c, duration_sec);
				energies_by_duration.put(c, energy_by_duration);
				pitches_by_duration.put(c, pitch_by_duration);
				
				c++;
			}
			
			
			vector2LabFile(new File(audiofile.getAbsolutePath().replace(".wav", "_stat.lab")));
		}

	}
	
	
	public void vector2LabFile(File labfile) throws Exception {
		int nm = markers.size();

		BufferedWriter bw = new BufferedWriter(new FileWriter(labfile));

		if (nm > 0) {
			
			for (int c=0;c<nm;c++) {
				double[] times = markers.get(c);
				double time0 = times[0];
				double time1 = times[1];
				//String annotation = "AVG_ENERGY="+energies.get(c)+";"+"AVG_PITCH="+pitches.get(c)+";"+"DURATION="+durations.get(c)+";"+"ENERGY_BY_DURATION="+energies_by_duration.get(c)+";"+"PITCH_BY_DURATION="+pitches_by_duration.get(c);
				String annotation = "AVG_ENERGY="+energies.get(c)+";"+"AVG_PITCH(Hz)="+pitches.get(c)+";"+"DURATION(s)="+durations.get(c);
				String labLine = time0 + " " + time1 + " "+annotation;
				bw.write(labLine + "\n");
				
			}
		}
		bw.close();
		System.out.println("Stats generated");
		
	}
	
	
	
	
}
