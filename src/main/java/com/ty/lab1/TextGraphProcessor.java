package com.ty.lab1;

import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

public class TextGraphProcessor {
    // 记录 A -> B 的次数（有向）
    private Map<String, Map<String, Integer>> graph = new HashMap<>();
    private final double DAMPING_FACTOR = 0.85;
    private final int MAX_ITERATIONS = 100;
    private final double CONVERGENCE_THRESHOLD = 0.0001;
    private Random random = new Random();

    public static void main(String[] args) throws IOException {
        // 在程序入口设置UI类型
        System.setProperty("org.graphstream.ui", "swing");
        new TextGraphProcessor().run(args);
    }

    // 核心方法
    public void run(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        String filePath;

        if (args.length > 0) {
            filePath = args[0];
        } else {
            System.out.print("Enter text file path: ");
            filePath = scanner.nextLine();
        }

        buildGraph(filePath);
        showMenu(scanner);
    }

    // 构建图结构
    private void buildGraph(String filePath) throws IOException {
        List<String> words = readAndProcessFile(filePath);
        for (int i = 0; i < words.size() - 1; i++) {
            String current = words.get(i);
            String next = words.get(i + 1);
            // 相邻的 word 在图中有一条边，graph 中如果没有 current 的子节点集合，则新建一个
            graph.putIfAbsent(current, new HashMap<>());
            Map<String, Integer> edges = graph.get(current);
            // 权重加 1（两个单词多一次相邻）
            edges.put(next, edges.getOrDefault(next, 0) + 1);
        }
    }

    // 文件处理
    private List<String> readAndProcessFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 把所有的非字母都换成空格，字母则都转为小写，最后将空白符转为空格
                String processed = line.replaceAll("[^a-zA-Z]", " ").toLowerCase().replaceAll("\\s+", " ");
                content.append(processed).append(" ");
            }
        }
        // 将处理完的 words 按照空白符分割，转为数组类型的流，过滤空串，收集为数组
        return Arrays.stream(content.toString().split("\\s+")).filter(word -> !word.isEmpty()).collect(Collectors.toList());
    }

    // 菜单系统
    private void showMenu(Scanner scanner) throws IOException {
        while (true) {
            System.out.println("\n=== Text Graph Processor ===");
            System.out.println("1. Show Graph Structure");
            System.out.println("2. Query Bridge Words");
            System.out.println("3. Generate New Text");
            System.out.println("4. Calculate Shortest Path");
            System.out.println("5. Calculate PageRank");
            System.out.println("6. Random Walk");
            System.out.println("7. Exit");
            System.out.print("Select option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1:
                    System.out.println("1. Text View");
                    System.out.println("2. Graphical View");
                    System.out.println("3. Export to DOT");
                    System.out.print("Select option: ");
                    int subChoice = scanner.nextInt();
                    scanner.nextLine();

                    switch (subChoice) {
                        case 1:
                            displayGraph();
                            break;
                        case 2:
                            visualizeGraph();
                            break;
                        case 3:
                            System.out.print("Enter dot file path: ");
                            String filename = scanner.nextLine();
                            exportToDotFile(filename);
                            break;
                        default:
                            System.out.println("Invalid option!");
                    }
                    break;
                case 2:
                    queryBridgeWords(scanner);
                    break;
                case 3:
                    generateNewText(scanner);
                    break;
                case 4:
                    calculateShortestPath(scanner);
                    break;
                case 5:
                    calculatePageRank();
                    break;
                case 6:
                    performRandomWalk(scanner);
                    break;
                case 7:
                    System.out.println("Exiting...");
                    System.exit(0);
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    // 功能1：显示图结构
    private void displayGraph() {
        System.out.println("\nGraph Structure:");
        graph.forEach((source, edges) -> {
            String connections = edges.entrySet().stream().map(e -> e.getKey() + "(" + e.getValue() + ")").collect(Collectors.joining(", "));
            System.out.println(source + " -> " + connections);
        });
    }

    // 功能2：查询桥接词
    private void queryBridgeWords(Scanner scanner) {
        System.out.print("Enter word1: ");
        String word1 = scanner.nextLine().toLowerCase();
        System.out.print("Enter word2: ");
        String word2 = scanner.nextLine().toLowerCase();

        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            System.out.println("No " + word1 + " or " + word2 + " in the graph!");
            return;
        }

        List<String> bridges = graph.get(word1).keySet().stream().filter(bridge -> graph.containsKey(bridge) && graph.get(bridge).containsKey(word2)).collect(Collectors.toList());

        if (bridges.isEmpty()) {
            System.out.println("No bridge words from " + word1 + " to " + word2 + "!");
        } else {
            String result = bridges.stream().collect(Collectors.joining(", ", "The bridge words from " + word1 + " to " + word2 + " are: ", "."));
            System.out.println(result);
        }
    }

    // 功能3：生成新文本
    private void generateNewText(Scanner scanner) {
        System.out.print("Enter new text: ");
        String[] inputWords = scanner.nextLine().toLowerCase().split("\\s+");
        List<String> output = new ArrayList<>();

        for (int i = 0; i < inputWords.length - 1; i++) {
            output.add(inputWords[i]);
            List<String> bridges = findBridgeWords(inputWords[i], inputWords[i + 1]);
            if (!bridges.isEmpty()) {
                output.add(bridges.get(random.nextInt(bridges.size())));
            }
        }
        output.add(inputWords[inputWords.length - 1]);

        System.out.println("Generated text: " + String.join(" ", output));
    }

    private List<String> findBridgeWords(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) return Collections.emptyList();
        return graph.get(word1).keySet().stream().filter(bridge -> graph.get(bridge).containsKey(word2)).collect(Collectors.toList());
    }

    // 功能4：计算最短路径（Dijkstra算法）
    private void calculateShortestPath(Scanner scanner) {
        System.out.print("Enter start word: ");
        String start = scanner.nextLine().toLowerCase();
        System.out.print("Enter end word: ");
        String end = scanner.nextLine().toLowerCase();

        if (!graph.containsKey(start) || !graph.containsKey(end)) {
            System.out.println("Words not in graph!");
            return;
        }

        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingInt(n -> distances.get(n)));

        graph.keySet().forEach(node -> distances.put(node, Integer.MAX_VALUE));
        distances.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(end)) break;

            graph.getOrDefault(current, Collections.emptyMap()).forEach((neighbor, weight) -> {
                int newDist = distances.get(current) + weight;
                if (newDist < distances.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    distances.put(neighbor, newDist);
                    predecessors.put(neighbor, current);
                    if (queue.contains(neighbor)) queue.remove(neighbor);
                    queue.add(neighbor);
                }
            });
        }

        if (distances.get(end) == Integer.MAX_VALUE) {
            System.out.println("No path exists!");
            return;
        }

        LinkedList<String> path = new LinkedList<>();
        for (String node = end; node != null; node = predecessors.get(node)) {
            path.addFirst(node);
        }

        System.out.println("Shortest path: " + String.join(" → ", path));
        System.out.println("Path length: " + distances.get(end));
    }

    // 功能5：PageRank计算
    private void calculatePageRank() {
        // 使用 final 修饰符确保引用不变
        final Map<String, Double> initialPr = new HashMap<>();
        final double initialValue = 1.0 / graph.size();

        // 在 Lambda 中使用 final 变量
        graph.keySet().forEach(node -> initialPr.put(node, initialValue));
        // 创建可修改的副本
        Map<String, Double> currentPr = new HashMap<>(initialPr);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            // 创建当前迭代的PR值快照
            final Map<String, Double> iterationPr = new HashMap<>(currentPr);

            // 计算悬挂节点贡献
            final double danglingSum = graph.entrySet().stream().filter(e -> e.getValue().isEmpty()).mapToDouble(e -> iterationPr.get(e.getKey())).sum();

            // 计算分发值（声明为final）
            final double distribute = DAMPING_FACTOR * danglingSum / graph.size();

            // 临时存储新PR值
            Map<String, Double> newPr = new HashMap<>();

            // 并行计算每个节点的PR值
            graph.keySet().parallelStream().forEach(node -> {
                // 计算来自其他节点的贡献
                double incomingSum = graph.entrySet().parallelStream().filter(e -> e.getValue().containsKey(node)).mapToDouble(e -> {
                    String source = e.getKey();
                    int outDegree = e.getValue().values().stream().mapToInt(Integer::intValue).sum();
                    return iterationPr.get(source) * e.getValue().get(node) / outDegree;
                }).sum();

                // 计算新PR值
                double prValue = (1 - DAMPING_FACTOR) / graph.size() + DAMPING_FACTOR * incomingSum + distribute;
                synchronized (newPr) {
                    newPr.put(node, prValue);
                }
            });

            // 检查收敛
            boolean converged = true;
            for (String node : graph.keySet()) {
                double diff = Math.abs(newPr.get(node) - currentPr.get(node));
                if (diff > CONVERGENCE_THRESHOLD) {
                    converged = false;
                    break;
                }
            }

            currentPr = newPr;
            if (converged) break;
        }

        // 输出结果（保持原有格式）
        System.out.println("\nPageRank Values:");
        currentPr.entrySet().stream().sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue())).forEach(e -> System.out.printf("%s: %.4f%n", e.getKey(), e.getValue()));
    }

    // 功能6：随机游走
    private void performRandomWalk(Scanner scanner) throws IOException {
        List<String> path = new ArrayList<>();
        Set<String> visitedEdges = new HashSet<>();
        List<String> nodes = new ArrayList<>(graph.keySet());
        String current = nodes.get(random.nextInt(nodes.size()));

        path.add(current);
        System.out.println("Starting random walk from: " + current);

        while (true) {
            Map<String, Integer> edges = graph.get(current);
            if (edges.isEmpty()) break;

            List<String> candidates = new ArrayList<>();
            edges.forEach((k, v) -> {
                for (int i = 0; i < v; i++) candidates.add(k);
            });

            String next = candidates.get(random.nextInt(candidates.size()));
            String edge = current + "->" + next;

            if (visitedEdges.contains(edge)) break;
            visitedEdges.add(edge);

            path.add(next);
            current = next;

            // 检查用户输入
            if (System.in.available() > 0) {
                scanner.nextLine();
                System.out.println("User stopped the walk");
                break;
            }
        }

        String result = String.join(" ", path);
        try (PrintWriter writer = new PrintWriter("random_walk.txt", "UTF-8")) {
            writer.println(result);
        }
        System.out.println("Walk saved to random_walk.txt\nResult: " + result);
    }

    private void visualizeGraph() {
        // 1. 创建图对象
        Graph streamGraph = new SingleGraph("Text Graph");
        streamGraph.setAttribute("ui.stylesheet", "node { fill-color: #A0D8EF; size: 20px; text-size: 14; }" + "edge { fill-color: #666; text-size: 12; }");

        // 2. 收集所有节点（包括源节点和目标节点）
        Set<String> allNodes = new HashSet<>();
        graph.forEach((source, edges) -> {
            allNodes.add(source);
            allNodes.addAll(edges.keySet()); // 添加所有目标节点
        });

        // 3. 添加所有节点
        allNodes.forEach(node -> {
            if (streamGraph.getNode(node) == null) {
                streamGraph.addNode(node).setAttribute("ui.label", node);
            }
        });

        // 4. 添加带权重的边
        AtomicInteger edgeId = new AtomicInteger(0);
        graph.forEach((source, edges) -> {
            edges.forEach((target, weight) -> {
                String edge = "E" + edgeId.getAndIncrement();
                streamGraph.addEdge(edge, source, target).setAttribute("ui.label", weight);
            });
        });

        // 5. 自动布局并显示
        Viewer viewer = streamGraph.display();
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
    }

    private void exportToDotFile(String filename) {
        try (PrintWriter writer = new PrintWriter(filename)) {
            writer.println("digraph G {");
            writer.println("  node [shape=circle, style=filled, fillcolor=\"#A0D8EF\"];");

            // 定义所有节点
            graph.keySet().forEach(node -> writer.println("  \"" + node + "\";"));

            // 添加带权重的边
            graph.forEach((source, edges) -> {
                edges.forEach((target, weight) -> {
                    writer.printf("  \"%s\" -> \"%s\" [label=\"%d\"];%n", source, target, weight);
                });
            });

            writer.println("}");
            System.out.println("DOT file saved to " + filename);
        } catch (FileNotFoundException e) {
            System.err.println("Error exporting DOT file");
        }
    }
}
