/**
 * Image.java
 * @version 29/03/2015
 *
 * @author Sun YE [rollingsunmoon@gmail.com]
 * @author Camille BOINAUD [boinaud@polytech.unice.fr]
 */

import utils.Pair;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class Image {

    public int width;
    public int height;
    public int depth;
    public byte[][][] image;
    public byte[] header;
    public String filename;

    public Image(String name){
        this.filename = name;
        this.width = 0;
        this.height = 0;
        setHeader(name);
        readBitmapHeader(name);
    }

    /*
     * Lecture du fichier bitmap, extraction des bytes de pixel dans la variable : image
     */
    private void setHeader(String path){

        try {
            DataInputStream reader = new DataInputStream(new FileInputStream(path+".bmp"));

            byte[] fileHeader = readBytes(14,reader);
            byte[] imageHeaderSize = readBytes(4, reader);

            ByteBuffer b = ByteBuffer.wrap(imageHeaderSize);
            b.order(ByteOrder.LITTLE_ENDIAN);

            byte[] imageHeader = readBytes(b.getInt() - 4, reader);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            outputStream.write(fileHeader);
            outputStream.write(imageHeaderSize);
            outputStream.write(imageHeader);

            header = outputStream.toByteArray( );

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * On récupère et analyse l'entête du fichier bmp
     */
    private void readBitmapHeader(String path){

        System.out.println("--> Lecture du fichier : "+path);

        try {
            DataInputStream reader = new DataInputStream(new FileInputStream(path + ".bmp"));

            //Lecture de la signature du fichier bitmap
            System.out.println("[INFO] Signature du fichier : "+(new String(readBytes(2, reader), "UTF-8")));

            //Lecture de la taille du fichier bitmap
            ByteBuffer b = ByteBuffer.wrap(readBytes(4, reader));
            b.order(ByteOrder.LITTLE_ENDIAN);
            System.out.println("[INFO] Taille du fichier : "+b.getInt());

            //Saut de bits inutiles du header de fichierbitmap
            reader.skipBytes(8);

            //taille du header de l'image
            b = ByteBuffer.wrap(readBytes(4, reader));
            b.order(ByteOrder.LITTLE_ENDIAN);

            int headerSize = b.getInt();

            //lecture largeur
            b = ByteBuffer.wrap(readBytes(4, reader));
            b.order(ByteOrder.LITTLE_ENDIAN);

            width = b.getInt();
            System.out.println("[INFO] Largeur de l'image : "+width);

            //lecture hauteur
            b = ByteBuffer.wrap(readBytes(4, reader));
            b.order(ByteOrder.LITTLE_ENDIAN);

            height = b.getInt();
            System.out.println("[INFO] Hauteur de l'image : "+height);

            //Saut de bits inutiles du header de l'image
            reader.skipBytes(2);

            //lecture du nombre de couleurs par pixel
            b = ByteBuffer.wrap(readBytes(2, reader));
            b.order(ByteOrder.LITTLE_ENDIAN);

            depth = b.getShort();
            System.out.println("[INFO] Nombre de bits par pixels : "+depth);

            //Saut de bits inutiles du header de l'image
            reader.skipBytes(headerSize-16);

            readImage(reader);
            fillHistogram();

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private byte[] readBytes(int nb, DataInputStream reader){

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
     * Write a byte array into a bmp file.
     * @params color : false -> image transformation in gray scale
     */
    public void writeBitmap(){
        try {
            DataOutputStream writer = new DataOutputStream (new FileOutputStream(filename+"_modified.bmp"));
            writer.write(header);
            writer.write(convertImageByteArray());
            writer.close();
            System.out.println("[INFO] Ecriture du fichier : "+filename+"_modified.bmp");
        } catch (FileNotFoundException e) {
            System.out.println("[FAIL] Ecriture du fichier impossible, fichier non trouvé.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("[FAIL] Ecriture du fichier impossible, exeption entrée/sorite levée.");
            e.printStackTrace();
        }

    }

   /*
    * Création de l'histogramme d'une image en niveaux de gris
    */
    abstract void fillHistogram();

    /*
     * Convert an image into a byte array
     */
    abstract byte[] convertImageByteArray();

    /*
     * Lecture de l'image pixel par pixel
     */
    abstract void readImage(DataInputStream reader);

    /*
     * Conversion d'une image en nbColors couleurs
     */
    abstract void conversion(int nbColors);

    /*
     * Minimum Pair.first
     */
    protected Pair<Integer,Integer> minFirst(Pair<Integer,Integer> a, Pair<Integer,Integer> b){
        if(a.getFirst() <= b.getFirst()) return a;
        else return b;
    }
}
