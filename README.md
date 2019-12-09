### Specifications  
Database: Postgres version 11.5  
ODBC: PostgreSQL JDBC 4.2 Driver, 42.2.8  
Written entirely in Java using the Swing framework

MLB statistics dataset created by Sean Lahman  
Available [here](http://www.seanlahman.com/baseball-archive/statistics/)  
Ported to Postgres by Michael Altamirano  
GitHub repository [here](https://github.com/michaeljaltamirano/lahman-baseball-database-2016-postgresql)  
This work is licensed under a Creative Commons Attribution-ShareAlike 3.0 Unported License.  
For details see: http://creativecommons.org/licenses/by-sa/3.0/
<br><br>
### Setup Instructions
* In postgres, create a new database called stats
* Import the sql files from the linked GitHub repository into the stats database
* Download the PostgreSQL JDBC driver [here](https://jdbc.postgresql.org/download.html) and add it to the build path
* Compile the program
<br><br>
### Running the program
* The program requires logging in as a postgres user
* Once logged in, you will be taken to the database screen
* There, you can execute queries, view your query history, save a query, or select a specific table to view
