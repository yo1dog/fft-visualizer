package net.devmike.fftVisualizer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class FFTVisualizer2
{
	// ===================================================================
	// Constants
	//
	// ===================================================================
	
	private static final int INIT_PANEL_WIDTH  = 1200;
	private static final int INIT_PANEL_HEIGHT = 500;
	
	private static final int VISUALIZATION_MIN_FREQUENCY = 500;
	private static final int VISUALIZATION_MAX_FREQUENCY = 3000;
	
	private static final int VISUALIZATION_TIME_ORIGIN_MS = 0;
	private static final int VISUALIZATION_TIME_RANGE_MS  = 5000;
	
	private static final long VISUALIZATION_TIME_ORIGIN   = -AudioSample.TIME_MIN_VALUE;
	private long VISUALIZATION_TIME_RANGE;
	
	private static final int VISUALIZATION_AMPLITUDE_ORIGIN = 0;
	private static final int VISUALIZATION_AMPLITUDE_RANGE  = 2000000;
	
	// ===================================================================
	// Variables
	//
	// ===================================================================
	
	// -------------------------------------------------------------------
	// window elements
	
	private final JFrame visualizerFrame;
	private FFTVisualizerPanel visualizerPanel;
	
	
	// -------------------------------------------------------------------
	// visualization data
	
	private static final class FrequencyPoint
	{
		public final long time;
		public final double amplitude;
		
		public FrequencyPoint(long time, double amplitude)
		{
			this.time = time;
			this.amplitude = amplitude;
		}
	}
	private static final class FrequencyPlot
	{
		public final double frequency;
		public final int fftSetSampleIndex;
		public final Color color;
		
		public final ArrayList<FrequencyPoint> points = new ArrayList<FrequencyPoint>();
		
		public FrequencyPlot(double frequency, int fftSetSampleIndex, Color color)
		{
			this.frequency = frequency;
			this.fftSetSampleIndex = fftSetSampleIndex;
			this.color = color;
		}
	}
	private FrequencyPlot[] frequencyPlots;
	
	
	// ===================================================================
	// Methods
	//
	// ===================================================================
	
	/**
	 * Creates a window for visualizing a FFT set.
	 */
	public FFTVisualizer2()
	{
		// I know very little about Java windows, so the bellow code is probably terrible...
		// please let me know how I can improve it.
		
		// create the frame
		visualizerFrame = new JFrame("AudioVisualizer");
		
		visualizerFrame.setBackground(Color.WHITE);
		visualizerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		// create the panel
		visualizerPanel = new FFTVisualizerPanel();
		visualizerPanel.setPreferredSize(new Dimension(INIT_PANEL_WIDTH, INIT_PANEL_HEIGHT));
		visualizerFrame.getContentPane().add(visualizerPanel, java.awt.BorderLayout.CENTER);
		
		// finalize and display
		visualizerFrame.pack();
		visualizerFrame.setVisible(true);
	}
	
	public void displayFFTSets(FFTSet[] fftSets)
	{
		for (int i = 0; i < fftSets.length; ++i)
			displayFFTSet(fftSets[i]);
	}
	
	public void displayFFTSet(FFTSet fftSet)
	{
		if (frequencyPlots == null)
		{
			VISUALIZATION_TIME_RANGE = Util.msToTime(5000);
			
			Color[] colors = {
				Color.RED,
				Color.BLUE,
				Color.GREEN,
				Color.ORANGE,
				Color.MAGENTA,
				Color.PINK
			};
			
			int fftSetSampleStartIndex  = -1;
			int fftSetSampleEndingIndex = -1;
			
			for (int i = 1; i < fftSet.fftSamples.length / 2 - 1; ++i)
			{
				if ((int)fftSet.fftSamples[i].frequency == 1033)
				{
					fftSetSampleStartIndex = i;
					fftSetSampleEndingIndex = i + 1;
					break;
				}
				/*if (fftSetSampleStartIndex == -1 && fftSet.fftSamples[i].frequency >= VISUALIZATION_MIN_FREQUENCY)
					fftSetSampleStartIndex = i;
				
				if (fftSet.fftSamples[i].frequency > VISUALIZATION_MAX_FREQUENCY)
				{
					fftSetSampleEndingIndex = i;
					break;
				}*/
			}
			
			if (fftSetSampleStartIndex == -1)  { System.err.println("MorseCodeDetector.MIN_MORSE_CODE_FREQUENCY too low!");  System.exit(1); return; }
			if (fftSetSampleEndingIndex == -1) { System.err.println("MorseCodeDetector.MAX_MORSE_CODE_FREQUENCY too high!"); System.exit(1); return; }
			
			frequencyPlots = new FrequencyPlot[fftSetSampleEndingIndex - fftSetSampleStartIndex];
			
			for (int i = fftSetSampleStartIndex; i < fftSetSampleEndingIndex; ++i)
				frequencyPlots[i - fftSetSampleStartIndex] = new FrequencyPlot(fftSet.fftSamples[i].frequency, i, colors[(i - fftSetSampleStartIndex) % colors.length]);
		}
		
		for (int i = 0; i < frequencyPlots.length; ++i)
			frequencyPlots[i].points.add(new FrequencyPoint(fftSet.startTime, fftSet.fftSamples[frequencyPlots[i].fftSetSampleIndex].amplitude));
		
		visualizerFrame.repaint();
	}
	
	
	
	
	/**
	 * Custom panel for drawing our visualization.
	 */
	private class FFTVisualizerPanel extends JPanel
	{
		private static final long serialVersionUID = 1l;
		
		public void paint(Graphics g)
		{
			if (frequencyPlots == null)
				return;
			
			for (int i = 0; i < frequencyPlots.length; ++i)
			{
				g.setColor(frequencyPlots[i].color);
				g.drawString(frequencyPlots[i].frequency + " Hz", visualizerPanel.getWidth() - 200, 10 + 15 * i);
				
				for (int j = 1; j < frequencyPlots[i].points.size(); ++j)
				{
					g.drawLine(
							getXForTime(frequencyPlots[i].points.get(j - 1).time),
							getYForAmplitude(frequencyPlots[i].points.get(j - 1).amplitude),
							getXForTime(frequencyPlots[i].points.get(j).time),
							getYForAmplitude(frequencyPlots[i].points.get(j).amplitude));
				}
			}
			
			g.setColor(Color.GRAY);
			for (long i = VISUALIZATION_TIME_ORIGIN_MS; i < VISUALIZATION_TIME_RANGE_MS; i += 1000)
			{
				int x = getXForTime(VISUALIZATION_TIME_ORIGIN + Util.msToTime(i));
				
				g.drawLine(
						x, 0,
						x, visualizerPanel.getHeight());
				
				g.drawString(i + "ms", x + 5, visualizerPanel.getHeight() - 10);
			}
			
			
			for (int i = VISUALIZATION_AMPLITUDE_ORIGIN; i < VISUALIZATION_AMPLITUDE_RANGE; i += 100000)
			{
				int y = getYForAmplitude(i);
				
				g.drawLine(
						0,                          y,
						visualizerPanel.getWidth(), y);
				
				g.drawString(i + "dB", 0, y);
			}
		}
	}
	
	/**
	 * Translates the given frequency into an X position on the visualization.
	 * 
	 * @param frequency - Frequency to translate.
	 * 
	 * @return X position on the visualization.
	 */
	private int getXForTime(long time)
	{
		return (int)((time - VISUALIZATION_TIME_ORIGIN) * (visualizerPanel.getWidth() / ((double)VISUALIZATION_TIME_RANGE)));
	}
	
	/**
	 * Translates the given amplitude into an Y position on the visualization.
	 * 
	 * @param amplitude - Amplitude to translate.
	 * 
	 * @return Y position on the visualization.
	 */
	private int getYForAmplitude(double amplitude)
	{
		return visualizerPanel.getHeight() - 50 - (int)((amplitude - VISUALIZATION_AMPLITUDE_ORIGIN) * ((visualizerPanel.getHeight() - 50) / ((float)VISUALIZATION_AMPLITUDE_RANGE)));
	}
}

