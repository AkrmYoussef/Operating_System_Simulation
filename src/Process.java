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
        pcb.put("Program Counter : ", minBoundary + 8);
        pcb.put("Process state : ", ProcessState.ready);
        this.os = os;
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
            if (os.getMemory()[i] != null && os.getMemory()[i].getVariable().equals(variable))
                if (OperatingSystem.isNumbers(value))
                    os.getMemory()[i].setData(newVal);
                else
                    os.getMemory()[i].setData(value);
        }


    }

    public void semWait(String resource) {
        if (resource.equals("userOutput")) {
            if (os.mutexR3 == 1) {
                os.mutexR3 = 0;
                System.out.println("Process " + this.pcb.get("Process ID : ") + "wants to acquire Outputting on the screen resource ");
                this.pcb.replace("Process state : ",ProcessState.running);
            } else {
                os.blockedForOutput.add(this);
                os.blockedQ.add(this);
                this.pcb.replace("Process state : ",ProcessState.blocked);
            }
        } else if (resource.equals("userInput")) {
            if (os.mutexR2 == 1) {
                os.mutexR2 = 0;
                System.out.println("Process " + this.pcb.get("Process ID : ") + " wants to acquire Taking user input resource ");
                this.pcb.replace("Process state : ",ProcessState.running);
            } else {
                os.blockedForOutput.add(this);
                os.blockedQ.add(this);
                this.pcb.replace("Process state : ",ProcessState.blocked);
            }
        } else {
            if (os.mutexR1 == 1) {
                os.mutexR1 = 0;
                System.out.println("Process " + this.pcb.get("Process ID : ") + " wants to acquire Accessing a file, to read or to write resource ");
                this.pcb.replace("Process state : ",ProcessState.running);
            } else {
                os.blockedForOutput.add(this);
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
                 this.pcb.replace("Process state : ",ProcessState.blocked);
             }
             else{
                 os.readyQ.add(os.blockedForOutput.poll());
                 Process p = os.blockedQ.poll();
                 p.pcb.replace("Process state : ",ProcessState.finished);
             }
        }
        else if(resource.equals("userInput")){
            if(os.blockedForOutput.isEmpty()){
                os.mutexR2 = 1;
                System.out.println("Process " + this.pcb.get("Process ID : ") + " has finished Taking user input resource ");
                this.pcb.replace("Process state : ",ProcessState.finished);
            }
            else{
                os.readyQ.add(os.blockedForOutput.poll());
                Process p = os.blockedQ.poll();
                p.pcb.replace("Process state : ",ProcessState.ready);
            }
        }
        else{
            if(os.blockedForOutput.isEmpty()){
                os.mutexR2 = 1;
                System.out.println("Process " + this.pcb.get("Process ID : ") + " has finished Accessing a file, to read or to write resource ");
                this.pcb.replace("Process state : ",ProcessState.finished);
            }
            else{
                os.readyQ.add(os.blockedForOutput.poll());
                Process p = os.blockedQ.poll();
                p.pcb.replace("Process state : ",ProcessState.ready);
            }

        }
    }
}