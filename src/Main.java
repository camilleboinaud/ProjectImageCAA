import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by camille on 18/03/15.
 */
public class Main {

    public static void main(String[] args){

        String filename1 = "house";
        ImageRGB img1 = new ImageRGB(filename1);
        System.out.println(img1.histogram.length);
        img1.conversion(16);
        img1.writeBitmap();


        /*String filename2 = "horse_modified";
        ImageGrayScale img2 = new ImageGrayScale(filename2);
        img2.conversion(16);
        img2.writeBitmap();*/
    }
}
