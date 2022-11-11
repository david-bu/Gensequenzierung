import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Genome {

    private static final int DEFAULT_SIZE = 30;
    private static int[][] genes = new int[DEFAULT_SIZE][2];
    private static int geneIndex = 0;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("kein Dateipfad angegeben!");
            System.exit(-1);
        }
        readGenome(args[0]);
    }

    /**
     * reads a char array of bases, check for start and stop codons and outputs all gene strings
     * @param file file path
     */
    public static void readGenome(String file) {
        Path filePath = Path.of(file);
        try(BufferedReader reader = Files.newBufferedReader(filePath)) {
            long length = new File(file).length();
            char[] genome = new char[(int)length];
            if (reader.read(genome) != length) {
                System.out.println("Es konnte nicht die gesamte Datei gelesen werden!");
                System.exit(-1);
            }

            int invalidBasePos = areAllBases(genome);
            if (invalidBasePos != -1) {
                System.err.println("Der Buchstabe an Position " + invalidBasePos + " stellt keine Base dar!");
                System.exit(-1);
            }

            for (int i = 0; i < genome.length - 5; i++) {
                if (isStartCodon(genome, i)) {
                    addStartCodon(i);
                    for (int j = i + 3; j < genome.length - 2; j=j+3) {
                        if (isStopCodon(genome, j)) {
                            addStopCodon(j);
                            break;
                        }
                    }
                }
            }

            String[] geneStrings = getGeneStrings(genome);
            System.out.println(geneStrings.length + " Genes gefunden");
            for (String geneString : geneStrings) {
                System.out.println(geneString);
            }
        } catch (IOException e) {
            System.err.println("Die Datei konnte nicht gelesen werden!");
        }
    }

    /**
     * checks for every char if it's a valid base
     * @param genome array containing all bases (which should be checked)
     * @return -1 if it's a valid array of bases else first invalid position
     */
    private static int areAllBases(char[] genome) {
        for (int i = 0; i < genome.length; i++) {
            if (!isBase(genome[i]))
                return i;
        }
        return -1;
    }

    /**
     * checks for a char if it's a base
     * @param chr char to check as a valid base
     * @return true if chr is valid else false
     */
    private static boolean isBase(char chr) {
        return (chr == 'a' || chr == 'c' || chr == 'g' || chr == 't');
    }

    /**
     * checks if the three chars in genome at i, i+1 and i+2 is a start codon
     * @param genome char array of all bases
     * @param i first index of the three chars to check
     * @return true if it's a start codon
     */
    private static boolean isStartCodon(char[] genome, int i) {
        return (genome[i] == 'a' && genome[i+1] == 't' && genome[i+2] == 'g');
    }

    /**
     * checks if the three chars in genome at i, i+1 and i+2 is a stop codon
     * @param genome char array of all bases
     * @param i first index of the three chars to check
     * @return true if it's a stop codon
     */
    private static boolean isStopCodon(char[] genome, int i) {
        return (genome[i] == 't' &&
                ((genome[i+1] == 'g' && genome[i+2] == 'a') ||
                (genome[i+1] == 'a' && genome[i+2] == 'a') ||
                (genome[i+1] == 'a' && genome[i+2] == 'g'))
        );
    }

    /**
     * adds a start codon position in the char array of all bases
     * @param position index of the first element of the start codon
     */
    private static void addStartCodon(int position) {
        addCodon(position, true);
    }

    /**
     * adds a stop codon position in the char array of all bases
     * @param position index of the first element of the stop codon
     */
    private static void addStopCodon(int position) {
        addCodon(position, false);
    }

    /**
     * adds a codon, increases index if needed
     * @param position index of the first element of the codon
     * @param isStart true if start codon, false if stop codon
     */
    private static void addCodon(int position, boolean isStart) {
        if (geneIndex >= genes.length)
            resizeGenes();

        int codonPos = isStart ? 0 : 1;
        genes[geneIndex][codonPos] = position;
        geneIndex += codonPos; // add 1 if it's a stop codon
    }

    /**
     * doubles the size of the array of all gene start and stop codons and copies values
     */
    private static void resizeGenes() {
        int[][] newGenes = new int[genes.length * 2][2];

        for (int i = 0; i < genes.length; i++) {
            newGenes[i][0] = genes[i][0];
            newGenes[i][1] = genes[i][1];
        }

        genes = newGenes;
    }

    /**
     * creates a string array out of the array of start and stop codons
     * converts every int[2] to string; { startCodonIndex, stopCodonIndex } -> string
     * @param genome char array where the gene bases are stored
     * @return array containing all gene strings
     */
    private static String[] getGeneStrings(char[] genome) {
        String[] geneStrings = new String[geneIndex];
        for (int i = 0; i < geneStrings.length; i++) {
            int startIndex = genes[i][0];
            int stopIndex = genes[i][1];
            int count = stopIndex - startIndex + 3;
            geneStrings[i] = String.copyValueOf(genome, startIndex, count);
        }
        return geneStrings;
    }
}