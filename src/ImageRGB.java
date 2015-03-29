/**
 * ImageRGB.java
 * @version 29/03/2015
 *
 * @author Sun YE [rollingsunmoon@gmail.com]
 * @author Camille BOINAUD [boinaud@polytech.unice.fr]
 */

import utils.Pair;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ImageRGB extends Image {

    public int[][] histogram;

    ImageRGB(String name) {
        super(name);
    }

    /*
    * Convert an image into a byte array
    */
    @Override
    protected byte[] convertImageByteArray() {
        int i = 0;

        byte[] data = new byte[width * height * 3];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                data[i++] = image[x][y][2]; //blue
                data[i++] = image[x][y][1]; //green
                data[i++] = image[x][y][0]; //red
            }
        }
        return data;
    }

    /*
     * Création de l'histogramme d'une image en niveaux de couleurs
     */
    @Override
    public void fillHistogram() {

        //Initialisation de l'histogramme
        histogram = new int[(int)Math.pow(2,8)][3];
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

    @Override
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


    //***********************************************************************
    //*                                                                     *
    //*                    COMPRESSION DE COULEURS RGB                      *
    //*                                                                     *
    //***********************************************************************


    /**
     * Erreur entre deux niveaux de couleurs au niveau de la nouvelle palette
     * de couleurs.
     *
     * @param rgb : choix de la couche de couleur
     * @param a : niveau inférieur
     * @param b : niveau supérieur
     * @return res : [double] erreur engendrée par ces deux niveaux consécutifs
     *               sélectionnés dans la nouvelle palette
     */
    private int intervalCost(int rgb, int a,int b){
        int med = (b - a - 1)/2;
        int res = ((b - a)%2 == 0)?(histogram[a+med+1][rgb]*(med+1)):0;

        for(int i = 1; i <= med ; i++){
            res += i*(histogram[a+i][rgb] + histogram[b - i][rgb]);
        }

        return res;
    }


    /**
     * Initialisation de la récursion de l'erreur totale
     *
     * @param colors : nombre de niveaux de couleurs pour la nouvelle palette de couleurs
     * @return optima
     */
    private Pair<Integer,Integer>[][] optimalLossInit(int rgb, int colors) {
        int nbColor = histogram.length;
        Pair<Integer,Integer>[][] optima = new Pair[colors][nbColor];

        for(int i = 0; i < colors ; i++){
            for(int j = 0; j < nbColor ; j++){
                optima[i][j] = new Pair(0,null);
            }
        }
        int partialSum = 0;

        for (int i = 1; i < nbColor; i++) {
            partialSum += histogram[i - 1][rgb];
            optima[0][i].setFirst(optima[0][i - 1].getFirst() + partialSum);
        }

        return optima;
    }

    /**
     * Calcul récursif de l'erreur totale
     *
     * @param colors : nombre de couleurs pour la nouvelle palette de couleurs
     * @return optima
     */
    private Pair<Integer,Integer>[][] optimalLoss(int rgb, int colors){
        Pair<Integer,Integer>[][] optima = optimalLossInit(rgb, colors);
        for(int i = 1 ; i < colors ; i++){
            for(int j = i+1 ; j< histogram.length ; j++){
                optima[i][j].setFirst(optima[i - 1][j - 1].getFirst());
                optima[i][j].setSecond(j - 1);
                for(int k = i ; k < j ; k++){
                    optima[i][j] = minFirst(optima[i][j],
                            new Pair<Integer, Integer>(optima[i-1][k].getFirst() + intervalCost(rgb, k,j), k));
                }
            }
        }
        return optima;
    }

    /**
     * Fonction de calcul de l’erreur entre a et n ;
     *
     * @param a : niveau inférieur
     * @return sum
     */
    private int lastCost(int rgb, int a){
        int n = histogram.length - 1;
        int sum = 0;
        for(int i = a+1 ; i <=n ; i++){
            sum += (i - a)*histogram[i][rgb];
        }
        return sum;
    }

    /**
     * Fonction de détermination de position du dernier
     * niveau de couleurs de la palette retenue.
     *
     * @param optima
     * @return best.getSecond()
     */
    private int findLast(int rgb, Pair<Integer,Integer>[][] optima){
        int colors = optima.length;
        int nbColor = optima[colors - 1].length;
        Pair<Integer,Integer> best = new Pair(optima[colors - 1][nbColor - 1].getFirst(), nbColor - 1);

        for(int i = nbColor - 2 ; i > colors ; i--){
            best = minFirst(best, new Pair(optima[colors - 1][i].getFirst() + lastCost(rgb, i), i));
        }
        return best.getSecond();
    }

    /**
     * Calcul de la palette optimale
     *
     * @param colors
     */
    private ArrayList<Integer> optimalColors(int rgb, int colors){
        Pair<Integer,Integer>[][] optima = optimalLoss(rgb, colors);
        return optimalColorsRec(optima, new ArrayList<Integer>(), colors - 1, findLast(rgb, optima));
    }

    protected ArrayList<Integer> optimalColorsRec(Pair<Integer,Integer>[][] p, ArrayList<Integer> acc, int i, int j){
        if(p[i][j].getSecond()== null) {
            acc.add(j);
        }
        else {
            acc.add(j);
            optimalColorsRec(p, acc, i - 1, p[i][j].getSecond());
        }
        return acc;
    }

    private int[] changeBegin(int[] newHistogram, int marqueur){
        for(int i = 0; i < marqueur ; i++){
            newHistogram[i] = marqueur;
        }
        return newHistogram;
    }

    private int[] changeMiddle(int [] newHistogram, int marqueur1, int marqueur2){
        int med = (marqueur1 + marqueur2)/2;
        for(int i = marqueur1 ; i < med ; i++){
            newHistogram[i] = marqueur1;
        }
        for(int i = med ; i < marqueur2 ; i++){
            newHistogram[i] = marqueur2;
        }
        return newHistogram;
    }

    private int[] changeEnd(int[] newHistogram, int marqueur){
        for(int i = marqueur ; i < newHistogram.length ; i++){
            newHistogram[i] = marqueur;
        }
        return newHistogram;
    }

    private int[] change(int rgb, int nbcolors){
        ArrayList<Integer> colors = optimalColors(rgb, nbcolors);
        int[] newHistogram = new int[histogram.length];
        newHistogram = changeBegin(newHistogram, colors.get(colors.size()-1));
        for(int i = colors.size()-1; i > 0 ; i--){
            newHistogram = changeMiddle(newHistogram, colors.get(i), colors.get(i-1));
        }
        newHistogram = changeEnd(newHistogram, colors.get(0));
        return newHistogram;
    }

    @Override
    protected void conversion(int nbcolors){
        int[][] newColors = new int[3][];
        newColors[0] = change(0,nbcolors);
        newColors[1] = change(1,nbcolors);
        newColors[2] = change(2,nbcolors);

        int indice = 0;
        for(int i = 0 ; i < super.width ; i++){
            for(int j = 0 ; j < super.height ; j++){

                //red
                indice = (super.image[i][j][0]<0)?super.image[i][j][0]+256:super.image[i][j][0];
                super.image[i][j][0] = (newColors[0][indice]>127)? (byte)(newColors[0][indice]- 256): (byte)newColors[0][indice];

                //green
                indice = (super.image[i][j][1]<0)?super.image[i][j][1]+256:super.image[i][j][1];
                super.image[i][j][1] = (newColors[1][indice]>127)? (byte)(newColors[1][indice]- 256): (byte)newColors[1][indice];

                //blue
                indice = (super.image[i][j][2]<0)?super.image[i][j][2]+256:super.image[i][j][2];
                super.image[i][j][2] = (newColors[2][indice]>127)? (byte)(newColors[2][indice]- 256): (byte)newColors[2][indice];
            }
        }
    }


}
