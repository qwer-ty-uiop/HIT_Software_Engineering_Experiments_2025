

package com.ty.lab1;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

/**
 * TextGraphProcessor 类用于处理文本生成图结构，并提供多种图操作功能.
 */
public class TextGraphProcessor {
  /* 有向图结构，记录单词间相邻关系及出现次数 */
  public static final Map<String, Map<String, Integer>> graph = new HashMap<>();
  private static final double DAMPING_FACTOR = 0.85;
  private static final int MAX_ITERATIONS = 100;
  private static final double CONVERGENCE_THRESHOLD = 0.0001;
  private static final Random random = new Random();

  /**
   * 主方法，程序入口.
   *
   * @param args 命令行参数
   * @throws IOException 当发生I/O错误时抛出
   */
  public static void main(String[] args) throws IOException {
    // 在程序入口设置UI类型
    System.setProperty("org.graphstream.ui", "swing");
    new TextGraphProcessor().run(args);
  }

  /**
   * 运行处理器，处理用户输入.
   *
   * @param args 命令行参数
   * @throws IOException 当发生I/O错误时抛出
   */
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

  /**
   * 构建图结构.
   *
   * @param filePath 文件路径
   */
  public static void buildGraph(String filePath) throws IOException {
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

  /**
   * 文件处理.
   *
   * @param filePath 文件路径
   * @return 读入文件的单词数组
   */
  public static List<String> readAndProcessFile(String filePath) throws IOException {
    StringBuilder content = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        // 把所有的非字母都换成空格，字母则都转为小写，最后将空白符转为空格
        String processed = line.replaceAll("[^a-zA-Z]", " ").toLowerCase().replaceAll("\\s+", " ");
        content.append(processed).append(" ");
      }
    }
    // 将处理完的 words 按照空白符分割，转为数组类型的流，过滤空串，收集为数组
    return Arrays.stream(content.toString().split("\\s+")).filter(word -> !word.isEmpty())
        .collect(Collectors.toList());
  }

  /**
   * 菜单系统.
   *
   * @param scanner 控制台输入流
   * @throws IOException 输入异常
   */
  public static void showMenu(Scanner scanner) throws IOException {
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
          break;
        default:
          System.out.println("Invalid option!");
          break;
      }
    }
  }

  /**
   * 功能1：显示图结构.
   */
  public static void displayGraph() {
    System.out.println("\nGraph Structure:");
    graph.forEach((source, edges) -> {
      String connections = edges.entrySet().stream().map(e -> e.getKey() + "(" + e.getValue() + ")")
          .collect(Collectors.joining(", "));
      System.out.println(source + " -> " + connections);
    });
  }

  /**
   * 功能2：查询桥接词.
   *
   * @param scanner 控制台输入流
   */
  public static void queryBridgeWords(Scanner scanner) {
    System.out.println("\nQuery Bridge Words From Word1 To Word2");
    System.out.print("Enter word1: ");
    String word1 = scanner.nextLine().toLowerCase();
    System.out.print("Enter word2: ");
    String word2 = scanner.nextLine().toLowerCase();

    if (!graph.containsKey(word1)) {
      System.out.println("No " + word1 + " in the graph!");
      return;
    }
    if (!graph.containsKey(word2)) {
      System.out.println("No " + word2 + " in the graph!");
      return;
    }

    List<String> bridges = graph.get(word1).keySet().stream()
        .filter(bridge -> graph.containsKey(bridge) && graph.get(bridge).containsKey(word2))
        .toList();

    if (bridges.isEmpty()) {
      System.out.println("No bridge words from " + word1 + " to " + word2 + "!");
    } else {
      String result = bridges.stream().collect(
          Collectors.joining(", ", "The bridge words from " + word1 + " to " + word2 + " are: ",
              "."));
      System.out.println(result);
    }
  }

  /**
   * 功能3：生成新文本.
   *
   * @param scanner 控制台输入流
   */
  public static void generateNewText(Scanner scanner) {
    System.out.print("Enter new text: ");
    String[] inputWords = scanner.nextLine().toLowerCase().split("\\s+");
    List<String> output = new ArrayList<>();

    for (int i = 0; i < inputWords.length - 1; i++) {
      output.add(inputWords[i]);
      List<String> bridges = findBridgeWords(inputWords[i], inputWords[i + 1]);
      // 随即添加其中一个连接词
      if (!bridges.isEmpty()) {
        output.add(bridges.get(random.nextInt(bridges.size())));
      }
    }
    output.add(inputWords[inputWords.length - 1]);
    System.out.println("Generated text: " + String.join(" ", output));
  }

  /**
   * 查询桥接词，并返回所有桥接词.
   *
   * @param word1 桥头词1
   * @param word2 桥头词2
   * @return 所有桥接词构成的列表
   */
  public static List<String> findBridgeWords(String word1, String word2) {
    Set<String> allNodes = getAllNodes();
    if (!allNodes.contains(word1) || !allNodes.contains(word2)) {
      return Collections.emptyList();
    }
    return graph.getOrDefault(word1, Map.of()).keySet().stream()
        .filter(bridge -> graph.getOrDefault(bridge, Map.of()).containsKey(word2))
        .collect(Collectors.toList());
  }

  /**
   * 功能4：计算单源最短路径（Dijkstra算法）.
   *
   * @param scanner 控制台输入流
   */
  public static void calculateShortestPath(Scanner scanner) {
    System.out.print("Enter start word: ");
    String start = scanner.nextLine().toLowerCase();
    System.out.print("Enter end word: ");
    String end = scanner.nextLine().toLowerCase();
    Set<String> allNodes = getAllNodes();
    if (!allNodes.contains(start)) {
      System.out.println("\"" + start + "\"" + " not in graph!");
      return;
    }
    if (!allNodes.contains(end)) {
      System.out.println("\"" + end + "\"" + " not in graph!");
      return;
    }
    if (start.equals(end)) {
      System.out.println("Shortest path: " + start + " → " + end);
      System.out.println("Path length: " + 0);
      return;
    }
    Map<String, Integer> distances = new HashMap<>();
    final Map<String, String> predecessors = new HashMap<>();
    PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));

    graph.keySet().forEach(node -> distances.put(node, Integer.MAX_VALUE));
    distances.put(start, 0);
    queue.add(start);

    while (!queue.isEmpty()) {
      String current = queue.poll();
      if (current.equals(end)) {
        break;
      }

      graph.getOrDefault(current, Collections.emptyMap()).forEach((neighbor, weight) -> {
        int newDist = distances.get(current) + weight;
        if (newDist < distances.getOrDefault(neighbor, Integer.MAX_VALUE)) {
          distances.put(neighbor, newDist);
          predecessors.put(neighbor, current);
          queue.remove(neighbor);
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

  /**
   * 功能5：PageRank计算.
   */
  public static void calculatePageRank() {
    // 获取图中所有节点（包括只有入边的节点）
    final Set<String> allNodes = getAllNodes();
    final double initialValue = 1.0 / allNodes.size();

    // 初始化所有节点的PR值
    Map<String, Double> currentPr =
        allNodes.stream().collect(Collectors.toMap(node -> node, _ -> initialValue));

    for (int i = 0; i < MAX_ITERATIONS; i++) {
      // 计算悬挂节点贡献（出边为空的节点）
      final double danglingSum = allNodes.stream()
          .filter(node -> graph.getOrDefault(node, Collections.emptyMap()).isEmpty())
          .mapToDouble(currentPr::get).sum();
      // 此次迭代悬挂节点的均分值
      final double distribute = DAMPING_FACTOR * danglingSum / allNodes.size();

      // 并行计算新PR值
      Map<String, Double> newPr = new ConcurrentHashMap<>();
      Map<String, Double> finalCurrentPr = currentPr;
      allNodes.parallelStream().forEach(node -> {
        // 计算入边贡献（所有指向本节点的边）
        double incomingSum = allNodes.stream()
            .filter(source -> graph.getOrDefault(source, Collections.emptyMap()).containsKey(node))
            .mapToDouble(source -> {
              Map<String, Integer> sourceEdges = graph.get(source);
              int outDegree = sourceEdges.values().stream().mapToInt(Integer::intValue).sum();
              return finalCurrentPr.get(source) * sourceEdges.get(node) / outDegree;
            }).sum();
        double prValue =
            (1 - DAMPING_FACTOR) / allNodes.size() + DAMPING_FACTOR * (incomingSum + distribute);
        newPr.put(node, prValue);
      });

      // 检查收敛
      boolean converged = true;
      for (String node : allNodes) {
        if (Math.abs(newPr.get(node) - currentPr.get(node)) > CONVERGENCE_THRESHOLD) {
          converged = false;
          break;
        }
      }

      currentPr = newPr;
      if (converged) {
        break;
      }
    }

    // 输出结果
    System.out.println("\nPageRank Values:");
    currentPr.entrySet().stream().sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
        .forEach(e -> System.out.printf("%s: %f\n", e.getKey(), e.getValue()));
  }

  /**
   * 获取所有节点（包括只有入边的节点）.
   *
   * @return 返回所有节点
   */
  public static Set<String> getAllNodes() {
    Set<String> nodes = new HashSet<>(graph.keySet());
    graph.values().forEach(edges -> nodes.addAll(edges.keySet()));
    return nodes;
  }

  /**
   * 功能6：随机游走.
   *
   * @param scanner 控制台输入流
   * @throws IOException 输入异常
   */
  public static void performRandomWalk(Scanner scanner) throws IOException {
    List<String> path = new ArrayList<>();
    Set<String> visitedEdges = new HashSet<>();
    List<String> nodes = new ArrayList<>(getAllNodes());
    String current = nodes.get(random.nextInt(nodes.size()));

    path.add(current);
    System.out.println("Starting random walk from: " + current);

    while (true) {
      Map<String, Integer> edges = graph.getOrDefault(current, Collections.emptyMap());
      if (edges.isEmpty()) {
        break;
      }

      List<String> candidates = new ArrayList<>();
      edges.forEach((k, _) -> candidates.add(k));

      String next = candidates.get(random.nextInt(candidates.size()));
      String edge = current + " -> " + next;

      if (visitedEdges.contains(edge)) {
        System.out.println("Edge " + edge + " already visited!");
        break;
      }
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
    try (PrintWriter writer = new PrintWriter("random_walk.txt", StandardCharsets.UTF_8)) {
      writer.println(result);
    }
    System.out.println("Walk saved to random_walk.txt\nResult: " + result);
  }

  /**
   * 可视化图像.
   */
  public static void visualizeGraph() {
    // 1. 创建图对象
    Graph streamGraph = new SingleGraph("Text Graph");
    streamGraph.setAttribute("ui.stylesheet",
        "node { fill-color: #A0D8EF; size: 20px; text-size: 14; }"
            + "edge { fill-color: #666; text-size: 12; }");

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
    graph.forEach((source, edges) -> edges.forEach((target, weight) -> {
      String edge = "E" + edgeId.getAndIncrement();
      streamGraph.addEdge(edge, source, target).setAttribute("ui.label", weight);
    }));

    // 5. 自动布局并显示
    Viewer viewer = streamGraph.display();
    viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
  }

  /**
   * 将图转化为dot文件并保存.
   *
   * @param filename 保存的文件名
   */
  public static void exportToDotFile(String filename) {
    try (PrintWriter writer = new PrintWriter(filename)) {
      writer.println("digraph G {");
      writer.println("  node [shape=circle, style=filled, fillcolor=\"#A0D8EF\"];");

      // 定义所有节点
      graph.keySet().forEach(node -> writer.println("  \"" + node + "\";"));

      // 添加带权重的边
      graph.forEach((source, edges) -> edges.forEach(
          (target, weight) -> writer.printf("  \"%s\" -> \"%s\" [label=\"%d\"];%n", source, target,
              weight)));

      writer.println("}");
      System.out.println("DOT file saved to " + filename);
    } catch (FileNotFoundException e) {
      System.err.println("Error exporting DOT file");
    }
  }
}
