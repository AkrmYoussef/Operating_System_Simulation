import java.security.PublicKey;
import java.sql.Time;
import java.time.LocalTime;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Stack;

public class Process {
    Hashtable<String, Object> pcb;
    OperatingSystem os;

    public Process(int pid, int minBoundary, int maxBoundary, OperatingSystem os) {
        pcb = new Hashtable();
        pcb.put("Minimum Boundary : ", minBoundary);
        pcb.put("Maximum Boundary : ", maxBoundary);
        pcb.put("Process ID : ", pid);
        pcb.put("Program Counter : ", minBoundary + 9);
        pcb.put("Process state : ", ProcessState.ready);
        this.os = os;
    }

    public void setProcessState(ProcessState ps) {
        pcb.replace("Process State : ", ps);
        os.writeIntoMemory((int) pcb.get("Minimum Boundary : ") + 1, ps);
    }

    public ProcessState getProcessState() {
        if(os.readFromMemory((int) pcb.get("Minimum Boundary : ") + 1)==null){
            return ProcessState.finished;
        }
        return (ProcessState) (os.readFromMemory((int) pcb.get("Minimum Boundary : ") + 1).getData());
    }

    public int getProgramCounter() {
        return (int) (os.readFromMemory((int) pcb.get("Minimum Boundary : ") + 2).getData());
    }

    public LocalTime getLastModified() {
        return (LocalTime) (os.readFromMemory((int) pcb.get("Minimum Boundary : ") + 8).getData());
    }

    public void setLastModified() {
        os.writeIntoMemory((int) pcb.get("Minimum Boundary : ") + 8, java.time.LocalTime.now());
    }

    public void executeLine() {
        int pc = (int) this.pcb.get("Program Counter : ");
        String lineOfInstructions = (String) os.getMemory()[pc].getData();
        String[] instructions = lineOfInstructions.split(" ");
        execute(instructions);
        this.pcb.replace("Program Counter : ", pc+1);
        os.writeIntoMemory((int) pcb.get("Minimum Boundary : ") + 2, pc+1);
    }
    public void execute(String[] instructions){
        switch (instructions[0]) {
            case "semWait":
                this.semWait(instructions[1]);
                break;
            case "semSignal":
                this.semSignal(instructions[1]);
                break;
            case "assign":
                if(instructions[2].equals("input"))
                    this.assign(instructions[1], instructions[2]);
                if(instructions[2].equals("readFile")){
                    String dataToRead1 = (String) this.mapVarToData(instructions[3]);
                    this.assign(instructions[1],this.os.readFile(dataToRead1));
                }
                break;
            case "printFromTo":
                int x = (int) this.mapVarToData(instructions[1]);
                int y = (int) this.mapVarToData(instructions[2]);
                printFromTo(x, y);
                break;
            case "print":
                String dataToPrint = (String) this.mapVarToData(instructions[1]);
                this.os.print(dataToPrint);
                break;
            case "readFile" :
                String dataToRead = (String) this.mapVarToData(instructions[1]);
                 this.os.readFile(dataToRead);
                 break;
            case "writeFile" :
                String fileName = (String) this.mapVarToData(instructions[1]);
                String data = (String) this.mapVarToData(instructions[2]);
                this.os.writeFile(fileName,data);
                break;
            default:
                System.out.println(instructions[0] + " instruction is not supported");

        }
    }

    public Object mapVarToData(String var) {
        for (int j = (int) this.pcb.get("Minimum Boundary : ") + 5; j < (int) this.pcb.get("Minimum Boundary : ") + 8; j++) {
            if (var.equals(os.getMemory()[j].getVariable()))
                return os.getMemory()[j].getData();
        }
        return null;
    }

    public void assign(String variable, String value) {
        System.out.println("Process " + this + " is now assigning " + value + " to variable " + variable);
        int newVal = 0;
        int minBoundary = (int) pcb.get("Minimum Boundary : ");
        int maxBoundary = (int) pcb.get("Maximum Boundary : ");
        if (value.equals("input")) {
            System.out.println("Please enter a value : ");
            Scanner sc = new Scanner(System.in);
            value = sc.nextLine();
        }
        if (OperatingSystem.isNumbers(value))
            newVal = Integer.parseInt(value);
        boolean variableFound = false;
        for (int i = minBoundary; i < maxBoundary; i++) {
            if (os.readFromMemory(i) != null && os.readFromMemory(i).getVariable().equals(variable)) {
                if (OperatingSystem.isNumbers(value))
                    os.writeIntoMemory(i,newVal);
                else
                    os.writeIntoMemory(i,value);
                variableFound = true;
            }
        }
        if(!variableFound){
            for (int i = minBoundary; i < maxBoundary; i++) {
                if (os.readFromMemory(i) != null && os.readFromMemory(i).getVariable().equals("a") && os.readFromMemory(i).getData()==null
                    || os.readFromMemory(i) != null && os.readFromMemory(i).getVariable().equals("b") && os.readFromMemory(i).getData()==null
                    || os.readFromMemory(i) != null && os.readFromMemory(i).getVariable().equals("c") && os.readFromMemory(i).getData()==null) {

                    if (OperatingSystem.isNumbers(value))
                        os.writeIntoMemory(i,newVal);
                    else
                        os.writeIntoMemory(i,value);
                    os.readFromMemory(i).setVariable(variable);
                    break;
                }
            }
        }


    }

    public void semWait(String resource) {
        if (resource.equals("userOutput")) {
            if (os.mutexR3 == 1) {
                os.mutexR3 = 0;
                System.out.println("Process " + this.pcb.get("Process ID : ") + " wants to acquire \"Outputting on the screen\" resource ");

            } else {
                os.blockedForOutput.add(this);
                os.blockedQ.add(this);
                this.setProcessState(ProcessState.blocked);

                //print blocked queue
                System.out.println("Process " + this + " is blocked for outputting on the screen.");
                os.printBlockedForOutput();
                os.printBlockedQ();
            }
        } else if (resource.equals("userInput")) {
            if (os.mutexR2 == 1) {
                os.mutexR2 = 0;
                System.out.println("Process " + this.pcb.get("Process ID : ") + " wants to acquire \"Taking user input\" resource ");
            } else {
                os.blockedForScanner.add(this);
                os.blockedQ.add(this);
                this.setProcessState(ProcessState.blocked);

                //print blocked queue
                System.out.println("Process " + this + " is blocked for taking user input.");
                os.printBlockedForScanner();
                os.printBlockedQ();
            }
        } else {
            if (os.mutexR1 == 1) {
                os.mutexR1 = 0;
                System.out.println("Process " + this.pcb.get("Process ID : ") + " wants to acquire \"Accessing a file, to read or to write\" resource ");
            } else {
                os.blockedForAccessing.add(this);
                os.blockedQ.add(this);
                this.setProcessState(ProcessState.blocked);

                //print blocked queue
                System.out.println("Process " + this + " is blocked for accessing a file.");
                os.printBlockedForAccessing();
                os.printBlockedQ();
            }

        }
    }

    public void semSignal(String resource) {
        if (resource.equals("userOutput")) {
            if (os.blockedForOutput.isEmpty()) {
                os.mutexR3 = 1;
                System.out.println("Process " + this.pcb.get("Process ID : ") + " has finished \"Outputting on the screen\" resource ");
                //this.setProcessState(ProcessState.ready);
            } else {
                Process p = os.blockedForOutput.poll();
                os.readyQ.add(p);
                p.setProcessState(ProcessState.ready);

                //print ready queue
                System.out.println("Process " + this.pcb.get("Process ID : ") + " has finished \"Outputting on the screen\" resource " +
                        "and process " + p + " is now ready to run.");
                os.printReadyQ();

                while (os.blockedQ.peek() != p)
                    os.blockedQ.add(os.blockedQ.poll());
                os.blockedQ.poll();


            }
        } else if (resource.equals("userInput")) {
            if (os.blockedForScanner.isEmpty()) {
                os.mutexR2 = 1;
                System.out.println("Process " + this.pcb.get("Process ID : ") + " has finished \"Taking user input\" resource ");
                //this.setProcessState(ProcessState.ready);
            } else {
                Process p = os.blockedForScanner.poll();
                os.readyQ.add(p);
                p.setProcessState(ProcessState.ready);

                //print ready queue
                System.out.println("Process " + this.pcb.get("Process ID : ") + " has finished \"Taking user input\" resource " +
                        "and process " + p + " is now ready to run.");
                os.printReadyQ();

                while (os.blockedQ.peek() != p)
                    os.blockedQ.add(os.blockedQ.poll());
                os.blockedQ.poll();
            }
        } else {
            if (os.blockedForAccessing.isEmpty()) {
                os.mutexR1 = 1;
                System.out.println("Process " + this.pcb.get("Process ID : ") + " has finished \"Accessing a file, to read or to write\" resource ");
                //this.setProcessState(ProcessState.ready);
            } else {
                Process p = os.blockedForAccessing.poll();
                os.readyQ.add(p);
                p.setProcessState(ProcessState.ready);

                //print ready queue
                System.out.println("Process " + this.pcb.get("Process ID : ") + " has finished \"Accessing a file, to read or to write\" resource " +
                        "and process " + p + " is now ready to run.");
                os.printReadyQ();

                while (os.blockedQ.peek() != p)
                    os.blockedQ.add(os.blockedQ.poll());
                os.blockedQ.poll();

            }

        }
    }

    public static void printFromTo(int x, int y) {
        OperatingSystem.print("Numbers from " + x + " to " + y + " are : ");
        for (int i = x + 1; i < y; i++)
            OperatingSystem.print(i + "" /*+ (((i == y - 1) ? "" : ","))*/);
        OperatingSystem.print("");
    }

    public String toString(){
        return this.pcb.get("Process ID : ").toString();
    }


}