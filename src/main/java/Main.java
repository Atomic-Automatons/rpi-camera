//import redOrblue;
import java.util.ArrayList;
import java.util.List;
import edu.wpi.first.wpilibj.networktables.*;
import edu.wpi.first.wpilibj.tables.*;
import edu.wpi.cscore.*;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import java.awt.Point;
import org.opencv.core.Core;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;



public class Main {
	static ArrayList<MatOfPoint> contoursRed = new ArrayList<MatOfPoint>();
	static ArrayList<MatOfPoint> contoursBlue = new ArrayList<MatOfPoint>();
	public static PrintWriter writer;
	static UsbCamera camera ;
	//private redOrblue redOrb = new redOrblue();
	private static void log(String stringythingy){
		writer.println(stringythingy);	
		writer.close();
	}
  public static void main(String[] args) {
	  int streamPort = 1185;
	MjpegServer inputStream = new MjpegServer("MJPEG Server", streamPort);
	
	  camera = setUsbCamera(0, inputStream);
	  Runtime.getRuntime().addShutdownHook(new Thread(){
				public void run(){
					if(camera != null){
						camera.free();
					}
				}
			});
    // Loads our OpenCV library. This MUST be included
    System.loadLibrary("opencv_java310");
	try{
		writer = new PrintWriter("/home/pi/log.txt", "UTF-8"); 
	}catch(FileNotFoundException e){
		e.printStackTrace();
	}catch(UnsupportedEncodingException e){
		e.printStackTrace();
	}
	
	
	log("this file has been written by team 6644 for some wierd purposes");
	String PI_ADDRESS = "10.66.44.41";
	int PORT = 1185;
	
	 

    // Connect NetworkTables, and get access to the publishing table
    NetworkTable.setClientMode();
    // Set your team number here
    NetworkTable.setTeam(6644);

    NetworkTable.initialize();

	// This is the network port you want to stream the raw received image to
    // By rules, this has to be between 1180 and 1190, so 1185 is a good choice
   

    // This stores our reference to our mjpeg server for streaming the input image
    
	NetworkTable.getTable("CameraPublisher").getSubTable("T. J. Eckleburg").putStringArray("streams", new String[]{"mjpeg:http://" + PI_ADDRESS + ":" + PORT + "/stream.mjpg"});
	NetworkTable.getTable("CameraPublisher").getSubTable("OpenCV Blue").putStringArray("streams", new String[]{"mjpeg:http://" + PI_ADDRESS + ":" + 1186 + "/stream.mjpg"});
	NetworkTable.getTable("CameraPublisher").getSubTable("OpenCV Red").putStringArray("streams", new String[]{"mjpeg:http://" + PI_ADDRESS + ":" + 1187 + "/stream.mjpg"});
	
	
	//setHttpCamera("PICAM", inputStream);
    // Selecting a Camera
    // Uncomment one of the 2 following camera options
    // The top one receives a stream from another device, and performs operations based on that
    // On windows, this one must be used since USB is not supported
    // The bottom one opens a USB camera, and performs operations on that, along with streaming
    // the input image so other devices can see it.

    // HTTP Camera
    /*
    // This is our camera name from the robot. this can be set in your robot code with the following command
    // CameraServer.getInstance().startAutomaticCapture("YourCameraNameHere");
    // "USB Camera 0" is the default if no string is specified
    String cameraName = "USB Camera 0";
    HttpCamera camera = setHttpCamera(cameraName, inputStream);
    // It is possible for the camera to be null. If it is, that means no camera could
    // be found using NetworkTables to connect to. Create an HttpCamera by giving a specified stream
    // Note if this happens, no restream will be created
    if (camera == null) {
      camera = new HttpCamera("CoprocessorCamera", "YourURLHere");
      inputStream.setSource(camera);
    }
    */
    
      

    /***********************************************/

    // USB Camera
    
    // This gets the image from a USB camera 
    // Usually this will be on device 0, but there are other overloads
    // that can be used
    //camera = setUsbCamera(0, inputStream);
    // Set the resolution for our camera, since this is over USB
    camera.setResolution(320,240);
	camera.setFPS(7);
    

    // This creates a CvSink for us to use. This grabs images from our selected camera, 
    // and will allow us to use those images in opencv
    CvSink imageSink = new CvSink("CV Image Grabber");
    imageSink.setSource(camera);

	
    // This creates a CvSource to use. This will take in a Mat image that has had OpenCV operations
    // operations 
    CvSource imageSourceb = new CvSource("CV Image Source blue", VideoMode.PixelFormat.kMJPEG, 640, 480, 30);
    MjpegServer cvStreamb = new MjpegServer("CV Image Stream blue", 1186);
    cvStreamb.setSource(imageSourceb);
	
	CvSource imageSourcer = new CvSource("CV Image Source red", VideoMode.PixelFormat.kMJPEG, 640, 480, 30);
    MjpegServer cvStreamr = new MjpegServer("CV Image Stream red ", 1187);
    cvStreamr.setSource(imageSourcer);

    // All Mats and Lists should be stored outside the loop to avoid allocations
    // as they are expensive to create
    Mat image = new Mat();
   
	

	double[] redrgbThresholdRed = { 190, 255.0 };
	double[] redrgbThresholdGreen = { 0.0, 117};
	double[] redrgbThresholdBlue = { 0.0, 185 };
	double[] bluergbThresholdRed = { 0, 130 };
	double[] bluergbThresholdGreen = { 0, 255 };
	double[] bluergbThresholdBlue = { 197, 255 };
	Mat blurredImage = new Mat();
	Mat redRGB = new Mat();
	Mat blueRGB = new Mat();
	Mat morphOutputRed = new Mat();
	Mat morphOutputBlue = new Mat();
	Mat output = new Mat();
	org.opencv.core.Point anchor = new org.opencv.core.Point(-1, -1);
    // Infinitely process image
    while (true) {
		// Grab a frame. If it has a frame time of 0, there was an error.
		// Just skip and continue
		long frameTime = imageSink.grabFrame(image);
		if (frameTime == 0) continue;

		// Below is where you would do your OpenCV operations on the provided image
		// The sample below just changes color source to HSV
		//Imgproc.medianBlur(image, blurredImage, 9);
		Imgproc.cvtColor(image, redRGB, Imgproc.COLOR_BGR2RGB);
		Core.inRange(redRGB, new Scalar(redrgbThresholdRed[0], redrgbThresholdGreen[0], redrgbThresholdBlue[0]), new Scalar(redrgbThresholdRed[1], redrgbThresholdGreen[1], redrgbThresholdBlue[1]), redRGB);
		Imgproc.dilate(redRGB, morphOutputRed, new Mat(), anchor, 9);
		findContours(morphOutputRed, contoursRed);

		Imgproc.cvtColor(image, blueRGB, Imgproc.COLOR_BGR2RGB);
		Core.inRange(blueRGB, new Scalar(bluergbThresholdRed[0], bluergbThresholdGreen[0], bluergbThresholdBlue[0]), new Scalar(bluergbThresholdRed[1], bluergbThresholdGreen[1], bluergbThresholdBlue[1]), blueRGB);
		Imgproc.dilate(blueRGB, morphOutputBlue, new Mat(), anchor, 9);
		findContours(morphOutputBlue, contoursBlue);
      // Here is where you would write a processed image that you want to restream
      // This will most likely be a marked up image of what the camera sees
      // For now, we are just going to stream the HSV image
	  red();
	  blue();
      imageSourceb.putFrame(blueRGB);
	  imageSourcer.putFrame(redRGB);
    }
	
  }
	private static void findContours(Mat input, List<MatOfPoint> contours) {
		contours.clear();
		Imgproc.findContours(input, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
	}
	

	private static void red() {
		double areaTot = 0;
		if(!contoursRed.isEmpty()){
			for(int i = 0; i< contoursRed.size(); i++){
				areaTot += Imgproc.contourArea(contoursRed.get(i));
			}
		}
		NetworkTable.getTable("SmartDashboard").putBoolean("red", !contoursRed.isEmpty());
		NetworkTable.getTable("SmartDashboard").putDouble("red area", areaTot);
	}

	private static void blue() {
		double areaTot = 0;
		if(!contoursBlue.isEmpty()){
			for(int i = 0; i< contoursBlue.size(); i++){
				areaTot += Imgproc.contourArea(contoursBlue.get(i));
			}
		}
		NetworkTable.getTable("SmartDashboard").putBoolean("blue",!contoursBlue.isEmpty());
		NetworkTable.getTable("SmartDashboard").putDouble("blue area", areaTot);
	}
  private static HttpCamera setHttpCamera(String cameraName, MjpegServer server) {
    // Start by grabbing the camera from NetworkTables
    NetworkTable publishingTable = NetworkTable.getTable("CameraPublisher");
    // Wait for robot to connect. Allow this to be attempted indefinitely
    while (true) {
      try {
        if (publishingTable.getSubTables().size() > 0) {
          break;
        }
        Thread.sleep(500);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    HttpCamera camera = null;
    if (!publishingTable.containsSubTable(cameraName)) {
      return null;
    }
    ITable cameraTable = publishingTable.getSubTable(cameraName);
    String[] urls = cameraTable.getStringArray("streams", null);
    if (urls == null) {
      return null;
    }
    ArrayList<String> fixedUrls = new ArrayList<String>();
    for (String url : urls) {
      if (url.startsWith("mjpg")) {
        fixedUrls.add(url.split(":", 2)[1]);
      }
    }
    camera = new HttpCamera("CoprocessorCamera", fixedUrls.toArray(new String[0]));
    server.setSource(camera);
    return camera;
  }

  private static UsbCamera setUsbCamera(int cameraId, MjpegServer server) {
    // This gets the image from a USB camera 
    // Usually this will be on device 0, but there are other overloads
    // that can be used
    UsbCamera camera = new UsbCamera("CoprocessorCamera", cameraId);
    server.setSource(camera);
    return camera;
  }
}