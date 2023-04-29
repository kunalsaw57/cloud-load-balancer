import java.util.*;
import GeneticAlgorithm.GeneticAlgorithmDatacenterBroker;
import FirstComeFirstServe.FCFSDatacenterBroker;
import Priority.PriorityDatacenterBroker;
import RoundRobin.RoundRobinDatacenterBroker;
import ShortestJobFirst.ShortestJobFirstDatacenterBroker;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.*;

public class LoadBalancer {
    private static List<Vm> createVM(int userId, int numberOfVm) {
        LinkedList<Vm> list = new LinkedList<>();
        long size = 10000; 
        int ram = 512; 
        int mips = 1000;
        long bw = 1000;
        int pesNumber = 1; 
        String vmm = "Xen"; 
        for(int i = 0; i< numberOfVm; i++){
            Vm vm = new Vm(i, userId, mips+(i*10), pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
            list.add(vm);
        }
        return list;
    }

    private static List<Cloudlet> createCloudlet(int userId, int numberOfCloudlet){
        LinkedList<Cloudlet> list = new LinkedList<>();
        long length = 1000;
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();
        for(int i = 0; i < numberOfCloudlet; i++){
            Cloudlet cloudlet = new Cloudlet(i, (length + 2L * i * 10), pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(userId);
            list.add(cloudlet);
        }

        return list;
    }

    public static void main(String[] args) {
        Log.printLine();
        Log.printLine("===================================== Load Balancer ==================================");
        Log.printLine("Title:        LoadBalancer" +
                "\nDescription:  A simulation to identify different approaches in Load Balancing of Cloud Computing" +
                "\n Devlopers:   Kunal Pathak" );
        try {
            Calendar calendar = Calendar.getInstance();

            Scanner scanner = new Scanner(System.in);

            Log.printLine();
            Log.printLine("First step: Initialize the CloudSim package.");
            Log.printLine("Enter number of grid users:");
            int numUsers = scanner.nextInt();
            
            // Initialize the CloudSim library
            CloudSim.init(numUsers, calendar, false);

            Log.printLine();
            Log.printLine("Second step: Create Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation.");
            Log.printLine("Enter number of datacenters:");
            int numberOfDatacenters = scanner.nextInt();

            for (int i = 0; i < numberOfDatacenters; i++) {
                createDatacenter("Datacenter_" + i);
            }

            Log.printLine();
            Log.printLine("Third step: Create Broker");
            Log.printLine("Select method for LoadBalancing:" +
                    "\n1. Round Robin" +
                    "\n2. Shortest Job First" +
                    "\n3. First Come First Serve" +
                    "\n4. Genetic Algorithm"+
                    "\n5. Priority");

            DatacenterBroker broker = null;
            boolean gotBroker = false;

            while(!gotBroker) {
                int option = scanner.nextInt();
                try {
                    switch (option) {
                        case 1 :
                            broker = new RoundRobinDatacenterBroker("Broker");
                            gotBroker = true;
                            break;
                        case 2 :
                            broker = new ShortestJobFirstDatacenterBroker("Broker");
                            gotBroker = true;
                            break;
                        case 3 :
                            broker = new FCFSDatacenterBroker("Broker");
                            gotBroker = true;
                            break;
                        case 4 :
                            broker = new GeneticAlgorithmDatacenterBroker("Broker");
                            gotBroker = true;
                            break;
                        case 5 :
                            broker = new PriorityDatacenterBroker("Broker");
                            gotBroker = true;
                            break;
                        default:
                            Log.printLine("Please, select from [1-] only:");
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            int brokerId = broker.getId();

            Log.printLine();
            Log.printLine("Fourth step: Create VMs");
            Log.printLine("Enter number of vms:");
            int numberOfVm = scanner.nextInt();

            List<Vm> vmList = createVM(brokerId, numberOfVm); 

            Log.printLine();
            Log.printLine("Fifth step: Create Cloudlets");
            Log.printLine("Enter number of cloudlet");
            int numberOfCloudlet = scanner.nextInt();

            List<Cloudlet> cloudletList = createCloudlet(brokerId, numberOfCloudlet);
            Log.printLine("Sending them to broker...");

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);

            Log.printLine();
            Log.printLine("Sixth step: Starts the simulation");
            Log.printLine("Press any key to continue...");
            scanner.next();

            CloudSim.startSimulation();

            Log.printLine();
            Log.printLine("Final step: Print results when simulation is over");
            Log.printLine("Press any key to continue...");
            scanner.next();

            List<Cloudlet> cloudletReceivedList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

            printCloudletList(cloudletReceivedList);

            Log.printLine();
            Log.printLine("Simulation Complete");
            scanner.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    private static Datacenter createDatacenter(String name){
        List<Host> hostList = new ArrayList<>();
        List<Pe> peList1 = new ArrayList<>();
        int mips = 10000;
        peList1.add(new Pe(0, new PeProvisionerSimple(mips + 500))); 
        peList1.add(new Pe(1, new PeProvisionerSimple(mips + 1000)));
        peList1.add(new Pe(2, new PeProvisionerSimple(mips + 1500)));
        peList1.add(new Pe(3, new PeProvisionerSimple(mips + 700)));

        List<Pe> peList2 = new ArrayList<>();
        peList2.add(new Pe(0, new PeProvisionerSimple(mips + 700)));
        peList2.add(new Pe(1, new PeProvisionerSimple(mips + 900)));

        int hostId=0;
        int ram = 1002048; 
        long storage = 1000000; 
        int bw = 10000;

        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList1,
                        new VmSchedulerTimeShared(peList1)
                )
        ); 

        hostId++;

        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList2,
                        new VmSchedulerTimeShared(peList2)
                )
        ); 

        String arch = "x86";  
        String os = "Linux";        
        String vmm = "Xen";
        double time_zone = 10.0;      
        double cost = 3.0;            
        double costPerMem = 0.05;		
        double costPerStorage = 0.1;	
        double costPerBw = 0.1;			
        LinkedList<Storage> storageList = new LinkedList<>();	

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private static void printCloudletList(List<Cloudlet> list) {

        String indent = "    ";
        Log.printLine();
        Log.printLine();
        Log.printLine("========================================== OUTPUT ==========================================");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Datacenter ID" + indent + "VM ID" + indent + " " + "Time" + indent + "Start Time" + indent + "Finish Time");
        double time = 0;

        for (Cloudlet value : list) {
            Log.print(indent + String.format("%02d", value.getCloudletId()) + indent + indent);

            if (value.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");
                time += (  value.getFinishTime() - value.getExecStartTime());

                Log.printLine(indent + indent + String.format("%02d", value.getResourceId()) +
                        indent + indent + indent + String.format("%02d", value.getVmId()) +
                        indent + indent + String.format("%.2f", value.getActualCPUTime()) +
                        indent + indent + String.format("%.2f", value.getExecStartTime()) +
                        indent + indent + indent + String.format("%.2f", value.getFinishTime()));
            }
        }
        double avgTime = time/list.toArray().length;
        Log.printLine("Total CPU Time: " + time);
        Log.printLine("Average CPU Time: " + avgTime);
    }
}