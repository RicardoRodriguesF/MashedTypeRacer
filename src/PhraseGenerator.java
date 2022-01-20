import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

public class PhraseGenerator {


    private final LinkedList<String> lines = new LinkedList<>();

    public PhraseGenerator() {
        loadLinesFromFile();
    }

    public void loadLinesFromFile() {


        lines.addAll(Arrays.asList(Phrases.PHRASE_LIST.split("\n")));


    }

    public String randomizePhrase(String phrase) {

        StringBuilder finalPhrase = new StringBuilder();

        String[] splattedPhrase = phrase.split(" ");

        Collections.shuffle(Arrays.asList(splattedPhrase));

        for (String word : splattedPhrase) {
            finalPhrase.append(word);
            finalPhrase.append(" ");
        }

        return finalPhrase.toString();

    }

    public String randomizeWords(String phrase) {

        StringBuilder finalPhrase = new StringBuilder();

        String[] splattedPhrase = phrase.split(" ");

        for (String word : splattedPhrase) {

            String[] wordRandomized = word.split("");

            Collections.shuffle(Arrays.asList(wordRandomized));

            for (String character : wordRandomized) {
                finalPhrase.append(character);
            }
            finalPhrase.append(" ");
        }

        return finalPhrase.toString();
    }

    public String[] grabTargetPhrases(boolean eachWordShuffle) {

        if (lines.isEmpty()) {
            System.out.println("You can't grab more lines! Your list is empty!");
            return null;
        }

        String normalPhrase = lines.get((int) Math.floor(Math.random() * lines.size()));
        String randomizedPhrase;

        if (eachWordShuffle) {
            randomizedPhrase = randomizeWords(normalPhrase);
        } else {
            randomizedPhrase = randomizePhrase(normalPhrase);
        }

        return new String[]{normalPhrase, randomizedPhrase};

    }

}
