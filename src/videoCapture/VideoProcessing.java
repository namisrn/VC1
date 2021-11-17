package videoCapture;

import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class VideoProcessing extends JFrame{
    private BufferedImagePanel panel1;
    private static JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));

    /**
     * Create object and perform the processing by calling private member functions.
     */
//constructor
    public VideoProcessing() {
        panel1 = null;
        createFrame();
        processShowVideo();
    }

    /**
     * Create the JFrame to be displayed, displaying two images.
     */
    private void createFrame() {

        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new FlowLayout());

        panel1 = new BufferedImagePanel();
        contentPane.add(panel1);

        // place the frame at the center of the screen and show
        pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width/2 - getWidth()/2, dim.height/2 - getHeight()/2);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void processShowVideo() {

        // BEGIN: Prepare streaming from internal web cam
        //NOTE: "To open default camera using default backend just pass 0."
        //https://docs.opencv.org/3.4/d8/dfe/classcv_1_1VideoCapture.html#a57c0e81e83e60f36c83027dc2a188e80

        //MY WEBCAM
        VideoCapture cap = new VideoCapture(0, Videoio.CAP_ANY);
        // END: Prepare streaming from internal web cam

        // BEGIN: Prepare streaming from video file
        //String filePathName = getFilePathName();
        //VideoCapture cap = new VideoCapture(filePathName);
        // END: Prepare streaming from video file

        Mat frame = new Mat();
        // Check of file or camera can be opened
        if(!cap.isOpened())
            throw new CvException("The Video File or the Camera could not be opened!");

        cap.read(frame);

        //frame update counter
        int i = 1;

        System.out.print("Frame count: (" + i + ")");
        // loop for grabbing frames
        while (cap.read(frame)) {
            i++;

            // display original frame from the video stream
            panel1.setImage(Mat2BufferedImage(frame));

        } // End for loop
        cap.release();
    }

    /**
     * Converts an OpenCV matrix into a BufferedImage.
     *
     * Inspired by
     * http://answers.opencv.org/question/10344/opencv-java-load-image-to-gui/
     * Fastest code
     *
     * @param OpenCV Matrix to be converted must be a one channel (grayscale) or
     * three channel (BGR) matrix, i.e. one or three byte(s) per pixel.
     * @return converted image as BufferedImage
     *
     */
    public BufferedImage Mat2BufferedImage(Mat imgMat){
        int bufferedImageType = 0;
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
}
