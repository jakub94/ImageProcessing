// BV Ue4 WS2014/15 Vorgabe
//
// Copyright (C) 2014 by Klaus Jung

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.*;
import java.awt.*;
import java.io.File;

public class MorphFilter extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String author = "Jakub Mueller & Dennis Schwabe";		// TODO: type in your name here
	private static final String initialFilename = "rhino_part.png";
	//private static final String initialFilename = "white.png";
	private static final File openPath = new File(".");
	private static final int borderWidth = 5;
	private static final int maxWidth = 445;
	private static final int maxHeight = maxWidth;

	private static JFrame frame;

	private ImageView filterView;			// source image view


	private int[] origPixels = null;
	private JLabel statusLine = new JLabel("    "); // to print some status text

	//treshold slider and label
	private JLabel tresholdLabel;
	private JLabel tresholdAmountLabel;
	private JSlider tresholdSlider;

	//dilation / filter 1 slider and label
	private JLabel dilationLabel;
	private JLabel dilationAmountLabel;
	private JSlider dilationSlider;

	//erosion / filter 2 slider and label
	private JLabel erosionLabel;
	private JLabel erosionAmountLabel;
	private JSlider erosionSlider;

	private double tresholdAmount = 0.0;
	private double dilationAmount = 0; 
	private double erosionAmount = 0; 

	private int[] binary; //BinaryPictureArray
	private int[] dilated; //DilatedPictureArray
	private int[] eroded;
	private int[] inverted; // InvertedPictureArray


	public MorphFilter() {
		super(new BorderLayout(borderWidth, borderWidth));

		setBorder(BorderFactory.createEmptyBorder(borderWidth,borderWidth,borderWidth,borderWidth));

		// load the default image
		File input = new File(initialFilename);

		if(!input.canRead()) input = openFile(); // file not found, choose another image

		filterView = new ImageView(input);
		filterView.setMaxSize(new Dimension(maxWidth, maxHeight));

		// convert to grayscale
		makeGray(filterView);

		// keep a copy of the grayscaled original image pixels
		origPixels = filterView.getPixels().clone();

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
				makeGray(filterView);  
				// keep a copy of the grayscaled original image pixels
				origPixels = filterView.getPixels().clone();
				calculate(true);
			}        	
		});
		
		// reset button
		JButton reset = new JButton("Reset Slider");
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tresholdAmount = 127;
				tresholdSlider.setValue(127);
				dilationAmount = 0;
				dilationSlider.setValue(0);
				erosionAmount = 0;
				erosionSlider.setValue(0);
				calculate(true);
			}
		});

		// amount of treshold
		tresholdLabel = new JLabel("Treshold:");
		tresholdAmountLabel = new JLabel("" + 127);
		tresholdSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, 127);
		tresholdSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				tresholdAmount = tresholdSlider.getValue() / 100.0;
				tresholdAmountLabel.setText("" + Math.round(tresholdAmount * 100.0));
				calculate(true);
			}
		});

		//amount of dilation
		dilationLabel = new JLabel("Dilation:");
		dilationAmountLabel = new JLabel("" + 0);
		dilationSlider = new JSlider(JSlider.HORIZONTAL,-50, 50, 0);
		dilationSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				dilationAmount = dilationSlider.getValue()/10.0;
				dilationAmountLabel.setText("" + dilationAmount);  
				calculate(true); 
			}
		});

		erosionLabel = new JLabel("Erosion:");
		erosionAmountLabel = new JLabel("" + 0);
		erosionSlider = new JSlider(JSlider.HORIZONTAL, -50, 50, 0);
		erosionSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				erosionAmount = erosionSlider.getValue()/10.0;
				erosionAmountLabel.setText("" + erosionAmount); 
				calculate(true);
			}
		});

		//OPTIONAL
		
		//activate treshold label and slider
		/*tresholdLabel.setEnabled(activateTreshold);
		tresholdSlider.setEnabled(activateTreshold);
		tresholdAmountLabel.setEnabled(activateTreshold);

		//activate dilation label and slider
		dilationLabel.setEnabled(activateDilation);
		dilationSlider.setEnabled(activateDilation);
		dilationAmountLabel.setEnabled(activateDilation);

		//activate erosion label and slider
		erosionLabel.setEnabled(activateErosion);
		erosionSlider.setEnabled(activateErosion);
		erosionAmountLabel.setEnabled(activateErosion);*/

		controls.add(load, c);
		controls.add(reset, c);

		//load treshold label and slider
		controls.add(tresholdLabel, c);
		controls.add(tresholdSlider, c);
		controls.add(tresholdAmountLabel, c);

		//load dilation label and slider
		controls.add(dilationLabel, c);
		controls.add(dilationSlider, c);

		//load erosion label and slider
		controls.add(erosionLabel, c);
		controls.add(erosionSlider, c);

		// images panel
		JPanel images = new JPanel(new GridLayout(1,2));
		images.add(filterView);

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
			filterView.loadImage(file);
			filterView.setMaxSize(new Dimension(maxWidth, maxHeight));
			frame.pack();
		}
	}

	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Morphological Filters - " + author);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JComponent newContentPane = new MorphFilter();
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

	private void calculate(boolean doOperation) {

		long startTime = System.currentTimeMillis();
		filterView.setPixels(origPixels);

		applyBinary(); 

		applyDilation();
		applyErosion();

		//filter1 = filterView.getPixels(); 
		filterView.applyChanges(); 

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

	private int[] binaryImage(ImageView iV, int threshold) 
	{
		int[] pixels = iV.getPixels();
		int[] pixelsN = new int[pixels.length]; 
		int width = iV.getImgWidth();
		int height = iV.getImgHeight();

		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {

				int pos = y * width + x;
				int argb = pixels[pos];  // Lesen der Originalwerte 

				int r = (argb >> 16) & 0xff;
				int g = (argb >>  8) & 0xff;
				int b =  argb        & 0xff;

				int greyValue256 = (r + g + b) / 3;

				int binaryValue = 0;

				binaryValue = (greyValue256 > threshold-1) ? 255 : 0;	// treshold-1, damit Bild nicht vollkommen schwarz wird

				int rn = binaryValue;
				int gn = binaryValue;
				int bn = binaryValue;

				pixelsN[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
			}
		}	
		return pixelsN;
	}	

	private int[] dilate(ImageView iV, double dilationAmount)
	{
		int[] pixels = iV.getPixels();
		int[] pixelsN = pixels.clone(); 
		int width = iV.getImgWidth();
		int height = iV.getImgHeight();

		int rad = (int)dilationAmount; 

		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {

				int pos = y * width + x;
				int argb = pixels[pos];  // Lesen der Originalwerte 			

				int r = (argb >> 16) & 0xff;
				int g = (argb >>  8) & 0xff;
				int b =  argb        & 0xff;

				int value = (r + g + b) / 3 ;

				if(value == 0)
				{
					for(int i = -rad; i <= rad; i++){
						for(int j = -rad; j <= rad; j++){

							int yn = y+i;	
							if(yn < 0) yn = 0;
							if(yn >= height) yn = height-1;

							int xn = x+j;
							if(xn < 0) xn = 0; 
							if(xn >= width) xn = width-1;

							int posn = yn*width+xn; 
							double d = (i*i + j*j);
							if(d <= dilationAmount*dilationAmount)
								pixelsN[posn] = 0xFF000000;
						}
					}
				}
			}			
		}
		return pixelsN;
	}

	public int constrain(int i)
	{
		if(i > 255)
			return 255;
		else if(i < 0)
			return 0;
		else
			return i;
	}

	public int[] invertImage(ImageView iV){
		int[] pixels = iV.getPixels();
		int[] pixelsN = new int[pixels.length]; 
		int width = iV.getImgWidth();
		int height = iV.getImgHeight();

		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {

				int pos = y * width + x;
				int argb = pixels[pos];  // Lesen der Originalwerte 			

				int r = (argb >> 16) & 0xff;
				int g = (argb >>  8) & 0xff;
				int b =  argb        & 0xff;

				int value = (r + g + b) / 3 ;

				if(value < 1)
				{
					pixelsN[pos] = 0xFFFFFFFF;
				}
				else if (value > 254)
				{
					pixelsN[pos] = 0xFF000000;
				}
			}		
		}
		return pixelsN; 
	}

	public void applyBinary()
	{
		binary = binaryImage(filterView, tresholdSlider.getValue());
		filterView.setPixels(binary);
	}
	
	public void applyDilation()
	{
		if(dilationAmount >= 0)
		{
			dilated =  dilate(filterView, dilationAmount);
			filterView.setPixels(dilated);
		}
		else if(dilationAmount < 0)
		{
			inverted = invertImage(filterView); 
			filterView.setPixels(inverted);

			dilated =  dilate(filterView, Math.abs(dilationAmount));
			filterView.setPixels(dilated);

			inverted = invertImage(filterView);			
			filterView.setPixels(inverted); 
		}
	}

	public void applyErosion()
	{
		if(erosionAmount >= 0)
		{
			inverted = invertImage(filterView); 
			filterView.setPixels(inverted);

			eroded =  dilate(filterView, erosionAmount);
			filterView.setPixels(eroded);

			inverted = invertImage(filterView);			
			filterView.setPixels(inverted); 
		}
		else if(erosionAmount < 0)
		{
			eroded =  dilate(filterView, Math.abs(erosionAmount));
			filterView.setPixels(eroded);
		}
	}
}