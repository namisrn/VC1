import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.opencv.core.Point;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

public class VideoProcessing extends JFrame{
    private double thresholdMin = 5;
    private double threshholdax = thresholdMin*3;

    private BufferedImagePanel panel1;
    private static JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));

    /**
     * Create object and perform the processing by calling private member functions.
     */

//constructor
    public VideoProcessing() {
        processShowVideo();
    }

    private void processShowVideo() {

        // Prepare streaming from internal webcam
        //NOTE: "To open default camera using default backend just pass 0."
        //https://docs.opencv.org/3.4/d8/dfe/classcv_1_1VideoCapture.html#a57c0e81e83e60f36c83027dc2a188e80

        //my webcam
        VideoCapture cap = new VideoCapture(0, Videoio.CAP_ANY);

        //used Matrices
        Mat frame = new Mat();
        Mat grayMat = new Mat();
        Mat edges = new Mat();
        Mat lines = new Mat();
        Mat dst = new Mat();
        Mat hsv = new Mat();
        Mat shape = new Mat();
        Mat testContours = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();



        //Check of file or camera can be opened
        if (!cap.isOpened())
            throw new CvException("The Video File or the Camera could not be opened!");
        cap.read(frame);
        //frame update counter
        int i = 1;
        System.out.print("Frame count: (" + i + ")");

        //loop for grabbing frames

        while (cap.read(frame)) {
            i++;

            //From original to grey
            Imgproc.cvtColor(frame, grayMat, Imgproc.COLOR_RGB2GRAY);

            //Gaussian Blur, only odd numbers
            //Imgproc.GaussianBlur(grayMat, edges, new Size(7,7), 0);

            //Convert BGR (RGB color space) into HSV
            Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);


            //h = hue, s = saturation,v = value
            //Core.inRange(hsv, new Scalar(36, 0, 60), new Scalar(134, 120, 178), hsv); //for light green
            Core.inRange(hsv, new Scalar(0, 180, 0), new Scalar(50,255,255), hsv); //for red
            //Core.inRange(hsv, new Scalar(70,80,255), new Scalar(70,255,255), hsv); //for yellow


            //medianBlur, aperture linear size, odd number
            Imgproc.medianBlur(hsv, hsv, 19);

            //detecting edges
            Imgproc.Canny(edges, edges, thresholdMin, thresholdMin * 30, 3);

            /*Imgproc.HoughLines(edges, lines, 1, Math.PI/180, 150);
            for (int j = 0; j < lines.rows(); j++) {
                double[] data = lines.get(j, 0);
                double rho = data[0];
                double theta = data[1];
                double a = Math.cos(theta);
                double b = Math.sin(theta);
                double x0 = a*rho;
                double y0 = b*rho;
                //Drawing lines on the image
                Point pt1 = new Point();
                Point pt2 = new Point();
                pt1.x = Math.round(x0 + 1000*(-b));
                pt1.y = Math.round(y0 + 1000*(a));
                pt2.x = Math.round(x0 - 1000*(-b));
                pt2.y = Math.round(y0 - 1000 *(a));
                Imgproc.line(edges, pt1, pt2, new Scalar(0, 0, 255));*/

            /*Testing shape detection*/

            //cvt RGB to HSV color space
            Imgproc.cvtColor(frame, shape, Imgproc.COLOR_RGB2HSV);
            //Blur
            Imgproc.medianBlur(shape, shape, 5);

            //Optimization saturation for color detection, goal: red
            Core.inRange(shape, new Scalar(0, 170, 0), new Scalar(120, 255, 255), shape);

            //Threshold need to be adjust
            //Imgproc.threshold(shape, shape, 80, 255, Imgproc.THRESH_BINARY);

            //Erode source image
            Imgproc.erode(shape, shape, new Mat(), new Point(-1,-1),1 );

            //Dilates source image by using a specific structuring element
            Imgproc.dilate(shape, shape, new Mat(), new Point(-1,-1),1);


            //Find contours, save information of white object in contours as arraylist
            Imgproc.findContours(shape, contours, testContours, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            //convert contours to a MatOfPoint2f object
            List<MatOfPoint2f> newContours = new ArrayList<>();
            for(MatOfPoint point : contours) {
                MatOfPoint2f newPoint = new MatOfPoint2f(point.toArray());
                newContours.add(newPoint);
            }

            //loop through possibilities
            for (int idx = 0; idx < contours.size(); idx++) {
                MatOfPoint2f approx = new MatOfPoint2f(); //approx parameter count contours of objects; important for interaction handling
                //allows the approximation of polygons and determine scope of object
                Imgproc.approxPolyDP(newContours.get(idx), approx, Imgproc.arcLength(newContours.get(idx), true) * 0.02, true);
                long count = approx.total();
                //filtering small blobs
                if(Math.abs(Imgproc.contourArea(contours.get(idx))) > 1000) {

                    //draw contours on objects
                    if (count == 5) {
                        Imgproc.drawContours(frame, contours, idx, new Scalar(75, 0, 0));
                    }
                    if (count == 6) {
                        Imgproc.drawContours(frame, contours, idx, new Scalar(255, 255, 255));
                    }
                    if (count == 4) {
                        Imgproc.drawContours(frame, contours, idx, new Scalar(200, 0, 0));
                    }
                    if (count == 3) {
                        Imgproc.drawContours(frame, contours, idx, new Scalar(360, 100, 50));
                    }
                }
            }

            //use the right matrix for certain function, mat or shape
            BoxLightTexMainWindowPP.panel1.setImage(Mat2BufferedImage(shape));
            contours.clear();

        } //end of loop
        //display original frame from the video stream
        cap.release();
    }

        /**
     * Converts an OpenCV matrix into a BufferedImage.
     *
     * Inspired by
     * http://answers.opencv.org/question/10344/opencv-java-load-image-to-gui/
     * Fastest code
     *
     * //@param OpenCV Matrix to be converted must be a one channel (grayscale) or
     * three channel (BGR) matrix, i.e. one or three byte(s) per pixel.
     * @return converted image as BufferedImage
     *
     */
    public BufferedImage Mat2BufferedImage(Mat imgMat){
        int bufferedImageType ;
        switch (imgMat.channels()) {
            case 1:
                bufferedImageType = BufferedImage.TYPE_BYTE_GRAY;
                break;
            case 3:
                bufferedImageType = BufferedImage.TYPE_3BYTE_BGR;
                break;
            default:
                throw new IllegalArgumentException("Unknown matrix type. Only one byte per pixel (one channel) or three bytes pre pixel (three channels) are allowed.");
        }
        BufferedImage bufferedImage = new BufferedImage(imgMat.cols(), imgMat.rows(), bufferedImageType);
        final byte[] bufferedImageBuffer = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        imgMat.get(0, 0, bufferedImageBuffer);
        return bufferedImage;
    }

    public void setThresholdMin(double thresholdMin) {
        this.thresholdMin = thresholdMin;
    }
}
