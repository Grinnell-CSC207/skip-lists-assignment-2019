
  import java.io.PrintWriter;

  public class SkipListExperiment {

    public static void main(String[] args) {
      PrintWriter pen = new PrintWriter(System.out, true);
      SkipList<String, String> list = new SkipList<String, String>();

      String[] strings =
          {"foxtrot", "alpha", "zulu", "ant", "bravo", "zoo", "aardvark",
              "computer", "science", "skip", "list", "binary", "search", "tree"};

      for (String str : strings) {
        pen.println("Adding " + str);
        list.set(str, str);
        list.dump(pen);
        pen.println();
      } // for

      for (String str : strings) {
        pen.println("Removing " + str);
        list.remove(str);
        list.dump(pen);
        pen.println();
      } // for
    } // main(String[])
  } // SkipListExperiment


