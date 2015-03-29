import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by camille on 18/03/15.
 */
public class Main {

    public static void main(String[] args){

        /*String filename1 = "img/pimen";
        ImageRGB img1 = new ImageRGB(filename1);
        img1.conversion(3);
        img1.writeBitmap();*/


        String filename2 = "img/mandr_lumi";
        ImageGrayScale img2 = new ImageGrayScale(filename2);
        img2.conversion(3);
        img2.writeBitmap();
    }
}
