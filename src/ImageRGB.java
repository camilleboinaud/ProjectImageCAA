import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by camille on 28/03/15.
 */
public class ImageRGB extends Image {

    public int[][] histogram;

    ImageRGB(String name) {
        super(name);
    }

    /*
    * Convert an image into a byte array
    */
    protected byte[] convertImageByteArray() {
        int i = 0;

        byte[] data = new byte[width * height * 3];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                data[i++] = image[x][y][2]; //green
                data[i++] = image[x][y][1]; //red
                data[i++] = image[x][y][0]; //blue
            }
        }
        return data;
    }

    /*
     * Création de l'histogramme d'une image en niveaux de gris
     */
    public void fillHistogram() {

        //Initialisation de l'histogramme
        histogram = new int[(int) Math.pow(2.0, (double) super.depth)][3];
        for (int i = 0; i < histogram.length; i++) histogram[i][0] = histogram[i][1] = histogram[i][2] = 0;

        //Calcul de l'histogramme pour l'ensemble des couleurs de l'image
        for (int x = 0; x < super.width; x++) {
            for (int y = 0; y < super.height; y++) {
                histogram[(super.image[x][y][0] < 0) ? super.image[x][y][0] + 256 : super.image[x][y][0]][0]++;
                histogram[(super.image[x][y][1] < 0) ? super.image[x][y][1] + 256 : super.image[x][y][1]][1]++;
                histogram[(super.image[x][y][2] < 0) ? super.image[x][y][2] + 256 : super.image[x][y][2]][2]++;
            }
        }
        System.out.println("[INFO] Histogramme de l'image RGB créé");
    }

    protected void readImage(DataInputStream reader){
        //Lecture de l'image pixel par pixel
        try {
            //Initialisation de la variable contenant les bytes de l'image
            super.image = new byte[super.width][super.height][3];
            for(int x = 0 ; x < super.width ; x++){
                for(int y = 0 ; y < super.height ; y++){
                    super.image[x][y][2] = reader.readByte(); //blue
                    super.image[x][y][1] = reader.readByte(); //green
                    super.image[x][y][0] = reader.readByte(); //red
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
