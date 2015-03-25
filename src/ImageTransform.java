import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;

/**
 * Created by camille on 24/03/15.
 */
public class ImageTransform {

    public static void TramsformToGrayScale(String name){
        byte[][][] data = Main.image;
        boolean isConverted = true;
        if(Main.image !=null){
            try {
                BufferedImage gray = ImageIO.read(new File(name+".bmp"));
                ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
                op.filter(gray, gray);

                for(int x = 0 ; x < Main.width ; x++){
                    for(int y = 0 ; y < Main.height ; y++){
                        Color color = new Color(gray.getRGB(y,x));
                        if(Main.depth > 8) {
                            data[Main.width - 1 - x][y][0] = (byte) color.getRed();
                            data[Main.width - 1 - x][y][1] = (byte) color.getGreen();
                            data[Main.width - 1 - x][y][2] = (byte) color.getBlue();
                        }else{
                            isConverted = false;
                            break;
                        }
                    }
                }
                if(isConverted){
                    Main.image = data;
                    System.out.println("[INFO] Image transformée en nuances de gris");
                }
                else System.out.println("[WARN] L'image est déjà en nuance de gris -> abort transformation");

            } catch (IOException e) {
                System.out.println("[FAIL] Impossible de charger l'image afin de la convertir en niveaux de gris");
                e.printStackTrace();
            }
        }else{
            System.out.println("[FAIL] Impossible de convertir l'image en niveaux de gris");
        }
    }
}
