import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Path file = Path.of(args.length > 0 ? args[0] : "questions.txt");
        List<Question> bank;
        try {
            bank = QuestionRepository.load(file);
        } catch (IOException e) {
            System.err.println("Could not load questions from " + file.toAbsolutePath() + ": " + e.getMessage());
            return;
        }
        new QuestionService(bank).run();
    }
}
