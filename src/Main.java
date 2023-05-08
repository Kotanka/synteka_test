import java.io.*;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Main {
    static class Cell {
        int row;
        int column;
        int value;

        public Cell(int row, int column, int value) {
            this.row = row;
            this.column = column;
            this.value = value;
        }
    }

    static class CellComparator implements Comparator<Cell> {

        @Override
        public int compare(Cell o1, Cell o2) {
            if (o1.value > o2.value)
                return -1;
            else if (o1.value == o2.value)
                return 0;
            else
                return 1;
        }
    }

    public static String[] readStringSet(BufferedReader reader)  throws IOException {
        int n = Integer.parseInt(reader.readLine().split(" ")[0]);
        String[] set = new String[n];
        for (int i = 0; i < set.length; i++)
            set[i] = reader.readLine();

        return set;
    }

    public static int[][] fillPhrasesDist(String[] setSmall, String[] setLarge) {
        int[][] distance = new int[setSmall.length][setLarge.length];

        for (int i = 0; i < distance.length; i++) {
            for (int j = 0; j < distance[0].length; j++) {
                distance[i][j] = calcPhraseDist(setSmall[i], setLarge[j]);
            }
        }

        return distance;
    }

    public static int calcPhraseDist(String s1, String s2) {
        String[] words1 = s1.split(" ");
        String[] words2 = s2.split(" ");

        if (words1.length > words2.length) {
            String[] tmp = words1;
            words1 = words2;
            words2 = tmp;
        }

        int[][] dist = new int[words1.length][words2.length];

        // Считаем расстояние Левенштейна для всех сочетаний слов из s1 и s2
        for (int i = 0; i < dist.length; i++) {
            for (int j = 0; j < dist[0].length; j++) {
                dist[i][j] = calcWordDist(words1[i], words2[j]);
            }
        }

        // Находим по оптимальной паре (расстояние Левенштейна минимально)
        // для каждого слова более короткой строки s1
        int[] wordPairs = getPairs(dist);
        boolean[] visitedS2 = new boolean[dist[0].length];

        int sum = 0;

        // Суммируем расстояние Левенштейна для каждой пары
        for (int i = 0; i < wordPairs.length; i++) {
            sum += dist[i][wordPairs[i]];
            visitedS2[wordPairs[i]] = true;
        }

        // Прибавляем к сумме все слова без пары из более длинной строки s2
        for (int i = 0; i < visitedS2.length; i++) {
            if (!visitedS2[i])
                sum += words2[i].length();
        }

        return sum;
    }

    public static int[] getPairs(int[][] dist) {
        PriorityQueue<Cell> queue = new PriorityQueue<>(new CellComparator());

        // Проходимся по строкам таблицы и находим для каждой минимум. Добавляем в heap
        for (int i = 0; i < dist.length; i++) {
            int indexMin = 0;
            for (int j = 0; j < dist[0].length; j++) {
                if (dist[i][j] < dist[i][indexMin])
                    indexMin = j;
            }
            queue.add(new Cell(i, indexMin, dist[i][indexMin]));
        }

        boolean[] visited2 = new boolean[dist[0].length];
        int[] pairs = new int[dist.length];

        // Вынимаем с верха heap, пока для каждой строки не будет найдена минимальная пара (без пересечений)
        while (!queue.isEmpty()) {
            Cell elem = queue.poll();
            // Если пара занята, то находим следующий минимум в строчке таблицы
            if (visited2[elem.column]) {
                int firstFree = 0;
                while (visited2[firstFree])
                    firstFree++;
                int i = elem.row;
                int indexMin = firstFree;
                for (int j = firstFree + 1; j < dist[0].length; j++) {
                    if (!visited2[j] && dist[i][j] < dist[i][indexMin])
                        indexMin = j;
                }
                elem.column = indexMin;
                queue.add(elem);
            } else { // Если пара свободна, то добавляем в ответ
                pairs[elem.row] = elem.column;
                visited2[elem.column] = true;
            }
        }

        return pairs;
    }

    public static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    static int calcWordDist(String w1, String w2) {
        int[][] dp = new int[w1.length() + 1][w2.length() + 1];

        for (int i = 0; i <= w1.length(); i++) {
            for (int j = 0; j <= w2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int tmp = Math.min(dp[i - 1][j - 1] + costOfSubstitution(w1.charAt(i - 1), w2.charAt(j - 1)),
                            dp[i - 1][j] + 1);
                    dp[i][j] = Math.min(dp[i][j - 1] + 1, tmp);
                }
            }
        }

        return dp[w1.length()][w2.length()];
    }

    public static void printResult(int[] pairs, String[] set1, String[] set2, boolean set1IsSmall) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));

        boolean[] visited;

        if (set1IsSmall)
            visited = new boolean[set2.length];
        else
            visited = new boolean[set1.length];

        for (int i = 0; i < pairs.length; i++) {
            if (set1IsSmall)
                writer.write(set1[i] + ":" + set2[pairs[i]] + "\n");
            else
                writer.write(set1[pairs[i]] + ":" + set2[i] + "\n");
            visited[pairs[i]] = true;
        }

        for (int i = 0; i < visited.length; i++) {
            if (!visited[i]) {
                if (set1IsSmall)
                    writer.write(set2[i]);
                else
                    writer.write(set1[i]);
                writer.write(":?\n");
            }
        }

        writer.close();
    }


    public static void main(String[] args) {
        String fileName = "input.txt";
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(fileName));

            String[] set1 = readStringSet(reader);
            String[] set2 = readStringSet(reader);

            int[][] phraseDistance;
            boolean set1IsSmall = true;

            if (set1.length > set2.length) {
                set1IsSmall = false;
                phraseDistance = fillPhrasesDist(set2, set1);
            }
            else {
                phraseDistance = fillPhrasesDist(set1, set2);
            }

            int[] pairs = getPairs(phraseDistance);

            printResult(pairs, set1, set2, set1IsSmall);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
