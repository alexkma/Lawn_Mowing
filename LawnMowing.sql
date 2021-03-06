DROP DATABASE IF EXISTS LAWN_MOWING;
CREATE DATABASE LAWN_MOWING;
USE LAWN_MOWING;

DROP TABLE IF EXISTS TECHNICIAN;
CREATE TABLE TECHNICIAN
(Technician_ID INT NOT NULL AUTO_INCREMENT,
FullName VARCHAR(30),
Company_Name VARCHAR(30),
Company_Address VARCHAR(30),
Phone_Number VARCHAR(30),
Cost DOUBLE,
PRIMARY KEY(Technician_ID)
);

DROP TABLE IF EXISTS CUSTOMER;
CREATE TABLE CUSTOMER
(Customer_ID INT NOT NULL AUTO_INCREMENT,
Technician_ID INT,
FullName VARCHAR(30),
Age INT CHECK (Age >= 18),
Birthday DATE,
CreditCard CHAR(16),
Address VARCHAR(30),
Phone_Number VARCHAR(30),
SignUpDate DATE,
PRIMARY KEY(Customer_ID),
FOREIGN KEY (Technician_ID) REFERENCES Technician(Technician_ID)
);

DROP TABLE IF EXISTS SERVICE;
CREATE TABLE SERVICE
(Service_ID INT NOT NULL AUTO_INCREMENT,
Customer_ID INT,
Technician_ID INT,
DateOfService DATE,
PRIMARY KEY(Service_ID),
FOREIGN KEY (Customer_ID) REFERENCES Customer(Customer_ID),
FOREIGN KEY (Technician_ID) REFERENCES Technician(Technician_ID)
);


LOAD DATA LOCAL INFILE 'C:\\Users\\Alex\\Desktop\\Lawn Mowing Project\\technician.txt' INTO TABLE TECHNICIAN;
LOAD DATA LOCAL INFILE 'C:\\Users\\Alex\\Desktop\\Lawn Mowing Project\\customer.txt' INTO TABLE CUSTOMER;
LOAD DATA LOCAL INFILE 'C:\\Users\\Alex\\Desktop\\Lawn Mowing Project\\service.txt' INTO TABLE SERVICE;

