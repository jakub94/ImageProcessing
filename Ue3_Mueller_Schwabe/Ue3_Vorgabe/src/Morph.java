// Uebung 3 Vorlage WS2014/15
// Copyright (C) 2014 by Klaus Jung
// All rights reserved.
// 
// Date: 2014-10-31


import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

public class Morph extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final int borderWidth = 5;
	private static final int maxWidth = 460;
	private static final int maxHeight = 320;
	private static final File openPath = new File(".");
	private static final int sliderGranularity = 100;

	private static final double scalingX = 1.0 / 0.76;
	private static final double scalingY = 1.0 / 0.66;

	private static JFrame frame;

	private ImageView startView;		// image view for start picture
	private ImageView morphView;		// image view for intermediate picture
	private ImageView endView;			// image view for end picture

	private double morphPos;			// 0.0 is "start", 1.0 is "end"

	private JSlider morphSlider;		// slider for current morphing position
	private JComboBox<String> method;	// the selected transmission method
	private JLabel statusLine;			// to print some status text


	public Morph() {
		super(new BorderLayout(borderWidth, borderWidth));

		setBorder(BorderFactory.createEmptyBorder(borderWidth,borderWidth,borderWidth,borderWidth));

		// load the default images
		File input1 = new File("RedApple.jpg");
		if(!input1.canRead()) input1 = openFile("Open Image 1"); // file not found, choose another image

		File input2 = new File("GreenApple.jpg");
		if(!input2.canRead()) input2 = openFile("Open Image 2"); // file not found, choose another image

		startView = new ImageView(input1);
		startView.setMaxSize(new Dimension(maxWidth, maxHeight));

		endView = new ImageView(input2);
		endView.setMaxSize(new Dimension(maxWidth, maxHeight));

		// create empty image for morphing
		morphView = new ImageView(startView.getImgWidth(), startView.getImgHeight());
		morphView.setMaxSize(new Dimension(maxWidth, maxHeight));

		// control panel
		JPanel controls = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0,borderWidth,0,0);

		// transmission methods
		String[] methodNames = {"Cross-fading", "Scale left image", "Scale right image", "Scale & move left image", "Scale & move right image", "Morphing"};

		method = new JComboBox<String>(methodNames);
		method.setSelectedIndex(0);		// set initial method
		method.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				calculate();
			}
		});
		// morphing position
		JLabel morphLabel = new JLabel("Morphing Position:");
		morphPos = 0;
		morphSlider = new JSlider(JSlider.HORIZONTAL, 0, sliderGranularity, (int)(morphPos * sliderGranularity));
		morphSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				morphPos = morphSlider.getValue() / (double)sliderGranularity;
				calculate();
			}
		});

		controls.add(method, c);
		controls.add(morphLabel, c);
		controls.add(morphSlider, c);

		// images
		JPanel images = new JPanel(new GridLayout(1,3));
		images.add(startView);
		images.add(morphView);
		images.add(endView);

		// status panel
		JPanel status = new JPanel(new GridLayout(1,3));

		// some status text
		statusLine = new JLabel(" ");
		status.add(statusLine, c);


		add(controls, BorderLayout.NORTH);
		add(images, BorderLayout.CENTER);
		add(status, BorderLayout.SOUTH);

		calculate();

	}

	private File openFile(String title) {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
		chooser.setFileFilter(filter);
		chooser.setCurrentDirectory(openPath);
		int ret = chooser.showDialog(this, title);
		if(ret == JFileChooser.APPROVE_OPTION) return chooser.getSelectedFile();
		return null;		
	}


	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Morph");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JComponent newContentPane = new Morph();
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

	private synchronized void calculate() {
		long startTime = System.currentTimeMillis();

		switch(method.getSelectedIndex()) {
		case 0:
			crossfade();
			break;
		case 1:
			scaleLeft();
			break;
		case 2: 
			scaleRight();
			break; 
		case 3: 
			scaleMoveLeft();
			break;
		case 4:
			scaleMoveRight();
			break; 
		case 5:
			morphing();
			break;
		default:
			Arrays.fill(morphView.getPixels(), 0xffffffff); // white image
			break;
		}

		morphView.applyChanges();

		long time = System.currentTimeMillis() - startTime;
		statusLine.setText("Processing time: " + time + " ms");
	}

	void crossfade() {
		int[] pixA = startView.getPixels();
		int[] pixM = morphView.getPixels();
		int[] pixB = endView.getPixels();

		int width = morphView.getImgWidth();
		int height = morphView.getImgHeight();
		double a = morphPos;

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				
				int posM = y * width + x;

				int argb = pixA[posM];
				int rA = (argb >> 16) & 0xff;
				int gA = (argb >>  8) & 0xff;
				int bA = (argb)       & 0xff;

				argb = pixB[posM];

				int rB = (argb >> 16) & 0xff;
				int gB = (argb >>  8) & 0xff;
				int bB = (argb)       & 0xff;

				int rM = (int) ((1 - a) * rA + a * rB);
				int gM = (int) ((1 - a) * gA + a * gB);
				int bM = (int) ((1 - a) * bA + a * bB);

				pixM[posM] = 0xFF000000 | (rM << 16) | (gM << 8) | bM;
			}
		}
	}

	void scaleLeft() {

		// This implements a simple nearest neighbor scaling.
		// You may replace it by a bilinear scaling for better visual results

		int[] pixA = startView.getPixels();
		int[] pixM = morphView.getPixels();

		int width = morphView.getImgWidth();
		int height = morphView.getImgHeight();
		double a = morphPos;

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				
				int posM = y * width + x;

				// current scaling
				double xS = scalingX * a + 1.0 * (1 - a);
				double yS = scalingY * a + 1.0 * (1 - a);

				// scaled coordinates in image A
				int xA = (int)(x * xS);
				int yA = (int)(y * yS);

				int argb = 0xffffffff; // white pixel

				if(xA >= 0 && xA < width && yA >= 0 && yA < height) {
					// we are inside image A
					argb = pixA[yA * width + xA];
				}

				int rM = (argb >> 16) & 0xff;
				int gM = (argb >>  8) & 0xff;
				int bM = (argb)       & 0xff;

				pixM[posM] = 0xFF000000 | (rM << 16) | (gM << 8) | bM;
			}
		}
	}

	void scaleMoveLeft()
	{
		// This implements a simple nearest neighbor scaling.
		// You may replace it by a bilinear scaling for better visual results

		int[] pixA = startView.getPixels();
		int[] pixM = morphView.getPixels();

		int width = morphView.getImgWidth();
		int height = morphView.getImgHeight();
		double a = morphPos;

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
			
				int posM = y * width + x;

				// current scaling
				double xS = scalingX * a + 1.0 * (1 - a);
				double yS = scalingY * a + 1.0 * (1 - a);

				// scaled coordinates in image A
				int xA = (int)(x * xS) - (int) ((width * a)*(1 / scalingX));
				int yA = (int)(y * yS) - (int) ((height * a)*(0.7 / scalingY));

				int argb = 0xffffffff; // white pixel

				if(xA >= 0 && xA < width && yA >= 0 && yA < height) {
					// we are inside image A
					argb = pixA[yA * width + xA];
				}

				int rM = (argb >> 16) & 0xff;
				int gM = (argb >>  8) & 0xff;
				int bM = (argb)       & 0xff;

				pixM[posM] = 0xFF000000 | (rM << 16) | (gM << 8) | bM;
			}
		} 
	}

	void scaleMoveRight() {

		// This implements a simple nearest neighbor scaling.
		// You may replace it by a bilinear scaling for better visual results

		int[] pixB = endView.getPixels();
		int[] pixM = morphView.getPixels();

		int width = morphView.getImgWidth();
		int height = morphView.getImgHeight();
		double a = morphPos;

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				
				int posM = y * width + x;

				// current scaling
				double xS = scalingX * (1-a) + 1.0 * (1 - (1-a));
				double yS = scalingY * (1-a) + 1.0 * (1 - (1-a));

				// scaled coordinates in image B
				int xB = (int)(x / xS) + (int) ((width * (1-a))*(1 / scalingX / scalingX));
				int yB = (int)(y / yS) + (int) ((height * (1-a))*(0.5 / scalingY / scalingY));



				int argb = 0xffffffff; // white pixel

				if(xB >= 0 && xB < width && yB >= 0 && yB < height) {
					// we are inside image B
					argb = pixB[yB * width + xB];
				}

				int rM = (argb >> 16) & 0xff;
				int gM = (argb >>  8) & 0xff;
				int bM = (argb)       & 0xff;

				pixM[posM] = 0xFF000000 | (rM << 16) | (gM << 8) | bM;
			}
		}
	}

	void scaleRight() {

		// This implements a simple nearest neighbor scaling.
		// You may replace it by a bilinear scaling for better visual results

		int[] pixB = endView.getPixels();
		int[] pixM = morphView.getPixels();

		int width = morphView.getImgWidth();
		int height = morphView.getImgHeight();
		double a = morphPos;

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				
				int posM = y * width + x;

				// current scaling
				double xS = scalingX * a + 1.0 * (1 - a);
				double yS = scalingY * a + 1.0 * (1 - a);

				// scaled coordinates in image B
				int xB = (int)(x / xS);
				int yB = (int)(y / yS);

				int argb = 0xffffffff; // white pixel

				if(xB >= 0 && xB < width && yB >= 0 && yB < height) {
					// we are inside image B
					argb = pixB[yB * width + xB];
				}

				int rM = (argb >> 16) & 0xff;
				int gM = (argb >>  8) & 0xff;
				int bM = (argb)       & 0xff;

				pixM[posM] = 0xFF000000 | (rM << 16) | (gM << 8) | bM;
			}
		}
	}

	void morphing() {
		int[] pixA = startView.getPixels();
		int[] pixM = morphView.getPixels();
		int[] pixB = endView.getPixels();

		int width = morphView.getImgWidth();
		int height = morphView.getImgHeight();
		double a = morphPos;
		
		//ScaleMoveRight
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				
				int posM = y * width + x;

				// current scaling
				double xS = scalingX * (1-a) + 1.0 * (1 - (1-a));
				double yS = scalingY * (1-a) + 1.0 * (1 - (1-a));

				// scaled coordinates in image B
				int xB = (int)(x / xS) + (int)((width * (1-a)) * (1 / scalingX / scalingX));
				int yB = (int)(y / yS) + (int)((height * (1-a)) * (0.5 / scalingY / scalingY));

				int argb = 0xffffffff; // white pixel

				if(xB >= 0 && xB < width && yB >= 0 && yB < height) {
					// we are inside image B
					argb = pixB[yB * width + xB];
				}

				int rM = (argb >> 16) & 0xff;
				int gM = (argb >>  8) & 0xff;
				int bM = (argb)       & 0xff;

				pixM[posM] = 0xFF000000 | (rM << 16) | (gM << 8) | bM;
			}
		}
		
		pixB = pixM.clone(); 
		
		//ScaleMoveLeft
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int posM = y * width + x;

				// current scaling
				double xS = scalingX * a + 1.0 * (1 - a);
				double yS = scalingY * a + 1.0 * (1 - a);

				// scaled coordinates in image A
				int xA = (int)(x * xS) - (int)((width * a)*(1 / scalingX));
				int yA = (int)(y * yS) - (int) ((height * a)*(0.7 / scalingY));

				int argb = 0xffffffff; // white pixel

				if(xA >= 0 && xA < width && yA >= 0 && yA < height) {
					// we are inside image A
					argb = pixA[yA * width + xA];
				}

				int rM = (argb >> 16) & 0xff;
				int gM = (argb >>  8) & 0xff;
				int bM = (argb)       & 0xff;

				pixM[posM] = 0xFF000000 | (rM << 16) | (gM << 8) | bM;
			}
		} 
		
		pixA = pixM.clone(); 
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int posM = y * width + x;

				int argb = pixA[posM];
				int rA = (argb >> 16) & 0xff;
				int gA = (argb >>  8) & 0xff;
				int bA = (argb)       & 0xff;

				argb = pixB[posM];

				int rB = (argb >> 16) & 0xff;
				int gB = (argb >>  8) & 0xff;
				int bB = (argb)       & 0xff;

				int rM = (int) ((1 - a) * rA + a * rB);
				int gM = (int) ((1 - a) * gA + a * gB);
				int bM = (int) ((1 - a) * bA + a * bB);

				pixM[posM] = 0xFF000000 | (rM << 16) | (gM << 8) | bM;
			}
		}
	}
}