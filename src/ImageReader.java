import java.awt.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by camille on 18/03/15.
 */
public class ImageReader {

    /*
     * Méthode permettant de faire une lecture de n bytes consécutifs.
     */
    private static byte[] ReadBytes(int nb, DataInputStream reader){

        byte[] data = new byte[nb];

        for(int i = 0 ; i < nb ; i++){
            try {
                data[i] = reader.readByte();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return data;
    }

    /*
     * Lecture du fichier bitmap, extraction des bytes de pixel dans la variable : image
     */
    public static void ReadBitmap(String path){
        ReadBitmapHeader(path);
        ReadBitmapImage(path);
    }

    /*
     * Lecture du fichier bitmap, extraction des bytes de pixel dans la variable : image
     */
    private static void ReadBitmapHeader(String path){

        try {
            DataInputStream reader = new DataInputStream(new FileInputStream(path+".bmp"));

            byte[] fileHeader = ReadBytes(14,reader);
            byte[] imageHeaderSize = ReadBytes(4, reader);

            ByteBuffer b = ByteBuffer.wrap(imageHeaderSize);
            b.order(ByteOrder.LITTLE_ENDIAN);

            byte[] imageHeader = ReadBytes(b.getInt()-4, reader);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            outputStream.write(fileHeader);
            outputStream.write(imageHeaderSize);
            outputStream.write(imageHeader);

            Main.header = outputStream.toByteArray( );

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Lecture du fichier bitmap, extraction des bytes de pixel dans la variable : image
     */
    private static void ReadBitmapImage(String path){

        System.out.println("--> Lecture du fichier : "+path);

        try {
            DataInputStream reader = new DataInputStream(new FileInputStream(path+".bmp"));
            Main.width = 0;
            Main.height = 0;

            //Lecture de la signature du fichier bitmap
            System.out.println("[INFO] Signature du fichier : "+(new String(ReadBytes(2, reader), "UTF-8")));

            //Lecture de la taille du fichier bitmap
            ByteBuffer b = ByteBuffer.wrap(ReadBytes(4, reader));
            b.order(ByteOrder.LITTLE_ENDIAN);
            System.out.println("[INFO] Taille du fichier : "+b.getInt());

            //Saut de bits inutiles du header de fichierbitmap
            reader.skipBytes(8);

            //taille du header de l'image
            b = ByteBuffer.wrap(ReadBytes(4, reader));
            b.order(ByteOrder.LITTLE_ENDIAN);

            int headerSize = b.getInt();

            //lecture largeur
            b = ByteBuffer.wrap(ReadBytes(4, reader));
            b.order(ByteOrder.LITTLE_ENDIAN);

            Main.width = b.getInt();
            System.out.println("[INFO] Largeur de l'image : "+Main.width);

            //lecture hauteur
            b = ByteBuffer.wrap(ReadBytes(4, reader));
            b.order(ByteOrder.LITTLE_ENDIAN);

            Main.height = b.getInt();
            System.out.println("[INFO] Hauteur de l'image : "+Main.height);

            //Saut de bits inutiles du header de l'image
            reader.skipBytes(2);

            //lecture du nombre de couleurs par pixel
            b = ByteBuffer.wrap(ReadBytes(2, reader));
            b.order(ByteOrder.LITTLE_ENDIAN);

            Main.depth = b.getShort();
            System.out.println("[INFO] Nombre de bits par pixels : "+Main.depth);

            //Saut de bits inutiles du header de l'image
            reader.skipBytes(headerSize-16);

            //Lecture de l'image pixel par pixel
            try {
                //Initialisation de la variable contenant les bytes de l'image
                Main.image = new byte[Main.width][Main.height][3];
                for(int x = 0 ; x < Main.width ; x++){
                    for(int y = 0 ; y < Main.height ; y++){
                        if(Main.depth > 8) {    //Si image en plus de 256 couleurs
                            Main.image[x][y][2] = reader.readByte(); //blue
                            Main.image[x][y][1] = reader.readByte(); //green
                            Main.image[x][y][0] = reader.readByte(); //red
                        }else{    //Si l'image possède 256 couleurs ou moins
                            Main.image[x][y][0] = reader.readByte();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(Main.depth <= 8){
                FillHistogramGrayScale();
            }

        } catch(Exception e){
            System.out.println(e.getMessage());
        }

    }

    /*
     * Création de l'histogramme d'une image en niveaux de gris
     */
    public static void FillHistogramGrayScale(){

        //Initialisation de l'histogramme
        Main.histogram = new int[(int)Math.pow(2.0,(double)Main.depth)];
        for(int i = 0; i < Main.histogram.length ; i++) Main.histogram[i] = 0;

        //Calcul de l'histogramme pour l'ensemble des couleurs de l'image
        for(int x = 0; x < Main.width ; x++){
            for(int y = 0 ; y < Main.height ; y++){
                Main.histogram[(Main.image[x][y][0]<0)?Main.image[x][y][0]+256:Main.image[x][y][0]]++;
            }
        }
        System.out.println("[INFO] Histogramme de l'image en niveaux de gris créé");
    }

}
