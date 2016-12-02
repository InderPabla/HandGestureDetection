
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
public class HandGesture2 extends JFrame implements Runnable{
	int width,height;
    Webcam webcam;
    int[] pixelRaster = null;
    BufferedImage initialWebcamImage;
    boolean once = false;

    public static void main(String[] args) throws Exception {
    	HandGesture2 gesture = new HandGesture2(); //make an image analysis object
        Thread thread = new Thread(gesture); //create thread
        thread.start();//start thread
        
    }

    /**
     * Initializes webcam, buffered image, 2D pixel raster and sets up window.
     */
    public HandGesture2() {
    	
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
    	
    	RescaleOp op = new RescaleOp(2f, 0, null);
    	initialWebcamImage = op.filter(initialWebcamImage, initialWebcamImage);
    	initialWebcamImage.getRGB(0, 0, width, height, pixelRaster, 0, width);
    	initialWebcamImage.getRGB(0, 0, width, height, tempRaster, 0, width);
    	
    	for(int i = 0 ;i<width*height;i++){
    		int[] color = hexToRGB(pixelRaster[i]); //convert hex arbg to array with rgb 0-255
    		float r = color[0];
    		float g = color[1];
    		float b = color[2];
    		
    		float[] hsb = new float[3];
    		Color.RGBtoHSB((int)r,(int)g,(int)b,hsb);
    		
    		if(hsb[0]<0.15f && hsb[1]>0.23f && hsb[1]<0.63f){
    			pixelRaster[i] = 0xFF00FF00; //if found turn pixel green
    		}
    		else{
    			pixelRaster[i] = 0xFF000000; //else turn pixel black
    		}
    		
    		/*if(color[0]<=80 && color[1]>80 && color[2]<=80 && Math.abs(color[0]-color[2])<=50){
    			pixelRaster[i] = 0xFF00FF00; //if found turn pixel green
    		}
    		else{
    			pixelRaster[i] = 0xFF000000; //else turn pixel black
    		}*/

    		
    	}
    	
    	//fill pixelRaster2D with pixelRaster information
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
    			if(pixelRaster2D[col][row] == 0xFF00FF00){		
    				//if current pixel is within checking bounds
    				if(col>10 && col<height-10 && row>10 && row<width-10){
    					//check if 5 pixels all around this pixel have any green color
    					for(int i = col-10;i<col+10;i++){
    						for(int j = row-10;j<row+10;j++){
    							if(pixelRaster2D[i][j] == 0xFF00FF00){
    	    						densityRaster[i][j]++; //update desnity if pixel found is green
    							}
        					}
    					}
    				}
    			}
    		}	
    	}

    	Vector<Rectangle> listOfFoundObjects = new Vector<Rectangle>(); //list of found objects
    	
    	//find individual objects using desnity raster
    	for(int col = 0 ;col<height;col++){
    		for(int row = 0 ;row<width;row++){
    			pixelRaster2D[col][row] = 0xFF000000; //make pixel black 
    			
    			//if denisty at this pixel is greater then 40
    			if(densityRaster[col][row]>60){
    				pixelRaster2D[col][row] = 0xFF00FF00; //turn this pixel green

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
    		}	
    	}
    	
    	
    	index = 0;
    	
    	for(int col = 0 ;col<height;col++){
    		for(int row = 0 ;row<width;row++){
    			if(pixelRaster2D[col][row] == 0xFF00FF00){
    				int max = 1;
					int lowY = col-max>=0?col-max:0;
					int highY = col+max<height?col+max:height-1;
					
					int lowX = row-max>=0?row-max:0;
					int highX = row+max<width?row+max:width-1;
    				
    				
					
					for(int i = lowY; i<=highY;i++){
						for(int j = lowX;j<=highX;j++){
							if(pixelRaster2D[i][j] == 0xFF000000 )
								pixelRaster2D[i][j] = 0xFFFD5F00;
    					}
					}
    			}
    		}
    	}
    	
    	for(int col = 0 ;col<height;col++){
    		for(int row = 0 ;row<width;row++){
    			pixelRaster[index] = 0xFF000000;
    			if(pixelRaster2D[col][row]!=0xFF000000)
    				pixelRaster[index] = tempRaster[index];
    			//pixelRaster[index] = pixelRaster2D[col][row];
    			/*pixelRaster[index] = 0xFF000000;
    			
    			if(pixelRaster2D[col][row]!=0xFF000000)
    				pixelRaster[index] = 0xFFFFFFFF; //pixelRaster2D[col][row];*/
    			index++;
    		}
    	}
    	
    	initialWebcamImage.setRGB(0, 0, width, height, tempRaster, 0, width); //initial webcam image to temp raster data
    	tempInitialWebcamImage.setRGB(0, 0, width, height, pixelRaster, 0, width); //temp webcam image to pixel raster data
    		
    	//draw buffered images 
    	graphic.drawImage(initialWebcamImage, 0, 0, null);
    	graphic.drawImage(tempInitialWebcamImage, width, 0, null);
    	
    	
    	
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