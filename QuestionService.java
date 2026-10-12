import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class QuestionService {

    private static final int QUIT = -2;
    private static final int SKIP = -1;

    private record Attempt(Question question, int chosen) {
        boolean correct() { return question.isCorrect(chosen); }
    }

    private final List<Question> bank;
    private final Scanner in = new Scanner(System.in);
    private final Random rnd = new Random();

    public QuestionService(List<Question> bank) {
        this.bank = List.copyOf(bank);
    }

    // ---------- main menu ----------

    public void run() {
        System.out.println("=== Programming Quiz (" + bank.size() + " questions) ===");
        while (true) {
            System.out.println("\n1) Start quiz\n2) Show categories\n3) Exit");
            switch (readInt("Choose: ", 1, 3)) {
                case 1 -> playQuiz();
                case 2 -> categoryCounts().forEach((c, n) -> System.out.println(" - " + c + " (" + n + ")"));
                default -> { System.out.println("Goodbye!"); return; }
            }
        }
    }

    // ---------- quiz flow ----------

    public void playQuiz() {
        List<Question> pool = choosePool();
        int count = readInt("How many questions? (1-" + pool.size() + "): ", 1, pool.size());

        Collections.shuffle(pool, rnd);
        List<Question> quiz = pool.subList(0, count).stream().map(q -> q.shuffled(rnd)).toList();

        List<Attempt> attempts = new ArrayList<>();
        long start = System.nanoTime();
        System.out.println("\nAnswer with 1-4, S to skip, Q to quit.\n");

        for (int i = 0; i < quiz.size(); i++) {
            Question q = quiz.get(i);
            show(q, i + 1, quiz.size());
            int choice = readAnswer(q.options().size());
            if (choice == QUIT) break;

            attempts.add(new Attempt(q, choice));
            if (choice == SKIP) System.out.println("Skipped. Answer: " + q.correctAnswer() + "\n");
            else if (q.isCorrect(choice)) System.out.println("Correct!\n");
            else System.out.println("Wrong. Answer: " + q.correctAnswer() + "\n");
        }
        long seconds = (System.nanoTime() - start) / 1_000_000_000L;
        printResults(attempts, seconds);
    }

    private List<Question> choosePool() {
        List<String> cats = new ArrayList<>(categories());
        System.out.println("\n0) All categories");
        for (int i = 0; i < cats.size(); i++) System.out.println((i + 1) + ") " + cats.get(i));
        int pick = readInt("Category: ", 0, cats.size());
        if (pick == 0) return new ArrayList<>(bank);
        String chosen = cats.get(pick - 1);
        return bank.stream().filter(q -> q.category().equals(chosen)).collect(Collectors.toCollection(ArrayList::new));
    }

    private void show(Question q, int number, int total) {
        System.out.printf("Q%d/%d [%s] %s%n", number, total, q.category(), q.text());
        for (int i = 0; i < q.options().size(); i++) {
            System.out.printf("  %d) %s%n", i + 1, q.options().get(i));
        }
    }

    private void printResults(List<Attempt> attempts, long seconds) {
        if (attempts.isEmpty()) {
            System.out.println("No questions answered.");
            return;
        }
        long right = attempts.stream().filter(Attempt::correct).count();
        System.out.println("=========== RESULT ===========");
        System.out.printf("Score: %d/%d (%.0f%%)  Time: %ds%n", right, attempts.size(),
                100.0 * right / attempts.size(), seconds);

        Map<String, int[]> perCategory = new TreeMap<>();
        for (Attempt a : attempts) {
            int[] s = perCategory.computeIfAbsent(a.question().category(), k -> new int[2]);
            if (a.correct()) s[0]++;
            s[1]++;
        }
        System.out.println("\nBy category:");
        perCategory.forEach((c, s) -> System.out.printf("  %-12s %d/%d%n", c, s[0], s[1]));

        List<Attempt> missed = attempts.stream().filter(a -> !a.correct()).toList();
        if (!missed.isEmpty()) {
            System.out.println("\nReview:");
            for (Attempt a : missed) {
                String yours = a.chosen() == SKIP ? "(skipped)" : a.question().options().get(a.chosen());
                System.out.printf("  %s%n    Your answer: %s | Correct: %s%n",
                        a.question().text(), yours, a.question().correctAnswer());
            }
        }
    }

    // ---------- helpers ----------

    private SortedSet<String> categories() {
        return bank.stream().map(Question::category).collect(Collectors.toCollection(TreeSet::new));
    }

    private Map<String, Long> categoryCounts() {
        return bank.stream().collect(Collectors.groupingBy(Question::category, TreeMap::new, Collectors.counting()));
    }

    /** Returns 0-based option index, SKIP or QUIT. Repeats until the input is valid. */
    private int readAnswer(int optionCount) {
        while (true) {
            String s = nextLine("> ");
            if (s.equalsIgnoreCase("q")) return QUIT;
            if (s.equalsIgnoreCase("s")) return SKIP;
            try {
                int n = Integer.parseInt(s);
                if (n >= 1 && n <= optionCount) return n - 1;
            } catch (NumberFormatException ignored) { }
            System.out.println("Please enter 1-" + optionCount + ", S or Q.");
        }
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            try {
                int n = Integer.parseInt(nextLine(prompt));
                if (n >= min && n <= max) return n;
            } catch (NumberFormatException ignored) { }
            System.out.println("Enter a number between " + min + " and " + max + ".");
        }
    }

    private String nextLine(String prompt) {
        System.out.print(prompt);
        if (!in.hasNextLine()) {
            System.out.println("\nInput closed. Goodbye!");
            System.exit(0);
        }
        return in.nextLine().trim();
    }
}
