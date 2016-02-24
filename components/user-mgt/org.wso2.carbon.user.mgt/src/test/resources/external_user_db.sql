
connect 'jdbc:derby:/home/muthulee/test/employee;user=tintin;password=snowy;create=true';
create table employees (id integer generated always as identity, employee_name varchar(255) not null, email varchar(255) not null, postal varchar(255) not null, profile_name varchar(255) not null, primary key (id)); 
create table roles (id integer generated always as identity, role_name varchar(255) not null, role_description varchar(255) not null, primary key (id)); 
create table employee_roles (id integer generated always as identity, role_id integer not null, emp_id integer not null, 
foreign key (emp_id) references employees(id) on delete cascade, foreign key (role_id) references roles(id) on delete cascade, primary key (id)); 
insert into employees(employee_name, email, postal, profile_name) values ('dimuthul','dimuthul@wso2.com', 'po-8979', 'work'); 
insert into employees(employee_name, email, postal, profile_name) values ('sameera','sameera@wso2.com', 'po-8979', 'work'); 
insert into employees(employee_name, email, postal, profile_name) values ('tika','tika@wso2.com', 'po-8979', 'work'); 
insert into employees(employee_name, email, postal, profile_name) values ('dimuthul','dimuthul@wso2.com', 'po-8979', 'home'); 
insert into employees(employee_name, email, postal, profile_name) values ('sameera','sameera@wso2.com', 'po-8979', 'home'); 
insert into employees(employee_name, email, postal, profile_name) values ('tika','tika@wso2.com', 'po-8979', 'home'); 
insert into roles(role_name, role_description) values ('admin','admin of comapnay system'); 
insert into roles(role_name, role_description) values ('developer','dev in the company'); 
insert into employee_roles(role_id, emp_id) values (1,1); 
insert into employee_roles(role_id, emp_id) values (1,2); 
insert into employee_roles(role_id, emp_id) values (2,2); 
insert into employee_roles(role_id, emp_id) values (2,3);