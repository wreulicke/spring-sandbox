

create table post (id bigint primary key auto_increment, content varchar(255), date datetime);
insert into post (id, content, date) values (1, 'test', now());