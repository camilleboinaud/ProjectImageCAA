import java.io.*;

/**
 * Created by camille on 18/03/15.
 */
public class ImageWriter {

    /*
     * Convert an image into a byte array
     */
    private static byte[] ConvertImageByteArray(boolean color){
        int i = 0;

        //Si on souhaite écrire l'image en niveau de gris, on fait la transformation
        if(!color) ImageTransform.TramsformToGrayScale(Main.filename);

        byte[] data = new byte[Main.width*Main.height*3];
        for(int x = 0; x < Main.width ; x++){
            for(int y = 0 ; y < Main.height ; y++){
                if(Main.depth > 8) {
                    data[i++] = Main.image[x][y][1]; //green;
                    data[i++] = Main.image[x][y][0]; //red;
                    data[i++] = Main.image[x][y][2]; //blue;
                }else{
                    data[i++] = Main.image[x][y][0];
                }
            }
        }
        return data;
    }

    /*
     * Write a byte array into a bmp file.
     * @params color : false -> image transformation in gray scale
     */
    public static void WriteBitmap(String name, boolean color){
        try {
            DataOutputStream writer = new DataOutputStream (new FileOutputStream(name+".bmp"));
            writer.write(Main.header);
            writer.write(ConvertImageByteArray(color));
            System.out.println("[INFO] Ecriture du fichier : "+name+".bmp");
        } catch (FileNotFoundException e) {
            System.out.println("[FAIL] Ecriture du fichier impossible, fichier non trouvé.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("[FAIL] Ecriture du fichier impossible, exeption entrée/sorite levée.");
            e.printStackTrace();
        }

    }

}
