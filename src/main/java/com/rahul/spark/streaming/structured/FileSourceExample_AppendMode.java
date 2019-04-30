/*
Spark has the ability to treat files as streaming data.
You can point your Spark application to a directory, and as new files are added to that directory,
the data in those files will be considered streaming data.
Now this is extremely useful if you have batch processing systems that output files and store it to a particular directory.
For example, Hadoop MapReduce jobs often output files that are stored to a specific directory at periodic intervals.

The datasets folder contains files that are created from a dataset on London crime.
Each file contains a few thousand crime reports from the city of London.
Within the datasets directory is the droplocation directory.
This droplocation is the directory that my Spark application is going to be listening to.
As we move files from the datasets folder to the droplocation folder, the Spark streaming application will pick them up.
 */

package com.rahul.spark.streaming.structured;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

public class FileSourceExample_AppendMode {
    public static void main(String[] args) throws StreamingQueryException {
        SparkSession sparkSession = SparkSession.builder().master("local[*]").appName("LondonCrimeRecordsReader").getOrCreate();

        sparkSession.sparkContext().setLogLevel("ERROR");

        StructType schema = new StructType(new StructField[]{
                new StructField("lsoa_code", DataTypes.StringType, true, Metadata.empty()),
                new StructField("borough", DataTypes.StringType, true, Metadata.empty()),
                new StructField("major_category", DataTypes.StringType, true, Metadata.empty()),
                new StructField("minor_category", DataTypes.StringType, true, Metadata.empty()),
                new StructField("value", DataTypes.StringType, true, Metadata.empty()),
                new StructField("year", DataTypes.StringType, true, Metadata.empty()),
                new StructField("month", DataTypes.StringType, true, Metadata.empty())
        });

        Dataset<Row> fileStreamDF = sparkSession.readStream()
                .option("header", "false")
                .schema(schema)
                .csv("/home/rahulbhatia/Rahul_Bhatia/intellij_workspace/SparkStructuredStreaming/datasets/droplocation");

        fileStreamDF.writeStream()
                .outputMode("append")
                .format("console")
                .option("truncate", "false")
                .option("numRows", 30)
                .start()
                .awaitTermination();
    }
}
