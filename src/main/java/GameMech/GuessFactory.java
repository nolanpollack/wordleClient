package GameMech;

import Messages.GuessMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

// Generates valid guesses for a wordle game.
public class GuessFactory { // the letters that are in the word
    private final Set<Character>[] letterNotAtPosition;
    private final Set<Character> knownLetters;
    private final Set<String> wordBank;

    /**
     * Creates a new GuessFactory. Initializes the word bank.
     */
    public GuessFactory() {

        wordBank = initWordBank();

        letterNotAtPosition = new HashSet[5];
        for (int i = 0; i < 5; i++) {
            letterNotAtPosition[i] = new HashSet<>();
        }
        knownLetters = new HashSet<>();
    }

    /**
     * Initializes the word bank from the txt file in resources.
     * @return a set of words that are in the word bank.
     */
    public Set<String> initWordBank() {
        Set<String> toReturn = new HashSet<>();
        try {
            Scanner file = new Scanner(new File("src/main/resources/project1-words.txt"));
            while (file.hasNext()) {
                toReturn.add(file.next());
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return toReturn;
    }

    /**
     * Creates a new guess, choosing the first legal option in the word bank.
     */
    public String createGuess() {
        return wordBank.iterator().next();
    }

    /**
     * Updates the word bank based on the given word marks.
     * @param marks the word marks that were received from the server.
     */
    public void ReceiveMarks(WordMark[] marks) {
        for (WordMark wordMark : marks) {
            List<Character> checkedLetters = new ArrayList<>();
            for (int i = 0; i < wordMark.marks.length; i++) {
                char letter = wordMark.word.charAt(i);
                switch (wordMark.marks[i]) {
                    case 0:
                        if (knownLetters.contains(letter)) {
                            letterNotAtPosition[i].add(letter);
                        } else {
                            for (int j = 0; j < 5; j++) {
                                letterNotAtPosition[j].add(letter);
                            }
                        }
                        break;
                    case 1:
                        letterNotAtPosition[i].add(letter);
                        knownLetters.add(letter);
                        int index = i;
                        wordBank.removeIf(word -> word.indexOf(letter) < 0 || word.indexOf(letter) == index);
                        break;
                    case 2:
                        knownLetters.add(letter);
                        index = i;
                        wordBank.removeIf(word -> word.charAt(index) != letter);
                        for (int j = 0; j < 5; j++) {
                            if (letterNotAtPosition[j].contains(letter)) {
                                letterNotAtPosition[i].remove(letter);
                            }
                        }
                        break;
                }
                checkedLetters.add(letter);
            }
        }
        updateWordBank();
    }

    /**
     * Updates the word bank by removing letters that are not at a certain position.
     */
    private void updateWordBank() {
        for (int i = 0; i < 5; i++) {
            Set<Character> charList = letterNotAtPosition[i];
            int index = i;
            wordBank.removeIf(word ->
                charList.contains(word.charAt(index))
            );
        }
    }
}
