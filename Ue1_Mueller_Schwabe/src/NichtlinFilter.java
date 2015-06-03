// BV Ue1 WS2014/15 Vorgabe
//
// Copyright (C) 2014 by Klaus Jung

import javax.swing.*;

import java.util.*;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.util.Random;

public class NichtlinFilter extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String author = "Jakub Mueller & Dennis Schwabe";		// TODO: type in your name here
	private static final String initialFilename = "lena_klein.png";
	private static final File openPath = new File(".");
	private static final int borderWidth = 5;
	private static final int maxWidth = 445;
	private static final int maxHeight = maxWidth;
	private static final int maxNoise = 30;	// in per cent

	private static JFrame frame;

	private ImageView srcView;			// source image view
	private ImageView dstView;			// filtered image view

	private int[] origPixels = null;

	private JLabel statusLine = new JLabel("    "); // to print some status text

	private JComboBox<String> noiseType;
	private JLabel noiseLabel;
	private JSlider noiseSlider;
	private JLabel noiseAmountLabel;
	private boolean addNoise = false;
	private double noiseFraction = 0.01;	// fraction for number of pixels to be modified by noise

	private JComboBox<String> filterType;


	public NichtlinFilter() {
		super(new BorderLayout(borderWidth, borderWidth));

		setBorder(BorderFactory.createEmptyBorder(borderWidth,borderWidth,borderWidth,borderWidth));

		// load the default image
		File input = new File(initialFilename);

		if(!input.canRead()) input = openFile(); // file not found, choose another image

		srcView = new ImageView(input);
		srcView.setMaxSize(new Dimension(maxWidth, maxHeight));

		// convert to grayscale
		makeGray(srcView);

		// keep a copy of the grayscaled original image pixels
		origPixels = srcView.getPixels().clone();

		// create empty destination image of same size
		dstView = new ImageView(srcView.getImgWidth(), srcView.getImgHeight());
		dstView.setMaxSize(new Dimension(maxWidth, maxHeight));

		// control panel
		JPanel controls = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0,borderWidth,0,0);

		// load image button
		JButton load = new JButton("Open Image");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadFile(openFile());
				// convert to grayscale
				makeGray(srcView);  
				// keep a copy of the grayscaled original image pixels
				origPixels = srcView.getPixels().clone();
				calculate(true);
			}        	
		});

		// selector for the noise method
		String[] noiseNames = {"No Noise", "Salt & Pepper"};

		noiseType = new JComboBox<String>(noiseNames);
		noiseType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addNoise = noiseType.getSelectedIndex() > 0;
				noiseLabel.setEnabled(addNoise);
				noiseSlider.setEnabled(addNoise);
				noiseAmountLabel.setEnabled(addNoise);
				calculate(true);
			}
		});

		// amount of noise
		noiseLabel = new JLabel("Noise:");
		noiseAmountLabel = new JLabel("" + Math.round(noiseFraction * 100.0)  + " %");
		noiseSlider = new JSlider(JSlider.HORIZONTAL, 0, maxNoise, (int) Math.round(noiseFraction * 100.0));
		noiseSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				noiseFraction = noiseSlider.getValue() / 100.0;
				noiseAmountLabel.setText("" + Math.round(noiseFraction * 100.0) + " %");
				calculate(true);
			}
		});
		noiseLabel.setEnabled(addNoise);
		noiseSlider.setEnabled(addNoise);
		noiseAmountLabel.setEnabled(addNoise);

		// selector for filter
		String[] filterNames = {"No Filter", "Min Filter", "Max Filter", "Box Filter", "Median Filter"};
		filterType = new JComboBox<String>(filterNames);
		filterType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				calculate(false);
			}
		});

		controls.add(load, c);
		controls.add(noiseType, c);
		controls.add(noiseLabel, c);
		controls.add(noiseSlider, c);
		controls.add(noiseAmountLabel, c);
		controls.add(filterType, c);

		// images panel
		JPanel images = new JPanel(new GridLayout(1,2));
		images.add(srcView);
		images.add(dstView);

		// status panel
		JPanel status = new JPanel(new GridBagLayout());

		status.add(statusLine, c);

		add(controls, BorderLayout.NORTH);
		add(images, BorderLayout.CENTER);
		add(status, BorderLayout.SOUTH);

		calculate(true);

	}

	private File openFile() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
		chooser.setFileFilter(filter);
		chooser.setCurrentDirectory(openPath);
		int ret = chooser.showOpenDialog(this);
		if(ret == JFileChooser.APPROVE_OPTION) return chooser.getSelectedFile();
		return null;		
	}

	private void loadFile(File file) {
		if(file != null) {
			srcView.loadImage(file);
			srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
			// create empty destination image of same size
			dstView.resetToSize(srcView.getImgWidth(), srcView.getImgHeight());
			frame.pack();
		}

	}


	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Nonlinear Filters - " + author);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JComponent newContentPane = new NichtlinFilter();
		newContentPane.setOpaque(true); //content panes must be opaque
		frame.setContentPane(newContentPane);

		// display the window.
		frame.pack();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	private void calculate(boolean createNoise) {
		long startTime = System.currentTimeMillis();

		if(createNoise) {
			// start with original image pixels
			srcView.setPixels(origPixels);
			// add noise
			if(addNoise) 
				makeNoise(srcView);
			// make changes visible
			srcView.applyChanges();
		}

		// apply filter
		filter();

		// make changes visible
		dstView.applyChanges();

		long time = System.currentTimeMillis() - startTime;
		statusLine.setText("Processing Time = " + time + " ms");
	}


	private void makeGray(ImageView imgView) {
		int pixels[] = imgView.getPixels();

		// TODO: convert pixels to grayscale

		// loop over all pixels
		for(int i = 0; i < pixels.length; i++) {
			int r = (pixels[i] >> 16 & 0xff);
			int g = (pixels[i] >> 8 & 0xff);
			int b = (pixels[i] & 0xff);

			int grayValue = (r+b+g)/3;

			pixels[i] = (0xff000000 | (grayValue << 16) | (grayValue << 8) | grayValue);  
		}
	}

	private void makeNoise(ImageView imgView) {
		int pixels[] = imgView.getPixels();

		// TODO: add noise to pixels
		int numberOfPixels = (int)(pixels.length * noiseFraction); // numbers of Pixels to be "noised"
		Random rand = new Random();
		for(int i = 0; i < numberOfPixels; i++){

			int randInt = rand.nextInt((pixels.length)-1);
			if( (pixels[randInt] == 0) | (pixels[randInt] == 0xffffffff) ) i--; 

			int blackOrWhite = rand.nextInt(2);
			int value = 0xff000000;	// default black

			if(blackOrWhite == 1)
				value = 0xffffffff; 
			pixels[randInt] = value; 
		}
	}

	private void filter() {
		int src[] = srcView.getPixels();
		int dst[] = dstView.getPixels();
		int width = srcView.getImgWidth();
		int height = srcView.getImgHeight();
		int filterIndex = filterType.getSelectedIndex();


		//Randbehandlung
		/**		
		int tmp[];
		tmp = new int[(width+2)*(height+2)];
		for(int ys = 0; ys < height+2; ys++){
			for(int xs = 0; xs <width+2; xs++){
				int poss = ys*(width+2)+xs; 
				int pos = ys*width+xs; 
				if((xs==0) || (ys==0) || (xs == width+2) || (ys == height+2)){
					tmp[poss]=0xff808080; 
				}
				else{
					tmp[poss]=src[pos]; 
				}

			}
		}

		 **/		

		// TODO: implement filters 
		switch(filterIndex){
		case 0: //copy

			for(int i = 0; i < src.length; i++){
				dst[i] = src[i]; 
			}
			break;
		case 1: // Minimumfilter 

			for(int y = 1; y < height-1 ; y++){
				for(int x = 1; x < width-1; x++){
					int pos = y*width+x; 
					ArrayList<Integer> values = new ArrayList<Integer>();

					for(int i = -1; i <= 1; i++){
						for(int j = -1; j <= 1; j++){

							int yn = y-i;
							if(yn < 0) yn = 0;
							if(yn >= height) yn = height-1;

							int xn = x-j;
							if(xn < 0) xn = 0; 
							if(xn >= width) xn = width-1; 

							int posn = yn*width+xn; 
							values.add(src[posn]);							
						}						
					}
					Collections.sort(values); 
					int gr = values.get(0);
					dst[pos] = 0xff000000 | gr << 16 | gr <<8 | gr;
				}
			}
			break; 
		case 2: // Maximumfilter 

			for(int y = 1; y < height-1 ; y++){
				for(int x = 1; x < width-1; x++){

					int pos = y*width+x; 
					ArrayList<Integer> values = new ArrayList<Integer>();

					for(int i = -1; i <= 1; i++){
						for(int j = -1; j <= 1; j++){

							int yn = y-i;
							if(yn < 0) yn = 0;
							if(yn >= height) yn = height-1;

							int xn = x-j;
							if(xn < 0) xn = 0; 
							if(xn >= width) xn = width-1;
							int posn = yn*width+xn; 
							values.add(src[posn]);							
						}						
					}
					Collections.sort(values); 
					int gr =values.get(8);
					dst[pos]=0xff000000 | gr << 16 | gr <<8 | gr; 
				}
			}
			break; 
		case 3: // Boxfilter

			for(int y = 1; y < height-1 ; y++){
				for(int x = 1; x < width-1; x++){

					int pos = y*width+x; 
					ArrayList<Integer> values = new ArrayList<Integer>();

					for(int i = -1; i <= 1; i++){
						for(int j = -1; j <= 1; j++){

							int yn = y-i;
							if(yn < 0) yn = 0;
							if(yn >= height) yn = height-1;

							int xn = x-j;
							if(xn < 0) xn = 0; 
							if(xn >= width) xn = width-1;

							int posn = yn*width+xn; 
							values.add(src[posn]&0xff);							
						}						
					}

					int gr = 0;
					for(int v = 0; v < values.size(); v++){
						gr += values.get(v);
					}

					gr = (int)(gr/9);

					dst[pos]=0xff000000 | gr << 16 | gr <<8 | gr; 
				}
			}
			break;
		case 4: // Medianfilter 

			for(int y = 1; y < height-1 ; y++){
				for(int x = 1; x < width-1; x++){
					
					int pos = y*width+x; 
					ArrayList<Integer> values = new ArrayList<Integer>();
					
					for(int i = -1; i <= 1; i++){
						for(int j = -1; j <= 1; j++){
					
							int yn = y-i;
							if(yn < 0) yn = 0;
							if(yn >= height) yn = height-1;

							int xn = x-j;
							if(xn < 0) xn = 0; 
							if(xn >= width) xn = width-1;
						
							int posn = yn*width+xn; 
							values.add(src[posn]);							
						}						
					}
					Collections.sort(values); 			
					dst[pos]=values.get(4); 
				}
			}
		}
	}
}