create table customer (
    id serial  primary key ,
    firstName varchar(255),
    lastName varchar(255),
    birthdate varchar(255)
);
create table customer2 (
    id serial  primary key ,
    firstName varchar(255),
    lastName varchar(255),
    birthdate varchar(255)
);
alter table customer alter column birthdate type date;
alter table customer drop column birthdate;
alter table customer add column birthdate date;

insert into customer values(default, 'mj1', 's1', now());
insert into customer values(default, 'mj2', 's2', now());
insert into customer values(default, 'mj3', 's3', now());
insert into customer values(default, 'mj4', 's4', now());
insert into customer values(default, 'mj5', 's5', now());
insert into customer values(default, 'mj6', 's6', now());
insert into customer values(default, 'mj7', 's7', now());
insert into customer values(default, 'mj8', 's8', now());
insert into customer values(default, 'mj9', 's9', now());
insert into customer values(default, 'mj10', 's10', now());
insert into customer values(default, 'mj11', 's11', now());
insert into customer values(default, 'mj12', 's12', now());
insert into customer values(default, 'mj13', 's13', now());
insert into customer values(default, 'mj14', 's14', now());
insert into customer values(default, 'mj15', 's15', now());
insert into customer values(default, 'mj16', 's16', now());
insert into customer values(default, 'mj17', 's17', now());
insert into customer values(default, 'mj18', 's18', now());
insert into customer values(default, 'mj19', 's19', now());
insert into customer values(default, 'mj20', 's20', now());
insert into customer values(default, 'mj21', 's21', now());
insert into customer values(default, 'mj22', 's22', now());
insert into customer values(default, 'mj23', 's23', now());
insert into customer values(default, 'mj24', 's24', now());
insert into customer values(default, 'mj25', 's25', now());
insert into customer values(default, 'mj26', 's26', now());
insert into customer values(default, 'mj27', 's27', now());
insert into customer values(default, 'mj28', 's28', now());
insert into customer values(default, 'mj29', 's29', now());
insert into customer values(default, 'mj30', 's30', now());

select *
from customer2;

select *
from address;

select *
from customer;