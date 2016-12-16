/**
 * Webcam capture library is NOT MINES. 
 * WEBCAM CAPTURE LIBARY BY sarxos:  https://github.com/sarxos/webcam-capture
 * 
 * HanGesture By: https://www.youtube.com/user/toy741life
 * Video Demo: https://www.youtube.com/watch?v=Y6oLbRKwmPk
 */

import com.github.sarxos.webcam.Webcam;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.RescaleOp;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.util.ArrayList;

/*
 * Hand gesture detection.
 */
public class HandGesture extends JFrame implements Runnable, MouseListener, KeyListener{
	
	//Data collection mode or mouse control mode
	//If data collection mode is false then it will go into hand gesture prediction mode (Python client will need to connect to this server)
	private boolean dataCollectionMode = false;
	private boolean mouseControlMode = false;
	
	/*If this value is true, the none detection will be done with neural network prediction.
	The reason for this variable is, the low level Python library Theano is heavily 
	CPU intensive. So, if there is no hand in the camera, we might as well just make it 100% NONE 
	without having to constantly ask the neural network if it's seeing NONE.*/
	private boolean detectNoneWithNeuralNetwork = false; //detect no gesture with neural network OR not 
	
    //Server socket for Python to connect to
	private ServerSocket serverSocket;
	private Socket socket;
	private boolean connected = false; //if server is connected to client
    
	private int width,height; //height and width of camera
	private Webcam webcam; //webcam from sarxos libary 
	private int[] pixelRaster = null; //pixel raster for initial cam image
	private BufferedImage initialWebcamImage; //initial cam image

	private String realTimePath = "C:/Users/Pabla/Desktop/ImageAnalysis/Tests/AdvancedCarModel/real_time.png"; //PICK A REAL TIME PATH LOCATION. Python's client will need this location
	
	//Data writing into files
	private PrintWriter writer; //writer to write to file
	private File rawData; //raw data file 
	private String rawDataFilename = "raw_data.txt"; //location where raw data would be created upon data collection being true

	private String[] gestureTypes = new String[]{"Ack","Fist","Hand","One","Straight", "Palm", "Thumbs", "None", "Swing", "Peace"}; //types of gestures
    
    // Image collection information 
	private int imageNumber= 0; //current image number being taken
	private int maxImageNumber = 500; //max image numbers
	private int imageCounter = 0; // wait timer before it equals reset count 
	private int resetCount = 1; // ticks to wait before image is taken
    
	private boolean clicked = false; // window location is clicked
	
	private int gestureIndex = 8; //Gesture index to collect data for
	private int currentGestureIndex = 7; //default to NONE
	
	private ArrayList<Integer[]> count = new ArrayList<Integer[]>(); //actions stored in here temporarily before being removed
	int searchDepth = 7; //Depth of actions stored in the count

    int move = 7; //keeps track to previous move
    
    Robot robot; //Robot to move the mouse
    
    Graphics bufferGraphics; //Double buffer!!! Very important to have no jitter effects 
    Image offscreen; // double buffer image
    
    Rectangle boxPosition; //Red box location
    
    public static void main(String[] args) throws Exception {
    	HandGesture gesture = new HandGesture(); //make an image analysis object
    	
        Thread thread = new Thread(gesture); //create gesture thread
        thread.start();//start thread
    }

    /**
     * Initializes webcam, buffered image, 2D pixel raster, server and robot.
     * Server and robot only initialized IF data collection mode is false.
     */
    public HandGesture() {
    	
    	//get default webcam connected to this computer
        webcam = Webcam.getDefault();
        webcam.open(); //open webcam communication
        
        //get webcam dimensions 
        width = webcam.getViewSize().width; 
        height = webcam.getViewSize().height;

        //initialize image buffer and pixel raster initialized according to buffer size
        initialWebcamImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB); 
        pixelRaster = ((DataBufferInt) initialWebcamImage.getRaster().getDataBuffer()).getData();
        
        //window setups
        setSize(width*3, height*4);
        setTitle("Hand Gesture Detection");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(null);
		setVisible(true);
		
		// add listeners 
		addMouseListener(this);
		addKeyListener(this);
		
		// add double buffer 
		offscreen = createImage(width*3,height*4);  
        bufferGraphics = offscreen.getGraphics(); 
		
		
		//if data collection mode is false start server and robot
		if(dataCollectionMode == false){
			
			// start robot
			try {
				robot = new Robot();
			} 
			catch (AWTException e1) {
				e1.printStackTrace();
			}
			
			//start server
			try {
				serverSocket = new ServerSocket(12345);
				socket = serverSocket.accept();
				connected = true;
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}	
		//else this is data collection mode
		else {
			rawData = new File(rawDataFilename); //raw data file
			
			// if it does not exist
			if(!rawData.exists()) {  
				
				//create new raw data file
				try {
					rawData.createNewFile();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			// create writer to be able to write to raw data file
			try {
				writer = new PrintWriter(rawData);
			} 
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
    }

    public void paint(Graphics graphic) {
    	
    	initialWebcamImage = webcam.getImage(); //get image
    	
    	//min and max bounds of the detected box
    	int minX = 10000;
    	int maxX = -10000;
    	int minY = 10000;
    	int maxY = -10000;

    	Rectangle handBound = null; //hand bound location
    	
    	BufferedImage tempInitialWebcamImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB); //temporary webcam image
    	BufferedImage newImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB); //50px by 50px image that will be fed into the neural network

    	Vector<Rectangle> listOfFoundObjects = new Vector<Rectangle>(); //list of found objects
    	
    	//Initialize rasters 
    	int[] tempRaster = new int[width*height]; //temp raster
    	
    	int[][] pixelRaster2D = new int [height][width]; //converting pixelRaster to 2D format to check for surrounding pixels 
    	int[][] tempRaster2D = new int [height][width]; //temp raster for initial image
    	int[][] densityRaster = new int [height][width]; //raster for density
    	int[][] clusterRaster = new int [height][width]; //raster for cluster
    	
    	int[] guess = new int[gestureTypes.length]; ///prediction of neural network for this tick
    	int index = 0; //used to access pixel raster when running through 2D array
    	
    	//Increase image contrast
    	RescaleOp op = new RescaleOp(2f, 0, null); //incerase contract by 2 times the scale factor
    	initialWebcamImage = op.filter(initialWebcamImage, initialWebcamImage); //use filter to update the camera image
    	
    	//get rasters
    	initialWebcamImage.getRGB(0, 0, width, height, pixelRaster, 0, width); // get pixel raster
    	initialWebcamImage.getRGB(0, 0, width, height, tempRaster, 0, width); //get temp raster

    	index = 0; 
    	
    	//First pass, get all skin pixel 
    	for(int i = 0;i<height;i++){
    		
    		for(int j = 0;j<width;j++,index++){
    			
    			tempRaster2D[i][j] = pixelRaster[index];
    			
    			int[] color = hexToRGB(pixelRaster[index]); //convert hex arbg integer to RGB array 

        		float[] hsb = new float[3]; // HSB array
        		Color.RGBtoHSB((int)color[0],(int)color[1],(int)color[2], hsb); //convert RGB to HSB array
        		
        		// Initial pass will use strict skin pixel rule. 
        		// It will only find skin pixels within smaller section compared to loose pixel rule
        		// This will help avoid impurities in the detection
        		if(strictSkinPixelRule(hsb) == true) {
        			pixelRaster2D[i][j] = 0xFFFFFFFF; //if found turn pixel white in the 2D array
        		}
        		else{
        			pixelRaster2D[i][j] = 0xFF000000; //else turn pixel black in the 2D array
        		}
        	}
    	}


    	//Creating a 2D density raster of found initial skin pixels
    	//Run through pixel raster 2D array
    	for(int col = 0 ;col<height;col++){
    		for(int row = 0 ;row<width;row++){

    			//IF pixel is white
    			if(pixelRaster2D[col][row] == 0xFFFFFFFF) {	
    				
    				//calculate pixel boundary (needed if the pixel is near the edges)
    				int max = 10;
					int lowY = col-max>=0?col-max:0;
					int highY = col+max<height?col+max:height-1;
					
					int lowX = row-max>=0?row-max:0;
					int highX = row+max<width?row+max:width-1;
    				
					//Run through pixels all pixels, at max 10 pixels away from this pixel in a square shape
					for(int i = lowY; i<=highY;i++){
						for(int j = lowX;j<=highX;j++){
							if(pixelRaster2D[i][j] == 0xFFFFFFFF) {
								//both work, but i feel like densityRaster[col][row] is a little better
								densityRaster[i][j]++; 
								//densityRaster[col][row]++; //update desnity of  if pixel found is white
							}
						}
    				
					}
    			}	
    		}
    	}
    	
    	//Now we can use that initial pass to find the general location of the hand in the image
    	for(int col = 0 ;col<height;col++) {
    		for(int row = 0 ;row<width;row++) {
    			
    			pixelRaster2D[col][row] = 0xFF000000; //make pixel black, since it should not be based upon the density raster
    			
    			//if density at this pixel is greater then 60
    			if(densityRaster[col][row] > 60) {
    				
    				pixelRaster2D[col][row] = 0xFFFFFFFF; //turn this pixel white

    				boolean intersects = false; //check if any rectangles intersect with the one about to be created
    				
					Rectangle rect = new Rectangle(row-7,col-7,14,14); //this pixel's rectangle 
					
					// check of any previous created rectagles intersect with new rectangle
					for(int i = 0;i<listOfFoundObjects.size();i++){ 
						//rectangle does intersect
						if(rect.intersects(listOfFoundObjects.get(i)) == true) {
							intersects = true; //if a rectangle is found, then this pixel needs to ignored
							break;
						}
					}
					
					// If no intersection found
					if(!intersects) {
						listOfFoundObjects.addElement(rect); //if no rectangles are found, then this rectangle can be added to the list
						
						// Update to see if there is a new top left or bottom right corner with this new rectangle
						if(minX>rect.x)
		        			minX = rect.x;
		        		
		        		if(maxX<rect.x + rect.width)
		        			maxX = rect.x + rect.width;
		        		
		        		if(minY>rect.y)
		        			minY = rect.y;
		        		
		        		if(maxY<rect.y + rect.height)
		        			maxY = rect.y + rect.height;
					}		
    			}
    		}	
    	}
    	
    	// if there is at least 1 rectangle found
    	if(listOfFoundObjects.size()>0) {
    		
    		//Fix the top left and bottom right location to be exactly 100 pixel by 100 pixel in in size
    		
    		//Fix x axis
    		if(maxX-minX > 100) {
	    		int diff = (maxX-minX)-100;
	    		int half = diff/2;
	    		minX += half;
	    		maxX -= half;
	    		
	    	}
	    	else if(maxX-minX<100){
	    		int diff = 100-(maxX-minX);
	    		int half = diff/2;
	    		minX -= half;
	    		maxX += half;
	    	}
	    	
    		//Fix y axis
	    	if(maxY-minY > 100) {
	    		
	    		int diff = (maxY-minY)-100;
	    		int half = diff/2;
	    		minY += half;
	    		maxY -= half;
	    		
	    	}
	    	else if(maxY-minY<100){
	    		int diff = 100-(maxY-minY);
	    		int half = diff/2;
	    		minY -= half;
	    		maxY += half;
	    	}
	    	
	    	//Fix bounds to be within the camera image 
	    	if(minX<0)
				minX = 0;
			if(minY<0)
				minY = 0;
			
			if(maxX>=width)
				maxX = width - 1;
			if(maxY>=height)
				maxY = height - 1;
			
			handBound = new Rectangle(minX,minY,maxX-minX,maxY-minY); //create hand bound location

			// Creating cluster raster
			for(int col = minY ;col<maxY;col++){
	    		for(int row = minX ;row<maxX;row++){
	    			
	    			//if pixel is white
	    			if(pixelRaster2D[col][row] == 0xFFFFFFFF) {
	    				
	    				int max = 5;
						int lowY = col-max>=0?col-max:0;
						int highY = col+max<height?col+max:height-1;
						
						int lowX = row-max>=0?row-max:0;
						int highX = row+max<width?row+max:width-1;
	
						// run through all pixels, 5 pixels away from this pixel
						for(int i = lowY; i<=highY;i++){
							for(int j = lowX;j<=highX;j++){
								clusterRaster[i][j]++; //increase clustering
							}
						}
	    			}
	    		}
	    	}
    		
			//Now that the hand bound has been found.
			//Cluster raster can be used to fill in the missing pixels. 
			for(int col = minY ;col<maxY;col++) {
	    		for(int row = minX ;row<maxX;row++) {
	    			
	    			//If cluster density is greater than 10 and this pixel is black.
	    			//It must mean that this pixel is near another white pixel!
	    			if(clusterRaster[col][row]>10 && pixelRaster2D[col][row]==0xFF000000){
	
		    			int[] color = hexToRGB(tempRaster2D[col][row]); 

	    	    		float[] hsb = new float[3];
	    	    		Color.RGBtoHSB(color[0],color[1],color[2],hsb);
		    			
	    	    		// Use loose skin pixel rule to check if this pixel is with in a certain range to be called a skin pixel
		    			if(looseSkinPixelRule(hsb) == true) {
	    					pixelRaster2D[col][row] = 0xFFFFFFFF; //turn it white
	    	    		}
	    			}
	    		}
	    	}
			
			//Copy pixel raster 2D into pixel raster 1D
			index = 0;
	    	for(int i = 0;i<height;i++){
	    		for(int j = 0;j<width;j++,index++){
	    			pixelRaster[index] = pixelRaster2D[i][j];
	    		}
	    	}
			
			
	    	// Set initial webcam image to the pixel raster
	    	initialWebcamImage.setRGB(0, 0, width, height, pixelRaster, 0, width);
	    	
	    	//crop hand from the pixel raster
			BufferedImage crop = cropImage(initialWebcamImage, handBound);

        	//Now the pixel raster image needs to be drawn on to the new image and be scaled down to 50px by 50px 
			Graphics2D g = newImage.createGraphics();
        	g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        	    RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        	//draw cropped image on to new image
        	g.drawImage(crop, (int)0, (int)0, (int)50, (int)50, (int)0, (int)0, crop.getWidth(),
        			crop.getHeight(), null);
        	g.dispose(); //dispose graphics as it is not needed
    	}
    	
    	
    	// if hand is hand bound is null, which means no hand is found
    	if(handBound == null) {
    		
    		//make a simple black image
    		Graphics g2 = newImage.getGraphics();
        	g2.setColor(Color.black);
        	g2.fillRect(0,0,50,50);
        	g2.dispose();  //dispose graphics as it is not needed
    		
    	}
    	
    	//If data collection mode is true and the user clicked on the window screen
    	if(dataCollectionMode==true && clicked==true){
    		
    		// if max number of images a taken 
    		if(imageNumber>=maxImageNumber){
    			clicked =false; 
    			writer.close(); //close print writer
    		}
    		// if max number of images are not taken yet
    		else {
    			
    			//if image counter equals reset count, it's time to taken an image
        		if(imageCounter == resetCount) {
        			
        			System.out.println(imageNumber); //print current image number being taken
        			int i = 0;
        			
        			// write 0's up to the gesture index and stop right before it
        			for(;i<gestureIndex;i++){
        				if(i == gestureTypes.length-1)
        					writer.print("0");
        				else
        					writer.print("0 ");
        			}
        			
        			// if current gesture type is the last index 
        			if(i == gestureTypes.length-1){
        				//write a 1
        				writer.print("1");
        				i++;
        			}
        			// else there is more gestures left
        			else{
        				
        				writer.print("1 "); //write a 1
        				i++;
        				
        				//write the rest of 0's
        				for(;i<gestureTypes.length;i++){
	        				if(i == gestureTypes.length-1)
	        					writer.print("0");
	        				else
	        					writer.print("0 ");
	        			}
        			}
        			
        			//save this image
        			try { 
            		    File outputfile = new File(imageNumber+".png");
            		    ImageIO.write(newImage, "png", outputfile);
            		} 
        			catch (IOException e) {
        				e.printStackTrace();
        			}
        			
        			
        			imageCounter = 0;
        			imageNumber++;
        			
        			//if this is not the last image write a new line character
        			if(imageNumber < maxImageNumber){
        				writer.print("\n");
        			}
        		}

        		imageCounter++;
    		}
    	}
    	
    	//if server has connected to python client is true 
    	if(connected == true) {
    		
    		//if no hand is detected and detect none with neural network is false
    		if(detectNoneWithNeuralNetwork == false && listOfFoundObjects.size()==0) {
    			// hard code set current gesture
    			currentGestureIndex = 7; 
    			guess[7] = 100;
    		}
    		else{
	    		// send neural network an image and get prediction 
    			try {
	      		   
	    		    //write new image to the real time path
    				File outputfile = new File(realTimePath); 
	    		    ImageIO.write(newImage, "png", outputfile);
					
					//data socket output data stream
	    		    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
	
	    		    //Write a random float to the the stream 
	    		    //This is only to let the client know it should start prediction 
	    		    out.writeFloat(1.23f); 
					
	                //Create buffered reader from the socket's input stream
	    		    InputStream is = socket.getInputStream();
	                InputStreamReader isr = new InputStreamReader(is);
	                BufferedReader br = new BufferedReader(isr);
	                
	                String number = br.readLine(); //read line
	                
	                String[] str = number.split(" "); //Split line at space
	                
	                int highestIndex = -1;
	                int highestValue = -1;

	                for(int i = 0; i<str.length;i++){
	                	try {
	                		int f  = (int)(100f*Float.parseFloat(str[i])); //parse string to float, and convert to integer
	                		guess[i] = f;

	                		//set highest value and index
	                		if(f>highestValue){
	                			highestValue = f;
	                			highestIndex = i;
	                		}
	                	}
	                	catch(Exception e){
	                		e.printStackTrace();
	                	}
	              
	                }
	
	                int[] countCheck = new int[gestureTypes.length]; //counting through the search depth 
	
	                count.add(new Integer[]{highestIndex,highestValue}); //add new index and value to the count
	               
	                // if count is bigger than search depth
	                if(count.size()>searchDepth) {
	                	count.remove(0); //remove first element
	                }
	                
	                float factor = 1f;
	                // run backwards through count
	                for(int i =count.size()-1;i>=0;i--) {
	                	// newly added gestures into count get higher precedence than old gestures 
	                	// older gestures will have a lower importance factor compared to new gestures 
	                	countCheck[count.get(i)[0]]+= (float)count.get(i)[1]/factor;
	                	factor *= 1.1f; 
	                }
	                
	                int correctIndex = -1;
	                int value = 0;
	                
	                //find the the correct index from the newly calculated countCheck array
	                for(int i =0;i<countCheck.length;i++){
	                	if(value<countCheck[i]){
	                		correctIndex= i;
	                		value = countCheck[i];
	                	}
	                }
	                
	                currentGestureIndex = correctIndex;

	    		} 
    			catch (IOException e) {
					e.printStackTrace();
				}
    		}
    	}

    	//set temp initial webcam image to temp raster
    	tempInitialWebcamImage.setRGB(0, 0, width, height, tempRaster, 0, width);

    	//draw white background on the double buffer
    	bufferGraphics.setColor(Color.white);
    	bufferGraphics.fillRect(0, 0, 5000, 5000);

    	//draw initial and temp images
    	bufferGraphics.drawImage(initialWebcamImage, 0+20, 0+40, null);
    	bufferGraphics.drawImage(tempInitialWebcamImage, width+20+10, 0+40, null);
    	
    	//draw green pixel boxes from the density raster
    	bufferGraphics.setColor(Color.green);
    	for(int i = 0;i<listOfFoundObjects.size();i++){
    		Rectangle rect = listOfFoundObjects.get(i);
    		bufferGraphics.drawRect(rect.x+20+width+10,rect.y+40,rect.width,rect.height);
		}

    	//draw new image
    	bufferGraphics.drawImage(newImage, 0+20, height+40+10, null);
    	
    	//draw bound hand if it exists
    	bufferGraphics.setColor(Color.red);
    	if(handBound!=null){
    		boxPosition = handBound;
    		bufferGraphics.drawRect(handBound.x+20+width+10, handBound.y+40, handBound.width, handBound.height);
    		
    	}
    	
    	//Draw prediction strings
    	Font myFont = new Font ("Courier New", Font.BOLD, 20);
    	bufferGraphics.setFont (myFont);

    	for(int i =0;i<guess.length;i++){
    		bufferGraphics.drawString(gestureTypes[i]+": "+guess[i]+"%", 0+20, height+40+10+50+30+i*20);
    		bufferGraphics.fillRect(175, height+40+10+50+15+i*20, guess[i]+10, 10);
    	}
    	
    	//Draw double buffer on to the initial graphics
    	graphic.drawImage(offscreen,0,0,null);
    }
    
    /**
     * Strict skin pixel detection. 
     * A small range of skin detection.
     * @param hsb HSB values
     * @return true or false
     */
    public boolean strictSkinPixelRule(float[] hsb) {
    	if(hsb[0]<0.15f && hsb[1]>0.2f && hsb[1]<0.63f) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    
    /**
     * Loose skin pixel detection.
     * A broader range values for the skin pixel.
     * @param hsb HSB values
     * @return true or false
     */
    public boolean looseSkinPixelRule(float[] hsb) {
    	if(hsb[0]<0.4f &&  hsb[1]<1f && hsb[2]<0.7f) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    
    /**
     * Returns a cropped image
     * @param src Source image
     * @param rect Bounds
     * @return New image cropped based on bounds
     */
    private BufferedImage cropImage(BufferedImage src, Rectangle rect) {
        BufferedImage dest = src.getSubimage(rect.x, rect.y, rect.width, rect.height);
        return dest; 
    }
    

	@Override
	/***
	 * Starts when thread is initialized and started. 
	 */
	public void run() {
		while(true) {
			
			repaint(); //repaint every 25 ms
			
			try {
				Thread.sleep(25); //sleep 25 ms
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}		

			//if in mouse control mode
			if(mouseControlMode == true && currentGestureIndex!=7){
				
				//Calculate distance from center of the detected hand
				float distanceFromCenter = (float)Math.sqrt(Math.pow((boxPosition.x+boxPosition.width/2)-width/2f,2f) + Math.pow((boxPosition.y+boxPosition.height/2)-height/2f,2f));  
				
				//Current gesture is FIST or ACK, and certain distance away from the center of the 
				if((distanceFromCenter>20 && currentGestureIndex == 0) || (distanceFromCenter>25 && currentGestureIndex == 1)) {
					
					//Calculate mouse movement speed depending on distance from the center of the camera
					float factor = 0;
					float xSubNorm = ((boxPosition.x+boxPosition.width/2f)-width/2f)/distanceFromCenter;
					float ySubNorm = ((boxPosition.y+boxPosition.height/2f)-height/2f)/distanceFromCenter;
					Point p = MouseInfo.getPointerInfo().getLocation();
					
					if(currentGestureIndex == 1){
						factor = 10f;
					}
					else if(currentGestureIndex == 0){
						factor = 2f;
					}
					
					p.x += (xSubNorm*factor);
					p.y += (ySubNorm*factor);
					
					go(p.x, p.y); //mode mouse to new location
				}
				//Current gesture is SWING, while the previous gesture was not!
				else if(move!=currentGestureIndex && currentGestureIndex == 8){
					move = currentGestureIndex;
					
					//double click
					click();
					click();
					
					System.out.println("Double Click");
				}
				//Current gesture is ONE, while the previous gesture was not!
				else if(move!=currentGestureIndex && currentGestureIndex == 3){
					move = currentGestureIndex;
					
					//single click
					click();
					
					System.out.println("One Click");
				}
			}
		}
		
		
	}
	
	/**
	 * Move mouse to the desired location
	 * @param x
	 * @param y
	 */
	public void go(int x, int y){
		robot.mouseMove(x, y);
		
		try {
			Thread.sleep(5);
		} catch (InterruptedException err) {
			err.printStackTrace();
		}
	}
	
	/**
	 * Do a normal mouse click
	 */
	public void click() {
		robot.mousePress(InputEvent.BUTTON1_MASK);
		
		try {
			Thread.sleep(5);
		} catch (InterruptedException err) {
			err.printStackTrace();
		}
		
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		
		try {
			Thread.sleep(5);
		} catch (InterruptedException err) {
			err.printStackTrace();
		}
	}
	
	/**
	 * Do a mouse down
	 */
	public void down() {
		robot.mousePress(InputEvent.BUTTON1_MASK);
		
		try {
			Thread.sleep(5);
		} 
		catch (InterruptedException err) {
			err.printStackTrace();
		}
	}
	
	
	/**
	 * Converts hex to integer array which contains red, green, and blue color of 0-255.
     * @param rgbHex integer in format of 0xAARRGGBB, A = alpha, R = red, G = green, B = blue
     */
	public int[] hexToRGB(int argbHex){
		int[] rgb = new int[3];
		
    	rgb[0] = (argbHex & 0xFF0000) >> 16; //get red
    	rgb[1] = (argbHex & 0xFF00) >> 8; //get green
    	rgb[2] = (argbHex & 0xFF); //get blue
    	
		return rgb;//return array
	}

	/**
	 * Mouse clicked
	 * @param event Mouse event action on click 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent event) {
		clicked = true;	
	}

	/**
	 * Unused implement methods.
	 * Key listener was added other other purposed which are now removed from the code. 
	 */
	public void keyReleased(KeyEvent e) {}
	public void keyPressed(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	
}