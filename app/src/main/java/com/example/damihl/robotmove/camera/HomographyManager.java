package com.example.damihl.robotmove.camera;


import java.util.LinkedList;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Imgproc;
/**
 * Created by poorpot on 27.04.15.
 */
public class HomographyManager {

    private static HomographyManager instance = null;

    public static HomographyManager getInstance(){
        if (instance == null) instance = new HomographyManager();
        return instance;
    }


    public Mat getHomographyMatrix(Mat mRgba) {
        final Size mPatternSize = new Size(6, 9); // number of inner corners in the used chessboard pattern
        float x = -48.0f; // coordinates of first detected inner corner on chessboard
        float y = 309.0f;
        float delta = 12.0f; // size of a single square edge in chessboard
        LinkedList<Point> PointList = new LinkedList<Point>();

        // Define real-world coordinates for given chessboard pattern:
        for (int i = 0; i < mPatternSize.height; i++) {
            y = 309.0f;
            for (int j = 0; j < mPatternSize.width; j++) {
                PointList.addLast(new Point(x,y));
                y += delta;
            }
            x += delta;
        }
        MatOfPoint2f RealWorldC = new MatOfPoint2f();
        RealWorldC.fromList(PointList);

        // Detect inner corners of chessboard pattern from image:
        Mat gray = new Mat();
        Imgproc.cvtColor(mRgba, gray, Imgproc.COLOR_RGBA2GRAY); // convert image to grayscale
        MatOfPoint2f mCorners = new MatOfPoint2f();
        boolean mPatternWasFound = Calib3d.findChessboardCorners(gray, mPatternSize, mCorners);

        // Calculate homography:
        if (mPatternWasFound)
            // Calib3d.drawChessboardCorners(mRgba, mPatternSize, mCorners, mPatternWasFound); //for visualization
            return Calib3d.findHomography(mCorners, RealWorldC);
        else
            return new Mat();
    }

}
