/**
 * ImageGrayScale.java
 * @version 29/03/2015
 *
 * @author Sun YE [rollingsunmoon@gmail.com]
 * @author Camille BOINAUD [boinaud@polytech.unice.fr]
 */

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import utils.Pair;

public class ImageGrayScale extends Image {

    public int[] histogram;

    ImageGrayScale(String name) {
        super(name);
    }

    /**
     * Convert an image into a byte array
     */
    @Override
    protected byte[] convertImageByteArray() {
        int i = 0;

        byte[] data = new byte[width * height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                data[i++] = image[x][y][0];
            }
        }
        return data;
    }

    /**
     * Création de l'histogramme d'une image en niveaux de gris
     */
    @Override
    public void fillHistogram() {

        //Initialisation de l'histogramme
        histogram = new int[(int) Math.pow(2.0, (double) super.depth)];
        for (int i = 0; i < histogram.length; i++) histogram[i] = 0;

        //Calcul de l'histogramme pour l'ensemble des couleurs de l'image
        for (int x = 0; x < super.width; x++) {
            for (int y = 0; y < super.height; y++) {
                histogram[(super.image[x][y][0] < 0) ? super.image[x][y][0] + 256 : super.image[x][y][0]]++;
            }
        }
        System.out.println("[INFO] Histogramme de l'image en niveaux de gris créé");
    }

    /**
     * Lecture du contenu de l'image pixel par pixel
     *
     * @param reader
     */
    @Override
    protected void readImage(DataInputStream reader) {
        //Lecture de l'image pixel par pixel
        try {
            //Initialisation de la variable contenant les bytes de l'image
            super.image = new byte[super.width][super.height][1];
            for (int x = 0; x < super.width; x++) {
                for (int y = 0; y < super.height; y++) {
                    super.image[x][y][0] = reader.readByte();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /***********************************************************************
     *                                                                     *
     *                  COMPRESSION DE NIVEAUX DE GRIS                     *
     *                                                                     *
     ***********************************************************************/


    /**
     * Erreur entre deux niveaux de gris au niveau de la nouvelle palette
     * de couleurs.
     *
     * @param a : niveau inférieur
     * @param b : niveau supérieur
     * @return res : [double] erreur engendrée par ces deux niveaux consécutifs
     * sélectionnés dans la nouvelle palette
     */
    private int intervalCost(int a, int b) {
        int med = (b - a - 1) / 2;
        int res = ((b - a) % 2 == 0) ? (histogram[a + med + 1] * (med + 1)) : 0;

        for (int i = 1; i <= med; i++) {
            res += i * (histogram[a + i] + histogram[b - i]);
        }

        return res;
    }


    /**
     * Initialisation de la récursion de l'erreur totale
     *
     * @param greys : nombre de niveaux de gris pour la nouvelle palette de couleurs
     * @return optima
     */
    private Pair<Integer, Integer>[][] optimalLossInit(int greys) {
        int nbColor = histogram.length;
        Pair<Integer, Integer>[][] optima = new Pair[greys][nbColor];

        for (int i = 0; i < greys; i++) {
            for (int j = 0; j < nbColor; j++) {
                optima[i][j] = new Pair(0, null);
            }
        }
        int partialSum = 0;

        for (int i = 1; i < nbColor; i++) {
            partialSum += histogram[i - 1];
            optima[0][i].setFirst(optima[0][i - 1].getFirst() + partialSum);
        }

        return optima;
    }

    /**
     * Calcul récursif de l'erreur totale
     *
     * @param greys : nombre de niveaux de gris pour la nouvelle palette de couleurs
     * @return optima
     */
    private Pair<Integer, Integer>[][] optimalLoss(int greys) {
        Pair<Integer, Integer>[][] optima = optimalLossInit(greys);
        for (int i = 1; i < greys; i++) {
            for (int j = i + 1; j < histogram.length; j++) {
                optima[i][j].setFirst(optima[i - 1][j - 1].getFirst());
                optima[i][j].setSecond(j - 1);
                for (int k = i; k < j; k++) {
                    optima[i][j] = minFirst(optima[i][j],
                            new Pair<Integer, Integer>(optima[i - 1][k].getFirst() + intervalCost(k, j), k));
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
    private int lastCost(int a) {
        int n = histogram.length - 1;
        int sum = 0;
        for (int i = a + 1; i <= n; i++) {
            sum += (i - a) * histogram[i];
        }
        return sum;
    }

    /**
     * Fonction de détermination de position du dernier
     * niveau de gris de la palette retenue.
     *
     * @param optima
     * @return best.getSecond()
     */
    private int findLast(Pair<Integer, Integer>[][] optima) {
        int greys = optima.length;
        int nbColor = optima[greys - 1].length;
        Pair<Integer, Integer> best = new Pair(optima[greys - 1][nbColor - 1].getFirst(), nbColor - 1);

        for (int i = nbColor - 2; i > greys; i--) {
            best = minFirst(best, new Pair(optima[greys - 1][i].getFirst() + lastCost(i), i));
        }
        return best.getSecond();
    }

    /**
     * Calcul de la palette optimale
     *
     * @param greys
     */
    private ArrayList<Integer> optimalColors(int greys) {
        Pair<Integer, Integer>[][] optima = optimalLoss(greys);
        return optimalColorsRec(optima, new ArrayList<Integer>(), greys - 1, findLast(optima));
    }

    protected ArrayList<Integer> optimalColorsRec(Pair<Integer, Integer>[][] p, ArrayList<Integer> acc, int i, int j) {
        if (p[i][j].getSecond() == null) {
            acc.add(j);
        } else {
            acc.add(j);
            optimalColorsRec(p, acc, i - 1, p[i][j].getSecond());
        }
        return acc;
    }

    private int[] changeBegin(int[] newHistogram, int marqueur) {
        for (int i = 0; i < marqueur; i++) {
            newHistogram[i] = marqueur;
        }
        return newHistogram;
    }

    private int[] changeMiddle(int[] newHistogram, int marqueur1, int marqueur2) {
        int med = (marqueur1 + marqueur2) / 2;
        for (int i = marqueur1; i < med; i++) {
            newHistogram[i] = marqueur1;
        }
        for (int i = med; i < marqueur2; i++) {
            newHistogram[i] = marqueur2;
        }
        return newHistogram;
    }

    private int[] changeEnd(int[] newHistogram, int marqueur) {
        for (int i = marqueur; i < newHistogram.length; i++) {
            newHistogram[i] = marqueur;
        }
        return newHistogram;
    }

    private int[] change(int nbGreys) {
        ArrayList<Integer> colors = optimalColors(nbGreys);
        int[] newHistogram = new int[histogram.length];
        newHistogram = changeBegin(newHistogram, colors.get(colors.size() - 1));
        for (int i = colors.size() - 1; i > 0; i--) {
            newHistogram = changeMiddle(newHistogram, colors.get(i), colors.get(i - 1));
        }
        newHistogram = changeEnd(newHistogram, colors.get(0));
        return newHistogram;
    }

    @Override
    protected void conversion(int nbGreys) {
        int[] newColors = change(nbGreys);
        for (int i = 0; i < super.width; i++) {
            for (int j = 0; j < super.height; j++) {
                int indice = (super.image[i][j][0] < 0) ? super.image[i][j][0] + 256 : super.image[i][j][0];
                super.image[i][j][0] = (newColors[indice] > 127) ? (byte) (newColors[indice] - 256) : (byte) newColors[indice];
            }
        }
    }
}