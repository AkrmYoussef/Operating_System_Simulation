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

        for (int i = 0; i < instructions.length; i++) {
            switch (instructions[i]) {
                case "semWait":
                    this.semWait(instructions[i++]);
                    i++;
                    break;
                case "semSignal":
                    this.semSignal(instructions[i++]);
                    i++;
                    break;
                case "assign":
                    this.assign(instructions[i + 1], instructions[i + 2]);
                    i = i + 2;
                    break;
                case "printFromTo":
                    int x = (int) this.getData(instructions[i + 1]);
                    int y = (int) this.getData(instructions[i + 2]);
                    i = i + 2;
                    printFromTo(x, y);
                    break;
                case "print":
                    String dataToPrint = (String) this.getData(instructions[i + 1]);
                    i++;
                    this.os.print(dataToPrint);
                    break;
                case "readFile" :
                    String dataToRead = (String) this.getData(instructions[i + 1]);
                    i++;
                    this.os.readFile(dataToRead);
                    break;
                case "writeFile" :
                    String fileName = (String) this.getData(instructions[i + 1]);
                    String data = (String) this.getData(instructions[i + 2]);
                    i= i+2;
                    this.os.writeFile(fileName,data);
                    break;
                default:
                    System.out.println("This instruction is not supported");

            }
        }
        this.pcb.replace("Program Counter : ", pc++);

    }

    public Object getData(String input) {
        for (int j = (int) this.pcb.get("Minimum Boundary : ") + 5; j < (int) this.pcb.get("Minimum Boundary : ") + 8; j++) {
            if (input.equals(os.getMemory()[j].getVariable()))
                return os.getMemory()[j].getData();
        }
        return null;
    }

    public void assign(String variable, String value) {
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
        for (int i = minBoundary; i < maxBoundary; i++) {
            if (os.readFromMemory(i) != null && os.readFromMemory(i).getVariable().equals(variable))
                if (OperatingSystem.isNumbers(value))
                    os.readFromMemory(i).setData(newVal);
                else
                    os.readFromMemory(i).setData(value);
        }


    }

    public void semWait(String resource) {
        if (resource.equals("userOutput")) {
            if (os.mutexR3 == 1) {
                os.mutexR3 = 0;
                System.out.println("Process " + this.pcb.get("Process ID : ") + "wants to acquire Outputting on the screen resource ");

            } else {
                os.blockedForOutput.add(this);
                os.blockedQ.add(this);
                this.pcb.replace("Process state : ", ProcessState.blocked);
            }
        } else if (resource.equals("userInput")) {
            if (os.mutexR2 == 1) {
                os.mutexR2 = 0;
                System.out.println("Process " + this.pcb.get("Process ID : ") + " wants to acquire Taking user input resource ");
            } else {
                os.blockedForScanner.add(this);
                os.blockedQ.add(this);
                this.pcb.replace("Process state : ", ProcessState.blocked);
            }
        } else {
            if (os.mutexR1 == 1) {
                os.mutexR1 = 0;
                System.out.println("Process " + this.pcb.get("Process ID : ") + " wants to acquire Accessing a file, to read or to write resource ");
            } else {
                os.blockedForAccessing.add(this);
                os.blockedQ.add(this);
                this.pcb.replace("Process state : ", ProcessState.blocked);
            }

        }
    }

    public void semSignal(String resource) {
        if (resource.equals("userOutput")) {
            if (os.blockedForOutput.isEmpty()) {
                os.mutexR3 = 1;
                System.out.println("Process " + this.pcb.get("Process ID : ") + " has finished Outputting on the screen resource ");
                this.pcb.replace("Process state : ", ProcessState.ready);
            } else {
                Process p = os.blockedForOutput.poll();
                os.readyQ.add(p);

                while (os.blockedQ.peek() != p)
                    os.blockedQ.add(os.blockedQ.poll());
                os.blockedQ.poll();


            }
        } else if (resource.equals("userInput")) {
            if (os.blockedForScanner.isEmpty()) {
                os.mutexR2 = 1;
                System.out.println("Process " + this.pcb.get("Process ID : ") + " has finished Taking user input resource ");
                this.pcb.replace("Process state : ", ProcessState.ready);
            } else {
                Process p = os.blockedForScanner.poll();
                os.readyQ.add(p);

                while (os.blockedQ.peek() != p)
                    os.blockedQ.add(os.blockedQ.poll());
                os.blockedQ.poll();
            }
        } else {
            if (os.blockedForAccessing.isEmpty()) {
                os.mutexR1 = 1;
                System.out.println("Process " + this.pcb.get("Process ID : ") + " has finished Accessing a file, to read or to write resource ");
                this.pcb.replace("Process state : ", ProcessState.ready);
            } else {
                Process p = os.blockedForAccessing.poll();
                os.readyQ.add(p);

                while (os.blockedQ.peek() != p)
                    os.blockedQ.add(os.blockedQ.poll());
                os.blockedQ.poll();

            }

        }
    }

    public static void printFromTo(int x, int y) {
        System.out.print("Numbers from " + x + "to" + y + "are : ");
        for (int i = x + 1; i < y; i++)
            System.out.print(i + (((i == y - 1) ? "" : ",")));

    }


}