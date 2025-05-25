package com.ty.lab1;

import static org.junit.Assert.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class TextGraphProcessorTestWhite {


  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

  @Before
  public void initGraph() {
    // 初始化固定测试图
    TextGraphProcessor.graph.clear();// 基础路径 start -> middle -> end
    TextGraphProcessor.graph.put("start", new HashMap<>(Map.of("middle", 2)));
    TextGraphProcessor.graph.put("middle", new HashMap<>(Map.of("end", 3)));

    // 多路径选择 A -> B 或 A -> C -> B
    TextGraphProcessor.graph.put("a", new HashMap<>(Map.of("b", 5, "c", 2)));
    TextGraphProcessor.graph.put("c", new HashMap<>(Map.of("b", 1)));

    // 孤立节点
    TextGraphProcessor.graph.put("data", new HashMap<>(Map.of("scientist", 1)));
    TextGraphProcessor.graph.put("lonely", new HashMap<>()); // 无出边
  }


  // ------------------- 基本路径测试 -------------------

  /**
   * 路径1：起点不在图中
   */
  @Test
  public void testPath1_StartNotInGraph() {
    // 空图
    simulateInputAndRun("unknown\ndata\n");
    assertOutputContains("\"unknown\" not in graph!");
  }

  /**
   * 路径2：终点不在图中
   */
  @Test
  public void testPath2_EndNotInGraph() {
    simulateInputAndRun("scientist\nunknown\n");
    assertOutputContains("\"unknown\" not in graph!");
  }

  /**
   * 路径3：起点到终点无路径
   */
  @Test
  public void testPath3_NoPathExists() {
    simulateInputAndRun("start\nlonely\n");
    assertOutputContains("No path exists!");
  }

  /**
   * 路径4：单一路径存在
   */
  @Test
  public void testPath4_SinglePath() {
    simulateInputAndRun("start\nend\n");
    assertOutputContains("start → middle → end");
    assertOutputContains("Path length: 5"); // 假设权重2+3=5
  }

  /**
   * 路径5：存在多条路径选择最优
   */
  @Test
  public void testPath5_MultiplePaths() {
    // 构建图：A->B(5) 和 A->C->B(2+1=3)
    simulateInputAndRun("a\nb\n");
    assertOutputContains("a → c → b");
    assertOutputContains("Path length: 3");
  }

  /**
   * 路径6：节点到自身
   */
  @Test
  public void testPath6_SameNode() {
    simulateInputAndRun("data\ndata\n");
    assertOutputContains("data → data");
    assertOutputContains("Path length: 0");
  }

  // ------------------- 辅助方法 -------------------

  private void simulateInputAndRun(String input) {
    System.setOut(new PrintStream(outContent)); // 捕获输出
    TextGraphProcessor.calculateShortestPath(new Scanner(input));
    System.setOut(System.out); // 恢复输出流
  }

  private void assertOutputContains(String expected) {
    assertTrue("Output should contain: " + expected,
        outContent.toString().contains(expected));
  }

  @After
  public void reset() {
    TextGraphProcessor.graph.clear();
    outContent.reset();
  }
}