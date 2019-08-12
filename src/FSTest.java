/*
 Download this program,
 compile:  javac FSTest.java
 ensure:  $SDC_DATA is set to the actual SDC_DATA directory.
 run: java FSTest

 The program needs to be run as a user who has r/w/x access to the directory
 which is pointed to by the $SDC_DATA environment variable.

 One line of trace is generated for every 10k times through the loop.
 eg:

 10000 open file time: 649ms  rename file time: 542ms  write file time: 28ms  close file time: 853ms  total time: 2073ms
 20000 open file time: 579ms  rename file time: 487ms  write file time: 15ms  close file time: 788ms  total time: 1870ms
 30000 open file time: 563ms  rename file time: 485ms  write file time: 15ms  close file time: 790ms  total time: 1854ms

*/

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FSTest {
  private static final long SCALE_NANOS_TO_MS = 1_000_000;

  private static final String data = "{\"f1\": \"abc\", \"f2\": \"xyz\", \"f3\": \"lmn\" }\n";

  public static void main(String[] args) {

    // check for $SDC_DATA directory:
    String dataDir = System.getenv("SDC_DATA");
    if (dataDir == null || dataDir.equals("")) {
      System.out.println("SDC_DATA is not defined or is blank");
      System.exit(2);
    }

    // build
    String bobPath = dataDir + "/runInfo/bob/0/bob";
    String bobOldPath = bobPath += ".old";
    System.out.println("paths: "+ bobPath + " and " + bobOldPath);
    File bob = new File(bobPath);
    File bobOld = new File(bobOldPath);

    int counter = 0;
    long rename = 0;
    long open = 0;
    long write = 0;
    long close = 0;
    long total = 0;

    // create paths if they don't exist
    bob.mkdirs();
    bobOld.mkdirs();

    // delete these files if they do exist.
    bob.delete();
    bobOld.delete();

    while (true) {     //NOSONAR
      long event0 = System.nanoTime();
      bob.renameTo(bobOld);
      long event1 = System.nanoTime();
      // figure elapsed time for rename.
      rename += event1 - event0;

      try {   // don't use try-with-resources, we want to measure close time, too.
        FileWriter fw = new FileWriter(bob);
        long event3 = System.nanoTime();
        // figure elapsed time for creation.
        open += event3 - event1;

        //write a little (very little) data into the file.
        fw.write(counter + data);
        fw.write(data);
        fw.write(data);

        // figure elapsed time for write.
        long event4 = System.nanoTime();
        write += event4 - event3;

        fw.close();
        // elapsed time for the close:
        long event5 = System.nanoTime();
        close += event5 - event4;
        total += event5 - event0;

      } catch (IOException ex) {
        System.out.println("exception" + ex.getMessage());
        ex.printStackTrace();
        continue;
      }

      ++counter;
      if (counter % 10000 == 0) {
        System.out.println(counter + " open file time: " + open / SCALE_NANOS_TO_MS + "ms  rename file time: " + rename / SCALE_NANOS_TO_MS + "ms  write file time: " + write / SCALE_NANOS_TO_MS + "ms  close file time: " + close / SCALE_NANOS_TO_MS + "ms  total time: " + total / SCALE_NANOS_TO_MS + "ms");
        open = rename = write = close = total = 0;
      }
    }
  }
}