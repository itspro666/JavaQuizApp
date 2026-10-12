import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads questions from a text file, one per line:
 * category|question|correct answer|wrong 1|wrong 2|wrong 3
 * Blank lines and lines starting with # are ignored.
 */
public final class QuestionRepository {

    private QuestionRepository() {}

    public static List<Question> load(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        List<Question> result = new ArrayList<>();
        int id = 1;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] p = line.split("\\|", -1);
            if (p.length != 6) {
                throw new IOException("Line " + (i + 1) + ": expected 6 fields but found " + p.length);
            }
            // The correct answer is stored first (index 0); it gets shuffled at quiz time.
            result.add(new Question(id++, p[0].trim(), p[1].trim(),
                    List.of(p[2].trim(), p[3].trim(), p[4].trim(), p[5].trim()), 0));
        }
        if (result.isEmpty()) throw new IOException("No questions found in " + file);
        return result;
    }
}
