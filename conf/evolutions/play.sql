# --- !Ups

create table hotel (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    city varchar(100) NOT NULL, room varchar(25),
    price INTEGER(11) NOT NULL);

insert into hotel(city, room, price) values('New Delhi', 'Deleuxe', 1000);
insert into hotel(city, room, price) values('Kolkata', 'Deleuxe', 5000);
insert into hotel(city, room, price) values('New Delhi', 'Honeymoon Suite', 20000);
insert into hotel(city, room, price) values('Jaipur', 'Deleuxe', 1000);
insert into hotel(city, room, price) values('Chennai', 'Deleuxe', 1000);
insert into hotel(city, room, price) values('Pune', 'Deleuxe', 1000);

# --- !Downs

drop table 'hotel' if exists;
