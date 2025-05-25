package com.ty.lab1;

import java.io.IOException;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TextGraphProcessorTest {
  private static final String path = "input/Easy Test.txt";

  private void initGraph() throws IOException {
    TextGraphProcessor.buildGraph(path);
  }

  @Before
  public void setup() throws Exception {
    initGraph();
  }

  @Test
  public void testReadAndProcessFile() throws IOException {
    List<String> words = com.ty.lab1.TextGraphProcessor.readAndProcessFile(path);
    List<String> answer =
        List.of("the", "scientist", "carefully", "analyzed", "the", "data", "wrote", "a",
            "detailed", "report", "and", "shared", "the", "report", "with", "the", "team", "but",
            "the", "team", "requested", "more", "data", "so", "the", "scientist", "analyzed", "it",
            "again", "and", "cause", "the", "result");
    Assert.assertEquals(answer, words);
  }

  @Test
  public void testQueryBridgeWords1() {
    List<String> ret = TextGraphProcessor.findBridgeWords("team", "more");
    List<String> answer = List.of("requested");
    Assert.assertEquals(answer, ret);
  }

  @Test
  public void testQueryBridgeWords2() {
    List<String> ret = TextGraphProcessor.findBridgeWords("so", "that");
    List<String> answer = List.of();
    Assert.assertEquals(answer, ret);
  }

  @Test
  public void testQueryBridgeWords3() {
    List<String> ret = TextGraphProcessor.findBridgeWords("the", "carefully");
    List<String> answer = List.of("scientist");
    Assert.assertEquals(answer, ret);
  }

  @Test
  public void testQueryBridgeWords4() {
    List<String> ret = TextGraphProcessor.findBridgeWords("and", "the");
    List<String> answer = List.of("shared", "cause");
    Assert.assertEquals(answer, ret);
  }

  @Test
  public void testQueryBridgeWords5() {
    List<String> ret = TextGraphProcessor.findBridgeWords("the", "and");
    List<String> answer = List.of("report");
    Assert.assertEquals(answer, ret);
  }

  @Test
  public void testQueryBridgeWords6() {
    List<String> ret = TextGraphProcessor.findBridgeWords("asd", "more");
    List<String> answer = List.of();
    Assert.assertEquals(answer, ret);
  }


}