# # arg0 = Launcher Choice
# 	# 0 = Launcher
# 	# 1 = Maven Launcher
# 	# 2 = Jar Launcher

# # arg1 = Sources Choice
#  	# 0 = LOCAL
#  	# 1 = GITHUB

# # arg2 = ORM Choice
# 	# 0 = FENIX_FRAMEWORK
# 	# 1 = SPRING_DATA_JPA

# # arg3 = projectName

# # arg4 = path/link

#Fenix Framework
#ldod
p="1 0 0 ldod-maven /home/samuel/ProjetoTese/repos/edition/edition-ldod"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#bw
p="1 0 0 bw-maven /home/samuel/ProjetoTese/repos/blended-workflow/engine/bw-core"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#fenix
p="1 0 0 fenix-maven /home/samuel/ProjetoTese/repos/fenixedu-academic"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#adventure-builder
p="1 0 0 adventure-builder-maven /home/samuel/ProjetoTese/repos/adventure-builder"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"


#Spring Data JPA
#quizzes-tutor
p="0 0 1 quizzes-tutor-launcher /home/samuel/ProjetoTese/repos/quizzes-tutor/backend"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#Axon-Trader
p="1 0 1 axon-trader-maven /home/samuel/ProjetoTese/repos/Axon-trader"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#incubator-wikift
p="2 0 1 incubator-wikift-jar /home/samuel/ProjetoTese/repos/incubator-wikift"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#OpenClinica
p="1 0 1 OpenClinica-maven /home/samuel/ProjetoTese/repos/OpenClinica"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#s2jh4net
p="2 0 1 s2jh4net-jar /home/samuel/ProjetoTese/repos/s2jh4net/aaa_JARS"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#SpringBlog_Adapted
p="2 0 1 SpringBlog_Adapted-jar /home/samuel/ProjetoTese/repos/SpringBlog_adapted"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#spring-cloud-gray
p="2 0 1 spring-cloud-gray-jar /home/samuel/ProjetoTese/repos/spring-cloud-gray"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#spring-framework-petclinic
p="0 0 1 spring-framework-petclinic-launcher /home/samuel/ProjetoTese/repos/spring-framework-petclinic"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#weixin-mp-java
p="2 0 1 weixin-mp-java-jar /home/samuel/ProjetoTese/repos/weixin-mp-java"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#jpabook
p="1 0 1 jpabook-maven /home/samuel/ProjetoTese/repos/jpabook/ch12-springdata-shop"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#bag-database_adapted
p="1 0 1 bag-database_adapted-maven /home/samuel/ProjetoTese/repos/bag-database_adapted"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#cloudunit
p="1 0 1 cloudunit-maven /home/samuel/ProjetoTese/repos/cloudunit"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#xs2a
p="2 0 1 xs2a-jar /home/samuel/ProjetoTese/repos/xs2a"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#keta-custom
p="0 0 1 keta-custom-launcher /home/samuel/ProjetoTese/repos/keta-custom"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#learndemo/soufang
p="1 0 1 learndemo-soufang-maven /home/samuel/ProjetoTese/repos/learndemo/soufang"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#reddit-app
p="1 0 1 reddit-app-maven /home/samuel/ProjetoTese/repos/reddit-app"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#spring-mvc-react
p="1 0 1 spring-mvn-react-maven /home/samuel/ProjetoTese/repos/spring-mvc-react"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#TipDM
p="1 0 1 TipDM-maven /home/samuel/ProjetoTese/repos/TipDM/backend"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#wallride
p="1 0 1 wallride-maven /home/samuel/ProjetoTese/repos/wallride"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#cheybao
p="1 0 1 cheybao-maven /home/samuel/ProjetoTese/repos/cheybao/beimi"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#JavaSpringMvcBlog
p="1 0 1 JavaSpringMvcBlog-maven /home/samuel/ProjetoTese/repos/JavaSpringMvcBlog"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#GTAS
p="1 0 1 gtas-maven /home/samuel/ProjetoTese/repos/GTAS/gtas-parent"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#wish
p="1 0 1 wish-maven /home/samuel/ProjetoTese/repos/wish"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#market-manage
p="0 0 1 market-manage-launcher /home/samuel/ProjetoTese/repos/market-manage"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#egov-smartcity-suite
p="0 0 1 egov-smartcity-suite-launcher /home/samuel/ProjetoTese/repos/egov-smartcity-suite/egov"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"

#myweb
p="1 0 1 myweb-maven /home/samuel/ProjetoTese/repos/myweb/myweb"
echo Collecting project: $p
mvn exec:java -Dexec.args="$p"