import java.util.Hashtable;
import java.util.Scanner;

public class Process {
    Hashtable<String,Object> pcb;
    OperatingSystem os;

    public Process(int pid,int minBoundary,int maxBoundary ,OperatingSystem os){
        pcb = new Hashtable();
        pcb.put("Minimum Boundary : ",minBoundary);
        pcb.put("Maximum Boundary : ",maxBoundary);
        pcb.put("Process ID : ", pid);
        pcb.put("Program Counter : ",minBoundary+8);
        pcb.put("Process state : ", ProcessState.ready);
        this.os = os;
    }

    public void assign(String variable , String value){
        int newVal = 0;
        int minBoundary = (int) pcb.get("Minimum Boundary : ");
        int maxBoundary = (int) pcb.get("Maximum Boundary : ");
        if(value.equals("input")) {
            System.out.println("Please enter a value : ");
            Scanner sc = new Scanner(System.in);
            value = sc.nextLine();
        }
        if (OperatingSystem.isNumbers(value))
            newVal = Integer.parseInt(value);
        for (int i = minBoundary; i<maxBoundary; i++) {
            if (os.getMemory()[i]!=null && os.getMemory()[i].getVariable().equals(variable))
                if (OperatingSystem.isNumbers(value))
                    os.getMemory()[i].setData(newVal);
                else
                    os.getMemory()[i].setData(value);
        }


    }
}
