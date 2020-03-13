import collectors.FenixFrameworkCollector;
import collectors.SpoonCollector;
import collectors.SpringDataJPACollector;

import java.util.Scanner;

public class SpoonCallgraph {

    public static void main(String[] args) throws Exception {
        System.out.println("Insert project path:");
        Scanner scanner = new Scanner(System.in);
//        String path = scanner.nextLine();
        String path = "/home/samuel/ProjetoTese/repos/petclinic-DataJPA/src/main/java";
//        String path = "/home/samuel/ProjetoTese/repos/edition-master/edition-ldod";
        System.out.println("Insert project framework:");
        System.out.println("[1] FénixFramework");
        System.out.println("[2] Spring Data JPA");
//        int option = scanner.nextInt();

        SpoonCollector collector = new SpringDataJPACollector(path);
//        SpoonCollector collector = new FenixFrameworkCollector(path);

//        SpoonCollector collector = null;
//        switch (option) {
//            case 1:
//                collector = new FenixFrameworkCollector(path);
//                break;
//            case 2:
//                collector = new SpringDataJPACollector(path);
//                break;
//            default:
//                System.exit(1);
//        }

        collector.run();
    }
}
