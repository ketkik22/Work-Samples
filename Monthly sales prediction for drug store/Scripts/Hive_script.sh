cd /user/user01/Project

tail -n +2 store.csv > storewithoutheader.csv
mv /user/user01/Project/storewithoutheader.csv /user/user01/Project/Input/storewithoutheader.csv
tail -n +2 train.csv > sales.csv
mv /user/user01/Project/sales.csv /user/user01/Project/Input/sales.csv

hive -f /user/user01/Project/Scripts/Hive.ql

export HADOOP_HOME=/opt/mapr/hadoop/hadoop-0.20.2
export LD_LIBRARY_PATH=$HADOOP_HOME/lib/native/Linux-amd64-64
export CLASSPATH=$HADOOP_HOME/*:$HADOOP_HOME/lib/* 
export HADOOP_CLASSPATH=$CLASSPATH


mkdir /user/user01/Project/classesSales
javac -d classesSales salesMapper.java
javac -d classesSales salesReducer.java
jar -cvf  sales.jar -C classesSales/ .
javac -classpath $CLASSPATH:sales.jar -d classesSales salesDriver.java
jar -uvf sales.jar -C classesSales/ .

mkdir /user/user01/Project/classesStore
javac -d classesStore StoreMapper.java
javac -d classesStore StoreReducer.java
jar -cvf Store.jar -C classesStore/ .
javac -classpath $CLASSPATH:Store.jar -d classesStore StoreDriver.java
jar -uvf Store.jar -C classesStore/ .

rm -rf /user/$USER/Project/Sales/OUT
hadoop jar sales.jar sales.salesDriver /user/hive/warehouse/sales/000000_0  /user/$USER/Project/Sales/OUT 

rm -rf /user/$USER/Project/Store/OUT
hadoop jar Store.jar Store.StoreDriver /user/hive/warehouse/store/000000_0 /user/$USER//Project/Store/OUT

mkdir /user/user01/Project/knnClasses
javac -d knnClasses Dimensions.java
jar -cvf Knn.jar -C knnClasses/ .
javac -classpath $CLASSPATH:Knn.jar -d knnClasses createMatrix.java
jar -uvf Knn.jar -C knnClasses/ .
javac -classpath $CLASSPATH:Knn.jar -d knnClasses Recommend.java
jar -uvf Knn.jar -C knnClasses/ .


mv /user/user01/Project/Store/OUT/part-r-00000 /user/user01/Project/Input/input.txt
java -cp Knn.jar Recommend /user/user01/Project/Input/input.txt $1 $2 $3