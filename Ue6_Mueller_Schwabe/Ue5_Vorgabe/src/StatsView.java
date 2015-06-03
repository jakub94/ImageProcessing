// BV Ue06 WS2014/15 Vorgabe Hilfsklasse StatsView
//
// Copyright (C) 2014 by Klaus Jung

import java.awt.Color;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


public class StatsView extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String[] names = { "Minimum:", "Maximum:", "Middle:", "Median:", "Variance:", "Entropy:" }; // TODO: enter proper names
	private static final int rows = names.length;
	private static final int border = 2;
	private static final int columns = 2;
	private static final int graySteps = 256;
	
	int lowerBorder = 0; // Lower Border 
	int upperBorder = 0; // Upper Border
	

	public int getLowerBorder() {
		return lowerBorder;
	}

	public int getUpperBorder() {
		return upperBorder;
	}

	private JLabel[] infoLabel = new JLabel[rows];
	private JLabel[] valueLabel = new JLabel[rows];

	private int[] histogram = null;

	public StatsView() {
		super(new GridLayout(rows, columns, border, border));
		TitledBorder titBorder = BorderFactory.createTitledBorder("Statistics");
		titBorder.setTitleColor(Color.GRAY);
		setBorder(titBorder);
		for(int i = 0; i < rows; i++) {
			infoLabel[i] = new JLabel(names[i]);
			valueLabel[i] = new JLabel("-----");
			add(infoLabel[i]);
			add(valueLabel[i]);
		}
	}

	private void setValue(int column, int value) {
		valueLabel[column].setText("" + value);
	}

	private void setValue(int column, double value) {
		valueLabel[column].setText(String.format(Locale.US, "%.2f", value));
	}

	public boolean setHistogram(int[] histogram) {
		if(histogram == null || histogram.length != graySteps) {
			return false;
		}
		this.histogram = histogram;
		update();
		return true;
	}

	public boolean update() {
		if(histogram == null) {
			return false;
		}

		// TODO: calculate and display statistic values
		//		setValue(0, 0);
		//		setValue(1, 3.1415);

		int[] sortedValues = histogram.clone();
		Arrays.sort(sortedValues);

		int min = 0;
		int max = 255; 
		int median = 0; 
		double middle = 0;	
		double variance = 0; 
		double entropy = 0;

		int pixelSum = 0;

		for(int i = 0; i < histogram.length; i++) {
			if(histogram[i] > 0) {
				min = i; 
				i = histogram.length; 
			}
		}

		for(int i = histogram.length-1; i > 0 ; i--) {
			if(histogram[i] > 0) {
				max = i; 
				i = 0; 
			}
		}
		
		for(int i : histogram) {
			// System.out.println(pixelSum);
			pixelSum += i;
		}		
	
		int pixelSumDividedBy2 = pixelSum/2;
		int pixelSumDividedBy100 = pixelSum/100;
	
		int pixelCount = 0;
		
		for(int i = 0; i < histogram.length-1; i++) {
			pixelCount += histogram[i];
			if(pixelCount >= pixelSumDividedBy2) {
				median = i;
				// System.out.println(pixelCount);
				break;
			}
		}
		
		pixelCount = 0;
		
		for(int i = 0; i < histogram.length-1; i++) {
			pixelCount += histogram[i];
			if(pixelCount >= pixelSumDividedBy100) {
				lowerBorder = i;
				break; 
			}
		}
		
		pixelCount = 0;
		
		for(int i = histogram.length-1; i >= 0 ; i--) {	
			pixelCount += histogram[i];
			if(pixelCount >= pixelSumDividedBy100) {
				upperBorder = i;
				break; 
			}
		}

		int tmp = 0;
		
		for (int i = 0; i < histogram.length; i++) {	
			tmp += i * histogram[i];
		}

		middle = 1.0 * tmp / pixelSum; 

		for(int i = 0; i < histogram.length; i++) {
			variance += (i-middle) * (i-middle) * histogram[i];
		}

		double logarithmOf2 = Math.log(2);
		
		for(int i : histogram) {
			double p = (i * 1.0 / pixelSum);
			if(p > 0)
				entropy += p * ((Math.log(p) / logarithmOf2));
		}
		
		entropy = -1 * entropy;
		variance = variance / pixelSum; 

		//Set Values
		setValue(0, min);
		setValue(1, max);
		setValue(2, middle);
		setValue(3, median);
		setValue(4, variance);
		setValue(5, entropy);

		return true;
	}
}