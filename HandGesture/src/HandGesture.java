
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.github.sarxos.webcam.Webcam;

/**
 * Finds objects of lime green color and puts red square around them.
 * @author      Inderpreet Pabla
 */
public class HandGesture extends JFrame implements Runnable{
	int width,height;
    Webcam webcam;
    int[] pixelRaster = null;
    BufferedImage initialWebcamImage;
    boolean once = false;

    public static void main(String[] args) throws Exception {
    	HandGesture gesture = new HandGesture(); //make an image analysis object
        Thread thread = new Thread(gesture); //create thread
        thread.start();//start thread
        
    }

    /**
     * Initializes webcam, buffered image, 2D pixel raster and sets up window.
     */
    public HandGesture() {
    	
    	//get default webcam connected to this computer
        webcam = Webcam.getDefault();
        webcam.open(); //open webcam communication
        
        //get webcam dimensions 
        width = webcam.getViewSize().width; 
        height = webcam.getViewSize().height;
        System.out.println(width+" "+height);
        //initialize image buffer and pixel raster initialized according to buffer size
        initialWebcamImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB); 
        pixelRaster = ((DataBufferInt) initialWebcamImage.getRaster().getDataBuffer()).getData();
        
        //window setups
        setSize((width*3), height+50);
        setTitle("Hand Gesture Detection");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(null);
		setVisible(true);
    }

    /**
     * Finds lime green colored objects that are detected infront of the camera and draws 
     * red boxes on top of them. It first finds pixels that are in the lime green spectrum,  
     * followed by counting density of found pixel in a given area. Density of over 40 units
     * is then added to a vector of rectangles and each pixel is checked if it's contained 
     * within the previously found rectangles. If it's not then a new rectangle is added to 
     * the location of the new pixel.   
     * @param g		used to draw buffered image along with red rectangles to the screen.
     */
    public void paint(Graphics graphic) {
    	initialWebcamImage = webcam.getImage(); //get image
    	BufferedImage tempInitialWebcamImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB); 
    	
    	//initilize rasters 
    	pixelRaster = new int[width*height];
    	int[] tempRaster = new int[width*height];
    	
    	int[][] pixelRaster2D = new int [height][width]; //converting pixelRaster to 2D format to check for surrounding pixels 
    	int[][] densityRaster = new int [height][width]; //raster for density
    	int index = 0; //used to access pixelRaster when running through 2D array
    	
    	//get rasters
    	
    	
    	RescaleOp op = new RescaleOp(2f, 0, null);
    	initialWebcamImage = op.filter(initialWebcamImage, initialWebcamImage);
    	initialWebcamImage.getRGB(0, 0, width, height, pixelRaster, 0, width);
    	initialWebcamImage.getRGB(0, 0, width, height, tempRaster, 0, width);
    	
    	
    	//FD5F00 HEX   253,95,0 RGB
    	//0xAARRGGBB
    	//Run through the pixel raster and find all pixels that have red <= 80, green >= 100 and blye <=80
    	for(int i = 0 ;i<width*height;i++){
    		int[] color = hexToRGB(pixelRaster[i]); //convert hex arbg to array with rgb 0-255
    		float r = color[0];
    		float g = color[1];
    		float b = color[2];
    		
    		if(((Math.abs(r-253f)/255f) + (Math.abs(g-95f)/255f) + (Math.abs(b-0f)/255f))/3f < 0.33f ){
    		//if(color[0]>50 && color[1] < 120f && color[1]<100f){
    			pixelRaster[i] = 0xFFFD5F00; //if found turn pixel green
    		}
    		else{
    			pixelRaster[i] = 0xFF000000; //else turn pixel black
    		}

    		
    	}
    	
    	
    	
    	for(int col = 0 ;col<height;col++){
    		for(int row = 0 ;row<width;row++){
    			pixelRaster2D[col][row] = pixelRaster[index];
        		index++;
    		}	
    	}
    	
    	//create a density raster map with given pixel information
    	for(int col = 0 ;col<height;col++){
    		for(int row = 0 ;row<width;row++){
    			//if current pixel is green
    			if(pixelRaster2D[col][row] == 0xFFFD5F00){		
    				//if current pixel is within checking bounds
    				//if(col>15 && col<height-15 && row>15 && row<width-15){
    					//check if 5 pixels all around this pixel have any green color
    					int max = 15;
    					int lowY = col-max>=0?col-max:0;
    					int highY = col+max<height?col+max:height-1;
    					
    					int lowX = row-max>=0?row-max:0;
    					int highX = row+max<width?row+max:width-1;
    					
    					for(int i = lowY; i<=highY;i++){
    						
    						for(int j = lowX;j<=highX;j++){
    							if(pixelRaster2D[i][j] == 0xFFFD5F00)
    	    						densityRaster[i][j]++; //update desnity if pixel found is green
    							else 
    								densityRaster[i][j]-=2;
        					}
    					}
    				//}
    			}
    		}	
    	}

    	index = 0; //reset index
    	Vector<Rectangle> listOfFoundObjects = new Vector<Rectangle>(); //list of found objects
    	
    	//find individual objects using desnity raster
    	for(int col = 0 ;col<height;col++){
    		for(int row = 0 ;row<width;row++){
    			pixelRaster[index] = 0xFF000000; //make pixel black 
    			
    			//if denisty at this pixel is greater then 40
    			if(densityRaster[col][row]>166){
    				pixelRaster[index] = 0xFFFD5F00; //turn this pixel green
    				
    				if(listOfFoundObjects.size() == 0)
    					listOfFoundObjects.addElement(new Rectangle(row-7,col-7,14,14)); //if list is empty add 14x14 rectangle with this pixel as the center to the list
    				else{
    					boolean intersects = false;
    					Rectangle foundRect = null;
    					Rectangle rect = new Rectangle(row-7,col-7,14,14); //this pixel's rectangle 
    					for(int i = 0;i<listOfFoundObjects.size();i++){ 
    						if(rect.intersects(listOfFoundObjects.get(i)) == true){
    							intersects = true; //if a rectangle is found, then this pixel needs to ignored
    							break;
    						}
    					}
    					if(!intersects)
    						listOfFoundObjects.addElement(rect); //if no rectangles are found, then this rectangle can be added to the list
    				}		
    			}
    			index++;
    		}	
    	}
    	
    	
    	//draw found rectangles 
    	graphic.setColor(Color.red);
    	Rectangle rec = null;
    	int minX = 10000;
    	int maxX = -10000;
    	
    	int minY = 10000;
    	int maxY = -10000;
    	
    	
    	graphic.setColor(Color.white);
    	graphic.fillRect(0, height, 1000, 1000);
    	
    	for(int i = 0;i<listOfFoundObjects.size();i++){
    		Rectangle rect = listOfFoundObjects.get(i);
    		if(minX>rect.x)
    			minX = rect.x;
    		
    		if(maxX<rect.x + rect.width)
    			maxX = rect.x + rect.width;
    		
    		if(minY>rect.y)
    			minY = rect.y;
    		
    		if(maxY<rect.y + rect.height)
    			maxY = rect.y + rect.height;
    		
    		
    		/*Rectangle rect = listOfFoundObjects.get(i);
    		graphic.drawRect(rect.x,rect.y,rect.width,rect.height);*/
		}
    	
    	if(listOfFoundObjects.size()>0){
    		if(minX<0)
    			minX = 0;
    		if(minY<0)
    			minY = 0;
    		
    		if(maxX>=width)
    			maxX = width - 1;
    		if(maxY>=height)
    			maxY = height - 1;
    		
    		
    		rec = new Rectangle(minX,minY,maxX-minX,maxY-minY);
    		index = 0;
    		for(int i =0; i<height;i++)
    		{
    			for(int j =0; j<width;j++)
        		{
        			if(i>=minX && i <=maxX && j>=minY && j <=maxY)
        			{
        				int max = 4;
    					int lowY = i-max>=0?i-max:0;
    					int highY = i+max<height?i+max:height-1;
    					
    					int lowX = j-max>=0?j-max:0;
    					int highX = j+max<width?j+max:width-1;
    					
    					for(int k = lowY; k<=highY;k++){
    						
    						for(int h = lowX;h<=highX;h++){
    							
    							if(pixelRaster2D[k][h] == 0xFFFD5F00)
    								densityRaster[i][j]++;
    								//pixelRaster[index] = 0xFF00FF00;
        					}
    					}
        				
        				if(densityRaster[i][j]>=1 && densityRaster[i][j]<120){
    						pixelRaster[index] = 0xFF00FF00;
        				}
        				
        			}
        			index++;
        		}
    			
    		}
    		
    		BufferedImage crop = cropImage(tempInitialWebcamImage,rec);
    		
    		/*BufferedImage resized = new BufferedImage(100, (int)(((float)height/(float)width)*100f), BufferedImage.TYPE_INT_ARGB);
        	Graphics2D g = resized.createGraphics();
        	g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        	    RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        	g.drawImage(crop, 0, 0, 100, (int)(((float)height/(float)width)*100f), 0, 0, crop.getWidth(),
        			crop.getHeight(), null);
        	g.dispose(); */ 
        	
        	
    		graphic.drawImage(crop, 0, height, null);
    		System.out.println(crop.getWidth()+" "+crop.getHeight());
		
    		
    	
    	}
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	initialWebcamImage.setRGB(0, 0, width, height, tempRaster, 0, width); //initial webcam image to temp raster data
    	tempInitialWebcamImage.setRGB(0, 0, width, height, pixelRaster, 0, width); //temp webcam image to pixel raster data
    		
    	//draw buffered images 
    	graphic.drawImage(initialWebcamImage, 0, 0, null);
    	graphic.drawImage(tempInitialWebcamImage, width, 0, null);
    	
    	
    	if(listOfFoundObjects.size()>0)
    	{
    		
    		graphic.setColor(Color.red);
    		graphic.drawRect(rec.x,rec.y,rec.width,rec.height);
    	}
    	
	
    }
    
    private BufferedImage cropImage(BufferedImage src, Rectangle rect) {
        BufferedImage dest = src.getSubimage(rect.x, rect.y, rect.width, rect.height);
        return dest; 
     }
    
    /**
     * Check if all pixels are within a certain range of each other.
     * Essentially it check if pixel is grey. 
     * @return boolean		Returns true of false
     */
    public boolean allColorsBetweenRange(int color[], int range){
    	int absRG = Math.abs(color[0]-color[1]);
    	int absRB = Math.abs(color[0]-color[2]);
    	int absGB = Math.abs(color[1]-color[2]);
    	if(absRG<=range && absRB<range && absGB<range)
    		return true;
    	else
    		return false;
    }

    /**
     * Repaint every 25 milliseconds. 
     */
	@Override
	public void run() {
		while(true){
			
			try {
				Thread.sleep(25);
				repaint();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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

}