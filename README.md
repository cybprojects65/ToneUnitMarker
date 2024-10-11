# Tone Unit Marker

A simple but powerful *Tone Unit Marker* implementation in Java, enhancing the algorithm explained in 

> D’Anna, L., Petrillo, M. (2003). Sistemi automatici per la
> segmentazione in unità tonali. In: Atti delle XIII Giornate di Studio
> del Gruppo di Fonetica Sperimentale (GFS), pp. 285–290

The current algorithm is described in:

> Coro, G., Bardelli, S., Cuttano, A., & Fossati, N. (2022). Automatic
> detection of potentially ineffective verbal communication for training
> through simulation in neonatology. _Education and Information
> Technologies_, _27_(7), 9181-9203.
> 
> Coro, G., Bardelli, S., Cuttano, A., Scaramuzzo, R. T., & Ciantelli,
> M. (2023). A self-training automatic infant-cry detector. _Neural
> Computing and Applications_, _35_(11), 8543-8559.


## Preparation

Create a standalone uber-JAR containing all dependencies from the GIT repository or download the [pre-coocked JAR file](https://github.com/cybprojects65/ToneUnitMarker/raw/refs/heads/main/tum.jar).

## General parameters

    -i: path to the input audio file
    -m: minimum number of tone unit to search for (=3)
    -t: maximum number of iterations (=100)
    -w: analysis window in seconds (=0.2)
    -e: energy threshold loss to set a marker (percent) (=90.0)
    -p: multiplier for minimum energy to set a marker (percent) (=100.0 Set 0 to disable)
    -o: output file (.LAB) to write (=<audiofilename>.lab)
    -h: help

   
## Execution from the command line

Install Java 16 or higher.
Download the [text.wav](https://github.com/cybprojects65/ToneUnitMarker/raw/refs/heads/main/test.wav) file (a mono, PCM-signed 16 bit, 16kHz recording).
Open a prompt.
Execute:

    java -cp ./tum.jar it.cnr.speech.toneunit.ToneUnitMarker -w0.2 -e90 -p100 -m3

This instruction executes the analysis using a 200 ms window (w=0.2 s), detecting energy loss of 90% between two consecutive windows, and discarding losses between  too high energetic windows (of at least 100 times the lowest energy window, which normally contains silence). It will search for at least 3 tone units. If less then 3 tone units are found, the system will automatically decrease the energy jump by 10% and retry. A maximum of 100 iterations of energy reduction will be attempted.

The parametrisation above is useful for long audio files containing multiple sentences.
