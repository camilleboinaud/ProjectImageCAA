import java.awt.*;
import java.util.HashMap;

/**
 * Created by camille on 18/03/15.
 */
public class Main {

    public static int width;
    public static int height;
    public static int depth;
    public static byte[][][] image;
    public static byte[] header;
    public static String filename;
    public static int[] histogram;

    public static void main(String[] args){
        filename = "horse";
        ImageReader.ReadBitmap(filename);
        ImageWriter.WriteBitmap(filename+"_nb",false);
    }
}
