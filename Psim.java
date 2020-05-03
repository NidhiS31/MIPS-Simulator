
// On my honor, I have neither given nor received unauthorized aid on this assignment

//Nidhi Sharma
//UFID : 6843-1215

import java.io.*;
import java.util.ArrayList;

public class Psim {

    public static ArrayList<String> INM = new ArrayList<>();
    public static ArrayList<String> INB = new ArrayList<>();
    public static ArrayList<String> AIB = new ArrayList<>();
    public static ArrayList<String> SIB = new ArrayList<>();
    public static ArrayList<String> PRB = new ArrayList<>();
    public static ArrayList<String> ADB = new ArrayList<>();
    public static ArrayList<String> REB = new ArrayList<>();
    public static int[] RGF = new int[16];
    public static int[] DAM = new int[16];
    public static BufferedWriter bufferWriter;
    public static BufferedReader bufferReader;
    public static FileReader fileReader;
    public static FileWriter fileWriter;
    public static File file;
    public static String readLine;
    public static boolean finishFlag;
    public static  int step = 0;


    public static void main(String[] args)throws Exception
    {
        String registerFile = "registers.txt";
        String dataMemoryFile = "datamemory.txt";
        String inputFile = "instructions.txt";
        String outputFile = "simulation.txt";

        //load values of registers in RGF register file
        loadRegisters(registerFile);

        //load data into memory i.e. DAM Buffer
        loadDataMemory(dataMemoryFile);

        //load instructions in INF buffer
        loadInstructions(inputFile);

        file = new File(outputFile);
        fileWriter = new FileWriter(file);
        bufferWriter = new BufferedWriter(fileWriter);

        //A flag to indicate the completion of the program
        finishFlag = finishIndicator();

        outputPrinter(finishFlag);

        while(finishFlag){

            //REB(Result Buffer) to RGF(Register File)
            if(REB.size() != 0){
                String resultBufferLoad = REB.get(0);
                REB.remove(0);
                String[] resultBufferArray = instructionSplit(resultBufferLoad);

                int regIndex = Integer.parseInt(resultBufferArray[0].substring(1));
                RGF[regIndex] = Integer.parseInt(resultBufferArray[1]);
            }

            //PRB(Partial Result Buffer) to REB(Result Buffer)
            if(PRB.size() != 0){
                String parResultBufferLoad = PRB.get(0);
                PRB.remove(0);
                String parResultBufferArray[] = instructionSplit(parResultBufferLoad);

                int firstRegValue = Integer.parseInt(parResultBufferArray[2]);
                int secondRegValue = Integer.parseInt(parResultBufferArray[3]);

                String PRBtoREB = "<" + parResultBufferArray[1] + "," + (firstRegValue * secondRegValue) + ">";
                REB.add(PRBtoREB);
            }

            //AIB(Arithmetic Instruction Buffer) to PRB(Partial Result Buffer)/REB(Result Buffer)
            if(AIB.size() != 0){
                String arithBufferLoad = AIB.get(0);
                AIB.remove(0);
                String[] arithBufferArray = instructionSplit(arithBufferLoad);

                int firstRegValue = Integer.parseInt(arithBufferArray[2]);
                int secondRegValue = Integer.parseInt(arithBufferArray[3]);
                int resultValue = 0;

                if(arithBufferArray[0].equals("MUL"))
                    PRB.add(arithBufferLoad);
                else{
                    if(arithBufferArray[0].equals("SUB"))
                        resultValue = firstRegValue - secondRegValue;
                    else if(arithBufferArray[0].equals("ADD"))
                        resultValue = firstRegValue + secondRegValue;

                    String AIBtoREB = "<" + arithBufferArray[1] + "," + resultValue + ">";
                    REB.add(AIBtoREB);
                }
            }

            //ADB(Address Buffer) to DAM(Data Memory)
            if(ADB.size() != 0){
                String addressBufferLoad = ADB.get(0);
                ADB.remove(0);
                String[] addressBufferArray = instructionSplit(addressBufferLoad);
                int regIndex = Integer.parseInt(addressBufferArray[0].substring(1));
                int memIndex = Integer.parseInt(addressBufferArray[1]);
                DAM[memIndex] = RGF[regIndex];
            }

            //SIB(Store Instruction Buffer) to ADB(Address Buffer)
            if(SIB.size() != 0){
                String storeBufferLoad = SIB.get(0);
                SIB.remove(0);
                String[] storeBufferArray = instructionSplit(storeBufferLoad);

                int firstRegValue = Integer.parseInt(storeBufferArray[2]);
                int secondRegValue = Integer.parseInt(storeBufferArray[3]);

                String SIBtoADB = "<" + storeBufferArray[1] + "," + (firstRegValue + secondRegValue) + ">";
                ADB.add(SIBtoADB);
            }

            //INB(Instruction Buffer) to AIB(Arithmetic Instruction Buffer)/SIB(Store Instruction Buffer)
            if(INB.size() != 0){
                String insBufferLoad = INB.get(0);
                INB.remove(0);
                String[] insBufferArray = instructionSplit(insBufferLoad);

                //Send instruction to either AIB or SIB based on the operation to be performed
                if(insBufferArray[0].equals("ST"))
                    SIB.add(insBufferLoad);
                else
                    AIB.add(insBufferLoad);
            }

            //INM(Instruction Memory) to INB(Instruction buffer)
            if(INM.size() > 0){
                String instructionLoad = INM.get(0);
                boolean register1Indicator = false;
                boolean register2Indicator = false;
                int register1Value = Integer.MIN_VALUE;
                int register2Value = Integer.MIN_VALUE;
                String[] instructionArray = instructionSplit(instructionLoad);

                //Replace first register with its value from RGF
                if(instructionArray[2].contains("R")){
                    register1Indicator = true;
                    int regIndex = Integer.parseInt(instructionArray[2].substring(1));
                    register1Value = RGF[regIndex];
                }

                //Replace second register with its value from RGF
                if(instructionArray[3].contains("R")){
                    register2Indicator = true;
                    int regIndex = Integer.parseInt(instructionArray[3].substring(1));
                    register2Value = RGF[regIndex];
                }

                String reg1String;
                if(register1Indicator)
                    reg1String = String.valueOf(register1Value);
                else
                    reg1String = instructionArray[2];

                String reg2String;
                if(register2Indicator)
                    reg2String = String.valueOf(register2Value);
                else
                    reg2String = instructionArray[3];

                //Convert the instruction from INM to INB format by replacing registers with their values
                String INMtoINB = "<" + instructionArray[0] + "," + instructionArray[1] + "," + reg1String + "," + reg2String + ">";

                INB.add(INMtoINB);
//                System.out.println(INB.get(INB.size() - 1));
                INM.remove(0);
            }

            finishFlag = finishIndicator();
            outputPrinter(finishFlag);
        }
        bufferWriter.close();
    }

    //Load data from registers.txt
    private static void loadRegisters(String registerFile) throws IOException {
        file = new File(registerFile);
        fileReader = new FileReader(file);
        bufferReader = new BufferedReader(fileReader);

//      Initialize the registers with initial values
        for( int i = 0; i<16 ; i++)
            RGF[i] = Integer.MIN_VALUE;

        readLine = bufferReader.readLine();

//      Load the registers with the values according to registers data file
        while(readLine != null){
            String[] regString = readLine.substring(1, readLine.length() - 1).split(",");
            int regIndex = Integer.parseInt(regString[0].replaceAll("R", ""));
            RGF[regIndex] = Integer.parseInt(regString[1]);
            readLine = bufferReader.readLine();
        }
    }

    //Load data from datamemory.txt
    private static void loadDataMemory(String dataMemoryFile) throws IOException {
        file = new File(dataMemoryFile);
        fileReader = new FileReader(file);
        bufferReader = new BufferedReader(fileReader);

//        Initialize the memory with initial data
        for(int i = 0; i < 16; i++)
            DAM[i] = Integer.MIN_VALUE;

        readLine = bufferReader.readLine();

//      Load the memory with the values according to data memory file
        while(readLine != null){
            String[] dataString = readLine.substring(1, readLine.length() - 1).split(",");
            int regIndex = Integer.parseInt(dataString[0]);
            DAM[regIndex] = Integer.parseInt(dataString[1]);
            readLine = bufferReader.readLine();
        }
    }

    //Load instructions from instructions.txt
    private static void loadInstructions(String inputFile) throws IOException {
        file = new File(inputFile);
        fileReader = new FileReader(file);
        bufferReader = new BufferedReader(fileReader);

        readLine = bufferReader.readLine();

        while(readLine != null){
            INM.add(readLine);
            readLine = bufferReader.readLine();
        }
    }

    private static boolean finishIndicator() {
        finishFlag = (ADB.size() > 0) || (AIB.size() > 0) || (INB.size() > 0) || (INM.size() > 0) || (PRB.size() > 0) || (REB.size() > 0) || (SIB.size() > 0);
        return finishFlag;
    }

    private static String[] instructionSplit(String instructionLoad){
        String[] instructionArray = instructionLoad.substring(1, instructionLoad.length() - 1).split(",");
        return instructionArray;
    }

    private static void outputPrinter(boolean finishFlag) throws IOException {
        bufferWriter.write("STEP " + step + ":");
        bufferWriter.newLine();

//      Writing data of buffer INM
        bufferPrinter("INM", INM);

//      Writing data of buffer INB
        bufferPrinter("INB", INB);

//      Writing data of buffer AIB
        bufferPrinter("AIB", AIB);

//      Writing data of buffer SIB
        bufferPrinter("SIB", SIB);

//      Writing data of buffer PRB
        bufferPrinter("PRB", PRB);

//      Writing data of buffer ADB
        bufferPrinter("ADB", ADB);

//      Writing data of buffer REB
        bufferPrinter("REB", REB);

        //Writing registers data to output file
        String registerBuffer = "RGF:";
        String regData ="";
        for(int regIndex = 0; regIndex < RGF.length; regIndex++){
            if(RGF[regIndex] != Integer.MIN_VALUE)
                regData += "<R" + regIndex + "," + RGF[regIndex] + ">,";
        }

        if(regData.length() > 0)
            registerBuffer = registerBuffer + regData.substring(0, regData.length() - 1);

        bufferWriter.write(registerBuffer);
        bufferWriter.newLine();

        //Writing Memory Data to Output File
        String dataMemoryBuffer = "DAM:";
        String memData = "";

        for(int memIndex = 0; memIndex < DAM.length; memIndex++){
            if(DAM[memIndex] != Integer.MIN_VALUE)
                memData += "<" + memIndex + "," + DAM[memIndex] + ">,";
        }

        if(regData.length() > 0)
            dataMemoryBuffer = dataMemoryBuffer + memData.substring(0, memData.length() - 1);

        bufferWriter.write(dataMemoryBuffer);
        bufferWriter.newLine();

        //Increment Step count after printing everything
        step += 1;
        if(finishFlag)
            bufferWriter.newLine();
    }

    private static void bufferPrinter(String bufferName, ArrayList<String> buffer) throws IOException {
        String bufferString = bufferName + ":";
        String bufferData = "";
        for(int index = 0; index < buffer.size(); index++)
            bufferData += buffer.get(index) + ",";
        if(bufferData.length() > 0)
            bufferString = bufferString + bufferData.substring(0, bufferData.length() - 1);

        bufferWriter.write(bufferString);
        bufferWriter.newLine();

    }

}
