// BV Ue2 WS2014/15 Vorgabe
//
// Copyright (C) 2013 by Klaus Jung

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.awt.*;
import java.io.File;

public class Perspective extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String author = "Mueller_Schwabe";		// TODO: type in your name here
	private static final String initialFilename = "59009_512.jpg";
	private static final File openPath = new File(".");
	private static final int maxWidth = 920;
	private static final int maxHeight = 920;
	private static final int border = 10;
	private static final double angleStepSize = 5.0;		// size used for angle increment and decrement

	private static JFrame frame;

	private ImageView srcView = null;		// source image view
	private ImageView dstView = null;		// rotated image view

	private JComboBox<String> methodList;	// the selected interpolation method
	private JSlider angleSlider;			// the selected angle
	private JLabel statusLine;				// to print some status text
	private double angle = 0.0;				// current angle in degrees

	/**
	 * Constructor. Constructs the layout of the GUI components and loads the initial image.
	 */
	public Perspective() {
		super(new BorderLayout(border, border));

		// load the default image
		File input = new File(initialFilename);

		if(!input.canRead()) input = openFile(); // file not found, choose another image

		srcView = new ImageView(input);
		srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
		initDstView();

		// load image button
		JButton load = new JButton("Open Image");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File input = openFile();
				if(input != null) {
					srcView.loadImage(input);
					srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
					initDstView();
					calculatePerspective(false);
				}
			}        	
		});

		// selector for the rotation method
		String[] methodNames = {"Nearest Neighbour", "Bilinear Interpolation"};

		methodList = new JComboBox<String>(methodNames);
		methodList.setSelectedIndex(0);		// set initial method
		methodList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				calculatePerspective(false);
			}
		});

		// rotation angle minus button
		JButton decAngleButton = new JButton("-");
		decAngleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				angle -= angleStepSize;
				if(angle < 0) angle += 360;
				angleSlider.setValue((int)angle);
				calculatePerspective(false);
			}        	
		});

		// rotation angle plus button
		JButton incAngleButton = new JButton("+");
		incAngleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				angle += angleStepSize;
				if(angle > 360) angle -= 360;
				angleSlider.setValue((int)angle);
				calculatePerspective(false);
			}        	
		});

		// rotation angle slider
		angleSlider = new JSlider(0, 360, (int)angle);
		angleSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				angle = angleSlider.getValue();
				calculatePerspective(false);				
			}        	
		});

		// speed test button
		JButton speedTestButton = new JButton("Speed Test");
		speedTestButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				long startTime = System.currentTimeMillis();
				double lastAngle = angle;
				int cnt = 0;
				for(angle = 0 ; angle < 360; angle += angleStepSize) {
					calculatePerspective(true);
					cnt++;
				}
				long time = System.currentTimeMillis() - startTime;
				statusLine.setText("Speed Test: Calculated " + cnt + " perspcetives in " + time + " ms");
				angle = lastAngle;
			}        	
		});


		// some status text
		statusLine = new JLabel(" ");

		// arrange all controls
		JPanel controls = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0,border,0,0);
		controls.add(load, c);
		controls.add(methodList, c);
		controls.add(decAngleButton, c);
		controls.add(angleSlider, c);
		controls.add(incAngleButton, c);
		controls.add(speedTestButton, c);

		// arrange images
		JPanel images = new JPanel();
		images.add(srcView);
		images.add(dstView);

		// add to main panel
		add(controls, BorderLayout.NORTH);
		add(images, BorderLayout.CENTER);
		add(statusLine, BorderLayout.SOUTH);

		// add border to main panel
		setBorder(BorderFactory.createEmptyBorder(border,border,border,border));

		// perform the initial rotation
		calculatePerspective(false);
	}


	/**
	 * Set up and show the main frame.
	 */
	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Perspective - " + author);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JComponent contentPane = new Perspective();
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
	 * Initialize the destination view giving it the correct size.
	 */
	private void initDstView() {
		// set destination size large enough to view a substantial part of the perspective

		int width = (int)(srcView.getImgWidth() * 1.4);
		int height = (int)(srcView.getImgHeight() * 1.2);

		// create an empty destination image
		if(dstView == null)
			dstView = new ImageView(width, height);
		else
			dstView.resetToSize(width, height);

		// limit viewing dimensions
		dstView.setMaxSize(new Dimension(maxWidth, maxHeight));

		frame.pack();
	}


	/**
	 * Calculate image perspective and show result in destination view.
	 * @param silent - set true when running the speed test (suppresses the image view).
	 */
	protected void calculatePerspective(boolean silent) {

		if(!silent) {
			// present some useful information
			statusLine.setText("Angle = " + angle + " degrees.");
		}

		// get dimensions and pixels references of images
		int srcPixels[] = srcView.getPixels();
		int srcWidth = srcView.getImgWidth();
		int srcHeight = srcView.getImgHeight();
		int dstPixels[] = dstView.getPixels();
		int dstWidth = dstView.getImgWidth();
		int dstHeight = dstView.getImgHeight();

		long startTime = System.currentTimeMillis();

		switch(methodList.getSelectedIndex()) {
		case 0:	// Nearest Neigbour
			calculateNearestNeigbour(srcPixels, srcWidth, srcHeight, dstPixels, dstWidth, dstHeight, angle);
			break;
		case 1:	// Bilinear Interpolation
			calculateBilinear(srcPixels, srcWidth, srcHeight, dstPixels, dstWidth, dstHeight, angle);
			break;
		}

		if(!silent) {
			// show processing time
			long time = System.currentTimeMillis() - startTime;
			statusLine.setText("Angle = " + angle + " degrees. Processing time = " + time + " ms.");
			// show the resulting image
			dstView.applyChanges();
		}
	}

	/**
	 * Image perspective algorithm using nearest neighbour image rendering
	 * @param srcPixels - source image pixel array of loaded image (ARGB values)
	 * @param srcWidth - source image width
	 * @param srcHeight - source image height
	 * @param dstPixels - destination image pixel array to be filled (ARGB values)
	 * @param dstWidth - destination image width
	 * @param dstHeight - destination image height
	 * @param degrees - angle in degrees for the perspective
	 */
	void calculateNearestNeigbour(int srcPixels[], int srcWidth, int srcHeight, int dstPixels[], int dstWidth, int dstHeight, double degrees) {

		/**** TODO: your implementation goes here ****/
		int xs, ys, pos, posNew;

		degrees = Math.toRadians(degrees);
		double s = 0.001;
		double cosDegrees = Math.cos(degrees);
		double sinDegrees = Math.sin(degrees);

		for(int xd = 0; xd < dstWidth; xd++){
			for(int yd = 0; yd < dstHeight; yd++){

				int xdt = xd - (dstWidth / 2);	
				int ydt = yd - (dstHeight / 2);

				//Verzerrung
				ys = (int)Math.round(ydt / cosDegrees - ydt * s * sinDegrees);
				xs = (int)Math.round(xdt * (s * sinDegrees * ys + 1));
				xs = Math.round(xs + (srcWidth / 2));
				ys = Math.round(ys + (srcHeight / 2));

				pos = ys * srcWidth + xs;
				posNew = yd * dstWidth + xd;

				if(((ys < 0) || (ys >= srcHeight)) || (xs < 0) || (xs >= srcWidth)){ // RANDBEDINGUNG
					dstPixels[posNew] = 0xEEEEEE; 
				}
				else{
					dstPixels[posNew] = srcPixels[pos];
				}
			}
		}
	}

	/**
	 * Image perspective algorithm using bilinear interpolation
	 * @param srcPixels - source image pixel array of loaded image (ARGB values)
	 * @param srcWidth - source image width
	 * @param srcHeight - source image height
	 * @param dstPixels - destination image pixel array to be filled (ARGB values)
	 * @param dstWidth - destination image width
	 * @param dstHeight - destination image height
	 * @param degrees - angle in degrees for the perspective
	 */
	void calculateBilinear(int srcPixels[], int srcWidth, int srcHeight, int dstPixels[], int dstWidth, int dstHeight, double degrees) {

		/**** TODO: your implementation goes here ****/
		int pos, posNew, xi, yi;
		double xs, ys, h, v; 

		degrees = Math.toRadians(degrees);
		double s = 0.001;
		double cosDegrees = Math.cos(degrees);
		double sinDegrees = Math.sin(degrees);

		for(int xd = 0; xd < dstWidth; xd++){
			for(int yd = 0; yd < dstHeight; yd++){

				//Change Coordinate System: Zero --> Center of Picture
				int xdt = xd - (dstWidth / 2);	
				int ydt = yd - (dstHeight / 2);
				
				//Perspektivische Verzerrung
				ys = ydt / (cosDegrees - ydt * s * sinDegrees);
				xs = xdt * (s * sinDegrees * ys + 1);
				xs = xs + (srcWidth / 2);
				ys = ys + (srcHeight / 2);

				xi = (int)Math.floor(xs);
				yi = (int)Math.floor(ys);
				
				h = xs - xi;
				v = ys - yi;

				//pos = (int)(ys * srcWidth + xs);
				posNew = yd * dstWidth + xd;	

				if(((ys < 0) || (ys >= srcHeight)) || (xs < 0) || (xs >= srcWidth)) { // RANDBEDINGUNG
					dstPixels[posNew] = 0xEEEEEE; 
				}
				else {
					int[] rgb = getNewPixelValue(srcPixels, srcWidth, srcHeight, xi, yi, h, v);
					dstPixels[posNew] = (0xFF<<24) | (rgb[0]<<16) | (rgb[1] << 8) | rgb[2];		
				}
			}
		}
	}

	private int[] getNewPixelValue(int[] origPix, int origWidth, int origHeight, int x, int y, double h, double v)
	{
		double[] newPix = {0,0,0};
		int pos = 0;
		double factor = 0;

		//Randbehandlung = original pixel
		//TODO Evtl. ordentliche Randbehandlung
		if((y == (origHeight-1))  || (x == (origWidth-1)))
		{
			int argb = origPix[(y*origWidth) + x];
			int r = (argb >> 16) & 0xff;
			int g = (argb >>  8) & 0xff;
			int b =  argb        & 0xff;
			return new int[]{r,g,b};
		}

		for(int i = y; i < y+2; i++) {
			for(int j = x; j < x+2; j++) {
			
				pos = (i * origWidth) + j;

				if(i == y && j == x)  					//A
					factor = (1-h)*(1-v);

				else if((i == (y)) && (j == (x+1))) 	//B
					factor = (h)*(1-v);				

				else if((i == (y+1)) && (j == x)) 		//C
					factor =  (1-h)*(v);

				else if((i == (y+1)) && (j == (x+1))) 	//D
					factor = (h*v);

				newPix = calculateValue(origPix[pos], newPix, factor);
			}
		}

		// newPix as int array
		int[] intNewPix = new int[3];

		// put double values from newPix as int values into intNewPix
		for (int i = 0; i < newPix.length; ++i) {
			intNewPix[i] = (int)Math.round(newPix[i]);
		}

		return constrain(intNewPix);
	}

	private int[] constrain(int[] rgb)
	{
		for(int i = 0; i < rgb.length; i++)
		{
			if(rgb[i] > 255)
				rgb[i] =  255;

			else if(rgb[i] < 0)
				rgb[i] =  0;
		}
		return rgb;
	}
	
	private double[] calculateValue(int origPix, double[] newPixel, double factor)
	{
		int r = (origPix >> 16) & 0xff;
		int g = (origPix >>  8) & 0xff;
		int b =  origPix        & 0xff;

		newPixel[0] += (r * factor);
		newPixel[1] += (g * factor);
		newPixel[2] += (b * factor);

		return newPixel;
	}
}