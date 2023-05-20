import java.sql.Time;
import java.time.LocalTime;
import java.util.Hashtable;
import java.util.Scanner;

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

    public void setProcessState(ProcessState ps){
        pcb.replace("Process State : ",ps);
        os.writeIntoMemory((int)pcb.get("Minimum Boundary : ") + 1, ps);
    }

    public ProcessState getProcessState(){
        return (ProcessState) (os.readFromMemory((int)pcb.get("Minimum Boundary : ") + 1).getData());
    }

    public int getProgramCounter(){
        return (int) (os.readFromMemory((int)pcb.get("Minimum Boundary : ") + 2).getData());
    }

    public LocalTime getLastModified(){
        return (LocalTime) (os.readFromMemory((int)pcb.get("Minimum Boundary : ") + 8).getData());
    }

    public void setLastModified(){
        os.writeIntoMemory((int)pcb.get("Minimum Boundary : ") + 8 , java.time.LocalTime.now() );
    }

    public void executeLine(){
        // *** omar and mohamed's part ***
        // use program counter please
        // need this method to test scheduler - ziad
    };

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
                this.pcb.replace("Process state : ",ProcessState.blocked);
            }
        } else if (resource.equals("userInput")) {
            if (os.mutexR2 == 1) {
                os.mutexR2 = 0;
                System.out.println("Process " + this.pcb.get("Process ID : ") + " wants to acquire Taking user input resource ");
            } else {
                os.blockedForScanner.add(this);
                os.blockedQ.add(this);
                this.pcb.replace("Process state : ",ProcessState.blocked);
            }
        } else {
            if (os.mutexR1 == 1) {
                os.mutexR1 = 0;
                System.out.println("Process " + this.pcb.get("Process ID : ") + " wants to acquire Accessing a file, to read or to write resource ");
            } else {
                os.blockedForAccessing.add(this);
                os.blockedQ.add(this);
                this.pcb.replace("Process state : ",ProcessState.blocked);
            }

        }
    }

    public void semSignal(String resource) {
        if (resource.equals("userOutput")) {
             if(os.blockedForOutput.isEmpty()){
                os.mutexR3 = 1;
                System.out.println("Process " + this.pcb.get("Process ID : ") + " has finished Outputting on the screen resource ");
                this.pcb.replace("Process state : ",ProcessState.ready);
             }
             else{
                 Process p = os.blockedForOutput.poll();
                 os.readyQ.add(p);

                 while (os.blockedQ.peek() != p)
                     os.blockedQ.add(os.blockedQ.poll());
                 os.blockedQ.poll();


                 // *** AKRAM PLEASE READ THIS VVVVVV ***
                 //process that is first in blockedForOutput queue isn't the same as the process that is first in blockedQ
                 //for example if a process 1 is blocked for input and then process 2 is blocked for output
                 //blockedForOutput has process 2 first but blockedQ has process 1 first, right?? - ziad

             }
        }
        else if(resource.equals("userInput")){
            if(os.blockedForScanner.isEmpty()){
                os.mutexR2 = 1;
                System.out.println("Process " + this.pcb.get("Process ID : ") + " has finished Taking user input resource ");
                this.pcb.replace("Process state : ",ProcessState.ready);
            }
            else{
                Process p = os.blockedForScanner.poll();
                os.readyQ.add(p);

                while (os.blockedQ.peek() != p)
                    os.blockedQ.add(os.blockedQ.poll());
                os.blockedQ.poll();
            }
        }
        else{
            if(os.blockedForAccessing.isEmpty()){
                os.mutexR1 = 1;
                System.out.println("Process " + this.pcb.get("Process ID : ") + " has finished Accessing a file, to read or to write resource ");
                this.pcb.replace("Process state : ",ProcessState.ready);
            }
            else{
                Process p = os.blockedForAccessing.poll();
                os.readyQ.add(p);

                while (os.blockedQ.peek() != p)
                    os.blockedQ.add(os.blockedQ.poll());
                os.blockedQ.poll();

            }

        }
    }
}