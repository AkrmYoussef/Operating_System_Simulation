import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.time.LocalTime;
import java.util.*;


public class OperatingSystem {
    Queue<Process> readyQ;
    Queue<Process> blockedQ;
    Queue<Process> blockedForAccessing; //1- Accessing a file, to read or to write.
    Queue<Process> blockedForScanner;  //2-Taking user input
    Queue<Process> blockedForOutput;  //3-Outputting on the screen.
    int highestPid;
    private Pair[] memory;
    private ArrayList<Pair> disk;

    //memory constants
    final static int partitionSize = 20;
    final static int memSize = 40;

    public Pair[] getMemory() {
        return memory;
    }

    public void setMemory(Pair[] memory) {
        this.memory = memory;
    }

    //scheduler constants
    private int time = -1;
    private final static int quantumSize = 2;
    private final static int processOneArrivalTime = 0;
    private final static int processTwoArrivalTime = 1;
    private final static int processThreeArrivalTime = 4;

    int mutexR1;
    int mutexR2;
    int mutexR3;

    public OperatingSystem() {
        highestPid = -1;
        memory = new Pair[memSize];
        disk = new ArrayList<>();
        readyQ = new LinkedList<>();
        blockedQ = new LinkedList<>();
        blockedForAccessing = new LinkedList<>();
        blockedForScanner = new LinkedList<>();
        blockedForOutput = new LinkedList<>();
        mutexR1=1;
        mutexR2=1;
        mutexR3=1;
    }

    public String readFile(String fileName) {
       String data ="";
        try {
            File myObj = new File(fileName + ".txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                data = data + line + "\n";
                //System.out.println(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred. This file may not exist");
            e.printStackTrace();
        }
        return data;
    }

    public Process createProcess(String fileName) {
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
            minBoundary = swapInProcess(null);

        maxBoundary = minBoundary + partitionSize - 1;
        processID = ++highestPid;

        memory[minBoundary + 0] = new Pair("processID", processID);
        memory[minBoundary + 1] = new Pair("processState", processState);
        memory[minBoundary + 2] = new Pair("programCounter", minBoundary + 9);
        memory[minBoundary + 3] = new Pair("minBoundary", minBoundary);
        memory[minBoundary + 4] = new Pair("maxBoundary", maxBoundary);
        memory[minBoundary + 5] = new Pair("a", null);
        memory[minBoundary + 6] = new Pair("b", null);
        memory[minBoundary + 7] = new Pair("c", null);
        memory[minBoundary + 8] = new Pair("lastModified", java.time.LocalTime.now());
        Process p = new Process(processID, minBoundary, maxBoundary, this);
        this.readyQ.add(p);

        int instructionCount = 0;
        String data = readFile(fileName);
        String[] arr = data.split("\n");
        for (int i = minBoundary + 9; i < maxBoundary; i++) {
            memory[i] = new Pair("instruction" + instructionCount, arr[instructionCount]);
            instructionCount++;
            if (instructionCount == arr.length)
                break;
        }

        System.out.println("Process with PID " + readFromMemory(minBoundary).getData() + " is created and put into memory");
         return p;
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


    public void print(String file) {
        String [] arr = file.split("\n");
        for (String s : arr) {
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


    public static boolean isNumbers(String str) {

        for (int i = 0; i < str.length(); i++)
            if (!Character.isDigit(str.charAt(i)))
                return false;
        return true;
    }

    public void passTime(){
        time++;
        if(time==processOneArrivalTime)
            createProcess("Program_1");
        if(time==processTwoArrivalTime)
            createProcess("Program_2");
        if(time==processThreeArrivalTime)
            createProcess("Program_3");

        //print memory
        System.out.println(" ");
        System.out.print("[");
        for (Pair p : memory)
            System.out.print(p + ", ");
        System.out.print("]");
        System.out.println(" ");

    }

    public int getTime(){
        return time;
    }

    public void startScheduler(){

        //set time to 0, creating the first process
        passTime();

        //loop until ready queue is empty
        while(!readyQ.isEmpty()) {

            //get first process from ready queue
            Process p = readyQ.poll();
            //set its state to running
            p.setProcessState(ProcessState.running);

            //check if it is in the memory
            boolean processPresentInMemory = false;
            for (int i = 0; i < memSize; i += partitionSize) {
                if (memory[i]!=null && memory[i].getData().equals(p.pcb.get("Process ID : "))) {
                    processPresentInMemory = true;
                }
            }

            //if not, swap it in
            if(!processPresentInMemory)
                swapInProcess(p);

            //run the process for its quantum size
            for(int i = 0; i<quantumSize; i++) {
                if (p.getProcessState() == ProcessState.running) {

                    p.executeLine();
                    p.setLastModified();
                    passTime();

                    //check if program counter is on a null value or outside of the process boundary
                    //if it is, consider the process finished and remove it from memory
                    int minBoundary = (int) p.pcb.get("Minimum Boundary : ");
                    int maxBoundary = (int) p.pcb.get("Maximum Boundary : ");
                    if(p.getProgramCounter()>maxBoundary ||  readFromMemory(p.getProgramCounter())==null) {
                        p.setProcessState(ProcessState.finished);
                        for(int j = minBoundary; j < maxBoundary; j++){
                            memory[j] = null;
                        }
                    }

                } else {
                    break;
                }
            }

            //return process to ready queue
            if(p.getProcessState()==ProcessState.running) {
                p.setProcessState(ProcessState.ready);
                readyQ.add(p);
            }

        }
    }

    public int swapInProcess(Process p){

        //find the least recently modified process, and get its minBoundary
        LocalTime leastRecentModification = null;
        int locationToSwapOut = 0;
        for (int i = 8; i < memSize; i += partitionSize) {
            if (leastRecentModification==null ||  ((LocalTime)memory[i].getData()).compareTo(leastRecentModification) < 0) {
                leastRecentModification = (LocalTime)memory[i].getData();
                locationToSwapOut = i - 8;
            }
        }

        System.out.println("Process with PID " + readFromMemory(locationToSwapOut).getData() + " is swapped out from memory");

        //for each word in the process boundary add it to ArrayList disk and remove it from memory
        for(int i = locationToSwapOut; i < locationToSwapOut + partitionSize; i++){
            disk.add(memory[i]);
            memory[i] = null;
        }

        //find the location (in disk) of the process we want to swap in
        int locationInDisk = -1;
        for(int i = 0; i<disk.size(); i+=partitionSize){
            if(p!=null && (int)disk.get(i).getData()==(int)p.pcb.get("Process ID : ")) {
                locationInDisk = i;
                break;
            }
        }

        //not in disk, this means process is still in the creation process
        if(locationInDisk==-1) {
            return locationToSwapOut;
        }

        //put in the process data from the disk into memory
        for(int i = locationToSwapOut; i < locationToSwapOut + partitionSize; i++){
            memory[i] = disk.remove(locationInDisk);
        }

        //set the new program counter and process boundaries
        int oldProgramCounter = (int) memory[locationToSwapOut + 2].getData();
        int oldMinBoundary = (int) memory[locationToSwapOut + 3].getData();
        memory[locationToSwapOut + 2] = new Pair("programCounter", locationToSwapOut + (oldProgramCounter-oldMinBoundary));
        memory[locationToSwapOut + 3] = new Pair("minBoundary", locationToSwapOut);
        memory[locationToSwapOut + 4] = new Pair("maxBoundary", locationToSwapOut + partitionSize - 1);
        p.pcb.replace("Minimum Boundary : ",locationToSwapOut);
        p.pcb.replace("Maximum Boundary : ",locationToSwapOut + partitionSize - 1);
        p.pcb.replace("Program Counter : ",locationToSwapOut + (oldProgramCounter-oldMinBoundary));

        System.out.println("Process with PID " + readFromMemory(locationToSwapOut).getData() + " is swapped into memory");

        return locationToSwapOut;

    }

    public static void main(String[] args) {
        OperatingSystem os = new OperatingSystem();
        // os.readFile("Program_1");
        //System.out.println(os.toString());
        // os.writeFile("omar&ziad","playing football and ping pong \n eating meat");
        // os.print("Program_1");
        // System.out.println(isNumbers("1242876jbub"));

        //os.readyQ.poll().assign("c", "input");

        //print memory
//     System.out.print("[");
//             for (Pair p : os.memory)
//                  System.out.print(p + ", ");
//        System.out.print("]");

//        printFromTo(3,9);
//        os.startScheduler(); //don't test will run forever!!!
//       String lineOfInstructions = "assign b readFile a";
//        String[] instructions = lineOfInstructions.split(" ");
//        for (int i =0 ;i<instructions.length;i++)
//            System.out.println(instructions[i]);











        /*  The following lines of  code  are to test executeLine() */



        Process po =os.createProcess("Program_3");

        System.out.print("[");
        for (Pair p : os.memory)
            System.out.print(p + ", ");
        System.out.println("]");

        int i = 0;
        while(i<9) {
            po.executeLine();
            i++;
        }


        System.out.print("[");
        for (Pair p : os.memory)
            System.out.print(p + ", ");
        System.out.print("]");

    }
}

