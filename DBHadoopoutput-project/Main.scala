package org.itc.com
import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.SparkConf
import org.apache.log4j.{Level, Logger}

import java.util.Properties


object Main {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.WARN)
    val spark = SparkSession.builder()
      .appName("Data_Analysis")
      .master("local[1]")
      .getOrCreate()
    // connect to database
    val url = "jdbc:postgresql://ec2-3-9-191-104.eu-west-2.compute.amazonaws.com:5432/testdb"
    val username ="consultants"
    val password = "WelcomeItc@2022"
    // JDBC Connection properties
    val connectionProperties = new Properties()
    connectionProperties.put("user",username)
    connectionProperties.put("password",password)

    val churntable = "churn"
    var newDF = spark.read.jdbc(url,churntable,connectionProperties)

    newDF.show()

    // spark SQL
    import spark.implicits._

    newDF.createOrReplaceTempView("custchurntab")
    spark.sql("select * from custchurntab").show(5)

    // churn count
    val df1 = spark.sql(
      """
        |SELECT churn, COUNT(*) AS count
        |FROM custchurntab
        |GROUP BY churn
  """.stripMargin
    )
    df1.show()
   df1.coalesce(1).write.option("header", "true").csv(args(0))
  }
}