import collectors.FenixFrameworkCollector;
import collectors.SpoonCollector;
import collectors.SpringDataJPACollector;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.*;

import java.util.Scanner;

public class SpoonCallgraph {

    public static void main(String[] args) throws Exception {
        System.out.println("Insert project path:");
        Scanner scanner = new Scanner(System.in);
//        String path = scanner.nextLine();
        String path = "/home/samuel/ProjetoTese/repos/petclinic-DataJPA/src/main/java";
//        String path = "/home/samuel/ProjetoTese/repos/spring-framework-petclinic/src/main/java";
//        String path = "/home/samuel/ProjetoTese/repos/edition-master/edition-ldod";
//        String path = "/home/samuel/ProjetoTese/repos/quizzes-tutor/backend/src/main/java";
//        String path = "/home/samuel/ProjetoTese/repos/HibernateTest/src/main/java";
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
