// BV Ue05 WS2014/15 Vorgabe
//
// Copyright (C) 2014 by Klaus Jung

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

public class ImageAnalysis extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String author = "Mueller_Schwabe";		// TODO: type in your name here
	private static final String initialFilename = "mountains.png";
	private static final File openPath = new File(".");
	private static final int border = 10;
	private static final int maxWidth = 920;
	private static final int maxHeight = 920;
	private static final int graySteps = 256;

	private static JFrame frame;

	private ImageView imgView;						// image view
	private ImageView tempView; 					// temporary view (no inplace)
	private HistoView histoView = new HistoView();	// histogram view
	private StatsView statsView = new StatsView();	// statistic values view
	private JSlider brightnessSlider;				// brightness Slider
	private JSlider contrastSlider; 				// contrast Slider  



	// TODO: add an array to hold the histogram of the loaded image
	int[] histo = new int[256];

	// TODO: add an array that holds the ARGB-Pixels of the originally loaded image
	int[] origPixels;

	// TODO: add a contrast slider
	private JLabel statusLine;				// to print some status text

	/**
	 * Constructor. Constructs the layout of the GUI components and loads the initial image.
	 */
	public ImageAnalysis() {
		super(new BorderLayout(border, border));

		// load the default image
		File input = new File(initialFilename);

		if(!input.canRead()) input = openFile(); // file not found, choose another image

		imgView = new ImageView(input);
		imgView.setMaxSize(new Dimension(maxWidth, maxHeight));
		
		tempView = new ImageView(input);
		tempView.setMaxSize(new Dimension(maxWidth, maxHeight));

		// TODO: set the histogram array of histView and statsView

		// TODO: initialize the original ARGB-Pixel array from the loaded image
		origPixels = imgView.getPixels().clone(); 	

		// load image button
		JButton load = new JButton("Open Image");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File input = openFile();
				if(input != null) {
					imgView.loadImage(input);
					imgView.setMaxSize(new Dimension(maxWidth, maxHeight));

					// TODO: initialize the original ARGB-Pixel array from the newly loaded image


					frame.pack();
					processImage();
				}
			}        	
		});

		JButton reset = new JButton("Reset Slider");
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				brightnessSlider.setValue(0);
				// TODO: reset contrast slider
				contrastSlider.setValue(100);
				processImage();
			}        	
		});

		// some status text
		statusLine = new JLabel(" ");

		// top view controls
		JPanel topControls = new JPanel(new GridBagLayout());
		topControls.add(load);
		topControls.add(reset);

		// center view
		JPanel centerControls = new JPanel();
		JPanel rightControls = new JPanel();
		rightControls.setLayout(new BoxLayout(rightControls, BoxLayout.Y_AXIS));
		centerControls.add(imgView);
		rightControls.add(histoView);
		rightControls.add(statsView);
		centerControls.add(rightControls);
		
		// bottom view controls
		JPanel botControls = new JPanel();
		botControls.setLayout(new BoxLayout(botControls, BoxLayout.Y_AXIS));

		// brightness slider
		brightnessSlider = new JSlider(-graySteps, graySteps, 0);
		TitledBorder titBorder = BorderFactory.createTitledBorder("Brightness");
		titBorder.setTitleColor(Color.GRAY);
		brightnessSlider.setBorder(titBorder);
		brightnessSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				processImage();				
			}        	
		});

		// TODO: setup contrast slider
		
		//  contrast slider
		contrastSlider = new JSlider(0, 200, 100); 
		TitledBorder titBorder2 = BorderFactory.createTitledBorder("Contrast");
		titBorder2.setTitleColor(Color.GRAY);
		contrastSlider.setBorder(titBorder2);
		contrastSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				processImage();				
			}        	
		});

		botControls.add(contrastSlider);		
		botControls.add(brightnessSlider);
		statusLine.setAlignmentX(Component.CENTER_ALIGNMENT);
		botControls.add(statusLine);

		// add to main panel
		add(topControls, BorderLayout.NORTH);
		add(centerControls, BorderLayout.CENTER);
		add(botControls, BorderLayout.SOUTH);

		// add border to main panel
		setBorder(BorderFactory.createEmptyBorder(border,border,border,border));

		// perform the initial rotation
		processImage();
	}


	/**
	 * Set up and show the main frame.
	 */
	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Image Analysis - " + author);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JComponent contentPane = new ImageAnalysis();
		contentPane.setOpaque(true); //content panes must be opaque
		frame.setContentPane(contentPane);

		// display the window
		frame.pack();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
		frame.setVisible(true);
	}

	/**
	 * Main method. 
	 * @param args - ignored. No arguments are used by this application.
	 */
	public static void main(String[] args) {
		// schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	/**
	 * Open file dialog used to select a new image.
	 * @return The selected file object or null on cancel.
	 */
	private File openFile() {
		// file open dialog
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
		chooser.setFileFilter(filter);
		chooser.setCurrentDirectory(openPath);
		int ret = chooser.showOpenDialog(this);
		if(ret == JFileChooser.APPROVE_OPTION) return chooser.getSelectedFile();
		return null;		
	}

	/**
	 * Update image with new brightness and contrast values.
	 * Update histogram, histogram view and statistics view.
	 */
	protected void processImage() {

		long startTime = System.currentTimeMillis();

		
		int height = imgView.getImgHeight();
		int width = imgView.getImgWidth();
		
		double contrastAmount = contrastSlider.getValue()/100.0; 
		int brightnessAmount = brightnessSlider.getValue();
		
		int[] adjustedBrightness = brighten(tempView, brightnessAmount);
//		imgView.setPixels(adjustedBrightness); 
		tempView.setPixels(adjustedBrightness);
		
		int[] adjustedContrast = changeContrast(tempView, contrastAmount);
		imgView.setPixels(adjustedContrast);
//		tempView.setPixels(adjustedContrast);

		int[] pixels = imgView.getPixels();
		
		// TODO: add your processing code here
		Arrays.fill(histo, 0);
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int pos = y * width + x; 
				int argb = pixels[pos];

				int r = (argb >> 16) & 0xFF; 
				int g = (argb >> 8) & 0xFF; 
				int b =  argb & 0xFF; 
				int greyValue = (r+g+b)/3; 

				//System.out.println(greyValue);

				histo[greyValue] += 1; 
			}
		}  

		histoView.setHistogram(histo);
		statsView.setHistogram(histo); 

		imgView.applyChanges();
		histoView.update();
		statsView.update();

		// show processing time
		long time = System.currentTimeMillis() - startTime;
		statusLine.setText("Processing time = " + time + " ms.");
	}

	public int[] brighten(ImageView img, int brightnessAmout)
	{
		int height = img.getImgHeight();
		int width = img.getImgWidth();
		int[] pixelsN = new int[origPixels.length]; 
		
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int pos = y * width + x; 
				int argb = origPixels[pos];

				int r = (argb >> 16) & 0xFF; 
				int g = (argb >> 8) & 0xFF; 
				int b =  argb & 0xFF; 
				
				r = constrain(r += brightnessAmout);
				g = b = r; 
//				g = constrain(g += brightnessAmout);
//				b = constrain(b += brightnessAmout);
				
				pixelsN[pos] = 0xFF000000 | (r<<16) | (g<<8) | b;
			}
		}
		return pixelsN; 
	}
	
	public int[] changeContrast(ImageView img, double contrastAmount)
	{
	
		int height = img.getImgHeight();
		int width = img.getImgWidth();
		int[] pixelsN = img.getPixels(); 
//		int[] pixelsN = new int[origPixels.length]; 
		double subtractValue = 0;
		
		if(contrastAmount != 1)
			subtractValue = (contrastAmount - 1) * 127;
			
//		System.out.println(contrastAmount);
//		System.out.println(subtractValue);
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {

				int pos = y * width + x; 
//				int argb = origPixels[pos];
				int argb = pixelsN[pos]; 
				
				int r = (argb >> 16) & 0xFF; 
				int g = (argb >> 8) & 0xFF; 
				int b =  argb & 0xFF; 
				
				r = constrain((int)(contrastAmount*r-subtractValue));
				g = b = r; 
//				g = constrain((int)(contrastAmount*g-subtractValue));
//				b = constrain((int)(contrastAmount*b-subtractValue)); 
				
				pixelsN[pos] = 0xFF000000 | (r<<16) | (g<<8) | b;
			}
		}
		return pixelsN; 
	}
	
	public int constrain(int i)
	{
		if(i > 255)
			return 255;
		else if (i < 0)
			return 0;
		return i;
	}
}