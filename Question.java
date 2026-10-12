import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Immutable quiz question. Requires Java 16+ (record).
 * correctIndex is the 0-based position of the right answer inside options.
 */
public record Question(int id, String category, String text, List<String> options, int correctIndex) {

    public Question {
        if (options == null || options.size() < 2) {
            throw new IllegalArgumentException("A question needs at least 2 options");
        }
        if (correctIndex < 0 || correctIndex >= options.size()) {
            throw new IllegalArgumentException("correctIndex out of range for question " + id);
        }
        options = List.copyOf(options);
    }

    public String correctAnswer() {
        return options.get(correctIndex);
    }

    public boolean isCorrect(int choice) {
        return choice == correctIndex;
    }

    /** Returns a copy with the options in random order, so the right answer is not always in the same slot. */
    public Question shuffled(Random rnd) {
        String right = correctAnswer();
        List<String> copy = new ArrayList<>(options);
        Collections.shuffle(copy, rnd);
        return new Question(id, category, text, copy, copy.indexOf(right));
    }
}
