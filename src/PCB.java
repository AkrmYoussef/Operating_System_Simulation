public class PCB {
   int processID;
   ProcessState processState;
   int pc;                       //program counter
   int minMemory;
   int maxMemory;

   public PCB(int pid){
      processState = ProcessState.initialized;
      processID = pid;
   }

}
