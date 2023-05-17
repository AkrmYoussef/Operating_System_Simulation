import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class OperatingSystem {
    Queue<Process> readyQ;
    Queue<Process> blockedQ;
    Queue<Process> blockedForAccessing; //1- Accessing a file, to read or to write.
    Queue<Process> blockedForScanner;  //2-Taking user input
    Queue<Process> blockedForOutput;  //3-Outputting on the screen.
    int highestPid;
    Pair[] memory;
    final static int partitionSize = 20;
    final static int memSize = 40;
    int mutexR1;
    int mutexR2;
    int mutexR3;
    public OperatingSystem() {
        highestPid = -1;
        memory = new Pair[memSize];
        readyQ = new LinkedList<>();
        blockedQ = new LinkedList<>();
        blockedForAccessing = new LinkedList<>();
        blockedForScanner = new LinkedList<>();
        blockedForOutput = new LinkedList<>();
    }

    public LinkedList<String> readFile(String fileName) {
        LinkedList<String> data = new LinkedList<>();
        try {
            File myObj = new File(fileName + ".txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                data.add(line);
                //System.out.println(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return data;
    }

    public void createProcess(String fileName) {
        int minBoundary = 0;
        int maxBoundary;
        int processID;
        ProcessState processState = ProcessState.ready;
        boolean spaceInMemory = false;
        for (int i = 0; i < memSize; i += partitionSize) {
            if (memory[i] == null) {
                minBoundary = i;
                spaceInMemory = true;
                break;
            }
        }
        if (!spaceInMemory)
            //call scheduler to swap
            ;

        maxBoundary = minBoundary + partitionSize;
        processID = ++highestPid;

        memory[minBoundary + 0] = new Pair("processID", processID);
        memory[minBoundary + 1] = new Pair("processState", processState);
        memory[minBoundary + 2] = new Pair("programCounter", minBoundary + 8);
        memory[minBoundary + 3] = new Pair("minBoundary", minBoundary);
        memory[minBoundary + 4] = new Pair("maxBoundary", maxBoundary);
        memory[minBoundary + 5] = new Pair("a", null);
        memory[minBoundary + 6] = new Pair("b", null);
        memory[minBoundary + 7] = new Pair("c", null);
        Process p = new Process(processID, minBoundary, maxBoundary, this);
        this.readyQ.add(p);

        int instructionCount = 0;
        LinkedList<String> data = readFile(fileName);
        for (int i = minBoundary + 8; i < maxBoundary; i++) {
            memory[i] = new Pair("instruction" + instructionCount, data.get(instructionCount));
            instructionCount++;
            if (instructionCount == data.size())
                break;
        }

    }

    public void writeFile(String filename, String data) {
        try {
            // to write in the file
            FileWriter fWriter = new FileWriter(filename + ".txt");


            fWriter.write(data);

            // Printing the contents of a file
            System.out.println(data);

            // Closing the file writing connection
            fWriter.close();

        } catch (IOException e) {

            // Print the exception
            System.out.print(e.getMessage());
        }

    }


    // print
    public void print(String filename) {
        for (String s : readFile(filename)) {
            System.out.println(s);
        }
    }

    public Pair readFromMemory(int idx) {
        return memory[idx];
    }

    public void writeIntoMemory(int idx, Object dataToWrite) {
        memory[idx].setData(dataToWrite);
    }


    // printFromTo
   public static void printFromTo(int x , int y){
      for(int i= x+1 ; i < y; i++)
          System.out.print(i+(((i==y-1)?"":",")));

   }






      // semWait



    // semSignal

    public static boolean isNumbers(String str) {

        for (int i = 0; i < str.length(); i++)
            if (!Character.isDigit(str.charAt(i)))
                return false;
        return true;
    }

    public Pair[] getMemory() {
        return memory;
    }

    public static void main(String[] args) {
/*        OperatingSystem os = new OperatingSystem();
        // os.readFile("Program_1");
        System.out.println();
        //System.out.println(os.toString());
        // os.writeFile("omar&ziad","playing football and ping pong \n eating meat");
        // os.print("Program_1");
        // System.out.println(isNumbers("1242876jbub"));
        os.createProcess("Program_1");
        os.readyQ.poll().assign("c", "input");

        //print memory
        System.out.print("[");
        for (Pair p : os.memory)
            System.out.print(p + ", ");
        System.out.print("]");*/

        printFromTo(3,9);

    }
}

